package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PurchaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // TODO get item to be purchased (delivery options are needed)
        String delivery = "χέρι-με-χέρι";

        availableDeliveryOptions(delivery);
    }

    /** Disables unavailable delivery option if any*/
    private void availableDeliveryOptions(String delivery) {
        RadioButton hand = findViewById(R.id.delivery_handToHand);
        RadioButton courier = findViewById(R.id.delivery_courier);
        if(delivery.equalsIgnoreCase(getResources().getString(R.string.shipment))){
            hand.setTextColor(getResources().getColor(R.color.grey));
            hand.setEnabled(false);
            courier.setChecked(true);
            LinearLayout details = findViewById(R.id.purchase_layout_hand);
            final int childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = details.getChildAt(i);
                v.setEnabled(false);
                if (v instanceof TextView)
                    ((TextView) v).setTextColor(getResources().getColor(R.color.grey));
                else if (v instanceof Button)
                    ((Button) v).setBackgroundColor(getResources().getColor(R.color.grey));
            }
            findViewById(R.id.hand_not_offered).setVisibility(View.VISIBLE);
        }
        else if(delivery.equalsIgnoreCase(getResources().getString(R.string.hand_in_hand))) {
            courier.setTextColor(getResources().getColor(R.color.grey));
            courier.setEnabled(false);
            hand.setChecked(true);
            LinearLayout details = findViewById(R.id.purchase_layout_courier);
            int childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = details.getChildAt(i);
                v.setEnabled(false);
                if (v instanceof EditText)
                    ((EditText) v).setTextColor(getResources().getColor(R.color.grey));
                else if (v instanceof Button)
                    ((Button) v).setBackgroundColor(getResources().getColor(R.color.grey));
                else if (v instanceof TextView)
                    ((TextView) v).setTextColor(getResources().getColor(R.color.grey));
            }

            details = findViewById(R.id.purchase_layout_courier_numbers);
            childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                EditText v = (EditText) details.getChildAt(i);
                v.setEnabled(false);
                v.setHintTextColor(getResources().getColor(R.color.grey));
            }
            findViewById(R.id.courier_not_offerer).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void goBack() {
        Intent main = new Intent(PurchaseActivity.this, NotificationsActivity.class);
        startActivity(main);
    }
}
