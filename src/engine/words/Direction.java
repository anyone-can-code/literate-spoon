package engine.words;

public class Direction extends Word {
	public Direction(String list, String direction) {
		super(list);

		// direction has 3 chars represented dx + 1, dy + 1, dz + 1;
		value = direction;// 0 N, 1 E, 2 S, 3 W, 4 U, 5 D
	}
}
