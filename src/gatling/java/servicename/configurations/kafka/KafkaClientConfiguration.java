package servicename.configurations.kafka;

import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import static ru.tinkoff.gatling.kafka.javaapi.KafkaDsl.kafka;
import static ru.tinkoff.gatling.javaapi.SimulationConfig.getStringParam;

/**
 * <h2>Класс настроек подключения к Kafka без ssl и без авторизации</h2>
 * <p>
 *     Параметры подключения к брокеру берутся из файла настроек <b>resources/simulation_.conf</b>
 * <p>
 *     Если вам требуется несколько вариантов подключений (с разными логинами или брокерами), то создайте копии класса c другими именами и используйте их.
 * <p>
 *     Например, FromSiteKafkaClientConfiguration и FromMobileKafkaClientConfiguration
 * @author  Roman Kislyy
 * @since 2023-08-31
 */
public class KafkaClientConfiguration {
    private String bootstrap = getStringParam("kafka.bootstrap.servers");
    private String keystore = getStringParam("kafka.keystore");
    private String truststore = getStringParam("kafka.truststore");

    /**
     * <p>Настройка протокола Kafka для использвоания в симуляции.</p>
     *
     * @return объект с типом ru.tinkoff.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew
     * @author Roman Kislyy
     * @since 2023-08-31
     */
    public ru.tinkoff.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew protocol(){

        // Про ProducerConfig https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html
        // Если здесь возникает ошибка java: incompatible types: no instance(s) of type variable(s) K,V exist so that java.util.Map<K,V> conforms to scala.collection.immutable.Map<java.lang.String,java.lang.Object>
        // То проверьте, что все импорты джавовые и из javaapi

        // Заголовки для продюсера
        HashMap<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

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

        return kafka().requestReply()
                .producerSettings(producerProps)
                .consumeSettings(consumerProps).timeout(Duration.ofSeconds(10));
    }
}
