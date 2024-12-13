package com.il4mb.edudoexam.components

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

class AppContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {

        fun wrap(context: Context, language: String): ContextWrapper {
            var newContext = context
            val config: Configuration = context.resources.configuration

            val currentLocale: Locale = getSystemLocale(config)

            if (language.isNotEmpty() && currentLocale.language != language) {
                val newLocale = Locale(language)
                Locale.setDefault(newLocale)

                setSystemLocale(config, newLocale)
            }

            newContext = newContext.createConfigurationContext(config)
            return AppContextWrapper(newContext)
        }

        fun getSystemLocale(config: Configuration): Locale {
            return config.locales[0]
        }

        fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}
