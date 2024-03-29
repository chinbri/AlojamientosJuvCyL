package com.chin.alb.juv.di

import com.chin.presentation.main.navigator.Navigator
import com.chin.alb.di.main.AddressSubComponent
import com.chin.alb.juv.di.main.MainSubComponent
import com.chin.alb.juv.di.map.MapSubComponent
import com.chin.alb.juv.di.settings.SettingsSubComponent
import dagger.Component
import kotlinx.coroutines.Job


@Component(
    modules = [ActivityModule::class],
    dependencies = [ApplicationComponent::class]
)
@ActivityScope
interface ActivityComponent {

    fun mainComponentBuilder(): MainSubComponent.Builder

    fun addressComponentBuilder(): AddressSubComponent.Builder

    fun mapComponentBuilder(): MapSubComponent.Builder

    fun settingsComponentBuilder(): SettingsSubComponent.Builder

    fun providesJob(): Job

    fun providesNavigator(): Navigator

}