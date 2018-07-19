package com.ydhnwb.comodity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.ydhnwb.comodity.IntroAct.SliderActivity
import com.ydhnwb.comodity.Model.UserModel
import com.ydhnwb.comodity.Utilities.Constant
import com.ydhnwb.comodity.Utilities.Constant.Companion.RC_SIGN_IN
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mGoogleSignInCient: GoogleSignInClient
    private val TAG = "LOGIN_ACT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        init()
        initComponents()
        signIn()
        val firstStart = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constant.NONAKTIFKAN_PERTAMA_KALI, true)
        if (firstStart) {
            val intent = Intent(this, SliderActivity::class.java)
            startActivityForResult(intent, Constant.REQUEST_CODE)
            //return
        }

        close_login.setOnClickListener({
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            processResult(task)
        }
        if (requestCode == Constant.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(Constant.NONAKTIFKAN_PERTAMA_KALI, false)
                        .apply()
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(Constant.NONAKTIFKAN_PERTAMA_KALI, true)
                        .apply()
                finish()
            }
        }
    }

    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val firebaseuser = it.currentUser
            if (firebaseuser != null) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
        mAuth.addAuthStateListener(mAuthStateListener)
    }
    override fun onStart() {
        changeStatusbarBackground()
        mAuth.addAuthStateListener(mAuthStateListener)
        super.onStart()
    }
    private fun changeStatusbarBackground(){
        if(Build.VERSION.SDK_INT >= 21){
            val w = window
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = getDarkColor(Color.WHITE,0.5)
        }
    }
    private fun getDarkColor(i : Int, value : Double) : Int{
        val r = Color.red(i)
        val g = Color.green(i)
        val b = Color.blue(i)
        return Color.rgb((r*value).toInt(), (g*value).toInt(), (b*value).toInt())
    }
    override fun onStop() {
        mAuth.removeAuthStateListener(mAuthStateListener)
        super.onStop()
    }
    private fun initComponents() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInCient = GoogleSignIn.getClient(this, googleSignInOptions)
    }
    private fun signIn() {
        google_sign_in.setOnClickListener({
            val intentActResult = mGoogleSignInCient.signInIntent
            startActivityForResult(intentActResult, Constant.RC_SIGN_IN)
        })
    }
    private fun processResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: Exception) {
            Log.d(TAG, "processResult : " + e.message)
        }
    }
    private fun goToMainActivity(){
        val intent = Intent(this@LoginActivity,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        try {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
            mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val user: FirebaseUser? = mAuth.currentUser
                    if (user != null) {
                        if(updateUI(user)){
                            goToMainActivity()
                        }
                    }
                } else if (!task.isSuccessful) {
                    Toast.makeText(this, "Failed to login", Toast.LENGTH_LONG).show()
                } else {
                    print("Something went wrong..")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "firebaseAuth " + e.message)
        }
    }

    private fun updateUI(firebaseUser : FirebaseUser):Boolean{
        val mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constant.USERS)
        val userModel = UserModel(firebaseUser.uid,firebaseUser.displayName.toString(),firebaseUser.displayName.toString().toLowerCase(),firebaseUser.email.toString(),firebaseUser.photoUrl.toString())
        mDatabaseReference.child(userModel.uid).setValue(userModel)
        return true
    }
}
