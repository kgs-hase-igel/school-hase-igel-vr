package kgs.spieler.hui;

/**
 * Importiere Sachen die wir brauchen
 */
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import kgs.spieler.hui.HelperLogic;
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
public class RITOJALogic implements IGameHandler {
    /**
     * Ein paar Objekt variablen, die aber privat bleiben.
     */
    private Starter client;
    private GameState gameState;
    private Player currentPlayer;

    private static final Logger log = LoggerFactory.getLogger(RITOJALogic.class);

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
    public RITOJALogic(Starter client) {
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
        // Hole Spieler Positionen
        int position = currentPlayer.getFieldIndex();
        int opponentPosition = gameState.getOtherPlayer().getFieldIndex();
        boolean is_first = (currentPlayer.getPlayerColor() == PlayerColor.RED);

        // Hole Feld Nummer nächster Felder
        int nextHaseFeld = gameState.getNextFieldByType(FieldType.HARE, position);
        int nextSecondFeld = gameState.getNextFieldByType(FieldType.POSITION_2, position);
        int nextSaladFeld = gameState.getNextFieldByType(FieldType.SALAD, position);

        // Extrahiere verschiedene Zug Arten
        ArrayList<Move> possibleMoves = gameState.getPossibleMoves();
        ArrayList<Move> winningMoves = new HelperLogic().extractWinningMoves(position, possibleMoves);
        ArrayList<Move> saladMoves = new HelperLogic().extractSaladMoves(gameState, position, possibleMoves);

        // Führe Winning Move direkt aus
        if(!winningMoves.isEmpty()) {
            // Wähle beliebigen Winning Move
            Move move = winningMoves.get(rand.nextInt(winningMoves.size()));

            // Sende move zum Server
            sendAction(move);

            // Beende Funktion
            return;
        }

        /**
         * Schritt 1
         */

        // Spieler 1, Position 0, Gegner 0
        if(position == 0 && opponentPosition == 0 && is_first) {
            for(Move move : saladMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(((Advance) action).getDistance() == 10) {
                            sendAction(move);
                            return;
                        }
                    }
                }
            }
        // Spieler 2, Position 0, Gegner 10
        } else if(position == 0 && opponentPosition == 10 && !is_first) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(((Advance) action).getDistance() == gameState.getNextFieldByType(FieldType.POSITION_2, position)) {
                            sendAction(move);

                            return;
                        }
                    }
                }
            }
        // Spieler 2, Position 0, Gegner 10, HasenFeld vor SecondFeld
        } else if(position == 0
                && opponentPosition == 10
                && !is_first
                && nextHaseFeld < nextSecondFeld) {
            Move highestMove = new Move();

            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(gameState.getTypeAt(((Advance) action).getDistance()) == FieldType.HARE) {
                            highestMove = move;
                        }
                    }
                }
            }

            sendAction(highestMove);

            return;
        // Spieler 2, Position 0, Gegner 10, HasenFeld nach SecondFeld, HasenFeld vor SaladFeld
        } else if(position == 0
                && opponentPosition == 10
                && nextHaseFeld > nextSecondFeld
                && nextHaseFeld < nextSaladFeld
                && !is_first) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(((Advance) action).getDistance()  == nextSecondFeld) {
                            for(Action findeataction : move.actions) {
                                if(findeataction instanceof Card) {
                                    if(((Card) findeataction).getType() == CardType.EAT_SALAD) {
                                        sendAction(move);

                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        // Spieler 2, Position 0, Gegner vor SecondFeld
        } else if(position == 0
                && opponentPosition < nextSecondFeld
                && !is_first) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(gameState.getTypeAt(((Advance) action).getDistance()) == FieldType.POSITION_2) {
                            sendAction(move);

                            return;
                        }
                    }
                }
            }
        // Spieler 2, Gegner nicht 10/weiter oder auf SecondFeld/weiter als Hasenfeld
        } else if(position == 0
                && opponentPosition == nextSecondFeld
                && opponentPosition != 10
                && nextHaseFeld < opponentPosition
                && !is_first) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(gameState.getTypeAt(((Advance) action).getDistance()) == FieldType.HARE) {
                        sendAction(move);

                        return;
                    }
                }
            }
        // Spieler 2, Position 0, Gegner nicht 10
        } else if(position == 0
                && opponentPosition != 10
                && !is_first
                ) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(((Advance) action).getDistance() == 10) {
                            sendAction(move);

                            return;
                        }
                    }
                }
            }
        }

        /**
         * Schritt 2
         *
         */
        if(gameState.getTypeAt(position) == FieldType.POSITION_2
                && opponentPosition == 10
                && !is_first) {
            for(Move move : possibleMoves) {
                for(Action action : move.actions) {
                    if(action instanceof Advance) {
                        if(((Advance) action).getDistance()  == nextSecondFeld) {
                            for(Action findeataction : move.actions) {
                                if(findeataction instanceof Card) {
                                    if(((Card) findeataction).getType() == CardType.EAT_SALAD) {
                                        sendAction(move);

                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // TODO: Falls kein Zug gefunden, irgendeinen machen und Fehlermeldung

        /**
         * Starte die RandomLogic
         */
        long startTime = System.nanoTime();
        log.info("Es wurde ein Zug angefordert.");
        ArrayList<Move> possibleMove = gameState.getPossibleMoves(); // Enthält mindestens ein Element
        saladMoves = new ArrayList<>();
        winningMoves = new ArrayList<>();
        ArrayList<Move> selectedMoves = new ArrayList<>();

        int index = currentPlayer.getFieldIndex();
        for (Move move : possibleMove) {
            for (Action action : move.actions) {
                if (action instanceof Advance) {
                    Advance advance = (Advance) action;
                    if (advance.getDistance() + index == Constants.NUM_FIELDS - 1) {
                        // Zug ins Ziel
                        winningMoves.add(move);

                    } else if (gameState.getBoard().getTypeAt(advance.getDistance() + index) == FieldType.SALAD) {
                        // Zug auf Salatfeld
                        saladMoves.add(move);
                    } else {
                        // Ziehe Vorwärts, wenn möglich
                        selectedMoves.add(move);
                    }
                } else if (action instanceof Card) {
                    Card card = (Card) action;
                    if (card.getType() == CardType.EAT_SALAD) {
                        // Zug auf Hasenfeld und danch Salatkarte
                        saladMoves.add(move);
                    } // Muss nicht zusätzlich ausgewählt werden, wurde schon durch Advance ausgewählt
                } else if (action instanceof ExchangeCarrots) {
                    ExchangeCarrots exchangeCarrots = (ExchangeCarrots) action;
                    if (exchangeCarrots.getValue() == 10 && currentPlayer.getCarrots() < 30 && index < 40
                            && !(currentPlayer.getLastNonSkipAction() instanceof ExchangeCarrots)) {
                        // Nehme nur Karotten auf, wenn weniger als 30 und nur am Anfang und nicht zwei mal hintereinander
                        selectedMoves.add(move);
                    } else if (exchangeCarrots.getValue() == -10 && currentPlayer.getCarrots() > 30 && index >= 40) {
                        // abgeben von Karotten ist nur am Ende sinnvoll
                        selectedMoves.add(move);
                    }
                } else if (action instanceof FallBack) {
                    if (index > 56 /*letztes Salatfeld*/ && currentPlayer.getSalads() > 0) {
                        // Falle nur am Ende (index > 56) zurück, außer du musst noch einen Salat loswerden
                        selectedMoves.add(move);
                    } else if (index <= 56 && index - gameState.getPreviousFieldByType(FieldType.HEDGEHOG, index) < 5) {
                        // Falle zurück, falls sich Rückzug lohnt (nicht zu viele Karotten aufnehmen)
                        selectedMoves.add(move);
                    }
                } else {
                    // Füge Salatessen oder Skip hinzu
                    selectedMoves.add(move);
                }
            }
        }
        Move move;
        if (!winningMoves.isEmpty()) {
            log.info("Sende Gewinnzug");
            move = winningMoves.get(rand.nextInt(winningMoves.size()));
        } else if (!saladMoves.isEmpty()) {
            // es gibt die Möglichkeit einen Salat zu essen
            log.info("Sende Zug zum Salatessen");
            move = saladMoves.get(rand.nextInt(saladMoves.size()));
        } else if (!selectedMoves.isEmpty()) {
            move = selectedMoves.get(rand.nextInt(selectedMoves.size()));
        } else {
            move = possibleMove.get(rand.nextInt(possibleMove.size()));
        }
        move.orderActions();
        log.info("Sende zug {}", move);
        long nowTime = System.nanoTime();
        sendAction(move);
        log.warn("Time needed for turn: {}", (nowTime - startTime) / 1000000);
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