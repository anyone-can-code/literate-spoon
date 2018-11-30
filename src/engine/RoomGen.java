package engine;

import java.util.ArrayList;
import java.util.Arrays;

import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;

public abstract class RoomGen {
	public static void gen(ArrayList<Room> rooms, ArrayList<Object> objectQueue) {
		rooms.add(new Room(0, 0, "Standard Room"));
		
		Object o = new Object("red [brick]", "on a", null);
		o.injury = Object.type.shatters;
		Object reference = new Object("nice hand-knitted [carpet]", o, null);
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = new Object("deformed [spider]", "on your", null);
		o.injury = Object.type.squishes;
		reference = new Object("[face]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = Engine.Consumable("dead [corpse]", "lying on", null, 10);
		o.injury = Object.type.bruises;
		o.holdable = null;
		reference = rooms.get(0).floor;
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = new Object("old wooden [bookshelf]", "in", null);
		o.injury = Object.type.shatters;
		o.holdable = null;
		o.container.addAll(Arrays.asList(new Object("dusty old [book]", o, null),
				new Object("trigonometry [textbook]", o, null), new Object("[jar] full of candy", o, null)));
		reference = new Object("the back of the [room]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		Entity e = new Entity("an old [man]", "standing in front of", 
				(Player p, Engine e1) -> {
					Terminal.print("The old man says hi.");
					}, 
				(Engine e2) -> {
					Terminal.println("The old man dies. He leaves you a corpse as a parting gift.");
					Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
					obj.injury = Object.type.bruises;
					obj.holdable = null;
					Object ref = new Object("the [floor]", obj, null);
					ref.abstractNoun();
					obj.reference = ref;
					objectQueue.add(obj);
					});
		reference = new Object("[you]", o, null);
		reference.abstractNoun();
		e.reference = reference;
		rooms.get(0).objects.add(e);
	}
}
