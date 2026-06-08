package com.example.provap2cinema.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.provap2cinema.model.Ticket
import com.example.provap2cinema.model.UserType
import com.example.provap2cinema.ui.screens.*
import com.example.provap2cinema.viewmodel.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Admin : Screen("admin")
    object ManageMovies : Screen("manage_movies")
    object AddMovie : Screen("add_movie")
    object EditMovie : Screen("edit_movie/{movieId}") {
        fun createRoute(movieId: String) = "edit_movie/$movieId"
    }
    object ManageStock : Screen("manage_stock")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object ManageSessions : Screen("manage_sessions")
    object AddSession : Screen("add_session")
    object EditSession : Screen("edit_session/{sessionId}") {
        fun createRoute(sessionId: String) = "edit_session/$sessionId"
    }
    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: String) = "movie_details/$movieId"
    }
    object SeatSelection : Screen("seat_selection/{sessionId}") {
        fun createRoute(sessionId: String) = "seat_selection/$sessionId"
    }
    object Cart : Screen("cart")
    object ProductList : Screen("product_list")
    object PurchaseHistory : Screen("purchase_history")
    object Reports : Screen("reports")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val movieViewModel: MovieViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val sessionViewModel: SessionViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val historyViewModel: PurchaseHistoryViewModel = viewModel()
    val reportsViewModel: ReportsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { user ->
                    if (user.type == UserType.EMPLOYEE) {
                        navController.navigate(Screen.Admin.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { user ->
                    if (user.type == UserType.EMPLOYEE) {
                        navController.navigate(Screen.Admin.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                movieViewModel = movieViewModel,
                cartViewModel = cartViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onMovieClick = { movie ->
                    navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToProducts = { navController.navigate(Screen.ProductList.route) },
                onNavigateToHistory = { navController.navigate(Screen.PurchaseHistory.route) },
                onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
            )
        }

        composable(Screen.ProductList.route) {
            ProductListScreen(
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.PurchaseHistory.route) {
            PurchaseHistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movieState = movieViewModel.movieState
            val movie = (movieState as? MovieState.Success)?.movies?.find { it.id == movieId }
            
            movie?.let {
                MovieDetailsScreen(
                    movie = it,
                    sessionViewModel = sessionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSessionClick = { session ->
                        navController.navigate(Screen.SeatSelection.createRoute(session.id))
                    }
                )
            }
        }

        composable(
            route = Screen.SeatSelection.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val sessionState = sessionViewModel.sessionState
            val session = (sessionState as? SessionState.Success)?.sessions?.find { it.id == sessionId }
            
            session?.let { currentSession ->
                SeatSelectionScreen(
                    session = currentSession,
                    onNavigateBack = { navController.popBackStack() },
                    onConfirmSeats = { selectedSeats ->
                        selectedSeats.forEach { seat ->
                            cartViewModel.addTicket(
                                Ticket(
                                    sessionId = currentSession.id,
                                    movieTitle = currentSession.movieTitle,
                                    seat = seat,
                                    price = currentSession.price
                                )
                            )
                        }
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }
        }

        composable(Screen.Cart.route) {
            CartScreen(
                viewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() },
                onFinishPurchase = { 
                    cartViewModel.finishPurchase {
                        historyViewModel.loadHistory() // Refresh history after purchase
                        navController.navigate(Screen.PurchaseHistory.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                }
            )
        }

        // --- Telas Administrativas ---
        composable(Screen.Admin.route) {
            AdminScreen(
                viewModel = authViewModel,
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                onManageMovies = { navController.navigate(Screen.ManageMovies.route) },
                onManageStock = { navController.navigate(Screen.ManageStock.route) },
                onManageSessions = { navController.navigate(Screen.ManageSessions.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.ManageMovies.route) {
            ManageMoviesScreen(
                viewModel = movieViewModel,
                onNavigateToAddMovie = { navController.navigate(Screen.AddMovie.route) },
                onNavigateToEditMovie = { movieId -> 
                    navController.navigate(Screen.EditMovie.createRoute(movieId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddMovie.route) {
            AddMovieScreen(
                viewModel = movieViewModel, 
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditMovie.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            AddMovieScreen(
                viewModel = movieViewModel,
                movieId = movieId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageStock.route) {
            ManageStockScreen(
                viewModel = productViewModel,
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(
                viewModel = productViewModel, 
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddProductScreen(
                viewModel = productViewModel,
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageSessions.route) {
            ManageSessionsScreen(
                viewModel = sessionViewModel,
                onNavigateToAddSession = { navController.navigate(Screen.AddSession.route) },
                onNavigateToEditSession = { sessionId ->
                    navController.navigate(Screen.EditSession.createRoute(sessionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddSession.route) {
            AddSessionScreen(
                movieViewModel = movieViewModel,
                sessionViewModel = sessionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditSession.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            AddSessionScreen(
                movieViewModel = movieViewModel,
                sessionViewModel = sessionViewModel,
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                viewModel = reportsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
