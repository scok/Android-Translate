package com.example.translation.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import com.example.translation.R;

public class ProgressDialog extends Dialog {
    public ProgressDialog(Context context)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);
    }
}
