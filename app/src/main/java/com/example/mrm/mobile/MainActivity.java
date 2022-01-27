package com.example.mrm.mobile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrm.mobile.model.Login;
import com.example.mrm.mobile.model.User;
import com.example.mrm.mobile.service.UserClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.gma-admin.com/")
            .addConverterFactory(GsonConverterFactory.create());

    Retrofit retrofit = builder.build();
    UserClient userClient = retrofit.create(UserClient.class);
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doLogin();

        // Floating action button that leads to the camera view for scanning a QR Code
        FloatingActionButton fab = findViewById(R.id.cameraFAB);
        fab.setOnClickListener(view -> {
            // Request camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, CameraActivity.class);
                mCameraViewIntentLauncher.launch(intent);
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // TODO: Improve this UI, inform the user that the camera permission is important and ask again
                mRequestPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                mRequestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Search button for getting data for a machine with ID typed into the main editor
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> {
            // Hide keyboard
            View currentView = this.getCurrentFocus();
            if (currentView != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            // Get the typed in machine code, validate and start backend connection process
            EditText editTextMachineCode = findViewById(R.id.editTextMachineCode);
            String machineCode = editTextMachineCode.getText().toString();
            if (!machineCode.isEmpty() && machineCode.matches("[A-Za-z0-9]+")) {
                getMachineInfoFromBackend(machineCode);
            } else {
                View mainView = findViewById(R.id.mainLayout);
                Snackbar.make(mainView, getResources().getString(R.string.machineCodeTextEditInvalidFormat), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    // Launches an intent to the camera view and waits for the response containing the QR Code scanned
    private final ActivityResultLauncher<Intent> mCameraViewIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String machineCode = data.getStringExtra(CameraActivity.MACHINE_CODE);
                    getMachineInfoFromBackend(machineCode);
                }
            });

    // Prompts the user for the camera permission then either calls the Camera View or inform that without the permission it  can't
    private final ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent intent = new Intent(this, CameraActivity.class);
                    mCameraViewIntentLauncher.launch(intent);
                } else {
                    // TODO: Improve handling when permission is denied, give user a second chance to grant the permission
                    View mainView = findViewById(R.id.mainLayout);
                    Snackbar.make(mainView, getResources().getString(R.string.cameraErrorNoPermission), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            });

    private void doLogin() {
        Login login = new Login("dev", "dev");
        Call<User> call = userClient.login(login);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    launchLoginResultActivity(true, "Logado!");
                    token = response.body().getToken();
                } else {
                    launchLoginResultActivity(false, "Usuário ou senha incorretos.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                launchLoginResultActivity(false, "Erro ao executar login.");
            }
        });
    }

    private void getMachineInfoFromBackend(String machineCode) {
        // Observer to wait for backend task response
        Observer<WorkInfo> observer = workInfo -> {
            if (workInfo.getState().isFinished()) {
                String workerResult = workInfo.getOutputData()
                        .getString(BackendConnectionWorker.WORKER_RESULT);
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    // Send received data from backend to new activity to display
                    launchEquipmentInfoActivity(workerResult);
                } else {
                    String errorMessage = getResources().getString(R.string.errorMessageGettingInfo, machineCode, workerResult);
                    launchOperationResultActivity(false, errorMessage);
                }

                // Response received, hides progress indicators
                hideProgressIndicators();
            }
        };

        // Information for the backend worker
        Data inputData = new Data.Builder()
                .putString(BackendConnectionWorker.ITEM_CODE, machineCode)
                .putString(BackendConnectionWorker.TOKEN, token)
                .putString(BackendConnectionWorker.CONNECTION_TYPE, BackendConnectionTypeEnum.GET_MACHINE_INFO.toString())
                .build();
        launchBackendConnectionWorker(inputData, observer);
    }

    // Launch the equipment info activity waiting with the equipment info
    private void launchEquipmentInfoActivity(String equipmentInfo) {
        Intent intent = new Intent(this, EquipmentInfoActivity.class);
        intent.putExtra(EquipmentInfoActivity.EQUIPMENT_INFO_KEY, equipmentInfo);
        mEquipmentInfoIntentLauncher.launch(intent);
    }

    // Launcher for the equipment info activity that expects the update info to send to the backend
    private final ActivityResultLauncher<Intent> mEquipmentInfoIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String machineCode = data.getStringExtra(EquipmentInfoActivity.MACHINE_CODE);
                    String machineUpdateJSON = data.getStringExtra(EquipmentInfoActivity.MACHINE_UPDATE_INFO);
                    updateMachineInfoOnBackend(machineCode, machineUpdateJSON);
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Intent data = result.getData();
                    if (data != null) {
                        String errorMessage = data.getStringExtra(EquipmentInfoActivity.ERROR_MESSAGE);
                        launchOperationResultActivity(false, errorMessage);
                    }
                }
            });

    // Shows the result screen
    void launchOperationResultActivity(boolean success, String detailedMessage) {
        Intent intent = new Intent(this, OperationResultActivity.class);
        intent.putExtra(OperationResultActivity.RESULT_KEY, success);
        intent.putExtra(OperationResultActivity.MESSAGE_KEY, detailedMessage);
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data.getStringExtra("logged").equals("false")) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    doLogin();
                                }
                            }, 3000);   //5 seconds
                        }
                    }
                }
            });

    void launchLoginResultActivity(boolean success, String detailedMessage) {
        Intent intent = new Intent(this, LoginResultActivity.class);
        intent.putExtra(LoginResultActivity.RESULT_KEY, success);
        intent.putExtra(LoginResultActivity.MESSAGE_KEY, detailedMessage);

        launchSomeActivity.launch(intent);
    }

    private void updateMachineInfoOnBackend(String machineCode, String machineDataJSON) {
        // Observer to wait for backend task response
        Observer<WorkInfo> observer = workInfo -> {
            if (workInfo.getState().isFinished()) {
                String workerResult = workInfo.getOutputData()
                        .getString(BackendConnectionWorker.WORKER_RESULT);

                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String successMessage = getResources().getString(R.string.successMessageUpdatingInfo, machineCode);
                    launchOperationResultActivity(true, successMessage);
                } else {
                    String errorMessage = getResources().getString(R.string.errorMessageUpdatingInfo, machineCode, workerResult);
                    launchOperationResultActivity(false, errorMessage);
                }

                // Response received, hides progress indicators
                hideProgressIndicators();
            }
        };

        // Information for the backend worker
        Data inputData = new Data.Builder()
                .putString(BackendConnectionWorker.ITEM_CODE, machineCode)
                .putString(BackendConnectionWorker.TOKEN, token)
                .putString(BackendConnectionWorker.ITEM_INFO, machineDataJSON)
                .putString(BackendConnectionWorker.CONNECTION_TYPE, BackendConnectionTypeEnum.UPDATE_MACHINE_INFO.toString())
                .build();
        launchBackendConnectionWorker(inputData, observer);
    }

    // Launches a background task to connect to the backend and changes visibility of progress bar
    private void launchBackendConnectionWorker(Data inputData, Observer<WorkInfo> observer) {
        // Show progress bar indicating that a connection to the backend is in progress
        showProgressIndicators();

        WorkRequest backendConnectionRequest = new OneTimeWorkRequest
                .Builder(BackendConnectionWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(backendConnectionRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(backendConnectionRequest.getId())
                .observe(this, observer);
    }

    // Shows the progress indicators on the Main Activity that are hidden by default
    private void showProgressIndicators() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.progressText);
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    // Hides the progress indicators after a response was received from the backend
    private void hideProgressIndicators() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.progressText);
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }
}
