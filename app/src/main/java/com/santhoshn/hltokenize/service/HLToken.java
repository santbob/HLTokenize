package com.santhoshn.hltokenize.service;

/**
 * Created by santhosh on 09/06/16.
 */
public class HLToken {
    private String object;
    private String token_type;
    private String token_value;
    private String token_expire;
    private Card card;
    private HLError error;

    public HLToken() {
    }

    public HLToken(String number, String cvc, int expMonth, int expYear) {
        this.object = "token";
        this.token_type = "supt";
        this.card = new Card(number, cvc, expMonth, expYear);
    }

    public HLToken(HLCard card) {
        this.object = "token";
        this.token_type = "supt";
        this.card = new Card(card.getNumber(), card.getCvv(), card.getExpMonth(), card.getExpYear());
    }

    public HLError getError() {
        return error;
    }

    public String getTokenType() {
        return token_type;
    }

    public String getTokenValue() {
        return token_value;
    }

    public void setToken_value(String token_value) {
        this.token_value = token_value;
    }

    public void setToken_expire(String token_expire) {
        this.token_expire = token_expire;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getTokenExpire() {
        return token_expire;
    }

    public Card getCard() {
        return card;
    }

    class Card {

        private String number;
        private String cvc;
        private Integer exp_month;
        private Integer exp_year;

        public Card() {

        }

        public Card(String number, String cvc, Integer expMonth, Integer expYear) {
            this.number = number;
            this.cvc = cvc;
            this.exp_month = expMonth;
            this.exp_year = expYear;
        }

        public String getNumber() {
            return number;
        }
    }
}
