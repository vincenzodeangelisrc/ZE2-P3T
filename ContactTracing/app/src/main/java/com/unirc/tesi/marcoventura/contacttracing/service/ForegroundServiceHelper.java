package com.unirc.tesi.marcoventura.contacttracing.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.res.Configuration;

import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.unirc.tesi.marcoventura.contacttracing.R;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleConnection;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleContactPhase;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleContactServer;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleHandshake;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleKeepAlive;
import com.unirc.tesi.marcoventura.contacttracing.ble.BleSendHandshake;
import com.unirc.tesi.marcoventura.contacttracing.ble.SendClientData;
import com.unirc.tesi.marcoventura.contacttracing.cipher.MakeHash;
import com.unirc.tesi.marcoventura.contacttracing.database.SQLiteHelper;
import com.unirc.tesi.marcoventura.contacttracing.token.Token;
import com.unirc.tesi.marcoventura.contacttracing.token.TokenHelper;
import com.unirc.tesi.marcoventura.contacttracing.util.CalculateCentroid;
import com.unirc.tesi.marcoventura.contacttracing.util.JSONHelper;
import com.unirc.tesi.marcoventura.contacttracing.util.TSPHelper;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.unirc.tesi.marcoventura.contacttracing.util.NotificationHelper.getNotification;


public class ForegroundServiceHelper extends Service {

    public static final String CHANNEL_ID = "com.unirc.tesi.marcoventura.contacttracing.service.channel_id";
    public static final String EXTRA_NOTIFICATION = "com.unirc.tesi.marcoventura.contacttracing.service.notification";

    private final IBinder iBinder = new LocalBinder();

    private static final int NOTIFICATION_ID = 1223;
    private boolean changingConfiguration = false;

    private Handler serviceHandler;

    // Bluetooth
    private BluetoothLeScanner mBluetoothLeScanner;
    ArrayList<BleHandshake> handshakes = new ArrayList<>();
    ArrayList<BleContactPhase> temporary_contacts = new ArrayList<>();
    ArrayList<BleKeepAlive> keepAlives = new ArrayList<>();
    ArrayList<BleConnection> bleConnections = new ArrayList<>();
    ArrayList<BleSendHandshake> bleSendHandshakes = new ArrayList<>();
    BluetoothLeAdvertiser advertiser;
    AdvertiseCallback advertisingCallback;
    boolean timer_keep_state = false;

    // Location
    private static final long UPDATE_LOCATION_INTERVAL = 60000; // 1 minuto
    private static final long UPDATE_FASTEST_LOCATION_INTERVAL = UPDATE_LOCATION_INTERVAL / 2;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallback;
    private Location location;

    private int max_time = 10000; // 10 seconds
    private int min_time = 0;

    private static boolean is_send = false;

    static SQLiteHelper database;
    static String random_identifier;

    private int Tp = 3 * 1000 * 60; // 15 minute

    private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();

            if (device == null)
                return;


            String message = new String(
                    Objects.requireNonNull(
                            result.getScanRecord()
                    )
                            .getServiceData(
                                    result.getScanRecord()
                                            .getServiceUuids()
                                            .get(0)
                            ),
                    StandardCharsets.UTF_8);

            String char_syn = "+";
            String char_keep_alive = "-";
            String first_char = String.valueOf(message.charAt(0));

            // ----- synchronization message -----
            if (char_syn.equals(first_char)) {

                String random_mac = message.substring(1);

                for (int i = 0; i < handshakes.size(); i++) {
                    if (handshakes.get(i).getMac_address().equals(random_mac))
                        return;
                }

                BleHandshake handshake = new BleHandshake(random_mac, 0,0,0, 0.0, "",false, true);
                Log.d("Device", random_mac);
                handshakes.add(handshake);

                BleKeepAlive keep = new BleKeepAlive(random_mac, true);
                keepAlives.add(keep);

                BleConnection connection = new BleConnection(random_mac, System.currentTimeMillis());
                bleConnections.add(connection);

                String random = TokenHelper.generateRandom();
                Token token = new Token(0, random, random_identifier);

                if (database.saveToken(token))
                    Log.d("Token Saved", random);

                //is_calculate = false;

                sendHandshakeData(handshake);

            // ----- keep alive message -----
            } else if(char_keep_alive.equals(first_char)) {

                String identifier = message.substring(1);

                for (int i = 0; i<keepAlives.size(); i++) {
                    if (keepAlives.get(i).getAddress().equals(identifier)){
                        keepAlives.get(i).setReceived(true);
                    }
                }

                // ----- data message -----
            } else {

                String[] msg = message.split(",");
                String mac_random = msg[0];

                String timestamp_char = "0";
                String random_time_and_centroid_char = "1";
                //String centroid_char = "2";
                String distance_char = "3";

                String type_of_message = String.valueOf(msg[1].charAt(0));

                // ----- check message -----
                BleHandshake ble_handshake = new BleHandshake();
                for (int i = 0; i<handshakes.size(); i++) {
                    if (handshakes.get(i).getMac_address().equals(mac_random)) {
                        ble_handshake = handshakes.get(i);
                    }
                }

                if (ble_handshake.isCalculate_parameter()){
                    return;
                }

                if (type_of_message.equals(timestamp_char) && ble_handshake.getTimestamp() == 0) {

                    ble_handshake.setTimestamp(Long.parseLong(msg[1].substring(1)));
                    Log.d("ble_hand_timestamp", msg[1].substring(1));

                } else if (type_of_message.equals(random_time_and_centroid_char) && ble_handshake.getRandom() == 0 && ble_handshake.getCentroid() == 0) {

                    ble_handshake.setRandom(Integer.parseInt(msg[1].substring(1)));
                    Log.d("ble_hand_random", msg[1].substring(1));

                    ble_handshake.setCentroid(Integer.parseInt(msg[2]));
                    Log.d("ble_hand_centroid", msg[2]);


                } else  if (type_of_message.equals(distance_char) && ble_handshake.getDistance() == 0.0) {

                    ble_handshake.setDistance(Double.parseDouble(msg[1].substring(1)));
                    Log.d("ble_hand_distance", msg[1].substring(1));

                }

                // ----- end check message -----

                // ----- check all value of BleHandshake -----

                if (ble_handshake.getTimestamp() == 0 || ble_handshake.getRandom() == 0
                        || ble_handshake.getCentroid() == 0 || ble_handshake.getDistance() == 0.0) {
                    return;
                }

                if (!ble_handshake.isCalculate_parameter()) {
                    //Log.d("Handshake", "Calculate Parameter...");
                    calculateHandshakeParameter(ble_handshake);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };

    private void calculateHandshakeParameter(final BleHandshake ble_handshake) {

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (is_send){

                    // Bluetooth Handshake
                    timer.cancel();
                    is_send = false;

                    long timestamp_device = ble_handshake.getTimestamp();
                    int random_time_device = ble_handshake.getRandom();
                    int centroid_device = ble_handshake.getCentroid();
                    double distance_device = ble_handshake.getDistance();

                    String address = ble_handshake.getMac_address();
                    BleSendHandshake bleSendHandshake = new BleSendHandshake();
                    for (BleSendHandshake ble: bleSendHandshakes){
                        if (ble.getAddress_to_tx().equals(address)){
                            bleSendHandshake = ble;
                        }
                    }

                    long final_timestamp = (bleSendHandshake.getTimestamp_tx() + timestamp_device) / 2;
                    int final_random_time = (bleSendHandshake.getRandom_tx() + random_time_device) / 2;

                    int final_centroid = 0;
                    if (distance_device < bleSendHandshake.getDistance_tx())
                        final_centroid = centroid_device;
                    else
                        final_centroid = bleSendHandshake.getCentroid_tx();

                    Log.d("Timestamp", String.valueOf(final_timestamp));
                    Log.d("Random", String.valueOf(final_random_time));
                    Log.d("Centroid", String.valueOf(final_centroid));

                    StringBuilder builder = new StringBuilder();
                    builder.append(final_centroid);
                    builder.append(TSPHelper.getSalt());

                    Log.d("String before hash", builder.toString());

                    String hash = "";
                    try {

                        hash = MakeHash.generateSHA(builder.toString());
                        Log.d("Hash", hash);

                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    ble_handshake.setTimestamp(final_timestamp);
                    ble_handshake.setRandom(final_random_time);
                    ble_handshake.setCentroid(final_centroid);
                    ble_handshake.setHash(hash);

                    /*
                    ArrayList<Token> all_token = database.readAllToken();
                    for (Token token : all_token)
                        Log.d("Token", token.getRandom_token());

                     */
                    ble_handshake.setCalculate_parameter(true);
                }
            }
        },0, 25);


    }

    private void sendHandshakeData(BleHandshake handshake) {

        initGPSDataAfterBtConnection(handshake);

    }

    private void BleContactPhase(BleHandshake handshake, Location location) {

        if(!timer_keep_state) {
            timer_keep_state = true;
            String keep_alive = "-" + random_identifier;
            advertise(keep_alive);
        }

        String address = handshake.getMac_address();
        BleSendHandshake bleSendHandshake = new BleSendHandshake();
        for (BleSendHandshake ble: bleSendHandshakes){
            if (ble.getAddress_to_tx().equals(address)){
                bleSendHandshake = ble;
            }
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double distance_to_common_centroid;

        if (handshake.getDistance() < bleSendHandshake.getDistance_tx()) {
            distance_to_common_centroid = CalculateCentroid.calculateDistanceFromCommonCentroid(latitude, longitude, handshake.getDistance());
        }else {
            distance_to_common_centroid = CalculateCentroid.calculateDistanceFromCommonCentroid(latitude, longitude, bleSendHandshake.getDistance_tx());
        }

        double angle = CalculateCentroid.angleFromCoordinate(latitude, longitude);
        long timestamp = location.getTime();

        BleContactPhase phase = new BleContactPhase(handshake.getMac_address(), distance_to_common_centroid, angle, timestamp);
        temporary_contacts.add(phase);

        String result = JSONHelper.getJsonFromObject(phase);
        Log.d("Add Contact Phase...", result);


        BleKeepAlive bleKeepAlive = new BleKeepAlive("", false);
        for (int i = 0; i<keepAlives.size(); i++){
            if (keepAlives.get(i).getAddress().equals(handshake.getMac_address())){
                bleKeepAlive = keepAlives.get(i);
            }
        }


        if (bleKeepAlive.isReceived()) {
            bleKeepAlive.setReceived(false);
        } else {
            connectionLost(handshake);
            timer_keep_state = false;
            advertiser.stopAdvertising(advertisingCallback);

        }


    }

    private void connectionLost(final BleHandshake handshake) {

        final String address = handshake.getMac_address();
        Log.d("connection", "lost");

        long end_timestamp = System.currentTimeMillis();
        long start_timestamp = 0;

        for (int i = 0; i < bleConnections.size(); i++){
            if (bleConnections.get(i).getAddress().equals(handshake.getMac_address())){
                start_timestamp = bleConnections.get(i).getStart_timestamp();
            }
        }


        final long time_of_contact = end_timestamp - start_timestamp;

        if (time_of_contact > 0 && time_of_contact < Tp) {

            Log.d("Time of Contact", "Less than 15 minutes, clear temporary contact...");

            for(int i = 0; i < temporary_contacts.size(); i++){
                if (temporary_contacts.get(i).getMac_address().equals(address)) {
                    temporary_contacts.remove(i);
                }
            }

            advertise_announcement();


        } else if (time_of_contact > 0 && time_of_contact > Tp){

            Log.d("Time of Contact", "More than 15 minutes, save temporary contact...");

            for (int i = 0; i < temporary_contacts.size(); i++){

                if (temporary_contacts.get(i).getMac_address().equals(address)) {
                    long tmp = temporary_contacts.get(i).getTimestamp();
                    long common_timestamp = handshake.getTimestamp();
                    long difference = tmp - common_timestamp;

                    if (difference < 0)
                        difference *= -1;

                    long final_timestamp = tmp + Tp + difference;
                    temporary_contacts.get(i).setTimestamp(final_timestamp);

                    // save in DB
                    if (database.saveContact(temporary_contacts.get(i)))
                        Log.d("Contact", "saved");
                    else
                        Log.d("Contact", "Not Saved");
                }

            }

            // Send to Server

            final long time_to_server = handshake.getTimestamp() + Tp + handshake.getRandom();
            long delay = (time_to_server - System.currentTimeMillis()) / 60 ;

            if (delay < 0)
                delay = delay * (-1);

            Log.d("DELAY", String.valueOf(delay));

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    ArrayList<BleContactServer> list = new ArrayList<>();

                    for (int i = 0; i < temporary_contacts.size(); i++){
                        if (temporary_contacts.get(i).getMac_address().equals(address)) {
                            double distance = temporary_contacts.get(i).getDistance();
                            double angle = temporary_contacts.get(i).getAngle();
                            long timestamp = temporary_contacts.get(i).getTimestamp();
                            BleContactServer phase = new BleContactServer(distance, angle, timestamp);
                            list.add(phase);
                        }
                    }

                    ArrayList<Token> all_token = database.readAllToken();
                    String token = all_token.get(all_token.size() - 1).getRandom_token();

                    String digest = handshake.getHash();

                    SendClientData clientData = new SendClientData(token, digest, time_to_server, list, time_of_contact);
                    String json = JSONHelper.getJsonFromObject(clientData);

                    Log.d("Send Data to Server...", json);

                    // clear contact
                    for(int i = 0; i < temporary_contacts.size(); i++){
                        if (temporary_contacts.get(i).getMac_address().equals(address)) {
                            temporary_contacts.remove(i);
                        }
                    }

                    handshake.setContinue_gps(false);
                    timer.cancel();

                    advertise_announcement();

                }
            },delay);

        }


    }


    private void initGPSDataAfterBtConnection(final BleHandshake handshake) {

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (!handshake.isContinue_gps()){
                    if (handshakes.size() == 1)
                        stopLocationUpdates();
                    return;
                }

                if (!handshake.isCalculate_parameter()) {
                    getGPSData(locationResult.getLastLocation(), handshake.getMac_address());
                }else{
                    BleContactPhase(handshake, locationResult.getLastLocation());
                }
                alternateLocationUpdates();
            }
        };

        buildLocationRequest();

        HandlerThread handlerThread = new HandlerThread("ContactTracing");
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());

        startLocationUpdates();

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getGPSData(Location lastLocation, String mac_address) {

        location = lastLocation;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        long timestamp = location.getTime();
        int random_time = (int) (Math.random() * (max_time - min_time + 1) + min_time);

        LatLng latLng_user = new LatLng(latitude, longitude);
        double distance = CalculateCentroid.calculateDistanceFromCentralCentroid(latLng_user);

        int centroid = CalculateCentroid.getCentroid(latitude, longitude);


        BleSendHandshake sendHandshake = new BleSendHandshake(mac_address, timestamp, random_time, centroid, distance);
        bleSendHandshakes.add(sendHandshake);

        final String timestamp_to_send = random_identifier + ",0"+ String.valueOf(timestamp);

        final Timer timer = new Timer();
        final Timer timer2 = new Timer();
        //final Timer timer3 = new Timer();
        final Timer timer4 = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                advertise(timestamp_to_send);
                timer.cancel();
            }
        }, 800);

        final String random_and_centroid_to_send = random_identifier + ",1"+ String.valueOf(random_time) + "," + String.valueOf(centroid);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                advertise(random_and_centroid_to_send);
                timer2.cancel();
            }
        }, 800);

        /*
        final String centroid_to_send = random_identifier + ",2" + String.valueOf(centroid);

        timer3.schedule(new TimerTask() {
            @Override
            public void run() {
                advertise(centroid_to_send);
                timer3.cancel();
            }
        }, 100);

         */

        final String distance_to_send = random_identifier + ",3"+ String.valueOf(distance);

        timer4.schedule(new TimerTask() {
            @Override
            public void run() {
                advertise(distance_to_send);
                timer4.cancel();
            }
        }, 800);

        is_send = true;

    }


    public ForegroundServiceHelper() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changingConfiguration = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        random_identifier = TokenHelper.generateMacAddressRandom();

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        database = new SQLiteHelper(this);

        HandlerThread handlerThread = new HandlerThread("ContactTracing");
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        discover();
        advertise_announcement();
    }

    private void advertise_announcement(){
        String syn_to_send = "+" + random_identifier;
        Log.d("Announcement... ", syn_to_send);
        advertise(syn_to_send);
    }


    private void discover() {
        List<ScanFilter> filters = new ArrayList<ScanFilter>();

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(UUID.fromString( getString(R.string.ble_uuid ) ) ) )
                .build();
        filters.add( filter );

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        /*
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 100000);

         */
    }

    private void advertise(final String message) {

        if (advertisingCallback != null) {
            advertiser.stopAdvertising(advertisingCallback);
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        final AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .addServiceUuid(pUuid)
                    .addServiceData(pUuid, message.getBytes(StandardCharsets.UTF_8))
                    .build();

        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d("Advertise...", message);
                super.onStartSuccess(settingsInEffect);

            }

            @Override
            public void onStartFailure(int errorCode) {

                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        return iBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!changingConfiguration) {
            startForeground(NOTIFICATION_ID, getNotification(this));
        }
        return true;
    }

    @Override
    public void onDestroy() {
        serviceHandler.removeCallbacks(null);
        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        locationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }


    public void stopLocationUpdates(){

        locationProviderClient.removeLocationUpdates(locationCallback);

    }


    private void alternateLocationUpdates() {

        stopLocationUpdates();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }, UPDATE_LOCATION_INTERVAL);
    }


    public class LocalBinder extends Binder {

        public ForegroundServiceHelper getService() {
            return ForegroundServiceHelper.this;
        }
    }
}
