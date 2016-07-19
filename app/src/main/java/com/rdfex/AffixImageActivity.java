package com.rdfex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AffixImageActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private ActiveUser activeUser = null;
    private String tripleId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affix_image);

        tripleId = getIntent().getExtras().getString("tripleId");

        String content = ExUtil.readFile(this, getString(R.string.credential_file_name));
        try {
            JSONObject user = new JSONObject(content);
            String name = user.optString("name", null);
            String email = user.optString("email", null);
            String userId = user.optString("_id", null);
            String token = user.optString("sessionToken", null);
            activeUser = new ActiveUser(name, email, userId, token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.imageView = (ImageView) this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // File Captured !!, Lets store this file into a temporary location
            final String file = getString(R.string.temp_image);
            FileOutputStream fout = null;
            try {
                fout = openFileOutput(file, 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (fout != null && photo != null) {
                System.out.println("File::: " + file);
                photo.compress(Bitmap.CompressFormat.JPEG, 18, fout);
                try {
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {


                        RequestBody body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("image",
                                        file,
                                        RequestBody.create(MediaType.parse("image/jpg"), getFileStreamPath(file)))
                                .addFormDataPart("user", activeUser.getUserId())
                                .addFormDataPart("sessionToken", activeUser.getSessionToken())
                                .addFormDataPart("triple", tripleId)
                                .build();

                        Request req = new Request.Builder()
                                .url(getString(R.string.affix_image_url))
                                .post(body)
                                .build();
                        try {
                            Response res = Constants.HTTP_CLIENT.newCall(req).execute();
                            return res.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        System.out.println(s);
                        finish();
                    }
                }.execute();
            }
            imageView.setImageBitmap(photo);
        }
    }
}
