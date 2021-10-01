// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

public class SubscriptionRecord {

    public final String subscriptionId;
    public final String userId;
    public final String clientState;

    public SubscriptionRecord(String subscriptionId, String userId, String clientState) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.clientState = clientState;
    }
}
