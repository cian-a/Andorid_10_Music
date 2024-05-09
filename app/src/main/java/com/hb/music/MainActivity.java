package com.hb.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static SeekBar sb; // 进度条
    MusicService.MusicControl musicControl; // 控制音乐播放的Binder对象
    private static TextView tv_progress, tv_total; // 显示当前播放进度和总时长的TextView
    Intent intent; // 启动Service的Intent
    MyServiceConn conn; // Service连接对象
    private boolean isUnbind = false; // 是否已解绑Service的标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(); // 初始化界面和相关对象
    }

    private void init() {
        tv_progress = findViewById(R.id.tv_progress); // 获取进度显示TextView
        tv_total = findViewById(R.id.tv_total); // 获取总时长显示TextView
        sb = findViewById(R.id.sb); // 获取进度条
        findViewById(R.id.bt_play).setOnClickListener(this); // 设置播放按钮点击事件
        findViewById(R.id.bt_pause).setOnClickListener(this); // 设置暂停按钮点击事件
        findViewById(R.id.bt_continue).setOnClickListener(this); // 设置继续播放按钮点击事件
        findViewById(R.id.bt_quit).setOnClickListener(this); // 设置退出按钮点击事件
        intent = new Intent(this, MusicService.class); // 创建启动Service的Intent
        conn = new MyServiceConn(); // 创建Service连接对象
        bindService(intent, conn, BIND_AUTO_CREATE); // 绑定Service
        // 设置进度条滑动监听器
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == seekBar.getMax()) {
                    // 当滑动条滑到末端时，结束动画
                    // animator.pause(); //停止播放动画
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 滑动条开始滑动时调用，暂时不需要处理
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 滑动条停止滑动时调用，根据拖动的进度改变音乐播放进度
                int progress = seekBar.getProgress(); // 获取seekBar的进度
                musicControl.seekTo(progress); // 改变播放进度
            }
        });
    }

    // Handler处理从Service接收到的消息，更新UI
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration"); // 总时长
            int currentPosition = bundle.getInt("currentPosition"); // 当前播放进度
            sb.setMax(duration); // 设置进度条最大值为总时长
            sb.setProgress(currentPosition); // 设置进度条当前进度为当前播放进度
            // 格式化显示总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            String strMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);
            String strSecond = (second < 10) ? "0" + second : String.valueOf(second);
            tv_total.setText(strMinute + ":" + strSecond);
            // 格式化显示当前播放进度
            minute = currentPosition / 1000 / 60;
            second = currentPosition / 1000 % 60;
            strMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);
            strSecond = (second < 10) ? "0" + second : String.valueOf(second);
            tv_progress.setText(strMinute + ":" + strSecond);
        }
    };

    // Service连接对象，用于绑定Service和处理连接状态
    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicControl = (MusicService.MusicControl) iBinder; // 获取Service中的Binder对象
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Service断开连接时的操作，暂时不需要处理
        }
    }

    // 在Activity销毁时解绑Service
    protected void onDestroy() {
        super.onDestroy();
        unbind(isUnbind);
    }

    // 解绑Service
    private void unbind(boolean isUnbind) {
        if (!isUnbind) {
            musicControl.pausePlay(); // 暂停音乐播放
            unbindService(conn); // 解绑Service
            stopService(intent); // 停止Service
        }
    }

    // 按钮点击事件处理
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_play:
                musicControl.play(); // 播放音乐
                break;
            case R.id.bt_pause:
                musicControl.pausePlay(); // 暂停音乐播放
                break;
            case R.id.bt_continue:
                musicControl.continuePlay(); // 继续音乐播放
                break;
            case R.id.bt_quit:
                unbind(isUnbind); // 解绑Service
                isUnbind = true; // 标记已解绑
                finish(); // 关闭Activity
                break;
        }
    }
}
