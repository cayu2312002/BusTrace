package co.edu.udea.compumovil.gr05_20212.bustrace

import android.content.ContentProviderClient
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class ProviderType{
    BASIC,
    GOOGLE
}

class LoginActivity : AppCompatActivity() {

    private lateinit var txtUser: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var client: GoogleSignInClient

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        textView= findViewById(R.id.signInWithGoogle)
        val options= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client=GoogleSignIn.getClient(this, options)
        textView.setOnClickListener {
            val intent= client.signInIntent
            startActivityForResult(intent, 10001)
        }

        MyToolbar().show(this, "Login", false)

        txtUser=findViewById(R.id.email)
        txtPassword=findViewById(R.id.password)
        auth= FirebaseAuth.getInstance()


        var consultas = Consultas()
        GlobalScope.launch(Dispatchers.IO) {
            var routes = consultas.getRoutes()
            for (i in routes) {
                Log.i("Route", i.id)
                Log.i("Route", i.name)
            }

            Log.i("RouteById", consultas.getRoutebyId("1").name)
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==10001){

            val task=GoogleSignIn.getSignedInAccountFromIntent(data)
            val account=task.getResult(ApiException::class.java)
            val credential=GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener{task->
                    if (task.isSuccessful){

                        val i=Intent(this, user_menu::class.java)
                        startActivity(i)
                    }
                    else{
                        Toast.makeText(this, task.exception?.message,Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null){
            val i=Intent(this, user_menu::class.java)
            startActivity(i)
        }
    }



    override fun onBackPressed() {

    }

    fun forgotPassword(view:View){
        startActivity(Intent(this, ForgotPassActivity::class.java))
    }
    fun register(view:View){
        startActivity(Intent(this, RegisterActivity::class.java))
    }
    fun login(view:View){
        if (view !=null){
            val hideMe=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        loginUser()
    }


    fun google(view: View){

    }


    private fun loginUser(){
        val user:String=txtUser.text.toString()
        val password:String=txtPassword.text.toString()

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password)){

            auth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this){
                    task->
                    if (task.isSuccessful){
                        action()
                    }
                    else{
                        val toast = Toast.makeText(this, "Error en la autenticación, por favor intente de nuevo.", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
        }
        else{
            val toast = Toast.makeText(this, "Los campos no pueden estar vacios.", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
            if (txtUser.length()==0){
                txtUser.setError("El correo no puede estar vacio.")
            }
            if(txtPassword.length()==0){
                txtPassword.setError("La contraseña no puede estar vacia.")
            }
        }
    }

    private fun action(){
        startActivity(Intent(this, user_menu::class.java))
    }
}