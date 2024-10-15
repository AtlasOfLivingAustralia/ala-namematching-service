package au.org.ala.names.ws.api;

import lombok.Getter;

/**
 * The type of search to undertake.
 */
public enum SearchStyle {
    /**
     * Standard match search.
     * <p>
     * The default.
     * Used for record name matching.
     * Includes higher-order matching, fuzzy matching and other
     *
     * </p>
     */
    MATCH(true, true, true),
    /**
     * Fuzzy matching.
     * <p>
     * Allow matching with allowance for misspelled names.
     * But no matching to higher-order concepts.
     * </p>
     */
    FUZZY(true, true, false),
    /**
     * Strict matching.
     * <p>
     * Used for exact matches in the case of list lookups, etc.
     * Only exact or canonical matches are accepted.
     * </p>
     */
    STRICT(false, false, false);

    /** Is this style loose by default? */
    @Getter
    private boolean loose;
    /** Does this style allow fuzzy lookups? */
    @Getter
    private boolean fuzzy;
    /** Does this style allow higher order lookups? */
    @Getter
    private boolean higherOrder;

    SearchStyle(boolean loose, boolean fuzzy, boolean higherOrder) {
        this.loose = loose;
        this.fuzzy = fuzzy;
        this.higherOrder = higherOrder;
    }
}
