package com.rittz.clover.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private static final String BROADCAST_ACTION = "com.clover.intent.action.PAYMENT_PROCESSED";

    private View broadcastButton;

    private EditText orderIdEditText;
    private EditText paymentIdEditText;
    private EditText amountEditText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcastButton = findViewById(R.id.broadcast);

        orderIdEditText = (EditText) findViewById(R.id.order_id);
        paymentIdEditText = (EditText) findViewById(R.id.payment_id);
        amountEditText = (EditText) findViewById(R.id.amount);

        broadcastButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final String orderId = orderIdEditText.getText().toString();
                final String paymentId = paymentIdEditText.getText().toString();

                try {
                    final long amount = Long.parseLong(amountEditText.getText().toString());
                    sendBroadcast(orderId, paymentId, amount);
                }
                catch (final NumberFormatException ex) {
                    Toast.makeText(MainActivity.this, "Amount must be a number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final CompoundButton locationTrack = (CheckBox) findViewById(R.id.location_tracking);
        locationTrack.setChecked(LocationTrackingService.isTrackingLocation());
        locationTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                final Intent intent = LocationTrackingService.makeIntent(MainActivity.this, isChecked);
                startService(intent);
            }
        });
    }

    private void sendBroadcast(final String orderId, final String paymentId, final long amount) {
        final Intent broadcastIntent = new Intent(BROADCAST_ACTION);

        broadcastIntent.putExtra(CloverReceiver.EXTRA_ORDER_ID, orderId);
        broadcastIntent.putExtra(CloverReceiver.EXTRA_PAYMENT_ID, paymentId);
        broadcastIntent.putExtra(CloverReceiver.EXTRA_AMOUNT, amount);

        sendBroadcast(broadcastIntent);

        Toast.makeText(this, "Sent Broadcast", Toast.LENGTH_SHORT).show();
    }
}
