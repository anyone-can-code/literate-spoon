package engine;

import java.io.IOException;
import java.util.*;

import engine.things.Player;
import engine.things.Effect;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Quest;
import engine.words.Verb;
import engine.words.Word;

import engine.Terminal;

public class Engine {

	public ArrayList<Player> protags = new ArrayList<Player>();

	// public ArrayList<Room> rooms;// can be accessed by verbs
	public Room worldMap;
	public final String worldName = "Azaroth";// just a random name (thank Liam)

	private ArrayList<Word> vocabulary;
	private ArrayList<String> prepositions;
	private ArrayList<String> articles;
	private ArrayList<String> omitWords;
	public ArrayList<Object> objectQueue = new ArrayList<Object>();
	Random rand = new Random();
	public Room startingRoom;

	public Engine() {
		// rooms = new ArrayList<Room>();
		worldMap = new Room(0, 0, "The World of " + worldName);
		startingRoom = RoomGen.gen(worldMap, objectQueue);
		vocabulary = new ArrayList<Word>();
		prepositions = new ArrayList<String>(Arrays.asList(new String[] { "aboard", "about", "above", "across", "after",
				"against", "along", "amid", "among", "around", "as", "at", "before", "behind", "below", "beside",
				"between", "by", "down", "in", "inside", "into", "my", "near", "on", "through", "to", "toward",
				"towards", "under", "with", "your" }));
		articles = new ArrayList<String>(Arrays.asList(new String[] { "a", "an", "the" }));
		omitWords = new ArrayList<String>(Arrays.asList(new String[] { "up", "down" }));

	}

	public void addWord(Word v) {
		vocabulary.add(v);
	}

	public static Object Consumable(String accessor, String descriptor, String inspection, int consumability) {
		Object o = new Object(accessor, descriptor, inspection);
		o.consumability = consumability;
		return o;
	}

	public String uRandOf(String[] s) {
		int x = rand.nextInt(s.length);

		// Convert 'a's to 'an's
		for (int i = 2; i < s[x].length(); i++) {
			ArrayList<Character> vowels = new ArrayList<Character>(Arrays.asList('a', 'e', 'i', 'o', 'u'));
			if (vowels.contains(s[x].charAt(i)) && s[x].charAt(i - 2) == 'a' && s[x].charAt(i - 1) == ' '
					&& s[x].charAt(i - 3) == ' ') {
				s[x] = s[x].substring(0, i - 2) + "an" + s[x].substring(i - 1, s[x].length());
			}
		}
		return s[x].substring(0, 1).toUpperCase() + s[x].substring(1, s[x].length());
	}

	public String lRandOf(String[] s) {
		int x = rand.nextInt(s.length);
		// Convert 'a's to 'an's
		for (int i = 2; i < s[x].length(); i++) {
			ArrayList<Character> vowels = new ArrayList<Character>(Arrays.asList('a', 'e', 'i', 'o', 'u'));
			if (vowels.contains(s[x].charAt(i)) && s[x].charAt(i - 2) == 'a' && s[x].charAt(i - 1) == ' '
					&& s[x].charAt(i - 3) == ' ') {
				s[x] = s[x].substring(0, i - 2) + "an" + s[x].substring(i - 1, s[x].length());
			}
		}
		return s[x].toLowerCase();
	}

	public void updatePlayerState(Player protag) {
		if (protag.hunger++ > 50) {
			if (protag.hunger > 80) {
				Terminal.sPrintln(
						"Intuition tells you that if you had eaten something, perhaps you wouldn't be so hungry.",
						protag.id);
			} else {
				Terminal.sPrintln("Intuition tells you that you might want to eat some food.", protag.id);
			}
		}
		if (protag.thirst++ > 50) {
			if (protag.thirst > 80) {
				Terminal.sPrintln("Intuition tells you that your thirst cannot be quenched by drinking air.",
						protag.id);
			} else {
				Terminal.sPrintln("Intuition tells you that you might want to drink something.", protag.id);
			}
		}
		if (protag.thirst < 20 && protag.hunger < 20 && protag.health != 0) {
			protag.health += protag.health < protag.maxHealth && protag.health > 0 ? 1 : 0;
		}
	}

	public void inspectRoom(boolean updated, Room roomCache, Player protag) {
		if (!updated) {
			if (protag.currentRoom != null) {
				Room holder = protag.currentRoom;
				String desc = holder.description;
				while (holder.fatherRoom != null) {
					holder = holder.fatherRoom;
					desc = "(B)" + holder.description + ":(B) " + desc;
				}

				Terminal.sPrintln(desc, protag.id);
			} else
				Terminal.sPrintln("Currently not in any room!", protag.id);
		}

		int x1 = 0;
		int x2 = 0;

		outerloop: for (int i = 0; i < protag.currentRoom.objects.size(); i++) {
			Object o = protag.currentRoom.objects.get(i);
			if (updated) {
				for (Object obj : roomCache.objects) {
					if (o.accessor.equals(obj.accessor)) {
						continue outerloop;
					}
				}
			}
			String compSub = o.compSub;
			if (o.health != null && o.health < o.maxHealth) {
				int p = (int) (((float) o.health / (float) o.maxHealth) * 4);
				switch (o.injury) {
				case crumples:
					compSub = (p == 3 ? "dented " : p == 2 ? "bent " : p == 1 ? "crumpled-up " : "crushed ")
							+ o.compSub;
					break;
				case shatters:
					compSub = (p == 3 ? "fractured " : p > 0 ? "cracked " : "shattered ") + o.compSub;
					break;
				case squishes:
					compSub = (p == 3 ? "bruised " : p == 2 ? "squashed " : p == 1 ? "compressed " : "trampled ")
							+ o.compSub;
					break;
				case bruises:
					compSub = (p == 3 ? "bruised " : p == 2 ? "damaged " : p == 1 ? "beaten-up " : "pulverized ")
							+ o.compSub;
					break;
				}
			}
			String rCompSub = "";
			if (o.reference != null) {
				rCompSub = o.reference.compSub;
				if (o.reference.health != null && o.reference.health < o.reference.maxHealth
						&& o.reference.injury != null) {
					int p = (int) (((float) o.reference.health / (float) o.reference.maxHealth) * 4);
					switch (o.reference.injury) {
					case crumples:
						rCompSub = (p == 3 ? "dented " : p == 2 ? "bent " : p == 1 ? "crumpled-up " : "crushed ")
								+ o.reference.compSub;
						break;
					case shatters:
						rCompSub = (p == 3 ? "fractured " : p > 0 ? "cracked " : "shattered ") + o.reference.compSub;
						break;
					case squishes:
						rCompSub = (p == 3 ? "bruised " : p == 2 ? "squashed " : p == 1 ? "compressed " : "trampled ")
								+ o.reference.compSub;
						break;
					case bruises:
						rCompSub = (p == 3 ? "bruised " : p == 2 ? "damaged " : p == 1 ? "beaten-up " : "pulverized ")
								+ o.reference.compSub;
						break;
					}
				}
			}

			if (protag.hunger > 0) {
				if (rand.nextInt(101 - protag.hunger) < 5 && o.reference != null) {
					compSub = lRandOf(new String[] { "possibly edible", "juicy and tender", "appetizing",
							"delicious-looking", "scrumptious" }) + " " + compSub;
				}
			}

			int n = i + 1;
			String s = null;
			while (s == null) {
				try {
					s = protag.currentRoom.objects.get(n).reference.accessor;
					if (s.equals(o.reference.accessor)) {
						x1 = 2;
					}
					break;
				} catch (NullPointerException e) {

				} catch (IndexOutOfBoundsException e) {
					break;
				}
				n++;
			}
			n = i - 1;
			s = null;
			while (s == null) {
				try {
					s = protag.currentRoom.objects.get(n).reference.accessor;
					if (s.equals(o.reference.accessor)) {
						if (x2 == 1) {
							x2 = 0;
						} else {
							x2 = 1;
						}
					}
				} catch (NullPointerException e) {

				} catch (IndexOutOfBoundsException e) {
					break;
				}
				n--;
			}

			try {
				Object r = o.reference;
				if (r != null) {
					Terminal.sPrint("(1000)", protag.id);
					if (x1 == 1) {
						if (x2 == 1) {
							Terminal.sPrint(lRandOf(
									new String[] { " as well as a " + compSub + " " + o.description + " " + rCompSub,
											" and a " + compSub + " " + o.description + " " + rCompSub }),
									protag.id);
						} else {
							Terminal.sPrint(", and a " + compSub + " " + o.description + " " + rCompSub, protag.id);
						}
					} else if (x1 == 2) {
						if (x2 == 0) {
							Terminal.sPrint(
									uRandOf(new String[] { "there is a " + compSub, "You notice a " + compSub }),
									protag.id);
						} else {
							Terminal.sPrint(", a " + compSub, protag.id);
						}
					} else {
						if (updated) {
							Terminal.sPrint(
									uRandOf(new String[] {
											"there now is a " + compSub + " " + o.description + " " + rCompSub,
											o.description + " " + rCompSub + ", " + "there now is a " + compSub }),
									protag.id);
						} else {
							Terminal.sPrint(uRandOf(new String[] {
									lRandOf(new String[] { "you observe that there is a ",
											"another thing you notice is a ", "there is a ", "you notice a ",
											"you can also see a " }) + compSub + " " + o.description + " " + rCompSub,
									o.description + " " + rCompSub + ", " + lRandOf(
											new String[] { "there is a ", "you can observe a ", "you notice a " })
											+ compSub }),
									protag.id);
						}
					}
					if (x1 > 0) {
						x1--;
					}
					if (x1 == 0) {
						x2 = 0;
						Terminal.sPrintln(".", protag.id);
					}
				}
			} catch (NullPointerException e) {

			}
		}
		if (!updated) {
			for (Room r : protag.currentRoom.fatherRoom.nestedMap) {
				String s = "";
				if (r.coords[1] == protag.currentRoom.coords[1] + 1) {//north
					s += "north";
				}
				if (r.coords[1] == protag.currentRoom.coords[1] - 1) {//south
					s += "south";
				}
				if (r.coords[0] == protag.currentRoom.coords[0] + 1) {//right
					if (s != "")
						s += "-";
					s += "east";
				}
				if (r.coords[0] == protag.currentRoom.coords[0] - 1) {//left
					if (s != "")
						s += "-";
					s += "west";
				}
				if (s != "") {
					Terminal.sPrintln(
							"To the " + s + ", there is a " + r.description.substring(0, r.description.indexOf("\n")),
							protag.id);
				}
			}
		}
	}

	public void runObjects(Player protag) {
		objectQueue.clear();
		for (Object o : protag.currentRoom.objects) {
			if (o.health != null && o.health <= 0) {
				for (Object obj : o.container) {
					obj.reference = protag.currentRoom.floor;
					obj.description = lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";

				}
				o.container.clear();
			}
		}

		for (Object o : protag.inventory) {
			if (o.health != null && o.health <= 0) {
				for (Object obj : o.container) {
					obj.reference = protag.currentRoom.floor;
					obj.description = lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";

				}
				o.container.clear();
			}
		}

		Iterator<Object> objectIt = protag.currentRoom.objects.iterator();
		while (objectIt.hasNext()) {
			Object o = objectIt.next();
			if (!o.abstractObj) {
				if (o.alive && o.health <= 0) {
					if (o.getClass().getSimpleName().equals("Entity")) {
						Entity e = (Entity) o;
						int s = objectQueue.size();
						e.death.accept(this, e.killer);
						for (Object obj : e.inventory) {
							if (objectQueue.size() != s) {
								objectQueue.get(s).container.addAll(e.inventory);
							} else {
								obj.reference = protag.currentRoom.floor;
								obj.description = "on";
								objectQueue.add(obj);
							}
						}
						objectIt.remove();
					}
				} else if (!o.alive && o.health <= 0) {
					for (Object obj : o.container) {
						objectQueue.add(obj);
					}
					o.container.clear();
				}
			}
		}
		if (protag.health <= 0) {
				int s = objectQueue.size();
				protag.death.accept(this, objectQueue);
				for (Object obj : protag.inventory) {
					if (objectQueue.size() != s) {
						objectQueue.get(s).container.addAll(protag.inventory);
					} else {
						obj.reference = protag.currentRoom.floor;
						obj.description = "on";
						objectQueue.add(obj);
					}
				}
				protag.currentRoom.objects.remove(protag);
		}
		protag.currentRoom.objects.addAll(objectQueue);
	}

	public void update(Player protag) {
		String userText;

		for (Quest q : protag.quests) {
			q.run(this, true, protag);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if(protag.health <= 90) {
		Terminal.sPrintln("", protag.id);
		}
		runObjects(protag);
		
		Terminal.sPrintln(protag.health > 90 ? ""
				: protag.health > 50 ? "You are feeling slightly injured."
						: protag.health > 0 ? "You think that you might have some injuries, but you've forgotten where."
								: "You have met your inevitable death a bit earlier than most.",
				protag.id);
		
		if (protag.health <= 0) {
			protags.set(protag.id, null);
			return;
		}
		try {
			outerloop: while (true) {// repeats until valid command

				Terminal.sPrint("(1000)", protag.id);
				if (protag.currentRoom != null) {
					if (protag.changedSurroundings) {
						inspectRoom(false, protag.roomCache, protag);
						try {
							for (int i = 0; i < Server.out.length; i++) {
								if (protags.get(i).id != protag.id
										&& protags.get(i).currentRoom == protag.currentRoom) {
									Terminal.sPrintln("Player " + protag.id + " entered the room.", protags.get(i).id);
								} else if (protags.get(i).id != protag.id
										&& protags.get(i).currentRoom.coords == protag.roomCache.coords) {
									Terminal.sPrintln("Player " + protag.id + " left the room.", protags.get(i).id);
								}
							}
						} catch (NullPointerException e) {
						}
					} else {
						inspectRoom(true, protag.roomCache, protag);
					}
				} else
					Terminal.sPrintln("Currently not in any room!", protag.id);
				for (Object o : protag.currentRoom.objects) {
					try {
						Entity e = (Entity) o;
						e.interactable = e.check(protag, this);
					} catch (Exception e) {
					}
				}
				protag.changedSurroundings = false;

				while (!Server.in[protag.id].ready()) {
					if (protag.health <= 0) {
						break outerloop;
					}
				}
				userText = Server.in[protag.id].readLine();
				
				if (userText.toLowerCase().contains("say")) {
					Terminal.broadcast(new String[] { "You say", "He says" }, userText.replaceAll("(?i)say", "") + ".",
							protag.id);
					break;
				}

				userText = userText.toLowerCase();

				for (String str : omitWords) {
					userText = userText.replace(" " + str + " ", " ");
				}

				for (String str : articles) {
					userText = userText.replace(" " + str + " ", " ");
				}
				for (Object o : protag.currentRoom.objects) {
					if (userText.contains(o.compSub)) {
						userText = userText.replace(o.compSub, o.accessor);
					}
					for (Object obj : o.container) {
						if (userText.contains(obj.compSub)) {
							userText = userText.replace(obj.compSub, obj.accessor);
						}
					}
				}

				for (Object o : protag.inventory) {
					if (userText.contains(o.compSub)) {
						userText = userText.replace(o.compSub, o.accessor);
					}
					for (Object obj : o.container) {
						if (userText.contains(obj.compSub)) {
							userText = userText.replace(obj.compSub, obj.accessor);
						}
					}
				}
				try {
					if (userText.contains(protag.rightHand.compSub)) {
						userText = userText.replace(protag.rightHand.compSub, protag.rightHand.accessor);
					}
				} catch (NullPointerException e) {
				}
				userText = userText.replace(".", "");
				while (userText.contains("  ")) {
					userText = userText.replace("  ", " ");
				}
				String temp = userText;
				String[] parts;
				String joinerWord = "";
				ArrayList<String> words = new ArrayList<String>();
				if ((" " + userText + " ").contains("with")) {
					parts = (" " + userText + " ").split(" with ");
					joinerWord = "with";
				} else if ((" " + userText + " ").contains("to")) {
					parts = (" " + userText + " ").split(" to ");
					joinerWord = "to";
				} else {
					parts = new String[1];
					parts[0] = userText;
				}

				String[] prepUsed = new String[parts.length];
				try {
					for (int i = 0; i < parts.length; i++) {
						prepUsed[i] = "";
						parts[i] = parts[i].trim();
						int w = 0;
						while (true) {
							for (String str : prepositions) {
								if (parts[i].substring(w, parts[i].substring(w).indexOf(' ') == -1 ? parts[i].length()
										: parts[i].substring(w).indexOf(' ') + w).equals(str)) {
									prepUsed[i] += " " + str;
									parts[i] = parts[i].substring(0, w).trim() + " "
											+ parts[i].substring(
													parts[i].substring(w).indexOf(' ') == -1 ? parts[i].length()
															: parts[i].substring(w).indexOf(' ') + w)
													.trim();
									break;
								}
							}

							if (parts[i].substring(w).indexOf(' ') == -1) {
								break;
							}

							w = parts[i].substring(w).indexOf(' ') + w + 1;
						}
					}

					// words = new ArrayList<String>();

					words.addAll(Arrays.asList(parts[0].split(" ")));
					if (words.size() > 2) {
						throw new Exception();
					}

					for (int i = 1; i < parts.length; i++)
						words.addAll(Arrays.asList(parts[i].trim().split(" ")));

					if (words.size() == 1) {
						throw new Exception();
					}
				} catch (Exception e) {
					words.clear();
					words.addAll(Arrays.asList(temp.split(" ")));
					if (words.size() > 2) {

						Terminal.sPrintln(uRandOf(new String[] {
								"Why don't you help both of us out by typing something I can understand?",
								"I'm sorry that I'm not smart enough to understand your big, complicated (I)sentences(I).",
								"I'm going to ignore that." }), protag.id);
						continue;
					}

					if (words.size() == 1) {
						Terminal.sPrintln("Please be more specific.", protag.id);
						continue;
					}
				}
				/*
				 * for (String str : s) { if (!str.isEmpty()) { words.add(str);// user text goes
				 * to array of words } }
				 */

				/*
				 * if (words.size() != 2) { Terminal.sPrintln("All commands must be 2 words.");
				 * continue; }
				 */
				Word w0 = null;
				Word w1 = null;
				Object o1 = null;
				Object o2 = null;
				boolean found = false;
				boolean foundObject = false;

				for (Word w : vocabulary) {
					if (w.checkWord(words.get(0))) {
						w0 = w;
						found = true;
					}
				} // finds word in array

				if (!found) {
					Terminal.sPrintln("I don't know what '" + words.get(0) + "' means.", protag.id);
					continue;
				}

				if (w0.getClass() != Verb.class) {
					Terminal.sPrintln("Commands always start with a verb.", protag.id);
					continue;
				}

				found = false;

				for (Word w : vocabulary) {
					if (w.checkWord(words.get(1))) {
						try {
							if (w.represents == null) {
								throw new NullPointerException();
							}
							o1 = (Object)((OneParamFuncReturn<Player>) w.represents).accept(protag);
							if(o1 == null) {
								throw new Exception();
							}
							foundObject = true;
						} catch (Exception e) {
							w1 = w;
							found = true;
						}
					}
				}

				for (Object o : protag.currentRoom.objects) {
					if (o != protag) {
						if (o.accessor.equals(words.get(1))) {
							o1 = o;
							foundObject = true;
						}
						if (words.size() > 2 && o.accessor.equals(words.get(2))) {
							o2 = o;
						}
						for (Object obj : o.container) {
							if (obj.accessor.equals(words.get(1))) {
								o1 = obj;
								foundObject = true;
							}
						}
					}
				}

				for (Object o : protag.inventory) {
					if (o.accessor.equals(words.get(1))) {
						o1 = o;
						foundObject = true;
					}
					for (Object obj : o.container) {
						if (obj.accessor.equals(words.get(1))) {
							o1 = obj;
							foundObject = true;
						}
					}
					if (words.size() > 2 && o.accessor.equals(words.get(2))) {
						o2 = o;
					}
				}
				try {
					if (protag.rightHand.accessor.equals(words.get(1))) {
						o1 = protag.rightHand;
						foundObject = true;
					}
					if (words.size() > 2 && protag.rightHand.accessor.equals(words.get(2))) {
						o2 = protag.rightHand;
					}
				} catch (NullPointerException e) {
				}

				if (!found && !foundObject) {
					String str = (" " + protag.currentRoom.description.replace(".", " ").replace(",", " ")
							.replace(";", " ").replace(":", " ") + " ").toLowerCase();
					if (str.contains(" " + words.get(1) + " ")) {

						Terminal.sPrintln("No.", protag.id);
						continue;
					}

					for (Object o : protag.currentRoom.objects) {
						try {
							if (o.reference.abstractObj && o.reference.accessor.equalsIgnoreCase(words.get(1))) {
								Terminal.sPrintln("No.", protag.id);
								continue outerloop;
							}
						} catch (Exception e) {
						}
					}

					Terminal.sPrintln("I don't know what '" + words.get(1) + "' means.", protag.id);
					continue;
				}

				if (found && !foundObject) {
					if (w1.getClass() == Verb.class) {
						Terminal.sPrintln("Commands never end with a verb.", protag.id);
						continue;
					}
				}
				protag.roomCache = protag.currentRoom.getClone();
				if (found) {
					try {
						w0.perform(w1, prepUsed[0], this, protag);// fills out word's function
					} catch (Exception exc) {
						Terminal.sPrintln(uRandOf(new String[] { "I'm not sure what you want me to do.", "Who, me?",
								"Try again.", "This isn't going to work out." }), protag.id);
						continue;
					}
				} else if (foundObject) {
					if (o2 == null) {
						w0.perform(o1, prepUsed[0], this, protag);
					} else {
						w0.perform(o1, o2, prepUsed[0], prepUsed[1], joinerWord, this, protag);
					}
				}
				updatePlayerState(protag);
				Iterator<Effect> effectIt = protag.effects.iterator();
				while (effectIt.hasNext()) {
					Effect e = effectIt.next();
					e.affect(protag);
					if (e.lifetime == 0) {
						effectIt.remove();
					}
				}

				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
