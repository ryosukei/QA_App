package jp.techacademy.ryosuke.aono.qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.nameText
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.*

class SettingActivity : AppCompatActivity() {

    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY,"")
        nameText.setText(name)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // UIの初期設定
        title = getString(R.string.settings_titile)
        val user = FirebaseAuth.getInstance().currentUser
        changeButton.setOnClickListener{v->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            if(user == null) {
                // ログインしていない場合は何もしない
                Snackbar.make(v, "ログインを行ってください。", Snackbar.LENGTH_LONG).show()
            }else{
                // 変更した表示名をFirebaseに保存する
                val name2 = nameText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name2
                userRef.setValue(data)

                // 変更した表示名をPreferenceに保存する
                val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp2.edit()
                editor.putString(NameKEY,name)
                editor.commit()

                Snackbar.make(v, "表示名を変更しました。", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener{v->
            FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v, "ログアウトに成功しました。", Snackbar.LENGTH_LONG).show()
        }
    }
}