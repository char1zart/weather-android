package com.example.ch1zart.weatherapp

import org.jetbrains.anko.doAsync
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import org.json.JSONObject



object RemoteFetch {

    lateinit var data:JSONObject

    var latit = 46.4846
    var longit = 30.7326
    private var OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?lat="+46.4846+"&lon="+30.7326+"&units=metric&lang=ru&appid=e1ca96c743d1dfd9ba1ccea0ae574559"

    fun getJSON(lat:Float, lon:Float, find:Boolean, city: String): JSONObject? {

        if(!find) {
            OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&lang=ru&appid=e1ca96c743d1dfd9ba1ccea0ae574559"
        }
        else {
            OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q="+city+"&units=metric&lang=ru&appid=e1ca96c743d1dfd9ba1ccea0ae574559"
        }
            val url = URL(String.format(OPEN_WEATHER_MAP_API))
            val connection = url.openConnection() as HttpURLConnection

            val reader = BufferedReader(
                    InputStreamReader(connection.inputStream))

            val json = StringBuffer(1024)

            var line: String? = null

            while ({ line = reader.readLine(); line }() != null) {
                // System.out.println(line)
                json.append(line).append("\n")
                println(line)
            }

            reader.close()

            data = JSONObject(json.toString())

        if (data.getInt("cod") != 200) {
            return null
        }
        else
            return data
    }

}