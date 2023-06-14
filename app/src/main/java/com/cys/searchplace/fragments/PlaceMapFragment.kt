package com.cys.searchplace.fragments

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cys.searchplace.activities.MainActivity
import com.cys.searchplace.activities.PlaceUrlActivity
import com.cys.searchplace.databinding.FragmentPlaceMapBinding
import com.cys.searchplace.model.Place
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class PlaceMapFragment : Fragment() {

    lateinit var binding: FragmentPlaceMapBinding
    val mapView: MapView by lazy { MapView(requireContext()) } //맵뷰객체 생성

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaceMapBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // java code
        // java code
        binding.containerMapview.addView(mapView)
        mapView.setPOIItemEventListener(markerEventListener)

        //지도관련 설정( 지도위치, 마커추가 등.. )
        setMapAndMarkers()
    }

    private fun setMapAndMarkers() {
        // 맵 중심점 변경
        // 현재 내 위치 위도,경도 좌표
        var lat: Double = (activity as MainActivity).myLocation?.latitude ?: 37.5663
        var lng: Double = (activity as MainActivity).myLocation?.longitude ?: 126.9779

        var myMapPoint: MapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
        mapView.setMapCenterPointAndZoomLevel(myMapPoint, 5, true)
        mapView.zoomIn(true)
        mapView.zoomOut(true)

        // 내 위치 표시 마커 추가
        var marker = MapPOIItem()
        marker.apply {
            itemName = "ME"
            mapPoint = myMapPoint
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.YellowPin
        }
        mapView.addPOIItem(marker)

        // 검색 장소들의 마커 추가
        val documents: MutableList<Place>? =
            (activity as MainActivity).searchPlaceResponse?.documents
        documents?.forEach {
            val point: MapPoint = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())

            val marker = MapPOIItem().apply {
                mapPoint = point
                itemName = it.place_name
                markerType = MapPOIItem.MarkerType.RedPin
                selectedMarkerType = MapPOIItem.MarkerType.YellowPin
                // 마커객체에 보관하고 싶은 데이터가 있다면.
                //즉. 해당 마커에 관련된 정보를 가지고 있는 객체를 마커에 저장해놓기
                userObject = it
            }
            mapView.addPOIItem(marker)
        }
    }


    // 마커 or 말풍선 클릭 이벤트 리스너
    val markerEventListener: MapView.POIItemEventListener = object : MapView.POIItemEventListener {
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
            //마커 클릭했을때 발동!
        }

        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
            //deprecated... 아래 메소드로 대체
        }

        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            // 말풍선 터치했을때 발동
            //두번째 파라미터 p1 : 클릭한 마커 객체
            //if(p1?.userObject==null) return
            p1?.userObject ?: return

            val place: Place = p1?.userObject as Place

            val intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("place_url", place.place_url)
            startActivity(intent)
        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
            //마커를 드래그했을때..발동
        }
    }////////////////////////////////////////////////////////////


}