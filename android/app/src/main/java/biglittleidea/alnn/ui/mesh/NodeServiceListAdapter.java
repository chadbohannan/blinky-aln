package biglittleidea.alnn.ui.mesh;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import biglittleidea.aln.Router;
import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.R;

public class NodeServiceListAdapter extends BaseAdapter {
    LayoutInflater inflter;
    List<Router.ServiceListItem> list = new ArrayList<>();
    Activity activity;
    public NodeServiceListAdapter(Activity activity, Set<Router.ServiceListItem> services) {
        this.activity = activity;
        for (Router.ServiceListItem item : services)
            this.list.add(item);
        inflter = (LayoutInflater.from(activity));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Router.ServiceListItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Router.ServiceListItem info = list.get(position);
        view = inflter.inflate(R.layout.service_node_item, null);
        TextView text = view.findViewById(R.id.service_name_view);
        text.setText(info.service);

        String[] actions = new String[]{"heart", "square", "circle", "happy-face"};
        RecyclerView rv = view.findViewById(R.id.packet_action_list);
        rv.setAdapter(new PacketButtonListAdapter(activity, actions));
        rv.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        return view;
    }
}
