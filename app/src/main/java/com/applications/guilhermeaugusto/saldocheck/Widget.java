package com.applications.guilhermeaugusto.saldocheck;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Created by guilhermeaugusto on 13/06/2014.
 */
public class Widget extends AppWidgetProvider{

    public static String updateMessage = "updateMessage";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.update(context);
    }

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        super.onReceive(context, intent);

        if (this.updateMessage.equals(intent.getAction()))
        {
            update(context);
        }
    }

    private void update(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.buttonWidget, configPendingIntent);
        remoteViews.setTextViewText(R.id.textViewWidget, this.getSaldoFromSharedPreferences(context));
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private String getSaldoFromSharedPreferences(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("storedSaldo", Context.MODE_PRIVATE);
        BigDecimal storedSaldo = new BigDecimal(sharedPref.getString("storedSaldo", "0"));
        final BigDecimal over1Billion = new BigDecimal("1000000000");
        final BigDecimal below100millions = new BigDecimal("-100000000");
        if(storedSaldo.compareTo(over1Billion) >= 0) {
            return context.getString(R.string.textViewOver1Billion);
        } else if(storedSaldo.compareTo(below100millions) <= 0) {
            return context.getString(R.string.textViewBelow100millions);
        } else {
            DecimalFormat decform = (DecimalFormat) NumberFormat.getCurrencyInstance();
            String symbol = decform.getCurrency().getSymbol();
            decform.setNegativePrefix(symbol + "-");
            decform.setNegativeSuffix("");
            return decform.format(storedSaldo);
        }
    }
}