package engine;

import java.io.IOException;
import java.util.*;

import engine.words.Direction;
import engine.words.Verb;
import engine.words.Word;
import engine.things.Effect;
import engine.things.Entity;
import engine.things.Object;
import engine.things.Player;
import engine.things.Quest;
import javafx.scene.control.Label;
import javafx.application.Platform;
import java.util.ArrayList;
import engine.Terminal;

public class Main extends Thread {
	public static Engine game;

	public Main() {
		game = new Engine();

		game.addWord(
				new Verb("move go walk run climb jog travel journey venture", (Word w, Engine t, Player protag) -> {
					if (w.getClass() != Direction.class) {
						Terminal.sPrintln("...What?", protag.id);
						return;
					}
					int x, y;

					int dx = Integer.parseInt(w.value.substring(0, 1)) - 1;
					int dy = Integer.parseInt(w.value.substring(1, 2)) - 1;

					Room currentRoom = protag.currentRoom;
					currentRoom.objects.remove(protag);
					while (true) {// recursion without recursion
						x = protag.currentRoom.coords[0];
						y = protag.currentRoom.coords[1];

						x += dx;
						y += dy;

						Room cR = protag.currentRoom;
						for (Room r : protag.currentRoom.fatherRoom.nestedMap) {
							if (x == r.coords[0] && y == r.coords[1]) {
								protag.currentRoom = r;
							}

							if (x == r.coords[0] && y == r.coords[1]) {
								if (dx > 0) {// flipped from how you'd think
									while (protag.currentRoom.westEntry != null) {
										protag.currentRoom = protag.currentRoom.westEntry;
									}
								} else if (dx < 0) {
									while (protag.currentRoom.eastEntry != null) {
										protag.currentRoom = protag.currentRoom.eastEntry;
									}
								} else if (dy > 0) {
									while (protag.currentRoom.southEntry != null) {
										protag.currentRoom = protag.currentRoom.southEntry;
									}
								} else if (dy < 0) {
									while (protag.currentRoom.northEntry != null) {
										protag.currentRoom = protag.currentRoom.northEntry;
									}
								}
								break;
							}
						}
						/*Platform.runLater(() -> Window.gp.getChildren().clear());
						for (Room r : protag.currentRoom.fatherRoom.nestedMap) {
							Platform.runLater(() -> Window.gp.add(new Label(protag.currentRoom == r ? "@" : "R"),
									r.coords[0], r.coords[1]));
						}*/
						for (Room r : cR.fatherRoom.nestedMap) {
							if (x == r.coords[0] && y == r.coords[1]) {
								protag.changedSurroundings = true;
								protag.currentRoom.objects.add(protag);
								return;
							}
						}

						if (protag.currentRoom.fatherRoom.fatherRoom != null)// main map only has 1 room, so can't be there
																				// either
						{
							protag.currentRoom = protag.currentRoom.fatherRoom;
						} else {
							break;
						}
					}
					protag.currentRoom = currentRoom;
					
					Terminal.sPrintln("You can't move that way.", protag.id);

					// protag.changePos(w.value);
				}, null));

		game.addWord(new Verb("eat consume", null, (Object o, Engine t, Player protag) -> {
			if (o.abstractObj) {
				Terminal.sPrintln("Impossible.", protag.id);
				return;
			}
			if (!o.alive) {
				protag.hunger -= o.consumability;
				if (o.drinkability != null) {
					protag.thirst -= o.drinkability;
				}
				if (o.consumability < 0) {
					if (o.poisonous) {
						protag.effects.add(new Effect((p) -> {
							protag.health--;
						}, 30, "That was painful to eat.", "nauseous and feverish"));
					} else {
						protag.effects.add(new Effect((p) -> {
							protag.health += o.consumability * 2;
						}, 3, "That was painful to eat.", "like you've eaten some that you shouldn't have"));
					}
				} else {
					Terminal.sPrintln("You ate the " + o.accessor + ". Delicious.", protag.id);
				}
				Terminal.describesPL("He starts to consume a " + o.compSub, protag.id);
				protag.currentRoom.objects.remove(o);
				protag.inventory.remove(o);
				removal(o, t, protag);
			} else {
				throw new NullPointerException();
			}

		}));

		game.addWord(new Verb("drink", null, (Object o, Engine t, Player protag) -> {
			if (!o.alive) {
				protag.thirst -= o.drinkability;
				if (o.poisonous) {
					Terminal.sPrintln("An acrid aftertaste fills your mouth.", protag.id);
				} else {
					Terminal.sPrintln("You drank the " + o.accessor + ". Delicious.", protag.id);
				}
				Terminal.describesPL("He drinks a " + o.compSub, protag.id);
				if (o.consumability == null) {
					protag.currentRoom.objects.remove(o);
					protag.inventory.remove(o);
					removal(o, t, protag);
				} else {
					o.drinkability = null;
				}
			} else {
				throw new NullPointerException();
			}
		}));

		game.addWord(new Verb("e inspect investigate examine scrutinize study observe look",
				(Word w, Engine t, Player protag) -> {
					if (w.represents == protag.inventory) {
						Terminal.sPrintln("Try checking your inventory instead.", protag.id);
					} else if (w.represents == protag.quests) {
						Terminal.sPrintln("Try checking your quests instead.", protag.id);
					} else if (w.represents == "room") {
						t.inspectRoom(false, null, protag);
					}
				}, (Object o, Engine t, Player protag) -> {
					if (o.container.isEmpty()) {
						Terminal.sPrint(t.uRandOf(new String[] { o.inspection }), protag.id);
					} else {
						Terminal.sPrint(t.uRandOf(new String[] {
								"Upon inspection, you observe that there is a " + o.container.get(0).compSub,
								"It looks like there is a " + o.container.get(0).compSub,
								"You now can see that there is a " + o.container.get(0).compSub }), protag.id);
						if (o.container.size() == 2) {
							Terminal.sPrint(" as well as a " + o.container.get(1).compSub, protag.id);
						} else if (o.container.size() > 2) {
							for (int i = 1; i < o.container.size() - 1; i++) {
								Terminal.sPrint(", a ", protag.id);
								Terminal.sPrint(o.container.get(i).compSub, protag.id);
							}
							Terminal.sPrint(", and a " + o.container.get(o.container.size() - 1).compSub, protag.id);
						}
						Terminal.sPrint(" inside the " + o.accessor, protag.id);
					}
					Terminal.sPrintln(".", protag.id);
					Terminal.describesPL("You can see him taking a closer look at a " + o.compSub, protag.id);
				}));

		game.addWord(new Verb("interact talk speak converse negotiate chat gossip", null,
				(Object o, Engine t, Player protag) -> {
					Entity e = (Entity) o;
					Terminal.describesPL("He begins conversing with the " + e.compSub, protag.id);
					if (e.talkedTo) {
						e.repeatInteraction.accept(protag, t);
					} else {
						e.interaction.accept(protag, t);
					}
				}));

		game.addWord(new Verb("attack assault assail hit pummel strike kill destroy", null,
				(Object o, Engine t, Player protag) -> {
					if (o.equals(protag)) {
						protag.health = 0;
						Terminal.sPrintln("You killed yourself. Nice job.", protag.id);
						return;
					}

					o.health -= protag.strength * protag.weapon.damage;
					protag.health -= protag.strength + protag.weapon.playerDamage;

					if (!protag.weapon.abstractObj) {
						protag.weapon.health -= protag.strength;
					}

					if (o.alive) {
						try {
							Entity e = (Entity) o;
							e.killer = protag;
							if (e.anger < e.restraint) {
								e.anger = e.restraint;
							}
						} catch (Exception e) {
						}
						Terminal.sPrintln(t.uRandOf(new String[] { "A cry of pain greets your ears.",
								"The sharp smell of blood fills the air.", "Something cracks.",
								"A surge of adrenaline shoots through you." }), protag.id);
						Terminal.describesPL(t.uRandOf(new String[] { "He strikes the " + o.compSub + " with an astounding force.",
								"The sharp smell of blood fills the air as he hits the " + o.compSub, "He uses his " + protag.weapon.accessor + " to hit the " + o.compSub, }), protag.id);
					}
					// Terminal.sPrintln("You attacked the " + o.accessor + " with the " +
					// protag.weapon.accessor + ".");
					Terminal.sPrintln("Weapon: " + protag.weapon.accessor, protag.id);
				}, (Object o, Object with, Engine t, Player protag) -> {
					protag.weapon = with;

					o.health -= protag.strength + protag.weapon.damage;
					protag.health -= protag.strength + protag.weapon.playerDamage;
					if (o.equals(protag) && protag.health <= 0) {
						Terminal.sPrintln("You killed yourself. Nice job.", protag.id);
						return;
					}
					if (!protag.weapon.abstractObj) {
						protag.weapon.health -= protag.strength;
					}

					if (o.alive) {
						try {
							Entity e = (Entity) o;
							if (e.anger < e.restraint) {
								e.anger = e.restraint;
							}
						} catch (Exception e) {
						}
						Terminal.sPrintln(t.uRandOf(new String[] { "A cry of pain greets your ears.",
								"The sharp smell of blood fills the air.", "Something cracks.",
								"A surge of adrenaline shoots through you." }), protag.id);
						Terminal.describesPL(t.uRandOf(new String[] { "He strikes the " + o.compSub + " with an astounding force.",
								"The sharp smell of blood fills the air as he hits the " + o.compSub, "He uses his " + protag.weapon.accessor + " to hit the " + o.compSub, }), protag.id);
					}

				}, "with"));

		game.addWord(new Verb("hold equip", null, (Object o, Engine t, Player protag) -> {
			boolean b = o.holdable;
			if (protag.inventory.contains(o)) {
				if (protag.rightHand != null) {
					protag.inventory.add(protag.rightHand);
				}
				protag.rightHand = o;
				protag.inventory.remove(o);
				Terminal.sPrintln("You are now holding a " + o.accessor + ".", protag.id);
				Terminal.describesPL("He graps a " + o.accessor + " in his hand.", protag.id);
			} else if (protag.currentRoom.objects.contains(o)) {
				if (protag.rightHand != null) {
					protag.inventory.add(protag.rightHand);
				}
				protag.rightHand = o;
				protag.currentRoom.objects.remove(o);
				removal(o, t, protag);
				Terminal.sPrintln("You are now holding a " + o.accessor + ".", protag.id);
				Terminal.describesPL("He graps a " + o.accessor + " in his hand.", protag.id);
			} else {
				throw new NullPointerException();
			}
		}));

		game.addWord(new Verb("take get steal grab seize apprehend liberate collect pick", null,
				(Object o, Engine t, Player protag) -> {
					boolean b = o.holdable;
					if (o.alive) {
						throw new NullPointerException();
					}
					if (!protag.inventory.contains(o)) {
						protag.inventory.add(o);
						protag.currentRoom.objects.remove(o);
						removal(o, t, protag);
						Terminal.sPrint("You took the " + o.accessor + ".", protag.id);
						Terminal.describesPL("He takes the " + o.accessor + ".", protag.id);
					} else {
						throw new NullPointerException();
					}
				}));

		game.addWord(new Verb("drop leave put", null, (Object o, Engine t, Player protag) -> {
			if (o.abstractObj) {
				Terminal.sPrintln("No.", protag.id);
				return;
			}
			if (protag.rightHand.equals(o)) {
				protag.rightHand = protag.fist;
				o.description = "on";
				o.reference = protag.currentRoom.floor;
				protag.currentRoom.objects.add(o);
				Terminal.sPrintln("You dropped the " + o.accessor + ".", protag.id);
				Terminal.describesPL("He drops his " + o.accessor + " onto the floor.", protag.id);
			} else if (protag.inventory.contains(o)) {
				protag.inventory.remove(o);
				o.description = "on";
				o.reference = protag.currentRoom.floor;

				protag.currentRoom.objects.add(o);
				Terminal.sPrintln("You dropped the " + o.accessor + ".", protag.id);
				Terminal.describesPL("He drops his " + o.accessor + " onto the floor.", protag.id);
			} else {
				Terminal.sPrintln("You don't have a " + o.accessor + " to drop.", protag.id);
			}
		}));

		game.addWord(new Verb("give gift supply donate put", null, null,
				(Object gift, Object receiver, Engine t, Player protag) -> {
					Iterator<Object> obj = protag.inventory.iterator();
					while (obj.hasNext()) {
						Object o = obj.next();
						if (o == gift) {
							if (receiver.getClass().toString().equals("class engine.things.Entity")) {
								Entity e = ((Entity) receiver);
								e.inventory.add(o);
								Terminal.sPrintln("The " + receiver.accessor + " gladly accepts your gift.", protag.id);
								for (Quest q : protag.quests) {
									q.gaveObj(t, (Entity) receiver, o, protag);
								}
								if (e.quest != null) {
									e.quest.gaveObj(t, e, o, protag);
								}
								Terminal.describesPL("He gives a " + o.accessor + " to the " + receiver.accessor + ".", protag.id);
							} else if (receiver.getClass().toString().equals("class engine.things.Object")) {
								receiver.container.add(o);
								Terminal.sPrintln(
										"If the " + receiver.accessor + " was alive, it would surely thank you.", protag.id);
								Terminal.describesPL("He puts a " + o.accessor + " into the " + receiver.accessor, protag.id);
							}
							
							obj.remove();
							break;
						}
					}

				}, "to"));

		game.addWord(new Verb("view open check show", (Word n, Engine t, Player protag) -> {
			if (n.represents == protag.inventory) {
				ArrayList<Object> realStuff = (ArrayList<Object>) protag.inventory.clone();
				for (int i = 0; i < realStuff.size(); i++) {
					if (realStuff.get(i).abstractObj) {
						realStuff.remove(i);
						i--;
					}
				}

				if (realStuff.isEmpty()) {
					Terminal.sPrint("You have nothing in your inventory.", protag.id);
				} else {
					Terminal.sPrint("You have a " + realStuff.get(0).compSub, protag.id);
					if (realStuff.size() == 2) {
						Terminal.sPrint(" as well as a " + realStuff.get(1).compSub, protag.id);
					} else if (realStuff.size() > 2) {
						for (int i = 1; i < realStuff.size() - 1; i++) {
							Terminal.sPrint(", a ", protag.id);
							Terminal.sPrint(realStuff.get(i).compSub, protag.id);
						}
						Terminal.sPrint(", and a " + realStuff.get(realStuff.size() - 1).compSub, protag.id);
					}
				}
				Terminal.sPrintln(".", protag.id);
			} else if (n.represents == protag.quests) {
				if (protag.quests.isEmpty()) {
					Terminal.sPrint("You have no quests.", protag.id);
				} else {
					for (Quest q : protag.quests) {
						Terminal.sPrintln(q, protag.id);
					}
				}
			}
		}, null));

		game.addWord(new Verb("read", null, (Object o, Engine t, Player protag) -> {
			if (o.text.length() == 0) {
				Terminal.sPrintln("The " + o.accessor + " says nothing.", protag.id);
				return;
			}
			Terminal.describesPL("He begins to read a book.", protag.id);
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

				if (word.length() > protag.literacy) {
					for (int i = 0; i < word.length(); i++) {
						if (word.charAt(i) != '\n') {
							word = word.substring(0, i)
									+ t.lRandOf(new String[] { "@", "#", "&", "/", "%", "$", "!", "*", "+" })
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

			Terminal.sPrintln(finalStr + "\n", protag.id);
			Terminal.sPrintln(protag.literacy < 1 ? "You can't read."
					: protag.literacy < 4 ? "You can barely read."
							: protag.literacy < 6 ? "You're reading skills are so-so."
									: protag.literacy < 8 ? "You can almost read perfectly."
											: "You're an amazing reader.", protag.id);

		}));
		// PLACEHOLDER FOR A REWRITTEN WORD-ACCESS-OBJECT FUNCTION
		/*game.addWord(new Word("inventory", game.protags.get(0).inventory));
		game.addWord(new Word("quests quest-log questlog", game.protags.get(0).quests));
		
		game.addWord(new Word("self me myself player", game.protags.get(0)));
		*/
		game.addWord(new Word("room area surroundings place around", "room"));

		game.addWord(new Direction("north forwards forward ahead onward", "12"));
		game.addWord(new Direction("south backwards backward back ", "10"));
		game.addWord(new Direction("east right", "21"));
		game.addWord(new Direction("west left", "01"));
	}

	public static void removal(Object o, Engine t, Player protag) {
		for (Object obj : protag.currentRoom.objects) {
			removeFromContainers(obj, o, protag.currentRoom);
		}
		try {
			o.referencer.reference = protag.currentRoom.floor;
			o.referencer.description = t.lRandOf(new String[] { "lying", "sitting", "resting" }) + " on";
		} catch (Exception e) {
		}
		try {
			if (o.reference != protag.currentRoom.floor) {
				o.reference.reference = protag.currentRoom.floor;
				o.reference.description = "on";
			}
		} catch (Exception e) {
		}
	}

	public static void removeFromContainers(Object o, Object removeO, Room r) {
		if (!o.container.isEmpty()) {
			r.objects.remove(removeO);
			o.container.remove(removeO);
			for (Object obj : o.container) {
				removeFromContainers(obj, removeO, r);
			}
		}
	}

	public void run() {
		for(int i = 0; i < game.protags.size(); i++) {
			final int b = i;
			Thread t = new Thread() {
				public void run() {
					while (true) {
						if(game.protags.get(b) != null) {
						game.update(game.protags.get(b));
						}
					}
				}
			};
			t.start();
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
