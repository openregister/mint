package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class UnvalidatedDatatype implements Datatype{

    @Override
    public boolean isValid(JsonNode value) {
        return true;
    }

}