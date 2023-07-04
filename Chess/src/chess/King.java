package chess;

import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * @author Dave4243
 */
public class King extends Piece {
	public King(int color) {
		super(color, icon(color));
	}

	private static ImageIcon icon(int c)
	{
		if (c == Piece.WHITE)
		{
			return new ImageIcon("src/chess/WhiteKing.png");
		}
		else if (c == Piece.BLACK)
		{
			return new ImageIcon("src/chess/BlackKing.png");
		}
		return null;
	}

	@Override
	public boolean canMove(Move m) {
		if ((m.getDestinationFile() - m.getOriginalFile() == 0 && Math.abs(m.getDestinationRow() - m.getOriginalRow())   == 1)
		||  (m.getDestinationRow() - m.getOriginalRow()   == 0 && Math.abs(m.getDestinationFile() - m.getOriginalFile()) == 1)
		|| (Math.abs(m.getDestinationFile() - m.getOriginalFile()) == 1 && Math.abs(m.getDestinationRow() - m.getOriginalRow()) == 1))
		{
			return true;
		}
		
		// castling
		if (super.getColor() == Piece.WHITE
				&& m.getOriginalRow() == 7 && m.getOriginalFile() == 4)
		{
			return Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 2 && m.getOriginalRow() == m.getDestinationRow();
		}
		
		if (super.getColor() == Piece.BLACK
				&& m.getOriginalRow() == 0 && m.getOriginalFile() == 4)
		{
			return Math.abs(m.getOriginalFile() - m.getDestinationFile()) == 2 && m.getOriginalRow() == m.getDestinationRow();
		}
		return false;
	}
	
	public ArrayList<Move> generateFakeMoves(int row, int col)
	{
		ArrayList<Move> result = new ArrayList<Move>();
		
		for (int i = -1; i <= 1; i++)
		{
			for (int j = -1; j <= 1; j++)
			{
				if (i == 0 && j == 0) {
					continue;
				}
				// bound conditions (within the board)
				if ((row + i < 8 && row+i >= 0) && (col + j < 8 && col+j >= 0)) {
					result.add(new Move(this, row, col, row+i, col+j));
				}
			}
		}
		if (super.getColor() == Piece.BLACK && row == 0 && col == 4
				|| (super.getColor() == Piece.WHITE && row == 7 && col == 4))
		{
			result.add(new Move(this, row, col, row, col + 2));
			result.add(new Move(this, row, col, row, col - 2));
		}
		return result;
	}
	
	public String toString()
	{
		return "K";
	}
	
	public char toChar()
	{
		if (super.getColor() == Piece.WHITE)
			return 'K';
		
		return 'k';
	}
	
	public int toInt() {
		return super.getColor() == Piece.WHITE ? 10 : 11;
	}
}
