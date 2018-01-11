package kgs.spieler.hui;

/**
 * Importiere Sachen die wir brauchen
 */
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.player2018.Starter;
import sc.plugin2018.*;
import sc.plugin2018.util.Constants;
import sc.shared.PlayerColor;
import sc.shared.GameResult;

/**
 * Das Herz des Simpleclients: Eine sehr simple Logik, die ihre Zuege zufaellig
 * waehlt, aber gueltige Zuege macht. Ausserdem werden zum Spielverlauf
 * Konsolenausgaben gemacht.
 */
public class JDJAnzahlKarottenLogic implements IGameHandler {
    /**
     * Ein paar Objekt variablen, die aber privat bleiben.
     */
    private Starter client;
    private GameState gameState;
    private Player currentPlayer;

    private static final Logger log = LoggerFactory.getLogger(JDJAnzahlKarottenLogic.class);

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
    public JDJAnzahlKarottenLogic(Starter client) {
        this.client = client;
    }

    /**
     * Funktion, die erst am Spiel Ende aufgerufen wird
     */
    public void gameEnded(GameResult data, PlayerColor color, String errorMessage) {
        log.info("Das Spiel ist beendet.");
    }

    /**
     * Funktion, f�r den Fall das der Server einen Spielzug fordert
     */
    @Override
    public void onRequestAction() {
        log.info("Es wurde ein Zug angefordert.");

        /**
         * Listen zur abspeicherung von den m�glichen Spielzu�gen
         */
        ArrayList<Move> possibleMove = gameState.getPossibleMoves(); // Enth�lt mindestens ein Element
        ArrayList<Move> saladMoves = new ArrayList<>();
        ArrayList<Move> winningMoves = new ArrayList<>();
        ArrayList<Move> selectedMoves = new ArrayList<>();

        /**
         * Hole die aktuelle Position des Spielers
         */
        int index = currentPlayer.getFieldIndex();

        /**
         * Gehe �ber jeden m�glichen Zug (Es gibt mind. 1 Element)
         */
        for (Move move : possibleMove) {
            /**
             * Gehe �ber die m�glichen Aktionen des Spielers
             */
            for (Action action : move.actions) {
                /**
                 * Teste ob die Aktion ein "fortschrittliche" Aktion ist
                 */
                if (action instanceof Advance) {
                    // Wenn ja, speichere diesen Zug
                    Advance advance = (Advance) action;

                    /**
                     * Teste ob ein Zug ins Ziel m�glich ist
                     */
                    if (advance.getDistance() + index == Constants.NUM_FIELDS - 1) {
                        /**
                         * Wenn ja, ziehe in das Ziel
                         */
                        winningMoves.add(move);
                        /**
                         * Gucke ob ein Salatfeld erreicht werden kann
                         */
                    } else if (gameState.getBoard().getTypeAt(advance.getDistance() + index) == FieldType.SALAD) {
                        /**
                         * Wenn ja, ziehe auf das Salatfeld
                         */
                        saladMoves.add(move);
                    } else {
                        /**
                         * Wenn keine der vorherigen Z�ge m�glich ist, mache einfachc einen Schritt
                         */
                        selectedMoves.add(move);
                    }
                    /**
                     * Ist die n�chste AKtion eine Karte
                     */
                } else if (action instanceof Card) {
                    /**
                     * Wenn ja, speichere Karte
                     */
                    Card card = (Card) action;

                    /**
                     * Soll ich einen Salat essen?
                     */
                    if (card.getType() == CardType.EAT_SALAD) {
                        // Zug auf Hasenfeld und danch Salatkarte
                        /**
                         * Mache einen Zug auf ein Hasenfeld, und esse Salat
                         */
                        saladMoves.add(move);
                    }
                    /**
                     * M�ssen Karotten ausgetauscht werden?
                     */
                } else if (action instanceof ExchangeCarrots) {
                    /**
                     * Speicher diese Aktion
                     */
                    ExchangeCarrots exchangeCarrots = (ExchangeCarrots) action;

                    /**
                     * Wenn Austausch von 10 Karotten m�glich ist,
                     * und ich weniger als 118 Karotten habe,
                     * und nicht weiter als Feld 52 bin,
                     * und die letzte Aktion kein Karottenaustausch war
                     */
                    if (exchangeCarrots.getValue() == 10 && currentPlayer.getCarrots() < 118 && index < 52 /*40*/
                            && !(currentPlayer.getLastNonSkipAction() instanceof ExchangeCarrots)) {
                        /**
                         * Nehme nur Karotten auf, wenn weniger als 30 und nur am Anfang und nicht zwei mal hintereinander
                         */
                        selectedMoves.add(move);
                    }
                    /** 
                     * Wenn Austausch von 10 Karotten m�glich ist,
                     * und ich weniger als 118 Karotten habe,
                     * und nicht weiter als Feld 52 bin,
                     * und die letzte Aktion kein Karottenaustausch war
                     */
                    else if (exchangeCarrots.getValue() == -10 && currentPlayer.getCarrots() > 13 && index >= 52 
                                && !(currentPlayer.getLastNonSkipAction() instanceof ExchangeCarrots)) {
                        /**
                         * Gib Karotten am Ende des Spiels ab
                         */
                            selectedMoves.add(move);
                    } 
                    /**
                     * Wenn ich 10 Karotten aufnehmen muss,
                     * und ich weniger als 18 Karotten habe,
                     * und ich mind. auf Feld 52 bin.
                     */
                    else if (exchangeCarrots.getValue() == 10 && currentPlayer.getCarrots() < 13 && index >= 52) {
                        /**
                         * Nimm Karotten am Ende des Spiels auf
                         */
                        selectedMoves.add(move);
                    }
                    /**
                     * Wenn die Aktion ein R�ckfall ist
                     */
                } else if (action instanceof FallBack) {
                    /**
                     * Ich weiter als das letzte Salatfeld bin (56),
                     * Und ich noch Salate habe.
                     */
                    if (index > 56 /*letztes Salatfeld*/ && currentPlayer.getSalads() > 0) {
                        /**
                         * Zur�ckgehen um Salate loszuwerden
                         */
                        selectedMoves.add(move);
                        /**
                         * Wenn ich vor Feld 57 bin (<= 56),
                         * Und es ich weniger als 5 Schritte zur�ck kann.
                         */
                    } else if (index <= 56 && index - gameState.getPreviousFieldByType(FieldType.HEDGEHOG, index) < 5) {
                        /**
                         * Fall zur�ck, wenn es nicht soviele Karotten gibt.
                         */
                        selectedMoves.add(move);
                    }
                } else {
                    /**
                     * Ansonsten Salat essen oder �berspringen,
                     * Je nach auf welchem Feld man ist
                     * (Wird automatisch bestimmt)
                     */
                    selectedMoves.add(move);
                }
            }
        }

        /**
         * Variable um den Spielzug auszuw�hlen
         */
        Move move;

        /**
         * Gibt es Z�ge um das Spiel zu gewinnen?
         */
        if (!winningMoves.isEmpty()) {
            /**
             * Sende zuf�lligen Gewinnzug,
             * Gewinn so oder so m�glich
             */
            log.info("Sende Gewinnzug");
            move = winningMoves.get(rand.nextInt(winningMoves.size()));
            /**
             * Gibt es Z�ge um Salate zu essen
             */
        } else if (!saladMoves.isEmpty()) {
            /**
             * W�hle zuf�llig eine M�glichkeit aus,
             * Um einen Salat zu essen
             */
            log.info("Sende Zug zum Salatessen");
            move = saladMoves.get(rand.nextInt(saladMoves.size()));
            /**
             * Gibt es ausgew�hlte Spielz�ge?
             */
        } else if (!selectedMoves.isEmpty()) {
            /**
             * W�hle zuf�llig einen der Asugew�hlten Schritte
             */
            move = selectedMoves.get(rand.nextInt(selectedMoves.size()));
        } else {
            /**
             * Wenn ansonsten nichts m�glich ist,
             * sende einen beliebigen Spielzug
             */
            move = possibleMove.get(rand.nextInt(possibleMove.size()));
        }

        /**
         * Ordne Spiel Aktionen
         */
        move.orderActions();

        /**
         * Mache Aktion auf Konsole sichtbar
         */
        log.info("Sende zug {}", move);

        /**
         * Sende Spielzug an Server
         */
        sendAction(move);
    }

    /**
     * Funkion f�r den Fall das Spieler aktualisert wird
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
     * Funktion f�r den Fall das der Spiel Stand aktualisiert wird
     */
    @Override
    public void onUpdate(GameState gameState) {
        /**
         * �nder Spiel Stand der Klasse
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
     * Funktion f�r den Fall, dass ein Schritt gesendet wird
     */
    @Override
    public void sendAction(Move move) {
        /**
         * Sende neue Schritt an den Server
         */
        client.sendMove(move);
    }

}