package com.example.mrm.mobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class RegisterMachineEventDialogFragment extends DialogFragment {
    public static String TAG = "register_machine_event_dialog";

    // Interface for allowing calle activity to process the events
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Instance of the interface to deliver the events
    NoticeDialogListener listener;

    // Toolbar instance to set click events
    private Toolbar toolbar;

    // Indicators of selected options
    MachineEventsEnum SelectedEventOption = MachineEventsEnum.NONE;
    boolean MaintenanceFlagChecked = false;
    String Comment;

    public static void display(FragmentManager fragmentManager) {
        RegisterMachineEventDialogFragment registerEventDialog = new RegisterMachineEventDialogFragment();
        registerEventDialog.show(fragmentManager, TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.register_event_dialog, container, false);

        toolbar = rootView.findViewById(R.id.toolbar);

        // Set radio button events to save last selected position
        RadioButton radioEventInventory = rootView.findViewById(R.id.radioEventInventory);
        radioEventInventory.setOnClickListener(view -> SelectedEventOption = MachineEventsEnum.INVENTORY);
        RadioButton radioEventCustomer = rootView.findViewById(R.id.radioEventCustomer);
        radioEventCustomer.setOnClickListener(view -> SelectedEventOption = MachineEventsEnum.CUSTOMER);
        RadioButton radioEventMaintenance = rootView.findViewById(R.id.radioEventMaintenance);
        radioEventMaintenance.setOnClickListener(view -> SelectedEventOption = MachineEventsEnum.MAINTENANCE);
        RadioButton radioEventReadyToRent = rootView.findViewById(R.id.radioEventReadyToRent);
        radioEventReadyToRent.setOnClickListener(view -> SelectedEventOption = MachineEventsEnum.READY_FOR_RENTAL);

        // Set checkbox event to save state
        CheckBox needsMaintenanceCheckBox = rootView.findViewById(R.id.checkBoxEventFlagNeedsMaintenance);
        needsMaintenanceCheckBox.setOnClickListener(view -> MaintenanceFlagChecked = ((CheckBox) view).isChecked());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> {
            listener.onDialogNegativeClick(RegisterMachineEventDialogFragment.this);
            dismiss();
        });
        toolbar.setTitle(getResources().getString(R.string.registerEventDialogTitle));
        toolbar.inflateMenu(R.menu.register_event_dialog);
        toolbar.setOnMenuItemClickListener(item -> {
            // Save final state of the comment
            EditText commentEditText = view.findViewById(R.id.editTextEventComment);
            Comment = commentEditText.getText().toString();
            listener.onDialogPositiveClick(RegisterMachineEventDialogFragment.this);
            dismiss();
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NoticeDialogListener");
        }
    }
}
