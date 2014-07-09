package com.twh.observeflow;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class MainActivityService extends Service {

	private static final int UPDATE_PIC = 0x100;
	private int statusBarHeight;// 状态栏高度
	private View view;// 透明窗体
	private TextView flow_text = null;
	private HandlerUI handler = null;
	private Thread updateThread = null;
	private boolean viewAdded = false;// 透明窗体是否已经显示
	private boolean viewHide = false; // 窗口隐藏
	private WindowManager windowManager;
	private WindowManager.LayoutParams layoutParams;
	private UpdateUI update;
	private long preTotalBytes;
	private long currentTotalBytes;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		createFloatView();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		System.out.println("------------------onStart");
		viewHide = false;
		refresh();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		removeView();
		handler.removeCallbacksAndMessages(null);
	}

	/**
	 * 关闭悬浮窗
	 */
	public void removeView() {
		if (viewAdded) {
			windowManager.removeView(view);
			viewAdded = false;
		}
	}
	private void createFloatView() {
		handler = new HandlerUI();
		update = new UpdateUI();
		updateThread = new Thread(update);
		updateThread.start(); // 开户线程
		preTotalBytes = TrafficStats.getTotalRxBytes();
		view = LayoutInflater.from(this).inflate(R.layout.flow_layout, null);
		flow_text = (TextView) view.findViewById(R.id.flowspeed);
		windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		/*
		 * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
		 * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
		 * PixelFormat.TRANSPARENT：悬浮窗透明
		 */
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_SYSTEM_ERROR,LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
		// layoutParams.gravity = Gravity.RIGHT|Gravity.BOTTOM; //悬浮窗开始在右下角显示
		layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;

		/**
		 * 监听窗体移动事件
		 */
		view.setOnTouchListener(new OnTouchListener() {
			float[] temp = new float[] { 0f, 0f };

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
				int eventaction = event.getAction();
				switch (eventaction) {
				case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
					temp[0] = event.getX();
					temp[1] = event.getY();
					break;

				case MotionEvent.ACTION_MOVE:
					refreshView((int) (event.getRawX() - temp[0]),
							(int) (event.getRawY() - temp[1]));
					break;
				}
				return true;
			}
		});

		
	}

	/**
	 * 刷新悬浮窗
	 * 
	 * @param x
	 *            拖动后的X轴坐标
	 * @param y
	 *            拖动后的Y轴坐标
	 */
	private void refreshView(int x, int y) {
		// 状态栏高度不能立即取，不然得到的值是0
		if (statusBarHeight == 0) {
			View rootView = view.getRootView();
			Rect r = new Rect();
			rootView.getWindowVisibleDisplayFrame(r);
			statusBarHeight = r.top;
		}

		layoutParams.x = x;
		// y轴减去状态栏的高度，因为状态栏不是用户可以绘制的区域，不然拖动的时候会有跳动
		layoutParams.y = y - statusBarHeight;// STATUS_HEIGHT;
		refresh();
	}

	/**
	 * 添加悬浮窗或者更新悬浮窗 如果悬浮窗还没添加则添加 如果已经添加则更新其位置
	 */
	private void refresh() {
		// 如果已经添加了就只更新view
		if (viewAdded) {
			windowManager.updateViewLayout(view, layoutParams);
		} else {
			windowManager.addView(view, layoutParams);
			viewAdded = true;
		}
	}

	
	private void refreshFlow()
	{
		long totalRxBytesPerSecond;
		currentTotalBytes = TrafficStats.getTotalRxBytes();
		totalRxBytesPerSecond = currentTotalBytes - preTotalBytes;
		preTotalBytes = currentTotalBytes;
//		Log.i("flow", totalRxBytesPerSecond+"");
		if(totalRxBytesPerSecond < 1024){
			flow_text.setText(totalRxBytesPerSecond + "B/S");
		}else if(totalRxBytesPerSecond >= 1024 && totalRxBytesPerSecond < 1024*1024){
			flow_text.setText(totalRxBytesPerSecond/1024 + "K/S");
		}else{
			flow_text.setText(totalRxBytesPerSecond/1024/1024 + "M/S");
		}
	}
	
	/**
	 * 接受消息和处理消息
	 * 
	 * @author Administrator
	 * 
	 */
	class HandlerUI extends Handler {
		public HandlerUI() {

		}

		public HandlerUI(Looper looper) {
			super(looper);
		}

		/**
		 * 接收消息
		 */
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// 根据收到的消息分别处理
			if (msg.what == UPDATE_PIC) {
				refreshFlow();
				if (!viewHide)
					refresh();
			} else {
				super.handleMessage(msg);
			}

		}

	}

	/**
	 * 更新悬浮窗的信息
	 * 
	 * @author Administrator
	 * 
	 */
	class UpdateUI implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 如果没有中断就一直运行
			while (!Thread.currentThread().isInterrupted()) {
				Message msg = handler.obtainMessage();
				msg.what = UPDATE_PIC; // 设置消息标识
				handler.sendMessage(msg);
				// 休眠1s
				try {

					Thread.sleep(1000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
