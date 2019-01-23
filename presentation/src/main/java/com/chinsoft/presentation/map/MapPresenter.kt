package com.chinsoft.presentation.map

import com.chinsoft.domain.entities.ShelterEntity

interface MapPresenter {

    fun initialize(mapScreenView: MapScreenView, shelterList: List<ShelterEntity>)

    fun onMapReady()

    fun onMarkerClicked(tag: String)

    fun onInfoClicked(pointEntity: ShelterEntity)

    fun onNavigationClicked(pointEntity: ShelterEntity)

    fun onRedimFooterClicked()

}