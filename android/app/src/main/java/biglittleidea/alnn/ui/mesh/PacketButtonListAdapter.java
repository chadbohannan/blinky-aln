package biglittleidea.alnn.ui.mesh;
import biglittleidea.alnn.App;
import biglittleidea.alnn.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PacketButtonListAdapter extends RecyclerView.Adapter<PacketButtonListAdapter.ViewHolder> {

    private String[] localDataSet;
    Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.view = view;
        }

        public TextView getTextView() {
            return view.findViewById(R.id.text_view);
        }

        public ImageView getImageView() {
            return view.findViewById(R.id.image_view);
        }
    }

    public PacketButtonListAdapter(Activity activity, String[] dataSet) {
        this.activity = activity;
        localDataSet = dataSet;
    }

    @Override // Create new views (invoked by the layout manager)
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.service_action_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override // Replace the contents of a view (invoked by the layout manager)
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (position >= localDataSet.length) {
            viewHolder.getImageView().setImageResource(R.drawable.icons8_plus);
        } else {
            viewHolder.getTextView().setText(localDataSet[position]);
        }
        viewHolder.view.setOnClickListener(onClickListener(position));
    }

    @Override // Return the size of your dataset (invoked by the layout manager)
    public int getItemCount() {
        return localDataSet.length + 1;
    }

    private View.OnClickListener onClickListener(final int position){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.service_action_item);
                dialog.setTitle("Position" + position);
                dialog.setCancelable(true);
                TextView name =(TextView)dialog.findViewById(R.id.text_view);
                name.setText("dialog test");
                dialog.show();
            }
        };
    }
}
