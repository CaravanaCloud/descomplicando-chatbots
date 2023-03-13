package linuxtips.bot.fn;

import javax.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;
import java.util.Map;

@Named("SimpleBotFn")
@SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
public class SimpleBotFn implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        var log = context.getLogger();
        log.log("--- input");
        log.log(input.toString());
        var session = (Map<String, Object>) input.get("sessionState");
        var intent = (Map<String, String>) session.get("intent");
        var intentState = intent.get("state");
        Map<String, Object> result;
        if (intentState.equals("ReadyForFulfillment")) {
            result = fulfill(session);
        } else {
            result = retry(session);
        }
        log.log("-- output");
        log.log(result.toString());
        return result;
    }

    private Map<String, Object> retry(Map<String, Object> session) {
        return closeIntent(session, "Não entendi, tente novamente...");
    }

    private Map<String, Object> fulfill(Map<String, Object> session) {
        switch (intentName(session)) {
            case "HelloIntent":
                return closeIntent(session, "Salve mundo!!!");
            default:
                return retry(session);
        }
    }

    private String intentName(Map<String, Object> session) {
        var intent = (Map<String,String>) session.get("intent");
        var intentName = intent.get("name");
        return  intentName;
    }

    private Map<String, Object> closeIntent(Map<String, Object> session, String content) {
        var action = "Close";
        var state = "Fulfilled";
        return action(session, action, state, content);
    }

    private Map<String, Object> action(Map<String, Object> session,
                                       String action,
                                       String intentState,
                                       String content) {
        var dialogAction= Map.of(
                "type", action
        );
        var intent = Map.of(
                "name", intentName(session),
                "state", intentState
        );
        var sessionState= Map.of(
                "dialogAction", dialogAction,
                "intent", intent
        );
        var message= Map.of(
                "contentType", "PlainText",
                "content", content
        );
        var messages = List.of(message);
        var result = Map.of(
                "sessionState", sessionState,
                "messages", messages
        );
        return result;
    }
}
