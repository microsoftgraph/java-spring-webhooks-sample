// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.requests.GraphServiceClient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import okhttp3.Request;

public class GraphClientHelper {

    private GraphClientHelper() {
        throw new IllegalStateException("Static class");
    }

    public static GraphServiceClient<Request> getGraphClient(OAuth2AuthorizedClient oauthClient) {
        final var authProvider = new SpringOAuth2AuthProvider(oauthClient);

        final var logger = new DefaultLogger();
        logger.setLoggingLevel(LoggerLevel.ERROR);

        return GraphServiceClient
            .builder()
            .authenticationProvider(authProvider)
            .logger(logger)
            .buildClient();
    }
}
