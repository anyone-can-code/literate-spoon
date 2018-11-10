package Engine;

import java.util.*;

import Engine.Things.Player;
import Engine.Words.Verb;
import Engine.Words.Word;


public class Engine {
	public Player protag;
	
	public ArrayList<Room> rooms;//can be accessed by verbs
	private ArrayList<Word> knownWords;
	
	private Scanner input;
	
	
	public Engine()  {
		protag = new Player(0, 0, 0);
		
		rooms = new ArrayList<Room>();
		knownWords = new ArrayList<Word>();
		
		input = new Scanner(System.in);
	}
	
	public void addRoom(Room r) {
		rooms.add(r);
	}
	
	public void addWord(Word v) {
		knownWords.add(v);
	}
	
	public void update() {
		System.out.println(protag);
		
		String userText;
		ArrayList<String> words;
		
		while (true) {//repeats until valid comman
			userText = input.nextLine();
			userText = userText.toLowerCase();
			
			
			words = new ArrayList<String>();
			
			while (userText.indexOf(' ') != -1) {
				words.add(userText.substring(0, userText.indexOf(' ')));
				userText = userText.substring(userText.indexOf(' ')+1);
			}	
			words.add(userText);//user text goes to array of words
			
			if (words.size() != 2) {
				System.out.println("All commands must be 2 words");
				continue;
			}
			
			Word w0 = null;
			Word w1 = null;
			
			boolean found = false;
				
			for (Word w: knownWords) {
				if (w.checkWord(words.get(0))) {
					w0 = w;
					found = true;
				}
			}//finds word in array
			
			if (!found) {
				System.out.println("I don't know what '" + words.get(0) + "' means");
				continue;
			}
			
			if (w0.getClass() != Verb.class) {
				System.out.println("Commands always start with a verb");
				continue;
			}
			
			found = false;
				
			for (Word w: knownWords) {
				if (w.checkWord(words.get(1))) {
					w1 = w;
					found = true;
				}
			}
			
			if (!found) {
				System.out.println("I don't know what '" + words.get(1) + "' means");
				continue;
			}
			
			if (w1.getClass() == Verb.class) {
				System.out.println("Commands never end with a verb");
				continue;
			}
			
			w0.perform(w1, this);//fills out word's function
			break;
		}
		
	}
}
