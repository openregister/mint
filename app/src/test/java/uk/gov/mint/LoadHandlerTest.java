package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;
import uk.gov.store.EntriesUpdateDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoadHandlerTest {

    @Mock
    EntriesUpdateDAO entriesUpdateDAO;

    @Captor
    ArgumentCaptor<List<byte[]>> entriesCaptor;

    @Test
    public void handle_addsTheHashAndThenSavesInTheDatabase() throws Exception {
        LoadHandler loadHandler = new LoadHandler(entriesUpdateDAO, new EntryValidator(new RegistersConfiguration(Optional.empty()), new FieldsConfiguration(Optional.empty())));

        String payload = "{\"register\":\"value1\"}\n{\"register\":\"value2\"}";

        String expectedHash1 = Digest.shasum("{\"register\":\"value1\"}");
        String expectedHash2 = Digest.shasum("{\"register\":\"value2\"}");

        final String entry1 = "{\"entry\":{\"register\":\"value1\"},\"hash\":\"" + expectedHash1 + "\"}";
        final byte[] entry1Bytes = canonicalise(entry1);
        final String entry2 = "{\"entry\":{\"register\":\"value2\"},\"hash\":\"" + expectedHash2 + "\"}";
        final byte[] entry2Bytes = canonicalise(entry2);

        loadHandler.handle("register", payload);

        verify(entriesUpdateDAO, times(1)).add(entriesCaptor.capture());
        final List<byte[]> payloadArray = entriesCaptor.getValue();
        assertThat(payloadArray.size(), equalTo(2));
        assertThat(payloadArray.get(0), equalTo(entry1Bytes));
        assertThat(payloadArray.get(1), equalTo(entry2Bytes));
    }

    private byte[] canonicalise(String nonCanonical) throws IOException {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(nonCanonical.getBytes(StandardCharsets.UTF_8));
        return canonicalJsonMapper.writeToBytes(jsonNode);
    }
}
