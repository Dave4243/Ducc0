package chess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import chess.TranspositionTable.NodeType;

/**
 * @author Dave4243
 * The ChessEngine.java class searches through moves
 * and finds the best move.
 */
public class ChessEngine {
	private PositionEvaluator  evaluator;
	private MoveGenerator      generator;
	private MoveChecker        checker;
	private TranspositionTable tTable;
	private MoveOrderer        orderer;
	private Board              b;
	private int searchedPVNodes;
	private int storedPVNodes;
	private int numTranspositions;
	private int numFullSorts;
	private int numCutoffs;
	
	private long endTime;
	private int numNodesSearched;

	private static final int maxValue = 1000000;
	private static final int minValue = -maxValue;
	
	private Move bestMove;

	public ChessEngine(Board b) {
		this.b = b;
		
		evaluator = new PositionEvaluator();
		generator = new MoveGenerator();
		checker   = new MoveChecker();
		tTable    = new TranspositionTable();
		orderer   = new MoveOrderer();
	}
	
	/**
	 * Finds the best move given a position
	 * @param b         The chess position represented as a Board.java
	 * @param timeleft  The time left on the move clock
	 * @param increment The increment on the time control
	 * @return The best move found during the time allocated for search
	 */
	public Move play(Board b, int timeleft, int increment) {
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
		
		int depth;
		for (depth = 1; depth < 99; depth++) {
			int currentDepthEval = search(depth, 0, minValue, maxValue);
			
			eval = currentDepthEval;
			if (System.currentTimeMillis() > endTime) 
				break;
		}

		b.doMove(bestMove);
		
		storedPVNodes = 0;
		searchedPVNodes = 0;
		
//		printStatistics(depth, eval, (int)((System.currentTimeMillis() - startTime)/1000.0));
		return bestMove;
	}
	
	/**
	 * Prints the statistics of the search
	 * @param depth The depth the search was searched to 
	 * @param eval  The evaluation of the position
	 * @param time  The amount of time the search took
	 */
	private void printStatistics(int depth, int eval, int time) {
		System.out.println("Move: "              + bestMove);
		System.out.println("Depth reached: "     + depth);
		System.out.println("Evaluation: "        + eval);
        System.out.println("Time taken: "        + time);
		System.out.println("Nodes searched: "    + numNodesSearched);
		System.out.println("Number of cutoffs: " + numCutoffs);
		System.out.println("Number of sorts: "   + numFullSorts);
		System.out.println("Stored PV nodes: "   + storedPVNodes);
		System.out.println("Searched PV Nodes: " + searchedPVNodes);
		System.out.println("Transpositions: "    + numTranspositions);
		System.out.print("PV: ");
//		displayPV();
		System.out.println();
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
		}
		
		numNodesSearched++;
		
		long key                 = b.getZobristKey();
		
		if (ply > 0 && b.isRepeat(key))
			return 0;
		
		TranspositionEntry entry = tTable.lookup(key);
		
		if (entry != null && entry.getDepth() >= depth) {
			numTranspositions++;
			int storedScore = entry.getEvaluation();
			if (entry.getNodeType() == NodeType.EXACT) {
				searchedPVNodes++;
				return storedScore;
			}
			
			else if (entry.getNodeType() == NodeType.LOWER) 
				alpha = Math.max(alpha, storedScore);
			
			else if (entry.getNodeType() == NodeType.UPPER) 
            	beta = Math.min(beta, storedScore);
			
	        if (beta <= alpha)
	            return storedScore;
		}
		
		int side                   = b.getSideToMove();
	    ArrayList<Move> legalMoves = generator.generateAllPossibleMoves(side, b);

	    if (legalMoves.isEmpty()) {
	    	// sees if the king is currently in check in this position
	    	if (!checker.canCheck(side, b.getKingPosition(side)[0], b.getKingPosition(side)[1], b))
	    		return 0;
	        return minValue + ply;
	    }
	    
	    if (b.getHalfMoveClock() >= 100) 
	    	return 0;
	    
	    if (ply <= 3 && entry != null && entry.getNodeType() == NodeType.EXACT) {
	    	numFullSorts++;
	    	orderer.fullSort(legalMoves, tTable, key);
	    } else {
	    	orderer.partialSort(legalMoves, 5);
	    }
	    
        NodeType type       = NodeType.UPPER;
        Move bestMoveInNode = legalMoves.get(0);
        
        for (Move move : legalMoves) {
        	b.doMove(move);
        	
        	/********************************************************************
        	 * Principal Variation Search                                       *
        	 * Assumes that if the first move is a PV move, then it is the best *
        	 * move. Searches all other moves with a small window to check if   *
        	 * any of them are could be better than the initial move. If the    *
        	 * search finds such a move, then it researches the nodes as if in  *
        	 * a regular alpha beta search. This is generally faster than alpha *
        	 * beta search because the time savings from if the first move is   *
        	 * actually the best move outweigh the negative of researching.     *
        	 *********************************************************************/
        	int score;
        	if (foundPV) {
        		score = -search(depth-1, ply+1, -alpha-1, -alpha);
        		if ((score > alpha) && (score < beta)) // finds a better move
        			score = -search(depth-1, ply+1, -beta, -alpha);
        	} 
        	else {
        		score = -search(depth - 1, ply+1, -beta, -alpha);
        	}
            b.undoMove(move);
            
            if( score > alpha ) {
            	type           = NodeType.EXACT;
            	alpha          = score;
            	bestMoveInNode = move;
            	
            	if (ply == 0) bestMove = move;
            	
            	if (score >= beta) {
            		numCutoffs++;
            		type = NodeType.LOWER;
            		tTable.store(key, move, beta, depth, b.getAge(), type);
            		return beta; 
            	}
            	foundPV = true;
            }
        }
        tTable.store(key, bestMoveInNode, alpha, depth, b.getAge(), type);
        return alpha;
	}
	
	/**
	 * Resolves the "horizon effect" where pieces may be hanging at depth 0,
	 * when the position is evaluated.
	 * @param alpha 
	 * @param beta
	 * @return The evaluation of the position, after all meaningful captures are
	 * made
	 */
	public int quiescenceSearch(int alpha, int beta) {
		numNodesSearched++;
		int eval = evaluator.evaluatePosition(b);
		
		if (eval >= beta)
			return beta;
		
		if (alpha < eval)
			alpha = eval;
		
		ArrayList<Move> captures = generator.generateAllCaptureMoves(b.getSideToMove(), b);
		orderer.fullSort(captures);
		
		for (int i = 0; i < captures.size(); i++) {
			Move m = captures.get(i);
			b.doMove(m);
			int score = -quiescenceSearch(-beta, -alpha);
			b.undoMove(m);
			
			if (score >= beta)
				return beta;
			
			if (score > alpha)
				alpha = score;
		}
		return alpha;
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
			b.doMove(pv.peekLast());
		}
		
		for (Move m : pv) {
			System.out.print(m + " ");
		}
		System.out.println();
		System.out.println(b);
		
		while (pv.peekLast() != null) {
			b.undoMove(pv.removeLast());
		}
	}
}