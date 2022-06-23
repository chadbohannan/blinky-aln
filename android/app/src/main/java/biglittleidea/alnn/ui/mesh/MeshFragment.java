package biglittleidea.alnn.ui.mesh;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Map;

import biglittleidea.aln.Router;
import biglittleidea.alnn.App;
import biglittleidea.alnn.SeparatedListAdapter;
import biglittleidea.alnn.databinding.FragmentMeshBinding;

public class MeshFragment extends Fragment {
    private FragmentMeshBinding binding;
    Map<String, Router.NodeInfoItem> nodeInfo;

    void resetAdapters(ListView listView) {
        App app = App.getInstance();
        SeparatedListAdapter listAdapter = new SeparatedListAdapter(app);
        for(String address : nodeInfo.keySet()) {
            listAdapter.addSection(address, new NodeListAdapter(getActivity(), nodeInfo));
        }
        listView.setAdapter(new NodeListAdapter(getActivity(), nodeInfo));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        App app = App.getInstance();
        binding = FragmentMeshBinding.inflate(inflater, container, false);
        final ListView listView = binding.listView;

        app.nodeInfo.observe(getViewLifecycleOwner(), nodeInfos -> {
            nodeInfo = nodeInfos;
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