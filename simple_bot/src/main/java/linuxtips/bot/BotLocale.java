package linuxtips.bot;

import java.util.Map;

public record BotLocale(String id,
                        Double nluThreshold,
                        Map<String, BotIntent> intents) {
}
