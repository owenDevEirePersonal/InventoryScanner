package com.deveire.dev.inventoryscanner;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AddProductActivity extends AppCompatActivity
{
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText caloriesEditText;
    private EditText saltEditText;
    private EditText fatEditText;
    private EditText saturatedFatEditText;
    private EditText sugarEditText;
    private EditText dateEditText;
    private EditText barcodeEditText;

    private Button saveProductButton;

    private SharedPreferences savedData;

    private ArrayList<Product> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        caloriesEditText = (EditText) findViewById(R.id.carloriesEditText);
        saltEditText = (EditText) findViewById(R.id.saltEditText);
        fatEditText = (EditText) findViewById(R.id.fatEditText);
        saturatedFatEditText = (EditText) findViewById(R.id.saturatedFatEditText);
        sugarEditText = (EditText) findViewById(R.id.sugarEditText);
        dateEditText= (EditText) findViewById(R.id.dateEditText);
        barcodeEditText = (EditText) findViewById(R.id.barcodeEditText);


        saveProductButton = (Button) findViewById(R.id.saveProductButton);
        saveProductButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Product newProduct = new Product();
                SimpleDateFormat aformat = new SimpleDateFormat("dd/MM/yyyy");
                try
                {
                    newProduct = new Product(
                            barcodeEditText.getText().toString(),
                            nameEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            Float.parseFloat(caloriesEditText.getText().toString()),
                            Float.parseFloat(saltEditText.getText().toString()),
                            Float.parseFloat(fatEditText.getText().toString()),
                            Float.parseFloat(saturatedFatEditText.getText().toString()),
                            Float.parseFloat(sugarEditText.getText().toString()),
                            aformat.parse(dateEditText.getText().toString())
                            );
                    saveNewProduct(newProduct);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }


            }
        });

        allProducts = new ArrayList<Product>();

        savedData = getSharedPreferences("SavedClaims", MODE_PRIVATE);
        loadProducts();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveProducts();
    }

    private void saveNewProduct(Product newProduct)
    {
        Log.i("Product", "Saving new Product: Barcode: " + newProduct.getBarcode() + " Name: " + newProduct.getName() + " Description: " + newProduct.getDescription() + " Calories: " + newProduct.getCalories() + " Salt: " + newProduct.getSalt() + " Fat: " + newProduct.getFat() + " Saturated Fat: " + newProduct.getSaturatedFat() + " Sugar: " + newProduct.getSugar() + " Use by date: " + newProduct.getUseByDate());
        int i = 0;
        boolean foundDuplicate = false;
        for (Product a: allProducts)
        {
            if(a.getBarcode().matches(newProduct.getBarcode()))
            {
                Log.i("Product", "Replacing old product:" + a.toString() + "\n with new product: " + newProduct.toString());
                Toast.makeText(this, "Replacing old product:" + a.toString() + "\n with new product: " + newProduct.toString(), Toast.LENGTH_LONG).show();
                allProducts.set(i, newProduct);
                foundDuplicate = true;
                break;
            }
            i++;
        }
        if(!foundDuplicate)
        {
            allProducts.add(newProduct);
            Log.i("Product", "Added new Product: " + newProduct.toString());
            Toast.makeText(this, "Added new Product: " + newProduct.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveProducts()
    {
        SimpleDateFormat aformat = new SimpleDateFormat("dd/MM/yyyy");
        SharedPreferences.Editor edit = savedData.edit();
        int i = 1;
        for (Product a: allProducts)
        {
            edit.putString("Product" + i + "_Barcode", a.getBarcode());
            edit.putString("Product" + i + "_Name", a.getName());
            edit.putString("Product" + i + "_Description", a.getDescription());
            edit.putFloat("Product" + i + "_Calories", a.getCalories());
            edit.putFloat("Product" + i + "_Salt", a.getSalt());
            edit.putFloat("Product" + i + "_Fat", a.getFat());
            edit.putFloat("Product" + i + "_SaturatedFat", a.getSaturatedFat());
            edit.putFloat("Product" + i + "_Sugar", a.getSugar());
            edit.putString("Product" + i + "_UseByDate", aformat.format(a.getUseByDate()));

            i++;
        }
        edit.putInt("numberOfProducts", allProducts.size());
        edit.commit();
    }

    private void loadProducts()
    {
        SimpleDateFormat aformat = new SimpleDateFormat("dd/MM/yyyy");
        int numberOfProducts = savedData.getInt("numberOfProducts", 0);

        for (int i = 1; i <= numberOfProducts; i++)
        {
            Product aProduct = new Product();
            aProduct.setBarcode(savedData.getString("Product" + i + "_Barcode", ""));
            aProduct.setName(savedData.getString("Product" + i + "_Name", ""));
            aProduct.setDescription(savedData.getString("Product" + i + "_Description", ""));
            aProduct.setCalories(savedData.getFloat("Product" + i + "_Calories", 0.0f));
            aProduct.setSalt(savedData.getFloat("Product" + i + "_Salt", 0.0f));
            aProduct.setFat(savedData.getFloat("Product" + i + "_Fat", 0.0f));
            aProduct.setSaturatedFat(savedData.getFloat("Product" + i + "_SaturatedFat", 0.0f));
            aProduct.setSugar(savedData.getFloat("Product" + i + "_Sugar", 0.0f));
            try
            {
                aProduct.setUseByDate(aformat.parse(savedData.getString("Product" + i + "_UseByDate", "01/01/2000")));
            } catch (ParseException e)
            {
                Log.e("Product", " Error parsing date: " + savedData.getString("Product" + i + "_UseByDate", "01/01/2000") + " of: " + aProduct.toString());
                e.printStackTrace();
            }
            allProducts.add(aProduct);
            Log.i("Product", "Product Loaded: " + aProduct.toString());
        }

    }
}
