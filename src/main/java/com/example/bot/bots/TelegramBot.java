package com.example.bot.bots;

import com.example.bot.UsersStatus;
import com.example.bot.entity.Users;
import com.example.bot.repositories.UsersRepository;
import com.example.bot.game.Blackjack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.Optional;

@Component
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {



    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BotInteraction botInteraction;
    @Override
    public void consume(Update update) {
        if(update.getMessage() == null){ return;}

        Long chatId = update.getMessage().getChatId();
        Optional<Users> user = usersRepository.findByChatId(chatId);

        String username = update.getMessage().getFrom().getUserName();
        if(username != null) System.out.println("Сейчас бот использует: @" + username);

        if(update.getMessage().hasText()){
            if(user.isPresent() && user.get().getStatus().equals(UsersStatus.WAIT_MESSAGE.toString())){
                botInteraction.sendMessageInAdminClient(update.getMessage().getChatId() +" "+update.getMessage().getText());
                usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND), chatId);
                botInteraction.sendMessageInGameClient("Сообщение успешно доставлено администрации", chatId);
                checkStatus(user.get());
            }

            if(user.isPresent() && user.get().getStatus().equals(UsersStatus.WAIT_BET.toString())){
                try{
                    Double bet = Double.parseDouble(update.getMessage().getText());
                    if(usersRepository.findBalanceByChatId(chatId) < bet){
                        botInteraction.sendMessageInGameClient("Ставка не может быть больше вашего баланса", chatId);
                        return;
                    }
                    usersRepository.setBet(bet, chatId);
                    user.get().setBet(bet);
                    botInteraction.sendMessageInGameClient("Ставка успешно поставлена", chatId);

                    Blackjack bj = new Blackjack(user.get());
                    bj.startGame();

                    usersRepository.save(user.get());
                    return;
                } catch (NumberFormatException e){
                    botInteraction.sendMessageInGameClient("Введите целочисленное число", chatId);
                    return;
                }
            }

            if(user.isPresent() && user.get().getStatus().equals(UsersStatus.GAME_CHOICE1.toString())){
                try{
                    int choice = Integer.parseInt(update.getMessage().getText());

                    if(!(choice == 1 || choice == 2 || choice == 3 || choice == 4)){
                        botInteraction.sendMessageInGameClient("Введите число от 1 до 4 включительно", chatId);
                        return;
                    }
                    Blackjack bj = new Blackjack(user.get());
                    bj.choice(choice);

                    checkStatus(user.get());
                    usersRepository.save(user.get());

                    return;
                } catch (NumberFormatException e){
                    botInteraction.sendMessageInGameClient("Введите целочисленное число", chatId);
                    return;
                }
            }

            if(user.isPresent() && (user.get().getStatus().equals(UsersStatus.GAME_REPEAT_CHOICE1.toString())
                    || user.get().getStatus().equals(UsersStatus.GAME_REPEAT_CHOICE21.toString())
                    || user.get().getStatus().equals(UsersStatus.GAME_REPEAT_CHOICE22.toString()))){

                String message = update.getMessage().getText();


                if(message.equals("да") || message.equals("Да") || message.equals("нет") || message.equals("Нет")){
                    Blackjack bj = new Blackjack(user.get());

                    bj.repeatChoice(message);
                    checkStatus(user.get());
                    usersRepository.save(user.get());
                } else{
                    botInteraction.sendMessageInGameClient("Введите Да или нет", chatId);
                }
                return;
            }

            if(user.isPresent() && user.get().getStatus().equals(UsersStatus.GAME_WAIT_INSURANCE.toString())){
                String message = update.getMessage().getText();


                if(message.equals("да") || message.equals("Да") || message.equals("нет") || message.equals("Нет")){
                    Blackjack bj = new Blackjack(user.get());

                    bj.insuranceInteraction(message);
                    checkStatus(user.get());
                    usersRepository.save(user.get());
                } else{
                    botInteraction.sendMessageInGameClient("Введите Да или нет", chatId);
                }
            }

            if(user.isPresent() && user.get().getStatus() !=null  && (user.get().getStatus().equals(UsersStatus.GAME_CHOICE21.toString())||
                    user.get().getStatus().equals(UsersStatus.GAME_CHOICE22.toString()))){
                try{
                    int choice = Integer.parseInt(update.getMessage().getText());

                    if(!(choice == 1 || choice == 2 || choice == 3)){
                        botInteraction.sendMessageInGameClient("Введите число от 1 до 3 включительно", chatId);
                        return;
                    }
                    Blackjack bj = new Blackjack(user.get());

                    bj.choiceInDoubleGame(choice);

                    checkStatus(user.get());
                    usersRepository.save(user.get());
                    return;
                } catch (NumberFormatException e){
                    botInteraction.sendMessageInGameClient("Введите целочисленное число", chatId);
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

                commandInteraction(command, chatId);
            }
            //sendMessage(String.valueOf(chatId), chatId);
        } else{
            botInteraction.sendMessageInGameClient("Данный тип данных не обрабатывается", chatId);
        }
    }

    public void checkStatus(Users user){
        if(user.getStatus().equals("WAIT_NEW_COMMAND")){ // Сделать сравнение через enum
            botInteraction.sendMessageInGameClient("""
                    /game - начать игру
                    /balance - проверить баланс
                    /help - связаться с администрацией
                    /rules - ознакомиться с правилами
                    """, user.getChatId());
        }
    }

    public void commandInteraction(String command, Long chatId){
        Optional<Users> user = usersRepository.findByChatId(chatId);
        switch (command){
            case "start" :
                if(user.isEmpty()){
                    Users newUser = new Users(chatId);
                    newUser.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND));
                    usersRepository.save(newUser);
                    botInteraction.sendMessageInGameClient("Вы успешно зарегистрировались", chatId);
                }
                break;

            case "balance":
                Double balance = usersRepository.findBalanceByChatId(chatId);

                if(checkUserRegistration(user, chatId)) break;

                botInteraction.sendMessageInGameClient("Ваш баланс: " + balance, chatId);
                break;
            case "help":
                if(checkUserRegistration(user, chatId)) break;
                // Добавить сохранение прошлого статуса
                usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_MESSAGE), chatId);
                botInteraction.sendMessageInGameClient("Отправьте сообщение администратору", chatId);
                break;

            case "rules":
                if(checkUserRegistration(user, chatId)) break;

                botInteraction.sendMessageInGameClient("""
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
                if(!checkUserRegistration(user, chatId) && user.get().getBalance() > 0 ){
                    botInteraction.sendMessageInGameClient("Игра успешно началась", chatId) ;
                    botInteraction.sendMessageInGameClient("Введите вашу ставку: ", chatId);
                    usersRepository.setStatus(String.valueOf(UsersStatus.WAIT_BET), chatId);
                }
                break;
            case "qweqweqwe":
                if(checkUserRegistration(user, chatId)) break;
                user.get().setBalance(1000.0);
                usersRepository.save(user.get());
        }

        if(!command.equals("game")) user.ifPresent(this::checkStatus);
    }

    public boolean checkUserRegistration(Optional<Users> user,Long chatId){
        if(user.isEmpty()){
            botInteraction.sendMessageInGameClient("Напишите /start для работы с ботом", chatId);
            return true;
        }
        return false;
    }
}
