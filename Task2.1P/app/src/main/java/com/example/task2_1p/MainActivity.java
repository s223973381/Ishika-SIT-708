package com.example.task2_1p;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerCategory, spinnerFrom, spinnerTo;
    EditText etInput;
    Button btnConvert;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        etInput = findViewById(R.id.etInput);
        btnConvert = findViewById(R.id.btnConvert);
        tvResult = findViewById(R.id.tvResult);

        setupCategorySpinner();

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performConversion();
            }
        });
    }

    private void setupCategorySpinner() {
        String[] categories = getResources().getStringArray(R.array.category_array);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                updateUnitSpinners(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateUnitSpinners(String category) {
        String[] units;

        switch (category) {
            case "Currency":
                units = getResources().getStringArray(R.array.currency_array);
                break;
            case "Fuel":
                units = getResources().getStringArray(R.array.fuel_array);
                break;
            case "Temperature":
                units = getResources().getStringArray(R.array.temperature_array);
                break;
            default:
                units = new String[]{};
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
    }

    private void performConversion() {
        String inputText = etInput.getText().toString().trim();

        if (inputText.isEmpty()) {
            etInput.setError("Please enter a value");
            Toast.makeText(this, "Input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(inputText);
        } catch (NumberFormatException e) {
            etInput.setError("Please enter a valid number");
            Toast.makeText(this, "Invalid numeric input", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spinnerCategory.getSelectedItem().toString();
        String fromUnit = spinnerFrom.getSelectedItem().toString();
        String toUnit = spinnerTo.getSelectedItem().toString();

        if (fromUnit.equals(toUnit)) {
            tvResult.setText("Same unit selected. Result: " + inputValue);
            Toast.makeText(this, "Identity conversion selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Fuel") && inputValue < 0) {
            etInput.setError("Negative value not allowed for fuel conversion");
            Toast.makeText(this, "Fuel values cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        Double result = null;

        switch (category) {
            case "Currency":
                result = convertCurrency(fromUnit, toUnit, inputValue);
                break;
            case "Fuel":
                result = convertFuel(fromUnit, toUnit, inputValue);
                break;
            case "Temperature":
                result = convertTemperature(fromUnit, toUnit, inputValue);
                break;
        }

        if (result == null) {
            tvResult.setText("Conversion not supported");
        } else {
            tvResult.setText("Result: " + String.format("%.2f", result));
        }
    }

    private double convertCurrency(String from, String to, double value) {
        double usdValue;

        switch (from) {
            case "USD":
                usdValue = value;
                break;
            case "AUD":
                usdValue = value / 1.55;
                break;
            case "EUR":
                usdValue = value / 0.92;
                break;
            case "JPY":
                usdValue = value / 148.50;
                break;
            case "GBP":
                usdValue = value / 0.78;
                break;
            default:
                usdValue = value;
                break;
        }

        switch (to) {
            case "USD":
                return usdValue;
            case "AUD":
                return usdValue * 1.55;
            case "EUR":
                return usdValue * 0.92;
            case "JPY":
                return usdValue * 148.50;
            case "GBP":
                return usdValue * 0.78;
            default:
                return usdValue;
        }
    }

    private Double convertFuel(String from, String to, double value) {
        if (from.equals("mpg") && to.equals("km/L")) {
            return value * 0.425;
        } else if (from.equals("km/L") && to.equals("mpg")) {
            return value / 0.425;
        } else if (from.equals("Gallon") && to.equals("Liter")) {
            return value * 3.785;
        } else if (from.equals("Liter") && to.equals("Gallon")) {
            return value / 3.785;
        } else if (from.equals("Nautical Mile") && to.equals("Kilometer")) {
            return value * 1.852;
        } else if (from.equals("Kilometer") && to.equals("Nautical Mile")) {
            return value / 1.852;
        } else {
            return null;
        }
    }

    private Double convertTemperature(String from, String to, double value) {
        if (from.equals("Celsius") && to.equals("Fahrenheit")) {
            return (value * 1.8) + 32;
        } else if (from.equals("Fahrenheit") && to.equals("Celsius")) {
            return (value - 32) / 1.8;
        } else if (from.equals("Celsius") && to.equals("Kelvin")) {
            return value + 273.15;
        } else if (from.equals("Kelvin") && to.equals("Celsius")) {
            return value - 273.15;
        } else {
            return null;
        }
    }
}