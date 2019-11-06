package com.dxc.hassanalthaf.eventscheduler;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import com.google.zxing.Result;

import java.nio.charset.StandardCharsets;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(getApplicationContext());
        setContentView(scannerView);
    }

    @Override
    public void handleResult(Result result) {
        String cipheredResult = result.getText();
        String decipheredJsonResult = new String(Base64.decode(cipheredResult, Base64.URL_SAFE), StandardCharsets.UTF_8);

        Intent intent = new Intent();
        intent.putExtra("Result", decipheredJsonResult);

        setResult(2, intent);
        onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}
