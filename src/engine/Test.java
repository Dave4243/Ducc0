package engine;
import java.util.ArrayList;

public class Test {
	public static void main(String args[]) {
		Tables.computeFirstRankAttacks();
		MoveGenerator gen = new MoveGenerator();
		Board b = new Board("r3k2r/P2p1p1p/5nbN/nP6/BBP5/q4N2/Pp5P/R2Q1RK1 w kq - 0 1");

//		System.out.println(new Evaluator().evaluatePosition(b));
		
//		Board b = new Board();
		Search s = new Search(b);
		
//		long startTime = System.nanoTime();
		System.out.println(s.getBestMove(b, 300000, 0));
//		System.out.println(moveGenerateTest(5, 0, b, gen));
//		long endTime = System.nanoTime();
		
//		long timeSpent = endTime - startTime;
		
//		System.out.println("Time Taken: " + timeSpent/1000000000.0);
//		b.toString();
	}
	
	private static int moveGenerateTest(int depth, int ply, Board b, MoveGenerator gen)
	{
		if (depth == 0)
			return 1;

		ArrayList<Move> moves = gen.generateMoves(b, false);
		
		int totalPositions= 0;
		int positionsForMove = 0;
		
		for (Move m : moves){
			if (b.doMove(m)) {
				int numPositions = moveGenerateTest(depth-1, ply +1,  b, gen);
				totalPositions += numPositions;
				positionsForMove += numPositions;
				b.undoMove(m);
				if (ply == 0) {
					System.out.println(m + ": " + positionsForMove);
					positionsForMove = 0;
				}
			}
			else b.undoMove(m);
		}
		return totalPositions;
	}
}
