package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.*;
import au.org.ala.names.AlaLinnaeanClassification;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.health.Checkable;
import org.cache2k.operation.CacheControl;
import org.cache2k.operation.CacheStatistics;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

/**
 * Common functionality for all search resources
 */
abstract public class SearchResource implements Checkable, Closeable {
    /**
     * Search style map
     */
    protected static final Map<SearchStyle, MatchOptions> SEARCH_STYLE_MAP = Map.of(
            SearchStyle.STRICT, MatchOptions.NONE,
            SearchStyle.FUZZY, MatchOptions.NONE.withFuzzyDerivations(true).withCanonicalDerivations(true).withModifyConsistency(true).withUseHints(true),
            SearchStyle.MATCH, MatchOptions.ALL
    );

    protected final TaxonomyResource taxonomy;
    /**
     * Use hints to guide search
     */
    protected final boolean useHints;
    /**
     * Use hints to check search
     */
    protected final boolean checkHints;
    /**
     * Default search style
     */
    protected final SearchStyle defaultStyle;
    /**
     * Measure performance
     */
    protected final boolean searchMetrics;

    /**
     * Construct for a configuration and taxonomic resource
     *
     * @param configuration The configuration
     * @param taxonomy The taxonomic resource
     */
    public SearchResource(NameSearchConfiguration configuration, TaxonomyResource taxonomy) {
        this.taxonomy = taxonomy;
        this.useHints = configuration.isUseHints();
        this.checkHints = configuration.isCheckHints();
        this.defaultStyle = configuration.getDefaultStyle();
        this.searchMetrics = configuration.isSearchMetrics();
    }

    /**
     * Make sure that the system is still operating.
     * <p>
     * By default, this returns true. Override to actually check things.
     * </p>
     *
     * @return True if things can still be found.
     */
    @Override
    public boolean check() {
        return true;
    }

    /**
     * Get information about the search resource.
     *
     * @return A suitably jsonable map of configuration and metrics
     */
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap();
        metrics.put("config", this.getConfigurationReport());
        metrics.put("search", this.getSearchReport());
        metrics.put("cache", this.getCacheReport());
        return metrics;
    }

    /**
     * Get the configuration part of the metrics.
     *
     * @return A configuration map
     */
    protected Map<String, Object> getConfigurationReport() {
        Map<java.lang.String, java.lang.Object> config = new HashMap<>();
        config.put("useHints", this.useHints);
        config.put("checkHints", this.checkHints);
        config.put("defaultStyle", this.defaultStyle);
        config.put("searchMetrics", this.searchMetrics);
        return config;
    }

    /**
     * Get the matcher used by this resource
     *
     * @return The matcher or null for not known
     */
    protected ClassificationMatcher<?, ?, ?, ?> getMatcher() {
        return null;
    }

    /**
     * Construct statistics for a matcher
     *
     * @return The match statistics
     */
    protected Map<String, Object> getSearchReport() {
        Map<String, Object> statistics = new HashMap<>();
        ClassificationMatcher<?, ?, ?, ?> matcher = this.getMatcher();
        if (matcher != null && this.searchMetrics) {
            statistics.put("candidates", matcher.getCandidateStatistics());
            statistics.put("hintModifications", matcher.getHintModificationStatistics());
            statistics.put("matchable", matcher.getMatchableStatistics());
            statistics.put("matchModifications", matcher.getMatchModificationStatistics());
            statistics.put("matches", matcher.getMatchStatistics());
            statistics.put("maxCandidates", matcher.getMaxCandidateStatistics());
            statistics.put("requests", matcher.getRequests());
            statistics.put("searchModifications", matcher.getSearchModificationStatistics());
            statistics.put("searches", matcher.getSearchStatistics());
            statistics.put("times", matcher.getTimeStatistics());
        }
        return statistics;
    }

    /**
     * Get the cache part of the metrics.
     *
     * @return A configuration map
     */
    protected Map<String, Object> getCacheReport() {
        Map<java.lang.String, java.lang.Object> cache = new HashMap<>();
        return cache;
    }

    /**
     * Close the resource.
     */
    @Override
    public void close() {
    }


}
