package com.example.weijun.multiplexedqr;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class DisplayPortQR extends Activity {

    private Button doneBtn;
    private String portNumber;

    private File fileUri;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    int flag;

    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);
    ArrayList<File> fileNames = new ArrayList<File>();
    ArrayList<String> dup = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_port_qr);



        ImageView qr_image = (ImageView) findViewById(R.id.imgViewDisplayPort);

        Intent intent = getIntent();
        if (intent.getExtras().getBoolean("SENDER")) {
            Bitmap bitmap = intent.getParcelableExtra("BitmapPort");
            qr_image.setImageBitmap(bitmap);

            portNumber = intent.getStringExtra("PortNumber");

            doneBtn = (Button) findViewById(R.id.doneBtn);
            doneBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Send get the port number for the edit text
                    Intent intent = new Intent(DisplayPortQR.this, MutualPasscode.class);
                    intent.putExtra("PortNumber", portNumber);
                    startActivity(intent);
                }
            });
        } else if (intent.getExtras().getBoolean("RECEIVER")) {

            Bitmap bitmap = intent.getParcelableExtra("BitmapPort_R");
            qr_image.setImageBitmap(bitmap);

            doneBtn = (Button) findViewById(R.id.doneBtn);
            doneBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // After verification, we need to scan the sender files.

                    recordQRCode();

                }
            });

        }
    }

    public void recordQRCode(){

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

        // start the Video Capture Intent
        flag=0;

        System.out.println("**************TEST****************");
        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
    }

    @SuppressLint("SimpleDateFormat")
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE: {

                Bitmap frame = null;
                if (resultCode == RESULT_OK) {

                    // get user permission first
                    Uri videoUri = data.getData();
                    if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                        marshMallowPermission.requestPermissionForExternalStorage();
                    }
                    try {
                        // get duration of the video (unit: microsecond)
                        MediaPlayer player = new MediaPlayer();
                        player.setDataSource(this, videoUri);
                        player.prepare();
                        int duration = player.getDuration() * 1000;

                        // instantiate MediaMetadataRetriever for the video
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(this, videoUri);
                        Log.d("Appx", videoUri.toString());

                        // unit: microsecond
                        int time = 0;
                        Bitmap test;
                        while (time < duration) {

                            frame = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                            if (frame.getWidth() > frame.getHeight()) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                frame = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), matrix, true);
                            }
                            frame = Bitmap.createScaledBitmap(frame, 500, 800, false);

                            if (fileNames.size() != 0 && fileNames.size() % 5 == 0) {
                                test = BitmapFactory.decodeFile(fileNames.get(fileNames.size() - 5).getAbsolutePath());
                                if (checkframe(decodeqrRed(frame)).equals(checkframe(decodeqrRed(test)))) {
                                    // DO NOTHING HERE
                                } else {
                                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                                    if (pictureFile == null) {
                                        Log.d("App", "Error creating media file, check storage permissions: ");
                                        return;
                                    }
                                    try {
                                        FileOutputStream fos = new FileOutputStream(pictureFile);
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        frame.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        byte[] byteArray = stream.toByteArray();
                                        fos.write(byteArray);
                                        fos.close();
                                        Log.d("App", pictureFile.getAbsolutePath());
                                    } catch (FileNotFoundException e) {
                                        Log.d("App", "File not found: " + e.getMessage());
                                    } catch (IOException e) {
                                        Log.d("App", "Error accessing file: " + e.getMessage());
                                    }
                                }
                                test.recycle();
                                test = null;

                            } else {
                                if (checkframe(decodeqrRed(frame)).equals("00")) {

                                } else {
                                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                                    if (pictureFile == null) {
                                        Log.d("App", "Error creating media file, check storage permissions: ");
                                        return;
                                    }
                                    try {
                                        FileOutputStream fos = new FileOutputStream(pictureFile);
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        frame.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        byte[] byteArray = stream.toByteArray();
                                        fos.write(byteArray);
                                        fos.close();
                                        Log.d("App", pictureFile.getAbsolutePath());
                                    } catch (FileNotFoundException e) {
                                        Log.d("App", "File not found: " + e.getMessage());
                                    } catch (IOException e) {
                                        Log.d("App", "Error accessing file: " + e.getMessage());
                                    }
                                }
                                frame.recycle();
                                frame = null;
                                time += 200000;
                            }
                        }
                        Log.d("App", fileNames.toString());
                        String s = "";
                        try {
                            int frameNumber = 5;
                            Bitmap myBitmap;
                            int img = 0;
                            for (int color = 1; color <= (fileNames.size() / frameNumber) * 3; color++) {
                                // colorFrame = red 1,4,7 ; green 2,5,8; blue 3,6,9;
                                outerloop:
                                for (int j = 0; j < frameNumber; j++) {
                                    // /frames/second
                                    myBitmap = BitmapFactory.decodeFile(fileNames.get(img).getAbsolutePath());

                                    if (!checkframe(decodeqrRed(myBitmap)).equals("00")) {

                                        switch (color % 3) {
                                            case 1:
                                                if (decodeReal(decodeqrRed(myBitmap), color).equals("|checksumerror|")) {
                                                    s += "|checksumerror|";
                                                    color = Integer.parseInt(checkframe(decodeqrRed(myBitmap))) - 1;
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                    break outerloop;
                                                } else if (!decodeReal(decodeqrRed(myBitmap), color).equals("|NotFoundException|")
                                                        && !decodeReal(decodeqrRed(myBitmap), color).equals("|ChecksumException|")
                                                        && !decodeReal(decodeqrRed(myBitmap), color).equals("|FormatException|")) {
                                                    if (dup.contains(checkframe(decodeqrRed(myBitmap)))) {

                                                    } else {
                                                        s += decodeReal(decodeqrRed(myBitmap), color);
                                                        dup.add(checkframe(decodeqrRed(myBitmap)));
                                                        Log.d("App", dup.toString());
                                                        Log.d("App", "s= " + s);
                                                        Log.d("App", "img= " + img);
                                                        break outerloop;
                                                    }

                                                    // last frame cannot find
                                                } else if (j == (frameNumber - 1)) {
                                                    s += "|NotFoundException|";
                                                    img -= (frameNumber);
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                }
                                                break;
                                            case 2:
                                                if (decodeReal(decodeqrGreen(myBitmap), color).equals("|checksumerror|")) {
                                                    s += "|checksumerror|";
                                                    color = Integer.parseInt(checkframe(decodeqrGreen(myBitmap))) - 1;
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                    break outerloop;
                                                } else if (!decodeReal(decodeqrGreen(myBitmap), color).equals("|NotFoundException|")
                                                        && !decodeReal(decodeqrGreen(myBitmap), color).equals("|ChecksumException|")
                                                        && !decodeReal(decodeqrGreen(myBitmap), color).equals("|FormatException|")) {
                                                    if (dup.contains(checkframe(decodeqrGreen(myBitmap)))) {
                                                        //DO NOTHING
                                                    } else {
                                                        s += decodeReal(decodeqrGreen(myBitmap), color);
                                                        dup.add(checkframe(decodeqrGreen(myBitmap)));
                                                        Log.d("App", dup.toString());
                                                        Log.d("App", "s= " + s);
                                                        Log.d("App", "img= " + img);
                                                        break outerloop;
                                                    }
                                                } else if (j == (frameNumber - 1)) {
                                                    s += "|NotFoundException|";
                                                    img -= (frameNumber);
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                }
                                                break;
                                            case 0:
                                                if (decodeReal(decodeqrBlue(myBitmap), color).equals("|checksumerror|")) {
                                                    s += "|checksumerror|";
                                                    color = Integer.parseInt(checkframe(decodeqrBlue(myBitmap))) - 1;
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                    break outerloop;
                                                } else if (!decodeReal(decodeqrBlue(myBitmap), color).equals("|NotFoundException|")
                                                        && !decodeReal(decodeqrBlue(myBitmap), color).equals("|ChecksumException|")
                                                        && !decodeReal(decodeqrBlue(myBitmap), color).equals("|FormatException|")) {
                                                    if (dup.contains(checkframe(decodeqrBlue(myBitmap)))) {
                                                        //DO NOTHING
                                                    } else {
                                                        s += decodeReal(decodeqrBlue(myBitmap), color);
                                                        dup.add(checkframe(decodeqrBlue(myBitmap)));
                                                        img = img + (frameNumber - (img % frameNumber));
                                                        Log.d("App", dup.toString());
                                                        Log.d("App", "s= " + s);
                                                        Log.d("App", "img= " + img);
                                                        break outerloop;
                                                    }
                                                } else if (j == (frameNumber - 1)) {
                                                    s += "|NotFoundException|";
                                                    Log.d("App", "s= " + s);
                                                    Log.d("App", "img= " + img);
                                                }
                                                break;
                                        }
                                    }
                                    img++;
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {

                        } finally {

                            if (flag == 0) {
                                String strFilePath = WriteByteArrayToFile(s);
                                File file = new File(strFilePath);
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setDataAndType(Uri.fromFile(file), "text/plain");
                                startActivity(intent1);
                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                                s = "";
                                fileNames.clear();
                                dup.clear();
                            } else if (flag == 1) {
                                String strFilePath = WriteByteArrayToImage(s);
                                File file = new File(strFilePath);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

                                fileNames.clear();
                                dup.clear();
                                s = "";
                            }

                        }
                    } catch (Exception e) {
                        Log.e("GUN", Log.getStackTraceString(e));

                    }
                    System.gc();
                }
                break;

            }

        }

    }

    private String checkframe(Bitmap pic){
        Result result = null;
        int width1 = pic.getWidth(), height1 = pic.getHeight();
        int[] pixels = new int[width1 * height1];
        pic.getPixels(pixels, 0, width1, 0, 0, width1, height1);
        pic.recycle();
        pic = null;

        LuminanceSource source = new RGBLuminanceSource(width1, height1, pixels);
        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

        try {
            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            result = reader.decode(bBitmap,decodeHints);
        }
        catch (NotFoundException e) {
            Log.d("App","NotFoundException");
            return "null";

        } catch (ChecksumException e) {
            Log.d("App","ChecksumException");
            return "null";
        }
        catch (FormatException e) {
            Log.d("App","FormatException");
            return "null";
        }
        return result.toString().substring(0, 2).replaceAll(" ", "");
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
