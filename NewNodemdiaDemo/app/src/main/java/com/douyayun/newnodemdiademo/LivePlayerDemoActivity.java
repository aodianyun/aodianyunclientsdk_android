package com.douyayun.newnodemdiademo;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.nodemedia.LivePlayer;
import cn.nodemedia.LivePlayerDelegate;

public class LivePlayerDemoActivity extends Activity {
	// LinearLayout liner0;
	boolean isPlaying;
	int tsID;
	String outTsPath;
	SurfaceView sv;
	EditText logText;
	Boolean showLog, enableVideo;
	float srcWidth;
	float srcHeight;
	DisplayMetrics dm;
	Button capBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		dm = getResources().getDisplayMetrics();

		showLog = (Boolean) SharedPreUtil.getBoolean(this, "enablePlayLog");
		enableVideo = (Boolean) SharedPreUtil.getBoolean(this, "enableVideo");

		LivePlayer.init(this);
		LivePlayer.setDelegate(new LivePlayerDelegate() {
			@Override
			public void onEventCallback(int event, String msg) {
				Message message = new Message();
				Bundle b = new Bundle();
				b.putString("msg", msg);
				message.setData(b);
				message.what = event;
				handler.sendMessage(message);
			}
		});

		sv = (SurfaceView) findViewById(R.id.surfaceview1);
		capBtn = (Button) findViewById(R.id.play_cap_button);
		capBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String capFilePath = Environment.getExternalStorageDirectory().getPath() + "/play_cap.jpg";
				if (LivePlayer.capturePicture(capFilePath)) {
					Toast.makeText(LivePlayerDemoActivity.this, "截图保存到 " + capFilePath, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(LivePlayerDemoActivity.this, "截图保存失败", Toast.LENGTH_SHORT).show();
				}
			}
		});

		logText = (EditText) findViewById(R.id.editText3);
		if (!showLog) {
			logText.setVisibility(View.GONE);
		}

		if (enableVideo) {
			LivePlayer.setUIVIew(sv);
		} else {
			LivePlayer.setUIVIew(null);
		}

		/**
		 * 设置缓冲区时长，与flash编程时一样，可以设置2个值
		 * 第一个bufferTime为从连接成功到开始播放的启动缓冲区长度，越小启动速度越快，最小100毫秒
		 * 注意：声音因为没有关键帧，所以这个缓冲区足够马上就可以听到声音，但视频需要等待关键帧后才会开始显示画面。
		 * 如果你的服务器支持GOP_cache可以开启来加快画面的出现
		 */
		int bufferTime = Integer.valueOf(SharedPreUtil.getString(this, "bufferTime")); // 获取上一个页面设置的bufferTIme，非sdk方法
		LivePlayer.setBufferTime(bufferTime);
		
		/**
		 * maxBufferTime为最大缓冲区，当遇到网络抖动，较大的maxBufferTime更加平滑，但延迟也会跟着增加。
		 * 这个值关乎延迟的大小。
		 */
		int maxBufferTime = Integer.valueOf(SharedPreUtil.getString(this, "maxBufferTime"));;// 获取上一个页面设置的maxBufferTIme，非sdk方法
		LivePlayer.setMaxBufferTime(maxBufferTime);

		/**
		 * 设置是否接收音视频流  协议参考 rtmp_specification_1.0.pdf 7.2.2.4. & 7.2.2.5.
		 * 默认值都为true 如不需要该功能可以不设置该值
		 * 注意：目前测试了fms和red5支持该参数设定有效，欢迎测试补充。目前版本只在开始播放前设置有效，中途无法变更。
		 */
//		LivePlayer.receiveAudio(true);
//		LivePlayer.receiveVideo(false);
		
		String playUrl = SharedPreUtil.getString(this, "playUrl");// 获取上一页设置的播放地址，非sdk方法
		/**
		 * 开始播放
		 */
		LivePlayer.startPlay(playUrl);
		
		/**
		 * Demo调试用例，每200毫秒获取一次缓冲时长 单位毫秒
		 */
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while(!LivePlayerDemoActivity.this.isFinishing()) {
//					Log.d("NodeMedia.java","BufferLength:"+LivePlayer.getBufferLength());
//					try {
//						Thread.sleep(200);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
//				
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LivePlayer.stopPlay();

	}

	/**
	 * 监听手机旋转，不销毁activity进行画面旋转，再缩放显示区域
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		doVideoFix();
	}

	/**
	 * 视频画面高宽等比缩放，此SDK——demo 取屏幕高宽做最大高宽缩放
	 */
	private void doVideoFix() {
		float maxWidth = dm.widthPixels;
		float maxHeight = dm.heightPixels;
		float fixWidth;
		float fixHeight;
		if (srcWidth / srcHeight <= maxWidth / maxHeight) {
			fixWidth = srcWidth * maxHeight / srcHeight;
			fixHeight = maxHeight;
		} else {
			fixWidth = maxWidth;
			fixHeight = srcHeight * maxWidth / srcWidth;
		}
		ViewGroup.LayoutParams lp = sv.getLayoutParams();
		lp.width = (int) fixWidth;
		lp.height = (int) fixHeight;

		sv.setLayoutParams(lp);
	}

	private Handler handler = new Handler() {
		// 回调处理
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StringBuffer sb = new StringBuffer();
			SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
			String sRecTime = sDateFormat.format(new java.util.Date());
			sb.append(sRecTime);
			sb.append(" - ");
			sb.append(msg.getData().getString("msg"));
			sb.append("\r\n");
			logText.append(sb);

			switch (msg.what) {
			case 1000:
				// Toast.makeText(LivePlayerDemoActivity.this, "正在连接视频",
				// Toast.LENGTH_SHORT).show();
				break;
			case 1001:
				// Toast.makeText(LivePlayerDemoActivity.this, "视频连接成功",
				// Toast.LENGTH_SHORT).show();
				break;
			case 1002:
				// Toast.makeText(LivePlayerDemoActivity.this, "视频连接失败",
				// Toast.LENGTH_SHORT).show();
				//流地址不存在，或者本地网络无法和服务端通信，回调这里。5秒后重连， 可停止
				//LivePlayer.stopPlay(); 
				break;
			case 1003:
				//Toast.makeText(LivePlayerDemoActivity.this, "视频开始重连",
				//LivePlayer.stopPlay();	//自动重连总开关
				break;
			case 1004:
				// Toast.makeText(LivePlayerDemoActivity.this, "视频播放结束",
				// Toast.LENGTH_SHORT).show();
				break;
			case 1005:
				// Toast.makeText(LivePlayerDemoActivity.this, "网络异常,播放中断",
				// Toast.LENGTH_SHORT).show();
				//播放中途网络异常，回调这里。1秒后重连，如不需要，可停止
				//LivePlayer.stopPlay(); 
				break;
			case 1100:
//				System.out.println("NetStream.Buffer.Empty");
				break;
			case 1101:
//				System.out.println("NetStream.Buffer.Buffering");
				break;
			case 1102:
//				System.out.println("NetStream.Buffer.Full");
				break;
			case 1103:
//				System.out.println("Stream EOF");
				//客服端明确收到服务端发送来的 StreamEOF 和 NetStream.Play.UnpublishNotify时回调这里
				//收到本事件，说明：此流的发布者明确停止了发布，或者网络异常，被服务端明确关闭了流
				//本sdk仍然会继续在1秒后重连，如不需要，可停止
//				LivePlayer.stopPlay(); 
				break;
			case 1104:
				/**
				 * 得到 解码后得到的视频高宽值,可用于重绘surfaceview的大小比例 格式为:{width}x{height}
				 * 本例使用LinearLayout内嵌SurfaceView
				 * LinearLayout的大小为最大高宽,SurfaceView在内部等比缩放,画面不失真
				 * 等比缩放使用在不确定视频源高宽比例的场景,用上层LinearLayout限定了最大高宽
				 */
				String[] info = msg.getData().getString("msg").split("x");
				srcWidth = Integer.valueOf(info[0]);
				srcHeight = Integer.valueOf(info[1]);
				doVideoFix();
				break;
			default:
				break;
			}
		}
	};

}
