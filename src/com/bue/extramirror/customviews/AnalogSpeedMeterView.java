package com.bue.extramirror.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import java.lang.Math;

import com.bue.extramirror.utilities.Utilities;

public class AnalogSpeedMeterView extends View {
	private Paint paint;
	private float canvasSize;
	private float centerX, centerY, radius;
	private int speed;
	private float distance;
	private int digitalSpeedometerColor;
	private String measureUnit;
	private int speedMeterMode;

	public AnalogSpeedMeterView(Context context, Float viewSize, String measureUnit, int mode) {
		super(context);
		paint=new Paint();
		canvasSize=viewSize;
		radius=(canvasSize/2)-canvasSize/30;
		centerX=(canvasSize/2);
		centerY=(canvasSize/2);
		speed=0;
		distance=0.0f;
		digitalSpeedometerColor=Color.RED;
		this.measureUnit=measureUnit;
		speedMeterMode=mode;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT);
		switch(speedMeterMode){
			case 0:
				drawOutsideArc(canvas);
				//canvas.drawCircle(centerX, centerY, radius, paint);
				drawInsideCircle(canvas);
				drawMeasureUnit(canvas);
				drawSpeedIndicatorPanel(canvas);
				drawAnalogSpeedIndicator(canvas);
				drawDigitalSpeedIndicator(canvas);
				drawDistanceIndicator(canvas);
				break;
			case 1:
				drawDigitalView(canvas);
				break;
		}
			
		
		super.onDraw(canvas);
	}
	
	private void drawOutsideArc(Canvas canvas){
		paint.setColor(Color.argb(80, 0, 102, 204));
		paint.setStrokeWidth(radius/10);		
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);		
		RectF arcRect=new RectF(canvasSize/30, canvasSize/30, canvasSize-canvasSize/30, canvasSize-canvasSize/30);
		canvas.drawArc(arcRect, 150, 260, false, paint);
	}
	
	public void drawInsideCircle(Canvas canvas){
		paint.setStrokeWidth(1.0f);
		canvas.drawCircle(centerX, centerY, radius/2, paint);
	}
	
	public void drawMeasureUnit(Canvas canvas){
		float xUnit=(float) (centerX+(radius-2*(radius/5))*Math.cos(Math.toRadians(-90)));
		float yUnit=(float) (centerY+(radius-2*(radius/5))*Math.sin(Math.toRadians(-90)));
		paint.setStyle(Style.FILL);
		paint.setTextSize(radius/15);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.rgb(255, 178, 102));
		paint.setStrokeWidth(1.0f);
		canvas.drawText(measureUnit, xUnit, yUnit, paint);
	}
	
	public void drawSpeedIndicatorPanel(Canvas canvas){
		if(measureUnit.equalsIgnoreCase("mph")){
			for(int i=0;i<=160;i+=5){
				if(i%10==0){
					paint.setStrokeWidth(5.0f);
					paint.setColor(Color.rgb(255, 178, 102));
					float x1=(float) (centerX+(radius+radius/20)*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float y1=(float) (centerY+(radius+radius/20)*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float x2=(float) (centerX+(radius-radius/20)*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float y2=(float) (centerY+(radius-radius/20)*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					canvas.drawLine(x1, y1, x2, y2, paint);	
				}else{				
					paint.setStrokeWidth(2.0f);
					paint.setColor(Color.rgb(255, 204, 153));
					float x1=(float) (centerX+(radius+radius/20)*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float y1=(float) (centerY+(radius+radius/20)*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float x2=(float) (centerX+(radius-radius/30)*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					float y2=(float) (centerY+(radius-radius/30)*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
					canvas.drawLine(x1, y1, x2, y2, paint);
				}
				if(i%10==0){
					float x3, y3;
					if(i%20==0)
						paint.setTextSize(radius/6);
					else
						paint.setTextSize(radius/9);
					paint.setTextSkewX(-0.25f);
					paint.setColor(Color.rgb(255, 178, 102));
					paint.setStrokeWidth(3.0f);
					paint.setTextAlign(Align.CENTER);
					if(i<20){
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)-2)));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)-2)));	
					}else if(i<=70){
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i))));					
	//				}else if(i==140){
	//					x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+i)));
	//					y3=(float) (centerY+(radius-2*(radius/9))*Math.sin(Math.toRadians(150.0+i)));					
					}else if(i<110){
						x3=(float) (centerX+(radius-2*(radius/8))*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)+2)));
						y3=(float) (centerY+(radius-2*(radius/8))*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)+2)));
					}else{
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)+4)));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+Utilities.convertMilesToKilometers(i)+4)));
						
					}
						canvas.drawText(Integer.toString(i), x3, y3, paint);
				}
			}
		}else{
			for(int i=0;i<=260;i+=5){
				if(i%10==0){
					paint.setStrokeWidth(5.0f);
					paint.setColor(Color.rgb(255, 178, 102));
					float x1=(float) (centerX+(radius+radius/20)*Math.cos(Math.toRadians(150.0+i)));
					float y1=(float) (centerY+(radius+radius/20)*Math.sin(Math.toRadians(150.0+i)));
					float x2=(float) (centerX+(radius-radius/20)*Math.cos(Math.toRadians(150.0+i)));
					float y2=(float) (centerY+(radius-radius/20)*Math.sin(Math.toRadians(150.0+i)));
					canvas.drawLine(x1, y1, x2, y2, paint);	
				}else{				
					paint.setStrokeWidth(2.0f);
					paint.setColor(Color.rgb(255, 204, 153));
					float x1=(float) (centerX+(radius+radius/20)*Math.cos(Math.toRadians(150.0+i)));
					float y1=(float) (centerY+(radius+radius/20)*Math.sin(Math.toRadians(150.0+i)));
					float x2=(float) (centerX+(radius-radius/30)*Math.cos(Math.toRadians(150.0+i)));
					float y2=(float) (centerY+(radius-radius/30)*Math.sin(Math.toRadians(150.0+i)));
					canvas.drawLine(x1, y1, x2, y2, paint);
				}
				if(i%20==0){
					float x3, y3;
					if(i%40==0)
						paint.setTextSize(radius/6);
					else
						paint.setTextSize(radius/9);
					paint.setTextSkewX(-0.25f);
					paint.setColor(Color.rgb(255, 178, 102));
					paint.setStrokeWidth(3.0f);
					paint.setTextAlign(Align.CENTER);
					if(i<40){
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+i-2)));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+i-2)));	
					}else if(i<=140){
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+i)));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+i)));					
	//				}else if(i==140){
	//					x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+i)));
	//					y3=(float) (centerY+(radius-2*(radius/9))*Math.sin(Math.toRadians(150.0+i)));					
					}else if(i<220){
						x3=(float) (centerX+(radius-2*(radius/8))*Math.cos(Math.toRadians(150.0+i+2)));
						y3=(float) (centerY+(radius-2*(radius/8))*Math.sin(Math.toRadians(150.0+i+2)));
					}else{
						x3=(float) (centerX+(radius-2*(radius/10))*Math.cos(Math.toRadians(150.0+i+4)));
						y3=(float) (centerY+(radius-2*(radius/10))*Math.sin(Math.toRadians(150.0+i+4)));
						
					}
						canvas.drawText(Integer.toString(i), x3, y3, paint);
				}
			}
		}
	}
	
	public void drawAnalogSpeedIndicator(Canvas canvas){
		paint.setColor(Color.RED);		
		float x4=(float) (centerX+(radius/2)*Math.cos(Math.toRadians(150.0+speed)));
		float y4=(float) (centerY+(radius/2)*Math.sin(Math.toRadians(150.0+speed)));
		float x5=(float) (centerX+radius*Math.cos(Math.toRadians(150.0+speed)));
		float y5=(float) (centerY+radius*Math.sin(Math.toRadians(150.0+speed)));
		canvas.drawLine(x4, y4, x5, y5, paint);	
	}
	
	public void drawDigitalSpeedIndicator(Canvas canvas){
		paint.setTextSize(radius/2);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(digitalSpeedometerColor);
		paint.setStrokeWidth(1.0f);
		paint.setTextSkewX(-0.0f);
		if(measureUnit.equalsIgnoreCase("Km/h"))
			canvas.drawText(Integer.toString(speed), centerX, centerY+(radius/6), paint);
		else if(measureUnit.equalsIgnoreCase("mph"))
			canvas.drawText(Utilities.convertKilometersToMiles((float)speed, true), centerX, centerY+(radius/6), paint);
	}
	
	public void drawDistanceIndicator(Canvas canvas){
		RectF distanceMeterRect=new RectF(centerX-radius/1.5f, centerY+(radius/2)+(radius/20), centerX+radius/3, centerY+radius-(radius/20));
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(Color.argb(80, 255, 178, 102));
		canvas.drawRoundRect(distanceMeterRect, 10, 10, paint);
		
		paint.setColor(Color.rgb(153, 76, 0));
		paint.setTextSize(radius/4);
		float xDistance=(centerX-radius/1.5f+centerX+radius/3)/2;
		float yDistance=((centerY+(radius/2)+(radius/20))+(centerY+radius-(radius/20)))/1.9f;
		if(measureUnit.equalsIgnoreCase("Km/h"))
			canvas.drawText(Utilities.convertMetersToKhm(distance, false), xDistance, yDistance, paint);
		else if(measureUnit.equalsIgnoreCase("mph"))
			canvas.drawText(Utilities.convertMetersToMiles(distance, false), xDistance, yDistance, paint);
		
	}
	
	public void drawDigitalView(Canvas canvas){
		String distanceMesureUnit="";
		paint.setTypeface(Typeface.MONOSPACE);
		paint.setTextAlign(Align.RIGHT);
		paint.setColor(digitalSpeedometerColor);
		paint.setStrokeWidth(1.0f);
		paint.setTextSkewX(-0.0f);
		if(measureUnit.equalsIgnoreCase("Km/h")){
			if(speed<100){
				paint.setTextSize(radius*1.5f);
				canvas.drawText(Integer.toString(speed), canvasSize-canvasSize/10, canvasSize-canvasSize/10, paint);
			}else{
				paint.setTextSize(radius);
				canvas.drawText(Integer.toString(speed), canvasSize-canvasSize/10, canvasSize-canvasSize/10, paint);
			}
			paint.setTextSize(radius/2);
			canvas.drawText(Utilities.convertMetersToKhm(distance, false), canvasSize-canvasSize/10, canvasSize/5, paint);
			distanceMesureUnit="km";
		}else if(measureUnit.equalsIgnoreCase("mph")){
			if(Utilities.convertKilometersToMiles(speed)<100){
				paint.setTextSize(radius*1.5f);
				canvas.drawText(Utilities.convertKilometersToMiles((float)speed, true), canvasSize-canvasSize/7, canvasSize-canvasSize/10, paint);
			}else{
				paint.setTextSize(radius);
				canvas.drawText(Utilities.convertKilometersToMiles((float)speed, true), canvasSize-canvasSize/7, canvasSize-canvasSize/10, paint);
			}
			paint.setTextSize(radius/2);
			canvas.drawText(Utilities.convertMetersToMiles(distance, false), canvasSize-canvasSize/10, canvasSize/5, paint);
			distanceMesureUnit="mi.";
		}
		paint.setTextSize(radius/8);
		paint.setTextAlign(Align.LEFT);
		canvas.drawText(measureUnit, canvasSize-canvasSize/7, canvasSize-canvasSize/10, paint);
		paint.setTextSize(radius/10);
		canvas.drawText(distanceMesureUnit, canvasSize-canvasSize/10, canvasSize/5, paint);
		
	}

	public void setSpeed(int speed) {
		this.speed = (int)Utilities.convertMetersToKhm(speed);
		invalidate();
	}

	public void setDistance(float distance) {
		this.distance = distance;
		invalidate();
	}

	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
		invalidate();
	}

	public void setDigitalSpeedometerColor(int color) {
		digitalSpeedometerColor=color;
		invalidate();		
	}
	
	public void changeMode(int mode){
		this.speedMeterMode=mode;
		invalidate();
	}
	
	
}
