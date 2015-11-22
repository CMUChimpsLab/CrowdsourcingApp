package com.dhchoi.crowdsourcingapp.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.R;

import static com.dhchoi.crowdsourcingapp.Constants.KEY_NAME;
import static com.dhchoi.crowdsourcingapp.Constants.KEY_QUESTION;

public class AnswerQuestionActivity extends AppCompatActivity {



    // TextViews
    TextView mCurrentLocationTextView;
    TextView mLocationQuestionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_question);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String locationName = getIntent().getExtras().getString(KEY_NAME);
        String questionText = getIntent().getExtras().getString(KEY_QUESTION);

        mCurrentLocationTextView = (TextView) findViewById(R.id.location_text);
        mLocationQuestionTextView = (TextView) findViewById(R.id.question_text);

        mCurrentLocationTextView.setText(locationName);
        mLocationQuestionTextView.setText(questionText);
    }

}
