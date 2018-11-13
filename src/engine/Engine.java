package engine;

import java.util.*;

import engine.things.Player;
import engine.things.Object;
import engine.words.Verb;
import engine.words.Word;

import engine.Terminal;


public class Engine {
	public Player protag;
	
	public ArrayList<Room> rooms;//can be accessed by verbs
	private ArrayList<Word> knownWords;
	
	
	
	public Engine()  {
		protag = new Player(0, 0, 0);
		
		rooms = new ArrayList<Room>();
		rooms.add(new Room(0, 0, 0));
		rooms.get(0).objects.add(new Object("brick", "on a nice hand-knitted carpet."));
		knownWords = new ArrayList<Word>();
	}
	
	public void addRoom(Room r) {
		rooms.add(r);
	}
	
	public void addWord(Word v) {
		knownWords.add(v);
	}
	
	public void update() {
		Terminal.println(protag);
		Terminal.println(rooms.get(0));
		
		String userText;
		ArrayList<String> words;
		
		while (true) {//repeats until valid command
			userText = Terminal.readln();
			userText = userText.toLowerCase();
			for(Room r : rooms) { 
				if(r.toString().equals(protag.toString())) {
					protag.currentRoom = r;
				} else {
					System.out.println("Currently not in any room");
				}
			}
			for(Object o : protag.currentRoom.objects) {
				Terminal.print("There is a " + o.accessor + " " + o.description + " ");
			}
			Terminal.print("\n");
			
			words = new ArrayList<String>();

			String[] s = userText.split(" ");
			for(String str : s) {
			words.add(str);//user text goes to array of words
			}
			if (words.size() != 2) {
				Terminal.println("All commands must be 2 words");
				continue;
			}
			
			Word w0 = null;
			Word w1 = null;
			Object o1 = null;
			boolean found = false;
			boolean foundObject = false;
			
			for (Word w: knownWords) {
				if (w.checkWord(words.get(0))) {
					w0 = w;
					found = true;
				}
			}//finds word in array
			
			if (!found) {
				Terminal.println("I don't know what '" + words.get(0) + "' means");
				continue;
			}
			
			if (w0.getClass() != Verb.class) {
				Terminal.println("Commands always start with a verb");
				continue;
			}
			
			found = false;
				
			for (Word w: knownWords) {
				if (w.checkWord(words.get(1))) {
					w1 = w;
					found = true;
				}
			}
			
			for(Object o : protag.currentRoom.objects) {
				if(o.accessor.equals(words.get(1))) {
					o1 = o;
					foundObject = true;
				}
			}
			
			if (!found && !foundObject) {
				Terminal.println("I don't know what '" + words.get(1) + "' means");
				continue;
			}
			
			if(found && !foundObject) {
			if (w1.getClass() == Verb.class) {
				Terminal.println("Commands never end with a verb");
				continue;
			}
			}
			
			if(found) {
				w0.perform(w1, this);//fills out word's function
			} else if(foundObject) {
				w0.perform(o1, this);
			}
			break;
		}
		
	}
}
