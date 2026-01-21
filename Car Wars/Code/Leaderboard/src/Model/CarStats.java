package Model;

import java.awt.image.BufferedImage;

/**
 * Datamodell för en bils statistik och information.
 * Innehåller spelarnamn, bilnamn, bild och spelstatistik.
 *
 * @author Rachid kontakgi
 * @version 1.0
 * @since 2025
 */
public class CarStats {
    /** Namnet på spelaren som kör bilen */
    public String playerName;

    /** Namnet på bilen */
    public String carName;

    /** Bildens grafiska representation */
    public BufferedImage carImage;

    /** Aktuell poäng för bilen */
    public double score;

    /** Bilens förmågevärde */
    public double ability;

    /**
     * Konstruktor för CarStats.
     *
     * @param playerName namnet på spelaren
     * @param carName namnet på bilen
     * @param carImage bildens grafiska representation
     * @param score startpoäng
     * @param ability startförmåga
     */
    public CarStats(String playerName, String carName, BufferedImage carImage, double score, double ability) {
        this.playerName = playerName;
        this.carName = carName;
        this.carImage = carImage;
        this.score = score;
        this.ability = ability;
    }
}