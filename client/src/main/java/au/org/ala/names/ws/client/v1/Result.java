package au.org.ala.names.ws.client.v1;

import lombok.Data;

/**
 * A result with a key and a current value.
 * <p>
 * If the current value is null, then it needs to be retrieved
 * </p>
 *
 * @param <K> The key class
 * @param <V> The value class
 */
@Data
public class Result<K, V> {
    private K key;
    private V value;

    public Result(K key) {
        this.key = key;
        this.value = null;
    }

    public boolean isSet() {
        return this.value != null;
    }
}
