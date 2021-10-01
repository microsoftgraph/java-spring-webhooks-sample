// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import com.microsoft.graph.models.ChatMessage;

public class NewChatMessageNotification {

    public final String sender;
    public final String body;

    public NewChatMessageNotification(ChatMessage message) {
        sender = message.from.user.displayName;
        body = message.body.content;
    }
}
