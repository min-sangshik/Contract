package kr.dtimes.contract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Req1WriteActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);

                    Intent mainIntent = new Intent(Req1WriteActivity.this, MainActivity.class);
                    Req1WriteActivity.this.startActivity(mainIntent);

                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent contractlistviewIntent = new Intent(Req1WriteActivity.this, ContractListViewActivity.class);
                    Req1WriteActivity.this.startActivity(contractlistviewIntent);


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
        setContentView(R.layout.activity_req1);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        RadioGroup group=(RadioGroup)findViewById(R.id.radioGroup);
        RadioButton rb_general=(RadioButton)findViewById(R.id.rb_general);
        RadioButton rb_money=(RadioButton)findViewById(R.id.rb_money);
        RadioButton rb_metoo=(RadioButton)findViewById(R.id.rb_metoo);

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText("                       계    약    서\n\n"
                                +"   1. 계약일 :    년   월    일\n"
                                +"   2. 계약내용\n   - (작성)\n\n\n"
                                +"  * 계약시각은 자동저장됨\n");

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                EditText editText = (EditText) findViewById(R.id.editText);
                switch (checkedId){
                    case R.id.rb_general:
                        editText.setText(
                                "                       계    약    서\n\n"
                                +"   1. 계약일 :    년   월    일\n"
                                +"   2. 계약내용\n   - (작성)\n\n\n"
                                +"  * 계약시각은 자동저장됨\n"
                                );
                        break;
                    case R.id.rb_money:
                        editText.setText(
                                "                       차    용    증\n\n"
                                +"   1. 차용일 :    년   월    일\n"
                                +"   2. 차용액 : 금     원정 (      )\n"
                                +"   3. 원금변제기한 :     년   월    일\n"
                                +"   4. 이자율 :   %\n"
                                +"   5. 이자지급일 매월    일\n"
                                +"   6. 채무변제 송금계좌\n   - 은행명    계좌번호    예금주\n\n"
                                +"   채권자 : 주소, 연락처, 성명 기입\n"
                                +"   채무자 : 주소, 연락처, 성명 기입\n   * 계약시각은 자동저장됨\n"

                        );
                        break;
                    case R.id.rb_metoo:
                        editText.setText(
                                "        미투방지 표준 계약서 (수정불필요)\n\n          아래와 같은 조건을 합의합니다.\n\n"
                                +"   1. 강요, 협박, 약취, 유인 등의 사실 없음\n"
                                +"   2. 상호 민형사상 성인임을 고지함\n"
                                +"   3. 사진촬영, 녹음, 동영상 촬영 불허\n"
                                +"   4. 일회성 만남을 원칙으로 함\n"
                                +"   5. 어떠한 대가를 약속한 사실 없음\n"
                                +"   6. 위 사항 위반시 상대는 소송면책권 가짐\n"
                                +"   7. 이 표준계약서의 유효기간은 영구함\n\n"
                                +"   * 당사자정보는 폰 고유정보에 기반함\n   ** 계약시각은 자동저장됨\n"
                        );
                        break;

                }
            }
        });


        //합의 요청 버튼 Req1WriteActivity -> Req2WriteActivity
        TextView reqButton = (TextView) findViewById(R.id.reqButton);
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
                    Log.e("INFO","Req1WriteActivity getSharedPreferences");
                    e.printStackTrace();}

                EditText editText = (EditText) findViewById(R.id.editText);
                String agreementText = editText.getText().toString();
                Log.i("INFO","합의내용"+ agreementText);

                // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean result = jsonResponse.getBoolean("result");
                            if(result == false) {
                                //Toast.makeText(getApplicationContext(), "서버통신 관련 오류 발생 CODE001", 5).show();
                                Log.i("INFO","합의요청 등록 실패");
                                //TODO 합의요청 등록 실패 에러처리
                            }
                            else{
                                String AGREEMENTTEXT =jsonResponse.getString("AGREEMENTTEXT");
                                String REQ_TRANSKEY =jsonResponse.getString("REQ_TRANSKEY");
                                Log.i("INFO","합의요청 등록 성공");
                                Log.i("INFO", AGREEMENTTEXT);
                                Log.i("INFO", REQ_TRANSKEY);

                                Intent reqIntent = new Intent(Req1WriteActivity.this, Req2SendActivity.class);
                                reqIntent.putExtra("AGREEMENTTEXT", AGREEMENTTEXT);
                                reqIntent.putExtra("REQ_TRANSKEY", REQ_TRANSKEY);
                                Req1WriteActivity.this.startActivity(reqIntent);

                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                };

                zommUserReq1WriteRequest zommrequest = new zommUserReq1WriteRequest(UserKey, PhoneNumber_Hashed, agreementText, responseListener);
                RequestQueue queue = Volley.newRequestQueue(Req1WriteActivity.this);
                queue.add(zommrequest);
            }
        });


    }
}
