package com.deveire.dev.inventoryscanner;

import java.util.Date;

/**
 * Created by owenryan on 08/01/2018.
 */

public class Product
{
    private String barcode;
    private String name;
    private String description;
    private float calories;
    private float salt; //in grams per kilo
    private float fat; //in grams per kilo
    private float saturatedFat; //in grams per kilo
    private float sugar; //in grams per kilo
    private boolean conditionIsOkay;
    private Date useByDate;

    public Product(String inBarcode, String inName, String inDescription, float inCalories, float inSalt, float inFat, float inSaturatedFat, float inSugar, Date inUseByDate)
    {
        this.barcode = inBarcode;
        this.name = inName;
        this.description = inDescription;
        this.calories = inCalories;
        this.salt = inSalt;
        this.fat = inFat;
        this.saturatedFat = inSaturatedFat;
        this.sugar = inSugar;
        this.useByDate = inUseByDate;
        this.conditionIsOkay = true;
    }

    public Product(String inBarcode, String inName, String inDescription, float inCalories, float inSalt, float inFat, float inSaturatedFat, float inSugar, boolean isOkay, Date inUseByDate)
    {
        this.barcode = inBarcode;
        this.name = inName;
        this.description = inDescription;
        this.calories = inCalories;
        this.salt = inSalt;
        this.fat = inFat;
        this.saturatedFat = inSaturatedFat;
        this.sugar = inSugar;
        this.useByDate = inUseByDate;
        this.conditionIsOkay = isOkay;
    }

    public Product()
    {
        //for use as placeholder, not for actual use.
    }

    public String toString()
    {
        return "Name: " + this.name + " condition: " + this.conditionIsOkay;
        //return "Name: " + this.name + " Barcode:" + this.barcode + " Description: " + this.description + " Calories: " + this.calories + " Salt: " + this.salt + " Fat: " + this.fat + " SaturatedFat: " + this.saturatedFat + " Sugar: " + this.sugar + " Use By Date: " + this.useByDate;
    }

    public String getBarcode()
    {
        return barcode;
    }

    public void setBarcode(String barcode)
    {
        this.barcode = barcode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public float getCalories()
    {
        return calories;
    }

    public void setCalories(float calories)
    {
        this.calories = calories;
    }

    public float getSalt()
    {
        return salt;
    }

    public void setSalt(float salt)
    {
        this.salt = salt;
    }

    public float getFat()
    {
        return fat;
    }

    public void setFat(float fat)
    {
        this.fat = fat;
    }

    public float getSaturatedFat()
    {
        return saturatedFat;
    }

    public void setSaturatedFat(float saturatedFat)
    {
        this.saturatedFat = saturatedFat;
    }

    public float getSugar()
    {
        return sugar;
    }

    public void setSugar(float sugar)
    {
        this.sugar = sugar;
    }

    public Date getUseByDate()
    {
        return useByDate;
    }

    public void setUseByDate(Date inDate)
    {
        this.useByDate = inDate;
    }

    public boolean isConditionIsOkay()
    {
        return this.conditionIsOkay;
    }

    public void setConditionIsOkay(boolean inConditionIsOkay)
    {
        this.conditionIsOkay = inConditionIsOkay;
    }
}
