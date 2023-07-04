package chess;

import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * @author Dave4243
 */
public class Queen extends Piece{
	public Queen(int color) {
		super(color, icon(color));
		// TODO Auto-generated constructor stub
	}
	
	private static ImageIcon icon(int c)
	{
		if (c == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhiteQueen.png");
		}
		else if (c == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackQueen.png");
		}
		return null;
	}

	public boolean canMove(Move m) {
		// TODO Auto-generated method stub}
		if (m.getOriginalRow() - m.getDestinationRow() == 0          // horizontal movement
		 || m.getOriginalFile() - m.getDestinationFile() == 0        // vertical movement
		 || Math.abs(m.getOriginalFile() - m.getDestinationFile()) == Math.abs(m.getOriginalRow() - m.getDestinationRow())) // diagonal movement
			
		{
			return true;
		}
		return false;
	}
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		addDiagonalMoves(result, row, col);
		addHorizontalMoves(result, row, col);
		return result;
	}
	
	public String toString()
	{
		return "Q";
	}
	
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'Q';
		
		return 'q';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 8 : 9;
	}
}
