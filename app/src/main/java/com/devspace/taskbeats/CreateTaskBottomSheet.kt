package com.devspace.taskbeats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateTaskBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val task: TaskUiData? = null,
    private val onCreateClicked:
        (TaskUiData) -> Unit,
    private val onDeleteClicked : (TaskUiData) -> Unit,
) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cu_task_bottom_sheet, container, false)
        val btnCreateUpdate = view.findViewById<Button>(R.id.btn_create_task)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.tie_task_name)
        var taskcategory: String? = null
        val spinner: Spinner = view.findViewById(R.id.category_spinner)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tempCategory = mutableListOf("Select")
        tempCategory.addAll(categoryList.map { it.name })
        val categoryStrs: List<String> = tempCategory

        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            categoryStrs
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        if (task == null) {
            btnDelete.isVisible = false
        } else {
            btnDelete.isVisible = true
            tvTitle.setText(R.string.update_task_title)
            btnCreateUpdate.setText(R.string.update)
            tieTaskName.setText(task.name)

            val currentCategory = categoryList.first { it.name == task.category }
            spinner.setSelection(categoryList.indexOf(currentCategory) + 1)
        }

        btnCreateUpdate.setOnClickListener {
            val name = tieTaskName.text.toString().trim()
            val id: Long = task?.id ?: 0

            if (taskcategory != "Select" && name.isNotEmpty()) {
                onCreateClicked.invoke(
                    TaskUiData(
                        id = id,
                        name = name,
                        category = requireNotNull(taskcategory)
                    )
                )
                dismiss()
            } else {
                Snackbar.make(btnCreateUpdate, "Please select Category or put a name", Snackbar.LENGTH_LONG).show()
            }
        }

        btnDelete.setOnClickListener {
            if (task != null) {
                onDeleteClicked.invoke(task)
                dismiss()
            } else {
                Log.d("createupdatetaskbottomsheet", "Task not found")
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                taskcategory = categoryStrs[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }
}