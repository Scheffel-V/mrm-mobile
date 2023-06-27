package com.example.mrm.mobile;

import android.app.Activity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrm.mobile.adapter.CommentHistoryAdapter;
import com.example.mrm.mobile.model.CommentHistory;

import java.util.ArrayList;
import java.util.List;

public class CommentHistoryDialogFragment extends DialogFragment {
    public static String TAG = "see_comment_history_dialog";
    RecyclerView recyclerView;
    CommentHistoryAdapter adapter;
    List<CommentHistory> commentHistoryList = new ArrayList<>();


    // Interface for allowing calle activity to process the events
    public interface NoticeDialogListener {
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
        CommentHistoryDialogFragment registerEventDialog = new CommentHistoryDialogFragment();
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
        View rootView = inflater.inflate(R.layout.comment_history_dialog, container, false);

        toolbar = rootView.findViewById(R.id.toolbar);
        this.commentHistoryList = ((EquipmentInfoActivity)getActivity()).getComments();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> {
            listener.onDialogNegativeClick(CommentHistoryDialogFragment.this);
            dismiss();
        });
        toolbar.setTitle(getResources().getString(R.string.commentHistoryDialogTitle));

        recyclerView = view.findViewById(R.id.recycler_view);
        setRecyclerView(view);
    }

    private void setRecyclerView(View view) {
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.adapter = new CommentHistoryAdapter(view.getContext(), getList());
        this.recyclerView.setAdapter(adapter);
    }

    private List<CommentHistory> getList() {
        return this.commentHistoryList;
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
