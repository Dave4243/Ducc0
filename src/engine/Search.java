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
	private static final Move nullMove = new Move(0,0);
	private SearchStack[] ss;

	public Search(Board b) {
		this.b = b;
		
		evaluator = new Evaluator();
		generator = new MoveGenerator();
		tTable    = new TranspositionTable();
		MoveOrderer.clearHistory();
		
		ss = new SearchStack[maxDepth];
		for (int i = 0; i < maxDepth; i++) {
			ss[i] = new SearchStack();
		}
		pvLength = new int[maxDepth];
		pvTable  = new Move[maxDepth][maxDepth];
	}
	
	class SearchStack {
		Move currentMove;
		int staticEval;
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
		int depth;
		int alpha = minValue;
		int beta  = maxValue;
		for (depth = 1; depth < maxDepth; depth++) {
			eval = search(depth, 0, alpha, beta);
			printStatistics(depth, eval, System.currentTimeMillis() - startTime);
			if (System.currentTimeMillis() >= absoluteEndTime)
				return pvTable[0][0];
			
			if (System.currentTimeMillis() > endTime)
				break;
			
			// mate score
			if (eval >= maxValue - maxDepth)
				break;
		}

		return pvTable[0][0];
	}
	
	private void printStatistics(int depth, int eval, long time) {
		System.out.print("info ");
		System.out.print("depth " + depth         + " ");
		System.out.print("score cp " + eval       + " ");
        System.out.print("time "  + time          + " ");
		System.out.print("nodes " + nodesSearched + " ");
		System.out.print("pv ");
		displayPV();
		System.out.println();
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
		
		long key = b.getZobristKey();
		
		if (ply > 0 && (b.isRepeat(key) || b.getHalfMoveClock() >= 100)) {
			return 0;
		}
		
		if (depth == 0) {
			return quiescenceSearch(alpha, beta);
		}
		
		nodesSearched++;

		
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
			ss[ply].currentMove = nullMove;
			int score = -search(depth - 1 - 2, ply + 1, -beta, -beta+1); // depth reduction of 2
        	madeNullMove = false;
			b.unMakeNullMove();
			
			if (score >= beta) {
				if (score >= maxValue - 100)
					return beta;
				return score;
			}
		}
		/*************************************************************************************/
		
	    ArrayList<Move> moves = generator.generateMoves(b, false);
    	MoveOrderer.fullSort(b, moves, hashMove, ply);
	    
        NodeType type       = NodeType.UPPER;
        Move bestMove = moves.get(0);
        
        int bestScore       = minValue;
	    int moveCount       = 0;
        
        for (Move move : moves) {
            if (!b.doMove(move)) {
            	b.undoMove(move);
            	continue;
            }
            ss[ply].currentMove = move;
        	madeNullMove = false;
        	moveCount++;
        	boolean isQuiet = move.getCapturedPiece() == null && move.getPromotionPiece() == Piece.NULL;
        	int score;
        	int ext = 0;
        	if (generator.isInCheck(b, b.getKingpos(1-side), 1-side)){
        		ext = 1;
        	}
        	
        	/********************** Principal Variation Search ******************************/
        	
        	// search starts off assuming the first node is a PV node
        	if (!foundPV) {
        		score = -search(depth - 1 + ext, ply+1, -beta, -alpha);
        	}
        	else {
        		// late move reduction
        		int reduction = 0;
        		if (depth >= 3
        				&& isQuiet
        				&& ext == 0
        				&& moveCount >= 3) {
        			reduction = moveCount >= 6 ? depth/3 : 1;
        			if (pvNode) reduction /= 2;
        			if (MoveOrderer.historyTable[side]
        				                     	[move.getFromSquare()]
        						             	[move.getToSquare()] >= 128){
        				reduction = 0;
        			}
        		}
        		
        		// for nodes after the first node, use a null window
        		score = -search(depth-1 + ext - reduction, ply+1, -alpha-1, -alpha);
        		
        		// if score ends up being above alpha AND there was a reduction
        		// then research with null window, but without reduction
        		if (score > alpha && reduction > 0)
        			score = -search(depth-1 + ext, ply+1, -alpha-1, -alpha);
        		
        		// at this point, if score ends up being above alpha, but less than beta,
        		// then the node is actually a pv node and prompts a full window research
        		if (score > alpha && score < beta)
        			score = -search(depth-1 + ext, ply+1, -beta, -alpha);
        	}     
        	b.undoMove(move);
        	
        	if (Math.abs(score) > maxValue +1000)
        		return searchAborted;
        	
            if (score > bestScore) {
            	bestScore      = score;
            	bestMove = move;
            	
            	/************************ Stores PV in PV table ************************/
            	pvTable[ply][ply] = move;
            	
            	for (int nextPly = ply +1; nextPly < pvLength[ply+1]; nextPly++) {
            		pvTable[ply][nextPly] = pvTable[ply+1][nextPly];
            	}
            	
            	pvLength[ply] = pvLength[ply+1];
            	/***********************************************************************/
            	
            	if (score > alpha) {
	            	type  = NodeType.EXACT;
	            	alpha = score;
	            	foundPV = true;
	            	
	        		if (isQuiet && (ply <= 1 || (ss[ply-1].currentMove != nullMove))) 
            			storeHistory(depth, side, move.getFromSquare(), move.getToSquare());
	        		
	            	if (score >= beta) {
	            		type = NodeType.LOWER;
	            		tTable.store(key, move, score, depth, b.getMoveNumber(), type);
	            		return score; 
	            	}
            	}
            }
        }
        
        // None of the pseudo-legal moves were legal
        if (moveCount == 0) {
	    	// sees if the king is currently in check in this position
	    	if (!generator.isInCheck(b, b.getKingpos(side), side)) {
	    		return 0; // stalemate
	    	}
	        return minValue + ply; // checkmate
        }
        tTable.store(key, bestMove, bestScore, depth, b.getMoveNumber(), type);
        return bestScore;
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
		int staticEvaluation = evaluator.evaluatePosition(b); // evaluates
		
		if (staticEvaluation >= beta)
			return staticEvaluation;
		
		int side = b.getSideToMove();
		
		/********************************* DELTA PRUNING ***************************/
		int delta = 950;
		if ((b.getBitBoard(side, Piece.PAWN) & (0xff000000000000L >>> (side * 40))) != 0)
			delta = 1750;
		
		if (staticEvaluation < alpha - delta)
			return staticEvaluation;
		// end of delta pruning
		
		if (staticEvaluation > alpha)
			alpha = staticEvaluation;
		
		ArrayList<Move> captures = generator.generateMoves(b, true);
		MoveOrderer.fullSort(b, captures);
		
		int bestScore = staticEvaluation;
		for (Move move : captures) {
			if (!b.doMove(move)) {
				b.undoMove(move);
				continue;
			}
			
			int score = -quiescenceSearch(-beta, -alpha);
			b.undoMove(move);
			
			if (score > bestScore) {
				bestScore = score;
				if (score > alpha) {
					alpha = score;
					if (score >= beta) return score;
				}
			}
		}
		return bestScore;
	}
	
	private void storeHistory(int depth, int color, int source, int dest) {
		// alternatively 2^depth or 1 << depth
		MoveOrderer.historyTable[color][source][dest] += depth * depth; 
		
		if (MoveOrderer.historyTable[color][source][dest] >= 1024) {
			ageHistory();
		}
	}
	
	private void ageHistory() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 64; j++) {
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
		for (int i = 0; i < pvLength[0]; i++) {
			System.out.print(pvTable[0][i] + " ");
		}
	}
}