package com.example.carder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

public class WriteActivity extends AppCompatActivity {

    // Key constants for Intent Extras
    public static final String KEY_NAME = "EXTRA_NAME";
    public static final String KEY_ROLE = "EXTRA_ROLE";
    public static final String KEY_PHONE = "EXTRA_PHONE";
    public static final String KEY_WEBSITE = "EXTRA_WEBSITE";
    public static final String KEY_KEY = "EXTRA_KEY";

    private static final String CHANNEL_ID = "card_created_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private String editKey = null;
    private EditText etFullName;
    private EditText etRole;
    private EditText etPhone;
    private EditText etWebsite;
    private Button btnCreateCard;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNotificationChannel();
        requestNotificationPermission();

        etFullName = findViewById(R.id.etFullName);
        etRole = findViewById(R.id.etRole);
        etPhone = findViewById(R.id.etPhone);
        etWebsite = findViewById(R.id.etWebsite);
        btnCreateCard = findViewById(R.id.btnCreateCard);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentEdit = getIntent();
        if (intentEdit != null && intentEdit.hasExtra(KEY_NAME)) {
            etFullName.setText(intentEdit.getStringExtra(KEY_NAME));
            etRole.setText(intentEdit.getStringExtra(KEY_ROLE));
            etPhone.setText(intentEdit.getStringExtra(KEY_PHONE));
            etWebsite.setText(intentEdit.getStringExtra(KEY_WEBSITE));
            editKey = intentEdit.getStringExtra(KEY_KEY);

            if (editKey != null)
                btnCreateCard.setText("Update");
        }

        btnCreateCard.setOnLongClickListener(v -> {
            etFullName.setText("Mr Dirt");
            etRole.setText("Factory Cleaner");
            etPhone.setText("056743890");
            etWebsite.setText("igotdirty.com");
            return true;
        });

        btnCreateCard.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        String name = etFullName.getText().toString().trim();
        String role = etRole.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String website = etWebsite.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            etFullName.setError("Please enter a name");
            isValid = false;
        } else if (name.matches(".*\\d.*")) {
            etFullName.setError("Name cannot contain numbers");
            isValid = false;
        } else if (!name.contains(" ")) {
            etFullName.setError("Please enter both first and last name");
            isValid = false;
        }

        if (role.isEmpty()) {
            etRole.setError("Please enter a role");
            isValid = false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Please enter a phone number");
            isValid = false;
        }

        if (website.isEmpty()) {
            etWebsite.setError("Please enter a website");
            isValid = false;
        }

        if (!isValid) {
            return;
        }
        if (!FirebaseConfig.isConfigured()) {
            Toast.makeText(this, "Values Left In FirebaseConfig.java", Toast.LENGTH_LONG).show();
            return;
        }

        final Card card = new Card(name, role, phone, website);
        DatabaseReference ref = CardsDb.cards(this);

        if (editKey == null) {
            String key = ref.push().getKey();
            card.setKey(key);

            ref.child(key).setValue(card)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(WriteActivity.this, "Card saved successfully", Toast.LENGTH_SHORT).show();
                            showCardNotification(card);
                            clearETs();
                            openCard(card);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(WriteActivity.this, "Failed to save card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            card.setKey(editKey);
            ref.child(editKey).setValue(card)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(WriteActivity.this, "Card updated successfully", Toast.LENGTH_SHORT).show();
                        clearETs();
                        openCard(card);
                    })
                    .addOnFailureListener(e -> Toast.makeText(WriteActivity.this, "Failed to update card: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void showCardNotification(Card card) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra(KEY_NAME, card.getName());
        intent.putExtra(KEY_ROLE, card.getRole());
        intent.putExtra(KEY_PHONE, card.getPhone());
        intent.putExtra(KEY_WEBSITE, card.getWebsite());
        intent.putExtra(KEY_KEY, card.getKey());

        int requestCode;
        if (card.getKey() != null) {
            requestCode = card.getKey().hashCode();
        } else {
            requestCode = (int) System.currentTimeMillis();
        }

        //WriteActivity->CardActivity
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, WriteActivity.class));
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                requestCode,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.plus_circle)
                .setContentTitle("New Business Card Created")
                .setContentText(card.getName() + " - " + card.getRole())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(requestCode, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Card Notifications";
            String description = "Notifications when a new business card is created";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_example) {
            etFullName.setText("Angel Williams");
            etRole.setText("Zoo Critic");
            etPhone.setText("0573348167");
            etWebsite.setText("thebestzooyouthink.com");
            return true;
        } else if (id == R.id.action_reset) {
            boolean isAllEmpty = etFullName.getText().toString().trim().isEmpty()
                    && etRole.getText().toString().trim().isEmpty()
                    && etPhone.getText().toString().trim().isEmpty()
                    && etWebsite.getText().toString().trim().isEmpty();

            if (isAllEmpty) {
                Toast.makeText(this, "Fields are already empty", Toast.LENGTH_SHORT).show();
            } else {
                etFullName.setText("");
                etRole.setText("");
                etPhone.setText("");
                etWebsite.setText("");
            }
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(WriteActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_list) {
            Intent intent = new Intent(WriteActivity.this, ListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCard(Card card) {
        Intent intent = new Intent(WriteActivity.this, CardActivity.class);
        intent.putExtra(KEY_NAME, card.getName());
        intent.putExtra(KEY_ROLE, card.getRole());
        intent.putExtra(KEY_PHONE, card.getPhone());
        intent.putExtra(KEY_WEBSITE, card.getWebsite());
        intent.putExtra(KEY_KEY, card.getKey());
        startActivity(intent);
    }

    private void clearETs() {
        etFullName.setText("");
        etRole.setText("");
        etPhone.setText("");
        etWebsite.setText("");

        etFullName.setError(null);
        etRole.setError(null);
        etPhone.setError(null);
        etWebsite.setError(null);

        editKey = null;
        btnCreateCard.setText("Create");
    }
}