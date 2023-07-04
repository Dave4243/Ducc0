package chess;

/**
 * @author Dave4243
 * The Move.java class represents a move in chess
 */
public class Move
{
	private Piece   piece, promotionPiece;
	private boolean enPassant;
	private Piece   capturedPiece;
	private boolean castleQueenside = false;
	private boolean castleKingside  = false;
	private int     originalRow;
	private int     originalFile;
	private int     destinationRow;
	private int     destinationFile;
	
	public Move(Piece p, int ogRow, int ogFile, int destRow, int destFile) {
		this.piece           = p;
		this.originalRow     = ogRow;
		this.originalFile    = ogFile;
		this.destinationRow  = destRow;
		this.destinationFile = destFile;
	}
	
	public String toString() {	
		String result = "";
		result += (char)(originalFile + 'a');
		result += 8-originalRow;
		result += (char)(destinationFile + 'a');
		result += 8-destinationRow;
		if (promotionPiece != null)
		{
			result += promotionPiece.toString().toLowerCase();
		}
		return result;
	}
	
	
	public String toString(boolean capture, boolean row, boolean col)
	{
		String result = "";
		result += piece.toString();
		if (capture && piece instanceof Pawn)
			col= true;
		
		if (row)
			result += originalRow;
		
		if (col)
			result += Character.toString((char) ('a' + originalFile));
		
		if (capture)
			result += "x";
		
		result += (Character.toString((char) ('a' + destinationFile)))+ (8-destinationRow);
		return result;
	}
	
	public boolean equals(Move m)
	{
		if (m == null)
			return false;
		if (m.getPiece().getClass()    == piece.getClass() 
			&& m.getPiece().getColor() == piece.getColor() 
			&& m.getOriginalRow()      == this.originalRow 
			&& m.getOriginalFile()     == this.originalFile
			&& m.getDestinationFile()  == this.destinationFile 
			&& m.getDestinationRow()   == this.destinationRow)
		{
			if (m.getPromotionPiece() == null)
				return promotionPiece == null;
			return this.promotionPiece.equals(m.promotionPiece);
		}
		return false;
	}
	
	public Move clone()
	{
		Move result = new Move(piece, originalRow, originalFile, destinationRow, destinationFile);
		result.promotionPiece = this.promotionPiece;
		result.capturedPiece = this.capturedPiece;
		result.enPassant = this.enPassant;
		result.castleKingside = this.castleKingside;
		result.castleQueenside = this.castleQueenside;
		return result;
	}
	
	/**
	 * @return The value of the move, for the purposes of 
	 * move ordering (MVV-LVA)
	 */
	public int getValue()
	{
		int thisValue = 0;
		if (piece instanceof Pawn) thisValue = 100;
		else if (piece instanceof Knight) thisValue = 305;
		else if (piece instanceof Bishop) thisValue = 320;
		else if (piece instanceof Rook)   thisValue = 520;
		else if (piece instanceof Queen)  thisValue = 950;
		int result = 0;
		if (this.getPromotionPiece() != null) {
			Piece p = this.getPromotionPiece();
			if (p instanceof Knight) result += 305;
			if (p instanceof Bishop) result += 320;
			if (p instanceof Rook) result += 520;
			if (p instanceof Queen) result += 950;
		}
		if (this.getCapturedPiece() != null) {
			Piece p = this.getPromotionPiece();
			if (p instanceof Pawn)   result += 100;
			if (p instanceof Knight) result += 305;
			if (p instanceof Bishop) result += 320;
			if (p instanceof Rook) result += 520;
			if (p instanceof Queen) result += 950;
		}
		return result - thisValue;
	}
	
	/**
	 * @return the piece
	 */
	public Piece getPiece() {
		return piece;
	}

	/**
	 * @return the originalRow
	 */
	public int getOriginalRow() {
		return originalRow;
	}

	/**
	 * @return the originalFile
	 */
	public int getOriginalFile() {
		return originalFile;
	}

	/**
	 * @return the destinationRow
	 */
	public int getDestinationRow() {
		return destinationRow;
	}

	/**
	 * @return the destinationFile
	 */
	public int getDestinationFile() {
		return destinationFile;
	}
	
	public boolean getEnPassant()
	{
		return enPassant;
	}
	
	public void setEnPassant(boolean t)
	{
		enPassant = t;
	}
	
	public void setPromotionPiece(Piece p)
	{
		this.promotionPiece = p;
	}
	
	public Piece getPromotionPiece()
	{
		return this.promotionPiece;
	}
	
	public void setCapturedPiece(Piece p)
	{
		capturedPiece = p;
	}
	
	public Piece getCapturedPiece()
	{
		return capturedPiece;
	}
	
	public boolean getCastleKingside(){
		return castleKingside;
	}
	
	public void setCastleKingside(boolean castleKingside)
	{
		this.castleKingside = castleKingside;
	}
	
	public boolean getCastleQueenside() {
		return castleQueenside;
	}

	public void setCastleQueenside(boolean castleQueenside) {
		this.castleQueenside = castleQueenside;
	}
}