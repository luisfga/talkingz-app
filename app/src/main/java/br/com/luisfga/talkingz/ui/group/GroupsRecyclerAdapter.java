package br.com.luisfga.talkingz.ui.group;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.Group;
import br.com.luisfga.talkingz.utils.BitmapUtility;


public class GroupsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Group> mItems;

    // XXXXXXXXXXXXX CONSTRUCTOR
    // Provide a suitable constructor (depends on the kind of dataset)
    GroupsRecyclerAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        else return 0;
    }

    Group getItem(int position) {
        return mItems.get(position);
    }

    void setItems(List<Group> items){
        mItems = items;
        notifyDataSetChanged();
    }

    void removeItem(int position) {
        mItems.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    void restoreItem(Group item, int position) {
        mItems.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    // XXXXXXXXXXXXXX OVERRIDING IMPLEMENTATIONS
    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = inflater.inflate(R.layout.fragment_contacts_list_item, parent, false);
        return new GroupsRecyclerAdapter.ListItemViewHolder(layout);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (mItems != null) {

            Group currentItem = mItems.get(position);

            if (currentItem.getThumbnail() != null) {
                Bitmap bitmap = BitmapUtility.getBitmapFromBytes(currentItem.getThumbnail());
                ((GroupsRecyclerAdapter.ListItemViewHolder)holder).imageView.setImageBitmap(bitmap);
            } else {
                ((GroupsRecyclerAdapter.ListItemViewHolder)holder).imageView.setImageBitmap(null);
            }

            //Load Item Title
            ((ListItemViewHolder)holder).titleTextView.setText(currentItem.getName());

        }
    }

    // XXXXXXXXXXXXXXX STATIC INNER CLASS Implementation (ViewHolder) XXXXXXXXXXX
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ListItemViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.rc_list_group_thumbnail);
            titleTextView = view.findViewById(R.id.rc_list_group_name);
        }
    }

}