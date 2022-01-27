package com.example.mrm.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginResultActivity extends AppCompatActivity {
    public static final String RESULT_KEY = "result";
    public static final String MESSAGE_KEY = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_result);
        Intent resultIntent = new Intent();

        // Gets the input data
        boolean result = getIntent().getBooleanExtra(RESULT_KEY, false);
        String detailsMessage = getIntent().getStringExtra(MESSAGE_KEY);

        ImageView indicatorImage = findViewById(R.id.indicatorIcon);
        TextView indicatorText = findViewById(R.id.indicatorText);
        TextView indicatorDetails = findViewById(R.id.indicatorDetails);

        if (result) {
            // Set success image and text
            indicatorImage.setImageResource(R.drawable.ic_success);
            indicatorText.setText(getResources().getString(R.string.operationResultTextSuccess));
            resultIntent.putExtra("logged", "true");
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            // Set failure image and text
            indicatorImage.setImageResource(R.drawable.ic_failure);
            indicatorText.setText(getResources().getString(R.string.operationResultTextFailure));
            resultIntent.putExtra("logged", "false");
            setResult(Activity.RESULT_OK, resultIntent);
        }

        // Sets detail message
        indicatorDetails.setText(detailsMessage);

        // Sets the button click to finish this activity
        Button operationResultAcknowledgeButton = findViewById(R.id.operationResultAcknowledgeButton);
        operationResultAcknowledgeButton.setOnClickListener(view -> finish());
    }
}
