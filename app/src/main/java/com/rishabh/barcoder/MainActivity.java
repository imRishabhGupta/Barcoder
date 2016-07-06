package com.rishabh.barcoder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
public class MainActivity extends AppCompatActivity {

    private BarcodeDetector barcodeDetector;
    private String string="";
    private static final int RESULT_LOAD_IMG=111;
    private final static String APP_TITLE = "Barcoder";// App Name
    private final static String APP_PNAME = "com.rishabh.barcoder";// Package Name
    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 10;//Min number of launches
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                { Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, APP_TITLE);
                    String sAux = "\nHey, I used Barcoder and loved it. Let me recommend you this application. Here is the link- \n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id="+APP_PNAME+" \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Share via"));
                }
                catch(Exception e)
                { //e.toString();
                }
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9BB67E6B330CB5844FD87C9FC5300131")
                .build();
        mAdView.loadAd(adRequest);

        app_launched();


        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();

    }

    private void startCameraSource() {
        Intent intent=new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Data extracted is");
        builder.setMessage(string);

        builder.setNeutralButton("Copy to clipboard",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager manager =
                                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        manager.setText(string);
                        Toast.makeText(getApplicationContext(), "Text in clipboard",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton("Share",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, APP_TITLE);
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, string);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        dialog.dismiss();
                    }
                });
        String negativeText = "Cancel";
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void scan(View v){

        if (Build.VERSION.SDK_INT>= 23)
            if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},11);
            }
            else
                startCameraSource();
        else
            startCameraSource();

    }

    public void pick(View view){
        if (Build.VERSION.SDK_INT>= 23)
            if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},12);
            }
            else{
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        else{
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }
    }

    public void createCode(View view){
        Intent i=new Intent(MainActivity.this,CreateCode.class);
        startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmap=BitmapFactory.decodeFile(imgDecodableString);
                Frame myFrame = new Frame.Builder()
                        .setBitmap(bitmap)
                        .build();
                SparseArray<Barcode> barcodes = barcodeDetector.detect(myFrame);
                if(barcodes.size() != 0) {
                    string=barcodes.valueAt(0).rawValue;
                    showDialog();
                }
                else
                Toast.makeText(this, "Couldn't make out of image.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You haven't picked an Image",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 11:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraSource();
                    return;
                } else {
                    Toast.makeText(getApplicationContext(),"Permission not granted.",Toast.LENGTH_SHORT).show();
                }
                break;
            case 12:if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            } else {
                Toast.makeText(getApplicationContext(),"Permission not granted.",Toast.LENGTH_SHORT).show();
                return;
            }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        int id = item.getItemId();

        if(id==R.id.action_share){
            try
            { Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Barcoder");
                String sAux = "\nHey, I used Barcoder and loved it. Let me recommend this application. Here is the link- \n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id="+APP_PNAME+" \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Share via"));
            }
            catch(Exception e)
            { //e.toString();
            }
        }
        else if(id==R.id.action_rate){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        }
        else if(id==R.id.action_about){
            Intent intent=new Intent(MainActivity.this,AboutUs.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void app_launched() {
        SharedPreferences prefs = getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("neverask", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +(DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                editor.putLong("launch_count", 7);
                editor.putLong("date_firstlaunch",System.currentTimeMillis()-  24 * 60 * 60 * 1000);
                showRateDialog(editor);
            }
        }

        editor.apply();
    }

    private void showRateDialog(final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Rate "+APP_TITLE);
        builder.setMessage("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        builder.setPositiveButton("Rate",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editor != null) {
                            editor.putBoolean("neverask", true);
                            editor.commit();
                        }
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                        dialog.dismiss();
                    }
                });

        builder.setNeutralButton("Later",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("Never",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editor != null) {
                            editor.putBoolean("neverask", true);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

}
