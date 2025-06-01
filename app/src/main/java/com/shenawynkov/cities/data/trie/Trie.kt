package com.shenawynkov.cities.data.trie

import com.shenawynkov.cities.domain.model.City
import java.util.Locale // For lowercase normalization

/**
 * Represents a node in the Trie.
 * Each node has children mapping to subsequent characters and can mark the end of a word (city name).
 * It stores a list of City objects if multiple cities end at this node (e.g., same name in different countries,
 * though city ID should make them unique, Trie key is based on name string).
 */
class TrieNode {
    val children: MutableMap<Char, TrieNode> = mutableMapOf()
    var isEndOfWord: Boolean = false
    val cities: MutableList<City> = mutableListOf()
}

/**
 * A Trie (Prefix Tree) data structure optimized for fast prefix-based searches of city names.
 * This implementation is chosen to meet the requirement for a search algorithm with
 * time efficiency better than linear (O(L) where L is the length of the prefix)
 *
 * It stores city names in a case-insensitive manner by normalizing them to lowercase.
 */
class Trie {
    private val root = TrieNode()
    private var isBuilt = false

    /**
     * Inserts a city into the Trie.
     * The city name is normalized to lowercase for case-insensitive storage and search.
     */
    private fun insert(city: City) {
        var current = root
        val normalizedName = city.name.lowercase(Locale.ENGLISH)
        for (char in normalizedName) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
        current.cities.add(city) // Add the original City object
    }

    /**
     * Searches for cities with names starting with the given prefix.
     * The prefix is normalized to lowercase.
     * Returns a list of matching City objects.
     */
    fun search(prefix: String): List<City> {
        if (!isBuilt) return emptyList() // Ensure Trie is built before searching
        val normalizedPrefix = prefix.lowercase(Locale.getDefault())
        var current = root
        for (char in normalizedPrefix) {
            val node = current.children[char]
            if (node == null) {
                return emptyList() // Prefix not found
            }
            current = node
        }
        // At this point, 'current' is the node corresponding to the end of the prefix.
        // We need to collect all cities from this node and its descendants.
        return collectAllCitiesFromNode(current)
    }

    /**
     * Collects all City objects from this node and all its descendant nodes.
     * Used by search to get all words starting with a given prefix.
     */
    private fun collectAllCitiesFromNode(node: TrieNode): List<City> {
        val results = mutableListOf<City>()
        if (node.isEndOfWord) {
            results.addAll(node.cities)
        }
        for ((_, childNode) in node.children) {
            results.addAll(collectAllCitiesFromNode(childNode))
        }
        // Results from Trie are based on prefix of "name, country".
        // They are already somewhat ordered by the Trie traversal.
        // The final sorting is crucial for display consistency.
        return results.distinctBy { it.id }
                       .sortedWith(compareBy({ it.name.lowercase() }, { it.country.lowercase() }))
    }

    /**
     * Builds the Trie from a list of cities.
     * Clears any existing data in the Trie before building.
     */
    fun build(cities: List<City>) {
        if (isBuilt) return // Avoid rebuilding if already done
        reset() // Clear previous state
        for (city in cities) {
            insert(city)
        }
        isBuilt = true
    }
    
    /**
     * Resets the Trie to an empty state.
     */
    private fun reset() {
        root.children.clear()
        root.isEndOfWord = false
        root.cities.clear()
    }
} 