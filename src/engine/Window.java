package engine;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

import java.awt.MouseInfo;
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Window extends Application {
	public static volatile boolean enterKeyPressed = false;
	public static String s = "";
	public static Pane background = new Pane();
	public static AnchorPane root = new AnchorPane();
	public static GridPane gp = new GridPane();
	public static double[][] gpTP = new double[1000][1000];
	public static volatile VBox stack = new VBox();
	public static volatile VBox map = new VBox();
	public static volatile String enterStack = "";
	public boolean skipStart = false; // for quick testing
	public boolean enterText = false;
	public static int moveX = 0;
	public static int moveY = 0;
	public static int rotation = 0;
	public double color = 1;
	public ArrayList<ChangeListener<Number>> rotListen = new ArrayList<ChangeListener<Number>>();

	public static void main(String args[]) {
		launch(args);
	}

	AnchorPane startRoot = new AnchorPane();
	Scene start = new Scene(startRoot, 1000, 1000);

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Literate Spoon");
		primaryStage.setScene(start);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> System.exit(0));

		Scene scene = new Scene(background, start.getWidth(), start.getHeight());
		background.getChildren().add(root);
		background.setMinWidth(start.getWidth());
		background.setMinHeight(start.getHeight());
		root.setMinWidth(start.getWidth());
		root.setMinHeight(start.getHeight());
		root.setMaxWidth(start.getWidth());
		root.setMaxHeight(start.getHeight());
		scene.widthProperty().addListener((o) -> {
			root.setMinWidth(scene.getWidth());
			root.setMinHeight(scene.getHeight());
			root.setMaxWidth(scene.getWidth());
			root.setMaxHeight(scene.getHeight());
		});
		scene.heightProperty().addListener((o) -> {
			root.setMinWidth(scene.getWidth());
			root.setMinHeight(scene.getHeight());
			root.setMaxWidth(scene.getWidth());
			root.setMaxHeight(scene.getHeight());
		});
		startRoot.setBackground(new Background(new BackgroundFill(Color.gray(0), CornerRadii.EMPTY, Insets.EMPTY)));
		Label label = new Label("Literate Spoon");
		label.setTextFill(Color.gray(0));
		label.setFont(Font.font("Futura", 150));
		label.setTranslateY(start.getHeight() / 2 - 100);
		Label sublabel = new Label("May cause soothing emotions");
		sublabel.setTextFill(Color.gray(0));
		sublabel.setFont(Font.font("Futura", 30));
		sublabel.setTranslateY(start.getHeight() / 2 + 60);
		startRoot.getChildren().add(label);
		startRoot.getChildren().add(sublabel);
		Button b = new Button("New game");
		b.setPrefSize(100, 50);
		b.setTranslateX(start.getWidth() / 2 - b.getPrefWidth() / 2);
		b.setTranslateY(start.getHeight() / 2 - b.getPrefHeight() / 2);
		b.setStyle("-fx-focus-color: transparent;-fx-faint-focus-color: transparent;-fx-background-color: #DDDDDD");
		start.addEventFilter(KeyEvent.KEY_PRESSED, k -> {
	        if (k.getCode() == KeyCode.SPACE){
	            k.consume();
	        }
	    });
		Thread thr = new Thread() {
			public void run() {
				Random rand = new Random();
				double momentumX = 0;
				double momentumY = 0;
				while (true) {
					color *= 1.001;
					Platform.runLater(() -> {
						startRoot.setBackground(new Background(new BackgroundFill(Color.gray(0.5 - Math.cos(color) / 2),
								CornerRadii.EMPTY, Insets.EMPTY)));
						label.setTextFill(Color.gray(0.5 + Math.cos(color) / 2));
						sublabel.setTextFill(Color.gray(0.5 + Math.cos(color) / 2));
						sublabel.setText("May cause " + (color > 300 ? "permanent mental damage"
								: color > 100 ? "major seizures"
										: color > 50 ? "minor seizures"
												: color > 5 ? "minor unease" : "soothing emotions"));
					});
					momentumX = 0.999 * momentumX + 0.001 * (rand.nextFloat() - 0.5) * 100;
					momentumY = 0.999 * momentumY + 0.001 * (rand.nextFloat() - 0.5) * 100;
					final double mX = momentumX;
					final double mY = momentumY;
					Platform.runLater(() -> {
						b.setRotate(b.getRotate() + mX + mY);
					});
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Platform.runLater(() -> {
						b.setTranslateX(b.getTranslateX() + mX);
						b.setTranslateY(b.getTranslateY() + mY);
					});
					Bounds bounds = b.localToScreen(b.getBoundsInLocal());
					double deltaX = (bounds.getMaxX() + bounds.getMinX()) / 2
							- MouseInfo.getPointerInfo().getLocation().getX();
					double deltaY = (bounds.getMaxY() + bounds.getMinY()) / 2
							- MouseInfo.getPointerInfo().getLocation().getY();
					double deltaMag = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					if (deltaMag < 100) {
						momentumX = 0.999 * momentumX + 0.0001 * deltaX / deltaMag * 2500;
						momentumY = 0.999 * momentumY + 0.0001 * deltaY / deltaMag * 2500;
					}
					Platform.runLater(() -> {
						if (b.getTranslateX() < -100) {
							b.setTranslateX(start.getWidth());
							b.setTranslateY(start.getHeight() - b.getTranslateY());
						} else if (b.getTranslateX() > start.getWidth() + 100) {
							b.setTranslateX(0);
							b.setTranslateY(start.getHeight() - b.getTranslateY());
						}
						if (b.getTranslateY() < -100) {
							b.setTranslateY(start.getHeight());
							b.setTranslateX(start.getWidth() - b.getTranslateX());
						} else if (b.getTranslateY() > start.getHeight() + 100) {
							b.setTranslateY(0);
							b.setTranslateX(start.getWidth() - b.getTranslateX());
						}
					});
				}
			}
		};
		b.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(scene);
				new Terminal();
				Main m = new Main();
				m.start();
				thr.stop();
			}
		});
		thr.start();
		if (skipStart) {
			primaryStage.setScene(scene);
			new Terminal();
			Main m = new Main();
			m.start();
			thr.stop();
		}
		startRoot.getChildren().add(b);
		background.setBackground(new Background(new BackgroundFill(Color.gray(0.0), CornerRadii.EMPTY, Insets.EMPTY)));
		root.setBackground(
				new Background(new BackgroundFill(Color.gray(0.07), new CornerRadii(20), new Insets(10, 10, 10, 10))));
		stack.setPadding(new Insets(10, 10, 10, 10));
		stack.setPrefWidth(root.getWidth() / 2 - 20);
		map.setPadding(new Insets(10, 10, 10, 10));
		map.setPrefWidth(root.getWidth() / 2 - 20);
		map.setPrefHeight(scene.getHeight() - 30);
		TextField input = new TextField();
		input.setText("input action...");
		input.setStyle("-fx-text-fill: rgb(150, 150, 150);");
		input.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				input.setEditable(true);
				if (input.getText().equals("input action..."))
					input.clear();
				input.setStyle("-fx-text-fill: rgb(255, 255, 255);");
			}
		});
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				input.setEditable(false);
				if (input.getText().equals(""))
					input.setText("input action...");
				input.setStyle("-fx-text-fill: rgb(150, 150, 150);");
			}
		});
		for (double[] d : gpTP) {
			for (int i = 0; i < d.length; i++) {
				d[i] = 1;
			}
		}

		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (!input.isEditable()) {
					Terminal.printing = false;
					KeyCode key = event.getCode();
					if (key.equals(KeyCode.W)) {
						moveY = 1;
					} else if (key.equals(KeyCode.S)) {
						moveY = -1;
					} else if (key.equals(KeyCode.D)) {
						rotation -= 45;
					} else if (key.equals(KeyCode.A)) {
						rotation += 45;
					} else {
						Terminal.printing = true;
						return;
					}
					ArrayList<Node> gpChildren = new ArrayList<>();
					synchronized (gp) {
						gpChildren = new ArrayList<>(gp.getChildren());
					}
					for (Node n : gpChildren) {
						if (gp.localToParent(n.getBoundsInParent()).getMinX() < 0
								|| gp.localToParent(n.getBoundsInParent()).getMinX() > map.getWidth()
								|| gp.localToParent(n.getBoundsInParent()).getMinY() < 0
								|| gp.localToParent(n.getBoundsInParent()).getMinY() > map.getHeight()) {
							n.setOpacity(0);
						} else {
							n.setOpacity(1);
						}
						gpTP[GridPane.getColumnIndex(n)][GridPane.getRowIndex(n)] = n.getOpacity();
					}
					Node node = null;
					for (Object o : gp.getChildren()) {
						Node n = (Node) o;
						try {
							Label l = (Label) o;
							if (l.getText().equals("@")) {
								node = n;
								TranslateTransition t = new TranslateTransition(Duration.millis(1000), gp);
								t.setFromX(gp.getTranslateX());
								t.setFromY(gp.getTranslateY());
								t.setToX(gp.getTranslateX() + map.getWidth() / 2
										- gp.localToParent(n.getBoundsInParent()).getMinX());
								t.setToY(gp.getTranslateY() + map.getHeight() / 2
										- gp.localToParent(n.getBoundsInParent()).getMinY());
								t.play();
							}
							RotateTransition rt = new RotateTransition(Duration.millis(1000), n);
							rt.setFromAngle(n.getRotate());
							rt.setToAngle(-rotation);
							rt.setAxis(Rotate.Z_AXIS);
							rt.play();
						} catch (Exception e) {
						}
					}
					if (Math.round(gp.getRotate()) != rotation) {
						RotateTransition rt = new RotateTransition(Duration.millis(1000), gp);
						rt.setFromAngle(gp.getRotate());
						rt.setToAngle(rotation);
						rt.setAxis(Rotate.Z_AXIS);

						final Node n = node;
						if (gp.getRotate() != rotation) {
							for (ChangeListener<Number> l : rotListen) {
								gp.rotateProperty().removeListener(l);
							}
							rotListen.clear();
							ChangeListener<Number> listener = new ChangeListener<Number>() {
								public void changed(ObservableValue<? extends Number> observable, Number oldValue,
										Number newValue) {
									gp.setTranslateX(gp.getTranslateX() + map.getWidth() / 2
											- gp.localToParent(n.getBoundsInParent()).getMinX());
									gp.setTranslateY(gp.getTranslateY() + map.getHeight() / 2
											- gp.localToParent(n.getBoundsInParent()).getMinY());
									for (Node n : gp.getChildren()) {
										if (gp.localToParent(n.getBoundsInParent()).getMinX() < 0
												|| gp.localToParent(n.getBoundsInParent()).getMinX() > map.getWidth()
												|| gp.localToParent(n.getBoundsInParent()).getMinY() < 0
												|| gp.localToParent(n.getBoundsInParent()).getMinY() > map
														.getHeight()) {
											n.setOpacity(0);
										} else {
											n.setOpacity(1);
										}
										gpTP[GridPane.getColumnIndex(n)][GridPane.getRowIndex(n)] = n.getOpacity();
									}
								}
							};
							gp.rotateProperty().addListener(listener);
							rotListen.add(listener);
						}
						rt.play();
					}
				}
			}
		});

		input.setFocusTraversable(false);
		Rectangle r = new Rectangle();
		r.setWidth(0);
		r.setFill(Color.TRANSPARENT);
		r.setStroke(Color.gray(0.8));
		r.setStrokeWidth(2);
		Line l1 = new Line(), l2 = new Line(), l3 = new Line(), l4 = new Line();
		l1.setStroke(Color.gray(0.07));
		l1.setStrokeWidth(4);
		l2.setStroke(Color.gray(0.07));
		l2.setStrokeWidth(4);
		l3.setStroke(Color.gray(0.07));
		l3.setStrokeWidth(4);
		l4.setStroke(Color.gray(0.07));
		l4.setStrokeWidth(4);
		r.widthProperty().addListener((o) -> {
			double minx = r.getBoundsInParent().getMinX();
			double miny = r.getBoundsInParent().getMinY();
			double maxx = r.getBoundsInParent().getMaxX();
			double maxy = r.getBoundsInParent().getMaxY();
			l1.setStartX(minx + 10);
			l1.setEndX(maxx - 10);
			l1.setStartY(miny);
			l1.setEndY(miny);
			l2.setStartX(minx);
			l2.setEndX(minx);
			l2.setStartY(miny + 10);
			l2.setEndY(maxy - 10);
			l3.setStartX(maxx);
			l3.setEndX(maxx);
			l3.setStartY(miny + 10);
			l3.setEndY(maxy - 10);
			l4.setStartX(minx + 10);
			l4.setEndX(maxx - 10);
			l4.setStartY(maxy);
			l4.setEndY(maxy);
			if (r.getWidth() <= 10) {
				r.setStroke(Color.TRANSPARENT);
			} else {
				r.setStroke(Color.gray(0.8));
			}
		});
		Thread thr1 = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Text t = new Text(input.getText());
					t.setFont(input.getFont());
					r.setWidth(t.getBoundsInLocal().getWidth() + 10);
					r.setHeight(input.getHeight());
				}
			}
		};
		thr1.start();
		input.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					Terminal.printing = false;
					s = input.getText();
					enterKeyPressed = true;
					input.clear();
				}

			}
		});

		input.setFont(Font.font("Futura", 20));
		input.setBackground(
				new Background(new BackgroundFill(Color.gray(0.07), new CornerRadii(20), new Insets(10, 10, 10, 10))));
		input.setPrefWidth(root.getWidth() - 20);

		Line l = new Line(root.getWidth() / 2, 20, root.getWidth() / 2, root.getHeight() - 20);
		l.setStrokeWidth(4d);
		l.setStroke(Color.gray(1));

		root.widthProperty().addListener((obs, oldVal, newVal) -> {
			stack.setPrefWidth(root.getWidth() / 2 - 20);
			map.setPrefWidth(root.getWidth() / 2 - 20);
			input.setPrefWidth(root.getWidth() - 40);
			l.setStartX(root.getWidth() / 2);
			l.setEndX(root.getWidth() / 2);
		});
		root.heightProperty().addListener((obs, oldVal, newVal) -> {
			l.setStartY(20);
			l.setEndY(root.getHeight() - 20);
		});

		AnchorPane.setBottomAnchor(input, 20d);
		AnchorPane.setBottomAnchor(r, 20d);
		AnchorPane.setLeftAnchor(input, 10d);
		AnchorPane.setLeftAnchor(r, 18d);
		AnchorPane.setLeftAnchor(stack, 15d);
		AnchorPane.setRightAnchor(map, 10d);
		root.getChildren().add(r);
		root.getChildren().add(input);
		root.getChildren().add(l1);
		root.getChildren().add(l2);
		root.getChildren().add(l3);
		root.getChildren().add(l4);
		root.getChildren().add(stack);
		root.getChildren().add(map);
		root.getChildren().add(l);

		Text text = new Text("Map");
		text.setFont(new Font(15));
		map.getChildren().add(text);
		map.getChildren().add(gp);
		map.setAlignment(Pos.CENTER);
		GridPane.setHgrow(gp, Priority.ALWAYS);
		GridPane.setVgrow(gp, Priority.ALWAYS);

		stack.heightProperty().addListener((obs, oldVal, newVal) -> {
			TranslateTransition tt = new TranslateTransition(Duration.seconds(1), stack);
			tt.setFromY(stack.getTranslateY());
			tt.setToY(root.getHeight() - stack.getHeight() - 60);
			tt.play();
			for (Node n : stack.getChildren()) {
				TextFlow flow = (TextFlow) n;
				for (Node n1 : flow.getChildren()) {
					try {
						Text t = (Text) n1;
						if (t.localToScene(t.getBoundsInLocal()).getMaxY() <= 250) {
							FadeTransition ft = new FadeTransition(Duration.millis(1000), t);
							if (t.localToScene(t.getBoundsInLocal()).getMaxY() <= 0) {
								ft.setFromValue(t.getOpacity());
								ft.setToValue(0);
								ft.play();
								continue;
							}
							ft.setFromValue(t.getOpacity());
							ft.setToValue((t.localToScene(t.getBoundsInLocal()).getMaxY() - 150) / 100);
							ft.play();
						} else {
							FadeTransition ft = new FadeTransition(Duration.millis(2000), t);
							ft.setFromValue(t.getOpacity());
							ft.setToValue(1.0);
							ft.play();
						}
					} catch (Exception e) {
					}
				}
			}
		});
	}
}
