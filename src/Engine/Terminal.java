package engine;

import java.util.Scanner;

public class Terminal {
	
	public Terminal() {

	}
	
	public static void println(Object s) {
		System.out.println(s.toString());
	}
	
	public static String readln() {
		return new Scanner(System.in).nextLine();
	}
}
