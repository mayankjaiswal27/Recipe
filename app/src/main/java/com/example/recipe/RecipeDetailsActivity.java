package com.example.recipe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecipeDetailsActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;
    ListView listView;
    Button clearDatabaseButton;
    Button backButton;
    ArrayAdapter<String> adapter;
    ArrayList<String> recipeNames;

    private static final int EDIT_RECIPE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        recipeDatabase = openOrCreateDatabase("recipeDatabase", MODE_PRIVATE, null);
        listView = findViewById(R.id.listView);
        clearDatabaseButton = findViewById(R.id.clearDatabaseButton);
        backButton = findViewById(R.id.backButton);

        recipeNames = new ArrayList<>();

        // Fetch all recipe names from the database
        fetchRecipeNames();

        // Populate ListView with recipe names
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeNames);
        listView.setAdapter(adapter);

        // Set item click listener on ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Fetch instructions and ingredients for the clicked recipe
                String recipeName = (String) parent.getItemAtPosition(position);
                Cursor cursor = recipeDatabase.rawQuery("SELECT * FROM recipes WHERE name=?", new String[]{recipeName});
                if (cursor.moveToFirst()) {
                    String ingredients = cursor.getString(cursor.getColumnIndex("ingredients"));
                    String instructions = cursor.getString(cursor.getColumnIndex("instructions"));
                    // Display ingredients and instructions in toast message
                    String message = "Ingredients: " + ingredients + "\n" + "Instructions: " + instructions;
                    Toast.makeText(RecipeDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
                cursor.close();

                // Handle edit action
                // Pass the recipe name to the EditRecipeActivity for editing
                Intent intent = new Intent(RecipeDetailsActivity.this, EditRecipeActivity.class);
                intent.putExtra("recipeName", recipeName);
                startActivityForResult(intent, EDIT_RECIPE_REQUEST_CODE);
            }
        });

        // Set click listener on Clear Database button
        clearDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDatabase();
                // Clear the ListView
                recipeNames.clear();
                adapter.notifyDataSetChanged();
            }
        });

        // Set click listener on Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Fetch all recipe names from the database and populate recipeNames ArrayList
    private void fetchRecipeNames() {
        Cursor cursor = recipeDatabase.rawQuery("SELECT name FROM recipes", null);
        if (cursor.moveToFirst()) {
            do {
                recipeNames.add(cursor.getString(cursor.getColumnIndex("name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Method to clear the database
    private void clearDatabase() {
        recipeDatabase.execSQL("DELETE FROM recipes");
        Toast.makeText(this, "Database cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Override back button to go to the previous activity
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_RECIPE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            boolean recipeUpdated = data.getBooleanExtra("recipeUpdated", false);
            boolean recipeDeleted = data.getBooleanExtra("recipeDeleted", false);
            if (recipeUpdated || recipeDeleted) {
                // Recipe was either updated or deleted, so refresh the list
                recipeNames.clear();
                fetchRecipeNames();
                adapter.notifyDataSetChanged();
            }
        }
    }
}
