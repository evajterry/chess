package client.websocket;

//import webSocketMessages.Notification; // should this import both usergamecommand and servermessage?

import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage message);
}
