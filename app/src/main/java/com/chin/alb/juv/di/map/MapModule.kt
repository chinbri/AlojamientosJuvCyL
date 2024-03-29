package com.chin.alb.juv.di.map

import com.chin.presentation.map.MapPresenter
import com.chin.presentation.map.MapPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class MapModule {

    @Provides
    @MapScope
    fun providesMapPresenter(impl: MapPresenterImpl): MapPresenter = impl

}