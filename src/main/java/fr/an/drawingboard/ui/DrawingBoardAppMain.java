package fr.an.drawingboard.ui;

import fr.an.drawingboard.ui.impl.DrawingBoardUi;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DrawingBoardAppMain extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		DrawingBoardUi ui = new DrawingBoardUi();
		primaryStage.setTitle("Drawing Board");
		primaryStage.setScene(new Scene(ui.getUi()));
		primaryStage.show();
	}

}
