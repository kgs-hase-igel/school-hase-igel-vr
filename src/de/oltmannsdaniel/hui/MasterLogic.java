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
    }

    public void onRequestActionOld() {
        log.info("Es wurde ein Zug angefordert.");

        /**
         * Listen zur abspeicherung von den möglichen Spielzuügen
         */
        ArrayList<Move> possibleMove = gameState.getPossibleMoves(); // Enthält mindestens ein Element
        ArrayList<Move> saladMoves = new ArrayList<>();
        ArrayList<Move> winningMoves = new ArrayList<>();
        ArrayList<Move> selectedMoves = new ArrayList<>();

        /**
         * Hole die aktuelle Position des Spielers
         */
        int index = currentPlayer.getFieldIndex();

        /**
         * Gehe über jeden möglichen Zug (Es gibt mind. 1 Element)
         */
        for (Move move : possibleMove) {
            /**
             * Gehe über die möglichen Aktionen des Spielers
             */
            for (Action action : move.actions) {
                /**
                 * Teste ob die Aktion ein "fortschrittliche" Aktion ist
                 */
                if (action instanceof Advance) {
                    // Wenn ja, speichere diesen Zug
                    Advance advance = (Advance) action;

                    /**
                     * Teste ob ein Zug ins Ziel möglich ist
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
                         * Wenn keine der vorherigen Züge möglich ist, mache einfachc einen Schritt
                         */
                        selectedMoves.add(move);
                    }
                    /**
                     * Ist die nächste AKtion eine Karte
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
                     * Müssen Karotten ausgetauscht werden?
                     */
                } else if (action instanceof ExchangeCarrots) {
                    /**
                     * Speicher diese Aktion
                     */
                    ExchangeCarrots exchangeCarrots = (ExchangeCarrots) action;

                    /**
                     * Wenn Austausch von 10 Karotten möglic ist,
                     * und ich weniger als 30 Karotten habe,
                     * und nicht weiter als Feld 40 bin,
                     * und die letzte Aktion kein Karottenaustausch war
                     */
                    if (exchangeCarrots.getValue() == 10 && currentPlayer.getCarrots() < 30 && index < 40
                            && !(currentPlayer.getLastNonSkipAction() instanceof ExchangeCarrots)) {
                        /**
                         * Nehme nur Karotten auf, wenn weniger als 30 und nur am Anfang und nicht zwei mal hintereinander
                         */
                        selectedMoves.add(move);
                        /**
                         * Wenn ich 10 Karotten abgeben muss,
                         * und ich mehr als 30 Karotten habe,
                         * und ich mind. auf Feld 40 bin.
                         */
                    } else if (exchangeCarrots.getValue() == -10 && currentPlayer.getCarrots() > 30 && index >= 40) {
                        /**
                         * Gebe Karotten am Ende des Spiels ab
                         */
                        selectedMoves.add(move);
                    }
                    /**
                     * Wenn die Aktion ein Rückfall ist
                     */
                } else if (action instanceof FallBack) {
                    /**
                     * Ich weiter als das letzte Salatfeld bin (56),
                     * Und ich noch Salate habe.
                     */
                    if (index > 56 /*letztes Salatfeld*/ && currentPlayer.getSalads() > 0) {
                        /**
                         * Zurückgehen um Salate loszuwerden
                         */
                        selectedMoves.add(move);
                        /**
                         * Wenn ich vor Feld 57 bin (<= 56),
                         * Und es ich weniger als 5 Schritte zurück kann.
                         */
                    } else if (index <= 56 && index - gameState.getPreviousFieldByType(FieldType.HEDGEHOG, index) < 5) {
                        /**
                         * Fall zurück, wenn es nicht soviele Karotten gibt.
                         */
                        selectedMoves.add(move);
                    }
                } else {
                    /**
                     * Ansonsten Salat essen oder Überspringen,
                     * Je nach auf welchem Feld man ist
                     * (Wird automatisch bestimmt)
                     */
                    selectedMoves.add(move);
                }
            }
        }

        /**
         * Variable um den Spielzug auszuwählen
         */
        Move move;

        /**
         * Gibt es Züge um das Spiel zu gewinnen?
         */
        if (!winningMoves.isEmpty()) {
            /**
             * Sende zufälligen Gewinnzug,
             * Gewinn so oder so möglich
             */
            log.info("Sende Gewinnzug");
            move = winningMoves.get(rand.nextInt(winningMoves.size()));
            /**
             * Gibt es Züge um Salate zu essen
             */
        } else if (!saladMoves.isEmpty()) {
            /**
             * Wähle zufällig eine Möglichkeit aus,
             * Um einen Salat zu essen
             */
            log.info("Sende Zug zum Salatessen");
            move = saladMoves.get(rand.nextInt(saladMoves.size()));
            /**
             * Gibt es ausgewählte Spielzüge?
             */
        } else if (!selectedMoves.isEmpty()) {
            /**
             * Wähle zufällig einen der Asugewählten Schritte
             */
            move = selectedMoves.get(rand.nextInt(selectedMoves.size()));
        } else {
            /**
             * Wenn ansonsten nichts möglich ist,
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