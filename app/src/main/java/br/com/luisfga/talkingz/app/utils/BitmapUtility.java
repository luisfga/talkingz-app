package br.com.luisfga.talkingz.app.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtility {

    private static final String TAG = "BitmapUtility";

    public static Bitmap decodeSampledBitmapFromUri(ContentResolver contentResolver, Uri selectedImage, int percentualTargetSize, boolean fixOrientation) {

        try {
            byte[] bytes = IOUtils.toByteArray(contentResolver.openInputStream(selectedImage));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(byteArrayInputStream, null, options);
            byteArrayInputStream.reset(); //reset to be reused

            // Calculate inSampleSize
            double factor = percentualTargetSize / 100.0;
            int reqWidth = (int)(factor * options.outWidth);
            int reqHeight = (int)(factor * options.outHeight);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false; //set false, because the same object will now be used to actually load the bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream, null, options);
            byteArrayInputStream.reset(); //reset to be reused

            Matrix orientationMatrix = fixOrientation ? getFixedOrientationMatrix(byteArrayInputStream) : null;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), orientationMatrix, false);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            return bitmap;

        } catch (IOException e) {
            Log.e(TAG, "Erro ao tentar carregar Bitmap", e);
        }
        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Matrix getFixedOrientationMatrix(InputStream inputStream){
        //checking orientation
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix mtx = null;
        if (rotate != 0) {
            // Setting pre rotate
            mtx = new Matrix();
            mtx.preRotate(rotate);
        }
        return mtx;
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getBitmapFromBytes(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static Bitmap centerCropSquare(Bitmap srcBmp) {
        Bitmap dstBmp;
        //Primeiro "Cropa" a partir do centro da imagem
        if (srcBmp.getWidth() >= srcBmp.getHeight()){ //se for paisagem
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        } else { //se for retrato
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
}