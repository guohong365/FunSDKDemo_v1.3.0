package com.example.funsdkdemo.devices;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.example.funsdkdemo.ActivityGuide;
import com.example.funsdkdemo.DemoModule;
import com.example.funsdkdemo.R;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.config.SystemInfo;
import com.lib.funsdk.support.models.FunDevice;

public class ActivityGuideDeviceSetup extends ActivityGuide {

	private List<DemoModule> mGuideModules = new ArrayList<DemoModule>();

	private FunDevice mFunDevice = null;

	protected void addDemoModules() {
		SystemInfo info = (SystemInfo) mFunDevice.getConfig(SystemInfo.CONFIG_NAME);
		if (info != null && info.getVideoInChannel() > mFunDevice.CurrChannel) {
			// 编码配置
			mGuideModules.add(new DemoModule(-1, R.string.device_setup_encode,
					R.string.device_setup_hint_encode_config_alarm, ActivityGuideDeviceSetupEncode.class));
		}
		// 报警配置
		mGuideModules.add(new DemoModule(-1, R.string.device_opt_alarm_config,
				R.string.device_setup_hint_alarm_config_alarm, ActivityGuideDeviceSetupAlarm.class));

		// 录像配置
		mGuideModules.add(new DemoModule(-1, R.string.device_setup_record,
				R.string.device_setup_hint_record_config_alarm, ActivityGuideDeviceSetupRecord.class));
		if (mFunDevice.channel != null && mFunDevice.channel.nChnCount == 1) {
			// 图像配置
			mGuideModules.add(new DemoModule(-1, R.string.device_setup_image,
					R.string.device_setup_hint_picture_config_alarm, ActivityGuideDeviceSetupCamera.class));
			// 高级配置
			mGuideModules.add(new DemoModule(-1, R.string.device_setup_expert,
					R.string.device_setup_hint_expert_config_alarm, ActivityGuideDeviceSetupExpert.class));
		}

		// 设备存储管理
		mGuideModules.add(new DemoModule(-1, R.string.device_setup_storage,
				R.string.device_setup_hint_harddisk_config_alarm, ActivityGuideDeviceSetupStorage.class));

		// 密码修改
		mGuideModules.add(new DemoModule(-1, R.string.device_setup_change_password,
				R.string.device_setup_hint_pwd_modify_alarm, ActivityGuideDeviceChangePassw.class));

		// 系统功能列表
		mGuideModules.add(
				new DemoModule(-1, R.string.device_setup_system_function, -1, ActivityGuideDeviceSystemFunction.class));
		// 报警中心
		mGuideModules.add(
				new DemoModule(-1, R.string.device_setup_alarm_center, -1, ActivityGuideDeviceSetupAlarmCenter.class));
		
		//Jason和DevCmd调试
		mGuideModules.add(
				new DemoModule(-1, R.string.device_setup_jsonanddevcmd, -1, ActivityGuideDeviceSetupJson.class));
		
		// 关于设备/设备信息
		mGuideModules.add(new DemoModule(-1, R.string.device_system_info, R.string.device_setup_hint_about_dev_alarm,
				ActivityGuideDeviceSystemInfo.class));
	}

	@Override
	protected List<DemoModule> getGuideModules() {
		return mGuideModules;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		int devId = getIntent().getIntExtra("FUN_DEVICE_ID", 0);
		mFunDevice = FunSupport.getInstance().findDeviceById(devId);
		
		addDemoModules();
		
		super.onCreate(savedInstanceState);

		// 设置标题为设备配置
		mTextTitle.setText(R.string.device_setup);
		// 显示返回按钮
		mBtnBack.setVisibility(View.VISIBLE);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		try {
			getGuideModule(position).startModule(this, mFunDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
