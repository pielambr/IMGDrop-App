package be.pielambr.imgdrop;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import be.pielambr.imgdrop.resources.Constants;


public class MainActivity extends ActionBarActivity {

    private TextView fileSelected;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fileSelected = (TextView) findViewById(R.id.browse_file);
        // Get the intent
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action)
                && type != null
                && type.startsWith("image/")) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                setImage(imageUri);
            }
        }
        EditText uploadUri = (EditText) findViewById(R.id.txtUpload);
        uploadUri.setText(getUploadUri());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void browse(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PICK_PHOTO);
    }

    public void upload(View view) {
        EditText upload = (EditText) findViewById(R.id.txtUpload);
        setUploadUri(upload.getText().toString());
        if (fileUri != null) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                getImage(fileUri).compress(Bitmap.CompressFormat.PNG, 100, out);
                byte[] myByteArray = out.toByteArray();
                ByteArrayInputStream input = new ByteArrayInputStream(myByteArray);
                RequestParams params = new RequestParams();
                params.put(Constants.UPLOAD_KEY, input, "image.png", "image/png");
                AsyncHttpClient httpClient = new AsyncHttpClient(true, 80, 443);
                httpClient.setUserAgent(Constants.USER_AGENT);
                httpClient.post(this, getFullUploadUri(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                        Log.i("IMGDrop", object.toString());
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String name = response.getString(Constants.IMAGE_KEY);
                            shareImage(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e("IMGDrop", e.getMessage());
            } catch (IOException e) {
                Log.e("IMGDrop", e.getMessage());
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Constants.PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = intent.getData();
                    setImage(imageUri);
                }
                break;
        }
    }

    private void setImage(Uri uri) {
        fileUri = uri;
        fileSelected.setText(uri.toString());
        try {
            Bitmap image = getImage(uri);
            ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail);
            thumbnail.setImageBitmap(image);
            fileSelected.setTextColor(getResources().getColor(R.color.black));
        } catch (IOException e) {
            fileSelected.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private Bitmap getImage(Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    private String getUploadUri() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString(Constants.SETTINGS_UPLOAD, "");
    }

    private void setUploadUri(String uri) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.SETTINGS_UPLOAD, uri);
        editor.commit();
    }

    private String getFullUploadUri() {
        String baseUri = getUploadUri();
        if (baseUri.charAt(baseUri.length() - 1) != '/') {
            baseUri += '/';
        }
        baseUri += Constants.UPLOAD_FILE;
        return baseUri;
    }

    private void shareImage(String name) {
        String baseUri = getUploadUri();
        if (baseUri.charAt(baseUri.length() - 1) != '/') {
            baseUri += '/';
        }
        baseUri += Constants.VIEW_FILE;
        baseUri += name;
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 30);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
        String date = sdf.format(now.getTime());
        final String message = getString(R.string.share_message, baseUri, date);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.share_title))
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                    }
                }).create().show();
    }
}
