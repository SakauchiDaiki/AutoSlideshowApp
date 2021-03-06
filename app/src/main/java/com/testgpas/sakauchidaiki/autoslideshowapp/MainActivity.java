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
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;
import android.os.Process;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // private cursor　メンバ変数に引き上げた。この場合は初期化はメンバ関数内で行うこと
    private ContentResolver resolver;
    private Cursor mCursor;
    // 再生・停止ボタンのフラグ
    boolean resumeFlag = false;

    // タイマー（自動再生）用の変数
    Timer mTimer;
    Handler mHandler = new Handler();

    // C言語とはenumの仕様が違うらしい。。
//    protected enum IMG_ID {
//        DEFAULT,
//        NEXT,
//        PREVIOUS
//    };


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
                getContentsInfo(0);
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo(0);
        }

    }


    @Override
    public void onClick(View v) {
        try {

            // 「進む」「戻る」は停止中のみ操作可能
            if(!resumeFlag) {
                // 進むボタン
                if (v.getId() == R.id.goNext_button) {
                    getContentsInfo(1);
                }
                // 戻るボタン
                else if (v.getId() == R.id.goBack_button) {
                    getContentsInfo(2);
                }
            }

            // 「再生」「停止」
            if (v.getId() == R.id.resume_stop_button) {
                Button resume_stop_button = (Button) findViewById(R.id.resume_stop_button);

                //　停止中に押したら再生を開始し、ボタンの表示を"停止"に
                if(!resumeFlag) {

                    // resumeFlag = trueのとき、ずっと稼働している必要あり

                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            // ここは最初に始動するポイントではないのか？？ここにgetContentInfo(1);記述したらエラー
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // 次の画像表示、なければ最初の画像に
                                                   getContentsInfo(1);
                                                }
                                            });
                                        }
                                    }, 2000, 2000); // 最初に始動させるまで2000ミリ秒、ループの感覚も2000ミリ秒に

                    resume_stop_button.setText("停止");
                }

                // 再生中に押したら停止し、ボタンの表示を"再生"に
                else{
                    mTimer.cancel();
                    mTimer = null;
                    resume_stop_button.setText("再生");
                }
                resumeFlag = !resumeFlag;
            }

        }

        // 適切な例外に。nullアクセスでok？
        catch (NullPointerException e) {
            Log.d("ANDROID", "[例外処理] nullへのアクセスです");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(0);
                }
                else{
                    // 許可されなかった場合、アプリを終了
                    finish();
                    Process.killProcess(Process.myPid());
                }
                break;
            default:
                break;
        }
    }


// permission拒否されたら or cursor = nullだったら
    private void getContentsInfo(int checkId) {

        // 適切なIDの時だけ対処
        if(0 <= checkId && checkId <= 2) {

            // 1枚目表示。デフォルト
            if (checkId == 0) {
                // なぜここで宣言しないといけないか要理解！一度だけで良い？
                resolver = getContentResolver();
                mCursor = resolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                        null, // 項目(null = 全項目)
                        null, // フィルタ条件(null = フィルタなし)
                        null, // フィルタ用パラメータ
                        null // ソート (null ソートなし)
                );

                mCursor.moveToFirst();
            }

            // 「次へ」を押した時。次の画像がなければ始めの画像に
            else if (checkId == 1) {
                if (mCursor.moveToNext()) {
                } else {
                    mCursor.moveToFirst();
                }
            }

            // 「前へ」を押した時。前の画像がなければ最後の画像に
            else if (checkId == 2) {
                if (mCursor.moveToPrevious()) {
                } else {
                    mCursor.moveToLast();
                }
            }

            if(mCursor != null) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = mCursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                Log.d("ANDROID", "URI : " + imageUri.toString());

                ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
            }
            else{
                Toast toast = Toast.makeText(MainActivity.this, "カーソルの値が不正です", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else{
            Toast toast = Toast.makeText(MainActivity.this, "不正な処理が指定されています", Toast.LENGTH_LONG);
            toast.show();
        }

    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        // アクティビティの終了と共にカーソル破棄
        mCursor.close();
    }

}