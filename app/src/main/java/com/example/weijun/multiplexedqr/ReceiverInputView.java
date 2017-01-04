package com.example.weijun.multiplexedqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ReceiverInputView extends Activity {

    private EditText portNumber,passCode;
    private Button encodeBtn;

    SenderView encoder = new SenderView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_input_view);


        portNumber = (EditText)findViewById(R.id.edit_portnum);
        passCode = (EditText)findViewById(R.id.edit_passcode);
        encodeBtn = (Button)findViewById(R.id.btn_encode);




        encodeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String portno = portNumber.getText().toString();
                String passcode = passCode.getText().toString();

                String msg = portno + "{}" + passcode + "//";
                System.out.println(msg);
                Bitmap bmp =  encoder.encodeStringQR(msg);
                Intent intent = new Intent(ReceiverInputView.this, DisplayPortQR.class);
                intent.putExtra("BitmapPort_R", bmp);
                intent.putExtra("RECEIVER",true);
                startActivity(intent);



                // submit the port number and passcode
                /*
                Intent intent = new Intent(MutualPasscode.this, MutualPasscode.class);
                intent.putExtra("PortNumber", portNumber);
                intent.putExtra("PassCode", passCode);
                startActivity(intent);
                */
            }
        });






    }

}
