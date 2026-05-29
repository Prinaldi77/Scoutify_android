package com.pab.scoutify.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pab.scoutify.R
import java.util.Locale

class MapPickerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var defaultLat: Double = -6.9175 // Default Bandung
    private var defaultLng: Double = 107.6191

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        // Read initial coordinates if provided
        defaultLat = intent.getDoubleExtra("EXTRA_LATITUDE", -6.9175)
        defaultLng = intent.getDoubleExtra("EXTRA_LONGITUDE", 107.6191)

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize WebView
        webView = findViewById(R.id.webViewMap)
        
        // Force Software Rendering for maximum stability inside emulators
        webView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Recalculate Leaflet sizing
                webView.postDelayed({
                    webView.evaluateJavascript("if(typeof map !== 'undefined') { map.invalidateSize(); }", null)
                }, 500)
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(
                    this@MapPickerActivity,
                    "Peta Error: ${error?.description}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        webView.webChromeClient = WebChromeClient()

        // Bind JS interface for receiving coordinates from HTML/Leaflet map
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onLocationSelected(lat: Double, lng: Double) {
                runOnUiThread {
                    val resultIntent = Intent().apply {
                        putExtra("EXTRA_SELECTED_LATITUDE", lat)
                        putExtra("EXTRA_SELECTED_LONGITUDE", lng)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }

            @JavascriptInterface
            fun getCurrentLatitude(): Double {
                return defaultLat
            }

            @JavascriptInterface
            fun getCurrentLongitude(): Double {
                return defaultLng
            }
        }, "AndroidMapInterface")

        // Load the HTML Leaflet map directly from Assets
        try {
            val inputStream = assets.open("map_picker.html")
            val htmlContent = inputStream.bufferedReader().use { it.readText() }
            webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            webView.loadUrl("file:///android_asset/map_picker.html")
        }
    }
}
