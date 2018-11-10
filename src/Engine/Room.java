package Engine;

public class Room {
	public int[] coords;//x, y, z
	
	
	public Room(int x, int y, int z) {
		coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	public String toString() {
		return coords[0] + ", " + coords[1] + ", " + coords[2];
	}
}