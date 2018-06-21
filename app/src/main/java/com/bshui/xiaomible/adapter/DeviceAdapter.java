package com.bshui.xiaomible.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bshui.xiaomible.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter{
    private Context context;
    private List<BleDevice> bleDeviceList;

    public DeviceAdapter(Context context){
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(BleDevice bleDevice){
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice){
        for(int i=0; i<bleDeviceList.size();i++){
            BleDevice device = bleDeviceList.get(i);
            if(bleDevice.getKey().equals(device.getKey())){
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice(){
        for(int i=0; i<bleDeviceList.size();i++){
            BleDevice device = bleDeviceList.get(i);
            if(BleManager.getInstance().isConnected(device)){
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice(){
        for(int i=0; i< bleDeviceList.size();i++){
            BleDevice device = bleDeviceList.get(i);
            if(!BleManager.getInstance().isConnected(device)){
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear(){
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int i) {
        if(i > bleDeviceList.size())
            return null;
        return bleDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view == null){
            view = View.inflate(context, R.layout.adapter_device, null );
            viewHolder = new ViewHolder();
            viewHolder.img_blue = (ImageView)view.findViewById(R.id.img_blue);
            viewHolder.txt_name = (TextView)view.findViewById(R.id.device_name);
            viewHolder.txt_mac  = (TextView)view.findViewById(R.id.device_address);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }

        final BleDevice bleDevice = getItem(i);
        if(bleDevice != null){
            String name = bleDevice.getName();
            String mac  = bleDevice.getMac();

            viewHolder.txt_name.setText(name);
            viewHolder.txt_mac.setText(mac);

        }
        return view;
    }

    class ViewHolder{
        ImageView img_blue;
        TextView  txt_name;
        TextView  txt_mac;
    }
}
