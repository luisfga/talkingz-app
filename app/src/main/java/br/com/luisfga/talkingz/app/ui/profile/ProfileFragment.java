package br.com.luisfga.talkingz.app.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.database.entity.user.User;
import br.com.luisfga.talkingz.app.ui.OrchestraAbstractRootFragment;
import br.com.luisfga.talkingz.app.utils.AppDefaultExecutor;
import br.com.luisfga.talkingz.app.utils.BitmapUtility;
import br.com.luisfga.talkingz.app.utils.DialogUtility;
import br.com.luisfga.talkingz.app.utils.FileUtility;
import br.com.luisfga.talkingz.commons.UserWrapper;
import br.com.luisfga.talkingz.commons.orchestration.command.CommandSyncUser;
import com.google.android.material.navigation.NavigationView;

public class ProfileFragment extends OrchestraAbstractRootFragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView thumbnail = getView().findViewById(R.id.thumbnail);
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseDialog();
            }
        });

        User mainUser = getTalkingzApp().getMainUser();
        if(mainUser.getThumbnail() != null && !"".equals(mainUser.getThumbnail())) {
            thumbnail.setImageBitmap(BitmapUtility.getBitmapFromBytes(mainUser.getThumbnail()));
        }

        EditText name = getView().findViewById(R.id.name);
        name.setText(mainUser.getName());

        EditText email = getView().findViewById(R.id.email);
        email.setText(mainUser.getEmail());

        EditText token = getView().findViewById(R.id.search_token);
        token.setText(mainUser.getSearchToken());

        TextView joinDate = getView().findViewById(R.id.joinDate);
        Locale locale = new Locale("pt", "BR");
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        joinDate.setText(simpleDateFormat.format(new Date(mainUser.getJoinTime())));

        Button buttonUpdate = getView().findViewById(R.id.updateButton);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainUser.setName(name.getText().toString());
                mainUser.setEmail(email.getText().toString());
                mainUser.setSearchToken(token.getText().toString());

                AppDefaultExecutor.getOrchestraNormalPriorityThread().execute(() -> getTalkingzApp().getTalkingzDB().userDAO().update(mainUser));

                syncronizeMainUser(mainUser);
                updateUserScreenLabels(mainUser);
            }
        });
    }

    private void showChooseDialog(){
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //Option1
        ImageButton option1 = alertDialog.findViewById(R.id.dialog_choose_opt1);
        option1.setImageResource(R.drawable.ic_gallery);
        option1.setOnClickListener(v -> {
            dispatchGalleryIntent();
            alertDialog.dismiss();
        });

        ImageButton option2 = alertDialog.findViewById(R.id.dialog_choose_opt2);
        option2.setImageResource(R.drawable.ic_camera);
        option2.setOnClickListener(v -> {

            if(!hasPermissions(getContext(), new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_FOR_CAMERA_AND_WRITE_EXTERNAL_STORAGE);
            } else {
                dispatchCameraIntent();
            }

            alertDialog.dismiss();
        });
    }

    /**
     * Set updated data to screen labels, like 'name' and 'token' on Navigation Drawer Header
     * @param mainUser
     */
    private void updateUserScreenLabels(User mainUser){

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

        //set user name on header
        TextView navHeaderUserName =  navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        navHeaderUserName.setText(getTalkingzApp().getMainUser().getName());
        //set user search token on header
        TextView navHeaderSearchToken =  navigationView.getHeaderView(0).findViewById(R.id.nav_header_search_token);
        navHeaderSearchToken.setText(getTalkingzApp().getMainUser().getSearchToken());
    }

    /**
     * Send updated data to server
     * @param mainUser
     */
    private void syncronizeMainUser(User mainUser){

        if (getTalkingzApp().isConnectionOpen()){
            UserWrapper userWrapper = new UserWrapper();
            userWrapper.setId(mainUser.getId());
            userWrapper.setName(mainUser.getName());
            userWrapper.setEmail(mainUser.getEmail());
            userWrapper.setSearchToken(mainUser.getSearchToken());
            userWrapper.setJoinTime(mainUser.getJoinTime());

            //decode thumb from file and convert to byte[] to send
            userWrapper.setThumbnail(mainUser.getThumbnail());

            CommandSyncUser commandSyncUser = new CommandSyncUser();
            commandSyncUser.setUserWrapper(userWrapper);

            getTalkingzApp().getWsClient().sendCommandOrFeedBack(commandSyncUser);

        } else {
            DialogUtility.showConnectionNotAvailableInfo(getContext());
        }
    }

    public static final int REQUEST_FOR_READ_FROM_GALLERY = 1;
    public static final int REQUEST_FOR_CAMERA_AND_WRITE_EXTERNAL_STORAGE = 2;

    public static boolean hasPermissions(Context context, String... permissions) {
        //Checagens necessárias apenas para Android 6.0 ou superior
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (context != null && permissions != null) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FOR_CAMERA_AND_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                    dispatchCameraIntent();
                } else {
                    Toast.makeText(getActivity(), "Permissão Negada", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private void dispatchCameraIntent() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = FileUtility.getOutputMediaFileUri(getContext(), FileUtility.MEDIA_TYPE_IMAGE, getTalkingzApp().getMainUser().getId().toString()); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private static final int PICK_IMAGE = 2;
    private void dispatchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(galleryIntent, PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            //carrega bitmap
            Bitmap bitmap = BitmapUtility.decodeSampledBitmapFromUri(getContext().getContentResolver(), fileUri, 25, true);

            //atualiza objeto do usuário
            User mainUser = getTalkingzApp().getMainUser();
            mainUser.setThumbnail(BitmapUtility.getBytesFromBitmap(bitmap));

            //coloca a imagem na view
            ImageView thumbnail = getActivity().findViewById(R.id.thumbnail);
            thumbnail.setImageBitmap(bitmap);

        } else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            Bitmap bitmap = BitmapUtility.decodeSampledBitmapFromUri(getContext().getContentResolver(), data.getData(), 25, true);

            //save file on disk and point database
            getTalkingzApp().getMainUser().setThumbnail(BitmapUtility.getBytesFromBitmap(bitmap));

            ImageView thumbnail = getActivity().findViewById(R.id.thumbnail);
            thumbnail.setImageBitmap(bitmap);
        }
    }

    private final String FILE_URI_KEY = "FILE_URI_KEY";
    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(FILE_URI_KEY, fileUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            fileUri = (Uri) savedInstanceState.get(FILE_URI_KEY);
        }
    }
}