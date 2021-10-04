// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import com.microsoft.graph.models.ChatMessage;

/**
 * Represents the information sent via SocketIO to subscribed
 * clients when a new Teams channel message notification
 * is received
 */
public class NewChatMessageNotification {

    /**
     * The display name of the sender
     */
    public final String sender;

    /**
     * The content of the message
     */
    public final String body;

    public NewChatMessageNotification(ChatMessage message) {
        sender = message.from.user.displayName;
        body = message.body.content;
    }
}
