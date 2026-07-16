package com.example.bot.bots;

import java.util.ArrayList;


import com.example.bot.Constant;
import com.example.bot.UsersStatus;
import com.example.bot.entity.Users;
import com.example.bot.repositories.UsersRepository;
import game.Blackjack;
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
import java.util.Locale;
import java.util.Optional;

@Component
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient = new OkHttpTelegramClient(Constant.BOTTOKEN);
    private final TelegramClient adminClient = new OkHttpTelegramClient(Constant.ADMINBOTTOKEN);


    @Autowired
    private UsersRepository usersRepository;

    @Override
    public void consume(Update update) {
        Long chatId = update.getMessage().getChatId();
        Optional<Users> user = usersRepository.findByChatId(chatId);

        if(update.getMessage().hasText()){
            if(user.isPresent() && user.get().getStatus() !=null  && user.get().getStatus().equals("WAIT_MESSAGE")){
                sendMessageInAdmin(update.getMessage().getChatId() +" "+update.getMessage().getText(), Constant.ADMINCHATID);
                usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND), chatId);
                sendMessage("Сообщение успешно доставлено администрации", chatId);
            }

            if(user.isPresent() && user.get().getStatus() !=null  && user.get().getStatus().equals("WAIT_BET")){
                try{
                    Double bet = Double.parseDouble(update.getMessage().getText());
                    if(usersRepository.findBalanceByChatId(chatId) < bet){
                        sendMessage("Ставка не может быть больше вашего баланса", chatId);
                    }
                    usersRepository.setBet(bet, chatId);
                    user.get().setBet(bet);
                    sendMessage("Ставка успешно поставлена", chatId);

                    Blackjack bj = new Blackjack(user.get());
                    bj.startGame();

                    usersRepository.save(user.get());
                    return;
                } catch (NumberFormatException e){
                    sendMessage("Введите целочисленное число", chatId);
                    return;
                }
            }

            if(user.isPresent() && user.get().getStatus() !=null  && user.get().getStatus().equals("GAME_CHOICE1")){
                try{
                    Integer choice = Integer.parseInt(update.getMessage().getText());

                    if(!(choice == 1 || choice == 2 || choice == 3 || choice == 4)){
                        sendMessage("Введите число от 1 до 4 включительно", chatId);
                        return;
                    }
                    Blackjack bj = new Blackjack(user.get());
                    bj.choice(choice);

                    usersRepository.save(user.get());

                    return;
                } catch (NumberFormatException e){
                    sendMessage("Введите целочисленное число", chatId);
                    return;
                }
            }

            if(user.isPresent() && user.get().getStatus() !=null  && (user.get().getStatus().equals("GAME_REPEAT_CHOICE1")||
                    user.get().getStatus().equals("GAME_REPEAT_CHOICE21") || user.get().getStatus().equals("GAME_REPEAT_CHOICE22"))){
                String message = update.getMessage().getText();


                if(message.equals("да") || message.equals("Да") || message.equals("нет") || message.equals("Нет")){
                    Blackjack bj = new Blackjack(user.get());

                    bj.repeatChoice(message);
                    usersRepository.save(user.get());
                } else{
                    sendMessage("Введите Да или нет", chatId);
                }
                return;
            }

            if(user.isPresent() && user.get().getStatus() !=null  && (user.get().getStatus().equals("GAME_CHOICE21")||
                    user.get().getStatus().equals("GAME_CHOICE22"))){
                try{
                    Integer choice = Integer.parseInt(update.getMessage().getText());

                    if(!(choice == 1 || choice == 2 || choice == 3)){
                        sendMessage("Введите число от 1 до 3 включительно", chatId);
                        return;
                    }
                    Blackjack bj = new Blackjack(user.get());

                    bj.choiceInDoubleGame(choice);

                    usersRepository.save(user.get());

                    return;
                } catch (NumberFormatException e){
                    sendMessage("Введите целочисленное число", chatId);
                    return;
                }
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

                //sendMessage(command, chatId);
                switch (command){
                    case "start" :
                       if(user.isEmpty()){
                            Users newUser = new Users(chatId);
                            usersRepository.save(newUser);
                            sendMessage("Вы успешно зарегистрировались", chatId);
                       } else{
                           sendMessage("Вы уже зарегистрированы", chatId);
                       }
                       break;

                    case "balance":
                        Double balance = usersRepository.findBalanceByChatId(chatId);
                        if(user.isEmpty()){
                            sendMessage("Напишите /start для работы с ботом", chatId);
                            break;
                        }

                        sendMessage("Ваш баланс: " + balance, chatId);
                        break;
                    case "help":
                        if(user.isEmpty()){
                            sendMessage("Напишите /start для работы с ботом", chatId);
                            break;
                        }
                        usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_MESSAGE), chatId);
                        sendMessage("Отправьте сообщение администратору", chatId);
                        break;

                    case "rules":
                        if (user.isEmpty()) {
                            sendMessage("Напишите /start для работы с ботом", chatId);
                            break;
                        }

                        sendMessage("""
                            Правила игры
                            
                             Цель игры
                            Набрать сумму очков, максимально близкую к 21, не превышая её, и набрать больше очков, чем дилер.
                            
                             Стоимость карт
                            • Карты от 2 до 10 — по своему номиналу.
                            • Валет, Дама и Король — по 10 очков.
                            • Туз — 11 очков, а при переборе автоматически считается за 1.
                            
                             Начало игры
                            • Перед началом партии игрок делает ставку.
                            • Игрок получает две карты.
                            • Дилер получает одну открытую карту.
                            
                             Блэкджек
                            Если первые две карты дают 21 очко, игрок получает Блэкджек.
                            При отсутствии Блэкджека у дилера выплата составляет 3:2.
                            
                             Страховка
                            Если первая карта дилера — туз, можно купить страховку стоимостью половины ставки.
                            
                             Доступные действия
                            • Взять карту.
                            • Остановиться.
                            • Удвоить ставку.
                            • Разделить карты (Split), если первые две карты одинакового достоинства или имеют стоимость 10 очков.
                            
                             Ход дилера
                            После окончания хода игрока дилер открывает вторую карту и добирает карты, пока сумма его очков меньше 17.
                            
                             Результат игры
                            • Победа — игрок набрал больше очков или дилер перебрал.
                            • Ничья — одинаковое количество очков.
                            • Поражение — игрок перебрал или набрал меньше очков, чем дилер.
                            
                             Выплаты
                            • Блэкджек — 3:2.
                            • Обычная победа — 1:1.
                            • При ничьей ставка возвращается.
                            """, chatId);
                        break;

                    case "game":
                        if(user.isEmpty()){
                            sendMessage("Напишите /start для работы с ботом", chatId);
                            break;
                        }

                        if(user.get().getBalance() > 0){
                            sendMessage("Игра успешно началась", chatId) ;
                            sendMessage("Введите вашу ставку: ", chatId);
                            usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_BET), chatId);
                        }
                }


            }
            //sendMessage(String.valueOf(chatId), chatId);
        } else{
            sendMessage("Данный тип данных не обрабатывается", chatId);
        }
    }

    public void sendMessage(String massage, Long chatId){
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

    public void sendMessageInAdmin(String massage, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), massage);
        try {
            adminClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
