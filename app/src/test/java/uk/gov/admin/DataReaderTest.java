package uk.gov.admin;

import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DataReaderTest {
    @Test
    public void should_be_able_to_read_local_file() {
        final String expectedData = "[\n" +
                "  {\n" +
                "    \"address\": \"0000001\",\n" +
                "    \"postcode\": \"01010101\"\n" +
                "  }\n" +
                "]\n";

        final String localfilePath = "/Users/saqib/Devel/GDS/mint/app/src/main/resources/test-load.json";
        DataReader dataReader = new DataReader(localfilePath);
        final String data = dataReader.data();

        assertEquals(expectedData, data);
    }

    @Test
    public void should_be_able_to_stream_local_file() throws IOException {
        final String expectedData =
                "{\"address\":\"0000001\",\"postcode\":\"01010101\"}\n" +
                "{\"address\":\"0000002\",\"postcode\":\"01010102\"}\n" +
                "{\"address\":\"0000003\",\"postcode\":\"01010103\"}\n" +
                "{\"address\":\"0000004\",\"postcode\":\"01010104\"}";

        final String localfilePath = "/Users/saqib/Devel/GDS/mint/app/src/main/resources/test-load.jsonl";
        DataReader dataReader = new DataReader(localfilePath);
        final String data = dataReader.streamData()
                .collect(Collectors.joining("\n"));

        assertEquals(expectedData, data);
    }
}


