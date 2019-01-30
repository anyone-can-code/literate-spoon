package engine.things;

import engine.Engine;
import engine.Terminal;
import engine.TwoParamFunc;
import engine.things.Player;
import engine.things.Entity;
import engine.things.Object;

public class Quest {
	public String name;
	public String description;
	public Object target;
	public TwoParamFunc<Engine, Entity> reward;
	public Entity giver;
	public boolean completed;
	public boolean found;
	public boolean given;

	public Quest(String nam, String desc, Object tar, Entity givr, TwoParamFunc<Engine, Entity> rew) {
		description = desc;
		target = tar;
		reward = rew;
		giver = givr;
		name = nam;

		completed = false;
		found = false;
		given = false;
	}

	public void giveTo(Player p) {
		if (!given) {
			p.quests.add(this);
			given = true;
			Terminal.sPrintln("Quest added.", p.id);
		} else {
			Terminal.sPrintln("Quest already added.", p.id);
		}
	}

	public void run(Engine t, boolean print, Player protag) {
		if (!found) {
			f1: for (Object o : protag.inventory) {
				if (o == target) {
					found = true;
					break;
				}
				for (Object Obj : o.container) {
					if (Obj == target) {
						found = true;
						break;
					}
				}
			}

			if (found && print)
				Terminal.sPrintln(
						"Return the " + target.accessor + " to the " + giver.accessor + " to receive a reward.",
						protag.id);
		}
	}

	public void gaveObj(Engine t, Entity e, Object o, Player protag) {
		if (!found) {
			run(t, false, protag);
		}
		if (found && e == giver && o == target) {
			target = null;
			if (reward != null) {
				reward.accept(t, e);
			} else {
				Terminal.sPrintln("You receive nothing but gratitude for your troubles.", protag.id);
			}
			completed = true;
		}
	}

	public String toString() {
		return name + (completed ? " [COMPLETE]" : "") + ":\n" + description;
	}
}
