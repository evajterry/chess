package client;

import com.sun.nio.sctp.NotificationHandler;

import java.util.Scanner;

import static client.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, (NotificationHandler) this); // idk if i should have notificationHandler here
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess by Eva. Sign in to start.");
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
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }
}
