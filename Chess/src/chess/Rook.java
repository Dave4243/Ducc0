package chess;

import java.util.ArrayList;
import javax.swing.*;

/**
 * @author Dave4243
 */
public class Rook extends Piece{
	public Rook(int color) {
		super(color, icon(color));
	}
	
	private static ImageIcon icon(int c)
	{
		if (c == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhiteRook.png");
		}
		else if (c == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackRook.png");
		}
		return null;
	}

	public boolean canMove(Move m) {
		if (m.getOriginalRow() - m.getDestinationRow() == 0     // horizontal movement
		 || m.getOriginalFile() - m.getDestinationFile() == 0)  // vertical movement
		{
			return true;
		}
		return false;
	}
	
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		addHorizontalMoves(result, row, col);
		return result;
	}
	
	public String toString()
	{
		return "R";
	}
	
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'R';
		
		return 'r';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 6 : 7;
	}
}
