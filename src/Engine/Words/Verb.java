package engine.words;
import java.util.function.Consumer; 

import engine.Engine;
import engine.TwoParamFunc;

public class Verb extends Word {
	
	private TwoParamFunc<Word, Engine> myFunc;
	
	public Verb(String list, TwoParamFunc<Word, Engine> func) {
		super(list);
		
		myFunc = func;
	}
	
	public void perform(Word w, Engine t) {
		//Terminal.println(w + " : " + t);
		myFunc.accept(w, t);
	}
}
