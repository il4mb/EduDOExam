package com.il4mb.edudoexam.components.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
class MDXEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private var isLoaded = false
    private var markdown: String = ""
    private val onChangedListeners: MutableList<(String) -> Unit> = mutableListOf()

    init {

        settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            domStorageEnabled = true
        }
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.TRANSPARENT)

        addJavascriptInterface(MDXInterface { md ->
            if(isLoaded) {
                markdown = md
                onChangedListeners.forEach { listener ->
                    try {
                        val mainHandler = android.os.Handler(context.mainLooper)
                        mainHandler.post {
                            listener.invoke(md)
                        }
                    } catch (e: Exception) {
                        Log.e("MDXEditor", "Error in listener $listener: ${e.message}", e)
                    }
                }
            }
        }, "MDXInterface")

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                loadUrl("javascript:MDXInterface.setMarkdown('$markdown')")
                isLoaded = true
            }
        }
        loadUrl("file:///android_asset/mdx_editor.html")
    }


    fun setMarkdown(md: String) {
        if(isLoaded) {
            post {
                loadUrl("javascript:MDXInterface.setMarkdown('$md')")
            }
        } else {
            markdown = md
        }
    }

    fun addOnChangedListener(listener: (String) -> Unit) {
        onChangedListeners.add(listener)
    }

    fun removeOnChangedListener(listener: (String) -> Unit) {
        onChangedListeners.remove(listener)
    }

   class MDXInterface(val onReceivedMarkdown: (String) -> Unit) {
       @android.webkit.JavascriptInterface
       fun sendMarkdown(markdown: String) {
           onReceivedMarkdown(markdown)
       }
   }
}