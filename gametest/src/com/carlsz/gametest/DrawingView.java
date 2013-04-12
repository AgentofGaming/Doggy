package com.carlsz.gametest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
	Paint black = new Paint();
	Paint red = new Paint();
	Paint blue = new Paint();
	Paint green = new Paint();
	Paint transparentBlack = new Paint();
	int x = 4;
	int y = 4;
	int xWidth, yHeight;
	float radius = (float) 25.0;
	Point touch = new Point();
	Point turret = new Point();
	Point intersect = new Point();
	float slope;
	int edgeAvoid = 100;
	final int numSavePoints = 10;
	Point[] savedPoints = new Point[numSavePoints];
	Point[] savedTouches  = new Point[numSavePoints];
	int pointIndex = 0;
	
	public DrawingView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		black.setColor(Color.BLACK);
		transparentBlack.setColor(Color.BLACK);
		transparentBlack.setAlpha(64);
		red.setColor(Color.RED);
		red.setStrokeWidth(10);
		green.setColor(Color.GREEN);
		blue.setColor(Color.BLUE);
		setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					touch.set((int)event.getX(), (int)event.getY());
					invalidate();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					savedPoints[pointIndex%numSavePoints] = intersect;
					savedTouches[pointIndex%numSavePoints] = new Point((int) event.getX(), (int)event.getY());
					Log.i("", savedPoints[pointIndex%numSavePoints].x + "," + savedPoints[pointIndex%numSavePoints].y );
					pointIndex++;
				}
				return true;
			}
		});
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		xWidth = getWidth()/x;
		yHeight = getHeight()/y;
		float hwRatio = ((float) getHeight() / (float) getWidth())*2; //Not sure why need X2 yet
		turret.set((int)getWidth()/2, (int)getHeight());
		
        for (int i = xWidth; i < getWidth(); i = i + xWidth) {
        	canvas.drawLine(i, 0, i, getHeight(), black);
        }
        for (int j = yHeight; j < getHeight(); j = j + yHeight) {
        	canvas.drawLine(0, j, getWidth(), j, black);
        }
        
        //Draw saved lines
        for (int i = 0; i < pointIndex && i < numSavePoints; i++) {
        	canvas.drawLine(turret.x, turret.y, savedPoints[i].x, savedPoints[i].y, transparentBlack);
			canvas.drawCircle(savedPoints[i].x, savedPoints[i].y, radius, transparentBlack);
			canvas.drawText(""+(i), savedTouches[i].x, savedTouches[i].y, red);
		}
        
        if (touch.x > 0 || touch.y > 0) {
        	canvas.drawCircle(touch.x, touch.y, (float) radius, red);
        	canvas.drawLine(turret.x, turret.y, touch.x, touch.y, red);
        	canvas.drawText(touch.x + "," + touch.y, touch.x, touch.y, black);
        	
        	//Avoid div by 0 case
        	Paint colorBySlopeCalculation;
        	int absoluteX = Math.abs(touch.x - turret.x);
        	int absoluteY =  Math.abs(touch.y - turret.y);
        	if (absoluteX <= absoluteY && (absoluteX == 0 || ((float) absoluteY / (float) absoluteX) > hwRatio)) {
        		slope = (float)(touch.x - turret.x) / (float)(touch.y - turret.y);
        		intersect = getEdgeByY(slope);
        		colorBySlopeCalculation = green;
        	} else {
        		slope = (float)(touch.y - turret.y) / (float)(touch.x - turret.x);
            	intersect = getEdgeByX(slope);
            	colorBySlopeCalculation = blue;
			}
        	
        	canvas.drawCircle(intersect.x, intersect.y, (float) radius, colorBySlopeCalculation);
        	canvas.drawLine(turret.x, turret.y, intersect.x, intersect.y, colorBySlopeCalculation);
        	if (intersect.x > turret.x)
        		canvas.drawText(intersect.x + "," + intersect.y, intersect.x-edgeAvoid, intersect.y+edgeAvoid, black);
        	else
        		canvas.drawText(intersect.x + "," + intersect.y, intersect.x+edgeAvoid, intersect.y+edgeAvoid, black);
        }
        canvas.drawText(x + "x" + y +"\nWidth: " + getWidth() + " Height: " + getHeight() + "\nxWidth: " + xWidth + " yHeight: " + yHeight, radius, radius, black);
        canvas.drawText("h/wRatio: " + hwRatio + " slope: " + slope, getWidth()/2, getHeight()/2, black);
		super.onDraw(canvas);
	}
	
	private Point getEdgeByX(float slope) {
		int x = turret.x;
		float y = turret.y;
		
		//Left and Right quadrants NOTE: turret must be on Y=getHeight, otherwise it is a 4 quadrant problem
		if (slope > 0) {
			while (x > 0 && y > 0) {
				y = y - slope;
				x--;
			}
			x++;
			y = y + slope;
		} else {
			while (x < getWidth() && y > 0) {
				y = y + slope;
				x++;
			}
			x--;
			y = y - slope;
		}
		
		return new Point(x, (int) y);
	}
	
	private Point getEdgeByY(float invSlope) {
		float x = turret.x;
		int y = turret.y;
		
		//Same above: 2 Quadrant assumption, in this case y Positive
		while (y > 0) {
			y--;
			x = x - invSlope;
		}
		y++;
		x = x + invSlope;
		return new Point((int)x, y);
	}
}