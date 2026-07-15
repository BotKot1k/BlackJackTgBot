package com.example.bot.bots;

import com.example.bot.Constant;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TelegramClient gameClient = new OkHttpTelegramClient(Constant.BOTTOKEN);

    @Autowired
    private TelegramBot telegramBot;

    @Override
    public void consume(Update update){
        Long chatId = update.getMessage().getChatId();
        String massage = update.getMessage().getText();
        if(!chatId.equals(Constant.ADMINCHATID)){
            sendMassage("Тебе тут не рады ", chatId);
            return;
        }
        int spaceIndex = massage.indexOf(' ');
        Long userChatId = Long.valueOf(massage.substring(0, spaceIndex));
        massage = massage.substring(spaceIndex +1);
        sendMassageInGeneralBot("Пришло сообщение от администрации: ", userChatId);
        sendMassageInGeneralBot(massage, userChatId);
        sendMassage("Сообщение успешно отправлено", chatId);
    }

    public void sendMassage(String massage, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), massage);
        try {
            adminClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMassageInGeneralBot(String massage, Long chatId) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), massage);
        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
