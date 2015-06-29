package uk.gov.mint;

import com.rabbitmq.client.*;
import uk.gov.integration.DataStoreApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnector implements AutoCloseable {
    private final DataStoreApplication dataStoreApplication;
    private final String connectionString;
    private final String queue;
    private final String exchange;
    private final String routingKey;

    private Channel channel;

    public RabbitMQConnector(Properties configuration, DataStoreApplication dataStoreApplication) {
        connectionString = configuration.getProperty("rabbitmq.connection.string");
        queue = configuration.getProperty("rabbitmq.queue");
        exchange = configuration.getProperty("rabbitmq.exchange");
        routingKey = configuration.getProperty("rabbitmq.exchange.routing.key");

        this.dataStoreApplication = dataStoreApplication;

        try {
            channel = prepareConnection();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | TimeoutException | IOException e) {
            throw new RuntimeException("Could not create connection to RabbitMQ", e);
        }
    }

    public void connect(Properties configuration) {
        try {
            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

            Consumer consumer = new MessageHandler(channel, dataStoreApplication);
            channel.basicConsume(queue, consumer);
        } catch (NullPointerException e) {
            throw new RuntimeException("Did you call prepareConnection?", e);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void publish(String data) {
        try {
            channel.basicPublish(exchange, routingKey, null, data.getBytes());
        } catch (NullPointerException e) {
            throw new RuntimeException("Did you call prepareConnection?", e);
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }

    private Channel prepareConnection() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(connectionString);
        Connection conn = factory.newConnection();
        return conn.createChannel();
    }

    @Override
    public void close() throws Exception {
        channel.close();
        channel.getConnection().close();
    }

    public void publish(List<String> listOfData) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(listOfData);
            channel.basicPublish(exchange, routingKey, null, outputStream.toByteArray());
        } catch (NullPointerException e) {
            throw new RuntimeException("Did you call prepareConnection?", e);
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }
}
