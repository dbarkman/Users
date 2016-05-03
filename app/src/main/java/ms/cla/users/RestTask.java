package ms.cla.users;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class RestTask extends AsyncTask<Map, Void, String> {

    private static String tag = "RestTask";

    public static final String httpResponse = "httpResponse";

    private Context context;
    private String action;
    private String apiUri;
    private String apiService;

    public RestTask(Context context, String action, String apiService) {
        this.context = context;
        this.action = action;
        this.apiService = apiService;
    }

    @Override
    protected String doInBackground(Map... params) {

        apiUri = "http://jsonplaceholder.typicode.com/" + apiService;

        Log.d(tag, "Processing: " + apiUri);

        try {
            URL url = new URL("http://jsonplaceholder.typicode.com/" + apiService);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(action);
        intent.putExtra(httpResponse, result);
        //Broadcast the completion
        context.sendBroadcast(intent);
    }
}