package com.chess;

import com.gameEngine.*;

public class ChessMove implements Move {
    private int origin, destination; // make it an array for castling?
    private int moving, taken, promoted; // pieces
    private ChessBoard board;
    private int moveType = SIMPLE_MOVE;

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
        if(moveName.equals("resign")) {
            resign = true;
            return;
        }
        else if(moveName.equals("0-0")) {
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

        origin = ChessBoard.stringToSquare(moveName.substring(charIndex));
        charIndex += 2;

        if(moveName.charAt(charIndex) == 'x') {
            charIndex++;
        }

        destination = ChessBoard.stringToSquare(moveName.substring(charIndex));
        charIndex += 2;

        construct();
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

    public void enPassant() {
        moveType = EN_PASSANT;
        int turn = board.getTurn();
        taken = board.getPiece(destination + (turn==0? -8:8));
    }

    public boolean isPawnPush2() {
        return moving%ChessBoard.BLACK == ChessBoard.PAWN      // is a pawn
               && abs(destination-origin) == 16;     // pushed 2 squares
    }

    public boolean isPawnPush2(int file) {
        return moving%ChessBoard.BLACK == ChessBoard.PAWN      // is a pawn
               && file == board.getFile(origin)      // is on the right file
               && abs(destination-origin) == 16;     // pushed 2 squares
    }

    public String toString() {
        if(resign) {
            return "resign";
        }
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
        s += ChessBoard.squareToString(origin);
        if (taken != ChessBoard.UNDEFINED) {
            s += "x";
        }
        s += ChessBoard.squareToString(destination);
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
        clone.resign = this.resign;
        if(resign) {
            return clone;
        }
        clone.moving = this.moving;
        clone.taken = this.taken;
        clone.promoted = this.promoted;
        clone.moveType = this.moveType;

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
        if(resign || move.resign) {
            if(resign && move.resign) { return 0; }
            if(resign) { return -1; }
            return 1;
        }
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
