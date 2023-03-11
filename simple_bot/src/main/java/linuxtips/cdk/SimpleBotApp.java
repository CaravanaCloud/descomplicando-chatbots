package linuxtips.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class SimpleBotApp {
    public static void main(final String[] args) {
        App app = new App();
        new SimpleBotStack(app, "SimpleBotStack", StackProps.builder().build());
        app.synth();
    }
}

