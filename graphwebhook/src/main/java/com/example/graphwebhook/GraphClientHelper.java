// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.Objects;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import okhttp3.Request;

public class GraphClientHelper {

    /**
     * private constructor to hide the implicit public one
     */
    private GraphClientHelper() {
        throw new IllegalStateException("Static class");
    }


    /**
     * @param oauthClient the authorized OAuth2 client to authenticate Graph requests with
     * @return A Graph client object that uses the provided OAuth2 client for access tokens
     */
    public static GraphServiceClient<Request> getGraphClient(
            @NonNull final OAuth2AuthorizedClient oauthClient) {
        Objects.requireNonNull(oauthClient);
        final var authProvider = new SpringOAuth2AuthProvider(oauthClient);

        final var logger = new DefaultLogger();
        logger.setLoggingLevel(LoggerLevel.ERROR);

        return GraphServiceClient.builder().authenticationProvider(authProvider).logger(logger)
                .buildClient();
    }
}
