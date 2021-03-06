package com.example.funsdkdemo.devices;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.common.DialogInputPasswd;
import com.example.common.UIFactory;
import com.example.funsdkdemo.ActivityDemo;
import com.example.funsdkdemo.R;
import com.lib.FunSDK;
import com.lib.funsdk.support.FunDevicePassword;
import com.lib.funsdk.support.FunError;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.OnFunDeviceOptListener;
import com.lib.funsdk.support.config.OPPTZControl;
import com.lib.funsdk.support.config.OPPTZPreset;
import com.lib.funsdk.support.config.SystemInfo;
import com.lib.funsdk.support.models.FunDevType;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.models.FunStreamType;
import com.lib.funsdk.support.utils.MyUtils;
import com.lib.funsdk.support.widget.FunVideoView;
import com.lib.sdk.struct.H264_DVR_FILE_DATA;

import static com.lib.funsdk.support.models.FunDevType.EE_DEV_SPORTCAMERA;

/**
 * Demo: 监控类设备播放控制等
 */
@SuppressLint("ClickableViewAccessibility")
public class ActivityGuideDeviceCamera
        extends ActivityDemo
        implements OnClickListener, OnFunDeviceOptListener, OnPreparedListener,  OnErrorListener,  OnInfoListener {

    private RelativeLayout mLayoutTop = null;

    private FunDevice mFunDevice = null;

    private RelativeLayout mLayoutVideoWnd = null;
    private FunVideoView mFunVideoView = null;
    private LinearLayout mVideoControlLayout = null;
    private TextView mTextStreamType = null;

    private Button mBtnScreenRatio = null;
    private ImageButton mBtnCaptureOn=null;
    private ImageButton mBtnCaptureMulti=null;
    private LinearLayout mLayoutControls = null;

    private TextView mTextVideoStat = null;

    private int mChannelCount;
    private boolean isGetSysFirst = true;

    private static final int MESSAGE_PLAY_MEDIA = 0x100;
    private static final int MESSAGE_AUTO_HIDE_CONTROL_BAR = 0x102;
    private static final int MESSAGE_TOAST_SCREENSHOT_PREVIEW = 0x103;
    private static final int MESSAGE_OPEN_VOICE = 0x104;

    // 自动隐藏底部的操作控制按钮栏的时间
    private static final int AUTO_HIDE_CONTROL_BAR_DURATION = 10000;

    private boolean mCanToPlay = false;

    public String NativeLoginPsw; //本地密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_camera);
        int devId = getIntent().getIntExtra("FUN_DEVICE_ID", 0);
        mFunDevice = FunSupport.getInstance().findDeviceById(devId);
        if (null == mFunDevice) {
            finish();
            return;
        }
        //setup top bar event handler
        mLayoutTop = (RelativeLayout) findViewById(R.id.layoutTop);
        TextView mTextTitle = (TextView) findViewById(R.id.textViewInTopLayout);
        ImageButton mBtnBack = (ImageButton) findViewById(R.id.backBtnInTopLayout);
        mBtnBack.setOnClickListener(this);

        //setup video window event handler
        mLayoutVideoWnd = (RelativeLayout) findViewById(R.id.layoutPlayWnd);
        Button mBtnPlay = (Button) findViewById(R.id.btnPlay);
        Button mBtnStop = (Button) findViewById(R.id.btnStop);
        Button mBtnStream = (Button) findViewById(R.id.btnStream);
        Button mBtnCapture = (Button) findViewById(R.id.btnCapture);
        Button mBtnRecord = (Button) findViewById(R.id.btnRecord);
        mBtnScreenRatio = (Button) findViewById(R.id.btnScreenRatio);

        mBtnPlay.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnStream.setOnClickListener(this);
        mBtnCapture.setOnClickListener(this);
        mBtnRecord.setOnClickListener(this);
        mBtnScreenRatio.setOnClickListener(this);

        mTextVideoStat = (TextView) findViewById(R.id.textVideoStat);

        RelativeLayout mBtnViewSnapLayout = (RelativeLayout) findViewById(R.id.btnViewSnapLayout);

        ImageButton mBtnViewSnap = (ImageButton) findViewById(R.id.btn_view_snap);
        mBtnCaptureOn = (ImageButton) findViewById(R.id.btn_capture_one);
        mBtnCaptureMulti = (ImageButton) findViewById(R.id.btn_capture_multi);

        mBtnViewSnapLayout.setOnClickListener(this);
        mBtnViewSnap.setOnClickListener(this);

        mBtnCaptureOn.setOnClickListener(this);
        mBtnCaptureMulti.setOnTouchListener(mIntercomTouchLs);

        mLayoutControls = (LinearLayout) findViewById(R.id.functionControlLayout);

        mFunVideoView = (FunVideoView) findViewById(R.id.funVideoView);
        if (mFunDevice.devType == FunDevType.EE_DEV_LAMP_FISHEYE) {
            // 鱼眼灯泡,设置鱼眼效果
            mFunVideoView.setFishEye(true);
        }

        mFunVideoView.setOnTouchListener(new OnVideoViewTouchListener());
        mFunVideoView.setOnPreparedListener(this);
        mFunVideoView.setOnErrorListener(this);
        mFunVideoView.setOnInfoListener(this);
        mVideoControlLayout = (LinearLayout) findViewById(R.id.layoutVideoControl);

        mTextStreamType = (TextView) findViewById(R.id.textStreamStat);

        setNavagateRightButton(R.layout.imagebutton_settings);
        ImageButton mBtnSetup = (ImageButton) findViewById(R.id.btnSettings);
        mBtnSetup.setOnClickListener(this);

        // 注册设备操作回调
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);


        mTextTitle.setText(mFunDevice.devName);

        // 允许横竖屏切换
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        showVideoControlBar();

        mCanToPlay = false;

        // 如果设备未登录,先登录设备
        if (!mFunDevice.hasLogin() || !mFunDevice.hasConnected()) {
            loginDevice();
        } else {
            requestSystemInfo();
        }
    }


    @Override
    protected void onDestroy() {
        stopMedia();

        FunSupport.getInstance().removeOnFunDeviceOptListener(this);

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mCanToPlay) {
            playRealMedia();
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        stopMedia();
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        // 如果当前是横屏，返回时先回到竖屏
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 检测屏幕的方向：纵向或横向
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // 当前为横屏， 在此处添加额外的处理代码
            showAsLandscape();
        } else if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            // 当前为竖屏， 在此处添加额外的处理代码
            showAsPortrait();
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() >= 1000 && v.getId() < 1000 + mChannelCount) {
            mFunDevice.CurrChannel = v.getId() - 1000;
            mFunVideoView.stopPlayback();
            playRealMedia();
        }
        switch (v.getId()) {
            case 1101: {
                startDevicesPreview();
            }
            break;
            case R.id.backBtnInTopLayout: {
                // 返回/退出
                onBackPressed();
            }
            break;
            case R.id.btnPlay: // 开始播放
            {
                mFunVideoView.stopPlayback();
                mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_MEDIA, 1000);
            }
            break;
            case R.id.btnStop: // 停止播放
            {
                stopMedia();
            }
            break;
            case R.id.btnStream: // 切换码流
            {
                switchMediaStream();
            }
            break;
            case R.id.btnCapture: // 截图
            {
                tryToCapture();
            }
            break;
            case R.id.btn_capture_one: // 远程设备图像列表
            {
                tryToCapture();
            }
            break;
            case R.id.btn_view_snap:
            {
                startPictureList();
            }
            break;
            case R.id.btnScreenRatio: // 横竖屏切换
            {
                switchOrientation();
            }
            break;
            case R.id.btnSettings: // 系统设置/系统信息
            {
                startDeviceSetup();
            }
            break;
        }
    }



    /**
     * 视频截图,并延时一会提示截图对话框
     */
    private void tryToCapture() {
        if (!mFunVideoView.isPlaying()) {
            showToast(R.string.media_capture_failure_need_playing);
            return;
        }
        mBtnCaptureOn.setEnabled(false);
        final String path = mFunVideoView.captureImage(null);    //图片异步保存
        //showToast(path);
        if (!TextUtils.isEmpty(path)) {
            Message message = new Message();
            message.what = MESSAGE_TOAST_SCREENSHOT_PREVIEW;
            message.obj = path;
            mHandler.sendMessageDelayed(message, 200);            //此处延时一定时间等待图片保存完成后显示，也可以在回调成功后显示
        }
    }


    /**
     * 显示截图成功对话框
     */
    private void toastScreenShotPreview(final String path) {
        /*
        File file = new File(path);
        File imgPath = new File(FunPath.PATH_PHOTO + File.separator
                + file.getName());
        if (imgPath.exists()) {
            showToast(R.string.device_socket_capture_exist);
        } else {
            FileUtils.copyFile(path, FunPath.PATH_PHOTO + File.separator
                    + file.getName());
            file.delete();
            showToast(R.string.device_socket_capture_save_success);
        }
        */
        showToast(R.string.device_socket_capture_save_success);
        mBtnCaptureOn.setEnabled(true);
        /*
        View view = getLayoutInflater().inflate(R.layout.screenshot_preview, null, false);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_screenshot_preview);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        iv.setImageBitmap(bitmap);
        CharSequence sequence= getText(R.string.device_socket_capture_preview);
        StringBuilder title=new StringBuilder(sequence);
        title.append("[").append(path).append("]");
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.device_socket_capture_save,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(path);
                                File imgPath = new File(FunPath.PATH_PHOTO + File.separator
                                        + file.getName());
                                if (imgPath.exists()) {
                                    showToast(R.string.device_socket_capture_exist);
                                } else {
                                    FileUtils.copyFile(path, FunPath.PATH_PHOTO + File.separator
                                            + file.getName());
                                    showToast(R.string.device_socket_capture_save_success);
                                }
                            }
                        })
                .setNegativeButton(R.string.device_socket_capture_delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FunPath.deleteFile(path);
                                showToast(R.string.device_socket_capture_delete_success);
                            }
                        })
                .show();
      */
    }

    private void showVideoControlBar() {
        if (mVideoControlLayout.getVisibility() != View.VISIBLE) {
            TranslateAnimation ani = new TranslateAnimation(0, 0, UIFactory.dip2px(this, 42), 0);
            ani.setDuration(200);
            mVideoControlLayout.startAnimation(ani);
            mVideoControlLayout.setVisibility(View.VISIBLE);
        }

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏情况下,顶部标题栏也动画显示
            TranslateAnimation ani = new TranslateAnimation(0, 0, -UIFactory.dip2px(this, 48), 0);
            ani.setDuration(200);
            mLayoutTop.startAnimation(ani);
            mLayoutTop.setVisibility(View.VISIBLE);
        } else {
            mLayoutTop.setVisibility(View.VISIBLE);
        }

        // 显示后设置10秒后自动隐藏
        mHandler.removeMessages(MESSAGE_AUTO_HIDE_CONTROL_BAR);
        mHandler.sendEmptyMessageDelayed(MESSAGE_AUTO_HIDE_CONTROL_BAR, AUTO_HIDE_CONTROL_BAR_DURATION);
    }

    private void hideVideoControlBar() {
        if (mVideoControlLayout.getVisibility() != View.GONE) {
            TranslateAnimation ani = new TranslateAnimation(0, 0, 0, UIFactory.dip2px(this, 42));
            ani.setDuration(200);
            mVideoControlLayout.startAnimation(ani);
            mVideoControlLayout.setVisibility(View.GONE);
        }

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏情况下,顶部标题栏也隐藏
            TranslateAnimation ani = new TranslateAnimation(0, 0, 0, -UIFactory.dip2px(this, 48));
            ani.setDuration(200);
            mLayoutTop.startAnimation(ani);
            mLayoutTop.setVisibility(View.GONE);
        }

        // 隐藏后清空自动隐藏的消息
        mHandler.removeMessages(MESSAGE_AUTO_HIDE_CONTROL_BAR);
    }

    private void showAsLandscape() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 隐藏底部的控制按钮区域
        mLayoutControls.setVisibility(View.GONE);

        // 视频窗口全屏显示
        RelativeLayout.LayoutParams lpWnd = (RelativeLayout.LayoutParams) mLayoutVideoWnd.getLayoutParams();
        lpWnd.height = LayoutParams.MATCH_PARENT;
        // lpWnd.removeRule(RelativeLayout.BELOW);
        lpWnd.topMargin = 0;
        mLayoutVideoWnd.setLayoutParams(lpWnd);

        // 上面标题半透明背景
        mLayoutTop.setBackgroundColor(0x40000000);

        mBtnScreenRatio.setText(R.string.device_opt_smallscreen);
    }

    private void showAsPortrait() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 还原上面标题栏背景
        mLayoutTop.setBackgroundColor(getResources().getColor(R.color.theme_color));
        mLayoutTop.setVisibility(View.VISIBLE);

        // 视频显示为小窗口
        RelativeLayout.LayoutParams lpWnd = (RelativeLayout.LayoutParams) mLayoutVideoWnd.getLayoutParams();
        lpWnd.height = UIFactory.dip2px(this, 240);
        lpWnd.topMargin = UIFactory.dip2px(this, 48);
        // lpWnd.addRule(RelativeLayout.BELOW, mLayoutTop.getId());
        mLayoutVideoWnd.setLayoutParams(lpWnd);

        // 显示底部的控制按钮区域
        mLayoutControls.setVisibility(View.VISIBLE);

        mBtnScreenRatio.setText(R.string.device_opt_fullscreen);
    }

    /**
     * 切换视频全屏/小视频窗口(以切横竖屏切换替代)
     */
    private void switchOrientation() {
        // 横竖屏切换
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 打开设备配置
     */
    private void startDeviceSetup() {
        Intent intent = new Intent();
        intent.putExtra("FUN_DEVICE_ID", mFunDevice.getId());
        intent.setClass(this, ActivityGuideDeviceSetup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /***
     * 打开 多通道预览
     */
    private void startDevicesPreview() {
        Intent intent = new Intent();
        intent.putExtra("FUNDEVICE_ID", mFunDevice.getId());
        intent.setClass(this, ActivityGuideDevicePreview.class);
        startActivityForResult(intent, 0);
    }

    private class OnVideoViewTouchListener implements OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 显示或隐藏视频操作菜单
                if (mVideoControlLayout.getVisibility() == View.VISIBLE) {
                    hideVideoControlBar();
                } else {
                    showVideoControlBar();
                }
            }
            return false;
        }
    }

    private void loginDevice() {
        showWaitDialog();
        FunSupport.getInstance().requestDeviceLogin(mFunDevice);
    }

    private void requestSystemInfo() {
        showWaitDialog();
        FunSupport.getInstance().requestDeviceConfig(mFunDevice, SystemInfo.CONFIG_NAME);
    }

    // 获取设备预置点列表
    private void requestPTZPreset() {
        FunSupport.getInstance().requestDeviceConfig(mFunDevice, OPPTZPreset.CONFIG_NAME, 0);
    }

    private void startPictureList() {
        Intent intent = new Intent();
        intent.putExtra("FUN_DEVICE_ID", mFunDevice.getId());
        intent.putExtra("FILE_TYPE", "jpg");
        if (mFunDevice.devType == EE_DEV_SPORTCAMERA) {
            intent.setClass(this, ActivityGuideDeviceSportPicList.class);
        } else {
            intent.setClass(this, ActivityGuideDevicePictureList.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void playRealMedia() {

        // 显示状态: 正在打开视频...
        mTextVideoStat.setText(R.string.media_player_opening);
        mTextVideoStat.setVisibility(View.VISIBLE);

        if (mFunDevice.isRemote) {
            mFunVideoView.setRealDevice(mFunDevice.getDevSn(), mFunDevice.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            mFunVideoView.setRealDevice(deviceIp, mFunDevice.CurrChannel);
        }

        // 打开声音
        mFunVideoView.setMediaSound(true);

        // 设置当前播放的码流类型
        if (FunStreamType.STREAM_SECONDARY == mFunVideoView.getStreamType()) {
            mTextStreamType.setText(R.string.media_stream_secondary);
        } else {
            mTextStreamType.setText(R.string.media_stream_main);
        }
    }

    // 添加通道选择按钮
    @SuppressWarnings("ResourceType")
    private void addChannelBtn(int channelCount) {

        int m = UIFactory.dip2px(this, 5);
        int p = UIFactory.dip2px(this, 3);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParamsT = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsT.setMargins(m, m, m, m);
        textView.setLayoutParams(layoutParamsT);
        textView.setText(R.string.device_opt_channel);
        textView.setTextSize(UIFactory.dip2px(this, 10));
        textView.setTextColor(getResources().getColor(R.color.theme_color));

        Button bt = new Button(this);
        bt.setId(1101);
        bt.setTextColor(getResources().getColor(R.color.theme_color));
        bt.setPadding(p, p, p, p);
        bt.setLayoutParams(layoutParamsT);
        bt.setText(R.string.device_camera_channels_preview_title);
        bt.setOnClickListener(this);

        for (int i = 0; i < channelCount; i++) {
            Button btn = new Button(this);
            btn.setId(1000 + i);
            btn.setTextColor(getResources().getColor(R.color.theme_color));
            btn.setPadding(p, p, p, p);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(UIFactory.dip2px(this, 40),
                    UIFactory.dip2px(this, 40));
            layoutParams.setMargins(m, m, m, m);
            btn.setLayoutParams(layoutParams);
            btn.setText(String.valueOf(i));
            btn.setOnClickListener(this);
        }

    }

    private void stopMedia() {
        if (null != mFunVideoView) {
            mFunVideoView.stopPlayback();
            mFunVideoView.stopRecordVideo();
        }
    }

    private void switchMediaStream() {
        if (null != mFunVideoView) {
            if (FunStreamType.STREAM_MAIN == mFunVideoView.getStreamType()) {
                mFunVideoView.setStreamType(FunStreamType.STREAM_SECONDARY);
            } else {
                mFunVideoView.setStreamType(FunStreamType.STREAM_MAIN);
            }
            // 重新播放
            mFunVideoView.stopPlayback();
            playRealMedia();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PLAY_MEDIA: {
                    playRealMedia();
                }
                break;
                case MESSAGE_AUTO_HIDE_CONTROL_BAR: {
                    hideVideoControlBar();
                }
                break;
                case MESSAGE_TOAST_SCREENSHOT_PREVIEW: {
                    String path = (String) msg.obj;
                    toastScreenShotPreview(path);
                }
                break;
                case MESSAGE_OPEN_VOICE: {
                    mFunVideoView.setMediaSound(true);
                }
            }
        }
    };

    private OnTouchListener mIntercomTouchLs = new OnTouchListener() {

        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            try {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    startMultiCapture();
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    stopMultiCaptureCapture(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    void startMultiCapture(){

    }
    void stopMultiCaptureCapture(int timeout){

    }

    /**
     * 显示输入设备密码对话框
     */
    private void showInputPasswordDialog() {
        DialogInputPasswd inputDialog = new DialogInputPasswd(this,
                getResources().getString(R.string.device_login_input_password), "", R.string.common_confirm,
                R.string.common_cancel) {

            @Override
            public boolean confirm(String editText) {
                // 重新以新的密码登录
                if (null != mFunDevice) {
                    NativeLoginPsw = editText;

                    onDeviceSaveNativePws();

                    // 重新登录
                    loginDevice();
                }
                return super.confirm(editText);
            }

            @Override
            public void cancel() {
                super.cancel();

                // 取消输入密码,直接退出
                finish();
            }

        };

        inputDialog.show();
    }

    public void onDeviceSaveNativePws() {
        FunDevicePassword.getInstance().saveDevicePassword(mFunDevice.getDevSn(),
                NativeLoginPsw);
        // 库函数方式本地保存密码
        if (FunSupport.getInstance().getSaveNativePassword()) {
            FunSDK.DevSetLocalPwd(mFunDevice.getDevSn(), "admin", NativeLoginPsw);
            // 如果设置了使用本地保存密码，则将密码保存到本地文件
        }
    }

    @Override
    public void onDeviceLoginSuccess(final FunDevice funDevice) {
        System.out.println("TTT---->>>> loginsuccess");

        if (null != mFunDevice && null != funDevice) {
            if (mFunDevice.getId() == funDevice.getId()) {

                // 登录成功后立刻获取SystemInfo
                // 如果不需要获取SystemInfo,在这里播放视频也可以:playRealMedia();
                requestSystemInfo();
            }
        }
    }

    @Override
    public void onDeviceLoginFailed(final FunDevice funDevice, final Integer errCode) {
        // 设备登录失败
        hideWaitDialog();
        showToast(FunError.getErrorStr(errCode));

        // 如果账号密码不正确,那么需要提示用户,输入密码重新登录
        if (errCode == FunError.EE_DVR_PASSWORD_NOT_VALID) {
            showInputPasswordDialog();
        }
    }

    @Override
    public void onDeviceGetConfigSuccess(final FunDevice funDevice, final String configName, final int nSeq) {
        int channelCount;
        if (SystemInfo.CONFIG_NAME.equals(configName)) {

            if (!isGetSysFirst) {
                return;
            }

            // 更新UI
            //此处为示例如何取通道信息，可能会增加打开视频的时间，可根据需求自行修改代码逻辑
            if (funDevice.channel == null) {
                FunSupport.getInstance().requestGetDevChnName(funDevice);
                requestSystemInfo();
                return;
            }
            channelCount = funDevice.channel.nChnCount;
            // if (channelCount >= 5) {
            // channelCount = 5;
            // }
            if (channelCount > 1) {
                mChannelCount = channelCount;

                addChannelBtn(channelCount);
            }

            hideWaitDialog();

            // 设置允许播放标志
            mCanToPlay = true;

            isGetSysFirst = false;

            showToast(getType(funDevice.getNetConnectType()));

            // 获取信息成功后,如果WiFi连接了就自动播放
            // 此处逻辑客户自定义
            if (MyUtils.detectWifiNetwork(this)) {
                playRealMedia();
            } else {
                showToast(R.string.meida_not_auto_play_because_no_wifi);
            }

            // 如果支持云台控制,在获取到SystemInfo之后,获取预置点信息,如果不需要云台控制/预置点功能功能,可忽略之
            if (mFunDevice.isSupportPTZ()) {
                requestPTZPreset();
            }
        } else if (OPPTZControl.CONFIG_NAME.equals(configName)) {
            Toast.makeText(getApplicationContext(), R.string.user_set_preset_succeed, Toast.LENGTH_SHORT).show();

            // 重新获取预置点列表
            requestPTZPreset();
        }
    }

    private String getType(int i) {
        switch (i) {
            case 0:
                return "P2P";
            case 1:
                return "DSS";
            case 2:
                return "IP";
            case 5:
                return "RPS";
            default:
                return "";
        }
    }

    @Override
    public void onDeviceGetConfigFailed(final FunDevice funDevice, final Integer errCode) {
        showToast(FunError.getErrorStr(errCode));
    }


    @Override
    public void onDeviceSetConfigSuccess(final FunDevice funDevice,
                                         final String configName) {

    }


    @Override
    public void onDeviceSetConfigFailed(final FunDevice funDevice,
                                        final String configName, final Integer errCode) {
        if (OPPTZControl.CONFIG_NAME.equals(configName)) {
            Toast.makeText(getApplicationContext(), R.string.user_set_preset_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeviceChangeInfoSuccess(final FunDevice funDevice) {
    }

    @Override
    public void onDeviceChangeInfoFailed(final FunDevice funDevice, final Integer errCode) {
    }

    @Override
    public void onDeviceOptionSuccess(final FunDevice funDevice, final String option) {
    }

    @Override
    public void onDeviceOptionFailed(final FunDevice funDevice, final String option, final Integer errCode) {
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice) {
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice, H264_DVR_FILE_DATA[] datas) {
    }


    @Override
    public void onPrepared(MediaPlayer arg0) {
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 播放失败
        showToast(getResources().getString(R.string.media_play_error)
                + " : "
                + FunError.getErrorStr(extra));

        if (FunError.EE_TPS_NOT_SUP_MAIN == extra
                || FunError.EE_DSS_NOT_SUP_MAIN == extra) {
            // 不支持高清码流,设置为标清码流重新播放
            if (null != mFunVideoView) {
                mFunVideoView.setStreamType(FunStreamType.STREAM_SECONDARY);
                playRealMedia();
            }
        }

        return true;
    }


    @Override
    public boolean onInfo(MediaPlayer arg0, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            mTextVideoStat.setText(R.string.media_player_buffering);
            mTextVideoStat.setVisibility(View.VISIBLE);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            mTextVideoStat.setVisibility(View.GONE);
        }
        return true;
    }


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        mFunDevice.CurrChannel = arg1;
        System.out.println("TTTT----" + mFunDevice.CurrChannel);
        if (mCanToPlay) {
            playRealMedia();
        }
    }


    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {
    }
}
