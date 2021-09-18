/*------------------------------------------------------------------------------
---- File information ----
Name of project : Chess Engine
Class : Board.java
Date : March - 2021
Author : Robin Coutelier
Co-author : Lize Pirenne
------------------------------------------------------------------------------*/

//lichess.org/games/export/RobCoutel?since=1627304400000

/*------------------------------------------------------------------------------
---- Class Description ----
Board.java is the center part of the implementation fo the rules of chess.
Each instance board takes around 2700 bytes of memory.

Contructor :
    Board(Player white, Player black)
        Arguments :
            - Player white : An instance of Player that will manage white pieces
            - Player black : An instance of Player that will manage black pieces

    Board(String boardID, Player white, Player black)
        Arguments :
            - String boardID : A formated String implementing the starting
                               position. Details of the format can be found in
                               the toString() method.
            - Player white : An instance of Player that will manage white pieces
            - Player black : An instance of Player that will manage black pieces

    Board clone()
        Arguments :
            void
        Description :
            Clone a board object. This is a deep cloning. The move stack is
            however not cloned.

Methods :
--------------------------------------------------------------------------------
                                    CORE CODE
--------------------------------------------------------------------------------
    void play()
        Arguments :
            void
        Return :
            void
        Description :
            Function that will invoke the play(Board board) method of the player
            who has to play a move, and plays the returned move until the game
            is over.

    void move(Move move)
        Arguments :
            Move move : An instance of Move that will be played on this instance
                        of board.
        Return :
            void
        Description :
            Function that will alter the board current configuration by playing
            the move described in move.
        Side note :
            This function will not update the legal moves.

    void undo()
        Arguments :
            void
        Return :
            void
        Description :
            Function that will take back the last move and recover the state of
            the board before a move was made. (Does nothing if move 0 of the
            position)

    int gameOver()
        Arguments :
            void
        Return :
            int result : -1 if black won the game
                          0 if the game is a draw
                          1 if white won
                          2 if the game is not over
        Side note :
            This function will update the legal moves

    void replay(int timeBetweenMoves)
        Arguments :
            int timeBetweenMoves : The time waiting before showing the next move
        Return :
            void
        Description :
            This function will replay the game that happened on this board. Each
            move will be separated by timeBetweenMoves ms

--------------------------------------------------------------------------------
                                    GETTERS
--------------------------------------------------------------------------------
    int getPiece(int index)
        Arguments :
            int index : The square of the board from which we wish to know the
                        piece value
        Return :
            int piece : The piece that is located in the square [index]

    Vector<Move> getLegalMoves()
        Arguments :
            void
        Return :
            Vector<Move> legalMoves : A vector containing all the legal moves
                            available from the current position of the board.

    int getTurn()
        Arguments :
            void
        Return :
            int turn : 0 if it is white's turn and 1 if it is black's turn

    int[] getControl(int player)
        Arguments :
            int player : The player from which we want to get the control
        Return :
            int[] control : The control array of the player

--------------------------------------------------------------------------------
                                    FORMAT
--------------------------------------------------------------------------------
    int square(int file, int rank)
        Arguments :
            int file, rank : Integers representing coordinate on the chess board
        Return :
            int index : The index of the square [rank][file] on the board
        Side note :
            if file or rank does not belong to [0,size-1]
            then the output value will be -1

    int getRank(int index)
        Arguments :
            int index : An index of the board
        Return :
            int rank : The rank of the square pointed by [index]

    int getFile(int index)
        Arguments :
            int index : An index of the board
        Return :
            int file : The file of the square pointed by [index]

    String toString(boolean flipped)
        Arguments :
            boolean flipped : boolean value to get the perspective of the board
        Return :
            String boardString : a terminal printable string that will show the
                current board position with white pieces (green) on the bottom
                if flipped si true, and black pieces (red) on the bottom other-
                wise

    String toString()
        Arguments :
            void
        Return :
            String boardID : An indentifier of the position. Pieces are added
                rank by rank starting from the bottom and identified with
                letters. Upper cased letters are white piece and lower cased
                letters are black pieces.
                A number represents the number of blank squares before the next
                piece or the next rank.
                Each rank is seperated by a '/' character.
                The 5 numbers after the 8th '/' are booleans representing
                [0-3] : castling rights (see code for details)
                [4] : the which turn it is
                The end of the string is the stringyfied format of the last move
        Side note :
            The boardID of the standard starting position is
                "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr/11110 null"


------------------------------------------------------------------------------*/
package com.chess;

import com.gameEngine.*;

import java.util.Vector;
import java.util.Stack;
import java.util.HashMap;
import java.util.Random;
import java.lang.IllegalArgumentException;

public class ChessBoard implements Board {
/*------------------------------------------------------------------------------
                                CONSTANTS
------------------------------------------------------------------------------*/
    private static final int size = 8;
    private static final int nbSquares = size*size;

    private static final int a = 0;
    private static final int b = 1;
    private static final int c = 2;
    private static final int d = 3;
    private static final int e = 4;
    private static final int f = 5;
    private static final int g = 6;
    private static final int h = 7;

    private static boolean debug = false;
    private boolean printGame = false;
    public static boolean debug2 = false;

    public static long[][] zobristTable = null;

/*------------------------------------------------------------------------------
                                    PIECES
------------------------------------------------------------------------------*/
    public static final int UNDEFINED = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    public static final int ROOK_VALUE = 5;
    public static final int BISHOP_VALUE = 3;
    public static final int KNIGHT_VALUE = 3;
    public static final int QUEEN_VALUE = 9;
    public static final int PAWN_VALUE = 1;

    public static final int BLACK = 7;

    private static final int[] kingMove = {-9, -1, 7, -8, 8, -7, 1, 9};
    private static final int[] knightMove = {-10, 6, -17, 15, -15, 17, -6, 10};
    private static final int[][] pawnMove = {{7, 8, 9}, {-9, -8, -7}};

    public static char pieceName(int piece) {
        switch(piece%BLACK) {
            case KING       : return 'K';
            case QUEEN      : return 'Q';
            case BISHOP     : return 'B';
            case KNIGHT     : return 'N';
            case ROOK       : return 'R';
            case PAWN       : return 'P';
            case UNDEFINED  : return 'U';
            default         : return 'F';
        }
    }

    public static int pieceValue(char pieceName) {
        switch(pieceName) {
            case 'K' : return KING;
            case 'Q' : return QUEEN;
            case 'B' : return BISHOP;
            case 'N' : return KNIGHT;
            case 'R' : return ROOK;
            case 'P' : return PAWN;
            case 'U' : return UNDEFINED;
            default : System.out.println("Warning (pieceValue) : unrecognized pieceName (" + pieceName + ")");
        }
        return -1;
    }

    public static int pieceWorth(int piece) {
        int toReturn;
        switch(piece%BLACK) {
            case KING       : toReturn = 10;
                              break;
            case QUEEN      : toReturn = QUEEN_VALUE;
                              break;
            case BISHOP     : toReturn = BISHOP_VALUE;
                              break;
            case KNIGHT     : toReturn = KNIGHT_VALUE;
                              break;
            case ROOK       : toReturn = ROOK_VALUE;
                              break;
            case PAWN       : toReturn = PAWN_VALUE;
                              break;
            default         : toReturn = 0;
        }
        return toReturn;
    }

/*------------------------------------------------------------------------------
                              INSTANCE VARIABLES
------------------------------------------------------------------------------*/
    private Player[] players;
    private int turn;

    protected int[] pieces;
    protected int[] kingIndex;
    protected int[][] control;
    protected boolean[] castlingRights;
    protected Vector<Move> legalMoves;
    protected int nbMovesNoTake = 0;
    protected int nbMoves = 2;
    protected int enPassantIndex = -1;
    protected HashMap<Long, Integer> playedPositions;

    protected Move lastMove = null;

    private Stack<BoardState> stateStack;

    private boolean isDraw = false;

    private String startingPosition;

    private int result = 2;
    private boolean computedResult = false;
    private boolean computedLegalMoves = false;
    protected boolean computedControlW = false;
    protected boolean computedControlB = false;

/*------------------------------------------------------------------------------
                                 CONSTRUCTORS
------------------------------------------------------------------------------*/
    public ChessBoard(String boardID, Player white, Player black) {
        construct(boardID, white, black);
    }

    public ChessBoard(Player white, Player black) {
        construct("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", white, black);
    }

    private ChessBoard(Player white, Player black, BoardState boardState) {
        players = new Player[2];
        kingIndex = new int[2];
        stateStack = new Stack<BoardState>();
        players[0] = white;
        players[1] = black;
        boardState.recoverBoardState(this);
    }

    private void construct(String boardID, Player white, Player black) {
        if (zobristTable == null) {
            zobristInit();
        }

        startingPosition = boardID;

        players = new Player[2];
        players[0] = white;
        players[1] = black;

        stateStack = new Stack<BoardState>();

        pieces = new int[nbSquares];

        kingIndex = new int[2];
        kingIndex[0] = -1;
        kingIndex[1] = -1;
        boardInit(boardID);
        if(kingIndex[0] == -1 || kingIndex[1] == -1) {
            String errMsg = "Error in ChessBoard.init : The provided FEN code is missing a king\n";
            throw new IllegalArgumentException(errMsg);
        }

        control = new int[2][nbSquares];

        legalMoves = new Vector<Move>();

        int initialCapacity = 200;
        playedPositions = new HashMap<Long, Integer>(initialCapacity);
        incrementInHashMap();
        updateLegalMoves();
    }

    public ChessBoard clone() {
        ChessBoard clone = new ChessBoard(players[0], players[1], new BoardState(this));
        clone.nbMoves = this.nbMoves;
        clone.turn = this.turn;

        // clone the last state of the stateStack
        if(!stateStack.isEmpty()) {
            clone.stateStack.push(stateStack.peek());
        }

        clone.playedPositions = new HashMap<Long, Integer>(playedPositions);
        return clone;
    }

/*------------------------------------------------------------------------------
                                  CORE CODE
------------------------------------------------------------------------------*/
    public void play() {
        while(gameOver() == 2) {
            if(printGame) {
                System.out.println(positionID());
                System.out.println(this);
                System.out.println(players[turn].getName() + " to play!");
            }
            ChessMove toPlay;

            long startTime = System.currentTimeMillis();
            toPlay = (ChessMove) players[turn].play(this.clone());
            if(debug2) {
                String s = "";
                try {
                  s = "and evaluates the position at " + players[turn].getEvaluation().evaluate(this);
                } catch (NullPointerException nl){
                  //nothing
                }
                System.out.println(players[turn].getName() + " took : "
                + (System.currentTimeMillis() - startTime) + " ms to play"
                + s);
            }
            if(toPlay == null) {
                result = 2*turn - 1;
                computedResult = true;
                return;
            }
            move(toPlay.clone(this));
        }
        if(printGame) {
            System.out.println(positionID());
            System.out.println(this);
            int result = gameOver();
            if(result == 1) {
                System.out.println(players[0].getName() + " wins the game");
            }
            else if(result == -1) {
                System.out.println(players[1].getName() + " wins the game");
            }
            else {
                System.out.println("----------------------\n  The game is a draw\n----------------------");
            }
        }
    }

    public void move(Move toPlay) {
        move(toPlay, true);
    }

    private void move(Move toPlay, boolean updateLegalMoves) {
        if(toPlay == null) {
            throw new IllegalArgumentException("Error in ChessBoard.move :"
            + " The move provided was null");
        }
        // save the current state of the board before playing the move
        lastMove = toPlay;
        stateStack.push(new BoardState(this));
        ChessMove move = (ChessMove) toPlay;

        // play the move
        makeMove(move);
        // save for en passant
        if(move.isPawnPush2()) {
            enPassantIndex = move.getDestination() + (turn==0? -8 : 8);
        }
        else { enPassantIndex = -1; }
        turn = 1 - turn;

        // update the 50 move counter
        if(move.getTaken() == UNDEFINED
        && move.getMoving()%BLACK != PAWN) {
            nbMovesNoTake++;
        }
        else {
            nbMovesNoTake = 0;
            playedPositions.clear();
        }

        // reset computed booleans
        computedResult = false;
        computedControlW = false;
        computedControlB = false;
        computedLegalMoves = false;

        // update HashMap
        incrementInHashMap();

        // update the derived state of the board
        updateCastlingRights();
        // does not update while in recursive call (updateLegalMoves->addMove->move)
        // to prevent infinite recursion
        if(updateLegalMoves) {
            updateLegalMoves();
        }
        nbMoves++;
    }

    public void undo() {
        turn = 1 - turn;
        // recover the derived state of the board
        stateStack.pop().recoverBoardState(this);

        computedResult = false;
        computedLegalMoves = true;
        nbMoves--;
    }

    public Vector<Move> getLegalMoves() {
        if(!computedLegalMoves) {
            updateLegalMoves();
        }
        return new Vector<Move>(legalMoves);
    }

    public int gameOver() {
        if(computedResult) { return result; }
        updateLegalMoves();
        if(legalMoves.isEmpty()) {
            if(isCheck(turn)) {
                result = turn == 0 ? -1 : 1;
            }
            else {
                result = 0;
            }
        }
        else if(nbMovesNoTake >= 100) {
            // 50 moves without take
            result = 0;
        }
        else if(isDraw) {
            // 3 fold repetition
            result = 0;
        }
        else {
            // game not over
            result = 2;
        }
        computedResult = true;
        return result;
    }

/*------------------------------------------------------------------------------
                            SIDE FUNCTIONS
------------------------------------------------------------------------------*/
    /* -----  Moving pieces  ----- */
    private void makeMove(ChessMove move) {
    if(move.isResignation()) {
        result = turn==0 ? -1 : 1;
        computedResult = true;
        return;
    }

    int moveType = move.moveType();
    switch(moveType) {
        case ChessMove.SHORT_CASTLE :
            pieces[e + turn*56] = UNDEFINED;
            pieces[f + turn*56] = ROOK + turn*BLACK;
            pieces[g + turn*56] = KING + turn*BLACK;
            pieces[h + turn*56] = UNDEFINED;
            kingIndex[turn] = g + turn*56;
            break;
        case ChessMove.LONG_CASTLE :
            pieces[e + turn*56] = UNDEFINED;
            pieces[d + turn*56] = ROOK + turn*BLACK;
            pieces[c + turn*56] = KING + turn*BLACK;
            pieces[a + turn*56] = UNDEFINED;
            kingIndex[turn] = c + turn*56;
            break;
        case ChessMove.PROMOTION :
            pieces[move.getOrigin()] = UNDEFINED;
            pieces[move.getDestination()] = move.getPromoted();
            break;
        case ChessMove.EN_PASSANT :
            pieces[move.getDestination()] = move.getMoving();
            pieces[move.getOrigin()] = UNDEFINED;
            int takenIndex = move.getDestination() + (-8 + 16*turn);
            pieces[takenIndex] = UNDEFINED;
            break;
        default :
            pieces[move.getDestination()] = move.getMoving();
            pieces[move.getOrigin()] = UNDEFINED;
            if(move.getMoving()%BLACK == KING) {
                kingIndex[turn] = move.getDestination();
            }
    }
}

    /* -----  Updating legal moves  ----- */
    private void updateLegalMoves() {
        if(computedLegalMoves) { return; }
        updateControl(1-turn);
        legalMoves = new Vector<Move>();
        int piece;
        for(int i=0; i<nbSquares; i++) {
            piece = pieces[i];
            if(piece != UNDEFINED && piece/BLACK == turn) {
                updateLegalMovesUnique(i);
            }
        }
        computedLegalMoves = true;
    }

    private void updateLegalMovesUnique(int index) {
        int piece = pieces[index];
        switch(piece%BLACK) {
            case KING :
                kingMove(index);
                break;
            case QUEEN :
                bishopMove(index);
                // no break on purpose
            case ROOK :
                rookMove(index);
                break;
            case BISHOP :
                bishopMove(index);
                break;
            case KNIGHT :
                knightMove(index);
                break;
            case PAWN :
                pawnMove(index);
                break;
            default:
                System.out.println("Waring (updateLegalMoves) : unrecognized piece (" + (piece%BLACK) + ")");
        }
    }

    private void kingMove(int index) {
        int rank = getRank(index);
        int file = getFile(index);
        // normal move
        int i = file == a ? 3 : 0;
        int end = file == h ? 5 : 8;
        for(; i<end; i++) {
            int dest = index + kingMove[i];
            addMove(new ChessMove(this, index, dest), dest);
        }
        // short castle
        if(castlingRights[2*turn]
        && pieces[index+1] == UNDEFINED
        && pieces[index+2] == UNDEFINED
        && control[1-turn][index] == 0 // is check?
        && control[1-turn][index+1] == 0
        && control[1-turn][index+2] == 0) {
            addMove(new ChessMove(this, ChessMove.SHORT_CASTLE), index+2);
        }
        // long castle
        if(castlingRights[2*turn+1]
        && pieces[index-1] == UNDEFINED
        && pieces[index-2] == UNDEFINED
        && pieces[index-3] == UNDEFINED
        && control[1-turn][index] == 0 // is check?
        && control[1-turn][index-1] == 0
        && control[1-turn][index-2] == 0) {
            addMove(new ChessMove(this, ChessMove.LONG_CASTLE), index-2);
        }
    }

    private void rookMove(int index) {
        slideMove(index, 0, 1);
        slideMove(index, 0, -1);
        slideMove(index, 1, 0);
        slideMove(index, -1, 0);
    }

    private void bishopMove(int index) {
        slideMove(index, 1, 1);
        slideMove(index, 1, -1);
        slideMove(index, -1, 1);
        slideMove(index, -1, -1);
    }

    private void knightMove(int index) {
        int rank = getRank(index);
        int file = getFile(index);
        int i = file == a ? 4 : 0;
        i = file == b ? 2 : i;
        int end = file == h ? 4 : 8;
        end = file == g ? 6 : end;
        for(; i<end; i++) {
            int dest = index + knightMove[i];
            addMove(new ChessMove(this, index, dest), dest);
        }
    }

    private void pawnMove(int index) {
        int rank = getRank(index);
        int file = getFile(index);
        int[] moves = pawnMove[turn];
        int startRank     =  5*turn + 1;
        int promotionRank = 6 - 5*turn;
        rank = getRank(index);
        file = getFile(index);

        int i = file == a ? 1 : 0;
        int end = file == h ? 2 : 3;
        for(; i<end; i++) {
            int dest = index + moves[i];
            // simple push
            if(i%2==1 && pieces[dest] == UNDEFINED) {
                // promotion
                if(rank == promotionRank) {
                    promotion(index, dest);
                    continue;
                }
                addMove(new ChessMove(this, index, dest), dest);
                // double push
                dest = dest + moves[i];
                if(rank == startRank && pieces[dest] == UNDEFINED) {
                    addMove(new ChessMove(this, index, dest), dest);
                }
            }
            else if(i%2==0
                && (pieces[dest] != UNDEFINED
                  && pieces[dest]/BLACK != turn)
                 || dest == enPassantIndex) {
                // promotion
                if(rank == promotionRank) {
                    promotion(index, dest);
                    continue;
                }
                ChessMove newMove = new ChessMove(this, index, dest);
                if(dest == enPassantIndex) {
                    newMove.enPassant();
                }
                addMove(newMove, dest);
            }
        }
    }

    private void promotion(int origin, int destination) {
        ChessMove move;
        move = new ChessMove(this, origin, destination);
        move.setPromotion(QUEEN + turn*BLACK);
        addMove(move, destination);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(ROOK + turn*BLACK);
        addMove(move, destination);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(BISHOP + turn*BLACK);
        addMove(move, destination);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(KNIGHT + turn*BLACK);
        addMove(move, destination);
    }

    private void slideMove(int currIndex, int fileInc, int rankInc) {
        int piece = pieces[currIndex];
        int currFile = getFile(currIndex);
        int currRank = getRank(currIndex);
        int file = currFile + fileInc;
        int rank = currRank + rankInc;
        while (file < size && file >= 0 && rank < size && rank >= 0) {
            int destination = square(file, rank);
            int pieceInDest = pieces[destination];

            // same color piece
            if(pieceInDest != UNDEFINED
            && pieceInDest/BLACK == piece/BLACK) {
                break;
            }

            addMove(new ChessMove(this, currIndex, destination), destination);

            // capture a piece (the color check is inplicit since the program
            // would have left the loop otherwise)
            if(pieceInDest != UNDEFINED) {
                break;
            }
            file = file + fileInc;
            rank = rank + rankInc;
        }
    }

    private void addMove(ChessMove move, int destination) {
        if(destination < 0
           || destination >= nbSquares
           || (pieces[destination]/BLACK == move.getMoving()/BLACK
           && pieces[destination] != UNDEFINED)) {
            return;
        }
        move(move, false);
        updateControl(turn);
        if(!isCheck(1 - turn)) {
            legalMoves.add(move);
        }
        undo();
    }

    /* -----  Updating controls  ----- */
    private void updateControl(int player) {
        if((player==0 && computedControlW)
        || (player==1 && computedControlB)
        || (player==2 && computedControlW && computedControlB)) {
            return;
        }
        control = new int[2][nbSquares];
        int piece;
        for(int i=0; i<nbSquares; i++) {
            piece = pieces[i];
            if(piece != UNDEFINED
            && (piece/BLACK == player
            || player == 2)) {
                updateControlUnique(piece/BLACK, i);
            }
        }
        if(player == 0 || player == 2 ) {
            computedControlW = true;
        }
        if(player == 0 || player == 2 ) {
            computedControlB = true;
        }
    }

    private void updateControlUnique(int player, int index) {
        int piece = pieces[index];
        int rank = getRank(index);
        int file = getFile(index);
        int i, end;
        switch(piece%BLACK) {
            case KING :
                // normal move
                i = file == a ? 3 : 0;
                end = file == h ? 5 : 8;
                for(; i<end; i++) {
                    addControl(player, index + kingMove[i]);
                }
                break;
            case QUEEN :
                // diagonal slide
                slideControl(player, index, 1, 1);
                slideControl(player, index, 1, -1);
                slideControl(player, index, -1, 1);
                slideControl(player, index, -1, -1);
                // no break on purpose
            case ROOK :
                // horizontal slide
                slideControl(player, index, 0, 1);
                slideControl(player, index, 0, -1);
                // vertical slide
                slideControl(player, index, 1, 0);
                slideControl(player, index, -1, 0);
                break;
            case BISHOP :
                // diagonal slide
                slideControl(player, index, 1, 1);
                slideControl(player, index, 1, -1);
                slideControl(player, index, -1, 1);
                slideControl(player, index, -1, -1);
                break;
            case KNIGHT :
                i = file == a ? 4 : 0;
                i = file == b ? 2 : i;
                end = file == h ? 4 : 8;
                end = file == g ? 6 : end;
                for(; i<end; i++) {
                    addControl(player, index + knightMove[i]);
                }
                break;
            case PAWN :
                if(file != a) {
                    addControl(player, index + pawnMove[player][0]);
                }
                if(file != h) {
                    addControl(player, index + pawnMove[player][2]);
                }
                break;
            default:
                System.out.println("Waring (updateControl) : unrecognized piece (" + (piece%BLACK) + ")");
        }
    }

    private void slideControl(int player, int index, int fileInc, int rankInc) {
        int currFile = getFile(index);
        int currRank = getRank(index);
        int file = currFile + fileInc;
        int rank = currRank + rankInc;
        while (file < size && file >= 0 && rank < size && rank >= 0) {
            int destination = square(file, rank);
            int pieceInDest = pieces[destination];
            addControl(player, destination);
            // non empty
            if(pieceInDest != UNDEFINED) {
                break;
            }
            file = file + fileInc;
            rank = rank + rankInc;
        }
    }

    private void addControl(int player, int index) {
        if(index >= 0 && index < nbSquares) {
            control[player][index]++;
        }
    }

    private void reset() {
        for(int i=0; i<nbSquares; i++) { pieces[i] = UNDEFINED; }
    }

    private void boardInit(String boardID) {
        reset();
        int nbBlank = 0;;
        char currChar;
        int charIndex = 0;
        for(int i=size-1; i>=0; i--) {
            for(int j=0; j<size; j++) {
                int index = square(j,i);
                currChar = boardID.charAt(charIndex++);
                if(currChar == '/') { currChar = boardID.charAt(charIndex++); }

                // black pieces are lowercase letters
                int color = 0;
                if (currChar < 'z' && currChar > 'a') {
                    color = 1;
                    currChar += 'A' - 'a';
                }
                // white pieces are uppercase letters
                if (currChar < 'Z' && currChar > 'A') {
                    int pieceValue = pieceValue(currChar);
                    pieces[index] = pieceValue + color * BLACK;
                    if(pieceValue%BLACK == KING) {
                        kingIndex[color] = index;
                    }
                }
                // not a letter -> must be a  number
                else if (currChar > 'Z' || currChar < 'A'){
                    try {
                        // a number means the number of blank squares on a rank
                        nbBlank = Byte.parseByte(Character.toString(currChar));
                        j += (nbBlank-1);
                        continue;
                    }
                    catch(Exception e) {
                        System.out.println("Warning (boardInit) : invalid character : '" + currChar + "'");
                    }
                }

            }
        }

        turn = boardID.charAt(++charIndex) == 'w' ? 0 : 1;
        charIndex++;

        // castling rights
        setCastlingRights(boardID.substring(++charIndex));

        // check for en passant
        while(boardID.charAt(charIndex) != ' ') { charIndex++; }
        charIndex++;
        currChar = boardID.charAt(charIndex);
        if(currChar == '-') { enPassantIndex = -1; }
        else {
            enPassantIndex = stringToSquare(boardID.substring(charIndex));
            charIndex += 2;
        }
        charIndex += 2;

        if(boardID.length() - charIndex < 2) {
            return;
        }
        // number of moves without take
        try {
            nbMovesNoTake = 0;
            while(boardID.charAt(charIndex) != ' ') {
                nbMovesNoTake *= 10;
                nbMovesNoTake += Character.getNumericValue(boardID.charAt(charIndex++));
            }
        } catch(Exception e) { e.printStackTrace(); }

        if(boardID.length() - charIndex < 2) {
            return;
        }
        charIndex++;
        // number of moves
        try {
            nbMoves = Integer.parseInt(boardID.substring(charIndex));
            nbMoves = nbMoves-1*2 + turn;
        } catch(Exception e) { e.printStackTrace(); }
    }

    /* -----  Updating castling rights  ----- */
    private void setCastlingRights(String s) {
        castlingRights = new boolean[4];
        for(int i=0; i<4; i++) {
            castlingRights[i] = false;
        }
        int charIndex = 0;
        castlingRights = new boolean[4];
        if(s.charAt(charIndex) == '-') { return; }
        if(s.charAt(charIndex) == 'K') {
            castlingRights[0] = true;
            charIndex++;
        }
        if(s.charAt(charIndex) == 'Q') {
            castlingRights[1] = true;
            charIndex++;
        }
        if(s.charAt(charIndex) == 'k') {
            castlingRights[2] = true;
            charIndex++;
        }
        if(s.charAt(charIndex) == 'q') {
            castlingRights[3] = true;
            charIndex++;
        }
    }

    private void updateCastlingRights() {
        boolean[] oldCastlingRight = castlingRights;
        castlingRights = new boolean[4];
        // white short castle
        castlingRights[0] = oldCastlingRight[0]
                            && pieces[e] == KING
                            && pieces[h] == ROOK;
        // white long castle
        castlingRights[1] = oldCastlingRight[1]
                            && pieces[e] == KING
                            && pieces[a] == ROOK;
        // black short castle
        castlingRights[2] = oldCastlingRight[2]
                            && pieces[e+56] == KING + BLACK
                            && pieces[h+56] == ROOK + BLACK;
        // black long castle
        castlingRights[3] = oldCastlingRight[3]
                            && pieces[e+56] == KING + BLACK
                            && pieces[a+56] == ROOK + BLACK;
    }

    /* -----  Looking for checks  ----- */
    private boolean isCheck(int player) {
        if(control[1-player][kingIndex[player]] > 0) {
            return true;
        }
        return false;
    }

    /* -----  Updating the Hashmap  ----- */
    private void incrementInHashMap() {
        Long id = toHash();
        Integer value = playedPositions.get(id);
        if(value == null) {
            playedPositions.put(id, Integer.valueOf(1));
            return;
        }
        int nbOccurences = (int) value;
        if(nbOccurences >= 2) {
            isDraw = true;
        }
        playedPositions.replace(id, Integer.valueOf(nbOccurences+1));
    }

/*------------------------------------------------------------------------------
                                GETTERS
------------------------------------------------------------------------------*/
    public int getPiece(int index) {
        if(index >= nbSquares || index < 0) {
            return UNDEFINED;
        }
        return pieces[index];
    }

    public double[] getCastlingRights(){
        double[] toReturn = new double[4];
        for(int i = 0; i < castlingRights.length; i++){
            toReturn[i] = castlingRights[i] ? 1 : -1;
        }
        return toReturn;
    }

    public int[] getSquares() { return pieces; }
    public int getTurn() { return turn; }
    public int getnbMoves(){ return nbMoves; }
    public int getKingPosition(int player) { return kingIndex[player]; }

    public int[] getControl(int player) {
        if(player > 1 || player < 0) {
            return null;
        }
        return control[player];
    }

/*------------------------------------------------------------------------------
                                FORMAT
------------------------------------------------------------------------------*/
    public static int square(int file, int rank) {
        if (file >= size || file < 0 || rank >= size || rank < 0) {
            return -1;
        }
        return rank*size + file;
    }

    public static int getRank(int index) { return index/size; }
    public static int getFile(int index) { return index%size; }

    private void zobristInit() {
        Random r = new Random();
        zobristTable = new long[nbSquares][14];
        for(int i=0; i<nbSquares; i++) {
            for(int j=0; j<14; j++) {
                zobristTable[i][j] = r.nextLong();
            }
        }
    }

    public Long toHash() {
        long h = 0;
        for(int i=0; i<nbSquares; i++) {
            int j = pieces[i];
            h = h ^ zobristTable[i][j];
        }
        return Long.valueOf(h);
    }

    public String gameToPGN() {
        String s = "";
        int move = 0;
        for(BoardState state : new Vector<BoardState>(stateStack)) {
            if(move%2 == 0) {
                s += Integer.toString(move/2 + 1) + ". ";
            }
            move++;
            s += state.lastMove + " ";
        }
        return s;
    }

    public String toString(boolean flipped) {
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";

        String s = "";
        if(!flipped) {
            s += "   | a | b | c | d | e | f | g | h | \n";
        }
        else {
            s += "   | h | g | f | e | d | c | b | a | \n";
        }
        for(byte i=0; i<8; i++) {
            byte raw = i;
            if (!flipped) {
                raw = (byte) (7-i);
            }
            s += "---+---+---+---+---+---+---+---+---+---\n";
            s += " " + Integer.toString(raw+1) + " ";
            for(byte j=0; j<8; j++) {
                byte column = j;
                if(flipped) {
                    column = (byte) (7-j);
                }
                int pieceInSquare = pieces[square(column, raw)];
                s += "| ";
                if(pieceInSquare == UNDEFINED) {
                    s += " ";
                }
                else {
                    if (pieceInSquare/BLACK == 1) {
                        s += ANSI_RED;
                    }
                    else {
                        s += ANSI_GREEN;
                    }
                    String pieceName;
                    s += pieceName(pieceInSquare);
                    s += ANSI_RESET;
                }
                s += " ";
            }
            s += "| " + Integer.toString(raw+1) + "\n";

        }
        s += "---+---+---+---+---+---+---+---+---+---\n";
        if(!flipped) {
            s += "   | a | b | c | d | e | f | g | h | \n";
        }
        else {
            s += "   | h | g | f | e | d | c | b | a | \n";
        }
        return s;
    }

    public String toString() {
        return toString(false);
    }

    public String positionID() {
        String s = "";
        for(int i=size-1; i>=0; i--) {
            int nbBlank = 0;
            for(int j=0; j<size; j++) {
                int index = square(j, i);
                int piece = pieces[index];
                if(piece == UNDEFINED) {
                    nbBlank++;
                    continue;
                }
                if(nbBlank != 0) {
                    s += Integer.toString(nbBlank);
                    nbBlank = 0;
                }
                char pieceName = (char) (pieceName(piece));
                pieceName -= piece/BLACK == 1 ? ('A'-'a') : 0;
                s += Character.toString(pieceName);
            }
            if(nbBlank != 0) {
                s += Integer.toString(nbBlank);
                nbBlank = 0;
            }
            s += i!=0? "/" : "";
        }
        // Turn
        s += (turn==0)? " w " : " b ";

        // castling rights
        s += castlingRights[0] ? "K" : "";
        s += castlingRights[1] ? "Q" : "";
        s += castlingRights[2] ? "k" : "";
        s += castlingRights[3] ? "q" : "";

        s += enPassantIndex != -1 ?
             " " + squareToString(enPassantIndex) + " " :
             " - ";
        s += Integer.toString(nbMovesNoTake) + " ";
        s += Integer.toString(nbMoves/2 + 1);

        return s;

    }

    public static String squareToString(int i) {
        int file = getFile(i);
        int rank = getRank(i);
        return Character.toString((char)('a' + file)) + Integer.toString(rank+1);
    }

    public static int stringToSquare(String s) {
        int file = s.charAt(0) - 'a';
        int rank = Character.getNumericValue(s.charAt(1)) - 1;
        return square(file, rank);
    }

    public void activatePrint() { printGame = true; }

    public void silentMode() { printGame = false; }

	public Player getPlayer(int number){
		return players[number];
	}
}

/*******************************************************************************
                                 BOARD STATE
*******************************************************************************/
class BoardState {
    protected final int enPassantIndex;
    protected final int[] pieces;
    protected final int[] kingIndex;
    protected final int[][] control;
    protected final Vector<Move> legalMoves;
    protected final boolean[] castlingRights;
    protected final int nbMovesNoTake;
    protected final Move lastMove;
    private final HashMap<Long, Integer> playedPositions;
    protected final boolean computedControlW;
    protected final boolean computedControlB;

    BoardState(ChessBoard board) {
        this.enPassantIndex = board.enPassantIndex;
        this.pieces = new int[64];
        for(int i=0; i<64; i++) this.pieces[i] = board.pieces[i];
        this.kingIndex = new int[2];
        this.kingIndex[0] = board.kingIndex[0];
        this.kingIndex[1] = board.kingIndex[1];
        this.control = board.control;
        this.legalMoves = board.legalMoves;
        this.castlingRights = board.castlingRights;
        this.nbMovesNoTake = board.nbMovesNoTake;
        this.lastMove = board.lastMove;
        this.playedPositions = new HashMap<Long, Integer>(board.playedPositions);
        this.computedControlW = board.computedControlW;
        this.computedControlB = board.computedControlB;
    }

    void recoverBoardState(ChessBoard board) {
        board.enPassantIndex = this.enPassantIndex;
        board.pieces = this.pieces;
        board.kingIndex[0] = this.kingIndex[0];
        board.kingIndex[1] = this.kingIndex[1];
        board.control = this.control;
        board.legalMoves = this.legalMoves;
        board.castlingRights = this.castlingRights;
        board.nbMovesNoTake = this.nbMovesNoTake;
        board.lastMove = lastMove;
        board.playedPositions = this.playedPositions;
        board.computedControlW = this.computedControlW;
        board.computedControlB = this.computedControlB;
    }
}
