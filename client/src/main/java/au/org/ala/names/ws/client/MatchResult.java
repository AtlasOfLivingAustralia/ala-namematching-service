package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;

public class MatchResult extends Result<NameSearch, NameUsageMatch> {
    public MatchResult(NameSearch key) {
        super(key);
    }

    public static MatchResult empty(NameSearch key) {
        return new MatchResult(key);
    }
}
