package engine;

public class MoveList {

	public int[] moves;
	public int[] quietsPlayed;
	public int[] scores;
	private int count;
	private int quietCounter;
	
	public MoveList() {
		moves = new int[128];
		quietsPlayed = new int[128];
		scores = new int[128];
		count = 0;
		quietCounter = 0;
	}
	
	public void add(int move) {
		moves[count] = move;
		count++;
	}
	
	public void addQuiet(int move) {
		quietsPlayed[quietCounter & 0x7f] = move;
		quietCounter++;
	}
	
	public int size() {
		return count;
	}
	
	public int numQuiets() {
		return quietCounter;
	}
}
