package com.il4mb.edudoexam.components.markdown

import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.tools.Debouncer
import com.il4mb.edudoexam.tools.SimpleTextWatcher
import com.il4mb.edudoexam.tools.Utils.Companion.dp
import io.noties.markwon.Markwon

class RichTextInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val rawTextView: EditText by lazy {
        EditText(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            gravity = Gravity.TOP
            addTextChangedListener(object : SimpleTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    markwon.toMarkdown(s.toString()).apply {
                        textLayout.editText?.setText(this)
                    }
                }
            })
        }
    }

    private var isBlocked = false
    private val debounce = Debouncer()
    private val markwon: Markwon by lazy {
        Markwon.builder(context)
            .build()
    }
    private val textLayout: TextInputLayout by lazy {
        TextInputLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            hint = "Enter text here..."
            addView(CustomTextInput(this.context).apply {
                /*
                addTextChangedListener(object : SimpleTextWatcher() {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        rawText = s
                    }
                    override fun afterTextChanged(s: Editable?) {
//                        if (!isBlocked) {
//                            renderMarkdown(s?.toString() ?: "")
//                        }
                    }
                })
                 */
            })
        }
    }
    private val actionToolbar: ActionToolbar by lazy {
        ActionToolbar(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 5.dp)
            }
        }
    }

    init {
        orientation = VERTICAL
        addView(actionToolbar)
        addView(textLayout)
        addView(rawTextView)
    }

    private fun renderMarkdown(markdownText: String) {
        isBlocked = true
        markwon.toMarkdown(markdownText).apply {
            textLayout.editText?.setText(this)
            isBlocked = false
        }
    }

    class CustomTextInput @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
    ) : AppCompatEditText(context, attrs, defStyleAttr), ActionMode.Callback {
        private val markwon: Markwon by lazy {
            Markwon.builder(context)
                .build()
        }
        init {
            customSelectionActionModeCallback = this
            customInsertionActionModeCallback = this
            gravity = Gravity.TOP
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            minHeight = 100.dp
        }

        override fun onSelectionChanged(selStart: Int, selEnd: Int) {
            super.onSelectionChanged(selStart, selEnd)
            if(selEnd > selStart) {
                val (rawMarkdown, selectionRange) = getSelectedMarkdownRange()

                Log.d("Markwon", "Raw Markdown: $rawMarkdown")
                Log.d("Markwon","Selection Range: ${selectionRange.first} - ${selectionRange.second}")

            }
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.ritch_text_menu, menu)
            return true
        }
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // No need to update the menu
        }
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_bold -> {
                    val start = selectionStart
                    val end = selectionEnd

                    if (start in 0 until end) {
                        // Extract the selected text
                        val selectedText = text?.substring(start, end) ?: ""

                        // Wrap the selected text with markdown syntax for bold
                        val wrappedText = "**$selectedText**"

                        // Replace the selected text with the wrapped text
                        text?.replace(start, end, wrappedText)

                        // Re-apply markdown rendering
                        renderMarkdown()
                    }
                    mode?.finish()
                    return true
                }
                R.id.action_italic -> {
                    val start = selectionStart
                    val end = selectionEnd

                    if (start in 0..< end) {
                        val selectedText = text?.substring(start, end)
                        val wrappedText = "_${selectedText}_"
                        text?.replace(start, end, wrappedText)
                    }
                    mode?.finish()
                    return true
                }
            }
            return false
        }
        override fun onDestroyActionMode(mode: ActionMode?) {

        }

        private fun renderMarkdown() {
            // Parse and render the markdown using Markwon
            val rawText = text.toString()
            val spannedText = markwon.toMarkdown(rawText)
            setText(spannedText)

            // Ensure the cursor remains in position after re-rendering
            setSelection(length())
        }

        private fun getSelectedMarkdownRange(): Pair<String, Pair<Int, Int>> {
            val spannedText = text
            val start = selectionStart
            val end = selectionEnd

            // Get the selected text (with spans) from the Spanned text
            val selectedSpannedText = spannedText?.subSequence(start, end)

            // Now, convert the whole Spanned text to raw markdown (without spans)
            val rawMarkdownText = convertSpannedToMarkdown(spannedText.toString())
            Log.d("Markwon", "Raw Markdown: $rawMarkdownText")

            // Find the position of the selected text in the raw markdown string
            val rawStart = rawMarkdownText.indexOf(selectedSpannedText.toString())
            val rawEnd = rawStart + (selectedSpannedText?.length ?: 0)

            // Return the raw markdown text and the selected range
            return Pair(rawMarkdownText, Pair(rawStart, rawEnd))
        }

        // Function to convert Spanned text to raw markdown
        private fun convertSpannedToMarkdown(spannedText: String): String {
            // Here, you would convert the Spanned text to raw markdown format manually or by using a markdown converter.
            // For simplicity, let's assume the text is already in raw markdown or use Markwon's API to render it to markdown.
            return markwon.toMarkdown(spannedText).toString()
        }

    }
}
