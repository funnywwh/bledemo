package com.telyes.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE";
    private ParcelUuid Service_UUID = ParcelUuid.fromString("22ad04cd-1b83-4559-93b6-a9a2fc16d79e");
    private SampleAdvertiseCallback mAdvertiseCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private SampleScanCallback mScanCallback;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mRunning;
    private View mBroadcastView;
    private View mScanView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,Service_UUID.toString());

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBroadcastView = findViewById(R.id.broadcast);
        mBroadcastView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBroadcast();
            }
        });
        mScanView = findViewById(R.id.scan);
        mScanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScan();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onBroadcast(){
        if(mRunning == false){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startAdvertising();
                }
            }).start();;

//            mRunning = true;
//            if(mScanView != null){
//                mScanView.setVisibility(View.GONE);
//            }
        }else{
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            if(mScanView != null){
                mScanView.setVisibility(View.VISIBLE);
            }
            mRunning = false;
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onScan(){
        if(mRunning == false){
            startScanning();
//            mRunning = true;
//            if(mBroadcastView != null){
//                mBroadcastView.setVisibility(View.GONE);
//            }
        }else{
            mRunning = false;
            if(mBroadcastView != null){
                mBroadcastView.setVisibility(View.VISIBLE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Advertising failed ");
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"failed",Toast.LENGTH_SHORT).show();
                    if(mScanView != null){
                        mScanView.setVisibility(View.VISIBLE);
                    }

                }
            });
            mRunning = false;
        }

        @Override
        public void onStartSuccess(final AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.e(TAG, "Advertising successfully started");
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,settingsInEffect.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }    
    /**
     * 设置频率:
     * ADVERTISE_MODE_LOW_LATENCY 100ms
     * ADVERTISE_MODE_LOW_POWER 1s
     * ADVERTISE_MODE_BALANCED  250ms
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }
    /**
     * 设置 serviceUuid
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData buildAdvertiseData() {
        String tmp = Service_UUID.toString();
//        String uuid = "12345678"+tmp.substring(8);
        String uuid = tmp;
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

        Log.d(TAG,uuid);
        ParcelUuid parcelUuid = ParcelUuid.fromString(uuid);
        dataBuilder.addServiceUuid(parcelUuid);
        dataBuilder.setIncludeDeviceName(false); // 是否包含设备名称
//        dataBuilder.addServiceData(Service_UUID, sendDatas);
        return dataBuilder.build();
    }
    /**
     * 开始蓝牙广播
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAdvertising() {
        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();

            mAdvertiseCallback = new SampleAdvertiseCallback();
            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you




        ParcelUuid mask = ParcelUuid.fromString("00000000-ffff-ffff-ffff-ffffffffffff");

        builder.setServiceUuid(Service_UUID,mask);
//        builder.setServiceUuid(Service_UUID);
        scanFilters.add(builder.build());
        return scanFilters;
    }

    Handler mUIHandler = new Handler();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();

            if(record.getServiceUuids() != null && record.getServiceUuids().size() > 0 ){
                final String hex  =  record.getServiceUuids().get(0).toString(); //parseAdverData(record.getBytes());
//            final String hex = Base64.encodeToString(data,data.length);

                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,hex,Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "data = " +hex);

            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            mRunning = false;
            if(mBroadcastView != null){
                mBroadcastView.setVisibility(View.VISIBLE);
            }
            Log.e(TAG, "Scan failed with error: " + errorCode);
        }
    }
    public static byte[] parseAdverData(byte[] scanRecord) {
        int len = scanRecord.length;
        int start = 0;
        for (int i = 0; i < len - 1; i++) {
            if ((scanRecord[i] & 0xff) == 0xb8
                    && (scanRecord[i + 1] & 0xff) == 0x06) {
                start = i + 5;
                break;
            }
        }

        byte[] data = new byte[3];
        data[0] = scanRecord[start];
        data[1] = scanRecord[start + 1];
        data[2] = scanRecord[start + 2];

        return data;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startScanning() {
        if (mScanCallback == null) {
            Log.d(TAG, "Starting Scanning");

            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
        }
    }
}
