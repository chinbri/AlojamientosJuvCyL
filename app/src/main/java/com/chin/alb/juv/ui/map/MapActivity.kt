package com.chin.alb.juv.ui.map

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.Toolbar
import com.chin.domain.entities.MapDataEntity
import com.chin.domain.entities.ShelterEntity
import com.chin.presentation.map.MapPresenter
import com.chin.presentation.map.MapScreenView
import com.chin.alb.juv.di.map.MapModule
import com.chin.alb.juv.ui.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.layout_map_footer.*
import javax.inject.Inject
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ScrollView
import com.chin.alb.juv.R
import com.chin.alb.juv.ui.images.ImageAdapter


class MapActivity : BaseActivity(), MapScreenView, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    @Inject
    lateinit var presenter: MapPresenter

    private lateinit var mMap: GoogleMap

    private var prevMarker: Marker? = null

    companion object {
        const val EXTRA_POINT_LIST = "EXTRA_POINT_LIST"
        private val gson = Gson()

        fun getLaunchIntent(
            activity: Activity,
            mapDataEntity: MapDataEntity
        ): Intent {
            val intent = Intent(activity, MapActivity::class.java)
            intent.putExtra(EXTRA_POINT_LIST, gson.toJson(mapDataEntity))
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        setSupportActionBar(customToolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        activityComponent
            .mapComponentBuilder()
            .mapModule(MapModule()).build()
            .inject(this)

        title = resources.getString(R.string.main_title)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mapDataEntity = Gson().fromJson<MapDataEntity>(
            intent.getStringExtra(EXTRA_POINT_LIST),
            MapDataEntity::class.java
        )

        presenter.initialize(
            this,
            mapDataEntity.shelterList,
            mapDataEntity.subtitle
        )
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_out_back, R.anim.slide_in_back)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        setupMapOptions(googleMap)

        mMap = googleMap

        presenter.onMapReady()
    }

    private fun setupMapOptions(googleMap: GoogleMap) {
        googleMap.setMinZoomPreference(6F)
        googleMap.setMaxZoomPreference(18F)
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false
    }

    override fun setupMarkers(shelterList: List<ShelterEntity>, onlyShelter: ShelterEntity?){

        shelterList.forEach {
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_marker_off))
            ).tag = it.id
        }

        mMap.setOnMarkerClickListener(this)
        moveCamera(shelterList, onlyShelter)

    }

    private fun moveCamera(
        shelterList: List<ShelterEntity>,
        onlyShelter: ShelterEntity?
    ){
        val builder = LatLngBounds.Builder()
        for (point in shelterList) {
            builder.include(LatLng(point.latitude, point.longitude))
        }
        val bounds = builder.build()

        val padding = 60 // offset from edges of the map in pixels
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        mMap.setOnMapLoadedCallback {
            mMap.setOnMapLoadedCallback(null)
            mMap.animateCamera(cu, object: GoogleMap.CancelableCallback {
                override fun onFinish() {
                    if(onlyShelter != null){
                        centerCameraInMarker(LatLng(onlyShelter.latitude, onlyShelter.longitude))
                    }
                }

                override fun onCancel() {
                    //do nothing
                }
            })
        }

    }

    private fun centerCameraInMarker(position: LatLng){

        val projection =  mMap.projection

        val latitudeOffset = (projection.visibleRegion.latLngBounds.northeast.latitude - projection.visibleRegion.latLngBounds.southwest.latitude)/3

        val cu = CameraUpdateFactory.newLatLng(LatLng(position.latitude - latitudeOffset, position.longitude))

        mMap.animateCamera(cu)

    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        if (marker != null
            && (prevMarker == null || marker != prevMarker)) {

            presenter.onMarkerClicked(marker.tag as String)

            prevMarker?.setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_marker_off))

            marker.setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_marker_on))

            prevMarker?.zIndex = Float.MAX_VALUE - 1
            marker.zIndex = Float.MAX_VALUE

            prevMarker = marker

            centerCameraInMarker(marker.position)

        }

        return true

    }

    private fun getEscapedSectionText(idString: Int, content: String?): SpannableString{
        val title = resources.getText(idString)
        val escapedHtml = Html.fromHtml(content ?: "")

        val spannablecontent = SpannableString("$title $escapedHtml")
        spannablecontent.setSpan(StyleSpan(Typeface.BOLD),
                         0,title.length, 0)

        return spannablecontent
    }


    override fun showShelterData(shelterEntity: ShelterEntity) {

        showFooter()

        tvName.text = shelterEntity.name
        tvDescription.text = shelterEntity.description
        wrapperFooterHeader.setOnClickListener {
            presenter.onRedimFooterClicked()
        }

        tvOpened.text = getEscapedSectionText(R.string.opened, shelterEntity.opened)
        tvOpened.visibility = if(shelterEntity.opened?.trim().isNullOrEmpty()) { View.GONE } else{ View.VISIBLE }

        tvCapacity.text = getEscapedSectionText(R.string.capacity, shelterEntity.capacity)
        tvCapacity.visibility = if(shelterEntity.capacity?.trim().isNullOrEmpty()) { View.GONE } else{ View.VISIBLE }

        tvEquipment.text = getEscapedSectionText(R.string.equipment, shelterEntity.equipment)
        tvEquipment.visibility = if(shelterEntity.equipment?.trim().isNullOrEmpty()) { View.GONE } else{ View.VISIBLE }

        tvActivities.text = getEscapedSectionText(R.string.activities, shelterEntity.activities)
        tvActivities.visibility = if(shelterEntity.activities?.trim().isNullOrEmpty()) { View.GONE } else{ View.VISIBLE }

        tvHobbies.text = getEscapedSectionText(R.string.hobbies, shelterEntity.hobbies)
        tvHobbies.visibility = if(shelterEntity.hobbies?.trim().isNullOrEmpty()) { View.GONE } else{ View.VISIBLE }

        tvDistance.text = resources.getString(R.string.distance_label_km, shelterEntity.distance)


        setupImageGallery(shelterEntity)

        wrapperDistance.visibility = when(shelterEntity.distance) {
            0F -> View.GONE
            else -> View.VISIBLE
        }

        if(shelterEntity.url.isNullOrEmpty()){
            ivInfo.visibility = View.GONE
        }else{
            ivInfo.visibility = View.VISIBLE
            ivInfo.setOnClickListener {
                presenter.onInfoClicked(shelterEntity)
            }
        }

        ivNavigation.setOnClickListener{
            presenter.onNavigationClicked(shelterEntity)
        }

        svMapFooterContent.fullScroll(ScrollView.FOCUS_UP)

    }

    override fun setSubtitle(subtitle: String){
        supportActionBar?.subtitle = subtitle
    }

    private fun setupImageGallery(shelterEntity: ShelterEntity) {
        if (shelterEntity.obtainImages().isNullOrEmpty()) {
            wrapperImages.visibility = View.GONE
            wrapperNoImages.visibility = View.VISIBLE
        } else {
            wrapperImages.visibility = View.VISIBLE
            wrapperNoImages.visibility = View.GONE
            pager.adapter = ImageAdapter(this, shelterEntity.obtainImages())
        }
    }


    private fun showFooter() {

        if(lyFooterMap.visibility != View.VISIBLE){
            val bottomUp = AnimationUtils.loadAnimation(
                this,
                R.anim.bottom_in
            )
            lyFooterMap.startAnimation(bottomUp)
            lyFooterMap.visibility = View.VISIBLE
        }

    }

    override fun minimizeFooter(){
        animateFooter(
            resources.getDimensionPixelSize(R.dimen.mapFooterHeight)
        )
    }

    override fun maximizeFooter(){
        animateFooter(0)
    }

    internal var currentDegrees = 0F

    private fun animateFooter(moveOffset: Int) {


        ObjectAnimator.ofFloat(lyFooterMap, "translationY", moveOffset.toFloat()).apply {
            duration = 400
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationEnd(animation: Animator?) {
                svMapFooterContent.fullScroll(ScrollView.FOCUS_UP)
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

        val nextDeg = (currentDegrees + 180F) % 360
        ObjectAnimator.ofFloat(ivMinimize, "rotation", nextDeg ).apply {
            duration = 400
            start()
        }

        currentDegrees = nextDeg
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        if(vectorDrawable != null){

            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)

        }
        return null

    }

}
