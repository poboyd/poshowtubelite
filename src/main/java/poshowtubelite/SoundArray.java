package main.java.poshowtubelite;

public class SoundArray {	
	
	public float songPosition;
	public float timeInseconds;
			      
    public float[] lowFFTBandL;
    public float[] midFFTBandL;
    public float[] highFFTBandL;
  
    public float[] lowFFTBandAvgL;
    public float[] midFFTBandAvgL;
    public float[] highFFTBandAvgL;          
    
    public float[] bufferWaveLeft;
    public float[] bufferWaveRight;
          
    public String debugAsString() {
    	String result="";
    	
    	result += "songPosition:"+songPosition;
    	result += " ,timeInseconds:"+timeInseconds;    	
    	return result;
    }
}
