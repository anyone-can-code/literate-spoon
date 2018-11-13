package engine.words;
import java.util.*;

import engine.Engine;


public abstract class Word {
	private ArrayList<String> synonyms;
	
	public String value; //for use of verbs
	
	public Word() {
		synonyms = new ArrayList<String>();
	}
	
	public Word(String list) {
		synonyms = new ArrayList<String>();
		addSynonyms(list);
	}
	
	public void perform(Word w, Engine t) {
		System.out.println(this.getClass());
	}
	
	public void addSynonyms(String list) {
		while (list.indexOf(' ') != -1) {
			synonyms.add(list.substring(0, list.indexOf(' ')));
			list = list.substring(list.indexOf(' ')+1);
		}
		
		synonyms.add(list);
	}
	
	public boolean checkWord(String word) {
		for (String t: synonyms) {
			if (t.equals(word))
				return true;
		}
		return false;
	}
}
