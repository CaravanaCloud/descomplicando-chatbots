package linuxtips.bot;

import java.util.Map;

public record Bot(
        String name,
        Long sessionTtl,
        Boolean childDirected,
        Map<String, BotLocale> locales) {
}
