package com.example.carstateapplication.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.carstateapplication.helper.CarPropertyHelper
import com.example.carstateapplication.listener.CarDataSpeedCallback

class CarPropertyService : Service() {
    private val binder: IBinder = CarPropertyServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        //Initialise CarPropertyHelper class which will do all the necessary work to fetch speed and
        // update database.
        CarPropertyHelper.getInstance()!!.init(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //To Unregister car instance initialized when init method called.
        CarPropertyHelper.getInstance()?.unRegisterCar()
    }

    /**
     * Class used for the client Binder.  Because this service always
     * runs in the same process as its clients, AIDL not required.
     */
    class CarPropertyServiceBinder : Binder() {
        fun addCarDataCallback(callbackInterface: CarDataSpeedCallback?) {
            CarPropertyHelper.getInstance()?.setCarDataCallback(callbackInterface)
        }
    }
}