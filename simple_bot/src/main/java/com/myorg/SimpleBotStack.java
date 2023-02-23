package com.myorg;

import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lex.CfnBot;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

import java.util.List;

public class SimpleBotStack extends Stack {
    public SimpleBotStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SimpleBotStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        var privacy = CfnBot.DataPrivacyProperty.builder()
                .childDirected(false)
                .build();
        var idleTimeout = 60 * 30L;
        var botName = "simple-bot";


        var botPrincipal = ServicePrincipal.Builder.create("lexv2.amazonaws.com").build();
        var botPolicyArn = "arn:aws:iam::aws:policy/AmazonLexRunBotsOnly";
        var botPolicy = ManagedPolicy.fromManagedPolicyArn(this, "simple-bot-policy", botPolicyArn);
        var botPolicies = List.of(botPolicy);

        var role = Role.Builder.create(this, "simple-bot-role")
                .assumedBy(botPrincipal)
                .managedPolicies(botPolicies)
                .build();
        var roleArn = role.getRoleArn();

        var br = createLocaleBR();
        var locales = List.of(br);
        var bot = CfnBot.Builder.create(this, "simple-bot")
                .dataPrivacy(privacy)
                .idleSessionTtlInSeconds(idleTimeout)
                .name(botName)
                .roleArn(roleArn)
                .botLocales(locales)
                .autoBuildBotLocales(true)
                .build();

    }

    private CfnBot.BotLocaleProperty createLocaleBR() {
        var fallback = CfnBot.IntentProperty.builder()
                .name("FallbackIntent")
                .description("Fallback if not understood")
                .parentIntentSignature("AMAZON.FallbackIntent")
                .build();
        var oi = CfnBot.SampleUtteranceProperty.builder()
                .utterance("Oi")
                .build();
        var salve = CfnBot.SampleUtteranceProperty.builder()
                .utterance("salve")
                .build();
        var utts = List.of(oi, salve);
        var hello = CfnBot.IntentProperty.builder()
                .name("HelloIntent")
                .description("Say hello to the bot")
                .sampleUtterances(utts)
                .build();
        var intents = List.of(hello, fallback);
        var locale = CfnBot.BotLocaleProperty.builder()
                .localeId("pt_BR")
                .nluConfidenceThreshold(0.5)
                .intents(intents)
                .build();
        return locale;
    }
}
