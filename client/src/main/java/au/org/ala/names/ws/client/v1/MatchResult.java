package au.org.ala.names.ws.client.v1;

import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;

public class MatchResult extends Result<NameSearch, NameUsageMatch> {
    public MatchResult(NameSearch key) {
        super(key);
    }

    public static MatchResult empty(NameSearch key) {
        return new MatchResult(key);
    }
}
