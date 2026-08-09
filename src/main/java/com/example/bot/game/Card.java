package com.example.bot.game;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Card implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer values;
    private String suit;

    public Card(Integer values, String suit){
        this.values = values;
        this.suit = suit;
    }

}
