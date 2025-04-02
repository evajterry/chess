package client;

import client.websocket.NotificationHandler;

import java.util.Scanner;

import static client.EscapeSequences.*;

import exception.ResponseException;
import websocket.messages.ServerMessage;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) throws ResponseException {
        client = new ChessClient(serverUrl, this); // idk if i should have notificationHandler here
    }

    public void run() {
        System.out.println("Welcome to Chess by Eva. Sign in or register to start!");
        System.out.print(client.preLogin());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.evalPreLogin(line);
                System.out.print(BLUE + result);
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }

    @Override
    public void notify(ServerMessage message) {
        System.out.println(RED + message.getMessage()); // Assuming getMessage() exists
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }
}
