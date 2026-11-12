package com.journet.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

// ═════════════════════════════════════════════════════════════
//  FILE NÀY GỒM: Màn Home + Thanh điều hướng dưới + Icon
//  (Đã gộp JournetBottomBar.kt vào đây. HÃY XOÁ file JournetBottomBar.kt)
//  Cần có sẵn trong MainActivity.kt: Bg, Dark, Teal, AV_SOFIA, Route, Post, FeedViewModel
// ═════════════════════════════════════════════════════════════

// ───────────── Design tokens ─────────────
private val Brand = Color(0xFF123D36)
private val Muted = Color(0xFF7D8581)
private val Hairline = Color(0xFFE3E5DC)
private val Field = Color(0xFFEEEFE7)
private val FieldBorder = Color(0xFFDEE0D6)
private val HPad = 22.dp

private val BarBg = Color(0xF2EDF1EA)
private val BarBorder = Color(0xFFDCE2D9)
private val BarSelected = Color(0xFFF8F9F6)
private val AddBg = Color(0xFF123D36)

private object S {
    const val LOGO = "journet"          // đổi thành "mingle" nếu muốn giống chữ trong ảnh
    const val COMPOSER_HINT = "Bạn đang nghĩ gì?"
    fun viewAll(n: Int) = "Xem tất cả $n bình luận"
}

// ───────────── Model UI ─────────────
data class MinglePostUi(
    val id: Int,
    val authorName: String,
    val authorAvatar: Any?,
    val meta: String,
    val text: String,
    val images: List<String> = emptyList(),
    val likes: Int = 0,
    val comments: Int = 0,
    val liked: Boolean = false,
    val saved: Boolean = false,
)

fun Post.toUi() = MinglePostUi(
    id = id,
    authorName = author,
    authorAvatar = avatar,
    meta = listOfNotNull(location, time).joinToString(" · "),
    text = text,
    images = images,
    likes = likes,
    comments = comments,
    liked = liked,
    saved = saved,
)

// ───────────── Màn hình Home ─────────────
@Composable
fun MingleHomeScreen(vm: FeedViewModel, nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        MingleTopBar(
            onSearch = { nav.navigate(Route.SEARCH) },
            onMenu = { nav.navigate(Route.MENU) }
        )
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 120.dp)) {
            item {
                MingleComposer(
                    onCompose = { nav.navigate(Route.CREATE_POST) },
                    onAvatar = { nav.navigate(Route.PROFILE) }
                )
            }
            vm.error?.let { e ->
                item { Text(e, color = Color.Red, modifier = Modifier.padding(HPad)) }
            }
            items(vm.posts, key = { it.id }) { p ->
                MinglePostCard(
                    post = p.toUi(),
                    onLike = { vm.like(p) },
                    onSave = { vm.save(p) },
                    onAuthor = { nav.navigate(Route.PROFILE) },
                )
            }
        }
    }
}

// ───────────── Thanh trên ─────────────
@Composable
private fun MingleTopBar(onSearch: () -> Unit, onMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).padding(start = HPad, end = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                S.LOGO, color = Brand, fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.6).sp
            )
            Box(Modifier.padding(start = 4.dp, top = 12.dp).size(8.dp).clip(CircleShape).background(Teal))
        }
        Box(
            Modifier.size(48.dp)
                .shadow(8.dp, CircleShape, ambientColor = Color(0x14000000), spotColor = Color(0x26123D36))
                .clip(CircleShape).background(Color.White).clickable(onClick = onSearch),
            Alignment.Center
        ) { Icon(Ic.Search, "Tìm kiếm", tint = Dark, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(16.dp))
        Box(Modifier.size(46.dp).clip(CircleShape).clickable(onClick = onMenu), Alignment.Center) {
            Icon(Ic.Menu, "Menu", tint = Dark, modifier = Modifier.size(26.dp))
        }
    }
}

// ───────────── Ô soạn bài ─────────────
@Composable
private fun MingleComposer(onCompose: () -> Unit, onAvatar: () -> Unit) {
    val pill = RoundedCornerShape(50)
    Row(
        Modifier.fillMaxWidth().padding(start = HPad, end = HPad, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MingleAvatar(AV_SOFIA, "S", 40.dp, Modifier.clickable(onClick = onAvatar))
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier.weight(1f).height(42.dp).clip(pill).background(Field)
                .border(1.dp, FieldBorder, pill).clickable(onClick = onCompose)
                .padding(horizontal = 18.dp),
            Alignment.CenterStart
        ) { Text(S.COMPOSER_HINT, color = Color(0xFF8A908B), fontSize = 15.sp) }
    }
    Spacer(Modifier.height(18.dp))
    MingleDivider()
}

@Composable
private fun MingleDivider() {
    Box(Modifier.padding(horizontal = HPad).fillMaxWidth().height(1.dp).background(Hairline))
}

// ───────────── Avatar ─────────────
@Composable
fun MingleAvatar(model: Any?, name: String, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(Color(0xFFD7E3DC)), Alignment.Center) {
        if (model == null || (model is String && model.isBlank())) {
            Text(
                name.take(1).uppercase(), color = Dark,
                fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.42f).sp
            )
        } else {
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ───────────── Thẻ bài viết ─────────────
@Composable
fun MinglePostCard(
    post: MinglePostUi,
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onAuthor: () -> Unit = {},
    onLike: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = HPad), verticalAlignment = Alignment.CenterVertically) {
            MingleAvatar(post.authorAvatar, post.authorName, 42.dp, Modifier.clickable(onClick = onAuthor))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(post.authorName, color = Dark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(post.meta, color = Muted, fontSize = 13.sp)
            }
            Box(
                Modifier.size(40.dp).offset(x = 8.dp).clip(CircleShape).clickable(onClick = onMore),
                Alignment.Center
            ) { MoreDots() }
        }

        if (post.images.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            MingleGallery(post.images)
            MingleActions(
                post.liked, post.likes, post.comments, post.saved,
                onLike = onLike, onComment = onComment, onShare = onShare, onSave = onSave,
            )
            if (post.text.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                MingleBody(post.text)
            }
        } else {
            if (post.text.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                MingleBody(post.text)
            }
            MingleActions(
                post.liked, post.likes, post.comments, post.saved,
                onLike = onLike, onComment = onComment, onShare = onShare, onSave = onSave,
            )
        }

        if (post.comments > 0) {
            Text(
                S.viewAll(post.comments), color = Muted, fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = HPad).padding(top = 4.dp).clickable(onClick = onComment)
            )
        }
        Spacer(Modifier.height(14.dp))
        MingleDivider()
    }
}

/** Dấu ba chấm ngang (thay cho Icons.Filled.MoreHoriz) */
@Composable
private fun MoreDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { Box(Modifier.size(4.5.dp).clip(CircleShape).background(Dark)) }
    }
}

@Composable
private fun MingleBody(text: String) {
    Text(
        text, color = Dark, fontSize = 15.sp, lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = HPad).fillMaxWidth()
    )
}

// ───────────── Ảnh bài viết ─────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MingleGallery(images: List<String>) {
    val pager = rememberPagerState(pageCount = { images.size })
    Box(
        Modifier.padding(horizontal = HPad).fillMaxWidth().aspectRatio(1.28f)
            .clip(RoundedCornerShape(26.dp)).background(Color(0xFFDCE4DD))
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            val url: String? = images[page].takeIf { it.isNotBlank() }
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (images.size > 1) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 15.dp, end = 14.dp)
                    .clip(RoundedCornerShape(50)).background(Color(0x99596662))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "${pager.currentPage + 1}/${images.size}", color = Color.White,
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ───────────── Hàng thao tác ─────────────
@Composable
private fun MingleActions(
    liked: Boolean, likes: Int, comments: Int, saved: Boolean,
    onLike: () -> Unit, onComment: () -> Unit, onShare: () -> Unit, onSave: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(start = HPad - 6.dp, end = HPad - 4.dp, top = 6.dp).height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(if (liked) Ic.HeartFilled else Ic.Heart, if (liked) Teal else Dark, "Thích", onLike)
        Spacer(Modifier.width(3.dp))
        ActionCount(likes)
        Spacer(Modifier.width(20.dp))
        ActionButton(Ic.Comment, Dark, "Bình luận", onComment)
        Spacer(Modifier.width(3.dp))
        ActionCount(comments)
        Spacer(Modifier.width(22.dp))
        ActionButton(Ic.Send, Dark, "Gửi", onShare)
        Spacer(Modifier.weight(1f))
        ActionButton(if (saved) Ic.BookmarkFilled else Ic.Bookmark, if (saved) Teal else Dark, "Lưu", onSave)
    }
}

@Composable
private fun ActionButton(icon: ImageVector, tint: Color, desc: String, onClick: () -> Unit) {
    Box(Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onClick), Alignment.Center) {
        Icon(icon, desc, tint = tint, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun ActionCount(n: Int) {
    Text(formatCount(n), color = Dark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
}

private fun formatCount(n: Int): String =
    if (n >= 1000) String.format("%.1fK", n / 1000f).replace(".0K", "K") else n.toString()

// ═════════════ THANH ĐIỀU HƯỚNG DƯỚI (đã gộp từ JournetBottomBar.kt) ═════════════
/** Thanh nổi dạng viên thuốc: Home · Video · (+) · Chat · Hồ sơ */
@Composable
fun JournetBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(14.dp, shape, ambientColor = Color(0x14000000), spotColor = Color(0x33123D36))
            .clip(shape)
            .background(BarBg)
            .border(1.dp, BarBorder, shape)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavItem(currentRoute == Route.HOME, { onNavigate(Route.HOME) }) {
            Icon(Ic.Home, "Trang chủ", tint = Dark, modifier = Modifier.size(26.dp))
        }
        NavItem(currentRoute == Route.REELS, { onNavigate(Route.REELS) }) {
            Icon(Ic.Play, "Video", tint = Dark, modifier = Modifier.size(26.dp))
        }
        Box(
            Modifier.size(50.dp).clip(CircleShape).background(AddBg)
                .clickable { onNavigate(Route.CREATE_POST) },
            Alignment.Center
        ) {
            Icon(Ic.AddCircle, "Tạo bài viết", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        NavItem(currentRoute == Route.CHAT, { onNavigate(Route.CHAT) }) {
            Icon(Ic.Chat, "Tin nhắn", tint = Dark, modifier = Modifier.size(26.dp))
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 9.dp, end = 8.dp)
                    .size(7.dp).clip(CircleShape).background(Teal)
            )
        }
        NavItem(currentRoute == Route.PROFILE, { onNavigate(Route.PROFILE) }) {
            MingleAvatar(AV_SOFIA, "S", 34.dp)
        }
    }
}

@Composable
private fun NavItem(selected: Boolean, onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape)
            .background(if (selected) BarSelected else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ═════════════ ICON TỰ VẼ (không cần thư viện material-icons-extended) ═════════════
private object Ic {
    private fun build(name: String, filled: Boolean = false, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(
                fill = if (filled) SolidColor(Color.Black) else null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) { block() }
        }.build()

    val Search: ImageVector by lazy {
        build("Search") {
            moveTo(17.5f, 11f)
            arcTo(6.5f, 6.5f, 0f, true, true, 4.5f, 11f)
            arcTo(6.5f, 6.5f, 0f, true, true, 17.5f, 11f)
            moveTo(16f, 16f); lineTo(21f, 21f)
        }
    }

    val Menu: ImageVector by lazy {
        build("Menu") {
            moveTo(4f, 6.5f); lineTo(20f, 6.5f)
            moveTo(4f, 12f); lineTo(20f, 12f)
            moveTo(4f, 17.5f); lineTo(20f, 17.5f)
        }
    }

    private fun PathBuilder.heartPath() {
        moveTo(20.84f, 4.61f)
        arcTo(5.5f, 5.5f, 0f, false, false, 13.06f, 4.61f)
        lineTo(12f, 5.67f)
        lineTo(10.94f, 4.61f)
        arcTo(5.5f, 5.5f, 0f, false, false, 3.16f, 12.39f)
        lineTo(4.22f, 13.45f)
        lineTo(12f, 21.23f)
        lineTo(19.78f, 13.45f)
        lineTo(20.84f, 12.39f)
        arcTo(5.5f, 5.5f, 0f, false, false, 20.84f, 4.61f)
        close()
    }

    val Heart: ImageVector by lazy { build("Heart") { heartPath() } }
    val HeartFilled: ImageVector by lazy { build("HeartFilled", filled = true) { heartPath() } }

    /** Bong bóng tròn có đuôi ở góc dưới trái */
    private fun PathBuilder.bubblePath() {
        moveTo(7.9f, 20f)
        arcTo(9f, 9f, 0f, true, false, 4f, 16.1f)
        lineTo(2f, 22f)
        close()
    }

    val Comment: ImageVector by lazy { build("Comment") { bubblePath() } }

    val Chat: ImageVector by lazy {
        build("Chat") {
            bubblePath()
            moveTo(8.5f, 10.5f); lineTo(15.5f, 10.5f)
            moveTo(8.5f, 14.2f); lineTo(12.5f, 14.2f)
        }
    }

    val Send: ImageVector by lazy {
        build("Send") {
            moveTo(21f, 3f)
            lineTo(14.5f, 21f)
            lineTo(10f, 13.5f)
            lineTo(3f, 9.5f)
            close()
            moveTo(21f, 3f); lineTo(10f, 13.5f)
        }
    }

    private fun PathBuilder.bookmarkPath() {
        moveTo(6.5f, 3.5f)
        lineTo(17.5f, 3.5f)
        lineTo(17.5f, 20.5f)
        lineTo(12f, 16.2f)
        lineTo(6.5f, 20.5f)
        close()
    }

    val Bookmark: ImageVector by lazy { build("Bookmark") { bookmarkPath() } }
    val BookmarkFilled: ImageVector by lazy { build("BookmarkFilled", filled = true) { bookmarkPath() } }

    val Home: ImageVector by lazy {
        build("Home") {
            moveTo(3.5f, 11f); lineTo(12f, 3.8f); lineTo(20.5f, 11f)
            moveTo(5.8f, 9.4f); lineTo(5.8f, 20f); lineTo(18.2f, 20f); lineTo(18.2f, 9.4f)
            moveTo(10f, 20f); lineTo(10f, 14.5f); lineTo(14f, 14.5f); lineTo(14f, 20f)
        }
    }

    /** Hình vuông bo góc có nút play */
    val Play: ImageVector by lazy {
        build("Play") {
            moveTo(7f, 4f)
            lineTo(17f, 4f)
            arcTo(3f, 3f, 0f, false, true, 20f, 7f)
            lineTo(20f, 17f)
            arcTo(3f, 3f, 0f, false, true, 17f, 20f)
            lineTo(7f, 20f)
            arcTo(3f, 3f, 0f, false, true, 4f, 17f)
            lineTo(4f, 7f)
            arcTo(3f, 3f, 0f, false, true, 7f, 4f)
            close()
            moveTo(10.5f, 9f); lineTo(15f, 12f); lineTo(10.5f, 15f); close()
        }
    }

    /** Vòng tròn có dấu + */
    val AddCircle: ImageVector by lazy {
        build("AddCircle") {
            moveTo(21f, 12f)
            arcTo(9f, 9f, 0f, true, true, 3f, 12f)
            arcTo(9f, 9f, 0f, true, true, 21f, 12f)
            moveTo(12f, 8f); lineTo(12f, 16f)
            moveTo(8f, 12f); lineTo(16f, 12f)
        }
    }
}
