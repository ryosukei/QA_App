package jp.techacademy.ryosuke.aono.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_questions.view.*
import java.util.*

class QuestionsListAdapter(context: Context): BaseAdapter() {
    // レイアウトを生成する為の変数
    private var mLayoutInflater: LayoutInflater
    // QuestionのList
    private var mQuestionArrayList = ArrayList<Question>()

    // レイアウトを生成する
    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        var convertView = view

        // ここでlist_questionsを紐付ける
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.list_questions,viewGroup,false)
        }

        val titleText = convertView!!.titleTextView as TextView
        titleText.text = mQuestionArrayList[position].title

        val nameText = convertView.nameTextView as TextView
        nameText.text = mQuestionArrayList[position].name

        val resText = convertView.resTextView as TextView
        val resNum = mQuestionArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = mQuestionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.imageView as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    override fun getItem(p0: Int): Any {
        return mQuestionArrayList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return mQuestionArrayList.size
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        mQuestionArrayList = questionArrayList
    }

}