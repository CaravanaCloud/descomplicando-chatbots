package linuxtips.bot;

import software.amazon.awscdk.services.lex.CfnBot;

import java.util.List;

public record Bot(String id,
                  String name,
                  boolean childDirected,
                  Long idleTimeout,
                  List<BotLocale> locales) {

}
