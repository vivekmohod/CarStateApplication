package com.example.carstateapplication.helper

import android.annotation.SuppressLint
import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.carstateapplication.listener.CarDataSpeedCallback
import android.car.CarNotConnectedException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.data


class CarPropertyHelper private constructor() {
    private var mContext: Context? = null
    private var mCar: Car? = null
    private var mCarPropertyManager: CarPropertyManager? = null

    private val CAR_ROOT = "cars"
    private val MAX_SPEED = "maxspeed"
    private val MAX_SPEED_VIO = "speed_violation"
    private var mVinNo: String? = ""

    /**
     * Callback to listen for car speed change
     */
    private val mSpeedCallback: CarPropertyManager.CarPropertyEventCallback<Float> =
        object : CarPropertyManager.CarPropertyEventCallback<Float?>() {
            fun onChangeEvent(value: CarPropertyValue<Float?>?) {
                value?.let {
                    val speed: Float? = value.getValue()

                    speed?.let { it1 -> dataCallbackInterface.onSpeedChanged(it1) } // Sends current speed to CarDataViewModel to update UI

                    speed?.let {
                        if (speed > mMaxSpeed) {
                            updateCarOverSpeed(speed)
                            showWarningPopup(speed)
                        }
                    }
                }
            }

            fun onErrorEvent(propertyId: Int, errorCode: Int) {
                Log.e("SpeedReader", "Error getting speed: $errorCode")
                // Handle errors appropriately
            }
        }

    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mDatabaseRef: DatabaseReference = mDatabase.getReference()

    private var dataCallbackInterface: CarDataSpeedCallback? = null

    private var mMaxSpeed = 0f

    /**
     * This method initialises Car object which will be used to get Car telemetry data
     * To get seed data declare the necessary permissions in your AndroidManifest.xml file.
     * in this case <uses-permission android:name="android.car.permission.CAR_SPEED"></uses-permission>
     */
    fun init(context: Context?) {
        mContext = context
        mCar = Car(mContext) // 'this' is your Context
        try {
            mCar!!.connect()
            mCarPropertyManager = mCar!!.getCarManager(CarPropertyManager::class.java)
            mCarPropertyManager?.let {
                // Handle the case where CarPropertyManager is not available
                Log.e(
                    "SpeedReader",
                    "CarPropertyManager is null. Car service might not be available."
                )
                return@let
            }
            try {
                mCarPropertyManager.registerCallback(
                    mSpeedCallback,
                    CarProperty.VEHICLE_SPEED
                )
            } catch (e: IllegalArgumentException) {
                Log.e("SpeedReader", "Error registering for speed updates")
            } catch (e: IllegalStateException) {
                Log.e("SpeedReader", "Error registering for speed updates")
            } catch (e: CarNotConnectedException) {
                Log.e("SpeedReader", "Error registering for speed updates")
            }
        } catch (e: CarNotConnectedException) {
            Log.e("SpeedReader", "Car not connected")
        }

        mVinNo = vinNo
        mMaxSpeed = getMaxSpeedForCar(mVinNo)
    }

    /**
     * Callback to update UI
     */
    fun setCarDataCallback(callback: CarDataSpeedCallback?) {
        dataCallbackInterface = callback

        //Sends Maximum speed limit value to CarDataViewModel to update UI
        dataCallbackInterface.onMaxSpeedSet(getMaxSpeedForCar(mVinNo).toInt())
    }

    private val vinNo: String
        /**
         * It is assumed that each car data is arranged in firebase with VIN NO of each car.
         * So by using this method we are getting Cars VIN NO.
         */
        get() {
            var vinValue: CarPropertyValue<String?>? = null
            mCarPropertyManager?.let {
                try {
                    vinValue = mCarPropertyManager!!.getProperty(
                        String::class.java, CarProperty.VEHICLE_IDENTIFICATION_NUMBER, 0
                    )

                    vinValue?.let {
                        val vin: String? = vinValue!!.getValue()
                        Log.d("VIN", "VIN: $vin")
                        // Use the VIN (e.g., display it in your app)
                    } ?: run {
                        Log.e("VIN", "VIN value is null")
                        return@let null
                    }
                } catch (e: CarNotConnectedException) {
                    Log.e("VIN", "Error getting VIN")
                    // Handle exceptions
                } catch (e: IllegalArgumentException) {
                    Log.e("VIN", "Error getting VIN")
                } catch (e: IllegalStateException) {
                    Log.e("VIN", "Error getting VIN")
                }
            } ?: run {
                Log.e("VIN", "CarPropertyManager is null")
            }
            val vinValue1 = vinValue
            return if (vinValue1 != null) vinValue1.getValue().toString() else null.toString()
        }

    /**
     * Method to get max speed set by Company.
     * It is assumed that from another application car rental company sets max speed for each vehicle.
     * Vehicle is identified by car rental company using VIN NO.
     */
    private fun getMaxSpeedForCar(vinno: String?): Float {
        val maxspeed = intArrayOf(0)
        mDatabaseRef.child(CAR_ROOT).child(vinno).child(MAX_SPEED).get()
            .addOnCompleteListener(object : OnCompleteListener<DataSnapshot?> {
                override fun onComplete(task: Task<DataSnapshot?>) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException())
                    } else {
                        maxspeed[0] = java.lang.String.valueOf(task.getResult().getValue()).toInt()
                    }
                }
            })
        return maxspeed[0].toFloat()
    }

    /**
     * Just shows a Toast as warning. We can also implement a popup here.
     */
    private fun showWarningPopup(speed: Float) {
        Toast.makeText(
            mContext,
            "Max Speed Limit Exceeded. Max Speed is $speed",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * When max speed is exceeded, that value is updated in firebase. It is assumed that "speed_violation" node is
     * actively monitored by another application to notify violation to Car rental company.
     */
    private fun updateCarOverSpeed(speed: Float) {
        mDatabaseRef.child(CAR_ROOT).child(mVinNo).child(MAX_SPEED_VIO).push().setValue(speed)
    }

    fun unRegisterCar() {
        if (mCarPropertyManager != null) {
            mCarPropertyManager!!.unregisterCallback(mSpeedCallback)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var carPropertyHelper: CarPropertyHelper? = null
        fun getInstance(): CarPropertyHelper? {
            carPropertyHelper?.let {
                carPropertyHelper = CarPropertyHelper()
            }
            return carPropertyHelper
        }

    }
}