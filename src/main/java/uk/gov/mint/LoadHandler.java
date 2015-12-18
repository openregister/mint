package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.store.EntriesUpdateDAO;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final String register;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private final EntryValidator entryValidator;

    public LoadHandler(String register, EntriesUpdateDAO entriesUpdateDAO, EntryValidator entryValidator) {
        this.register = register;
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.entryValidator = entryValidator;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
        entriesUpdateDAO.ensureTableExists();
    }

    public void handle(String payload) {
        processEntries(payload.split("\n"));
    }

    private void processEntries(String[] entries) {
        final List<byte[]> entriesAsBytes = Arrays.stream(entries)
                .map(e -> {
                    try {
                        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                        //Note: commented the entry validation till the data is not cleaned
                        //Also Validation doesn't respect the cardinality of a field. trello card https://trello.com/c/6GIewuwc
//                        entryValidator.validateEntry(register, jsonNode);
                        return canonicalJsonMapper.writeToBytes(hashedEntry(jsonNode));
                    } catch (Exception ex) {
                        //Rethrowing this error using ExceptionUtils because I want to return
                        //the JsonParseException to the caller of processEntries method. This will be then handled by JsonParseExceptionMapper.
                        return ExceptionUtils.rethrow(ex);
                    }
                })
                .collect(Collectors.toList());
        entriesUpdateDAO.add(entriesAsBytes);
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}