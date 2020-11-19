package br.com.luisfga.talkingz.app.ui.attachments;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.background.OrchestraCache;
import br.com.luisfga.talkingz.app.ui.OrchestraAbstractRootActivity;
import br.com.luisfga.talkingz.app.utils.BitmapUtility;
import br.com.luisfga.talkingz.app.utils.FileUtility;
import br.com.luisfga.talkingz.commons.Mimetype;

public class AttachNewMediaActivity extends OrchestraAbstractRootActivity {

    private final String TAG = "AttachNewMediaActivity";

    private Bitmap mediaThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setContentView(R.layout.activity_new_media);

        int selectedMediaType = getIntent().getIntExtra(FileUtility.MEDIA_TYPE_EXTRA_KEY, FileUtility.MEDIA_TYPE_IMAGE);

        dispatchCameraIntent(selectedMediaType);

    }

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private Uri fileUri;

    private void dispatchCameraIntent(int type) {
        if (type == FileUtility.MEDIA_TYPE_IMAGE) {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = FileUtility.getOutputMediaFileUri(getApplicationContext(), FileUtility.MEDIA_TYPE_IMAGE, orchestraApp.getMainUser().getId().toString()); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        } else if (type == FileUtility.MEDIA_TYPE_VIDEO) {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            fileUri = FileUtility.getOutputMediaFileUri(getApplicationContext(), FileUtility.MEDIA_TYPE_VIDEO, orchestraApp.getMainUser().getId().toString()); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // set the video image quality to low

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            File newMediaFile = new File(fileUri.getPath());
            this.mediaThumbnail = BitmapUtility.decodeSampledBitmapFromUri(getContentResolver(), fileUri, 25, true);

            //seta na view
            ImageView addPhotoImageView = findViewById(R.id.add_photo_image_view);
            addPhotoImageView.setImageBitmap(mediaThumbnail);

            //botão Cancelar
            ImageButton addPhotoCancelButton = findViewById(R.id.add_photo_cancel_button);
            addPhotoCancelButton.setOnClickListener(v -> finish());

            //texto
            EditText addPhotoMsgEditText = findViewById(R.id.add_photo_msg_edit_text);

            //botão enviar
            ImageButton addPhotoSendButton = findViewById(R.id.add_photo_send_button);
            addPhotoSendButton.setOnClickListener(v -> {

                //coloca bitmap num cache pra ser recuperado na atividade de mensagem sem passar pelo java binder
                OrchestraCache.getInstance().getLru().put("mediaThumbnail", mediaThumbnail);

                //salvarDados em extras
                Intent resultIntent = new Intent();
                resultIntent.putExtra("msg", addPhotoMsgEditText.getText().toString());
                resultIntent.putExtra("mediaUri", fileUri);
                resultIntent.putExtra("mimeType", Mimetype.IMAGE_GENERIC);
                setResult(RESULT_OK, resultIntent);
                finish();
            });

        } else if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            File newMediaFile = new File(fileUri.getPath());
            String newMediaFileName = newMediaFile.getName();
            String newMediaFilePath = newMediaFile.getAbsolutePath();
            this.mediaThumbnail = ThumbnailUtils.createVideoThumbnail(newMediaFilePath, MediaStore.Video.Thumbnails.MINI_KIND);

            //seta na view
            ImageView addPhotoImageView = findViewById(R.id.add_photo_image_view);
            addPhotoImageView.setImageBitmap(mediaThumbnail);

            //botão Cancelar
            ImageButton addPhotoCancelButton = findViewById(R.id.add_photo_cancel_button);
            addPhotoCancelButton.setOnClickListener(v -> finish());

            //texto
            EditText addPhotoMsgEditText = findViewById(R.id.add_photo_msg_edit_text);

            //botão enviar
            ImageButton addPhotoSendButton = findViewById(R.id.add_photo_send_button);
            addPhotoSendButton.setOnClickListener(v -> {

                //coloca bitmap num cache pra ser recuperado na atividade de mensagem sem passar pelo java binder
                OrchestraCache.getInstance().getLru().put("mediaThumbnail", mediaThumbnail);

                //salvarDados em extras
                Intent resultIntent = new Intent();
                resultIntent.putExtra("msg", addPhotoMsgEditText.getText().toString());
                resultIntent.putExtra("mediaFilename", newMediaFileName);
                resultIntent.putExtra("mimeType", Mimetype.VIDEO_MPEG);
                setResult(RESULT_OK, resultIntent);
                finish();
            });


        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
    }

    private final String FILE_URI_KEY = "FILE_URI_KEY";
    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(FILE_URI_KEY, fileUri);
        super.onSaveInstanceState(outState);
    }

    // This callback is called only when there is a saved instance that is previously saved by using
    // onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
    // other state here, possibly usable after onStart() has completed.
    // The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = (Uri) savedInstanceState.get(FILE_URI_KEY);
    }
}