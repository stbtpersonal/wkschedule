package io.github.stbtpersonal.wkschedule

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class StudyRecyclerViewAdapter : RecyclerView.Adapter<StudyRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView)

    private val studySubjects = mutableListOf<StudySubject>()

    fun setStudySubjects(studySubjects: List<StudySubject>) {
        this.studySubjects.clear()
        this.studySubjects.addAll(studySubjects.toList())
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val studySubjectView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_study_subject, parent, false) as ViewGroup
        return ViewHolder(studySubjectView)
    }

    override fun getItemCount(): Int {
        return this.studySubjects.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val studySubjectView = holder.itemView
        val studySubject = this.studySubjects.get(position)

        val subjectBackground = studySubjectView.findViewById<ViewGroup>(R.id.subjectBackground)
        val color = when {
            studySubject.type == "radical" && !studySubject.isUnlocked ->
                ContextCompat.getColor(studySubjectView.context, R.color.wkRadicalsLocked)
            studySubject.type == "radical" && studySubject.isUnlocked ->
                ContextCompat.getColor(studySubjectView.context, R.color.wkRadicals)
            studySubject.type == "kanji" && !studySubject.isUnlocked ->
                ContextCompat.getColor(studySubjectView.context, R.color.wkKanjiLocked)
            studySubject.type == "kanji" && studySubject.isUnlocked ->
                ContextCompat.getColor(studySubjectView.context, R.color.wkKanji)
            else -> 0
        }
        subjectBackground.background = ColorDrawable(color)

        val subjectCharacterText = studySubjectView.findViewById<TextView>(R.id.subjectCharacterText)
        val subjectCharacterImage = studySubjectView.findViewById<ImageView>(R.id.subjectCharacterImage)
        if (studySubject.character != null) {
            subjectCharacterText.text = studySubject.character
            subjectCharacterText.visibility = View.VISIBLE
            subjectCharacterImage.visibility = View.INVISIBLE
        } else {
            Picasso.get().load(studySubject.characterImageUrl).into(subjectCharacterImage)
            subjectCharacterText.visibility = View.INVISIBLE
            subjectCharacterImage.visibility = View.VISIBLE
            subjectCharacterImage.setColorFilter(Color.WHITE)
        }

        val subjectMeanings = studySubjectView.findViewById<TextView>(R.id.subjectMeanings)
        subjectMeanings.text = studySubject.meanings.joinToString(separator = ", ", prefix = "", postfix = "")

        val subjectReadings = studySubjectView.findViewById<TextView>(R.id.subjectReadings)
        subjectReadings.text = studySubject.readings.joinToString(separator = ", ", prefix = "", postfix = "")
        subjectReadings.visibility = if (studySubject.type == "radical") View.GONE else View.VISIBLE

        val subjectCharacter = studySubjectView.findViewById<ViewGroup>(R.id.subjectCharacter)
        val subjectDetails = studySubjectView.findViewById<ViewGroup>(R.id.subjectDetails)
        subjectCharacter.visibility = View.VISIBLE
        subjectDetails.visibility = View.INVISIBLE

        studySubjectView.setOnClickListener {
            val areDetailsVisible = subjectDetails.visibility == View.VISIBLE

            subjectCharacter.visibility = if (areDetailsVisible) View.VISIBLE else View.INVISIBLE
            subjectDetails.visibility = if (areDetailsVisible) View.INVISIBLE else View.VISIBLE
        }
    }
}