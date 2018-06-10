package com.ydhnwb.comodity

import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import android.os.Build
import android.text.Spanned
import android.text.SpannableString
import android.text.style.TypefaceSpan




/**
 * Created by Prieyuda Akadita S on 22/05/2018.
 */
class SliderActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isButtonBackVisible = false
        isButtonNextVisible = false
        isButtonCtaVisible = true
        buttonCtaTintMode = IntroActivity.BUTTON_CTA_TINT_MODE_BACKGROUND
        val labelSpan = TypefaceSpan(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) "sans-serif-medium" else "sans serif")
        val label = SpannableString.valueOf("MULAI") //teks button
        label.setSpan(labelSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        buttonCtaLabel = label

        pageScrollDuration = 500
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in)
        }
        addSlide(SimpleSlide.Builder()
                .title("Selamat datang")
                .description("Komoditas.id memudahkan anda untuk menjual hasil tani")
                .image(R.drawable.art_canteen_intro1)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.color_dark_canteen)
                .build())

        addSlide(SimpleSlide.Builder()
                .title("Mudah digunakan")
                .description("Didesain untuk anda. Kenyamanan anda adalah nomor satu")
                .image(R.drawable.art_canteen_intro2)
                .backgroundDark(R.color.color_dark_canteen)
                .background(R.color.colorWhite)
                .build())

        addSlide(SimpleSlide.Builder()
                .title("Petani Berdaya")
                .description("Menjauhkan petani dari transaksi yang merugikan dan menghubungkannya dengan tangan terakhir")
                .image(R.drawable.art_canteen_intro3)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.color_dark_canteen)
                .build())


        autoplay(2500, IntroActivity.INFINITE) // 2500 adalah saat animasi berhenti sejenak yaitu selama 2.5 detik

    }


}