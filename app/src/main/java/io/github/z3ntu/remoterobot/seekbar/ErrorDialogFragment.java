package io.github.z3ntu.remoterobot.seekbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Luca on 23.05.2015.
 */
public class ErrorDialogFragment extends DialogFragment {


    private String text = "EMPTY";

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(text).setTitle("Error!");
        builder.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ErrorDialogFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    public ErrorDialogFragment setText(String text) {
        this.text = text;
        return this;
    }
}
