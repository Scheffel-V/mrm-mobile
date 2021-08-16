package com.example.mrm.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.snackbar.Snackbar;

public class CameraActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    public static String MACHINE_CODE = "machineCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Setup QR Code scanner
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            Intent data = new Intent();
            data.putExtra(MACHINE_CODE, result.getText());
            setResult(RESULT_OK, data);
            finish();
        }));

        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
