package com.example.weijun.multiplexedqr;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ReceiverView extends Activity {


    TextView countdown;
    TextView displayPortNo;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_view);

        countdown = (TextView) findViewById(R.id.lbl_timer);
        displayPortNo = (TextView) findViewById(R.id.lbl_receivePortNum);


        // get and display port number
        final Intent intent = getIntent();
        String portNo = intent.getStringExtra("QRResult");

        displayPortNo.setText(portNo);


        // countdown to intent next activity
        CountDownTimer Count = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                countdown.setText(""+millisUntilFinished / 1000);
            }

            public void onFinish() {
                countdown.setText("Finished");
                Intent intent = new Intent(ReceiverView.this, ReceiverInputView.class);
                startActivity(intent);
            }
        };

        Count.start();


        // do the same for next button
        nextBtn = (Button) findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ReceiverView.this, ReceiverInputView.class);
                startActivity(intent);
            }
        });
    }

}
