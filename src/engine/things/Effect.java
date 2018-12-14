package engine.things;

import java.util.Random;

import engine.OneParamFunc;
import engine.Terminal;

public class Effect {
	public OneParamFunc<Player> f;
	public int lifetime;
	public String quality = "";
	Random rand = new Random();
	public Effect(OneParamFunc<Player> f, int lifetime, String init, String quality) {
		this.f = f;
		this.lifetime = lifetime;
		this.quality = quality;
		Terminal.println(init);
	}

	public void affect(Player p) {
		f.accept(p);
		if(rand.nextInt(5) == 0) {
			Terminal.println("You are feeling " + quality + ".");
		}
		lifetime--;
	}
}
