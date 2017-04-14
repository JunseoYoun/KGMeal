package com.pointstudio.kgshmealdataapp.utils;

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SeekBar
import android.widget.TextView
import com.androidquery.AQuery
import com.androidquery.callback.AjaxCallback
import com.androidquery.callback.AjaxStatus
import com.pointstudio.kgshmealdataapp.R
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*

open class MealManager(context: Context) {
    val url = "https://www.game.hs.kr/~game/2013/inner.php?sMenu=E4100"
    val url_food = "table.foodbox tbody tr"
    var aq: AQuery

    init {
        aq = AQuery(context)
    }

    open class MealData {
        var day: String = ""
        var data0: String = ""
        var data1: String = ""
        var data2: String = ""
        var data3: String = ""
        var data4: String = ""
        var data5: String = ""
        var mealMonthDay = ""
        var goodChoice = 0
        var badChoice = 0
        var goodCallback = object : View.OnClickListener {
            override fun onClick(p0: View?) {

            }
        }
        var badCallback = object : View.OnClickListener {
            override fun onClick(p0: View?) {

            }
        }

        constructor(day: String, data0: String, data1: String, data2: String, data3: String, data4: String, data5: String) {
            this.day = day
            this.data0 = data0
            this.data1 = data1
            this.data2 = data2
            this.data3 = data3
            this.data4 = data4
            this.data5 = data5
        }
    }

    class MealHolder(view: View) {
        val day: TextView
        val data0: TextView
        val data1: TextView
        val data2: TextView
        val data3: TextView
        val data4: TextView
        val data5: TextView
        val choice: SeekBar

        val good: TextView
        val bad: TextView

        init {
            this.day = view.findViewById(R.id.kg_meal_time) as TextView
            this.data0 = view.findViewById(R.id.kg_meal_item1) as TextView
            this.data1 = view.findViewById(R.id.kg_meal_item2) as TextView
            this.data2 = view.findViewById(R.id.kg_meal_item3) as TextView
            this.data3 = view.findViewById(R.id.kg_meal_item4) as TextView
            this.data4 = view.findViewById(R.id.kg_meal_item5) as TextView
            this.data5 = view.findViewById(R.id.kg_meal_item6) as TextView
            this.choice = view.findViewById(R.id.kg_meal_choice) as SeekBar
            this.good = view.findViewById(R.id.kg_meal_good) as TextView
            this.bad = view.findViewById(R.id.kg_meal_bad) as TextView
        }
    }

    class MealAdapter(context: Context) : BaseAdapter() {
        val inflater: LayoutInflater
        val meals = ArrayList<MealManager.MealData>()

        init {
            inflater = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return meals.count()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return meals[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: MealHolder?

            if (convertView == null) {
                view = inflater.inflate(R.layout.meal_item, parent, false)
                holder = MealHolder(view)
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as MealHolder
            }

            holder.day.text = meals[position].day
            holder.data0.text = meals[position].data0
            holder.data1.text = meals[position].data1
            holder.data2.text = meals[position].data2
            holder.data3.text = meals[position].data3
            holder.data4.text = meals[position].data4
            holder.data5.text = meals[position].data5
            holder.choice.max = meals[position].badChoice + meals[position].goodChoice
            holder.choice.progress = meals[position].goodChoice
            holder.good.setOnClickListener(meals[position].goodCallback)
            holder.bad.setOnClickListener(meals[position].badCallback)
            return view
        }
    }

    // 가져온 급식마다 callback을 호출한다. ( (아침, 점심, 저녁), 밥, 반찬1, 반찬2, 반찬3 ... )
    // day = ayOfWeek
    // mealDay
    // -1. 아침, 점심, 저녁
    // 1. 아침
    // 2. 점심
    // 3. 저녁
    fun getMeal(day: Int, mealDay: Int = -1, callback: ((md: MealData, time: Int) -> Unit)) {
        Thread {
            try {
                var meal_time_counter = 0
                val ssl = SSLConnect()
                ssl.postHttps(url, 1000, 1000)
                val doc = Jsoup.connect(url).get()
                val contents = doc.select(url_food)
                val day_contents = contents[day].children()[0].getElementsByTag("strong").text()
                val process = Handler(Looper.getMainLooper())
                process.postDelayed(Runnable {
                    var meal_days = ArrayList<Int>()
                    if (mealDay == -1) {
                        meal_days.add(1)
                        meal_days.add(3)
                        meal_days.add(5)
                    } else
                        meal_days.add(mealDay)

                    for (meal_day in meal_days) {
                        val data = contents[day].children()[meal_day].toString().splitKeeping("<br>", "<td>", "</td>")
                        // MainActivity.Instance.instance!!.main_title!!.text = day_contents
                        var meal_count = 0
                        val meal_content_datas = ArrayList<String>()
                        for (item in data) {
                            if (item.equals("<br>") || item.equals("<td>") || item.equals("</td>"))
                                continue
                            var m_item = ""
                            if (item[0] == ' ')
                                for (i in 1..item.length - 1)
                                    m_item += item[i]
                            else
                                m_item = item
                            meal_content_datas.add(meal_count, m_item)
                            meal_count += 1
                        }
                        if (meal_content_datas.size > 1) {
                            var day_string = "아침"
                            if (meal_day == 3)
                                day_string = "점심"
                            else if (meal_day == 5)
                                day_string = "저녁"
                            val meal = MealData(day_string, meal_content_datas[0], meal_content_datas[1], meal_content_datas[2], meal_content_datas[3], meal_content_datas[4], meal_content_datas[5])

                            meal.mealMonthDay = day_contents

                            callback.invoke(meal, meal_time_counter)
                            meal_time_counter += 1
                        }
                    }
                }, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun getChoice(date: String, time: Int, callback: (good: Int, bad: Int) -> Unit) {
        aq.ajax("http://junsueg5737.dothome.co.kr/KGMaster/KGMaster_getMeal.php?KG_CONTENTS_DATE=" + date, JSONObject().javaClass, object : AjaxCallback<JSONObject>() {
            override fun callback(url: String?, `object`: JSONObject?, status: AjaxStatus?) {
                if (`object` != null) {
                    val jsonObject: JSONObject = `object`.getJSONObject("m" + time.toString())
                    callback.invoke((jsonObject["good"] as String).toInt(), (jsonObject["bad"] as String).toInt())
                }
            }
        })
    }

    fun setChoice(date: String, time: Int, choice: Int, callback: (good: Int, bad: Int) -> Unit) {
        aq.ajax("http://junsueg5737.dothome.co.kr/KGMaster/KGMaster_setMeal.php?KG_CONTENTS_DATE=" + date +
                "&KG_CONTENTS_TIME=" + time.toString() +
                "&KG_CONTENTS_CHOICE=" + choice.toString(), JSONObject().javaClass, object : AjaxCallback<JSONObject>() {
            override fun callback(url: String?, `object`: JSONObject?, status: AjaxStatus?) {
                if (`object` != null) {
                    callback.invoke((`object`["good"] as String).toInt(), (`object`["bad"] as String).toInt())
                }
            }
        })
    }

    fun String.splitKeeping(str: String): List<String> {
        return this.split(str).flatMap { listOf(it, str) }.dropLast(1).filterNot { it.isEmpty() }
    }

    fun String.splitKeeping(vararg strs: String): List<String> {
        var res = listOf(this)
        strs.forEach { str ->
            res = res.flatMap { it.splitKeeping(str) }
        }
        return res
    }
}