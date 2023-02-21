package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lex.CfnBot;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.Queue;

public class SimpleBotStack extends Stack {
    public SimpleBotStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SimpleBotStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        final Queue queue = Queue.Builder.create(this, "SimpleBotQueueLinuxTips")
                 .visibilityTimeout(Duration.seconds(333))
                 .build();
        
        final CfnBot bot = CfnBot.Builder.create(this, "SimpleBot")
                
                .build();
    }
}
