package com.example.ch1zart.weatherapp

import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import org.jetbrains.anko.locationManager
import org.jetbrains.anko.toast


class weathe_app : FragmentActivity() {

    private var mPager: ViewPager? = null

    private var mPagerAdapter: PagerAdapter? = null
    var position_p = 0

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weathe_app)

       if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

           mPager = findViewById(R.id.pager) as ViewPager
           mPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
           mPager!!.adapter = mPagerAdapter
       }
       else { toast("Проверь доступ к GPS")}

    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            position_p = position

            when (position) {
                0 -> return WeatherFragment()

            }
            return WeatherFragment()
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    companion object {

        private val NUM_PAGES = 2
    }
}
