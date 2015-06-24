package com.example.kimwj.smsimporter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Kimwj on 15. 6. 8..
 */
public class HeadlessSmsSendService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("KIMWJ", "onBind");
        return null;
    }
}
