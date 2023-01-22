// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Objects;

import com.microsoft.graph.models.ChangeType;
import com.microsoft.graph.models.Subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WatchController {

    private static final String CREATE_SUBSCRIPTION_ERROR = "Error creating subscription";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_LOGOUT = "redirect:/logout";
    private static final String APP_ONLY = "APP-ONLY";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubscriptionStoreService subscriptionStore;

    @Autowired
    private CertificateStoreService certificateStore;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${notifications.host}")
    private String notificationHost;

    /**
     * The delegated auth page of the app. This will subscribe for the authenticated user's
     * inbox on Exchange Online
     * @param model the model provided by Spring
     * @param authentication authentication information for the request
     * @param redirectAttributes redirect attributes provided by Spring
     * @param oauthClient a delegated auth OAuth2 client for the authenticated user
     * @return the name of the template used to render the response
     */
    @GetMapping("/delegated")
    public CompletableFuture<String> delegated(Model model, OAuth2AuthenticationToken authentication,
            RedirectAttributes redirectAttributes,
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient oauthClient) {

        final var graphClient = GraphClientHelper.getGraphClient(Objects.requireNonNull(oauthClient));

        // Get the authenticated user's info
        final var userFuture = graphClient.me().buildRequest()
                .select("displayName,mail,userPrincipalName").getAsync();

        // Create the subscription
        final var subscriptionRequest = new Subscription();
        subscriptionRequest.changeType = ChangeType.CREATED.toString();
        subscriptionRequest.notificationUrl = notificationHost + "/listen";
        subscriptionRequest.resource = "me/mailfolders/inbox/messages";
        subscriptionRequest.clientState = UUID.randomUUID().toString();
        subscriptionRequest.includeResourceData = false;
        subscriptionRequest.expirationDateTime = OffsetDateTime.now().plusHours(1);

        final var subscriptionFuture =
                graphClient.subscriptions().buildRequest().postAsync(subscriptionRequest);

        return userFuture.thenCombine(subscriptionFuture, (user, subscription) -> {
            log.info("Created subscription {} for user {}", subscription.id, user.displayName);

            // Save the authorized client so we can use it later from the notification controller
            authorizedClientService.saveAuthorizedClient(oauthClient, authentication);

            // Add information to the model
            model.addAttribute("user", user);
            model.addAttribute("subscriptionId", subscription.id);

            final var subscriptionJson =
                    graphClient.getHttpProvider().getSerializer().serializeObject(subscription);
            model.addAttribute("subscription", subscriptionJson);

            // Add record in subscription store
            subscriptionStore.addSubscription(subscription,
                Objects.requireNonNull(authentication.getName()));

            model.addAttribute("success", "Subscription created.");

            return "delegated";
        }).exceptionally(e -> {
            log.error(CREATE_SUBSCRIPTION_ERROR, e);
            redirectAttributes.addFlashAttribute("error", CREATE_SUBSCRIPTION_ERROR);
            redirectAttributes.addFlashAttribute("debug", e.getMessage());
            return REDIRECT_HOME;
        });
    }


    /** The app-only auth page of the app. This will subscribe for notifications on all new
     * Teams channel messages
     * @param model the model provided by Spring
     * @param redirectAttributes redirect attributes provided by Spring
     * @param oauthClient an app-only auth OAuth2 client
     * @return the name of the template used to render the response
     */
    @GetMapping("/apponly")
    public CompletableFuture<String> apponly(Model model, RedirectAttributes redirectAttributes,
            @RegisteredOAuth2AuthorizedClient("apponly") OAuth2AuthorizedClient oauthClient) {

        final var graphClient = GraphClientHelper.getGraphClient(Objects.requireNonNull(oauthClient));

        // Apps are only allowed one subscription to the /teams/getAllMessages resource
        // If we already had one, delete it so we can create a new one
        final var existingSubscriptions = subscriptionStore.getSubscriptionsForUser(APP_ONLY);
        for (final var sub : existingSubscriptions) {
            graphClient.subscriptions(sub.subscriptionId).buildRequest().delete();
        }

        // Create the subscription
        final var subscriptionRequest = new Subscription();
        subscriptionRequest.changeType = ChangeType.CREATED.toString();
        subscriptionRequest.notificationUrl = notificationHost + "/listen";
        subscriptionRequest.resource = "/teams/getAllMessages";
        subscriptionRequest.clientState = UUID.randomUUID().toString();
        subscriptionRequest.includeResourceData = true;
        subscriptionRequest.expirationDateTime = OffsetDateTime.now().plusHours(1);
        subscriptionRequest.encryptionCertificate = certificateStore.getBase64EncodedCertificate();
        subscriptionRequest.encryptionCertificateId = certificateStore.getCertificateId();

        return graphClient.subscriptions().buildRequest().postAsync(subscriptionRequest)
                .thenApply(subscription -> {
                    log.info("Created subscription {} for all Teams messages", subscription.id);

                    // Add information to the model
                    model.addAttribute("subscriptionId", subscription.id);

                    var subscriptionJson = graphClient.getHttpProvider().getSerializer()
                            .serializeObject(subscription);
                    model.addAttribute("subscription", subscriptionJson);

                    // Add record in subscription store
                    subscriptionStore.addSubscription(subscription, APP_ONLY);

                    model.addAttribute("success", "Subscription created.");
                    return "apponly";
                }).exceptionally(e -> {
                    log.error(CREATE_SUBSCRIPTION_ERROR, e);
                    redirectAttributes.addFlashAttribute("error", CREATE_SUBSCRIPTION_ERROR);
                    redirectAttributes.addFlashAttribute("debug", e.getMessage());
                    return REDIRECT_HOME;
                });
    }


    /**
     * Deletes a subscription and logs the user out
     * @param subscriptionId the subscription ID to delete
     * @param oauthClient a delegated auth OAuth2 client for the authenticated user
     * @return a redirect to the logout page
     */
    @GetMapping("/unsubscribe")
    public CompletableFuture<String> unsubscribe(
            @RequestParam(value = "subscriptionId") final String subscriptionId,
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient oauthClient) {

        final var graphClient = GraphClientHelper.getGraphClient(Objects.requireNonNull(oauthClient));

        return graphClient.subscriptions(subscriptionId).buildRequest().deleteAsync()
                .thenApply(sub -> {
                    // Remove subscription from store
                    subscriptionStore.deleteSubscription(Objects.requireNonNull(subscriptionId));

                    // Logout user
                    return REDIRECT_LOGOUT;
                });
    }


    /**
     * Deletes an app-only subscription
     * @param subscriptionId the subscription ID to delete
     * @param oauthClient an app-only auth OAuth2 client
     * @return a redirect to the home page
     */
    @GetMapping("/unsubscribeapponly")
    public CompletableFuture<String> unsubscribeapponly(
            @RequestParam(value = "subscriptionId") final String subscriptionId,
            @RegisteredOAuth2AuthorizedClient("apponly") OAuth2AuthorizedClient oauthClient) {

        final var graphClient = GraphClientHelper.getGraphClient(Objects.requireNonNull(oauthClient));

        return graphClient.subscriptions(subscriptionId).buildRequest().deleteAsync()
                .thenApply(sub -> {
                    // Remove subscription from store
                    subscriptionStore.deleteSubscription(Objects.requireNonNull(subscriptionId));

                    // Logout user
                    return REDIRECT_HOME;
                });
    }
}
