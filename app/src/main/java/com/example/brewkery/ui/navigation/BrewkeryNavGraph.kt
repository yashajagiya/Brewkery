package com.example.brewkery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brewkery.ui.cart.CartScreen
import com.example.brewkery.ui.cart.CartViewModel
import com.example.brewkery.ui.detail.DetailScreen
import com.example.brewkery.ui.detail.DetailViewModel
import com.example.brewkery.ui.home.HomeScreen
import com.example.brewkery.ui.home.HomeViewModel
import com.example.brewkery.ui.order.OrderStatusScreen
import com.example.brewkery.ui.order.OrderStatusViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
    object OrderStatus : Screen("order_status/{ticketId}") {
        fun createRoute(ticketId: String) = "order_status/$ticketId"
    }
}

@Composable
fun BrewkeryNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(context))
            HomeScreen(
                viewModel = homeViewModel,
                onItemClick = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onOrderStatusClick = { ticketId ->
                    navController.navigate(Screen.OrderStatus.createRoute(ticketId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 1
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(itemId = itemId, context = context)
            )
            DetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onCartClick = {
                    navController.navigate(Screen.Cart.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory(context))
            CartScreen(
                viewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = { ticketId ->
                    navController.navigate(Screen.OrderStatus.createRoute(ticketId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Screen.OrderStatus.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            val orderStatusViewModel: OrderStatusViewModel = viewModel(
                factory = OrderStatusViewModel.Factory(ticketId = ticketId, context = context)
            )
            OrderStatusScreen(
                viewModel = orderStatusViewModel,
                onBackToHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
