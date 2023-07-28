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
	
	private int searchedPVNodes;
	private int storedPVNodes;
	private int numTranspositions;
	private int numFullSorts;
	private int numCutoffs;
	private int positionsEvaluated;
	
	private long endTime;
	private int numNodesSearched;

	private static final int maxValue = 1000000;
	private static final int minValue = -maxValue;
	
	private Move bestMove;

	public Search(Board b) {
		this.b = b;
		
		evaluator = new Evaluator();
		generator = new MoveGenerator();
		tTable    = new TranspositionTable();
		MoveOrderer.clearKillers();
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
		timeAllowance /= 4;
		
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
		int depth;

		for (depth = 1; depth < 100; depth++) {
			int currentDepthEval = search(depth, 0, minValue, maxValue);
			eval = currentDepthEval;

			if (System.currentTimeMillis() > endTime)
				break;
			// mate score
			if (eval >= maxValue-100)
				break;
		}
		storedPVNodes = 0;
		searchedPVNodes = 0;
		
//		printStatistics(depth, eval, System.currentTimeMillis() - startTime);
		return bestMove;
	}
	
	/**
	 * Prints the statistics of the search
	 * @param depth The depth the search was searched to 
	 * @param eval  The evaluation of the position
	 * @param time  The amount of time the search took
	 */
	private void printStatistics(int depth, int eval, long time) {
		System.out.print("info ");
		System.out.print("depth "  + depth + " ");
        System.out.print("time "   + time + " ");
		System.out.print("nodes "  + numNodesSearched + " ");
		System.out.print("pv ");
		displayPV();
		System.out.println("score cp "  + eval);

//		System.out.println("Positions evaled: "  + positionsEvaluated);
//		System.out.println("Number of cutoffs: " + numCutoffs);
//		System.out.println("Number of sorts: "   + numFullSorts);
//		System.out.println("Stored PV nodes: "   + storedPVNodes);
//		System.out.println("Searched PV Nodes: " + searchedPVNodes);
//		System.out.println("Transpositions: "    + numTranspositions);
//		System.out.print("PV: ");
		
		System.out.println("NPS: " + (long)(numNodesSearched/(time/1000.0)));

		System.out.println();
		
		numNodesSearched = 0;
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
		boolean foundPV = false;
		if (depth == 0) {
			return quiescenceSearch(alpha, beta);
//			return evaluator.evaluatePosition(b);
		}
		
		numNodesSearched++;
		
		long key = b.getZobristKey();
		
		if (ply > 0 && b.isRepeat(key)) {
			return 0;
		}
		
		Entry entry = tTable.lookup(key);
		
		if (entry != null && entry.getDepth() >= depth) {
			numTranspositions++;
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
	        	numCutoffs++;
	            return beta;
	        }
		}
		
		int side              = b.getSideToMove();
	    ArrayList<Move> moves = generator.generateMoves(b, false);

	    boolean hasLegalMoves = false;
	    
	    if (b.getHalfMoveClock() >= 100) 
	    	return 0;
	    
	    if (ply <= 3 && entry != null && entry.getNodeType() == NodeType.EXACT) {
	    	MoveOrderer.fullSort(b, moves, entry.getBestMove(), key, ply);
	    } else {
	    	MoveOrderer.partialSort(b, moves, 8, ply);
	    }
	    
        NodeType type       = NodeType.UPPER;
        Move bestMoveInNode = moves.get(0);

        for (Move move : moves) {
            if (b.doMove(move)) {
	        	hasLegalMoves = true;
	        	int score;
	        	int ext = 0;
	        	if (generator.isInCheck(b, BitBoard.getLSB(b.getBitBoard(1-side, Piece.KING)), 1-side)){
	        		ext = 1;
	        	}
	        	if (foundPV) {
	        		score = -search(depth-1 + ext, ply+1, -alpha-1, -alpha);
	        		if ((score > alpha) && (score < beta)) // finds a better move
	        			score = -search(depth-1 + ext, ply+1, -beta, -alpha);
	        	} 
	        	else {
	        		score = -search(depth - 1 + ext, ply+1, -beta, -alpha);
	        	}           
	        	b.undoMove(move);
	
	            if( score > alpha ) {
	            	type           = NodeType.EXACT;
	            	alpha          = score;
	            	bestMoveInNode = move;
	            	
	            	if (ply == 0) bestMove = move;
	            	
	            	if (beta <= score) {
	            		if (move.getCapturedPiece() == null && move.getPromotionPiece() == Piece.NULL) {
	            			storeKiller(ply, move);
	            		}
	            		numCutoffs++;
	            		type = NodeType.LOWER;
	            		tTable.store(key, move, beta, depth, b.getHalfMoveClock(), type);
	            		return beta; 
	            	}
	            	foundPV = true;
	            }
            }
            else b.undoMove(move);
        }
        
        if (!hasLegalMoves) {
	    	// sees if the king is currently in check in this position
	    	if (!generator.isInCheck(b, BitBoard.getLSB(b.getBitBoard(side, Piece.KING)), side)) {
	    		return 0;
	    	}
	        return minValue + ply;
        }
        tTable.store(key, bestMoveInNode, alpha, depth, b.getHalfMoveClock(), type);
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
		numNodesSearched++;
		positionsEvaluated++;
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
		MoveOrderer.killerTable[ply][0] = MoveOrderer.killerTable[ply][1];
		MoveOrderer.killerTable[ply][1] = m;
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