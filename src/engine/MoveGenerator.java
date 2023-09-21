package engine;

public class MoveGenerator {
		
	/**
	 * Generates all pseudolegal moves for the position, with the option
	 * to generate only captures
	 * @param b The Board to generate moves from
	 * @param onlyCaptures Whether to generate only captures or not
	 * @return A list of pseudolegal moves for the position
	 */
	public MoveList generateMoves(Board b, boolean onlyCaptures) {
		MoveList result = new MoveList();
		int color = b.getSideToMove();
		
		int  source   = 0;
		long attackBB = 0;
		
		long pawns   = b.getBitBoard(color, Piece.PAWN);
		long knights = b.getBitBoard(color, Piece.KNIGHT);
		long bishops = b.getBitBoard(color, Piece.BISHOP);
		long rooks   = b.getBitBoard(color, Piece.ROOK);
		long queens  = b.getBitBoard(color, Piece.QUEEN);
		long kings   = b.getBitBoard(color, Piece.KING);
		
		long moveableSquares = 0xffffffffffffffffL;
		if (onlyCaptures) moveableSquares = b.getPieceBitBoard(1-color);
		
		int[] promotionSourceFiles = {6, 1};
		int[] push = {-8, 8};
		while (pawns != 0) {
			source = BitBoard.getLSB(pawns);
			long captureBB = getPawnCaptures(b, source, color);
			long moveBB    = getPawnMoves(b, source, color);
			long epTarget = b.getEnPassantTarget();
			if ((captureBB & epTarget) != 0) {
				Move m = new Move(source, BitBoard.getLSB(epTarget));
				m.setEnPassant(true);
				m.setCapturedPiece(b.getPiece(m.getToSquare() + push[color]));
				result.add(m);
				captureBB &= ~b.getEnPassantTarget();
			}
			// generate promotions instead of regular moves
			if ((source >>> 3 == promotionSourceFiles[color])) {
				generatePromotions(result, captureBB, source, b);
				generatePromotions(result, moveBB, source, b);
			}
			else {
				generateMovesFromBitBoard(result, captureBB, source, b);
				if (!onlyCaptures) generateMovesFromBitBoard(result, moveBB, source, b);
			}
			pawns &= pawns -1; 
		}
		
		while (knights != 0) {
			source = BitBoard.getLSB(knights);
			attackBB = getKnightAttacks(b, source, color) & moveableSquares;
			generateMovesFromBitBoard(result, attackBB, source, b);
			knights &= knights -1; 
		}
		
		while (bishops != 0) {
			source = BitBoard.getLSB(bishops);
			attackBB = getBishopAttacks(b, source, color) & moveableSquares;
			generateMovesFromBitBoard(result, attackBB, source, b);
			bishops &= bishops -1; 
		}
		
		while (rooks != 0) {
			source = BitBoard.getLSB(rooks);
			attackBB = getRookAttacks(b, source, color) & moveableSquares;
			generateMovesFromBitBoard(result, attackBB, source, b);
			rooks &= rooks -1; 
		}
		
		while (queens != 0) {
			source = BitBoard.getLSB(queens);
			attackBB = getQueenAttacks(b, source, color) & moveableSquares;
			generateMovesFromBitBoard(result, attackBB, source, b);
			queens &= queens -1; 
		}
		
		int kingIndex = BitBoard.getLSB(kings);
		attackBB = getKingAttacks(b, color) & moveableSquares;
		generateMovesFromBitBoard(result, attackBB, kingIndex, b);
		
		if (!onlyCaptures) generateCastles(result, b, color);
		
		return result;
	}

	private void generateMovesFromBitBoard(MoveList result, long attackBitBoard, int source, Board b) {
		while (attackBitBoard != 0) {
			int dest = BitBoard.getLSB(attackBitBoard);
			Move m = new Move(source, dest);
			m.setCapturedPiece(b.getPiece(dest));
			result.add(m);
			attackBitBoard &= attackBitBoard -1;
		}
	}
	
	private void generatePromotions(MoveList result, long attackBitBoard, int source, Board b) {
		while (attackBitBoard != 0) {
			int dest = BitBoard.getLSB(attackBitBoard);
			Move m1 = new Move(source, dest, 4);
			m1.setCapturedPiece(b.getPiece(dest));
			Move m2 = new Move(source, dest, 3);
			m2.setCapturedPiece(b.getPiece(dest));
			Move m3 = new Move(source, dest, 2);
			m3.setCapturedPiece(b.getPiece(dest));
			Move m4 = new Move(source, dest, 1);
			m4.setCapturedPiece(b.getPiece(dest));
			
			result.add(m1);
			result.add(m2);
			result.add(m3);
			result.add(m4);
			attackBitBoard &= attackBitBoard -1;
		}
	}
	
	private void generateCastles(MoveList result, Board b, int color) {
		int mask      = color == Piece.WHITE ? 0b0011 : 0b1100;
		int kingIndex = color == Piece.WHITE ? 4 : 60;
		
		if ((b.getCastlingRights() & mask) == 0)
			return;
		if (b.getPiece(kingIndex) == null 
				|| b.getPiece(kingIndex).getType() != Piece.KING )
			return;
		if (isInCheck(b, kingIndex, color))
			return;
		
		if (color == Piece.WHITE) generateWhiteCastles(result, b, color);
		else                      generateBlackCastles(result, b, color);
	}
	
	private void generateWhiteCastles(MoveList result, Board b, int color) {
		if ((b.getCastlingRights() & 0b0001) != 0 && (b.getOccupiedSquares() & BitBoard.WKMASK) == 0
				&& !isInCheck(b, 5, Piece.WHITE)) {
			Move whiteKingside = new Move(4, 6);
			whiteKingside.setCastlingFlag(0b0001);
			result.add(whiteKingside);
		}
		if ((b.getCastlingRights() & 0b0010) != 0 && (b.getOccupiedSquares() & BitBoard.WQMASK) == 0
			&& !isInCheck(b, 3, Piece.WHITE)) {
			Move whiteQueenside = new Move(4, 2);
			whiteQueenside.setCastlingFlag(0b0010);
			result.add(whiteQueenside);
		}	
	}
	
	private void generateBlackCastles(MoveList result, Board b, int color) {
		if ((b.getCastlingRights() & 0b0100) != 0 && (b.getOccupiedSquares() & BitBoard.BKMASK) == 0
				&& !isInCheck(b, 61, Piece.BLACK)) {
			Move blackKingside = new Move(60, 62);
			blackKingside.setCastlingFlag(0b0100);
			result.add(blackKingside);
		}	
		
		if ((b.getCastlingRights() & 0b1000) != 0 && (b.getOccupiedSquares() & BitBoard.BQMASK) == 0
				&& !isInCheck(b, 59, Piece.BLACK)) {
			Move blackQueenside = new Move(60, 58);
			blackQueenside.setCastlingFlag(0b1000);
			result.add(blackQueenside);
		}
	}
	
	public boolean isInCheck(Board b, int kingIndex, int color) {
		if ((getPawnCaptures(b, kingIndex, color) & b.getBitBoard(1-color, Piece.PAWN)) != 0)
			return true;
		
		if ((getKnightAttacks(b, kingIndex, color) & b.getBitBoard(1-color, Piece.KNIGHT)) != 0)
			return true;
		
		if ((getBishopAttacks(b, kingIndex, color) & b.getBitBoard(1-color, Piece.BISHOP)) != 0)
			return true;
		
		if ((getRookAttacks(b, kingIndex, color) & b.getBitBoard(1-color, Piece.ROOK)) != 0)
			return true;
		
		if ((getQueenAttacks(b, kingIndex, color) & b.getBitBoard(1-color, Piece.QUEEN)) != 0)
			return true;
		
		if ((getKingAttacks(b, color) & b.getBitBoard(1-color, Piece.KING)) != 0)
			return true;
		
		return false;
	}
	
	public long getPawnMoves(Board b, int source, int pawnColor) {
		long empty        = b.getEmptySquares();
		long singlePushes = (0x1L << (source + 8) >>> (pawnColor << 4)) & empty;
		long doublePushes = (singlePushes << 8) >>> (pawnColor << 4) & empty;
		doublePushes &= BitBoard.pawnDoublePushMasks[pawnColor];
		return singlePushes | doublePushes;
	}
	
	public long getPawnCaptures(Board b, int source, int pawnColor) {
		long[] captureTable = Tables.pawnAttacks[pawnColor];
		long possibleCaptures = captureTable[source];
		
		return possibleCaptures & (b.getPieceBitBoard(1-pawnColor)| b.getEnPassantTarget());
	}
	
	public long getKnightAttacks(Board b, int source, int knightColor) {
		long attackBitBoard = Tables.knightMoves[source];
		
		return attackBitBoard &= ~b.getPieceBitBoard(knightColor);
	}
	
	public long getBishopAttacks(Board b, int source, int bishopColor) {
		return getDiagonalMoves(b, source, bishopColor) 
				| getAntiDiagonalMoves(b, source, bishopColor);
	}
	
	public long getRookAttacks(Board b, int source, int rookColor) {
		return getRankMoves(b, source, rookColor) 
				| getFileMoves(b, source, rookColor);
	}
	
	public long getQueenAttacks(Board b, int source, int queenColor) {
		return getRankMoves(b, source, queenColor) 
				| getFileMoves(b, source, queenColor)
				| getDiagonalMoves(b, source, queenColor) 
				| getAntiDiagonalMoves(b, source, queenColor);
	}
	
	public long getKingAttacks(Board b, int kingColor) {
		long king           = b.getBitBoard(kingColor, Piece.KING);
		long attackBitBoard = Tables.kingMoves[BitBoard.getLSB(king)];
		
		return attackBitBoard & ~b.getPieceBitBoard(kingColor);
	}
	
	public static long getDiagonalMoves(Board b, int source, int pieceColor) {
		long occupiedSquares = b.getOccupiedSquares();
		long diagonalMask    = BitBoard.getDiagonalMask(source);
		long index           = ((diagonalMask & occupiedSquares)
	    	                       * BitBoard.getFileMask(1)) >>> 58;
		long attacks         = Tables.slidingAttackLookup[source & 7][(int)index];
		attacks = (attacks * BitBoard.getFileMask(0)) & diagonalMask;
		return attacks & ~b.getPieceBitBoard(pieceColor);
	}
	
	public static long getAntiDiagonalMoves(Board b, int source, int pieceColor) {
		long occupiedSquares  = b.getOccupiedSquares();
		long antiDiagonalMask = BitBoard.getAntiDiagonalMask(source);
		long index            = ((antiDiagonalMask & occupiedSquares) 
				   				    * BitBoard.getFileMask(1)) >>> 58;
		long attacks          = Tables.slidingAttackLookup[source & 7][(int)index];
		attacks = (attacks * BitBoard.getFileMask(0)) & antiDiagonalMask;
		return attacks & ~b.getPieceBitBoard(pieceColor);
	}
	
	public static long getRankMoves(Board b, int source, int pieceColor) {
		int rankIndex = source >>> 3 << 3;
		long index    = ((b.getOccupiedSquares() >>> rankIndex) >>> 1) & 0x3fL; 
		long attacks  = Tables.slidingAttackLookup[source & 7][(int)index];
		return (attacks << rankIndex) & ~b.getPieceBitBoard(pieceColor);
	}
	
	public static long getFileMoves(Board b, int source, int pieceColor) {
		long occupiedSquares = b.getOccupiedSquares();
		long aFileMask       = BitBoard.getFileMask(0);
		long index           = aFileMask & (occupiedSquares >>> (source & 7 ));
		index                = (index * 0x4081020408000L) >>> 58;
		long attacks = Tables.slidingAttackLookup[source >>> 3][(int)index];

		// maps attacks to H file and rotates 180 degress to A file
		// the problem with the H file is that the attacks are flipped
		attacks = Long.reverse(attacks * 0x8040201008040201L);
		attacks = attacks & aFileMask;
		attacks = attacks << ((source & 7));
		
		return attacks & ~b.getPieceBitBoard(pieceColor);
	}
}