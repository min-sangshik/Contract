package kr.dtimes.contract;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class zommUserAgr1CheckRequest extends StringRequest {

    //http://neohan.org/contract-app1/agr1_agreementcheck.php?UserKey=2ee6e8a2-cd3a-4217-9601-8d7d53f549d0&PhoneNumber_Hashed=F4D01BFA1D3D15D0A0EFEBB1F8F21324BAC540186B5631DF61C17F02A3AC5656&Debug=Debug&AgrTransKey=ARAI
    final static private String zommURL = "http://www.neohan.org/contract-app1/agr1_agreementcheck.php";
    private Map<String, String> parameters;

    public zommUserAgr1CheckRequest(String UserKey, String PhoneNumber_Hashed, String AgrTransKey, Response.Listener<String> listener){
        super(Request.Method.POST, zommURL, listener, null);
        parameters = new HashMap<>();
        parameters.put("UserKey", UserKey);
        parameters.put("PhoneNumber_Hashed", PhoneNumber_Hashed);
        parameters.put("AgrTransKey", AgrTransKey);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}
