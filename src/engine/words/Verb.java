package engine.words;

import engine.Engine;
import engine.FourParamFunc;
import engine.Terminal;
import engine.ThreeParamFunc;
import engine.TwoParamFunc;
import engine.things.Object;
import engine.things.Player;

public class Verb extends Word {

	private ThreeParamFunc<Word, Engine, Player> myFunc;
	private ThreeParamFunc<Object, Engine, Player> myFunc2;
	private FourParamFunc<Object, Object, Engine, Player> myFunc3;

	private String joinerWord;

	public Verb(String list, ThreeParamFunc<Word, Engine, Player> func, ThreeParamFunc<Object, Engine, Player> func2) {
		super(list);

		myFunc = func;
		myFunc2 = func2;

		joinerWord = "";
	}

	public Verb(String list, ThreeParamFunc<Word, Engine, Player> func, ThreeParamFunc<Object, Engine, Player> func2,
			FourParamFunc<Object, Object, Engine, Player> func3, String joinerWord) {
		super(list);

		myFunc = func;
		myFunc2 = func2;
		myFunc3 = func3;

		this.joinerWord = joinerWord;
	}

	public void perform(Word w, String prepUsed, Engine t, Player p) {
		// Terminal.println(w + " : " + t);
		try {
			myFunc.accept(w, t, p);
		} catch (Exception e) {
			Terminal.sPrintln("You cannot " + synonyms.get(0) + prepUsed + " the " + w.synonyms.get(0) + ".", p.id);
		}
	}

	public void perform(Object o, String prepUsed, Engine t, Player p) {
		// Terminal.println(o + " : " + t);
		try {
			myFunc2.accept(o, t, p);
		} catch (Exception e) {
			Terminal.sPrintln("You cannot " + synonyms.get(0) + (prepUsed.isEmpty() ? " the " : prepUsed + " ")
					+ o.accessor + ".", p.id);
		}
	}

	public void perform(Object o1, Object o2, String prepUsed1, String prepUsed2, String joiningWord, Engine t, Player p) {
		// Terminal.println(o + " : " + t);
		try {
			if (!joiningWord.equals(joinerWord)) {
				throw new Exception();
			}
			myFunc3.accept(o1, o2, t, p);
		} catch (Exception e) {
			Terminal.sPrintln(
					"You cannot " + synonyms.get(0) + (prepUsed1.isEmpty() ? " the " : prepUsed1 + " ") + o1.accessor
							+ " " + joiningWord + (prepUsed2.isEmpty() ? " the " : prepUsed2 + " ") + o2.accessor, p.id);
		}
	}
}
