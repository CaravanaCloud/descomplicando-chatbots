import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.LexEvent;
import com.amazonaws.services.lexruntimev2.AmazonLexRuntimeV2;
import com.amazonaws.services.lexruntimev2.AmazonLexRuntimeV2ClientBuilder;
import com.amazonaws.services.lexruntimev2.model.DialogAction;
import com.amazonaws.services.lexruntimev2.model.DialogActionType;
import com.amazonaws.services.lexruntimev2.model.FulfillmentState;
import com.amazonaws.services.lexruntimev2.model.Message;

public class TestLambda implements RequestHandler<LexEvent, Object> {
  
  public Object handleRequest(LexEvent input, Context context) {
    System.out.println("Incoming event properties:");
    System.out.println("InputTranscript: " + input.getInputTranscript());
    System.out.println("InvocationSource: " + input.getInvocationSource());
    System.out.println("Bot: " + input.getBot());
    System.out.println("CurrentIntent: " + input.getCurrentIntent());
    System.out.println("UserId: " + input.getUserId());
    System.out.println("SessionAttributes: " + input.getSessionAttributes());
    System.out.println("RequestAttributes: " + input.getRequestAttributes());

    // Construct the dialog action
    var message = new Message().withContentType("PlainText").withContent("Salveeee!!!");
    var dialogAction = new DialogAction()
            .withType(DialogActionType.Close)
            .withFulfillmentState(FulfillmentState.Fulfilled)
            .withMessage(message);

    // Return the dialog action as the response to Amazon Lex
    return dialogAction;
  }
}
