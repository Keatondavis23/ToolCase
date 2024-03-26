// java/org/woheller69/level/MessagesActivity.java
package org.woheller69.level;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.text.Html;

public class MessagesActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        String[] messages = new String[] { "Connecting to Case", "Displaying Data", "Troubleshooting", "Message 4" };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        if ("Troubleshooting".equals(item)) {
            showTroubleshootingInstructions();
        } else {
            Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        }
    }

    private void showTroubleshootingInstructions() {
        Spanned troubleshootingText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            troubleshootingText = Html.fromHtml(getString(R.string.troubleshooting_instructions), Html.FROM_HTML_MODE_COMPACT);
        } else {
            troubleshootingText = Html.fromHtml(getString(R.string.troubleshooting_instructions));
        }

        new AlertDialog.Builder(this)
                .setTitle("Troubleshooting Instructions")
                .setMessage(troubleshootingText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
