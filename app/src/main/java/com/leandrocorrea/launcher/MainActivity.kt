package com.leandrocorrea.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(4, 5, 8)
    private val panel = Color.rgb(17, 20, 27)
    private val cyan = Color.rgb(110, 231, 249)
    private val white = Color.WHITE
    private val muted = Color.rgb(156, 163, 175)

    private var soundsEnabled = true
    private var navigationSounds = true
    private var appSounds = true
    private var animationsEnabled = true
    private var clockVisible = true
    private var soundVolume = 50
    private var settingsVisible = false
    private var tone: ToneGenerator? = null
    private lateinit var root: LinearLayout
    private lateinit var clockText: TextView
    private lateinit var dateText: TextView
    private lateinit var statusText: TextView
    private val handler = android.os.Handler()
    private val clockRunnable = object : Runnable {
        override fun run() {
            if (::clockText.isInitialized) {
                val now = Date()
                clockText.text = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(now)
                dateText.text = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
                    .format(now).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
                clockText.visibility = if (clockVisible) View.VISIBLE else View.GONE
                dateText.visibility = if (clockVisible) View.VISIBLE else View.GONE
            }
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        try { tone = ToneGenerator(AudioManager.STREAM_MUSIC, soundVolume) } catch (_: Exception) {}
        buildHome()
        handler.post(clockRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockRunnable)
        tone?.release()
        tone = null
        super.onDestroy()
    }

    private fun buildHome() {
        settingsVisible = false
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(34), dp(22), dp(34), dp(24))
            isFocusable = true
            isFocusableInTouchMode = true
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(root, ScrollView.LayoutParams(-1, -2))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "LEANDRO TV HUB"
            textSize = 18f
            setTextColor(cyan)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.12f
        }
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val settingsBtn = makeButton("⚙  CONFIGURAÇÕES") { playNav(); buildSettings() }
        top.addView(settingsBtn)
        root.addView(top, LinearLayout.LayoutParams(-1, -2))
        addSpace(root, 22)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(18), dp(26), dp(18))
            background = rounded(panel, 22)
        }
        clockText = TextView(this).apply {
            textSize = 58f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        dateText = TextView(this).apply {
            textSize = 17f
            setTextColor(muted)
        }
        statusText = TextView(this).apply {
            text = "Sua central de entretenimento"
            textSize = 15f
            setTextColor(cyan)
            setPadding(0, dp(14), 0, 0)
        }
        hero.addView(clockText)
        hero.addView(dateText)
        hero.addView(statusText)
        root.addView(hero, LinearLayout.LayoutParams(-1, -2))
        addSpace(root, 24)

        addSection(root, "STREAMING", listOf(
            Triple("▶  Flix Cine", "flix", "Abrir Flix Cine"),
            Triple("▶  YouTube", "youtube", "Abrir YouTube"),
            Triple("▣  TCL Channel", "tcl", "Abrir TCL Channel")
        ))
        addSpace(root, 18)
        addSection(root, "GAMES", listOf(
            Triple("🎮  Xbox Series S", "xbox", "Dica: selecione a entrada HDMI do Xbox na TV"),
            Triple("🎮  Jogos instalados", "games", "Abrir jogos instalados")
        ))
        addSpace(root, 18)
        addSection(root, "MÚSICAS", listOf(
            Triple("♫  Spotify", "spotify", "Abrir Spotify"),
            Triple("♫  YouTube Music", "ytmusic", "Abrir YouTube Music")
        ))
        addSpace(root, 24)
        val footer = TextView(this).apply {
            text = "CRIADO POR LEANDRO CORRÊA  •  @teytus12"
            textSize = 12f
            setTextColor(Color.rgb(100, 108, 122))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(6))
        }
        root.addView(footer, LinearLayout.LayoutParams(-1, -2))
        setContentView(scroll)
        root.requestFocus()
    }

    private fun addSection(parent: LinearLayout, title: String, items: List<Triple<String, String, String>>) {
        val heading = TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.15f
        }
        parent.addView(heading)
        addSpace(parent, 10)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        items.forEachIndexed { index, item ->
            val button = makeButton(item.first) {
                playApp()
                when (item.second) {
                    "flix" -> launchByNames(listOf("flix cine", "flixcine", "flix"))
                    "youtube" -> launchByNames(listOf("youtube"))
                    "tcl" -> launchByNames(listOf("tcl channel", "tclchannel"))
                    "xbox" -> Toast.makeText(this, item.third, Toast.LENGTH_LONG).show()
                    "games" -> openGames()
                    "spotify" -> launchByNames(listOf("spotify"))
                    "ytmusic" -> launchByNames(listOf("youtube music", "yt music"))
                }
            }
            val lp = LinearLayout.LayoutParams(0, dp(64), 1f)
            if (index > 0) lp.marginStart = dp(10)
            row.addView(button, lp)
        }
        parent.addView(row, LinearLayout.LayoutParams(-1, -2))
    }

    private fun buildSettings() {
        settingsVisible = true
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(34), dp(24), dp(34), dp(24))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        scroll.addView(root, ScrollView.LayoutParams(-1, -2))
        val title = TextView(this).apply {
            text = "CONFIGURAÇÕES"
            textSize = 28f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(title)
        addSpace(root, 8)
        addText(root, "Personalize sua experiência na TV.", muted, 15f)
        addSpace(root, 24)

        addSettingRow("Efeitos sonoros", "Sons da interface", soundsEnabled) {
            soundsEnabled = it
            if (!it) { navigationSounds = false; appSounds = false }
            buildSettings()
        }
        addSettingRow("Som ao navegar", "Som ao mover e selecionar", navigationSounds) {
            navigationSounds = it
            if (it) soundsEnabled = true
            buildSettings()
        }
        addSettingRow("Som ao abrir/sair", "Efeito ao abrir aplicativos", appSounds) {
            appSounds = it
            if (it) soundsEnabled = true
            buildSettings()
        }
        addSpace(root, 8)
        addText(root, "VOLUME DOS EFEITOS: $soundVolume%", white, 16f)
        val volumeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        volumeRow.addView(makeButton("−") { soundVolume = (soundVolume - 10).coerceAtLeast(0); resetTone(); buildSettings() },
            LinearLayout.LayoutParams(dp(70), dp(54)))
        val volumeValue = TextView(this).apply {
            text = "$soundVolume%"
            textSize = 18f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }
        volumeRow.addView(volumeValue, LinearLayout.LayoutParams(0, dp(54), 1f))
        volumeRow.addView(makeButton("+") { soundVolume = (soundVolume + 10).coerceAtMost(100); resetTone(); buildSettings() },
            LinearLayout.LayoutParams(dp(70), dp(54)))
        root.addView(volumeRow)
        addSpace(root, 12)

        addSettingRow("Animações", "Transições e realce dos cartões", animationsEnabled) {
            animationsEnabled = it
            buildSettings()
        }
        addSettingRow("Relógio e data", "Exibir na tela inicial", clockVisible) {
            clockVisible = it
            buildSettings()
        }
        addSpace(root, 22)
        addText(root, "APARÊNCIA", cyan, 13f)
        addSpace(root, 10)
        addText(root, "Tema OLED preto — ativo", white, 18f)
        addText(root, "Fundo escuro para combinar com sua TV OLED.", muted, 14f)
        addSpace(root, 24)
        addText(root, "SOBRE", cyan, 13f)
        addSpace(root, 8)
        addText(root, "LEANDRO TV HUB", white, 20f)
        addText(root, "Criado por Leandro Corrêa", muted, 15f)
        val insta = makeButton("Instagram  @teytus12") {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/teytus12/"))) }
            catch (_: Exception) { Toast.makeText(this, "Não foi possível abrir o Instagram.", Toast.LENGTH_SHORT).show() }
        }
        root.addView(insta, LinearLayout.LayoutParams(-1, dp(54)))
        addSpace(root, 24)
        root.addView(makeButton("←  VOLTAR À TELA INICIAL") { playNav(); buildHome() },
            LinearLayout.LayoutParams(-1, dp(60)))
        setContentView(scroll)
        root.requestFocus()
    }

    private fun addSettingRow(label: String, subtitle: String, checked: Boolean, changed: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = rounded(panel, 16)
        }
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        addText(labels, label, white, 17f)
        addText(labels, subtitle, muted, 13f)
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(makeButton(if (checked) "ATIVADO" else "DESATIVADO") { changed(!checked) },
            LinearLayout.LayoutParams(dp(140), dp(48)))
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        root.addView(row, lp)
    }

    private fun makeButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(white)
            background = rounded(Color.rgb(27, 32, 42), 14)
            isFocusable = true
            isFocusableInTouchMode = true
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    if (animationsEnabled) {
                        view.animate().scaleX(1.035f).scaleY(1.035f).setDuration(120)
                            .setInterpolator(DecelerateInterpolator()).start()
                    }
                    view.background = rounded(Color.rgb(37, 53, 66), 14, cyan)
                    playNav()
                } else {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    view.background = rounded(Color.rgb(27, 32, 42), 14)
                }
            }
            setOnClickListener { action() }
        }
    }

    private fun launchByNames(names: List<String>) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val match = apps.firstOrNull { app ->
            val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
            names.any { label.contains(it.lowercase(Locale.ROOT)) }
        }
        if (match != null) {
            val intent = pm.getLaunchIntentForPackage(match.packageName)
            if (intent != null) {
                try { startActivity(intent); return }
                catch (_: ActivityNotFoundException) {}
            }
        }
        Toast.makeText(this, "Aplicativo não encontrado. Verifique se está instalado na TV.", Toast.LENGTH_LONG).show()
    }

    private fun openGames() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        try { startActivity(intent) } catch (_: Exception) {
            try { startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (_: Exception) {}
        }
    }

    private fun playNav() {
        if (soundsEnabled && navigationSounds && soundVolume > 0) {
            try { tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 35) } catch (_: Exception) {}
        }
    }

    private fun playApp() {
        if (soundsEnabled && appSounds && soundVolume > 0) {
            try { tone?.startTone(ToneGenerator.TONE_PROP_ACK, 90) } catch (_: Exception) {}
        }
    }

    private fun resetTone() {
        try { tone?.release() } catch (_: Exception) {}
        tone = try { ToneGenerator(AudioManager.STREAM_MUSIC, soundVolume) } catch (_: Exception) { null }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && settingsVisible) {
            playNav()
            buildHome()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun addText(parent: LinearLayout, text: String, color: Int, size: Float) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
        }, LinearLayout.LayoutParams(-1, -2))
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun rounded(color: Int, radiusDp: Int, stroke: Int? = null): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            if (stroke != null) setStroke(dp(2), stroke)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
