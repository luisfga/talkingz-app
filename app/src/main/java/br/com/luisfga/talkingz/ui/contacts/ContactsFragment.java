package br.com.luisfga.talkingz.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.database.viewmodels.ContactViewModel;
import br.com.luisfga.talkingz.ui.AddContactFragment;
import br.com.luisfga.talkingz.ui.TalkingzAbstractRootFragment;
import br.com.luisfga.talkingz.utils.TouchHelperListCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends TalkingzAbstractRootFragment implements TouchHelperListCallback.ItemTouchHelperCallbackListener {

    private RecyclerView myRecyclerView;
    private ContactViewModel mContactViewModel;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Getting the ViewModel
        mContactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        myRecyclerView = view.findViewById(R.id.contacts_recycler_view);
        myRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(getActivity());
        myRecyclerView.setAdapter(adapter);

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback ithSimpleCallback = new TouchHelperListCallback(0, 0, this); //this <listener> impl attaches with the callback
        new ItemTouchHelper(ithSimpleCallback).attachToRecyclerView(myRecyclerView); //helper attaches the callback with recyclerView.

        //itemModelView's list will be observed to refresh the adapter's list
        mContactViewModel.getAllContact().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> contact) {
                //refresh list fragment
                adapter.setItems(contact);
            }
        });

        FloatingActionButton addContactButton = getActivity().findViewById(R.id.addContact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(getView()).navigate(R.id.nav_add_contact);
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

    }
}