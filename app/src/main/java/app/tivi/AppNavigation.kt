/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navArgument
import app.tivi.account.AccountUi
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.home.discover.Discover
import app.tivi.home.followed.Followed
import app.tivi.home.popular.Popular
import app.tivi.home.recommended.Recommended
import app.tivi.home.search.Search
import app.tivi.home.trending.Trending
import app.tivi.home.watched.Watched
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.seasons.ShowSeasons
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

internal sealed class Screen(val route: String) {
    object Discover : Screen("discoverroot")
    object Following : Screen("followingroot")
    object Watched : Screen("watchedroot")
    object Search : Screen("searchroot")
}

private sealed class LeafScreen(val route: String) {
    object Discover : LeafScreen("discover")
    object Following : LeafScreen("following")
    object Trending : LeafScreen("trending")
    object Popular : LeafScreen("popular")

    object ShowDetails : LeafScreen("show/{showId}") {
        fun createRoute(showId: Long): String = "show/$showId"
    }

    object EpisodeDetails : LeafScreen("episode/{episodeId}") {
        fun createRoute(episodeId: Long): String = "episode/$episodeId"
    }

    object ShowSeasons : LeafScreen("show/{showId}/seasons?seasonId={seasonId}") {
        fun createRoute(showId: Long, seasonId: Long? = null): String {
            return "show/$showId/seasons" + (if (seasonId != null) "?seasonId=$seasonId" else "")
        }
    }

    object RecommendedShows : LeafScreen("recommendedshows")
    object Watched : LeafScreen("watched")
    object Search : LeafScreen("search")
    object Account : LeafScreen("account")
}

@ExperimentalAnimationApi
private fun tiviEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph

    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeIn()
    }

    return fadeIn() + slideInHorizontally(initialOffsetX = { width -> width / 2 })
}

@ExperimentalAnimationApi
private fun tiviExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph

    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeOut()
    }

    return fadeOut() + slideOutHorizontally(targetOffsetX = { width -> -width / 2 })
}

private fun NavDestination.isStartDestination(): Boolean {
    return hostNavGraph.startDestinationId == this.id
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

@Suppress("UNUSED_VARIABLE")
@ExperimentalAnimationApi
private fun tiviPopEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    if (initial.destination.isStartDestination() && target.destination.isStartDestination()) {
        return fadeIn()
    }
    return fadeIn() + slideInHorizontally(initialOffsetX = { width -> -width / 2 })
}

@ExperimentalAnimationApi
private fun tiviPopExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    if (initial.destination.isStartDestination() && target.destination.isStartDestination()) {
        return fadeOut()
    }
    return fadeOut() + slideOutHorizontally(targetOffsetX = { width -> width / 2 })
}

@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Discover.route
    ) {
        addDiscoverTopLevel(navController, onOpenSettings)
        addFollowingTopLevel(navController, onOpenSettings)
        addWatchedTopLevel(navController, onOpenSettings)
        addSearchTopLevel(navController, onOpenSettings)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Discover.route,
        startDestination = LeafScreen.Discover.route,
        enterTransition = ::tiviEnterTransition,
        exitTransition = ::tiviExitTransition,
        popEnterTransition = ::tiviPopEnterTransition,
        popExitTransition = ::tiviPopExitTransition,
    ) {
        addDiscover(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addShowSeasons(navController)
        addEpisodeDetails(navController)
        addRecommendedShows(navController)
        addTrendingShows(navController)
        addPopularShows(navController)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addFollowingTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Following.route,
        startDestination = LeafScreen.Following.route,
        enterTransition = ::tiviEnterTransition,
        exitTransition = ::tiviExitTransition,
        popEnterTransition = ::tiviPopEnterTransition,
        popExitTransition = ::tiviPopExitTransition,
    ) {
        addFollowedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addShowSeasons(navController)
        addEpisodeDetails(navController)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addWatchedTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Watched.route,
        startDestination = LeafScreen.Watched.route,
        enterTransition = ::tiviEnterTransition,
        exitTransition = ::tiviExitTransition,
        popEnterTransition = ::tiviPopEnterTransition,
        popExitTransition = ::tiviPopExitTransition,
    ) {
        addWatchedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addShowSeasons(navController)
        addEpisodeDetails(navController)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Search.route,
        startDestination = LeafScreen.Search.route,
        enterTransition = ::tiviEnterTransition,
        exitTransition = ::tiviExitTransition,
        popEnterTransition = ::tiviPopEnterTransition,
        popExitTransition = ::tiviPopExitTransition,
    ) {
        addSearch(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addShowSeasons(navController)
        addEpisodeDetails(navController)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscover(navController: NavController) {
    composable(LeafScreen.Discover.route) {
        Discover(
            openTrendingShows = {
                navController.navigate(LeafScreen.Trending.route)
            },
            openPopularShows = {
                navController.navigate(LeafScreen.Popular.route)
            },
            openRecommendedShows = {
                navController.navigate(LeafScreen.RecommendedShows.route)
            },
            openShowDetails = { showId, episodeId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
                }
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addFollowedShows(navController: NavController) {
    composable(LeafScreen.Following.route) {
        Followed(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addWatchedShows(navController: NavController) {
    composable(LeafScreen.Watched.route) {
        Watched(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearch(navController: NavController) {
    composable(LeafScreen.Search.route) {
        Search(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowDetails(navController: NavController) {
    composable(
        route = LeafScreen.ShowDetails.route,
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType }
        )
    ) {
        ShowDetails(
            navigateUp = navController::navigateUp,
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
            },
            openSeasons = { showId, seasonId ->
                navController.navigate(LeafScreen.ShowSeasons.createRoute(showId, seasonId))
            }
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addEpisodeDetails(navController: NavController) {
    composable(
        route = LeafScreen.EpisodeDetails.route,
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        )
    ) {
        EpisodeDetails(
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addRecommendedShows(navController: NavController) {
    composable(LeafScreen.RecommendedShows.route) {
        Recommended(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addTrendingShows(navController: NavController) {
    composable(LeafScreen.Trending.route) {
        Trending(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addPopularShows(navController: NavController) {
    composable(LeafScreen.Popular.route) {
        Popular(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addAccount(
    navController: NavController,
    onOpenSettings: () -> Unit,
) {
    dialog(LeafScreen.Account.route) {
        AccountUi(navController, onOpenSettings)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowSeasons(navController: NavController) {
    composable(
        route = LeafScreen.ShowSeasons.route,
        arguments = listOf(
            navArgument("showId") {
                type = NavType.LongType
            },
            navArgument("seasonId") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        ShowSeasons(
            navigateUp = navController::navigateUp,
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
            },
            initialSeasonId = it.arguments?.getString("seasonId")?.toLong()
        )
    }
}
