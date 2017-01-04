package com.example.weijun.multiplexedqr;


import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;



public class MutualPasscode extends Activity {


    public final static String EXTRA_MESSAGE = "com.example.MULTIPLEXEDQR";

    final int ACTIVITY_CHOOSE_FILE = 4;



    private TextView lblPortNumber;
    private EditText editPasscode;
    private Button verificationBtn;


    private String portNumber;


    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutual_passcode);


        Intent intent = getIntent();
        portNumber = intent.getStringExtra("PortNumber");
        lblPortNumber = (TextView) findViewById(R.id.lblPortNo_Result);
        lblPortNumber.setText(portNumber);

        verificationBtn = (Button)findViewById(R.id.btn_verify);
        verificationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                // Read QR Code
                verificationBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if (!marshMallowPermission.checkPermissionForCamera()) {
                            marshMallowPermission.requestPermissionForCamera();
                        }
                        if (!marshMallowPermission.checkPermissionForRecord()) {
                            marshMallowPermission.requestPermissionForRecord();
                        }
                        try {

                            IntentIntegrator integrator = new IntentIntegrator(MutualPasscode.this);
                            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                            integrator.setPrompt("Scan a QR code");
                            integrator.setOrientationLocked(false);
                            integrator.setCameraId(0);  // Use a specific camera of the device
                            integrator.setBeepEnabled(false);
                            integrator.initiateScan();


                        } catch (Exception e) {

                            System.out.println("ERROR IN QR PARSING");

                        }
                    }
                });

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // FOR QR RECEIVING CONTENT
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String msg = result.getContents();


                editPasscode = (EditText) findViewById(R.id.edit_passcode);


                // remove unnecessary character after the ending flag = '//'
                String[] parts = msg.split("//");
                String refine_msg = parts[0];


                String passCode = editPasscode.getText().toString();

                String _msg = portNumber + "{}" + passCode;

                /*
                System.out.println("PASSCODE VALUE :: " + passCode + " PORT NUMBER ::" + portNumber);
                System.out.println("MSG VALUE :: " + _msg);
                System.out.println("QR Read VALUE :: " + refine_msg);
                */

                if (refine_msg.equals(_msg)) {
                    sendMessage();
                } else {
                    System.out.println("ERROR");
                    Toast.makeText(getApplicationContext(), "INVALID PORT NO / PASSCODE. PLEASE TRY AGAIN.", Toast.LENGTH_LONG).show();
                }

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
        }

    }


    private void sendMessage() {
        String manufactures = android.os.Build.MANUFACTURER;
        if(manufactures.equalsIgnoreCase("samsung")){
            Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            intent.putExtra("CONTENT_TYPE", "*/*");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        } else{
            Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
            selectFile.setType("*/*");
            startActivityForResult(selectFile,ACTIVITY_CHOOSE_FILE);  }
    }



    }
