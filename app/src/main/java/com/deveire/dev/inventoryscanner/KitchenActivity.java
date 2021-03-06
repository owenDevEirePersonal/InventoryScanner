package com.deveire.dev.inventoryscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class KitchenActivity extends AppCompatActivity implements RecognitionListener
{
    private TextView orderInfoText;
    private EditText orderIDEditText;
    private EditText userNameEditText;
    private Button resetButton;

    private ConstraintLayout qandQLayout;
    private EditText quantityEditText;
    private Button badQualityButton;
    private Button subParQualityButton;
    private Button okQualityButton;
    private Button goodQualityButton;
    private Button submitButton;
    private Button addProductButton;


    private Timer waitForRestOfBarcodeTimer;
    private boolean isWaitingForRestOfBarcode;

    private ArrayList<String> claimedIDs;
    private ArrayList<String> claimedUsers;
    private ArrayList<Date> claimedDates;
    private ArrayList<Integer> claimedQuantities;
    private ArrayList<String> claimedQualities;
    private String tempclaimedID;
    private String tempclaimedUser;
    private Date tempclaimedDate;
    private Integer tempclaimedQuantity;
    private String tempclaimedQuality;

    private int currentQuality;
    private final int QUALITY_BAD = 1;
    private final int QUALITY_SUBPAR = 2;
    private final int QUALITY_OK = 3;
    private final int QUALITY_GOOD = 4;

    private DateFormat simpleFormat;

    private String currentOrderID;
    private String currentOrderDetails;

    private SharedPreferences savedData;

    private SpeechRecognizer recognizer;
    private Intent recogIntent;
    private int pingingRecogFor;
    private static final int pingingRecogFor_Quality = 1;
    private static final int pingingRecogFor_Quantity = 2;
    private static final int pingingRecogFor_EditQuality = 3;
    private static final int pingingRecogFor_EditQuantity = 4;
    private static final int pingingRecogFor_doEdit = 5;
    private static final int pingingRecogFor_Weight = 6;
    private static final int pingingRecogFor_MealName = 7;
    private static final int pingingRecogFor_MealServings = 8;
    private static final int pingingRecogFor_None = 9;

    private int claimToEditIndex;

    //[Stocker Variables]
    private ArrayList<Product> allProducts;
    private Product currentProduct;
    private Product currentMealValues;
    private String currentMealName;
    private int currentMealServings;
    private boolean scanningProducts;
    //[/Stocker Variables]

    //[Text To Speech Variables]
    private TextToSpeech toSpeech;
    private String speechInText;
    private HashMap<String, String> endOfSpeakIndentifier;

    private boolean isShuttingDownSpeech;

    private static final String speechID_AskForWeight = "AskingForWeight";
    private static final String speechID_AskForMealName = "AskingForMealName";
    private static final String speechID_AskForMealServings = "AskingforMealServings";
    //[/Text To Speech Variables]


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qandQLayout = (ConstraintLayout) findViewById(R.id.qandQLayout);
        submitButton = (Button) findViewById(R.id.submitButton);
        quantityEditText = (EditText) findViewById(R.id.quantityEditText);
        badQualityButton = (Button) findViewById(R.id.badQualityButton);
        subParQualityButton = (Button) findViewById(R.id.subparQualityButton);
        okQualityButton = (Button) findViewById(R.id.okQualityButton);
        goodQualityButton = (Button) findViewById(R.id.goodQualityButton);

        qandQLayout.setVisibility(View.INVISIBLE);




        simpleFormat = new SimpleDateFormat("dd:MM:yyyy hh:mm");

        savedData = getSharedPreferences("SavedClaims", MODE_PRIVATE);
        loadSavedClaims();


        Calendar aCalender = Calendar.getInstance();
        DateFormat simpleFormat = new SimpleDateFormat("dd:MM:yyyy hh:mm");

        claimedIDs.add("0456B2527D3680");
        claimedUsers.add("René Artois");
        claimedQualities.add("Good");
        claimedQuantities.add(10);
        try
        {
            claimedDates.add(simpleFormat.parse("24:09:2017 03:15"));
        }
        catch (ParseException e)
        {

        }

        orderInfoText = (TextView) findViewById(R.id.orderInfoText);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
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
                if(!isWaitingForRestOfBarcode && !orderIDEditText.getText().toString().matches("") && orderIDEditText.getText().toString().length() > 0)//14)
                {
                    isWaitingForRestOfBarcode = true;

                    waitForRestOfBarcodeTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Log.i("Order Update", "Scanned Order ID = " + orderIDEditText.getText().toString());
                                    currentOrderID = orderIDEditText.getText().toString();
                                    //displayOrderDetails(currentOrderID);
                                    if(scanningProducts)
                                    {
                                        for (Product aProduct: allProducts)
                                        {
                                            if(aProduct.getBarcode().matches(currentOrderID))
                                            {
                                                currentProduct = aProduct;
                                                toSpeech.speak("What is the weight of " + currentProduct.getName() + "?", TextToSpeech.QUEUE_FLUSH, null, speechID_AskForWeight);
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        toSpeech.speak("What is the name of the meal?", TextToSpeech.QUEUE_FLUSH, null, speechID_AskForMealName);
                                    }
                                    orderIDEditText.setText("");
                                    isWaitingForRestOfBarcode = false;
                                }
                            });
                        }
                    }, 300);

                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });




        badQualityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                badQualityButton.setBackgroundColor(Color.CYAN);
                subParQualityButton.setBackgroundColor(Color.DKGRAY);
                okQualityButton.setBackgroundColor(Color.DKGRAY);
                goodQualityButton.setBackgroundColor(Color.DKGRAY);
                currentQuality = QUALITY_BAD;
            }
        });
        subParQualityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                badQualityButton.setBackgroundColor(Color.DKGRAY);
                subParQualityButton.setBackgroundColor(Color.CYAN);
                okQualityButton.setBackgroundColor(Color.DKGRAY);
                goodQualityButton.setBackgroundColor(Color.DKGRAY);
                currentQuality = QUALITY_SUBPAR;
            }
        });
        okQualityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                badQualityButton.setBackgroundColor(Color.DKGRAY);
                subParQualityButton.setBackgroundColor(Color.DKGRAY);
                okQualityButton.setBackgroundColor(Color.CYAN);
                goodQualityButton.setBackgroundColor(Color.DKGRAY);
                currentQuality = QUALITY_OK;
            }
        });
        goodQualityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                badQualityButton.setBackgroundColor(Color.DKGRAY);
                subParQualityButton.setBackgroundColor(Color.DKGRAY);
                okQualityButton.setBackgroundColor(Color.DKGRAY);
                goodQualityButton.setBackgroundColor(Color.CYAN);
                currentQuality = QUALITY_GOOD;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if((currentQuality == QUALITY_BAD || currentQuality == QUALITY_SUBPAR || currentQuality == QUALITY_OK || currentQuality == QUALITY_GOOD) && !(quantityEditText.getText().toString().matches("")))
                {
                    qandQLayout.setVisibility(View.INVISIBLE);
                    submitClaim();
                }
            }
        });


        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                claimedUsers = new ArrayList<String>();
                claimedIDs = new ArrayList<String>();
                claimedDates = new ArrayList<Date>();
                orderInfoText.setText("-Please Scan Tag-");
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

        waitForRestOfBarcodeTimer = new Timer();
        isWaitingForRestOfBarcode = false;


        recognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recognizer.setRecognitionListener(this);
        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        //[Stocker Intialization]
        allProducts = new ArrayList<Product>();
        loadProducts();
        currentMealName = "No Meal";
        currentMealServings = 1;
        currentMealValues = new Product("", "", "", 0.0f, 0.0f , 0.0f, 0.0f, 0.0f, new Date());
        scanningProducts = false;
        //[/Stocker Intialization]

        setupTextToSpeech();
    }

    private void displayOrderDetails(String orderIDin)
    {
        Calendar aCalender = Calendar.getInstance();
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
                case "0475A5527D3680": currentOrderDetails = " ID: " + orderIDin + "\n\n Item Name: Eggs\n\n Quanity: 16 Cartons(12 eggs each)\n\n Allegens: Eggs \n\n Dispatched: 25/09/2017 10:06\n\n" +
                        " Received: " + simpleFormat.format(aCalender.getTime()) ;
                    details = currentOrderDetails + "\n\n Awaiting " + userNameEditText.getText().toString() + " to submit quanity and quality details";
                    //qandQLayout.setVisibility(View.VISIBLE);
                    submitClaim();
                    break;

                case "0456B2527D3680": currentOrderDetails = " ID: " + orderIDin + "\n\n Item Name: Glutten-Free Pre-Sliced Bread Loaves\n\n Quanity: 100 Loaves\n\n Allegens: None \n\n Dispatched: 24/09/2017 10:01\n\n" +
                        " Received: " + simpleFormat.format(aCalender.getTime()) ;
                    details = currentOrderDetails + "\n\n Awaiting Claim from:" + userNameEditText.getText().toString();
                    //qandQLayout.setVisibility(View.VISIBLE);
                    submitClaim();
                    break;
                case "04753C527D3680": currentOrderDetails = " ID: " + orderIDin + "\n\n Item Name: Potatoes\n\n Quanity: 50 Potatoes\n\n Allegens: None \n\n Dispatched: 24/09/2017 10:01\n\n" +
                        " Received: " + simpleFormat.format(aCalender.getTime()) ;
                    details = currentOrderDetails + "\n\n Awaiting Claim from:" + userNameEditText.getText().toString();
                    //qandQLayout.setVisibility(View.VISIBLE);
                    submitClaim();
                    break;
                default: details = "Order with ID " + orderIDin + " not found.";
            }
        }
        else
        {
            details = " Order " + claimedIDs.get(claimedOrderIndex) + " has already been claimed by " + claimedUsers.get(claimedOrderIndex) + " at " + simpleFormat.format(claimedDates.get(claimedOrderIndex)) + " with a reported quantity of " + claimedQuantities.get(claimedOrderIndex) + " in " + claimedQualities.get(claimedOrderIndex) + " quality. \nWould you like to edit it?";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                //speak leaves out the date from details
                toSpeech.speak(" Order " + claimedIDs.get(claimedOrderIndex) + " has already been claimed by " + claimedUsers.get(claimedOrderIndex) + " with a reported quantity of " + claimedQuantities.get(claimedOrderIndex) + " in " + claimedQualities.get(claimedOrderIndex) + " quality. \nWould you like to edit it?" , TextToSpeech.QUEUE_FLUSH, null,"DoEdit");
            }
        }
        orderInfoText.setText(details);

        //recognizer.startListening(recogIntent);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        saveClaims();

        waitForRestOfBarcodeTimer.cancel();
        waitForRestOfBarcodeTimer.purge();

        isShuttingDownSpeech = true;

        toSpeech.stop();
        recognizer.stopListening();
        recognizer.cancel();
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

    private void submitClaim()
    {
        Calendar aCalender = Calendar.getInstance();
        DateFormat simpleFormat = new SimpleDateFormat("dd:MM:yyyy hh:mm");


        tempclaimedUser = userNameEditText.getText().toString();
        tempclaimedID = currentOrderID;
        tempclaimedDate = aCalender.getTime();
        //recognizer.startListening(recogIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            toSpeech.speak("What is the quality of the goods delivered:", TextToSpeech.QUEUE_FLUSH, null, "QualityAsk");
        }
        /*claimedQuantities.add(Integer.parseInt(quantityEditText.getText().toString()));
        switch (currentQuality)
        {
            case QUALITY_BAD: claimedQualities.add("Bad"); break;
            case QUALITY_SUBPAR: claimedQualities.add("Sub-par"); break;
            case QUALITY_OK: claimedQualities.add("Ok"); break;
            case QUALITY_GOOD: claimedQualities.add("Good"); break;
        }

        String details = currentOrderDetails + "\n\n Claimed by: " + userNameEditText.getText().toString() +
                "\n\n Reported Quality: " + claimedQualities.get(claimedQualities.size() - 1) +
                "\n\n Reported Quantity: " + claimedQuantities.get(claimedQuantities.size() - 1);

        orderInfoText.setText(details);*/
    }

    private void loadSavedClaims()
    {
        claimedIDs = new ArrayList<String>();
        claimedUsers = new ArrayList<String>();
        claimedQuantities = new ArrayList<Integer>();
        claimedQualities = new ArrayList<String>();
        claimedDates = new ArrayList<Date>();

        int claimsCount = savedData.getInt("claimsCount", 0);
        for(int i = 1; i <= claimsCount; i++)
        {
            claimedUsers.add(savedData.getString("claimedUser" + i, "ERROR"));
            claimedIDs.add(savedData.getString("claimedID" + i, "ERROR"));
            claimedQuantities.add(savedData.getInt("claimedQuantity" + i, -1));
            claimedQualities.add(savedData.getString("claimedQuality" + i, "ERROR"));

            try
            {
                claimedDates.add(simpleFormat.parse(savedData.getString("claimedDates" + i, "ERROR")));
            }
            catch (ParseException e)
            {
                Log.e("InventoryReader", "ERROR trying to load claims: " + e.toString());
            }

        }
    }

    private void saveClaims()
    {
        SharedPreferences.Editor edit = savedData.edit();
        edit.putInt("claimsCount", claimedUsers.size());
        for(int i = 1; i <= claimedUsers.size(); i++)
        {
            edit.putString("claimedUser" + i, claimedUsers.get(i - 1));
            edit.putString("claimedID" + i, claimedIDs.get(i - 1));
            edit.putInt("claimedQuantity" + i, claimedQuantities.get(i - 1));
            edit.putString("claimedQuality" + i, claimedQualities.get(i - 1));
            edit.putString("claimedDates" + i, simpleFormat.format(claimedDates.get(i - 1)));
        }
        edit.commit();
    }

    private void addNutrientsToMeal(Product meal, int servings, Product ingredient, int ingredientWeightInKilos)
    {
        meal.setCalories(meal.getCalories() + ((ingredient.getCalories() * ingredientWeightInKilos) / servings));
        meal.setSalt(meal.getSalt() + ((ingredient.getSalt() * ingredientWeightInKilos) / servings));
        meal.setFat(meal.getFat() + ((ingredient.getFat() * ingredientWeightInKilos) / servings));
        meal.setSaturatedFat(meal.getSaturatedFat() + ((ingredient.getSaturatedFat() * ingredientWeightInKilos) / servings));
        meal.setSugar(meal.getSugar() + ((ingredient.getSugar() * ingredientWeightInKilos) / servings));
    }


//++++++++[Recognition Listener Code]
    @Override
    public void onReadyForSpeech(Bundle bundle)
    {
        Log.e("Recog", "ReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.e("Recog", "BeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v)
    {
        Log.e("Recog", "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes)
    {
        Log.e("Recog", "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech()
    {
        Log.e("Recog", "End ofSpeech");
        recognizer.stopListening();
    }

    @Override
    public void onError(int i)
    {
        switch (i)
        {
            //case RecognizerIntent.RESULT_AUDIO_ERROR: Log.e("Recog", "RESULT AUDIO ERROR"); break;
            //case RecognizerIntent.RESULT_CLIENT_ERROR: Log.e("Recog", "RESULT CLIENT ERROR"); break;
            //case RecognizerIntent.RESULT_NETWORK_ERROR: Log.e("Recog", "RESULT NETWORK ERROR"); break;
            //case RecognizerIntent.RESULT_SERVER_ERROR: Log.e("Recog", "RESULT SERVER ERROR"); break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: Log.e("Recog", "SPEECH TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_SERVER: Log.e("Recog", "SERVER ERROR"); break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e("Recog", "BUSY ERROR"); break;
            case SpeechRecognizer.ERROR_NO_MATCH: Log.e("Recog", "NO MATCH ERROR"); toSpeech.speak("No response Detected, please repeat", TextToSpeech.QUEUE_FLUSH, null, "repeat"); break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e("Recog", "NETWORK TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_NETWORK: Log.e("Recog", "TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e("Recog", "INSUFFICENT PERMISSIONS ERROR"); break;
            case SpeechRecognizer.ERROR_CLIENT: Log.e("Recog", "CLIENT ERROR"); break;
            case SpeechRecognizer.ERROR_AUDIO: Log.e("Recog", "AUDIO ERROR"); break;
            default: Log.e("Recog", "UNKNOWN ERROR: " + i); break;
        }
    }

    @Override
    public void onResults(Bundle bundle)
    {
        if(!isShuttingDownSpeech)
        {
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.i("Recog", "Results recieved: " + matches);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                switch (pingingRecogFor)
                {
                    case pingingRecogFor_Quality:

                        switch (matches.get(0).split(" ")[0])
                        {
                            case "bad":
                                tempclaimedQuality = ("Bad");
                                toSpeech.speak("Quality Recorded as: bad. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                                Log.i("Recog", "Quality Recorded as: bad. What was the number of goods delivered");
                                break;
                            case "subpar":
                                tempclaimedQuality = ("Sub-par");
                                toSpeech.speak("Quality Recorded as: sub par. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                                Log.i("Recog", "Quality Recorded as: sub par. What was the number of goods delivered");
                                break;
                            case "ok":
                                tempclaimedQuality = ("Ok");
                                toSpeech.speak("Quality Recorded as: okay. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                                Log.i("Recog", "Quality Recorded as: ok. What was the number of goods delivered");
                                break;
                            case "good":
                                tempclaimedQuality = ("Good");
                                toSpeech.speak("Quality Recorded as: good. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                                Log.i("Recog", "Quality Recorded as: good. What was the number of goods delivered");
                                break;
                            default:
                                if (tempclaimedQuality != null)
                                {

                                }
                                else
                                {
                                    toSpeech.speak("Unrecognised Quality, please repeat.", TextToSpeech.QUEUE_FLUSH, null, "QualityAsk");
                                    Log.i("Recog", "Unrecognised Quality, please repeat.");
                                }
                                break;
                        }
                        break;

                    case pingingRecogFor_Quantity:
                        try
                        {
                            int quantity = Integer.parseInt(matches.get(0));
                            tempclaimedQuantity = quantity;
                            toSpeech.speak("Quantity Recorded as: " + quantity, TextToSpeech.QUEUE_FLUSH, null, "end");
                            Log.i("Recog", "Quantity Recorded as: " + quantity);
                            claimedIDs.add(tempclaimedID);
                            claimedDates.add(tempclaimedDate);
                            claimedQualities.add(tempclaimedQuality);
                            claimedQuantities.add(tempclaimedQuantity);
                            claimedUsers.add(tempclaimedUser);

                            String details = currentOrderDetails + "\n\n Claimed by: " + userNameEditText.getText().toString() +
                                    "\n\n Reported Quality: " + claimedQualities.get(claimedQualities.size() - 1) +
                                    "\n\n Reported Quantity: " + claimedQuantities.get(claimedQuantities.size() - 1);

                            orderInfoText.setText(details);

                            tempclaimedQuality = null;
                            tempclaimedUser = null;
                            tempclaimedQuantity = null;
                            tempclaimedDate = null;
                            tempclaimedID = null;
                        } catch (Exception e)
                        {
                            Log.e("Recog", "ERROR Exception Parsing quantity: " + e.toString());
                            toSpeech.speak("Response matches no known number, please repeat.", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                            Log.i("Recog", "Response matches no known number, please repeat.");
                        }
                        break;


                    case pingingRecogFor_EditQuality:

                        switch (matches.get(0).split(" ")[0])
                        {
                            case "bad":
                                tempclaimedQuality = ("Bad");
                                toSpeech.speak("Quality Recorded as: bad. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityEdit");
                                Log.i("Recog", "Quality Recorded as: bad. What was the number of goods delivered");
                                break;
                            case "subpar":
                                tempclaimedQuality = ("Sub-par");
                                toSpeech.speak("Quality Recorded as: sub par. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityEdit");
                                Log.i("Recog", "Quality Recorded as: sub par. What was the number of goods delivered");
                                break;
                            case "ok":
                                tempclaimedQuality = ("Ok");
                                toSpeech.speak("Quality Recorded as: okay. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityEdit");
                                Log.i("Recog", "Quality Recorded as: ok. What was the number of goods delivered");
                                break;
                            case "good":
                                tempclaimedQuality = ("Good");
                                toSpeech.speak("Quality Recorded as: good. What was the number of goods delivered", TextToSpeech.QUEUE_FLUSH, null, "QuantityEdit");
                                Log.i("Recog", "Quality Recorded as: good. What was the number of goods delivered");
                                break;
                            default:
                                if (tempclaimedQuality != null)
                                {

                                }
                                else
                                {
                                    toSpeech.speak("Unrecognised Quality, please repeat.", TextToSpeech.QUEUE_FLUSH, null, "QualityEdit");
                                    Log.i("Recog", "Unrecognised Quality, please repeat.");
                                }
                                break;
                        }
                        break;

                    case pingingRecogFor_EditQuantity:
                        try
                        {
                            int quantity = Integer.parseInt(matches.get(0));
                            tempclaimedQuantity = quantity;
                            toSpeech.speak("Quantity Recorded as: " + quantity, TextToSpeech.QUEUE_FLUSH, null, "end");
                            Log.i("Recog", "Quantity Recorded as: " + quantity);
                            //claimedIDs.set(claimToEditIndex, tempclaimedID);
                            //claimedDates.set(claimToEditIndex,tempclaimedDate);
                            claimedQualities.set(claimToEditIndex, tempclaimedQuality);
                            claimedQuantities.set(claimToEditIndex, tempclaimedQuantity);
                            //claimedUsers.set(claimToEditIndex, tempclaimedUser);

                            String details = "Edit Complete: Order " + claimedIDs.get(claimToEditIndex) + " claimed by " + claimedUsers.get(claimToEditIndex) + " at " + claimedDates.get(claimToEditIndex) + "\n\n" +
                                    "\n\n Reported Quality: " + claimedQualities.get(claimToEditIndex) +
                                    "\n\n Reported Quantity: " + claimedQuantities.get(claimToEditIndex);

                            orderInfoText.setText(details);

                            tempclaimedQuality = null;
                            tempclaimedUser = null;
                            tempclaimedQuantity = null;
                            tempclaimedDate = null;
                            tempclaimedID = null;
                        } catch (Exception e)
                        {
                            Log.e("Recog", "ERROR Exception Parsing quantity: " + e.toString());
                            toSpeech.speak("Response matches no known number, please repeat.", TextToSpeech.QUEUE_FLUSH, null, "QuantityAsk");
                            Log.i("Recog", "Response matches no known number, please repeat.");
                        }
                        break;

                    case pingingRecogFor_doEdit:
                        if (matches.get(0).split(" ")[0].matches("yes"))
                        {
                            int anIndex = 0;
                            for (String anID : claimedIDs)
                            {
                                if (anID.matches(currentOrderID))
                                {
                                    claimToEditIndex = anIndex;
                                    break;
                                }
                                anIndex++;
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            {
                                toSpeech.speak("What is the quality of the goods delivered:", TextToSpeech.QUEUE_FLUSH, null, "QualityEdit");
                            }
                        }
                        break;


                    case pingingRecogFor_MealName:
                        currentMealName = matches.get(0);
                        toSpeech.speak("Meal Recorded as " + matches.get(0) + ". How many servings of " + matches.get(0) + " are you making?", TextToSpeech.QUEUE_FLUSH, null, speechID_AskForMealServings);
                        break;

                    case pingingRecogFor_MealServings:
                        try
                        {
                            currentMealServings = Integer.parseInt(matches.get(0).split(" ")[0]);
                            toSpeech.speak("Understood, you are serving " + currentMealServings + " servings of " + currentMealName + ". ", TextToSpeech.QUEUE_FLUSH, null, "");
                            scanningProducts = true;
                        } catch (NumberFormatException e)
                        {
                            toSpeech.speak("Error, that is not a number, please repeat.", TextToSpeech.QUEUE_FLUSH, null, speechID_AskForMealServings);
                        }
                        break;

                    case pingingRecogFor_Weight:
                        try
                        {
                            int weight = Integer.parseInt(matches.get(0).split(" ")[0]);
                            toSpeech.speak("Understood, you are adding " + weight + " kilos of " + currentProduct.getName() + ". Adjusting Serving Nutritional Stats. ", TextToSpeech.QUEUE_FLUSH, null, "");
                            addNutrientsToMeal(currentMealValues, currentMealServings, currentProduct, weight);
                            orderInfoText.setText("1 serving of " + currentMealName + " contains: " + "\n\nSalt: " + currentMealValues.getSalt() + "\n\nFat: " + currentMealValues.getFat() + "\n\nSaturated Fat: " + currentMealValues.getSaturatedFat() + "\n\nSugar: " + currentMealValues.getSugar());
                        } catch (NumberFormatException e)
                        {
                            toSpeech.speak("Error, that is not a number, please repeat.", TextToSpeech.QUEUE_FLUSH, null, speechID_AskForWeight);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle)
    {
        Log.e("Recog", "Partial Result");
    }

    @Override
    public void onEvent(int i, Bundle bundle)
    {
        Log.e("Recog", "onEvent");
    }
//++++++++[/Recognition Listener Code]
//++++++++[Recognition Other Code]
    private String sortThroughRecognizerResults(String[] results, String[] matchablePhrases)
    {
        for (String aResult: results)
        {
            Log.i("Recog", "Sorting results for result: " + aResult);
            for (String aPhrase: matchablePhrases)
            {
                Log.i("Recog", "Sorting results for result: " + aResult + " and Phrase: " + aPhrase);
                if(aResult.matches(aPhrase))
                {
                    Log.i("Recog", "Match Found");
                    return aPhrase;
                }
            }
        }
        Log.i("Recog", "No matches found, returning empty string \"\" .");
        return "";
    }

    private String sortThroughRecognizerResults(String[] results, String matchablePhrase)
    {
        for (String aResult: results)
        {
            Log.i("Recog", "Sorting results for result: " + aResult + " and Phrase: " + matchablePhrase);
            if(aResult.matches(matchablePhrase))
            {
                Log.i("Recog", "Match Found");
                return matchablePhrase;
            }
        }
        Log.i("Recog", "No matches found, returning empty string \"\" .");
        return "";
    }
//++++++++[/Recognition Other Code]

//++++++++[Text To Speech Code]
    private void setupTextToSpeech()
    {
        isShuttingDownSpeech = false;

        toSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status)
            {
                Log.i("Text To Speech Update", "onInit Complete");
                toSpeech.setLanguage(Locale.ENGLISH);
                endOfSpeakIndentifier = new HashMap();
                endOfSpeakIndentifier.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "endOfSpeech");
                toSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                {
                    @Override
                    public void onStart(String utteranceId)
                    {
                        Log.i("Text To Speech Update", "onStart called");
                    }

                    @Override
                    public void onDone(final String utteranceId)
                    {
                        if(!isShuttingDownSpeech)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    switch (utteranceId)
                                    {
                                        case "QualityAsk":
                                            pingingRecogFor = pingingRecogFor_Quality;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case "QuantityAsk":
                                            pingingRecogFor = pingingRecogFor_Quantity;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case "QualityEdit":
                                            pingingRecogFor = pingingRecogFor_EditQuality;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case "QuantityEdit":
                                            pingingRecogFor = pingingRecogFor_EditQuantity;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case "DoEdit":
                                            pingingRecogFor = pingingRecogFor_doEdit;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case "repeat":
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case speechID_AskForWeight:
                                            pingingRecogFor = pingingRecogFor_Weight;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case speechID_AskForMealName:
                                            pingingRecogFor = pingingRecogFor_MealName;
                                            recognizer.startListening(recogIntent);
                                            break;
                                        case speechID_AskForMealServings:
                                            pingingRecogFor = pingingRecogFor_MealServings;
                                            recognizer.startListening(recogIntent);
                                            break;
                                    }
                                }
                            });
                        }

                        /*if(utteranceId.matches("QualityAsk") || utteranceId.matches("QuantityAsk"))
                        {
                            pingingRecogFor = pingingRecogFor_Quality
                            recognizer.startListening(recogIntent);
                        }
                        else
                        {
                            recognizer.startListening(recogIntent);
                        }*/
                        //toSpeech.shutdown();
                    }

                    @Override
                    public void onError(String utteranceId)
                    {
                        Log.i("Text To Speech Update", "ERROR DETECTED");
                    }
                });
            }
        });
    }
//++++++++[/Text To Speech Code]

}




/*

 */