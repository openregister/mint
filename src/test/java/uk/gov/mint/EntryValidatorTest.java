package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EntryValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(Optional.empty());
    private RegistersConfiguration registerConfiguration = new RegistersConfiguration(Optional.empty());

    private EntryValidator entryValidator = new EntryValidator(registerConfiguration, fieldsConfiguration);

    @Test
    public void validateEntry_throwsValidationException_givenPrimaryKeyOfRegisterNotExists() throws IOException {
        String jsonString = "{\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Entry does not contain primary key field 'register'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenPrimaryKeyFieldIsEmpty() throws IOException {
        String jsonString = "{\"register\":\"  \",\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Primary key field 'register' must have a valid value"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenFieldValueIsNotOfCorrectDatatypeType() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":5}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'text' value must be of type 'text'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenEntryContainsUnknownFields() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"5\",\"key1\":\"value\",\"key2\":\"value\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Entry contains invalid fields: [key1, key2]"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenFieldWithCardinalityManyHasNonArrayValue() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":\"nonAcceptableNonArrayFieldValue\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' has cardinality 'n' so the value must be an array of 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenFieldWithCardinalityManyHasNonMatchedDatatypeValues() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":[\"foo\",5]}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' values must be of type 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void noErrorWhenEntryIsValid() throws IOException, EntryValidationException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"some text\"}";
        entryValidator.validateEntry("register", nodeOf(jsonString));
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, JsonNode.class);
    }

    @Test
    public void validateEntry_throwsValidationException_whenEmptyFieldsPresent() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Empty or blank fields are not allowed"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }
}
