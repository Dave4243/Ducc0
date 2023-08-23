package engine;
import java.util.ArrayList;
import engine.TranspositionTable.NodeType;

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

	private static final int maxValue = 1000000;
	private static final int minValue = -maxValue;
	private static final int searchAborted = 69000000;
	
	private static final int maxDepth = 100;
	
	private boolean madeNullMove;
	
	private int[] pvLength;
	private Move[][] pvTable;
	
	private Move bestMove;

	public Search(Board b) {
		this.b = b;
		
		evaluator = new Evaluator();
		generator = new MoveGenerator();
		tTable    = new TranspositionTable();
		MoveOrderer.clearKillers();
		MoveOrderer.clearHistory();
		
		pvLength = new int[maxDepth];
		pvTable  = new Move[maxDepth][maxDepth];
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
		for (depth = 1; depth < maxDepth; depth++) {
			eval = search(depth, 0, minValue, maxValue);
			printStatistics(depth, eval, System.currentTimeMillis() - startTime);
			if (System.currentTimeMillis() >= absoluteEndTime)
				return bestMoveLastIteration;
			
			if (System.currentTimeMillis() > endTime)
				break;
			
			// mate score
			if (eval >= maxValue - maxDepth)
				break;
			
			bestMoveLastIteration = bestMove;
		}

		return bestMove;
	}
	
	private void printStatistics(int depth, int eval, long time) {
		System.out.print("info ");
		System.out.print("depth " + depth         + " ");
        System.out.print("time "  + time          + " ");
		System.out.print("nodes " + nodesSearched + " ");
		System.out.print("pv ");
		displayPV();
		System.out.println("score cp "  + eval);
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
		
		pvLength[ply] = ply;
		
		boolean foundPV = false;
		boolean pvNode = beta - alpha > 1;
		
		if (depth == 0) {
			return quiescenceSearch(alpha, beta);
		}
		
		nodesSearched++;
		
		long key = b.getZobristKey();
		
		if (ply > 0 && b.isRepeat(key)) {
			return 0;
		}
	    
		if (b.getHalfMoveClock() >= 100) {
	    	return 0;
	    }
		
		/************************** probes the transposition table ****************************/
		Entry entry = tTable.lookup(key);
		Move hashMove = null;
		if (entry != null)
			hashMove = entry.getBestMove();
		
		// use entry if entry depth >= current depth and current node != pv node
		if (entry != null 
				&& entry.getDepth() >= depth 
				&& Math.abs(entry.getEvaluation()) != searchAborted
				&& !pvNode) {
			int storedScore = entry.getEvaluation();
			NodeType type = entry.getNodeType();
			
			if (type == NodeType.EXACT
				|| (type == NodeType.UPPER && storedScore <= alpha)
				|| (type == NodeType.LOWER && storedScore >= beta)) {
				return storedScore;
			}
		}
		/**************************************************************************************/

		int side = b.getSideToMove();
		
		/****************************** null move pruning *************************************/
		// don't prune at pv nodes, at possible zugzwang nodes, if pruning descends into qs
		// also don't make consecutive null moves
		if (!generator.isInCheck(b, b.getKingpos(side), side)
				&& depth > 2 
				&& ply > 0 
				&& madeNullMove == false
				&& !pvNode
				&& (Long.bitCount(b.getOccupiedSquares() ^ b.getBitBoard(Piece.WHITE, Piece.PAWN)
						^ b.getBitBoard(Piece.BLACK, Piece.PAWN))) >= 5) {
			b.makeNullMove();
			madeNullMove = true;
			int score = -search(depth - 1 - 2, ply + 1, -beta, -beta+1); // depth reduction of 2
        	madeNullMove = false;
			b.unMakeNullMove();
			
			if (score >= beta) return beta;
		}
		/*************************************************************************************/
		
	    ArrayList<Move> moves = generator.generateMoves(b, false);

	    boolean hasLegalMoves = false;
	    
    	MoveOrderer.fullSort(b, moves, hashMove, ply);
	    
        NodeType type       = NodeType.UPPER;
        Move bestMoveInNode = moves.get(0);

        for (Move move : moves) {
            if (b.doMove(move)) {
            	boolean isQuiet = move.getCapturedPiece() == null && move.getPromotionPiece() == Piece.NULL;
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
	            	
	            	/************************ Stores PV in PV table ************************/
	            	pvTable[ply][ply] = move;
	            	
	            	for (int nextPly = ply +1; nextPly < pvLength[ply+1]; nextPly++) {
	            		pvTable[ply][nextPly] = pvTable[ply+1][nextPly];
	            	}
	            	
	            	pvLength[ply] = pvLength[ply+1];
	            	/***********************************************************************/
	            	
	            	if (isQuiet) {
	            		storeHistory(depth, side, b.getPiece(move.getFromSquare()).getType(), move.getToSquare());
	            	}
	            	
	            	if (beta <= score) {
	            		if (isQuiet) {
	            			storeKiller(ply, move);
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
        
        // None of the pseudo-legal moves were legal
        if (!hasLegalMoves) {
	    	// sees if the king is currently in check in this position
	    	if (!generator.isInCheck(b, b.getKingpos(side), side)) {
	    		return 0; // stalemate
	    	}
	        return minValue + ply; // checkmate
        }
        tTable.store(key, bestMoveInNode, alpha, depth, b.getMoveNumber(), type);
        return alpha;
	}
	
	/**
	 * Resolves captures so there are no hanging pieces during evaluation
	 * @param alpha 
	 * @param beta
	 * @return The evaluation of the position, after all meaningful captures are made
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
		MoveOrderer.killerTable[ply][1] = MoveOrderer.killerTable[ply][0];
		MoveOrderer.killerTable[ply][0] = m;
	}
	
	private void storeHistory(int depth, int color, int pieceType, int dest) {
		// alternatively 2^depth or 1 << depth
		MoveOrderer.historyTable[color][pieceType][dest] += depth * depth; 
		
		if (MoveOrderer.historyTable[color][pieceType][dest] >= 4096) {
			ageHistory();
		}
	}
	
	private void ageHistory() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				for (int x = 0; x < 64; x++) {
					MoveOrderer.historyTable[i][j][x] >>>= 2; // divided by 8
				}
			}
		}
	}
	
	/**
	 * Displays the principal variation (engine line)
	 */
	private void displayPV() {
		for (int i = 0; i < pvLength[0]; i++) {
			System.out.print(pvTable[0][i] + " ");
		}
	}
}