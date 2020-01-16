package com.example.httpphp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int Result_Load_Image = 1;
    private static final int CAMERA_REQUEST_CODE =2828;
    private static final int STORAGE_REQUEST_CODE = 2727;
    String CameraPermissions[];
    String StoragePermissions[];



    private static final int Camera_Request = 1000;

    //private static final String URL = "http://";
    //"https://1234image.000webhostapp.com/";

    Button buplaod,download,camera;
    ImageView imageToUpload;
    EditText textToUpload;
    Uri image_uri;
    Bitmap result;
    String getUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buplaod = findViewById(R.id.Upload);
        buplaod.setVisibility(View.INVISIBLE);
        imageToUpload = findViewById(R.id.imageToUpload);
        textToUpload = findViewById(R.id.nameToUpload);
        camera = findViewById(R.id.Camera);

        CameraPermissions = new String[]
                {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};



        imageToUpload.setOnClickListener(this);
        buplaod.setOnClickListener(this);
        textToUpload.setOnClickListener(this);



        //get user input url
        Intent intent = getIntent();
        getUrl = intent.getStringExtra("geturl");


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permissions();
                buplaod.setVisibility(View.VISIBLE);
                if (getApplicationContext().getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA)) {
                    // Open default camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

                    // start the image capture Intent
                    startActivityForResult(intent, Camera_Request);

                } else {
                    Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
                }
            }
        });



    }

    public void permissions()
    {
        if(!checkCameraPermssions())
        {
            RequestCameraPermissions();
        }
        else
        {
            System.out.println("Permissions are already granted");
        }
    }


    public boolean checkCameraPermssions()
    {

        boolean Camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);

        boolean Storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return Camera_result && Storage_result;

    }

    public void RequestCameraPermissions()
    {
        ActivityCompat.requestPermissions(this,CameraPermissions,CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this,CameraPermissions,STORAGE_REQUEST_CODE);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.imageToUpload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,Result_Load_Image);
                break;

            case R.id.Upload:
                 upload();
                break;

            case R.id.nameToUpload:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Camera_Request && resultCode == RESULT_OK && data!=null)
        {
            //Uri it shows us the image address
           Uri selectedImage = data.getData();

           imageToUpload.setImageURI(selectedImage);
//         Bitmap photo = (Bitmap) data.getExtras().get("data");
//         imageToUpload.setImageBitmap(photo);


        }
        else
        {

            Toast.makeText(MainActivity.this,"Error in display",Toast.LENGTH_LONG).show();
        }
    }

        public void upload()
        {
            Bitmap image = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
            new UploadImage(image,textToUpload.getText().toString()).execute();


        }


    private class UploadImage extends AsyncTask<Void, Void, Bitmap>
    {
        private ProgressDialog pd = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Uploading Image To server ");
            pd.show();
        }

        Bitmap image;
        String name;


        public UploadImage(Bitmap image, String name)
            {
                this.image = image;
                this.name = name;

            }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            ByteArrayOutputStream bao = new ByteArrayOutputStream(); //it holds the byte representation of the iamge
            image.compress(Bitmap.CompressFormat.JPEG, 100 ,bao);
            String encodedImage = Base64.encodeToString(bao.toByteArray(),Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedImage));
            dataToSend.add(new BasicNameValuePair("name",name));

            HttpParams httpRequestParams = getHttpRequestParams();

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost("http://"+getUrl+" /");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));


                HttpResponse response = client.execute(post);

                InputStream is = response.getEntity().getContent();
                result = BitmapFactory.decodeStream(is);

                FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "test.png"));
                int read = 0;
                byte[] buffer = new byte[32768];
                while( (read = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                }



                fos.close();
                is.close();
                System.out.println("Response from server is ");


            }
            catch (Exception e)
            {

            }



            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();

            imageToUpload.setImageBitmap(result);
//            Bitmap bmp = BitmapFactory.decodeFile("/storage/emulator/0");
//            downloadedImage.setImageBitmap(bmp);






            //Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(response));
            //downloadedImage.setImageBitmap(bmp);
            //downloadedImage.setImageBitmap(response);
            //downloadedImage.setImageURI((Uri) response);
            //imageToUpload.setImageURI((Uri) response);
             Toast.makeText(MainActivity.this,"Image Uploaded to" +getUrl,Toast.LENGTH_LONG).show();
        }
    }

    private HttpParams getHttpRequestParams()
    {
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 60);
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 60);
        return httpRequestParams;
    }
}
