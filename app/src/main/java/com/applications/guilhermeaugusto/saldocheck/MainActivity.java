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

    private enum ModelType { Edit, Visualize  }
    private enum EditType { Subtract, Add, New, Positive, Negative }
    private enum VisualizeType { New, Over, Under, Default }
    private enum DialogType { OverFlow, Reset, None }

    private BigDecimal saldoValue;
    private BigDecimal over1Billion;
    private BigDecimal below100millions;
    private BigDecimal changeValue;
    private EditType editType;
    private VisualizeType visualizeType;
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
        rulesForShowComponents(ModelType.Visualize);
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
        extendedTextWatcher = new ExtendedTextWatcher(editText);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboardOpen = false;
    }

    private void createAdBanner(){
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
    }

    public void buttonSaqueOnClick(View v){
        if(visualizeType == VisualizeType.New){ editType = EditType.Negative; }
        else { editType = EditType.Subtract; }
        rulesForShowComponents(ModelType.Edit);
    }

    public void buttonDepositoOnClick(View v){
        if(visualizeType == VisualizeType.New){ editType = EditType.Positive; }
        else {editType = EditType.Add; }
        rulesForShowComponents(ModelType.Edit);
    }

    public void buttonCleanerOnClick(View v){
        createAlertDialog(DialogType.Reset,
                getResources().getString(R.string.textDialogTitle),
                getResources().getString(R.string.textDialogMessage),
                getResources().getString(R.string.textDialogPositive),
                getResources().getString(R.string.textDialogNegative));
    }

    private  void rulesForShowComponents(ModelType modelType) {
        ExtendedTextView extendedTextView = (ExtendedTextView) findViewById(R.id.textViewSaldoAtual);
        ExtendedEditText extendedEditText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        TextView textViewOperation = (TextView) findViewById(R.id.textViewOperation);
        TextView textViewComplementMessage = (TextView) findViewById(R.id.textViewComplementMessage);
        Button somarButton = (Button) findViewById(R.id.buttonDeposito);
        Button subtrairButton = (Button) findViewById(R.id.buttonSaque);
        Button limparButton = (Button) findViewById(R.id.buttonAtualizar);
        AdView adView = (AdView)this.findViewById(R.id.adView);

        switch (modelType){
            case Edit: {
                extendedEditText.setVisibility(View.VISIBLE);
                extendedTextView.setVisibility(View.INVISIBLE);
                somarButton.setVisibility(View.INVISIBLE);
                subtrairButton.setVisibility(View.INVISIBLE);
                limparButton.setVisibility(View.INVISIBLE);
                textViewOperation.setVisibility(View.VISIBLE);
                textViewComplementMessage.setVisibility(View.INVISIBLE);
                manageEditText();
                setEditTextFirstValue();
                extendedEditText.addTextChangedListener(extendedTextWatcher);
                extendedEditText.requestFocus();
                inputMethodManager.showSoftInput(extendedEditText, InputMethodManager.SHOW_IMPLICIT);
                keyboardOpen = true;
                adView.setVisibility(View.INVISIBLE);

                switch (editType){
                    case Add: textViewOperation.setText(getResources().getString(R.string.textViewOperationDeposito)); break;
                    case Subtract: textViewOperation.setText(getResources().getString(R.string.textViewOperationSaque)); break;
                    case Positive: textViewOperation.setText(getResources().getString(R.string.textViewOperationSaldoPositivo)); break;
                    case Negative: textViewOperation.setText(getResources().getString(R.string.textViewOperationSaldoNegativo)); break;
                    default: break;
                }

                break;
            }
            case Visualize: {
                extendedEditText.setVisibility(View.INVISIBLE);
                extendedTextView.setVisibility(View.VISIBLE);
                somarButton.setVisibility(View.VISIBLE);
                subtrairButton.setVisibility(View.VISIBLE);
                somarButton.setText(getResources().getString(R.string.buttonNameDeposito));
                subtrairButton.setText(getResources().getString(R.string.buttonNameSaque));
                limparButton.setVisibility(View.VISIBLE);
                textViewOperation.setVisibility(View.VISIBLE);
                textViewComplementMessage.setVisibility(View.INVISIBLE);
                extendedEditText.removeTextChangedListener(extendedTextWatcher);
                inputMethodManager.hideSoftInputFromWindow(extendedEditText.getWindowToken(), 0);
                whaitFor(10);
                adView.setVisibility(View.VISIBLE);

                switch (visualizeType){
                    case New: {
                        extendedTextView.setVisibility(View.INVISIBLE);
                        textViewComplementMessage.setVisibility(View.VISIBLE);
                        limparButton.setVisibility(View.INVISIBLE);
                        textViewOperation.setText(getResources().getString(R.string.textViewOperationNewSaldo));
                        somarButton.setText(getResources().getString(R.string.buttonNamePositivo));
                        subtrairButton.setText(getResources().getString(R.string.buttonNameNegativo));
                        break;
                    }
                    case Over: {
                        extendedTextView.setText(R.string.textViewOver1Billion);
                        textViewOperation.setText(getResources().getString(R.string.textViewOperationVisualizar));
                        break;
                    }
                    case Under: {
                        extendedTextView.setText(R.string.textViewBelow100millions);
                        textViewOperation.setText(getResources().getString(R.string.textViewOperationVisualizar));
                        break;
                    }
                    case Default: {
                        textViewOperation.setText(getResources().getString(R.string.textViewOperationVisualizar));
                        extendedTextView.setText(formatCurrency(saldoValue));
                    } break;
                    default: break;
                }
                break;
            }
            default: break;
        }
    }

    private void setEditTextFirstValue() {
        ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        BigDecimal parsed = new BigDecimal("0").setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        String formatted = NumberFormat.getCurrencyInstance().format(parsed);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
    }

    private void manageEditText(){
        final ExtendedEditText editText = (ExtendedEditText) findViewById(R.id.editTextSaldo);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        String regex = "[" + getCurrencySymbol() + ".,]";
                        String value = editText.getText().toString().replaceAll(regex, "");
                        changeValue = new BigDecimal(value.substring(0, value.length() - 2) + "." + value.substring(value.length() - 2, value.length()));
                        executeOperation();
                    } catch (Exception e) { e.printStackTrace(); }
                    keyboardOpen = false;
                }
                return false;
            }
        });

        editText.setEventListener(new ExtendedEditText.KeyPreImeListener() {
            @Override
            public void onKeyPreImeAccured() {
                rulesForShowComponents(ModelType.Visualize);
            }
        });
    }

    private void executeOperation(){
        if(saldoValue == null) saldoValue = BigDecimal.ZERO;

        switch (editType){
            case Negative:
            case Subtract:{ saldoValue = saldoValue.subtract(changeValue); break; }
            case Positive:
            case Add:{ saldoValue =  saldoValue.add(changeValue); break; }
            case New:{ saldoValue = null; break; }
            default:{ break; }
        }
        updateSaldo();
        saveSaldo();
    }

    private void saveSaldo(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storedSaldo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(saldoValue != null) { editor.putString("storedSaldo", saldoValue.toString()); }
        else { editor.putString("storedSaldo", null); }
        editor.commit();

        Intent updateWidgetIntent = new Intent(getApplicationContext(), Widget.class);
        updateWidgetIntent.setAction(Widget.updateMessage);
        getApplicationContext().sendBroadcast(updateWidgetIntent);
    }

    private void loadSaldo(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("storedSaldo", MODE_PRIVATE);
        String saldo = sharedPref.getString("storedSaldo", null);
        if(saldo != null) { saldoValue = new BigDecimal(saldo); }
        else { saldoValue = null; }
    }

    private void updateSaldo(){
        if(saldoValue == null){ visualizeType = VisualizeType.New; }
        else if(saldoValue.compareTo(over1Billion) >= 0){
            visualizeType = VisualizeType.Over;
            createAlertDialog(DialogType.OverFlow,
                    getResources().getString(R.string.textDialogTitleOver1Billion),
                    getResources().getString(R.string.textDialogMessageOver1Billion),
                    getResources().getString(R.string.textDialogPositiveOver1Billion),
                    null);
        }
        else if(saldoValue.compareTo(below100millions) <= 0) {
            visualizeType = VisualizeType.Under;
            createAlertDialog(DialogType.OverFlow,
                    getResources().getString(R.string.textDialogTitleBelow100millions),
                    getResources().getString(R.string.textDialogMessageBelow100millions),
                    getResources().getString(R.string.textDialogPositiveBelow100millions),
                    null);}
        else { visualizeType = VisualizeType.Default; }
        rulesForShowComponents(ModelType.Visualize);
    }

    public void createAlertDialog(DialogType dialogType, String title, String message, String positiveText, String negativeText){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        switch (dialogType){
            case Reset: {
                alertDialogBuilder.setPositiveButton(positiveText,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                    editType = EditType.New;
                    executeOperation();
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });
                break;
            }
            case OverFlow: {
                alertDialogBuilder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });
                break;
            }
            default: break;
        }
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void whaitFor(int miliseconds){
        try { Thread.sleep(miliseconds); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    public String getCurrencySymbol(){
        DecimalFormat decform = (DecimalFormat) NumberFormat.getCurrencyInstance();
        return decform.getCurrency().getSymbol();
    }

    public String formatCurrency(BigDecimal value){
        DecimalFormat decform = (DecimalFormat) NumberFormat.getCurrencyInstance();
        decform.setNegativePrefix(getCurrencySymbol() + "-");
        decform.setNegativeSuffix("");
        return decform.format(value);
    }
}
