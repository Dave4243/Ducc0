package chess;

import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * @author Dave4243
 * The Bishop.java class is a Piece that is either black or white and can only move 
 * in a diagonal direction.
 */
public class Bishop extends Piece {
	
	/**
	 * Constructor
	 * @param color The color of the piece
	 */
	public Bishop(int color)
	{
		super(color, icon(color));
	}
	
	/**
	 * Helper method for constructor, allows the constructor to only take in 1 argument
	 * @param c The color of the piece
	 * @return  The appropriate ImageIcon according the the color argument passed
	 */
	private static ImageIcon icon(int color)
	{
		if (color == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhiteBishop.png");
		}
		else if (color == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackBishop.png");
		}
		return null;
	}
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		
		addDiagonalMoves(result, row, col);
		
		return result;
	}

	/**
	 * Checks if the move is a potentially valid 
	 * @param m The move to check
	 */
	public boolean canMove(Move m) {
		if (Math.abs(m.getOriginalFile() - m.getDestinationFile()) == Math.abs(m.getOriginalRow() - m.getDestinationRow()))
		{
			return true;
		}
		return false;
	}
	
	public String toString()
	{
		return "B";
	}
	
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'B';
		return 'b';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 4 : 5;
	}
}