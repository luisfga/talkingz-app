package br.com.luisfga.talkingz.ui.contacts;

import android.os.Bundle;
import android.util.Log;
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
import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.database.viewmodels.ContactViewModel;
import br.com.luisfga.talkingz.ui.TalkingzAbstractRootFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends TalkingzAbstractRootFragment {

    private static final String TAG = "ContactsFragment";

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

        //se houver mais opções, colocar o FloatButton em uma ViewGroup junto com as outras opções
        FloatingActionButton removeContactsButton = view.findViewById(R.id.removeContacts);
        ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(getActivity(), removeContactsButton);
        myRecyclerView.setAdapter(adapter);
        removeContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Remover contatos selecionados! View: " + v);
                Set<User> removeds = adapter.getSelecteds();
                for (User user : removeds) {
                    mContactViewModel.delete(user);
                    //unset selection
                    adapter.unsetSelection(user);
                }
                adapter.notifyDataSetChanged();
            }
        });

        //itemModelView's list will be observed to refresh the adapter's list
        mContactViewModel.getAllContact().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> contacts) {
                //refresh list fragment
                adapter.setItems(contacts);
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

}