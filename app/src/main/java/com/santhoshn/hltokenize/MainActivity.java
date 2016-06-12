package com.santhoshn.hltokenize;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardForm;
import com.santhoshn.hltokenize.service.HLCard;
import com.santhoshn.hltokenize.service.HLToken;
import com.santhoshn.hltokenize.service.HLTokenService;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

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
                    hideKeyboard(MainActivity.this, resultTextView);
                    showProgressDialog();
                    HLTokenService service = new HLTokenService(""); // set the public here.
                    HLCard card = new HLCard();
                    card.setNumber(cardForm.getCardNumber());
                    card.setExpMonth(Integer.parseInt(cardForm.getExpirationMonth()));
                    card.setExpYear(Integer.parseInt(cardForm.getExpirationYear()));
                    card.setCvv(cardForm.getCvv());

                    service.getToken(card, new HLTokenService.TokenCallback() {
                        @Override
                        public void onComplete(HLToken response) {
                            hideProgressDialog();
                            if (response == null || response.getError() != null) {
                                String message = (response == null) ? "Empty Response" : response.getError().getMessage();
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            } else {
                                if (resultTextView != null) {
                                    resultTextView.setText(response.getTokenValue());
                                    resultTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.please_wait), getString(R.string.tokenizing));
            mProgressDialog.setCancelable(false);
        } else {
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
