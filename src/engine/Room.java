package engine;

import java.util.ArrayList;
import engine.things.Object;

public class Room {
	public int[] coords;// x, y, z
	public ArrayList<Object> objects = new ArrayList<Object>();
	Object floor = new Object("the [floor]", (String)null, null);
	
	public Room(int x, int y, int z) {
		coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		floor.abstractNoun();
	}

	public String toString() {
		return coords[0] + ", " + coords[1] + ", " + coords[2];
	}
}
