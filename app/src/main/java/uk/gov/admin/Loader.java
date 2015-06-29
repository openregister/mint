package uk.gov.admin;

import javaslang.collection.Stream;
import uk.gov.mint.RabbitMQConnector;

import java.util.Properties;

public class Loader {
    private LoaderArgsParser.LoaderArgs loaderArgs;

    Loader(LoaderArgsParser.LoaderArgs loaderArgs) {
        this.loaderArgs = loaderArgs;
    }

    public static void main(String[] args) {
        final LoaderArgsParser.LoaderArgs loaderArgs = new LoaderArgsParser().parseArgs(args);

        new Loader(loaderArgs).load();
    }

    public void load() {
        Properties props = new Properties();
        props.putAll(loaderArgs.config);
        try (RabbitMQConnector connector = new RabbitMQConnector(props, null)) {
            final Stream<String> collect = loaderArgs.data.collect(Stream.<String>collector());
            collect.grouped(1000).forEach(g -> {
                connector.publish(g.toJavaList());
            });
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }
}
