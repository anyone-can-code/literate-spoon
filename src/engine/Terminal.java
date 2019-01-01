package engine;

import javafx.application.Platform;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;

public class Terminal {
	static TextFlow flow = new TextFlow();
	static boolean bold = false;
	static boolean italic = false;
	static boolean printing = true;

	public Terminal() {
		flow.setPrefWidth(Window.stack.getWidth());
		Platform.runLater(() -> Window.stack.getChildren().add(flow));
	}

	public static void println(Object s) {
		if(printing) {
		printText(s.toString());
		Platform.runLater(() -> Window.enterStack = "\n");
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}

	public static void print(Object s) {
		if(printing) {
		printText(s.toString());
		}
	}

	public static String readln() {
		while (!Window.enterKeyPressed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Window.enterKeyPressed = false;
		TextFlow newFlow = new TextFlow();
		newFlow.setPrefWidth(Window.root.getWidth() / 2);
		Platform.runLater(() -> Window.stack.getChildren().add(newFlow));

		flow = newFlow;
		printing = true;
		return Window.s;
	}

	public static void printText(String s) {
		s = s.replace(".", ".(500)");
		s = s.replace("(", "∆").replace(")", "∆");
		boolean b = false;
		if (!s.isEmpty()) {
			if (s.charAt(0) == '∆') {
				s = " " + s;
				b = true;
			}
		}
		String[] strs = s.split("∆");
		if (!b) {
			final boolean bo = bold;
			final boolean it = italic;
			Platform.runLater(() -> {
				Text t = new Text(Window.enterStack + strs[0]);
				if (bo) {
					t.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
				} else if (it) {
					t.setFont(Font.font("Verdana", FontPosture.ITALIC, 15));
				} else {
					t.setFont(new Font(15));
				}
				FadeTransition ft = new FadeTransition(Duration.millis(2000), t);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				flow.getChildren().add(t);
				Window.enterStack = "";
			});
		}
		for (int i = 1; i < strs.length; i += 2) {
			int n = 0;

			try {
				n = Integer.parseInt(strs[i]);
			} catch (Exception e) {
				if (strs[i].equalsIgnoreCase("B")) {
					bold = !bold;
				} else if (strs[i].equalsIgnoreCase("I")) {
					italic = !italic;
				}
			}
			try {
				Thread.sleep(n);
			} catch (InterruptedException e) {}

			if (i + 1 != strs.length) {
				try {
					final int o = i;
					final boolean bo = bold;
					final boolean it = italic;
					Platform.runLater(() -> {
						Text t = new Text(Window.enterStack + strs[o + 1]);
						if (bo) {
							t.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
						} else if (it) {
							t.setFont(Font.font("Verdana", FontPosture.ITALIC, 15));
						} else {
							t.setFont(new Font(15));
						}
						FadeTransition ft = new FadeTransition(Duration.millis(2000), t);
						ft.setFromValue(0.0);
						ft.setToValue(1.0);
						ft.play();
						flow.getChildren().add(t);
						Window.enterStack = "";
					});
				} catch (Exception e) {
				}
			}

		}
	}
}
