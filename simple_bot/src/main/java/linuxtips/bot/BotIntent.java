package linuxtips.bot;

import java.util.List;

public record BotIntent(String name,
                        List<BotUtterance> utterances) {
}
