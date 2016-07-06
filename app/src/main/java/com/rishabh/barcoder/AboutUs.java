package com.rishabh.barcoder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AboutUs extends AppCompatActivity implements View.OnClickListener{

    private final static String APP_PNAME = "com.rishabh.barcoder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.imageButton){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://twitter.com/imRishabhGupta"));
            startActivity(i);
        }
        else if(v.getId()==R.id.imageButton2){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://in.linkedin.com/in/rishabh-gupta-8b9626a0"));
            startActivity(i);
        }

        else if(v.getId()==R.id.imageButton6){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://github.com/imRishabhGupta"));
            startActivity(i);
        }
        else if(v.getId()==R.id.share){
            try
            { Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Barcoder");
                String sAux = "\nHey, I used Barcoder and loved it. Let me recommend you this application. Here is the link- \n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id="+APP_PNAME+" \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Share via"));
            }
            catch(Exception e)
            { //e.toString();
            }
        }
        else if(v.getId()==R.id.rate){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        }
    }
}
