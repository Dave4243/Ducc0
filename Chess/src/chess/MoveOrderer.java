package chess;

import java.util.ArrayList;

/**
 * @author Dave4243
 * The MoveOrderer.java class orders moves 
 */
public class MoveOrderer {
	/**
	 * 1. Hash/PV Moves (Moves deemed best from the transposition table)
	 * 2. Winning captures/promotions
	 * 3. Equal captures/promotions
	 * 4. Everything else
	 */
	public void fullSort(ArrayList<Move> moves, TranspositionTable t, long hash) {
		TranspositionEntry entry = t.lookup(hash);
		Move hashMove = null;
		if (entry != null) {
			hashMove = entry.getBestMove();
		}
		for (int i = 0; i < moves.size() - 1; i++) {
			int value = computeValue(moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(moves.get(j));
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
	
	// no hashmove (implemented only in quiescence search)
	public void fullSort(ArrayList<Move> moves) {
		for (int i = 0; i < moves.size() - 1; i++) {
			int value = computeValue(moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(moves.get(j));
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
	public void partialSort(ArrayList<Move> moves, int sorts) {
		sorts = moves.size() < sorts ? moves.size() : sorts;
		for (int i = 0; i < sorts; i++) {
			int value = computeValue(moves.get(i));
			int maxIndex = i;
			for (int j = i; j < moves.size(); j++) {
				int newValue = computeValue(moves.get(j));
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
	
	private int computeValue(Move m) {		
		// no captures are searched last
		if (m.getCapturedPiece() == null)
			return -5;

		int promotion = convertPiece(m.getPromotionPiece());
		int aggressor = convertPiece(m.getPiece());
		int victim    = convertPiece(m.getCapturedPiece());
		return victim - aggressor + promotion;
	}
	
	private int convertPiece(Piece p) {
		if (p == null)
			return 0;
		if (p instanceof Pawn)
			return 10;
		else if (p instanceof Knight || p instanceof Bishop)
			return 30;
		else if (p instanceof Rook)
			return 50;
		else if (p instanceof Queen)
			return 90;
		else 
			return 1000000;
	}
}
