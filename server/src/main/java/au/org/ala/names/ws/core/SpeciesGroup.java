package au.org.ala.names.ws.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;

import java.util.*;
import java.util.stream.Collectors;

@JsonDeserialize(builder = SpeciesGroup.SpeciesGroupBuilder.class)
@Builder
@JsonPOJOBuilder
public class SpeciesGroup {

    String name;
    String rank;
    List<String> values;
    List<String> excludedValues;
    List<LftRgtValues> lftRgtValues;
    String parent;

    /*
     * Determines whether the supplied lft value represents a species from this group/
     * Relies on the excluded values coming first in the lftRgtValues array
     */
    Boolean isPartOfGroup(Integer lft) {

        for (LftRgtValues lftRgtValue : lftRgtValues) {
            if (lft >= lftRgtValue.lft && lft < lftRgtValue.rgt)
                return lftRgtValue.tobeIncluded;
        }
        return false;
    }

    /**
     * Test to see if two species groups overlap in range.
     * <p>
     * We do this by testing the end points of each set of lr-values and seeing if
     * there is an overlap.
     * </p>
     *
     * @param other The other species group.
     *
     * @return True if at least one LR-pair in each group overlap.
     */
    public boolean overlaps(SpeciesGroup other) {
        return this.overlaps1(other) || other.overlaps1(this);
    }

    // Simple one dimensional overlap test
    private boolean overlaps1(SpeciesGroup other) {
        for (LftRgtValues lr: this.lftRgtValues) {
            if (this.isPartOfGroup(lr.lft) && other.isPartOfGroup(lr.lft))
                return true;
            if (this.isPartOfGroup(lr.rgt) && other.isPartOfGroup(lr.rgt)) // Occurs when exclusion is present
                return true;
        }
        return false;
    }
}
