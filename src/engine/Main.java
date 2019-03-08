package engine;

import java.util.*;

import engine.words.Direction;
import engine.words.Verb;
import engine.words.Word;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import engine.things.Effect;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;
import engine.things.Quest;
import java.util.ArrayList;
import engine.Terminal;

public class Main extends Thread {
	public static Engine game;

	public Main() {
		game = new Engine();
		Terminal.t = game;
		game.addWord(new Verb("move go walk run climb jog travel journey venture", (Word w, Engine t) -> {
			if (w.getClass() != Direction.class) {
				Terminal.println("...What?");
				return;
			}try {
			int x, y;

			int dx = Integer.parseInt(w.value.substring(0, 1)) - 1;
			int dy = Integer.parseInt(w.value.substring(1, 2)) - 1;
			ArrayList<Node> gp = new ArrayList<>();
			synchronized(Window.gp) {
				gp = new ArrayList<>(Window.gp.getChildren());
			}
			double pX = 0;
			double pY = 0;
			Node playerNode = null;
			for (Node n : gp) {
				try {
					Label l = (Label) n;
					if (l.getText().equals("@")) {
						pX = Window.gp.localToParent(n.getBoundsInParent()).getMinX() + dx * 100;
						pY = Window.gp.localToParent(n.getBoundsInParent()).getMinY() + dy * 100;
						playerNode = n;
					}
				} catch (Exception e) {
				}
			}
			double closest = 100;
			Node closestNode = null;
			for (Node n : gp) {
				if (n != playerNode && GridPane.getColumnIndex(n) > t.protag.x - 2
						&& GridPane.getColumnIndex(n) < t.protag.x + 2 && GridPane.getRowIndex(n) > t.protag.y - 2
						&& GridPane.getRowIndex(n) < t.protag.y + 2) {
					double deltaX = Window.gp.localToParent(n.getBoundsInParent()).getMinX() - pX;
					double deltaY = Window.gp.localToParent(n.getBoundsInParent()).getMinY() - pY;
					double deltaMag = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					if (deltaMag < closest) {
						closestNode = n;
						closest = deltaMag;
					}
				}
			}
			dx = GridPane.getColumnIndex(closestNode) - GridPane.getColumnIndex(playerNode);
			dy = GridPane.getRowIndex(closestNode) - GridPane.getRowIndex(playerNode);
			t.protag.x += dx;
			t.protag.y += dy;

			x = t.protag.currentRoom.coords[0];
			y = t.protag.currentRoom.coords[1];

			Room cR = t.protag.currentRoom;
			if (t.protag.x < 0 || t.protag.y < 0 || t.protag.x >= cR.area.length || t.protag.y >= cR.area[0].length) {
				t.protag.x -= dx;
				t.protag.y -= dy;

				for (Room r : t.protag.currentRoom.fatherRoom.nestedMap) {
					if (x + dx == r.coords[0] && y + dy == r.coords[1]) {
						t.protag.currentRoom = r;
						t.changedRoom = true;
						return;
					}
				}

				Terminal.println("You can't move that way.");
				return;
			} else {
				t.changedLocation = true;
				return;
			}
			} catch(Exception e) {}
		}, null, null, null));

		game.addWord(new Verb("eat consume", null, (Object o, Engine t) ->{
			if (o.abstractObj) {
				Terminal.println("Impossible.");
				return;
			}
			if (!o.alive) {
				t.protag.hunger -= o.consumability;
				if (o.drinkability != null) {
					t.protag.thirst -= o.drinkability;
				}
				if (o.consumability < 0) {
					if (o.poisonous) {
						t.protag.effects.add(new Effect((p) -> {
							t.protag.health--;
						}, 30, "That was painful to eat.", "nauseous and feverish"));
					} else {
						t.protag.effects.add(new Effect((p) -> {
							t.protag.health += o.consumability * 2;
						}, 3, "That was painful to eat.", "like you've eaten some that you shouldn't have"));
					}
				} else {
					Terminal.println("You ate the " + o.accessor + ". Delicious.");
				}
				removal(o, t);
			} else {
				throw new NullPointerException();
			}

		}, null, (Object o, Engine t)-> {
			Terminal.println("Your physical limitations prevent you from consuming the " + o.compSub + ".");
		}));

		game.addWord(new Verb("drink", null, (Object o, Engine t) -> {
			if (!o.alive) {
				t.protag.thirst -= o.drinkability;
				if (o.poisonous) {
					Terminal.println("An acrid aftertaste fills your mouth.");
				} else {
					Terminal.println("You drank the " + o.accessor + ". Delicious.");
				}
				if (o.consumability == null) {
					removal(o, t);
				} else {
					o.drinkability = null;
				}
			} else {
				throw new NullPointerException();
			}
		}, null, (Object o, Engine t)-> {
			Terminal.println("You aren't able to drink the " + o.compSub + ".");
		}));

		game.addWord(new Verb("inspect investigate examine scrutinize study observe look", (Word w, Engine t) -> {
			if (w.represents == t.protag.inventory) {
				for (Word word : t.vocabulary) {
					if (word.checkWord("check")) {
						for (Word obj : t.vocabulary) {
							if (obj.checkWord("inventory")) {
								word.perform(obj, null, t);
							}
						}
					}
				}
			} else if (w.represents == t.protag.quests) {
				for (Word word : t.vocabulary) {
					if (word.checkWord("check")) {
						for (Word obj : t.vocabulary) {
							if (obj.checkWord("quests")) {
								word.perform(obj, null, t);
							}
						}
					}
				}
			} else if (w.represents == "room") {
				t.inspectRoom(false, t.roomCache);
			}
		}, (Object o, Engine t) -> {
			if (o.container.isEmpty()) {
				Terminal.print(Engine.uRandOf(new String[] { "Upon inspection, you realize that " + o.inspection,
						"It looks like " + o.inspection, "You now can see that " + o.inspection }));
			} else {
				Terminal.print(Engine.uRandOf(
						new String[] { "Upon inspection, you observe that there is a " + o.container.get(0).compSub,
								"It looks like there is a " + o.container.get(0).compSub,
								"You now can see that there is a " + o.container.get(0).compSub }));
				if (o.container.size() == 2) {
					Terminal.print(" as well as a " + o.container.get(1).compSub);
				} else if (o.container.size() > 2) {
					for (int i = 1; i < o.container.size() - 1; i++) {
						Terminal.print(", a ");
						Terminal.print(o.container.get(i).compSub);
					}
					Terminal.print(", and a " + o.container.get(o.container.size() - 1).compSub);
				}
				Terminal.print(" inside the " + o.accessor);
			}
			Terminal.println(".");
		}, null, (Object o, Engine t)-> {
			Terminal.println("You can't inspect that.");
		}));

		game.addWord(new Verb("interact talk speak converse negotiate chat gossip", null, (Object o, Engine t) -> {
			Entity e = (Entity) o;
			if (e.talkedTo) {
				e.repeatInteraction.accept(t.protag, t);
			} else {
				e.interaction.accept(t.protag, t);
			}
		}, null, (Object o, Engine t)-> {
			Terminal.println("You greet the " + o.accessor + ", but it doesn't respond.");
		}));

		game.addWord(new Verb("attack assault assail hit pummel strike kill destroy", null, (Object o, Engine t) -> {
			if (o.equals(t.protag)) {
				t.protag.health = 0;
				Terminal.println("You killed yourself. Nice job.");
				return;
			}

			o.health -= t.protag.strength * t.protag.weapon.damage;
			t.protag.health -= t.protag.strength + t.protag.weapon.playerDamage;

			if (!t.protag.weapon.abstractObj) {
				t.protag.weapon.health -= t.protag.strength;
			}

			if (o.alive) {
				try {
					Entity e = (Entity) o;
					if (e.anger < e.restraint) {
						e.anger = e.restraint;
					}
				} catch (Exception e) {
				}
				Terminal.println(Engine.uRandOf(
						new String[] { "A cry of pain greets your ears.", "The sharp smell of blood fills the air.",
								"Something cracks.", "A surge of adrenaline shoots through you." }));
			} else {
			Terminal.println("You attacked the " + o.accessor + " with the " +
			t.protag.weapon.accessor + ".");
			}
		}, (Object o, Object with, Engine t) -> {
			if (!with.holdable) {
				Terminal.println("You cannot hold that item, and therefore cannot attack with it.");
				return;
			} else {
				if (!t.protag.inventory.contains(with)) {
					removal(with, t);
					t.protag.inventory.add(with);
				}
			}
			t.protag.weapon = with;

			o.health -= t.protag.strength + t.protag.weapon.damage;
			t.protag.health -= t.protag.strength + t.protag.weapon.playerDamage;
			if (o.equals(t.protag) && t.protag.health <= 0) {
				Terminal.println("You killed yourself. Nice job.");
				return;
			}
			if (!t.protag.weapon.abstractObj) {
				t.protag.weapon.health -= t.protag.strength;
			}

			if (o.alive) {
				try {
					Entity e = (Entity) o;
					if (e.anger < e.restraint) {
						e.anger = e.restraint;
					}
				} catch (Exception e) {
				}
				Terminal.println(Engine.uRandOf(
						new String[] { "A cry of pain greets your ears.", "The sharp smell of blood fills the air.",
								"Something cracks.", "A surge of adrenaline shoots through you." }));
			} else {
				Terminal.println("You attacked the " + o.accessor + " with the " +
						t.protag.weapon.accessor + ".");
			}

		}, "with", null, null, null));

		game.addWord(new Verb("hold equip", null, (Object o, Engine t) -> {
			boolean b = o.holdable;
			if (t.protag.inventory.contains(o)) {
				if (t.protag.rightHand != null) {
					t.protag.inventory.add(t.protag.rightHand);
				}
				t.protag.rightHand = o;
				t.protag.inventory.remove(o);
				Terminal.println("You are now holding a " + o.accessor + ".");
			} else if (t.objectsViewed.contains(o)) {
				removal(o, t);
				if (t.protag.rightHand != null) {
					t.protag.inventory.add(t.protag.rightHand);
				}
				t.protag.rightHand = o;
				Terminal.println("You are now holding a " + o.accessor + ".");
			} else {
				throw new NullPointerException();
			}
		}, null, (Object o, Engine t)-> {
			Terminal.println("You can't hold the " + o.compSub + ".");
		}));

		game.addWord(
				new Verb("take get steal grab seize apprehend liberate collect pick", null, (Object o, Engine t) -> {
					boolean b = o.holdable;
					if (o.alive) {
						throw new NullPointerException();
					}
					if (!t.protag.inventory.contains(o)) {
						removal(o, t);
						t.protag.inventory.add(o);
						Terminal.println("You took the " + o.accessor + ".");
					} else {
						throw new NullPointerException();
					}
				}, null, (Object o, Engine t)-> {
					Terminal.println("The " + o.compSub + " doesn't appear to be something you can merely 'take'.");
				}));

		game.addWord(new Verb("drop leave put", null, (Object o, Engine t) -> {
			if (o.abstractObj) {
				Terminal.println("Impossible.");
				return;
			}
			if (t.protag.rightHand.equals(o)) {
				t.protag.rightHand = t.protag.fist;
				o.description = "on";
				o.reference = t.protag.currentRoom.floor;
				addObject(o, t.protag.x, t.protag.y, t.protag.currentRoom);
				Terminal.println("You dropped the " + o.accessor + ".");
			} else if (t.protag.inventory.contains(o)) {
				t.protag.inventory.remove(o);
				o.description = "on";
				o.reference = t.protag.currentRoom.floor;
				addObject(o, t.protag.x, t.protag.y, t.protag.currentRoom);
				Terminal.println("You dropped the " + o.accessor + ".");
			} else {
				Terminal.println("You don't have a " + o.accessor + " to drop.");
			}
		}, null, null));

		game.addWord(new Verb("give gift supply donate", null, null, (Object gift, Object receiver, Engine t) -> {
			Iterator<Object> obj = t.protag.inventory.iterator();
			while (obj.hasNext()) {
				Object o = obj.next();
				if (o == gift) {
					if (receiver.getClass().toString().equals("class engine.things.Entity")) {
						Entity e = ((Entity) receiver);
						e.inventory.add(o);
						Terminal.println("The " + receiver.accessor + " gladly accepts your gift.");
						for (Quest q : t.protag.quests) {
							q.gaveObj(t, (Entity) receiver, o);
						}
						if (e.quest != null) {
							e.quest.gaveObj(t, e, o);
						}
					} else if (receiver.getClass().toString().equals("class engine.things.Object")) {
						receiver.container.add(o);
						Terminal.println("If the " + receiver.accessor + " was alive, it would surely thank you.");
					}
					obj.remove();
					break;
				}
			}

		}, "to", null, null, (Object gift, Object receiver, Engine t)-> {
			Terminal.println("You cannot give the " + gift.accessor + " to the " + receiver.accessor + ".");
		}));

		game.addWord(new Verb("view open check show", (Word n, Engine t) -> {
			if (n.represents == t.protag.inventory) {
				ArrayList<Object> realStuff = (ArrayList<Object>) t.protag.inventory.clone();
				for (int i = 0; i < realStuff.size(); i++) {
					if (realStuff.get(i).abstractObj) {
						realStuff.remove(i);
						i--;
					}
				}

				if (realStuff.isEmpty()) {
					Terminal.print("You have nothing in your inventory");
				} else {
					Terminal.print("You have a " + realStuff.get(0).compSub);
					if (realStuff.size() == 2) {
						Terminal.print(" as well as a " + realStuff.get(1).compSub);
					} else if (realStuff.size() > 2) {
						for (int i = 1; i < realStuff.size() - 1; i++) {
							Terminal.print(", a ");
							Terminal.print(realStuff.get(i).compSub);
						}
						Terminal.print(", and a " + realStuff.get(realStuff.size() - 1).compSub);
					}
				}
				Terminal.println(".");
			} else if (n.represents == t.protag.quests) {
				if (t.protag.quests.isEmpty()) {
					Terminal.print("You have no quests.");
				} else {
					for (Quest q : t.protag.quests) {
						Terminal.println(q);
					}
				}
			}
		}, null, (Word w, Engine t)-> {
			Terminal.println("You can't check that.");
		}, null));

		game.addWord(new Verb("read", null, (Object o, Engine t) -> {
			if (o.text.length() == 0) {
				Terminal.println("The " + o.accessor + " says nothing.");
				return;
			}
			String str = o.text;
			String finalStr = "";
			String word;
			int index = 0;
			while (index != -1) {
				if (str.substring(index).indexOf(' ') != -1) {
					word = str.substring(index, str.substring(index).indexOf(' ') + index);
				} else {
					word = str.substring(index);
				}

				if (word.length() > t.protag.literacy) {
					for (int i = 0; i < word.length(); i++) {
						if (word.charAt(i) != '\n') {
							word = word.substring(0, i)
									+ Engine.lRandOf(new String[] { "@", "#", "&", "/", "%", "$", "!", "*", "+" })
									+ word.substring(i + 1);
						}
					}
				}

				finalStr += word + " ";

				if (str.substring(index).indexOf(' ') != -1) {
					index = str.substring(index).indexOf(' ') + index + 1;
				} else {
					break;
				}
			}

			Terminal.println(finalStr + "\n");
			Terminal.println(t.protag.literacy < 1 ? "You can't read."
					: t.protag.literacy < 4 ? "You can barely read."
							: t.protag.literacy < 6 ? "You're reading skills are so-so."
									: t.protag.literacy < 8 ? "You can almost read perfectly."
											: "You're an amazing reader.");

		}, null, (Object o, Engine t)-> {
			Terminal.println("I don't think that the " + o.compSub + " is something that you can read.");
		}));

		game.addWord(new Word("inventory", game.protag.inventory));
		game.addWord(new Word("quests quest-log questlog", game.protag.quests));

		game.addWord(new Word("self me myself player", game.protag));
		game.addWord(new Word("room area surroundings place around", "room"));

		game.addWord(new Direction("north forwards forward ahead onward", "10"));
		game.addWord(new Direction("south backwards backward back ", "12"));
		game.addWord(new Direction("east right", "21"));
		game.addWord(new Direction("west left", "01"));
	}
	public void addObject(Object o, int x, int y, Room r) {
		o.x = x;
		o.y = y;
		r.area[x][y] = o;
	}
	public static void removal(Object o, Engine t) {
		Room r = t.protag.currentRoom;
		Player p = t.protag;
		for(Object[] objs : r.area) {
			for(int i = 0; i < objs.length; i++) {
				if(objs[i] == o) {
					r.objects.remove(o);
					p.inventory.remove(o);
					try {
						objs[i] = o.reference;
					} catch (Exception e) {
						objs[i] = r.floor;
					}
				}
			}
		}
		try {
			o.referencer.reference = r.floor;
			o.referencer.description = Engine.lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";
		} catch (Exception e) {
		}
		try {
			if (o.reference != r.floor) {
				o.reference.reference = r.floor;
				o.reference.description = "on";
			}
		} catch (Exception e) {
		}
	}

	public void run() {
		while (true) {
			game.update();
		}
	}
}
//Some say the world will end in fire, some say in ice.
//I say end it with a comment, and it's really just as nice.
//So farewell to you my friend, you who have ventured brave and bold
//through the convoluted forests of Aidan's story, in code, told.
//What you have read may yet escape you, as it once did for me,
//but rest assured, the holy Lambda is something men can learn to see.
//I rest my case with the finality of the ending close paren —
//And I swear I'll never, EVER set Aidan's lambdas free again.
// — Nico Mantione, 11 December 2018
