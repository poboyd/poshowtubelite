package main.java.poshowtubelite;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import ddf.minim.*;
import main.java.poshowtubelite.VideoExport;
import main.java.poshowtubelite.FFmpegUtility;
import main.java.poshowtubelite.AudioFFT;

public class pro extends PApplet {
		
	float movieFPS = 30;
	float frameDuration = 1 / movieFPS;
	public int frameRate = 60;
	
	public VideoExport videoExport;
	
	public float bkRed = 200;
	public float bkGreen = 100;
	public float bkBlue = 50;
	
	boolean moving = false;
		
	Minim minim;	
	AudioFFT audioFFT = new AudioFFT();
	
	boolean loading = true;	
			
	public startUpController javaFXcontroller;		
							
	PGraphics pg;
		
	public PrintStream loggingPrintSystem;		
	
	public DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	
	public void setWindowLocation() {						
	   	surface.setLocation(600, 300);							
	}
	
	public pro() {			
	}
	
	public void setJavaFX(startUpController thisController){
		javaFXcontroller = thisController;
	}
	
	public void run(){
		String[] processingArgs = {"main.java.poshowtubelite.pro"};
		System.out.println("sketch.javaPlatform:" + javaPlatform + " sketch.javaVersionName:" + javaVersionName + "  PApplet.calcSketchPath():" + PApplet.calcSketchPath() );		
		PApplet.runSketch(processingArgs, this);		
	}		
	
	public void redrawSketch() {
		redraw();
	}		
	
	public void settings() {																
		size(800, 500, P3D);
	}		 	
	
	public void setup() {					

		pixelDensity(1); 
		
		background(bkRed, bkGreen, bkBlue,255);
				
		smooth();				
				
		// We need time for threads to catch up now that weve removed CP5 which took time to load
		int count= 0;
		while (javaFXcontroller==null){
			count++;
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {}
			
			if (count>20) {
				System.out.println("count should not get beyond 20, somthing has failed to load:" + count);
				break;
			}
		}
		System.out.println("count:" + count);				
		
		surface.setTitle("My PROCESSING WINDOW");	
		
		System.out.println("setting up minim");
		minim = new Minim(this);
		if(minim==null)
			System.out.println("error: setup mimim is null");
				
		System.out.println("setting up videoexport");
		videoExport = new VideoExport(this);
		
		if(videoExport==null)
			System.out.println("error: setup videoExport is null");			
		
		audioFFT = new AudioFFT(this, minim, Constants.getApplicationPath() + "/BobbyAlco.5.mp3");															
		audioFFT.audioToArray(); 
		audioFFT.soundArrayIndex=0;					 
		audioFFT.updateSongPosition();	
	}					
	
	public void draw() {
		background(bkRed, bkGreen, bkBlue,255);
				
		try {		
															
			if (audioFFT.isRecording) {						  				
							
				  if ( (audioFFT.soundArray.length - audioFFT.soundArrayIndex) < 50 || frameCount % 40 == 0 ){
					String recordDetails = "Video recording running. ";
					recordDetails += System.getProperty("line.separator") + "Remaining:" + (audioFFT.soundArray.length - audioFFT.soundArrayIndex);					
					javaFXcontroller.txtVideoDetails.setText(recordDetails);						
				  }
				  
				  if (audioFFT.soundArrayIndex >= audioFFT.soundArray.length) {
				     // Done reading the file, Close the video file.					 
					 endRecord();					
				     return;
				     
				  } else {
				    // The sound time in seconds.
				    float soundTime = audioFFT.soundArray[audioFFT.soundArrayIndex].timeInseconds;
	
				    audioFFT.setFFTValues();
				    
				    while (videoExport.getCurrentTime() < soundTime + frameDuration * 0.5) {				       
				     			
				    	// RUN COMMON SKETCH CODE HERE
				    	
					   if (!videoExport.saveFrame()) {						   						   						   							  							   							 							  						
						   noLoop();
						   audioFFT.song.pause();	
						   audioFFT.isRecording  = false;	
						   
						   videoExport.tidyFailed();							 
						   
						   return;								 							  
					   }
				    }
				    
				    audioFFT.soundArrayIndex++;
				  }
			}
			else
			{														
				audioFFT.updateSongPosition();  		
				audioFFT.setFFTValues();	
				
				 // RUN COMMON SKETCH CODE HERE
			}
			
			
		}
		catch(Exception exc)		{
			StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	exc.printStackTrace(pw);
	    	String sStackTrace = sw.toString(); // stack trace as a string
			System.out.println("Exception in draw:" + sStackTrace);     		
		}									
		
	}
	
	public void mouseClicked() {
		
		System.out.println("DEBUG: mouseClicked X:" + pmouseX + " Y:" + pmouseY);				
		
		javaFXcontroller.mousePosition(pmouseX, pmouseY);
		
		return;		
	}
		
	public void keyPressed() {
		
		System.out.println("DEBUG: Pressed:" + key);	
		
		javaFXcontroller.keyPressed(key + "");
		
		javaFXcontroller.println("Key Pressed:" + key);				
	}

	public void endRecord() {											
		
		audioFFT.isSavingRecording = true;
		noLoop();
		audioFFT.song.pause();				
		  						
		try {			
			TimeUnit.SECONDS.sleep(2);			
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
		videoExport.endMovie();
		
		try {
			TimeUnit.SECONDS.sleep(4);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
		videoExport.standaloneAttachSound();
		
		try {			
			TimeUnit.SECONDS.sleep(2);			
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
		audioFFT.isRecording  = false;	
				
		System.out.println("end of recording frameCount:" + frameCount);								
		
		
		if (audioFFT.song.isMuted()){
			audioFFT.song.unmute();				
		}
		
		redraw();
		
		audioFFT.isSavingRecording = false;
	}
	
	public void clickPlay() {		
		moving = false;
		
		if (audioFFT.isRecording) return;
		
		audioFFT.song.play(0);
		loop();
		audioFFT.song.play();			
	}
	
	public void clickPause() {	
		moving = false;			
		
		if (audioFFT.isSavingRecording)
			return;
		
		if (audioFFT.isRecording) {			
			endRecord();					
		}	
		
		noLoop();
		frameCount = 2;
		audioFFT.song.pause();
		redraw();
				
	}
	
	public void clickRecord() {	
				
		if (audioFFT.isSavingRecording)
			return;
		
		if (audioFFT.isRecording ) {			
			endRecord();					
			return;
		}							
		
		audioFFT.isSavingRecording = false;
		
		noLoop();
		audioFFT.song.mute();				
								
		try {			
			TimeUnit.SECONDS.sleep(2);			
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
						
		videoExport.reset();								
		videoExport.audioFilePath = Constants.getApplicationPath() + "/BobbyAlco.5.mp3";
		
		checkAudioAndStop();														
		
		audioFFT.song.play(0);
		audioFFT.soundArrayIndex=0;
				
		audioFFT.isRecording = true;
			
		FFmpegUtility ffmpegUtility = new FFmpegUtility(); 
		ffmpegUtility.getDetails();		
		try {			
			TimeUnit.SECONDS.sleep(2);			
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
						
		videoExport.setMovieFileName(Constants.getApplicationPath() + "/BobbyAlco.5.mp4");
		videoExport.startMovie();
								
		loop();		
	}	
	
	public void checkAudioAndStop() {
		if (minim!=null && audioFFT!=null && audioFFT.song!=null)
			audioFFT.song.pause();		
	}
}
