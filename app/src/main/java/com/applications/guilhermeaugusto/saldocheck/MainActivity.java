package com.applications.guilhermeaugusto.saldocheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.AdRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends ActionBarActivity{

    private enum OperationType { Subtract, Add, New, Visualize  }

    private BigDecimal saldoValue;
    private BigDecimal over1Billion;
    private BigDecimal below100millions;
    private BigDecimal changeValue;
    private OperationType operationType;
    private boolean keyboardOpen;
    private ExtendedTextWatcher extendedTextWatcher;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAllGlobalVariables();
        loadSaldo();
        updateSaldo();
        createAdBanner();
    }

    @Override
    protected void onPause(){
        super.onPause();
        showEditTextComponents(false);
    }

    @Override
    public void onBackPressed(){
        if(!keyboardOpen) super.onBackPressed();
        else keyboardOpen = false;
    }

    private void startAllGlobalVariables(){
        ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        over1Billion = new BigDecimal("1000000000");
        below100millions = new BigDecimal("-100000000");
        changeValue = BigDecimal.ZERO;
        operationType = OperationType.Visualize;
        extendedTextWatcher = new ExtendedTextWatcher(editText);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboardOpen = false;
        setOperationText();
    }

    private void createAdBanner(){
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("2C867BB5A2F2C60A3BACB3C0302ECC8B")
                //.addTestDevice("C32AD3AA2C5D97BFD40ADB6FA9536FC0")
                //.addTestDevice("F85FBF4907189CF6E54D8FBAFF1499E3")
                .build();
        adView.loadAd(adRequest);
    }

    public void buttonSaqueOnClick(View v){
        operationType = OperationType.Subtract;
        manageEditText();
    }

    public void buttonDepositoOnClick(View v){
        operationType = OperationType.Add;
        manageEditText();
    }

    public void buttonCleanerOnClick(View v){
        createAlertDialog(true,
                getResources().getString(R.string.textDialogTitle),
                getResources().getString(R.string.textDialogMessage),
                getResources().getString(R.string.textDialogPositive),
                getResources().getString(R.string.textDialogNegative));
    }

    public void createAlertDialog(boolean options, String title, String message, String positiveText, String negativeText){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        if(options){
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveText,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            operationType = OperationType.New;
                            executeOperation();
                        }
                    })
                    .setNegativeButton(negativeText,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
        } else {
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveText,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
        }
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private  void showEditTextComponents(boolean show) {
        ExtendedTextView textView = (ExtendedTextView) findViewById(R.id.textViewSaldoAtual);
        ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        Button somarButton = (Button) findViewById(R.id.buttonDeposito);
        Button subtrairButton = (Button) findViewById(R.id.buttonSaque);
        Button limparButton = (Button) findViewById(R.id.buttonAtualizar);
        AdView adView = (AdView)this.findViewById(R.id.adView);

        if (show){
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            somarButton.setVisibility(View.INVISIBLE);
            subtrairButton.setVisibility(View.INVISIBLE);
            limparButton.setVisibility(View.INVISIBLE);
            setEditTextFirstValue();
            editText.addTextChangedListener(extendedTextWatcher);
            editText.requestFocus();
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            keyboardOpen = true;
            adView.setVisibility(View.INVISIBLE);
        } else {
            operationType = OperationType.Visualize;
            editText.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
            somarButton.setVisibility(View.VISIBLE);
            subtrairButton.setVisibility(View.VISIBLE);
            limparButton.setVisibility(View.VISIBLE);
            editText.removeTextChangedListener(extendedTextWatcher);
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            whaitFor(10);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            adView.setVisibility(View.VISIBLE);
        }
        setOperationText();
    }

    private void setEditTextFirstValue() {
        ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        BigDecimal parsed = new BigDecimal("0").setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        String formatted = NumberFormat.getCurrencyInstance().format(parsed);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
    }
    private void manageEditText(){
        showEditTextComponents(true);
        final ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        String regex = "[" + getCurrencySymbol() + ".]";
                        changeValue = new BigDecimal(editText.getText().toString().replaceAll(regex, "").replace(',', '.'));
                        executeOperation();
                    } catch (Exception e) { e.printStackTrace(); }
                    showEditTextComponents(false);
                    keyboardOpen = false;
                }
                return false;
            }
        });

        editText.setEventListener(new ExtendedEditText.KeyPreImeListener() {
            @Override
            public void onKeyPreImeAccured() {
                showEditTextComponents(false);
            }
        });
    }

    private void executeOperation(){
        switch (operationType){
            case Subtract:{
                saldoValue = saldoValue.subtract(changeValue);
                break;
            }
            case Add:{
                saldoValue =  saldoValue.add(changeValue);
                break;
            }
            case New:{
                saldoValue = BigDecimal.ZERO;
                break;
            }
            default:{
                break;
            }
        }
        updateSaldo();
        saveSaldo();
    }

    private void setOperationText(){
        TextView operationTextView = (TextView) findViewById(R.id.textViewOperation);
        if(operationType == OperationType.Subtract) operationTextView.setText(getResources().getString(R.string.textViewOperationSaque));
        else if(operationType == OperationType.Add) operationTextView.setText(getResources().getString(R.string.textViewOperationDeposito));
        else operationTextView.setText(getResources().getString(R.string.textViewOperationVisualizar));
    }

    private void saveSaldo(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storedSaldo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("storedSaldo", saldoValue.toString());
        editor.commit();

        Intent updateWidgetIntent = new Intent(getApplicationContext(), Widget.class);
        updateWidgetIntent.setAction(Widget.updateMessage);
        getApplicationContext().sendBroadcast(updateWidgetIntent);
    }

    private void loadSaldo(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storedSaldo", MODE_PRIVATE);
        saldoValue = new BigDecimal(sharedPref.getString("storedSaldo", "0"));
    }

    private void updateSaldo(){
        ExtendedTextView textView = (ExtendedTextView) findViewById(R.id.textViewSaldoAtual);
        if(saldoValue.compareTo(over1Billion) >= 0){
            createAlertDialog(false,
                    getResources().getString(R.string.textDialogTitleOver1Billion),
                    getResources().getString(R.string.textDialogMessageOver1Billion),
                    getResources().getString(R.string.textDialogPositiveOver1Billion),
                    null);
                textView.setText(R.string.textViewOver1Billion);
        } else if(saldoValue.compareTo(below100millions) <= 0) {
            createAlertDialog(false,
                    getResources().getString(R.string.textDialogTitleBelow100millions),
                    getResources().getString(R.string.textDialogMessageBelow100millions),
                    getResources().getString(R.string.textDialogPositiveBelow100millions),
                    null);
            textView.setText(R.string.textViewBelow100millions);
        } else {
            DecimalFormat decform = (DecimalFormat) NumberFormat.getCurrencyInstance();
            decform.setNegativePrefix(getCurrencySymbol() + "-");
            decform.setNegativeSuffix("");
            textView.setText(decform.format(saldoValue));
        }
    }

    private void whaitFor(int miliseconds){
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getCurrencySymbol(){
        DecimalFormat decform = (DecimalFormat) NumberFormat.getCurrencyInstance();
        return decform.getCurrency().getSymbol();

    }
}
