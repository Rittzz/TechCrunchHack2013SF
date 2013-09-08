package com.rittz.clover.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CloverReceiver extends BroadcastReceiver {

    public static final String EXTRA_ORDER_ID = "com.clover.intent.extra.ORDER_ID";
    public static final String EXTRA_PAYMENT_ID = "com.clover.intent.extra.PAYMENT_ID";
    public static final String EXTRA_AMOUNT = "com.clover.intent.extra.AMOUNT";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String orderId = intent.getExtras().getString(EXTRA_ORDER_ID);
        final String paymentId = intent.getExtras().getString(EXTRA_PAYMENT_ID);
        final long amount = intent.getExtras().getLong(EXTRA_AMOUNT);

        //Toast.makeText(context, "Received Action ["+orderId+","+paymentId+","+amount+"]", Toast.LENGTH_SHORT).show();

        final Intent serviceIntent = WebService.makeIntent(context, orderId, paymentId, amount);
        context.startService(serviceIntent);
    }
}
