package biglittleidea.alnn.ui.service;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalServiceHandler;
import biglittleidea.alnn.databinding.FragmentServiceBinding;

public class ServiceFragment extends Fragment {
    App app = App.getInstance();

    private FragmentServiceBinding binding;


    List<LocalServiceHandler> localServices = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentServiceBinding.inflate(inflater, container, false);
        // TODO add service button
        binding.addSeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TODO add service", Toast.LENGTH_SHORT).show();
                App.getInstance().addLocalService("test");
            }
        });
        app.mldLocalServices.observe(getViewLifecycleOwner(), localServices -> {
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
