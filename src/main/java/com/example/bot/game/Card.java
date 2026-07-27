package com.example.bot.game;

import java.io.Serial;
import java.io.Serializable;

public class Card implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer values;
    private String suit;

    public Card(){}
    public Card(Integer values, String suit){
        this.values = values;
        this.suit = suit;
    }

    public Integer getValues(){
        return values;
    }

    public String getSuit(){
        return suit;
    }

    public void setValues(Integer values){
        this.values = values;
    }

    public void setSuit(String suit){
        this.suit = suit;
    }
}
