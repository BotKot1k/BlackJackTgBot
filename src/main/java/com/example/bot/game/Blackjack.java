package com.example.bot.game;

import com.example.bot.UsersStatus;
import com.example.bot.bots.BotInteraction;
import com.example.bot.entity.Users;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

public class Blackjack {
    private Double balance;
    private Double bet;

    private ArrayList<Card> cards;
    private ArrayList<Card> startPlayerCards = new ArrayList<>();
    private final ArrayList<ArrayList<Card>> playerCards;
    private final ArrayList<Card> dealerCards;

    private boolean firstDealerAce = false;
    //private boolean insurance = false;

    private final Random rd = new Random();
    private final BotInteraction botInteraction = new BotInteraction();

    private final Long chatId;
    private final Users user;


    public Blackjack(Users user){
        this.user = user;
        chatId = user.getChatId();

        if(this.user.getDealerCards() != null){
            this.dealerCards = this.user.getDealerCards();
        } else{
            this.dealerCards = new ArrayList<>();
        }

        if(this.user.getCards() != null){
            this.cards = this.user.getCards();
        } else{
            this.cards = new ArrayList<>();
        }

        if(this.user.getPlayerCards() != null){
            this.playerCards = this.user.getPlayerCards();
        } else{
            this.playerCards = new ArrayList<>();
        }

        this.bet = this.user.getBet();

        this.balance = this.user.getBalance();
    }

    public void startGame(){
        checkCards();

        getSomeCardPlayer(startPlayerCards);
        getSomeCardPlayer(startPlayerCards);
        printAllCards(startPlayerCards, 1);



        getSomeCardDealer();
        printAllCards(dealerCards, 0);

        if(getScore(startPlayerCards) == 21 && !firstDealerAce ){
            blackJack();
            return;
        }
        user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE1));
        playerCards.add(startPlayerCards);


        user.setDealerCards(dealerCards);
        user.setPlayerCards(playerCards);
        user.setCards(cards);

        botInteraction.sendCustomKeyboard("Желаете взять карту (1), удвоить ставку (2), разделить карты (3), ничего не делать (4)",
                new KeyboardRow("1", "2", "3","4") ,chatId);
        // Сделать клаву
    }

    public void endGame(){
        for(int i = 0; i < playerCards.size(); i++){
            ArrayList<Card> currentCards = playerCards.get(i);
            if(getScore(currentCards) > 21){
                lose();
                playerCards.remove(currentCards);
            }
        }

        if(playerCards.isEmpty()){
            resetProperty();
            return;
        }



        while(getScore(dealerCards) < 17){
            getSomeCardDealer();
        }
        printAllCards(dealerCards, 0);

        int count = 1;

        for(ArrayList<Card> currentCards : playerCards){
            botInteraction.sendMessageInGameClient("Игра №"+count +"\nВаш счёт: " + getScore(currentCards), chatId);
            count++;

            if(getScore(dealerCards) > 21){
                win();
                continue;
            }

            if(getScore(currentCards) > getScore(dealerCards)){
                win();
            } else if (getScore(currentCards) < getScore(dealerCards)){
                lose();
            } else{
                botInteraction.sendMessageInGameClient("Ничья", chatId);
            }
        }
        resetProperty();
    }

    public void repeatChoice(String answer){
        switch (user.getStatus()){
            case "GAME_REPEAT_CHOICE1":
                if(answer.equals("да") || answer.equals("Да")){
                    startPlayerCards = playerCards.get(0);
                    playerCards.remove(0);

                    getSomeCardPlayer(startPlayerCards);
                    printAllCards(startPlayerCards, 1);

                    if(getScore(startPlayerCards) > 21) {
                        playerCards.add(startPlayerCards);
                        endGame();
                        return;
                    }

                    botInteraction.sendCustomKeyboard("Желаете ли взять ещё карту? (Да / Нет)",
                            new KeyboardRow("Да", "Нет") ,chatId);

                    playerCards.add(startPlayerCards);
                } else{
                    endGame();
                }
                break;
            case "GAME_REPEAT_CHOICE21":
                if(answer.equals("да")){
                    getSomeCardPlayer(playerCards.get(0));
                    printAllCards(playerCards.get(0), 1);

                    if(getScore(playerCards.get(0)) > 21) {
                        user.setStatus("GAME_CHOICE22");
                        return;
                    }

                    botInteraction.sendCustomKeyboard("Желаете ли взять ещё карту? (Да / Нет)",
                            new KeyboardRow("Да", "Нет") ,chatId);
                } else{
                    user.setStatus("GAME_CHOICE22");
                }
                break;
            case "GAME_REPEAT_CHOICE22":
                if(answer.equals("да")){
                    getSomeCardPlayer(playerCards.get(1));
                    printAllCards(playerCards.get(1), 1);

                    if(getScore(playerCards.get(1)) > 21) {
                        endGame();
                        return;
                    }

                    botInteraction.sendCustomKeyboard("Желаете ли взять ещё карту? (Да / Нет)",
                            new KeyboardRow("Да", "Нет") ,chatId);
                } else{
                    endGame();
                }
                break;
        }
    }

    public void choiceInDoubleGame(int numb){
        int count;
        if(user.getStatus().equals("GAME_CHOICE21")){count = 0;}
        else{count = 1;}

        switch (numb){
            case 3:
                if(user.getStatus().equals("GAME_CHOICE22")) {endGame();}
                else{user.setStatus("GAME_CHOICE22"); doubleGame();}
                return ;
            case 2: // не работает
                botInteraction.sendMessageInGameClient("Функция временно не работает, выберите другую", chatId);
                break;
                /*if(bet * 2 > balance){
                    sendMessage("Внимание! Новая ставка не может быть больше баланса, выберите другую функцию", chatId);
                    return ;
                }
                bet *=2; // Вот корень зла

                System.out.println("Вы взяли карту.");
                getSomeCardPlayer(playerCards.get(count));
                printAllCards(playerCards.get(count), 1);
                sendMessage("     Ваш счёт: " + getScore(playerCards.get(count)), chatId);

                if(count == 1){endGame();}
                else{user.setStatus("GAME_CHOICE22");}
                return ;*/
            case 1:

                getSomeCardPlayer(playerCards.get(count));
                printAllCards(playerCards.get(count), 1);

                if(getScore(playerCards.get(count)) > 21) {
                    if(count == 1) endGame();
                    else{user.setStatus("GAME_CHOICE_22");
                        doubleGame();
                    }
                    return;
                }

                botInteraction.sendCustomKeyboard("Желаете ли взять ещё карту? (Да / Нет)",
                        new KeyboardRow("Да", "Нет") ,chatId);
                if(count == 0){
                    user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE21));
                } else{
                    user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE22));
                }
                break;
        }
    }

    public void choice(int choice){
        startPlayerCards = playerCards.get(0);
        playerCards.remove(0);
        switch (choice){
            case 1:
                getSomeCardPlayer(startPlayerCards);
                printAllCards(startPlayerCards, 1);

                if(getScore(startPlayerCards) > 21) {
                    playerCards.add(startPlayerCards);
                    endGame();
                    return;
                }

                if(getScore(startPlayerCards) == 21){
                    playerCards.add(startPlayerCards);
                    endGame();
                    return;
                }

                botInteraction.sendCustomKeyboard("Желаете ли взять ещё карту? (Да / Нет)",
                        new KeyboardRow("Да", "Нет") ,chatId);
                user.setStatus(String.valueOf(UsersStatus.GAME_REPEAT_CHOICE1));
                playerCards.add(startPlayerCards);
                break;

            case 2:
                if(bet * 2 > balance){
                    botInteraction.sendMessageInGameClient("Недостаточно денег для удвоения ставки, выберите другую функцию", chatId);
                    playerCards.add(startPlayerCards);
                    return;
                }
                bet *=2;
                getSomeCardPlayer(startPlayerCards);
                printAllCards(startPlayerCards, 1);

                playerCards.add(startPlayerCards);
                endGame();
                return;

            case 3:
                if(balance < bet * 2){
                    botInteraction.sendMessageInGameClient("Недостаточно денег для ставки, выберите другую функцию", chatId);
                    playerCards.add(startPlayerCards);
                    return ;
                }
                if (!((getScore(startPlayerCards) % 2 == 0 && Objects.equals(startPlayerCards.get(0).getValues(), startPlayerCards.get(1).getValues()))
                        || (startPlayerCards.get(0).getValues() >=10 && startPlayerCards.get(1).getValues() >=10) && (startPlayerCards.size()==2)) ){
                    botInteraction.sendMessageInGameClient("Не соблюдены условия сплита, выберите другую функцию", chatId);
                    playerCards.add(startPlayerCards);
                    return ;
                }
                ArrayList<Card> newPacks = new ArrayList<>();
                newPacks.add(startPlayerCards.get(0));
                playerCards.add(newPacks);

                newPacks = new ArrayList<>();
                newPacks.add(startPlayerCards.get(1));
                playerCards.add(newPacks);

                doubleGame();
                break;
            case 4:
                playerCards.add(startPlayerCards);
                endGame();
                break;

        }
    }

    private void doubleGame(){
        int count;
        if(user.getStatus().equals("GAME_CHOICE1")) count =0;
        else {count = 1;}

        botInteraction.sendMessageInGameClient("Рука №" +(count+1), chatId);

        ArrayList<Card> currentCards = playerCards.get(count);


        getSomeCardPlayer(currentCards);
        printAllCards(currentCards, 1);

        if(getScore(currentCards) == 21){ // Блэкджек при сплите не считается блэкджеком
            playerCards.add(currentCards);
            return ;
        }

        botInteraction.sendCustomKeyboard("Желаете взять карту (1), удвоить ставку (2), ничего не делать (3)",
                new KeyboardRow("1", "2", "3") ,chatId);

        if(count == 0) {user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE21));}
        else{user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE22));}
    }



    private void blackJack(){
        botInteraction.sendMessageInGameClient("Блэкджек!!! Вы выйграли: " + (1.5 * bet), chatId);
        balance += 1.5 * bet;

        user.setBalance(balance);
        user.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND));
    }

    private void win(){
        botInteraction.sendMessageInGameClient("Вы выйграли: " + (bet), chatId);
        balance += bet;

        user.setBalance(balance);

    }

    private void lose(){
        botInteraction.sendMessageInGameClient("Вы проиграли: " + (bet), chatId);
        balance -= bet;

        user.setBalance(balance);

    }

    private void getSomeCardPlayer(ArrayList<Card> playerCards){
        int numberCard = rd.nextInt(cards.size());
        Card currentCard = cards.get(numberCard);

        /*if(playerCards.size() < 3)*/ playerCards.add(currentCard);
        //printAllCards(playerCards);
        cards.remove(numberCard);
        checkCards();
    }

    private void getSomeCardDealer(){
        int numberCard = rd.nextInt(cards.size());
        Card currentCard = cards.get(numberCard);

        if(dealerCards.isEmpty() && currentCard.getValues() == 14) {firstDealerAce = true;}
        dealerCards.add(currentCard);
        cards.remove(numberCard);
        checkCards();
    }

    private void checkCards(){
        if(cards == null || cards.isEmpty() || cards.size() < 17){
            cards = getNewCards();
        }
    }

    private ArrayList<Card> getNewCards(){
        botInteraction.sendMessageInGameClient("Перетасовка карт", chatId);
        return new ArrayList<>(Arrays.asList(
                new Card(2, "Пики (♠)"), new Card(2, "Черва (♥)"), new Card(2, "Бубна (♦)"), new Card(2, "Трефа (♣)"),
                new Card(3, "Пики (♠)"), new Card(3, "Черва (♥)"), new Card(3, "Бубна (♦)"), new Card(3, "Трефа (♣)"),
                new Card(4, "Пики (♠)"), new Card(4, "Черва (♥)"), new Card(4, "Бубна (♦)"), new Card(4, "Трефа (♣)"),
                new Card(5, "Пики (♠)"), new Card(5, "Черва (♥)"), new Card(5, "Бубна (♦)"), new Card(5, "Трефа (♣)"),
                new Card(6, "Пики (♠)"), new Card(6, "Черва (♥)"), new Card(6, "Бубна (♦)"), new Card(6, "Трефа (♣)"),
                new Card(7, "Пики (♠)"), new Card(7, "Черва (♥)"), new Card(7, "Бубна (♦)"), new Card(7, "Трефа (♣)"),
                new Card(8, "Пики (♠)"), new Card(8, "Черва (♥)"), new Card(8, "Бубна (♦)"), new Card(8, "Трефа (♣)"),
                new Card(9, "Пики (♠)"), new Card(9, "Черва (♥)"), new Card(9, "Бубна (♦)"), new Card(9, "Трефа (♣)"),
                new Card(10, "Пики (♠)"), new Card(10, "Черва (♥)"), new Card(10, "Бубна (♦)"), new Card(10, "Трефа (♣)"),
                new Card(11, "Пики (♠)"), new Card(11, "Черва (♥)"), new Card(11, "Бубна (♦)"), new Card(11, "Трефа (♣)"), // Валет
                new Card(12, "Пики (♠)"), new Card(12, "Черва (♥)"), new Card(12, "Бубна (♦)"), new Card(12, "Трефа (♣)"), // Дамы
                new Card(13, "Пики (♠)"), new Card(13, "Черва (♥)"), new Card(13, "Бубна (♦)"), new Card(13, "Трефа (♣)"), // Король
                new Card(14, "Пики (♠)"), new Card(14, "Черва (♥)"), new Card(14, "Бубна (♦)"), new Card(14, "Трефа (♣)")  // Туз
                // ♠ ♣ ♦ ♥


        ));
    }

    private int getScore( ArrayList<Card> cards){ // Подсчёт счёта по картам, также простая проверка на два туза

        for(int i = 0; i < cards.size(); i++){ // Если туз не последний, то метод неправильно будет считать количество очков
            if(cards.get(i).getValues() == 14){
                Card card = cards.remove(i);
                cards.add(card);
            }
        }

        int sum = 0;
        for(Card card : cards){
            if(card.getValues() <= 10){
                sum += card.getValues();
            } else if(card.getValues() <= 13){
                sum += 10;
            } else{
                if(sum + 11 > 21) sum +=1;
                else sum+= 11;
            }
        }

        return sum;
    }


    private void printAllCards(ArrayList<Card> card, int n){ // 1 if player; 0 if dealer
        StringBuilder message = new StringBuilder();
        if(n == 1) message.append("Ваши карты: \n");
        else message.append("Карты дилера: \n");

        for(Card currentCard : card){
            if (currentCard.getValues() > 10) {
                switch (currentCard.getValues()) {
                    case 11:
                        message.append("     Валет ");

                        break;
                    case 12:
                        message.append("     Дама ");

                        break;
                    case 13:
                        message.append("     Король ");

                        break;
                    case 14:
                        message.append("     Туз ");

                        break;
                }

            } else {
                message.append("     ").append(currentCard.getValues()).append(" ");

            }
            message.append(currentCard.getSuit());
            message.append("\n");
        }
        message.append("Счёт: ").append(getScore(card));
        botInteraction.sendMessageInGameClient(message.toString(), chatId);
    }

    private void resetProperty(){
        user.setBalance((double) 0);
        user.setDealerCards(new ArrayList<>());
        user.setPlayerCards(new ArrayList<>());
        user.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND));
    }
}
