package linuxtips;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lex.CfnBotAlias;
import software.amazon.awscdk.services.lex.CfnBotAliasProps;
import software.amazon.awscdk.services.lex.CfnBotVersion;
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
        //--
        var botArn = bot.getAttrArn();
        var botId = bot.getAttrId();
        var botIdOut = CfnOutput.Builder.create(this, "simple-bot-id")
                .value(botId)
                .build();
        var botArnOut = CfnOutput.Builder.create(this, "simple-bot-arn")
                .value(botArn)
                .build();
        //
        var botAliasName = "simple-bot-alias";
        //
        /*
        var botVersion = CfnBotVersion.Builder.create(this, "simple-bot-version")
                .botId(botId)
                .build();
        var botVersionStr = botVersion.getAttrBotVersion();
        */
        //
        var lambdaArn = "arn:aws:lambda:us-west-2:192912639870:function:simple-bot-fn-SimpleBotFn-ZS06C7Vi24ef";
        var lambdaHook = CfnBot.LambdaCodeHookProperty.builder()
                .lambdaArn(lambdaArn)
                .codeHookInterfaceVersion("1.0")
                .build();
        var hook = CfnBot.CodeHookSpecificationProperty.builder()
                .lambdaCodeHook(lambdaHook)
                .build();
        //
        var aliasLocaleSetting = CfnBot.BotAliasLocaleSettingsProperty.builder()
                .enabled(true)
                .codeHookSpecification(hook)
                .build();
        var aliasLocaleSettingItem = CfnBot.BotAliasLocaleSettingsItemProperty
                        .builder()
                        .botAliasLocaleSetting(aliasLocaleSetting)
                        .localeId("pt_BR")
                        .build();
        var aliasLocaleSettings = List.of(aliasLocaleSetting);
        //
        var aliasProp = CfnBotAliasProps.builder()
                .botAliasLocaleSettings(aliasLocaleSettings)
                .botAliasName(botAliasName)
                .botId(botId)
                .build();
        var aliasProps = List.of(aliasProp);
        //
        var botAlias = CfnBotAlias.Builder.create(this, "simple-bot-alias")
                .botAliasName(botAliasName)
                .botId(botId)
                //.botVersion(botVersionStr)
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
