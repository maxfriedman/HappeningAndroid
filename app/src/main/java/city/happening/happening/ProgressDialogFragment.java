package city.happening.happening;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

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
    public View loadingAdapter(Context c){
        View view = new View(c);
        view.setBackground(getResources().getDrawable(R.drawable.customprogress));

        return view;
    }
}