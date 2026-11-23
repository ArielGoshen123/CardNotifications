package com.example.carder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CardActivity extends AppCompatActivity {

    private TextView tvCardName;
    private TextView tvCardRole;
    private TextView tvCardPhone;
    private TextView tvCardWebsite;
    private String name = "";
    private String role = "";
    private String phone = "";
    private String website = "";
    private String key = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card_menu, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_card);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCardName = findViewById(R.id.tvCardName);
        tvCardRole = findViewById(R.id.tvCardRole);
        tvCardPhone = findViewById(R.id.tvCardPhone);
        tvCardWebsite = findViewById(R.id.tvCardWebsite);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get extras from MainActivity using static key constants
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra(MainActivity.KEY_NAME);
            role = intent.getStringExtra(MainActivity.KEY_ROLE);
            phone = intent.getStringExtra(MainActivity.KEY_PHONE);
            website = intent.getStringExtra(MainActivity.KEY_WEBSITE);
            key = intent.getStringExtra(MainActivity.KEY_KEY);
        }

        tvCardName.setText(name);
        tvCardRole.setText(role);
        tvCardPhone.setText("📞 " + phone);
        tvCardWebsite.setText("🌐 " + website);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dial) {
            if (phone != null && !phone.isEmpty()) {
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phone));
                    startActivity(dialIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CardActivity.this, "No app available to place phone calls", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CardActivity.this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_share) {
            String shareBody = "Business Card:\n" +
                    "Name: " + name + "\n" +
                    "Role: " + role + "\n" +
                    "Phone: " + phone + "\n" +
                    "Website: " + website;

            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(shareIntent, "Share Business Card Via"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(CardActivity.this, "No app available to share content", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_website) {
            if (website != null && !website.isEmpty()) {
                String formattedUrl = website;
                if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                    formattedUrl = "https://" + formattedUrl;
                }
                try {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl));
                    startActivity(webIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CardActivity.this, "No web browser available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CardActivity.this, "No website URL available", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (id == R.id.action_edit) {
            Intent intent = new Intent(CardActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.KEY_NAME, name);
            intent.putExtra(MainActivity.KEY_ROLE, role);
            intent.putExtra(MainActivity.KEY_PHONE, phone);
            intent.putExtra(MainActivity.KEY_WEBSITE, website);
            intent.putExtra(MainActivity.KEY_KEY, key); // <-- MODIFIED: Passing key back to MainActivity
            startActivity(intent);
            finish();
            return true;
        }
        else if (id == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}