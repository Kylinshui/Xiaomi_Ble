package com.bshui.xiaomible;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bshui.xiaomible.adapter.DeviceAdapter;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private Button bt_scan;
    private ListView lv_dev;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private static final int ACCESS_COARSE_LOCATION = 100;
    private static int connect_count = 0;
    String[] perms = {Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkpermission();
        initView();

        //1.初始化及配置
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1,5000)
                .setOperateTimeout(50000);

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
    private void checkpermission(){
        if(EasyPermissions.hasPermissions(this,perms)){

        }else{
            EasyPermissions.requestPermissions(this,
                    "Need ACCESS_COARSE_LOCATION",
                    ACCESS_COARSE_LOCATION,
                    perms);
        }
    }
    private void initView(){
        bt_scan = (Button)findViewById(R.id.btScan);
        lv_dev  = (ListView)findViewById(R.id.lvDev);
        progressDialog = new ProgressDialog(this);

        bt_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //2.扫描外围蓝牙设备
                startScan();
            }
        });

        mDeviceAdapter = new DeviceAdapter(this);
        lv_dev.setAdapter(mDeviceAdapter);
        lv_dev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               //点击蓝牙设备选项 连接蓝牙 并进入相应的操作页面

                final BleDevice bleDevice = mDeviceAdapter.getItem(i);
                if(bleDevice == null)
                    return;

                if(!BleManager.getInstance().isConnected(bleDevice)){
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);

                }else{
                    //如果已经连上,直接转到设备信息页面
                    Intent intent = new Intent(MainActivity.this,DeviceInfoActivity.class);
                    intent.putExtra("BleDevice",bleDevice);
                    startActivity(intent);
                }



            }
        });
    }

    private void connect(final BleDevice bleDevice){
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                    if(connect_count==0) {
                        progressDialog.show();
                    }
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                if(connect_count==0) {
                    progressDialog.dismiss();
                }
            }
            //连接成功并发现服务
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                if(connect_count==0) {
                    progressDialog.dismiss();
                    //Toast.makeText(getApplicationContext(),"Connect Success",
                    //  Toast.LENGTH_LONG).show();
                    //如果连接成功,则转到操作页
                    Intent intent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                    intent.putExtra("BleDevice", bleDevice);
                    startActivity(intent);
                    connect_count++;
                }

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.i("TAG","onDisConnected"+" isActiveDisConnected"+isActiveDisConnected);


                Toast.makeText(getApplicationContext(),"Connect Fail",
                        Toast.LENGTH_LONG).show();
                if(connect_count >0)
                    connect(bleDevice);
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults,
                this);
    }

}
