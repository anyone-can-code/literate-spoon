package engine.things;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import engine.Engine;
import engine.OneParamFunc;
import engine.Terminal;
import engine.TwoParamFunc;
import engine.things.Object;

public class Entity extends Object {
	public int anger = 0;
	public int restraint = 10;
	public int nervousness = 10;
	public int truthfulness = 20;
	public int concealsEmotions = 10;
	public int kindness = 10;
	public int talkativity = 5;
	public int strength = 5;
	public int intelligence = 10;
	public int agility = 10;
	public int charisma = 10;
	public String accessor;
	public String compSub = "";
	public String description;
	public String initConversation = null;
	public Boolean interactable = true;
	public Boolean talkedTo = false;
	public ArrayList<String> knowledge = new ArrayList<String>();
	public ArrayList<Object> inventory = new ArrayList<Object>();
	public OneParamFunc<Engine> death;
	public TwoParamFunc<Player, Engine> interaction;
	public Quest quest = null;
	public TwoParamFunc<Player, Engine> repeatInteraction;

	public Entity(String compSub, String description, OneParamFunc<Engine> death) {
		super(compSub, description, null);
		this.accessor = compSub.substring(compSub.indexOf("[") + 1, compSub.indexOf("]"));
		this.compSub = compSub.replace("[", "").replace("]", "");
		this.description = description;
		this.death = death;
		alive = true;
		injury = type.bruises;
		setHealth(20);
	}

	public void Dialogue(String statement, HashMap<String, OneParamFunc<Player>> options, Player p) {
		boolean b = interactable;
		Terminal.print(statement);
		for (Map.Entry<String, OneParamFunc<Player>> entry : options.entrySet()) {
			Terminal.print(" [" + entry.getKey() + "]");
		}
		Terminal.println("");
		while (true) {
			String str = Terminal.readln();
			for (Map.Entry<String, OneParamFunc<Player>> entry : options.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(str)) {
					entry.getValue().accept(p);
					return;
				}
			}
			Terminal.println("Not a valid response.");
		}
	}

	public Boolean check(Player p, Engine e) {
		if (anger >= restraint) {
			attack(p);
			return (Boolean) null;
		}
		if (initConversation != null && e.changedSurroundings) {
			Terminal.println(initConversation);
			interaction.accept(p, e);
		}
		return true;
	}

	public void attack(Player p) {
		Terminal.println("The " + accessor + " attacks you.");
		p.health -= strength;
	}
}