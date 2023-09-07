package servicename.actions;

import helpers.VarsHelper;
import io.gatling.javaapi.core.ChainBuilder;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static ru.tinkoff.gatling.javaapi.Feeders.RandomUUIDFeeder;
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
    // Logger не требуется в классах, в которых вы ничего не хотите вывести в консоль
    private final Logger log = LoggerFactory.getLogger(CreateOrder.class);
    /**
     * <p>Запрсос на покупку красных носков</p>
     * <p> Тело JSON запроса берем в ресурсах
     * @return объект с типом ChainBuilder
     * @author  Roman Kislyy
     * @since 2023-08-11
     */
    public ChainBuilder buyRedSocks(int count){

        // Создаем feeder с именем 'orderId' random uuid.
        // orderId нам потребуется для создания уникальных ключей для сообщений kafka
        // Мы не можем использовать UUID.randomUUID().toString() в тестах, потому что, значение будет одно и то же, сгенерируется один раз при старте теста.
        Iterator<Map<String, Object>> orderIdFeeder = RandomUUIDFeeder("orderId");

        Headers headers = new RecordHeaders().add("test-header", "test_value".getBytes());
        ChainBuilder chain =
              // Пример. Здесь мы создаем переменную #{count} в session, чтобы потом ее использоваться в шаблоне json запроса
              exec(VarsHelper.set("count", count))
             .feed(orderIdFeeder)
             .exec(
                    kafka("Send order (red socks)").requestReply()
                    .requestTopic("gatling_order_rq")
                    .replyTopic("gatling_order_rq") // Указан одинаковый топик для запросов и ответов только для демо. Потому что нам никто не отвечает
                            //.replyTopic("gatling_order_rs")
                    .send(
                            // Ключ сообщения, его значение должно быть уникальным в противном случае, могут быть коллизии, и маппинг ответов и запросов работать не будет
                            "#{orderId}",
                                    // Тело сообщения
                                    "{\n" +
                                    "  \"order_id\": \"#{orderId}\",\n" +
                                    "  \"client_id\": \"#{randomUuid()}\",\n" +
                                    "  \"item_name\": \"Red Socks\",\n" +
                                    "  \"quantity\": #{count},\n" +
                                    "  \"color\": \"Red\",\n" +
                                    "  \"total_price\": 20.00,\n" +
                                    "  \"success\": true\n" +
                                    "}",
                            // Заголовки и типы данных для ключа и тела
                            headers, String.class, String.class)
                    // Выполняем проверку ответа
                    .check(
                            // Проверим, что в ответе есть поле success
                            jsonPath("$.success").exists(),
                            // Проверим, что вернулось true. Для этого подойдет is()
                            jsonPath("$.success").is("true"),
                            // Проверим, что в поле order_id вернулось значение из запроса. Для этого используем isEL(), потому что значение #{orderId} динамическое
                            jsonPath("$.order_id").isEL("#{orderId}"),
                            // Сохраним в сессию переменную ResponseOrderId со значением, которое пришло в ответе. Данную переменную можно будет использовать дальше в сценарие
                            jsonPath("$.order_id").saveAs("responseOrderId")
                            // Пример, если хотим сохранить все тело ответа
                            //,bodyString().saveAs("responseBody")
                    )
            )
//            .exec(session -> {
//                    // Пример логирования. Может быть необходим для отладки, но необходимо избегать в тестах
//                    log.warn("Request id = {}, Response id = {}", session.get("orderId"), session.get("responseOrderId"));
//                    return session;
//                 }
//            )
            ;
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
        ChainBuilder chain = exec(
                /*You code here*/
        );
        return chain;
    }
}
