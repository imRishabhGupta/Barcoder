package com.rishabh.barcoder;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    private CameraSourcePreview surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private GraphicOverlay graphicOverlay;
    private TextView barcodeInfo;
    private String data ="";
    private Barcode barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_acitivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);


        graphicOverlay=(GraphicOverlay)findViewById(R.id.overlay);

        surfaceView=(CameraSourcePreview) findViewById(R.id.surface_view);
        barcodeInfo=(TextView)findViewById(R.id.barcode_info);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();

        final BarcodeGraphic graphic = new BarcodeGraphic(graphicOverlay);

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .build();

        startCameraSource();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {
                        public void run() {

                            barcode = barcodes.valueAt(0);
                            graphic.updateItem(barcode);
                            graphicOverlay.add(graphic);
                            data = barcodes.valueAt(0).rawValue;
                            System.out.println(data);
                            barcodeInfo.setText(data);
                        }
                    });
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code,9001);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                surfaceView.start(cameraSource, graphicOverlay);

            } catch (IOException e) {
                Log.e("kk", "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    public void share(View view){
        if(data.equals("")){
            Toast.makeText(getApplicationContext(),"No data to send.",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hey, I just used Barcoder to decode a barcode and here is the extracted data");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void copy(View view){
        if(data.equals(""))
            Toast.makeText(getApplicationContext(), "No text to copy to clipboard.",Toast.LENGTH_SHORT).show();
        ClipboardManager manager =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setText(data);
        Toast.makeText(getApplicationContext(), "Text in clipboard",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        data="";
        barcodeInfo.setText(data);
        graphicOverlay.clear();
        startCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            if (cameraSource != null) {
              cameraSource.release();
        }
    }

}
