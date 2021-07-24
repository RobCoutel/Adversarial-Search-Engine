/*------------------------------------------------------------------------------
---- File information ----
Name of project : Chess Engine
Class : Board.java
Date : March - 2021
Author : Robin Coutelier
Co-author : Gilles Pirenne
------------------------------------------------------------------------------*/

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

    Move getLastMove()
        Arguments :
            void
        Return :
            Move lastMove : The last instance of Move played on the board

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
import java.util.HashMap;
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

/*------------------------------------------------------------------------------
                                    PIECES
------------------------------------------------------------------------------*/
    public static final int UNDEFINED = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 6;
    public static final int KING = 7;

    public static final int ROOK_VALUE = 5;
    public static final int BISHOP_VALUE = 3;
    public static final int KNIGHT_VALUE = 3;
    public static final int QUEEN_VALUE = 9;
    public static final int PAWN_VALUE = 1;

    public static final int BLACK = 8;

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

    private int[] pieces;
    private int [] kingIndex;
    private int[] whiteControls, blackControls;

    private boolean[] castlingRights;
    private int turn;

    private int nbMovesNoTake = 0;

    private MoveStack moves;
    private Vector<Move> legalMoves;
    private HashMap<Long, Integer> playedPositions;
    private boolean isDraw = false;

    private String startingPosition;
    private int moveNumber;

    private int result = 2;
    private boolean computedResult = false;

/*------------------------------------------------------------------------------
                                 CONSTRUCTORS
------------------------------------------------------------------------------*/
    public ChessBoard(String boardID, Player white, Player black) {
        construct(boardID, white, black);
    }

    public ChessBoard(Player white, Player black) {
        construct("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr/11110 null", white, black);
    }

    private void construct(String boardID, Player white, Player black) {
        startingPosition = boardID;

        moveNumber = 0;

        players = new Player[2];
        players[0] = white;
        players[1] = black;

        moves = new MoveStack();
        pieces = new int[nbSquares];

        kingIndex = new int[2];
        boardInit(boardID);

        whiteControls = new int[nbSquares];
        blackControls = new int[nbSquares];
        resetControl();

        legalMoves = new Vector<Move>();

        int initialCapacity = 200;
        playedPositions = new HashMap<Long, Integer>(initialCapacity);
        incrementInHashMap();

        this.updateControl();
        this.updateLegalMoves();
    }

    public ChessBoard clone() {
        ChessBoard clone = new ChessBoard(this.positionID(), players[0], players[1]);
        clone.nbMovesNoTake = this.nbMovesNoTake;
        clone.moveNumber = this.moveNumber + 1;
        // remove the wrong last move of the clone
        clone.moves.pop();
        // clone the last move - can't reconstruct it only from string
        ChessMove lastMove = moves.top();
        if(lastMove != null) {
            clone.moves.push(lastMove.clone(clone));
        }

        clone.playedPositions = new HashMap<Long, Integer>(playedPositions);

        clone.updateControl();
        clone.updateLegalMoves();
        return clone;
    }

/*------------------------------------------------------------------------------
                                  CORE CODE
------------------------------------------------------------------------------*/
    public void play() {
        while(gameOver() == 2) {
            if(printGame) {
                System.out.println(this);
                System.out.println(players[turn].getName() + " to play!");
            }
            moveNumber += 1;

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
                try {
                    Thread.sleep(100);
                } catch(Exception e){}
                continue;
            }
            //System.out.println("Move played : " + toPlay);
            move(toPlay.clone(this));
            //try{Thread.sleep(10);} catch(Exception e){}
        }
        if(printGame) {
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
        if(toPlay == null) {
            throw new IllegalArgumentException("Error in ChessBoard.move :"
            + " The move provided was null");
        }
        if(toPlay.isResignation()) {
            result = turn==0? -1 : 1;
            computedResult = true;
            return;
        }

        ChessMove move = (ChessMove) toPlay;
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
                int takenIndex = move.getDestination() + ((turn==0)?-8:+8);
                pieces[takenIndex] = UNDEFINED;
                break;
            default :
                pieces[move.getDestination()] = move.getMoving();
                pieces[move.getOrigin()] = UNDEFINED;
                if(move.getMoving()%BLACK == KING) {
                    kingIndex[turn] = move.getDestination();
                }
        }

        int taken = move.getTaken();
        int moving = move.getMoving();

        // save castlingRights and number of moves without take of push
        move.setPreviousCastlingRights((boolean[]) castlingRights.clone());
        move.setPreviousNbMovesNoTake(nbMovesNoTake);


        moves.push(move);
        updateCastlingRights();

        if(taken == UNDEFINED && moving%BLACK != PAWN) {
            nbMovesNoTake++;
        }
        else {
            nbMovesNoTake = 0;
        }
        turn = 1 - turn;
        updateControl();

        // update HashMap
        incrementInHashMap();
        computedResult = false;
    }

    public int getMoveNumber(){ return moveNumber; }

    public void undo() {
        // update HashMap
        decrementInHashMap();
        ChessMove lastMove = moves.pop();
        turn = 1 - turn;
        int moveType = lastMove.moveType();
        switch(moveType) {
            case ChessMove.SHORT_CASTLE :
                pieces[e + turn*56] = KING + turn*BLACK;
                pieces[f + turn*56] = UNDEFINED;
                pieces[g + turn*56] = UNDEFINED;
                pieces[h + turn*56] = ROOK + turn*BLACK;
                kingIndex[turn] = e + turn*56;
                break;

            case ChessMove.LONG_CASTLE :
                pieces[e + turn*56] = KING + turn*BLACK;
                pieces[d + turn*56] = UNDEFINED;
                pieces[c + turn*56] = UNDEFINED;
                pieces[a + turn*56] = ROOK + turn*BLACK;
                kingIndex[turn] = e + turn*56;
                break;

            case ChessMove.EN_PASSANT :
                pieces[lastMove.getDestination()] = UNDEFINED;
                pieces[lastMove.getOrigin()] = lastMove.getMoving();
                int takenIndex = lastMove.getDestination() + ((turn==0)?-8:+8);
                pieces[takenIndex] = lastMove.getTaken();
                break;

            default :
                pieces[lastMove.getDestination()] = lastMove.getTaken();
                pieces[lastMove.getOrigin()] = lastMove.getMoving();
                if(lastMove.getMoving()%BLACK == KING) {
                    kingIndex[turn] = lastMove.getOrigin();
                }
        }
        // recover castling rights and number of moves withou take of push
        castlingRights = lastMove.getPreviousCastlingRights();
        nbMovesNoTake = lastMove.getPreviousNbMovesNoTake();
        updateControl();
    }

    public void updateLegalMoves() {
        legalMoves.clear();
        int piece;
        for(int i=0; i<nbSquares; i++) {
            piece = pieces[i];
            if(piece != UNDEFINED && piece/BLACK == turn) {
                updateLegalMovesUnique(i);
            }
        }
    }

    private void updateLegalMovesUnique(int index) {
        int piece = pieces[index];
        int rank = getRank(index);
        int file = getFile(index);
        switch(piece%BLACK) {
            case KING :
                // normal move
                for(int i=-1; i<=1; i++) {
                    for(int j=-1; j<=1; j++) {
                        if(j == 0 && i == 0) { continue; }
                        addMove(new ChessMove(this, index, square(file+i, rank+j)));
                    }
                }
                // short castle
                int[] control = turn==0? blackControls : whiteControls;
                if(castlingRights[2*turn]
                 && pieces[index+1] == UNDEFINED
                 && pieces[index+2] == UNDEFINED
                 && control[index] == 0 // is check?
                 && control[index+1] == 0
                 && control[index+2] == 0) {
                     addMove(new ChessMove(this, "0-0"));
                }
                // long castle
                if(castlingRights[2*turn+1]
                 && pieces[index-1] == UNDEFINED
                 && pieces[index-2] == UNDEFINED
                 && pieces[index-3] == UNDEFINED
                 && control[index] == 0 // is check?
                 && control[index-1] == 0
                 && control[index-2] == 0) {
                     addMove(new ChessMove(this, "0-0-0"));
                }
                break;
            case QUEEN :
                // diagonal slide
                slideMove(index, 1, 1);
                slideMove(index, 1, -1);
                slideMove(index, -1, 1);
                slideMove(index, -1, -1);
                // no break on purpose

            case ROOK :
                // horizontal slide
                slideMove(index, 0, 1);
                slideMove(index, 0, -1);
                // vertical slide
                slideMove(index, 1, 0);
                slideMove(index, -1, 0);
                break;
            case BISHOP :
                // diagonal slide
                slideMove(index, 1, 1);
                slideMove(index, 1, -1);
                slideMove(index, -1, 1);
                slideMove(index, -1, -1);
                break;

            case KNIGHT :
                addMove(new ChessMove (this, index, square(file + 2, rank + 1)));
                addMove(new ChessMove (this, index, square(file + 2, rank - 1)));
                addMove(new ChessMove (this, index, square(file - 2, rank + 1)));
                addMove(new ChessMove (this, index, square(file - 2, rank - 1)));
                addMove(new ChessMove (this, index, square(file + 1, rank + 2)));
                addMove(new ChessMove (this, index, square(file + 1, rank - 2)));
                addMove(new ChessMove (this, index, square(file - 1, rank + 2)));
                addMove(new ChessMove (this, index, square(file - 1, rank - 2)));
                break;

            case PAWN :
                int direction = -1;
                int startRank = 6;
                int promotionRank = 1;
                if(turn == 0) {
                    direction = 1;
                    startRank = 1;
                    promotionRank = 6;
                }
                rank = getRank(index);
                file = getFile(index);
                // promotions
                if(rank == promotionRank) {
                    int dest = square(file, rank+direction);
                    if(dest >= 0 && pieces[dest] == UNDEFINED) {
                        promotion(index, dest);
                    }
                    dest = square(file-1, rank+direction);
                    // take left
                    if(dest >= 0
                    && pieces[dest] != UNDEFINED
                    && pieces[dest]/BLACK != turn) {
                        promotion(index, dest);
                    }
                    // take right
                    dest = square(file+1, rank+direction);
                    if(dest >= 0
                    && pieces[dest] != UNDEFINED
                    && pieces[dest]/BLACK != turn) {
                        promotion(index, dest);
                    }
                }
                else {
                    int dest = square(file, rank+direction);
                    // pushing
                    if(dest>= 0 && pieces[dest] == UNDEFINED) {
                        addMove(new ChessMove(this, index, square(file, rank + direction)));
                        if (rank == startRank
                        && pieces[square(file, rank+2*direction)] == UNDEFINED) {
                            addMove(new ChessMove(this, index, square(file, rank + 2*direction)));
                        }
                    }
                    // take left
                    pawnTake(index, square(file-1, rank+direction));
                    // take right
                    pawnTake(index, square(file+1, rank+direction));
                    // en passant left
                    enPassant(index, square(file-1, rank+direction));
                    // en passant right
                    enPassant(index, square(file+1, rank+direction));
                }
                break;

            default:
                System.out.println("Waring (updateLegalMoves) : unrecognized piece (" + (piece%BLACK) + ")");
        }
    }

    public void updateControl() {
        resetControl();
        int piece;
        for(int i=0; i<nbSquares; i++) {
            piece = pieces[i];
            if(piece != UNDEFINED) {
                updateControlUnique(piece/BLACK, i);
            }
        }
    }

    private void updateControlUnique(int player, int index) {
        int piece = pieces[index];
        int rank = getRank(index);
        int file = getFile(index);
        switch(piece%BLACK) {
            case KING :
            // normal move
            for(int i=-1; i<=1; i++) {
                for(int j=-1; j<=1; j++) {
                    if(j == 0 && i == 0) { continue; }
                    addControl(player, square(file+i, rank+j));
                }
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
            addControl(player, square(file + 2, rank + 1));
            addControl(player, square(file + 2, rank - 1));
            addControl(player, square(file - 2, rank + 1));
            addControl(player, square(file - 2, rank - 1));
            addControl(player, square(file + 1, rank + 2));
            addControl(player, square(file + 1, rank - 2));
            addControl(player, square(file - 1, rank + 2));
            addControl(player, square(file - 1, rank - 2));
            break;
            case PAWN :
            int direction = player==0 ? 1 : -1;
            addControl(player, square(file+1, rank+direction));
            addControl(player, square(file-1, rank+direction));
            break;
            default:
            System.out.println("Waring (updateControl) : unrecognized piece (" + (piece%BLACK) + ")");
        }
    }

    public int gameOver() {
        if(computedResult) {
            return result;
        }

        updateLegalMoves();
        updateControl();
        if(legalMoves.isEmpty()) {
            if(isCheck(turn)) {
                if(debug) {
                    System.out.println("Checkmate");
                }
                result = turn == 0 ? -1 : 1;
                return result;
            }
            if(debug) {
                System.out.println("Stale mate");
            }
            result = 0;
            return result;
        }
        // 50 moves without take
        if(nbMovesNoTake >= 50) {
            if(debug) {
                System.out.println("50 moves without taking");
            }
            result = 0;
            return result;
        }
        // 3 fold repetition
        if(isDraw) {
            if(debug) {
                System.out.println("3 fold repetition");
            }
            result = 0;
            return result;
        }
        // game not over
        result = 2;
        return result;
    }

    public void replay(int timeBetweenMoves) {
        boardInit(startingPosition);
        System.out.println("Replay");
        while (!moves.reachedHead()) {
            move(moves.head().clone(this));
            try {
              Thread.sleep(timeBetweenMoves);
            } catch(Exception e){}
            moves.pop();
        }
        moves.resetHead();
    }

/*------------------------------------------------------------------------------
                            SIDE FUNCTIONS
------------------------------------------------------------------------------*/
    private void slideMove(int currIndex, int fileInc, int rankInc) {
        int piece = pieces[currIndex];
        int currFile = getFile(currIndex);
        int currRank = getRank(currIndex);
        int file = currFile + fileInc;
        int rank = currRank + rankInc;
        while (file < size && file >= 0 && rank < size && rank >= 0) {
            int destination = square(file, rank);
            int pieceInDest = pieces[destination];
            // empty square
            if(pieceInDest == UNDEFINED) {
                addMove(new ChessMove(this, currIndex, destination));
            }
            // same color piece
            else if(pieceInDest/BLACK == piece/BLACK) {
                break;
            }
            // different color piece
            else if(pieceInDest/BLACK != piece/BLACK) {
                addMove(new ChessMove(this, currIndex, destination));
                break;
            }
            file = file + fileInc;
            rank = rank + rankInc;
        }
    }

    private void pawnTake(int index, int dest) {
        if(dest >= 0
           && pieces[dest] != UNDEFINED
           && pieces[dest]/BLACK != turn) {
            addMove(new ChessMove(this, index, dest));
        }
    }

    private void enPassant(int index, int dest) {
        if(dest < 0 || getRank(index) != 4 - turn) {
            return;
        }
        int destFile = getFile(dest);
        int takenIndex = dest + (turn==0? -8:8);
        if(takenIndex >= 0
           && pieces[takenIndex] == PAWN + (1 - turn)*BLACK
           && !moves.isEmpty()
           && moves.top().isPawnPush2(destFile)) {
            ChessMove newMove = new ChessMove(this, index, dest);
            newMove.enPassant();
            addMove(newMove);
        }
    }

    private void promotion(int origin, int destination) {
        ChessMove move;
        move = new ChessMove(this, origin, destination);
        move.setPromotion(QUEEN + turn*BLACK);
        addMove(move);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(ROOK + turn*BLACK);
        addMove(move);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(BISHOP + turn*BLACK);
        addMove(move);
        move = new ChessMove(this, origin, destination);
        move.setPromotion(KNIGHT + turn*BLACK);
        addMove(move);
    }

    private void addMove(ChessMove move) {
        int destination = move.getDestination();
        if(destination < 0
           || destination > nbSquares
           || (pieces[destination]/BLACK == move.getMoving()/BLACK
           && pieces[destination] != UNDEFINED)) {
            return;
        }
        move(move);
        if(!isCheck(1 - turn)) {
            legalMoves.add(move);
        }
        undo();
    }

    private void slideControl(int player, int index, int fileInc, int rankInc) {
        int currFile = getFile(index);
        int currRank = getRank(index);
        int file = currFile + fileInc;
        int rank = currRank + rankInc;
        while (file < size && file >= 0 && rank < size && rank >= 0) {
            int destination = square(file, rank);
            int pieceInDest = pieces[destination];
            // empty square
            if(pieceInDest == UNDEFINED) {
                addControl(player, destination);
            }
            else  {
                addControl(player, destination);
                break;
            }
            file = file + fileInc;
            rank = rank + rankInc;
        }
    }

    private void addControl(int player, int index) {
        if(index >= 0 && index < nbSquares) {
            int tmp = player==0? whiteControls[index]++ : blackControls[index]++;
        }
    }

    private void reset() {
        for(int i=0; i<nbSquares; i++) { pieces[i] = UNDEFINED; }
    }

    private void resetControl() {
        for(int i=0; i<nbSquares; i++) {
            whiteControls[i] = 0;
            blackControls[i] = 0;
        }
    }

    private void boardInit(String boardID) {
        reset();
        int nbBlank = 0;;
        char currChar;
        int charIndex = 0;
        for(byte i=0; i<nbSquares; i++) {
            currChar = boardID.charAt(charIndex++);
            if(currChar == '/') {
                i--;
                continue;
            }
            // white pieces are uppercase letters
            if (currChar < 'Z' && currChar > 'A') {
                pieces[i] = pieceValue(currChar);
                if(pieces[i]%BLACK == KING) {
                    kingIndex[0] = i;
                }
            }
            // black pieces are lowercase letters
            if (currChar < 'z' && currChar > 'a') {
                pieces[i] = BLACK;
                currChar += 'A' - 'a';
                pieces[i] += pieceValue(currChar);
                if(pieces[i]%BLACK == KING) {
                    kingIndex[1] = i;
                }
            }
            // not a letter -> must be a  number
            else if (currChar > 'Z' || currChar < 'A'){
                try {
                    // a number means the number of blank squares on a rank
                    nbBlank = Byte.parseByte(Character.toString(currChar++));
                    i += (nbBlank-1);
                    continue;
                }
                catch(Exception e) {
                    System.out.println("Warning (boardInit) : invalid caracter : '" + currChar + "'");
                }
            }
        }
        setCastlingRights(boardID.substring(charIndex+1));
    }

    private void setCastlingRights(String s) {
        if(s.equals(" ")) {
            castlingRights = new boolean[4];
            castlingRights[0] = true;
            castlingRights[1] = true;
            castlingRights[2] = true;
            castlingRights[3] = true;
            turn = 0;
            return;
        }
        int charIndex = 0;

        castlingRights = new boolean[4];
        castlingRights[0] = s.charAt(charIndex++) == '1';
        castlingRights[1] = s.charAt(charIndex++) == '1';
        castlingRights[2] = s.charAt(charIndex++) == '1';
        castlingRights[3] = s.charAt(charIndex++) == '1';


        turn = (int) (s.charAt(charIndex++) - '0');
        charIndex++;
        String substring = s.substring(charIndex);
        if(substring.equals("null")) {
            return;
        }
        moves.push(new ChessMove(this, substring));
    }

    private void updateCastlingRights() {
        // white castlingRights
        if(pieces[e] != KING) {
            castlingRights[0] = false;
            castlingRights[1] = false;
        }
        else {
            if(pieces[h] != ROOK) {
                castlingRights[0] = false;
            }
            if(pieces[a] != ROOK) {
                castlingRights[1] = false;
            }
        }
        // black castling rights
        if(pieces[e+56] != KING + BLACK) {
            castlingRights[2] = false;
            castlingRights[3] = false;
        }
        else {
            if(pieces[h+56] != ROOK + BLACK) {
                castlingRights[2] = false;
            }
            if(pieces[a+56] != ROOK + BLACK) {
                castlingRights[3] = false;
            }
        }
    }

    private boolean isCheck(int player) {
        int[] control = player==0? blackControls : whiteControls;
        if(control[kingIndex[player]] > 0) {
            return true;
        }
        return false;
    }

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

    private void decrementInHashMap() {
        Long id = toHash();
        Integer value = playedPositions.get(id);
        if(value == null) { return; }
        int nbOccurences = (int) value;
        if(nbOccurences == 1) {
            playedPositions.remove(id);
            return;
        }
        if(nbOccurences == 3) {
            isDraw = false;
        }
        playedPositions.replace(id, Integer.valueOf(nbOccurences-1));
    }

/*------------------------------------------------------------------------------
                                GETTERS
------------------------------------------------------------------------------*/
    public int getPiece(int index) {
        if(index > nbSquares || index < 0) {
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

    public Vector<Move> getLegalMoves() { return new Vector<Move>(legalMoves); }

    public int getTurn() { return turn; }

    public Move getLastMove() { return moves.top(); }

    public int[] getControl(int player) {
        if(player > 1) {
            return null;
        }
        return player==0? whiteControls : blackControls;
    }

    public int getKingPosition(int player) { return kingIndex[player]; }

/*------------------------------------------------------------------------------
                                FORMAT
------------------------------------------------------------------------------*/
    public int square(int file, int rank) {
        if (file >= size || file < 0 || rank >= size || rank < 0) {
            return -1;
        }
        return rank*size + file;
    }

    public int getRank(int index) { return index/size; }

    public int getFile(int index) { return index%size; }

    private Long toHash() {
        long toReturn = 0;
        long nbBlank = 0;
        long base = 127;
        long currBaseValue = base * base;
        for(int i=0; i<nbSquares; i++) {
            if(pieces[i] == UNDEFINED) {
                nbBlank++;
                continue;
            }
            if(nbBlank != 0) {
                toReturn += nbBlank * currBaseValue;
                nbBlank = 0;
            }
            toReturn +=  pieces[i] * currBaseValue;
            currBaseValue *= base;
        }
        return Long.valueOf(toReturn);
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
        int nbBlank = 0;
        for(int i=0; i<nbSquares; i++) {
            if(i != 0 && i%size == 0) {
                if(nbBlank != 0) {
                    s += Integer.toString(nbBlank);
                    nbBlank = 0;
                }
                s += "/";
            }
            int piece = pieces[i];
            if(piece == UNDEFINED) {
                nbBlank++;
                if(nbBlank == 8) {
                    s += "8";
                    nbBlank = 0;
                }
                continue;
            }
            if(nbBlank != 0) {
                s += Integer.toString(nbBlank);
                nbBlank = 0;
            }
            if(piece/BLACK == 0) {
                s += pieceName(piece);
                continue;
            }
            s += Character.toString((char)(pieceName(piece) - ('A'-'a')));
        }
        if(nbBlank != 0) {
            s += Integer.toString(nbBlank);
            nbBlank = 0;
        }
        // castling rights
        s += "/";
        for(int i=0; i<4; i++) {
            s += castlingRights[i]? "1" : "0";
        }

        // Turn
        s += (turn==0)? "0" : "1";
        if(moves.isEmpty()) {
            s += " null";
            return s;
        }
        s += " " + moves.top();

        return s;

    }

    public String controlToString(int player, boolean flipped) {
        final String ANSI_BG_BLACK  = "\u001B[40m";

        final String ANSI_BRIGHT_BG_YELLOW = "\u001B[103m";
        final String ANSI_BRIGHT_BG_CYAN   = "\u001B[106m";
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";

        String toReturn = "";
        if(!flipped) {
            toReturn += "   | a | b | c | d | e | f | g | h | \n";
        }
        else {
            toReturn += "   | h | g | f | e | d | c | b | a | \n";
        }
        for(byte i=0; i<8; i++) {
            byte raw = i;
            if (!flipped) {
              raw = (byte) (7-i);
            }
            toReturn += "---+---+---+---+---+---+---+---+---+---\n";
            toReturn += " " + Integer.toString(raw+1) + " ";
            for(byte j=0; j<8; j++) {
                byte column = j;
                if(flipped) {
                column = (byte) (7-j);
            }
                int pieceInSquare = pieces[square(column, raw)];
                toReturn += "|";

                if(player == 0) {
                    if (whiteControls[square(column, raw)] != 0) {
                        toReturn += ANSI_BRIGHT_BG_CYAN;
                    }
                }
                else {
                    if (blackControls[square(column, raw)] != 0) {
                        toReturn += ANSI_BRIGHT_BG_YELLOW;
                    }
                }

                if(pieceInSquare == UNDEFINED) {
                    toReturn += "   ";
                }
                else {
                    if (pieceInSquare/BLACK == 0) {
                        toReturn += ANSI_GREEN;
                    }
                    else {
                        toReturn += ANSI_RED;
                    }
                    toReturn += " " + Character.toString(pieceName(pieceInSquare)) + " ";
                    toReturn += ANSI_RESET;
                }
                toReturn += ANSI_BG_BLACK;
            }
            toReturn += "| " + Integer.toString(raw+1) + "\n";
        }
        toReturn += "---+---+---+---+---+---+---+---+---+---\n";
        if(!flipped) {
            toReturn += "   | a | b | c | d | e | f | g | h | \n";
        }
        else {
            toReturn += "   | h | g | f | e | d | c | b | a | \n";
        }
        return toReturn;
    }

    public void activatePrint() { printGame = true; }

    public void silentMode() { printGame = false; }

	public Player getPlayer(int number){
		return players[number];
	}
}

class MoveStack {
    private final int initSize = 10;
    private ChessMove[] stack;
    private int nbElements;
    private int head = 0;

    public MoveStack() {
        stack = new ChessMove[initSize];
        nbElements = 0;
    }

    private void rescale() {
        ChessMove[] newStack = new ChessMove[2*stack.length];
        for(int i=0; i<nbElements; i++) {
            newStack[i] = stack[i];
        }
        stack = newStack;
    }

    public void push(ChessMove move) {
        if(nbElements >= stack.length) {
            rescale();
        }
        stack[nbElements++] = move;
    }

    public ChessMove pop() {
        if (nbElements == 0) {
            return null;
        }
        return stack[--nbElements];
    }

    public ChessMove top() {
        if (nbElements == 0) {
            return null;
        }
        return stack[nbElements-1];
    }

    public boolean isEmpty() {
        return (nbElements == 0);
    }

    public ChessMove head() {
        if (head >= nbElements) {
            return null;
        }
        return stack[head++];
    }

    public boolean reachedHead() {
        //System.out.println("Head : " + head + ", nbElements : " + nbElements);
        return head >= nbElements;
    }

    public void resetHead() {
        head = 0;
    }

    public int getSize(){
        return stack.length;
    }
}
