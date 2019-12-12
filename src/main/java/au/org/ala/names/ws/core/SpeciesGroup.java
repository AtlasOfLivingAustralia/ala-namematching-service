package au.org.ala.names.ws.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

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
    Boolean isPartOfGroup(Integer lft){

        for (LftRgtValues lftRgtValue : lftRgtValues){
            if(lft >= lftRgtValue.lft && lft < lftRgtValue.rgt)
                return true;
        }
        return false;
    }
}
