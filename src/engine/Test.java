package engine;
import java.io.*;
import java.util.Scanner;
public class Test {
	
	public static void main(String args[]) {
		testPosition("startpos");
//		testPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
//		Piece p = new Piece(0, 5);
//		Entry e = new Entry(999999999, new Move(4, 6), 0, 12, 6);
//		System.out.println("Move size: " + ObjectSizeFetcher.getObjectSize(e.getBestMove()));
//		System.out.println("Piece size: " + ObjectSizeFetcher.getObjectSize(p));
//		Board b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
//		b.toString();

//		Board b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
////     	Board b = new Board();
//		MoveGenerator mg = new MoveGenerator();
//		long start = System.nanoTime();
//		int totalPositions = moveGenerateTest(5, 0, b, mg);
//		System.out.println("Total: " + totalPositions);
//		long end = System.nanoTime();
//		double totalTime = (end - start)/1000000000.0;
//		System.out.println("Total Time: " + totalTime);
//		System.out.println("NPS: " + totalPositions/totalTime);
		
//		int move = Move.make(5, 12, Piece.QUEEN);
//		move = Move.setCap(move, new Piece(1, Piece.PAWN));
//		System.out.println(move);
//		System.out.println("From: " + Move.getFrom(move));
//		System.out.println("To: " + Move.getTo(move));
//		System.out.println("Promotion: " + Move.getPromotion(move));
//		System.out.println("Captured: " + Move.getCaptured(move));
//		System.out.println("En Passant: " + Move.getCastle(move));
//		System.out.println("Castling flags: " + Move.getCastle(move));
//		System.out.println(Move.toString(move));
//		BitBoard.print(move);
//		System.out.println("===========================");
//		
//		TranspositionTable tt = new TranspositionTable();
//		tt.store(123456789, move, -777, 25, (byte)0);
//
//		System.out.println(tt.lookup(123456789).getBestMove());
//		int ttMove = tt.lookup(123456789).getBestMove();
//		System.out.println("From: " + Move.getFrom(ttMove));
//		System.out.println("To: " + Move.getTo(ttMove));
//		System.out.println("Promotion: " + Move.getPromotion(ttMove));
//		System.out.println("Captured: " + Move.getCaptured(ttMove));
//		System.out.println(Move.toString(ttMove));
//		System.out.println("En Passant: " + Move.getCastle(ttMove));
//		System.out.println("Castling flags: " + Move.getCastle(ttMove));
//		BitBoard.print(ttMove);
//		System.out.println("==================");
		
//		getCommands();
		
		
//		BitBoard.print(Tables.pawnAttacks[0][13]);
	}
	
	private static void testPosition(String fen) {
		MoveGenerator gen = new MoveGenerator();
		Board b;
		if (fen.equals("startpos")) {
			b = new Board();
		}
		else {
			b = new Board(fen);
		}

		Search s = new Search(b);
		long startTime = System.nanoTime();
		System.out.println(Move.toString(s.getBestMove(b, 600000, 0)));
		long endTime = System.nanoTime();
		
		long timeSpent = endTime - startTime;
		System.out.println(timeSpent/1000000000.0);
	}
	
	private static int moveGenerateTest(int depth, int ply, Board b, MoveGenerator gen)
	{
		if (depth == 0)
			return 1;

		MoveList moveList = new MoveList();
                gen.generateMoves(b, false, moveList);
		
		int totalPositions= 0;
		int positionsForMove = 0;
		
		for (int i = 0; i < moveList.size(); i++){
			int m = moveList.moves[i];
			if (b.doMove(m)) {
//				if (b.getZobristKey() != Zobrist.getKey(b)) {
//					System.out.println("ERRORRR");
//				}
				int numPositions = moveGenerateTest(depth-1, ply +1,  b, gen);
				totalPositions += numPositions;
				positionsForMove += numPositions;
				b.undoMove(m);
				if (ply == 0) {
					System.out.println(Move.toString(m) + ": " + positionsForMove);
					positionsForMove = 0;
				}
			}
			else b.undoMove(m);
		}
		return totalPositions;
	}
	
	private static void getCommands() {
		try {
			File output = new File("output.txt");
			File input = new File("TESTING.txt");
			Scanner s = new Scanner(input);
			Writer w = new FileWriter(output);
			
			while (s.hasNextLine()) {
				String data = s.nextLine();
				if (!data.contains("<") && !data.contains("MOVE32") && !data.contains("isready")) {
					w.write(data.substring(22) + '\n');
					w.write('\n');
					if (data.contains("go wtime")) {
						w.write('\n');
					}
				}
			}
			
			s.close();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
