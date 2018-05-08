package kr.dtimes.contract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
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

public class Agree1CheckActivity extends AppCompatActivity {
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mainIntent = new Intent(Agree1CheckActivity.this, MainActivity.class);
                    Agree1CheckActivity.this.startActivity(mainIntent);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent contractlistviewIntent = new Intent(Agree1CheckActivity.this, ContractListViewActivity.class);
                    Agree1CheckActivity.this.startActivity(contractlistviewIntent);
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
        setContentView(R.layout.activity_agree1_check);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        EditText agrTranskeyText = (EditText)findViewById(R.id.agrTranskeyText);
        agrTranskeyText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        //체크키 및  합의내용 확인 버튼 Agree1CheckActivity -> Agree2CheckActivity
        TextView reqButton = (TextView) findViewById(R.id.checkButton);
        reqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // UserKey, 전화번호, 합의내용 서버로 전송

                //BASIC USER AUTH DATA
                String UserKey="";
                String PhoneNumber_Hashed="";
                try{
                    SharedPreferences pref;
                    pref = getSharedPreferences("pref", MODE_PRIVATE);
                    UserKey=pref.getString("UserKey",null);
                    PhoneNumber_Hashed=pref.getString("PhoneNumber_Hashed",null);
                }catch(Exception e){
                    Log.e("INFO","Agree1CheckActivity getSharedPreferences");
                    e.printStackTrace();}

                EditText editText = (EditText) findViewById(R.id.agrTranskeyText);
                String agrTransKey = editText.getText().toString();
                Log.i("INFO","합의키"+ agrTransKey);

                // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean result = jsonResponse.getBoolean("result");
                            if(result == false) {
                                //Toast.makeText(getApplicationContext(), "서버통신 관련 오류 발생 CODE001", 5).show();
                                Log.i("INFO","합의내용 읽기 실패");
                                //TODO 합의요청 등록 실패 에러처리
                            }
                            else{
                                String AGREEMENTTEXT =jsonResponse.getString("AGREEMENTTEXT");
                                String AGRTRANSKEY =jsonResponse.getString("AGRTRANSKEY");
                                Log.i("INFO","합의내용 읽기 성공");
                                Log.i("INFO", AGREEMENTTEXT);
                                Log.i("INFO", AGRTRANSKEY);

                                Intent reqIntent = new Intent(Agree1CheckActivity.this, Agree2WriteActivity.class);
                                reqIntent.putExtra("AGRTRANSKEY", AGRTRANSKEY);
                                reqIntent.putExtra("AGREEMENTTEXT", AGREEMENTTEXT);
                                Agree1CheckActivity.this.startActivity(reqIntent);

                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                };

                zommUserAgr1CheckRequest zommrequest = new zommUserAgr1CheckRequest(UserKey, PhoneNumber_Hashed, agrTransKey, responseListener);
                RequestQueue queue = Volley.newRequestQueue(Agree1CheckActivity.this);
                queue.add(zommrequest);
            }
        });



    }
}
