package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;

public abstract class RoomGen {
	public static void gen(ArrayList<Room> rooms, ArrayList<Object> objectQueue) {
		Random rand = new Random();
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
		
		Entity e = new Entity("an old [man]", "standing in front of", (Engine e2) -> {
			Terminal.println("The old man dies. He leaves you a corpse as a parting gift.");
			Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
			obj.injury = Object.type.bruises;
			obj.holdable = null;
			Object ref = new Object("the [floor]", obj, null);
			ref.abstractNoun();
			obj.reference = ref;
			objectQueue.add(obj);
		});
		e.interaction = (Player p, Engine eng) -> {
			HashMap<String, TwoParamFunc<Entity, Player>> options1 = new HashMap<String, TwoParamFunc<Entity, Player>>();
			options1.put("yes", (Entity e1, Player p1) -> {
				HashMap<String, TwoParamFunc<Entity, Player>> options2 = new HashMap<String, TwoParamFunc<Entity, Player>>();
				options2.put("yes", (Entity e2, Player p2) -> {
					e2.attack(p2);
				});
				options2.put("no", (Entity e2, Player p2) -> {
					if(p2.agility + rand.nextInt(3) - 1 > 10) {
						Terminal.println("You dodged the attack.");
					} else {
						Terminal.println("You failed to dodge his attack.");
						e2.attack(p2);
					}
				});
				Entity.Dialogue("The old man tries to kill you. Let him?", options2, e1, p1);
			});
			options1.put("no", (Entity e1, Player p1) -> {});
			Entity.Dialogue("The old man says hi. Greet him? [yes] [no]", options1, e, p);
		};
		reference = new Object("[you]", o, null);
		reference.abstractNoun();
		e.reference = reference;
		rooms.get(0).objects.add(e);
	}
}
