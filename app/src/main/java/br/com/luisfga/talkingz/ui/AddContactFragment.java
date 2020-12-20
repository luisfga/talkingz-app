package br.com.luisfga.talkingz.ui;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import androidx.navigation.Navigation;
import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.database.viewmodels.ContactViewModel;
import br.com.luisfga.talkingz.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.utils.BitmapUtility;
import br.com.luisfga.talkingz.utils.DialogUtility;
import br.com.luisfga.talkingz.commons.UserWrapper;
import br.com.luisfga.talkingz.commons.orchestration.response.CommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;

public class AddContactFragment extends TalkingzAbstractRootFragment implements ResponseHandler<ResponseCommandFindContact> {

    EditText inputEditText;
    LinearLayout validationErrorPanel;
    TextView validationErrorMessage;
    ImageView validationErrorImage;

    ProgressBar responseProgressBar;

    LinearLayout responseDataGroup;
    ImageView responseIcon;
    TextView responseMessage;

    Button responseButton;

    LinearLayout contactDataGroup;
    ImageView contactThumbnail;
    TextView contactName;

    Button searchButton;
//    Button negativeButton;

    ContactViewModel mContactViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        mContactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        inputEditText = getView().findViewById(R.id.input);
        validationErrorPanel = getView().findViewById(R.id.validationErrorPanel);
        validationErrorMessage = getView().findViewById(R.id.validationErrorMessage);
        validationErrorImage = getView().findViewById(R.id.validationErrorImage);

        responseProgressBar = getView().findViewById(R.id.responseProgressBar);

        responseDataGroup = getView().findViewById(R.id.responseDataGroup);
        responseIcon = getView().findViewById(R.id.responseIcon);
        responseMessage = getView().findViewById(R.id.responseMessage);
        responseButton = getView().findViewById(R.id.responseButton);

        contactDataGroup = getView().findViewById(R.id.contactDataGroup);
        contactThumbnail = getView().findViewById(R.id.contactThumbnail);
        contactName = getView().findViewById(R.id.contactName);

        searchButton = getView().findViewById(R.id.search_button);
        searchButton.setOnClickListener(new SearchButtonOnClickListener());

//        negativeButton = findViewById(R.id.negativeButton);
//        negativeButton.setOnClickListener(v -> finish());

        getTalkingzApp().getMessagingService().getWsClient().setResponseCommandFindContactHandler(this);
    }

    private void adicionarContato(User contact) {
        mContactViewModel.insert(contact);
        Navigation.findNavController(getView()).navigate(R.id.nav_home);
    }

    /*
     * Trata resposta do servidor ao comando CommandFindContact. Essa classe implementa a interface
     * ResponseHandler<ResponseCommandFindContact>, portanto deve ter sido registrada no MessagingWSClient
     * com a chamada ao método 'getTalkinzApp().getMessagingService().getWsClient().setResponseCommandFindContactHandler(this);'
     * */
    @Override
    public void handleResponse(ResponseCommandFindContact responseCommandFindContact) {
        UserWrapper userWrapper = responseCommandFindContact.getUserWrapper();

        // Stuff that updates the UI
        if (userWrapper != null) {

            User newContact = new User();

            newContact.setId(userWrapper.getId());
            newContact.setName(userWrapper.getName());
            newContact.setEmail(userWrapper.getEmail());
            newContact.setSearchToken(userWrapper.getSearchToken());
            newContact.setMainUser(false);

            if(userWrapper.getThumbnail() != null) {
                newContact.setThumbnail(userWrapper.getThumbnail());
            }

            getActivity().runOnUiThread(() -> {
                responseProgressBar.setVisibility(View.GONE);

                responseIcon.setImageResource(R.drawable.ic_resp_success);
                responseMessage.setText("Contato encontrado");
                responseDataGroup.setVisibility(View.VISIBLE);

                responseButton.setVisibility(View.VISIBLE);
                responseButton.setText(R.string.addContact);
                Drawable iconAddPerson = getActivity().getDrawable(R.drawable.ic_add_person);
                responseButton.setCompoundDrawablesWithIntrinsicBounds(iconAddPerson, null, null, null);
                responseButton.setOnClickListener(v1 -> adicionarContato(newContact));

                //exibir contato
                contactThumbnail.setImageBitmap(BitmapUtility.getBitmapFromBytes(newContact.getThumbnail()));
                contactName.setText(newContact.getName());
                contactDataGroup.setVisibility(View.VISIBLE);

            });

        } else {
            getActivity().runOnUiThread(() -> {
                responseProgressBar.setVisibility(View.GONE);

                responseIcon.setImageResource(R.drawable.ic_resp_failure);
                responseMessage.setText("Contato não encontrado");
                responseDataGroup.setVisibility(View.VISIBLE);

                responseButton.setVisibility(View.VISIBLE);
                responseButton.setText("Tentar novamente");
                Drawable iconRetry = getActivity().getDrawable(R.drawable.ic_retry);
                responseButton.setCompoundDrawablesWithIntrinsicBounds(iconRetry, null, null, null);
                responseButton.setOnClickListener(v12 -> searchButton.performClick());
            });
        }
    }

    private class SearchButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String token = inputEditText.getText().toString();

            //Validação do TOKEN
            if (!"".equals(token) && !getTalkingzApp().getMainUser().getSearchToken().equals(token)){

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);

                //verificar se o usuário já está adicionado
                User contact;
                Future<User> loadingContact = AppDefaultExecutor.getTalkingzBackloadMaxPriorityThread().submit(() ->
                        getTalkingzApp().getTalkingzDB().userDAO().getByToken(token));
                try {
                    contact = loadingContact.get();
                    if (contact != null) {
                        validationErrorMessage.setText("Esse contato já existe na sua lista");
                        validationErrorImage.setImageResource(R.drawable.ic_resp_success);
                        validationErrorPanel.setVisibility(View.VISIBLE);
                        return;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                //se não retornou no if anterior, significa que não há esse token na lista de contatos. Segue processo normal.
                if (getTalkingzApp().isConnectionOpen()) {

                    validationErrorPanel.setVisibility(View.GONE);

                    responseProgressBar.setVisibility(View.VISIBLE);

                    responseDataGroup.setVisibility(View.GONE);
                    responseMessage.setText("Procurando contato com o token ("+token+")");
                    responseMessage.setVisibility(View.VISIBLE);

                    responseButton.setVisibility(View.GONE);

                    contactDataGroup.setVisibility(View.GONE);

                    //Busca Contato
                    CommandFindContact commandFindContact = new CommandFindContact();
                    commandFindContact.setSearchToken(token);
                    getTalkingzApp().getMessagingService().getWsClient().sendCommandOrFeedBack(commandFindContact);

                    //Aguarda retorno - tratado no método handle

                } else {
                    DialogUtility.showConnectionNotAvailableInfo(getActivity().getWindow().getContext());
                }


            } else if (getTalkingzApp().getMainUser().getSearchToken().equals(token)) {
                validationErrorMessage.setText("Esse é o seu token");
                validationErrorImage.setImageResource(R.drawable.ic_resp_success);
                validationErrorPanel.setVisibility(View.VISIBLE);
            } else if ("".equals(token)) {
                validationErrorMessage.setText("Por favor, informe o token do contato");
                validationErrorImage.setImageResource(R.drawable.ic_resp_failure);
                validationErrorPanel.setVisibility(View.VISIBLE);
            }
        }
    }
}
