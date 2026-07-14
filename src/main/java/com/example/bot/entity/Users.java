package com.example.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.javatuples.Pair;
import java.util.ArrayList;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "chat_id")
    private Long chatId;

    private String status;

    private Integer balance;

    private Integer bet;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dealer_cards",columnDefinition = "jsonb")
    private ArrayList<Pair<Integer, String>> dealerCards = new ArrayList<>();


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ArrayList<Pair<Integer, String>>  cards = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "player_cards",columnDefinition = "jsonb")
    private ArrayList<ArrayList<Pair<Integer, String>>>  playerCards = new ArrayList<>();

    public Users(Long chatId){
        this.chatId = chatId;
        balance = 1000;
    }

}