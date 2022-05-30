package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

// @Authors Mohammed Kharbouch, Reda Merbah, Dorian Vabre

public class Main extends Application {


	/** The main controller of the application */
	private static MainController mc;
	
    @Override
    public void start(Stage primaryStage) {
        try {
        	// Using the Sample.fxml file for the display
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Sample.fxml"));
            fxmlLoader.setController(mc);
            Parent root = fxmlLoader.load();
            
            Scene scene = new Scene(root,1200,800);
            root.requestFocus();

            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            
    		Canvas canvas = (Canvas) scene.lookup("#canvas");
    		mc.setCanvas(canvas);
    		
    		GraphicsContext gc = canvas.getGraphicsContext2D();
    		mc.setGraphicsContext(gc);
    		
    		mc.setStage(primaryStage);
        	
            primaryStage.setScene(scene);
            //primaryStage.setMaximized(true);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) {
    	
    	// Redirecting the errors in a file
		File file = new File("err.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    	mc = new MainController();

    	// Launching the app
        launch(args);
    }
 }