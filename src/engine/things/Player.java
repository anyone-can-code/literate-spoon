package engine.things;

import engine.Room;

public class Player {
	private int[] pos;
	public Room currentRoom;
	public Player(int x, int y, int z) {
		pos = new int[3];
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
	}
	
	public void changePos(String amt) {//amt example in Direction class
		pos[0] += Integer.parseInt(amt.substring(0, 1)) - 1;
		pos[1] += Integer.parseInt(amt.substring(1, 2)) - 1;
		pos[2] += Integer.parseInt(amt.substring(2)) - 1;
	}
	
	public String toString() {
		return pos[0] + ", " + pos[1] + ", " + pos[2];
	}
}
