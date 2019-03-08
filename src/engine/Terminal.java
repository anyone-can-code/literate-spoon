package engine;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;

public class Terminal {
	static TextFlow flow = new TextFlow();
	static boolean bold = false;
	static boolean italic = false;
	static boolean title = false;
	static boolean printing = true;
	public static Font titleFont;
	public static Font passageFont;
	public static Font mapFont;
	public static Engine t;
	public Terminal() {
		titleFont = Font.loadFont(this.getClass().getResource("fonts/ailerons.otf").toExternalForm(), 35);
		mapFont = Font.font("Courier", 20);
		passageFont = Font.loadFont(this.getClass().getResource("fonts/clearlight.ttf").toExternalForm(), 32);
		flow.setPrefWidth(Window.stack.getWidth());
		Platform.runLater(() -> Window.stack.getChildren().add(flow));
	}

	public static void println() {
		if (printing) {
			Platform.runLater(() -> Window.enterStack = "\n");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void println(Object s) {
		if (printing) {
			printText(s.toString());
			Platform.runLater(() -> Window.enterStack = "\n");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void print(Object s) {
		if (printing) {
			printText(s.toString());
		}
	}

	public static String readln() {
		while (!Window.enterKeyPressed) {
			if (Window.moveX != 0 || Window.moveY != 0) {
				int moveX = Window.moveX;
				int moveY = Window.moveY;
				Window.moveX = 0;
				Window.moveY = 0;
				printing = true;
				return (moveX == 1 ? "move right"
						: moveX == -1 ? "move left" : moveY == 1 ? "move north" : "move south");
			}
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
			final boolean ti = title;
			Platform.runLater(() -> {
				Text t = new Text(Window.enterStack + strs[0]);
				t.setFill(Color.gray(0.8));
				if (ti) {
					t.setFont(titleFont);
				} else if (bo) {
					t.setFont(passageFont);
					t.setStyle("-fx-font-weight: bold;");
				} else if (it) {
					t.setFont(passageFont);
					t.setStyle("-fx-font-style: italic;");
				} else {
					t.setFont(passageFont);
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
				if (strs[i].equalsIgnoreCase("T")) {
					title = !title;
				}
			}
			try {
				Platform.runLater(() -> Window.gp.getChildren().clear());
				for (int x = 0; x < t.protag.currentRoom.area.length; x++) {
					for (int y = 0; y < t.protag.currentRoom.area[x].length; y++) {
						Label l = new Label(t.protag.x == x && t.protag.y == y ? "@"
								: t.protag.currentRoom.area[x][y] != null ? String.valueOf(t.protag.currentRoom.area[x][y].label)
										: "~");
						l.setFont(mapFont);
						l.setTextFill(Color.gray(0.8));
						try {
							if (t.view[x][y] == 1) {
								l.setTextFill(Color.gray(0.5));
							}
						} catch (Exception e) {
						}
						l.setRotate(-Window.rotation);
						l.setOpacity(Window.gpTP[x][y]);
						final int x1 = x;
						final int y1 = y;
						Platform.runLater(() -> Window.gp.add(l, x1, y1));
					}
				}
				Thread.sleep(n);
			} catch (InterruptedException e) {
			}

			if (i + 1 != strs.length) {
				try {
					final int o = i;
					final boolean bo = bold;
					final boolean it = italic;
					final boolean ti = title;
					Platform.runLater(() -> {
						Text t = new Text(Window.enterStack + strs[o + 1]);
						t.setFill(Color.gray(0.8));
						if (ti) {
							t.setFont(titleFont);
						} else if (bo) {
							t.setFont(passageFont);
							t.setStyle("-fx-font-weight: bold;");
						} else if (it) {
							t.setFont(passageFont);
							t.setStyle("-fx-font-style: italic;");
						} else {
							t.setFont(passageFont);
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
