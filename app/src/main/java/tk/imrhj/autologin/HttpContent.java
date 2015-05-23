package tk.imrhj.autologin;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by rhj on 15/5/23.
 */
public  class HttpContent {


    public static String getResponse(String url) {
        String response = new String();
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                response = EntityUtils.toString(entity, "utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public static String getVersionFromJson() {
        String url = "http://www.imrhj.tk/wp-content/uploads/version.json";
        String version = "1.3";
        String response = getResponse(url);
        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            version = jsonObject.getString("version");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }
}
