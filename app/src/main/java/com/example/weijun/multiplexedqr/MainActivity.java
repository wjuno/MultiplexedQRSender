package com.example.weijun.multiplexedqr;


// OpenCV
import org.opencv.android.OpenCVLoader;

// Android
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;



// TODO : Remove directory menu
// TODO : Remove validation, prompt user to enter mutual passcode to receive object
// TODO : Resolve bug during decoding
// TODO : Enable start signal and end signal for sending

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	final int ACTIVITY_CHOOSE_FILE = 4;
	final int RESULT_LOAD_IMAGE=5;

	public final static String EXTRA_MESSAGE = "com.example.MULTIPLEXEDQR";

	private Button mSendBtn;
	private Button mQRbtn;
	private static final int PICKFILE_RESULT_CODE = 100;

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		verifyStoragePermissions(this);

		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_fileFolder){
			//openFolder();
			return true;
		}
		if (id == R.id.action_start) {
			return true;
		}
		if (id == R.id.action_stop){
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		setup();
	}

	// initialization
	private void setup() {
		// Initialize the send button with a listener that for click events
		mSendBtn = (Button) findViewById(R.id.button_send);
		mSendBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				// old code
				// sendMessage();
				Intent intent = new Intent(MainActivity.this, SenderView.class);
				startActivity(intent);
			}
		});


		// Reading from QR Code
		mQRbtn = (Button) findViewById(R.id.btn_takeQR);
		mQRbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				try {
					IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
					integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
					integrator.setPrompt("Scan a QR code");
					integrator.setOrientationLocked(false);
					integrator.setCameraId(0);  // Use a specific camera of the device
					integrator.setBeepEnabled(false);
					integrator.initiateScan();


				// Get the results:
				} catch (Exception e) {
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
					startActivity(marketIntent);
				}
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// FOR QR RECEIVING CONTENT
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			if (result.getContents() == null) {
				Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
				String msg = result.getContents();
				Intent intent = new Intent(this, ReceiverView.class);
				intent.putExtra("QRResult",msg);
				startActivity(intent);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}

		switch (requestCode) {

			case ACTIVITY_CHOOSE_FILE: {
				if (resultCode == RESULT_OK) {
					Uri uri = data.getData();
					String filePath = uri.getPath();

					// call DisplayQRActivity to convert to multiplexed QR
					Intent intent = new Intent(this, DisplayQRActivity.class);

					// passing the filepath and flag to DisplayQRActivity
					intent.putExtra(EXTRA_MESSAGE, filePath);
					intent.addFlags(0);
					startActivity(intent);
				}
				break;
			}
			case RESULT_LOAD_IMAGE: {
				if (resultCode == RESULT_OK) {
					Uri selectedImage = data.getData();
					String[] filePathColumn = {MediaStore.Images.Media.DATA};

					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String picturePath = cursor.getString(columnIndex);
					cursor.close();

					Intent intent = new Intent(this, DisplayQRActivity.class);
					intent.putExtra(EXTRA_MESSAGE, picturePath);
					intent.addFlags(1);
					startActivity(intent);
				}
				break;
			}
			case PICKFILE_RESULT_CODE: {
				if (resultCode == RESULT_OK) {
					String FilePath = data.getData().getPath();

					//FilePath is your file as a string
					File file = new File(Environment.getDataDirectory() + FilePath);
					Log.d("path", FilePath.toString());
					Uri path = Uri.fromFile(file);
					Intent txtOpenintent = new Intent(Intent.ACTION_VIEW);
					txtOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					txtOpenintent.setDataAndType(path, "*/*");
					try {
						startActivity(txtOpenintent);
					} catch (ActivityNotFoundException e) {

					}
				}
				break;
			}
		}
	}
}

	// OPEN FOLDER / DIRECTORY

//	public void openFolder()
//	{
//
//		Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//		fileIntent.setType("*/*");
//		try {
//			startActivityForResult(fileIntent, PICKFILE_RESULT_CODE);
//		} catch (ActivityNotFoundException e) {
//			Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
//		}
//	}


