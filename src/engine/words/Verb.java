package engine.words;

import java.util.function.Consumer;

import engine.Engine;
import engine.Terminal;
import engine.TwoParamFunc;
import engine.things.Object;

public class Verb extends Word {

	private TwoParamFunc<Word, Engine> myFunc;
	private TwoParamFunc<Object, Engine> myFunc2;

	public Verb(String list, TwoParamFunc<Word, Engine> func, TwoParamFunc<Object, Engine> func2) {
		super(list);

		myFunc = func;
		myFunc2 = func2;
	}

	public void perform(Word w, Engine t) {
		// Terminal.println(w + " : " + t);
		myFunc.accept(w, t);
	}

	public void perform(Object o, String prepUsed, Engine t) {
		// Terminal.println(o + " : " + t);
		try {
			myFunc2.accept(o, t);
		} catch (NullPointerException e) {
			Terminal.println("You cannot " + synonyms.get(0) + " " + prepUsed + " the " + o.accessor + ".");
		}
	}
}
