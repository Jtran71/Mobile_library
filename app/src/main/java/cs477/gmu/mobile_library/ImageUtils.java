package cs477.gmu.mobile_library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static void loadBookCover(ImageView imageView, String imagePath, int placeholderResId) {
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("local:")) {
                File imageFile = new File(imagePath.substring(6));
                Picasso.get()
                        .load(imageFile)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
            } else {
                Picasso.get()
                        .load(imagePath)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
            }
        } else {
            imageView.setImageResource(placeholderResId);
        }
    }

    public static String saveImageLocally(Context context, Bitmap bitmap, String bookId) {
        try {
            File storageDir = new File(context.getFilesDir(), "book_covers");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, bookId + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            return "local:" + imageFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    public static Bitmap compressImage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int maxWidth = 800;
            int maxHeight = 1200;
            int scale = 1;
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                int heightScale = Math.round((float) options.outHeight / maxHeight);
                int widthScale = Math.round((float) options.outWidth / maxWidth);
                scale = Math.max(heightScale, widthScale);
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;

            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    public static void deleteLocalImage(String imagePath) {
        if (imagePath != null && imagePath.startsWith("local:")) {
            File imageFile = new File(imagePath.substring(6));
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }
}

