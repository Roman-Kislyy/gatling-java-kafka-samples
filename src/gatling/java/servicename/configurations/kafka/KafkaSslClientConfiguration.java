package servicename.configurations.kafka;

import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import helpers.ResourceHelper;
import helpers.ssl.JksHelper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static helpers.ResourceHelper.gatlingResourcePath;
import static ru.tinkoff.gatling.javaapi.SimulationConfig.getStringParam;
import static ru.tinkoff.gatling.kafka.javaapi.KafkaDsl.kafka;

/**
 * <h2>Класс настроек подключения к Kafka</h2>
 * <p>
 *     Параметры подключения к брокеру берутся из файла настроек <b>resources/simulation_.conf</b>
 * <p>
 *     Если вам требуется несколько вариантов подключений (с разными логинами или брокерами), то создайте копии класса c другими именами и используйте их.
 *
 * @author  Roman Kislyy
 * @since 2023-08-31
 */
public class KafkaSslClientConfiguration {
    private final Logger log = LoggerFactory.getLogger(KafkaSslClientConfiguration.class);
    private String bootstrap = getStringParam("kafka.bootstrap.ssl.servers");
    private String keystore = getStringParam("kafka.keystore");
    private String keystorePass = getStringParam("kafka.keystorePass");
    private String truststore = getStringParam("kafka.truststore");
    private String truststorePass = getStringParam("kafka.truststorePass");

    /**
     * <p>Настройка протокола Kafka с SSL для использвоания в симуляции.</p>
     *
     * @return объект с типом ru.tinkoff.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew
     * @author Roman Kislyy
     * @since 2023-08-31
     */
    public ru.tinkoff.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew protocol(){

//        String keystoreAbsolutePath = "d:\\load\\git\\gatling-java-kafka-samples\\src\\gatling\\resources\\" + (keystore);
//        String truststoreAbsolutePath = "d:\\load\\git\\gatling-java-kafka-samples\\src\\gatling\\resources\\" + (truststore);
        String keystoreAbsolutePath = gatlingResourcePath(keystore);
        String truststoreAbsolutePath = gatlingResourcePath(truststore);
        // Проверяем доступность сертификатов и пароли к ним
        try {
            if (keystore.equals("") || truststore.equals("")){
                log.error("Keystore and truststore path's are required!");
                throw new Exception();
            }
            JksHelper.isValid(keystoreAbsolutePath, keystorePass);
            JksHelper.isValid(truststoreAbsolutePath, truststorePass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Заголовки для продюсера
        HashMap<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        producerProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        producerProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreAbsolutePath);
        producerProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePass);
        producerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreAbsolutePath);
        producerProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePass);
        producerProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePass);

        // Заголовки для консюмера
        HashMap<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
//        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "val");
//        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "val");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringArrayDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 5000);
        consumerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        consumerProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        consumerProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreAbsolutePath);
        consumerProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePass);
        consumerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreAbsolutePath);
        consumerProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePass);
        consumerProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePass);

        return kafka().requestReply()
                .producerSettings(
                    // Про ProducerConfig https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html
                    // Если здесь возникает ошибка java: incompatible types: no instance(s) of type variable(s) K,V exist so that java.util.Map<K,V> conforms to scala.collection.immutable.Map<java.lang.String,java.lang.Object>
                    // То проверьте, что все импорты джавовые и из javaapi
                    producerProps
                )
                .consumeSettings(consumerProps)
                .timeout(Duration.ofSeconds(10));
    }
}
