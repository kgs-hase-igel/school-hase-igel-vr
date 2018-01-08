package de.oltmannsdaniel.hui;

/**
 * Importiere Sachen die wir brauchen
 */
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.player2018.Starter;
import sc.player2018.logic.RandomLogic;
import sc.plugin2018.*;
import sc.plugin2018.util.Constants;
import sc.shared.PlayerColor;
import sc.shared.GameResult;

/**
 * Das Herz des Simpleclients: Eine sehr simple Logik, die ihre Zuege zufaellig
 * waehlt, aber gueltige Zuege macht. Ausserdem werden zum Spielverlauf
 * Konsolenausgaben gemacht.
 */
public class MasterLogic implements IGameHandler {
    /**
     * Ein paar Objekt variablen, die aber privat bleiben.
     */
    private Starter client;
    private GameState gameState;
    private Player currentPlayer;
    private int counter = 0;

    private static final Logger log = LoggerFactory.getLogger(MasterLogic.class);

    /*
     * Klassenweit verfuegbarer Zufallsgenerator der beim Laden der klasse
     * einmalig erzeugt wird und darn immer zur Verfuegung steht.
     */
    private static final Random rand = new SecureRandom();

    /**
     * Erzeugt ein neues Strategieobjekt aus diese Klasse
     *
     * @param client
     *            Der Zugrundeliegende Client der mit dem Spielserver
     *            kommunizieren kann.
     */
    public MasterLogic(Starter client) {
        this.client = client;
    }

    /**
     * Funktion, die erst am Spiel Ende aufgerufen wird
     */
    public void gameEnded(GameResult data, PlayerColor color, String errorMessage) {
        log.info("Das Spiel ist beendet.");
    }

    /**
     * Funktion, für den Fall das der Server einen Spielzug fordert
     */
    @Override
    public void onRequestAction() {
        /**
         * Hier kommt der Code für die Spielzug Auswahl hin.
         * Ein Beispiel findet ihr unter `public void onRequestActionOld`
         */
        this.counter++;

        System.out.print(gameState.getCurrentPlayerColor().toString());
        System.out.print(gameState.getOtherPlayerColor());
        System.out.print("Request #" + this.counter);
    }

    /**
     * Funkion für den Fall das Spieler aktualisert wird
     */
    @Override
    public void onUpdate(Player player, Player otherPlayer) {
        /**
         * Aktualisiere Spieler bei Anforderung
         */
        currentPlayer = player;

        /**
         * Mache diesen Spielerwechsel auf der Konsole sichtbar
         */
        log.info("Spielerwechsel: " + player.getPlayerColor());
    }

    /**
     * Funktion für den Fall das der Spiel Stand aktualisiert wird
     */
    @Override
    public void onUpdate(GameState gameState) {
        /**
         * Änder Spiel Stand der Klasse
         */
        this.gameState = gameState;

        /**
         * Aktualisere den aktuellen Spieler
         */
        currentPlayer = gameState.getCurrentPlayer();

        /**
         * Mache diese Aktion auf der Konsole sichtbar
         */
        log.info("Das Spiel geht voran: Zug: {}", gameState.getTurn());
        log.info("Spieler: {}", currentPlayer.getPlayerColor());
    }

    /**
     * Funktion für den Fall, dass ein Schritt gesendet wird
     */
    @Override
    public void sendAction(Move move) {
        /**
         * Sende neue Schritt an den Server
         */
        client.sendMove(move);
    }

}