package com.bue.extramirror.utilities;

public interface ExtraMirrorSharedPreferences {
	public final String PREFS_NAME="ExtraMirrorSharedPreferences";
	public final String PREFS_MEASURE_UNIT="MeasureUnit";
	public final String PREFS_ENERGY_SAVING="EnergySaving";
	public final String PREFS_BRIGHTNESS="Brightness";
	public final String PREFS_SPEEDINDICATOR="SpeedIndicator";
	public final String PREFS_SHOW_INTRO="ShowIntro";
	public final String PREFS_ACTIVE_CAMERA="LastActiveCamera";
    public final String PREFS_CAMERA_ENABLED="isCameraEnabled";
	//public final String PREFS_PAUSED_ACTIVITY="PausedActivity";
	
	public final String STATE_PAUSED_AT_MAIN="PausedAtMain";

    //Intent extras
    public final String EXTRA_INTRO_ORINTATION="introOrientation";
	public final int PAUSED_AT_INTRO=0;
	public final int PAUSED_AT_EXTRA_MIRROR=1;
}
