package be.pielambr.imgdrop;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
            if(imageUri != null) {
                fileUri = imageUri;
                fileSelected.setText(imageUri.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode){
            case Constants.PICK_PHOTO:
                if(resultCode == RESULT_OK) {
                    Uri imageUri = intent.getData();
                    fileUri = imageUri;
                    fileSelected.setText(imageUri.toString());
                }
                break;
        }
    }
}
