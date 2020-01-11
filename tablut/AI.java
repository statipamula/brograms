package tablut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

import static tablut.Square.ROOK_SQUARES;
import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Santosh Tatipamula
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    /** creates AI. **/
    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }
    /** returns AI move. **/
    @Override
    String myMove() {
        Move  m = findMove();
        _controller.reportMove(m);
        return m.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        if (this.myPiece() == BLACK) {
            findMove(b, 2, true, 1, -INFTY, INFTY);
        }
        if (this.myPiece() == WHITE) {
            findMove(b, 2, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            HashMap<Move, Integer> moves = new HashMap<>();
            int maxeval = -INFTY;
            for (Move m : board.legalMoves((BLACK))) {
                board.makeMove(m);
                Board b = new Board(board);
                board.undo();
                int eval = findMove(b, depth - 1, true, -1, alpha, beta);
                moves.put(m, eval);
                maxeval = max(maxeval, eval);
                alpha = max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            int max = Collections.max(moves.values());
            for (Move m : moves.keySet()) {
                if (moves.get(m) == max) {
                    _lastFoundMove = m;
                }
            }
            return maxeval;
        }
        if (sense == -1) {
            HashMap<Move, Integer> moves = new HashMap<>();
            int mineval = +INFTY;
            for (Move m: board.legalMoves((WHITE))) {
                board.makeMove(m);
                Board b = new Board(board);
                board.undo();
                int eval = findMove(b, depth - 1, true, 1, alpha, beta);
                moves.put(m, eval);
                mineval = min(mineval, eval);
                beta = min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            int min = Collections.min(moves.values());
            for (Move m : moves.keySet()) {
                if (moves.get(m) == min) {
                    _lastFoundMove = m;
                    break;
                }
            }
            return mineval;
        }
        return 0;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 4;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        if (board.winner() == WHITE) {
            return -INFTY;
        }
        if (board.winner() == BLACK) {
            return INFTY;
        }
        int dist = distfromedge(board);
        int kingdist = kingrookmoveedge(board);
        int kingsurround = kingsurrounded(board);
        int numblack = board.pieceLocations(BLACK).size() * 3;
        int numwhite = board.pieceLocations(WHITE).size() * -4;
        int capbyblack = 0;
        int capbywhite = 0;
        if (!board.capbyblack().isEmpty()) {
            capbyblack = board.capbyblack().size() * 10;
            System.out.println(capbyblack);
        }
        if (!board.capbywhite().isEmpty()) {
            for (Map.Entry mapelement: board.capbywhite().entrySet()) {
                capbywhite -= 2;
            }
        }

        return dist + numblack + numwhite
                + capbyblack + capbywhite + kingsurround + kingdist;
    }

    /**Return a value for a side's distance from edge given BOARD.**/
    private int distfromedge(Board board) {
        int count = 0;
        List<Square> blacks =
                new ArrayList<Square>(board.pieceLocations(BLACK));
        List<Square> whites
                = new ArrayList<Square>(board.pieceLocations(WHITE));
        for (Square s : whites) {
            for (int i = 0; i < 4; i++) {
                if (ROOK_SQUARES[s.index()][i].size() > 0) {
                    int max = ROOK_SQUARES[s.index()][i].size() - 1;
                    Square sq = ROOK_SQUARES[s.index()][i].get(max);
                    if (board.isUnblockedMove(s, sq)) {
                        count += 5;
                    }
                }
            }
        }
        return count;
    }

    /**Return a value for kings distance from edge given BOARD.**/
    private int kingrookmoveedge(Board board) {
        int count = 0;
        Square pos = board.kpos();
        for (int i = 0; i < 4; i++) {
            int max = ROOK_SQUARES[pos.index()][i].size() - 1;
            Square sq = ROOK_SQUARES[pos.index()][i].get(max);
            if (board.isUnblockedMove(board.kpos(), sq)) {
                count += 10;
            }
        }
        return count;
    }

    /**Return a value for number of blacks around king given BOARD.**/
    private int kingsurrounded(Board board) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            HashSet<Square> newl = board.pieceLocations(BLACK);
            if (newl.contains(board.kpos().rookMove(i, 1))) {
                count += 10;
            }
        }
        return count;
    }
}
