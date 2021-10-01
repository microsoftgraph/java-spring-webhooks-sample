// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import com.microsoft.graph.models.Message;

public class NewMessageNotification {

    public final String subject;
    public final String id;

    public NewMessageNotification(Message message) {
        subject = message.subject;
        id = message.id;
    }
}
