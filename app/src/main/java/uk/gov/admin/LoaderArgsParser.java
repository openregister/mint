package uk.gov.admin;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

class LoaderArgsParser {
    private final static Logger log = LoggerFactory.getLogger(LoaderArgsParser.class);

    OptionSet parseArgs(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("datafile", "File containing data to load. Currently only JSON is accepted.").withRequiredArg();
        parser.accepts("configfile", "File containing configuration in regular java.util.Properties format.").withRequiredArg();
        parser.accepts("overwrite", "Overwrite existing data.").withOptionalArg();

        final OptionSet options = parser.parse(args);
        if (!(options.has("datafile") && options.has("configfile"))) {
            throw new IllegalArgumentException(Loader.usageMessage);
        }

        return options;
    }

    // TODO: This should parse the Json text into JsonNode[]
    String parseJson(InputStream in) {
        try (InputStreamReader inr = new InputStreamReader(in);
             BufferedReader inb = new BufferedReader(inr)) {
            return inb.lines().collect(Collectors.joining(""));
        } catch (IOException e) {
            log.error("Error occurred reading the datafile", e);
            throw new RuntimeException(e);
        }
    }

    // Try to parse before pushing onto queue
    public InputStream process(String datafile) throws IOException {
        if (datafile.startsWith("/")) { // Absolute file path
            return new FileInputStream(new File(datafile));
        } else if (datafile.startsWith("http://")) { // URL
            return new URL(datafile).openStream();
        } else { // File in current dir
            return new FileInputStream(new File(datafile));
        }
    }
}
