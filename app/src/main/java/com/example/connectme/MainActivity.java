package com.example.connectme;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeAdvertiser leAdvertiser;
    private BluetoothLeScanner leScanner;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;



    private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");
    private static ScanFilter EDDYSTONE_SCAN_FILTER = new ScanFilter.Builder()
            .setServiceUuid(EDDYSTONE_SERVICE_UUID)
            .build();
    private ScanSettings SCAN_SETTINGS = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build();
    private static List<ScanFilter> SCAN_FILTERS = buildScanFilters();
    private static List<ScanFilter> buildScanFilters(){
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(EDDYSTONE_SCAN_FILTER);
        return scanFilters;
    }



    private static Map<Integer,byte[]> publicKeyByteList;
    private static ConcurrentHashMap<Integer, Integer> rssiHashMap;
    private static ConcurrentHashMap<Integer, Double> gyroAngleMap;
    private static ConcurrentHashMap<Integer, Double> accelValueMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();

        publicKeyByteList = new TreeMap<>();
        rssiHashMap = new ConcurrentHashMap<>();
        gyroAngleMap = new ConcurrentHashMap<>();
        accelValueMap = new ConcurrentHashMap<>();
    }

    private static int packetNo = -1;
    private static boolean isKeyReceived = false;
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(!isKeyReceived){
                if(packetNo < 0){
                    packetNo = (int) scanRecord[1];
                }else if(publicKeyByteList.size() != packetNo){
                    int dataID = (int)(scanRecord[3]-0xaa);
                    if(!publicKeyByteList.containsKey(dataID)){
                        publicKeyByteList.put(dataID,scanRecord);
                    }
                }else{
                    isKeyReceived = true;
                }
            }


        }
    };
}
