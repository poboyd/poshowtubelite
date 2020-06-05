package main.java.poshowtubelite;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {

	public pro sketch;	
	public PrintStream loggingPrintSystem;
	public Scene scene;	
	
	@Override
	public void init() throws Exception {			
		
		sketch = new pro();
		sketch.run();		
	}	
			
	@Override
	public void start(Stage primaryStage) {
		try {								  												
			
			checkSystemSetup();
						
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startUp.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			scene = new Scene(root);		
								
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);			
			primaryStage.show();
			
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		    primaryStage.setX(  ((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2) -600 );
		    primaryStage.setY(  ((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2)   );		        		   
		        
			primaryStage.setResizable(true);			
			primaryStage.setMinWidth(200.0);
			primaryStage.setWidth(400.0);
			primaryStage.setMaxWidth(505.0);			
			primaryStage.setMinHeight(580.0);
			
			primaryStage.getIcons().add(new Image("file:" + Constants.getIconsFolderPath() + "/favicon.png"));
			
			startUpController controller = fxmlLoader.<startUpController>getController();
			controller.setSketch(sketch);
			controller.setStage(primaryStage);
											
			sketch.setJavaFX(controller);
																		
			controller.setConfig();
			controller.setJavafxApp(this);
												
			try {		
				sketch.setWindowLocation();
				
				scene.getWindow().setX( 200 );
				scene.getWindow().setY( 200 );	
				scene.getWindow().setHeight( 100 );	
								
			}
			catch(Exception exc) {
				System.out.println("Error getting fx screen settings at startup" + exc.getMessage());
			}									

			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}								
	
	@Override
	public void stop() {
		
		sketch.noLoop();
		
		try {												
			System.out.println("getWindow.getX:" + scene.getWindow().getX());
			System.out.println("getWindow.getY:" + scene.getWindow().getY());			
			System.out.println("scene.getWindow().getHeight():" + scene.getWindow().getHeight());
		}
		catch(Exception exc) {
			System.out.println("Error saving fx screen settings at shutdown" + exc.getMessage());
		}
		
		
		System.out.println("Stop");
		if (sketch != null) {
								
			try {
				com.jogamp.newt.opengl.GLWindow sketchSurface = (com.jogamp.newt.opengl.GLWindow)sketch.getSurface().getNative();											
				System.out.println("sketchSurface.getX:" + sketchSurface.getX());
				System.out.println("sketchSurface.getY:" + sketchSurface.getY());	
			}
			catch(Exception exc) {
				System.out.println("Error saving processing screen settings at shutdown" + exc.getMessage());
			}
												
			sketch.stop();
			sketch.exit();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}			
			
	public boolean checkFileExists(String filePathString)
	{					
		try
		{
			File f = new File(filePathString);
			if(f.exists() && !f.isDirectory()) { 
			    return true;
			}
		}
		catch (Exception e)		  {
			System.err.println("exception trying checkFileExists:" + e.getMessage() );
		}
						  	
		return false;	
	}
	
	public void checkSystemSetup()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();		
		System.out.println("Start running:" + dateFormat.format(date));			
		
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
				
		File f = new File(Constants.getDataFolderPath());
		if (f.exists() && f.isDirectory()) {
			System.out.println("data folder exists" );
		}
		else
			System.out.println("data folder DOES NOT exists" );
		
	}
	
}
