package com.example.bot.bots;

import com.example.bot.Constant;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotInitializer {

    private TelegramClient telegramClient = new OkHttpTelegramClient(Constant.BOTTOKEN);

    private final TelegramBot telegramBot;
    private final AdminTelegramBot adminTelegramBot;

    public BotInitializer(TelegramBot telegramBot,
                          AdminTelegramBot adminTelegramBot) {
        this.telegramBot = telegramBot;
        this.adminTelegramBot = adminTelegramBot;
    }

    @PostConstruct
    public void init() throws TelegramApiException {
        TelegramBotsLongPollingApplication app =
                new TelegramBotsLongPollingApplication();

        app.registerBot(Constant.BOTTOKEN, telegramBot);
        app.registerBot(Constant.ADMINBOTTOKEN, adminTelegramBot);
    }

    @PostConstruct
    public void registerCommands() {

        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("start", "Запустить бота"));
        commands.add(new BotCommand("balance", "Показать баланс"));
        commands.add(new BotCommand("help", "Связаться с администрацией"));

        try {
            SetMyCommands setMyCommands = SetMyCommands.builder()
                    .commands(commands)
                    .scope(new BotCommandScopeDefault())
                    .build();
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
        }
    }
}