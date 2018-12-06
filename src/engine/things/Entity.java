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
	public Boolean interactable = true;
	public ArrayList<Object> inventory = new ArrayList<Object>();
	public OneParamFunc<Engine> death;
	public TwoParamFunc<Player, Engine> interaction;

	public Entity(String compSub, String description,
			OneParamFunc<Engine> death) {
		super(compSub, description, null);
		this.accessor = compSub.substring(compSub.indexOf("[") + 1, compSub.indexOf("]"));
		this.compSub = compSub.replace("[", "").replace("]", "");
		this.description = description;
		this.death = death;
		alive = true;
		injury = type.bruises;
		setHealth(20);
	}

	public void Dialogue(String statement, HashMap<String, TwoParamFunc<Entity, Player>> options, Entity e, Player p) {
		boolean b = interactable;
		Terminal.print(statement);
		for(Map.Entry<String, TwoParamFunc<Entity, Player>> entry : options.entrySet()) {
			Terminal.print(" [" + entry.getKey() + "]");
		}
		Terminal.println("");
		while (true) {
			String str = Terminal.readln();
			for (Map.Entry<String, TwoParamFunc<Entity, Player>> entry : options.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(str)) {
					entry.getValue().accept(e, p);
					return;
				}
			}
			Terminal.println("Not a valid response.");
		}
	}
	
	public Boolean check(Player p) {
		if(anger >= restraint) {
			attack(p);
			return (Boolean)null;
		}
		return true;
	}
	
	public void attack(Player p) {
		Terminal.println("The " + accessor + " attacks you.");
		p.health -= strength;
	}
}
