package gan.wiki.a18775.littlericecake;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button startSteam = null;
    private TextView textView = null;
    private ProgressBar tbar = null;
    private ProgressBar pbar1 = null;
    private ProgressBar pbar2 = null;
    private int time = 20; //min
    private int htime = time*30;
    private int ctime = time*60;
    private int levelt = 1;
    private int level1 = 1;
    private int level2 = 1;

    private Messenger serviceMessenger = null;
    private Messenger activityMessenger = null;
    private ServiceConnection serviceConnection = null;



    private TextView tnv = null;
    private TextView l1nv = null;
    private TextView l2nv = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: steam countdown
        startSteam = (Button) findViewById(R.id.button);
        startSteam.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what=1;
                handler.sendMessage(msg);
            }
        });

        textView = (TextView) findViewById(R.id.textView);
        int radius = 15;


        tbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar1 = (ProgressBar) findViewById(R.id.progressBar2);
        pbar2 = (ProgressBar) findViewById(R.id.progressBar3);

        tnv =(TextView) findViewById(R.id.textView7);
        l1nv = (TextView) findViewById(R.id.textView5);
        l2nv = (TextView) findViewById(R.id.textView6);


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceMessenger = new Messenger(service);
                activityMessenger = new Messenger(handler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(this,MyService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);


    }

    public void sendCount(){
        Message msg = Message.obtain();
        msg.what=1;
        msg.replyTo=activityMessenger;
        msg.obj = this;
        Bundle bundle = new Bundle();
        bundle.putInt("time",time);

        msg.setData(bundle);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        if (levelt > htime){
            level1=htime;
            pbar1.setProgress(level1);
            level2 = levelt-htime;
            pbar2.setProgress(levelt);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.setting:
                startActivity(new Intent (this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                         countdown();
                        sendCount();
                    break;
                case 2:
                    Bundle bundle = new Bundle();
                    bundle = msg.getData();
                    changetime(bundle.getString("time"));
                    break;
                case 3:

                    break;
            }
            super.handleMessage(msg);
        }
    };

    //TODO: change textbox
    @SuppressLint({"ResourceAsColor", "LongLogTag"})
    public void changetime(String str){
        textView.setText(str);
        Integer _str =1200- Integer.parseInt(str);


        Log.e("activitytime:", str);
        //TODO：leve1 jump to level2
        levelt++;
        Log.e("activitytime level totle:", String.valueOf(levelt));
        tbar.setProgress(levelt);
        if (levelt <= htime){
            level1++;
            pbar1.setProgress(level1);
        }else{
            level2++;
            pbar2.setProgress(level2);
        }
        //TODO：play level 1 sound
        if (levelt==htime){
            Log.e("play htime:","htime is:"+ htime+", levelt is:"+levelt+", str is:"+str);
            l1nv.setText("已完成");
            showSoundStop("大火","时间已经使用10分钟，点击按钮结束声音！");
            l2nv.setText("进行中");
        }
        //TODO: play level 2 sound
        if (levelt==ctime-1){
            Log.e("play ctime:","ctime is:"+ctime+", levelt is:"+ levelt+", str is:"+str);
            startSteam.setEnabled(true);
            l2nv.setText("已完成");
            showSoundStop("小火","时间已经使用20分钟，点击按钮结束声音！");
            tnv.setText("已完成");
        }

    }
    //TODO:start countdown
    public void countdown(){
        //TODO：init countdown UI;
        initcoutdown();
        startSteam.setEnabled(false);

    }

    @SuppressLint("ResourceAsColor")
    public void initcoutdown(){
        this.tbar.setProgress(1);
        this.tbar.setMax(ctime);
        Log.e("ctime", String.valueOf(ctime));
        this.pbar1.setProgress(1);
        this.pbar1.setMax(htime);
        Log.e("htime",String.valueOf(htime));
        this.pbar2.setProgress(1);
        this.pbar2.setMax(htime);
        this.levelt = 1;
        this.level1 = 1;
        this.level2 = 1;

        tnv.setText("进行中");
        l1nv.setText("进行中");

        showSoundStop("状态","正在开始，点击确认停止声音");
    }

    private void showSoundStop(String title,String msg){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message msg = Message.obtain();
                msg.what=3;
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
        dialog.show();
    }



}
