package biglittleidea.alnn.ui.bluetooth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import biglittleidea.alnn.App;
import biglittleidea.alnn.databinding.FragmentBluetoothBinding;

public class BluetoothFragment extends Fragment {

    private static String[] PERMISSIONS_BLUETOOTH = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
    };
    private static final int REQUEST_BLUETOOTH = 1;

    private FragmentBluetoothBinding binding;
    App app = App.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        BluetoothViewModel bluetoothViewModel =
//                new ViewModelProvider(this).get(BluetoothViewModel.class);

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBluetooth;
        final Button startButton = binding.scanButton;
        final ListView listView = binding.discoveredDevicesList;
        app.bluetoothDevices.observe(getViewLifecycleOwner(), devices -> {
            listView.setAdapter(new BluetoothDiscoveryListAdapter(devices));
            String msg = String.format("%d bluetooth devices found", devices.size());
            textView.setText(msg);
        });

        app.bluetoothDiscoveryStatus.observe(getViewLifecycleOwner(), status -> {
            textView.setText(status);
        });

        app.bluetoothDiscoveryIsActive.observe(getViewLifecycleOwner(), isActive -> {
            if (isActive) {
                startButton.setText("Stop Scan");
            } else {
                startButton.setText("Restart Scan");
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_BLUETOOTH, REQUEST_BLUETOOTH);
                } else {
                    app.toggleBluetoothDiscovery();
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}