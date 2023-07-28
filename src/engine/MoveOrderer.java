package engine;

import java.util.ArrayList;
/**
 * @author Dave4243
 * The MoveOrderer.java class orders moves 
 */
public class MoveOrderer {
	
	private static int[] pieceValues = {100, 310, 320, 520, 950, 32000};
	
	public static Move[][] killerTable = new Move[100][2];
	
	/**
	 * 1. Hash/PV Moves (Moves deemed best from the transposition table)
	 * 2. Winning captures/promotions
	 * 3. Equal captures/promotions
	 * 4. Losing captures/promotions
	 * 4. Everything else
	 */
	public static void fullSort(Board b, ArrayList<Move> moves, Move hashMove, long hash, int ply) {
		for (int i = 0; i < moves.size() - 1; i++) {
			int value = computeValue(b, moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(b, moves.get(j));
				
				if (isKiller(moves.get(j), ply))
					newValue = -15; // right after an equal capture
				
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
			int value = computeValue(b, moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(b, moves.get(j));
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
	
	// only sorts a few moves specified by the parameter sorts
	public static void partialSort(Board b, ArrayList<Move> moves, int sorts, int ply) {
		sorts = moves.size() < sorts ? moves.size() : sorts;
		for (int i = 0; i < sorts; i++) {
			int value = computeValue(b, moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(b, moves.get(j));
				
				if (isKiller(moves.get(j), ply))
					newValue = -15; // right after an equal capture
				
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
	
	private static boolean isKiller(Move m, int ply) {
		if (m.equals(killerTable[ply][0]) || m.equals(killerTable[ply][1]))
			return true;
		return false;
	}
	
	private static int computeValue(Board b, Move m) {
		
		// non captures are searched after equal captures and killers
		if (m.getCapturedPiece() == null) {
			return -50;
		}
		
		int promotion = 0;
		if (m.getPromotionPiece() != Piece.NULL) {
			promotion = pieceValues[m.getPromotionPiece()];
		}
		
		int aggressor = pieceValues[b.getPiece(m.getFromSquare()).getType()];

		int victim    = 0;
		if (m.getCapturedPiece() != null) {
			victim = pieceValues[m.getCapturedPiece().getType()];
		}
		
		return victim - aggressor + promotion;
	}
	
	public static void clearKillers() {
		killerTable = new Move[100][2];
	}
	
	private MoveOrderer() {};
}