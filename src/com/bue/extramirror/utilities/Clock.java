package com.bue.extramirror.utilities;

public class Clock {
	/*private long hours, minutes, seconds;

	public Clock() {
		super();
		hours=0;
		minutes=0;
		seconds=0;
	}*/
	
	public static String convertTime(Long miliseconds){
		String time="";
		long seconds,minutes,hours;
        minutes=0;
        hours=0;
        seconds=miliseconds/1000;
		if(seconds>59){
			minutes=seconds/60;
			seconds=seconds%60;
			if(minutes>59){
				hours=minutes/60;
				minutes=minutes%60;
			}
		}
//		StringBuilder sb=new StringBuilder();
//		Formatter formatter =new Formatter(sb, Locale.US);
		time=String.format("%d:%02d:%02d", hours, minutes, seconds);
		//time=hours+":"+minutes+":"+seconds;
		
		return time;
				
	}
	

}
