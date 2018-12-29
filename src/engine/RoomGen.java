package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Iterator;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;
import engine.things.Quest;

public abstract class RoomGen {

	static Random rand = new Random();

	public static void gen(ArrayList<Room> rooms, ArrayList<Object> objectQueue) {
		rooms.add(new Room(0, 0, "Standard Room"));
	}

	public static Room gen(Room map, ArrayList<Object> objectQueue) {
		Room mainArea = new Room(0, 0, "Colossal Cave");
		map.addRoom(mainArea);

		Room start = new Room(0, 0,
				"A Dark Cavern\n(1000)The ceiling is too high for you to make out in the darkness. Cold, rough stone lies under your feet. A light wind passes over you.");
		mainArea.addRoom(start);

		Object o = new Object("red [brick]", "on a", null);
		o.injury = Object.type.shatters;
		o.reference = new Object("nice hand-knitted [carpet]", o, null);
		o.reference.injury = Object.type.squishes;
		o.damage = 3;
		start.objects.add(o);
		o = new Object("deformed [spider]", "on your", null);
		o.injury = Object.type.bruises;
		o.reference = new Object("[face]", o, null);
		o.reference.abstractNoun();
		/*
		 * o = new Object("deformed [spider]", "on your", null); o.injury =
		 * Object.type.squishes; o.damage = 2; reference = new Object("[face]", o,
		 * null); reference.abstractNoun(); o.reference = reference; >>>>>>>
		 * refs/heads/actualMaster start.objects.add(o);
		 * 
		 * o = Engine.Consumable("dead [corpse]", "lying on", null, 10); o.injury =
		 * Object.type.bruises; o.holdable = null; reference =
		 * map.nestedMap.get(0).floor; o.reference = reference; start.objects.add(o);
		 */

		o = new Object("old wooden [bookshelf]", "in", null);
		o.injury = Object.type.shatters;
		o.holdable = null;

		Object o2 = new Object("dusty old [book]", o, null);
		o2.text = map.description + "\nBy Anonymous";
		o.container.addAll(Arrays.asList(o2, new Object("[jar] full of candy", o, null)));
		Object reference = new Object("the back of the [room]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		start.objects.add(o);

		Entity e = new Entity("old [man]", "standing in front of", (Engine e2) -> {
			Terminal.println("The old man dies. He leaves you a corpse as a parting gift.");
			Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
			obj.injury = Object.type.bruises;
			obj.holdable = null;
			obj.reference = e2.protag.currentRoom.floor;
			objectQueue.add(obj);
		});
		e.reference = new Object("[you]", e, null);
		e.reference.abstractNoun();
		e.interaction = (Player p, Engine eng) -> {
			HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();
			h.put("yes", (Player p1) -> {
				HashMap<String, OneParamFunc<Player>> h1 = new HashMap<String, OneParamFunc<Player>>();
				h1.put("yes", (Player p2) -> {
					e.attack(p2);
				});
				h1.put("no", (Player p2) -> {
					if (p2.agility + rand.nextInt(3) - 1 >= e.agility) {
						Terminal.println("You dodged the attack.");
					} else {
						Terminal.println("You failed to dodge his attack.");
						e.attack(p2);
					}
				});
				e.Dialogue("The old man tries to kill you. Let him?", h1, p1);
			});
			h.put("no", (Player p1) -> {
				Terminal.println("You walk away, leaving him slightly confused and annoyed.");
				e.anger += 20;
			});
			e.Dialogue("The old man says hi. Greet him?", h, p);
		};
		o = new Object("water [bottle]", (String) null, null);
		o.consumability = -5;
		o.drinkability = 5;
		o.injury = Object.type.crumples;
		e.inventory.add(o);

		start.objects.add(e);
		/*
		 * Template for choices HashMap<String, OneParamFunc<Player>> h = new
		 * HashMap<String, OneParamFunc<Player>>(); h.put("option 1", (Player p1) -> {
		 * 
		 * }); h.put("option 2", (Player p1) -> {
		 * 
		 * }); e.Dialogue("statement", h, p);
		 */

		Room r = new Room(0, 1, "A Dark Stone Passageway");
		mainArea.addRoom(r);

		Object o1 = new Object("sharp chunk of [obsidian]", "in a", null);
		o1.injury = Object.type.shatters;
		o1.damage = 4;
		o1.reference = new Object("small [puddle]", o, null);
		o1.reference.holdable = null;
		o1.reference.drinkability = 5;
		o1.reference.consumability = null;
		r.objects.add(o1);

		mainArea.setEntries();// to be called after mainArea completely defined
		// alternatively, you could pick the entry points directly if you want more
		// control

		mainArea = new Room(0, 1, "Emerald Forest");
		map.addRoom(mainArea);

		r = new Room(0, 0,
				"A Small Grove\n(1000)Tall, yellow blades of grass sway in the light breeze. The clouds are a dark grey, twisting in turmoil, a storm on its way.");

		Entity e1 = new Entity("shiny metal [box]", "fixed in the", (Engine e2) -> {
			Terminal.println("You killed a lifeless chunk of metal. Anger issues?");
		});
		e1.inspection = "the box is securely attached to the ground. You'll never be able to move it. The box also seems to have a rusty speaker on its side";
		e1.reference = new Object("[ground]", e, null);
		e1.reference.abstractNoun();
		e1.quest = new Quest("Death by water",
				"An ancient metal box in the forest asks for a water bottle so that it can finally short its circuits. Eternity has driven it mad.",
				o, e1, null);
		e1.interaction = (Player p, Engine eng) -> {
			if (!e1.quest.completed) {
				HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();
				h.put("yes", (Player p1) -> {
					HashMap<String, OneParamFunc<Player>> h1 = new HashMap<String, OneParamFunc<Player>>();
					h1.put("yes", (Player p2) -> {
						e1.quest.giveTo(p);
					});
					h1.put("no", (Player p2) -> {
						Terminal.println("The old box tries to shed a tear.");
					});
					e1.Dialogue("The box asks for a water bottle so it doesn't have to suffer eternity. Accept quest?",
							h1, p1);
				});
				h.put("no", (Player p1) -> {
					Terminal.println(
							"You walk away. The box keeps its static expression, but it seems more sorrowful.");
					e1.anger += 20;
				});
				e1.Dialogue("With a soft, monotone noise, the box groans and asks you for help. Help it?", h, p);
			} else {
				Terminal.println("The box creaks out a 'thank you' before shutting back off.");
			}
		};
		r.objects.add(e1);
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
			if (r.nestedMap.size() > 0) {
				compileReferences(r);
			}
		}
	}
}
