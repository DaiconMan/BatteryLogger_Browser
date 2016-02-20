package jp.daiconman.android.batterylogger_browser;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

public class MainActivity extends Activity {
    
	String url;
	int Brightness;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final Button TestStart_btn = (Button)findViewById(R.id.button1);
        final Button AutoinputURL30fps_btn = (Button)findViewById(R.id.button3);
        final Button AutoinputURL12fps_btn = (Button)findViewById(R.id.button4);
        final EditText Brightness_EditText = (EditText)findViewById(R.id.editText1);
        final EditText url_EditText = (EditText)findViewById(R.id.editText2);
        
        //TestStartが押されたときの処理
        TestStart_btn.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v){
        		SpannableStringBuilder url_EditText_sb = (SpannableStringBuilder)url_EditText.getText();
        		SpannableStringBuilder Brightness_EditText_sb = (SpannableStringBuilder)Brightness_EditText.getText();
        		url = url_EditText_sb.toString();
        		String Brightness_string = Brightness_EditText_sb.toString();
        		Brightness = Integer.parseInt(Brightness_string);
        		
        		Intent intent = new Intent(MainActivity.this, TestActivity.class);
        		intent.putExtra("url", url);
        		intent.putExtra("brightness", Brightness);
        		startActivity(intent);
        	}
        });
        
        //30fpsが押されたときの処理
        AutoinputURL30fps_btn.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v){
        		url_EditText.setText("http://challenger.sugitalab.ce.fit.ac.jp/menber/2013/nishimuraken/DPSExperimentation/30fps.html");
        	}
        });
        
      //12fpsが押されたときの処理
        AutoinputURL12fps_btn.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v){
        		url_EditText.setText("http://challenger.sugitalab.ce.fit.ac.jp/menber/2013/nishimuraken/DPSExperimentation/12fps.html");
        	}
        });
    }
}
