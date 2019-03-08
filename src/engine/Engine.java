package engine;

import java.util.*;

import engine.things.Player;
import engine.things.Effect;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Quest;
import engine.words.Verb;
import engine.words.Word;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import engine.Terminal;

public class Engine {
	public Player protag;
	public Room worldMap;
	public final String worldName = "Azaroth";// just a random name (thank Liam)

	public ArrayList<Word> vocabulary;
	private ArrayList<String> prepositions;
	private ArrayList<String> articles;
	private ArrayList<String> omitWords;
	public ArrayList<Object> objectQueue = new ArrayList<Object>();
	Random rand = new Random();

	public boolean changedRoom = true;
	public boolean changedLocation = true;
	public Room roomCache;

	public Engine() {
		protag = new Player(0, 0);
		protag.setHealth(100);

		// rooms = new ArrayList<Room>();
		worldMap = new Room(0, 0, 1, 1, "The World of " + worldName);

		protag.currentRoom = RoomGen.gen(worldMap, objectQueue);// returns starting room
		roomCache = protag.currentRoom.getClone();

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

	public static String uRandOf(String[] s) {
		Random rand = new Random();
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

	public static String lRandOf(String[] s) {
		Random rand = new Random();
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

	public static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
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
		if (protag.thirst < 20 && protag.hunger < 20) {
			protag.health += protag.health < protag.maxHealth && protag.health > 0 ? 1 : 0;
		}
	}

	public ArrayList<Object> objectCache = new ArrayList<Object>();
	public ArrayList<Object> objectsViewed = new ArrayList<Object>();
	public float[][] view = new float[1000][1000];
	int kl = 0;
	public void inspectRoom(boolean updated, Room roomCache) {
		if (protag.health <= 90) {
			Terminal.println(protag.health > 50 ? "You are feeling slightly injured."
					: protag.health > 0 ? "You think that you might have some injuries, but you've forgotten where."
							: "You feel slightly dead, but you aren't sure.");
			updatePlayerState();
		}
		if (!updated) {
			if (protag.currentRoom != null) {
				Room holder = protag.currentRoom;
				String desc = holder.description;
				while (holder.fatherRoom != null) {
					holder = holder.fatherRoom;
					desc = "(T)" + holder.description + ":(T) " + desc;
				}

				Terminal.println(desc);
			} else
				Terminal.println("Currently not in any room!");
		}

		for (float[] f : view) {
			for (int i = 0; i < f.length; i++) {
				f[i] = 0;
			}
		}
		ArrayList<Node> gp = new ArrayList<>();
		synchronized(Window.gp) {
			gp = new ArrayList<>(Window.gp.getChildren());
		}
		double pX = 0;
		double pY = 0;
		for (Node n : gp) {
			if (GridPane.getColumnIndex(n) == protag.x && GridPane.getRowIndex(n) == protag.y) {
				pX = Window.gp.localToParent(n.getBoundsInParent()).getMinX();
				pY = Window.gp.localToParent(n.getBoundsInParent()).getMinY();
			}
		}

		objectsViewed.clear();
		for (Node n : gp) {
			double deltaX = pX - Window.gp.localToParent(n.getBoundsInParent()).getMinX();
			double deltaY = pY - Window.gp.localToParent(n.getBoundsInParent()).getMinY();
			if (!(deltaX == 0 && deltaY == 0)) {
				double deltaMag = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
				if (deltaMag < 150) {
					if (Math.atan2(deltaY, deltaX) > Math.PI / 3 && Math.atan2(deltaY, deltaX) < 2 * Math.PI / 3) {
						int x = GridPane.getColumnIndex(n);
						int y = GridPane.getRowIndex(n);
						if (protag.currentRoom.area[x][y] != null
								&& !protag.currentRoom.area[x][y].accessor.equals("floor")) {
							objectsViewed.add(protag.currentRoom.area[x][y]);
							if(protag.currentRoom.area[x][y].reference != null) {
								objectsViewed.add(protag.currentRoom.area[x][y].reference);
							}
							for(Object o : protag.currentRoom.area[x][y].container) {
								objectsViewed.add(o);
							}
						}
						view[x][y] = 1;
					}
				}
			}
		}
		int x1 = 0;
		int x2 = 0;
		outerloop: for (int i = 0; i < objectsViewed.size(); i++) {
			Object o = objectsViewed.get(i);
			if (updated) {
				if (objectCache.contains(o))
					continue outerloop;
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
					s = objectsViewed.get(n).reference.accessor;
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
					s = objectsViewed.get(n).reference.accessor;
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
					Terminal.print("(500)");
					if (x1 == 1) {
						if (x2 == 1) {
							Terminal.print(lRandOf(
									new String[] { " as well as a " + compSub + " " + o.description + " " + rCompSub,
											" and a " + compSub + " " + o.description + " " + rCompSub }));
						} else {
							Terminal.print(", and a " + compSub + " " + o.description + " " + rCompSub);
						}
					} else if (x1 == 2) {
						if (x2 == 0) {
							Terminal.print(
									uRandOf(new String[] { "there is a " + compSub, "You notice a " + compSub }));
						} else {
							Terminal.print(", a " + compSub);
						}
					} else {
						if (updated) {
							Terminal.print(uRandOf(new String[] {
									"you find that there is a " + compSub + " " + o.description + " " + rCompSub,
									o.description + " " + rCompSub + " in front of you, " + "there is a " + compSub }));
						} else {
							Terminal.print(uRandOf(new String[] {
									lRandOf(new String[] { "you observe that there is a ",
											"another thing you notice is a ", "there is a ", "you notice a ",
											"you can also see a " }) + compSub + " " + o.description + " " + rCompSub,
									o.description
											+ " " + rCompSub + ", " + lRandOf(new String[] { "there is a ",
													"you can observe that there is a ", "you notice a " })
											+ compSub }));
						}
					}
					if (x1 > 0) {
						x1--;
					}
					if (x1 == 0) {
						x2 = 0;
						Terminal.println(".");
					}
				}
			} catch (NullPointerException e) {

			}
		}
		objectCache.clear();
		objectCache.addAll(objectsViewed);
		if (!updated) {
			for (Room r : protag.currentRoom.fatherRoom.nestedMap) {
				String s = "";
				if (r.coords[1] == protag.currentRoom.coords[1] + 1) {//north
					s += "south";
				}
				if (r.coords[1] == protag.currentRoom.coords[1] - 1) {//south
					s += "north";
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
				if (s != "" && Math.abs(r.coords[0] - protag.currentRoom.coords[0]) < 2
						&& Math.abs(r.coords[1] - protag.currentRoom.coords[1]) < 2) {
					try {
						Terminal.println("(500)To the " + s + ", there is "
								+ r.description.substring(0, r.description.indexOf("\n")).replace("(T)", "(B)"));
					} catch (Exception e) {
						Terminal.println("(500)To the " + s + ", there is " + r.description.replace("(T)", "(B)"));
					}
				}
			}
		}
	}

	public void runObjects() {
		objectQueue.clear();
		for (Object o : protag.currentRoom.objects) {
			if (o.health != null && o.health <= 0) {
				for (Object obj : o.container) {
					obj.reference = protag.currentRoom.floor;
					obj.description = lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";
					distribute(protag.currentRoom, o, obj);
				}
				o.container.clear();
			}
		}

		for (Object o : protag.inventory) {
			if (o.health != null && o.health <= 0) {
				for (Object obj : o.container) {
					obj.reference = protag.currentRoom.floor;
					obj.description = lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";
					distribute(protag.currentRoom, o, obj);
				}
				o.container.clear();
			}
		}

		Iterator<Object> objectIt = objectsViewed.iterator();
		while (objectIt.hasNext()) {
			Object o = objectIt.next();
			if (!o.abstractObj) {
				if (o.alive && o.health <= 0) {
					if (o.getClass().getSimpleName().equals("Entity")) {
						Entity e = (Entity) o;
						int s = objectQueue.size();
						e.death.accept(this);
						if(objectQueue.size() != s)
							objectQueue.get(s).x = e.x;
							objectQueue.get(s).y = e.y;
							protag.currentRoom.area[e.x][e.y] = objectQueue.get(s);
						for (Object obj : e.inventory) {
							if (objectQueue.size() != s) {
								objectQueue.get(s).container.addAll(e.inventory);
							} else {
								obj.reference = protag.currentRoom.floor;
								obj.description = "on";
								distribute(protag.currentRoom, o, obj);
								objectQueue.add(obj);
							}
						}
						Main.removal(o, this);
						objectIt.remove();
					}
				} else if (!o.alive && o.health <= 0) {
					for (Object obj : o.container) {
						distribute(protag.currentRoom, o, obj);
						objectQueue.add(obj);
					}
					o.container.clear();
				}
			}
		}

		protag.currentRoom.objects.addAll(objectQueue);
	}
	
	public void distribute(Room r, Object o, Object obj) {
		int area = 1;
		int i = 0;
		while (i++ < 1000) {
			obj.x = o.x + rand.nextInt(2 + area) - area;
			obj.y = o.y + rand.nextInt(2 + area) - area;
			if (r.area[obj.x][obj.y] == r.floor) {
				r.area[obj.x][obj.y] = obj;
				break;
			} else if (i == 500 || i == 750) {
				area++;
			}
		}
	}
	
	public void update() {
		String userText;
		runObjects();

		for (Quest q : protag.quests) {
			q.run(this, true);
		}
		if (protag.currentRoom != null) {
			if (changedRoom) {
				inspectRoom(false, roomCache);
			} else {
				inspectRoom(true, roomCache);
			}
			roomCache = protag.currentRoom.getClone();
		} else
			Terminal.println("Currently not in any room!");
		for (Object o : objectsViewed) {
			try {
				Entity e = (Entity) o;
				e.interactable = e.check(protag, this);
			} catch (Exception e) {
			}
		}
		Platform.runLater(() -> Window.gp.getChildren().clear());
		for (int x = 0; x < protag.currentRoom.area.length; x++) {
			for (int y = 0; y < protag.currentRoom.area[x].length; y++) {
				Label l = new Label(protag.x == x && protag.y == y ? "@"
						: protag.currentRoom.area[x][y] != null ? String.valueOf(protag.currentRoom.area[x][y].label)
								: "~");
				l.setFont(Terminal.mapFont);
				l.setTextFill(Color.gray(0.8));
				try {
					if (view[x][y] == 1) {
						l.setTextFill(Color.gray(0.5));
					}
				} catch (Exception e) {
				}
				l.setRotate(-Window.rotation);
				l.setOpacity(Window.gpTP[x][y]);
				final int x1 = x;
				final int y1 = y;
				Platform.runLater(() -> Window.gp.add(l, x1, y1));
			}
		}

		outerloop: while (true) {// repeats until valid command
			Terminal.print("(250)");
			changedRoom = false;
			changedLocation = false;
			userText = Terminal.readln();
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
										+ parts[i]
												.substring(parts[i].substring(w).indexOf(' ') == -1 ? parts[i].length()
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
					Terminal.println(uRandOf(new String[] {
							"Why don't you help both of us out by typing something I can understand?",
							"I'm sorry that I'm not smart enough to understand your big, complicated (I)sentences(I).",
							"I'm going to ignore that." }));
					continue;
				}

				if (words.size() == 1) {
					Terminal.println("Please be more specific.");
					continue;
				}
			}
			/*
			 * for (String str : s) { if (!str.isEmpty()) { words.add(str);// user text goes
			 * to array of words } }
			 */

			/*
			 * if (words.size() != 2) { Terminal.println("All commands must be 2 words.");
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
					try {
						if (w.represents == null) {
							throw new NullPointerException();
						}
						o1 = (Object) w.represents;
						foundObject = true;
					} catch (Exception e) {
						w1 = w;
						found = true;
					}
				}
			}

			for (Object o : objectsViewed) {
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
				String str = (" " + protag.currentRoom.description.replace(".", " ").replace(",", " ").replace(";", " ")
						.replace(":", " ") + " ").toLowerCase();
				if (str.contains(" " + words.get(1) + " ")) {

					Terminal.println("No.");
					continue;
				}

				for (Object o : objectsViewed) {
					try {
						if (o.reference.abstractObj && o.reference.accessor.equalsIgnoreCase(words.get(1))) {
							Terminal.println("No.");
							continue outerloop;
						}
					} catch (Exception e) {
					}
				}

				Terminal.println("I can't see a " + words.get(1) + " anywhere here.");
				continue;
			}

			if (found && !foundObject) {
				if (w1.getClass() == Verb.class) {
					Terminal.println("Commands never end with a verb.");
					continue;
				}
			}

			if (found) {
				try {
					w0.perform(w1, prepUsed[0], this);// fills out word's function
				} catch (Exception exc) {
					Terminal.println(uRandOf(new String[] { "I'm not sure what you want me to do.", "Who, me?",
							"Try again.", "This isn't going to work out." }));
					continue;
				}
			} else if (foundObject) {
				if (o2 == null) {
					w0.perform(o1, prepUsed[0], this);
				} else {
					w0.perform(o1, o2, prepUsed[0], prepUsed[1], joinerWord, this);
				}
			}
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
	}
}
