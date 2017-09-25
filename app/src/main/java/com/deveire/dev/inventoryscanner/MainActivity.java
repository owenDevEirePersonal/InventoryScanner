package com.deveire.dev.inventoryscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    private TextView orderInfoText;
    private EditText orderIDEditText;
    private ArrayList<String> claimedIDs;
    private ArrayList<String> claimedUsers;
    private ArrayList<Date> claimedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        claimedIDs = new ArrayList<String>();
        claimedUsers = new ArrayList<String>();

        Calendar aCalender = Calendar.getInstance();
        DateFormat simpleFormat = new SimpleDateFormat("dd:MM:yyyy hh:mm");

        claimedIDs.add("0456B2527D3680");
        claimedUsers.add("René Artois");
        try
        {
            claimedDate.add(simpleFormat.parse("24:09:2017 03:15"));
        }
        catch (ParseException e)
        {

        }

        orderInfoText = (TextView) findViewById(R.id.orderInfoText);
        orderIDEditText = (EditText) findViewById(R.id.orderIDEditText);
        orderIDEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(!orderIDEditText.getText().toString().matches("") && orderIDEditText.getText().toString().length() == 14)
                {
                    Log.i("Order Update", "Scanned Order ID = " + orderIDEditText.getText().toString());
                    displayOrderDetails(orderIDEditText.getText().toString());
                    orderIDEditText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });
    }

    private void displayOrderDetails(String orderIDin)
    {
        Calendar aCalender = Calendar.getInstance();
        DateFormat simpleFormat = new SimpleDateFormat("dd:MM:yyyy hh:mm");
        String details;

        boolean orderHasBeenClaimed = false;
        int claimedOrderIndex = 0;
        for (String aId: claimedIDs)
        {
            if(orderIDin.matches(aId))
            {
                orderHasBeenClaimed = true;
                break;
            }
            claimedOrderIndex++;
        }

        if(!orderHasBeenClaimed)
        {
            switch (orderIDin)
            {
                case "0475A5527D3680": details = " ID: " + orderIDin + "\n\n Item Name: Eggs\n\n Quanity: 16 Cartons(12 eggs each)\n\n Allegens: Eggs \n\n Dispatched: 25/09/2017 10:06\n\n" +
                        " Received: " + simpleFormat.format(aCalender.getTime()) + "\n\n Signed for by:" + " Joe Exotic";
                    claimedUsers.add("Joe Exotic");
                    claimedIDs.add(orderIDin);
                    claimedDate.add(aCalender.getTime());
                    break;

                case "0456B2527D3680": details = " ID: " + orderIDin + "\n\n Item Name: Glutten-Free Pre-Sliced Bread Loaves\n\n Quanity: 100 Loaves\n\n Allegens: None \n\n Dispatched: 24/09/2017 10:01\n\n" +
                        " Received: " + simpleFormat.format(aCalender.getTime()) + "\n\n Signed for by:" + " Joe Exotic";
                    claimedUsers.add("Joe Exotic");
                    claimedIDs.add(orderIDin);
                    claimedDate.add(aCalender.getTime());
                    break;
                default: details = "Order with ID " + orderIDin + " not found.";
            }
        }
        else
        {
            details = " Order " + claimedIDs.get(claimedOrderIndex) + " has already been claimed by " + claimedUsers.get(claimedOrderIndex) + " at " + simpleFormat.format(claimedDate.get(claimedOrderIndex)) + ".";
        }
        orderInfoText.setText(details);
    }
}
