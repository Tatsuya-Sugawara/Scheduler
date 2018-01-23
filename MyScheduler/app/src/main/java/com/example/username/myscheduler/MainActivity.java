package com.example.username.myscheduler;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.CalendarDayEvent;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static java.util.Calendar.YEAR;

public class MainActivity extends AppCompatActivity implements CalendarView.OnDateChangeListener{

    private Toolbar toolbar;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("yyyy- MMMM", Locale.getDefault());
    private Realm mR;
    private ListView mLV;
    private CalendarView cv;
    private CompactCalendarView calendarView;
    private RealmResults<Schedule> schedules;
    private Calendar calendar = Calendar.getInstance();
    ScheduleAdapter adapter;
    FloatingActionButton fab;
    public  static  String onDate;
    public  static String sEAyear;

    static int year;

    static int month;

    static int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("test","なんで返信くれないの？？？ひどいよ。。。");

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);

        if (Build.VERSION.SDK_INT >= 19) {
            View decor = this.getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawable d = toolbar.getBackground();
        d.setAlpha(0);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(null);

        calendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        Date curDate = Calendar.getInstance().getTime();
        calendarView.setCurrentDate(curDate);
        calendarView.setShouldDrawDaysHeader(true);//選択した日にイベントがある場合でもイベントを表示
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendarView.drawSmallIndicatorForEvents(true);
        calendarView.setUseThreeLetterAbbreviation(true);

        actionBar.setTitle("　　　　　　"+dateFormatForMonth.format(calendarView.getFirstDayOfCurrentMonth()));

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                System.out.println("なんででないんですか");
                cv =  new CalendarView(
                        MainActivity.this
                );
                Date d = dateClicked;
                cv.setDate(d.getDate());
                System.out.println(cv.getDate());
//                cv.setOnDateChangeListener(this);
                Toast.makeText(MainActivity.this, "Date : " + dateClicked.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                // Changes toolbar title on monthChange
                actionBar.setTitle("　　　　　　"+dateFormatForMonth.format(firstDayOfNewMonth));

            }

        });



        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == fab){
                    Toast toast = Toast.makeText(MainActivity.this, "日付を選択してください", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });

        //System.out.println("getInstance前");
        mR = Realm.getDefaultInstance();
        try {
            mR.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Calendar calendar = Calendar.getInstance();
                    final  Schedule schedule = new Schedule();
                    schedule.setDatetime("10:00");
                    schedule.setDate(calendar.getTime());
                    schedule.setDetail("aaaaaaaaaaaa");
                    schedule.setTitle("aaaaaaaaaaaa");
                    // プライマリーキーが同じならアップデート
                    realm.copyToRealmOrUpdate(schedule);
                }
            });
        } finally {
            // getしたらcloseする
//            mR.close();
        }
        System.out.println("Relam　出てる？");

        mLV = (ListView) findViewById(R.id.listView);





        onSelectedDayChange(cv,year,month,day);

        mLV.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                        Schedule schedule = (Schedule) parent.getItemAtPosition(position);
                        startActivity(new Intent(MainActivity.this,ScheduleEditActivity.class).putExtra("schedule_id", schedule.getId()));
                    }
                });




        /*191行まで常駐通知の作成*/
        Intent intent = new Intent(this, ScheduleEditActivity.class/*ここで指定したクラスが通知をタップした時に呼び出される*/);
        //intent.putExtra("PARAM", 1);
        PendingIntent penintent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("もやし")    //  タイトル
                .setContentText("タップしてアプリを起動します")    //  メッセージ
                .setContentIntent(penintent)    //  タップされた時の動作
                .setAutoCancel(false)    //  タップしたときに通知バーから消去する場合はtrue
                .setSmallIcon(R.drawable.aicon)   //  左側のアイコン画像
                .build();                       //通知の作成
        //notification.flags = Notification.FLAG_NO_CLEAR;  //通知が本来ならこのコードで固定されるっぽいが動作しない
        //notification.flags = Notification.FLAG_ONGOING_EVENT; //こっちも無理

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, notification);    //引数は適当に
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mR.close();
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        onDate = (year + "/" + (month + 1) +  "/" + dayOfMonth);
        sEAyear = String.valueOf(year);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {startActivity(new Intent(MainActivity.this,ScheduleEditActivity.class));}
        });

        Date date = new Date(year-1900,month,dayOfMonth);


        date.setHours(00);
        date.setMinutes(00);
        date.setSeconds(00);


        RealmQuery<Schedule> rQ = mR.where(Schedule.class);


        System.out.println("scheduleDBの中身" + mR.where(Schedule.class).findAll());

        rQ.equalTo("date",date);

        System.out.println("scheduleDBの検索結果" + rQ.findAll());
        schedules = rQ.findAll();

        adapter = new ScheduleAdapter(schedules,cv);
        mLV.setAdapter(adapter);
    }
}
