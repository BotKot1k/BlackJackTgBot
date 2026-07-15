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
    private final ArrayList<Card> startPlayerCards = new ArrayList<>();
    private final ArrayList<ArrayList<Card>> playerCards;
    private final ArrayList<Card> dealerCards;

    private Integer count = 1;
    private boolean firstDealerAce = false;
    private boolean insurance = false;

    private Random rd = new Random();
    private Scanner sc = new Scanner(System.in);
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
        playerCards.add(startPlayerCards);

        user.setDealerCards(dealerCards);
        user.setPlayerCards(playerCards);
        user.setCards(cards);

        sendMessage("Желаете взять карту (1), удвоить ставку (2), разделить карты (3), ничего не делать (4)", chatId);
    }


    public void game(){

        checkCards();

        getSomeCardPlayer(startPlayerCards);
        getSomeCardPlayer(startPlayerCards);
        printAllCards(startPlayerCards, 1);

        System.out.println();
        System.out.println("     Ваш счёт: " + getScore(startPlayerCards));



        getSomeCardDealer();
        printAllCards(dealerCards, 0);

        if(getScore(startPlayerCards) == 21 /*&& !firstDealerAce*/ ){ // Допилить страховку
            blackJack();
            return;
        }


        if(getScore(startPlayerCards) == 21 && firstDealerAce){
            System.out.println("Желаете ли вы перестраховаться в данной ситуации? (Да/нет)");
            String answer = sc.next();
            if(answer.equals("Да") || answer.equals("да") || answer.equals("1")){
                win();
                return;
            } else{
                getSomeCardDealer();
                printAllCards(dealerCards, 0);

                if(getScore(dealerCards) == 21){
                    System.out.println("Ничья");
                    return;
                } else {
                    blackJack();
                }
            }
        }

        if(firstDealerAce){
            System.out.print("Желаете ли вы застраховать ставку? (1/0)");
            String answer = sc.next();
            if(Objects.equals(answer, "1")){
                if(balance < bet/2){
                    System.out.println("Не хватает денег");
                    return;
                }
                insurance = true;
            }
        }

        if(!choice()){
            System.out.println("Ошибка");
            return;
        }





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

        System.out.println();
        System.out.println("     Счёт дилера: " + getScore(dealerCards));

        // Итоги
        int count = 1;
        for(ArrayList<Card> currentCards : playerCards){
            System.out.println("Игра №"+count);
            //printAllCards(currentCards, 1);
            System.out.println("Ваш счёт: " + getScore(currentCards));
            count++;
            if(getScore(dealerCards) > 21){
                win();
                continue;
            }

            if(dealerCards.size() == 2 && getScore(dealerCards) == 21 && insurance){
                System.out.println("У дилера блэкджек! Страховка сыграла");
                bet/=2;
                win();
            } else if(insurance) {
                System.out.println("Страховка не сыграла");
                balance -= bet/2;
            }

            if(getScore(currentCards) > getScore(dealerCards)){
                win();
            } else if (getScore(currentCards) < getScore(dealerCards)){
                lose();
            } else{
                System.out.println("Ничья");
            }
            System.out.println();
        }
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
        if(cards == null || cards.isEmpty()){
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

    private boolean doubleGame(Card card){
        System.out.println("Колода №" +count);
        //System.out.println(card.getValues() + " " + card.getSuit() + "    " + player);

        ArrayList<Card> currentCards = new ArrayList<>();
        currentCards.add(card);

        count++;
        getSomeCardPlayer(currentCards);
        printAllCards(currentCards, 1);

        if(getScore(currentCards) == 21){ // Блэкджек при сплите не считается блэкджеком
            playerCards.add(currentCards);
            return true;
        }
        if(!choiceInDoubleGame(currentCards)){
            System.out.println("Ошибка");
            return false;
        }
        return true;
    }

    private boolean choice(){
        String answer;
        System.out.println("Желаете взять карту (1), удвоить ставку (2), разделить карты (3), ничего не делать (4)");
        int numb = sc.nextInt();

        switch (numb){
            case 1:
                do{
                    getSomeCardPlayer(startPlayerCards);
                    printAllCards(startPlayerCards, 1);
                    System.out.println("     Ваш счёт: " + getScore(startPlayerCards));

                    if(getScore(startPlayerCards) > 21) {
                        playerCards.add(startPlayerCards);
                        return true;
                    }


                    System.out.println("Желаете ли взять ещё карту? (Да / нет)");
                    answer = sc.next();

                }while(answer.equals("Да") || answer.equals("да") || answer.equals("1"));
                playerCards.add(startPlayerCards);
                return true;

            case 2:
                if(bet * 2 > balance){
                    System.out.println("Внимание! Новая ставка не может быть больше баланса");
                    return false;
                }
                bet *=2;
                System.out.println("Вы взяли: ");
                getSomeCardPlayer(startPlayerCards);
                printAllCards(startPlayerCards, 1);
                System.out.println("     Ваш счёт: " + getScore(startPlayerCards));

                if(getScore(startPlayerCards) > 21) {
                    playerCards.add(startPlayerCards);
                    return true;
                }
                playerCards.add(startPlayerCards);
                return true;

            case 3:
                if(balance < bet * 2){
                    System.out.println("Недостаточно денег для ставки");
                    return false;
                }
                if (!((getScore(startPlayerCards) % 2 == 0 && Objects.equals(startPlayerCards.get(0).getValues(), startPlayerCards.get(1).getValues()))
                        || (startPlayerCards.get(0).getValues() >=10 && startPlayerCards.get(1).getValues() >=10) && (startPlayerCards.size()==2)) ){
                    System.out.println("Не соблюдены условия сплита");
                    return false;
                }


                return doubleGame(startPlayerCards.get(0)) && doubleGame(startPlayerCards.get(1));
            case 4:
                playerCards.add(startPlayerCards);
                return true;
            default:
                System.out.println("Неправильная операция");
                return false;
        }
    }

    private boolean choiceInDoubleGame( ArrayList<Card> currentPlayerCards){
        String answer;

        System.out.println("Желаете взять карту (1), удвоить ставку (2), ничего не делать (3)");
        int numb = sc.nextInt();

        switch (numb){
            case 3:
                playerCards.add(currentPlayerCards);
                return true;
            case 2:
                if(bet * 2 > balance){
                    System.out.println("Внимание! Новая ставка не может быть больше баланса");
                    return false;
                }
                bet *=2;
                System.out.println("Вы взяли: ");
                getSomeCardPlayer(currentPlayerCards);
                printAllCards(currentPlayerCards, 1);
                System.out.println("     Ваш счёт: " + getScore(currentPlayerCards));

                if(getScore(currentPlayerCards) > 21) {
                    playerCards.add(currentPlayerCards);
                    return true;
                }

                return true;
            case 1:
                do{
                    System.out.println("Вы взяли: ");
                    getSomeCardPlayer(currentPlayerCards);
                    printAllCards(currentPlayerCards, 1);
                    System.out.println("     Ваш счёт: " + getScore(currentPlayerCards));

                    if(getScore(currentPlayerCards) > 21) {
                        playerCards.add(currentPlayerCards);
                        return true;
                    }

                    System.out.println("Желаете ли взять ещё карту? (Да/нет) (1/0)");
                    answer = sc.next();

                }while(answer.equals("Да") || answer.equals("да") || answer.equals("1") );
                playerCards.add(currentPlayerCards);
                return true;
            default:
                System.out.println("Неправильная операция");
                return false;

        }
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
