package com.craftrom.raiderstatus

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.craftrom.raiderstatus.databinding.ActivityItemWebViewBinding

class ItemWebViewActivity : SplashActivity() {

    private lateinit var menu: Menu
    private lateinit var webView: WebView
    private lateinit var webSettings: WebSettings
    private lateinit var pageUrl: String
    private lateinit var binding: ActivityItemWebViewBinding
    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView

    private var pageTitle: String? = null

    override fun showMainUI(savedInstanceState: Bundle?) {
        binding = ActivityItemWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initializeToolbarViews()
        setPageUrl()
        configureWebView()
        loadPage()
    }


    private fun setPageUrl() {
        val intent = intent
        pageUrl = intent.getStringExtra("feedItemUrl") ?: ""
        pageTitle = intent.getStringExtra("pageTitle") ?: pageUrl
    }



    private fun initializeToolbarViews() {
        supportActionBar?.apply {
            titleTextView = findViewById(R.id.toolbar_title)
            subtitleTextView = findViewById(R.id.toolbar_subtitle)

            titleTextView.maxLines = 1
            subtitleTextView.maxLines = 1

        }
    }
    private fun setPageTitle(title: String) {
        runOnUiThread {
            titleTextView.text = title
        }
    }

    private fun setPageSubtitle(subtitle: String) {
        runOnUiThread {
            subtitleTextView.text = subtitle
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView = findViewById(R.id.webview_feed_item)
        webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.minimumFontSize = MIN_FONT_SIZE
        val statusIcon = findViewById<ImageView>(R.id.status_icon)
        statusIcon.visibility = View.INVISIBLE

        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                val pageTitle = title ?: "Just a moment"
                setPageTitle(pageTitle)
            }
        }

        // Добавляем проверку безопасного соединения
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    setPageSubtitle(url)
                    return false
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                setPageSubtitle(url)
                if (webView.url?.startsWith("https://") == true) {
                    statusIcon.visibility = View.VISIBLE
                    statusIcon.setImageResource(R.drawable.ic_https)
                } else {
                    statusIcon.visibility = View.VISIBLE
                    statusIcon.setImageResource(R.drawable.ic_http)
                }
            }
        }

        if (!CookieManager.getInstance().acceptThirdPartyCookies(webView)) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }
    }

    private fun loadPage() {
        try {
            webView.loadUrl(pageUrl)
        } catch (e: Exception) {
            Log.d("loadError", e.message ?: "")
        }
    }
    private fun showError() {
        Toast.makeText(this, "An error occurred while loading the page", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_webview, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_copy_link -> {
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("feedItemUrl", pageUrl)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_open_in_browser -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack() // Возврат к предыдущей странице
        } else {
            super.onBackPressed()
        }
    }


    companion object {
        const val MIN_FONT_SIZE = 14
    }
}