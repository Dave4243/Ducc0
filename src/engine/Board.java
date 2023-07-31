package engine;
import java.util.ArrayDeque;
import java.util.HashSet;

/**
 * @author Dave4243
 */
public class Board {
	protected long[][] bitBoards;
	protected Piece[]  pieces; // redundant array based board for easy lookup
	
	private long[]     pieceBitBoard;
	private long       occupiedSquares;
	private long       emptySquares;
	private long       enPassantTarget;
	private long       zobristKey;
	
	private int        sideToMove;
	private int        castlingRights;
	private int        halfMoveClock;
	private int        moveNumber;
	
	private long[]     pastKeys;
	
	private ArrayDeque<BoardState> stack = new ArrayDeque<BoardState>();

	private MoveGenerator moveGen = new MoveGenerator();
	
	public Board() {
		initalizeBitBoards();
		initalizeBoard();
		pastKeys       = new long[110];
		sideToMove     = Piece.WHITE;
		castlingRights = 0b1111;
		zobristKey = Zobrist.getKey(this);
	}
	
	public Board(String fen) {
		bitBoards = new long[2][6];
		pieceBitBoard  = new long[2];
		pieces = new Piece[64];
		FenConverter.convert(this, fen);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				pieceBitBoard[i] |= bitBoards[i][j];
			}
		}
		occupiedSquares = pieceBitBoard[Piece.WHITE] | pieceBitBoard[Piece.BLACK];
		emptySquares = ~occupiedSquares;
		zobristKey = Zobrist.getKey(this);
		pastKeys       = new long[110];
	}
	
	public boolean doMove(Move m) {
		stack.add(new BoardState(halfMoveClock, zobristKey, castlingRights, enPassantTarget));
		pastKeys[halfMoveClock] = zobristKey;
		
		int   fromSquare    = m.getFromSquare();
		int   toSquare      = m.getToSquare();
		int   pieceType     = pieces[fromSquare].getType();
		Piece movedPiece    = pieces[fromSquare];
		Piece capturedPiece = pieces[toSquare];
		boolean isEnPassant = false;
		boolean isPromotion = m.getPromotionPiece() != Piece.NULL;
		boolean isCastle    = m.getCastlingFlag() != 0;
		
		if (enPassantTarget != 0 
				&& (m.getToSquare() == BitBoard.getLSB(enPassantTarget))
				&& (pieceType == Piece.PAWN)) {
			isEnPassant = true;
		}
		
		if (pieceType == Piece.PAWN || capturedPiece != null) {
			halfMoveClock = 0;	
		}
		else {
			halfMoveClock++;
		}
		
		if (isPromotion) {
			handlePromotion(m, sideToMove);
			
			pieces[toSquare]   = new Piece(sideToMove, m.getPromotionPiece());
			pieces[fromSquare] = null;
		}
		else if (isEnPassant) {
			handleEnPassant(m, sideToMove);
			
			pieces[toSquare]   = pieces[fromSquare];
			pieces[fromSquare] = null;

			if (sideToMove == Piece.WHITE) {
				m.setCapturedPiece(pieces[toSquare - 8]);
				pieces[toSquare - 8] = null;
			}
			if (sideToMove == Piece.BLACK) {
				m.setCapturedPiece(pieces[toSquare + 8]);
				pieces[toSquare + 8] = null;
			}
		}
		else {
			if (isCastle) castleRook(m.getCastlingFlag(), sideToMove);
			updateBitBoards(m, pieceType, sideToMove);

			pieces[toSquare]   = pieces[fromSquare];
			pieces[fromSquare] = null;
		}
		
		handleCastlingRights(m, pieceType);
		
		enPassantTarget = 0;
		
		if (pieceType == Piece.PAWN 
				&& Math.abs((fromSquare >>> 3) - (toSquare >>> 3)) == 2) {
			zobristKey ^= Zobrist.KEYS[Zobrist.ENPASSANTINDEX + (fromSquare & 7)];
			if (sideToMove == Piece.WHITE) {
				enPassantTarget = 0x1L << (m.getToSquare() - 8);
			}
			else {
				enPassantTarget = 0x1L << (m.getToSquare() + 8);
			}
		}
		
//		handleZobrist(fromSquare, toSquare, isEnPassant, capturedPiece, movedPiece);
		recomputeBitBoards();
		moveNumber++;
		sideToMove = 1 - sideToMove;
		zobristKey = Zobrist.getKey(this);
		return !moveGen.isInCheck(this, BitBoard.getLSB(bitBoards[1-sideToMove][Piece.KING]), 1-sideToMove);
	}
	
	public void undoMove(Move m) {
		BoardState pastState = stack.pollLast();
		enPassantTarget = pastState.getEnPassantBB();
		moveNumber--;
		pastKeys[halfMoveClock] = 0;
		sideToMove = 1-sideToMove;
		
		int   fromSquare    = m.getFromSquare();
		int   toSquare      = m.getToSquare();
		int   pieceType     = pieces[toSquare].getType();
		Piece capturedPiece = m.getCapturedPiece();
		boolean isEnPassant = false;
		boolean isPromotion = m.getPromotionPiece() != Piece.NULL;
		boolean isCastle    = m.getCastlingFlag()   != 0;
		
		if (enPassantTarget != 0 
				&& (m.getToSquare() == BitBoard.getLSB(enPassantTarget))
				&& (pieceType == Piece.PAWN)) {
			isEnPassant = true;
		}
		
		if (isPromotion) {
			handlePromotion(m, sideToMove);
			
			pieces[toSquare]   = capturedPiece;
			pieces[fromSquare] = new Piece(sideToMove, Piece.PAWN);
		}
		else if (isEnPassant) {
			handleEnPassant(m, sideToMove);
			
			pieces[fromSquare] = pieces[toSquare];
			pieces[toSquare]   = null;

			if (sideToMove == Piece.WHITE) pieces[toSquare - 8] = capturedPiece;
			if (sideToMove == Piece.BLACK) pieces[toSquare + 8] = capturedPiece;
		}
		else {
			updateBitBoards(m, pieceType, sideToMove);
			
			pieces[fromSquare] = pieces[toSquare];
			pieces[toSquare]   = capturedPiece;
			
			if (isCastle) castleRook(m.getCastlingFlag(), sideToMove);
		}
		castlingRights  = pastState.getCastlingRights();
		zobristKey      = pastState.getZobrist();
		enPassantTarget = pastState.getEnPassantBB();
		halfMoveClock   = pastState.getHalfmoves();
		recomputeBitBoards();
	}
	
	private void updateBitBoards(Move m, int pieceType, int color) {
		long fromBB = 0x1L << m.getFromSquare();
		long toBB   = 0x1L << m.getToSquare();
		long fromToBB = fromBB ^ toBB;
		
		bitBoards[color][pieceType] ^= fromToBB;
		
		if (m.getCapturedPiece() != null) {
			bitBoards[1-color][m.getCapturedPiece().getType()] ^= toBB;
		}
	}
	
	private void handlePromotion(Move m, int color) {
		long fromBB = 0x1L << m.getFromSquare();
		long toBB   = 0x1L << m.getToSquare();
		
		bitBoards[color][Piece.PAWN] ^= fromBB;
		bitBoards[color][m.getPromotionPiece()] ^= toBB;
		
		if (m.getCapturedPiece() != null) {
			bitBoards[1-color][m.getCapturedPiece().getType()] ^= toBB;
		}
	}
	
	private void handleCastlingRights(Move m, int type) {
		zobristKey ^= castlingRights;
		if (type == Piece.KING) 
			castlingRights &= sideToMove == Piece.WHITE ? 0b1100 : 0b0011;
		// unnecessary to do this every time for a move, but is easier and maybe cheaper
		if ((bitBoards[Piece.WHITE][Piece.ROOK] & 0x80L) == 0) 
			castlingRights &= 0b1110;
		if ((bitBoards[Piece.WHITE][Piece.ROOK] & 0x1L) == 0) 
			castlingRights &= 0b1101;
		if ((bitBoards[Piece.BLACK][Piece.ROOK] & 0x8000000000000000L) == 0) 
			castlingRights &= 0b1011;
		if ((bitBoards[Piece.BLACK][Piece.ROOK] & 0x100000000000000L) == 0) 
			castlingRights &= 0b0111;
		zobristKey ^= castlingRights;
	}
	
	private void castleRook(int flag, int color) {
		long castleMask = 0;
		Piece temp = null;
		switch (flag) {
			case 0b0001:
				temp = pieces[5];
				pieces[5] = pieces[7];
				pieces[7] = temp;
				castleMask = 0xa0L;
				break;
			case 0b0010:
				temp = pieces[3];
				pieces[3] = pieces[0];
				pieces[0] = temp;
				castleMask = 0x9L;
				break;
			case 0b0100:
				temp = pieces[61];
				pieces[61] = pieces[63];
				pieces[63] = temp;
				castleMask = 0xa000000000000000L;
				break;
			case 0b1000:
				temp = pieces[59];
				pieces[59] = pieces[56];
				pieces[56] = temp;
				castleMask = 0x900000000000000L;
				break;
		}
		bitBoards[color][Piece.ROOK] ^= castleMask;
	}
	
	private void handleEnPassant(Move m, int color) {
		long fromBB   = 0x1L << m.getFromSquare();
		long toBB     = 0x1L << m.getToSquare();
		long fromToBB = fromBB ^ toBB;
		
		bitBoards[color][Piece.PAWN]   ^= fromToBB;
		bitBoards[1-color][Piece.PAWN] ^= color == Piece.WHITE
				? enPassantTarget >>> 8 : enPassantTarget << 8;
	}

	private void handleZobrist(int from, int to, boolean enPassant, Piece captured, Piece moved) {

		zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(moved, from)];
		zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[to], to)];
		
		if (enPassant) {
			int direction = sideToMove == Piece.WHITE ? -8 : 8;
			int index = Utility.getZobristIndex(captured, to+direction);
			zobristKey ^= Zobrist.KEYS[index];
		}
		else if (captured != null) {
			int index = Utility.getZobristIndex(captured, to);
			zobristKey ^= Zobrist.KEYS[index];
		}
		zobristKey ^= Zobrist.KEYS[Zobrist.SIDEINDEX];
	}
	
	public void makeNullMove() {
		stack.add(new BoardState(halfMoveClock, zobristKey, castlingRights, enPassantTarget));
		sideToMove = 1-sideToMove;
		zobristKey ^= Zobrist.KEYS[Zobrist.SIDEINDEX];
		
		if (enPassantTarget != 0) {
			int targetFile = BitBoard.getLSB(enPassantTarget) & 7;
			zobristKey ^= Zobrist.KEYS[Zobrist.ENPASSANTINDEX + targetFile];
		}
		enPassantTarget = 0;
	}
	
	public void unMakeNullMove() {
		BoardState pastState = stack.pollLast();
		sideToMove = 1-sideToMove;
		this.halfMoveClock = pastState.getHalfmoves();
		this.zobristKey = pastState.getZobrist();
		this.castlingRights = pastState.getCastlingRights();
		this.enPassantTarget = pastState.getEnPassantBB();
	}
	
	public long getBitBoard(int color, int type) {
		return bitBoards[color][type];
	}
	
	public long getPieceBitBoard(int color) {
		return pieceBitBoard[color];
	}
	
	public long getOccupiedSquares() {
		return occupiedSquares;
	}
	
	public long getEmptySquares() {
		return emptySquares;
	}
	
	public long getEnPassantTarget() {
		return enPassantTarget;
	}
	
	protected void setEnPassantTarget(long newTarget) {
		enPassantTarget = newTarget;
	}
	
	public long getZobristKey() {
		return zobristKey;
	}
	
	public void setZobristKey(long newKey) {
		zobristKey = newKey;
	}
	
	public int getSideToMove() {
		return sideToMove;
	}
	
	protected void setSideToMove(int color) {
		sideToMove = color;
	}
	
	public int getCastlingRights() {
		return castlingRights;
	}
	
	protected void setCastlingRights(int castleRight) {
		castlingRights = castleRight;
	}
	
	public int getHalfMoveClock() {
		return halfMoveClock;
	}
	
	protected void setHalfMoveClock(int clock) {
		halfMoveClock = clock;
	}
	
	public int getMoveNumber() {
		return moveNumber;
	}
	
	public Piece getPiece(int index) {
		return pieces[index];
	}
	
	public int getKingpos(int color) {
		return BitBoard.getLSB(bitBoards[color][Piece.KING]);
	}
	
	public String toString() {
		for (int i = 56; i >= 0; i -= 8) {
			for (int j = 0; j < 8; j++) {
				if (pieces[i + j] != null)
					System.out.print(Utility.convertPiece(pieces[i+j]) + " ");
				else
					System.out.print(". ");
			}
			System.out.println();
		}
		return "";
	}
	
	private void recomputeBitBoards() {
		pieceBitBoard[0] = 0;
		pieceBitBoard[1] = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				pieceBitBoard[i] |= bitBoards[i][j];
			}
		}
		occupiedSquares = pieceBitBoard[Piece.WHITE] | pieceBitBoard[Piece.BLACK];
		emptySquares = ~occupiedSquares;
	}
	
	private void initalizeBitBoards() {
		bitBoards = new long[][] {
			{0xff00L, 0x42L, 0x24L, 0x81L, 0x8L, 0x10L},
			{0x00ff000000000000L, 0x4200000000000000L, 0x2400000000000000L, 
			 0x8100000000000000L, 0x0800000000000000L, 0x1000000000000000L}
		};
		pieceBitBoard = new long[2];
		recomputeBitBoards();
	}
	
	private void initalizeBoard() {
		pieces = new Piece[64];

		pieces[0] = new Piece(Piece.WHITE, Piece.ROOK);
		pieces[1] = new Piece(Piece.WHITE, Piece.KNIGHT);
		pieces[2] = new Piece(Piece.WHITE, Piece.BISHOP);
		pieces[3] = new Piece(Piece.WHITE, Piece.QUEEN);
		pieces[4] = new Piece(Piece.WHITE, Piece.KING);
		pieces[5] = new Piece(Piece.WHITE, Piece.BISHOP);
		pieces[6] = new Piece(Piece.WHITE, Piece.KNIGHT);
		pieces[7] = new Piece(Piece.WHITE, Piece.ROOK);
		
		for (int i = 0; i < 8; i++)
		{
			pieces[8 + i] = new Piece(Piece.WHITE, Piece.PAWN);
		}
		
		pieces[56] = new Piece(Piece.BLACK, Piece.ROOK);
		pieces[57] = new Piece(Piece.BLACK, Piece.KNIGHT);
		pieces[58] = new Piece(Piece.BLACK, Piece.BISHOP);
		pieces[59] = new Piece(Piece.BLACK, Piece.QUEEN);
		pieces[60] = new Piece(Piece.BLACK, Piece.KING);
		pieces[61] = new Piece(Piece.BLACK, Piece.BISHOP);
		pieces[62] = new Piece(Piece.BLACK, Piece.KNIGHT);
		pieces[63] = new Piece(Piece.BLACK, Piece.ROOK);
		
		for (int i = 0; i < 8; i++)
		{
			pieces[48 + i] = new Piece(Piece.BLACK, Piece.PAWN);
		}
	}
	
	public boolean isRepeat(long key) {
		for (int i = 0; i < halfMoveClock; i++) {
			if (pastKeys[i] == key)
				return true;
		}
		return false;
	}
}