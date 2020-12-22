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

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends TalkingzAbstractRootFragment {

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
}
