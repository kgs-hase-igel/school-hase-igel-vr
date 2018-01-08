package kgs.spieler.hui;

/**
 * Importiere Sachen die wir brauchen
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.player2018.Starter;
import sc.plugin2018.*;
import sc.plugin2018.util.Constants;
import sc.shared.GameResult;
import sc.shared.PlayerColor;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

/**
 * Das Herz des Simpleclients: Eine sehr simple Logik, die ihre Zuege zufaellig
 * waehlt, aber gueltige Zuege macht. Ausserdem werden zum Spielverlauf
 * Konsolenausgaben gemacht.
 */
public class CJTLogic implements IGameHandler {
    /**
     * Ein paar Objekt variablen, die aber privat bleiben.
     */
    private Starter client;
    private GameState gameState;
    private Player currentPlayer;

    private static final Logger log = LoggerFactory.getLogger(CJTLogic.class);

    /*
     * Klassenweit verfuegbarer Zufallsgenerator der beim Laden der klasse
     * einmalig erzeugt wird und darn immer zur Verfuegung steht.
     */
    private static final Random rand = new SecureRandom();

    /**
     * Erzeugt ein neues Strategieobjekt aus diese Klasse
     *
     * @param client Der Zugrundeliegende Client der mit dem Spielserver
     *               kommunizieren kann.
     */
    public CJTLogic(Starter client) {
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
        // Hole aktuelle Position auf dem Feld
        int index = currentPlayer.getFieldIndex();

        // Unsre Listen mit Schritten
        ArrayList<Move> possibleMoves = gameState.getPossibleMoves();
        ArrayList<Move> winningMoves = new ArrayList<>();

        // Hole den Gegenspieler für zusätzliche Infos
        Player opponent = gameState.getOtherPlayer();

        // Extrahiere die WinningMoves
        winningMoves = extractWinningMoves(index, possibleMoves);

        // Wenn mindestens ein winningMove vorhanden ist
        if (!winningMoves.isEmpty()) {
            Move move;

            // Wähle zufälligen Zug aus
            move = winningMoves.get(rand.nextInt(winningMoves.size()));

            // Sende Zug zum Server
            move.orderActions();
            sendAction(move);

            // Stoppe die Funktion
            return;
        }

        // TODO: Esse Salat wenn möglich

        // Hole infos zur Gegenspieler Position
        int opponentPosition = opponent.getFieldIndex();
        FieldType type = gameState.getTypeAt(opponentPosition);
        int nextSaladField = gameState.getNextFieldByType(FieldType.SALAD, index);
        int nextSecondField = gameState.getNextFieldByType(FieldType.POSITION_2, index);
        int nextHaseFeld = gameState.getNextFieldByType(FieldType.HARE, index);

        // Erste Überprüfung
        if (index == 0) {
            if (nextSaladField != opponentPosition) {
                Move move = new Move();

                move.actions.add(new Advance(nextSaladField - index));

                move.orderActions();
                sendAction(move);

                return;
            } else {
                if(nextSecondField < nextHaseFeld) {
                    Move move = new Move();

                    move.actions.add(new Advance(index - nextSecondField));
                    move.orderActions();

                    sendAction(move);
                    return;
                } else {
                    Move move = new Move();

                    move.actions.add(new Advance(index - nextHaseFeld));
                    move.orderActions();

                    sendAction(move);

                    return;
                }
            }

        } else if (type == FieldType.SALAD && opponentPosition < 57) {
            // Hole Position des nächsten Salatfeldes
            int secondField = gameState.getNextFieldByType(FieldType.POSITION_2, index);
            int igelField = gameState.getPreviousFieldByType(FieldType.HEDGEHOG, index);

            // Wenn das nächste 2te Position Feld weiter als meine eigene Position ist
            if (secondField > index) {
                // Erstelle neuen Spielzug
                Move move = new Move();

                // Füge neue Aktion hinzu
                move.actions.add(new Advance(index - secondField));

                // Sende Zug zum Server
                move.orderActions();
                sendAction(move);

                // Stoppe Code
                return;
            } else if (opponentPosition != igelField) {
                // Erstelle neuen Move
                Move move = new Move();

                // Füge neue Aktion hinzu
                move.actions.add(new Advance(index - igelField));

                // Sende Zug zum Server
                move.orderActions();
                sendAction(move);

                // Stoppe Code
                return;
            }

            System.out.print("Keinen Zug gefunden #1\n");
        } else if (type == FieldType.SALAD && opponentPosition < 57 && index < 57) {
            int hasenField = gameState.getNextFieldByType(FieldType.HARE, index);

            if (hasenField > index) {
                // Erstelle neuen Move
                Move move = new Move();

                // Füge Aktion für Schritte hinzu
                move.actions.add(new Advance(index - hasenField));

                // Sende Zug zum Server
                move.orderActions();
                sendAction(move);

                return;
            } else {
                // TODO: Karotten Zug
                System.out.print("KARROOOOTTEEEENNN\n");
            }
        } else if (nextSaladField > index && opponentPosition != nextSaladField) {
            Move move = new Move();

            move.actions.add(new Advance(nextSaladField - index));

            move.orderActions();
            sendAction(move);

            return;
        } else if (index > 57 && currentPlayer.getSalads() > 0) {
            if (opponentPosition != nextSaladField) {
                Move move = new Move();

                move.actions.add(new Advance(nextSaladField - index));

                move.orderActions();
                sendAction(move);

                return;
            }
        }
    }


    /**
     * Die Winning Moves aus den possibleMoves herausfiltern
     * @param possibleMoves
     */
    private ArrayList<Move> extractWinningMoves(int index, ArrayList<Move> possibleMoves) {
        ArrayList<Move> winningMoves = new ArrayList<>();
        for(Move move : possibleMoves) {
            for(Action action : move.actions) {
                if(action instanceof Advance
                        && ((Advance) action).getDistance() + index == Constants.NUM_FIELDS) {
                    winningMoves.add(move);
                }
            }
        }

        return winningMoves;
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