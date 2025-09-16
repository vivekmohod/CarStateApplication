package com.example.carstateapplication.listener

interface CarDataSpeedCallback {
    fun onMaxSpeedSet(maxSpeed: Int)
    fun onSpeedChanged(speed: Float)
}