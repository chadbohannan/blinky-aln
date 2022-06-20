package biglittleidea.alnn.ui.wifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.BeaconInfo;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.SeparatedListAdapter;
import biglittleidea.alnn.databinding.FragmentWifiBinding;

public class WifiFragment extends Fragment {

    private FragmentWifiBinding binding;
    List<LocalInetInfo> localInetInfoList = new ArrayList<>();
    List<BeaconInfo> beaconInfoList = new ArrayList<>();

    void resetAdapters(ListView listView) {
        App app = App.getInstance();
        SeparatedListAdapter listAdapter = new SeparatedListAdapter(app);
        listAdapter.addSection("Interfaces", new LocalNetInfoListAdapter(app, localInetInfoList));
        if (beaconInfoList.size() > 0)
            listAdapter.addSection("Beacons Discovered", new BeaconInfoListAdapter(app, beaconInfoList));
        listView.setAdapter(listAdapter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        App app = App.getInstance();
        binding = FragmentWifiBinding.inflate(inflater, container, false);
        final ListView listView = binding.listView;

        app.localInetInfo.observe(getViewLifecycleOwner(), localInetInfos -> {
            localInetInfoList = localInetInfos;
            resetAdapters(listView);
        });
        app.beaconInfo.observe(getViewLifecycleOwner(), beaconInfos -> {
            beaconInfoList = beaconInfos;
            resetAdapters(listView);
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}