package com.example.echoenglish_mobile.view.activity.chatbot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationCategoriesActivity extends AppCompatActivity {

    private List<ConversationCategory> categories;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        initData();
        setupRecyclerViews();
    }

    // --- Data Initialization
    private void initData() {
        categories = new ArrayList<>();

        // --- Daily Conversation ---
        List<ConversationScenario> dailyScenarios = new ArrayList<>();
        dailyScenarios.add(new ConversationScenario("daily_coffee_shop", "Coffee Shop Chat", "Practice ordering coffee and casual conversation", "ic_coffee", "Beginner", "5-10 min"));
        dailyScenarios.add(new ConversationScenario("daily_restaurant", "Restaurant Ordering", "Practice ordering food and interacting with staff", "ic_restaurant", "Beginner", "5-10 min"));
        dailyScenarios.add(new ConversationScenario("daily_shopping", "Shopping Conversation", "Practice shopping-related inquiries", "ic_shopping", "Intermediate", "10-15 min"));
        dailyScenarios.add(new ConversationScenario("daily_transportation", "Public Transport", "Practice asking directions and using transport", "ic_transport", "Beginner", "5-10 min"));
        categories.add(new ConversationCategory("Daily Conversation", "#3B82F6", dailyScenarios));

        // --- Office Conversation ---
        List<ConversationScenario> officeScenarios = new ArrayList<>();
        officeScenarios.add(new ConversationScenario("office_meeting", "Team Meeting", "Practice participating in team meetings", "ic_meeting", "Intermediate", "10-15 min"));
        officeScenarios.add(new ConversationScenario("office_presentation", "Presentation Skills", "Practice delivering presentations", "ic_presentation", "Advanced", "15-20 min"));
        officeScenarios.add(new ConversationScenario("office_negotiation", "Negotiation Practice", "Practice negotiation skills", "ic_negotiation", "Advanced", "15-20 min"));
        officeScenarios.add(new ConversationScenario("office_interview", "Job Interview", "Practice answering interview questions", "ic_interview", "Intermediate", "10-15 min"));
        categories.add(new ConversationCategory("Office Conversation", "#10B981", officeScenarios));

        // --- Technical Discussion ---
        List<ConversationScenario> technicalScenarios = new ArrayList<>();
        technicalScenarios.add(new ConversationScenario("tech_code_review", "Code Review", "Practice discussing code issues", "ic_code", "Advanced", "15-20 min"));
        technicalScenarios.add(new ConversationScenario("tech_architecture", "Architecture Planning", "Discuss system architecture", "ic_architecture", "Advanced", "15-20 min"));
        technicalScenarios.add(new ConversationScenario("tech_debugging", "Debugging Session", "Practice collaborating on debugging", "ic_debug", "Intermediate", "10-15 min"));
        categories.add(new ConversationCategory("Technical Discussion", "#F59E0B", technicalScenarios));

        // --- Language Practice ---
        List<ConversationScenario> languageScenarios = new ArrayList<>();
        languageScenarios.add(new ConversationScenario("lang_english", "English Practice", "Practice everyday English", "ic_english", "Beginner", "5-10 min"));
        languageScenarios.add(new ConversationScenario("lang_spanish", "Spanish Practice", "Practice basic Spanish", "ic_spanish", "Intermediate", "10-15 min"));
        languageScenarios.add(new ConversationScenario("lang_japanese", "Japanese Practice", "Learn essential Japanese", "ic_japanese", "Beginner", "5-10 min"));
        languageScenarios.add(new ConversationScenario("lang_french", "French Practice", "Practice conversational French", "ic_french", "Intermediate", "10-15 min"));
        categories.add(new ConversationCategory("Language Practice", "#EC4899", languageScenarios));
    }

    // --- Setup RecyclerViews
    private void setupRecyclerViews() {
        if (categories.size() < 4) {
            Log.e("CategoriesActivity", "Insufficient categories initialized!");
            Toast.makeText(this, "Error loading conversation categories.", Toast.LENGTH_SHORT).show();
            return;
        }

        RecyclerView rvDaily = findViewById(R.id.rvDailyConversation);
        TextView tvDailyTitle = findViewById(R.id.tvDailyTitle);
        setupCategorySection(rvDaily, tvDailyTitle, categories.get(0));

        RecyclerView rvOffice = findViewById(R.id.rvOfficeConversation);
        TextView tvOfficeTitle = findViewById(R.id.tvOfficeTitle);
        setupCategorySection(rvOffice, tvOfficeTitle, categories.get(1));

        RecyclerView rvTechnical = findViewById(R.id.rvTechnicalDiscussion);
        TextView tvTechnicalTitle = findViewById(R.id.tvTechnicalTitle);
        setupCategorySection(rvTechnical, tvTechnicalTitle, categories.get(2));

        RecyclerView rvLanguage = findViewById(R.id.rvLanguagePractice);
        TextView tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        setupCategorySection(rvLanguage, tvLanguageTitle, categories.get(3));
    }

    // --- Helper method to setup a category section
    private void setupCategorySection(RecyclerView recyclerView, TextView titleView, ConversationCategory category) {
        titleView.setText(category.getTitle());
        int categoryColor = Color.parseColor(category.getColorHex());
        titleView.setTextColor(categoryColor);

        try {
            ViewGroup parentLayout = (ViewGroup) titleView.getParent();
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                View child = parentLayout.getChildAt(i);
                if (child instanceof TextView && child.getId() != titleView.getId()) {
                    ((TextView) child).setTextColor(categoryColor);
                    break;
                }
            }
        } catch (Exception e) {
            Log.w("CategoriesActivity", "Could not set 'View all' color for: " + category.getTitle());
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ConversationAdapter adapter = new ConversationAdapter(this, category.getScenarios(), category.getColorHex(),
                scenario -> launchScenarioActivity(scenario)); // Use the updated launch method
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    // --- Launch the Conversation Activity with Context and JSON ---
    private void launchScenarioActivity(ConversationScenario scenario) {
        Intent intent = new Intent(this, ConversationActivity.class);

        // 1. Pass the context (e.g., scenario title)
        String context = scenario.getTitle();
        intent.putExtra(ConversationActivity.EXTRA_CONTEXT, context);

        // 2. Get the initial JSON for this specific scenario
        String initialJson = getInitialJsonForScenario(scenario.getId());

        // 3. Pass the JSON string IF it exists
        if (initialJson != null && !initialJson.isEmpty()) {
            intent.putExtra(ConversationActivity.EXTRA_START_CONVERSATION_JSON, initialJson);
            Log.d("CategoriesActivity", "Launching scenario: " + context + " with initial JSON.");
        } else {
            // If no specific JSON is found, ConversationActivity will just use the context
            // and call startConversation API itself.
            Log.d("CategoriesActivity", "Launching scenario: " + context + " (context only, no initial JSON found).");
        }

        startActivity(intent);
    }

    // --- NEW HELPER: Generate Fake Initial JSON based on Scenario ID ---
    private String getInitialJsonForScenario(String scenarioId) {
        // Using multi-line String literals (Java 15+) for readability.
        // If using older Java, escape quotes (\") and newlines (\n).
        switch (scenarioId) {
            case "daily_coffee_shop":
                return """
                {
                  "aiResponse": "Welcome to our coffee shop! What can I get started for you today? Feel free to ask about our specials.",
                  "updatedChecklist": [
                    {"id": "order_drink", "description": "Order a beverage.", "completed": false},
                    {"id": "ask_pastries", "description": "Ask about available pastries or snacks.", "completed": false},
                    {"id": "make_small_talk", "description": "Engage in brief small talk (optional).", "completed": false},
                    {"id": "pay_order", "description": "Complete the payment process.", "completed": false}
                  ],
                  "allTasksCompleted": false
                }
                """;
            case "daily_restaurant":
                return """
                {
                  "aiResponse": "Good evening! Welcome. Table for how many? Here are the menus. Can I get you started with some drinks?",
                  "updatedChecklist": [
                    {"id": "request_table", "description": "Request a table.", "completed": false},
                    {"id": "order_drinks", "description": "Order drinks.", "completed": false},
                    {"id": "order_food", "description": "Order main courses/appetizers.", "completed": false},
                    {"id": "ask_recommendation", "description": "Ask for a recommendation (optional).", "completed": false},
                    {"id": "request_bill", "description": "Ask for the bill.", "completed": false}
                  ],
                  "allTasksCompleted": false
                }
                """;
            case "daily_shopping":
                return """
                 {
                   "aiResponse": "Hi there, welcome! Can I help you find anything specific today, or are you just browsing?",
                   "updatedChecklist": [
                     {"id": "greet_staff", "description": "Greet the staff.", "completed": false},
                     {"id": "ask_for_item", "description": "Ask for a specific item or section.", "completed": false},
                     {"id": "ask_about_size_color", "description": "Inquire about size, color, or availability.", "completed": false},
                     {"id": "try_on_item", "description": "Mention trying something on (if applicable).", "completed": false},
                     {"id": "proceed_to_checkout", "description": "Indicate readiness to purchase.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            case "daily_transportation":
                return """
                 {
                   "aiResponse": "Hello! How can I help you with your travel today? Are you looking for directions or information on tickets?",
                   "updatedChecklist": [
                     {"id": "ask_for_destination", "description": "Ask how to get to a specific place.", "completed": false},
                     {"id": "inquire_route_number", "description": "Ask about a specific bus/train route.", "completed": false},
                     {"id": "ask_ticket_price", "description": "Inquire about the ticket price or pass.", "completed": false},
                     {"id": "confirm_platform_stop", "description": "Confirm the correct platform or stop.", "completed": false},
                     {"id": "thank_staff", "description": "Thank the staff for help.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            case "office_meeting":
                return """
                {
                  "aiResponse": "Alright team, let's begin the weekly sync. First up, project updates. Who wants to start?",
                  "updatedChecklist": [
                    {"id": "provide_update", "description": "Provide an update on your tasks.", "completed": false},
                    {"id": "ask_clarifying_question", "description": "Ask a question about someone else's update.", "completed": false},
                    {"id": "state_opinion", "description": "State your opinion on a discussion point.", "completed": false},
                    {"id": "volunteer_task", "description": "Volunteer for an action item (optional).", "completed": false},
                    {"id": "confirm_understanding", "description": "Confirm understanding of next steps.", "completed": false}
                  ],
                  "allTasksCompleted": false
                }
                """;
            case "office_presentation":
                return """
                 {
                   "aiResponse": "Good morning everyone. Today, I'll be presenting our Q3 results and projections for Q4. Let's start with the key highlights...",
                   "updatedChecklist": [
                     {"id": "introduce_topic", "description": "Introduce the presentation topic.", "completed": false},
                     {"id": "present_key_point", "description": "Present a key data point or argument.", "completed": false},
                     {"id": "use_visual_aid", "description": "Refer to a slide or visual aid.", "completed": false},
                     {"id": "handle_question", "description": "Respond to a question from the audience.", "completed": false},
                     {"id": "summarize_conclude", "description": "Summarize main points and conclude.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            case "office_negotiation":
                return """
                 {
                   "aiResponse": "Thanks for meeting. Regarding the proposal, our initial assessment suggests a few areas we'd like to discuss further. Let's start with the timeline.",
                   "updatedChecklist": [
                     {"id": "state_opening_position", "description": "State your initial position or objective.", "completed": false},
                     {"id": "ask_about_counterpart_needs", "description": "Inquire about the other party's priorities.", "completed": false},
                     {"id": "propose_concession", "description": "Propose a compromise or concession.", "completed": false},
                     {"id": "justify_position", "description": "Provide reasoning for your stance.", "completed": false},
                     {"id": "seek_agreement", "description": "Work towards reaching a mutual agreement.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            case "office_interview":
                return """
                 {
                   "aiResponse": "Welcome! Thanks for coming in today. To start, could you tell me a little bit about yourself and why you're interested in this role?",
                   "updatedChecklist": [
                     {"id": "introduce_yourself", "description": "Briefly introduce yourself.", "completed": false},
                     {"id": "explain_interest_in_role", "description": "Explain your interest in the specific role.", "completed": false},
                     {"id": "answer_behavioral_question", "description": "Answer a 'Tell me about a time when...' question.", "completed": false},
                     {"id": "describe_strength_weakness", "description": "Describe a relevant strength or weakness.", "completed": false},
                     {"id": "ask_interviewer_question", "description": "Ask a question about the role or company.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            // Add cases for tech_code_review, tech_architecture, tech_debugging
            // Add cases for lang_english, lang_spanish, lang_japanese, lang_french

            // --- Example for Technical Discussion ---
            case "tech_code_review":
                return """
                 {
                   "aiResponse": "Okay, I've pulled up your latest commit for the feature branch. Looking at the `UserService.java` file, I have a couple of thoughts. Let's discuss the error handling approach here.",
                   "updatedChecklist": [
                     {"id": "explain_code_logic", "description": "Explain the logic behind a specific code block.", "completed": false},
                     {"id": "respond_to_feedback", "description": "Respond to a comment or suggestion from the reviewer.", "completed": false},
                     {"id": "suggest_alternative", "description": "Suggest an alternative implementation.", "completed": false},
                     {"id": "ask_for_clarification", "description": "Ask for clarification on a review comment.", "completed": false},
                     {"id": "agree_on_changes", "description": "Agree on necessary changes.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            // --- Example for Language Practice ---
            case "lang_english":
                return """
                 {
                   "aiResponse": "Hi! Let's practice some English. How was your day today? Tell me one interesting thing that happened.",
                   "updatedChecklist": [
                     {"id": "greet_and_respond", "description": "Greet the AI and respond to its opening question.", "completed": false},
                     {"id": "describe_activity", "description": "Describe a simple activity or event.", "completed": false},
                     {"id": "ask_a_question", "description": "Ask the AI a question.", "completed": false},
                     {"id": "use_past_tense", "description": "Use the past tense correctly.", "completed": false},
                     {"id": "express_opinion", "description": "Express a simple opinion.", "completed": false}
                   ],
                   "allTasksCompleted": false
                 }
                 """;
            default:
                // Return null or an empty string if no specific JSON is defined
                // ConversationActivity will handle this by calling startConversation API
                return null;
        }
    }
}