package linuxtips;

import javax.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("simple-bot-lambda")
public class SimpleBotCodeHookLambda implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        var log = context.getLogger();
        log.log("-------");
        input.forEach( (k,v) -> log.log(k + " : " + v) );
        var result = new HashMap<String, Object>();
        var sessionState = Map.of(
                "dialogAction", Map.of(
                        "type", "Close"
                ),
                "intent", Map.of(
                        "name", "HelloIntent",
                        "state", "Fulfilled")
        );
        result.put("sessionState", sessionState);
        var message = Map.of(
                "contentType", "PlainText",
                "content", "Salve mundao!"
        );
        var messages = List.of(message);
        result.put("messages", messages);
        result.forEach( (k,v) -> log.log(k + " : " + v) );
        return result;
    }
}
