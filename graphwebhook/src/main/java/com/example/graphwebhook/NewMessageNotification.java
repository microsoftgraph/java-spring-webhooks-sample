// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import com.microsoft.graph.models.Message;

/**
 * Represents the information sent via SocketIO to subscribed
 * clients when a new message notification is received
 */
public class NewMessageNotification {

    /**
     * The subject of the message
     */
    public final String subject;

    /**
     * The id of the message, can be used to GET the message via Graph
     */
    public final String id;

    public NewMessageNotification(Message message) {
        subject = message.subject;
        id = message.id;
    }
}
