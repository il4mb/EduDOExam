package com.il4mb.edudoexam.components.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.tools.Utils.Companion.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.google.android.material.appbar.AppBarLayout as AppBarLayoutMaterial

@SuppressLint("ObjectAnimatorBinding", "ResourceAsColor")
class AppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppBarLayoutMaterial(context, attrs, defStyleAttr), ViewTreeObserver.OnGlobalLayoutListener {

    enum class CollapseStatus { COLLAPSED, EXPANDED, IDLE }

    val progressbar: ProgressBar by lazy {
        ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            progressDrawable = ContextCompat.getDrawable(context, R.drawable.progressbar_loading_bg)?.apply {
                setBounds(0, 0, 0, 0)
            }
            scaleY = 1f
            isIndeterminate = true
        }
    }
    val toolbar: Toolbar by lazy {
        Toolbar(context).apply {
            layoutParams  = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(12.dp, 12.dp, 12.dp, 12.dp)
            background    = ColorDrawable()
            elevation     = 0f
            gravity       = Gravity.START
        }
    }
    var title: String
        get() = toolbar.title.toString()
        set(value) {
            toolbar.title = value
        }

    var subtitle: String
        get() = toolbar.subtitle.toString()
        set(value) {
            toolbar.subtitle = value
        }

    private val collapsingToolbarLayout: CollapsingToolbarLayout by lazy {
        CollapsingToolbarLayout(context).apply {
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                }
            setContentScrimColor(Color.TRANSPARENT)
            setExpandedTitleColor(android.R.color.transparent)
            setCollapsedTitleTextColor(android.R.color.white)
        }
    }
    private val menuLayout: MenuLayout by lazy {
        MenuLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
            elevation = 0f
        }
    }
    private val customToolbar: LinearLayout by lazy {
        LinearLayout(context).apply {
            layoutParams = CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                55.dp
            ).apply {
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_OFF
                overScrollMode = OVER_SCROLL_NEVER
            }

            addView(toolbar, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            addView(menuLayout, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                setMargins(0, 0, 0, 0)
            })
            elevation = 0f
        }
    }
    private val container: FrameLayout by lazy {
        FrameLayout(context).apply {
            layoutParams = CollapsingToolbarLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
                parallaxMultiplier = 0f
            }
            elevation = 0f
        }
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scrollJob: Job? = null
    private var lastVerticalOffset = 0
    private var ignoredNextScroll = false

    private var collapseStatus: CollapseStatus = CollapseStatus.EXPANDED

    init {

        context.withStyledAttributes(attrs, R.styleable.AppBarLayout) {
            title    = getString(R.styleable.AppBarLayout_title)    ?: ""
            subtitle = getString(R.styleable.AppBarLayout_subtitle) ?: ""
        }
        LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            layoutParams = this
        }
        setupLayout()
        setupOutline()
        TransitionManager.beginDelayedTransition(this, ChangeBounds())

        addOnOffsetChangedListener { appBar, verticalOffset ->
            when {
                verticalOffset == 0 -> onAppBarExpanded()
                abs(verticalOffset) >= (appBar.totalScrollRange - 75.dp) -> onAppBarCollapsed()
                else -> onAppBarIdle()
            }

            if (lastVerticalOffset != verticalOffset && !ignoredNextScroll) {

                val initialLastOffset = lastVerticalOffset
                scrollJob?.cancel()

                scrollJob = scope.launch {
                    delay(100)

                    @SuppressLint("CONDITIONAL")
                    if (verticalOffset > initialLastOffset) {
                        onScrollDown()
                    } else if (verticalOffset < initialLastOffset) {
                        onScrollUp()
                    }
                    delay(200)
                    ignoredNextScroll = false
                }
                lastVerticalOffset = verticalOffset
                ignoredNextScroll = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("Parent", parent::class.java.name)
        (parent as? ViewGroup)?.setOnTouchListener { a, b ->
            Log.d("Parent", "Touch")
            ignoredNextScroll = true
            false
        }
    }

    private fun onScrollDown() {
        setExpanded(true, true)
        ignoredNextScroll = true
    }

    private fun onScrollUp() {
        setExpanded(false, true)
        ignoredNextScroll = true
    }

    private fun setupLayout() {
        addView(collapsingToolbarLayout)
        collapsingToolbarLayout.apply {

            addView(container, CollapsingToolbarLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            ).apply {
                gravity      = Gravity.BOTTOM
                topMargin    = 75.dp
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
                parallaxMultiplier = 3f
            })

            addView(progressbar, CollapsingToolbarLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 2.dp
            ).apply {
                gravity = Gravity.TOP
            })

            addView(customToolbar, CollapsingToolbarLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            ).apply {
                gravity      = Gravity.TOP
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
                overScrollMode = OVER_SCROLL_NEVER
            })
        }
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    private fun setupOutline() {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val width = view?.width?.toFloat() ?: 0f
                val height = view?.height?.toFloat() ?: 0f

                val radius = 75f
                outline?.setRoundRect(0, -radius.toInt(), width.toInt(), height.toInt(), radius)
            }
        }
    }

    private fun onAppBarExpanded() {
        collapseStatus = CollapseStatus.EXPANDED
    }

    private fun onAppBarCollapsed() {
        collapseStatus = CollapseStatus.COLLAPSED
    }

    private fun onAppBarIdle() {
        collapseStatus = CollapseStatus.IDLE
    }

    private fun animateOut(v: View, onAnimationEnd: () -> Unit) {

        val parent = v.parent as? View
        val parentTranslationY = parent?.y ?: 0f
        v.focusable = NOT_FOCUSABLE
        v.y = parentTranslationY - (v.height / 2) + 50
        v.animate()
            .setDuration(120)
            .translationY(if(collapseStatus == CollapseStatus.IDLE || collapseStatus == CollapseStatus.COLLAPSED) v.y - 20f else -20f)
            .alpha(0f)
            .withEndAction {
                onAnimationEnd()
            }
            .start()
    }

    override fun onGlobalLayout() {

        collapsingToolbarLayout.minimumHeight = customToolbar.height + 50
        CollapsingToolbarLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, 10.dp
        ).apply {
            collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
            progressbar.layoutParams = this
        }
    }

    fun addMenu(menuItem: MenuLayout.MenuItem) {
        menuLayout.addMenu(menuItem)
    }

    fun removeAllMenus() {
        menuLayout.removeAllViews()
    }

    fun setContentView(view: View?) {
        view?.let { newView ->
            setExpanded(true, false)
            newView.alpha = 0.6f
            newView.translationY = -20f
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.removeView(view)
            }
            container.addView(newView)

            newView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()

            val oldViews = container.children.toList()
            if (oldViews.size > 1) {
                val oldView = oldViews[0]
                animateOut(oldView) {
                    container.removeView(oldView)
                }
            }
        } ?: run {
            removeAllContentView()
        }
    }

    fun removeAllContentView(finished: (() -> Unit)? = null) {

        val children = container.children.toList()
        children.forEach { child ->
            animateOut(child) {
                removeContentView(child)
                if (child == children.last()) {
                    finished?.invoke()
                }
            }
        }
    }

    fun removeContentView(view: View) {
        animateOut(view) {
            TransitionManager.beginDelayedTransition(this, ChangeBounds())
            container.removeView(view)
        }
    }

}
