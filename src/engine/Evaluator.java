package engine;

/**
 * @author Dave4243
 * The Evaluate.java class evaluates a chess position
 */
public class Evaluator {
	// maybe put these values into another class called constants idk
	private final int[] mgPieceValues = {100, 310, 320, 520, 950, 32000};
	
	private final int[][] passedPawnBonus = {
		{0,0}, {0,20}, {10,30}, {20,50}, {40, 60}, {74, 80}, {100, 100}, {0,0}
	};
	
	private final int[] openFileBonus     = {35, 5};
	private final int[] semiOpenFileBonus = {20,10};

	private final int[] exposedKingPenalty = {-50, 0};
	
	// indexed by piece types (pawn = 0, ... , king = 5}
	// emeny pieces farther away from king = good
	// should not be a linear relationship between distance and value
	// as distance is farther, the bonus drops faster
	// distance max 14, min 2
	private final int[][] kingTropismValues = {{0,0}, {2,1}, {1,1}, {4,2}, {8,4}, {0,0}};
	
	private final int[] bishopPairBonus  = {20, 60};
	
	/////////////////////////////////////////////////////////////////////
	
	public int evaluatePosition(Board b)
	{
		int[] mgEval = {0, 0};
		int[] egEval = {0, 0};
		
		int[] numPieces = new int[6];
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				long bb = b.getBitBoard(i,j);
				mgEval[i] += Long.bitCount(bb) * mgPieceValues[j];
				egEval[i] += Long.bitCount(bb) * mgPieceValues[j];
				numPieces[j] += Long.bitCount(bb);
				
				while (bb != 0) {
					int index = BitBoard.getLSB(bb);
					if (i == Piece.WHITE)
						index = (7-(index >>> 3)) * 8 + (index & 7);
					
					mgEval[i] += PSQT.mgTables[j][index];
					egEval[i] += PSQT.egTables[j][index];
					
					bb &= bb-1;
				}
			}
		}

		long whiteKing = b.getBitBoard(Piece.WHITE, Piece.KING);
		long blackKing = b.getBitBoard(Piece.BLACK, Piece.KING);
		
		long whitePawns = b.getBitBoard(Piece.WHITE, Piece.PAWN);
		long blackPawns = b.getBitBoard(Piece.BLACK, Piece.PAWN);
		
		long whiteKnights = b.getBitBoard(Piece.WHITE, Piece.KNIGHT);
		long blackKnights = b.getBitBoard(Piece.BLACK, Piece.KNIGHT);
		
		long whiteBishops = b.getBitBoard(Piece.WHITE, Piece.BISHOP);
		long blackBishops = b.getBitBoard(Piece.BLACK, Piece.BISHOP);
		
		long whiteRooks = b.getBitBoard(Piece.WHITE, Piece.ROOK);
		long blackRooks = b.getBitBoard(Piece.BLACK, Piece.ROOK);
		
		long whiteQueens = b.getBitBoard(Piece.WHITE, Piece.QUEEN);
		long blackQueens = b.getBitBoard(Piece.BLACK, Piece.QUEEN);
		
		// insufficient material = draw
		if (Long.bitCount(whitePawns) == 0 && Long.bitCount(blackPawns) == 0
				&& whiteRooks == 0 && blackRooks == 0
				&& whiteQueens == 0 && blackQueens == 0) {
			int totalKnights = Long.bitCount(whiteKnights | blackKnights);
			int totalBishops = Long.bitCount(whiteBishops | blackBishops);
			
			if (totalKnights == 0 && totalBishops == 1)
				return 0;
			if (totalKnights <= 2 && totalBishops == 0)
				return 0;
		}
		
		// bishop pair
		if (Long.bitCount(b.getBitBoard(Piece.WHITE, Piece.BISHOP)) == 2) {
			mgEval[Piece.WHITE] += bishopPairBonus[0];
			egEval[Piece.WHITE] += bishopPairBonus[1];
		}
		if (Long.bitCount(b.getBitBoard(Piece.BLACK, Piece.BISHOP)) == 2) {
			mgEval[Piece.BLACK] += bishopPairBonus[0];
			egEval[Piece.BLACK] += bishopPairBonus[1];
		}
		
		// open file bonuses
		long openFiles = ~(BitBoard.fileFill(whitePawns) | BitBoard.fileFill(blackPawns));
		
		mgEval[Piece.WHITE] += Long.bitCount(whiteRooks & openFiles) * openFileBonus[0];
		mgEval[Piece.BLACK] += Long.bitCount(blackRooks & openFiles) * openFileBonus[0];
		egEval[Piece.WHITE] += Long.bitCount(whiteRooks & openFiles) * openFileBonus[1];
		egEval[Piece.BLACK] += Long.bitCount(blackRooks & openFiles) * openFileBonus[1];
		
		long wkRowMask = whiteKing | ((whiteKing >>> 1) & ~BitBoard.getFileMask(7))
								   | ((whiteKing << 1) & ~BitBoard.getFileMask(0));
		long bkRowMask = blackKing | ((blackKing >>> 1) & ~BitBoard.getFileMask(7))
				                   | ((blackKing << 1) & ~BitBoard.getFileMask(0));
		
		mgEval[Piece.WHITE] += Long.bitCount(wkRowMask & openFiles) * exposedKingPenalty[0];
		mgEval[Piece.BLACK] += Long.bitCount(bkRowMask & openFiles) * exposedKingPenalty[0];
		
		// semi open file stuff
		long semiOpenFiles = BitBoard.fileFill(whitePawns) ^ BitBoard.fileFill(blackPawns);

		mgEval[Piece.WHITE] += Long.bitCount(whiteRooks & semiOpenFiles) * semiOpenFileBonus[0];
		mgEval[Piece.BLACK] += Long.bitCount(blackRooks & semiOpenFiles) * semiOpenFileBonus[0];
		egEval[Piece.WHITE] += Long.bitCount(whiteRooks & semiOpenFiles) * semiOpenFileBonus[1];
		egEval[Piece.BLACK] += Long.bitCount(blackRooks & semiOpenFiles) * semiOpenFileBonus[1];
		
		mgEval[Piece.WHITE] += Long.bitCount(wkRowMask & semiOpenFiles) * exposedKingPenalty[0]/2;
		mgEval[Piece.BLACK] += Long.bitCount(bkRowMask & semiOpenFiles) * exposedKingPenalty[0]/2;
		
		// passed pawn evaluation
		long whitePassers = getWhitePassers(whitePawns, blackPawns);
		long blackPassers = getBlackPassers(whitePawns, blackPawns);
		
		while (whitePassers != 0) {
			int index = BitBoard.getLSB(whitePassers);
			int row = index >>> 3;
		    if (Long.bitCount(whitePassers & BitBoard.getFileMask(index)) == 2) {
				mgEval[Piece.WHITE] += passedPawnBonus[row][0]/2;
				egEval[Piece.WHITE] += passedPawnBonus[row][1]/2;
		    }
		    else {
				mgEval[Piece.WHITE] += passedPawnBonus[row][0];
				egEval[Piece.WHITE] += passedPawnBonus[row][1];
		    }
			whitePassers &= whitePassers-1;
		}
		
		while (blackPassers != 0) {
			int index = BitBoard.getLSB(blackPassers);
			int row = index >>> 3;
			if (Long.bitCount(blackPassers & BitBoard.getFileMask(index)) == 2) {
				mgEval[Piece.BLACK] += passedPawnBonus[7-row][0]/2;
				egEval[Piece.BLACK] += passedPawnBonus[7-row][1]/2;
			}
			else {
				mgEval[Piece.BLACK] += passedPawnBonus[7-row][0];
				egEval[Piece.BLACK] += passedPawnBonus[7-row][1];
			}
			blackPassers &= blackPassers-1;
		}
		
		// tapered evaluation
		int pawnPhase = 0;
		int	knightPhase = 1;
		int	bishopPhase = 1;
		int	rookPhase = 2;
		int	queenPhase = 4;
		int	totalPhase = pawnPhase*16 + knightPhase*4 + bishopPhase*4 + rookPhase*4 + queenPhase*2;

		int	phase = totalPhase;

		phase -= numPieces[0] * pawnPhase; 
		phase -= numPieces[1] * knightPhase;
		phase -= numPieces[2] * bishopPhase;
		phase -= numPieces[3] * rookPhase;
		phase -= numPieces[4] * queenPhase;

		phase = (phase * 256 + (totalPhase / 2)) / totalPhase;
		
		int openingEval = mgEval[0] - mgEval[1];
		int endgameEval = egEval[0] - egEval[1];
		
		int eval = ((openingEval * (256 - phase)) + (endgameEval * phase)) / 256;
		
		if (b.getSideToMove() == Piece.BLACK) {
			eval *= -1;
		}
		return eval;
	}
	
	private long getWhitePassers(long wPawns, long bPawns) {
	   long frontSpans = BitBoard.frontSpan(bPawns, Piece.BLACK);
	   frontSpans |= ((frontSpans << 1) & ~(BitBoard.getFileMask(0)))
	              |  ((frontSpans >>> 1) & ~(BitBoard.getFileMask(7)));
	   return wPawns & ~frontSpans;
	}
	
	private long getBlackPassers(long wPawns, long bPawns) {
		   long frontSpans = BitBoard.frontSpan(wPawns, Piece.WHITE);
		   frontSpans |= ((frontSpans << 1) & ~(BitBoard.getFileMask(0)))
		              |  ((frontSpans >>> 1) & ~(BitBoard.getFileMask(7)));
		   return bPawns & ~frontSpans;
		}
}