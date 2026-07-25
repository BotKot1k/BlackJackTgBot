package com.example.bot.bots;

import com.example.bot.Constant;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class BotInteraction {
    private TelegramClient gameClient = new OkHttpTelegramClient(Constant.BOTTOKEN);
    private TelegramClient adminClient = new OkHttpTelegramClient(Constant.ADMINBOTTOKEN);

    public void sendMessageInGameClient(String message, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageInAdminClient(String message){
        SendMessage sendMessage = new SendMessage(String.valueOf(Constant.ADMINCHATID), message);
        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
