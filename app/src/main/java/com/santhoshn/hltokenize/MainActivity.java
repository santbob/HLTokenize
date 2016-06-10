package com.santhoshn.hltokenize;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardForm;
import com.santhoshn.hltokenize.service.HLCard;
import com.santhoshn.hltokenize.service.HLToken;
import com.santhoshn.hltokenize.service.HLTokenService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView resultTextView = (TextView) findViewById(R.id.tokenizeResult);
        final CardForm cardForm = (CardForm) findViewById(R.id.bt_card_form);
        if (cardForm != null) {
            cardForm.setRequiredFields(MainActivity.this, true, true, true, false, "Tokenize");

            cardForm.setOnCardFormSubmitListener(new OnCardFormSubmitListener() {
                @Override
                public void onCardFormSubmit() {

                    HLTokenService service = new HLTokenService(""); // set the public here.
                    HLCard card = new HLCard();
                    card.setNumber(cardForm.getCardNumber());
                    card.setExpMonth(Integer.parseInt(cardForm.getExpirationMonth()));
                    card.setExpYear(Integer.parseInt(cardForm.getExpirationYear()));
                    card.setCvv(cardForm.getCvv());

                    service.getToken(card, new HLTokenService.TokenCallback() {
                        @Override
                        public void onComplete(HLToken response) {
                            if (response == null || response.getError() != null) {
                                String message = (response == null) ? "Empty Response" : response.getError().getMessage();
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            } else {
                                if (resultTextView != null) {
                                    resultTextView.setText(response.getTokenValue());
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
