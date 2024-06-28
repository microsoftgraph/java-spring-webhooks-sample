// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.example.graphwebhook.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.microsoft.kiota.serialization.AdditionalDataHolder;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.store.BackedModel;
import com.microsoft.kiota.store.BackingStore;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

public class ChangeNotificationCollection implements AdditionalDataHolder, BackedModel, Parsable {
    @jakarta.annotation.Nonnull
    protected BackingStore backingStore;

    public ChangeNotificationCollection() {
        this.backingStore = BackingStoreFactorySingleton.instance.createBackingStore();
        this.setAdditionalData(new HashMap<>());
    }

    @jakarta.annotation.Nonnull
    public static ChangeNotificationCollection createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new ChangeNotificationCollection();
    }

    @jakarta.annotation.Nonnull
    public Map<String, java.util.function.Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>();

        deserializerMap.put("validationTokens", (n) -> {
            this.setValidationTokens(n.getCollectionOfPrimitiveValues(String.class));
        });
        deserializerMap.put("value", (n) -> {
            this.setValue(n.getCollectionOfObjectValues(ChangeNotification::createFromDiscriminatorValue));
        });

        return deserializerMap;
    }


    public Map<String, Object> getAdditionalData() {
        return this.backingStore.get("additionalData");
    }

    public BackingStore getBackingStore() {
        return this.backingStore;
    }

    public List<String> getValidationTokens() {
        return this.backingStore.get("validationTokens");
    }

    public List<ChangeNotification> getValue() {
        return this.backingStore.get("value");
    }

    public void setAdditionalData(@jakarta.annotation.Nullable final Map<String, Object> value) {
        this.backingStore.set("additionalData", value);
    }

    public void setValidationTokens(final List<String> value) {
        this.backingStore.set("validationTokens", value);
    }

    public void setValue(final List<ChangeNotification> value) {
        this.backingStore.set("value", value);
    }

    @Override
    public void serialize(SerializationWriter writer) {
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
}
