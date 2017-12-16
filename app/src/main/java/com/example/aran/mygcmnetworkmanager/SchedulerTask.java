package com.example.aran.mygcmnetworkmanager;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

/**
 * Created by aran on 16/12/17.
 */
//Kelas SchedulerTask merupakan kelas yang memiliki fungsi
// untuk membuat dan membatalkan penjadwalan tugas yang ditentukan
public class SchedulerTask {
    private GcmNetworkManager mGcmNetworkManager;
    public SchedulerTask(Context context){
        mGcmNetworkManager = GcmNetworkManager.getInstance(context);
    }
    //merepresentasikan pembuatan sebuah task yang akan dijalankan secara terjadwal oleh sistem android
    public void createPeriodicTask() {
        Task periodicTask = new PeriodicTask.Builder()
                //setService(). Method ini menentukan GcmTaskService yang akan dijalankan ketika kriteria dipenuhi
                .setService(SchedulerService.class)
                //setPeriod(). Method ini menentukan interval task yang akan dijalankan dalam satuan detik.
                .setPeriod(60)
                //setFlex(). Method ini menentukan range waktu untuk ekseskusi task yang akan dijalankan.
                // Misal kita set period = 30 dan flex = 10 maka task akan dijalankan di range antara 20 sampai 30.
                .setFlex(10)
                //setTag(). Method ini untuk set nilai tag dari task yang akan dijalankan
                .setTag(SchedulerService.TAG_TASK_WEATHER_LOG)
                //setPersisted()
                // Method ini akan membuat semua task yang akan dijalankan dipertahankan ketika terjadi proses reboot device.
                .setPersisted(true)
                .build();
        //Setelah selesai pembuatan obyek task
        // selanjutnya kita hanya tinggal men-set task tersebut untuk dijalankan oleh GcmNetworkManager seperti ini.
        mGcmNetworkManager.schedule(periodicTask);
    }
    //Method ini akan membatalkan proses terjadwal yang sudah kita set
    // berdasarkan tag dan kelas Service-nya.
    // Setelah dibatalkan maka semua proses terjadwal akan dihapus dari sistem android
    public void cancelPeriodicTask(){
        if (mGcmNetworkManager != null){
            mGcmNetworkManager.cancelTask(SchedulerService.TAG_TASK_WEATHER_LOG, SchedulerService.class);
        }
    }
}