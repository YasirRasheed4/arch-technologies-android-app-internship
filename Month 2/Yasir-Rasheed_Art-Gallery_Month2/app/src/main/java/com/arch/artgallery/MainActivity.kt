package com.arch.artgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arch.artgallery.ui.DetailScreen
import com.arch.artgallery.ui.FavoritesScreen
import com.arch.artgallery.ui.GalleryScreen
import com.arch.artgallery.ui.GalleryViewModel
import com.arch.artgallery.ui.UploadScreen
import com.arch.artgallery.ui.theme.ArtGalleryTheme

class MainActivity : ComponentActivity() {
    private val vm: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtGalleryTheme { AppNav(vm) }
        }
    }
}

@Composable
private fun AppNav(vm: GalleryViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "gallery") {
        composable("gallery") {
            GalleryScreen(
                vm = vm,
                onOpen = { nav.navigate("detail/$it") },
                onUpload = { nav.navigate("upload") },
                onFavorites = { nav.navigate("favorites") }
            )
        }
        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            DetailScreen(vm, entry.arguments!!.getLong("id")) { nav.popBackStack() }
        }
        composable("upload") { UploadScreen(vm) { nav.popBackStack() } }
        composable("favorites") {
            FavoritesScreen(vm, onOpen = { nav.navigate("detail/$it") }) { nav.popBackStack() }
        }
    }
}
