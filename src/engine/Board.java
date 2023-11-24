package engine;
import java.util.ArrayDeque;

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
	
	int[] push = {-8, 8};
	
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
		emptySquares    = ~occupiedSquares;
		zobristKey      = Zobrist.getKey(this);
		pastKeys        = new long[110];
	}
	
	public boolean doMove(int m) {
		stack.add(new BoardState(halfMoveClock, zobristKey, castlingRights, enPassantTarget));
		pastKeys[halfMoveClock] = zobristKey;
		zobristKey ^= Zobrist.KEYS[Zobrist.CASTLEINDEX + castlingRights];
		
		int   fromSquare    = Move.getFrom(m);
		int   toSquare      = Move.getTo(m);
		int   pieceType     = pieces[fromSquare].getType();
		int   capturedPieceType = Move.getCaptured(m);
		Piece capturedPiece = capturedPieceType != -1 ? new Piece(1-sideToMove, capturedPieceType): null;
		
		if (pieceType == Piece.PAWN || capturedPieceType != -1) {
			halfMoveClock = 0;	
		}
		else {
			halfMoveClock++;
		}
		
		long fromBB = 0x1L << fromSquare;
		long toBB = 0x1L << toSquare;
		bitBoards[sideToMove][pieceType] ^= fromBB ^ toBB;
		zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[fromSquare], fromSquare)];
		
		pieces[toSquare] = pieces[fromSquare];
		pieces[fromSquare] = null;
		
		// capture
		if (capturedPieceType != -1) {
			if (Move.getEnPassant(m) == 1) {
				bitBoards[1-sideToMove][capturedPieceType] ^= 0x1L << (toSquare + push[sideToMove]);
				zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(capturedPiece, toSquare + push[sideToMove])];
				pieces[toSquare + push[sideToMove]] = null;
			}
			else {
				bitBoards[1-sideToMove][capturedPieceType] ^= toBB;
				zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(capturedPiece, toSquare)];
			}
		}
		// castle
		else if (Move.getCastle(m) != 0) {
			castleRook(Move.getCastle(m), sideToMove);
		}
		// promotion
		if (Move.getPromotion(m) != -1) {
			bitBoards[sideToMove][Piece.PAWN] ^= toBB;
			bitBoards[sideToMove][Move.getPromotion(m)] ^= toBB;
			pieces[toSquare] = new Piece(sideToMove, Move.getPromotion(m));
		}
		
		zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[toSquare], toSquare)];
		
		handleCastlingRights(pieceType);
		
		if (enPassantTarget != 0) {
			zobristKey ^= Zobrist.KEYS[Zobrist.ENPASSANTINDEX + (BitBoard.getLSB(enPassantTarget) & 7)];
		}
		
		enPassantTarget = 0;
		
		if (pieceType == Piece.PAWN 
				&& Math.abs((fromSquare >>> 3) - (toSquare >>> 3)) == 2) {
			zobristKey ^= Zobrist.KEYS[Zobrist.ENPASSANTINDEX + (fromSquare & 7)];
			enPassantTarget = 0x1L << (toSquare + push[sideToMove]);
		}
		
		zobristKey ^= Zobrist.KEYS[Zobrist.CASTLEINDEX + castlingRights];
		zobristKey ^= Zobrist.KEYS[Zobrist.SIDEINDEX];
		recomputeBitBoards();
		moveNumber++;
		sideToMove = 1 - sideToMove;
		return !moveGen.isInCheck(this, getKingpos(1-sideToMove), 1-sideToMove);
	}
	
	public void undoMove(int m) {
		BoardState pastState = stack.pollLast();
		enPassantTarget = pastState.getEnPassantBB();
		moveNumber--;
		pastKeys[halfMoveClock] = 0;
		sideToMove = 1-sideToMove;

		int   fromSquare    = Move.getFrom(m);
		int   toSquare      = Move.getTo(m);
		int   pieceType     = pieces[toSquare].getType();
		int   capturedPieceType = Move.getCaptured(m);
		Piece capturedPiece = capturedPieceType != -1 ? new Piece(1-sideToMove, capturedPieceType) : null;
		
		long fromBB = 0x1L << fromSquare;
		long toBB = 0x1L << toSquare;
		bitBoards[sideToMove][pieceType] ^= fromBB ^ toBB;

		pieces[fromSquare] = pieces[toSquare];
		pieces[toSquare] = capturedPiece;
		
		// capture
		if (capturedPieceType != -1) {
			if (Move.getEnPassant(m) == 1) {
				bitBoards[1-sideToMove][capturedPieceType] ^= 0x1L << (toSquare + push[sideToMove]);
				pieces[toSquare] = null;
				pieces[toSquare + push[sideToMove]] = capturedPiece;
			}
			else {
				bitBoards[1-sideToMove][capturedPieceType] ^= toBB;
			}
		}
		// castle
		else if (Move.getCastle(m) != 0) {
			castleRook(Move.getCastle(m), sideToMove);
		}
		// promotion
		if (Move.getPromotion(m) != -1) {
			bitBoards[sideToMove][Piece.PAWN] ^= fromBB;
			bitBoards[sideToMove][Move.getPromotion(m)] ^= fromBB;
			pieces[fromSquare] = new Piece(sideToMove, Piece.PAWN);
		}

		castlingRights  = pastState.getCastlingRights();
		zobristKey      = pastState.getZobrist();
		enPassantTarget = pastState.getEnPassantBB();
		halfMoveClock   = pastState.getHalfmoves();
		recomputeBitBoards();
	}
	
	private void handleCastlingRights(int type) {
		if (type == Piece.KING) {
			// white = 0, so 0b0011 << 2 = 0b1100
			// black = 0, so 0b0011 << 0 = 0b0011
			castlingRights &= 0b0011 << ((1-sideToMove) << 1); 
			return;
		}
		// unnecessary to do this every time for a move, but is easier and maybe cheaper
		if ((bitBoards[Piece.WHITE][Piece.ROOK] & 0x80L) == 0) 
			castlingRights &= 0b1110;
		if ((bitBoards[Piece.WHITE][Piece.ROOK] & 0x1L) == 0) 
			castlingRights &= 0b1101;
		if ((bitBoards[Piece.BLACK][Piece.ROOK] & 0x8000000000000000L) == 0) 
			castlingRights &= 0b1011;
		if ((bitBoards[Piece.BLACK][Piece.ROOK] & 0x100000000000000L) == 0) 
			castlingRights &= 0b0111;
	}
	
	private void castleRook(int flag, int color) {
		long castleMask = 0;
		Piece temp = null;
		switch (flag) {
		case 0b0001:
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[7], 7)];
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[7], 5)];
			temp = pieces[5];
			pieces[5] = pieces[7];
			pieces[7] = temp;
			castleMask = 0xa0L;
			break;
		case 0b0010:
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[0], 0)];
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[0], 3)];
			temp = pieces[3];
			pieces[3] = pieces[0];
			pieces[0] = temp;
			castleMask = 0x9L;
			break;
		case 0b0100:
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[63], 63)];
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[63], 61)];
			temp = pieces[61];
			pieces[61] = pieces[63];
			pieces[63] = temp;
			castleMask = 0xa000000000000000L;
			break;
		case 0b1000:
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[56], 56)];
			zobristKey ^= Zobrist.KEYS[Utility.getZobristIndex(pieces[56], 59)];
			temp = pieces[59];
			pieces[59] = pieces[56];
			pieces[56] = temp;
			castleMask = 0x900000000000000L;
			break;
		}
		bitBoards[color][Piece.ROOK] ^= castleMask;
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