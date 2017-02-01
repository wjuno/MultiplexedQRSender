package com.example.weijun.multiplexedqr;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class EnterPasscode extends Activity {

    private AlertDialog dialog = null;
    private File fileUri;
    Button btnFile,btnImg;
    boolean imgF = false;


    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);

    ArrayList<File> fileNames = new ArrayList<File>();
    ArrayList<String> dup = new ArrayList<String>();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    EditText edt_pwd;
    Button btn_camera,btn_openFile;


    String userPass;
    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_passcode);

        edt_pwd = (EditText)findViewById(R.id.edt_userPwd);
        btn_camera = (Button)findViewById(R.id.btn_camera);
        btn_openFile = (Button)findViewById(R.id.btn_openFile);

    }

    @Override
    public void onStart() {
        super.onStart();

        btn_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //  get the port number for the edit text for validation purpose
                userPass = edt_pwd.getText().toString();
                recordQRCode();

            }
        });


        btn_openFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // TODO Open the Downloads Folder
                Intent intent = new Intent();
                Uri pathUri= Uri.parse(Environment.getExternalStorageDirectory().getPath()
                        + "/MultiplexedQR/");
                intent.setDataAndType(pathUri, "file/*");
                startActivity(intent);


            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();

    }



    @SuppressLint("SimpleDateFormat")
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            fileNames.add(mediaFile);

        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    public void recordQRCode(){

        // Custom Dialog
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(EnterPasscode.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_camera, null);


        // Saving passcode
        btnFile = (Button) mView.findViewById(R.id.btn_fileFormat);
        btnFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imgF = false;
                intentCamera();
                if (EnterPasscode.this.dialog!=null) {
                    EnterPasscode.this.dialog.dismiss();
                }
            }
        });

        // Displaying passcode
        btnImg = (Button) mView.findViewById(R.id.btn_imgFormat);
        btnImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imgF = true;
                intentCamera();
                if (EnterPasscode.this.dialog!=null) {
                    EnterPasscode.this.dialog.dismiss();
                }
            }
        });
        mBuilder.setView(mView);
        this.dialog = mBuilder.create();
        this.dialog.show();



    }

    public void intentCamera(){

        if (!marshMallowPermission.checkPermissionForCamera()) {
            marshMallowPermission.requestPermissionForCamera();
        }
        if (!marshMallowPermission.checkPermissionForRecord()) {
            marshMallowPermission.requestPermissionForRecord();
        }

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);


        // create a file to save the video
        fileUri = getOutputMediaFile(MEDIA_TYPE_VIDEO);

        // set the image file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        flag = 0;

        System.out.println("**************TEST****************");
        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
    }



    // AsyncTask
    private class doit extends AsyncTask<Uri, Void, Integer> {
        boolean passframe = false;
        String qrText,qrText2,qrText3;
        private ProgressDialog dialog ;
        // use hashset to remove any duplicates
        Set<String> noDup = new HashSet<String>();
        private List<String> trackStrList = new ArrayList<String>();

        // TODO : Problem with Android Lollipop Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(EnterPasscode.this, "",
                    "Loading please wait ...", true);
        }

        @Override
        protected Integer doInBackground(Uri... uris) {
            try {

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(EnterPasscode.this,uris[0]);
                String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int videoDur = Integer.parseInt(time) * 1000;// This will give the total video time in microsecond
                Log.d("Duration","Time is : " +  videoDur);


                // TODO: Do decode passcode algo here
                // initialization start time
                int vidInit = 0;

                while (vidInit < videoDur){

                    // Test : 6 microsecond = multiplexed
                    Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(vidInit); //unit in microsecond

                    // Adjust the frame to make sure rotate to the correct position
                    if (bmFrame.getWidth() > bmFrame.getHeight()) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        bmFrame = Bitmap.createBitmap(bmFrame, 0, 0, bmFrame.getWidth(), bmFrame.getHeight(), matrix, true);
                    }

                    // Setting the frame sizes for displaying (Testing purpose)
                    bmFrame = Bitmap.createScaledBitmap(bmFrame, 500, 800, false);

                    // Decode qr code
                    qrText = checkframe(decodeqrRed(bmFrame));
                    Log.d("FrameString","String is : " +  qrText);

                    if(qrText!=null){
                        if(qrText.equals(userPass)){
                            // Pass
                            // Call check multiplexed qr method
                            // Exit Loop
                            Log.d("passcode","passcode1 == " + qrText);
                            Log.d("passcode","passcode2 == " + userPass);
                            Log.d("vidDur", ": " + vidInit);
                            passframe = true;
                            break;
                        }else if(vidInit >= videoDur/2){
                            Log.d("passcode","FAIL RETRY");
                            passframe = false;
                            break;
                        }
                    }
                    vidInit = vidInit + 500000;
                }


                // only if pass validating then can proceed to decoding the rest of the file
                if(passframe){

                    while (vidInit <= videoDur) {
                        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(vidInit); //unit in microsecond

                        // Adjust the frame to make sure rotate to the correct position
                        if (bmFrame.getWidth() > bmFrame.getHeight()) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            bmFrame = Bitmap.createBitmap(bmFrame, 0, 0, bmFrame.getWidth(), bmFrame.getHeight(), matrix, true);
                        }

                        // Setting the frame sizes for displaying (Testing purpose)
                        bmFrame = Bitmap.createScaledBitmap(bmFrame, 500, 800, false);

                        // Decode qr code
                        qrText = checkframe(decodeqrRed(bmFrame));
                        Log.d("QRTEXT", "Red Content is : " + qrText);
                        qrText2 = checkframe(decodeqrGreen(bmFrame));
                        Log.d("QRTEXT", "Green Content is : " + qrText2);
                        qrText3 = checkframe(decodeqrBlue(bmFrame));
                        Log.d("QRTEXT", "Blue Content is : " + qrText3);

                        // Make sure to check only the actual dataset (remove duplicates using hashset)
                        if(!qrText.equals(userPass)) {
                            if(!decodeChecksum(qrText,qrText2,qrText3).equals("")){
                                if(decodeChecksum(qrText,qrText2,qrText3).length()>5){
                                    String trackString = decodeChecksum(qrText,qrText2,qrText3).substring(0,5);
                                    trackStrList.add(trackString);
                                }
                                noDup.add(decodeChecksum(qrText,qrText2,qrText3));
                            }
                        }

                        vidInit = vidInit + 500000;

                        // Searching for ending QR code
                        if(vidInit > videoDur/1.5){
                            if(qrText.equals(userPass)||qrText2.equals(userPass)||qrText3.equals(userPass)){
                                Log.d("App","Pass Application");
                                break;
                            }
                        }
                    } // END WHILE

                    // Then we use treeset to do order sets into increasing order (Sorting)
                    TreeSet myTreeSet = new TreeSet();
                    String mergeStr = "";

                    if(!noDup.isEmpty()){
                        myTreeSet.addAll(noDup);
                        Iterator iterator;
                        iterator = myTreeSet.iterator();

                        // displaying the decoded set data (cleaning)
                        System.out.print("Tree set data: ");

                        // TODO

                        while (iterator.hasNext()) {
                            mergeStr += iterator.next().toString();

                        }
                    }

                    // by using this replacing method then able to work else triming it before merging won't works
                    for(int i=0; i<trackStrList.size();i++){
                        Log.d("TAG","tracking: " + trackStrList.get(i));
                        if(mergeStr.contains(trackStrList.get(i))){
                            mergeStr = mergeStr.replaceAll(trackStrList.get(i).toString().substring(0,2),"");
                        }
                    }

                    // if its a image file, we need to decode and save image
                    if(imgF){
                        Log.d("TAG","STRING IS ==> " + mergeStr);
                        openDecodedImg(mergeStr);
                        Log.d("TAG","IMAGE FILE");

                    }else{
                        Log.d("TAG","NORMAL FILE");
                        File file;
                        FileOutputStream outputStream;

                        try {

                            String filename = "QRCode_" + (new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())).format(new Date()) + ".txt";
                            File folder = new File(Environment.getExternalStorageDirectory() +
                                    File.separator + "MultiplexedQR");
                            boolean success = true;
                            if (!folder.exists()) {
                                success = folder.mkdirs();
                            }
                            if (success) {
                                // Do something on success
                                file = new File(getExternalStorageDirectory().getPath()+"/MultiplexedQR/", filename);
                                outputStream = new FileOutputStream(file);
                                outputStream.write(mergeStr.getBytes());
                                outputStream.close();

                                // open file from download folder
                                File myFile = new File(String.valueOf(file.getAbsoluteFile()));
                                FileOpen.openFile(EnterPasscode.this, myFile);
                            } else {
                                // Do something else on failure
                                // Do something on success
                                file = new File(getExternalStorageDirectory().getPath()+"/MultiplexedQR/", filename);
                                outputStream = new FileOutputStream(file);
                                outputStream.write(mergeStr.getBytes());
                                outputStream.close();

                                // open file from download folder
                                File myFile = new File(String.valueOf(file.getAbsoluteFile()));
                                FileOpen.openFile(EnterPasscode.this, myFile);
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } // END PASS

            } catch (Exception e) { // END MAIN TRY
                Log.e("GUN", Log.getStackTraceString(e));

            }

            return null;
        }




        @Override
        protected void onPostExecute(Integer result) {

            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }

            if(passframe==false){
                // Invalid passcode
                AlertDialog.Builder adb = new AlertDialog.Builder(EnterPasscode.this);
                adb.setTitle("Incorrect Passcode")
                        .setMessage("Please ensure passcode is correct.")
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // go back to main page
                                Intent intent = new Intent(EnterPasscode.this, EnterPasscode.class);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        switch (requestCode) {

            case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:


                if (resultCode == RESULT_OK) {

                    // TODO REMOVE TESTING VID
                    // get user permission first
                    //Uri videoUri = data.getData();

                    // use this video as test first.
                    Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.testing);

                    if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                        marshMallowPermission.requestPermissionForExternalStorage();
                    }

                    // Perform the async task (main decoding)
                   new doit().execute(videoUri);

                    System.gc();

                } // END IF RESULT_OK

                break;

        } // END SWITCH CASE

    }


    private void openDecodedImg(String base64Str){
        byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        File file = null;
        String filename = "QRCode_" + (new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())).format(new Date()) + ".PNG";
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "MultiplexedQR");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
            FileOutputStream out = null;
            try {
                file = new File(getExternalStorageDirectory().getPath()+"/MultiplexedQR/", filename);
                out = new FileOutputStream(file);
                decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        // open file from download folder
                        File myFile = new File(String.valueOf(file.getAbsoluteFile()));
                        FileOpen.openFile(EnterPasscode.this, myFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private String decodeChecksum(String red, String green, String blue){

        String r = "",g = "",b = "",fullString = "";

        // Show the red frame number only as the header # of the frame (for sorting purposes)
        if (red!=null) {
            if(red.length()>13){
                if (red.substring(0)!=null || !red.substring(0).equals("")) {
                    r = red.replace(red.substring(2, 13), "");
                    r = r.replace(r.substring(r.length()-3),"");
                }
            }
        }


        if (green!=null){
            if(green.length()>13) {
                g = green.replace(green.substring(0, 13), "");
                g = g.replace(g.substring(g.length()-3),"");
            }
        }

        if (blue!=null){
            if(blue.length()>13){
                b = blue.replace(blue.substring(0,13), "");
                b = b.replace(b.substring(b.length()-3),"");
            }
        }


        fullString = r + g + b;
        Log.d("ERROR1","Full String : ==> " + fullString);


        return fullString;
    }

    // keep trying for 3 times

    private String checkframe(Bitmap pic){
        boolean tries = false;
        Result result = null;
        int width1 = pic.getWidth(), height1 = pic.getHeight();
        int[] pixels = new int[width1 * height1];
        pic.getPixels(pixels, 0, width1, 0, 0, width1, height1);
        pic.recycle();


        LuminanceSource source = new RGBLuminanceSource(width1, height1, pixels);
        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

//        int count = 0;
//        int maxTries = 20;
//        while (true){
        try {
            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            result = reader.decode(bBitmap, decodeHints);
            return result.toString();

        } catch (NotFoundException e) {
            Log.d("App", "NotFoundException" + ": checkframe method");
            // if (++count == maxTries){break;}

        } catch (ChecksumException e) {
            Log.d("App", "ChecksumException");
            // if (++count == maxTries){break;}

        } catch (FormatException e) {
            Log.d("App", "FormatException");
            // if (++count == maxTries){break;}
        }
 //       }

        return "fffailll";
    }

    @SuppressLint("NewApi")
    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), paint);
        canvas.drawBitmap(bmp2, new Matrix(), paint);
        return bmOverlay;
    }



    // Decode the Red QR
    private Bitmap decodeqrRed(Bitmap bm) {
        Mat src = new Mat(bm.getWidth(), bm.getHeight(), Imgproc.COLOR_BGR2GRAY);
        Utils.bitmapToMat(bm, src);
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);
        Bitmap bmp1 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        bmp2.eraseColor(Color.WHITE);
        Utils.matToBitmap(rgb.get(0), bmp1);
        return overlay(bmp1,bmp2);
    }

    // Decode the Green QR
    private Bitmap decodeqrGreen(Bitmap bm) {
        Mat src = new Mat(bm.getWidth(), bm.getHeight(), Imgproc.COLOR_BGR2GRAY);
        Utils.bitmapToMat(bm, src);
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);
        Bitmap bmp1 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgb.get(1), bmp1);
        Bitmap bmp2 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        bmp2.eraseColor(Color.WHITE);
        return overlay(bmp1,bmp2);
    }

    // Decode the Blue QR
    private Bitmap decodeqrBlue(Bitmap bm) {
        Mat src = new Mat(bm.getWidth(), bm.getHeight(), Imgproc.COLOR_BGR2GRAY);
        Utils.bitmapToMat(bm, src);
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);
        Bitmap bmp1 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgb.get(2), bmp1);
        Bitmap bmp2 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        bmp2.eraseColor(Color.WHITE);
        return overlay(bmp1,bmp2);

    }

    private String decodeReal(Bitmap pic, int frameno){

        Result result = null;
        int width1 = pic.getWidth(), height1 = pic.getHeight();
        int[] pixels = new int[width1 * height1];
        pic.getPixels(pixels, 0, width1, 0, 0, width1, height1);
        pic.recycle();
        pic = null;

        LuminanceSource source = new RGBLuminanceSource(width1, height1, pixels);
        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

        try
        {
            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            result = reader.decode(bBitmap,decodeHints);
        }

        catch (NotFoundException e) {
            return "|NotFoundException|";

        } catch (ChecksumException e) {
            return "|ChecksumException|";
        }
        catch (FormatException e) {
            return "|FormatException|";
        }

        if (validateChecksum(result.toString(),frameno)==false){
            // |checksumerror|
            return "|checksumerror|";
        }else{

            return result.toString().substring(13, result.toString().length()-3);}
    }

    private String WriteByteArrayToFile(String s) {
        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(Environment.getExternalStorageDirectory()+File.separator+"ColorQR");
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

        String strFilePath = outDir.getPath()+File.separator+dateFormat.format(date)+".txt";
        try {

            FileOutputStream fos = new FileOutputStream(strFilePath);
            fos.write(s.getBytes());
            fos.close();
        }

        catch(FileNotFoundException ex)   {
            System.out.println("FileNotFoundException : " + ex);
        }
        catch(IOException ioe)  {
            System.out.println("IOException : " + ioe);
        }
        return strFilePath;

    }


    private String WriteByteArrayToImage(String s) {
        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(Environment.getExternalStorageDirectory()+File.separator+"ColorQR");
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
        String strFilePath = outDir.getPath()+File.separator+dateFormat.format(date)+".jpg";

        try {
            byte[] decodedString = Base64.decode(s, Base64.NO_WRAP);
            FileOutputStream fos = new FileOutputStream(strFilePath);
            fos.write(decodedString);
            fos.close();
        }
        catch(FileNotFoundException ex)   {
            System.out.println("FileNotFoundException : " + ex);
        }
        catch(IOException ioe)  {
            System.out.println("IOException : " + ioe);
        }catch(IllegalArgumentException iae){
            Toast.makeText(getApplicationContext(), "Bad base 64", Toast.LENGTH_LONG).show();
        }
        return strFilePath;
    }

    private boolean cRC32Test(String values,long expected){
        byte[] bytes = values.getBytes();
        Checksum crc=new CRC32();
        crc.update(bytes, 0, bytes.length);
        long checksum = crc.getValue();
        if(checksum==expected)
            return true;
        return false;
    }

    private boolean validateChecksum(String result, int frameno) {
        boolean correctframe = false;
        boolean correctchecksum = false;
        boolean correctlength = false;
        String frame;
        String checksumvalue;
        String strlength;
        String string;

        frame=result.toString().substring(0, 2).replaceAll(" ", "");

        checksumvalue=result.toString().substring(2, 13).replaceAll(" ", "");

        strlength=result.toString().substring(result.length()-3, result.length()).replaceAll(" ", "");

        string=result.toString().substring(13, result.toString().length()-3);

        if (Integer.valueOf(frame).equals(frameno))
            correctframe= true;

        if (cRC32Test(string, Long.parseLong(checksumvalue))==true)
            correctchecksum= true;

        if (Integer.valueOf(strlength).equals(string.length()))
            correctlength= true;
        Log.d("App",String.valueOf(frame));
        Log.d("App",String.valueOf(correctframe));
        Log.d("App",String.valueOf(Long.parseLong(checksumvalue)));
        Log.d("App",String.valueOf(correctchecksum));
        Log.d("App",String.valueOf(string));

        if (correctframe== true&&correctchecksum== true&&correctlength== true)
            return true;
        return false;
    }
}
