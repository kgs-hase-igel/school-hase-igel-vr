package kgs.spieler.hui;

import sc.plugin2018.*;
import sc.plugin2018.util.Constants;

import java.util.ArrayList;

public class HelperLogic {
    /**
     * Die Winning Moves aus den possibleMoves herausfiltern
     * @param possibleMoves
     */
    public ArrayList<Move> extractWinningMoves(int index, ArrayList<Move> possibleMoves) {
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

    public ArrayList<Move> extractSaladMoves(GameState gameState, int index, ArrayList<Move> possibleMoves) {
        ArrayList<Move> saladMoves = new ArrayList<>();

        for(Move move : possibleMoves) {
            for(Action action : move.actions) {
                if (action instanceof Advance) {
                    Advance advance = (Advance) action;

                    if (gameState.getBoard().getTypeAt(advance.getDistance() + index) == FieldType.SALAD) {
                        // Zug auf Salatfeld
                        saladMoves.add(move);
                    }
                } else if (action instanceof Card) {
                    Card card = (Card) action;

                    if (card.getType() == CardType.EAT_SALAD) {
                        // Zug auf Hasenfeld und danch Salatkarte
                        saladMoves.add(move);
                    }
                }
            }
        }

        return saladMoves;
    }
}
