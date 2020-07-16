package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;

import java.io.Closeable;

public interface ALANameMatchService extends Closeable {

    /**
     * Matches scientific names against the ALA Taxonomy.
     *
     * @param key The search key
     *
     * @return a possible null name match
     */
    NameUsageMatch match(NameSearch key);
}
