package com.chin.alb.juv.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.chin.alb.juv.R
import com.chin.domain.util.PreferenceUtils
import com.chin.alb.juv.utils.PreferenceUtilsImpl
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(val application: Application) {

    @Provides
    @Singleton
    fun providesApplication():Application = application

    @Provides
    @Singleton
    fun providesContext(): Context = application

    @Provides
    @Singleton
    @Named("datosAbiertosApiKey")
    fun provideDatosAbiertosApiKey(): String = application.resources.getString(R.string.datos_abiertos_api_key)

    @Provides
    @Singleton
    fun providesSharedPreferences(): SharedPreferences =
        application
            .getSharedPreferences("SHARED_PREFERENCES_SHELTERS", MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePreferenceUtils(impl: PreferenceUtilsImpl): PreferenceUtils = impl

}