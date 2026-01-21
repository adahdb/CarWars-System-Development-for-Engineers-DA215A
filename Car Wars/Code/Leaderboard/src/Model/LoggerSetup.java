package Model;

import java.io.IOException;
import java.util.logging.*;

/**
 * Hjälpklass för att konfigurera logging för applikationen.
 * Skapar en fil-baserad logger för att spara händelser.
 *
 * @author Rachid kontakgi
 * @version 1.0
 * @since 2025
 */
public class LoggerSetup {

    /**
     * Skapar och konfigurerar en logger med fil-output.
     *
     * @param name namnet på loggern
     * @return konfigurerad Logger-instans
     */
    public static Logger setupLogger(String name) {
        Logger logger = Logger.getLogger(name);
        try {
            // Skapa fil-handler för att logga till fil
            FileHandler fileHandler = new FileHandler("leaderboard.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Logga inte till konsol
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Logger setup misslyckades: " + e.getMessage());
        }
        return logger;
    }
}