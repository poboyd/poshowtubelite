package main.java.poshowtubelite;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import ddf.minim.*;
import ddf.minim.analysis.FFT;

import processing.core.PApplet;

public class AudioFFT {     
  public FFT fft;
  private Minim appMinim;
  public AudioPlayer song;     // used for playback as its buffering, as opposed to AudioSample which loads the whole sample/song into memory
 
  public int specSize = 0;
     
  public boolean isRecording= false;
  public boolean isSavingRecording= false;
  
  public boolean hideTransitions = false; // used to hide transitions on actors during playback
  
  public boolean enabled = true;
  public String songFile="";
  public float songPosition=0;  
  public int soundPosition=0; // used by text file FFT
  
  public float loudness=0;    

  public float specLow = (float) 0.08; // 8%
  public float specMid = (float) 0.20;  // 20%
  public float specHi = (float) 0.40;   // 40% 
         
  public SoundArray[] soundArray=new SoundArray[0];
  public int soundArrayIndex=0;

  public String selectedActorName = "";
  
  public String seperatorChar = "|";
   
  public final int minBandwidthPerOctave = 200;
  public final int bandsPerOctave = 10;
    
  AudioFFT(){
  }
  
  AudioFFT(PApplet p, Minim thisminim, String thisSongFile)
  {    
	  try {
		System.out.println("AudioFFT setting appMinim");		  		 
		  
		appMinim = thisminim;				
		        
		System.out.println("running new AudioFFT()");
		
		song = null;
		
		try {
			songFile = thisSongFile;
			PApplet.println("appMinim.loadFile:" + songFile);
			song = appMinim.loadFile(songFile);
		}catch(Exception exc){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exc.printStackTrace(pw);
			String sStackTrace = sw.toString(); 		        	   
			System.out.println("Exception running new AudioFFT():" + sStackTrace);          	        	
		}
		
		
		PApplet.println("new FFT song.bufferSize():" + song.bufferSize() + " song.sampleRate():" + song.sampleRate() );    
		
		fft = new FFT(song.bufferSize(), song.sampleRate());      
		PApplet.println("song.bufferSize():" + song.bufferSize() + " song.sampleRate():" + song.sampleRate() + " fft.specSize():" + fft.specSize());
    }
    catch(Exception exc){
    	StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
    	exc.printStackTrace(pw);
    	String sStackTrace = sw.toString();   	 
    	System.out.println("Exception running new AudioFFT():" + sStackTrace);    	    	    
    }
  }
  
  public void updateSongPosition(){
			 
    songPosition = song.position();
          
    int startingIndex=soundArrayIndex;
    
    if (soundArrayIndex>50)
    	startingIndex = soundArrayIndex -50; //was 150
    
    //int checkingCount=0;
    
    for (int i = startingIndex; i < soundArray.length; i++) {
    	//checkingCount++;
    	
    	if (  Math.abs( soundArray[i].songPosition - songPosition) < 0.01 ){ 
    		soundArrayIndex = i;    		    		    
    		//System.out.println("checkingCount:" + checkingCount + " soundArrayIndex:" + soundArrayIndex + "  soundArray[i].songPosition:" + soundArray[i].songPosition + " songPosition:" + songPosition + "  Math.abs:" + Math.abs( soundArray[i].songPosition - songPosition) );
    		break;
    	}   
    }
 
  }   
  
  public void moveSongToPosition(int position){        	  	  	
	  	  	  
	  for (int i = 0; i < soundArray.length; i++) {
		  
		  //System.out.println("i:" + i + "  current position:" + song.position() + "  soundArray[i].songPosition:" + soundArray[i].songPosition + "  position requested:" + position );
						
		  if ( Math.abs( soundArray[i].songPosition - position) < 60 ){
			
			song.cue( (int)soundArray[i].songPosition );
			soundArrayIndex = i;
			
			System.out.println( "moveSong requested position:" + position + "  actual position:" + soundArray[soundArrayIndex].songPosition );
			return;
		  }    	
	  }	        	     	    				 	  	
  }
   
  
  public void audioToArray() {
	  
    System.out.println("appMinim loading AudioSample:" + songFile);
	  
    AudioSample track = appMinim.loadSample(songFile, Constants.audioBufferSize);
  
    //1024; 512, 256, 128, 64, 32,16, 8, 2
    specSize = 1024;
    float sampleRate = track.sampleRate();
  
    System.out.println("sampleRate:" + sampleRate);    
    
    float[] fftSamplesL = new float[specSize];
    float[] fftSamplesR = new float[specSize];

    System.out.println("samplesL getChannel:" + AudioSample.LEFT);
    
    // THIS USED TO USE RIGHT BUT SOME FILES FAILED WITH BAD ERROR nummber
    float[] samplesL = track.getChannel(AudioSample.LEFT);
    float[] samplesR = track.getChannel(AudioSample.LEFT);  

    System.out.println("samplesL size:" + samplesL.length + " samplesR size:" + samplesR.length);
    
    FFT fftL = new FFT(specSize, sampleRate);
    FFT fftR = new FFT(specSize, sampleRate);
    
    System.out.println("new FFT specSize:" + specSize + " sampleRate:" + sampleRate);
      
    fftL.logAverages(minBandwidthPerOctave, bandsPerOctave);
    fftR.logAverages(minBandwidthPerOctave, bandsPerOctave);
    
    System.out.println("fftL.logAverages");
    
    int totalChunks = (samplesL.length / specSize) + 1;
    int fftSlices = fftL.avgSize();  
    
    soundArray = new SoundArray[totalChunks];
    soundArrayIndex = 0;      
    
    int bandwidthLow = (int) (fft.specSize() * specLow);    		    
    System.out.println("lowFFTBand 0 to " + bandwidthLow);
    
    int bandwidthMid = (int) (fft.specSize() * specMid);
    System.out.println("bandwidthMid " + bandwidthLow + " to " + (bandwidthMid+ bandwidthLow) );
    
    int bandwidthHigh = (int) (fft.specSize() * specHi);
    System.out.println("bandwidthHigh " + (bandwidthMid+ bandwidthLow) + " to " + (bandwidthMid+ bandwidthLow + bandwidthHigh ) );
          
    System.out.println("samplesL.length:" + samplesL.length  + " sampleRate:" + sampleRate + "  totalChunks:" + totalChunks + "  fftSlices:" + fftSlices );
           
              
    for (int ci = 0; ci < totalChunks; ++ci) {
      int chunkStartIndex = ci * specSize;   
      int chunkSize = PApplet.min( samplesL.length - chunkStartIndex, specSize );

      System.arraycopy( samplesL, chunkStartIndex, fftSamplesL, 0, chunkSize);      
      System.arraycopy( samplesR, chunkStartIndex, fftSamplesR, 0, chunkSize);  
      
      if ( chunkSize < specSize ) {
        java.util.Arrays.fill( fftSamplesL, chunkSize, fftSamplesL.length - 1, (float)0.0 );
        java.util.Arrays.fill( fftSamplesR, chunkSize, fftSamplesR.length - 1, (float)0.0 );
      }

      fftL.forward( fftSamplesL );
      fftR.forward( fftSamplesR );         
                 
      SoundArray chunkSoundArray = new SoundArray();       
                          
      //System.out.println( "fft.specSize():" + fft.specSize() + "  fftSamplesL:" + fftSamplesL.length);
      
      
      chunkSoundArray.bufferWaveLeft = new float[(int)(fftSamplesL.length/2)];
      int samplesIndex=0;      
      for (int i = 0; i < chunkSoundArray.bufferWaveLeft.length; i++){    	  
    	  chunkSoundArray.bufferWaveLeft[i] = (fftSamplesL[samplesIndex] + fftSamplesL[samplesIndex+1])/2;
    	  samplesIndex+=2;
      }

      chunkSoundArray.bufferWaveRight = new float[(int)(fftSamplesR.length/2)];
      samplesIndex=0;      
      for (int i = 0; i < chunkSoundArray.bufferWaveRight.length; i++){    	  
    	  chunkSoundArray.bufferWaveRight[i] = (fftSamplesR[samplesIndex] + fftSamplesR[samplesIndex+1])/2;
    	  samplesIndex+=2;
      }
                             
      chunkSoundArray.songPosition = Math.round((chunkStartIndex/sampleRate)*1000); // time in milliseconds
      chunkSoundArray.timeInseconds = Float.parseFloat(PApplet.nf(chunkStartIndex/sampleRate, 0, 3).replace(',', '.'));             
            
      soundArray[ci] = chunkSoundArray;          
          
    }
    track.close(); 
    System.out.println("Sound analysis done");
  }    
        
  public void setFFTValues()
  {   		  
	if (soundArrayIndex>=soundArray.length )
		return;
	
	songPosition = soundArray[soundArrayIndex].songPosition;    	
  }
  
  public void dumpSoundArrayAsText()
  {
	  try {
		  	BufferedWriter writer = new BufferedWriter(new FileWriter( System.getProperty("user.dir") + "/debugSoundArray.txt" )  );
			
			for (int i = 0; i < soundArray.length; i++){
				writer.write(soundArray[i].debugAsString() );
				writer.newLine();
			}						
			
			writer.close();
			
			
		} catch (Exception e) {
			System.err.println("exception writing data:" + e);
		}
  }
        
  
}
