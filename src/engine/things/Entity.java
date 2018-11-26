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
	public String accessor;
	public String compSub = "";
	public String description;
	public ArrayList<Object> inventory = new ArrayList<Object>();
	public OneParamFunc<Engine> death;
	public TwoParamFunc<Player, Engine> interaction;

	public Entity(String compSub, String description, TwoParamFunc<Player, Engine> interaction,
			OneParamFunc<Engine> death) {
		super(compSub, description, null);
		this.accessor = compSub.substring(compSub.indexOf("[") + 1, compSub.indexOf("]"));
		this.compSub = compSub.replace("[", "").replace("]", "");
		this.description = description;
		this.interaction = interaction;
		this.death = death;
		alive = true;
		injury = type.bruises;
		setHealth(20);
	}

	public void Dialogue(String statement, HashMap<String, TwoParamFunc<Entity, Player>> options, Entity e, Player p) {
		while (true) {
			String str = Terminal.readln();
			for (Map.Entry<String, TwoParamFunc<Entity, Player>> entry : options.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(str)) {
					entry.getValue().accept(e, p);
					break;
				}
			}
		}
	}
}
