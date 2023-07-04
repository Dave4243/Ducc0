package chess;

import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * @author Dave4243
 */
public class Knight extends Piece{
	public Knight(int color) {
		super(color, icon(color));
	}

	private static ImageIcon icon(int c)
	{
		if (c == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhiteKnight.png");
		}
		else if (c == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackKnight.png");
		}
		return null;
	}
	
	public boolean canMove(Move m) {
		// TODO Auto-generated method stub
		if (Math.abs(m.getOriginalRow() - m.getDestinationRow()) == 1 && Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 2
		||  Math.abs(m.getOriginalRow() - m.getDestinationRow()) == 2 && Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 1)
		{
			return true;
		}
		return false;
	}
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		if (row + 2 < 8)
		{
			if (col+1 < 8)
				result.add(new Move(this, row, col, row+2, col+1));
			
			if (col-1 >= 0)
				result.add(new Move(this, row, col, row+2, col-1));
		}
		
		if (row - 2 >= 0)
		{
			if (col+1 < 8)
				result.add(new Move(this, row, col, row-2, col+1));
			
			if (col-1 >= 0)
				result.add(new Move(this, row, col, row-2, col-1));
		}
		
		if (col + 2 < 8)
		{
			if (row+1 < 8)
				result.add(new Move(this, row, col, row+1, col+2));
		
			if (row-1 >= 0)
				result.add(new Move(this, row, col, row-1, col+2));
		}
		
		if (col - 2 >= 0)
		{
			if (row+1 < 8)
				result.add(new Move(this, row, col, row+1, col-2));
			
			if (row-1 >= 0)
				result.add(new Move(this, row, col, row-1, col-2));
		}
		return result;
	}
	
	public String toString()
	{
		return "N";
	}
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'N';
		
		return 'n';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 2 : 3;
	}
}
