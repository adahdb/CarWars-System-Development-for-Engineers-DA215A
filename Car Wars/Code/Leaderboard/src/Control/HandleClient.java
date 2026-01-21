package Control;

import java.io.*;
import java.net.Socket;

/**
 * Tråd-klass som hanterar kommunikation med en enskild klient (ESP32 eller ESP8266).
 * Lyssnar på meddelanden från klienten och vidarebefordrar till servern.
 *
 * @author Rachid,Adnan,Josh
 * @version 3
 * @since 2025
 */
public class HandleClient extends Thread {

    /** Socket-anslutning till klienten */
    private Socket clientSocket;

    /** Referens till huvudservern */
    private MyServer server;

    /** Klientanslutningsobjekt som innehåller metadata */
    private MyServer.ClientConnection clientConnection;

    /**
     * Konstruktor för HandleClient.
     *
     * @param socket socket-anslutning till klienten
     * @param server referens till huvudservern
     * @param clientConnection klientanslutningsobjekt
     */
    public HandleClient(Socket socket, MyServer server, MyServer.ClientConnection clientConnection) {
        this.clientSocket = socket;
        this.server = server;
        this.clientConnection = clientConnection;
    }

    /**
     * Huvudmetod som körs när tråden startar.
     * Lyssnar kontinuerligt på meddelanden från klienten och hanterar dem.
     */
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(" Mottaget från " + clientSocket.getInetAddress() + ": " + line);

                // Vidarebefordra alla meddelanden till servern för hantering
                server.handleClientMessage(line, clientConnection);
            }
        } catch (IOException e) {
            System.out.println(" Klient frånkopplad (" + clientSocket.getInetAddress() + "): " + e.getMessage());
        } finally {
            // Rensa upp när klienten kopplar från
            server.removeClient(clientConnection);
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Fel vid stängning av socket: " + e.getMessage());
            }
        }
    }
}