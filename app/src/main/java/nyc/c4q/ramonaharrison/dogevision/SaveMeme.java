package nyc.c4q.ramonaharrison.dogevision;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SaveMeme {

    public Bitmap loadBitmapFromView(FrameLayout view) {

        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        Bitmap bm = view.getDrawingCache();

        return bm;
    }

    public String saveMeme(Bitmap bm, String imgName, ContentResolver c) {

        OutputStream fOut = null;
        String strDirectory = Environment.getExternalStorageDirectory().toString();
        String pathBm = "";

        File f = new File(strDirectory, imgName);
        try {
            fOut = new FileOutputStream(f);

            // Compress image
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();

            // Update image to gallery
            pathBm = MediaStore.Images.Media.insertImage(c,
                    f.getAbsolutePath(), f.getName(), f.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pathBm;
    }
}