package com.devspace.taskbeats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var tasks = listOf<TaskUiData>()
    private val categoryAdapter = CategoryListAdapter()
    private val taskAdapter = TaskListAdapter()
    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var fabCreateTask: FloatingActionButton


    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDataBase::class.java, "database-task-beat"
        ).build()
    }

    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao: TaskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        ctnEmptyView = findViewById<LinearLayout>(R.id.ll_empty_view)
        fabCreateTask = findViewById<FloatingActionButton>(R.id.fab)

        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        val btnCreateEmpty = findViewById<Button>(R.id.create_empty)

        btnCreateEmpty.setOnClickListener {
            createCategoryBottomSheet()
        }

        fabCreateTask.setOnClickListener {
            showCreateUpdateTaskBottomSheet()
        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                createCategoryBottomSheet()
            } else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                if (selected.name != "ALL") {
                    filterTaskByCategory(selected.name)
                } else {
                    getTasksfromDatabase(taskAdapter)
                }

                categoryAdapter.submitList(categoryTemp)
            }
        }

        categoryAdapter.setOnLongClickListener { selected ->
            if (selected.name != "+" && selected.name != "ALL") {
                val title = this.getString(R.string.info_title)
                val description = this.getString(R.string.category_delete_description)
                val btntext = this.getString(R.string.delete)

                showInfoDialog(title, description, btntext) {
                    val category = CategoryEntity(
                        name = selected.name,
                        isSelected = selected.isSelected
                    )
                    deleteCategory(category)
                }
            }
        }

        rvCategory.adapter = categoryAdapter
        getCategoriesfromDatabase()

        rvTask.adapter = taskAdapter
        taskAdapter.setOnClickListener { task ->
            showCreateUpdateTaskBottomSheet(task)
        }

        getTasksfromDatabase(taskAdapter)
    }

    /////////////////////////////////////////////////////////////////////////////////

    private fun showInfoDialog(
        title: String,
        description: String,
        btntext: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btntext,
            onClick
        )
        infoBottomSheet.show(supportFragmentManager, "infobottomsheet")
    }

    private fun filterTaskByCategory(category: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksdb = taskDao.getAllByCategory(category)
            val tasksUiData = tasksdb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                taskAdapter.submitList(tasksUiData)
            }
        }
    }

    fun getCategoriesfromDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            categoriesEntity = categoryDao.getAll()

            val categoriesUiData = categoriesEntity.map {
                CategoryUiData(
                    name = it.name,
                    isSelected = it.isSelected
                )
            }.toMutableList()
            categoriesUiData.add(
                0,
                CategoryUiData(
                    name = "ALL",
                    isSelected = true
                )
            )
            categoriesUiData.add(
                CategoryUiData(
                    name = "+",
                    isSelected = false
                )
            )
            GlobalScope.launch(Dispatchers.Main) {
                if (categoriesEntity.isEmpty()) {
                    rvCategory.isVisible = false
                    ctnEmptyView.isVisible = true
                    fabCreateTask.isVisible = false
                } else {
                    rvCategory.isVisible = true
                    ctnEmptyView.isVisible = false
                    fabCreateTask.isVisible = true
                }
                categories = categoriesUiData
                categoryAdapter.submitList(categories)
            }
        }
    }

    private fun getTasksfromDatabase(adapter: TaskListAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksdb = taskDao.getAll()
            val tasksUiData = tasksdb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                tasks = tasksUiData
                adapter.submitList(tasksUiData)
            }
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesfromDatabase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategory(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesfromDatabase()
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun insertTask(tasKEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(tasKEntity)
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun deleteTask(tasKEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(tasKEntity)
            getTasksfromDatabase(taskAdapter)
        }
    }

    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createTaskBottomSheet = CreateTaskBottomSheet(
            task = taskUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { tasktobeCreatedorUpdated ->
                insertTask(
                    TaskEntity(
                        id = tasktobeCreatedorUpdated.id,
                        name = tasktobeCreatedorUpdated.name,
                        category = tasktobeCreatedorUpdated.category
                    )
                )
            },
            onDeleteClicked = { tasktobeDeleted ->
                deleteTask(
                    TaskEntity(
                        id = tasktobeDeleted.id,
                        name = tasktobeDeleted.name,
                        category = tasktobeDeleted.category
                    )
                )
            }
        )
        createTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")
    }

    private fun createCategoryBottomSheet() {
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                isSelected = false
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }

}
