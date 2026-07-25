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
    @Autowired
    private BotInteraction botInteraction;

    @Override
    public void consume(Update update){
        if(update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String massage = update.getMessage().getText();
            if (!chatId.equals(Constant.ADMINCHATID)) {
                return;
            }
            int spaceIndex = massage.indexOf(' ');
            Long userChatId = Long.valueOf(massage.substring(0, spaceIndex));
            massage = massage.substring(spaceIndex + 1);
            botInteraction.sendMessageInGameClient("Пришло сообщение от администрации: ", userChatId);
            botInteraction.sendMessageInGameClient(massage, userChatId);
            botInteraction.sendMessageInAdminClient("Сообщение успешно отправлено");
        }
    }


}
