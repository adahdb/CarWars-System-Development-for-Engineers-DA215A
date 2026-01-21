package GUI;

import Model.CarStats;
import Imageresources.ImageResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI-klass för att konfigurera match-inställningar före start.
 * Låter användaren välja spelläge och ange spelar- och bilnamn.
 *
 * @author Rachid,Josh
 * @version 2
 * @since 2025
 */
public class MatchSetupGUI extends JFrame {

    /** Combobox för val av spelläge */
    private JComboBox<String> gameModeCombo;

    /** Textfält för bilnamn */
    private JTextField[] carNameFields;

    /** Textfält för spelarnamn */
    private JTextField[] playerNameFields;

    /** Knapp för att starta matchen */
    private JButton startButton;

    /** Callback som anropas när matchen startas */
    private Consumer<MatchConfig> onMatchStartCallback;

    /** Resurs-klass för bilbilder */
    private ImageResources imageResources;

    /**
     * Konfigurationsklass som innehåller match-inställningar.
     */
    public static class MatchConfig {
        /** Valt spelläge */
        public final String gameMode;

        /** Lista över spelare i matchen */
        public final List<CarStats> players;

        /**
         * Konstruktor för MatchConfig.
         *
         * @param gameMode spelläge
         * @param players lista över spelare
         */
        public MatchConfig(String gameMode, List<CarStats> players) {
            this.gameMode = gameMode;
            this.players = players;
        }
    }

    /**
     * Konstruktor för MatchSetupGUI.
     *
     * @param onMatchStartCallback callback som anropas när matchen startas
     */
    public MatchSetupGUI(Consumer<MatchConfig> onMatchStartCallback) {
        this.onMatchStartCallback = onMatchStartCallback;
        this.imageResources = new ImageResources();
        initializeGUI();
    }

    /**
     * Initierar GUI-komponenter och layout.
     */
    private void initializeGUI() {
        setTitle("Match Setup - Car Championship");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.BLACK);

        // Titel
        JLabel titleLabel = new JLabel("🏁 SETUP MATCH 🏁", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Huvudpanel för inställningar
        JPanel setupPanel = new JPanel(new GridBagLayout());
        setupPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Spelläge val
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel gameModeLabel = new JLabel("Välj Spelläge:", SwingConstants.CENTER);
        gameModeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gameModeLabel.setForeground(Color.WHITE);
        setupPanel.add(gameModeLabel, gbc);

        gbc.gridy = 1;
        String[] gameModes = {"2v2 (2 bilar)", "4v4 (4 bilar)"};
        gameModeCombo = new JComboBox<>(gameModes);
        gameModeCombo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gameModeCombo.setPreferredSize(new Dimension(200, 30));
        gameModeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePlayerFields();
            }
        });
        setupPanel.add(gameModeCombo, gbc);

        // Spelarinställningar panel
        gbc.gridy = 2; gbc.gridwidth = 1;
        JPanel playersPanel = createPlayersPanel();
        setupPanel.add(playersPanel, gbc);

        mainPanel.add(setupPanel, BorderLayout.CENTER);

        // Start knapp
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        startButton = new JButton("🚗 STARTA MATCH 🚗");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        startButton.setPreferredSize(new Dimension(250, 50));
        startButton.setBackground(new Color(0, 150, 0));
        startButton.setForeground(Color.BLACK);
        startButton.addActionListener(this::startMatch);
        buttonPanel.add(startButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Initiera med 2v2 läge
        updatePlayerFields();
    }

    /**
     * Skapar panelen för spelarinställningar.
     *
     * @return JPanel med textfält för spelar- och bilnamn
     */
    private JPanel createPlayersPanel() {
        JPanel playersPanel = new JPanel(new GridBagLayout());
        playersPanel.setBackground(Color.BLACK);
        playersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                "Spelarinställningar",
                0, 0,
                new Font("SansSerif", Font.BOLD, 16),
                Color.WHITE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Headers
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel playerHeader = new JLabel("Spelarnamn");
        playerHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        playerHeader.setForeground(Color.WHITE);
        playersPanel.add(playerHeader, gbc);

        gbc.gridx = 1;
        JLabel carHeader = new JLabel("Bilnamn");
        carHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        carHeader.setForeground(Color.WHITE);
        playersPanel.add(carHeader, gbc);

        // Initialisera fält för max 4 spelare
        playerNameFields = new JTextField[4];
        carNameFields = new JTextField[4];

        String[] defaultPlayerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
        String[] defaultCarNames = {"IronCrusher", "MudEater", "BlazeFury", "BlueThunder"};

        for (int i = 0; i < 4; i++) {
            gbc.gridy = i + 1;

            // Spelarnamn fält
            gbc.gridx = 0;
            playerNameFields[i] = new JTextField(defaultPlayerNames[i], 15);
            playerNameFields[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            playersPanel.add(playerNameFields[i], gbc);

            // Bilnamn fält
            gbc.gridx = 1;
            carNameFields[i] = new JTextField(defaultCarNames[i], 15);
            carNameFields[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            playersPanel.add(carNameFields[i], gbc);
        }

        return playersPanel;
    }

    /**
     * Uppdaterar vilka spelarfält som är aktiva baserat på valt spelläge.
     */
    private void updatePlayerFields() {
        String selectedMode = (String) gameModeCombo.getSelectedItem();
        boolean is2v2 = selectedMode.contains("2v2");

        // Aktivera/inaktivera fält baserat på spelläge
        for (int i = 0; i < 4; i++) {
            boolean enabled = is2v2 ? (i < 2) : true;
            playerNameFields[i].setEnabled(enabled);
            carNameFields[i].setEnabled(enabled);

            if (!enabled) {
                playerNameFields[i].setBackground(Color.LIGHT_GRAY);
                carNameFields[i].setBackground(Color.LIGHT_GRAY);
            } else {
                playerNameFields[i].setBackground(Color.WHITE);
                carNameFields[i].setBackground(Color.WHITE);
            }
        }
    }

    /**
     * Startar matchen med de konfigurerade inställningarna.
     * Validerar input och skapar MatchConfig-objekt.
     *
     * @param e ActionEvent från start-knappen
     */
    private void startMatch(ActionEvent e) {
        String selectedMode = (String) gameModeCombo.getSelectedItem();
        boolean is2v2 = selectedMode.contains("2v2");
        int playerCount = is2v2 ? 2 : 4;

        // Validera att alla aktiva fält är ifyllda
        for (int i = 0; i < playerCount; i++) {
            if (playerNameFields[i].getText().trim().isEmpty() ||
                    carNameFields[i].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Alla spelar- och bilnamn måste fyllas i!",
                        "Validering",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Skapa spelare
        List<CarStats> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String playerName = playerNameFields[i].getText().trim();
            String carName = carNameFields[i].getText().trim();

            // Välj bildbild baserat på position
            BufferedImage carImage = getCarImageForIndex(i);

            players.add(new CarStats(playerName, carName, carImage, 10.0, 10.0));
        }

        // Skapa match config och starta
        MatchConfig config = new MatchConfig(selectedMode, players);
        onMatchStartCallback.accept(config);

        // Stäng setup-fönstret
        dispose();
    }

    /**
     * Väljer lämplig bilbild baserat på spelarindex.
     *
     * @param index spelarens position (0-3)
     * @return BufferedImage för bilen
     */
    private BufferedImage getCarImageForIndex(int index) {
        switch (index) {
            case 0: return imageResources.getTankImage();
            case 1: return imageResources.getTractorImage();
            case 2: return imageResources.getRaceCarRedImage();
            case 3: return imageResources.getRaceCarBlueImage();
            default: return imageResources.getTankImage();
        }
    }
}