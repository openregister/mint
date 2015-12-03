package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.datatype.Datatype;
import uk.gov.register.datatype.DatatypeFactory;

import java.util.Optional;

public class Field {
    final String fieldName;
    final Datatype datatype;
    final Optional<String> register;
    final Cardinality cardinality;
    final String text;

    @JsonCreator
    public Field(@JsonProperty("field") String fieldName,
                 @JsonProperty("datatype") String datatype,
                 @JsonProperty("register") String register,
                 @JsonProperty("cardinality") Cardinality cardinality,
                 @JsonProperty("text") String text) {
        this.fieldName = fieldName;
        this.datatype = DatatypeFactory.get(datatype);
        this.register = StringUtils.isNotEmpty(register) ? Optional.of(register) : Optional.empty();
        this.cardinality = cardinality;
        this.text = text;
    }

    public Optional<String> getRegister() {
        return register;
    }

    @SuppressWarnings("unused")
    public Cardinality getCardinality() {
        return cardinality;
    }

    public Datatype getDatatype() {
        return datatype;
    }
}
