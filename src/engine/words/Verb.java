package engine.words;

import engine.Engine;
import engine.Terminal;
import engine.ThreeParamFunc;
import engine.TwoParamFunc;
import engine.things.Object;

public class Verb extends Word {

    private TwoParamFunc<Word, Engine> myFunc;
    private TwoParamFunc<Object, Engine> myFunc2;
    private ThreeParamFunc<Object, Object, Engine> myFunc3;

    private String joinerWord;

    public Verb(String list, TwoParamFunc<Word, Engine> func, TwoParamFunc<Object, Engine> func2) {
        super(list);

        myFunc = func;
        myFunc2 = func2;

        joinerWord = "";
    }

    public Verb(String list, TwoParamFunc<Word, Engine> func, TwoParamFunc<Object, Engine> func2, ThreeParamFunc<Object, Object, Engine> func3, String joinerWord) {
        super(list);

        myFunc = func;
        myFunc2 = func2;
        myFunc3 = func3;

        this.joinerWord = joinerWord;
    }

    public void perform(Word w, String prepUsed, Engine t) {
        // Terminal.println(w + " : " + t);
        try {
            myFunc.accept(w, t);
        } catch (Exception e) {
            Terminal.println("You cannot " + synonyms.get(0) + prepUsed + " the " + w.synonyms.get(0) + ".");
        }
    }

    public void perform(Object o, String prepUsed, Engine t) {
        // Terminal.println(o + " : " + t);
        try {
            myFunc2.accept(o, t);
        } catch (Exception e) {
            Terminal.println("You cannot " + synonyms.get(0) + (prepUsed.isEmpty() ? " the " : prepUsed + " ") + o.accessor + ".");
        }
    }

    public void perform(Object o1, Object o2, String prepUsed1, String prepUsed2, String joiningWord, Engine t) {
        // Terminal.println(o + " : " + t);
        try {
            if (!joiningWord.equals(joinerWord)) {
                throw new Exception();
            }
            myFunc3.accept(o1, o2, t);
        } catch (Exception e) {
            Terminal.println("You cannot " + synonyms.get(0) + (prepUsed1.isEmpty() ? " the " : prepUsed1 + " ") + o1.accessor + " " + joiningWord + (prepUsed2.isEmpty() ? " the " : prepUsed2 + " ") + o2.accessor);
        }
    }
}
