package com.afrimedia.crm.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.afrimedia.crm.data.remote.SessionManager
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.clients.ClientDetailScreen
import com.afrimedia.crm.ui.clients.ClientsListScreen
import com.afrimedia.crm.ui.dashboard.DashboardScreen
import com.afrimedia.crm.ui.invoices.InvoiceDetailScreen
import com.afrimedia.crm.ui.invoices.InvoicesListScreen
import com.afrimedia.crm.ui.login.LoginScreen
import com.afrimedia.crm.ui.projects.ProjectDetailScreen
import com.afrimedia.crm.ui.projects.ProjectFormScreen
import com.afrimedia.crm.ui.projects.ProjectsListScreen
import com.afrimedia.crm.ui.projects.TaskDetailScreen
import com.afrimedia.crm.ui.projects.TaskFormScreen
import com.afrimedia.crm.ui.quotes.QuoteDetailScreen
import com.afrimedia.crm.ui.quotes.QuotesListScreen

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab("dashboard", "Dashboard", Icons.Filled.Dashboard),
    BottomTab("clients", "Clients", Icons.Filled.People),
    BottomTab("quotes", "Quotes", Icons.Filled.Description),
    BottomTab("invoices", "Invoices", Icons.Filled.Receipt),
    BottomTab("projects", "Projects", Icons.Filled.Assignment)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(session: SessionManager, repository: Repository) {
    var loggedIn by remember { mutableStateOf(session.isLoggedIn) }

    if (!loggedIn) {
        LoginScreen(session = session, onLoggedIn = { loggedIn = true })
        return
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("dashboard") {
                DashboardScreen(repository = repository)
            }
            composable("clients") {
                ClientsListScreen(
                    repository = repository,
                    onOpenClient = { id -> navController.navigate("clients/$id") },
                    onNewClient = { navController.navigate("clients/0") }
                )
            }
            composable(
                "clients/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                ClientDetailScreen(
                    repository = repository,
                    clientId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("quotes") {
                QuotesListScreen(
                    repository = repository,
                    onOpenQuote = { id -> navController.navigate("quotes/$id") },
                    onNewQuote = { navController.navigate("quotes/0") }
                )
            }
            composable(
                "quotes/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                QuoteDetailScreen(
                    repository = repository,
                    quoteId = id,
                    onBack = { navController.popBackStack() },
                    onOpenInvoice = { invoiceId ->
                        navController.navigate("invoices/$invoiceId") { popUpTo("quotes") }
                    }
                )
            }
            composable("invoices") {
                InvoicesListScreen(
                    repository = repository,
                    onOpenInvoice = { id -> navController.navigate("invoices/$id") },
                    onNewInvoice = { navController.navigate("invoices/0") }
                )
            }
            composable(
                "invoices/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                InvoiceDetailScreen(
                    repository = repository,
                    invoiceId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("projects") {
                ProjectsListScreen(
                    repository = repository,
                    onOpenProject = { id -> navController.navigate("projects/$id") },
                    onNewProject = { navController.navigate("projects/0/edit") }
                )
            }
            composable(
                "projects/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                ProjectDetailScreen(
                    repository = repository,
                    projectId = id,
                    onBack = { navController.popBackStack() },
                    onEditProject = { pid -> navController.navigate("projects/$pid/edit") },
                    onOpenTask = { taskId -> navController.navigate("tasks/$taskId") },
                    onNewTask = { pid, taskListId -> navController.navigate("projects/$pid/task-lists/$taskListId/tasks/0") }
                )
            }
            composable(
                "projects/{id}/edit",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                ProjectFormScreen(
                    repository = repository,
                    projectId = id,
                    onBack = { navController.popBackStack() },
                    onSaved = { savedId ->
                        navController.navigate("projects/$savedId") {
                            popUpTo("projects") { inclusive = false }
                        }
                    }
                )
            }
            composable(
                "projects/{projectId}/task-lists/{taskListId}/tasks/0",
                arguments = listOf(
                    navArgument("projectId") { type = NavType.IntType },
                    navArgument("taskListId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getInt("projectId") ?: 0
                val taskListId = backStackEntry.arguments?.getInt("taskListId") ?: 0
                TaskFormScreen(
                    repository = repository,
                    taskId = 0,
                    projectId = pid,
                    taskListId = taskListId,
                    onBack = { navController.popBackStack() },
                    onSaved = { savedTaskId ->
                        navController.navigate("tasks/$savedTaskId") {
                            popUpTo("projects/$pid") { inclusive = false }
                        }
                    }
                )
            }
            composable(
                "tasks/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                TaskDetailScreen(
                    repository = repository,
                    taskId = id,
                    onBack = { navController.popBackStack() },
                    onEditTask = { taskId -> navController.navigate("tasks/$taskId/edit") }
                )
            }
            composable(
                "tasks/{id}/edit",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                TaskFormScreen(
                    repository = repository,
                    taskId = id,
                    projectId = 0,
                    taskListId = 0,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
