// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.example.graphwebhook;

import javax.annotation.Nonnull;

public class Utilities {

    @Nonnull
    public static <T> T ensureNonNull(T object) {
        if (object != null) {
            return object;
        }

        throw new NullPointerException();
    }
}
