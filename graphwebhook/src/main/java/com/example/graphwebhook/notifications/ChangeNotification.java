// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.example.graphwebhook.notifications;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.microsoft.graph.models.Entity;
import com.microsoft.kiota.serialization.ParseNode;

public class ChangeNotification extends Entity {
    public ChangeNotification() {
        super();
    }

    @jakarta.annotation.Nonnull
    public static ChangeNotification createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new ChangeNotification();
    }

    @jakarta.annotation.Nonnull
    public Map<String, java.util.function.Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>(
                        super.getFieldDeserializers());

        deserializerMap.put("changeType", (n) -> {
            this.setChangeType(n.getStringValue());
        });
        deserializerMap.put("clientState", (n) -> {
            this.setClientState(n.getStringValue());
        });
        deserializerMap.put("encryptedContent", (n) -> {
            this.setEncryptedContent(n.getObjectValue(ChangeNotificationEncryptedContent::createFromDiscriminatorValue));
        });
        deserializerMap.put("lifecycleEvent", (n) -> {
            this.setLifecycleEvent(n.getStringValue());
        });
        deserializerMap.put("resource", (n) -> {
            this.setResource(n.getStringValue());
        });
        deserializerMap.put("resourceData", (n) -> {
            this.setResourceData(n.getObjectValue(ResourceData::createFromDiscriminatorValue));
        });
        deserializerMap.put("subscriptionExpirationDateTime", (n) -> {
            this.setSubscriptionExpirationDateTime(n.getOffsetDateTimeValue());
        });
        deserializerMap.put("subscriptionId", (n) -> {
            this.setSubscriptionId(n.getStringValue());
        });
        deserializerMap.put("tenantId", (n) -> {
            this.setTenantId(n.getStringValue());
        });

        return deserializerMap;
    }

    public String getChangeType() {
        return this.backingStore.get("changeType");
    }

    public String getClientState() {
        return this.backingStore.get("clientState");
    }

    public ChangeNotificationEncryptedContent getEncryptedContent() {
        return this.backingStore.get("encryptedContent");
    }

    public String getLifecycleEvent() {
        return this.backingStore.get("lifecycleEvent");
    }

    public String getResource() {
        return this.backingStore.get("resource");
    }

    public ResourceData getResourceData() {
        return this.backingStore.get("resourceData");
    }

    public OffsetDateTime getSubscriptionExpirationDateTime() {
        return this.backingStore.get("subscriptionExpirationDateTime");
    }

    public String getSubscriptionId() {
        return this.backingStore.get("subscriptionId");
    }

    public String getTenantId() {
        return this.backingStore.get("tenantId");
    }

    public void setChangeType(final String value) {
        this.backingStore.set("changeType", value);
    }

    public void setClientState(final String value) {
        this.backingStore.set("clientState", value);
    }

    public void setEncryptedContent(final ChangeNotificationEncryptedContent value) {
        this.backingStore.set("encryptedContent", value);
    }

    public void setLifecycleEvent(final String value) {
        this.backingStore.set("lifecycleEvent", value);
    }

    public void setResource(final String value) {
        this.backingStore.set("resource", value);
    }

    public void setResourceData(final ResourceData value) {
        this.backingStore.set("resourceData", value);
    }

    public void setSubscriptionExpirationDateTime(final OffsetDateTime value) {
        this.backingStore.set("subscriptionExpirationDateTime", value);
    }

    public void setSubscriptionId(final String value) {
        this.backingStore.set("subscriptionId", value);
    }

    public void setTenantId(final String value) {
        this.backingStore.set("tenantId", value);
    }
}
