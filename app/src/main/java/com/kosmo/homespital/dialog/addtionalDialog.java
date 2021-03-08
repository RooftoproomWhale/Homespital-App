package com.kosmo.homespital.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kosmo.homespital.R;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class addtionalDialog extends AlertDialog {

    private Context context;
    private TextView effectDialog,usageDialog,carefulDialog;

    public addtionalDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    protected addtionalDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected addtionalDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicine_detail_dialog);

        effectDialog = findViewById(R.id.effectDialog);
        usageDialog = findViewById(R.id.usageDialog);
        carefulDialog = findViewById(R.id.carefulDialog);
    }

    public String getEffect() {
        return effectDialog.getText().toString();
    }

    public void setEffect(String text) {
        effectDialog.setText(Html.fromHtml(text,FROM_HTML_MODE_LEGACY));
    }

    public String getUsage() {
        return usageDialog.getText().toString();
    }

    public void setUsage(String text) {
        usageDialog.setText(Html.fromHtml(text,FROM_HTML_MODE_LEGACY));
    }

    public String getCareful() {
        return carefulDialog.getText().toString();
    }

    public void setCareful(String text) {
        carefulDialog.setText(Html.fromHtml(text,FROM_HTML_MODE_LEGACY));
    }
}
