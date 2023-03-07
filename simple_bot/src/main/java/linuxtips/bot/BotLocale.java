package linuxtips.bot;

import java.util.List;

public record BotLocale(String localeId,
                        List<BotIntent> intents) {
}
