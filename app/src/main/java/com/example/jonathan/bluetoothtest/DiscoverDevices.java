package com.example.jonathan.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class DiscoverDevices extends AppCompatActivity {

    private static final String TAG = "BluetoothTest";

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    LinearLayout deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_devices);
        deviceList = findViewById(R.id.devices_discovered);
        Intent intent = getIntent();
        if(intent.getIntExtra("requestCode") == MainActivity.DISCOVER_REQUEST_CODE)) {
            discoverBtDevices();
        } else if(intent.getString)
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LinearLayout devicesDiscovered = findViewById(R.id.devices_discovered);
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                createButton(deviceName, deviceHardwareAddress);
            }
        }
    };

    private void createButton(final String deviceName, final String deviceHardwareAddress){
        Button b = new Button(this);
        b.setText(deviceName);
        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent returnIntent = new Intent();
                returnIntent.putExtra("deviceName", deviceName);
                returnIntent.putExtra("deviceHardwareAddress", deviceHardwareAddress);
                mBluetoothAdapter.cancelDiscovery();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        deviceList.addView(b);
    }

    public void discoverBtDevices(){
        Log.d(TAG, "discovery started " + mBluetoothAdapter.startDiscovery());

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
