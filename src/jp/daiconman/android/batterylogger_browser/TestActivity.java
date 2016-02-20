package jp.daiconman.android.batterylogger_browser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;

public class TestActivity extends Activity {
	
	//日付を取得
    final Calendar calendar = Calendar.getInstance();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
    final int minute = calendar.get(Calendar.MINUTE);
    final int second = calendar.get(Calendar.SECOND);
    final int ms = calendar.get(Calendar.MILLISECOND);
    
    double user;
    double nice;
    double sys;
    double idle;
    
    String TestStartData = year + "." + (month + 1) + "." + day + " " + hour + "h" + minute + "m" + second + "s";
    
    boolean TestFastFlag;
    boolean postDelayedFlag;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        WebView web= (WebView) findViewById(R.id.webView1);
        //対象のWebivewを取得して設定を有効化
        web.getSettings().setJavaScriptEnabled(true);
        //リンクをクリックしたときに標準ブラウザで開かない
        web.setWebViewClient(new WebViewClient());
        //キャッシュを使用しない
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        int brightness_percent = intent.getIntExtra("brightness",10);
        
        //ページをロード
        web.loadUrl(url);
        
        //画面をロックしないように設定
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        
        //実験が初めて実行されたことを検出できるようにフラグをたてる
        TestFastFlag = true;
        
        //実験が開始されているので
        postDelayedFlag = true;
        
        //画面輝度の設定(危険!! brightnessをにするとブラックアウトする)
        // 輝度を設定(brightnessは0～255)
        float brightness = (float)brightness_percent / 100.0f * 255.0f;
        //Log.v("brightness", "" + (int)brightness);
        //Log.v("brightness", "" + brightness_percent);
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, (int)brightness);

        // 輝度を0.0～1.0に変換
        float brf = (float)brightness  / 255.0f;

        // android.view.WindowManager.LayoutParamsを取得
        LayoutParams lp = getWindow().getAttributes();

        // 輝度を設定
        lp.screenBrightness = brf;

        // 輝度を反映(この時点で画面の輝度が変わる)
        // 本来この方法は現在のActivityの輝度設定にしか使えない
        getWindow().setAttributes(lp);

        // この後にActivityを閉じると設定値が永続的に反映される(されてしまう)
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter filter = new IntentFilter();
        
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);  
        
        //Keep screen off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        unregisterReceiver(mBroadcastReceiver);
        
        //Keep screen off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    public String[] cpu_used() {
    	// カーネル全体の統計情報を表示する
    	String [] cmdArgs = {"/system/bin/cat","/proc/stat"};

    	String cpuLine    =  "";
    	StringBuffer cpuBuffer    = new StringBuffer();

    	ProcessBuilder cmd = new ProcessBuilder(cmdArgs);

    	try {

    	Process process = cmd.start();

    	InputStream in  = process.getInputStream();

    	// 統計情報より1024バイト分を読み込む
    	// cpu user/nice/system/idle/iowait/irq/softirq/steal/の情報を取得する

    	byte[] lineBytes = new byte[1024];

    	while(in.read(lineBytes) != -1 ) {
    		cpuBuffer.append(new String(lineBytes));
    	}

    	in.close();

    	}catch (IOException e) {
    	}

    	cpuLine = cpuBuffer.toString();

    	// 1024バイトより「cpu～cpu0」までの文字列を抽出
    	int start = cpuLine.indexOf("cpu");
    	int end = cpuLine.indexOf("cpu0");

    	cpuLine = cpuLine.substring(start, end);

    	//Log.i("CPU_VALUES_LINE",cpuLine);
    	
    	String[] cpuLine_tmp = cpuLine.split("\\s", 0);
    	List<String> cpuLineList = new ArrayList<String>(Arrays.asList(cpuLine_tmp));
    	cpuLineList.remove("cpu");
    	
    	String[] temp = (String[]) cpuLineList.toArray(new String[cpuLineList.size()]);
    	
    	return temp;
    }

    class AsyncAppTask extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
        	//前処理
        }
        @Override
        protected String doInBackground(Void... arg0) {
        	// バックグランド処理をここに記述します
        	
        	//CPU使用率
            //--ここから--
            String[] cpu1 = cpu_used();
            
            //1000ミリ秒Sleepする
            try{
            	  Thread.sleep(1000);
            }catch(InterruptedException e){}
            
            String[] cpu2 = cpu_used();
            
            int[] cpu1_int = new int[cpu1.length];
            int[] cpu2_int = new int[cpu2.length];
            int[] sub_cpu = new int[cpu1.length];
            int total = 0;
            
            for(int i = 1; i < cpu1.length; i++) {
            	sub_cpu[i] = Integer.parseInt(cpu2[i]) - Integer.parseInt(cpu1[i]);
            	total += sub_cpu[i];
            }
            
            user = 1.0 * sub_cpu[1] / total * 100.0;
            nice = 1.0 * sub_cpu[2] / total * 100.0;
            sys = 1.0 * sub_cpu[3] / total * 100.0;
            idle = 1.0 * sub_cpu[4] / total * 100.0;
            Log.i("CPU_VALUES_LINE","user:" + user +"[%]");
            Log.i("CPU_VALUES_LINE","nice:" + nice +"[%]");
            Log.i("CPU_VALUES_LINE","sys:" + sys +"[%]");
            Log.i("CPU_VALUES_LINE","idle:" + idle +"[%]");
            //--ここまで--
        	
        	return "Thread CPUload";
        }
        @Override
        protected void onPostExecute(String result) {
        	//バックグラウンド終了処理
        }
      }
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WebView web= (WebView) findViewById(R.id.webView1);
            
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                
                String statusString = "";
                
                switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = "unknown";
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = "charging";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = "discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = "not charging";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = "full";
                    break;
                }
                
                String healthString = "";
                
                switch (health) {
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    healthString = "unknown";
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    healthString = "good";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    healthString = "overheat";
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    healthString = "dead";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    healthString = "voltage";
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    healthString = "unspecified failure";
                    break;
                }
                
                String acString = "";
                
                switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    acString = "plugged ac";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    acString = "plugged usb";
                    break;
                }
                
                Log.v("info", "---BatteryInfo---");
                Log.v("status", statusString);
                Log.v("health", healthString);
                Log.v("present", String.valueOf(present));
                Log.v("level", String.valueOf(level));
                Log.v("scale", String.valueOf(scale));
                Log.v("icon_small", String.valueOf(icon_small));
                Log.v("plugged", acString);
                Log.v("voltage", String.valueOf(voltage));
                Log.v("temperature", String.valueOf(temperature));
                Log.v("technology", technology);
                
                //日付を取得
                final Calendar calendar = Calendar.getInstance();
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
                final int second = calendar.get(Calendar.SECOND);
                final int ms = calendar.get(Calendar.MILLISECOND);
                
                int brightness = 0;
                // 輝度を取得(brightnessは0～255)
				try {
					brightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS);
				} catch (SettingNotFoundException e2) {
					// TODO 自動生成された catch ブロック
					e2.printStackTrace();
				}
                // パーセント表記(0～100)にする場合
                int percent = brightness * 100 / 255;
             
                // 保存先パスの指定(SDカードのフォルダ(/sdcard/など)以下 のsample.txt)
                String filePath= Environment.getExternalStorageDirectory() + "/BLB/" + "/Log[" + TestStartData + "].csv";
                
                //WebViewのURL
                String WebView_Title = web.getTitle();
                String WebView_URL = web.getUrl();
                
                //CPU使用率の取得
                new AsyncAppTask().execute();
            	
                // 書き込む内容
                String fileString = 
                		year + "/" + (month + 1) + "/" + day + "," + hour + ":" + minute + ":" + second + "." + ms + "," +
                		status + "," +
                	    level + "," +
                		voltage + "," +
                	    temperature + "," +
                	    percent + "," +
                		WebView_Title + "," +
                	    WebView_URL + "," +
                		user + "," +
                	    nice + "," +
                		sys + "," +
                	    idle + "," +
                	    "\n";
                
                String FastfileString = 
                		"Data," + 
                		"Time," +
                		"status," +
                	    "level," +
                		"voltage," +
                	    "temperature," +
                	    "brightness," +
                	    "WebTitle," +
                	    "WebURL," +
                	    "CPU_User," +
                	    "CPU_nice," +
                	    "CPU_Sys," +
                	    "CPU_Idle," +
                		"\n" +
                	    fileString;
                
                // openFileOutputの宣言
                FileOutputStream fos = null;
                
                if (TestFastFlag == true) {
                	// ファイルの書き込み処理
                    try {
                    	fos = new FileOutputStream(filePath, true);
                    	
                    	//ファイル書き込み
                    	fos.write(FastfileString.getBytes());
                    	// ファイルのクローズ
                    	fos.close();
                    	
                    	TestFastFlag = false;
                    } catch (FileNotFoundException e1) {
                    	e1.printStackTrace();
                    } catch (IOException e) {
                    	e.printStackTrace();
                    }
                } else {
                	// ファイルの書き込み処理
                    try {
                    	fos = new FileOutputStream(filePath, true);
                   
                    	// ファイルの書き込み
                    	fos.write(fileString.getBytes());
                    	// ファイルのクローズ
                    	fos.close();
                    } catch (FileNotFoundException e1) {
                    	e1.printStackTrace();
                    } catch (IOException e) {
                    	e.printStackTrace();
                    }
                }
                
                //バッテリ残量が10%以下になったらBackボタンと同じ動作で測定を終了する(はず...)
                if(level < 10) {
                	finish();
                }
            }
        }
    };
}
