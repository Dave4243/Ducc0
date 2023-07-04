package chess;

import java.util.*;

// tests stuff
public class Test
{
	MoveGenerator generator = new MoveGenerator();
	Board board = new Board();
	PositionEvaluator eval = new PositionEvaluator();
	
	public static void main(String args[]) {
		Test t = new Test();

		t.board = FenConverter.convert("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
		ChessEngine engine = new ChessEngine(t.board);
		
		long startTime = System.nanoTime();
//		System.out.println(t.moveGenerateTest(1));
		System.out.println(engine.search(7, 0, -1000000, 1000000));
		long endTime = System.nanoTime();
		
		System.out.println("Time taken: " + (endTime-startTime)/1000000000.0);
		
		//t.board = new Board();
	}
	
	private int moveGenerateTest(int depth)
	{
		if (depth == 0)
		{
			return 1;
		}
		
		List<Move> moves = generator.generateAllPossibleMoves(board.getSideToMove(), board);
		
		int totalPositions= 0;
		
		for (Move m : moves)
		{
			if (m != null)
			{
				board.doMove(m);
				totalPositions += moveGenerateTest(depth-1);
				board.undoMove(m);

			}
		}
		return totalPositions;
	}
	
	public String toString()
	{
		return board.toString();
	}
	
	public void movePiece(int a, int b, int c, int d)
	{
		board.setPiece(board.getPiece(a, b), c, d);
		board.removePiece(a, b);
	}
	
	public void unmovePiece(int a, int b, int c, int d)
	{
		board.setPiece(board.getPiece(c, d), a, b);
		board.removePiece(c, d);
	}
	public static int convertChar(char letter) {
	    // Convert the letter to lowercase to simplify the calculation
	    letter = Character.toLowerCase(letter);
	    
	    // Subtract the ASCII code of 'a' from the ASCII code of the letter
	    // and add 1 to get the position in the alphabet
	    int position = letter - 'a';
	    
	    return position;
	}

}