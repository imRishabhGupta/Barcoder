package com.rishabh.barcoder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateCode extends AppCompatActivity {

    private ImageView imageView;
    private BarcodeFormat format=BarcodeFormat.QR_CODE;
    private ProgressBar progressBar;
    private ImageButton save,share;
    private final static String APP_TITLE = "Barcoder";// App Name
    private final static String APP_PNAME = "com.rishabh.barcoder";// Package Name
    private Bitmap bitmap ;
    private final static int WIDTH=500;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_code);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        imageView=(ImageView)findViewById(R.id.barcode);
        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        save=(ImageButton)findViewById(R.id.save);
        share=(ImageButton)findViewById(R.id.share);
        save.setVisibility(View.GONE);
        share.setVisibility(View.GONE);
        bitmap=null;

        Spinner spinner=(Spinner)findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.barcode_formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:format=BarcodeFormat.QR_CODE;
                        break;
                    case 1:format=BarcodeFormat.CODABAR;
                        break;
                    case 2:format=BarcodeFormat.AZTEC;
                        break;
                    case 3:format=BarcodeFormat.CODE_39;
                        break;
                    case 4:format=BarcodeFormat.CODE_93;
                        break;
                    case 5:format=BarcodeFormat.CODE_128;
                        break;
                    case 6:format=BarcodeFormat.DATA_MATRIX;
                        break;
                    case 7:format=BarcodeFormat.EAN_8;
                        break;
                    case 8:format=BarcodeFormat.EAN_13;
                        break;
                    case 9:format=BarcodeFormat.ITF;
                        break;
                    case 10:format=BarcodeFormat.MAXICODE;
                        break;
                    case 11:format=BarcodeFormat.PDF_417;
                        break;
                    case 12:format=BarcodeFormat.RSS_14;
                        break;
                    case 13:format=BarcodeFormat.RSS_EXPANDED;
                        break;
                    case 14:format=BarcodeFormat.UPC_A;
                        break;
                    case 15:format=BarcodeFormat.UPC_E;
                        break;
                    case 16:format=BarcodeFormat.UPC_EAN_EXTENSION;
                        break;
                    case 17:format=BarcodeFormat.QR_CODE;
                        break;
                    default:format=BarcodeFormat.QR_CODE;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                format=BarcodeFormat.QR_CODE;
            }
        });

        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId("ca-app-pub-5792178194954929/9168211899");

        AdRequest adRequest1 = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest1);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {

            }
            @Override
            public void onAdClosed() {
                NavUtils.navigateUpFromSameTask(CreateCode.this);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void create(View v){
        save.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        share.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    EditText info=(EditText)findViewById(R.id.info);
                    String string=info.getText().toString();
                    bitmap = encodeAsBitmap(string);
                    synchronized (this) {
                        wait(5000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    imageView.setImageBitmap(bitmap);
                                    imageView.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    save.setVisibility(View.VISIBLE);
                                    share.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),"Sorry could not produce.",Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    System.out.println("2");
                                    e.printStackTrace();
                                }
                            } // end of run method
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("3");
                }
            }
        });
        t.start();
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,format, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            System.out.println("1");
            Toast.makeText(getApplicationContext(),"Sorry wrong format with wrong input.",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);

            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 13:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                    return;
                } else {
                    Toast.makeText(getApplicationContext(),"Grant permission to save image of code.",Toast.LENGTH_SHORT).show();
                }
                break;
            case 14:if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                         share();
                        return;
            } else {
                Toast.makeText(getApplicationContext(),"Need storage permission to share image of code.",Toast.LENGTH_SHORT).show();
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void saveImage(){
        String location="/sdcard/Barcoder";
        File file=new File(location);
        if(!file.exists()) {
            File wallpaperDirectory = new File(location);
            wallpaperDirectory.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        String mImageName="Code_"+ timeStamp +".jpeg";
        File file1 = new File(new File(location), mImageName);
        try {
            FileOutputStream out = new FileOutputStream(file1);
            Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(getApplicationContext(),"Timetable saved at "+location,Toast.LENGTH_LONG).show();
            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext()," Error ocurred while downloading! ",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void share(){

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    public void shareImage(View view){

        if (Build.VERSION.SDK_INT>= 23)
            if (ContextCompat.checkSelfPermission(CreateCode.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateCode.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},14);
            }
            else share();
        else share();

    }

    public void storeImage(View view) {
        if (Build.VERSION.SDK_INT>= 23)
            if (ContextCompat.checkSelfPermission(CreateCode.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateCode.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
            }
            else saveImage();
        else saveImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

       if(id==R.id.action_share){
            try
            {Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, APP_TITLE);
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
            Intent intent=new Intent(getApplicationContext(),AboutUs.class);
            startActivity(intent);
        }

        else if(id==android.R.id.home){
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()&&bitmap!=null) {
                    mInterstitialAd.show();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()&&bitmap!=null) {
            mInterstitialAd.show();
        }
        super.onBackPressed();
    }
}
