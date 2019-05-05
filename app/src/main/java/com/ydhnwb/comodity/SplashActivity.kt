package com.ydhnwb.comodity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import com.ydhnwb.comodity.Utilities.NetworkManager
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR



class SplashActivity : AppCompatActivity() {

    private val waitHandler = Handler()
    private val waitCallback = Runnable {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        changeStatusbarBackground()
        supportActionBar!!.hide()
        if(NetworkManager.isConnected(this@SplashActivity)){
            waitHandler.postDelayed(waitCallback,1000)
        }else if(!NetworkManager.isConnected(this@SplashActivity)){
            waitHandler.postDelayed(waitCallback, 3000)
        }
    }



    private fun getDarkColor(i : Int, value : Double) : Int{
        val r = Color.red(i)
        val g = Color.green(i)
        val b = Color.blue(i)
        return Color.rgb((r*value).toInt(), (g*value).toInt(), (b*value).toInt())
    }

    private fun changeStatusbarBackground(){
        if(Build.VERSION.SDK_INT >= 21){
            val w = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = this.getDarkColor(Color.BLACK,1.0)
        }
    }

    private fun setLightStatusbar() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        val decorView = getWindow().getDecorView()
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
      }
    }

    override fun onDestroy() {
        waitHandler.removeCallbacks(waitCallback)
        super.onDestroy()
    }

    override fun onStart() {
        //setLightStatusbar()
        super.onStart()
    }
}
