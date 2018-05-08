package kr.dtimes.contract;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class zommUserRegisterRequest extends StringRequest {

    //http://neohan.org/contract-app1/userregister.php?UserKey=2ee6e8a2-cd3a-4217-9601-8d7d53f549d0&PhoneNumber_Hashed=F4D01BFA1D3D15D0A0EFEBB1F8F21324BAC540186B5631DF61C17F02A3AC5656&Debug=Debug&111
    final static private String UserRegister_URL = "http://www.neohan.org/contract-app1/userregister.php";
    private Map<String, String> parameters;

    public zommUserRegisterRequest(String UserKey, String PhoneNumber_Hashed, Response.Listener<String> listener){
        super(Method.POST, UserRegister_URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("UserKey", UserKey);
        parameters.put("PhoneNumber_Hashed", PhoneNumber_Hashed);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }

}
