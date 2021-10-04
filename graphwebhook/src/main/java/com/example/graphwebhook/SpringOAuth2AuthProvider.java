// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.microsoft.graph.authentication.IAuthenticationProvider;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

/**
 * An implementation of IAuthenticationProvider that uses Spring's
 * OAuth2AuthorizedClient to get access tokens
 */
public class SpringOAuth2AuthProvider implements IAuthenticationProvider {

    private OAuth2AuthorizedClient oauthClient;

    public SpringOAuth2AuthProvider(OAuth2AuthorizedClient oauthClient) {
        this.oauthClient = oauthClient;
    }

    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
        return CompletableFuture.completedFuture(oauthClient.getAccessToken().getTokenValue());
    }

}
