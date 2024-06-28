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

public class ChangeNotificationEncryptedContent implements AdditionalDataHolder, BackedModel, Parsable {
    @jakarta.annotation.Nonnull
    protected BackingStore backingStore;

    public ChangeNotificationEncryptedContent() {
        this.backingStore = BackingStoreFactorySingleton.instance.createBackingStore();
        this.setAdditionalData(new HashMap<>());
    }

    @jakarta.annotation.Nonnull
    public static ChangeNotificationEncryptedContent createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new ChangeNotificationEncryptedContent();
    }

    @jakarta.annotation.Nonnull
    public Map<String, java.util.function.Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>();

        deserializerMap.put("data", (n) -> {
            this.setData(n.getStringValue());
        });
        deserializerMap.put("dataKey", (n) -> {
            this.setDataKey(n.getStringValue());
        });
        deserializerMap.put("dataSignature", (n) -> {
            this.setDataSignature(n.getStringValue());
        });
        deserializerMap.put("encryptionCertificateId", (n) -> {
            this.setEncryptionCertificateId(n.getStringValue());
        });
        deserializerMap.put("encryptionCertificateThumbprint", (n) -> {
            this.setEncryptionCertificateThumbprint(n.getStringValue());
        });

        return deserializerMap;
    }

    public Map<String, Object> getAdditionalData() {
        return this.backingStore.get("additionalData");
    }

    public BackingStore getBackingStore() {
        return this.backingStore;
    }

    public String getData() {
        return this.backingStore.get("data");
    }

    public String getDataKey() {
        return this.backingStore.get("dataKey");
    }

    public String getDataSignature() {
        return this.backingStore.get("dataSignature");
    }

    public String getEncryptionCertificateId() {
        return this.backingStore.get("encryptionCertificateId");
    }

    public String getEncryptionCertificateThumbprint() {
        return this.backingStore.get("encryptionCertificateThumbprint");
    }

    public void setAdditionalData(@jakarta.annotation.Nullable final Map<String, Object> value) {
        this.backingStore.set("additionalData", value);
    }

    public void setData(final String value) {
        this.backingStore.set("data", value);
    }

    public void setDataKey(final String value) {
        this.backingStore.set("dataKey", value);
    }

    public void setDataSignature(final String value) {
        this.backingStore.set("dataSignature", value);
    }

    public void setEncryptionCertificateId(final String value) {
        this.backingStore.set("encryptionCertificateId", value);
    }

    public void setEncryptionCertificateThumbprint(final String value) {
        this.backingStore.set("encryptionCertificateThumbprint", value);
    }

    @Override
    public void serialize(SerializationWriter writer) {
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
}
