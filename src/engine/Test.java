package engine;

public class Test {
	
	public static void main(String args[]) {
		MoveGenerator gen = new MoveGenerator();
		Board b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
//		System.out.println(new Evaluator().evaluatePosition(b));
//		Board b = new Board();
		Search s = new Search(b);
		long startTime = System.nanoTime();
		System.out.println(s.getBestMove(b, 600000, 0));
//		b.makeNullMove();
//		System.out.println(moveGenerateTest(6, 0, b, gen));
		long endTime = System.nanoTime();
		
		long timeSpent = endTime - startTime;
		System.out.println(timeSpent/1000000000.0);
		
		for (int[] i : Tables.lmrTable) {
			for (int j : i) {
				System.out.print(j + " ");
			}
			System.out.println();
		}
	}
	
	private static int moveGenerateTest(int depth, int ply, Board b, MoveGenerator gen)
	{
		if (depth == 0)
			return 1;

		
		MoveList moveList = gen.generateMoves(b, false);
		
		int totalPositions= 0;
		int positionsForMove = 0;
		
		for (int i = 0; i < moveList.size(); i++){
			Move m = moveList.moves[i];
			if (b.doMove(m)) {
//				if (b.getZobristKey() != Zobrist.getKey(b)) {
//					System.out.println("ERRORRR");
//				}
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
