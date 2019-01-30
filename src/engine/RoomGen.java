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
				"(B)A Dark Cavern(B)\n(1000)The ceiling is too high for you to make out in the darkness. Cold, rough stone lies under your feet. A light wind passes over you.");
		mainArea.addRoom(start);

		Object o = new Object("red [brick]", "on a",
				"This brick is old and dusty. For no apparent reason, you feel compelled to consume it");
		o.injury = Object.type.shatters;
		o.reference = new Object("nice hand-knitted [carpet]", o, null);
		o.reference.injury = Object.type.squishes;
		o.damage = 3;
		start.objects.add(o);

		/*o = new Object("deformed [spider]", "on your", null);
		o.injury = Object.type.squishes;
		o.damage = 2;
		reference = new Object("[face]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		start.objects.add(o);
		
		o = Engine.Consumable("dead [corpse]", "lying on", null, 10);
		o.injury = Object.type.bruises;
		o.holdable = null;
		reference = map.nestedMap.get(0).floor;
		o.reference = reference;
		start.objects.add(o);*/

		o = new Object("old wooden [bookshelf]", "in", null);
		o.injury = Object.type.shatters;
		o.holdable = null;

		Object o2 = new Object("dusty old [book]", o, null);
		o2.text = map.description + "\nBy Anonymous";
		Object o3 = new Object("[jar]", o, null);
		Object o4 = new Object("piece of [candy]", "in the", null);
		o4.consumability = 5;
		o3.container.add(o4);
		o.container.addAll(Arrays.asList(o2, o3));
		Object reference = new Object("the back of the [room]", o, null);
		reference.abstractNoun();
		o.reference = reference;
		start.objects.add(o);
		if (true) {
			Entity e = new Entity("old [man]", "standing in front of", (Engine e2, Player p) -> {
				Terminal.sPrintln("The old man dies. He leaves you a corpse as a parting gift.", p.id);
				Object obj = Engine.Consumable("dead [corpse]", "lying on", null, 10);
				obj.injury = Object.type.bruises;
				obj.holdable = null;
				obj.reference = e2.protags.get(0).currentRoom.floor;
				objectQueue.add(obj);
			});
			e.reference = new Object("[you]", e, null);
			e.reference.abstractNoun();
			e.interaction = (Player p, Engine eng) -> {
				HashMap<String, OneParamFunc<Player>> basicActions = new HashMap<String, OneParamFunc<Player>>();
				basicActions.put("insult him", (Player p1) -> {
					Terminal.sPrintln("He does not respond to your insult.", p1.id);
				});
				basicActions.put("leave", (Player p1) -> {
					Terminal.sPrintln(
							"You start walking away.(1000)\n\nStop, the man says in a clear voice.(1000) He says something about the plot before lapsing into his insane mumblings.",
							p1.id);
				});
				HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();
				h.put("speak louder", (Player p1) -> {
					HashMap<String, OneParamFunc<Player>> h1 = new HashMap<String, OneParamFunc<Player>>();
					h1.put("repeat question", (Player p2) -> {
						Terminal.sPrintln("You ask again.(2000)\n\nThere is silence. Nothing more.", p2.id);
						e.Dialogue("He does not respond to your presence.", h1, p1);
					});
					h1.put("kick him", (Player p2) -> {
						e.anger = 20;
						Terminal.sPrintln(
								"His mouth curls into a feral smile, his head tilting sideways. The gaping hole of a mouth he has reveals his lack of teeth.(1000)\n\nYou suppose he's hungry.",
								p2.id);
					});
					basicActions.forEach(h1::putIfAbsent);
					e.Dialogue(
							"His head snaps up at you, and you now can see that his eyes are gouged out. You step backwards instinctively.",
							h1, p1);
				});
				h.put("slap him", (Player p1) -> {
					e.anger++;
					h.get("speak louder").accept(p1);
				});

				basicActions.forEach(h::putIfAbsent);
				e.Dialogue("You ask the man why he is here. He does not respond.", h, p);
				e.talkedTo = true;
			};
			e.repeatInteraction = (Player p, Engine eng) -> {
				HashMap<String, OneParamFunc<Player>> basicActions = new HashMap<String, OneParamFunc<Player>>();
				basicActions.put("insult him", (Player p1) -> {
					Terminal.sPrintln("He does not respond to your insult.", p1.id);
				});
				basicActions.put("leave", (Player p1) -> {
					Terminal.sPrintln("You walk away, your presence unnoticed.", p1.id);
				});
				HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();

				basicActions.forEach(h::putIfAbsent);
				e.Dialogue(
						eng.uRandOf(new String[] { "The man shouts something incoherent before lapsing into silence.",
								"He absent-mindedly chews on his hand.",
								"He mutters something about apples before dissolving into giggles." }),
						h, p);
			};
			o = new Object("water [bottle]", (String) null, null);
			o.consumability = -5;
			o.drinkability = 5;
			o.injury = Object.type.crumples;
			e.inventory.add(o);

			start.objects.add(e);
		}
		/*
		 * Template for choices HashMap<String, OneParamFunc<Player>> h = new
		 * HashMap<String, OneParamFunc<Player>>(); h.put("option 1", (Player p1) -> {
		 * 
		 * }); h.put("option 2", (Player p1) -> {
		 * 
		 * }); e.Dialogue("statement", h, p);
		 */

		Room r = new Room(0, 1, "(B)A Dark Stone Passageway(B)\n(1000)Very dark, very stone.");
		mainArea.addRoom(r);

		o = new Object("sharp chunk of [obsidian]", "in a", null);
		o.injury = Object.type.shatters;
		o.damage = 4;
		Object ref = new Object("small [puddle]", o, null);
		ref.holdable = null;
		ref.drinkability = 5;
		ref.consumability = null;
		o.reference = ref;
		r.objects.add(o);

		mainArea.setEntries();// to be called after mainArea completely defined
		// alternatively, you could pick the entry points directly if you want more
		// control

		mainArea = new Room(0, 1, "Emerald Forest");
		map.addRoom(mainArea);

		r = new Room(0, 0,
				"(B)A Small Grove(B)\n(1000)Tall, yellow blades of grass sway in the light breeze. The clouds are a dark grey, twisting in turmoil, a storm on its way.");
		if (true) {
			Entity e = new Entity("shiny metal [box]", "fixed in the", (Engine e2, Player p) -> {
				Terminal.sPrintln("You killed a lifeless chunk of metal. Anger issues?", p.id);
			});
			e.inspection = "the box is securely attached to the ground. You'll never be able to move it. The box also seems to have a rusty speaker on its side";
			e.reference = new Object("[ground]", e, null);
			e.reference.abstractNoun();
			e.quest = new Quest("Death by water",
					"An ancient metal box in the forest asks for a water bottle so that it can finally short its circuits. Eternity has driven it mad.",
					o, e, null);
			e.interaction = (Player p, Engine eng) -> {
				HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();
				h.put("yes", (Player p1) -> {
					HashMap<String, OneParamFunc<Player>> h1 = new HashMap<String, OneParamFunc<Player>>();
					h1.put("yes", (Player p2) -> {
						e.quest.giveTo(p);
					});
					h1.put("no", (Player p2) -> {
						Terminal.sPrintln("The old box tries to shed a tear.", p2.id);
					});
					e.Dialogue("The box asks for a water bottle so it doesn't have to suffer eternity. Accept quest?",
							h1, p1);
				});
				h.put("no", (Player p1) -> {
					Terminal.sPrintln(
							"You walk away. The box keeps its static expression, but it seems more sorrowful.", p.id);
					e.anger += 20;
				});
				e.Dialogue("With a soft, monotone noise, the box groans and asks you for help. Help it?", h, p);
				e.talkedTo = true;
			};
			e.repeatInteraction = (Player p, Engine eng) -> {
				if (!e.quest.completed) {
					HashMap<String, OneParamFunc<Player>> h = new HashMap<String, OneParamFunc<Player>>();
					h.put("yes", (Player p1) -> {
						if (e.quest.found) {
							e.quest.gaveObj(eng, e, e.quest.target, p);
						} else {

							Terminal.sPrintln(
									"You say yes.(1000)\nWhen it asks where it is, your lie becomes apparent and you walk away, hopefully ashamed.",
									p.id);
						}
					});
					h.put("no", (Player p1) -> {
						Terminal.sPrintln(
								"You tell it no, and it asks you why you came here if you didn't have it. It was a rhetorical question, so you walk away.",
								p.id);
					});
					e.Dialogue(
							"With a soft, monotone noise, the box groans and asks you if you if you have the item it requested.",
							h, p);
				} else {
					e.death = (Engine t, Player p1) -> {
						Terminal.sPrintln("The box creaks out a 'thank you' before shutting back off.", p1.id);
						Object obj = new Object(e.compSub, e.description, null);
						obj.holdable = false;
						obj.consumability = null;
						t.objectQueue.add(obj);
					};
					e.health = 0;
				}
			};
			r.objects.add(e);
		}
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
				allContainers(obj, references);
			}
			r.objects.addAll(references);
			if (r.nestedMap.size() > 0) {
				compileReferences(r);
			}
		}
	}

	public static void allContainers(Object o, ArrayList<Object> references) {
		if (!o.container.isEmpty()) {
			references.addAll(o.container);
			for (Object obj : o.container) {
				allContainers(obj, references);
			}
		}
	}
}
