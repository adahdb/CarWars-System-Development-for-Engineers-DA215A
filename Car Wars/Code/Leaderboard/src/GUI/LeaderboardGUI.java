package GUI;

import Model.CarStats;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI-klass för att visa leaderboard/poängtavla för bilkapplöpning.
 * Visar spelares bilar, poäng och förmågor i realtid.
 *
 * @author Rachid kontakgi
 * @version 3
 * @since 2025
 */
public class LeaderboardGUI extends JFrame {

    /** Karta över bilnamn och deras poäng-progressbars */
    private final Map<String, JProgressBar> scoreBars = new HashMap<>();

    /** Karta över bilnamn och deras förmåge-progressbars */
    private final Map<String, JProgressBar> abilityBars = new HashMap<>();

    /** Lista över alla spelare i matchen */
    private final List<CarStats> players;

    /** Huvudpanel som innehåller alla bilrader */
    private JPanel mainPanel;

    /** Container för hela GUI:n */
    private final JPanel container;

    /** Aktuellt spelläge */
    private final String gameMode;

    /**
     * Konstruktor för LeaderboardGUI med specificerat spelläge.
     *
     * @param players lista över spelare i matchen
     * @param gameMode spelläge (t.ex. "2v2", "4v4")
     */
    public LeaderboardGUI(List<CarStats> players, String gameMode) {
        this.players = new ArrayList<>(players);
        this.gameMode = gameMode;

        setTitle("Car Championship - " + gameMode);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 200 + (players.size() * 300)); // Dynamisk höjd baserat på antal spelare
        setLocationRelativeTo(null);

        // Sortera spelare efter poäng (högst först)
        this.players.sort((a, b) -> Double.compare(b.score, a.score));

        // Skapa huvudlayout
        mainPanel = new JPanel(new GridLayout(players.size(), 1));
        mainPanel.setBackground(Color.BLACK);

        // Fyll huvudpanelen med bilrader
        updateLeaderboardDisplay();

        JLabel titleLabel = new JLabel("⚔️ " + gameMode + " - Leaderboard ⚔️", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 70));
        titleLabel.setForeground(Color.WHITE);

        container = new JPanel(new BorderLayout());
        container.setBackground(Color.BLACK);
        container.add(titleLabel, BorderLayout.NORTH);
        container.add(mainPanel, BorderLayout.CENTER);

        setContentPane(container);
    }

    /**
     * Konstruktor för bakåtkompatibilitet.
     * Använder standardspelläge "4v4 (4 bilar)".
     *
     * @param players lista över spelare i matchen
     */
    public LeaderboardGUI(List<CarStats> players) {
        this(players, "4v4 (4 bilar)");
    }

    /**
     * Skapar en bildrad för en spelare med placeringsinformation.
     *
     * @param place placeringstext (t.ex. "🥇 1st Place")
     * @param player spelardata med bil och statistik
     * @return JPanel med spelarens information
     */
    private JPanel createImageRow(String place, CarStats player) {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image scaledImage = player.carImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                g.drawImage(scaledImage, 0, 0, null);
            }
        };

        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(1100, 300));
        panel.setOpaque(false);

        // Place label
        JLabel placeLabel = new JLabel(place);
        placeLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        placeLabel.setForeground(Color.WHITE);
        placeLabel.setBounds(30, 20, 600, 50);
        panel.add(placeLabel);

        // Player + car name label
        JLabel playerCarLabel = new JLabel(player.playerName + " - " + player.carName);
        playerCarLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        playerCarLabel.setForeground(Color.WHITE);
        playerCarLabel.setBounds(30, 80, 600, 50);
        panel.add(playerCarLabel);

        // Score bar
        JProgressBar scoreBar = new JProgressBar(0, 100);
        scoreBar.setValue((int) (player.score * 10));
        scoreBar.setStringPainted(true);
        scoreBar.setForeground(Color.GREEN);
        scoreBar.setBackground(Color.DARK_GRAY);
        scoreBar.setBounds(30, 140, 300, 25);
        scoreBar.setFont(new Font("SansSerif", Font.BOLD, 14));
        scoreBar.setString("Score: " + player.score);
        panel.add(scoreBar);
        scoreBars.put(player.carName, scoreBar);

        // Ability bar
        JProgressBar abilityBar = new JProgressBar(0, 100);
        abilityBar.setValue((int) (player.ability * 10));
        abilityBar.setStringPainted(true);
        abilityBar.setForeground(Color.CYAN);
        abilityBar.setBackground(Color.DARK_GRAY);
        abilityBar.setBounds(30, 180, 300, 25);
        abilityBar.setFont(new Font("SansSerif", Font.BOLD, 14));
        abilityBar.setString("Ability: " + player.ability);
        panel.add(abilityBar);
        abilityBars.put(player.carName, abilityBar);

        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        return panel;
    }

    /**
     * Uppdaterar poäng för en specifik bil.
     *
     * @param carName namnet på bilen
     * @param score ny poäng
     */
    public void setScore(String carName, double score) {
        boolean updated = false;

        // Uppdatera poängen i modellen
        for (CarStats player : players) {
            if (player.carName.equals(carName)) {
                player.score = score;
                updated = true;
                break;
            }
        }

        // Uppdatera scoreBar om den finns
        if (scoreBars.containsKey(carName)) {
            JProgressBar bar = scoreBars.get(carName);
            int value = (int) (score * 10);
            bar.setValue(value);
            bar.setString("Score: " + score);
        }

        // Om poängen uppdaterades, sortera om leaderboarden
        if (updated) {
            updateLeaderboardOrder();
        }
    }

    /**
     * Uppdaterar förmåga för en specifik bil.
     *
     * @param carName namnet på bilen
     * @param ability ny förmåga
     */
    public void setAbility(String carName, double ability) {
        // Uppdatera ability i modellen
        for (CarStats player : players) {
            if (player.carName.equals(carName)) {
                player.ability = ability;
                break;
            }
        }

        // Uppdatera abilityBar om den finns
        if (abilityBars.containsKey(carName)) {
            JProgressBar bar = abilityBars.get(carName);
            int value = (int) (ability * 10);
            bar.setValue(value);
            bar.setString("Ability: " + ability);
        }
    }

    /**
     * Uppdaterar ordningen på leaderboarden baserat på aktuella poäng.
     */
    private void updateLeaderboardOrder() {
        // Sortera spelarna efter poäng (högst först)
        players.sort((a, b) -> Double.compare(b.score, a.score));

        // Uppdatera UI:n med de nya positionerna
        updateLeaderboardDisplay();

        // Uppdatera fönstret
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Uppdaterar leaderboard-displayen med aktuell ordning.
     */
    private void updateLeaderboardDisplay() {
        // Rensa huvudpanelen
        mainPanel.removeAll();

        // Dynamiska placeringsemojis baserat på antal spelare
        Map<Integer, String> emojis = getPlaceEmojis(players.size());

        // Lägg till bildraderna i sorterad ordning
        for (int i = 0; i < players.size(); i++) {
            CarStats player = players.get(i);
            String label = emojis.getOrDefault(i + 1, "🏁 " + (i + 1) + " Place");
            JPanel row = createImageRow(label, player);
            mainPanel.add(row);
        }

        // Uppdatera UI:n
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Genererar placeringsemojis baserat på antal spelare.
     *
     * @param playerCount antal spelare i matchen
     * @return karta med placering och motsvarande emoji-text
     */
    private Map<Integer, String> getPlaceEmojis(int playerCount) {
        Map<Integer, String> emojis = new HashMap<>();
        if (playerCount >= 1) emojis.put(1, "🥇 1st Place");
        if (playerCount >= 2) emojis.put(2, "🥈 2nd Place");
        if (playerCount >= 3) emojis.put(3, "🥉 3rd Place");
        if (playerCount >= 4) emojis.put(4, "🏁 4th Place");
        return emojis;
    }
}