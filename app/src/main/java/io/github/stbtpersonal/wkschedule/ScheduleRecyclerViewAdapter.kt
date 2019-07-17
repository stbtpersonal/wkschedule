package io.github.stbtpersonal.wkschedule

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ScheduleRecyclerViewAdapter : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView)

    private val scheduleItems = mutableListOf<Pair<Date, ScheduleItem>>()

    fun setScheduleItems(scheduleItems: Map<Date, ScheduleItem>) {
        this.scheduleItems.clear()
        this.scheduleItems.addAll(scheduleItems.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val scheduleItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_schedule_item, parent, false) as ViewGroup
        return ViewHolder(scheduleItemView)
    }

    override fun getItemCount(): Int {
        return this.scheduleItems.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scheduleItemView = holder.itemView
        val dateAndScheduleItem = this.scheduleItems.get(position)
        val date = dateAndScheduleItem.first
        val scheduleItem = dateAndScheduleItem.second

        val scheduleItemHour = scheduleItemView.findViewById<TextView>(R.id.scheduleItemHour)
        if (date == DateUtils.epoch) {
            scheduleItemHour.text = "Now"
        }
        else {
            val calendar = Calendar.getInstance()
            calendar.time = date
            scheduleItemHour.text = "${calendar.get(Calendar.HOUR_OF_DAY)}:00"
        }

        val scheduleItemRadicals = scheduleItemView.findViewById<TextView>(R.id.scheduleItemRadicals)
        scheduleItemRadicals.text = "Radicals: ${scheduleItem.radicals} (${scheduleItem.newRadicals})"

        val scheduleItemKanji = scheduleItemView.findViewById<TextView>(R.id.scheduleItemKanji)
        scheduleItemKanji.text = "Kanji: ${scheduleItem.kanji} (${scheduleItem.newKanji})"

        val scheduleItemVocabulary = scheduleItemView.findViewById<TextView>(R.id.scheduleItemVocabulary)
        scheduleItemVocabulary.text = "Vocabulary: ${scheduleItem.vocabulary} (${scheduleItem.newVocabulary})"
    }
}