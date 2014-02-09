package com.bue.extramirror.customviews;

import com.bue.extramirror.R;
import com.bue.extramirror.R.array;
import com.bue.extramirror.R.id;
import com.bue.extramirror.R.layout;
import com.bue.extramirror.R.string;
import com.bue.extramirror.utilities.ExtraMirrorSharedPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;


public class SettingsDialogFragment extends DialogFragment implements ExtraMirrorSharedPreferences {




    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SettingsDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    private SettingsDialogListener mListener;
    
    private int measureUnit;//0 means metric, 1 means imperial
	private boolean energySaving;
	private float brightness;
	private int speedometerIndicatorMode;
	private boolean showIntro;
    private boolean isCameraEnabled;
	
	private Spinner measureUnitsSettingsSpinner;
	private Spinner speedometerModeSettingsSpinner;
	private SeekBar brightnessSettingsSeekBar;
	private CheckBox energySavingSettingsCheckBox;
	private CheckBox showIntroSettingsCheckBox;
    private CheckBox enableCameraSettingsCheckBox;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Get Settings
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		measureUnit=settings.getInt(PREFS_MEASURE_UNIT, 0);
		brightness=settings.getFloat(PREFS_BRIGHTNESS, 1.0f);
		energySaving=settings.getBoolean(PREFS_ENERGY_SAVING, false);
		speedometerIndicatorMode=settings.getInt(PREFS_SPEEDINDICATOR, 0);
		showIntro=settings.getBoolean(PREFS_SHOW_INTRO, true);
        isCameraEnabled=settings.getBoolean(PREFS_CAMERA_ENABLED, true);
		
		//Create main dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogMainView=inflater.inflate(R.layout.settings_dialog, null);
		
		//Define Spinner
		measureUnitsSettingsSpinner=(Spinner) dialogMainView.findViewById(R.id.measureUnitsSettingsSpinner);
		ArrayAdapter<CharSequence> measureUnitsSettingsAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.measure_units_values, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		measureUnitsSettingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		measureUnitsSettingsSpinner.setAdapter(measureUnitsSettingsAdapter);
		measureUnitsSettingsSpinner.setSelection(measureUnit);
		measureUnitsSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				measureUnit=position;
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//Define Brightness SeekBar
		brightnessSettingsSeekBar=(SeekBar) dialogMainView.findViewById(R.id.brightnessSettingsSeekBar);
		brightnessSettingsSeekBar.setProgress((int)(brightness*10));
		brightnessSettingsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				brightness=(float)(progress)/10.0f;
				if(brightness<0.2f){
					brightness=0.2f;
				}
				WindowManager.LayoutParams layoutParameters;
				layoutParameters=getActivity().getWindow().getAttributes();
				layoutParameters.screenBrightness = brightness;
				getActivity().getWindow().setAttributes(layoutParameters);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		//Define energy saving check box
		energySavingSettingsCheckBox=(CheckBox) dialogMainView.findViewById(R.id.energySavingSettingsCheckBox);
		energySavingSettingsCheckBox.setChecked(!energySaving);
		energySavingSettingsCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked)
					energySaving=false;
				else
					energySaving=true;				
			}
		});
		
		//Define intro check box
		showIntroSettingsCheckBox=(CheckBox) dialogMainView.findViewById(R.id.showIntroSettingsCheckBox);
		showIntroSettingsCheckBox.setChecked(showIntro);
		showIntroSettingsCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				showIntro=isChecked;
				
			}
			
		});

        //Define enable camera check box
        enableCameraSettingsCheckBox=(CheckBox) dialogMainView.findViewById(R.id.enableCameraSettingsCheckBox);
        enableCameraSettingsCheckBox.setChecked(isCameraEnabled);
        enableCameraSettingsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isCameraEnabled=isChecked;
            }
        });

		
		//Define Speedometer Mode Spinner
		speedometerModeSettingsSpinner=(Spinner) dialogMainView.findViewById(R.id.speedometerModeSettingsSpinner);
		
		ArrayAdapter<CharSequence> speedometerModeSettingsAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.speedometer_modes_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		speedometerModeSettingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		speedometerModeSettingsSpinner.setAdapter(speedometerModeSettingsAdapter);
		speedometerModeSettingsSpinner.setSelection(speedometerIndicatorMode);
		speedometerModeSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				speedometerIndicatorMode=position;
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		
        builder.setTitle(R.string.menu_settings)
        .setView(dialogMainView)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the positive button event back to the host activity
                       mListener.onDialogPositiveClick(SettingsDialogFragment.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the negative button event back to the host activity
                       mListener.onDialogNegativeClick(SettingsDialogFragment.this);
                   }
               });
		return builder.create();
	}
	
	 @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the SettingsDialogListener so we can send events to the host
	            mListener = (SettingsDialogListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement SettingsDialogListener");
	        }
	    }

	public int getMeasureUnit() {
		return measureUnit;
	}

	public boolean getEnergySaving() {
		return energySaving;
	}

	public float getBrightness() {
		return brightness;
	}

	public int getSpeedometerIndicatorMode() {
		return speedometerIndicatorMode;
	}

	public boolean isShowIntro() {
		return showIntro;
	}

    public boolean isCameraEnabled() {
        return isCameraEnabled;
    }
}
