
package engine;

import java.util.*;

import engine.things.Player;
import engine.things.Effect;
import engine.things.Object;
import engine.words.Verb;
import engine.words.Word;

import engine.Terminal;

public class Engine {
	public Player protag;

	public ArrayList<Room> rooms;// can be accessed by verbs
	private ArrayList<Word> vocabulary;
	Random rand = new Random();

	public Engine() {
		protag = new Player(0, 0, 0);
		protag.setHealth(100);

		rooms = new ArrayList<Room>();
		rooms.add(new Room(0, 0, 0));
		
		Object o = new Object("red [brick]", "on a", null);
		o.injury = Object.type.shatters;
		Object reference = new Object("nice hand-knitted [carpet]", o, null);
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = new Object("soft and tender [spider]", "on", null);
		o.injury = Object.type.squishes;
		reference = new Object("your [face]", o, null);
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = Consumable("dead [corpse]", "lying on", null, 10);
		o.injury = Object.type.bruises;
		reference = new Object("the [floor]", o, null);
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		o = new Object("old wooden [bookshelf]", "on", null);
		o.injury = Object.type.shatters;
		o.container.addAll(Arrays.asList(new Object("dusty old [book]", o, null), new Object("trigonometry [textbook]", o, null), new Object("[jar] full of candy", o, null)));
		reference = new Object("the [floor]", o, null);
		reference.consumability = null;
		reference.holdable = null;
		reference.setHealth(null);
		o.reference = reference;
		rooms.get(0).objects.add(o);
		
		ArrayList<Object> references = new ArrayList<Object>();
		for(Room r : rooms) {
			Iterator<Object> it = r.objects.iterator();
			while(it.hasNext()) {
				Object obj = it.next();
				if(obj.reference != null) {
					references.add(obj.reference);
				}
			}
		}
		for(Room r : rooms) {
			r.objects.addAll(references);
		}
		vocabulary = new ArrayList<Word>();
	}

	public void addRoom(Room r) {
		rooms.add(r);
	}

	public void addWord(Word v) {
		vocabulary.add(v);
	}

	public Object Consumable(String accessor, String descriptor, String inspection, int consumability) {
		Object o = new Object(accessor, descriptor, inspection);
		o.consumability = consumability;
		return o;
	}

	public String randOf(String[] s) {
		int x = rand.nextInt(s.length);
		return s[x].substring(0, 1).toUpperCase() + s[x].substring(1, s[x].length());
	}

	public void updatePlayerState() {
		if (protag.hunger++ > 50) {
			if (protag.hunger > 80) {
				Terminal.println(
						"Intuition tells you that if you had eaten something, perhaps you wouldn't be so hungry.");
			} else {
				Terminal.println("Intuition tells you that you might want to eat some food.");
			}
		}
		if (protag.thirst++ > 50) {
			if (protag.thirst > 80) {
				Terminal.println("Intuition tells you that your thirst cannot be quenched by drinking air.");
			} else {
				Terminal.println("Intuition tells you that you might want to drink something.");
			}
		}
		protag.health += protag.health < protag.maxHealth ? 1 : 0;
	}

	public void update() {
		Terminal.println(protag);

		String userText;
		ArrayList<String> words;

		while (true) {// repeats until valid command
			for (Room r : rooms) {
				if (r.toString().equals(protag.toString())) {
					protag.currentRoom = r;
				} else {
					System.out.println("Currently not in any room");
				}
			}
			Terminal.println(protag.health > 90 ? "You are feeling fine."
					: protag.health > 50 ? "You are feeling slightly injured."
							: protag.health > 0
									? "You think that you might have some injuries, but you've forgotten where."
									: "You feel slightly dead, but you aren't sure.");
			for (Object o : protag.currentRoom.objects) {
				if(o.health != null && o.health < o.maxHealth) {
					int p = (int)(((float)o.health/(float)o.maxHealth) * 4);
					switch (o.injury) {
						case crumples :
							o.compSub = (p == 3 ? "dented " : p == 2 ? "bent " : p == 1 ? "crumpled-up " : "crushed ")  + o.accessor;
							break;
						case shatters :
							o.compSub = (p == 3 ? "fractured " : p > 0 ? "cracked " : "shattered ") + o.accessor;
							break;
						case squishes :
							o.compSub = (p == 3 ? "bruised " : p == 2 ? "squashed " : p == 1 ? "compressed " : "trampled ") + o.accessor;
							break;
						case bruises :
							o.compSub = (p == 3 ? "bruised " : p == 2 ? "damaged " : p == 1 ? "beaten-up " : "pulverized ") + o.accessor;
							break;
					}
				}
				try {
					Object r = o.reference;
					Terminal.print(randOf(new String[] { "There is a " + o.compSub + " " + o.description + " " + r.compSub,
						o.description + " " + r.compSub + ", there is a " + o.compSub,
						"You notice a " + o.compSub + " " + o.description + " " + r.compSub }));
					Terminal.print(". ");
				} catch(NullPointerException e) {
					
				}
				
			}
			Terminal.print("\n");

			userText = Terminal.readln();
			userText = userText.toLowerCase();
			words = new ArrayList<String>();

			String[] s = userText.split(" ");
			for (String str : s) {
				words.add(str);// user text goes to array of words
			}
			if (words.size() != 2) {
				Terminal.println("All commands must be 2 words");
				continue;
			}

			Word w0 = null;
			Word w1 = null;
			Object o1 = null;
			boolean found = false;
			boolean foundObject = false;

			for (Word w : vocabulary) {
				if (w.checkWord(words.get(0))) {
					w0 = w;
					found = true;
				}
			} // finds word in array

			if (!found) {
				Terminal.println("I don't know what '" + words.get(0) + "' means.");
				continue;
			}

			if (w0.getClass() != Verb.class) {
				Terminal.println("Commands always start with a verb.");
				continue;
			}

			found = false;

			for (Word w : vocabulary) {
				if (w.checkWord(words.get(1))) {
					w1 = w;
					found = true;
				}
			}

			for (Object o : protag.currentRoom.objects) {
				if (o.accessor.equals(words.get(1))) {
					o1 = o;
					foundObject = true;
				}
			}

			if (!found && !foundObject) {
				Terminal.println("I don't know what '" + words.get(1) + "' means.");
				continue;
			}

			if (found && !foundObject) {
				if (w1.getClass() == Verb.class) {
					Terminal.println("Commands never end with a verb.");
					continue;
				}
			}

			if (found) {
				w0.perform(w1, this);// fills out word's function
			} else if (foundObject) {
				w0.perform(o1, this);
			}

			updatePlayerState();
			Iterator<Effect> it = protag.effects.iterator();
			while (it.hasNext()) {
				Effect e = it.next();
				e.affect(protag);
				if (e.lifetime == 0) {
					it.remove();
				}
			}
			break;
		}
	}
}
