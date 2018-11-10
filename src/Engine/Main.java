package Engine;

import Engine.Words.Direction;
import Engine.Words.Verb;
import Engine.Words.Word;

public class Main {
	
	public static Engine game;
	
	public static void main(String args[]) {
		game = new Engine();
		game.addWord(new Verb("move go walk run climb jog strut", (Word w, Engine t) -> {
			if (w.getClass() != Direction.class) {
				System.out.println("Please specify a direction");
				return;
			}
			
			t.protag.changePos(w.value);
			
		}));
		
		game.addWord(new Direction("north forwards", "121"));
		game.addWord(new Direction("south backwards", "101"));
		game.addWord(new Direction("east right", "211"));
		game.addWord(new Direction("west left", "011"));
		game.addWord(new Direction("up skyward", "112"));
		game.addWord(new Direction("down groundward", "110"));
		
		while(true) {
			
			game.update();
		}
		
	}
}
