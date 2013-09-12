package com.bue.extramirror.utilities;

import java.text.DecimalFormat;

public class Utilities {
	public static String convertMetersToKhm(float distanceMeters, boolean isSpeed){
		String distanceKhm="";
		float distanceKhmNum=distanceMeters/1000;
		DecimalFormat df;
		if(isSpeed)
			df=new DecimalFormat("#");
		else
			df = new DecimalFormat("#.#");
		distanceKhm=df.format(distanceKhmNum);
		return distanceKhm;
	}
	
	public static float convertMetersToKhm(int distanceMeters){
		return distanceMeters/1000;
	}
	
	public static String convertMetersToMiles(float distanceMeters, boolean isSpeed){
		String distanceMiles="";
		float distanceMilesNum=distanceMeters*0.00062137f;
		DecimalFormat df;
		if(isSpeed)
			df=new DecimalFormat("#");
		else
			df = new DecimalFormat("#.#");
		distanceMiles=df.format(distanceMilesNum);
		return distanceMiles;
	}
	
	public static String convertKilometersToMiles(float distanceMeters, boolean isSpeed){
		String distanceMiles="";
		float distanceMilesNum=distanceMeters*0.62137f;
		DecimalFormat df;
		if(isSpeed)
			df=new DecimalFormat("#");
		else
			df = new DecimalFormat("#.#");
		distanceMiles=df.format(distanceMilesNum);
		return distanceMiles;
	}
	
	public static float convertMetersToMiles(float distanceMeters){
		return distanceMeters*0.00062137f;
	}
	
	public static float convertKilometersToMiles(float distanceMeters){
		return distanceMeters*0.62137f;
	}
	
	public static float convertMilesToKilometers(float distanceMiles){
		return distanceMiles*1.609344f;
	}
}
