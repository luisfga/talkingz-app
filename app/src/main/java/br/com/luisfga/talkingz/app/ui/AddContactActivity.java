package br.com.luisfga.talkingz.app.ui;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.graphics.Bitmap;
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

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.database.entity.User;
import br.com.luisfga.talkingz.app.database.viewmodels.ContactViewModel;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.app.utils.BitmapUtility;
import br.com.luisfga.talkingz.app.utils.DialogUtility;
import br.com.luisfga.talkingz.commons.UserWrapper;
import br.com.luisfga.talkingz.commons.orchestration.response.CommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandFindContact;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;

public class AddContactActivity extends OrchestraAbstractRootActivity implements ResponseHandler<ResponseCommandFindContact> {

    EditText inputEditText;
    LinearLayout validationErrorPanel;
    TextView validationErrorMessage;
    ImageView validationErrorImage;

    ProgressBar responseProgressBar;
    ImageView responseIcon;
    TextView responseMessage;
    Button responseButton;

    Button positiveButton;
    Button negativeButton;

    ContactViewModel mContactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        mContactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        inputEditText = findViewById(R.id.input);
        validationErrorPanel = findViewById(R.id.validationErrorPanel);
        validationErrorMessage = findViewById(R.id.validationErrorMessage);
        validationErrorImage = findViewById(R.id.validationErrorImage);

        responseProgressBar = findViewById(R.id.responseProgressBar);
        responseIcon = findViewById(R.id.responseIcon);
        responseMessage = findViewById(R.id.responseMessage);
        responseButton = findViewById(R.id.responseButton);

        positiveButton = findViewById(R.id.positiveButton);
        positiveButton.setOnClickListener(new PositiveButtonOnClickListener());

        negativeButton = findViewById(R.id.negativeButton);
        negativeButton.setOnClickListener(v -> finish());

        getTalkinzApp().setResponseCommandFindContactHandler(this);
    }

    private void adicionarContato(User contact) {
        mContactViewModel.insert(contact);
        finish();
    }

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
                Bitmap bitmap = BitmapUtility.getBitmapFromBytes(userWrapper.getThumbnail());
                newContact.setThumbnail(userWrapper.getThumbnail());
            }

            runOnUiThread(() -> {
                responseProgressBar.setVisibility(View.GONE);

                responseIcon.setImageResource(R.drawable.ic_resp_success);
                responseIcon.setVisibility(View.VISIBLE);
                responseMessage.setText("Contato encontrado");
                responseButton.setVisibility(View.VISIBLE);
                responseButton.setText("Adicionar contato");
                responseButton.setOnClickListener(v1 -> adicionarContato(newContact));
            });

        } else {
            runOnUiThread(() -> {
                responseProgressBar.setVisibility(View.GONE);

                responseIcon.setImageResource(R.drawable.ic_resp_failure);
                responseIcon.setVisibility(View.VISIBLE);
                responseMessage.setText("Contato não encontrado");
                responseButton.setVisibility(View.VISIBLE);
                responseButton.setText("Tentar novamente");
                responseButton.setOnClickListener(v12 -> positiveButton.performClick());
            });
        }
    }

    private class PositiveButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String token = inputEditText.getText().toString();

            //Validação do TOKEN
            if (!"".equals(token) && !getTalkinzApp().getMainUser().getSearchToken().equals(token)){

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);

                //verificar se o usuário já está adicionado
                User contact;
                Future<User> loadingContact = AppDefaultExecutor.getOrchestraBackloadMaxPriorityThread().submit(() ->
                        getTalkinzApp().getTalkingzDB().userDAO().getByToken(token));
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

                if (getTalkinzApp().isConnectionOpen()) {
                    //se não retornou significa que não há esse token na lista de contatos
                    validationErrorPanel.setVisibility(View.GONE);

                    responseProgressBar.setVisibility(View.VISIBLE);
                    responseIcon.setVisibility(View.GONE);
                    responseMessage.setText("Procurando contato com o token ("+token+")");
                    responseMessage.setVisibility(View.VISIBLE);
                    responseButton.setVisibility(View.GONE);

                    //Busca Contato
                    CommandFindContact commandFindContact = new CommandFindContact();
                    commandFindContact.setSearchToken(token);
                    getTalkinzApp().getWsClient().sendCommandOrFeedBack(commandFindContact);

                    //Aguarda retorno - tratado no método handle

                } else {
                    DialogUtility.showConnectionNotAvailableInfo(getWindow().getContext());
                }


            } else if (getTalkinzApp().getMainUser().getSearchToken().equals(token)) {
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
