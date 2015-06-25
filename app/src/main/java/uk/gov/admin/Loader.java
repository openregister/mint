package uk.gov.admin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loader {
    private final static Logger log = LoggerFactory.getLogger(Loader.class);
    private LoaderArgsParser.LoaderArgs loaderArgs;

    Loader(LoaderArgsParser.LoaderArgs loaderArgs) {
        this.loaderArgs = loaderArgs;
    }

    public static void main(String[] args) {
        final LoaderArgsParser.LoaderArgs loaderArgs = new LoaderArgsParser().parseArgs(args);

        new Loader(loaderArgs).load();
    }

    public void load() {
        try {
            String connectionString = (String) loaderArgs.config.get("rabbitmq.connection.string");
            String exchange = (String) loaderArgs.config.get("rabbitmq.exchange");
            String routingKey = (String) loaderArgs.config.get("rabbitmq.exchange.routing.key");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            channel.basicPublish(exchange, routingKey, null, loaderArgs.data.getBytes());
            channel.close();
            conn.close();
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }
}
