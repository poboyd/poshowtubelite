package main.java.poshowtubelite;

/**
 * Video Export
 * Simple video file exporter.
 * https://funprogramming.org/VideoExport-for-Processing
 * <p>
 * Copyright (c) 2017 Abe Pazos http://hamoid.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Abe Pazos http://hamoid.com
 * @modified 02/28/2018
 * @version 0.2.3 (23)
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Wincon;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.StringList;


public class VideoExport {

    public final static String VERSION = "##library.prettyVersion##";
    protected String SETTINGS_FFMPEG_PATH = "";        
    protected final static String SETTINGS_CMD_ENCODE_VIDEO = "encode_video";
    protected final static String SETTINGS_CMD_ENCODE_AUDIO = "encode_audio"; 
    protected final static String SETTINGS_CMD_ENCODE_VIDEO_AUDIO_URL = "encode_video_audio_url";
    protected static JSONArray CMD_ENCODE_AUDIO_DEFAULT;
    protected static JSONArray CMD_ENCODE_VIDEO_DEFAULT;
    protected static JSONArray CMD_ENCODE_VIDEO_AUDIO_URL_DEFAULT;
    protected final String ffmpegMetadataComment = "Made with ffmpeg";
    protected ProcessBuilder processBuilder;
    protected Process process;
    protected byte[] pixelsByte = null;
    protected int frameCount;
    protected boolean loadPixelsEnabled = true;
    protected boolean saveDebugInfo = true;
    protected String outputFilePath;
    protected String audioFilePath;
    protected String audioUrl = null;
    protected PImage img;
    protected PApplet parent;
    protected int ffmpegCrfQuality;
    protected int ffmpegAudioBitRate;
    protected float ffmpegFrameRate;
    protected boolean ffmpegFound = false;
    protected File ffmpegOutputLog;
    protected OutputStream ffmpeg;
    protected JSONObject settings;
    protected String settingsPath;

    public boolean errorHappened = false;
    public String lastRecordingError = "";
    
    protected ArrayList<String> videoMsgs = new  ArrayList<String>();
    
    public ArrayList<String> getVideoMsgs() {
		return videoMsgs;
	}
	
    public VideoExport(PApplet parent) {
        this(parent, "processing-movie.mp4", parent.g);
    }
   
    public VideoExport(PApplet parent, final String outputFileName, PImage img) {

        parent.registerMethod("dispose", this);
            
        errorHappened = false;
        
        this.parent = parent;
        this.img = img;

        reset();
        
        outputFilePath = parent.sketchPath(outputFileName);
    }
    
    public void reset() {
     	
         CMD_ENCODE_VIDEO_DEFAULT = new JSONArray(new StringList(
                 new String[]{
                         "[ffmpeg]",                       // ffmpeg executable
                         "-y",                             // overwrite old file
                         "-f", "rawvideo",                 // format rgb raw
                         "-vcodec", "rawvideo",            // in codec rgb raw
                         "-s", "[width]x[height]",         // size
                         "-pix_fmt", "rgb24",              // pix format rgb24
                         "-r", "[fps]",                    // frame rate
                         "-i", "-",                        // pipe input
                         "-an",                            // no audio
                         "-vcodec", "h264",                // out codec h264
                         "-pix_fmt", "yuv420p",            // color space yuv420p
                         "-crf", "[crf]",                  // quality
                         "-metadata", "comment=[comment]", // comment
                         "[output]"                        // output file
                 }));

         CMD_ENCODE_VIDEO_AUDIO_URL_DEFAULT = new JSONArray(new StringList(
                 new String[]{
                         "[ffmpeg]",                       // ffmpeg executable
                         "-y",                             // overwrite old file
                         "-f", "rawvideo",                 // format rgb raw
                         "-vcodec", "rawvideo",            // in codec rgb raw
                         "-s", "[width]x[height]",         // size
                         "-pix_fmt", "rgb24",              // pix format rgb24
                         "-r", "[fps]",                    // frame rate
                         "-i", "-",                        // pipe input
                         "-i", "[url]",                    // to get audio URL
                         "-acodec", "aac",                 // audio codec
                         "-vcodec", "h264",                // out codec h264
                         "-pix_fmt", "yuv420p",            // color space yuv420p
                         "-crf", "[crf]",                  // quality
                         "-strict", "experimental",        // audio url won't work without this 
                         "-metadata", "comment=[comment]", // comment
                         "[output]"                        // output file
                 }));        
         
         CMD_ENCODE_AUDIO_DEFAULT = new JSONArray(new StringList(
                 new String[]{
                         "[ffmpeg]",                       // ffmpeg executable
                         "-y",                             // overwrite old file
                         "-i", "[inputvideo]",             // video file path
                         "-i", "[inputaudio]",             // audio file path
                         "-filter_complex", "[1:0]apad",   // pad with silence
                         "-shortest",                      // match shortest file
                         "-vcodec", "copy",                // don't reencode vid
                         "-acodec", "aac",                 // aac audio encoding
                         "-b:a", "[bitrate]k",             // bit rate (quality)
                         "-metadata", "comment=[comment]", // comment                        
                         "-strict", "-2",                  // enable aac
                         "[output]"                        // output file
                 }));
                             
         settings = new JSONObject();
      
         settings.setJSONArray(SETTINGS_CMD_ENCODE_VIDEO, CMD_ENCODE_VIDEO_DEFAULT);
         
         settings.setJSONArray(SETTINGS_CMD_ENCODE_AUDIO, CMD_ENCODE_AUDIO_DEFAULT);
         
         settings.setJSONArray(SETTINGS_CMD_ENCODE_VIDEO_AUDIO_URL, CMD_ENCODE_VIDEO_AUDIO_URL_DEFAULT);                                                         

         //System.out.println("FFMPEG settings:" + settings.toString());
                
         ffmpegFrameRate = 30f;
         ffmpegCrfQuality = 15; // default 15
         ffmpegAudioBitRate = 128;                
    }
    
  
    public void setMovieFileName(final String newMovieFileName) {
        outputFilePath = newMovieFileName;
    }

    public String getMovieFileName() {
        return outputFilePath;
    }
      
    public void setAudioFileName(final String audioFileName) {
        if (audioUrl != null) {
            System.err.println("Can't setAudioFileName() after setAudioUrl()!");
            return;
        }
        audioFilePath = audioFileName;
    }
    
    public String getAudioFileName() {
    	    	
        return audioFilePath==null || audioFilePath.isEmpty() ? audioUrl : audioFilePath;
    }
            
    public String getFrameRate() {
        return Float.toString(ffmpegFrameRate);
    }          
 
    public boolean saveFrame() {
        if (img != null && img.width > 0) {
            if (!ffmpegFound) {
                return false;
            }
            
            if (pixelsByte == null) {
                pixelsByte = new byte[img.pixelWidth * img.pixelHeight * 3];
            }
            if (loadPixelsEnabled) {
                img.loadPixels();
            }

            int byteNum = 0;
            for (final int px : img.pixels) {
                pixelsByte[byteNum++] = (byte) (px >> 16);
                pixelsByte[byteNum++] = (byte) (px >> 8);
                pixelsByte[byteNum++] = (byte) (px);
            }

            try {
            	if (ffmpeg!=null && pixelsByte!=null)
            		ffmpeg.write(pixelsByte);
                frameCount++;
                return true;
            } catch (Exception e) {
            	errorHappened = true;
                //e.printStackTrace();
                err(ffmpegOutputLog);               
                lastRecordingError = "Ffmpeg failed:" + e.getMessage();
                return false;
            }
        }
        
        return false;
    }
   
    protected void initialize() {         	
    	try {
    		lastRecordingError = "";
    		errorHappened = false;
    		 
    		tidyFailed();
    		
    		startFfmpeg(SETTINGS_FFMPEG_PATH);
    		
    		
        } catch (Exception e) {
            e.printStackTrace();
              err(ffmpegOutputLog);
        }    	         	                                                                  
    }
        
    public float getCurrentTime() {
        return frameCount / ffmpegFrameRate;
    }

    protected void startFfmpeg(String executable) {       
    	System.out.println("FFMPEG startFfmpeg executable:" + executable);
    	
        if (img.pixelWidth == 0 || img.pixelHeight == 0) {
            err("The export image size is 0!");
        }

        if (img.pixelWidth % 2 == 1 || img.pixelHeight % 2 == 1) {
            err("Width and height can only be even numbers when using the h264 encoder\n"
                    + "but the requested image size is " + img.pixelWidth + "x"
                    + img.pixelHeight);
        }
      
        JSONArray cmd;
      
        String cmdSelection = SETTINGS_CMD_ENCODE_VIDEO;
        if (audioUrl != null) {
        	cmdSelection = SETTINGS_CMD_ENCODE_VIDEO_AUDIO_URL;
        }
        try {
            cmd = settings.getJSONArray(cmdSelection);
        } catch (RuntimeException e) {
            cmd = CMD_ENCODE_VIDEO_DEFAULT;
        }
        
        System.out.println("FFMPEG startFfmpeg command:" + cmd.toString());
        
        String processBuilderString = "";
        
        // JSONArray -> String[]
        String[] cmdArgs = cmd.getStringArray();
       
        for (int i = 0; i < cmdArgs.length; i++) {
            if (cmdArgs[i].contains("[")) {
                cmdArgs[i] = cmdArgs[i]
                        .replace("[ffmpeg]", executable)
                        .replace("[width]", "" + img.pixelWidth)
                        .replace("[height]", "" + img.pixelHeight)
                        .replace("[fps]", "" + ffmpegFrameRate)
                        .replace("[url]", "" + audioUrl)
                        .replace("[crf]", "" + ffmpegCrfQuality)
                        .replace("[comment]", ffmpegMetadataComment)
                        .replace("[output]", outputFilePath);
                
                processBuilderString += " " + cmdArgs[i];
                //System.out.println("FFMPEG startFfmpeg cmdArgs:" + i + " ARG:" + cmdArgs[i]);
            }

        }
        
        System.out.println("FFMPEG startFfmpeg cmdArgs:" + processBuilderString);
        
        //System.out.println("FFMPEG startFfmpeg cmdArgs to string:" + cmdArgs.toString());
        
        processBuilder = new ProcessBuilder(cmdArgs);
        processBuilder.redirectErrorStream(true);
        ffmpegOutputLog = new File( Constants.getApplicationPath() + "/ffmpeg.txt");
        processBuilder.redirectOutput(ffmpegOutputLog);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        try {
            process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            err(ffmpegOutputLog);
        }

        ffmpeg = process.getOutputStream();
        ffmpegFound = true;
        frameCount = 0;
    }

    public void startMovie() {
    	SETTINGS_FFMPEG_PATH = Constants.getApplicationPath() + "/ffmpeg.exe";
    	System.out.println("Running ffmpeg at:" + SETTINGS_FFMPEG_PATH);
        initialize();        
    }
    
    public void tidyFailed() {
    	if (process != null) {
    		 try {                

                 if (PApplet.platform == PConstants.WINDOWS) {
                   
                     ProcessBuilder ps = new ProcessBuilder("tasklist");
                     Process pr = ps.start();

                   
                     BufferedReader allProcesses = new BufferedReader(
                             new InputStreamReader(pr.getInputStream()));
                   
                     Pattern isFfmpeg = Pattern
                             .compile("ffmpeg\\.exe.*?([0-9]+)");
                     String processDetails;
                   
                     while ((processDetails = allProcesses.readLine()) != null) {
                         Matcher m = isFfmpeg.matcher(processDetails);
                         if (m.find()) {
                             Wincon wincon = Kernel32.INSTANCE;
                             wincon.GenerateConsoleCtrlEvent(Wincon.CTRL_C_EVENT,
                                     Integer.parseInt(m.group(1)));
                             break;
                         }
                     }
                 } else {
                 	Thread.sleep(500);
                    process.destroy();
                 }
                 
                 if(!process.waitFor(20, TimeUnit.SECONDS)) {                    
                 	process.destroy();
                 }
    		 
	    	} catch (InterruptedException e) {
	            PApplet.println("Waiting for ffmpeg timed out!");
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
    	}    	
    }
      
    public void endMovie() {
        if (ffmpeg != null) {
            try {
                ffmpeg.flush();
                ffmpeg.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ffmpeg = null;
        }
        if (process != null) {
            try {              

                if (PApplet.platform == PConstants.WINDOWS) {
                   
                    ProcessBuilder ps = new ProcessBuilder("tasklist");
                    Process pr = ps.start();
                   
                    BufferedReader allProcesses = new BufferedReader(
                            new InputStreamReader(pr.getInputStream()));
                   
                    Pattern isFfmpeg = Pattern
                            .compile("ffmpeg\\.exe.*?([0-9]+)");
                    String processDetails;

                    while ((processDetails = allProcesses.readLine()) != null) {
                        Matcher m = isFfmpeg.matcher(processDetails);
                        if (m.find()) {
                            Wincon wincon = Kernel32.INSTANCE;
                            wincon.GenerateConsoleCtrlEvent(Wincon.CTRL_C_EVENT,
                                    Integer.parseInt(m.group(1)));
                            break;
                        }
                    }
                } else {
                	Thread.sleep(500);
                    process.destroy();
                }
                
                if(!process.waitFor(20, TimeUnit.SECONDS)) {                    
                	process.destroy();
                }
                
                TimeUnit.SECONDS.sleep(2);
              
                if (!saveDebugInfo && ffmpegOutputLog.isFile()) {
                    ffmpegOutputLog.delete();
                    ffmpegOutputLog = null;
                }

                PApplet.println(outputFilePath, "saved.");
            } catch (InterruptedException e) {
                PApplet.println("Waiting for ffmpeg timed out!");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            processBuilder = null;
            process = null;
            
        }
    }
       
    public void standaloneAttachSound() {      
       
        File audioPath = new File(audioFilePath);
        if (!audioPath.exists() || !audioPath.isFile()) {
            System.err.println("The file " + audioFilePath
                    + " was not found or is not a regular file.");
            return;
        }
        
        JSONArray cmd;
        try {
            cmd = settings.getJSONArray(SETTINGS_CMD_ENCODE_AUDIO);
        } catch (RuntimeException e) {
            cmd = CMD_ENCODE_AUDIO_DEFAULT;
        }       
        String[] cmdArgs = cmd.getStringArray();

        String tmpAudioFile = "temp-with-audio.mp4";
        
        videoMsgs = new ArrayList<String>();
        videoMsgs.add("ffmpeg: " + SETTINGS_FFMPEG_PATH);
        videoMsgs.add("inputaudio: " + audioFilePath);
        videoMsgs.add("bitrate:" + ffmpegAudioBitRate);        
        
        String processBuilderString = "";
               
        for (int i = 0; i < cmdArgs.length; i++) {
            if (cmdArgs[i].contains("[")) {
                cmdArgs[i] = cmdArgs[i]
                        .replace("[ffmpeg]", SETTINGS_FFMPEG_PATH)
                        .replace("[inputvideo]", outputFilePath)
                        .replace("[inputaudio]", audioFilePath)
                        .replace("[bitrate]", "" + ffmpegAudioBitRate)
                        .replace("[comment]", ffmpegMetadataComment)
                        .replace("[output]", parent.sketchFile(tmpAudioFile).getAbsolutePath());
                
                processBuilderString += " " + cmdArgs[i];
                //System.out.println("FFMPEG attachSound i:" + i + " ARG:" + cmdArgs[i]);
            }
        }       
        
        System.out.println("FFMPEG attachSound cmdArgs:" + processBuilderString);
        
        processBuilder = new ProcessBuilder(cmdArgs);
        processBuilder.redirectErrorStream(true);
        File ffmpegOutputLogAudio = new File(parent.sketchPath("ffmpeg-audio.txt"));
        videoMsgs.add("ffmpeg Log: " + ffmpegOutputLogAudio.getAbsolutePath());
        
        processBuilder.redirectOutput(ffmpegOutputLogAudio);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            err(ffmpegOutputLogAudio);
        }

        if (process != null) {
            try {               
                process.waitFor();
                new File(outputFilePath).delete();
                parent.sketchFile(tmpAudioFile).renameTo(new File(outputFilePath));
                videoMsgs.add("outputFilePath: " + outputFilePath);
            } catch (InterruptedException e) {
                PApplet.println( "Waiting for ffmpeg while adding audio timed out!");
                e.printStackTrace();
            }
        }
        if (!saveDebugInfo && ffmpegOutputLogAudio.isFile()) {
            ffmpegOutputLogAudio.delete();
        }
        processBuilder = null;
        process = null;

    }

    public void dispose() {
        endMovie();
    }
   
    protected void err(String msg) {
        System.err.println("\nVideoExport error: " + msg + "\n");       
    }

    protected void err(File f) {
        err("Ffmpeg failed. Study " + f + " for more details.");
    }

}
