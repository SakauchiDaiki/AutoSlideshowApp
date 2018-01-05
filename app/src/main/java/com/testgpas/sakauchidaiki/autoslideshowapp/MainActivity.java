package com.testgpas.sakauchidaiki.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
//
//    // private cursor　メンバ関数に引き上げた
//    private ContentResolver resolver = getContentResolver();
//    private Cursor mCursor = resolver.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
//            null, // 項目(null = 全項目)
//            null, // フィルタ条件(null = フィルタなし)
//            null, // フィルタ用パラメータ
//            null // ソート (null ソートなし)
//    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button goNext_button = (Button) findViewById(R.id.goNext_button);
        goNext_button.setOnClickListener(this);
        Button goBack_button = (Button) findViewById(R.id.goBack_button);
        goBack_button.setOnClickListener(this);
        Button resume_stop_button = (Button) findViewById(R.id.resume_stop_button);
        resume_stop_button.setOnClickListener(this);


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {

            getContentsInfo();
        }
    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.goNext_button) {
                // 次へ。なかったら最初の画像に
//                if (mCursor.moveToNext()) {
//                } else {
//                    mCursor.moveToFirst();
//                }
                getContentsInfo();
            } else if (v.getId() == R.id.goBack_button) {
                // 前へ。なかったら最後の画像に
//                if (mCursor.moveToPrevious()) {
//                } else {
//                    mCursor.moveToLast();
//                }
                getContentsInfo();
            } else if (v.getId() == R.id.resume_stop_button) {
                // 再生・停止
            }

        }

        // 適切な例外に
        catch (NumberFormatException e) {
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }


// permission拒否されたら or cursor = nullだったら

    private void getContentsInfo() {


        // private cursor　メンバ関数に引き上げた
        ContentResolver resolver = getContentResolver();
        Cursor mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {

            // indexからIDを取得し、そのIDから画像のURIを取得する
            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = mCursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            Log.d("ANDROID", "URI : " + imageUri.toString());

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Android", "onDestroy");
        // アクティビティの終了と共にカーソル破棄
       // mCursor.close();
    }

// onDestroyでcursor.close

}