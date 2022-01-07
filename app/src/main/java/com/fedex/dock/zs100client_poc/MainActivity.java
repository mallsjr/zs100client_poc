package com.fedex.dock.zs100client_poc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public Messenger messenger;
    public Messenger localmessenger;
    public boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = CreateIntentForServiceInternal();
        List<ResolveInfo> list = this.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
        Log.i(TAG, "Service Found: " + !list.isEmpty());

        boolean isBound = bindService(intent, vendorScaleServiceConnection, BIND_AUTO_CREATE);

        Log.i(TAG, "Service Bound: " + isBound);

        if(isBound) {
            localmessenger = new Messenger(new VendorScaleHandler(this));
        }
    }

    public void getScaleInformation(View view) {
        Log.i(TAG, "getting scale information");
        Message message = Message.obtain(null, 90);
        message.replyTo = localmessenger;
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, "There was an error trying to send message", e);
        }
    }

    private Intent CreateIntentForServiceInternal() {
        ComponentName cn = new ComponentName("com.awt.zs100service", "com.awt.zs100service.WeighingService");
//        ComponentName cn = new ComponentName("com.awtx.zs100service", "com.awtx.zs100service.WeighingService");
        Intent serviceToStart = new Intent();
        serviceToStart.setComponent(cn);
        return serviceToStart;
    }

    public ServiceConnection vendorScaleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
            isConnected = messenger != null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service Disconnected");
            isConnected = false;
            messenger = null;
        }
    };

    static class VendorScaleHandler extends Handler {

        Context applicationContext;

        VendorScaleHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        String res = "";

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 100) {
                res = msg.getData().getString("GetScaleInfo_Key");
                Toast.makeText(applicationContext, res, Toast.LENGTH_LONG).show();
            } else {
                super.handleMessage(msg);
            }
        }
    }
}