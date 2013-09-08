package com.rittz.clover.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WebService extends IntentService {

    private static final String LOG_TAG = "WebService";

    private static final String EXTRA_ORDER_ID = "com.rittz.clover.service.ORDER_ID";
    private static final String EXTRA_PAYMENT_ID = "com.rittz.clover.service.PAYMENT_ID";
    private static final String EXTRA_AMOUNT = "com.rittz.clover.service.AMOUNT";

    public WebService() {
        super("WebService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String orderId = intent.getExtras().getString(EXTRA_ORDER_ID);
        final String paymentId = intent.getExtras().getString(EXTRA_PAYMENT_ID);
        final long amount = intent.getExtras().getLong(EXTRA_AMOUNT);

        Log.d(LOG_TAG, "Starting Action ["+orderId+","+paymentId+","+amount+"]");

        try {
            final String apiEndpoint = getString(R.string.api_endpoint);

            // Create a new HttpClient and Post Header
            final HttpClient httpclient = new DefaultHttpClient();
            final HttpPost httppost = new HttpPost(apiEndpoint);

            // Add your data
            final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("order_id", orderId));
            nameValuePairs.add(new BasicNameValuePair("payment_id", paymentId));
            nameValuePairs.add(new BasicNameValuePair("amount", Long.toString(amount)));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            final HttpResponse response = httpclient.execute(httppost);
            final int statusCode = response.getStatusLine().getStatusCode();

            Log.d(LOG_TAG, statusCode + " ::: " + apiEndpoint);
        }
        catch (final Exception ex) {
            Log.e(LOG_TAG, "Error", ex);
        }
    }


    public static Intent makeIntent(final Context ctx, final String orderId, final String paymentId, final long amount) {
        final Intent intent = new Intent(ctx, WebService.class);

        intent.putExtra(EXTRA_ORDER_ID, orderId);
        intent.putExtra(EXTRA_PAYMENT_ID, paymentId);
        intent.putExtra(EXTRA_AMOUNT, amount);

        return intent;
    }
}
