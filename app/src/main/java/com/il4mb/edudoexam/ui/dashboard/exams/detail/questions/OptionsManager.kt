package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions

import com.il4mb.edudoexam.models.Option

class OptionsManager {

    private var options = mutableListOf<Option>()

    fun add(option: Option) {
        options.add(option)
        options = options.sortedBy { it.label }.toMutableList()
    }

    fun remove(option: Option) {
        options.remove(option)
        options = options.sortedBy { it.label }.toMutableList()
    }

    fun get(index: Int): Option {
        return options[index]
    }

    fun size(): Int {
        return options.size
    }

    fun clear() {
        options.clear()
    }

    fun set(index: Int, option: Option) {
        options[index] = option
        options = options.sortedBy { it.label }.toMutableList()
    }

    fun getOptions(): List<Option> {
        return options
    }

    fun setOptions(options: List<Option>) {
        this.options.clear()
        this.options.addAll(options)
    }

    fun update(index: Int, option: Option) {
        options[index] = option
        options = options.sortedBy { it.label }.toMutableList()
    }
}