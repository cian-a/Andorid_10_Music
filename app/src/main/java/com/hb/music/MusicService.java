package com.hb.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer; // 媒体播放器对象
    private Timer timer; // 计时器对象用于更新播放进度

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl(); // 返回自定义的Binder对象
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer(); // 初始化媒体播放器
    }

    // 自定义Binder类，用于客户端与Service通信
    class MusicControl extends Binder {
        // 播放音乐
        public void play() {
            mediaPlayer.reset(); // 重置媒体播放器
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.music); // 创建媒体播放器并加载音乐资源
            mediaPlayer.start(); // 开始播放音乐
            addTimer(); // 启动计时器
        }

        // 暂停播放
        public void pausePlay() {
            mediaPlayer.pause();
        }

        // 继续播放
        public void continuePlay() {
            mediaPlayer.start();
        }

        // 跳转至指定进度
        public void seekTo(int progress) {
            mediaPlayer.seekTo(progress);
        }
    }

    // Service销毁时的操作
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop(); // 停止播放音乐
        mediaPlayer.release(); // 释放资源
        mediaPlayer = null;
    }

    // 添加计时器用于更新播放进度
    public void addTimer() {
        if (timer == null) {
            timer = new Timer(); // 创建计时器对象
            TimerTask task = new TimerTask() {
                @Override
                public void run() { // 创建一个线程
                    if (mediaPlayer == null) return;
                    // 获取歌曲总时长
                    int duration = mediaPlayer.getDuration();
                    // 获取当前播放进度
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    // 创建消息对象
                    Message msg = MainActivity.handler.obtainMessage();
                    // 将音乐的总时长和当前播放进度封装至消息对象中
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    msg.setData(bundle);
                    // 将消息发送到主线程的消息队列
                    MainActivity.handler.sendMessage(msg);
                }
            };
            // 调用Timer对象的schedule()方法执行TimerTask任务
            // 参数说明：1、要执行的任务；2、5毫秒后第一次执行task任务；3、每隔500毫秒执行一次
            timer.schedule(task, 5, 500);
        }
    }
}
