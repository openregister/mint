package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public interface Datatype {
    boolean isValid(JsonNode value);

    String getName();
}

