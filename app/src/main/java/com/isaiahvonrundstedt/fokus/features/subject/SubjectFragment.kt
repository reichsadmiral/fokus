package com.isaiahvonrundstedt.fokus.features.subject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.custom.ItemSwipeCallback
import kotlinx.android.synthetic.main.fragment_subject.*

class SubjectFragment: BaseFragment(), BaseAdapter.ActionListener {

    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SubjectAdapter(this)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.fetch()?.observe(viewLifecycleOwner, Observer { items ->
            adapter?.submitList(items)
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private var adapter: SubjectAdapter? = null
    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, SubjectEditor::class.java),
                SubjectEditor.insertRequestCode)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is SubjectResource) {
            when (action) {
                // Create the intent for the editorUI and pass the extras
                // and wait for the result
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, SubjectEditor::class.java).apply {
                        putExtra(SubjectEditor.extraSubject, t.subject)
                        putParcelableArrayListExtra(SubjectEditor.extraSchedules, t.scheduleList.toArrayList())
                    }
                    startActivityWithTransition(views, intent, SubjectEditor.updateRequestCode)
                }
                // Item has been swiped from the RecyclerView, notify user action
                // in the ViewModel to delete it from the database
                // then show a SnackBar feedback
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.subject)

                    createSnackbar(recyclerView, R.string.feedback_subject_removed).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.subject, t.scheduleList) }
                        show()
                    }
                }
                BaseAdapter.ActionListener.Action.MODIFY -> {}
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == SubjectEditor.insertRequestCode
                || requestCode == SubjectEditor.updateRequestCode) {

            if (resultCode == BaseEditor.RESULT_OK || resultCode == BaseEditor.RESULT_DELETE) {
                val subject: Subject? = data?.getParcelableExtra(SubjectEditor.extraSubject)
                val scheduleList: List<Schedule>? = data?.getListExtra(SubjectEditor.extraSchedules)

                subject?.also {
                    if (resultCode == BaseEditor.RESULT_OK) {
                        when (requestCode) {
                            SubjectEditor.insertRequestCode ->
                                viewModel.insert(it, scheduleList ?: emptyList())
                            SubjectEditor.updateRequestCode ->
                                viewModel.update(it, scheduleList ?: emptyList())
                        }
                    } else if (resultCode == BaseEditor.RESULT_DELETE) {
                        viewModel.remove(it)
                        createSnackbar(recyclerView, R.string.feedback_subject_removed).apply {
                            setAction(R.string.button_undo) { _ -> viewModel.insert(it, scheduleList ?: emptyList()) }
                            show()
                        }
                    }
                }
            }
        }
    }

}