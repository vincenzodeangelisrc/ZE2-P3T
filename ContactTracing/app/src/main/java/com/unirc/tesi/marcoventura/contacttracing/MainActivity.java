package com.unirc.tesi.marcoventura.contacttracing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature.BlindSignature;
import com.unirc.tesi.marcoventura.contacttracing.database.SQLiteHelper;
import com.unirc.tesi.marcoventura.contacttracing.service.ForegroundServiceHelper;
import com.unirc.tesi.marcoventura.contacttracing.token.SendSignedToken;
import com.unirc.tesi.marcoventura.contacttracing.token.Token;
import com.unirc.tesi.marcoventura.contacttracing.util.JSONHelper;

import java.util.ArrayList;

import static com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature.BlindSignature.signature;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeScanner mBluetoothLeScanner;

    ForegroundServiceHelper serviceHelper = new ForegroundServiceHelper();
    boolean bound = false;

    static MainActivity instance;

    SQLiteDatabase myDB;
    SQLiteHelper database;

    Button btn_blind_signature;
    private static final int REQUEST_ENABLE_BT = 3;


    public static MainActivity getMainActivityInstance(){
        return instance;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            ForegroundServiceHelper.LocalBinder binder = (ForegroundServiceHelper.LocalBinder) service;
            serviceHelper = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceHelper = null;
            bound = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDB = new SQLiteHelper(this).getWritableDatabase();
        database = new SQLiteHelper(this);

        instance = this;

        // Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(new String[]{
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.INTERNET}
                            , 10);
                } else {
                    requestPermissions(new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.INTERNET}
                            , 10);
                }
            }
            return;
        }

        checkBluetoothStatus();


        // Exposure Notification
        btn_blind_signature = findViewById(R.id.btn_blind_signature);
        btn_blind_signature.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                BlindSignature.executeBlindSignature(MainActivity.this);

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10)
            checkBluetoothStatus();
    }

    private void checkBluetoothStatus() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }


        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
            Toast.makeText( this, "Multiple advertisement not supported", Toast.LENGTH_SHORT ).show();
            finish();

        } else {

            startBackgroundService();
        }

    }

    private void startBackgroundService() {

        Log.d("Service", "Starting...");
        Intent serviceIntent = new Intent(this, ForegroundServiceHelper.class);
        startService(serviceIntent);

        bindService(new Intent(MainActivity.this, ForegroundServiceHelper.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        instance = null;
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK)
            startBackgroundService();
        else
            Log.d("Bluetooth", "Not Enable!");
    }
}