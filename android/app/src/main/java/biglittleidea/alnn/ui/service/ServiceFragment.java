package biglittleidea.alnn.ui.service;

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
import java.util.Map;

import biglittleidea.aln.Router;
import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalServiceHandler;
import biglittleidea.alnn.databinding.FragmentServiceBinding;
import biglittleidea.alnn.ui.mesh.NodeListAdapter;

public class ServiceFragment extends Fragment {
    App app = App.getInstance();

    private FragmentServiceBinding binding;


    List<LocalServiceHandler> localServices = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentServiceBinding.inflate(inflater, container, false);
        // TODO define binding.listView
        // TODO attach updates to app.mdlLocalServices

        app.mdlLocalServices.observe(getViewLifecycleOwner(), localServices -> {
            this.localServices = localServices;
            binding.listView.setAdapter(new LocalServiceListAdapter(getActivity(), localServices));
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
