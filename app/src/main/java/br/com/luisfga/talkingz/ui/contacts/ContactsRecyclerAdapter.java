package br.com.luisfga.talkingz.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.ui.directmessage.DirectMessageActivity;
import br.com.luisfga.talkingz.utils.BitmapUtility;


public class ContactsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ContactsRecyclerAdapter";

    private final LayoutInflater inflater;
    private List<User> mItems;
    private Context context;
    private View removeContactsButton;
    private Map<User, View> selectedItems = new HashMap<>();

    // XXXXXXXXXXXXX CONSTRUCTOR
    // Provide a suitable constructor (depends on the kind of dataset)
    ContactsRecyclerAdapter(Context context, View removeContactsButton) {
        this.context = context;
        this.removeContactsButton = removeContactsButton;
        this.inflater = LayoutInflater.from(context);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        else return 0;
    }

    User getItem(int position) {
        return mItems.get(position);
    }

    void setItems(List<User> items){
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

    void restoreItem(User item, int position) {
        mItems.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    Set<User> getSelecteds(){
//        mItems.removeAll(selectedItems);
//        notifyDataSetChanged();
//        removeContactsButton.setVisibility(View.GONE);
        return selectedItems.keySet();
    }
    void unsetSelection(User user){
        View view = selectedItems.get(user);
        view.setSelected(false);
        selectedItems.remove(user);
        if(selectedItems.size() == 0) {
            removeContactsButton.setVisibility(View.GONE);
        }
    }

    // XXXXXXXXXXXXXX OVERRIDING IMPLEMENTATIONS
    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = inflater.inflate(R.layout.fragment_contacts_list_item, parent, false);
        return new ContactsRecyclerAdapter.ListItemViewHolder(layout);
    }



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (mItems != null) {

            User currentItem = mItems.get(position);

            holder.itemView.setOnClickListener(v -> {
                if (holder.itemView.isSelected()) {
                    unsetSelection(currentItem);

                } else {
                    Intent chatIntent = new Intent(context, DirectMessageActivity.class);
                    chatIntent.putExtra(DirectMessageActivity.CONTACT_ID_KEY, currentItem.getId().toString());
                    context.startActivity(chatIntent);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                holder.itemView.setSelected(!holder.itemView.isSelected());

                boolean isSelected = holder.itemView.isSelected();
                if (isSelected) {
                    selectedItems.put(currentItem, holder.itemView);
                    removeContactsButton.setVisibility(View.VISIBLE);
                } else {
                    unsetSelection(currentItem);
                }
                return true;
            });

            if (currentItem.getThumbnail() != null) {
//                Bitmap srcBmp = BitmapUtility.loadContactThumbnail(context, currentItem, 100);
                Bitmap srcBmp = BitmapUtility.getBitmapFromBytes(currentItem.getThumbnail());
                Bitmap tempBmp = BitmapUtility.centerCropSquare(srcBmp); //pega um quadrado pra manter proporções
                Bitmap dstBmp = ThumbnailUtils.extractThumbnail(tempBmp, 75, 75);

                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), dstBmp);
                roundedBitmapDrawable.setCircular(true);
                ((ContactsRecyclerAdapter.ListItemViewHolder)holder).thumbnailView.setImageDrawable(roundedBitmapDrawable);
//                ((ContactsRecyclerAdapter.ListItemViewHolder)holder).thumbnailView.setImageBitmap(testBmp);
            }

            //Load Item Title
            ((ListItemViewHolder)holder).nameTextView.setText(currentItem.getName());

        }


    }

    // XXXXXXXXXXXXXXX STATIC INNER CLASS Implementation (ViewHolder) XXXXXXXXXXX
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;
        TextView nameTextView;
        ListItemViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.rc_list_contact_thumbnail);
            nameTextView = view.findViewById(R.id.rc_list_contact_name);
        }
    }

}