package com.isaiahvonrundstedt.fokus.features.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter

class ScheduleAdapter(private val actionListener: ActionListener)
    : BaseAdapter<Schedule, ScheduleAdapter.ViewHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_schedule_editor, parent, false)
        return ViewHolder(rowView, actionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(itemView: View, private val actionListener: ActionListener)
        : BaseAdapter.BaseViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val summaryView: TextView = itemView.findViewById(R.id.summaryView)
        private val removeButton: Chip = itemView.findViewById(R.id.removeButton)

        override fun <T> onBind(t: T) {
            if (t is Schedule) {
                titleView.text = t.formatDaysOfWeek(rootView.context, false)
                summaryView.text = t.formatBothTime()
            }
            removeButton.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.DELETE, emptyMap())
            }

            rootView.setOnClickListener {
                actionListener.onActionPerformed(t, ActionListener.Action.SELECT, emptyMap())
            }
        }
    }

    companion object {
        val callback = object: DiffUtil.ItemCallback<Schedule>() {
            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleID == newItem.scheduleID
            }
        }
    }
}