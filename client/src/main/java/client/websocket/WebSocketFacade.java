package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.swing.*;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            if (url.startsWith("https")) {
                url = url.replace("https", "wss"); // maybe not necessary
            } else {
                url = url.replace("http", "ws");
            }

            if (!url.contains(":8081")) {
                url = url + ":8081";
            }

            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            // websocket facade sends a request too

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                        System.out.println("Notification: " + serverMessage.getMessage());
                    }
//                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
        // add methods here for stuff like v
        // ws.enterPetShop(visitorName);
    }

    public void sendMessage(ServerMessage message) {
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = new Gson().toJson(message);
                session.getBasicRemote().sendText(jsonMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

//    public void enterGame2(UserGameCommand message) throws ResponseException {
//        try {
//            var action = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
//            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }

    public void enterGame(UserGameCommand command) throws ResponseException {
        try {
            String jsonMessage = new Gson().toJson(command); // Convert the command to JSON
            if (this.session != null && this.session.isOpen()) {
                this.session.getBasicRemote().sendText(jsonMessage);
            }
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}
