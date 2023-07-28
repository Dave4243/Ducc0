package engine;
import java.security.SecureRandom;

/**
 * @author Dave4243
 * @description Turns a position into a Zobrist key
 */
public class Zobrist {
	public static final long[] KEYS;
	
	public static int SIDEINDEX = 768;
	public static int CASTLEINDEX = 769;
	public static int ENPASSANTINDEX = 785;

	static {
        KEYS = new long[793];
        SecureRandom r = new SecureRandom();
        
        r.setSeed(3141592653589L);
        
        for (int i = 0; i < 793; i++) {
        	KEYS[i] = r.nextLong();
        }     
	}
	public static long getKey(Board b) {
		long result = 0;
		for (int i = 0; i < 64; i++) {
			Piece temp = b.getPiece(i);
			if (temp != null) {
				int index = 12*i + temp.getType()*2 + temp.getColor();
				result ^= KEYS[index];
			}
		}
		if (b.getSideToMove() == Piece.BLACK) result ^= KEYS[SIDEINDEX];
		
		result ^= KEYS[CASTLEINDEX + b.getCastlingRights()];
		
		if (b.getEnPassantTarget() != 0)
			result ^= KEYS[ENPASSANTINDEX + (BitBoard.getLSB(b.getEnPassantTarget()) & 7)];
		
		return result;
	} 
	
	private Zobrist() {};
}
