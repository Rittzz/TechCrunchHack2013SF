package com.rittz.clover.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

public class WebService extends IntentService {

    private static final String API_ENDPOINT = "http://ec2-107-21-174-28.compute-1.amazonaws.com:1407/payment_processed";

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
            // Format Data
            final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("order_id", orderId));
            nameValuePairs.add(new BasicNameValuePair("payment_id", paymentId));
            nameValuePairs.add(new BasicNameValuePair("amount", Long.toString(amount)));

            // Get location
            final Location loc = getLastKnownLocation();

            nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(loc.getLatitude())));
            nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(loc.getLongitude())));

            final Uri.Builder uri = Uri.parse(API_ENDPOINT).buildUpon();
            for (final NameValuePair pair : nameValuePairs) {
                uri.appendQueryParameter(pair.getName(), pair.getValue());
            }

            // Create a new HttpClient and Request
            final HttpClient httpclient = new DefaultHttpClient();
            final HttpGet httpRequest = new HttpGet(uri.build().toString());

            // Execute HTTP Request
            final HttpResponse response = httpclient.execute(httpRequest);
            final int statusCode = response.getStatusLine().getStatusCode();

            Log.d(LOG_TAG, statusCode + " ::: " + httpRequest.getURI().toString());
        }
        catch (final Exception ex) {
            Log.e(LOG_TAG, "Error", ex);
        }
    }

    private Location getLastKnownLocation() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location loc = null;

        // Start with the worst and then get the best
        loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        return loc;
    }

    public static Intent makeIntent(final Context ctx, final String orderId, final String paymentId, final long amount) {
        final Intent intent = new Intent(ctx, WebService.class);

        intent.putExtra(EXTRA_ORDER_ID, orderId);
        intent.putExtra(EXTRA_PAYMENT_ID, paymentId);
        intent.putExtra(EXTRA_AMOUNT, amount);

        return intent;
    }
}
