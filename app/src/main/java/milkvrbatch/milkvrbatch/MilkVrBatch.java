package milkvrbatch.milkvrbatch;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

public class MilkVrBatch extends AppCompatActivity {

    String dlnaPort;
    String webFolder;
    String finalMsg;
    boolean useThumbnails;
    boolean useAudio;
    boolean useHttp;
    EditText dlnaPortTextBox;
    CheckBox thumbnailsCheckbox;
    CheckBox audioCheckbox;
    RadioButton httpRadioButton;
    RadioButton jpgRadioButton;
    EditText addressTextBox;
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getApplicationContext();
        final Handler mHandler= new Handler();
        super.onCreate(savedInstanceState);
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_milk_vr_batch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dlnaPortTextBox = (EditText) findViewById(R.id.dlnaPortText);
        addressTextBox = (EditText) findViewById(R.id.addressText);
        thumbnailsCheckbox = (CheckBox) findViewById(R.id.thumbnails);
        audioCheckbox = (CheckBox) findViewById(R.id.audioTags);
        httpRadioButton = (RadioButton) findViewById(R.id.httpRadioButton);
        jpgRadioButton = (RadioButton) findViewById(R.id.jpgRadioButton);

        if (dlnaPort != null) {
            dlnaPortTextBox.setText(dlnaPort);}

        if (webFolder != null) {
            addressTextBox.setText(webFolder);}

        thumbnailsCheckbox.setChecked(useThumbnails);
        httpRadioButton.setChecked(useHttp);
        jpgRadioButton.setChecked(!useHttp);
        httpRadioButton.setEnabled(useThumbnails);
        jpgRadioButton.setEnabled(useThumbnails);

        Button generate = (Button) findViewById(R.id.generate);
        if (generate != null) {
            generate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dlnaPortTextBox.getText() != null) {
                        dlnaPort = dlnaPortTextBox.getText().toString().trim();
                    }
                    if (addressTextBox.getText() != null) {
                        webFolder = addressTextBox.getText().toString().trim();
                    }

                    useThumbnails = thumbnailsCheckbox.isChecked();
                    useAudio = audioCheckbox.isChecked();
                    useHttp = httpRadioButton.isChecked();

                    SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    String dlnaTag = gson.toJson(dlnaPort);
                    String webTag = gson.toJson(webFolder);
                    prefsEditor.putString("dlnaPort", dlnaTag);
                    prefsEditor.putString("webFolder", webTag);
                    prefsEditor.putBoolean("useThumbnails", useThumbnails);
                    prefsEditor.putBoolean("useAudio", useAudio);
                    prefsEditor.putBoolean("useHttp", useHttp);
                    prefsEditor.commit();

                    final ProgressDialog pd = new ProgressDialog(MilkVrBatch.this);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setMessage("Creating .mvrl...");
                    pd.setIndeterminate(true);
                    pd.setCancelable(false);
                    pd.show();
                    Thread mThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Utility utility = new Utility(dlnaPort, webFolder, useThumbnails, useAudio, useHttp);
                                finalMsg = utility.run();
                                pd.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        MilkVrBatch.this.createDialogMsg(finalMsg);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mThread.start();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_milk_vr_batch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Exit) {
            finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRadioButtonClicked(View view) {
        RadioButton button = (RadioButton) view;
        boolean checked = button.isChecked();

        switch(view.getId()) {
            case R.id.httpRadioButton:
                if (checked)
                {
                    httpRadioButton.setChecked(true);
                    jpgRadioButton.setChecked(false);
                }
                    break;
            case R.id.jpgRadioButton:
                if (checked)
                {
                    httpRadioButton.setChecked(false);
                    jpgRadioButton.setChecked(true);
                }
                    break;
        }
    }

    public void thumbnailClicked(View view) {
        boolean checked = thumbnailsCheckbox.isChecked();
                if (checked)
                {
                    httpRadioButton.setEnabled(true);
                    jpgRadioButton.setEnabled(true);
                }
               else{
                    httpRadioButton.setEnabled(false);
                    jpgRadioButton.setEnabled(false);
                }
    }

    public void loadProperties() throws IOException, SecurityException {
        SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String dlnaTag = mPrefs.getString("dlnaPort", "");
        String webTag = mPrefs.getString("webFolder", "");
        dlnaPort = gson.fromJson(dlnaTag, String.class);
        webFolder = gson.fromJson(webTag, String.class);
        useThumbnails = mPrefs.getBoolean("useThumbnails", true);
        useAudio = mPrefs.getBoolean("useAudio", true);
        useHttp = mPrefs.getBoolean("useHttp", true);

        if (dlnaPort == null || dlnaPort.trim().equals("") ) {dlnaPort = "5001";} else{dlnaPort.trim();}
        if (webFolder == null || webFolder.trim().equals("")) {webFolder = "http://192.168.1.100:9001/browse/55";}else{webFolder.trim();}
    }

    public void createDialogMsg(String msg) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(MilkVrBatch.this);
        builder1.setMessage(msg);
        builder1.setCancelable(true);

        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alert = builder1.create();
        alert.show();
    }

}
