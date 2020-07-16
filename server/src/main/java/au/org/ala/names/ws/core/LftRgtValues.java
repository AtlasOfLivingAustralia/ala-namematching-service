package au.org.ala.names.ws.core;

import au.org.ala.vocab.IsDefinedBy;
import lombok.Builder;

@Builder
public class LftRgtValues {
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/left")
    Integer lft;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/right")
    Integer rgt;
    Boolean tobeIncluded;
}
