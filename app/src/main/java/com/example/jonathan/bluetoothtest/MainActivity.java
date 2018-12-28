package com.example.jonathan.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.d(TAG, "name: " + device.getName() + " address: " + device.getAddress());
            try{
                tmp = device.createRfcommSocketToServiceRecord(mmDevice.getUuids()[0].getUuid());
            }catch(IOException e){
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run(){
            mBluetoothAdapter.cancelDiscovery();
            try{
                Log.d(TAG, "attempting connection");
                mmSocket.connect();
                Log.d(TAG, "connection successful");
            }catch(IOException connectException){
                try{
                    mmSocket.close();
                }catch(IOException closeException){
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG, "Could not close client socket", e);
            }
        }
    }

    private static final String TAG = "BluetoothTest";
    public static final int ENABLE_REQUEST_CODE = 1;
    public static final int DISCOVER_REQUEST_CODE = 2;
    public static final int PAIRED_REQUEST_CODE = 3;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String selectedDeviceName;
    String selectedDeviceAddress;

    File AUDIO_FILE = new File("testAudioFile");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void enableBluetooth(View view){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "Device doesn't support Bluetooth");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is already enabled");
            } else {
                Log.d(TAG, "Bluetooth is not enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_REQUEST_CODE);
            }
        }
    }

    public void getPairedDevices(View view){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            Log.d(TAG, "Paired Devices:");
            for(BluetoothDevice device : pairedDevices){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.d(TAG, "name: " + deviceName + " address: " + deviceHardwareAddress);
            }
        }
        Intent pairedIntent = new Intent(this, DiscoverDevices.class);
        pairedIntent.putExtra("requestCode", PAIRED_REQUEST_CODE);
        startActivityForResult(pairedIntent, PAIRED_REQUEST_CODE);
    }

    public void discoverBtDevices(View view) {
        Intent discoverIntent = new Intent(this, DiscoverDevices.class);
        discoverIntent.putExtra("requestCode", DISCOVER_REQUEST_CODE);
        startActivityForResult(discoverIntent, DISCOVER_REQUEST_CODE);
    }

    public void connectToDevice(View view) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(selectedDeviceAddress);
        Log.d(TAG,"name: " + device.getName());
        Log.d(TAG,"address: " + device.getAddress());
        Log.d(TAG,"bond state: " + device.getBondState());
        ConnectThread connection = new ConnectThread(device);
        connection.start();
    }

    public void recordVoice(View view) {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        recorder.setOutputFile(AUDIO_FILE);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        recorder.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedDeviceName = data.getStringExtra("deviceName");
        selectedDeviceAddress = data.getStringExtra("deviceHardwareAddress");
        ((TextView)findViewById(R.id.selected_device_name)).setText(selectedDeviceName);
    }
}