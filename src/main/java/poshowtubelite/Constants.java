package main.java.poshowtubelite;

import processing.core.PConstants;


public class Constants {   
  
  public static final String version = "1.0";
  
  public static String appName = "MyAppName";  
  
  public static final int audioBufferSize = 2048;
  
  public static String getApplicationPath() {	  
	  String applicationPath = System.getProperty("user.dir");
	  //System.out.println("application path:" + applicationPath );
	  return  applicationPath.replace("\\", "/");
  }   
   
  public static String getDataFolderPath() {
	  String dataFolderPath = System.getProperty("user.dir") + "/data";
	  //System.out.println("Data folder:" + dataFolderPath );
	  return  dataFolderPath.replace("\\", "/");
  }
  
  public static String getIconsFolderPath() {
	  String iconsFolderPath = System.getProperty("user.dir") + "/data/icons";
	  return  iconsFolderPath.replace("\\", "/");
  }  
    
  public static int getBlendMode(String blendMode) {
	  
		switch(blendMode)
		{
		   case "BLEND":			 			  									  
			   return PConstants.BLEND; 
		      
		   case "ADD":
			   return PConstants.ADD;
					   
		   case "SUBTRACT":
			   return PConstants.SUBTRACT;
					   
		   case "DARKEST":
			   return PConstants.DARKEST;
					   
		   case "LIGHTEST":
			   return PConstants.LIGHTEST;
					   
		   case "DIFFERENCE":
			   return PConstants.DIFFERENCE;			   

		   case "EXCLUSION":
			   return PConstants.EXCLUSION;
			   
		   case "MULTIPLY":
			   return PConstants.MULTIPLY;
					   
		   case "SCREEN":
			   return PConstants.SCREEN;
			   
		   case "REPLACE":
			   return PConstants.REPLACE;
			   
		   default :
			   return PConstants.BLEND;
		}
  }
  
  public static int getFilter(String filter) {
	  
		switch(filter)
		{
		   case "THRESHOLD":			 			  									  
			   return PConstants.THRESHOLD; 
		      
		   case "GRAY":
			   return PConstants.GRAY;
					   
		   case "OPAQUE":
			   return PConstants.OPAQUE;
					   
		   case "INVERT":
			   return PConstants.INVERT;
					   
		   case "POSTERIZE":
			   return PConstants.POSTERIZE;					   			  

		   case "ERODE":
			   return PConstants.ERODE;
			   
		   case "DILATE":
			   return PConstants.DILATE;					   		 
			   
		   case "NONE":
			   return -1;	
			   
		   default :
			   return -1;
		}
  }
  
   
  
  // FADES
  public static final int Fade_None = 0;
  public static final int Fade_In = 1;
  public static final int Fade_Out = 2;
  
  
  
    
}