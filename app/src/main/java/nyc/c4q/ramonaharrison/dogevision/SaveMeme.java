package nyc.c4q.ramonaharrison.dogevision;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SaveMeme
{

    /**
     * Helper class for taking a Bitmap screenshot of the meme and saving it to the gallery.
     */

    public Bitmap loadBitmapFromView(FrameLayout view)
    {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public String saveMeme(Bitmap bitmap, String name, ContentResolver contentResolver)
    {

        OutputStream outputStream;
        String directory = Environment.getExternalStorageDirectory().toString();
        String path = "";
        File file = new File(directory, name);

        try
        {
            outputStream = new FileOutputStream(file);

            // Compress image
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            // Update image to gallery
            path = MediaStore.Images.Media.insertImage(contentResolver,
                    file.getAbsolutePath(), file.getName(), file.getName());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return path;
    }
}