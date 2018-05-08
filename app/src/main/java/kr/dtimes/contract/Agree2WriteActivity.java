package kr.dtimes.contract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Agree2WriteActivity extends AppCompatActivity {

    String agrTranskeyText = "";
    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mainIntent = new Intent(Agree2WriteActivity.this, MainActivity.class);
                    Agree2WriteActivity.this.startActivity(mainIntent);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent contractlistviewIntent = new Intent(Agree2WriteActivity.this, ContractListViewActivity.class);
                    Agree2WriteActivity.this.startActivity(contractlistviewIntent);
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree2_write);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        Intent intent = getIntent();

        TextView agrTranskey = (TextView) findViewById(R.id.agrTranskeyText);
        agrTranskey.setText(intent.getStringExtra("AGRTRANSKEY").toString());
        agrTranskeyText = intent.getStringExtra("AGRTRANSKEY").toString();

        TextView agreementText = (TextView) findViewById(R.id.agreementText);
        agreementText.setText(intent.getStringExtra("AGREEMENTTEXT").toString());



        Log.i("INFO","합의키(agrTranskeyText) : "+ agrTranskeyText);


        // 동의버튼 Agree2CheckActivity ->  Agree3AgreedActivity
        TextView agreeButton = (TextView) findViewById(R.id.agreeButton);
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // UserKey, PhoneNumber_Hashed, agrTranskeyText 서버로 전송

                //BASIC USER AUTH DATA
                String UserKey="";
                String PhoneNumber_Hashed="";
                try{
                    SharedPreferences pref;
                    pref = getSharedPreferences("pref", MODE_PRIVATE);
                    UserKey=pref.getString("UserKey",null);
                    PhoneNumber_Hashed=pref.getString("PhoneNumber_Hashed",null);
                }catch(Exception e){
                    Log.e("INFO","Agree2CheckActivity getSharedPreferences");
                    e.printStackTrace();}

                //EditText editText = (EditText) findViewById(R.id.agrTranskeyText);
                //String agrTransKey = editText.getText().toString();

                //Log.i("INFO","합의키(agrTransKey)"+ agrTransKey);

                // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean result = jsonResponse.getBoolean("result");
                            if(result == false) {
                                //Toast.makeText(getApplicationContext(), "서버통신 관련 오류 발생 CODE001", 5).show();
                                Log.i("INFO","동의저장 실패");
                                //TODO 합의요청 등록 실패 에러처리
                            }
                            else{
                                String resultmessage =jsonResponse.getString("resultmessage");
                                String UserKey =jsonResponse.getString("UserKey");
                                String PhoneNumber_Hashed =jsonResponse.getString("PhoneNumber_Hashed");
                                String AgrTransKey =jsonResponse.getString("AgrTransKey");
                                Log.i("INFO","동의저장 성공");
                                Log.i("INFO", resultmessage);
                                Log.i("INFO", UserKey);
                                Log.i("INFO", PhoneNumber_Hashed);
                                Log.i("INFO", AgrTransKey);

                                Intent reqIntent = new Intent(Agree2WriteActivity.this, Agree3AgreedActivity.class);
                                //reqIntent.putExtra("AGRTRANSKEY", AGRTRANSKEY);
                                //reqIntent.putExtra("AGREEMENTTEXT", AGREEMENTTEXT);
                                Agree2WriteActivity.this.startActivity(reqIntent);

                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                };

                zommUserAgr2WriteRequest zommrequest = new zommUserAgr2WriteRequest(UserKey, PhoneNumber_Hashed, agrTranskeyText, responseListener);
                RequestQueue queue = Volley.newRequestQueue(Agree2WriteActivity.this);
                queue.add(zommrequest);
            }
        });



    }
}
