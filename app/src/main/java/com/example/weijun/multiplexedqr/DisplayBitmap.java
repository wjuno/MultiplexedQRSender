package com.example.weijun.multiplexedqr;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class DisplayBitmap extends AppCompatActivity {

	/*
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
	     @Override
	     public void onManagerConnected(int status) {
	       switch (status) {
	           case LoaderCallbackInterface.SUCCESS:
	           {
	              Log.i("app", "OpenCV loaded successfully");
	              
	           } break;


	           default:
	           {
	               super.onManagerConnected(status);
	           } break;
	         }
	      }
	 };
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_bitmap);
		if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    }

		byte[] byteArray = getIntent().getByteArrayExtra("image");
		Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

		ImageView img1= (ImageView) findViewById(R.id.imageView1);
		 img1.setImageBitmap(bmp);
		
	}

	/*
	@SuppressLint("NewApi")
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(Mode.OVERLAY));
        Bitmap bmOverlay = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), paint);
        canvas.drawBitmap(bmp2, new Matrix(), paint);
        return bmOverlay;
    }
    */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_bitmap, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
