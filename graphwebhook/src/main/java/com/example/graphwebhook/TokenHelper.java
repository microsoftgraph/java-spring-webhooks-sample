// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Jwts;

/**
 * Helper class for validating the JSON web token included
 * in Microsoft Graph change notifications with encrypted content
 */
public class TokenHelper {

    private static JwkKeyResolver keyResolver;
    private static final Logger log = LoggerFactory.getLogger(TokenHelper.class);

    private TokenHelper() {
        throw new IllegalStateException("Static class");
    }


    /**
     * Validate a JSON web token.
     * @param validAudiences list of valid audiences - in this case, the app's client ID
     * @param validTenantIds list of valid tenant IDs
     * @param serializedToken the raw token
     * @param keyDiscoveryUrl the JWKS endpoint to use to retrieve signing keys
     * @return true if the token is valid, false if not
     */
    public static boolean isValidationTokenValid(final String[] validAudiences,
            final String[] validTenantIds, final String serializedToken,
            final String keyDiscoveryUrl) {
        try {
            if (keyResolver == null) {
                keyResolver = new JwkKeyResolver(keyDiscoveryUrl);
            }

            // Parse the serialized token
            // As part of this process, the signature is validated
            // This throws if the signature is invalid
            var token = Jwts.parserBuilder().setSigningKeyResolver(keyResolver).build()
                    .parseClaimsJws(serializedToken);

            var body = token.getBody();
            var audience = body.getAudience();
            var issuer = body.getIssuer();

            // The audience should match the app's client ID
            boolean isAudienceValid = false;
            for (final String validAudience : validAudiences) {
                isAudienceValid = isAudienceValid || validAudience.equals(audience);
            }

            // Microsoft identity tokens will have an issuer like
            // that contains the tenant ID
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


    /**
     * Validates a list of JSON web tokens
     * @param validAudiences list of valid audiences - in this case, the app's client ID
     * @param validTenantIds list of valid tenant IDs
     * @param serializedToken the raw token
     * @param keyDiscoveryUrl the JWKS endpoint to use to retrieve signing keys
     * @return true if all tokens are valid, false if one or more are invalid
     */
    public static boolean areValidationTokensValid(final String[] validAudiences,
            final String[] validTenantIds, final List<String> serializedTokens,
            final String keyDiscoveryUrl) {
        for (final String serializedToken : serializedTokens) {
            if (!isValidationTokenValid(validAudiences, validTenantIds, serializedToken,
                    keyDiscoveryUrl)) {
                return false;
            }
        }

        return true;
    }
}
