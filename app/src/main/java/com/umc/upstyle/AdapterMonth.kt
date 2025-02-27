package com.umc.upstyle


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.databinding.ListItemMonthBinding
import java.util.*

class AdapterMonth : RecyclerView.Adapter<AdapterMonth.MonthView>() {

    val center = Int.MAX_VALUE / 2
    private var calendar = Calendar.getInstance()

    inner class MonthView(val binding: ListItemMonthBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthView {
        // 뷰 바인딩으로 뷰 인플레이트
        val binding = ListItemMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthView(binding)
    }

    override fun onBindViewHolder(holder: MonthView, position: Int) {
        calendar.time = Date()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, position - center)

        // 바인딩을 이용해 텍스트 설정
        val tempMonth = calendar.get(Calendar.MONTH)

        // 날짜 리스트 생성
        val dayList: MutableList<Date> = MutableList(6 * 7) { Date() }
        for (i in 0..5) {
            for (k in 0..6) {
                calendar.add(Calendar.DAY_OF_MONTH, (1 - calendar.get(Calendar.DAY_OF_WEEK)) + k)
                dayList[i * 7 + k] = calendar.time
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        }

        // RecyclerView 설정
        val dayListManager = GridLayoutManager(holder.binding.root.context, 7)
        val dayListAdapter = AdapterDay(tempMonth, dayList)

        holder.binding.itemMonthDayList.apply {
            layoutManager = dayListManager
            adapter = dayListAdapter
        }
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}