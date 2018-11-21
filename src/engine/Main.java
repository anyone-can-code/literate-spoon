package engine;

import engine.words.Direction;
import engine.words.Verb;
import engine.words.Word;
import engine.things.Effect;
import engine.things.Object;
import engine.Terminal;

public class Main {

	public static Engine game;

	public static void main(String args[]) {
		game = new Engine();
		game.addWord(new Verb("move go walk run climb jog strut", (Word w, Engine t) -> {
			if (w.getClass() != Direction.class) {
				Terminal.println("Please specify a direction");
				return;
			}

			t.protag.changePos(w.value);
		}, null));
		game.addWord(new Verb("eat consume", null, (Object o, Engine t) -> {
			t.protag.hunger -= o.consumability;
			if (o.consumability < 0) {
				t.protag.effects.add(new Effect((p) -> {
					t.protag.health += o.consumability * 2;
				}, 3, "That was painful to eat."));
			} else {
				Terminal.println("You ate a " + o.accessor + ". Delicious.");
			}
			t.protag.currentRoom.objects.remove(o);
			try {
			o.referencer.reference.compSub = t.randOf(new String[] {"floor", "ground"}).toLowerCase();
			o.referencer.description = o.referencer.description.replace(" a", " the");
			} catch (Exception e) {
				
			}
		}));
		game.addWord(new Verb("inspect investigate examine scrutinize study observe", null, (Object o, Engine t) -> {
			if(o.container.isEmpty()) {
			Terminal.print(t.randOf(new String[] {"Upon inspection, you realize that " + o.inspection, "It looks like " + o.inspection, 
					"You now can see that " + o.inspection}));
			} else {
				if(o.container.size() == 1) {
					Terminal.print(t.randOf(new String[] {"Upon inspection, you observe that there is a " + o.container.get(0).compSub, "It looks like there is a " + o.container.get(0).compSub, 
							"You now can see that there is a " + o.container.get(0).compSub}));
					Terminal.print(" inside the " + o.accessor);
				} else if(o.container.size() == 2) {
					Terminal.print(t.randOf(new String[] {"Upon inspection, you observe that there is a " + o.container.get(0).compSub, "It looks like there is a " + o.container.get(0).compSub, 
							"You now can see that there is a " + o.container.get(0).compSub}));
					Terminal.print(" as well as a " + o.container.get(1).compSub + " inside the " + o.accessor);
				} else {
					Terminal.print(t.randOf(new String[] {"Upon inspection, you observe that there is a " + o.container.get(0).compSub, "It looks like there is a " + o.container.get(0).compSub, 
							"You now can see that there is a " + o.container.get(0).compSub}));
					for(int i = 1; i < o.container.size() - 1; i++) {
						Terminal.print(", a ");
						Terminal.print(o.container.get(i).compSub);
					}
					Terminal.print(", and a " + o.container.get(o.container.size() - 1).compSub + " inside the " + o.accessor);
				}
			}
			Terminal.println(".");
		}));
		
		game.addWord(new Verb("attack assault assail punch hit kick pummel strike", null, (Object o, Engine t) -> {
			o.health -= t.protag.basicAttack;
			Terminal.println("You attacked the " + o.accessor + ".");
		}));
		game.addWord(new Verb("take steal grab seize apprehend liberate", null, (Object o, Engine t) -> {
			boolean b = o.holdable;
			t.protag.inventory.add(o);
			t.protag.currentRoom.objects.remove(o);
			Terminal.println("You took the " + o.accessor + ".");
		}));


		game.addWord(new Direction("north forwards", "121"));
		game.addWord(new Direction("south backwards", "101"));
		game.addWord(new Direction("east right", "211"));
		game.addWord(new Direction("west left", "011"));
		game.addWord(new Direction("up skyward", "112"));
		game.addWord(new Direction("down groundward", "110"));

		while (true) {
			game.update();
		}

	}
}
