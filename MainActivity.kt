package com.journet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ═════════════ Theme ═════════════
val Bg = Color(0xFFF6F7F0)
val Dark = Color(0xFF1B2B27)
val Teal = Color(0xFF1F8A7A)
const val AV_SOFIA = "https://i.pravatar.cc/100?img=47"

@Composable
fun JournetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Teal, background = Bg),
        content = content
    )
}

// ═════════════ Data ═════════════
data class Post(
    val id: Int,
    val author: String,
    val avatar: String?,
    val location: String?,
    val time: String?,
    val images: List<String> = emptyList(),
    val text: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val liked: Boolean = false,
    val saved: Boolean = false,
    val feed: String? = null
)

data class NewPost(val text: String, val type: String)

interface Api {
    @GET("api/posts") suspend fun posts(@Query("feed") feed: String): List<Post>
    @POST("api/posts/{id}/like") suspend fun like(@Path("id") id: Int): Post
    @POST("api/posts/{id}/save") suspend fun save(@Path("id") id: Int): Post
    @POST("api/posts") suspend fun create(@Body p: NewPost): Post
}

// 10.0.2.2 = localhost của máy tính khi chạy Android Emulator.
// Máy thật: dùng IP LAN của máy chạy backend, ví dụ "http://192.168.1.10:8080/".
const val BASE_URL = "http://10.0.2.2:8080/"

val api: Api by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Api::class.java)
}

enum class Feed(val key: String, val label: String) {
    FOLLOWING("following", "Đang theo dõi"),
    SUGGESTED("suggested", "Đề xuất"),
    FAVORITES("favorites", "Yêu thích")
}

class FeedViewModel : ViewModel() {
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var feed by mutableStateOf(Feed.FOLLOWING)
        private set

    init { load() }

    fun load(f: Feed = feed) {
        feed = f
        viewModelScope.launch {
            loading = true
            error = null
            runCatching { api.posts(f.key) }
                .onSuccess { posts = it }
                .onFailure { error = "Không kết nối được máy chủ (${it.message})" }
            loading = false
        }
    }

    private fun replace(p: Post) {
        posts = posts.map { if (it.id == p.id) p else it }
    }

    fun like(p: Post) {
        replace(p.copy(liked = !p.liked, likes = p.likes + if (p.liked) -1 else 1)) // optimistic
        viewModelScope.launch {
            runCatching { api.like(p.id) }
                .onSuccess { replace(it) }
                .onFailure { replace(p) }
        }
    }

    fun save(p: Post) {
        replace(p.copy(saved = !p.saved))
        viewModelScope.launch {
            runCatching { api.save(p.id) }
                .onSuccess { replace(it) }
                .onFailure { replace(p) }
        }
    }

    fun create(text: String, type: String, done: () -> Unit) {
        viewModelScope.launch {
            runCatching { api.create(NewPost(text, type)) }
                .onSuccess { load(Feed.FOLLOWING); done() }
                .onFailure { error = "Đăng thất bại" }
        }
    }
}

// ═════════════ Activity + Navigation ═════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { JournetTheme { AppNav() } }
    }
}

object Route {
    const val HOME = "home"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val MENU = "menu"
    const val PROFILE = "profile"
    const val CREATE_POST = "create/post"
    const val REELS = "reels"
    const val CHAT = "chat"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: FeedViewModel = viewModel()
    val entry by nav.currentBackStackEntryAsState()
    val route: String? = entry?.destination?.route
    val barRoutes = listOf(Route.HOME, Route.REELS, Route.CHAT, Route.PROFILE)
    val showBar = route != null && route in barRoutes

    Box(Modifier.fillMaxSize().background(Bg)) {
        NavHost(navController = nav, startDestination = Route.HOME) {
            composable(Route.HOME) { MingleHomeScreen(vm, nav) }
            composable(Route.REELS) { Placeholder("Video", nav) }
            composable(Route.CHAT) { Placeholder("Tin nhắn", nav) }
            composable(Route.SEARCH) { Placeholder("Tìm kiếm", nav) }
            composable(Route.NOTIFICATIONS) { Placeholder("Thông báo", nav) }
            composable(Route.MENU) { Placeholder("Menu", nav) }
            composable(Route.PROFILE) { Placeholder("Hồ sơ", nav) }
            composable(Route.CREATE_POST) { CreatePostScreen(vm, nav) }
        }
        if (showBar) {
            JournetBottomBar(
                currentRoute = route,
                onNavigate = { target ->
                    if (target == Route.CREATE_POST) {
                        nav.navigate(target)
                    } else {
                        nav.navigate(target) {
                            popUpTo(Route.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
fun Placeholder(title: String, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Dark)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { nav.popBackStack() }) { Text("Quay lại") }
    }
}

@Composable
fun CreatePostScreen(vm: FeedViewModel, nav: NavHostController) {
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(24.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Bạn đang nghĩ gì?") }
        )
        Spacer(Modifier.height(16.dp))
        Row {
            TextButton(onClick = { nav.popBackStack() }) { Text("Huỷ") }
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = text.isNotBlank(),
                onClick = { vm.create(text, "text") { nav.popBackStack() } }
            ) { Text("Đăng") }
        }
    }
}
