package GUI;

import Control.MyServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * GUI-klass för att kontrollera bilar via server-kommandon.
 * Tillhandahåller knappar för att skicka kommandon till alla anslutna ESP32-bilar.
 *
 * @author Rachid kontakgi,Adnan,Josh
 * @version 1.0
 * @since 2025
 */
public class CarControlPanel extends JFrame {

    /** Referens till servern för att skicka kommandon */
    private MyServer server;

    /** Label för att visa server-status */
    private JLabel statusLabel;

    /** Label för att visa antal anslutna bilar */
    private JLabel connectedCarsLabel;

    /** Timer för att uppdatera status automatiskt */
    private Timer statusUpdateTimer;

    /** Aktuell arena-status (true = öppen, false = stängd) */
    private boolean isArenaOpen = false;

    /**
     * Konstruktor för CarControlPanel.
     *
     * @param server server-instans för att skicka kommandon
     */
    public CarControlPanel(MyServer server) {
        this.server = server;
        initializeGUI();
        startStatusUpdater();
    }

    /**
     * Initierar GUI-komponenter och layout.
     */
    private void initializeGUI() {
        setTitle(" Bil Kontrollpanel ");
        setSize(500, 450); // Ökat höjd för fler knappar
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Bara göm, stäng inte hela applikationen
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.BLACK);

        // Titel
        JLabel titleLabel = new JLabel("🏁BIL KONTROLLPANEL ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Status panel
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.CENTER);

        // Kontrollknappar
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Skapar status-panelen som visar server-information.
     *
     * @return JPanel med status-komponenter
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new GridLayout(4, 1, 5, 5)); // Ökat till 4 rader
        statusPanel.setBackground(Color.BLACK);
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                "Server Status",
                0, 0,
                new Font("SansSerif", Font.BOLD, 16),
                Color.WHITE
        ));

        // Server status
        statusLabel = new JLabel("Server: Aktiv", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        statusLabel.setForeground(Color.GREEN);
        statusPanel.add(statusLabel);

        // Anslutna bilar
        connectedCarsLabel = new JLabel("Anslutna bilar: 0", SwingConstants.CENTER);
        connectedCarsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        connectedCarsLabel.setForeground(Color.CYAN);
        statusPanel.add(connectedCarsLabel);

        // Arena status
        JLabel arenaStatusLabel = new JLabel("Arena: STÄNGD", SwingConstants.CENTER);
        arenaStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        arenaStatusLabel.setForeground(Color.RED);
        arenaStatusLabel.setName("arenaStatus"); // För att kunna hitta den senare
        statusPanel.add(arenaStatusLabel);

        // Uppdatera knapp
        JButton refreshButton = new JButton(" Uppdatera Status");
        refreshButton.setBackground(new Color(0, 100, 200));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.addActionListener(e -> updateStatus());
        statusPanel.add(refreshButton);

        return statusPanel;
    }

    /**
     * Skapar kontrollpanelen med knappar för bil-kommandon.
     *
     * @return JPanel med kontrollknappar
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(4, 2, 10, 10)); // Ökat till 4x2 grid
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                "Bil & Arena Kommandon",
                0, 0,
                new Font("SansSerif", Font.BOLD, 16),
                Color.WHITE
        ));

        // START kommando
        JButton startButton = createControlButton(" STARTA BILAR", "START", new Color(0, 150, 0));
        controlPanel.add(startButton);

        // STOP kommando
        JButton stopButton = createControlButton(" STOPPA BILAR", "STOP", new Color(200, 0, 0));
        controlPanel.add(stopButton);

        // ÖPPNA ARENA knapp
        JButton openArenaButton = createArenaControlButton(" ÖPPNA ARENA", "open", new Color(0, 180, 0));
        controlPanel.add(openArenaButton);

        // STÄNG ARENA knapp
        JButton closeArenaButton = createArenaControlButton(" STÄNG ARENA", "close", new Color(180, 0, 0));
        controlPanel.add(closeArenaButton);

        // RESET kommando
        JButton resetButton = createControlButton(" ÅTERSTÄLL", "RESET", new Color(150, 150, 0));
        controlPanel.add(resetButton);

        // READY kommando
        JButton readyButton = createControlButton(" REDO LÄGE", "READY", new Color(0, 100, 200));
        controlPanel.add(readyButton);

        // Lista anslutna bilar
        JButton listCarsButton = createControlButton("📋LISTA BILAR", "", new Color(100, 100, 100));
        listCarsButton.addActionListener(e -> {
            server.listConnectedCars();
            JOptionPane.showMessageDialog(this,
                    "Kolla konsolen för lista över anslutna bilar",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        controlPanel.add(listCarsButton);

        // Test meddelande
        JButton testButton = createControlButton(" TEST MEDDELANDE", "TEST", new Color(150, 0, 150));
        controlPanel.add(testButton);

        return controlPanel;
    }

    /**
     * Skapar en kontrollknapp med specificerad text, kommando och färg.
     *
     * @param text text som visas på knappen
     * @param command kommando att skicka till bilar
     * @param backgroundColor bakgrundsfärg för knappen
     * @return JButton med konfigurerade egenskaper
     */
    private JButton createControlButton(String text, String command, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(200, 40));

        if (!command.isEmpty()) {
            button.addActionListener(e -> sendCommandToAllCars(command));
        }

        return button;
    }

    /**
     * Skapar en arena-kontrollknapp för servo-styrning.
     *
     * @param text text som visas på knappen
     * @param servoCommand servo-kommando ("open" eller "close")
     * @param backgroundColor bakgrundsfärg för knappen
     * @return JButton med konfigurerade egenskaper
     */
    private JButton createArenaControlButton(String text, String servoCommand, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(200, 40));

        button.addActionListener(e -> sendArenaCommand(servoCommand));

        return button;
    }

    /**
     * Skickar arena-kommando (servo-kontroll) till ESP8266.
     *
     * @param command servo-kommando ("open" eller "close")
     */
    private void sendArenaCommand(String command) {
        try {
            // Skicka till alla ESP8266 enheter (eller specifik ESP8266 om du vill)
            server.broadcastToAllCars(command);

            // Uppdatera arena-status
            isArenaOpen = command.equals("open");
            updateArenaStatus();

            // Visa bekräftelse
            String displayCommand = command.equals("open") ? "ÖPPNA ARENA" : "STÄNG ARENA";
            statusLabel.setText("Arena: " + displayCommand);
            statusLabel.setForeground(Color.YELLOW);

            // Återställ status efter 3 sekunder
            Timer resetTimer = new Timer(3000, e -> {
                statusLabel.setText("Server: Aktiv");
                statusLabel.setForeground(Color.GREEN);
            });
            resetTimer.setRepeats(false);
            resetTimer.start();

            System.out.println("Arena-kommando skickat: " + command);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Fel vid sändning av arena-kommando: " + e.getMessage(),
                    "Fel",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Skickar ett kommando till alla anslutna bilar.
     *
     * @param command kommando att skicka
     */
    private void sendCommandToAllCars(String command) {
        try {
            server.broadcastToAllCars(command);

            // Visa bekräftelse
            statusLabel.setText("Kommando skickat: " + command);
            statusLabel.setForeground(Color.YELLOW);

            // Återställ status efter 3 sekunder
            Timer resetTimer = new Timer(3000, e -> {
                statusLabel.setText("Server: Aktiv");
                statusLabel.setForeground(Color.GREEN);
            });
            resetTimer.setRepeats(false);
            resetTimer.start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Fel vid sändning av kommando: " + e.getMessage(),
                    "Fel",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Uppdaterar arena-status i GUI:n.
     */
    private void updateArenaStatus() {
        // Hitta arena status label
        Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && comp.getName() != null && comp.getName().equals("arenaStatus")) {
                JLabel arenaLabel = (JLabel) comp;
                arenaLabel.setText("Arena: " + (isArenaOpen ? "ÖPPEN" : "STÄNGD"));
                arenaLabel.setForeground(isArenaOpen ? Color.GREEN : Color.RED);
                break;
            }
        }
    }

    /**
     * Uppdaterar status-information för anslutna bilar.
     */
    private void updateStatus() {
        int connectedCars = server.getConnectedCarsCount();
        connectedCarsLabel.setText("Anslutna bilar: " + connectedCars);

        if (connectedCars > 0) {
            connectedCarsLabel.setForeground(Color.GREEN);
        } else {
            connectedCarsLabel.setForeground(Color.RED);
        }
    }

    /**
     * Startar automatisk status-uppdatering var 2:a sekund.
     */
    private void startStatusUpdater() {
        // Uppdatera status var 2:a sekund
        statusUpdateTimer = new Timer(2000, e -> updateStatus());
        statusUpdateTimer.start();
    }

    /**
     * Stoppar timers och stänger resurser när fönstret stängs.
     */
    @Override
    public void dispose() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.stop();
        }
        super.dispose();
    }
}