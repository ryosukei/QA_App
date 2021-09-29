package jp.techacademy.ryosuke.aono.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.list_question_detail.view.*
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.util.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question, private var isFavorite: Boolean) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }
    private var mLayoutInflater: LayoutInflater? = null
    private lateinit var mFavoriteRef: DatabaseReference
    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFavoriteRef = FirebaseDatabase.getInstance().reference
    }
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        var convertView = view

        if(getItemViewType(position) == TYPE_QUESTION){
            if(convertView == null){
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail,viewGroup,false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body
            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if(bytes.isNotEmpty()){
                val image = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
            val btnView = convertView.findViewById<View>(R.id.favoriteBtnView) as Button
            // ログインしてなければボタンを表示しない
            val user = FirebaseAuth.getInstance().currentUser
            if(user == null){
                btnView.visibility = View.GONE
            }else{
                changeFavoriteText(btnView)
                // この質問がお気に入りに登録されているかどうかの判定
                if(isFavorite){
                    btnView.setOnClickListener { v ->
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val favoriteRef = mFavoriteRef.child("favorites").child(currentUser!!.uid).child(mQuestion.questionUid)
                        favoriteRef.removeValue()
                        isFavorite = !isFavorite
                        notifyDataSetChanged()
                    }
                }
                else{
                btnView.setOnClickListener { v ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val data =  HashMap<String, Any>()
                    data["title"] = mQuestion.title
                    data["body"] = body
                    data["name"] = name
                    // 画像をいい感じにする
                    if(bytes != null){
                        val bitmapString = Base64.encodeToString(bytes, Base64.DEFAULT)
                        data["bytes"] =  bitmapString
                    }
                    val favoriteRef = mFavoriteRef.child("favorites").child(currentUser!!.uid).child(mQuestion.questionUid)
                    favoriteRef.setValue(data)
                    isFavorite = !isFavorite
                    changeFavoriteText(btnView)
                    notifyDataSetChanged()
                }
            }
            }
        }else{
            if(convertView == null){
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer,viewGroup,false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body
            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name
        }
        return convertView
    }

    private fun changeFavoriteText(btnView:Button){
        if(isFavorite){
            btnView.setBackgroundColor(Color.rgb(255,247,102))
            btnView.setTextColor(Color.rgb(0,0,0))
            btnView.text = "お気に入り済み"
        }else{
            btnView.setBackgroundColor(Color.rgb(204,204,204))
            btnView.setTextColor(Color.rgb(0,0,0))
            btnView.text = "お気に入り"
        }
    }

    override fun getItem(p0: Int): Any {
        return mQuestion
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    // 質問か回答か
    override fun getItemViewType(position: Int): Int {
        return if(position == 0){
            TYPE_QUESTION
        }else{
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }
}