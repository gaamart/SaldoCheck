package com.applications.guilhermeaugusto.saldocheck;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by guilhermeaugusto on 29/07/2014.
 */
public class ExtendedTextWatcher implements TextWatcher {

    private String current = "";
    private final WeakReference<EditText> editTextWeakReference;

    public ExtendedTextWatcher(EditText editText) {
        editTextWeakReference = new WeakReference<EditText>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals(current)) {
            EditText editText = editTextWeakReference.get();
            editText.removeTextChangedListener(this);
            String cleanString = s.toString().replaceAll("[R$,.]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}
