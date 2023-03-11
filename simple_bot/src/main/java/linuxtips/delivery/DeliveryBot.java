package linuxtips.delivery;

import linuxtips.bot.Bot;
import linuxtips.bot.BotIntent;
import linuxtips.bot.BotLocale;

import java.util.List;
import java.util.Map;

public class DeliveryBot {
    public static Bot of() {
        var fallback = new BotIntent("FallbackIntent",
                "Fallback if not understood",
                "AMAZON.FallbackIntent",
                false,
                List.of());
        var hello = new BotIntent("HelloIntent",
                "Say hello to the bot",
                null,
                true,
                List.of("Oi", "Salve", "Ola", "Bao"));
        var intents = Map.of(
                "FallbackIntent", fallback,
                "HelloIntent" , hello
        );
        var ptBR = new BotLocale(
                "pt_BR",
                0.5,
                intents
        );
        Map<String, BotLocale> locales = Map.of(
                "pt_BR", ptBR
        );
        var deliveryBot = new Bot(
                "simple-bot",
                30 * 60L,
                false,
                locales
        );
        return deliveryBot;
    }
}
