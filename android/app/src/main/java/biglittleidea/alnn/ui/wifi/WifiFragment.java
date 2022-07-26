package biglittleidea.alnn.ui.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Set;

import biglittleidea.alnn.App;
import biglittleidea.alnn.databinding.FragmentWifiBinding;

public class WifiFragment extends Fragment {

    private FragmentWifiBinding binding;

    DirectConnectionItem[] composeDirectConnectionList(Set<String> set) {
        DirectConnectionItem[] array = new DirectConnectionItem[set.size()];
        int i = 0;
        for (String value : set) {
            String title, protocol, address, host, node = "";
            short port = 8181;
            String[] parts = value.split("\t");
            switch (parts.length) {
                case 2:
                    title = parts[0];
                    parts = parts[1].split("://");
                    if (parts.length == 2) {
                        protocol = parts[0];
                        address = parts[1];
                    } else{
                        protocol = "unknown";
                        address = parts[0];
                    }
                    break;
                case 3:
                    title = parts[0];
                    protocol = parts[1];
                    address = parts[2];
                    break;
                default:
                protocol = "";
                address = parts[0];
                title = "untitled";
            }
            parts = address.split("/");
            address = parts[0];
            if (parts.length == 2) {
                node = parts[1];
            }
            parts = address.split(":");
            host = parts[0];
            if (parts.length == 2) {
                port = Short.parseShort(parts[1]);
            }
            array[i++] = new DirectConnectionItem(title, protocol, host, port, node);
        }
        return array;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        App app = App.getInstance();
        binding = FragmentWifiBinding.inflate(inflater, container, false);
        final ListView interfaceListView = binding.interfaceListView;
        final ListView discoveryListView = binding.discoveryListView;
        final ListView directListView = binding.directListView;




        app.localInetInfo.observe(getViewLifecycleOwner(), localInetInfos -> {
            interfaceListView.setAdapter(new LocalNetInfoListAdapter(app, localInetInfos));
            ViewGroup.LayoutParams params = interfaceListView.getLayoutParams();

            float dip = 91f;
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());

            params.height = px * localInetInfos.size();
            interfaceListView.setLayoutParams(params);
            interfaceListView.requestLayout();
        });

        app.beaconInfo.observe(getViewLifecycleOwner(), beaconInfos -> {
            discoveryListView.setAdapter(new BeaconInfoListAdapter(app, beaconInfos, getViewLifecycleOwner()));
            ViewGroup.LayoutParams params = discoveryListView.getLayoutParams();

            float dip = 91f;
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());

            params.height = px * beaconInfos.size();
            discoveryListView.setLayoutParams(params);
            discoveryListView.requestLayout();
        });

        app.directConnections.observe(getViewLifecycleOwner(), connectionList -> {
            DirectConnectionItem[] array = composeDirectConnectionList(connectionList);
            DirectConnectionListAdapter adapter = new DirectConnectionListAdapter(getActivity(), array, getViewLifecycleOwner());
            directListView.setAdapter(adapter);

            float dip = 121f;
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());

            ViewGroup.LayoutParams params = directListView.getLayoutParams();
            params.height = px * (adapter.getCount());
            directListView.setLayoutParams(params);
            directListView.requestLayout();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}