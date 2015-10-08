package city.happening.happening;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Alex on 10/6/2015.
 */

public class ProgressDialogFragment extends DialogFragment {
    private String mMessage;
    public ProgressDialogFragment newInstance(String s) {
        mMessage = s;
        return new ProgressDialogFragment();
    }
    // Build ProgressDialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create new ProgressDialog
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        // Set Dialog message
        dialog.setMessage(mMessage);
        // Dialog will be displayed for an unknown amount of time
        dialog.setIndeterminate(true);

        return dialog;
    }
}