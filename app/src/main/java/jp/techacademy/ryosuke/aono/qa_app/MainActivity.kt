package jp.techacademy.ryosuke.aono.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64.decode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mGenre = 0
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mFavoriteArrayList: ArrayList<FavoriteQuestion>
    private lateinit var mAdapter: QuestionsListAdapter
    private var mGenreRef: DatabaseReference? = null
    private var mFavoriteRef: DatabaseReference? = null
    private var mFavoriteQuestionRef: DatabaseReference? = null
    private var isFavorite: Boolean = false
    private val mEventListener = object : ChildEventListener{
        // データをRealtimeDBから取得する作業
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
           val map = snapshot.value as Map<String,String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }
            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String,String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }
            val question = Question(title, body, name, uid, snapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }
        // 要素が変更されたとき
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?){
            val map = dataSnapshot.value as Map<String,String>

            // answerを探す
            for(question in mQuestionArrayList){
                if(dataSnapshot.key!!.equals(question.questionUid)){
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String,String>?
                    if(answerMap != null){
                        for(key in answerMap.keys){
                            val temp = answerMap[key] as Map<String,String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }
                }
            }
            mAdapter.notifyDataSetChanged()
        }
        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }

    }

    private val favoriteListener = object: ChildEventListener{
        override fun onCancelled(error: DatabaseError) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("Favorite", snapshot.value.toString())
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("tag", snapshot.value.toString())
            val map = snapshot.value as Map<String,String>;
            val uid = map["uid"] ?: ""
            val questionUid = snapshot.key as String
            val genreId = map["genre"].toString()
            val favoriteQuestion = FavoriteQuestion(genreId,uid, questionUid)
            mFavoriteArrayList.add(favoriteQuestion)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

    }

    // お気に入り質問を取得するときのリスナー
    private val favoriteQuestionListener = object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val map = dataSnapshot.value as Map<String,String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }
            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String,String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }
            val favoriteQuestion = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(favoriteQuestion)
            mAdapter.notifyDataSetChanged()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // ログイン済みのユーザーを取得
        val user = FirebaseAuth.getInstance().currentUser

        fab.setOnClickListener{ view ->

            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if(mGenre == 0){
                Snackbar.make(view, getString(R.string.question_no_select_genre),Snackbar.LENGTH_LONG).show()
            } else {

            }
            val user = FirebaseAuth.getInstance().currentUser
            // ログインしていなければログイン画面に遷移させる
            if(user == null){
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }else{
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext,QuestionSendActivity::class.java)
                intent.putExtra("genre",mGenre)
                startActivity(intent)
            }
        }

        // ドロワー関連
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        // ログイン済みのユーザーを取得
        if(user == null){
            Log.d("tag","isVisible")
            nav_view.menu.getItem(4).isVisible = false
        }
        nav_view.setNavigationItemSelectedListener(this)
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mFavoriteArrayList = ArrayList<FavoriteQuestion>()
        // 最初にお気に入り質問を取得しておく
        mFavoriteRef = mDatabaseReference.child("favorites").child(user!!.uid)
        mFavoriteRef!!.addChildEventListener(favoriteListener)
        mAdapter.notifyDataSetChanged()
        // adapterを準備したらlistview.で設定できる
        listView.setOnItemClickListener { parent, view, position, id ->
            // 質問のintent
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            // 該当の質問のお気に入りが存在しているのか確認する
            val user = FirebaseAuth.getInstance().currentUser
            if(user != null){
                for (data in mFavoriteArrayList) {
                    isFavorite = data.questionUid == mQuestionArrayList[position].questionUid
                }
            }
            intent.putExtra("isFavorite", isFavorite)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }

    // メニュードロワーに遷移する
    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 設定画面に遷移する
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem):Boolean{
        val id = item.itemId
        // ログイン済みのユーザーを取得
        val user = FirebaseAuth.getInstance().currentUser
        if(id == R.id.nav_hobby){
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        }else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        }else if(id == R.id.nav_favorite){
            toolbar.title = "お気に入り"
            mGenre = 5
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        // お気に入り以外の時
        if(mGenre < 5) {
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            listView.adapter = mAdapter
            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            // 選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef!!.removeEventListener(mEventListener)
            }
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        }else{
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            listView.adapter = mAdapter
            for(data in mFavoriteArrayList){
                    mFavoriteQuestionRef =  mDatabaseReference.child(ContentsPATH).child(data.genreId).child(data.questionUid)
                    Log.d("tag",mFavoriteQuestionRef.toString())
                    mFavoriteQuestionRef!!.addValueEventListener(favoriteQuestionListener)
            }
        }
        return true
    }
}