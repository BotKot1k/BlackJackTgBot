package com.example.bot.bots;

import com.example.bot.Constant;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotInteraction {
    private TelegramClient gameClient = new OkHttpTelegramClient(Constant.BOTTOKEN);
    private TelegramClient adminClient = new OkHttpTelegramClient(Constant.ADMINBOTTOKEN);

    public void sendMessageInGameClient(String message, Long chatId) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageInAdminClient(String message) {
        SendMessage sendMessage = new SendMessage(String.valueOf(Constant.ADMINCHATID), message);
        try {
            adminClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendCustomKeyboard(String message, KeyboardRow row, Long chatId) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)           // The constructor/builder now requires the list
                .resizeKeyboard(true)         // Recommended: makes buttons fit the screen
                .oneTimeKeyboard(true)       // Keeps the keyboard visible after use
                .selective(false)
                .build();

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
