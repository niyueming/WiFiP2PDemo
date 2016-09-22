package net.nym.wifip2pdemo;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author nym
 * @date 2016/9/22.
 * @since 1.0
 */

public class WifiP2pDeviceAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mContext;
    private ArrayList<WifiP2pDevice> mData;
    public WifiP2pDeviceAdapter(Context context,ArrayList<WifiP2pDevice> list){
        mContext = context;
        mData = list;

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_wifip2p_device,null);
        }
        convertView.setTag(position);
        convertView.setOnClickListener(this);
        TextView textView = (TextView) convertView.findViewById(R.id.text);
        textView.setText(mData.get(position).toString());

        return convertView;
    }


    @Override
    public void onClick(View v) {
        int position = Integer.parseInt(v.getTag().toString());
        WifiP2pDevice device = mData.get(position);
        switch (device.status){
            case WifiP2pDevice.AVAILABLE:
            case WifiP2pDevice.CONNECTED:
            case WifiP2pDevice.INVITED:
                ((MainActivity)mContext).connect(device);
                break;
            case WifiP2pDevice.FAILED:
            case WifiP2pDevice.UNAVAILABLE:
                Toast.makeText(mContext,String.format(Locale.getDefault(),"status=%d",device.status),Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
