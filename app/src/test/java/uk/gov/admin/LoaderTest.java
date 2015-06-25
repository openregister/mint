package uk.gov.admin;

import joptsimple.OptionSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoaderTest {
    @Test(expected = IllegalArgumentException.class)
    public void loader_should_parse_empty_args() {
        final String[] args = new String[0];
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final OptionSet optionSet = loaderArgsParser.parseArgs(args);
    }

    @Test(expected = NullPointerException.class)
    public void loader_should_not_parse_null_args() {
        final String[] args = null;
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final OptionSet optionSet = loaderArgsParser.parseArgs(args);
    }

    @Test
    public void loader_should_parse_without_optional_args() throws Exception {
        final String[] args = new String[]{"--datafile", "boo", "--configfile", "baa"};
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final OptionSet optionSet = loaderArgsParser.parseArgs(args);

        assertEquals("boo", optionSet.valueOf("datafile"));
        assertEquals("baa", optionSet.valueOf("configfile"));
    }

    @Test
    public void loader_should_parse_with_optional_args() throws Exception {
        final String[] args = new String[]{"--datafile=boo", "--overwrite", "--configfile=baa"};
        final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
        final OptionSet optionSet = loaderArgsParser.parseArgs(args);

        assertEquals("boo", optionSet.valueOf("datafile"));
        assertEquals("baa", optionSet.valueOf("configfile"));
        assertTrue(optionSet.has("overwrite"));
    }
}
