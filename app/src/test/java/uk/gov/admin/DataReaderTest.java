package uk.gov.admin;

import org.junit.Test;

import static org.junit.Assert.*;

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
}
