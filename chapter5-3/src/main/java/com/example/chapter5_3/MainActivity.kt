package com.example.chapter5_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chapter5_3.ui.theme.FastCampusComposeTheme
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastCampusComposeTheme { Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                TopLevel()
            }
            }
        }
    }
}

class ToDoViewModel : ViewModel() {
    // 단계 2: text 상태를 라이브 데이터로 변경합니다.
    // 사용하는 측에서는 `text.observeAsState()`로 구독하세요.
//    val text = mutableStateOf("")
    private val _text = MutableLiveData("")
    val text: LiveData<String> = _text
    val setText: (String) -> Unit = {
        _text.value = it
    }

    // 단계 3: toDoList 상태를 라이브 데이터로 변경합니다.
    // 모든 연산에서 List를 새로 만들어 라이브 데이터로 전달해야 합니다!!!
    // (초 비추!!)
//    val toDoList = mutableStateListOf<ToDoData>()
    private val _rawToDoList = mutableListOf<ToDoData>()
    private val _toDoList = MutableLiveData<List<ToDoData>>(_rawToDoList)
    val toDoList: LiveData<List<ToDoData>> = _toDoList

    val onSubmit: (String) -> Unit = {
        val key = (_rawToDoList.lastOrNull()?.key ?: 0) + 1
        _rawToDoList.add(ToDoData(key, it))
        _toDoList.value = _rawToDoList.toMutableList()
        _text.value = ""
    }

    val onEdit: (Int, String) -> Unit = { key, newText ->
        val i = _rawToDoList.indexOfFirst { it.key == key }
        _rawToDoList[i] = _rawToDoList[i].copy(text = newText)
        _toDoList.value = _rawToDoList.toMutableList()
    }

    val onToggle: (Int, Boolean) -> Unit = { key, checked ->
        val i = _rawToDoList.indexOfFirst { it.key == key }
        _rawToDoList[i] = _rawToDoList[i].copy(done = checked)
        _toDoList.value = _rawToDoList.toMutableList()
    }

    val onDelete: (Int) -> Unit = { key ->
        val i = _rawToDoList.indexOfFirst { it.key == key }
        _rawToDoList.removeAt(i)
        _toDoList.value = _rawToDoList.toMutableList()
    }
}

@Composable
fun TopLevel(viewModel: ToDoViewModel = viewModel()) {

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ToDoInput(
                text = viewModel.text.observeAsState("").value,
                onTextChange = viewModel.setText,
                onSubmit = viewModel.onSubmit
            )
            val items = viewModel.toDoList.observeAsState(emptyList()).value
            LazyColumn {
                items(
                    items = items,
                    key = { it.key }
                ) { toDoData ->
                    ToDo(
                        toDoData = toDoData,
                        onEdit = viewModel.onEdit,
                        onToggle = viewModel.onToggle,
                        onDelete = viewModel.onDelete
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FastCampusComposeTheme {
        TopLevel()
    }
}

@Composable
fun ToDoInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    Row(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Button(onClick = {
            onSubmit(text)
        }) {
            Text("입력")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToDoInputPreview() {
    FastCampusComposeTheme {
        ToDoInput("테스트", {}, {})
    }
}

@Composable
fun ToDo(
    toDoData: ToDoData,
    onEdit: (key: Int, text: String) -> Unit = { _, _ -> },
    onToggle: (key: Int, checked: Boolean) -> Unit = { _, _ -> },
    onDelete: (key: Int) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Crossfade(
            targetState = isEditing,
        ) {
            when (it) {
                false -> {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = toDoData.text,
                            modifier = Modifier.weight(1f)
                        )
                        Text("완료")
                        Checkbox(
                            checked = toDoData.done,
                            onCheckedChange = { checked ->
                                onToggle(toDoData.key, checked)
                            }
                        )
                        Button(
                            onClick = { isEditing = true }
                        ) {
                            Text("수정")
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                        Button(
                            onClick = { onDelete(toDoData.key) }
                        ) {
                            Text("삭제")
                        }
                    }
                }
                true -> {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (text, setText) = remember { mutableStateOf(toDoData.text) }
                        OutlinedTextField(
                            value = text,
                            onValueChange = setText,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = {
                            isEditing = false
                            onEdit(toDoData.key, text)
                        }) {
                            Text("완료")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToDoPreview() {
    FastCampusComposeTheme {
        ToDo(ToDoData(1, "nice", true))
    }
}

data class ToDoData(
    val key: Int,
    val text: String,
    val done: Boolean = false
)