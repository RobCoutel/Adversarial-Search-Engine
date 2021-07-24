package com.chess;

import com.gameEngine.*;

public class ChessMove implements Move {
    private int origin, destination; // make it an array for castling?
    private int moving, taken, promoted; // pieces
    private ChessBoard board;
    private int moveType = SIMPLE_MOVE;
    private boolean[] previousCastlingRights;
    private int previousNbMovesNoTake;

    private boolean resign = false;

    public static final int SIMPLE_MOVE = 0;
    public static final int PROMOTION = 1;
    public static final int SHORT_CASTLE = 2;
    public static final int LONG_CASTLE = 3;
    public static final int EN_PASSANT = 4;


    public ChessMove(ChessBoard board, int origin, int dest) {
        if(origin < 0 || dest < 0) {
            resign = true;
        }
        this.board = board;
        this.origin = origin;
        this.destination = dest;
        construct();
    }

    public ChessMove(ChessBoard board, String moveName) {
        this.board = board;
        if(moveName.equals("0-0")) {
            moveType = SHORT_CASTLE;
            origin = 4 + board.getTurn()*56;
            destination = 6 + board.getTurn()*56;
            return;
        }
        else if(moveName.equals("0-0-0")) {
            moveType = LONG_CASTLE;
            origin = 4 + board.getTurn()*56;
            destination = 2 + board.getTurn()*56;
            return;
        }

        int charIndex = 0;
        char currChar = moveName.charAt(charIndex);
        if(currChar >= 'A' && currChar <= 'Z') {
            moving = ChessBoard.pieceValue(currChar);
            charIndex++;
        }
        else {
            moving = ChessBoard.PAWN;
        }

        origin = parseCoordinate(moveName.substring(charIndex));
        charIndex += 2;

        if(moveName.charAt(charIndex) == 'x') {
            charIndex++;
        }

        destination = parseCoordinate(moveName.substring(charIndex));
        charIndex += 2;

        construct();
    }

    private int parseCoordinate(String s) {
        int file = s.charAt(0) - 'a';
        int rank = Character.getNumericValue(s.charAt(1)) - 1;
        return board.square(file, rank);
    }

    private void construct() {
        moving = board.getPiece(origin);
        taken = board.getPiece(destination);
        promoted = ChessBoard.UNDEFINED;

        // castling
        if(moving%ChessBoard.BLACK == ChessBoard.KING
           && origin == 4 + moving/ChessBoard.BLACK*56) {
            if(destination == 6 + moving/ChessBoard.BLACK*56
               || destination == 7 + moving/ChessBoard.BLACK*56){
                   moveType = SHORT_CASTLE;
                   return;
             }
             if(destination == 2 + moving/ChessBoard.BLACK*56
                || destination == moving/ChessBoard.BLACK*56){
                    moveType = LONG_CASTLE;
                    return;
              }
              return;
        }
        if (moving%ChessBoard.BLACK == ChessBoard.PAWN) {
            // default promotion
            if (board.getRank(destination) == ((moving/ChessBoard.BLACK+1)%2)*7) {
                promoted = ChessBoard.QUEEN;
                moveType = PROMOTION;
                return;
            }
            // en passant
            if(taken == ChessBoard.UNDEFINED
            && origin%8 != destination%8) {
                taken = board.getPiece(destination + (board.getTurn()==0? -8:8));
                moveType = EN_PASSANT;
            }
        }

    }

    public void setPromotion(int promoted) {
        this.promoted = promoted;
        moveType = PROMOTION;
    }

    public int getDestination() { return destination; }
    public int getOrigin() { return origin; }
    public int getMoving() { return moving; }
    public int getTaken() { return taken; }
    public int getPromoted() { return promoted; }
    public int moveType() { return moveType; }
    public void setPreviousNbMovesNoTake(int nbMovesNoTake) { previousNbMovesNoTake = nbMovesNoTake; }
    public int getPreviousNbMovesNoTake() { return previousNbMovesNoTake; }
    public boolean[] getPreviousCastlingRights() { return previousCastlingRights; }

    public void setPreviousCastlingRights(boolean[] castlingRights) {
        previousCastlingRights = castlingRights;
    }

    public void enPassant() {
        moveType = EN_PASSANT;
        int turn = board.getTurn();
        taken = board.getPiece(destination + (turn==0? -8:8));
    }

    public boolean isPawnPush2(int file) {
        return moving%ChessBoard.BLACK == ChessBoard.PAWN      // is a pawn
               && file == board.getFile(origin)      // is on the right file
               && abs(destination-origin) == 16;     // pushed 2 squares
    }

    public String toString() {
        if(moveType == SHORT_CASTLE) {
            return "0-0";
        }
        if(moveType == LONG_CASTLE) {
            return "0-0-0";
        }
        String s = "";
        char pieceName = ChessBoard.pieceName(moving);
        if(pieceName != 'P') {
            s += pieceName;
        }
        int originFile = board.getFile(origin);
        int originRank = board.getRank(origin);
        s += Character.toString((char)('a' + originFile)) + Integer.toString(originRank+1);
        if (taken != ChessBoard.UNDEFINED) {
            s += "x";
        }
        int destFile = board.getFile(destination);
        int destRank = board.getRank(destination);
        /*if(taken != ChessBoard.UNDEFINED) {
            s += ChessBoard.pieceName(taken);
        }*/
        s += Character.toString((char)('a' + destFile)) + Integer.toString(destRank+1);
        if(promoted != ChessBoard.UNDEFINED) {
            s += "=" + ChessBoard.pieceName(promoted);
        }
        if(moveType == EN_PASSANT) {
            s += "enPass";
        }
        return s;
    }

    public boolean equals(Move move2) {
        return this.toString().equals(move2.toString());
    }

    public ChessMove clone(ChessBoard extBoard) {
        ChessMove clone = new ChessMove(extBoard, this.origin, this.destination);
        clone.moving = this.moving;
        clone.taken = this.taken;
        clone.promoted = this.promoted;
        clone.moveType = this.moveType;
        clone.previousCastlingRights = (boolean[]) this.previousCastlingRights.clone();
        clone.previousNbMovesNoTake = this.previousNbMovesNoTake;

        return clone;
    }

    public int moveImbalance() {
        int toReturn = ChessBoard.pieceWorth(taken) + ChessBoard.pieceWorth(promoted);
        int[] oponentControl = board.getControl((moving/ChessBoard.BLACK+1)%2);
        int[] myControl = board.getControl(moving/ChessBoard.BLACK);
        if(toReturn != 0 && oponentControl[destination] > myControl[destination]) {
            toReturn -= ChessBoard.pieceWorth(moving);
        }
        return toReturn;
    }

    private int abs(int a) { return a<0? -a : a; }


    public int compareTo(Move move2) {
        ChessMove move = (ChessMove) move2;
        int moveImbalance1 = this.moveImbalance();
        int moveImbalance2 = move.moveImbalance();
        if (moveImbalance1 == moveImbalance2) {
            return 0;
        }
        if(moveImbalance1 > moveImbalance2) {
            return -1;
        }
        return 1;
    }

    public boolean isResignation() { return resign; }

}
