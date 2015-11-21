package nyc.c4q.ramonaharrison.dogevision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


public class MainActivity extends ActionBarActivity {

    // TODO: you must add mashape API key here before compiling
    private static final String MASHAPE_KEY = "cGsl2kc7rBmshwL6R0AIXUnONyBDp19n2LzjsnWhosH4D5c2ey";
    private static final String PHOTO = "photoUri";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final ButterKnife.Setter<TextView, Typeface> SETFONT = new ButterKnife.Setter<TextView, Typeface>() {
        @Override
        public void set(TextView view, Typeface font, int index) {
            view.setTypeface(font);
        }
    };

    private int touchX, touchY, deltaX, deltaY;
    private FrameLayout.LayoutParams layoutParams;
    private boolean editMode;
    private Uri photoUri;

    @Bind(R.id.meme)
    FrameLayout meme;
    @Bind(R.id.root)
    ViewGroup rootLayout;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.dogeView)
    ImageView dogeView;
    @Bind(R.id.photoView)
    ImageView photoView;
    @Bind(R.id.fab)
    FloatingActionButton cameraButton;
    @Bind({R.id.redText, R.id.yellowText, R.id.cyanText, R.id.greenText, R.id.magentaText})
    List<TextView> dogeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializeUi();
        editMode = false;

        if (savedInstanceState != null) {
            // If there is a savedInstanceState, recover the
            photoUri = savedInstanceState.getParcelable(PHOTO);
            if (photoUri != null) {
                Picasso.with(this).load(new File(photoUri.toString())).into(photoView);
                editMode = true;
            }
        }
    }


    /**
     * Sets up the UI: applies typeface assets to the meme TextViews,
     * loads Doge image into the dogeView, hides the progress bar.
     */

    private void initializeUi() {
        Typeface comic_sans = Typeface.createFromAsset(getAssets(), "ComicSans.ttf");
        ButterKnife.apply(dogeText, SETFONT, comic_sans);
        Picasso.with(this).load(R.drawable.doge).into(dogeView);
        progressBar.setVisibility(View.INVISIBLE);
    }


    /**
     * Binds an onClickListener to the camera FAB. When the button is clicked,
     * a new file is created, the Uri is saved, and an intent is launched
     * to retrieve a photo from the camera.
     */

    @OnClick(R.id.fab)
    public void launchCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
                photoUri = (Uri.parse(photoFile.getAbsolutePath()));
            } catch (IOException ex) {
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }


    /**
     * Returns a temporary file that will be used to store the image returned from the camera intent.
     *
     * @return the temp file for storing the image
     */

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,                      /* prefix */
                ".jpg",                             /* suffix */
                externalStoragePublicDirectory      /* directory */
        );

        return image;
    }


    /**
     * Binds an onTouchListener to the meme TextViews. When a TextView is touched/dragged,
     * the XY touch coordinates are retrieved and the TextView's layoutParams are updated
     * to reposition the view.
     */

    @OnTouch({R.id.redText, R.id.yellowText, R.id.cyanText, R.id.greenText, R.id.magentaText})
    public boolean onDogeTextTouch(View view, MotionEvent event) {
        touchX = (int) event.getRawX();
        touchY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                deltaX = touchX - layoutParams.leftMargin;
                deltaY = touchY - layoutParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = touchX - deltaX;
                layoutParams.topMargin = touchY - deltaY;
                view.setLayoutParams(layoutParams);
                break;
        }
        rootLayout.invalidate();
        return true;
    }


    /**
     * Binds an onTouchListener to the meme TextViews. When a TextView is touched/dragged,
     * the XY touch coordinates are retrieved and the TextView's layoutParams are updated
     * to reposition the view.
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK && data != null) {

                // The image capture was successful. Iterate through the TextViews to clear any strings left over from previous memes.
                int i = 0;
                while (i < dogeText.size()) {
                    dogeText.get(i).setText("");
                    i++;
                }

                // Load the image captured from the camera into the photoView.
                Picasso.with(this).load(new File(photoUri.toString())).into(photoView);

                // Unhide the progress bar while work is being done on an async task.
                progressBar.setVisibility(View.VISIBLE);

                // Start async task to upload image, retrieve token + message
                AsyncGoFetchToken goFetch = new AsyncGoFetchToken();
                goFetch.execute(photoUri.toString());

            } else if (resultCode == RESULT_CANCELED) {

                // User cancelled the image capture
                Toast.makeText(this, "Camera capture cancelled.", Toast.LENGTH_LONG).show();

            } else {

                // Image capture failed, advise user
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHOTO, photoUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            // The share button is clicked.
            if (editMode) {

                // If there is a meme to save, hide the camera button and take a screenshot to capture the meme.
                // TODO: should be moved off the UI thread!

                cameraButton.setVisibility(View.INVISIBLE);
                SaveMeme sm = new SaveMeme();
                Bitmap bitmap = sm.loadBitmapFromView(meme);
                String pathBm = sm.saveMeme(bitmap, "meme", getContentResolver());
                Uri bmUri = Uri.parse(pathBm);
                Intent attachIntent = new Intent(Intent.ACTION_SEND);
                attachIntent.putExtra(Intent.EXTRA_STREAM, bmUri);
                attachIntent.setType("image/png");
                startActivity(attachIntent);
                cameraButton.setVisibility(View.VISIBLE);
            } else {
                // There isn't a meme to save yet
                Toast.makeText(this, "Such empty canvas.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * AsyncGoFetchToken: an AsyncTask to
     * delivers it the parameters given to AsyncTask.execute()
     */


    public class AsyncGoFetchToken extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... imageUris) {

            try {
                return requestImageDescription(imageUris[0]);
            } catch (UnirestException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        private String requestImageDescription(String filepath) throws UnirestException, IOException {
            // These code snippets use an open-source library. http://unirest.io/java
            HttpResponse<InputStream> tokenResponse = Unirest.post("https://camfind.p.mashape.com/image_requests")
                    .header("X-Mashape-Key", MASHAPE_KEY)
                    .field("image_request[image]", new File(filepath))
                    .field("image_request[locale]", "en_US").asBinary();


            String token = extractTokenFromJsonStream(tokenResponse.getBody());
            return token;

        }


        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */

        protected void onPostExecute(String token) {
            startDelay(token);
        }


        /**
         *
         */

        private String extractTokenFromJsonStream(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                return readTokenMessage(reader);
            } finally {
                reader.close();
            }
        }


        /**
         *
         */

        private String readTokenMessage(JsonReader reader) throws IOException {
            String token = "";

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("token")) {
                    token = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return token;
        }


        /**
         *
         */

        private void startDelay(String token) {
            final String theToken = token;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AsyncGoFetchMessage goFetchMessage = new AsyncGoFetchMessage();
                    goFetchMessage.execute(theToken);
                }
            }, 10000);
        }

    }

    public class AsyncGoFetchMessage extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... imageUris) {

            try {
                return requestImageDescription(imageUris[0]);
            } catch (UnirestException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        private String requestImageDescription(String token) throws UnirestException, IOException {

            // These code snippets use an open-source library. http://unirest.io/java
            String responseUrl = "https://camfind.p.mashape.com/image_responses/" + token;
            HttpResponse<InputStream> response = Unirest.get(responseUrl)
                    .header("X-Mashape-Key", MASHAPE_KEY)
                    .header("Accept", "application/json")
                    .asBinary();

            String description = extractDescriptionFromJsonStream(response.getRawBody());
            return description;

        }

        /**
         * This method always returns immediately, whether or not the
         * image exists. When this applet attempts to draw the image on
         * the screen, the data will be loaded. The graphics primitives
         * that draw the image will incrementally paint on the screen.
         *
         * @param description an absolute URL giving the base location of the image
         */

        protected void onPostExecute(String description) {

            String dogeTalk[] = {"such", "so", "many", "wow", "very"};
            String dogeFiller[] = {"nice", "majesty", "wonder", "photo", "artist"};
            String words[] = description.split(" ");
            int i;
            for (i = 0; i < words.length && i < dogeText.size(); i++) {
                dogeText.get(i).setText(dogeTalk[i] + " " + words[i]);
            }
            while (i < dogeText.size()) {
                dogeText.get(i).setText(dogeTalk[i] + " " + dogeFiller[i]);
                i++;
            }
            progressBar.setVisibility(View.INVISIBLE);
            editMode = true;
        }


        private String extractDescriptionFromJsonStream(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            try {
                DescriptionMessage message = readDescriptionMessage(reader);
                if (message.getStatus().equals("completed")) {
                    return message.toString();
                } else {
                    Log.d("status", message.getStatus() + " " + message.getReason());
                    return "mystery enigma confuse tricky";
                }

            } finally {
                reader.close();
            }

        }

        private DescriptionMessage readDescriptionMessage(JsonReader reader) throws IOException {

            String status = "";
            String name = "";
            String reason = "";

            reader.beginObject();

            while (reader.hasNext()) {
                String field = reader.nextName();
                if (field.equals("status")) {
                    status = reader.nextString();
                } else if (field.equals("name")) {
                    name = reader.nextString();
                } else if (field.equals("reason")) {
                    reason = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();

            return new DescriptionMessage(status, name, reason);
        }
    }
}


