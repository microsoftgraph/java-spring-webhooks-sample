// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Jwts;

public class TokenHelper {

    private static JwkKeyResolver keyResolver;
    private static final Logger log = LoggerFactory.getLogger(TokenHelper.class);

    private TokenHelper() {
        throw new IllegalStateException("Static class");
    }

    public static boolean isValidationTokenValid(final String[] validAudiences,
                                                 final String[] validTenantIds,
                                                 final String serializedToken,
                                                 final String keyDiscoveryUrl) {
        try {
            if (keyResolver == null) {
                keyResolver = new JwkKeyResolver(keyDiscoveryUrl);
            }

            var token = Jwts
                .parserBuilder()
                .setSigningKeyResolver(keyResolver)
                .build()
                .parseClaimsJws(serializedToken);

            var body = token.getBody();
            var audience = body.getAudience();
            var issuer = body.getIssuer();

            boolean isAudienceValid = false;
            for (final String validAudience : validAudiences) {
                isAudienceValid = isAudienceValid || validAudience.equals(audience);
            }

            boolean isIssuerValid = false;
            for (final String validTenantId : validTenantIds) {
                isIssuerValid = isIssuerValid || issuer.endsWith(validTenantId + "/");
            }

            return isAudienceValid && isIssuerValid;
        } catch (final Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public static boolean areValidationTokensValid(final String[] validAudiences,
                                                   final String[] validTenantIds,
                                                   final List<String> serializedTokens,
                                                   final String keyDiscoveryUrl) {
        for (final String serializedToken : serializedTokens) {
            if (!isValidationTokenValid(validAudiences, validTenantIds, serializedToken, keyDiscoveryUrl)) {
                return false;
            }
        }

        return true;
    }
}
