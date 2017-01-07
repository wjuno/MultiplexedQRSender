package com.example.weijun.multiplexedqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Hashtable;

public class SenderView extends Activity {

    private Button submitBtn;

    // initialize member
    private EditText portText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender_view);

        portText = (EditText)findViewById(R.id.edit_portNo);

        submitBtn = (Button) findViewById(R.id.btn_encode);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send get the port number for the edit text
                String value = portText.getText().toString();
                System.out.println(value);
                Bitmap bmp =  encodeStringQR(value);
                Intent intent = new Intent(SenderView.this, DisplayPortQR.class);
                intent.putExtra("BitmapPort", bmp);
                intent.putExtra("PortNumber", value);
                intent.putExtra("SENDER",true);
                startActivity(intent);
            }
        });

    }


    /**
     *
     *  Example for getting value from EditText
     *
     EditText text = (EditText)findViewById(R.id.edit_portNo);
     String value = text.getText().toString();
     System.out.println(value);

     */
    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first


    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first

        // Activity being restarted from stopped state
    }



    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass

        // Stop method tracing that the activity started during onCreate()
        android.os.Debug.stopMethodTracing();
    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
    }



    public Bitmap encodeStringQR(String msg){


        Bitmap bmp = null;
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        byte[] b = null;
        try {
            // Convert a string to UTF-8 bytes in a ByteBuffer

            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(msg));
            b = bbuf.array();
        } catch (CharacterCodingException e) {
            System.out.println(e.getMessage());
        }

        String data;

        try {
            data = new String(b, "UTF-8");
            // get a byte matrix for the data
            BitMatrix matrix = null;
            int h = 100;
            int w = 100;
            com.google.zxing.Writer writer = new MultiFormatWriter();
            try {
                Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>(2);
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                matrix = writer.encode(data,
                        com.google.zxing.BarcodeFormat.QR_CODE, w, h, hints);
            } catch (com.google.zxing.WriterException e) {
                System.out.println(e.getMessage());
            }

            int height = matrix.getHeight();
            int width = matrix.getWidth();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++){
                for (int y = 0; y < height; y++){
                    bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        return bmp;

    }

}
