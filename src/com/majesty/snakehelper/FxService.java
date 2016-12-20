package com.majesty.snakehelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FxService extends Service {

    String TAG = "MAJESTY_TAG";

    // ���帡�����ڲ���
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    // ���������������ò��ֲ����Ķ���
    WindowManager mWindowManager;
    DisplayMetrics metric = new DisplayMetrics();

    // ��Ҫ�ؼ�
    Button btn_start, btn_stop;
    TextView tv_status = null;

    Timer timer = null;
    TimerTask task = null;
    int run_count = 0;
    Runtime runtime;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatView() {
        
        wmParams = new WindowManager.LayoutParams();
        // ��ȡWindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        // ����window type
        wmParams.type = LayoutParams.TYPE_PHONE;

        // ����ͼƬ��ʽ��Ч��Ϊ����͸��
        wmParams.format = PixelFormat.RGBA_8888;
        // �����ڿ��Ի�ý��㣨û������ FLAG_NOT_FOCUSALBE
        // ѡ�ʱ����Ȼ�����ڷ�Χ֮��ĵ��豸�¼�����ꡢ�����������͸�����Ĵ��ڴ���
        // ����������ռ���еĵ��豸�¼��������������ǲ��Ƿ����ڴ��ڷ�Χ��
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;// FLAG_NOT_TOUCH_MODAL;

        // 放到右下角去
        wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        // 包裹控件就可以了
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        // mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        // 绑定控件
        btn_start = (Button) mFloatLayout.findViewById(R.id.btn_start);
        btn_stop = (Button) mFloatLayout.findViewById(R.id.btn_stop);
        tv_status = (TextView) mFloatLayout.findViewById(R.id.tv_status);

        // 开始事件
        btn_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                run_count = 0;
                // 启动定时器
                timer = new Timer();
                // 定时任务
                task = new TimerTask() {
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                };
                timer.schedule(task, 0, 200);
                tv_status.setText("开始运行~");
                btn_start.setEnabled(false);
            }
        });
        // 结束
        btn_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                tv_status.setText("已停止~");
                btn_start.setEnabled(true);
            }
        });
        runtime = Runtime.getRuntime();
        try {
            runtime.exec("/system/bin/su");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "density is : " + getResources().getDisplayMetrics().density);
    } // end of create view

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    // 真正要做的事情
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                tv_status.setText("第" + (run_count++) + "次运行~");
                try {
                    tackAction(0, (run_count * 30) % 360);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    // 操作封装
    // ACT:
    // 0=move
    // 1=speedup
    // 2=
    // angle:
    // 0~360
    private void tackAction(int ACT, int anger_degree) throws Exception {

        double center_x = 240.0;
        double center_y = 840.0;
        double r1 = 90.0;
        double r2 = 140.0;
        double anger_radians = anger_degree * 2 * Math.PI / 360.0;

        switch (ACT) {
        case 0:
            long start_time = System.currentTimeMillis();
            int s_x = (int) (center_x + r1 * Math.cos(anger_radians));
            int s_y = (int) (center_y + r1 * Math.sin(anger_radians));
            int e_x = (int) (center_x + r2 * Math.cos(anger_radians));
            int e_y = (int) (center_y + r2 * Math.sin(anger_radians));

            String command = "input swipe " + s_x + " " + s_y + " " + e_x + " " + e_y + " 100";
            Log.d("in tackAction", command);
            execShellCmd(command);
            Log.d("in tackAction", "time(ms):" + (System.currentTimeMillis() - start_time));

            break;
        case 1:
            // runtime.exec("input touchscreen tap 323 240");
            break;
        case 2:
            // runtime.exec("input touchscreen tap 200 600");
            break;

        default:
            break;
        }

    }

    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    /*
     * private void doKey(final int eventCode) { new Thread(new Runnable() {
     * public void run() { long now = SystemClock.uptimeMillis(); try { KeyEvent
     * down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, eventCode, 0);
     * KeyEvent up = new KeyEvent(now, now, KeyEvent.ACTION_UP, eventCode, 0);
     * mWindowManager.injectKeyEvent(down, true);
     * mWindowManager.injectKeyEvent(up, true); } catch (RemoteException e) {
     * Log.d("Input", "DeadOjbectException"); } } }).start(); }
     */

    // 读图片
    private void GetandSaveCurrentImage() {
        // 1.构建Bitmap
        Display display = mWindowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();

        Bitmap Bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);

        // 2.获取屏幕
        View decorview = null;// this.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);
        Bmp = decorview.getDrawingCache();
        String SavePath = getSDCardPath() + "/PrintScreenDemo/ScreenImage";

        // 3.保存Bitmap
        try {
            File path = new File(SavePath);
            // 文件
            String filepath = SavePath + "/Screen_" + 1 + ".png";
            File file = new File(filepath);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();

                Toast.makeText(this, "截屏文件已保存至SDCard/PrintScreenDemo/ScreenImage/下", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSDCardPath() {
        File sdcardDir = null;
        // 判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }

        return sdcardDir.toString();
    }
}
