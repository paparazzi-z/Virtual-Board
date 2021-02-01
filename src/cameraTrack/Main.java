package cameraTrack;

import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

	
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		// Stage :
		primaryStage.setTitle("Virtual Board");
		
		// Using Scenebuilder to build the GUI (BorderPane, Buttons Layout..):
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainView.fxml"));
		AnchorPane mainLayout = loader.load();
		Scene scene = new Scene(mainLayout);

	
		primaryStage.setScene(scene);
		primaryStage.show();
		
		// Loading functions from MainController :
		MainController controller = loader.getController();


		

		
		// The default function to close the Stage :
		primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we)
			{
					controller.setClosed();
			}
		}));
	}	
	
	// To start a JavaFX application.
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
