package engine;

import java.util.ArrayList;
import engine.things.Object;

public class Room {
	public int[] coords;// x and y
	public ArrayList<Object> objects = new ArrayList<Object>();
	Object floor = new Object("the [floor]", (String)null, null);
	public String description;	
	
	public Room() {
		coords = new int[2];
	}

	public Room(int x, int y, String description) {
		coords = new int[2];
		coords[0] = x;
		coords[1] = y;
		floor.abstractNoun();
		this.description = description;
	}
	
	public void addObject(Object o) {
		objects.add(o);
	}

	public String toString() {
		return coords[0] + ", " + coords[1];
	}
}
