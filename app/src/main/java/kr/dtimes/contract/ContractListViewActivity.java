package kr.dtimes.contract;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContractListViewActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mainIntent = new Intent(ContractListViewActivity.this, MainActivity.class);
                    ContractListViewActivity.this.startActivity(mainIntent);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private ListView contractView;
    private ContractListAdapter adapter;
    private List<Contract> contractList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_list_view);
        Intent intent = getIntent();

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //BASIC USER AUTH DATA
        String UserKey="";
        String PhoneNumber_Hashed="";
        try{
            SharedPreferences pref;
            pref = getSharedPreferences("pref", MODE_PRIVATE);
            UserKey=pref.getString("UserKey",null);
            PhoneNumber_Hashed=pref.getString("PhoneNumber_Hashed",null);
        }catch(Exception e){
            Log.e("INFO","ContractListViewActivity getSharedPreferences");
            e.printStackTrace();}


        // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    //초기화
                    contractView = (ListView) findViewById(R.id.contractView);
                    contractList = new ArrayList<Contract>();

                    //테스트데이터
                    //contractList.add(new Contract("1234", "계약내용1"));
                    //contractList.add(new Contract("1211", "계약내용2"));
                    //contractList.add(new Contract("1233", "계약내용3"));



                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("response");
                    int count = 0;
                    String REQ_TIME, REQ_TRANSKEY, AGR_TIME, AGREEMENTTEXT;
                    while (count < jsonArray.length()){
                        // array_push($response, array("REQ_TIME"=>$REQ_TIME, "REQ_TRANSKEY"=>$REQ_TRANSKEY,"AGR_TIME"=>$AGR_TIME, "AGR_TRANSKEY"=>$AGR_TRANSKEY, "AGREEMENTTEXT"=>$AGREEMENTTEXT, "AGREEMENT"=>$AGREEMENT));
                        JSONObject object = jsonArray.getJSONObject(count);
                        REQ_TIME = object.getString("REQ_TIME");
                        REQ_TRANSKEY = object.getString("REQ_TRANSKEY");
                        AGR_TIME = object.getString("AGR_TIME");
                        AGREEMENTTEXT = object.getString("AGREEMENTTEXT");
                        AGREEMENTTEXT = AGREEMENTTEXT + "\n\n* 계약요청일자 : "+REQ_TIME+"\n* 계약합의일자 : "+AGR_TIME;
                        Contract contract = new Contract("계약번호:"+REQ_TRANSKEY+" ("+REQ_TIME+")", AGREEMENTTEXT);
                        contractList.add(contract);
                        count++;
                    }

                    //contractList.add(new Contract("1233", "계약내용4"));

                    adapter = new ContractListAdapter(getApplicationContext(), contractList);
                    contractView.setAdapter(adapter);


                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        };

        zommGetContractListRequest zommrequest = new zommGetContractListRequest(UserKey, PhoneNumber_Hashed, responseListener);
        RequestQueue queue = Volley.newRequestQueue(ContractListViewActivity.this);
        queue.add(zommrequest);


    }
}
