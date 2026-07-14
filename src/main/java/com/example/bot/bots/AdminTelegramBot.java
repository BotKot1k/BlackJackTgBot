package com.example.bot.bots;

import com.example.bot.Constant;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class AdminTelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient adminClient = new OkHttpTelegramClient(Constant.ADMINBOTTOKEN);

    @Override
    public void consume(Update update){
        sendMassage(String.valueOf(update.getMessage().getChatId()), update.getMessage().getChatId());
    }

    public void sendMassage(String massage, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), massage);
        try {
            adminClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
