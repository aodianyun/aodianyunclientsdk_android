package com.douyayun.newnodemdiademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	Button playerBtn, encoderBtn,nodePlayerBtn;
	EditText playUrl, pubUrl, bufferTime,maxBufferTime;
	CheckBox enablePlayCB, enableVideoCB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		playerBtn = (Button) findViewById(R.id.button1);
		encoderBtn = (Button) findViewById(R.id.button2);
		nodePlayerBtn = (Button)findViewById(R.id.button3);
		playUrl = (EditText) findViewById(R.id.editText_play_url);
		pubUrl = (EditText) findViewById(R.id.editText_pub_url);
		bufferTime = (EditText) findViewById(R.id.editText_buffersize);
		maxBufferTime = (EditText) findViewById(R.id.editText_maxbuffersize);
		enablePlayCB = (CheckBox) findViewById(R.id.checkBox_play_log);
		enableVideoCB = (CheckBox) findViewById(R.id.CheckBox_video);

		playUrl.setText(SharedPreUtil.getString(this, "playUrl", "rtmp://lssplay.aodianyun.com/demo/game"));
		pubUrl.setText(SharedPreUtil.getString(this, "pubUrl", "rtmp://stream.nodemedia.cn/live/streams"));
		bufferTime.setText(SharedPreUtil.getString(this, "bufferTime", "100"));
		maxBufferTime.setText(SharedPreUtil.getString(this, "maxBufferTime", "1000"));
		enablePlayCB.setChecked(SharedPreUtil.getBoolean(this, "enablePlayLog"));
		enableVideoCB.setChecked((Boolean) SharedPreUtil.getBoolean(this, "enableVideo"));

		playerBtn.setOnClickListener(this);
		encoderBtn.setOnClickListener(this);
		nodePlayerBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			// 记住上次播放配置的信息，只在demo中使用，非SDK方法
			SharedPreUtil.put(MainActivity.this, "playUrl", playUrl.getText().toString());
			SharedPreUtil.put(MainActivity.this, "bufferTime", bufferTime.getText().toString());
			SharedPreUtil.put(MainActivity.this, "maxBufferTime", maxBufferTime.getText().toString());
			SharedPreUtil.put(MainActivity.this, "enablePlayLog", enablePlayCB.isChecked());
			SharedPreUtil.put(MainActivity.this, "enableVideo", enableVideoCB.isChecked());

			MainActivity.this.startActivity(new Intent(MainActivity.this, LivePlayerDemoActivity.class));
			break;
		case R.id.button2:
			// 记住上次输入的发布地址，只在demo中使用，非SDK方法
			SharedPreUtil.put(MainActivity.this, "pubUrl", pubUrl.getText().toString());

			MainActivity.this.startActivity(new Intent(MainActivity.this, LivePublisherDemoActivity.class));
			break;
		case R.id.button3:
			
			MainActivity.this.startActivity(new Intent(MainActivity.this, NodePlayerDemoActivity.class));
			break;
		}
	}

}
