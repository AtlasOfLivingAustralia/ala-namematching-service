package au.org.ala.names.ws.client.v2;

import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import au.org.ala.names.ws.client.Result;

public class MatchResult extends Result<NameSearch, NameUsageMatch> {
    public MatchResult(NameSearch key) {
        super(key);
    }

    public static MatchResult empty(NameSearch key) {
        return new MatchResult(key);
    }
}
