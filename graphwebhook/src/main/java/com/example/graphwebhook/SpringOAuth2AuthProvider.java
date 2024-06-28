// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

/**
 * An implementation of IAuthenticationProvider that uses Spring's OAuth2AuthorizedClient to get
 * access tokens
 */
public class SpringOAuth2AuthProvider implements AuthenticationProvider {

    private OAuth2AuthorizedClient oauthClient;

    public SpringOAuth2AuthProvider(@NonNull OAuth2AuthorizedClient oauthClient) {
        this.oauthClient = Objects.requireNonNull(oauthClient);
    }

    @Override
    public void authenticateRequest(RequestInformation request,
            Map<String, Object> additionalAuthenticationContext) {

        try {
            if (request.getUri().getHost().equalsIgnoreCase("graph.microsoft.com")) {
                final String accessToken = oauthClient.getAccessToken().getTokenValue();
                request.headers.add("Authorization", "Bearer " + accessToken);
            }
        } catch (IllegalStateException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
