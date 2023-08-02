package engine;
import java.util.ArrayList;
import java.util.LinkedList;
import engine.TranspositionTable.NodeType;
import java.util.HashSet;

/**
 * @author Dave4243
 * The ChessEngine.java class searches through moves
 * and finds the best move.
 */
public class Search {
	private Evaluator          evaluator;
	private MoveGenerator      generator;
	private TranspositionTable tTable;
	private Board              b;
	
	private long endTime;
	private long absoluteEndTime;
	private int  nodesSearched;
	private int  sorts;
	private int  totalMoves;

	private static final int maxValue = 1000000;
	private static final int minValue = -maxValue;
	public static final int searchAborted = 69000000;
	
	private boolean madeNullMove;
	
	private final int R = 2; // reduction
	
	private Move bestMove;

	public Search(Board b) {
		this.b = b;
		
		evaluator = new Evaluator();
		generator = new MoveGenerator();
		tTable    = new TranspositionTable();
		MoveOrderer.clearKillers();
		MoveOrderer.clearHistory();
	}
	
	/**
	 * Finds the best move given a position
	 * @param b         The chess position represented as a Board.java
	 * @param timeleft  The time left on the move clock
	 * @param increment The increment on the time control
	 * @return The best move found during the time allocated for search
	 */
	public Move getBestMove(Board b, int timeleft, int increment) {
		this.b = b;
		// in milliseconds
		long timeAllowance = (long)((timeleft/20.0 + increment/2.0));
		timeAllowance /= 3;
		
		int eval = b.getSideToMove() == Piece.WHITE ? minValue : maxValue;

		/*
		 ************************************************************************
		 * Iterative Deepening:                                                 *
		 * Start with depth 1 search and increment depth until time runs out.   *
		 * The best sequence of moves from the previous search is searched      *
		 * first in the next iteration, which helps with pruning. This sequence *
		 * is stored in the transposition table as the best moves from each PV  *
		 * position.                                                            *
		 ************************************************************************  
		 */
		long startTime = System.currentTimeMillis();
		endTime = startTime + timeAllowance;
		absoluteEndTime = startTime + timeleft/3;
		Move bestMoveLastIteration = bestMove;
		int depth;
		for (depth = 1; depth < 100; depth++) {
			eval = search(depth, 0, minValue, maxValue);
			
			if (System.currentTimeMillis() >= absoluteEndTime)
				return bestMoveLastIteration;
			
			if (System.currentTimeMillis() > endTime)
				break;
			// mate score
			if (eval >= maxValue-100)
				break;
			bestMoveLastIteration = bestMove;
		}
		
//		printStatistics(depth, eval, System.currentTimeMillis() - startTime);
		return bestMove;
	}
	
	private void printStatistics(int depth, int eval, long time) {
		System.out.print("info ");
		System.out.print("depth "  + depth + " ");
        System.out.print("time "   + time + " ");
		System.out.print("nodes "  + nodesSearched + " ");
		System.out.print("pv ");
		displayPV();
		System.out.println("score cp "  + eval);

		System.out.println();
		
		System.out.println("Total Sorts: " + sorts);
		System.out.println("Total Moves: " + totalMoves);
		nodesSearched = 0;
	}
	
	/**
	 * Searches the position for the best move
	 * @param depth The depth to search to
	 * @param ply   The current distance from the root in half-moves
	 * @param alpha The best score that the player is guaranteed
	 * @param beta  The best score that the opponent is guaranteed
	 * @return The evaluation of the position after playing the best move
	 */
	public int search(int depth, int ply, int alpha, int beta) {
		
		if ((nodesSearched & 2047) == 0 && System.currentTimeMillis() >= absoluteEndTime) {
			return searchAborted;
		}
		boolean foundPV = false;
		if (depth == 0) {
			return quiescenceSearch(alpha, beta);
		}
		
		nodesSearched++;
		
		long key = b.getZobristKey();
		
		if (ply > 0 && b.isRepeat(key)) {
			return 0;
		}
		
		Entry entry = tTable.lookup(key);
		
		if (entry != null && entry.getDepth() >= depth 
				&& Math.abs(entry.getEvaluation()) != searchAborted) {
			if (ply == 0)
				bestMove = entry.getBestMove();
			
			int storedScore = entry.getEvaluation();
			if (entry.getNodeType() == NodeType.EXACT) {
				return storedScore;
			}
			
			else if (entry.getNodeType() == NodeType.LOWER) 
				alpha = Math.max(alpha, storedScore);
			
			else if (entry.getNodeType() == NodeType.UPPER) 
            	beta = Math.min(beta, storedScore);
			
	        if (beta <= alpha) {
	            return beta;
	        }
		}
		
		int side = b.getSideToMove();
		
		// null move pruning
		if (!generator.isInCheck(b, b.getKingpos(side), side)
				&& depth > 2 
				&& ply > 0 
				&& madeNullMove == false
				&& !(beta - alpha > 1)
				&& (Long.bitCount(b.getOccupiedSquares() ^ b.getBitBoard(Piece.WHITE, Piece.PAWN)
						^ b.getBitBoard(Piece.BLACK, Piece.PAWN))) >= 5) {
			b.makeNullMove();
			madeNullMove = true;
			int score = -search(depth - 1 - R, ply + 1, -beta, -beta+1);
        	madeNullMove = false;
			b.unMakeNullMove();
			
			if (score >= beta) return beta;
		}
		
	    ArrayList<Move> moves = generator.generateMoves(b, false);

	    boolean hasLegalMoves = false;
	    
	    if (b.getHalfMoveClock() >= 100) 
	    	return 0;
	    
	    if (entry != null && entry.getNodeType() == NodeType.EXACT) {
	    	MoveOrderer.fullSort(b, moves, entry.getBestMove(), ply);
	    	sorts++;
	    } 
	    else {
	    	MoveOrderer.partialSort(b, moves, moves.size(), ply);
	    	sorts++;
	    }
	    totalMoves += moves.size();
	    
        NodeType type       = NodeType.UPPER;
        Move bestMoveInNode = moves.get(0);

        for (Move move : moves) {
            if (b.doMove(move)) {
            	madeNullMove = false;
	        	hasLegalMoves = true;
	        	int score;
	        	int ext = 0;
	        	if (generator.isInCheck(b, b.getKingpos(1-side), 1-side)){
	        		ext = 1;
	        	}
	        	/* Principal Variation Search:
	        	 * Wait until a move is found that increases alpha, then search all
	        	 * other moves with a null window, with the assumption that the leftmost
	        	 * node that causes a alpha increase is the best move due to move ordering.
	        	 * If this assumption is incorrect and alpha is increased further, then we
	        	 * search the node with a full search to establish its true value.
	        	 */
	        	if (foundPV) {
	        		score = -search(depth-1 + ext, ply+1, -alpha-1, -alpha);
	        		if ((score > alpha) && (score < beta)) {// finds a better move
	        			score = -search(depth-1 + ext, ply+1, -beta, -alpha);
	        		}
	        	} 
	        	else {
	        		score = -search(depth - 1 + ext, ply+1, -beta, -alpha);
	        	}           
	        	b.undoMove(move);
	        	
	        	if (Math.abs(score) > maxValue +1000)
	        		return searchAborted;
	        	
	            if( score > alpha ) {
	            	type           = NodeType.EXACT;
	            	alpha          = score;
	            	bestMoveInNode = move;
	            	
	            	if (ply == 0) bestMove = move;
	            	
	            	if (beta <= score) {
	            		if (move.getCapturedPiece() == null && move.getPromotionPiece() == Piece.NULL) {
	            			storeKiller(ply, move);
	            			storeHistory(depth, side, b.getPiece(move.getFromSquare()).getType(), move.getToSquare());
	            		}
	 
	            		type = NodeType.LOWER;
	            		tTable.store(key, move, beta, depth, b.getMoveNumber(), type);
	            		return beta; 
	            	}
	            	foundPV = true;
	            }
            }
            else b.undoMove(move);
        }
        
        if (!hasLegalMoves) {
	    	// sees if the king is currently in check in this position
	    	if (!generator.isInCheck(b, b.getKingpos(side), side)) {
	    		return 0;
	    	}
	        return minValue + ply;
        }
        tTable.store(key, bestMoveInNode, alpha, depth, b.getMoveNumber(), type);
        return alpha;
	}
	
	/**
	 * Reduces the "horizon effect" 
	 * @param alpha 
	 * @param beta
	 * @return The evaluation of the position, after all meaningful captures are
	 * made
	 */
	public int quiescenceSearch(int alpha, int beta) {
		if ((nodesSearched & 2047) == 0 && System.currentTimeMillis() >= absoluteEndTime) {
			return searchAborted;
		}
		
		nodesSearched++;
		int eval = evaluator.evaluatePosition(b);
		
		if (eval >= beta)
			return beta;
		
		if (alpha < eval)
			alpha = eval;
		
		ArrayList<Move> captures = generator.generateMoves(b, true);
		MoveOrderer.fullSort(b, captures);
		sorts++;
		totalMoves += captures.size();
		for (int i = 0; i < captures.size(); i++) {
			Move m = captures.get(i);
			if (b.doMove(m)) {
				int score = -quiescenceSearch(-beta, -alpha);
				b.undoMove(m);
				
				if (score >= beta) return beta;
				if (score > alpha) alpha = score;
			}
			else b.undoMove(m);
		}
		return alpha;
	}
	
	private void storeKiller(int ply, Move m) {
		Move[] storedKillers = MoveOrderer.killerTable[ply];
		for (int i = 0; i < 2; i++) {
			if (m.equals(storedKillers[i])) {
				return;
			}
			if (storedKillers[i] == null) {
				MoveOrderer.killerTable[ply][i] = m;
				return;
			}
		}
		MoveOrderer.killerTable[ply][0] = MoveOrderer.killerTable[ply][1];
		MoveOrderer.killerTable[ply][1] = m;
	}
	
	private void storeHistory(int depth, int color, int pieceType, int dest) {
		// alternatively 2^depth or 1 << depth
		MoveOrderer.historyTable[color][pieceType][dest] += depth * depth; 
		
		if (MoveOrderer.historyTable[color][pieceType][dest] >= 1024) {
			ageHistory();
		}
	}
	
	private void ageHistory() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				for (int x = 0; x < 64; x++) {
					MoveOrderer.historyTable[i][j][x] >>>= 3; // divided by 8
				}
			}
		}
	}
	/**
	 * Displays the principal variation (engine line)
	 */
	private void displayPV() {
		LinkedList<Move> pv = new LinkedList<Move>();
		// for the purpose of stopping infinite loop due to position repeat (circle)
		HashSet<Long> keys = new HashSet<Long>();
		
		while (tTable.contains(b.getZobristKey())) {
			if (keys.contains(b.getZobristKey())) 
				break;
			keys.add(b.getZobristKey());
			pv.add(tTable.lookup(b.getZobristKey()).getBestMove());
			if (!b.doMove(pv.peekLast())){
				b.undoMove(pv.removeLast());
				break;
			}
		}
		System.out.println(b);
		
		for (Move m : pv) {
			System.out.print(m + " ");
		}
		
		while (pv.peekLast() != null) {
			b.undoMove(pv.removeLast());
		}
	}
}