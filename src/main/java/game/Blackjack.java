package game;

import com.example.bot.Constant;
import com.example.bot.UsersStatus;
import com.example.bot.entity.Users;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Component
public class Blackjack {
    private Double balance;
    private Double bet;

    private ArrayList<Card> cards;
    private ArrayList<Card> startPlayerCards = new ArrayList<>();
    private final ArrayList<ArrayList<Card>> playerCards;
    private final ArrayList<Card> dealerCards;

    private boolean firstDealerAce = false;
    private boolean insurance = false;

    private final Random rd = new Random();
    private TelegramClient gameClient = new OkHttpTelegramClient(Constant.BOTTOKEN);

    private Long chatId;
    private Users user;


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

            sendMessage("\n     Ваш счёт: " + getScore(startPlayerCards), chatId);


            getSomeCardDealer();
            printAllCards(dealerCards, 0);

            if(getScore(startPlayerCards) == 21 && !firstDealerAce ){
                blackJack();
                return;
            }
            user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE1));
            playerCards.add(startPlayerCards);

            System.out.println(playerCards.size());
            System.out.println(playerCards.get(0));
            user.setDealerCards(dealerCards);
            user.setPlayerCards(playerCards);
            user.setCards(cards);

            sendMessage("Желаете взять карту (1), удвоить ставку (2), разделить карты (3), ничего не делать (4)", chatId);
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

        if(playerCards.isEmpty()) return;



        while(getScore(dealerCards) < 17){
            getSomeCardDealer();
        }
        printAllCards(dealerCards, 0);


        sendMessage("\n     Счёт дилера: " + getScore(dealerCards), chatId);

        int count = 1;

        for(ArrayList<Card> currentCards : playerCards){
            sendMessage("Игра №"+count +"\nВаш счёт: " + getScore(currentCards), chatId);
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
                sendMessage("Ничья", chatId);
            }
        }
        user.setDealerCards(new ArrayList<>());
        user.setPlayerCards(new ArrayList<>());
        user.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND));
    }

    public void repeatChoice(String answer){
        switch (user.getStatus()){
            case "GAME_REPEAT_CHOICE1":
                if(answer.equals("да") || answer.equals("Да")){
                    startPlayerCards = playerCards.get(0);
                    playerCards.remove(0);

                    getSomeCardPlayer(startPlayerCards);
                    printAllCards(startPlayerCards, 1);
                    sendMessage("     Ваш счёт: " + getScore(startPlayerCards), chatId);

                    if(getScore(startPlayerCards) > 21) {
                        playerCards.add(startPlayerCards);
                        endGame();
                    }

                    sendMessage("Желаете ли взять ещё карту? (Да / Нет)", chatId); // Добавить клаву

                    playerCards.add(startPlayerCards);
                } else{
                    endGame();
                }
                break;
            case "GAME_REPEAT_CHOICE21":
                if(answer.equals("да")){
                    getSomeCardPlayer(playerCards.get(0));
                    printAllCards(playerCards.get(0), 1);
                    sendMessage("     Ваш счёт: " + getScore(playerCards.get(0)), chatId);

                    if(getScore(playerCards.get(0)) > 21) {
                        user.setStatus("GAME_CHOICE22");
                        return;
                    }

                    sendMessage("Желаете ли взять ещё карту? (Да / Нет)", chatId); // Добавить клаву
                } else{
                    user.setStatus("GAME_CHOICE22");
                }
                break;
            case "GAME_REPEAT_CHOICE22":
                if(answer.equals("да")){
                    getSomeCardPlayer(playerCards.get(1));
                    printAllCards(playerCards.get(1), 1);
                    sendMessage("     Ваш счёт: " + getScore(playerCards.get(1)), chatId);

                    if(getScore(playerCards.get(1)) > 21) {
                        endGame();
                        return;
                    }

                    sendMessage("Желаете ли взять ещё карту? (Да / Нет)", chatId); // Добавить клаву
                } else{
                    endGame();
                }
                break;
        }
    }

    public void choiceInDoubleGame(Integer numb){
        int count;
        if(user.getStatus().equals("GAME_CHOICE21")){count = 0;}
        else{count = 1;}

        switch (numb){
            case 3:
                if(user.getStatus().equals("GAME_CHOICE22")) {endGame();}
                else{user.setStatus("GAME_CHOICE22"); doubleGame();}
                return ;
            case 2: // не работает
                sendMessage("Функция временно не работает, выберите другую", chatId);
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
                sendMessage("     Ваш счёт: " + getScore(playerCards.get(count)), chatId);

                if(getScore(playerCards.get(count)) > 21) {
                    if(count == 1) endGame();
                    else{user.setStatus("GAME_CHOICE_22");
                        doubleGame();
                    }
                    return;
                }

                sendMessage("Желаете ли взять ещё карту? (Да / Нет)", chatId); // Добавить клаву
                if(count == 0){
                    user.setStatus("GAME_REPEAT_CHOICE21");
                } else{
                    user.setStatus("GAME_REPEAT_CHOICE22");
                }
                break;
        }
    }

    public void choice(Integer choice){
        startPlayerCards = playerCards.get(0);
        playerCards.remove(0);
        switch (choice){
            case 1:
                getSomeCardPlayer(startPlayerCards);
                printAllCards(startPlayerCards, 1);
                sendMessage("     Ваш счёт: " + getScore(startPlayerCards), chatId);

                if(getScore(startPlayerCards) > 21) {
                    playerCards.add(startPlayerCards);
                    endGame();
                    user.setStatus("WAIT_NEW_COMMAND");
                    return;
                }

                sendMessage("Желаете ли взять ещё карту? (Да / Нет)", chatId); // Добавить клаву
                user.setStatus(String.valueOf(UsersStatus.GAME_REPEAT_CHOICE1));
                playerCards.add(startPlayerCards);
                break;

            case 2:
                if(bet * 2 > balance){
                    sendMessage("Недостаточно денег для удвоения ставки, выберите другую функцию", chatId);
                    playerCards.add(startPlayerCards);
                    return;
                }
                bet *=2;
                sendMessage("Вы взяли карту.", chatId);
                getSomeCardPlayer(startPlayerCards);
                printAllCards(startPlayerCards, 1);
                sendMessage("     Ваш счёт: " + getScore(startPlayerCards), chatId);

                playerCards.add(startPlayerCards);
                endGame();
                return;

            case 3:
                if(balance < bet * 2){
                    sendMessage("Недостаточно денег для ставки, выберите другую функцию", chatId);
                    playerCards.add(startPlayerCards);
                    return ;
                }
                if (!((getScore(startPlayerCards) % 2 == 0 && Objects.equals(startPlayerCards.get(0).getValues(), startPlayerCards.get(1).getValues()))
                        || (startPlayerCards.get(0).getValues() >=10 && startPlayerCards.get(1).getValues() >=10) && (startPlayerCards.size()==2)) ){
                    sendMessage("Не соблюдены условия сплита, выберите другую функцию", chatId);
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

        sendMessage("Рука №" +(count+1), chatId);

        ArrayList<Card> currentCards = playerCards.get(count);


        getSomeCardPlayer(currentCards);
        printAllCards(currentCards, 1);

        if(getScore(currentCards) == 21){ // Блэкджек при сплите не считается блэкджеком
            playerCards.add(currentCards);
            return ;
        }

        sendMessage("Желаете взять карту (1), удвоить ставку (2), ничего не делать (3)", chatId);
        if(count == 0) {user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE21));}
        else{user.setStatus(String.valueOf(UsersStatus.GAME_CHOICE22));}
    }



    private void blackJack(){
        sendMessage("Блэкджек!!! Вы выйграли: " + (1.5 * bet), chatId);
        balance += 1.5 * bet;

        user.setBalance(balance);
        user.setStatus(String.valueOf(UsersStatus.WAIT_NEW_COMMAND));
    }

    private void win(){
       sendMessage("Вы выйграли: " + (bet), chatId);
        balance += bet;

        user.setBalance(balance);

    }

    private void lose(){
        sendMessage("Вы проиграли: " + (bet), chatId);
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
        sendMessage("Перетасовка карт", chatId);
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
        if(n == 1) sendMessage("Ваши карты: ", chatId);
        else sendMessage("Карты дилера: ", chatId);

        StringBuilder message = new StringBuilder();
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
        sendMessage(message.toString(), chatId);
    }

    public void sendMessage(String message, Long chatId){
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        try {
            gameClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
