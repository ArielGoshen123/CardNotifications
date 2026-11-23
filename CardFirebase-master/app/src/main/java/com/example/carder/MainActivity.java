package com.example.carder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends AppCompatActivity {

    //Key constants for Intent Extras
    public static final String KEY_NAME = "EXTRA_NAME";
    public static final String KEY_ROLE = "EXTRA_ROLE";
    public static final String KEY_PHONE = "EXTRA_PHONE";
    public static final String KEY_WEBSITE = "EXTRA_WEBSITE";
    public static final String KEY_KEY = "EXTRA_KEY"; // <-- MODIFIED: Added database key constant

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

        //CardsDb.cards(this).child("test_connection").setValue("Hello Firebase");

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
            editKey = intentEdit.getStringExtra(KEY_KEY); // <-- MODIFIED: Directly read editKey in onCreate

            if (editKey != null)
                btnCreateCard.setText("Update");
        }


        //Secret data fill
        btnCreateCard.setOnLongClickListener(v -> {
            etFullName.setText("Mr Dirt");
            etRole.setText("Factory Cleaner");
            etPhone.setText("056743890");
            etWebsite.setText("igotdirty.com");
            return true;
        });

        btnCreateCard.setOnClickListener(v -> {
            saveCard();
        });

    }
    private void saveCard() {
        String name  = etFullName.getText().toString().trim();
        String role   = etRole.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String website  = etWebsite.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            etFullName.setError("Please enter a name");
            isValid = false;
        } else if (name.matches(".*\\d.*")) { //Checks for a digits
            etFullName.setError("Name cannot contain numbers");
            isValid = false;
        } else if (!name.contains(" ")) {
            etFullName.setError("Please enter both first and last name");
            isValid = false;
        }

        //Validate Role
        if (role.isEmpty()) {
            etRole.setError("Please enter a role");
            isValid = false;
        }

        //Validate Phone
        if (phone.isEmpty()) {
            etPhone.setError("Please enter a phone number");
            isValid = false;
        }

        //Validate Website
        if (website.isEmpty()) {
            etWebsite.setError("Please enter a website");
            isValid = false;
        }

        //Stop execution if any field is invalid
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
            //Generate a new unique push key from the Firebase Database reference
            String key = ref.push().getKey();
            card.setKey(key); // Assumes setter exists: setKey(key) or setId(key)

            //Save the card object under the newly generated key child
            ref.child(key).setValue(card)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MainActivity.this, "Card saved successfully", Toast.LENGTH_SHORT).show();
                            clearETs();
                            openCard(card);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to save card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // ===== עדכון (Update) ===== // <-- MODIFIED: Standard update logic using editKey
            card.setKey(editKey);
            ref.child(editKey).setValue(card)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(MainActivity.this, "Card updated successfully", Toast.LENGTH_SHORT).show();
                        clearETs();
                        openCard(card);
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update card: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_list) {
            Intent intent = new Intent(MainActivity.this, ListActivity.class); // <-- MODIFIED: Pointed to CardsActivity
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCard(Card card) {
        Intent intent = new Intent(MainActivity.this, CardActivity.class);
        intent.putExtra(KEY_NAME, card.getName());
        intent.putExtra(KEY_ROLE, card.getRole());
        intent.putExtra(KEY_PHONE, card.getPhone());
        intent.putExtra(KEY_WEBSITE, card.getWebsite());
        intent.putExtra(KEY_KEY, card.getKey()); // <-- MODIFIED: Passing database key to CardActivity
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