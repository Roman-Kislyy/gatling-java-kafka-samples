package servicename.actions;

import helpers.VarsHelper;
import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.core.ChainBuilder;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import static io.gatling.javaapi.core.CoreDsl.*;
import static ru.tinkoff.gatling.kafka.javaapi.KafkaDsl.kafka;

/**
 * <h2>Только для примера. Класс создания заказов.</h2>
 * <p>
 *     Реализует создание разных вариантов клиентских заказов (покупок).
 * <p>
 *     Приводится в качестве примеры структуры организации тестов.
 * <p>
 * @author  Roman Kislyy
 * @since 2023-08-11
 */
public class CreateOrder {
    /**
     * <p>Запрсос на покупку красных носков</p>
     * <p> Тело JSON запроса берем в ресурсах
     * @return объект с типом ChainBuilder
     * @author  Roman Kislyy
     * @since 2023-08-11
     */
    public ChainBuilder buyRedSocks(int count){
        Headers headers = new RecordHeaders().add("test-header", "test_value".getBytes());
        ChainBuilder chain =
              exec(VarsHelper.set("count", count))  // Здесь мы создаем переменную #{count} в session, чтобы потом ее использоваться в шаблоне json запроса
             .exec(
                    kafka("Send order (red socks)").requestReply()
                    .requestTopic("gatling_order_rq")
                    .replyTopic("gatling_order_rq") // Указан одинаковый топик для запросов и ответов только для демо. Потому что нам никто не отвечает
                            //.replyTopic("gatling_order_rs")
                    .send("key",                                                        // Ключ сообщения
                            ElFileBody("json/CreateOrder/requests/buyRedSocks.json").toString(),// Тело сообщения
                            headers,                                                        // Заголовки
                            String.class, String.class)                            // Типы данных для ключа и тела
                    .check(jsonPath("$.success").exists())
            );
        return chain;
    }
    /**
     * <p>Запрсос на покупку черных шляп</p>
     * <p> TODO
     * @return объект с типом ChainBuilder
     * @author  Roman Kislyy
     * @since 2023-08-11
     */
    public ChainBuilder buyBlackHat(int count){
        ChainBuilder chain = exec(/*You code here*/);
        return chain;
    }
}
