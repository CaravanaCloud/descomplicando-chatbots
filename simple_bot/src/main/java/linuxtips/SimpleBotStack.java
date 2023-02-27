package linuxtips;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.CfnPermission;
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
        
        var botVersionLocaleDetailsBR = CfnBotVersion.BotVersionLocaleDetailsProperty.builder()
                .sourceBotVersion("DRAFT")
                .build();

        var versionLocaleSpecBR = CfnBotVersion.BotVersionLocaleSpecificationProperty.builder()
                .localeId("pt_BR")
                .botVersionLocaleDetails(botVersionLocaleDetailsBR)
                .build();

        var versionLocaleSpecs = List.of(versionLocaleSpecBR);
        var botVersion = CfnBotVersion.Builder.create(this, "simple-bot-version")
                .botId(botId)
                .botVersionLocaleSpecification(versionLocaleSpecs)
                .build();

        var botVersionStr = botVersion.getAttrBotVersion();
        
        //
        var lambdaStackName = "simple-bot-fn";
        var lambdaArn = Fn.importValue(String.format("%s-SimpleBotFnArn",lambdaStackName));
        var lambdaName = Fn.importValue(String.format("%s-SimpleBotFnName",lambdaStackName));

        //
        /* 
        var lambdaHook = CfnBot.LambdaCodeHookProperty.builder()
                .lambdaArn(lambdaArn)
                .codeHookInterfaceVersion("1.0")
                .build();
        
        var hook = CfnBot.CodeHookSpecificationProperty.builder()
                .lambdaCodeHook(lambdaHook)
                .build();

        */
        var aliasLambda = CfnBotAlias.LambdaCodeHookProperty.builder()
                .lambdaArn(lambdaArn)
                .codeHookInterfaceVersion("1.0")
                .build();
        var aliasHook = CfnBotAlias.CodeHookSpecificationProperty.builder()
                .lambdaCodeHook(aliasLambda)
                .build();
        var aliasLocaleSetting = CfnBotAlias.BotAliasLocaleSettingsProperty.builder()
                .codeHookSpecification(aliasHook)
                .enabled(true)
                .build();
        var aliasLocaleSettingItem = CfnBotAlias.BotAliasLocaleSettingsItemProperty
                        .builder()
                        .localeId("pt_BR")
                        .botAliasLocaleSetting(aliasLocaleSetting)
                        .build();
        var aliasLocaleSettings = List.of(aliasLocaleSettingItem);
        /*
        var aliasProp = CfnBotAliasProps.builder()
                .botAliasLocaleSettings(aliasLocaleSettings)
                .botAliasName(botAliasName)
                .botId(botId)
                .build();
        var aliasProps = List.of(aliasProp);
         */
        var botAlias = CfnBotAlias.Builder.create(this, "simple-bot-alias")
                .botAliasName(botAliasName)
                .botId(botId)
                .botVersion(botVersionStr)
                .botAliasLocaleSettings(aliasLocaleSettings)
                .build();
        //
        var botAliasArn = botAlias.getAttrArn();
        var lambdaPerm = CfnPermission.Builder.create(this, "simple-bot-perm-alias")
                .functionName(lambdaName)
                .action("lambda:InvokeFunction")
                .principal("lexv2.amazonaws.com")
                .sourceArn(botAliasArn)
                .build();
        System.out.println(botAliasArn);
        var botAliasArnOut = CfnOutput.Builder.create(this, "simple-bot-alias-arn")
                .value(botAliasArn)
                .build();
        var accountId = Fn.ref("AWS::AccountId");
        var region = Fn.ref("AWS::Region");
        var testBotAliasArn = String.format("arn:aws:lex:%s:%s:bot-alias/%s/%s	", 
         region, accountId, botId, "TSALIASID");
        var testBotAliasArnOut = CfnOutput.Builder.create(this, "simple-bot-test-alias-arn")
         .value(testBotAliasArn)
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
                "AMAZON.FallbackIntent",
                false);
        var hello = createIntent("HelloIntent",
                "Say hello to the bot",
                null,
                true,
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
                                               boolean codeHookEnabled,
                                               String... utts) {
        var uttsList = Arrays
                .stream(utts)
                .map(this::createUtterance)
                .collect(Collectors.toList());
        var codeHook = CfnBot.FulfillmentCodeHookSettingProperty
                .builder()
                .enabled(codeHookEnabled)
                .build();
        var intent = CfnBot.IntentProperty.builder()
                .name(name)
                .description(description)
                .fulfillmentCodeHook(codeHook)
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
