package com.dayzerostudio.primer.ui

import android.content.Context
import android.os.*

fun vibrate(context: Context, pattern: LongArray, repeat: Int = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vib = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vib.vibrate(
            CombinedVibration.createParallel(
                VibrationEffect.createWaveform
                    (pattern, repeat)
            )
        )
    } else {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(
            VibrationEffect.createWaveform
                (pattern, repeat)
        )
    }
}
