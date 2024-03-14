package com.activerecycle.tripgauge.bluetooth;

import static com.activerecycle.tripgauge.ConsumptionActivity.startThread;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activerecycle.tripgauge.ConsumptionActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by Owner on 8/2/2018.
 */

public class CustomListViewAdapter extends ArrayAdapter {

    public List<CustomBluetoothDeviceWrapper> m_list;

    public CustomListViewAdapter(@NonNull Context context, int layoutId,
                                 List<CustomBluetoothDeviceWrapper> underLyingList) {
        super(context, layoutId, underLyingList);
        m_list = underLyingList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);

        CustomBluetoothDeviceWrapper device = getItem(position);
        TextView customText1 = customView.findViewById(R.id.text1);
        TextView customText2 = customView.findViewById(R.id.text2);
        TextView customText3 = customView.findViewById(R.id.text3);
        TextView customText4 = (TextView) customView.findViewById(R.id.tv_connected);

        customText1.setText(device.getName());
        customText2.setText(device.getAddress());
        customText3.setText(Integer.toString(device.getRssi()));

        return customView;
    }

    public void add(@Nullable CustomBluetoothDeviceWrapper device) {
        for (CustomBluetoothDeviceWrapper deviceWrapper : m_list) {
            if (device != null && device.getAddress().equals(deviceWrapper.getAddress())) {
                return;  //중복이면 저장하지 않고 넘어감.
            }

        }

//        // Need to wipe list on starting new scan
//        if (device != null && device.getName().contains("DSD Tech")) {
//            m_list.add(0, device);
//            return;
//        }

        if (device != null && device.getName().toLowerCase(Locale.ROOT).contains("arduino")
                || device.getName().toLowerCase(Locale.ROOT).contains("recycle")
                || device.getName().toLowerCase(Locale.ROOT).contains("ble")
                || device.getName().toLowerCase(Locale.ROOT).contains("unknown"))

        {
                m_list.add(device);
        }

    }

    @Nullable
    @Override
    public CustomBluetoothDeviceWrapper getItem(int position) {
        return m_list.get(position);
    }

}
