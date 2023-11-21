package engine;

/**
 * @author Dave4243
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
		timeAllowance /= 2;
		
		int eval = b.getSideToMove() == Piece.WHITE ? minValue : maxValue;

		long startTime = System.currentTimeMillis();
		endTime = startTime + timeAllowance;
		absoluteEndTime = startTime + timeleft / 3;
		int depth;
		int previousEval = 0;
		Move previousBest = null;
		/*
		 * Iterative deepening:
		 * Search the position with greater and greater depth, using entries
		 * stored in the transposition table to improve move ordering.
		 * Allows the position to be searched to the highest depth given time constraints
		 */
		for (depth = 1; depth < maxDepth; depth++) {
//			eval = search(depth, 0, alpha, beta);
			eval = aspirationSearch(previousEval, depth);
			previousEval = eval;
			boolean aborted = Math.abs(eval) == searchAborted;
			printStatistics(depth, eval, System.currentTimeMillis() - startTime);
			if (System.currentTimeMillis() >= absoluteEndTime) {
				if (aborted) return previousBest;
				
				return pvTable[0][0];
			}
			
			if (System.currentTimeMillis() > endTime)
				break;
			
			// mate score
			if (eval >= maxValue - maxDepth)
				break;
			
			previousBest = pvTable[0][0];
		}

		return pvTable[0][0];
	}
	
	/**
	 * Performs a search with a smaller window to get more cutoffs
	 * @param prevEval The evaluation of the position searched to the previous depth
	 * @param depth    The depth of this search
	 * @return The evaluation of the position after the aspirated search
	 */
	private int aspirationSearch(int prevEval, int depth) {
		int delta = 25;
		int score = 0;
		int alpha = minValue;
		int beta  = maxValue;
		
		// only make the smaller windows if depth is high enough-> lower variability in eval
		if (depth >= 5) {
			alpha = Math.max(prevEval - delta, minValue);
			beta  = Math.min(prevEval + delta, maxValue);
		}
		
		while (true) {
			score = search(depth, 0, alpha, beta);
			if (System.currentTimeMillis() >= absoluteEndTime) {
				break;
			}
			
			// fail low, re-search with a lower alpha bound
			if (score <= alpha) {
				beta = (alpha + beta)/2;
				alpha = Math.max(alpha - delta, minValue);
			}
			
			// fail high, re-search with a higher beta bound
			else if (score >= beta) {
				beta = Math.min(beta + delta, maxValue);
			}
			
			else {
				break;
			}
			delta *= 2;
		}
		return score;
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
		
		pvLength[ply]  = ply;
		boolean pvNode = beta - alpha > 1;
		long    key    = b.getZobristKey();
		boolean ttHit  = false;
		
		if (ply > 0 && b.isRepeat(key) || b.getHalfMoveClock() >= 100) {
			return 0;
		}
		
		if (depth == 0) {
			return quiescenceSearch(alpha, beta);
		}
		
		nodesSearched++;

		
		/************************** probes the transposition table ****************************/
		Entry entry = tTable.lookup(key);
		Move hashMove = null;
		if (entry != null) {
			hashMove = entry.getBestMove();
			ttHit = true;
		}
		
		// use entry if entry depth >= current depth and current node != pv node
		if (entry != null 
				&& entry.getDepth() >= depth 
				&& Math.abs(entry.getEvaluation()) != searchAborted
				&& !pvNode) {
			int storedScore = entry.getEvaluation();
			byte type = entry.getNodeType();
			
			if (type == Entry.EXACT
				|| (type == Entry.UPPER && storedScore <= alpha)
				|| (type == Entry.LOWER && storedScore >= beta)) {
				return storedScore;
			}
		}
		/**************************************************************************************/

		int side        = b.getSideToMove();
		boolean inCheck = generator.isInCheck(b, b.getKingpos(side), side);
		int staticEval  = ttHit ? entry.getEvaluation() : evaluator.evaluatePosition(b);
//		int improving   = ply >= 2 && !inCheck && staticEval > ss[ply-2].staticEval ? 1 : 0;
		
		/**************************** reverse futility pruning ********************************/
		if (!pvNode
				&& !inCheck
				&& depth <= 6
				&& staticEval - depth * 80 > beta) {
			return staticEval;
		}

		/****************************** null move pruning *************************************/
		// don't prune at pv nodes, at possible zugzwang nodes, if pruning descends into qs
		// also don't make consecutive null moves
		if (!inCheck
				&& !pvNode
				&& staticEval >= beta
				&& depth > 2
				&& ss[ply-1].currentMove != nullMove
				&& (Long.bitCount(b.getOccupiedSquares() ^ b.getBitBoard(Piece.WHITE, Piece.PAWN)
						^ b.getBitBoard(Piece.BLACK, Piece.PAWN))) >= 5) {
			b.makeNullMove();
			ss[ply].currentMove = nullMove;
			int reduction = Math.min(depth-1, 3 + depth/3);
			int score = -search(depth - reduction, ply + 1, -beta, -beta+1);
			b.unMakeNullMove();
			
			if (score >= beta) {
				if (score >= maxValue - 100)
					return beta;
				return score;
			}
		}
		/*************************************************************************************/
		
	    MoveList moveList = generator.generateMoves(b, false);
	    MoveOrderer.scoreMoves(moveList, b, hashMove, ply);
	    
        byte type = Entry.UPPER;
        Move bestMove = moveList.moves[0];
        
        int bestScore = minValue;
        int moveCount = 0;
        
        for (int i = 0; i < moveList.size(); i++) {
        	MoveOrderer.sortNext(moveList, i);
        	Move move = moveList.moves[i];
            if (!b.doMove(move)) {
            	b.undoMove(move);
            	continue;
            }
            ss[ply].currentMove = move;
        	moveCount++;
        	boolean isQuiet = isQuiet(move);
        	if (isQuiet) moveList.addQuiet(move);
        	
        	/******************************** Late Move Pruning *******************************/
        	if (!pvNode
        			&& !inCheck
        			&& isQuiet
        			&& depth <= 3
        			&& moveCount >= depth * 10) {
        		b.undoMove(move);
        		break;
        	}
        	
        	int score;
        	int ext = 0;
        	if (inCheck) ext = 1;
	
        	/********************** Principal Variation Search ******************************/
        	
        	// search starts off assuming the first node is a PV node
        	if (moveCount == 1) {
        		score = -search(depth - 1 + ext, ply+1, -beta, -alpha);
        	}
        	else {
        		// late move reduction
        		int reduction = 0;
        		if (depth >= 3
        				&& isQuiet
        				&& ext == 0
        				&& moveCount >= 3) {
        			reduction = Tables.lmrTable[Math.min(depth, 63)][Math.min(moveCount, 63)];
        			reduction -= Math.max(-2, 
        					Math.min(2, MoveOrderer.historyTable[side]
        								[move.getFromSquare()]
        								[move.getToSquare()]/5000)
        					);
        			if (!pvNode) reduction += 1;
        			reduction = Math.min(depth-2, Math.max(reduction, 0));
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
	            	type  = Entry.EXACT;
	            	alpha = score;

	            	if (score >= beta) {
		        		if (isQuiet && (ply <= 1 || (ss[ply-1].currentMove != nullMove))) {
		        			updateQuietHistory(moveList, depth, side);
		        		}
	            		type = Entry.LOWER;
	            		tTable.store(key, move, score, depth, type);
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
        tTable.store(key, bestMove, bestScore, depth, type);
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
		
	    MoveList moveList = generator.generateMoves(b, true);
	    MoveOrderer.scoreMoves(moveList, b, null, -1);
		
		int bestScore = staticEvaluation;
		for (int i = 0; i < moveList.size(); i++) {
			MoveOrderer.sortNext(moveList, i);
			Move move = moveList.moves[i];
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
	
	/**
	 * Assigns a bonus to the quiet cutoff move, penalties to all other played quiets
	 * @param ml    The MoveList
	 * @param depth The depth the position was searched to
	 * @param side  The side that made the cutoff move
	 */
	private void updateQuietHistory(MoveList ml, int depth, int side) {
		
		for (int i = 0; i < ml.numQuiets(); i++) {
			Move m = ml.quietsPlayed[i];
			updateHistory(m.getFromSquare(), m.getToSquare(), side, depth, i == ml.numQuiets()-1);

		}
	}
	
	/**
	 * Calculates the appropriate penalty/bonus to a quiet move
	 * @param from  The origin square
	 * @param to    The destination square
	 * @param side  The side that made the move
	 * @param depth The depth the moves were searched to
	 * @param good  Whether the move was the cutoff move or not
	 */
	private void updateHistory(int from, int to, int side, int depth, boolean good) {
		int entry = MoveOrderer.historyTable[side][from][to];
		int bonus = depth * depth;
		bonus = good ? bonus : -bonus;
		
		MoveOrderer.historyTable[side][from][to] = entry + bonus - entry * Math.abs(bonus)/16384;
	}
	
	/**
	 * Determines if the move is quiet
	 * @param m The move to determine
	 * @return  True if the move is quiet, false if it is not
	 */
	private boolean isQuiet(Move m) {
		if (m.getCapturedPieceType() != -1 || m.getPromotionPiece() != Piece.NULL) {
			return false;
		}
		return true;
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