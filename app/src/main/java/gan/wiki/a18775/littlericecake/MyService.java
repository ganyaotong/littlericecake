package gan.wiki.a18775.littlericecake;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class MyService extends Service {
    private CountDownTimer cdt = null;
    private Messenger serviceMessenger=null;
    private Messenger activityMessenger=null;
    private Activity activity= null;
    private int time = 0;
    private int htime = 600;
    private int ctime = 1200;
    private int ringcount = 0;
    private Ringtone ring = null;
    private MediaPlayer mediaPlayer = null;
    NotificationCompat.Builder builder = null;

    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;


    public MyService() {


    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    activityMessenger = msg.replyTo;
                    activity= (Activity) msg.obj;
                    Bundle bundle = new Bundle();
                    bundle = msg.getData();
                    time = bundle.getInt("time");
                    countdown();
                    break;
                case 2:
                    playring();
                    break;
                case 3:
                    //wakeAndUnlock(false);
                    mediaPlayer.pause();
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public void playring(){
        ringcount++;
        Log.e("ringcount", String.valueOf(ringcount));
        if (ringcount==htime){
            /*
            Intent intent = new Intent(this,Main3Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION );
            this.startActivity(intent);
            */
            wakeAndUnlock(true);
            mediaPlayer.start();
            Log.e("playring","600");
        }
        if (ringcount==ctime-2){
            wakeAndUnlock(true);
            mediaPlayer.start();
            Log.e("playring","1200");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        serviceMessenger = new Messenger(handler);
        return serviceMessenger.getBinder();
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public void countdown(){
        //TODO：move countdowntimer to Myservice
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String ringtone = sharedPreferences.getString("notifications_new_message_ringtone","DEFAULT");
        mediaPlayer = MediaPlayer.create(this,Uri.parse(ringtone));
        //ring = RingtoneManager.getRingtone(activity, Uri.parse(ringtone));
        //ring.play();
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        notibar();

        gencount();
        runcount();
    }

    //TODO: generate countdown
    public void gencount(){
        //time: 20min
        int  t=  1000*60*time;
        cdt = new CountDownTimer(t,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                Message message = new Message();
                message.what=2;
                Bundle bundle = new Bundle();
                bundle.putString("time",""+millisUntilFinished/1000);
                message.setData(bundle);
                builder.setSubText("剩下（秒）："+millisUntilFinished/1000);

                playring();
                try {
                    activityMessenger.send(message);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("service count:",""+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                Message message = new Message();
                message.what=3;
                ringcount=0;
                try {
                    activityMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //TODO: running countdown
    public void runcount(){
        cdt.start();
    }

    public void notibar(){
        builder = new NotificationCompat.Builder(activity,"a")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("砵儿果正在准备")
                .setContentTitle("砵儿果")
                .setContentInfo("砵儿果正在准备")
                .setSubText("剩下（秒）：")
                .setContentText("砵儿果正在准备");
        Intent intent = new Intent(activity,FinishActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setLights(Color.BLUE,500,500);
        long[] pattern={500,500,500,500,500,500,500,500,500};
        builder.setVibrate(pattern);
        builder.setStyle(new NotificationCompat.InboxStyle());
        //RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.layout);
        //builder.setContent(remoteViews);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }

    private void wakeAndUnlock(boolean b)
    {
        if(b)
        {
            //获取电源管理器对象
            pm=(PowerManager) getSystemService(Context.POWER_SERVICE);

            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire();

            //得到键盘锁管理器对象
            /*
            km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");
            //解锁
            kl.disableKeyguard();
            */
        }
        else
        {
            //锁屏
            //kl.reenableKeyguard();
            //释放wakeLock，关灯
            wl.release();
        }

    }
}
