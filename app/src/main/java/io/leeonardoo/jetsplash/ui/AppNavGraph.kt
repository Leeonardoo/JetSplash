package io.leeonardoo.jetsplash.ui

import io.leeonardoo.jetsplash.ui.parallel.NavGraph
import io.leeonardoo.jetsplash.ui.parallel.destinations.ParallelScreenDestination

val appNavGraph = NavGraph(
    route = "nav_graph",
    startRoute = ParallelScreenDestination,
    destinations = listOf(
        ParallelScreenDestination
    )
)