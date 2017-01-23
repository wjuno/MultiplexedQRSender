package com.example.weijun.multiplexedqr;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.BarcodeFormat;

import com.google.zxing.WriterException;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;


import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;


@SuppressLint("NewApi")
public class DisplayQRActivity extends Activity {

	// specify colors
	public static final int RED = 0xFFFF0000;
	public static final int GREEN = 0xFF00FF00;
	public static final int BLUE = 0xFF0000FF;

	String pwdCode;

	// declare bitmap for RGB
	Bitmap bitmap, bitmap1, bitmap2, pwdBitmap;
	Button startBtn, finishBtn, readyBtn;
	TextView lblTxt;
	// authenticate qr


	// initialize the height and width of QR code
	int width = 750, height = 750;
	String message;

	// init animation frames
	private int currentFrame = 0;




	final AnimationDrawable animDrawable = new AnimationDrawable();
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayqr);
		ActionBar myActionBar = getActionBar();
		myActionBar.hide();




		// retrieving passcode set by user from SharedPreferences
		SharedPreferences sharedPrefer = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		pwdCode = sharedPrefer.getString("pwdcode", "");

		// ensure passcode is initialized before encoding
		if(pwdCode=="" || pwdCode==null) {

			new AlertDialog.Builder(this)
					.setTitle("Empty Passcode")
					.setMessage("Please ensure passcode is set.")
					.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// go back to main page
							Intent intent = new Intent(DisplayQRActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();

		}

		// status text
		lblTxt = (TextView) findViewById(R.id.lbl_text);

		// Initialize the send button with a listener that for click events
		startBtn = (Button) findViewById(R.id.btn_start);
		readyBtn = (Button) findViewById(R.id.btn_ready);
		finishBtn = (Button) findViewById(R.id.btn_finish);
		startBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {


				// this method execute the Asyntask for loading image
				new doit().execute();

				// make finish button visible
				startBtn.setVisibility(View.GONE);
				//finishBtn.setVisibility(View.VISIBLE);
			}
		});


		readyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				readyBtn.setVisibility(View.GONE);
				new CountDownTimer(5000, 1000) {

					public void onTick(long millisUntilFinished) {
						lblTxt.setText("STARTING IN: " + millisUntilFinished / 1000);
					}

					public void onFinish() {
						lblTxt.setText("Focus camera centre on QR code");
					}
				}.start();

				ImageView img;

				// animate the calculated multiplexed qr code
				img = (ImageView) findViewById(R.id.qrCode1);
				img.setBackgroundDrawable(animDrawable);
				animDrawable.start();
				animDrawable.setOneShot(true);
				// Pass our animation drawable to our custom drawable class
				CustomAnimationDrawableNew anim_qr = new CustomAnimationDrawableNew(animDrawable) {
					@Override
					void onAnimationFinish() {
						// Animation has finished...
						Log.d("SUCCESS","!!!");
						finishBtn.setVisibility(View.VISIBLE);
					}
				};
				anim_qr.start();

//				Message msg = new Message();
//				Handler startAnimation = new Handler() {
//					public void handleMessage(Message msg) {
//						super.handleMessage(msg);
//						// Pass our animation drawable to our custom drawable class
//						CustomAnimationDrawableNew anim_qr = new CustomAnimationDrawableNew(animDrawable) {
//							@Override
//							void onAnimationFinish() {
//								// Animation has finished...
//								Log.d("SUCCESS","!!!");
//								readyBtn.setText("DONE");
//								readyBtn.setVisibility(View.VISIBLE);
//							}
//						};
//						anim_qr.start();
//						animDrawable.start();
//						animDrawable.setOneShot(true);
//
//					}
//				};
//				startAnimation.sendMessage(msg);

			}
		});





		finishBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// call DisplayQRActivity to convert to multiplexed QR
				Intent intent = new Intent(DisplayQRActivity.this, MainActivity.class);
				startActivity(intent);
				finish();

			}
		});


		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}


	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("DisplayQR Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}


	@Override
	public void onStart() {
		super.onStart();


		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());

	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}


	private class doit extends AsyncTask<Void, Void, Integer> {
		private ProgressDialog Dialog = new ProgressDialog(DisplayQRActivity.this);

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Loading please wait ...");
			Dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// receive filepath msg here
			Intent intent = getIntent();
			message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
			int flag = intent.getFlags();
			String messagebyte = null;


			try {
				// decode if received flag = 0 decode file
				if (flag == 0)
					messagebyte = decodefile(message);


					// decode if received flag = 1 decode image
				else if (flag == 1)
					messagebyte = decodeimage(message);
			} catch (IOException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			}

			// encoding to multiplexed here.
			try {

				// encode the start passcode first
			//	encodeqr(pwdCode);

				// display multiplexed
				encodeqr(messagebyte);

				// encode the end passcode
			//	encodeqr(pwdCode);



			} catch (WriterException e) {

				e.printStackTrace();
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {

			// ready to execute anytime.
			readyBtn.setVisibility(View.VISIBLE);

			Dialog.dismiss();
		}
	}



	// TODO : note that only file is working so far.
	// TODO : decode for image as well



	// decode the selected file path to here to messagebyte for encoding
	private String decodefile(String FilePath) throws IOException {


		File file = new File(FilePath);
		FileInputStream fis = new FileInputStream(file);

		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}

		Log.d("App TODO::: WEIJUN TEST", Integer.toString(sb.length()));
		Log.d("App TODO::: WEIJUN TEST", sb.toString());
		return sb.toString();

	}

	private String decodeimage(String FilePath) throws IOException {
		File imagefile = new File(FilePath);
		FileInputStream fis1 = null;
		try {
			fis1 = new FileInputStream(imagefile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] b = new byte[(int) imagefile.length()];
		FileInputStream fis = new FileInputStream(imagefile);
		fis.read(b);
		fis.close();
		String sb = Base64.encodeToString(b, Base64.NO_WRAP);
		Log.d("App", Integer.toString(b.length));
		Log.d("App", sb.toString());
		return sb;


	}


	// MAIN ENCODING METHOD HERE
	@SuppressWarnings("deprecation")
	private void encodeqr(String strParse) throws WriterException {

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// initialization
		int frameno = 1;
		int colorcode = 1;
		String sb = "";
		String checksumstr = "";
		int i = 0;

		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap2 = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

		//String repeated = new String(new char[100]).replace("\0", "0");
	//	Log.d("REPEATED","String is : \n" + repeated);

		// this will be the starting flag QR code shown first (passcode)
		performStart(pwdCode, 0);


		int j = 0;
		int x = 0;

		while (true) {
			j++;

			// max 500 characters
			if (strParse.length() / (j * 3) <= 500) {
				x = j;
				break;
			}
		}

		i = -((strParse.length() / (x * 3)) + 1);
		while (i < strParse.length()) {
			i += ((strParse.length() / (x * 3)) + 1);
			sb = (strParse.substring(i, Math.min((i + strParse.length() / (x * 3)) + 1, strParse.length())));

			// checksum CRC32 - Frame Number --> 2 characters, checksum --> 11, string length --> 3 (since max is 500 characters in one frame)
			checksumstr = String.format("%1$2s", frameno) + String.format("%1$11s", String.valueOf(checksum(sb))) + sb + String.format("%1$3s", sb.length());
			Log.d("checksum_string", checksumstr + " | " + colorcode + "\n");

			performQRencoding(checksumstr, colorcode);

			// try to add to 3 which is color = Blue
			colorcode++;
			frameno++;
			currentFrame++;

			// else 4 restart to color = Red
			if (colorcode == 4)
				colorcode = 1;
			if (Math.min((i + strParse.length() / (x * 3)) + 1, strParse.length()) == strParse.length())
				break;
		}

		// this will be the ending flag QR code shown (passcode)
		performStart(pwdCode, 0);

		// after completed finished the progressbar

	}

	// method to encode base on colorcode
	private void performQRencoding(String checksumstr, int colorcode) throws WriterException {

		// encodebitmap generate the QR code
		bitmap = overlay(bitmap, encodebitmap(checksumstr, colorcode));

		// encodeqr method will keep looping till blue. R>G>B
		if (colorcode == 3) {//QR= blue color

			bitmap1 = invert(bitmap);
			Bitmap draw = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.ARGB_8888);
			Drawable frame1 = new BitmapDrawable(draw);
			Canvas canvas = new Canvas(draw);
			canvas.drawBitmap(bitmap1, 0, 0, new Paint());
			frame1.draw(canvas);
			animDrawable.addFrame(frame1, 1250);//flash every 0.5s
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}
	}

	// this method generate the start QR code image.
	private void performStart(String checksumstr, int colorcode) throws WriterException {


		// encodebitmap generate the QR code
		bitmap2 = encodebitmap(checksumstr, colorcode);

		Bitmap draw = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.ARGB_8888);
		Drawable frame1 = new BitmapDrawable(getResources(), draw);
		Canvas canvas = new Canvas(draw);
		canvas.drawBitmap(bitmap2, new Matrix(), new Paint());
		frame1.draw(canvas);
		animDrawable.addFrame(frame1, 5000); // set frame flash every 5 seconds
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	private String checksum(String sb) {
		byte[] bytes = sb.getBytes();
		Checksum checksumEngine = new CRC32();
		checksumEngine.update(bytes, 0, bytes.length);
		long checksum = checksumEngine.getValue();

		return Long.toString(checksum);
	}


	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.ADD));
		Bitmap bmOverlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), paint);
		canvas.drawBitmap(bmp2, new Matrix(), paint);
		return bmOverlay;
	}

	//  generate the QR code
	@SuppressWarnings("static-access")
	private Bitmap encodebitmap(String sb, int colorcode) throws WriterException {
		int color;
		switch (colorcode) {
			case 1:
				color = RED;
				break;
			case 2:
				color = GREEN;
				break;
			case 3:
				color = BLUE;
				break;
			default:
				//RED
				color = 0xff000000;
				break;
		}

		// call the QRCodeEncoder class to handle
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(sb,
				null,
				Contents.Type.TEXT,
				BarcodeFormat.QR_CODE.toString(),
				width);

		Bitmap bitmap = qrCodeEncoder.encodeAsBitmap(color);
		bitmap = bitmap.createScaledBitmap(bitmap, width, width, false);

		return bitmap;

	}

	public static Bitmap invert(Bitmap src) {
		Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		int A, R, G, B;
		int pixelColor;
		int height = src.getHeight();
		int width = src.getWidth();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixelColor = src.getPixel(x, y);
				A = Color.alpha(pixelColor);

				R = 255 - Color.red(pixelColor);
				G = 255 - Color.green(pixelColor);
				B = 255 - Color.blue(pixelColor);

				output.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		return output;
	}

	public void backButton(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}




	/*
	public static Bitmap changeBitmapContrastBrightness(Bitmap bmp)
	{
	    ColorMatrix cm = new ColorMatrix(new float[]
	            {
	                10, 0, 0, 0, 100,
	                0, 10, 0, 0, 100,
	                0, 0, 10, 0, 100,
	                0, 0, 0, 1, 0
	            });

	    Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

	    Canvas canvas = new Canvas(ret);

	    Paint paint = new Paint();
	    paint.setColorFilter(new ColorMatrixColorFilter(cm));
	    canvas.drawBitmap(bmp, 0, 0, paint);

	    return ret;
	}

    */

