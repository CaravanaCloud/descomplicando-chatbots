package linuxtips;

import javax.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;
import java.util.Map;

@Named("SimpleBotFn")
public class SimpleBotFn implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        var log = context.getLogger();
        log.log("-- input");
        log.log(input.toString());
        var dialogAction= Map.of(
                "type", "Close"
        );
        var intent = Map.of(
                "name", "HelloIntent",
                "state", "Fulfilled"
        );
        var sessionState= Map.of(
                "dialogAction", dialogAction,
                "intent", intent
        );
        var message= Map.of(
                "contentType", "PlainText",
                "content", "Salve mundo!"
        );
        var messages = List.of(message);
        var result = Map.of(
                "sessionState", sessionState,
                "messages", messages
        );
        log.log("-- output");
        log.log(result.toString());
        return result;
    }
}
