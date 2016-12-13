package com.example.atd.anddevclassproj;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

public class MainActivity extends AppCompatActivity {

    // This is needed so the TAG variables in onMessageReceived don't give an error
    // This was NOT in the tutorial from Google
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the title that is displayed in the action bar
        getSupportActionBar().setTitle("Deposit and Payment Application");
    }

    // This method is called when the user clicks the deposit button
    // This method will create and execute an intent to open the deposit activity
    public void onClickSendDeposit(View view) {
        Intent intent = new Intent(this, depositActivity.class);
        startActivity(intent);
    }

    // This method is called when the user clicks the payment button
    // This method will create and execute an intent to open the payment activity
    public void onClickSendPayment(View view) {
        Intent intent = new Intent(this, paymentActivity.class);
        startActivity(intent);
    }

    // This method handles notifications that arrive when the app is in the foreground.
    // This method will write the notification data to the log
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
