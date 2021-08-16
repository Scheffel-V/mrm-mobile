package com.example.mrm.mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.StringWriter;

public class EquipmentInfoActivity extends AppCompatActivity
        implements RegisterMachineEventDialogFragment.NoticeDialogListener {
    public static String TAG = "equipment_info_activity";

    public static final String MACHINE_CODE = "machine_code";
    public static final String MACHINE_UPDATE_INFO = "machine_update_info";
    public static final String EQUIPMENT_INFO_KEY = "equipment_info";
    public static final String ERROR_MESSAGE = "error_message";

    private StockItem mStockItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_info);

        // Gets the input data
        String equipmentInfo = getIntent().getStringExtra(EQUIPMENT_INFO_KEY);

        // Parse the equipment info into its own class
        String fallbackString = getResources().getString(R.string.stockItemFallbackString);
        mStockItem = new StockItem(equipmentInfo, fallbackString);

        // TODO: Show the image on the layout

        // Prepares the output
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(getResources().getString(R.string.machineCode)).append(": ").append(mStockItem.infoMap.get(StockItemFields.code)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineName)).append(": ").append(mStockItem.infoMap.get(StockItemFields.name)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineType)).append(": ").append(mStockItem.infoMap.get(StockItemFields.type)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineBrand)).append(": ").append(mStockItem.infoMap.get(StockItemFields.brand)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineModel)).append(": ").append(mStockItem.infoMap.get(StockItemFields.model)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machinePower)).append(": ").append(mStockItem.infoMap.get(StockItemFields.power)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineVoltage)).append(": ").append(mStockItem.infoMap.get(StockItemFields.voltage)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machinePressure)).append(": ").append(mStockItem.infoMap.get(StockItemFields.pressure)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineThroughput)).append(": ").append(mStockItem.infoMap.get(StockItemFields.throughput)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineYear)).append(": ").append(mStockItem.infoMap.get(StockItemFields.year)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineSerialNumber)).append(": ").append(mStockItem.infoMap.get(StockItemFields.serialNumber)).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineStatus)).append(": ").append(statusToReadableText(mStockItem.infoMap.get(StockItemFields.status))).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineMaintenanceNeeded)).append("? ").append(booleanToReadableText(mStockItem.infoMap.get(StockItemFields.needsMaintenance))).append("\n");
        outputBuilder.append(getResources().getString(R.string.machineComment)).append(": ").append(mStockItem.infoMap.get(StockItemFields.comment)).append("\n");

        // Writes it to the main textview
        TextView infoTextView = findViewById(R.id.infoTextView);
        infoTextView.setText(outputBuilder);

        // Button for registering an event that changes the machine status, like arrival or departure
        FloatingActionButton fab = findViewById(R.id.registerEventFAB);
        fab.setOnClickListener(view -> {
            // Show dialog in full screen
            RegisterMachineEventDialogFragment.display(getSupportFragmentManager());
        });
    }

    // Converts the status values from the backend to a readable, translated string
    private String statusToReadableText(String status) {
        if (status == null) {
            // TODO: Improve error handling
            return "";
        }

        switch (status) {
            case "INVENTORY":
                return getResources().getString(R.string.machineStatusInventory);
            case "MAINTENANCE":
                return getResources().getString(R.string.machineStatusMaintenance);
            case "READY_FOR_RENTAL":
                return getResources().getString(R.string.machineStatusReadyForRental);
            case "CUSTOMER":
                return getResources().getString(R.string.machineStatusCustomer);
            default:
                // TODO: Improve error handling
                return "";
        }
    }

    // Converts the boolean to a translated string
    private String booleanToReadableText(String booleanValue) {
        if (booleanValue == null) {
            // TODO: Improve error handling
            return "";
        }

        switch (booleanValue) {
            case "true":
                return getResources().getString(R.string.True);
            case "false":
                return getResources().getString(R.string.False);
            default:
                // TODO: Improve error handling
                return "";
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Get user input
        RegisterMachineEventDialogFragment dialogFragment = (RegisterMachineEventDialogFragment) dialog;
        MachineEventsEnum selectedOption = dialogFragment.SelectedEventOption;
        boolean flag = dialogFragment.MaintenanceFlagChecked;
        String comment = dialogFragment.Comment;

        // Validate that an event was chosen
        if (selectedOption == MachineEventsEnum.NONE) {
            Intent data = new Intent();
            data.putExtra(ERROR_MESSAGE, getResources().getString(R.string.machineEventErrorNoEventSelected, mStockItem.getMachineCode()));
            setResult(RESULT_CANCELED, data);
            finish();
        }

        // Generate update JSON
        // TODO: Handle error building the JSON object
        String updateJSON = buildUpdateJSON(selectedOption, flag, comment);

        // Send back to the main activity to connect to the backend and show the progress
        Intent data = new Intent();
        data.putExtra(MACHINE_CODE, mStockItem.getMachineCode());
        data.putExtra(MACHINE_UPDATE_INFO, updateJSON);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {}

    private String buildUpdateJSON(MachineEventsEnum event, boolean needsMaintenance, String comment) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        try {
            jsonWriter.beginObject();

            jsonWriter.name(StockItemFields.status.toString()).value(event.toString());
            jsonWriter.name(StockItemFields.needsMaintenance.toString()).value(needsMaintenance);
            jsonWriter.name(StockItemFields.comment.toString()).value(comment);

            jsonWriter.endObject();
        } catch (Exception e) {
            Log.d(TAG, "buildUpdateJSON: error building JSON: " + e.getMessage());
            return "";
        }

        // Return result
        String updateJSON = stringWriter.toString();

        try {
            stringWriter.close();
            jsonWriter.close();
        } catch (Exception e) {
            Log.d(TAG, "buildUpdateJSON: error closing writers: " + e.getMessage());
        }

        return updateJSON;
    }
}
