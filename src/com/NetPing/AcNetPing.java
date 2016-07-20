package com.NetPing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.NetPing.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AcNetPing extends Activity {
	private ScrollView scrollView;
	private EditText edtAddress;
	private EditText edtCount;
	private Button btnPing;
	private TextView tvReContent;
	private TextView tvTip;
	private ProgressDialog pDialog;
	private long exitTime;
	
	

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 1) {
				String response =(String)msg.obj;
				if(response!=null&& response.indexOf("icmp_seq")!=-1) {
					String host = response.substring(response.indexOf("from")+4,response.indexOf(":"));
					String count = response.substring(response.indexOf("icmp_seq")+9,response.indexOf("ttl"));
					String time = response.substring(response.indexOf("time")+5);
					tvReContent.append(String.format("%s响应第%s次请求，用时%s", host,count,time) +"\n");
				} else tvReContent.append(response +"\n");
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			} else if (msg.what == 0) {
				Toast.makeText(AcNetPing.this, "消息发送失败!", Toast.LENGTH_LONG)
						.show();
			} else if(msg.what ==2) {
				tvTip.setText((String)msg.obj);
			}
			//pDialog.dismiss();
		}

	};
	
	private void init() {
		edtAddress.setText("");
		edtCount.setText("100");
		tvTip.setText("请输入服务器地址,默认发100次消息");
		tvReContent.setText("");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		edtAddress = (EditText) findViewById(R.id.edtAddress);
		edtCount =(EditText) findViewById(R.id.edtCount);
		btnPing = (Button) findViewById(R.id.btnPing);
		tvReContent = (TextView) findViewById(R.id.tvReContent);
		tvTip =(TextView) findViewById(R.id.tip);
		
		scrollView =(ScrollView)findViewById(R.id.scroll);
		init();
		btnPing.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String addr = edtAddress.getText().toString().trim();
				String count = edtCount.getText().toString().trim();
				if (!addr.isEmpty()) {
					// ��ʾProgressDialog
			/*		pDialog = ProgressDialog.show(AcNetPing.this, "Ping...",
							"Please wait...", true, false);*/
					tvTip.setText(String.format("开始连接%s...", edtAddress.getText()));
					tvReContent.setText("");
					new Thread(new PingRun(addr,count)).start();
				} else {
					init();
					Toast.makeText(AcNetPing.this, "请输入相关信息",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	class PingRun implements Runnable {
		private String address;
		private String count;

		public PingRun(String address,String count) {
			super();
			this.address = address;
			this.count = count;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Ping(address,count);
		}

	}

	public boolean Ping(String address,String count) {
		String result = null;
		int cnt = -1;
		try{
			if(count ==null ||count.length()==0) {
				cnt = 100;
			} else {
				cnt = Integer.parseInt(count);
			}
		}catch(NumberFormatException e) {
			cnt = -1;
		}
		if(cnt == -1) {
			handler.sendMessage(Message.obtain(handler, 2,
					"将使用默认次数"));
		}
		try {
			Process p = Runtime.getRuntime()
					.exec("ping -c "+cnt+" -w 60000 " + address);// ping
			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer stringBuffer = new StringBuffer();
			String content = "";
			while ((content = in.readLine()) != null) {
				Log.i("TTT", "result content : " + stringBuffer.toString());
				stringBuffer.append(content);
				handler.sendMessage(Message.obtain(handler, 1,
						content.toString()));
			}
			
			int status = p.waitFor();
			
			if (status == 0) {
				result = "发送成功";
				handler.sendMessage(Message.obtain(handler, 2,
						result));
				return true;
			} else {
				result = "发送失败";
				//handler.sendMessage(Message.obtain(handler, 2,result));
			}
		} catch (IOException e) {
			result = "failed~ IOException";
		} catch (InterruptedException e) {
			result = "failed~ InterruptedException";
		} finally {
			Log.i("TTT", "result = " + result);
		}
		handler.sendMessage(Message.obtain(handler, 2,result));
		handler.sendMessage(Message.obtain(handler, 0));
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				this.exitApp();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 
	 */
	private void exitApp() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(AcNetPing.this, "再按一次，将退出程序", Toast.LENGTH_SHORT)
					.show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
