package com.example.weijun.multiplexedqr;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoDemo extends Activity {

    private MediaController media_control;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        VideoView video_view = (VideoView) findViewById(R.id.VideoView);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.qr_video);
        Log.d("vid_path","==> " + uri.toString());

        media_control = new MediaController(this);
        video_view.setMediaController(media_control);

        video_view.setVideoURI(uri);
        video_view.start();
    }
}

// Library from wseemann github
//                        FFmpegMediaMetadataRetriever med = new FFmpegMediaMetadataRetriever();
//                        med.setDataSource(this,videoUri);
//                        // loop this
//                        for (int i = 0; i<10;i++) {
//                            Bitmap bmp = med.getFrameAtTime(i * 1000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
//                            Log.d("Appx", bmp.toString());
//                        }


// Decoding code from Esther here


// get duration of the video (unit: microsecond)
//                        MediaPlayer player = new MediaPlayer();
//                        player.setDataSource(EnterPasscode.this, videoUri);
//                        player.prepare();
//                        int duration = player.getDuration() * 1000;
//                        Log.d("Appx","duration : " + duration + " microsecond");

// instantiate MediaMetadataRetriever for the video
//  MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//  retriever.setDataSource(this, videoUri);



//                        // unit: microsecond
//                        int time = 0;
//                        Bitmap test;
//
//                        while (time < duration) {
//
//                            // This option is used with getFrameAtTime(long, int) to retrieve a frame
//                            // associated with a data source that is located closest to or at the given time.
//                            frame = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
//                            if (frame.getWidth() > frame.getHeight()) {
//                                Matrix matrix = new Matrix();
//                                matrix.postRotate(90);
//                                frame = Bitmap.createBitmap(frame, 0, 0, frame.getWidth(), frame.getHeight(), matrix, true);
//                            }
//
//                            frame = Bitmap.createScaledBitmap(frame, 500, 800, false);
//
//
//                            if (fileNames.size() != 0 && fileNames.size() % 5 == 0) {
//                                test = BitmapFactory.decodeFile(fileNames.get(fileNames.size() - 5).getAbsolutePath());
//                                Log.d("App","test code :  " + test);
//                                if (checkframe(decodeqrRed(frame)).equals(checkframe(decodeqrRed(test)))) {
//                                    // DO NOTHING HERE
//                                } else {
//                                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                                    if (pictureFile == null) {
//                                        Log.d("App", "Error creating media file, check storage permissions: ");
//                                        return;
//                                    }
//                                    try {
//                                        FileOutputStream fos = new FileOutputStream(pictureFile);
//                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                                        frame.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                                        byte[] byteArray = stream.toByteArray();
//                                        fos.write(byteArray);
//                                        fos.close();
//                                        Log.d("App", pictureFile.getAbsolutePath());
//                                    } catch (FileNotFoundException e) {
//                                        Log.d("App", "File not found: " + e.getMessage());
//                                    } catch (IOException e) {
//                                        Log.d("App", "Error accessing file: " + e.getMessage());
//                                    }
//                                }
//                                test.recycle();
//                                test = null;
//
//                            } else {
//
//                                    if (checkframe(decodeqrRed(frame)).equals("00")) {
//
//                                    } else {
//                                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                                        if (pictureFile == null) {
//                                            Log.d("App", "Error creating media file, check storage permissions: ");
//                                            return;
//                                        }
//                                        try {
//                                            FileOutputStream fos = new FileOutputStream(pictureFile);
//                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                                            frame.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                                            byte[] byteArray = stream.toByteArray();
//                                            fos.write(byteArray);
//                                            fos.close();
//                                            Log.d("App", pictureFile.getAbsolutePath());
//                                        } catch (FileNotFoundException e) {
//                                            Log.d("App", "File not found: " + e.getMessage());
//                                        } catch (IOException e) {
//                                            Log.d("App", "Error accessing file: " + e.getMessage());
//                                        }
//                                    }
//                                    frame.recycle();
//                                    frame = null;
//                                    time += 200000;
//                            }
//                        } // END WHILE STATEMENT
//
//
//                        Log.d("App", "Content here: "+ fileNames.toString());
//                        String s = "";
//
//                        try {
//                            int frameNumber = 5;
//                            Bitmap myBitmap;
//                            int img = 0;
//                            for (int color = 1; color <= (fileNames.size() / frameNumber) * 3; color++) {
//                                // colorFrame = red 1,4,7 ; green 2,5,8; blue 3,6,9;
//                                outerloop:
//                                for (int j = 0; j < frameNumber; j++) {
//                                    // /frames/second
//                                    myBitmap = BitmapFactory.decodeFile(fileNames.get(img).getAbsolutePath());
//
//                                    if (!checkframe(decodeqrRed(myBitmap)).equals("00")) {
//
//                                        switch (color % 3) {
//                                            case 1:
//                                                if (decodeReal(decodeqrRed(myBitmap), color).equals("|checksumerror|")) {
//                                                    s += "|checksumerror|";
//                                                    color = Integer.parseInt(checkframe(decodeqrRed(myBitmap))) - 1;
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                    break outerloop;
//                                                } else if (!decodeReal(decodeqrRed(myBitmap), color).equals("|NotFoundException|")
//                                                        && !decodeReal(decodeqrRed(myBitmap), color).equals("|ChecksumException|")
//                                                        && !decodeReal(decodeqrRed(myBitmap), color).equals("|FormatException|")) {
//                                                    if (dup.contains(checkframe(decodeqrRed(myBitmap)))) {
//
//                                                    } else {
//                                                        s += decodeReal(decodeqrRed(myBitmap), color);
//                                                        dup.add(checkframe(decodeqrRed(myBitmap)));
//                                                        Log.d("App", dup.toString());
//                                                        Log.d("App", "s= " + s);
//                                                        Log.d("App", "img= " + img);
//                                                        break outerloop;
//                                                    }
//
//                                                    // last frame cannot find
//                                                } else if (j == (frameNumber - 1)) {
//                                                    s += "|NotFoundException|";
//                                                    img -= (frameNumber);
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                }
//                                                break;
//                                            case 2:
//                                                if (decodeReal(decodeqrGreen(myBitmap), color).equals("|checksumerror|")) {
//                                                    s += "|checksumerror|";
//                                                    color = Integer.parseInt(checkframe(decodeqrGreen(myBitmap))) - 1;
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                    break outerloop;
//                                                } else if (!decodeReal(decodeqrGreen(myBitmap), color).equals("|NotFoundException|")
//                                                        && !decodeReal(decodeqrGreen(myBitmap), color).equals("|ChecksumException|")
//                                                        && !decodeReal(decodeqrGreen(myBitmap), color).equals("|FormatException|")) {
//                                                    if (dup.contains(checkframe(decodeqrGreen(myBitmap)))) {
//                                                        //DO NOTHING
//                                                    } else {
//                                                        s += decodeReal(decodeqrGreen(myBitmap), color);
//                                                        dup.add(checkframe(decodeqrGreen(myBitmap)));
//                                                        Log.d("App", dup.toString());
//                                                        Log.d("App", "s= " + s);
//                                                        Log.d("App", "img= " + img);
//                                                        break outerloop;
//                                                    }
//                                                } else if (j == (frameNumber - 1)) {
//                                                    s += "|NotFoundException|";
//                                                    img -= (frameNumber);
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                }
//                                                break;
//                                            case 0:
//                                                if (decodeReal(decodeqrBlue(myBitmap), color).equals("|checksumerror|")) {
//                                                    s += "|checksumerror|";
//                                                    color = Integer.parseInt(checkframe(decodeqrBlue(myBitmap))) - 1;
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                    break outerloop;
//                                                } else if (!decodeReal(decodeqrBlue(myBitmap), color).equals("|NotFoundException|")
//                                                        && !decodeReal(decodeqrBlue(myBitmap), color).equals("|ChecksumException|")
//                                                        && !decodeReal(decodeqrBlue(myBitmap), color).equals("|FormatException|")) {
//                                                    if (dup.contains(checkframe(decodeqrBlue(myBitmap)))) {
//                                                        //DO NOTHING
//                                                    } else {
//                                                        s += decodeReal(decodeqrBlue(myBitmap), color);
//                                                        dup.add(checkframe(decodeqrBlue(myBitmap)));
//                                                        img = img + (frameNumber - (img % frameNumber));
//                                                        Log.d("App", dup.toString());
//                                                        Log.d("App", "s= " + s);
//                                                        Log.d("App", "img= " + img);
//                                                        break outerloop;
//                                                    }
//                                                } else if (j == (frameNumber - 1)) {
//                                                    s += "|NotFoundException|";
//                                                    Log.d("App", "s= " + s);
//                                                    Log.d("App", "img= " + img);
//                                                }
//                                                break;
//                                        } // END SWITCH
//                                    } // END IF
//                                    img++;
//                                } // END FOR
//                            } // END OUTER FOR
//                        } catch (IndexOutOfBoundsException e) { // END TRY
//
//                        } finally {
//
//                            if (flag == 0) {
//                                String strFilePath = WriteByteArrayToFile(s);
//                                File file = new File(strFilePath);
//                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
//                                intent1.setDataAndType(Uri.fromFile(file), "text/plain");
//                                startActivity(intent1);
//                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
//                                s = "";
//                                fileNames.clear();
//                                dup.clear();
//                            } else if (flag == 1) {
//                                String strFilePath = WriteByteArrayToImage(s);
//                                File file = new File(strFilePath);
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
//                                startActivity(intent);
//                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
//
//                                fileNames.clear();
//                                dup.clear();
//                                s = "";
//                            }
//
//                        }





//    // Get store to the storage
//    String ImagePath = MediaStore.Images.Media.insertImage(
//            getContentResolver(),
//            bmFrame,
//            "demo_image",
//            "demo_image"
//    );
//
//    Uri URI = Uri.parse(ImagePath);
//   Log.d("Location","" + URI   + " | " + ImagePath );
//
//        Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show();