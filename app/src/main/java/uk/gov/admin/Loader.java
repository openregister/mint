package uk.gov.admin;

import com.rabbitmq.client.*;
import joptsimple.OptionException;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class Loader {
    private final static Logger log = LoggerFactory.getLogger(Loader.class);

    static final String usageMessage = "Usage: java Loader [--overwrite] --configfile=<config.properties> --datafile=<loadfile.json>";
    private static Loader loader;

    public static void main(String[] args) {
        try {
            final LoaderArgsParser loaderArgsParser = new LoaderArgsParser();
            final OptionSet optionSet = loaderArgsParser.parseArgs(args);
            final String datafile = (String) optionSet.valueOf("datafile");
            final String configfile = (String) optionSet.valueOf("configfile");
            final Boolean overwrite = optionSet.has("overwrite");
            final String data = loaderArgsParser.parseJson(loaderArgsParser.process(datafile));
            final Properties config = new Properties();
            config.load(new FileInputStream(configfile));
            loader = new Loader(data, config, overwrite);
            loader.load();
        } catch (OptionException | IOException e) {
            log.error("Something bad happened...", e);
            log.error(usageMessage);
        }
    }

    private final String data;
    private final Boolean overwrite;
    private final Properties configuration;

    Loader(String data, Properties config, Boolean overwrite) {
        this.data = data;
        this.overwrite = overwrite;
        this.configuration = config;
    }

    public void load() {
        try {
            String connectionString = configuration.getProperty("rabbitmq.connection.string");
            String queue = configuration.getProperty("rabbitmq.queue");
            String exchange = configuration.getProperty("rabbitmq.exchange");
            String routingKey = configuration.getProperty("rabbitmq.exchange.routing.key");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

            channel.basicPublish(exchange, routingKey, null, data.getBytes());
            channel.close();
            conn.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
