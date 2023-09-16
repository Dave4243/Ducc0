package engine;

import java.util.ArrayList;
/**
 * @author Dave4243
 * The MoveOrderer.java class orders moves 
 */
public class MoveOrderer {
	
	private static int[] pieceValues = {0, 100, 315, 315, 520, 950, 32000};
	
//	public static Move[][] killerTable = new Move[100][2];
	public static int[][][] historyTable = new int[2][64][64];
	private static final int captureBonus = 65536;
	
	/**
	 * 1. Hash/PV Moves (Moves deemed best from the transposition table)
	 * 2. Winning captures/promotions
	 * 3. Equal captures/promotions
	 * 4. Killer Moves
	 * 5. Quiet moves ordered based on history heuristic
	 * 6. Losing captures/promotions
	 */
	public static void fullSort(Board b, ArrayList<Move> moves, Move hashMove, int ply) {
		for (int i = 0; i < moves.size() - 1; i++) {
			int value = computeValue(b, moves.get(i), ply);
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(b, moves.get(j), ply);
				
				if (moves.get(j).equals(hashMove)) {
					maxIndex = j;
					break;
				}
				if (newValue > value) {
					value = newValue;
					maxIndex = j;
				}
			}
			Move temp = moves.get(i);
			moves.set(i, moves.get(maxIndex));
			moves.set(maxIndex, temp);
		}
	}
	
	// no hashmove (or use in quiescence search)
	public static void fullSort(Board b, ArrayList<Move> moves) {
		for (int i = 0; i < moves.size() - 1; i++) {
			int value = computeValue(b, moves.get(i), -1);
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(b, moves.get(j), -1);
				if (newValue > value) {
					value = newValue;
					maxIndex = j;
				}
			}
			Move temp = moves.get(i);
			moves.set(i, moves.get(maxIndex));
			moves.set(maxIndex, temp);
		}
	}
	
	private static int computeValue(Board b, Move m, int ply) {
		// quiet moves are searched after equal captures and killers
		if (m.getCapturedPiece() == null && m.getPromotionPiece() == Piece.NULL) {
//			if (ply != -1 && m.equals(killerTable[ply][0])) {
//				return 60000;
//			}
//			if (ply != -1 && m.equals(killerTable[ply][1])) {
//				return 59000;
//			}

			return historyTable[b.getSideToMove()][m.getFromSquare()][m.getToSquare()];
		}
		
		int promotion = pieceValues[m.getPromotionPiece() + 1];
		
		int aggressor = pieceValues[b.getPiece(m.getFromSquare()).getType()];

		int victim = pieceValues[m.getCapturedPieceType()];

		int score = victim - aggressor + promotion;
		
		if (score >= 0) score += captureBonus;
		else score -= captureBonus;
		
		return score; // equal capture = 65536
	}
	
//	public static void clearKillers() {
//		killerTable = new Move[100][2];
//	}
	
	public static void clearHistory() {
		historyTable = new int[2][64][64];
	}
	
	private MoveOrderer() {};
}