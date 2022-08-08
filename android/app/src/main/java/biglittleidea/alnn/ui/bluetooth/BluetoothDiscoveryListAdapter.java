package biglittleidea.alnn.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.R;

public class BluetoothDiscoveryListAdapter extends BaseAdapter {

    private static String serviceUUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    private LayoutInflater inflter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();

    @SuppressLint("MissingPermission")
    public BluetoothDiscoveryListAdapter(List<BluetoothDevice> deviceList) {
        inflter = LayoutInflater.from(App.getInstance());
        for (BluetoothDevice device : deviceList) {
            ParcelUuid[] uuids = device.getUuids();
            if (uuids == null)
                continue;
            for (ParcelUuid uuid : uuids) {
                Log.d("ALNN", "uuid:" + uuid.toString());
                if (uuid.toString().equals(serviceUUID)) {
                    this.deviceList.add(device);
                    break;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return deviceList.get(position).getAddress().hashCode();
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflter.inflate(R.layout.bluetooth_discovery_item, null);
        TextView nameText = view.findViewById(R.id.nameText);
        BluetoothDevice device = deviceList.get(position);
        String name = device.getName();
        String addr = device.getAddress();
        int len = device.getUuids().length;
        nameText.setText(String.format("%s (%d) %s", name,  len, addr));
        App.getInstance().connectTo("bluetooth", device.getAddress(), (short)0, serviceUUID, true);
        return view;
    }
}
