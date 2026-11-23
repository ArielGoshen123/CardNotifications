package com.example.carder;

import com.google.firebase.database.Exclude;

public class Card {

    // ----- 4 השדות שנשמרים בענן -----
    private String name;
    private String role;
    private String phone;
    private String website;

    // ----- הכתובת של הכרטיס בעץ. לא נשמרת כשדה! -----
    private String key;

    // בנאי ריק: חובה! Firebase בונה דרכו את העצם כשהוא קורא מהמסד
    public Card() {
    }

    // בנאי נוח: בשבילנו, כשאנחנו יוצרים כרטיס מהטופס
    public Card(String name, String role, String phone, String website) {
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.website = website;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    @Exclude
    public String getKey() { return key; }

    @Exclude
    public void setKey(String key) { this.key = key; }

    @Override
    public String toString() {
        return name + " · " + role;
    }

}
