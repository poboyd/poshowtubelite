package main.java.poshowtubelite;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;


public class startUpController implements Initializable {

	
	@FXML
	Label  labelMousePosition; 
	
	@FXML
	TextField txtKeyPressed;
	
	@FXML
	TextArea txtVideoDetails;
	
	public pro sketch;

	Application javafxApp;		
			
	private final Timer timer = new Timer();
	private TimerTask timerTask;
	
	public Random rand= new Random();

	List<String> errorMessages = new ArrayList<>();

	Stage controllerStage;
	
	@FXML
	Pane lockedPain;
		
	String findThisActor = "";
	
	boolean loading=true;
		
	@FXML
	public void exitApplication(ActionEvent event) {
	   Platform.exit();
	}
					
						
	public void setJavafxApp(Application thisjavafxApp) {
		javafxApp = thisjavafxApp;
	}
		
	public void setSketch(pro thissketch) {
		sketch = thissketch;	 							
	}

	public void setStage(Stage thisStage) {
		controllerStage = thisStage;
	}

	public void setConfig() {				
		loading=false;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {		
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		println("STARTED: " + formatter.format(LocalDateTime.now()));
		println("URL location: " + location);
																	
	}							
	
	public void buttonTest() {	
		sketch.bkBlue = rand.nextInt(254);
		sketch.bkGreen = rand.nextInt(254);
		sketch.bkRed = rand.nextInt(254);
	}
			
	public void keyPressed(String key) {	
		txtKeyPressed.setText(key);
	}
	
	public void clickRecord() {						
		sketch.clickRecord();
	}
	
	public void clickPlay() {
		sketch.clickPlay();
	}

	public void clickPause() {
		sketch.clickPause();
	}
	
	public void mousePosition(int pmouseX, int pmouseY) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				labelMousePosition.setText("mouse position is X:" + pmouseX + "  Y:" + pmouseY);				
			}
		});
	}	
	
	public void addButtonHoldEvents(Button thisButton, String methodName, Object obj) {
		thisButton.addEventHandler(MouseEvent.MOUSE_PRESSED, me -> {
			timerTask = new TimerTask() {
				@Override
				public void run() {

					Platform.runLater(() -> {
						java.lang.reflect.Method method = null;
						try {
							method = startUpController.class.getMethod(methodName);
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							method.invoke(obj);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}

			};
			timer.schedule(timerTask, 0L, 10L);
		});

		thisButton.addEventHandler(MouseEvent.MOUSE_RELEASED, me -> {
			timerTask.cancel();
			timerTask = null;
			timer.purge();
		});
	}
					
	public void println(String s) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				addWithLimit(errorMessages, s, 40);

				String fullText = "";
				for (int i = 0; i < errorMessages.size(); i++) {
					fullText = errorMessages.get(i) + "\n" + fullText;
				}
			
				System.out.println(s);
			}
		});
	}

	public void setName(String name) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				controllerStage.setTitle("My JAVAFX Window");
				
				controllerStage.heightProperty().addListener((obs, oldVal, newVal) -> {
					
					double oldV = (double) oldVal;
					double newV = (double) newVal;
					
					if (Math.abs( oldV - newV)  > 3)
					{  
					}
				});
			}
		});
	}	
			
	public static <T, C extends Collection<T>> void addWithLimit(C c, T itemToAdd, int limit) {
		List<T> list = new ArrayList<>(c);
		list.add(itemToAdd);
		while (list.size() > limit) {
			list.remove(0);
		}
		c.clear();
		c.addAll(list);
	}

}
