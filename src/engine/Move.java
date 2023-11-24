package engine;

public class Move {
	
	public static int make(int from, int to) {
		int result = from;
		return result |= to << 6;
	}
	
	public static int make(int from, int to, int promotion) {
		int result = make(from, to);
		return result |= (promotion + 1) << 16;
	}
	
	public static int setPromo(int move, int promotion) {
		return (move & ~0x70000) | ((promotion + 1) << 16);
	}
	
	public static int setCap(int move, Piece captured) {
		int cap = captured == null ? 0 : captured.getType() + 1;
		return (move & ~0x380000) | (cap << 19);
	}
	
	public static int setCastle(int move, int flag) {
		return (move & ~0xf000) | (flag << 12);
	}
	
	public static int setEnPassant(int move, int ep) {
		return (move & ~0x400000) | (ep << 22);
	}
	
	public static int getFrom(int move) {
		return move & 0x3f;
	}
	
	public static int getTo(int move) {
		return (move & 0xfc0) >>> 6;
	}
	
	public static int getCastle(int move) {
		return (move & 0xf000) >>> 12;
	}
	
	public static int getPromotion(int move) {
		return ((move & 0x70000) >>> 16) - 1;
	}
	
	public static int getCaptured(int move) {
		return ((move & 0x380000) >>> 19) - 1;
	}
	
	public static int getEnPassant(int move) {
		return (move & 0x400000) >>> 22;
	}
	
	public static String toString(int move) {
		int fromRank = getFrom(move) >>> 3;
		int fromFile = getFrom(move) & 7;
		
		int toRank = getTo(move) >>> 3;
		int toFile = getTo(move) & 7;
		
		String promotion = "";
		
		switch (getPromotion(move)) {
		case 1:
			promotion = "n";
			break;
		case 2:
			promotion = "b";
			break;
		case 3:
			promotion = "r";
			break;
		case 4:
			promotion = "q";
			break;
		}
		return "" + (char)('a' + fromFile) + (fromRank +1)
				  + (char)('a' + toFile) + (toRank + 1) + promotion;
	}
	
	private Move() {};
//	private byte fromSquare;
//	private byte toSquare;
//	private byte promotionPiece;
//	private byte castlingFlag;
//	private byte cPieceType;
//	
//	private boolean isEnPassant;
//	
//	public Move() {
//		this.fromSquare = 0;
//		this.toSquare = 0;
//		this.promotionPiece = Piece.NULL;
//	}
//
//	public Move(int from, int to) {
//		this.fromSquare     = (byte)from;
//		this.toSquare       = (byte)to;
//		this.promotionPiece = Piece.NULL;
//	}
//	
//	public Move(int from, int to, int pp) {
//		this.fromSquare     = (byte)from;
//		this.toSquare       = (byte)to;
//		this.promotionPiece = (byte)pp;
//	}
//	
//	public int getFromSquare() {
//		return fromSquare;
//	}
//	
//	public int getToSquare() {
//		return toSquare;
//	}
//	
//	public int getPromotionPiece() {
//		return promotionPiece;
//	}
//	
//	public void setPromotionPiece(int p) {
//		this.promotionPiece = (byte)p;
//	}
//	
//	public void setCapturedPiece(Piece p) {
//		cPieceType = p != null ? (byte) (p.getType()) : -1;
//	}
//	
//	public int getCapturedPieceType() {
//		return cPieceType;
//	}
//	
//	public void setCastlingFlag(int f) {
//		this.castlingFlag = (byte)f;
//	}
//	
//	public int getCastlingFlag() {
//		return castlingFlag;
//	}
//
//	public boolean isEnPassant() {
//		return isEnPassant;
//	}
//
//	public void setEnPassant(boolean isEnPassant) {
//		this.isEnPassant = isEnPassant;
//	}
//
//	public String toString() {
//		int fromRank = fromSquare >>> 3;
//		int fromFile = fromSquare & 7;
//		
//		int toRank = toSquare >>> 3;
//		int toFile = toSquare & 7;
//		
//		String promotion = "";
//		
//		switch (promotionPiece) {
//		case 1:
//			promotion = "n";
//			break;
//		case 2:
//			promotion = "b";
//			break;
//		case 3:
//			promotion = "r";
//			break;
//		case 4:
//			promotion = "q";
//			break;
//		}
//		return "" + (char)('a' + fromFile) + (fromRank +1)
//				  + (char)('a' + toFile) + (toRank + 1) + promotion;
//	}
//
//	public int toInt() {
//		int result = fromSquare; // first 6 bits
//		result |= (int)toSquare << 6; // bits 7-12
//		result |= (int)castlingFlag << 12; // bits 13-16
//		result |= ((int)promotionPiece + 1) << 16; // bits 17-19
//		result |= ((int)cPieceType + 1) << 19; // bits 20-22
//		if (isEnPassant) result |= 1 << 22; // bit 23
//		return result;
//	}
//	public static Move convert(int move) {
//		Move result = new Move(getFrom(move), getTo(move));
//		result.setCapturedPiece(new Piece(0, getCaptured(move)));
//		result.setCastlingFlag((move & 0xf000) >>> 12);
//		result.setPromotionPiece(((move & 0x70000) >>> 16)-1);
//		if ((move & 0x400000) != 0) result.setEnPassant(true);
//		return result;
//	}
//	
//	public boolean equals(Move m) {
//		if (m != null 
//			&& (this.fromSquare     == m.fromSquare)
//			&& (this.toSquare       == m.toSquare)
//			&& (this.promotionPiece == m.promotionPiece)
//			&& (this.isEnPassant    == m.isEnPassant)
//			&& (this.castlingFlag   == m.castlingFlag)
//			&& (this.cPieceType     == m.cPieceType)) {
//			return true;
//		}
//		return false;
//	}
}