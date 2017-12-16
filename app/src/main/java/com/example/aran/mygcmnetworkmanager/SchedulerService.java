package com.example.aran.mygcmnetworkmanager;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

/**
 * Created by aran on 16/12/17.
 */
//secara default GcmTaskService sudah memiliki thread terpisah
// ketika dijalankan untuk proses yang bersifat Asynchronous

//terdapat kelebihan yang ada di GcmNetworkManager yang tidak ada di JobScheduler
//salah satunya kelas service yang digunakan pada GcmNetworkManager
// sudah secara default memiliki worker thread
// jadi memudahkan untuk bekerja secara Asynchronous.
// Selain itu GcmNetworkManager mendukung versi dibawah lolipop.
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class SchedulerService extends GcmTaskService {
    public static final String TAG = "GetWeather";
    private final String APP_ID = "9bf872cfae86872fef943bb836ceb536";
    private final String CITY = "Yogyakarta";
    public static String TAG_TASK_WEATHER_LOG = "WeatherTask";
    @Override
    //Ketika kriterianya telah sesuai atau cocok maka
    // SchedulerService akan dijalankan
    // dan method ini akan langsung dieksekusi
    public int onRunTask(TaskParams taskParams) {
        int result = 0;
        if (taskParams.getTag().equals(TAG_TASK_WEATHER_LOG)){
            getCurrentWeather();
            result = GcmNetworkManager.RESULT_SUCCESS;
        }
        return result;
    }
    private void getCurrentWeather(){
        Log.d("GetWeather", "Running");
        //Karena secara default kelas SchedulerService yang inherit ke GcmTaskService
        // sudah memiliki worker thread yang mampu berjalan secara asynchronous
        // jadi kita tidak perlu lagi untuk membuat thread lagi
        // untuk menjalankan request ke api.openweathermap.org secara asynchronous
        SyncHttpClient client = new SyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temprature = new DecimalFormat("##.##").format(tempInCelcius);
                    String title = "Current Weather";
                    String message = currentWeather +", "+description+" with "+temprature+" celcius";
                    int notifId = 100;
                    showNotification(getApplicationContext(), title, message, notifId);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("GetWeather", "Failed");
            }
        });
    }
    @Override
    //Method ini akan dieksekusi ketika library GooglePlayService pada device pengguna diupdate
    // sehingga mengharuskan aplikasi kita untuk menjadwalkan ulang proses terjadwal
    // yang sudah diset sebelumnya.
    public void onInitializeTasks() {
        super.onInitializeTasks();
        SchedulerTask mSchedulerTask = new SchedulerTask(this);
        mSchedulerTask.createPeriodicTask();
    }
    private void showNotification(Context context, String title, String message, int notifId){
        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        notificationManagerCompat.notify(notifId, builder.build());
    }
}