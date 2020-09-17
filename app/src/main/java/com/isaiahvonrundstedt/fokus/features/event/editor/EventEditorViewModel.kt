package com.isaiahvonrundstedt.fokus.features.event.editor

import android.app.Application
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.coroutines.launch

class EventEditorViewModel(app: Application): BaseViewModel(app) {

    private var database = AppDatabase.getInstance(applicationContext)

    private var _event: MutableLiveData<Event> = MutableLiveData(Event())
    private var _subject: MutableLiveData<Subject?> = MutableLiveData(null)
    private var _schedules: MutableLiveData<List<Schedule>> = MutableLiveData(emptyList())

    private val _hasEventName: LiveData<Boolean> =
        Transformations.map(_event) { !it.name.isNullOrEmpty() }
    private val _hasLocation: LiveData<Boolean> =
        Transformations.map(_event) { !it.location.isNullOrEmpty() }
    private val _hasSchedule: LiveData<Boolean> =
        Transformations.map(_event) { it.schedule != null }

    val event: LiveData<Event> = _event
    val subject: LiveData<Subject?> = _subject
    val schedules: LiveData<List<Schedule>> = _schedules

    val hasEventName: Boolean
        get() = _hasEventName.value ?: false
    val hasLocation: Boolean
        get() = _hasLocation.value ?: false
    val hasSchedule: Boolean
        get() = _hasSchedule.value ?: false

    fun getEvent(): Event? { return _event.value }
    fun setEvent(event: Event?) { _event.value = event }

    fun getSubject(): Subject? { return _subject.value }
    fun setSubject(subject: Subject?) = viewModelScope.launch {
        _subject.value = subject
        if (subject != null) {
            _event.value?.subject = subject.subjectID
            _subject.value = subject
            _schedules.value = database.schedules().fetchUsingID(subject.subjectID)
        } else {
            _event.value?.subject = null
            _subject.value = null
            _schedules.value = emptyList()
        }
    }

    fun getSchedules(): List<Schedule> { return _schedules.value ?: emptyList() }
    fun setSchedules(schedules: List<Schedule>) { _schedules.value = schedules }


}