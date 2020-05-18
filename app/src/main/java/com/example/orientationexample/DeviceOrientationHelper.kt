package com.example.orientationexample

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class DeviceOrientationHelper(context: Context) : SensorEventListener {

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val remapMatrix = FloatArray(9)
    private val resultObservable: PublishSubject<Float> = PublishSubject.create()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun getObservable(): Observable<Float> = resultObservable.hide()


    override fun onSensorChanged(event: SensorEvent) {
        //https://stackoverflow.com/questions/12800982/accelerometer-with-low-passfilter-in-android
        val alpha = 0.97f
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading[0] = alpha * accelerometerReading[0] + (1 - alpha) * event.values[0]
            accelerometerReading[1] = alpha * accelerometerReading[1] + (1 - alpha) * event.values[1]
            accelerometerReading[2] = alpha * accelerometerReading[2] + (1 - alpha) * event.values[2]
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerReading[0] = alpha * magnetometerReading[0] + (1 - alpha) * event.values[0]
            magnetometerReading[1] = alpha * magnetometerReading[1] + (1 - alpha) * event.values[1]
            magnetometerReading[2] = alpha * magnetometerReading[2] + (1 - alpha) * event.values[2]
        }
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun registerSensors() {
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                    this,
                    magneticField,
                    SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun unRegisterSensors() {
        sensorManager.unregisterListener(this)
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
        )

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_Z, remapMatrix)
        SensorManager.getOrientation(remapMatrix, orientationAngles)

        var degree = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        degree = ((degree + 270) % 360)
        println("llllllll $degree")
        resultObservable.onNext(degree)
    }


    //https://developer.android.com/guide/topics/sensors/sensors_position#kotlin
    //https://stackoverflow.com/questions/10221567/how-to-get-android-phone-orientation-matching-human-orientation
}
