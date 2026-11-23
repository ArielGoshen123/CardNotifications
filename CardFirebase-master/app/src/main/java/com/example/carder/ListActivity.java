package com.example.carder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private ListView lvCards;
    private TextView tvCount;

    private ArrayList<Card> cards = new ArrayList<>();
    private ArrayAdapter<Card> adapter;

    private DatabaseReference ref;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("My Cards");

        lvCards = findViewById(R.id.lvCards);
        tvCount = findViewById(R.id.tvCount);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cards);
        lvCards.setAdapter(adapter);

        if (!FirebaseConfig.isConfigured()) {
            tvCount.setText("Values Left In FirebaseConfig.java");
            return;
        }
        ref = CardsDb.cards(this);

        // ===== קריאה (Read) =====
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO 4-6
                cards.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Card c = child.getValue(Card.class);
                    if (c != null) {
                        c.setKey(child.getKey());
                        cards.add(c);
                    }
                }
                adapter.notifyDataSetChanged();
                if (cards.size() == 1)
                    tvCount.setText("You currently have one card");
                else if (cards.size() == 0)
                    tvCount.setText("Create your first card!");
                else
                    tvCount.setText("You currently have " + cards.size() + " cards");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListActivity.this,
                        "Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        ref.addValueEventListener(listener);

        lvCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Card c = cards.get(position);
                Intent intent = new Intent(ListActivity.this, CardActivity.class);
                intent.putExtra(MainActivity.KEY_NAME, c.getName());
                intent.putExtra(MainActivity.KEY_ROLE, c.getRole());
                intent.putExtra(MainActivity.KEY_PHONE, c.getPhone());
                intent.putExtra(MainActivity.KEY_WEBSITE, c.getWebsite());
                intent.putExtra(MainActivity.KEY_KEY, c.getKey());
                startActivity(intent);
            }
        });

        lvCards.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                askDelete(cards.get(position));
                return true;
            }
        });
    }

    private void askDelete(final Card c) {
        new AlertDialog.Builder(this)
                .setTitle("Card Deletion")
                .setMessage("Delete " + c.getName() + "'s Card?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ref.child(c.getKey()).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref != null && listener != null) {
            ref.removeEventListener(listener);
        }
    }
}