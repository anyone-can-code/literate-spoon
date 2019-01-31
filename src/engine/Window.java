package engine;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

import javafx.scene.control.Button;

import java.awt.RenderingHints.Key;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;

@SuppressWarnings("restriction")
public class Window extends Application {
	public static volatile boolean enterKeyPressed = false;
	public static String s = "";
	public static AnchorPane root = new AnchorPane();
	public static GridPane gp = new GridPane();
	public static volatile VBox stack = new VBox();
	public static volatile VBox map = new VBox();
	public static volatile String enterStack = "";
	public static PrintWriter out;
	public static Scanner in;
	public static int clientNumber;

	public static void main(String args[]) {
		launch(args);
	}

	Scene scene = new Scene(root, 600, 400);
	public static AnchorPane priorRoot = new AnchorPane();
	Scene priorScene = new Scene(priorRoot, 600, 400);

	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(priorScene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		Button b1 = new Button("Join");
		b1.setPrefWidth(100);
		Button b2 = new Button("Host");
		b2.setPrefWidth(100);
		
		Label label = new Label("Literate Spoon");
		label.setFont(Font.font("Helvetica", 30));
		priorRoot.getChildren().add(label);
		label.setTranslateX(priorScene.getWidth()/2 - 100);
		label.setTranslateY(priorScene.getHeight()/2 - 10);
		b1.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				priorRoot.getChildren().clear();
				TextField input = new TextField();
				priorRoot.getChildren().add(input);
				input.setFocusTraversable(false);
				input.prefWidth(100);
				input.prefHeight(30);
				input.setPromptText("IP number here");
				input.setTranslateX(priorScene.getWidth()/2 - 75);
				input.setTranslateY(priorScene.getHeight()/2 - input.getPrefHeight()/2 - 15);
				TextField input2 = new TextField();
				priorRoot.getChildren().add(input2);
				input2.setFocusTraversable(false);
				input2.prefWidth(100);
				input2.prefHeight(30);
				input2.setPromptText("Port number here");
				input2.setTranslateX(priorScene.getWidth()/2 - 75);
				input2.setTranslateY(priorScene.getHeight()/2 - input.getPrefHeight()/2 + 15);
				input2.setOnKeyReleased(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {
							try {
								int i = Integer.parseInt(input2.getText());
								String serverName = input.getText();
								int port = i;
								try {
									System.out.println("Connecting to " + serverName + " on port " + port);
									Socket server = new Socket(serverName, port);
									System.out.println("Just connected to " + server.getRemoteSocketAddress());
									out = new PrintWriter(server.getOutputStream(), true);

									InputStream inFromServer = server.getInputStream();
									in = new Scanner(inFromServer);
									clientNumber = Integer.parseInt(in.nextLine());
									Thread t = new Thread() {
										public void run() {
											while (true) {
												String s = in.nextLine();
												if (s != "" && Terminal.printing) {
													try {
														int i = Integer.parseInt(s.substring(0, 1));
														String[] strs = s.split(",");
														Platform.runLater(() -> Window.gp.add(new Label(strs[2]),
																Integer.parseInt(strs[0]), Integer.parseInt(strs[1])));
													} catch (Exception e) {
														if (s.contains("CLEARMAP")) {
															Platform.runLater(() -> Window.gp.getChildren().clear());
														} else if (s.contains("[PRINT]")) {
															Terminal.print(s.replace("[PRINT]", ""));
														} else if (s.contains("[PRINTLN]")) {
															Terminal.println(s.replace("[PRINTLN]", ""));
														}
													}
												}
											}
										}
									};
									t.start();
									primaryStage.setTitle("Text Adventure: Client " + clientNumber);
									primaryStage.setScene(scene);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
			}

		});
		b2.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				priorRoot.getChildren().clear();
				TextField input = new TextField();
				priorRoot.getChildren().add(input);
				input.setFocusTraversable(false);
				input.prefWidth(100);
				input.prefHeight(30);
				input.setPromptText("Port number here");
				input.setTranslateX(priorScene.getWidth()/2 - 75);
				input.setTranslateY(priorScene.getHeight()/2 - input.getPrefHeight()/2);
				input.setOnKeyReleased(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
						if (ke.getCode().equals(KeyCode.ENTER)) {
							String serverName = "localhost";
							try {
								serverName = Inet4Address.getLocalHost().getHostAddress();
							} catch (UnknownHostException e1) {
								e1.printStackTrace();
							}
							int port = Integer.parseInt(input.getText());
							Thread thr = new Thread() {
								public void run() {
									Server.main(new String[] { input.getText() });
								}
							};
							thr.start();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e2) {
								e2.printStackTrace();
							}
							try {
								System.out.println("Connecting to " + serverName + " on port " + port);
								Socket server = new Socket(serverName, port);
								System.out.println("Just connected to " + server.getRemoteSocketAddress());
								out = new PrintWriter(server.getOutputStream(), true);

								InputStream inFromServer = server.getInputStream();
								in = new Scanner(inFromServer);
								clientNumber = Integer.parseInt(in.nextLine());
								Thread t = new Thread() {
									public void run() {
										while (true) {
											String s = in.nextLine();
											if (s != "" && Terminal.printing) {
												try {
													int i = Integer.parseInt(s.substring(0, 1));
													String[] strs = s.split(",");
													Platform.runLater(() -> Window.gp.add(new Label(strs[2]),
															Integer.parseInt(strs[0]), Integer.parseInt(strs[1])));
												} catch (Exception e) {
													if (s.contains("CLEARMAP")) {
														Platform.runLater(() -> Window.gp.getChildren().clear());
													} else if (s.contains("[PRINT]")) {
														Terminal.print(s.replace("[PRINT]", ""));
													} else if (s.contains("[PRINTLN]")) {
														Terminal.println(s.replace("[PRINTLN]", ""));
													}
												}
											}
										}
									}
								};
								t.start();
								primaryStage.setTitle("Text Adventure: Client " + clientNumber + ", Hosting at " + serverName);
								primaryStage.setScene(scene);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}
		});
		priorRoot.setPrefWidth(100);
		priorRoot.setPrefHeight(500);
		priorRoot.getChildren().add(b1);
		b1.setTranslateX(scene.getWidth() / 2 - b1.getPrefWidth() / 2);
		AnchorPane.setTopAnchor(b1, 100.0);
		priorRoot.getChildren().add(b2);
		b2.setTranslateX(scene.getWidth() / 2 - b2.getPrefWidth() / 2);
		AnchorPane.setBottomAnchor(b2, 100.0);
		

		
		stack.setPadding(new Insets(10, 10, 10, 10));
		stack.setPrefWidth(root.getWidth() / 2 - 20);
		map.setPadding(new Insets(10, 10, 10, 10));
		map.setPrefWidth(root.getWidth() / 2 - 20);
		TextField input = new TextField();
		input.setFocusTraversable(false);
		input.setPromptText("Insert action here");
		input.setBackground(Background.EMPTY);
		input.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					Terminal.printing = false;
					s = input.getText();
					enterKeyPressed = true;
					input.clear();
				}
			}
		});
		input.setPrefWidth(root.getWidth() - 20);
		input.setPrefHeight(30d);
		Line l = new Line(root.getWidth() / 2, 20, root.getWidth() / 2, root.getHeight() - 20);
		l.setStrokeWidth(2d);
		l.setStroke(Paint.valueOf("#DDDDDD"));
		root.getChildren().add(l);
		root.widthProperty().addListener((obs, oldVal, newVal) -> {
			stack.setPrefWidth(root.getWidth() / 2 - 20);
			map.setPrefWidth(root.getWidth() / 2 - 20);
			input.setPrefWidth(root.getWidth() - 20);
			l.setStartX(root.getWidth() / 2);
			l.setEndX(root.getWidth() / 2);

		});
		root.heightProperty().addListener((obs, oldVal, newVal) -> {
			l.setStartY(20);
			l.setEndY(root.getHeight() - 20);
		});
		AnchorPane.setBottomAnchor(input, 10d);
		AnchorPane.setLeftAnchor(input, 10d);
		AnchorPane.setLeftAnchor(stack, 10d);
		AnchorPane.setRightAnchor(map, 10d);
		root.getChildren().add(input);
		root.getChildren().add(stack);
		root.getChildren().add(map);
		Text text = new Text("Map");
		text.setFont(new Font(15));
		map.getChildren().add(text);
		map.getChildren().add(gp);
		gp.setPrefHeight(root.getHeight());
		map.setAlignment(Pos.CENTER);
		gp.setAlignment(Pos.CENTER);

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
							FadeTransition ft = new FadeTransition(Duration.millis(2000), t);
							if (t.localToScene(t.getBoundsInLocal()).getMaxY() <= 0) {
								ft.setFromValue(t.getOpacity());
								ft.setToValue(0);
								ft.play();
								continue;
							}
							ft.setFromValue(t.getOpacity());
							ft.setToValue((t.localToScene(t.getBoundsInLocal()).getMaxY() - 50) / 200);
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
		new Terminal();

		Thread t = new Thread() {
			public void run() {
				while (true) {
					Terminal.readln();
				}
			}
		};
		t.start();
	}

}
