package com.myapp.lenovo.hwmquiz;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivityFragment extends Fragment {
    private static final String TAG = "Quiz Activity";

    private List<String> fileNameList;
    private List<String> quizCreatureList;
    private Set<String> fractionsSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int numberQuestions = 10;
    private int guessRows;
    private SecureRandom random; // used to randomize the quiz
    private Handler handler; // used to delay loading next picture
    private Animation shakeAnimation;

    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView ImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList = new ArrayList<>();
        quizCreatureList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();


        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        quizLinearLayout =
                (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        ImageView = (ImageView) view.findViewById(R.id.ImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] =
                (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(getString(R.string.question, 1, numberQuestions));
        return view;
    }

    public void  updateNumberOfQuestions(SharedPreferences sharedPreferences){
        String questions =
                sharedPreferences.getString(MainActivity.QUESTIONS, null);
        numberQuestions = Integer.parseInt(questions) ;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {

        String choices =
                sharedPreferences.getString(com.myapp.lenovo.hwmquiz.MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateFractions(SharedPreferences sharedPreferences) {
        fractionsSet =
                sharedPreferences.getStringSet(com.myapp.lenovo.hwmquiz.MainActivity.FRACTIONS, null);
    }

    public void resetQuiz() {
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            for (String fraction : fractionsSet) {

                String[] paths = assets.list(fraction);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0;
        totalGuesses = 0;
        quizCreatureList.clear();
        int pictureCounter = 1;
        int numberOfPictures = fileNameList.size();

        while (pictureCounter <= numberQuestions) {
            int randomIndex = random.nextInt(numberOfPictures);
            String filename = fileNameList.get(randomIndex);

            if (!quizCreatureList.contains(filename)) {
                quizCreatureList.add(filename);
                ++pictureCounter;
            }
        }

        loadNextPicture();
    }


    private void loadNextPicture() {
        String nextImage = quizCreatureList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");
        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), numberQuestions));

        String fraction = nextImage.substring(0, nextImage.indexOf('-'));

        AssetManager assets = getActivity().getAssets();

        try (InputStream stream =
                     assets.open(fraction + "/" + nextImage + ".png")) {
            Drawable picture = Drawable.createFromStream(stream, nextImage);
            ImageView.setImageDrawable(picture);

            animate(false);
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        loadButtons();
    }

   public void loadButtons(){
        Collections.shuffle(fileNameList);

        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row = 0; row < guessRows; row++) {

            for (int column = 0;
                 column < guessLinearLayouts[row].getChildCount();
                 column++) {

                Button newGuessButton =
                        (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCreatureName(filename));
            }
        }


        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayouts[row];
        String countryName = getCreatureName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    private String getCreatureName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    private void animate(boolean animateOut) {
        if (correctAnswers == 0)
            return;

        int centerX = (quizLinearLayout.getLeft() +
                quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() +
                quizLinearLayout.getBottom()) / 2;

        int radius = Math.max(quizLinearLayout.getWidth(),
                quizLinearLayout.getHeight());

        Animator animator;

        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(
                    new AnimatorListenerAdapter() {
                        // called when the animation finishes
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loadNextPicture();
                        }
                    }
            );
        }
        else {
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500);
        animator.start();
    }

    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCreatureName(correctAnswer);
            ++totalGuesses;

            if (guess.equals(answer)) {
                ++correctAnswers;


                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons();

                if (correctAnswers == numberQuestions) {
                    Intent intent = new Intent(getContext(), Result.class);
                    intent.putExtra("1111", getString(R.string.result, numberQuestions, totalGuesses));
                    intent.putExtra("2222", getString(R.string.results, (numberQuestions * 100 / (double) totalGuesses)));
                    startActivity(intent);
                }
                else {
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true);
                                }
                            }, 500);
                }
            }
            else {
                ImageView.startAnimation(shakeAnimation);

//                answerTextView.setText(R.string.incorrect_answer);
//                answerTextView.setTextColor(getResources().getColor(
//                        R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }
}
