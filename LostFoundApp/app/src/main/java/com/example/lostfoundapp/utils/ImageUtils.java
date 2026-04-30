package com.example.lostfoundapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(imageUri);
            if (in == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();

            if (bitmap.getWidth() > 1024) {
                float ratio = 1024f / bitmap.getWidth();
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        1024, (int) (bitmap.getHeight() * ratio), true);
            }

            File file = new File(context.getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    public static Bitmap loadBitmap(String path) {
        if (path == null || path.isEmpty()) return null;
        return BitmapFactory.decodeFile(path);
    }
}
