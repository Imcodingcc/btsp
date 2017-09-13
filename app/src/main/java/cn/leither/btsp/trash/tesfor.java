package cn.leither.btsp.trash;

import android.app.Activity;
import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.leither.btsp.adapter.CommonAdapter;

/**
 * Created by lvqiang on 17-8-31.
 */

public class tesfor{
    public static void main(final String[] args){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (args){
                        args.wait(190);
                    }
                } catch (InterruptedException e) {
                    System.out.print("Wrong");
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static void a(){
        int a = 1;
    }
}
