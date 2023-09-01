package servicename.configurations.kafka;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import java.time.Duration;
import java.util.Map;
import static ru.tinkoff.gatling.kafka.javaapi.KafkaDsl.kafka;
import static ru.tinkoff.gatling.javaapi.SimulationConfig.getStringParam;

/**
 * <h2>Класс настроек подключения к Kafka</h2>
 * <p>
 *     Параметры подключения к брокеру берутся из файла настроек <b>resources/simulation.conf</b>
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
     * @return объект с типом ProtocolBuilder
     * @author Roman Kislyy
     * @since 2023-08-31
     */
    public ru.tinkoff.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew protocol(){
        return kafka().requestReply()
                .producerSettings(
                        // Про ProducerConfig https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html
                        // Eдесь возникает ошибка java: incompatible types: no instance(s) of type variable(s) K,V exist so that java.util.Map<K,V> conforms to scala.collection.immutable.Map<java.lang.String,java.lang.Object>
                        // То проверьте, что все импорты джавовые и из javaapi
                        Map.of(
                            ProducerConfig.ACKS_CONFIG, "1",
                            // Указываем kafka brokers
                            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                            // Указываем тип сериализации ключа и сообщения
                            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName(),
                            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()
                        )
                )
                .consumeSettings(
                        Map.of(
                                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap
                        )
                ).timeout(Duration.ofSeconds(10));
    }
}
