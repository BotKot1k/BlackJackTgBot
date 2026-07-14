package com.example.bot.bots;

import java.util.ArrayList;


import com.example.bot.Constant;
import com.example.bot.UsersStatus;
import com.example.bot.entity.Users;
import com.example.bot.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

@Component
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient = new OkHttpTelegramClient(Constant.BOTTOKEN);

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public void consume(Update update) {
        Long chatId = update.getMessage().getChatId();
        Optional<Users> user = usersRepository.findByChatId(chatId);

        if(update.getMessage().hasText()){
            if(user.isPresent() && user.get().getStatus() !=null  && user.get().getStatus().equals("WAIT_MESSAGE")){

            }

            String massage = update.getMessage().getText();

            if(massage.charAt(0) == '/'){
                String command;
                int spaceIndex = massage.indexOf(' ');
                if(spaceIndex != -1){
                    command = massage.substring(1, spaceIndex);
                } else{
                    command = massage.substring(1);
                }

                //sendMassage(command, chatId);
                switch (command){
                    case "start" :
                       if(user.isEmpty()){
                            Users newUser = new Users(chatId);
                            usersRepository.save(newUser);
                            sendMassage("Вы успешно зарегистрировались", chatId);
                       } else{
                           sendMassage("Вы уже зарегистрированы", chatId);
                       }
                       break;

                    case "balance":
                        Integer balance = usersRepository.findBalanceByChatId(chatId);
                        if(user.isEmpty()){
                            sendMassage("Напишите /start для работы с ботом", chatId);
                            break;
                        }

                        sendMassage("Ваш баланс: " + String.valueOf(balance), chatId);
                        break;
                    case "help":
                        if(user.isEmpty()){
                            sendMassage("Напишите /start для работы с ботом", chatId);
                            break;
                        }
                        usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_MESSAGE), chatId);
                        sendMassage("Отправьте сообщение администратору", chatId);
                }


            }
            //sendMassage(String.valueOf(chatId), chatId);
        } else{
            sendMassage("Данный тип данных не обрабатывается", chatId);
        }
    }

    public void sendMassage(String massage, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), massage);
        try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    }

    public void sendCustomKeyboard(String chatId) {
        // 1. Prepare the rows and buttons
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Create the first row
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Да");
        row1.add("Нет");

        keyboard.add(row1);

        // 2. Build the ReplyKeyboardMarkup
        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)           // The constructor/builder now requires the list
                .resizeKeyboard(true)         // Recommended: makes buttons fit the screen
                .oneTimeKeyboard(true)       // Keeps the keyboard visible after use
                .selective(false)
                .build();

        // 3. Build the SendMessage with the markup
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("123")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
