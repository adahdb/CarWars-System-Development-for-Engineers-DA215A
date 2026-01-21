package Control;

import GUI.LeaderboardGUI;
import Model.CarStats;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-klass som hanterar kommunikation med ESP32-bilar och ESP8266 arena-kontroll.
 * Ansvarar för att ta emot krockmeddelanden och skicka arena-kommandon.
 *
 * @author Adnan,Rachid,Josh
 * @version 5
 * @since 2025
 */
public class MyServer {

    /** Karta över bil-ID och deras aktuella poäng */
    private HashMap<String, Integer> scoreMap = new HashMap<>();

    /** Referens till leaderboard GUI för att uppdatera poängtavlan */
    private LeaderboardGUI leaderboardGUI;

    /** Mapping mellan bil-ID och bilnamn */
    private Map<String, String> carIdToNameMapping = new HashMap<>();

    /** Lista över alla anslutna klienter (ESP32-enheter och ESP8266) */
    private CopyOnWriteArrayList<ClientConnection> connectedClients = new CopyOnWriteArrayList<>();

    /** Aktuellt arena-kommando att skicka till ESP8266 */
    private String currentArenaCommand = "close"; // Standardvärde: stängd

    /**
     * Klass för att hålla koll på anslutna klienter.
     * Innehåller socket, output stream och klient-typ.
     */
    public static class ClientConnection {
        /** Socket-anslutning till klienten */
        public Socket socket;

        /** Output stream för att skicka meddelanden till klienten */
        public PrintWriter output;

        /** Bil-ID som denna klient representerar (för ESP32) */
        public String carId;

        /** Typ av klient: "ESP32" eller "ESP8266" */
        public String clientType;

        /**
         * Konstruktor för ClientConnection.
         *
         * @param socket socket-anslutning
         * @param output output stream för meddelanden
         */
        public ClientConnection(Socket socket, PrintWriter output) {
            this.socket = socket;
            this.output = output;
            this.carId = null; // Sätts när vi får första meddelandet
            this.clientType = "UNKNOWN"; // Bestäms från första meddelandet
        }
    }

    /**
     * Setter för LeaderboardGUI och players.
     * Initierar scoreMap och bil-mappningar baserat på spelarna.
     *
     * @param leaderboardGUI GUI för att visa poängtavlan
     * @param players lista över spelare i matchen
     */
    public void setLeaderboardGUI(LeaderboardGUI leaderboardGUI, List<CarStats> players) {
        this.leaderboardGUI = leaderboardGUI;

        // Initiera scoreMap och mapping baserat på spelarna
        for (int i = 0; i < players.size(); i++) {
            String carId = "BIL" + (i + 1); // BIL1, BIL2, etc.
            String carName = players.get(i).carName;

            scoreMap.put(carId, 10);
            carIdToNameMapping.put(carId, carName);
        }

        System.out.println("Server initialiserad med följande bilar:");
        for (Map.Entry<String, String> entry : carIdToNameMapping.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    /**
     * Startar server-socketen och lyssnar på port 6000.
     * Accepterar nya klientanslutningar och startar HandleClient-trådar.
     */
    public void socket() {
        try {
            ServerSocket serverSocket = new ServerSocket(6000);
            System.out.println("Server igång på port 6000...");
            System.out.println("Väntar på ESP32 bilar och ESP8266 arena-kontroll...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Ny enhet ansluten: " + clientSocket.getInetAddress());

                // Skapa output stream för att skicka meddelanden tillbaka till klienten
                PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

                // Lägg till klienten i listan över anslutna klienter
                ClientConnection clientConnection = new ClientConnection(clientSocket, clientOutput);
                connectedClients.add(clientConnection);

                HandleClient clientHandler = new HandleClient(clientSocket, this, clientConnection);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Fel: " + e.getMessage());
        }
    }

    /**
     * Hanterar meddelanden från anslutna enheter.
     * Identifierar om det är ESP32 eller ESP8266 baserat på meddelandet.
     *
     * @param message meddelande från klienten
     * @param clientConnection anslutningen som skickade meddelandet
     */
    public synchronized void handleClientMessage(String message, ClientConnection clientConnection) {
        System.out.println("Mottaget meddelande: " + message);

        // Identifiera klient-typ baserat på meddelandet
        if (message.contains("ESP8266 frågar om kommando")) {
            // Detta är ESP8266 arena-kontroll
            clientConnection.clientType = "ESP8266";
            clientConnection.carId = "ARENA";
            System.out.println("✓ ESP8266 Arena-kontroll identifierad");

            // Skicka aktuellt arena-kommando
            clientConnection.output.println(currentArenaCommand);
            System.out.println(" Skickat till ESP8266: " + currentArenaCommand);

        } else if (message.contains(":KROCK")) {
            // Detta är ESP32 bil som rapporterar krock
            handleCrashMessage(message, clientConnection);

        } else if (message.startsWith("PING")) {
            // Heartbeat från någon enhet
            clientConnection.output.println("PONG");

        } else {
            System.out.println("Okänt meddelande från " +
                    clientConnection.socket.getInetAddress() + ": " + message);
        }
    }

    /**
     * Hanterar krockmeddelanden från ESP32-bilar.
     * Minskar poäng och kontrollerar win-condition.
     *
     * @param message meddelande från ESP32 (format: "BIL1:KROCK")
     * @param clientConnection anslutningen som skickade meddelandet
     */
    public synchronized void handleCrashMessage(String message, ClientConnection clientConnection) {
        // Ex: "BIL1:KROCK"
        String[] parts = message.split(":");
        if (parts.length != 2) return;

        String carId = parts[0];
        String event = parts[1];

        // Sätt carId för denna klient om det inte är satt
        if (clientConnection.carId == null) {
            clientConnection.carId = carId;
            clientConnection.clientType = "ESP32";
            System.out.println("ESP32 bil identifierad som: " + carId);
        }

        if (!event.equals("KROCK")) return;

        // Kontrollera om bil-ID:t finns i vår mapping
        if (!carIdToNameMapping.containsKey(carId)) {
            System.out.println("Okänt bil-ID: " + carId);
            return;
        }

        // Startvärde om bilen inte finns
        scoreMap.putIfAbsent(carId, 10);

        // Minska score med 1 (men aldrig under 0)
        int newScore = Math.max(scoreMap.get(carId) - 1, 0);
        scoreMap.put(carId, newScore);

        System.out.println(carId + " (" + carIdToNameMapping.get(carId) + ") har nu score: " + newScore);

        // Uppdatera GUI:n om den är tillgänglig
        if (leaderboardGUI != null) {
            String carName = carIdToNameMapping.get(carId);
            if (carName != null) {
                SwingUtilities.invokeLater(() -> {
                    leaderboardGUI.setScore(carName, newScore);
                });
            }
        }

        // Kontrollera win-condition
        checkWinCondition();
    }

    /**
     * Skickar arena-kommando till ESP8266.
     *
     * @param command arena-kommando ("open" eller "close")
     */
    public void sendArenaCommand(String command) {
        currentArenaCommand = command;
        System.out.println("🏟️ Sätter arena-kommando till: " + command);

        int sentCount = 0;

        // Skicka till alla ESP8266 enheter
        for (ClientConnection client : connectedClients) {
            if ("ESP8266".equals(client.clientType)) {
                try {
                    if (client.output != null && client.socket.isConnected()) {
                        client.output.println(command);
                        sentCount++;
                        System.out.println("📤 Skickat '" + command + "' till ESP8266 (" +
                                client.socket.getInetAddress() + ")");
                    }
                } catch (Exception e) {
                    System.out.println("Fel vid sändning till ESP8266: " + e.getMessage());
                    connectedClients.remove(client);
                }
            }
        }

        if (sentCount == 0) {
            System.out.println("⚠️ Ingen ESP8266 ansluten för arena-kontroll");
        } else {
            System.out.println("✅ Arena-kommando skickat till " + sentCount + " ESP8266 enheter");
        }
    }

    /**
     * Kontrollerar om någon bil har vunnit (alla andra bilar har 0 poäng).
     * Visar ett JOptionPane-meddelande när en vinnare finns.
     */
    private void checkWinCondition() {
        String potentialWinner = null;
        int activeCars = 0;

        // Räkna hur många bilar som fortfarande har poäng
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            if (entry.getValue() > 0) {
                activeCars++;
                potentialWinner = entry.getKey();
            }
        }

        // Skapa final variabler för lambda
        final String finalWinner = potentialWinner;
        final int finalActiveCars = activeCars;

        // Om bara en bil har poäng kvar - vi har en vinnare!
        if (finalActiveCars == 1 && finalWinner != null) {
            String winnerName = carIdToNameMapping.get(finalWinner);

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "🏆 " + winnerName + " (" + finalWinner + ") HAR VUNNIT! 🏆\n\n" +
                                "Grattis till segern!",
                        "MATCH ÖVER",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            System.out.println("MATCH ÖVER! Vinnare: " + winnerName + " (" + finalWinner + ")");
        }
        // Om inga bilar har poäng kvar - oavgjort
        else if (finalActiveCars == 0) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "OAVGJORT! \n\n" +
                                "Alla bilar förlorade samtidigt!",
                        "MATCH ÖVER",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            System.out.println("MATCH ÖVER! Oavgjort - alla bilar förlorade!");
        }
    }

    /**
     * Skickar kommandon till alla anslutna ESP32-bilar.
     *
     * @param message meddelande att skicka till bilar
     */
    public void broadcastToAllCars(String message) {
        System.out.println(" Broadcast till alla enheter: " + message);

        // Kontrollera om det är arena-kommando
        if (message.equalsIgnoreCase("open") || message.equalsIgnoreCase("close")) {
            sendArenaCommand(message);
            return;
        }

        // Skicka till ESP32 bilar
        int sentCount = 0;
        for (ClientConnection client : connectedClients) {
            if ("ESP32".equals(client.clientType)) {
                try {
                    if (client.output != null && client.socket.isConnected()) {
                        client.output.println(message);
                        sentCount++;
                        System.out.println("📤 Skickat '" + message + "' till " +
                                (client.carId != null ? client.carId : "ESP32"));
                    }
                } catch (Exception e) {
                    System.out.println("Fel vid sändning till ESP32: " + e.getMessage());
                    connectedClients.remove(client);
                }
            }
        }

        System.out.println(" Meddelande skickat till " + sentCount + " ESP32 bilar");
    }

    /**
     * Skickar kommando till en specifik bil.
     *
     * @param carId ID för bilen att skicka till
     * @param message meddelande att skicka
     */
    public void sendToSpecificCar(String carId, String message) {
        System.out.println("🎯 Skickar till " + carId + ": " + message);

        for (ClientConnection client : connectedClients) {
            if (carId.equals(client.carId)) {
                try {
                    if (client.output != null && client.socket.isConnected()) {
                        client.output.println(message);
                        System.out.println(" Skickade '" + message + "' till " + carId);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Fel vid sändning till " + carId + ": " + e.getMessage());
                    connectedClients.remove(client);
                }
            }
        }

        System.out.println("Kunde inte hitta ansluten enhet: " + carId);
    }

    /**
     * Tar bort en frånkopplad klient från listan.
     *
     * @param clientConnection klienten att ta bort
     */
    public void removeClient(ClientConnection clientConnection) {
        connectedClients.remove(clientConnection);
        String clientInfo = clientConnection.clientType + " " +
                (clientConnection.carId != null ? clientConnection.carId : "okänd");
        System.out.println(" Klient frånkopplad: " + clientInfo);
    }

    /**
     * Får antal anslutna enheter.
     *
     * @return antal anslutna klienter
     */
    public int getConnectedCarsCount() {
        return connectedClients.size();
    }

    /**
     * Listar alla anslutna enheter i konsolen.
     */
    public void listConnectedCars() {
        System.out.println(" Anslutna enheter (" + connectedClients.size() + "):");
        for (ClientConnection client : connectedClients) {
            String clientInfo = client.clientType + " - " +
                    (client.carId != null ? client.carId : "Väntar på identifiering");
            System.out.println("  • " + clientInfo + " (" + client.socket.getRemoteSocketAddress() + ")");
        }
    }

    /**
     * Får poäng för en specifik bil.
     *
     * @param carId bil-ID
     * @return aktuell poäng
     */
    public int getScore(String carId) {
        return scoreMap.getOrDefault(carId, 10);
    }

    /**
     * Får bilnamn för ett bil-ID.
     *
     * @param carId bil-ID
     * @return bilnamn
     */
    public String getCarName(String carId) {
        return carIdToNameMapping.get(carId);
    }

    /**
     * Listar alla aktiva bilar och deras poäng i konsolen.
     */
    public void listActiveCars() {
        System.out.println("🏁 Aktiva bilar:");
        for (Map.Entry<String, String> entry : carIdToNameMapping.entrySet()) {
            int score = scoreMap.getOrDefault(entry.getKey(), 10);
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue() + " (Score: " + score + ")");
        }
    }
}