package orbital.com.menusnap.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Abel on 6/7/2016.
 */

public class ImageUtils {
    public static final String COMPRESSED = "(COMPRESSED)";
    private static final float maxHeight = 1280.0f;
    private static final float maxWidth = 1280.0f;

    public static byte[] compressImage(Context context, Uri imageUri) throws FileNotFoundException {
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bmp = BitmapFactory.decodeStream(imageStream, null, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        options.inSampleSize = ImageUtils.calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            bmp = BitmapFactory.decodeStream(imageStream, null, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            if (imageStream != null) {
                imageStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ByteArrayOutputStream greyOut = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap greyScaleBmp = toGrayscale(scaledBitmap);
        greyScaleBmp.compress(Bitmap.CompressFormat.JPEG, 85, greyOut);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        byte[] coloredResult = out.toByteArray();
        try {
            writeFile(context, coloredResult, imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return greyOut.toByteArray();
    }

    private static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpCopy = Bitmap.createBitmap(bmpOriginal);
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpCopy, 0, 0, paint);
        return bmpGrayscale;
    }

    public static void writeFile(Context context, byte[] data, Uri outputUri) throws IOException {
        OutputStream out = context.getContentResolver().openOutputStream(outputUri);
        if (out != null) {
            out.write(data);
            out.close();
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static boolean isLandscape(Context context, Uri imageUri) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        int height, width;
        InputStream imageStream;
        Metadata metadata;
        ExifIFD0Directory ifd0Directory;
        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            metadata = ImageMetadataReader.readMetadata(imageStream);
            ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
            return true;
        }
        try{
            orientation = ifd0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (MetadataException e) {
            e.printStackTrace();
        }
        try {
            imageStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            height = options.outHeight;
            width = options.outWidth;
            if (imageStream != null) {
                imageStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                return width > height;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return false;
            default:
                return true;
        }
    }
}
