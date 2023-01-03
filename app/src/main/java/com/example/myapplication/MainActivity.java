package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;


public class MainActivity extends AppCompatActivity {
    VideoView iv_video;
    Button btnCamara, btnEnviar;

    String nombre;
    private static final int VIDEO_TIME = 4;

    private Uri vvURI_Practice;

    OkHttpClient okHttpClient;
    String URL = "http://192.168.2.251:"+5000+"/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        okHttpClient = new OkHttpClient();

        btnCamara = findViewById(R.id.btn_camara);
        iv_video = findViewById(R.id.iv_video);
        btnEnviar = findViewById(R.id.btn_enviar);

        btnCamara.setOnClickListener(v -> {

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_TIME);


            camaraLauncher.launch(intent);

        });
        btnEnviar.setOnClickListener(v->{
            getContenido.launch("video/*");
        });

        btnEnviar.setVisibility(View.GONE);
        iv_video.setVisibility(View.GONE);

    }

    protected void onResume() {
        super.onResume();
        if (vvURI_Practice != null){
            iv_video.setVideoURI(vvURI_Practice);
            iv_video.start();
            iv_video.setOnCompletionListener(mediaPlayer -> iv_video.start());
        }
    }

    ActivityResultLauncher<Intent> camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode()== RESULT_OK){
            btnEnviar.setVisibility(View.VISIBLE);
            iv_video.setVisibility(View.VISIBLE);

        }
    });

    ActivityResultLauncher<String> getContenido = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result1 -> {
                iv_video.setVideoURI(result1);
                iv_video.start();
                iv_video.setOnCompletionListener(mediaPlayer -> iv_video.start());
                btnEnviar.setVisibility(View.GONE);
                iv_video.setVisibility(View.GONE);
                try {
                    setVvURI_Practice(result1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });


    public void setVvURI_Practice(Uri vvURI_Practice) throws FileNotFoundException {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        final String contentType = contentResolver.getType(vvURI_Practice);
        final AssetFileDescriptor fd = contentResolver.openAssetFileDescriptor(vvURI_Practice, "r");


        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse(contentType);
            }

            @Override
            public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                try (InputStream is = fd.createInputStream()) {
                    bufferedSink.writeAll(Okio.buffer(Okio.source(is)));
                }
            }
        };

        RequestBody multi = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file","fname.mp4",requestBody)
                .build();

        Request request = new Request.Builder()
                .url(URL)
                .post(multi)
                .build();


        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

    }
}