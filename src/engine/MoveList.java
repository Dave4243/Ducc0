package engine;

public class MoveList {

	public Move[] moves;
	public Move[] quietsPlayed;
	public int[] scores;
	private int count;
	private int quietCounter;
	
	public MoveList() {
		moves = new Move[256];
		quietsPlayed = new Move[128];
		scores = new int[256];
		count = 0;
		quietCounter = 0;
	}
	
	public void add(Move m) {
		moves[count] = m;
		count++;
	}
	
	public void addQuiet(Move m) {
		quietsPlayed[quietCounter & 0x7f] = m;
		quietCounter++;
	}
	
	public int size() {
		return count;
	}
	
	public int numQuiets() {
		return quietCounter;
	}
}
