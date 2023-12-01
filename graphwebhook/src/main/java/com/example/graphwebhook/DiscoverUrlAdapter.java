// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.Objects;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import io.jsonwebtoken.JweHeader;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.LocatorAdapter;
/**
 * Custom implementation of SigningKeyResolverAdapter that retrieves the signing key from the
 * Microsoft identity platform's JWKS endpoint
 */
public class DiscoverUrlAdapter extends LocatorAdapter<Key> {

    private final JwkProvider keyStore;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DiscoverUrlAdapter(@NonNull final String keyDiscoveryUrl)
            throws URISyntaxException, MalformedURLException {
        this.keyStore =
                new UrlJwkProvider(new URI(Objects.requireNonNull(keyDiscoveryUrl)).toURL());
    }

    @Override
    protected Key locate(JwsHeader header) {
        Objects.requireNonNull(header);
        try {
            var keyId = header.getKeyId();
            var publicKey = keyStore.get(keyId);
            return publicKey.getPublicKey();
        } catch (final Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    protected Key locate(JweHeader header) {
        return null;
    }
}
