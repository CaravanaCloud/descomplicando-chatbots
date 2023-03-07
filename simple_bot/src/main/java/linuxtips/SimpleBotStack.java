package linuxtips;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.CfnPermission;
import software.amazon.awscdk.services.lex.CfnBotAlias;
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

        var botId = bot.getAttrId();
        var botIdOut = CfnOutput.Builder.create(this, "simple-bot-id")
                .value(botId)
                .build();

        var botArn = bot.getAttrArn();
        var botArnOut = CfnOutput.Builder.create(this, "simple-bot-arn")
                .value(botArn)
                .build();

        var versionLocaleDetailsBR = CfnBotVersion.BotVersionLocaleDetailsProperty
                .builder()
                .sourceBotVersion("DRAFT")
                .build();
        var versionLocaleSpecificationBR = CfnBotVersion.BotVersionLocaleSpecificationProperty
                .builder()
                .localeId("pt_BR")
                .botVersionLocaleDetails(versionLocaleDetailsBR)
                .build();
        var versionLocaleSpecifications = List.of(versionLocaleSpecificationBR);
        var botVersion = CfnBotVersion.Builder.create(this, "simple-bot-version")
                .botId(botId)
                .botVersionLocaleSpecification(versionLocaleSpecifications)
                .build();
        var botVersionId = botVersion.getAttrBotVersion();
        var botVersionIdOut = CfnOutput.Builder.create(this, "simple-bot-version-id")
                .value(botVersionId)
                .build();

        var aliasName = "simple-bot-alias";

        var lambdaStackName = "simple-bot-fn";
        var lambdaArnExport = String.format("%s-SimpleBotFnArn", lambdaStackName);
        var lambdaArn = Fn.importValue(lambdaArnExport);
        var lambdaIdExport = String.format("%s-SimpleBotFnId", lambdaStackName);
        var lambdaId = Fn.importValue(lambdaIdExport);

        var lambdaCodeHook = CfnBotAlias.LambdaCodeHookProperty.builder()
                .lambdaArn(lambdaArn)
                .codeHookInterfaceVersion("1.0")
                .build();
        var aliasCodeHook = CfnBotAlias.CodeHookSpecificationProperty
                .builder()
                .lambdaCodeHook(lambdaCodeHook)
                .build();
        var aliasLocaleSetting = CfnBotAlias.BotAliasLocaleSettingsProperty
                .builder()
                .codeHookSpecification(aliasCodeHook)
                .enabled(true)
                .build();
        var aliasLocaleSettingsItem = CfnBotAlias.BotAliasLocaleSettingsItemProperty
                .builder()
                .localeId("pt_BR")
                .botAliasLocaleSetting(aliasLocaleSetting)
                .build();
        var aliasLocaleSettings = List.of(aliasLocaleSettingsItem);
        var botAlias = CfnBotAlias.Builder.create(this, "simple-bot-alias")
                .botId(botId)
                .botVersion(botVersionId)
                .botAliasName(aliasName)
                .botAliasLocaleSettings(aliasLocaleSettings)
                .build();
        var botAliasId = botAlias.getAttrBotAliasId();
        var acctId = Fn.ref("AWS::AccountId");
        var region = Fn.ref("AWS::Region");
        var aliasArnFmt = "arn:aws:lex:%s:%s:bot-alias/%s/%s";
        var aliasArn = String.format(aliasArnFmt,
                region,
                acctId,
                botId,
                botAliasId);

        var lambdaPerm = CfnPermission.Builder.create(this, "simple-bot-alias-perm")
                .functionName(lambdaId)
                .action("lambda:invokeFunction")
                .principal("lexv2.amazonaws.com")
                .sourceArn(aliasArn)
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
