package com.rank.football.data.repository

import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.StreamSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/** Maintains per-fixture stream URLs injected from remote config or admin API. */
class StreamRepository {

    private val fixtureStreams = ConcurrentHashMap<Int, List<StreamSource>>()
    private val _catalogLoaded = MutableStateFlow(false)
    val catalogLoaded: StateFlow<Boolean> = _catalogLoaded.asStateFlow()

    /** Returns configured streams for a fixture, or empty when none are available. */
    fun getStreamsForFixture(fixtureId: Int): List<StreamSource> =
        fixtureStreams[fixtureId].orEmpty()

    /** Returns true when at least one stream URL exists for the fixture. */
    fun hasStream(fixtureId: Int): Boolean = getStreamsForFixture(fixtureId).isNotEmpty()

    /** Returns fixture IDs that currently have configured streams. */
    fun streamableFixtureIds(): Set<Int> = fixtureStreams.filterValues { it.isNotEmpty() }.keys

    /** Keeps only fixtures that have at least one configured stream. */
    fun filterStreamable(fixtures: List<FixtureItem>): List<FixtureItem> =
        fixtures.filter { hasStream(it.fixture.id) }

    /** Stores stream sources for a single fixture. */
    fun setStreamsForFixture(fixtureId: Int, streams: List<StreamSource>) {
        if (streams.isEmpty()) fixtureStreams.remove(fixtureId) else fixtureStreams[fixtureId] = streams
        _catalogLoaded.value = fixtureStreams.isNotEmpty()
    }

    /** Bulk-updates stream mappings from remote configuration. */
    fun updateStreamsFromRemote(config: Map<Int, List<StreamSource>>) {
        config.forEach { (fixtureId, streams) -> setStreamsForFixture(fixtureId, streams) }
    }

    /** Replaces the entire catalog with a fresh remote snapshot. */
    fun replaceAll(config: Map<Int, List<StreamSource>>) {
        fixtureStreams.clear()
        config.forEach { (fixtureId, streams) ->
            if (streams.isNotEmpty()) fixtureStreams[fixtureId] = streams
        }
        _catalogLoaded.value = fixtureStreams.isNotEmpty()
    }

    /** Returns the number of fixtures with configured streams. */
    fun streamCount(): Int = fixtureStreams.size
}
