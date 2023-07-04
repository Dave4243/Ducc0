package chess;

/**
 * @author Dave4243
 */
public class Square {
	private Piece piece;
	private int   row;
	private int   file;

	public Square(Piece p, int row, int file)
	{
		this.piece = p;
		this.row = row;
		this.file = file;
	}
	
	/**
	 * @return the piece
	 */
	public Piece getPiece() {
		return piece;
	}
	
	public void removePiece() {
		piece = null;
	}
	
	/**
	 * @param p the piece to set
	 */
	public void setPiece(Piece p) {
		this.piece = p;
	}

	public String toString()
	{
		return piece + "x: " + file + "  y: " + row;
	}
}
