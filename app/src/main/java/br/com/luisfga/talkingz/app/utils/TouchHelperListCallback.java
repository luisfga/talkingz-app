package br.com.luisfga.talkingz.app.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TouchHelperListCallback extends ItemTouchHelper.SimpleCallback {

    private ItemTouchHelperCallbackListener listener;

    public TouchHelperListCallback(int dragDirs, int swipeDirs, ItemTouchHelperCallbackListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface ItemTouchHelperCallbackListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}