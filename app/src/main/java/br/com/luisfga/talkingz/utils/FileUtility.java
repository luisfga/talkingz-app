package br.com.luisfga.talkingz.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtility {

    public static final byte MEDIA_TYPE_IMAGE = 1;
    public static final byte MEDIA_TYPE_VIDEO = 2;

    public static final String MEDIA_TYPE_EXTRA_KEY = "MEDIA_TYPE_EXTRA_KEY";

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(Context context, byte type, long userId){

        // Add a specific media item.
        ContentResolver resolver = context.getContentResolver();

        //https://developer.android.com/training/data-storage/shared/media#add-item
        // Find all audio files on the primary external storage device.
        // On API <= 28, use VOLUME_EXTERNAL instead.
        Uri filesCollection;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        // Keeps a handle to the new song's URI in case we need to modify it later.
        Uri newFileUri;
        if (type == MEDIA_TYPE_IMAGE){
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
                filesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                filesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            // Publish a new song.
            ContentValues newFileDetails = new ContentValues();
            newFileDetails.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_"+userId+"_"+timeStamp+".jpg");
            return resolver.insert(filesCollection, newFileDetails);

        } else if(type == MEDIA_TYPE_VIDEO) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
                filesCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                filesCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            // Publish a new song.
            ContentValues newFileDetails = new ContentValues();
            newFileDetails.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_"+userId+"_"+timeStamp+".mp4");
            return resolver.insert(filesCollection, newFileDetails);

        } else {
            return null;
        }
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(Context context, byte type, String downloadToken){

        // Add a specific media item.
        ContentResolver resolver = context.getContentResolver();

        //https://developer.android.com/training/data-storage/shared/media#add-item
        // Find all audio files on the primary external storage device.
        // On API <= 28, use VOLUME_EXTERNAL instead.
        Uri filesCollection;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        // Keeps a handle to the new song's URI in case we need to modify it later.
        Uri newFileUri;
        if (type == MEDIA_TYPE_IMAGE){
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
                filesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                filesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            // Publish a new song.
            ContentValues newFileDetails = new ContentValues();
            newFileDetails.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_"+downloadToken+".jpg");
            return resolver.insert(filesCollection, newFileDetails);

        } else if(type == MEDIA_TYPE_VIDEO) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
                filesCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                filesCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            // Publish a new song.
            ContentValues newFileDetails = new ContentValues();
            newFileDetails.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_"+downloadToken+".mp4");
            return resolver.insert(filesCollection, newFileDetails);

        } else {
            return null;
        }
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(Context context, byte type, long userId){
        Uri outputMediaFileUri = getOutputMediaFileUri(context, type, userId);
        if (outputMediaFileUri != null)
            return new File(outputMediaFileUri.getPath());
        else
            return null;
    }

    public static boolean fileExists(Context context, String fileName) {
//        return new File(getMediaStorageDir(context), fileName).exists();
        return false;
    }

    public static void openMediaFile(Context context, String fileName) throws IOException {
//        // Create URI
//        File file = new File(getMediaStorageDir(context), fileName);
//        Uri uri = Uri.fromFile(file);
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        if(fileName.toString().contains(".wav") || fileName.toString().contains(".mp3")) {
//            // WAV audio file
//            intent.setDataAndType(uri, "audio/x-wav");
//        } else if(fileName.toString().contains(".gif")) {
//            // GIF file
//            intent.setDataAndType(uri, "image/gif");
//        } else if(fileName.toString().contains(".jpg") || fileName.toString().contains(".jpeg") || fileName.toString().contains(".png")) {
//            // JPG file
//            intent.setDataAndType(uri, "image/jpeg");
//        } else if(fileName.toString().contains(".3gp") || fileName.toString().contains(".mpg") || fileName.toString().contains(".mpeg") || fileName.toString().contains(".mpe") || fileName.toString().contains(".mp4") || fileName.toString().contains(".avi")) {
//            // Video files
//            intent.setDataAndType(uri, "video/*");
//        } else {
//            Toast.makeText(context, "Erro - não é um arquivo de mídia", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        context.startActivity(intent);
    }

//    public static void openAnyFile(Context context, String fileName) throws IOException {
//        // Create URI
//        File file = new File(getMediaStorageDir(context), fileName);
//        Uri uri = Uri.fromFile(file);
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        // Check what kind of file you are trying to open, by comparing the url with extensions.
//        // When the if condition is matched, plugin sets the correct intent (mime) type,
//        // so Android knew what application to use to open the file
//        if (fileName.toString().contains(".doc") || fileName.toString().contains(".docx")) {
//            // Word document
//            intent.setDataAndType(uri, "application/msword");
//        } else if(fileName.toString().contains(".pdf")) {
//            // PDF file
//            intent.setDataAndType(uri, "application/pdf");
//        } else if(fileName.toString().contains(".ppt") || fileName.toString().contains(".pptx")) {
//            // Powerpoint file
//            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
//        } else if(fileName.toString().contains(".xls") || fileName.toString().contains(".xlsx")) {
//            // Excel file
//            intent.setDataAndType(uri, "application/vnd.ms-excel");
//        } else if(fileName.toString().contains(".zip") || fileName.toString().contains(".rar")) {
//            // WAV audio file
//            intent.setDataAndType(uri, "application/x-wav");
//        } else if(fileName.toString().contains(".rtf")) {
//            // RTF file
//            intent.setDataAndType(uri, "application/rtf");
//        } else if(fileName.toString().contains(".wav") || fileName.toString().contains(".mp3")) {
//            // WAV audio file
//            intent.setDataAndType(uri, "audio/x-wav");
//        } else if(fileName.toString().contains(".gif")) {
//            // GIF file
//            intent.setDataAndType(uri, "image/gif");
//        } else if(fileName.toString().contains(".jpg") || fileName.toString().contains(".jpeg") || fileName.toString().contains(".png")) {
//            // JPG file
//            intent.setDataAndType(uri, "image/jpeg");
//        } else if(fileName.toString().contains(".txt")) {
//            // Text file
//            intent.setDataAndType(uri, "text/plain");
//        } else if(fileName.toString().contains(".3gp") || fileName.toString().contains(".mpg") || fileName.toString().contains(".mpeg") || fileName.toString().contains(".mpe") || fileName.toString().contains(".mp4") || fileName.toString().contains(".avi")) {
//            // Video files
//            intent.setDataAndType(uri, "video/*");
//        } else {
//            //if you want you can also define the intent type for any other file
//
//            //additionally use else clause below, to manage other unknown extensions
//            //in this case, Android will show all applications installed on the device
//            //so you can choose which application to use
//            intent.setDataAndType(uri, "*/*");
//        }
//
//        context.startActivity(intent);
//    }
}
