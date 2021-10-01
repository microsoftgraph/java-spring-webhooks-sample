// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.concurrent.CompletableFuture;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.ChangeNotificationCollection;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.serializer.DefaultSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Value("${azure.activedirectory.client-id}")
    private String clientId;

    @Value("${azure.activedirectory.tenant-id}")
    private String tenantId;

    @Value("${azure.activedirectory.keydiscoveryurl")
    private String keyDiscoveryUrl;

    @Autowired
    public ListenController(SocketIOServer socketIOServer) {
        socketIONamespace = socketIOServer.addNamespace("/emitNotification");
        socketIONamespace.addEventListener("create_room", String.class, new DataListener<String>(){
            @Override
            public void onData(SocketIOClient client, String roomName, AckRequest ackSender) throws Exception {
                log.info("Client {} creating room for subscription {}", client.getSessionId(), roomName);
                client.joinRoom(roomName);
            }
        });
    }

    @PostMapping(value = "/listen", headers = { "content-type=text/plain" })
    @ResponseBody
    public ResponseEntity<String> handleValidation(
        @RequestParam(value = "validationToken") final String validationToken) {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(validationToken);
    }

    @PostMapping("/listen")
    public CompletableFuture<ResponseEntity<String>> handleNotification(
        @RequestBody final String jsonPayload) {
        final var serializer = new DefaultSerializer(new DefaultLogger());
        final var notifications = serializer
            .deserializeObject(jsonPayload, ChangeNotificationCollection.class);

        // Check for validation tokens
        boolean areTokensValid = true;
        if (notifications.validationTokens != null &&
            !notifications.validationTokens.isEmpty()) {
            areTokensValid = TokenHelper.areValidationTokensValid(new String[] {clientId},
                new String[] {tenantId}, notifications.validationTokens, keyDiscoveryUrl);
        }

        if (areTokensValid) {
            for(ChangeNotification notification : notifications.value) {
                // Look up subscription in store
                var subscription = subscriptionStore
                    .getSubscription(notification.subscriptionId.toString());

                if (subscription != null && subscription.clientState.equals(notification.clientState)) {
                    if (notification.encryptedContent == null) {
                        processNewMessageNotification(notification, subscription);
                    } else {
                        processNewChannelMessageNotification(notification, subscription);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(ResponseEntity.accepted().body(""));
    }

    private void processNewMessageNotification(final ChangeNotification notification, final SubscriptionRecord subscription) {
        final var oauthClient = authorizedClientService
            .loadAuthorizedClient("graph", subscription.userId);

        final var graphClient = GraphClientHelper.getGraphClient(oauthClient);

        graphClient
            .customRequest("/"+notification.resource, Message.class)
            .buildRequest()
            .getAsync()
            .thenAccept(message -> socketIONamespace
                .getRoomOperations(subscription.subscriptionId)
                .sendEvent("notificationReceived", new NewMessageNotification(message)));
    }

    private void processNewChannelMessageNotification(final ChangeNotification notification, final SubscriptionRecord subscription) {
        final var decryptedKey = certificateStore.getEncryptionKey(notification.encryptedContent.dataKey);

        if (certificateStore.isDataSignatureValid(decryptedKey, notification.encryptedContent.data, notification.encryptedContent.dataSignature)) {
            final var decryptedData = certificateStore.getDecryptedData(decryptedKey, notification.encryptedContent.data);
            final var serializer = new DefaultSerializer(new DefaultLogger());
            final var chatMessage = serializer.deserializeObject(decryptedData, ChatMessage.class);
            socketIONamespace
                .getRoomOperations(subscription.subscriptionId)
                .sendEvent("notificationReceived", new NewChatMessageNotification(chatMessage));
        }
    }
}
