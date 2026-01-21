package Control;

import GUI.LeaderboardGUI;
import GUI.MatchSetupGUI;
import GUI.CarControlPanel;
import Model.LoggerSetup;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Huvudklass för bilkappkörningsapplikationen.
 * Ansvarar för att starta applikationen och koordinera setup-processen.
 *
 * @author Rachid kontakgi,Adnan,Joshua
 * @version 1.0
 * @since 2025
 */
public class AppLauncher {
    /** Server-instans för att hantera ESP32-kommunikation */
    private static MyServer myServer;

    /**
     * Huvudmetod som startar applikationen.
     * Initierar logger, server och visar setup-GUI.
     *
     * @param args kommandoradsargument (används inte)
     */
    public static void main(String[] args) {
        Logger logger = LoggerSetup.setupLogger("LeaderboardApp");

        // Skapa servern men starta den inte ännu
        myServer = new MyServer();

        // Starta setup GUI i Swing-tråden (EDT)
        SwingUtilities.invokeLater(() -> {
            // Skapa och visa match setup GUI
            MatchSetupGUI setupGUI = new MatchSetupGUI(matchConfig -> {
                // Denna callback körs när användaren startar matchen
                startMatch(matchConfig, logger);
            });

            setupGUI.setVisible(true);
        });
    }

    /**
     * Startar en match baserat på den valda konfigurationen.
     * Skapar och visar leaderboard GUI och kontrollpanel.
     *
     * @param matchConfig konfiguration för matchen (spelare, spelläge)
     * @param logger logger för att logga händelser
     */
    private static void startMatch(MatchSetupGUI.MatchConfig matchConfig, Logger logger) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Skapa leaderboard GUI med de konfigurerade spelarna
                LeaderboardGUI leaderboardGUI = new LeaderboardGUI(
                        matchConfig.players,
                        matchConfig.gameMode
                );

                // Koppla GUI:n till servern
                myServer.setLeaderboardGUI(leaderboardGUI, matchConfig.players);

                // Visa leaderboard
                leaderboardGUI.setVisible(true);

                // Skapa och visa kontrollpanel för bilarna
                CarControlPanel controlPanel = new CarControlPanel(myServer);
                controlPanel.setVisible(true);

                // Lista aktiva bilar för debugging
                myServer.listActiveCars();

                logger.info("Match startad: " + matchConfig.gameMode +
                        " med " + matchConfig.players.size() + " spelare");

            } catch (Exception e) {
                logger.severe("Fel vid start av match: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Fel vid start av match: " + e.getMessage(),
                        "Fel",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Starta servern i en egen tråd
        new Thread(() -> {
            myServer.socket();
        }).start();
    }
}