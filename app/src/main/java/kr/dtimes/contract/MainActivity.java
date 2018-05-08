package kr.dtimes.contract;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent contractlistviewIntent = new Intent(MainActivity.this, ContractListViewActivity.class);
                    MainActivity.this.startActivity(contractlistviewIntent);
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
        setContentView(R.layout.activity_main);


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //권한체크
        int permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS);
        if(permissionCheck1 == PackageManager.PERMISSION_DENIED || permissionCheck2 == PackageManager.PERMISSION_DENIED){
            //권한없음
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS}, 0);
        }
        else{
            //기본정보 서버체크
            String UserKey="";
            String PhoneNumber = "";
            String PhoneNumber_Hashed="";

            try{


                SharedPreferences pref;
                pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                boolean needTransferToServer = false;

                UserKey=pref.getString("UserKey",null);
                PhoneNumber = pref.getString("PhoneNumber",null);
                PhoneNumber_Hashed=pref.getString("PhoneNumber_Hashed",null);

                //GET REALTIME INFO
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String PhoneNumber_RealTime = telManager.getLine1Number();
                if(PhoneNumber_RealTime.startsWith("+82")){
                    PhoneNumber_RealTime = PhoneNumber_RealTime.replace("+82", "0");
                }
                String PhoneNumber_RealTime_Hashed = bin2hex(getHash(PhoneNumber_RealTime+"CONTRACT_V1"/*SALT*/));

                //SAVED USERKEY IS NULL
                if(UserKey == null) {
                    Log.i("INFO","UserKey is NULL");
                    UserKey = UUID.randomUUID().toString();
                    editor.putString("UserKey", UserKey);
                    editor.commit();
                }

                if(PhoneNumber == null || PhoneNumber_Hashed == null) {
                    Log.i("INFO","PhoneNumber or PhoneNumber_Hashed is NULL");
                    Log.i("INFO","PhoneNumber_RealTime is " + PhoneNumber_RealTime);
                    Log.i("INFO","PhoneNumber_RealTime_Hashed is " + PhoneNumber_RealTime_Hashed);

                    editor.putString("PhoneNumber", PhoneNumber_RealTime);
                    editor.putString("PhoneNumber_Hashed", PhoneNumber_RealTime_Hashed);
                    editor.commit();
                }

                //TODO 전화번호 바뀐경우 체크로직 추가하기
                if(PhoneNumber != null) {
                    if(PhoneNumber != PhoneNumber_RealTime) {
                        Log.i("INFO", "PhoneNumber is not null but Changed!");
                        Log.i("INFO", "PhoneNumber is " + PhoneNumber);
                        Log.i("INFO", "PhoneNumber_RealTime is " + PhoneNumber_RealTime);

                        editor.putString("PhoneNumber", PhoneNumber_RealTime);
                        editor.putString("PhoneNumber_Hashed", PhoneNumber_RealTime_Hashed);
                        editor.commit();
                    }
                }

                //FOR LOGGING
                Log.i("INFO","UserKey is " + UserKey);
                Log.i("INFO","PhoneNumber_Hashed is " + PhoneNumber_Hashed);
                Log.i("INFO","PhoneNumber_RealTime_Hashed is " + PhoneNumber_RealTime_Hashed);
            }
            catch(Exception e){ e.printStackTrace();}



            // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean result = jsonResponse.getBoolean("result");
                        if(result == false) {
                            //Toast.makeText(getApplicationContext(), "서버통신 관련 오류 발생 CODE001", 5).show();
                            Log.i("INFO","회원정보 등록/체크 실패");
                        }
                        else{
                            Log.i("INFO","회원정보 등록/체크 성공");
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            };

            // UserKey와 전화번호 서버로 전송하여 이용자 등록하기 (최초건, 아니건 상관없음)
            zommUserRegisterRequest userregisterRequest = new zommUserRegisterRequest(UserKey, PhoneNumber_Hashed, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(userregisterRequest);

        }

        //합의 요청 버튼 Req1WriteActivity -> Req2WriteActivity
        TextView reqButton = (TextView) findViewById(R.id.reqButton);
        reqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reqIntent = new Intent(MainActivity.this, Req1WriteActivity.class);
                MainActivity.this.startActivity(reqIntent);
            }
        });

        //동의 버튼
        TextView agreeButton = (TextView) findViewById(R.id.agreeButton);
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent agree1writeIntent = new Intent(MainActivity.this, Agree1CheckActivity.class);
                MainActivity.this.startActivity(agree1writeIntent);
            }
        });

        //설명 버튼
        TextView aboutButton = (TextView) findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(aboutIntent);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //위 예시에서 requestPermission 메서드를 썼을시 , 마지막 매개변수에 0을 넣어 줬으므로, 매칭
        if(requestCode == 0){
            // requestPermission의 두번째 매개변수는 배열이므로 아이템이 여러개 있을 수 있기 때문에 결과를 배열로 받는다.
            // 해당 예시는 요청 퍼미션이 한개 이므로 i=0 만 호출한다.
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    // Permission 획득 못 했을 때 시나리오 구현.
                    return;
                }
            }

            // 권한 획득한 경우 할일 정의
            String UserKey="";
            String PhoneNumber = "";
            String PhoneNumber_Hashed="";

            try{


                SharedPreferences pref;
                pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                boolean needTransferToServer = false;

                UserKey=pref.getString("UserKey",null);
                PhoneNumber = pref.getString("PhoneNumber",null);
                PhoneNumber_Hashed=pref.getString("PhoneNumber_Hashed",null);

                //GET REALTIME INFO
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String PhoneNumber_RealTime = telManager.getLine1Number();
                if(PhoneNumber_RealTime.startsWith("+82")){
                    PhoneNumber_RealTime = PhoneNumber_RealTime.replace("+82", "0");
                }
                String PhoneNumber_RealTime_Hashed = bin2hex(getHash(PhoneNumber_RealTime+"CONTRACT_V1"/*SALT*/));

                //SAVED USERKEY IS NULL
                if(UserKey == null) {
                    Log.i("INFO","UserKey is NULL");
                    UserKey = UUID.randomUUID().toString();
                    editor.putString("UserKey", UserKey);
                    editor.commit();
                }

                if(PhoneNumber == null || PhoneNumber_Hashed == null) {
                    Log.i("INFO","PhoneNumber or PhoneNumber_Hashed is NULL");
                    Log.i("INFO","PhoneNumber_RealTime is " + PhoneNumber_RealTime);
                    Log.i("INFO","PhoneNumber_RealTime_Hashed is " + PhoneNumber_RealTime_Hashed);

                    editor.putString("PhoneNumber", PhoneNumber_RealTime);
                    editor.putString("PhoneNumber_Hashed", PhoneNumber_RealTime_Hashed);
                    editor.commit();
                }

                //TODO 전화번호 바뀐경우 체크로직 추가하기
                if(PhoneNumber != null) {
                    if(PhoneNumber != PhoneNumber_RealTime) {
                        Log.i("INFO", "PhoneNumber is not null but Changed!");
                        Log.i("INFO", "PhoneNumber is " + PhoneNumber);
                        Log.i("INFO", "PhoneNumber_RealTime is " + PhoneNumber_RealTime);

                        editor.putString("PhoneNumber", PhoneNumber_RealTime);
                        editor.putString("PhoneNumber_Hashed", PhoneNumber_RealTime_Hashed);
                        editor.commit();
                    }
                }

                //FOR LOGGING
                Log.i("INFO","UserKey is " + UserKey);
                Log.i("INFO","PhoneNumber_Hashed is " + PhoneNumber_Hashed);
                Log.i("INFO","PhoneNumber_RealTime_Hashed is " + PhoneNumber_RealTime_Hashed);
            }
            catch(Exception e){ e.printStackTrace();}



            // 키값 앱 및 서버에 저장 ( 서버: 전화번호 | KEY )
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean result = jsonResponse.getBoolean("result");
                        if(result == false) {
                            //Toast.makeText(getApplicationContext(), "서버통신 관련 오류 발생 CODE001", 5).show();
                            Log.i("INFO","회원정보 등록/체크 실패");
                        }
                        else{
                            Log.i("INFO","회원정보 등록/체크 성공");
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            };

            // UserKey와 전화번호 서버로 전송하여 이용자 등록하기 (최초건, 아니건 상관없음)
            zommUserRegisterRequest userregisterRequest = new zommUserRegisterRequest(UserKey, PhoneNumber_Hashed, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(userregisterRequest);


        }else{
            //해당 권한이 거절된 경우.
            Toast.makeText(this, "폰 정보 읽기 권한 거절됨("+permissions[0]+")", Toast.LENGTH_SHORT).show();
        }
    }





    /////////////////////////////////////////////////////////////////////////////////////
    // 사용법 : Log.i("Eamorr",bin2hex(getHash("asdf")));
    public byte[] getHash(String password) {
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
    }
    /////////////////////////////////////////////////////////////////////////////////////


}
