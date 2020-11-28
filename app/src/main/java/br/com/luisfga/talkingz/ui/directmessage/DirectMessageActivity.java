package br.com.luisfga.talkingz.ui.directmessage;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import br.com.luisfga.talkingz.FileTransferWSClient;
import br.com.luisfga.talkingz.utils.TalkingzCache;
import br.com.luisfga.talkingz.ui.attachments.AttachNewMediaActivity;
import br.com.luisfga.talkingz.R;
import br.com.luisfga.talkingz.database.entity.User;
import br.com.luisfga.talkingz.database.viewmodels.DirectMessageViewModel;
import br.com.luisfga.talkingz.database.viewmodels.DirectMessageViewModelFactory;
import br.com.luisfga.talkingz.database.entity.DirectMessage;
import br.com.luisfga.talkingz.ui.TalkingzAbstractRootActivity;
import br.com.luisfga.talkingz.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.utils.BitmapUtility;
import br.com.luisfga.talkingz.utils.FileUtility;
import br.com.luisfga.talkingz.commons.constants.MessageStatus;
import br.com.luisfga.talkingz.commons.MessageWrapper;
import br.com.luisfga.talkingz.commons.constants.Mimetype;
import br.com.luisfga.talkingz.commons.orchestration.command.CommandSend;
import br.com.luisfga.talkingz.commons.orchestration.response.CommandGetFile;
import br.com.luisfga.talkingz.commons.orchestration.response.ResponseCommandGetFile;
import br.com.luisfga.talkingz.commons.orchestration.response.dispatching.ResponseHandler;

public class DirectMessageActivity extends TalkingzAbstractRootActivity implements ResponseHandler<ResponseCommandGetFile> {

    public static final String CONTACT_ID_KEY = "CONTACT_ID_KEY";
    private static final String TAG = "DirectMessageActivity";

    DirectMessageViewModel directMessageViewModel;

    View rootLayout;

    ListView msgsListView;

    ImageView contactThumbnail;
    TextView contactName;

    EditText msgEditText;
    ImageButton attachButton;
    ImageButton micButton;

    ImageButton sendButton;

    User contact;

    LinearLayout actionPanel;

    FileTransferWSClient fileTransferWSClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootLayout =  getLayoutInflater().inflate(R.layout.activity_direct_message, null);
        setContentView(rootLayout);

        UUID contactId = UUID.fromString(getIntent().getExtras().getString(CONTACT_ID_KEY));

        //SET MESSAGES RECYCLER
        msgsListView = findViewById(R.id.msgs_list_view);

        UUID mainUserId = getTalkinzApp().getMainUser().getId();
        directMessageViewModel = new ViewModelProvider(this, new DirectMessageViewModelFactory(getApplication(), contactId, mainUserId)).get(DirectMessageViewModel.class);
        DirectMessageListAdapter adapter = new DirectMessageListAdapter(this, mainUserId);
        msgsListView.setAdapter(adapter);

        //itemModelView's list will be observed to refresh the adapter's list
        directMessageViewModel.getAllMessages().observe(this, new Observer<List<DirectMessage>>() {
            @Override
            public void onChanged(List<DirectMessage> directMessages) {
                //refresh list fragment
                adapter.setItems(directMessages);
                msgsListView.deferNotifyDataSetChanged();
                msgsListView.smoothScrollToPosition(adapter.getCount()-1);
            }
        });

        //SET OTHERS COMPONENTS
        //load contact
        Future<User> loadingContact = AppDefaultExecutor.getTalkingzNormalPriorityThread().submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return getTalkinzApp().getTalkingzDB().userDAO().getById(contactId);
            }
        });
        try {
            contact = loadingContact.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        contactThumbnail = findViewById(R.id.contact_thumbnail);
        if(contact.getThumbnail() != null) {
//            Bitmap bitmap = BitmapUtility.loadContactThumbnail(getApplicationContext(),contact,100);
            Bitmap bitmap = BitmapUtility.getBitmapFromBytes(contact.getThumbnail());
            bitmap = BitmapUtility.centerCropSquare(bitmap);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            contactThumbnail.setImageDrawable(roundedBitmapDrawable);
        }
        contactName = findViewById(R.id.contact_name);
        contactName.setText(contact.getName());

        //action panel
        actionPanel = findViewById(R.id.action_panel); //taken to positioning attachements options panel
        msgEditText = findViewById(R.id.msg_edit_text);
        msgEditText.addTextChangedListener(new MsgEditTextWatcher());
        attachButton = findViewById(R.id.attach_button);
        micButton = findViewById(R.id.mic_button);
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new SendButtonClickListener());

        //set attachments menu
        View popupMenuWindowView = getLayoutInflater().inflate(R.layout.popup_menu_window,null);
        RelativeLayout attachmentsMenuRelativeLayout = popupMenuWindowView.findViewById(R.id.attachments_menu_relative_layout);
        PopupWindow popupWindow = new PopupWindow(popupMenuWindowView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.getContentView().setOnClickListener(v -> popupWindow.dismiss());

        Button fotoMenuButton = popupMenuWindowView.findViewById(R.id.foto_menu_button);
        fotoMenuButton.setOnClickListener(v1 -> {
            Intent attachNewPhotoMediaIntent = new Intent(getApplicationContext(), AttachNewMediaActivity.class);
            attachNewPhotoMediaIntent.putExtra(FileUtility.MEDIA_TYPE_EXTRA_KEY, FileUtility.MEDIA_TYPE_IMAGE);
            startActivityForResult(attachNewPhotoMediaIntent, ATTACH_PHOTO_REQUEST_CODE);
            popupWindow.dismiss();
        });

        Button videoMenuButton = popupMenuWindowView.findViewById(R.id.video_menu_button);
        videoMenuButton.setOnClickListener(v1 -> {
            Intent attachNewVideoMediaIntent = new Intent(getApplicationContext(), AttachNewMediaActivity.class);
            attachNewVideoMediaIntent.putExtra(FileUtility.MEDIA_TYPE_EXTRA_KEY, FileUtility.MEDIA_TYPE_VIDEO);
            startActivityForResult(attachNewVideoMediaIntent, ATTACH_VIDEO_REQUEST_CODE);
            popupWindow.dismiss();
        });

        attachButton.setOnClickListener(v -> {
            popupWindow.showAtLocation(actionPanel, Gravity.BOTTOM, 0, 0);
            int[] coordinates = new int[2];
            actionPanel.getLocationOnScreen(coordinates);
            attachmentsMenuRelativeLayout.setTranslationY(coordinates[1]-actionPanel.getHeight()-201);
        });
    }

    private final int ATTACH_PHOTO_REQUEST_CODE = 1;
    private final int ATTACH_VIDEO_REQUEST_CODE = 2;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ATTACH_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String msg = data.getStringExtra("msg");
            byte mimeType = data.getByteExtra("mimeType", Mimetype.IMAGE_GENERIC);

            //pega bitmap do cache LRU
            Bitmap mediaThumbnail = (Bitmap) TalkingzCache.getInstance().getLru().get("mediaThumbnail");
            byte[] mediaThumbnailBytes = BitmapUtility.getBytesFromBitmap(mediaThumbnail);

            Uri mediaUri = data.getParcelableExtra("mediaUri");

            saveNewMessage(msg, mediaThumbnailBytes, mediaUri, mimeType);

        } else if (requestCode == ATTACH_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String msg = data.getStringExtra("msg");
            byte mimeType = data.getByteExtra("mimeType", Mimetype.IMAGE_GENERIC);

            //pega bitmap do cache LRU
            Bitmap mediaThumbnail = (Bitmap) TalkingzCache.getInstance().getLru().get("mediaThumbnail");
            byte[] mediaThumbnailBytes = BitmapUtility.getBytesFromBitmap(mediaThumbnail);

            Uri mediaUri = data.getParcelableExtra("mediaUri");

            saveNewMessage(msg, mediaThumbnailBytes, mediaUri, mimeType);

        }
    }

    private void saveNewMessage(String msg, byte[] mediaThumbnailBytes, Uri mediaUri, byte mimeType) {
        //Log.i(i(TAG, "saveNewMessage");
        //#ProcessoMensagem#1 - salva a imagem no banco do cliente
        DirectMessage directMessage = new DirectMessage();

        UUID mainUserId = getTalkinzApp().getMainUser().getId();
        UUID messageUUID = UUID.randomUUID();

        directMessage.setId(messageUUID);
        directMessage.setDestId(contact.getId());
        directMessage.setSenderId(getTalkinzApp().getMainUser().getId());
        directMessage.setContent(msg);
        directMessage.setSentTime(new Timestamp(System.currentTimeMillis()));
        directMessage.setStatus(MessageStatus.MSG_STATUS_SENT);
        directMessage.setMimeType(mimeType);
        if (mediaUri != null) {
            directMessage.setMediaUriPath(mediaUri.toString());
            directMessage.setMediaDownloadToken(mainUserId+"_"+mediaUri.getLastPathSegment());
            directMessage.setMediaThumbnail(mediaThumbnailBytes);
        }

        //Log.i(i(TAG,"Salvando mensagem");
        directMessageViewModel.insert(directMessage);
        //Log.i(i(TAG,"Mensagem salva");

        //Log.i(i(TAG, "Mensagem pronta para ser enviada");
        if (getTalkinzApp().isConnectionOpen()) {
            //montando mensagem websocket
            MessageWrapper messageWrapper = new MessageWrapper();
            messageWrapper.setId(directMessage.getId());
            messageWrapper.setSenderId(directMessage.getSenderId().toString());
            messageWrapper.setDestId(directMessage.getDestId().toString());
            messageWrapper.setSentTimeInMilis(directMessage.getSentTime().getTime());
            messageWrapper.setContent(directMessage.getContent());
            messageWrapper.setMimetype(directMessage.getMimeType());
            messageWrapper.setDownloadToken(directMessage.getMediaDownloadToken());
            messageWrapper.setMediaThumbnail(mediaThumbnailBytes);

            //Log.i(i(TAG, "Enviando mensagem");
            CommandSend commandSend = new CommandSend();
            commandSend.setMessageWrapper(messageWrapper);
            getTalkinzApp().getMessagingService().getWsClient().sendCommandOrFeedBack(commandSend);

            //enviar arquivo de mídia, se for o caso
            if (directMessage.getMediaUriPath() != null) {
                AppDefaultExecutor.getTalkingzLowPriorityNetworkingThreadPool().execute(() -> {
                    try {
                        fileTransferWSClient = new FileTransferWSClient(
                                FileTransferWSClient.ACTION_SEND, null,
                                directMessage.getMediaUriPath(), directMessage.getMimeType(), directMessage.getMediaDownloadToken());
                        fileTransferWSClient.conectar(getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    void sendCommandGetFile(String downloadToken) {
        CommandGetFile commandGetFile = new CommandGetFile();
        commandGetFile.setDownloadToken(downloadToken);
        getTalkinzApp().getMessagingService().getWsClient().sendCommandOrFeedBack(commandGetFile);
        getTalkinzApp().getMessagingService().getWsClient().setResponseCommandGetFileHandler(this);
    }

    @Override
    public void handleResponse(ResponseCommandGetFile responseCommandGetFile) {
        Toast.makeText(this, responseCommandGetFile.getMessage(), Toast.LENGTH_SHORT);
    }

    private class MsgEditTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals("")) {
                attachButton.setVisibility(View.VISIBLE);
                micButton.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.GONE);
            } else {
                attachButton.setVisibility(View.GONE);
                micButton.setVisibility(View.GONE);
                sendButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class SendButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            saveNewMessage(msgEditText.getText().toString(), null, null, Mimetype.TXT);
            msgEditText.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(msgEditText.getWindowToken(), 0);
        }
    }
}