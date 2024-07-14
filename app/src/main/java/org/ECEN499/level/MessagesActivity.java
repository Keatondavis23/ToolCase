package org.ECEN499.level;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MessagesActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        String[] messages = new String[] { "Connecting to Case", "Displaying Data", "Using Level", "Using Thermometer", "Using Range Finder" };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages));
        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_laser:
                    startActivity(new Intent(MessagesActivity.this, Laser.class));
                    return true;
                case R.id.nav_thermometer:
                    startActivity(new Intent(MessagesActivity.this, Thermometer.class));
                    return true;
                case R.id.nav_range_finder:
                    startActivity(new Intent(MessagesActivity.this, RangeFinder.class));
                    return true;
                case R.id.nav_menu:
                    startActivity(new Intent(MessagesActivity.this, HomeActivity.class));
                    return true;
            }
            return false;
        });


    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        if ("Connecting to Case".equals(item)) {
            showConnectingToCaseInstructions();
        }
        else if ("Displaying Data".equals(item)) {
            showDisplayingDataInstructions();
        }
        else if ("Using Level".equals(item)) {
            showUsingLevelInstructions();
        }
        else if ("Using Thermometer".equals(item)) {
            showUsingThermometerInstructions();
        }
        else if ("Using Range Finder".equals(item)) {
            showUsingRangeFinderInstructions();
        }
        else {
            Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        }
    }

    private void showConnectingToCaseInstructions() {
        Spanned ConnectingToCaseText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ConnectingToCaseText = Html.fromHtml(getString(R.string.connecting_to_case_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            ConnectingToCaseText = Html.fromHtml(getString(R.string.connecting_to_case_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Connecting To Case Instructions")
                .setMessage(ConnectingToCaseText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showDisplayingDataInstructions() {
        Spanned displayingDataText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            displayingDataText = Html.fromHtml(getString(R.string.displaying_data_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            displayingDataText = Html.fromHtml(getString(R.string.displaying_data_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Connecting To Case Instructions")
                .setMessage(displayingDataText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showUsingLevelInstructions() {
        Spanned UsingLevelText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            UsingLevelText = Html.fromHtml(getString(R.string.using_level_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            UsingLevelText = Html.fromHtml(getString(R.string.using_level_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Level Instructions")
                .setMessage(UsingLevelText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showUsingThermometerInstructions() {
        Spanned UsingThermometerText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            UsingThermometerText = Html.fromHtml(getString(R.string.using_thermometer_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            UsingThermometerText = Html.fromHtml(getString(R.string.using_thermometer_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Thermometer Instructions")
                .setMessage(UsingThermometerText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showUsingRangeFinderInstructions() {
        Spanned UsingRangeFinderText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            UsingRangeFinderText = Html.fromHtml(getString(R.string.using_range_finder_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            UsingRangeFinderText = Html.fromHtml(getString(R.string.using_range_finder_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Range Finder Instructions")
                .setMessage(UsingRangeFinderText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
