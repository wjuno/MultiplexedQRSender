package com.example.weijun.multiplexedqr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;




public class SelectFiles extends Activity {

    public final static String EXTRA_MESSAGE = "com.example.MULTIPLEXEDQR";


    final int ACTIVITY_CHOOSE_FILE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_files);
        sendMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


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

    public void backButton(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
