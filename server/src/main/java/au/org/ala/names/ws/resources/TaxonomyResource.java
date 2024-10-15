package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.Observable;
import au.org.ala.location.AlaLocationFactory;
import au.org.ala.names.ALANameSearcher;
import au.org.ala.names.AlaLinnaeanFactory;
import au.org.ala.names.RankAnalysis;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import au.org.ala.names.ws.health.Checkable;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A shared set of resources for taxonomy.
 */
@Slf4j
public class TaxonomyResource implements Closeable, Managed, Checkable {
    //** Linnaean names onto observables for hints */
    private static final Map<String, Observable<String>> HINT_OBSERVABLE_MAP = Collections.unmodifiableMap(buildHintObservableMap());

    private static Map<String, Observable<String>> buildHintObservableMap() {
        Map<String, Observable<String>> hoMap = new HashMap<>();
        for (Observable<?> observable: AlaLinnaeanFactory.OBSERVABLES) {
            if (observable.getStyle() != Observable.Style.IDENTIFIER && observable.getType() == String.class)
                hoMap.put(observable.getTerm().simpleName(), (Observable<String>) observable);
        }
        for (Observable<?> observable: AlaLocationFactory.OBSERVABLES) {
            if (observable.getStyle() != Observable.Style.IDENTIFIER && observable.getType() == String.class)
                hoMap.put(observable.getTerm().simpleName(), (Observable<String>) observable);
        }
        return hoMap;
    }

    /**
     * Searcher for names
     */
    @Getter
    private final ALANameSearcher searcher;
    /**
     * Rank name to value analysis
     */
    @Getter
    private final RankAnalysis rankAnalysis = new RankAnalysis();
    /**
     * Map taxa onto species groups
     */
    @Getter
    private final SpeciesGroupsUtil speciesGroups;

    /**
     * Construct a taxonomy resource from a configuration
     *
     * @param configuration The configuration
     *
     * @throws Exception If unable to build the resource
     */
    public TaxonomyResource(NameSearchConfiguration configuration) throws Exception {
        this.searcher = new ALANameSearcher(configuration.getSearcher());
        this.speciesGroups = SpeciesGroupsUtil.getInstance(configuration, this.searcher);

    }

    /**
     * Get the observable that corresponds to a piece of the classification.
     * <p>
     * Only string observables are considered worthy.
     * </p>
     *
     * @param name The name
     *
     * @return The equivalent observable, or none for n
     */
    public Observable<String> getObservable(String name) {
        return HINT_OBSERVABLE_MAP.get(name);
    }

    /**
     * Clean up on close
     *
     * @throws IOException if unable to close the resoruce
     */
    @Override
    public void close() throws IOException {
        try {
            this.searcher.close();
        } catch (Exception ex) {
            throw new IOException("Failure on taxonomy resource close", ex);
        }
    }

    @Override
    public void start() throws Exception {
        log.info("Starting taxonomy resource");
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping taxonomy resource");
        this.close();
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public Map<String, Object> getMetrics() {
        return new HashMap<>();
    }
}
