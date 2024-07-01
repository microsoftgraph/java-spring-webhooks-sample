// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.example.graphwebhook.notifications.ChangeNotification;
import com.example.graphwebhook.notifications.ChangeNotificationCollection;
import com.example.graphwebhook.notifications.ChangeNotificationEncryptedContent;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.Message;
import com.microsoft.kiota.HttpMethod;
import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.serialization.KiotaJsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListenController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubscriptionStoreService subscriptionStore;

    @Autowired
    private CertificateStoreService certificateStore;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private SocketIONamespace socketIONamespace;

    @Value("${spring.cloud.azure.active-directory.credential.client-id}")
    private String clientId;

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    @Value("${spring.cloud.azure.active-directory.keydiscoveryurl}")
    private String keyDiscoveryUrl;

    public ListenController(SocketIOServer socketIOServer) {
        // Set up a SocketIO server namespace to broadcast
        // incoming notifications to clients (browser)
        socketIONamespace = socketIOServer.addNamespace("/emitNotification");
        socketIONamespace.addEventListener("create_room", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String roomName, AckRequest ackSender)
                    throws Exception {
                log.info("Client {} creating room for subscription {}", client.getSessionId(),
                        roomName);
                client.joinRoom(roomName);
            }
        });
    }


    /**
     * <p>
     * This method handles the initial
     * <a href="https://docs.microsoft.com/graph/webhooks#notification-endpoint-validation">endpoint
     * validation request sent</a> by Microsoft Graph when the subscription is created.
     *
     * @param validationToken A validation token provided as a query parameter
     * @return a 200 OK response with the validationToken in the text/plain body
     */
    @PostMapping(value = "/listen", headers = {"content-type=text/plain"})
    @ResponseBody
    public ResponseEntity<String> handleValidation(
            @RequestParam(value = "validationToken") final String validationToken) {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(validationToken);
    }


    /**
     * This method receives and processes incoming notifications from Microsoft Graph
     *
     * @param jsonPayload the JSON body of the request
     * @return A 202 Accepted response
     */
    @PostMapping("/listen")
    public ResponseEntity<String> handleNotification(
            @RequestBody @NonNull final String jsonPayload) {
        try {
            // Deserialize the JSON body into a ChangeNotificationCollection
            final ChangeNotificationCollection notifications =
                    KiotaJsonSerialization.deserialize(jsonPayload,
                        ChangeNotificationCollection::createFromDiscriminatorValue);

            if (notifications == null) {
                return ResponseEntity.accepted().body("");
            }

            // Check for validation tokens
            boolean areTokensValid = true;
            final List<String> validationTokens = notifications.getValidationTokens();
            if (validationTokens != null && validationTokens.isEmpty()) {
                areTokensValid = TokenHelper.areValidationTokensValid(new String[] {clientId},
                        new String[] {tenantId}, Objects.requireNonNull(validationTokens),
                        Objects.requireNonNull(keyDiscoveryUrl));
            }

            if (areTokensValid) {
                for (ChangeNotification notification : Objects.requireNonNull(notifications.getValue())) {
                    // Look up subscription in store
                    var subscription = subscriptionStore.getSubscription(
                            Objects.requireNonNull(notification.getSubscriptionId()));

                    // Only process if we know about this subscription AND
                    // the client state in the notification matches
                    if (subscription != null
                            && subscription.clientState.equals(notification.getClientState())) {
                        if (notification.getEncryptedContent() == null) {
                            // No encrypted content, this is a new message notification
                            // without resource data
                            processNewMessageNotification(notification, subscription);
                        } else {
                            // With encrypted content, this is a new channel message
                            // notification with encrypted resource data
                            processNewChannelMessageNotification(notification, subscription);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.accepted().body("");
    }


    /**
     * Processes a new message notification by getting the message from Microsoft Graph
     *
     * @param notification the new message notification
     * @param subscription the matching subscription record
     */
    private void processNewMessageNotification(@NonNull final ChangeNotification notification,
            @NonNull final SubscriptionRecord subscription) {
        // Get the authorized OAuth2 client for the relevant user
        // This allows the service to access the user's mailbox with delegated auth
        final var oauthClient =
                authorizedClientService.loadAuthorizedClient("graph", subscription.userId);

        final var graphClient =
                GraphClientHelper.getGraphClient(Objects.requireNonNull(oauthClient));

        // The notification contains the relative URL to the message
        // so use the customRequest method instead of the fluent API
        // Once message has been retrieved, send the information via SocketIO
        // to subscribed clients

        final RequestInformation request = new RequestInformation();
        request.httpMethod = HttpMethod.GET;
        URI messageUri;
        try {
            messageUri = new URI(
                graphClient.getRequestAdapter().getBaseUrl() + "/" + notification.getResource());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        request.setUri(messageUri);
        final Message message = graphClient.getRequestAdapter().send(request, null,
                Message::createFromDiscriminatorValue);
        if (message != null)
            socketIONamespace.getRoomOperations(subscription.subscriptionId)
                    .sendEvent("notificationReceived", new NewMessageNotification(message));
    }


    /**
     * Processes a new channel message notification by decrypting the included resource data
     *
     * @param notification the new channel message notification
     * @param subscription the matching subscription record
     */
    private void processNewChannelMessageNotification(
            @NonNull final ChangeNotification notification,
            @NonNull final SubscriptionRecord subscription) {
        // Decrypt the encrypted key from the notification
        final ChangeNotificationEncryptedContent encryptedContent =
                Objects.requireNonNull(notification.getEncryptedContent());
        final var decryptedKey = Objects.requireNonNull(certificateStore
                .getEncryptionKey(encryptedContent.getDataKey()));

        // Validate the signature
        final String data = encryptedContent.getData();
        if (certificateStore.isDataSignatureValid(decryptedKey,
                data,
                encryptedContent.getDataSignature())) {
            // Decrypt the data using the decrypted key
            final var decryptedData = certificateStore.getDecryptedData(decryptedKey, data);

            // Deserialize the decrypted JSON into a ChatMessage
            ChatMessage chatMessage;
            try {
                chatMessage = KiotaJsonSerialization.deserialize(decryptedData,
                        ChatMessage::createFromDiscriminatorValue);
                // Send the information to subscribed clients
                socketIONamespace.getRoomOperations(subscription.subscriptionId)
                    .sendEvent("notificationReceived", new NewChatMessageNotification(chatMessage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
