// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.graph.models.Subscription;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionStoreService {

    private static Map<String, SubscriptionRecord> subscriptions = new HashMap<>();

    public void addSubscription(Subscription subscription, String userId) {
        var newRecord = new SubscriptionRecord(subscription.id, userId, subscription.clientState);
        subscriptions.put(subscription.id, newRecord);
    }

    public SubscriptionRecord getSubscription(String id) {
        return subscriptions.get(id);
    }

    public void deleteSubscription(String id) {
        subscriptions.remove(id);
    }

    public List<SubscriptionRecord> getSubscriptionsForUser(String userId) {
        final List<SubscriptionRecord> userSubscriptions = new ArrayList<>();

        subscriptions.forEach((id, subscription) -> {
            if (subscription.userId.equals(userId)) {
                userSubscriptions.add(subscription);
            }
        });

        return userSubscriptions;
    }
}
