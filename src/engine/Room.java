package engine;

import java.util.ArrayList;
import engine.things.Object;

public class Room implements Cloneable {
	public int[] coords;// x and y
	public ArrayList<Object> objects = new ArrayList<Object>();
	public ArrayList<Room> nestedMap = new ArrayList<Room>();
	public Room westEntry;
	public Room eastEntry;
	public Room northEntry;
	public Room southEntry;
	public Room fatherRoom = null;
	public Object[][] area;

	Object floor = new Object("the [floor]", (String) null, null);

	public String description;

	public Room(int width, int height) {
		coords = new int[2];
	}

	public Room(int x, int y, int width, int height, String description) {
		coords = new int[2];
		coords[0] = x;
		coords[1] = y;
		area = new Object[width][height];
		floor.label = '#';
		for(Object[] objs : area) {
			for(int i = 0; i < objs.length; i++) {
				objs[i] = floor;
			}
		}
		floor.abstractNoun();
		objects.add(floor);
		this.description = description;
	}

	public void addObject(Object o, int x, int y) {
		objects.add(o);
		area[x][y] = o;
		o.x = x;
		o.y = y;
		try {
			o.reference.x = o.x;
			o.reference.y = o.y;
		} catch(Exception e) {}
	}

	public void addRoom(Room r) {
		r.fatherRoom = this;
		nestedMap.add(r);
	}

	public void setEntries() {
		Room farthest = nestedMap.get(0);

		for (Room r : nestedMap) {
			if (r.coords[0] < farthest.coords[0])
				farthest = r;
		}
		westEntry = farthest;

		for (Room r : nestedMap) {
			if (r.coords[0] > farthest.coords[0])
				farthest = r;
		}
		eastEntry = farthest;

		for (Room r : nestedMap) {
			if (r.coords[1] < farthest.coords[1])
				farthest = r;
		}
		southEntry = farthest;

		for (Room r : nestedMap) {
			if (r.coords[1] > farthest.coords[1])
				farthest = r;
		}
		northEntry = farthest;
	}

	public String toString() {
		return coords[0] + ", " + coords[1];
	}

	@SuppressWarnings("unchecked")
	public Room getClone() {
		try {
			Room r = (Room) super.clone();
			r.objects = (ArrayList<Object>) this.objects.clone();
			return r;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
