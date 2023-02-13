package com.BoldRope.Walker

import android.app.Application
import com.BoldRope.Walker.utils.Constants
import com.Catch.OrRunOr.utils.SharedPref
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import java.util.*

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        ViewPump.init(RewordInterceptor)
        if (SharedPref(this).getString(Constants.KEY_LANGUAGE) == Constants.RU) {
            AppLocale.desiredLocale = Locale(Constants.RU)
        }
    }
}