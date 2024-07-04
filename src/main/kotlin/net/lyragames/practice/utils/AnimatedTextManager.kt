package net.lyragames.practice.utils

import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class AnimatedTextManager(
    private var mainColor: String = "&b",
    private var trailColor: String = "&f",
    private var writeEffect: Boolean = false,
    private var colorTrailEffect: Boolean = false,
    private var flashOnFinish: Boolean = false,
    private var typeTextBar: Boolean = false,
    private var typeBar: String = "|",
    private var barDuration: Long = 500L,
    private val updateInterval: Long = 100L
) {

    private val textFragments = mutableListOf<String>()
    private val textIndex = AtomicInteger(0)
    private val charIndex = AtomicInteger(0)
    private var lastUpdateTime = System.currentTimeMillis()
    private var lastBarUpdateTime = System.currentTimeMillis()
    private var currentDisplay = ""
    private var flashState = false
    private var isFlashing = false
    private var isDeleting = false
    private var barVisible = true
    private var isWaiting = false
    private var waitEndTime = 0L
    private var finished = false
    private var flashing = false
    private var colorTrailEffectFinished = false
    private var flashEndTime = 0L
    private var lastAnimation: String = ""

    fun setTextFragments(newTextFragments: List<String>) {
        textFragments.clear()
        textFragments.addAll(newTextFragments)
    }

    fun getText(): String {
        updateTextIfNeeded()
        return currentDisplay
    }

    private fun updateTextIfNeeded() {
        val currentTime = System.currentTimeMillis()

        if (isWaiting) {
            updateBar(currentTime)
            if (currentTime >= waitEndTime) {
                isWaiting = false
                if (finished) {
                    finished = false
                    if (flashOnFinish) {
                        isFlashing = true
                        flashEndTime = currentTime + 1000L
                    } else {
                        resetAnimation()
                    }
                }
            }
        } else {
            if (currentTime - lastUpdateTime >= updateInterval) {
                if (isFlashing) {
                    flashState = !flashState
                    if (currentTime >= flashEndTime) {
                        isFlashing = false
                        resetAnimation()
                    }
                } else if (isDeleting) {
                    if (charIndex.get() > 0) {
                        charIndex.decrementAndGet()
                    } else {
                        isDeleting = false
                        isWaiting = true
                        finished = true
                        if (writeEffect)
                            waitEndTime = currentTime + 1000L
                        lastAnimation = "delete"
                    }
                } else {
                    val currentText = stripColorCodes(textFragments[textIndex.get()])
                    if (charIndex.get() < currentText.length) {
                        charIndex.incrementAndGet()
                    } else {
                        isWaiting = true
                        finished = true
                        if (writeEffect)
                            waitEndTime = currentTime + 1000L
                        lastAnimation = when {
                            colorTrailEffect -> "colorTrail"
                            writeEffect -> "write"
                            else -> ""
                        }
                    }
                }
                lastUpdateTime = currentTime
            }
        }

        updateBar(currentTime)

        val text = textFragments[textIndex.get()]
        val index = charIndex.get()

        currentDisplay = when {
            isFlashing -> buildFlashEffectText(text)
            colorTrailEffect -> buildColorTrailText(text, index).also {
                if (index == stripColorCodes(text).length) {
                    colorTrailEffectFinished = true
                    if (!flashOnFinish) resetAnimation()
                }
            }
            writeEffect -> buildWriteEffectText(text, index)
            else -> "$mainColor${stripColorCodes(text)}"
        }
    }

    private fun resetAnimation() {
        if (lastAnimation == "delete" || !writeEffect) {
            charIndex.set(0)
            textIndex.updateAndGet { (it + 1) % textFragments.size }
        } else {
            isDeleting = true
        }
    }

    private fun updateBar(currentTime: Long) {
        if (currentTime - lastBarUpdateTime >= barDuration) {
            barVisible = !barVisible
            lastBarUpdateTime = currentTime
        }
    }

    private fun buildColorTrailText(text: String, index: Int): String {
        val strippedText = stripColorCodes(text)
        val colorCodedText = applyColorCodes(text)
        val realIndex = getRealIndex(text, index)
        return buildString {
            append(trailColor)
            append(colorCodedText.substring(0, realIndex))
            append(mainColor)
            append(colorCodedText.substring(realIndex))
        }
    }

    private fun buildWriteEffectText(text: String, index: Int): String {
        val strippedText = stripColorCodes(text)
        val colorCodedText = applyColorCodes(text)
        val realIndex = getRealIndex(text, index)
        return if (typeTextBar) {
            if (isDeleting) {
                colorCodedText.substring(0, realIndex) + if (barVisible) typeBar else " "
            } else if (index == strippedText.length) {
                if (isFlashing) {
                    colorCodedText.substring(0, realIndex) + if (flashState) " " else typeBar
                } else {
                    colorCodedText.substring(0, realIndex) + if (barVisible) typeBar else " "
                }
            } else {
                colorCodedText.substring(0, realIndex) + if (barVisible) typeBar else " "
            }
        } else {
            colorCodedText.substring(0, realIndex)
        }
    }

    private fun buildFlashEffectText(text: String): String {
        return if (flashState) {
            "$mainColor${stripColorCodes(text)}"
        } else {
            "$trailColor${stripColorCodes(text)}"
        }
    }

    private fun stripColorCodes(text: String): String {
        return text.replace("&[0-9a-fk-or]".toRegex(), "")
    }

    private fun applyColorCodes(text: String): String {
        return text.replace("&", "§")
    }

    private fun getRealIndex(text: String, strippedIndex: Int): Int {
        val matcher = Pattern.compile("&[0-9a-fk-or]").matcher(text)
        var realIndex = strippedIndex
        while (matcher.find() && matcher.start() < realIndex) {
            realIndex += matcher.end() - matcher.start()
        }
        return realIndex
    }

    fun updateConfig(
        mainColor: String,
        trailColor: String,
        writeEffect: Boolean,
        colorTrailEffect: Boolean,
        flashOnFinish: Boolean,
        typeTextBar: Boolean,
        typeBar: String,
        barDuration: Long
    ) {
        this.mainColor = mainColor
        this.trailColor = trailColor
        this.writeEffect = writeEffect
        this.colorTrailEffect = colorTrailEffect
        this.flashOnFinish = flashOnFinish
        this.typeTextBar = typeTextBar
        this.typeBar = typeBar
        this.barDuration = barDuration
    }

    fun isColorTrailEffectFinished(): Boolean {
        return colorTrailEffectFinished
    }
}