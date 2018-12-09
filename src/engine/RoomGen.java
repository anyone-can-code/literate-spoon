package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Iterator;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;

public abstract class RoomGen {
	static Random rand = new Random();

	public static void gen(ArrayList<Room> rooms, ArrayList<Object> objectQueue) {
		rooms.add(new Room(0, 0, "Standard Room"));
	}

	public static Room gen(Room map, ArrayList<Object> objectQueue) {
		Room mainArea = new Room(0, 0, "Colossal Cave");
		map.addRoom(mainArea);

		Room start = new Room(0, 0,
				"A Dark Cavern\nThe ceiling is too high for you to make out in the darkness. Cold, rough stone lies under your feet. A light wind passes over you.(1000)");
		mainArea.addRoom(start);

		Object o = new Object("red [brick]", "on a", null) {
			{
				injury = type.shatters;
				reference = new Object("nice hand-knitted [carpet]", this, null);
			}
		};
		start.objects.add(o);

		o = new Object("deformed [spider]", "on your", null) {
			{
				injury = type.bruises;
				reference = new Object("[face]", this, null) {
					{
						abstractNoun();
					}
				};
			}
		};
		start.objects.add(o);
		/*
		o = Engine.Consumable("dead [corpse]", "lying on", null, 10);
		o.injury = Object.type.bruises;
		o.holdable = null;
		reference = map.nestedMap.get(0).floor;
		o.reference = reference;
		start.objects.add(o);
		
		o = new Object("old wooden [bookshelf]", "in", null);
		o.injury = Object.type.shatters;
		o.holdable = null;
		o.container.addAll(Arrays.asList(new Object("dusty old [book]", o, null),
				new Object("trigonometry [textbook]", o, null), new Object("[jar] full of candy", o, null)));
		reference = new Object("the back of the [room]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		start.objects.add(o);*/

		Entity e = new Entity("old [man]", "standing in front of", (Engine e2) -> {
			Terminal.println("The old man dies. He leaves you a corpse as a parting gift.");
			Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
			obj.injury = Object.type.bruises;
			obj.holdable = null;
			Object ref = new Object("the [floor]", obj, null);
			ref.abstractNoun();
			obj.reference = ref;
			objectQueue.add(obj);
		}) {
			{
				interaction = (Player p, Engine eng) -> {
					Dialogue("The old man says hi. Greet him?", new HashMap<String, OneParamFunc<Player>>() {
						{
							put("yes", (Player p1) -> {
								Dialogue("The old man tries to kill you. Let him?",
										new HashMap<String, OneParamFunc<Player>>() {
											{
												put("yes", (Player p2) -> {
													attack(p2);
												});
												put("no", (Player p2) -> {
													if (p2.agility + rand.nextInt(3) - 1 >= agility) {
														Terminal.println("You dodged the attack.");
													} else {
														Terminal.println("You failed to dodge his attack.");
														attack(p2);
													}
												});
											}
										}, p1);
							});
							put("no", (Player p1) -> {
								Terminal.println("You walk away, leaving him slightly confused and annoyed.");
								anger += 20;
							});
						}
					}, p);
				};
				inventory.add(new Object("water [bottle]", (String) null, null) {
					{
						consumability = 5;
					}
				});
				reference = new Object("[you]", this, null) {
					{
						abstractNoun();
					}
				};
			}
		};
		start.objects.add(e);
		/* Template for choices
		 	e.Dialogue("Statement", new HashMap<String, OneParamFunc<Player>>(){{
				put("option1", (Player p1) -> {
				
				});
				put("option2", (Player p1) -> {
					
				});
			}}, p);
		 */

		Room r = new Room(0, 1, "A Dark Stone Passageway");
		mainArea.addRoom(r);

		o = new Object("chunk of [obsidian]", "in a", null) {
			{
				injury = type.shatters;
				reference = new Object("small [puddle]", this, null) {
					{
						holdable = null;
					}
				};
			}
		};
		r.objects.add(o);

		mainArea.setEntries();// to be called after mainArea completely defined
		// alternatively, you could pick the entry points directly if you want more
		// control

		mainArea = new Room(0, 1, "Emerald Forest");
		map.addRoom(mainArea);

		r = new Room(0, 0,
				"A Small Grove\nTall, yellow blades of grass sway in the light breeze. The clouds are a dark grey, twisting in turmoil, a storm on its way.");
		mainArea.addRoom(r);

		mainArea.setEntries();

		compileReferences(map);

		return start;
	}

	public static void compileReferences(Room map) {
		for (Room r : map.nestedMap) {
			ArrayList<Object> references = new ArrayList<Object>();
			Iterator<Object> it = r.objects.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj.reference != null && !obj.reference.abstractObj) {
					references.add(obj.reference);
				}
				if (!obj.container.isEmpty()) {
					references.addAll(obj.container);
				}
			}
			r.objects.addAll(references);
			if (r.nestedMap.size() > 0)
				compileReferences(r);
		}
	}
}
