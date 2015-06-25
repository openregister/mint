package uk.gov.admin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

class LoaderArgsParser {
    private static final String usageMessage =
            "Usage: java Loader [--overwrite] --configfile=<config.properties> --schemafile=<dataschema.json> --datafile=<loadfile.json>";

    LoaderArgs parseArgs(String[] args) {

        final OptionSet options = optionParser().parse(args);
        if (!(options.has("datafile") && options.has("configfile") && options.has("schemafile"))) {
            throw new IllegalArgumentException(usageMessage);
        }

        return optionsToLoaderArgs(options);
    }

    private OptionParser optionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts("schemafile", "File containing the schema that describes the data format.").withRequiredArg();
        parser.accepts("datafile", "File containing data to load. Currently only JSON is accepted.").withRequiredArg();
        parser.accepts("configfile", "File containing configuration in regular java.util.Properties format.").withRequiredArg();
        parser.accepts("overwrite", "Overwrite existing data.").withOptionalArg();

        return parser;
    }

    private LoaderArgs optionsToLoaderArgs(OptionSet options) {
        final String datafile = (String) options.valueOf("datafile");
        final String configfile = (String) options.valueOf("configfile");
        final Boolean overwrite = options.has("overwrite");

        DataReader reader = new DataReader(datafile);

        Map<String, Object> config;
        try(final FileInputStream configInStream = new FileInputStream(configfile)) {
            final Properties configProps = new Properties();
            configProps.load(configInStream);

            config = configProps.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));
        } catch (IOException e) {
            throw new RuntimeException("Error occurred loading configfile: " + configfile, e);
        }

        return new LoaderArgs(validateDataInputAsJson(reader.data()), config, overwrite);
    }

    private String validateDataInputAsJson(String in) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readValue(in, JsonNode.class);

            return jsonNode.toString();
        } catch (JsonParseException | JsonMappingException e) {
            throw new RuntimeException("Error occurred parsing the json datafile - are you sure it is valid JSON?", e);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred reading the datafile", e);
        }
    }

    public class LoaderArgs {
        public final String data;
        public final Map<String, Object> config;
        public final boolean overwrite;

        public LoaderArgs(String data, Map<String, Object> config, boolean overwrite) {
            this.data = data;
            this.config = config;
            this.overwrite = overwrite;
        }
    }
}
