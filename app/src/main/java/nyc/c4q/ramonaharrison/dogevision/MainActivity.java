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
    private final String mashape_key = "";
    
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String PHOTO = "photoUri";
    private Uri photoUri;
    private int touchX, touchY, deltaX, deltaY;
    private boolean editMode;
    private FrameLayout.LayoutParams layoutParams;

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
        Typeface comic_sans = Typeface.createFromAsset(getAssets(), "ComicSans.ttf");
        ButterKnife.apply(dogeText, SETFONT, comic_sans);
        editMode = false;
        Picasso.with(this).load(R.drawable.doge).into(dogeView);
        progressBar.setVisibility(View.INVISIBLE);

        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getParcelable(PHOTO);
            if (photoUri != null) {
                Picasso.with(this).load(new File(photoUri.toString())).into(photoView);
                editMode = true;
            }
        }
    }

    @OnClick(R.id.fab)
    public void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create a file to store the image.
            File photoFile = null;
            try {
                photoFile = createImageFile();
                photoUri = (Uri.parse(photoFile.getAbsolutePath()));
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                // Image capture successful
                int i = 0;
                while (i < dogeText.size()) {
                    dogeText.get(i).setText("");
                    i++;
                }
                Picasso.with(this).load(new File(photoUri.toString())).into(photoView);

                //Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Start async task to upload image, retrieve token + message
                AsyncGoFetchToken goFetch = new AsyncGoFetchToken();
                goFetch.execute(photoUri.toString());

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Camera capture cancelled.", Toast.LENGTH_LONG).show();
            } else {
                // Image capture failed, advise user
                Toast.makeText(this, "Camera capture failed.", Toast.LENGTH_LONG).show();
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
            if (editMode) {
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
                Toast.makeText(this, "Such empty canvas", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static final ButterKnife.Setter<TextView, Typeface> SETFONT = new ButterKnife.Setter<TextView, Typeface>() {
        @Override
        public void set(TextView view, Typeface font, int index) {
            view.setTypeface(font);
        }
    };

    public class AsyncGoFetchToken extends AsyncTask<String, Void, String> {

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */

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
                    .header("X-Mashape-Key", mashape_key)
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

        public String extractTokenFromJsonStream(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                return readTokenMessage(reader);
            } finally {
                reader.close();
            }

        }

        public String readTokenMessage(JsonReader reader) throws IOException {
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

            String responseUrl = "https://camfind.p.mashape.com/image_responses/" + token;
            // These code snippets use an open-source library. http://unirest.io/java
            HttpResponse<InputStream> response = Unirest.get(responseUrl)
                    .header("X-Mashape-Key", mashape_key)
                    .header("Accept", "application/json")
                    .asBinary();

            String description = extractDescriptionFromJsonStream(response.getRawBody());
            return description;

        }

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

        public String extractDescriptionFromJsonStream(InputStream in) throws IOException {
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

        public DescriptionMessage readDescriptionMessage(JsonReader reader) throws IOException {
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


