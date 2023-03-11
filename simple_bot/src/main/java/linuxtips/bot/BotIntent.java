package linuxtips.bot;

import java.util.List;

public record BotIntent(
        String name,
        String description,
        String parentSignature,
        Boolean codeHook,
        List<String> utterances
) {
}
