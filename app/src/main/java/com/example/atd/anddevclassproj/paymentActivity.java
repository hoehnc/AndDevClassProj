package com.example.atd.anddevclassproj;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class paymentActivity extends AppCompatActivity {

    private static int Result_Load_Image = 1;
    String imgDecodeString;
    private StorageReference mStorageRef;
    String imgFilePath;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title that is displayed in the action bar
        getSupportActionBar().setTitle("Bank - Payment");

        // Initilize our Firebase storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    // This method is called when the user clicks the select image from gallery button
    // This method will make the calls needed to select an image from the gallery and display a preview on the screen
    public void onClickSelectPictureFromGallery(View view) {
        // Create the intent to open the image gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Set this to 1 to show we are picking an image
        Result_Load_Image = 1;
        // start the intent
        startActivityForResult(galleryIntent,Result_Load_Image);
    }

    // This method is called when the user clicks the take picture button
    // This method will create and execute an intent to open the camera
    public void onClickTakePic(View view) {
        // Create our intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a new filename so that we know where the image we take will be located
        imgFilePath = "fname_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),imgFilePath));

        // Set Result_Load_Image to 2 to show we are taking a picture with the camera
        Result_Load_Image = 2;
        // Start the intent
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,0);
    }

    // This method is called after an image is selected (or not selected. but after we are done with the gallery).
    // This method will pull the image from the mediastore and display it on the screen.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        try {
            // Check Result_Load_Image. For 1, we are picking a image from the gallery. For 2, we are taking a picture with the camera
            if (Result_Load_Image == 1)  {
                // Handle an image getting picked from the gallery
                if (requestCode == Result_Load_Image && resultCode == RESULT_OK && null != data) {
                    // We have gotten an image. We now need to pull it from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor and move to the first row
                    Cursor cursor = getContentResolver().query(selectedImage,filePathColumn,null,null,null);
                    cursor.moveToFirst();

                    // Get the string for the file and give it to the image view to display the image
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodeString = cursor.getString(columnIndex);
                    cursor.close();
                    ImageView imgView = (ImageView) findViewById(R.id.ivSelectedPic);
                    imgView.setImageBitmap(BitmapFactory.decodeFile(imgDecodeString));
                } else {
                    // The user didn't pick an image. Let the user know that
                    Toast.makeText(this, "No image selected",Toast.LENGTH_LONG).show();
                }
            } else if (Result_Load_Image == 2) {
                // Get the file path to the image we just took and add the image to the example view
                ImageView mImageView = (ImageView) findViewById(R.id.ivSelectedPic);
                imgDecodeString = "/storage/sdcard/" + imgFilePath;
                mImageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodeString));
            }

        } catch (Exception e) {
            // If something goes wrong, let the user know
            Toast.makeText(this, "Yo, things not working!", Toast.LENGTH_LONG).show();
        }
    }

    // This method will be called when the user clicks the send payment button
    // This method will send the selected image to the Firebase project
    public void onClickSendPayment(View view) {

        // Get the URI to the image we want to upload
        Uri file = Uri.fromFile(new File(imgDecodeString));

        // Get the current date and time and add them to the name of the image to prevent overwriting images
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        StorageReference paymentStorageRef = mStorageRef.child("Payment/paymentImage " + currentDateTimeString + ".jpg");

        // Attempt to send the file to Firebase
        paymentStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        SendNotificationWithPicture(); // Send a notification with the image to let the user know of the success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // A toast can't be placed here, so just do nothing.
                        // If the upload failed then the user doesn't get the notification saying it was a success. That shows it failed.
                    }
                });
    }

    // This method is called when a image is sent to FireBase
    // This method will create a notification to let the user know that the image was sent, and shows that image in the notification
    public void SendNotificationWithPicture() {
        // Get the title and content
        String title = "Payment Made";
        String body = "Payment Made";

        // Get the image we want to display
        Bitmap paymentBitmap = BitmapFactory.decodeFile(imgDecodeString);

        // Build the notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setLargeIcon(paymentBitmap)
                        .setStyle(new android.support.v7.app.NotificationCompat.BigPictureStyle()
                                .bigPicture(paymentBitmap));


        // Create our intent and stack builder and send the notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotiManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiManager.notify(9998,mBuilder.build());
    }

}
