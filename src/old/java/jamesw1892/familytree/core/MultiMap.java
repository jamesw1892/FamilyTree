package core;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Data structure allowing efficient storage of multiple distinct values of
 * generic type V under the same key of generic type K.
 */
public class MultiMap<K, V> {
    private HashMap<K, HashSet<V>> map;

    /**
     * Contruct an empty MultiMap
     */
    public MultiMap() {
        this.map = new HashMap<>();
    }

    /**
     * Get a string representation of the MultiMap
     */
	public String toString() {
		return this.map.toString();
	}

    /**
     * Get all values associated with the given key
     * @param key
     * @return
     */
	public HashSet<V> getAll(K key) {
		return this.map.get(key);
	}

    /**
     * Associate the value with the given key
     * @param key
     * @param value
     */
    public void add(K key, V value) {
        HashSet<V> values = this.map.get(key);
        if (values == null) {
            values = new HashSet<>();
            values.add(value);
            this.map.put(key, values);
        } else {
            values.add(value);
        }
    }

    /**
     * Remove the given key and all associated
     * values from the MultiMap
     * @param key
     * @return
     */
	public HashSet<V> remove(K key) {
		return this.map.remove(key);
	}
}