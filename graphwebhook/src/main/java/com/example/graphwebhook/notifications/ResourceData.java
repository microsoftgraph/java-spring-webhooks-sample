// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.example.graphwebhook.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.microsoft.kiota.serialization.AdditionalDataHolder;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.store.BackedModel;
import com.microsoft.kiota.store.BackingStore;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

public class ResourceData implements AdditionalDataHolder, BackedModel, Parsable {
    @jakarta.annotation.Nonnull
    protected BackingStore backingStore;

    public ResourceData() {
        this.backingStore = BackingStoreFactorySingleton.instance.createBackingStore();
        this.setAdditionalData(new HashMap<>());
    }

    @jakarta.annotation.Nonnull
    public static ResourceData createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new ResourceData();
    }

    @jakarta.annotation.Nonnull
    public Map<String, java.util.function.Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>();

        return deserializerMap;
    }

    public Map<String, Object> getAdditionalData() {
        return this.backingStore.get("additionalData");
    }

    public BackingStore getBackingStore() {
        return this.backingStore;
    }

    public void setAdditionalData(@jakarta.annotation.Nullable final Map<String, Object> value) {
        this.backingStore.set("additionalData", value);
    }

    @Override
    public void serialize(SerializationWriter writer) {
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
}
