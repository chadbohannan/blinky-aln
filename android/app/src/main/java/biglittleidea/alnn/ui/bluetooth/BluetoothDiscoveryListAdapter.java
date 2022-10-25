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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.R;

public class BluetoothDiscoveryListAdapter extends BaseAdapter {

    private static HashMap<String, String> serviceUUIDs = new HashMap<>();


    private LayoutInflater inflter;
    private List<BluetoothService> deviceList = new ArrayList<>();

    protected class BluetoothService {
        protected BluetoothDevice device;
        protected String uuid;
        protected BluetoothService(BluetoothDevice device, String uuid) {
            this.device = device;
            this.uuid = uuid;
        }
        public String toString() {
            return String.format("%s-%s", device.getAddress(), uuid);
        }
    }

    @SuppressLint("MissingPermission")
    public BluetoothDiscoveryListAdapter(List<BluetoothDevice> deviceList) {
        serviceUUIDs.put("94f39d29-7d6d-437d-973b-fba39e49d4ee", "rfcomm-client");
        serviceUUIDs.put("00001101-0000-1000-8000-10ca10ddba11", "blinky-bt");
        serviceUUIDs.put("00001101-0000-1000-8000-00805F9B34FB", "serial adapter");
        serviceUUIDs.put("00001101-0000-1000-8000-00805f9b34fb", "serial adapter");


        inflter = LayoutInflater.from(App.getInstance());
        for (BluetoothDevice device : deviceList) {
            ParcelUuid[] uuids = device.getUuids();
            if (uuids == null)
                continue;
            for (ParcelUuid uuid : uuids) {
                if (serviceUUIDs.containsKey(uuid.toString()))
                    this.deviceList.add(new BluetoothService(device, uuid.toString()));
            }
        }
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothService getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return deviceList.get(position).toString().hashCode();
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflter.inflate(R.layout.bluetooth_discovery_item, null);

        BluetoothService service = deviceList.get(position);
        String addr = service.device.getAddress();
        String name = service.device.getName();
        String serv = serviceUUIDs.get(service.uuid);

        TextView nameText = view.findViewById(R.id.nameText);
        nameText.setText(String.format("%s %s %s", addr, name, serv));

        final boolean isConnected = App.getInstance().isConnected(service.device, service.uuid);
        if (isConnected) {
            view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                App.getInstance().connectTo(service.device, service.uuid, !isConnected);
            }
        });

        return view;
    }
}
