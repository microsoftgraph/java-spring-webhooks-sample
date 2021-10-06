// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.util.Objects;
import com.microsoft.graph.models.ChatMessage;
import org.springframework.lang.NonNull;

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

    public NewChatMessageNotification(@NonNull ChatMessage message) {
        Objects.requireNonNull(message);
        sender = message.from.user.displayName;
        body = message.body.content;
    }
}
