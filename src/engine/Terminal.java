package engine;

import javafx.application.Platform;

import java.io.IOException;

import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;

@SuppressWarnings("restriction")
public class Terminal {
	public static TextFlow flow = new TextFlow();
	static boolean bold = false;
	static boolean italic = false;
	static boolean printing = true;

	public Terminal() {
		flow.setPrefWidth(Window.stack.getWidth());
		Platform.runLater(() -> Window.stack.getChildren().add(flow));
	}

	public static void println(Object s) {
		if (printing) {
			printText(s.toString());
			Platform.runLater(() -> Window.enterStack = "\n");
		}
	}

	public static void sPrintln(Object s, int id) {
		if (printing) {
			Server.out[id].println("[PRINTLN]" + s.toString());
		}
	}

	public static void sPrint(Object s, int id) {
		if (printing) {
			Server.out[id].println("[PRINT]" + s.toString());
		}
	}

	public static void broadcast(String[] intro, Object s, int id) {
		if (printing) {
			for (int i = 0; i < Server.out.length; i++) {
				if (Main.game.protags.get(i) != null) {
					Server.out[i].println("[PRINTLN]" + intro[i == id ? 0 : 1] + s.toString());
					Server.out[i].flush();
				}
			}
		}
	}
	public static void broadcast(Object s, String[] ending, int id) {
		if (printing) {
			for (int i = 0; i < Server.out.length; i++) {
				if (Main.game.protags.get(i) != null) {
					Server.out[i].println("[PRINTLN]" + s.toString() + ending[i == id ? 0 : 1]);
					Server.out[i].flush();
				}
			}
		}
	}

	public static void describesPL(Object s, int id) {
		if (printing) {
			for (int i = 0; i < Server.out.length; i++) {
				if (Main.game.protags.get(i) != null) {
					if (i != id && Main.game.protags.get(id).currentRoom == Main.game.protags.get(i).currentRoom) {
						if (s.toString().contains("player" + i)) {
							Server.out[i].println("[PRINTLN]" + s.toString().replace("the player" + i, "you"));
						} else {
							Server.out[i].println("[PRINTLN]" + s.toString().replace("the player" + id, Main.game.protags.get(i).name));
						}
						Server.out[i].flush();
					}
				}
			}
		}
	}

	public static void describesP(Object s, int id) {
		if (printing) {
			for (int i = 0; i < Server.out.length; i++) {
				if (Main.game.protags.get(i) != null) {
					if (i != id && Main.game.protags.get(id).currentRoom == Main.game.protags.get(i).currentRoom) {
						if (s.toString().contains("player" + i)) {
							Server.out[i].println("[PRINT]" + s.toString().replace("the player" + i, "you"));
						} else {
							Server.out[i].println("[PRINT]" + s.toString().replace("the player" + id, Main.game.protags.get(i).name));
						}
						Server.out[i].flush();
					}
				}
			}
		}
	}

	public static void print(Object s) {
		if (printing) {
			printText(s.toString());
		}
	}

	public static void readln() {
		while (!Window.enterKeyPressed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Window.t.interrupt();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Window.enterKeyPressed = false;
		TextFlow newFlow = new TextFlow();
		newFlow.setPrefWidth(Window.root.getWidth() / 2);
		Platform.runLater(() -> Window.stack.getChildren().add(newFlow));

		flow = newFlow;
		Window.out.println(Window.s);
		Window.out.flush();

		printing = true;

	}

	public static void printText(String s) {
		s = s.replace(".", ".(250)");
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
			} catch (InterruptedException e) {
			}

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
