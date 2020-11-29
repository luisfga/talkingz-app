package br.com.luisfga.talkingz.ui.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.viewmodels.GroupViewModel;
import br.com.luisfga.talkingz.database.entity.Group;
import br.com.luisfga.talkingz.ui.TalkingzAbstractRootFragment;
import br.com.luisfga.talkingz.utils.TouchHelperListCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends TalkingzAbstractRootFragment implements TouchHelperListCallback.ItemTouchHelperCallbackListener {

    private RecyclerView myRecyclerView;
    private GroupViewModel mGroupViewModel;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Getting the ViewModel
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        myRecyclerView = view.findViewById(R.id.groups_recycler_view);

        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        GroupsRecyclerAdapter adapter = new GroupsRecyclerAdapter(getActivity());
        myRecyclerView.setAdapter(adapter);

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback ithSimpleCallback = new TouchHelperListCallback(0, ItemTouchHelper.LEFT, this); //this <listener> impl attaches with the callback
        new ItemTouchHelper(ithSimpleCallback).attachToRecyclerView(myRecyclerView); //helper attaches the callback with recyclerView.

        //itemModelView's list will be observed to refresh the adapter's list
        mGroupViewModel.getAllGroups().observe(getActivity(), new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> contact) {
                //refresh list fragment
                adapter.setItems(contact);
            }
        });

        FloatingActionButton createGroupButton = getActivity().findViewById(R.id.createGroupButton);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent addContactIntent = new Intent(getActivity(), AddContactActivity.class);
//                startActivity(addContactIntent);
            }
        });
    }

    /**
     * callback when recycler view is swiped
     * item will be removed on swiped
     * confirmation will be shown in a AlertDialog
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        Log.d( "GroupsFragment", "onSwiped Fired!");

        if (viewHolder instanceof GroupsRecyclerAdapter.ListItemViewHolder) {

            GroupsRecyclerAdapter.ListItemViewHolder itemViewHolder = (GroupsRecyclerAdapter.ListItemViewHolder) viewHolder;
            GroupsRecyclerAdapter adapter = (GroupsRecyclerAdapter) myRecyclerView.getAdapter();
            Group group = adapter.getItem(position);

            //remotion only from adapter
            adapter.removeItem(position);

            confirmDeletion(position, adapter, group);
        }
    }

    private void confirmDeletion(int position, GroupsRecyclerAdapter adapter, Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Excluir permanenmente?");
        // Add the buttons
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //remotion from database
                mGroupViewModel.delete(group);
            }
        });
        builder.setNegativeButton("Desfazer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //restoring removed item due the cancelation
                adapter.restoreItem(group, position);
                myRecyclerView.scrollToPosition(position);
            }
        });

        //TODO usado apenas para esconder a UI - procurar um solução melhor
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //hide ui
//                AppUtility.hideSystemUI((TabsActivity) getActivity());
            }
        });

        // Set other dialog properties
        builder.setCancelable(false);

        // Create and Show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
