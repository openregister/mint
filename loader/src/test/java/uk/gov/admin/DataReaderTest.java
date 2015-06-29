package uk.gov.admin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataReaderTest {
    @Test
    public void should_be_able_to_read_local_file() {
        final String expectedData = "{\"address\":\"0000001\",\"postcode\":\"01010101\"}\n";

        final String localfilePath = "/Users/saqib/Devel/GDS/mint/loader/src/test/resources/test-load.jsonl";
        DataReader dataReader = new DataReader(localfilePath);
        final String data = dataReader.data();

        assertEquals(expectedData, data);
    }
}


