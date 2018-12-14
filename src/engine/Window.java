package engine;


import java.awt.event.ActionListener;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.HPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;

public class Window extends Application {
	public static volatile boolean enterKeyPressed = false;
	public static String s = "";
	public static AnchorPane root = new AnchorPane();
	public static GridPane gp = new GridPane();
	public static volatile VBox stack = new VBox();

	public static volatile VBox map = new VBox();
	public static SequentialTransition t = new SequentialTransition();

	
	@SuppressWarnings("restriction")
	public static void main(String args[]) {
		launch(args);
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {
	
		Scene scene = new Scene(root, 600, 400);
		primaryStage.setTitle("Text Adventure");
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		stack.setPadding(new Insets(10, 10, 10, 10));
		stack.setPrefWidth(root.getWidth()/2 - 20);
		map.setPadding(new Insets(10, 10, 10, 10));
		map.setPrefWidth(root.getWidth()/2 - 20);
		TextField input = new TextField();
		input.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					s = input.getText();
					enterKeyPressed = true;
					input.clear();
				}
			}
		});
		input.setPrefWidth(root.getWidth() - 20);
		input.setPrefHeight(30d);
		Line l = new Line(root.getWidth()/2, 20, root.getWidth()/2, root.getHeight() - 20);
		l.setStrokeWidth(2d);
		l.setStroke(Paint.valueOf("#DDDDDD"));
		root.getChildren().add(l);
		root.widthProperty().addListener((obs, oldVal, newVal) -> {
			stack.setPrefWidth(root.getWidth()/2 - 20);
			map.setPrefWidth(root.getWidth()/2 - 20);
			input.setPrefWidth(root.getWidth() - 20);
			l.setStartX(root.getWidth()/2);
			l.setEndX(root.getWidth()/2);
			
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
		map.setAlignment(Pos.CENTER);
		GridPane.setHgrow(gp, Priority.ALWAYS);
		GridPane.setVgrow(gp, Priority.ALWAYS);
		stack.heightProperty().addListener((obs, oldVal, newVal) -> {
			TranslateTransition tt = new TranslateTransition(Duration.seconds(1), stack);
			tt.setFromY(stack.getTranslateY());
			tt.setToY(root.getHeight() - stack.getHeight() - 30);
			tt.play();
		});
		new Terminal();
		Main m = new Main();
		m.start();
	}

}
