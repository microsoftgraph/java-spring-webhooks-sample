// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

/**
 * An implementation of IAuthenticationProvider that uses Spring's OAuth2AuthorizedClient to get
 * access tokens
 */
public class SpringOAuth2AuthProvider implements IAuthenticationProvider {

    private OAuth2AuthorizedClient oauthClient;

    public SpringOAuth2AuthProvider(@NonNull OAuth2AuthorizedClient oauthClient) {
        this.oauthClient = Objects.requireNonNull(oauthClient);
    }

    @Override
    @Nonnull
    public CompletableFuture<String> getAuthorizationTokenAsync(@Nonnull URL requestUrl) {
        return Utilities.ensureNonNull(
                CompletableFuture.completedFuture(oauthClient.getAccessToken().getTokenValue()));
    }
}
