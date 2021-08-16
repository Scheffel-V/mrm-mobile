package com.example.mrm.mobile;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackendConnectionWorker extends Worker {
    public static String TAG = "backend_connection_worker";

    public static final String ITEM_CODE = "code";
    public static final String ITEM_INFO = "info";
    public static final String CONNECTION_TYPE = "type";
    public static final String WORKER_RESULT = "result";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String BACKEND_ERROR_MESSAGE_KEY = "message";
    private static final String ERROR_MESSAGE_FALLBACK = "unavailable info";

    public BackendConnectionWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();

        // Gets the machine code
        String machineCode = inputData.getString(ITEM_CODE);
        if (machineCode == null) {
            return Result.failure();
        }

        // Checks which connection to make to the backend
        String connectionType = inputData.getString(CONNECTION_TYPE);
        if (connectionType == null) {
            return Result.failure();
        }

        // TODO: Update references to backend endpoint
        String backendEndpoint = "http://10.0.2.2:3134/api/stockItems/code/" + machineCode;

        if (connectionType.equals(BackendConnectionTypeEnum.GET_MACHINE_INFO.toString())) {
            Request request = new Request.Builder()
                    .url(backendEndpoint)
                    .build();

            return executeRequest(request);
        }

        if (connectionType.equals(BackendConnectionTypeEnum.UPDATE_MACHINE_INFO.toString())) {
            // Gets the updated machine info
            String updatedInfo = inputData.getString(ITEM_INFO);
            if (updatedInfo == null) {
                return Result.failure();
            }

            RequestBody body = RequestBody.create(updatedInfo, JSON);

            Request request = new Request.Builder()
                    .url(backendEndpoint)
                    .put(body)
                    .build();

            return executeRequest(request);
        }

        // Invalid connection type
        return Result.failure();
    }

    private Result executeRequest(Request request) {
        // Connect to the backend asking for the data of the machine with the id read in the qr code
        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                int errorCode = response.code();
                String backendResponse = Objects.requireNonNull(response.body()).string();
                if (errorCode < 400) {
                    Data connectionResponse = new Data.Builder()
                            .putString(WORKER_RESULT, backendResponse)
                            .build();
                    return Result.success(connectionResponse);
                } else {
                    // TODO: Improve error handling
                    Data errorResponse = new Data.Builder()
                            .putString(WORKER_RESULT, getErrorMessageFromResponse(backendResponse))
                            .build();
                    return Result.failure(errorResponse);
                }
            } else {
                // TODO: Improve error handling
                Data errorResponse = new Data.Builder()
                        .putString(WORKER_RESULT, "generic error connecting to the backend")
                        .build();
                return Result.failure(errorResponse);
            }
        } catch (Exception e) {
            // TODO: Improve error handling
            Data errorResponse = new Data.Builder()
                    .putString(WORKER_RESULT, e.getMessage())
                    .build();
            return Result.failure(errorResponse);
        }
    }

    private String getErrorMessageFromResponse(String backendResponse) {
        String errorMessage = ERROR_MESSAGE_FALLBACK;

        try {
            JSONObject responseJSON = new JSONObject(backendResponse);
            errorMessage = responseJSON.optString(BACKEND_ERROR_MESSAGE_KEY, ERROR_MESSAGE_FALLBACK);
        } catch (Exception e) {
            // TODO: Improve error handling
            Log.d(TAG, "Error parsing backend error: " + e.getMessage());
        }

        return errorMessage;
    }
}
