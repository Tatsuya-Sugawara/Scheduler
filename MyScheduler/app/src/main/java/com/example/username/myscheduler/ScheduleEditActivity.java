package com.example.username.myscheduler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.ContentValues.TAG;

//import android.support.v7.util.ThreadUtil;

public class ScheduleEditActivity extends AppCompatActivity {

    static final int PHOTO_REQUEST_CODE = 1;
    private static final int REQUEST_CODE = 1000;
    private TessBaseAPI tessBaseApi;
    private static final String lang = "jpn";
    // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
    private int lan = 2;
    private String DATA_PATH;
    private static final String TESSDATA = "tessdata";
    private Realm mRealm;
    ProgressDialog progressDialog;
    Bitmap bitmap = null;
    String resultOCR;
    String dateOCR;
    Handler handler;
    Thread dateThread;
    ImageView img;


    EditText mDateEdit;
    EditText mDatetimeEdit;
    EditText mTitleEdit;
    EditText mDetailEdit;
    Button mDelete;

    String onDate;
    String sEAyear;




    public void ScheduleEditActivity(){
        int year;
        int month;
        int day;
        System.out.println("コンストラクタ起動");
        final Calendar CALENDAR = Calendar.getInstance();
        year = CALENDAR.get(Calendar.YEAR);
        month = CALENDAR.get(Calendar.MONTH) + 1;
        day = CALENDAR.get(Calendar.DATE);

        mRealm = Realm.getDefaultInstance();
        mDateEdit = (EditText) findViewById(R.id.dateEdit);
        mDatetimeEdit = (EditText)findViewById(R.id.dateTimeEdit);
        mTitleEdit = (EditText) findViewById(R.id.titleEdit);
        mDetailEdit = (EditText) findViewById(R.id.detailEdit);
        mDelete = (Button) findViewById(R.id.delete);
        mDateEdit.setText(year + "/" + month + "/" + day);
        mDatetimeEdit.setText("00:00");
        if(MainActivity.onDate != null){
            onDate = MainActivity.onDate;
        }else{
            onDate = (year + "/" + month + "/" + day);
        }
        if(MainActivity.sEAyear != null){
            sEAyear = MainActivity.sEAyear;
        }else{
            sEAyear = String.valueOf(year);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);
        ScheduleEditActivity();



        handler = new Handler();

        ImageButton voiceToText = (ImageButton) findViewById(R.id.voice_to_text);
        voiceToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 音声認識を開始
                speech();
            }
        });

        DATA_PATH = getFilesDir().toString() + "/OCR/";
        // textView = (TextView) findViewById(R.id.textResult);
        //Log.v("てすと",DATA_PATH);
        //Log.v("てすと",getFilesDir().toString());
        prepareDirectory(DATA_PATH);
        prepareDirectory(DATA_PATH + TESSDATA);
        copyTessDataFiles(TESSDATA);

        Button captureImg = (Button) findViewById(R.id.action_btn);
        if (captureImg != null) {
            captureImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT > 19) {
                        Log.v("てすと","化石じゃないです");
                        selectGallery();
                    } else {
                        Log.v("てすと","化石です");
                        selectGalleryNamekuzi();
                    }
                }
            });
        }




        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
//編集画面
            RealmResults<Schedule> results = mRealm.where(Schedule.class)
                    .equalTo("id", scheduleId).findAll();
            Schedule schedule = results.first();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String date = sdf.format(schedule.getDate());
            mDateEdit.setText(date);
            mDatetimeEdit.setText(schedule.getDatetime());
            mTitleEdit.setText(schedule.getTitle());
            mDetailEdit.setText(schedule.getDetail());

            mDelete.setVisibility(View.VISIBLE);
        } else {
//初期登録
            if(onDate != null){
                mDateEdit.setText(onDate);
            }

            System.out.println(onDate);
            mDelete.setVisibility(View.INVISIBLE);
        }
    }

    private void selectGalleryNamekuzi() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_REQUEST_CODE);
    }

    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if(data != null) {
                uri = data.getData();
                try {
                    bitmap = getBitmapFromUri(uri);
                    alertDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if(match.isMatch(resultOCR,MainActivity.sEAyear,onDate).equals(onDate)){
//                Toast.makeText(this, "日付が読み込めませんでした。\n選択日の日付を入力します。", Toast.LENGTH_LONG).show();
//            }
        } else {

        }
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            // 認識結果を ArrayList で取得
            ArrayList<String> candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (candidates.size() > 0) {
                // 認識結果候補で一番有力なものを表示
                mDetailEdit.setText(candidates.get(0));
            }
        }
    }

    private void alertDialog(){
        img = new ImageView(this);
        img.setImageBitmap(Bitmap.createScaledBitmap(bitmap,600,1000,false));
        new AlertDialog.Builder(this)
                .setTitle("最終確認")
                .setMessage("本当にこの画像でよろしいでしょうか?")
                .setView(img)
                .setPositiveButton("承諾", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setOCR();
                    }
                })
                .setNegativeButton("やっぱやめとく",null)
                .setCancelable(false)
                .show();
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
           // Log.v("てすと",path + "にはファイルはありません");
            if (dir.mkdirs()) {
               // Log.v("てすと",path + "にファイル作成されました");
            } else {
              //  Log.v("てすと",path + "にはファイル作成されませんでした");
            }
        } else {
          //  Log.v("てすと",path + "にはファイルは存在します");
        }
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                String pathToDataFile = DATA_PATH + path + "/" + fileName;

                Log.v("てすと",DATA_PATH + path + "/" + fileName);

                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setOCR(){
        Log.v("てすと","あああ");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("ただいま画像処理中です...");
        progressDialog.setMessage("しばらくお待ちください...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Thread ocrThread = new Thread(new Runnable() {
            @Override
            public void run() {

                resultOCR = extractText(bitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDetailEdit.setText(resultOCR);
                        progressDialog.dismiss();
                        dateThread.start();
                    }
                });
            }
        });


        dateThread = new Thread(new Runnable() {
            @Override
            public void run() {

                dateOCR = resultOCR;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SelectDialog days = new SelectDialog();
                        Match match = new Match();
                        days.setItems(match.isMatch(resultOCR, sEAyear, onDate));
                        days.setCancelable(false);
                        days.show((getFragmentManager()), "tag");

                        mDateEdit.setText(days.getRes());
                        System.out.println("ダイアログタップ後");
                    }
                });
            }
        });
        ocrThread.start();
    }

    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }


    public void onSaveTapped(View view) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date dateParse = new Date();
        try {
            dateParse = sdf.parse(mDateEdit.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date workDate  = new Date(dateParse.getYear(),dateParse.getMonth(),dateParse.getDate());
        workDate.setHours(00);
        workDate.setMinutes(00);
        workDate.setSeconds(00);

        final Date date = workDate;
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
            final RealmResults<Schedule> results = mRealm.where(Schedule.class)
                    .equalTo("id", scheduleId).findAll();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Schedule schedule = results.first();
                    schedule.setDate(date);
                    schedule.setDatetime(mDatetimeEdit.getText().toString());
                    schedule.setTitle(mTitleEdit.getText().toString());
                    schedule.setDetail(mDetailEdit.getText().toString());
                }
            });
            Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Number maxId = realm.where(Schedule.class).max("id");
                    long nextId = 0;
                    if (maxId != null) nextId = maxId.longValue() + 1;
                    Schedule schedule
                            = realm.createObject(Schedule.class, new Long(nextId));
                    schedule.setDate(date);
                    schedule.setDatetime(mDatetimeEdit.getText().toString());
                    schedule.setTitle(mTitleEdit.getText().toString());
                    schedule.setDetail(mDetailEdit.getText().toString());
                }
            });

            Toast.makeText(this, "追加しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onDeleteTapped(View view) {
        final long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId != -1) {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Schedule schedule = realm.where(Schedule.class)
                            .equalTo("id", scheduleId).findFirst();
                    schedule.deleteFromRealm();
                }
            });
            Toast.makeText(this, "削除しました", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void speech(){
        // 音声認識が使えるか確認する
        try {
            // 音声認識の　Intent インスタンス
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            if(lan == 0){
                // 日本語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString() );
            }
            else if(lan == 1){
                // 英語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString() );
            }
            else if(lan == 2){
                // Off line mode
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            }
            else{
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            }

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力");
            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e) {
            mDetailEdit.setText("No Activity " );
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @SuppressLint("ValidFragment")
    public class SelectDialog extends DialogFragment {

        String[] items;
        String res;




        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int defaultItem = -1; // デフォルトでチェックされているアイテム
            final List<Integer> checkedItems = new ArrayList<>();
            checkedItems.add(defaultItem);
            return new AlertDialog.Builder(getActivity())
                    .setTitle("日付の選択")
                    .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            checkedItems.clear();
                            checkedItems.add(which);
                            res = items[which];
//                            System.out.println(res);
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            if(!checkedItems.isEmpty()){
                                Log.d("checkedItem:","" + checkedItems.get(0));
                            }
                            mDateEdit.setText(res);

                        }
                            })
                    .setNegativeButton("Cancel",null)
                    .show();
//                    .setItems(items, new DialogInterface.OnClickListener(){
//                        public void onClick(DialogInterface dialog, int which) {
//                            // item_which pressed
//                            res = items[which];
//                            mDateEdit.setText(res);
//                        }
//                    })
//                    .setNegativeButton("Cancel",null)
//                    .show();
        }
        public void setItems(String[] days) {
            int cnt = 0;
            items = new String[days.length];
            for(String item:days){
                items[cnt] = item;
                cnt++;
            }
        }

        public String getRes() {
            System.out.println("res:" + res);
            if(res !=null){
                return res;
            }else{
                return String.valueOf(mDateEdit.getText());
            }
        }

    }

}
