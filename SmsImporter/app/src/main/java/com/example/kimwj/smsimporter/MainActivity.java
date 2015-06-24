package com.example.kimwj.smsimporter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    SmsReceiver smsReceiver = new SmsReceiver();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startCursorTest();

    }


    private void startCursorTest(){
        Cursor c = getCursor();
        c.moveToFirst();
        for(int i = 0; i < 10; i++){
            String number = c.getString(1);
            Log.v("KIMWJ", "number : " + number);
            c.moveToNext();
        }
    }

    private Cursor getCursor(){
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        MatrixCursor extras = new MatrixCursor(new String[]{"_id","title"});
        extras.addRow(new String[]{"-1","test1"});
        extras.addRow(new String[]{"-2","test2"});
        Cursor extendedCursor = new MergeCursor(new Cursor[]{extras, c});
        return extendedCursor;
    }

    private void startSms(){
        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage("not default");
            b.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent =
                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            myPackageName);
                    startActivity(intent);
                }
            });
            b.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            b.create().show();
        }

        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(SmsReceiver.getInstance(), smsIntentFilter);

        IntentFilter mmsIntentFilter = new IntentFilter();
        mmsIntentFilter.addAction("android.provider.Telephony.WAP_PUSH_DELIVER");
        registerReceiver(new MmsReceiver(), mmsIntentFilter);
    }

    private Cursor getSmsCursor(long startTime, long destTime){
        StringBuilder filter = new StringBuilder("");

        if(startTime > 0){
            if(filter.length() > 0)
                filter.append( " AND ");
            filter.append( CallLog.Calls.DATE +">'").append(startTime).append("'");
        }

        if(destTime > 0){
            if(filter.length() > 0)
                filter.append(" AND ");
            filter.append( CallLog.Calls.DATE +"<'").append(destTime).append("'");
        }

        if(filter.length() > 0)
            filter.append( " AND ");
        filter.append( CallLog.Calls.TYPE ).append("<=").append("2");


        return getContentResolver().query(Uri.parse("content://sms/"), new String[]{"count(*)"}, filter.toString(), null, null);
    }

//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
}
