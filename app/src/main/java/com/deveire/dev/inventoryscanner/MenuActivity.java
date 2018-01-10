package com.deveire.dev.inventoryscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity
{

    private Button shippingButton;
    private Button kitchenButton;
    private Button addProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        shippingButton = (Button) findViewById(R.id.shippingButton);
        shippingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), ShippingActivity.class));
            }
        });

        kitchenButton = (Button) findViewById(R.id.kitchenButton);
        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        addProductButton = (Button) findViewById(R.id.addProductButton);
        addProductButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), AddProductActivity.class));
            }
        });

    }
}
