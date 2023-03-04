package linuxtips;

import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lex.CfnBot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleBotStack extends Stack {
    @SuppressWarnings("unused")
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
        var roleArn = createRole();
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

    private String createRole() {
        var botPrincipal = ServicePrincipal.Builder.create("lexv2.amazonaws.com").build();
        var botPolicyArn = "arn:aws:iam::aws:policy/AmazonLexRunBotsOnly";
        var botPolicy = ManagedPolicy.fromManagedPolicyArn(this, "simple-bot-policy", botPolicyArn);
        var botPolicies = List.of(botPolicy);

        var role = Role.Builder.create(this, "simple-bot-role")
                .assumedBy(botPrincipal)
                .managedPolicies(botPolicies)
                .build();
        var roleArn = role.getRoleArn();
        return roleArn;
    }

    private CfnBot.BotLocaleProperty createLocaleBR() {
        var fallback = createIntent("FallbackIntent",
                "Fallback if not understood",
                "AMAZON.FallbackIntent");
        var hello = createIntent("HelloIntent",
                "Say hello to the bot",
                null,
                "Oi", "Salve", "Ola", "Bao");
        var intents = List.of(hello, fallback);
        var locale = CfnBot.BotLocaleProperty.builder()
                .localeId("pt_BR")
                .nluConfidenceThreshold(0.5)
                .intents(intents)
                .build();
        return locale;
    }

    private CfnBot.IntentProperty createIntent(String name,
                                               String description,
                                               String parentIntentSignature,
                                               String... utts) {
        var uttsList = Arrays
                .stream(utts) 
                .map(this::createUtterance)
                .collect(Collectors.toList());
        var intent = CfnBot.IntentProperty.builder()
                .name(name)
                .description(description)
                .parentIntentSignature(parentIntentSignature);
        if (! uttsList.isEmpty()){
            intent = intent.sampleUtterances(uttsList);
        }
        return intent.build();
    }

    private CfnBot.SampleUtteranceProperty createUtterance(String utt) {
        return CfnBot.SampleUtteranceProperty.builder()
                .utterance(utt)
                .build();
    }
}
