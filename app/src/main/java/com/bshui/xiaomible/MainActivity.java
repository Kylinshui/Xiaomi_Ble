package com.bshui.xiaomible;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bshui.xiaomible.adapter.DeviceAdapter;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button bt_scan;
    private ListView lv_dev;
    private DeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        //1.初始化及配置
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1,5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        //判断本机是否打开蓝牙
        if(!BleManager.getInstance().isBlueEnable()){
            //直接打开蓝牙
           // BleManager.getInstance().enableBluetooth();
            //引导界面引导用户打开蓝牙
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    private void initView(){
        bt_scan = (Button)findViewById(R.id.btScan);
        lv_dev  = (ListView)findViewById(R.id.lvDev);

        bt_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //2.扫描外围蓝牙设备
                startScan();
            }
        });

        mDeviceAdapter = new DeviceAdapter(this);
        lv_dev.setAdapter(mDeviceAdapter);

    }

    private void startScan(){
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {


            }






        });
    }
}
