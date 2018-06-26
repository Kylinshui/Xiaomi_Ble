package com.bshui.xiaomible;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import java.util.List;
import java.util.UUID;

public class DeviceInfoActivity extends AppCompatActivity {
    private BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;


    private TextView tv_name;
    private TextView tv_mac;
    private TextView tv_level;//电池电量
    private TextView tv_step;//计步
    private Button   bt_step;//读步数
    private Button   bt_find;//寻找手环,会发光,振动
    private Button   bt_level;//读电量
    private Button   bt_disconnect;//断开连接

    private static String UUID_BATTERY_SERVICE = "0000fee0-0000-1000-8000-00805f9b34fb";
    private static String UUID_BATTERY_CHARA   = "0000ff0c-0000-1000-8000-00805f9b34fb";

    private static String UUID_STEP_SERVICE = "0000fee0-0000-1000-8000-00805f9b34fb";
    private static String UUID_STEP_CHARA   = "0000ff06-0000-1000-8000-00805f9b34fb";

    private static String UUID_FIND_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    private static String UUID_FIND_CHARA   = "00002a06-0000-1000-8000-00805f9b34fb";
    private byte[] FIND_CMD = {0x02};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        tv_name  = (TextView)findViewById(R.id.tv_name);
        tv_mac   = (TextView)findViewById(R.id.tv_mac);
        tv_level = (TextView)findViewById(R.id.tv_level);
        tv_step  = (TextView)findViewById(R.id.tv_step);
        bt_step  = (Button)findViewById(R.id.bt_step);
        bt_find  = (Button)findViewById(R.id.bt_find);
        bt_level = (Button)findViewById(R.id.bt_level);
        bt_disconnect = (Button)findViewById(R.id.btDisconnect);

        bleDevice = getIntent().getParcelableExtra("BleDevice");
        if(bleDevice == null)
            finish();
        String name = bleDevice.getName();
        String mac  = bleDevice.getMac();
        tv_name.setText("设备名:"+name);
        tv_mac.setText("设备地址:"+mac);
        //BluetoothGatt对象作为连接桥梁,双向通信
       // BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        //测试获取所有的Service和Characteristic的UUID
        /*if(gatt!=null) {
            Print_UUID(gatt);
        }*/

        print_level();
        try{
            Thread.sleep(100);
        }catch (Exception e){
            e.printStackTrace();
        }

        print_step();
        bt_level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print_level();
            }
        });

        bt_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print_step();
            }
        });

        bt_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleManager.getInstance().write(
                        bleDevice,
                        UUID_FIND_SERVICE,
                        UUID_FIND_CHARA,
                        FIND_CMD,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {

                            }

                            @Override
                            public void onWriteFailure(BleException exception) {

                            }
                        }
                );
            }
        });

        bt_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bleDevice!=null){
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    //显示电池电量
    private void print_level(){
        BleManager.getInstance().read(bleDevice,
                UUID_BATTERY_SERVICE,
                UUID_BATTERY_CHARA,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_level.setText("电池电量:"+data[0]+"%");
                            }
                        });
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_level.setText("电池电量:读取失败");
                            }
                        });

                    }
                });
    }
    //显示步数
    private void print_step(){
        BleManager.getInstance().read(bleDevice,
                UUID_STEP_SERVICE,
                UUID_STEP_CHARA,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String step0 = HexUtil.extractData(data,0);
                                String step1 = HexUtil.extractData(data,1);
                                int bu0 = Integer.valueOf(step0,16);
                                int bu1 = Integer.valueOf(step1, 16)*256;
                                int total = bu0+bu1;


                                tv_step.setText("运动步数:"+total+" 步");
                            }
                        });
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_step.setText("运动步数:读取失败");
                            }
                        });

                    }
                });
    }
    private void Print_UUID(BluetoothGatt gatt){
        List<BluetoothGattService> serviceList = gatt.getServices();
        for(BluetoothGattService service : serviceList){
            UUID uuid_service = service.getUuid();
            Log.i("TAG","s:"+uuid_service.toString());

            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            for(BluetoothGattCharacteristic characteristic : characteristicList){
                UUID uuid_chara = characteristic.getUuid();
                Log.i("TAG","c:"+uuid_chara.toString());
            }
        }

    }
}
