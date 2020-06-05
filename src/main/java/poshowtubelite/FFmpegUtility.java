package main.java.poshowtubelite;

import java.io.File;
import java.io.IOException;

public class FFmpegUtility {

	protected String SETTINGS_FFMPEG_PATH = "";

	public FFmpegUtility() {
		SETTINGS_FFMPEG_PATH = Constants.getApplicationPath() + "/ffmpeg.exe"; // SET FFMPEG LOCATION HERE
	}

	public void getDetails() {
				
		Process process = null;
				
		System.out.println( "FFmpegUtility running test on:" + SETTINGS_FFMPEG_PATH);
		
		ProcessBuilder processBuilder = new ProcessBuilder(SETTINGS_FFMPEG_PATH);
        processBuilder.redirectErrorStream(true);
        
        File ffmpegOutputLog = new File(Constants.getApplicationPath() + "/ffmpeg.txt");
        
        processBuilder.redirectOutput(ffmpegOutputLog);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();           
            System.out.println( "FFmpegUtility getDetails running error" + e.getMessage());
        }

        if (process != null) {
            try {
                process.waitFor();
              
            } catch (InterruptedException e) {
                System.out.println( "FFmpegUtility getDetails closing error" + e.getMessage());
                e.printStackTrace();
            }            
        }
        
        System.out.println( "FFmpegUtility test finished, check details at:" + Constants.getApplicationPath() + "/ffmpeg.txt");
        
        processBuilder = null;
        process = null;											
	}
	

}
