package com.example.helloandroid;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String[][] vocabulary = {
            {"abandon", "捨てる、放棄する"},
            {"abstract", "抽象的な"},
            {"accurate", "正確な"},
            {"acquire", "習得する、獲得する"},
            {"adapt", "適応する"},
            {"adequate", "十分な、適切な"},
            {"advocate", "主張する、支持する"},
            {"ambiguous", "曖昧な"},
            {"analyze", "分析する"},
            {"apparent", "明白な"},
            {"approach", "取り組む、接近する"},
            {"assess", "評価する"},
            {"assume", "仮定する"},
            {"attain", "達成する"},
            {"beneficial", "有益な"},
            {"capacity", "能力、収容能力"},
            {"circumstance", "状況、事情"},
            {"coherent", "筋の通った、一貫した"},
            {"compensate", "補償する"},
            {"complex", "複雑な"},
            {"comprehensive", "包括的な"},
            {"consequence", "結果、影響"},
            {"considerable", "かなりの、相当な"},
            {"constitute", "構成する"},
            {"controversial", "議論を呼ぶ"},
            {"conventional", "従来の、慣習的な"},
            {"crucial", "極めて重要な"},
            {"decline", "減少する、断る"},
            {"demonstrate", "実証する、示す"},
            {"derive", "引き出す、由来する"},
            {"diverse", "多様な"},
            {"eliminate", "取り除く"},
            {"emerge", "現れる、明らかになる"},
            {"emphasize", "強調する"},
            {"enable", "可能にする"},
            {"encounter", "遭遇する"},
            {"equivalent", "同等の"},
            {"establish", "確立する"},
            {"evaluate", "評価する"},
            {"evident", "明らかな"}
    };

    private final int TOTAL = vocabulary.length;
    private final int MAX_LIVES = 3;
    private final Random random = new Random();

    private List<Integer> questionOrder;
    private int index;
    private int xp;
    private int lives;
    private int streak;
    private int correct;
    private boolean answered;

    private TextView titleText;
    private TextView statsText;
    private TextView progressText;
    private TextView wordText;
    private TextView questionText;
    private TextView feedbackText;
    private TextView progressBarText;
    private Button[] answerButtons;
    private Button nextButton;
    private LinearLayout root;

    private final int BG = Color.rgb(17, 20, 42);
    private final int PANEL = Color.rgb(31, 36, 68);
    private final int PURPLE = Color.rgb(124, 92, 230);
    private final int GOLD = Color.rgb(255, 202, 87);
    private final int WHITE = Color.rgb(245, 245, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startGame();
    }

    private void startGame() {
        index = 0;
        xp = 0;
        lives = MAX_LIVES;
        streak = 0;
        correct = 0;
        answered = false;

        questionOrder = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            questionOrder.add(i);
        }
        Collections.shuffle(questionOrder);

        buildScreen();
        showQuestion();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(22, 24, 22, 24);

        scroll.addView(root);

        titleText = makeText("⚔ WORD QUEST ⚔", 27, GOLD);
        titleText.setTypeface(null, Typeface.BOLD);
        root.addView(titleText);

        TextView subtitle = makeText(
                "VOCABULARY ADVENTURE", 12, WHITE);
        subtitle.setPadding(0, 4, 0, 22);
        root.addView(subtitle);

        statsText = makeText("", 15, WHITE);
        statsText.setPadding(12, 14, 12, 14);
        statsText.setBackground(round(PANEL, 18));
        root.addView(statsText, fullWidth());

        progressText = makeText("", 14, GOLD);
        progressText.setPadding(0, 20, 0, 8);
        root.addView(progressText);

        progressBarText = makeText("", 12, WHITE);
        progressBarText.setPadding(0, 0, 0, 18);
        root.addView(progressBarText);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(16, 28, 16, 28);
        card.setBackground(round(PANEL, 22));

        wordText = makeText("", 34, WHITE);
        wordText.setTypeface(null, Typeface.BOLD);
        card.addView(wordText);

        TextView hint = makeText(
                "この英単語の意味を選べ！", 15, GOLD);
        hint.setPadding(0, 12, 0, 0);
        card.addView(hint);

        LinearLayout.LayoutParams cardParams = fullWidth();
        cardParams.bottomMargin = 18;
        root.addView(card, cardParams);

        questionText = makeText("", 17, WHITE);
        questionText.setPadding(0, 4, 0, 10);
        root.addView(questionText);

        answerButtons = new Button[4];

        for (int i = 0; i < 4; i++) {
            final int selected = i;
            Button button = new Button(this);
            answerButtons[i] = button;
            button.setTextSize(15);
            button.setTextColor(WHITE);
            button.setAllCaps(false);
            button.setBackground(round(
                    Color.rgb(48, 54, 91), 14));

            LinearLayout.LayoutParams params = fullWidth();
            params.setMargins(0, 5, 0, 5);
            root.addView(button, params);

            button.setOnClickListener(v -> checkAnswer(selected));
        }

        feedbackText = makeText("", 15, GOLD);
        feedbackText.setPadding(0, 12, 0, 8);
        root.addView(feedbackText);

        nextButton = new Button(this);
        nextButton.setText("次のクエスト ➜");
        nextButton.setTextColor(Color.WHITE);
        nextButton.setAllCaps(false);
        nextButton.setTextSize(16);
        nextButton.setBackground(round(PURPLE, 18));

        LinearLayout.LayoutParams nextParams = fullWidth();
        nextParams.setMargins(0, 10, 0, 12);
        root.addView(nextButton, nextParams);

        nextButton.setOnClickListener(v -> {
            if (!answered) {
                Toast.makeText(this, "まず答えを選ぼう！",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (lives <= 0 || index == TOTAL - 1) {
                showResult();
            } else {
                index++;
                showQuestion();
            }
        });

        setContentView(scroll);
    }

    private TextView makeText(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private void showQuestion() {
        answered = false;

        int wordIndex = questionOrder.get(index);
        wordText.setText(vocabulary[wordIndex][0]);

        progressText.setText(
                "QUEST " + (index + 1) + " / " + TOTAL);

        int percent = (index * 100) / TOTAL;
        progressBarText.setText(
                "進行度 " + percent + "%  " + "▰".repeat(
                        Math.max(1, percent / 10)));

        updateStats();

        questionText.setText("正しい意味はどれだ？");
        feedbackText.setText("");

        List<String> options = new ArrayList<>();
        options.add(vocabulary[wordIndex][1]);

        while (options.size() < 4) {
            String meaning = vocabulary[
                    random.nextInt(TOTAL)][1];

            if (!options.contains(meaning)) {
                options.add(meaning);
            }
        }

        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(
                    (char) ('A' + i) + "  " + options.get(i));
            answerButtons[i].setEnabled(true);
            answerButtons[i].setVisibility(View.VISIBLE);
            answerButtons[i].setBackground(
                    round(Color.rgb(48, 54, 91), 14));
        }

        nextButton.setText("次のクエスト ➜");
        nextButton.setVisibility(View.VISIBLE);
    }

    private void checkAnswer(int selected) {
        if (answered) return;
        answered = true;

        int wordIndex = questionOrder.get(index);
        String selectedText =
                answerButtons[selected].getText().toString();
        String correctMeaning = vocabulary[wordIndex][1];

        boolean isCorrect = selectedText.endsWith(correctMeaning);

        if (isCorrect) {
            correct++;
            streak++;

            int bonus = streak % 3 == 0 ? 5 : 0;
            xp += 10 + bonus;

            feedbackText.setText(
                    "✨ 正解！ +" + (10 + bonus) + " XP"
                            + (bonus > 0 ? "  コンボボーナス！" : ""));

            feedbackText.setTextColor(GOLD);
            answerButtons[selected].setBackground(
                    round(Color.rgb(35, 125, 91), 14));
        } else {
            lives--;
            streak = 0;

            feedbackText.setText(
                    "💥 不正解！ 正解：" + correctMeaning);
            feedbackText.setTextColor(
                    Color.rgb(255, 120, 130));

            answerButtons[selected].setBackground(
                    round(Color.rgb(145, 47, 68), 14));
        }

        for (Button button : answerButtons) {
            button.setEnabled(false);
        }

        updateStats();

        if (lives <= 0 || index == TOTAL - 1) {
            nextButton.setText("結果を見る 🏆");
        }
    }

    private void updateStats() {
        int level = xp / 50 + 1;

        statsText.setText(
                "❤️ LIFE: " + "♥ ".repeat(lives)
                        + "♡ ".repeat(MAX_LIVES - lives)
                        + "\n⭐ LEVEL " + level
                        + "     ✨ XP " + xp
                        + "\n🔥 COMBO " + streak
                        + "     🏆 " + correct + "問正解");
    }

    private void showResult() {
        root.removeAllViews();

        TextView trophy = makeText("🏆", 58, GOLD);
        root.addView(trophy);

        TextView heading = makeText(
                lives <= 0 ? "QUEST OVER" : "QUEST COMPLETE!",
                28, GOLD);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 16, 0, 20);
        root.addView(heading);

        TextView result = makeText(
                "正解数： " + correct + " / " + TOTAL
                        + "\n\n獲得 XP： " + xp
                        + "\n\n到達レベル： " + (xp / 50 + 1)
                        + "\n\n正答率： "
                        + (correct * 100 / TOTAL) + "%",
                22, WHITE);

        result.setPadding(20, 30, 20, 30);
        result.setBackground(round(PANEL, 22));
        root.addView(result, fullWidth());

        TextView message = makeText(
                correct >= 30
                        ? "伝説の英単語マスター！"
                        : correct >= 20
                        ? "すばらしい冒険だった！"
                        : "もう一度挑戦して強くなろう！",
                17, GOLD);

        message.setPadding(0, 28, 0, 28);
        root.addView(message);

        Button retry = new Button(this);
        retry.setText("もう一度冒険する ↻");
        retry.setTextColor(WHITE);
        retry.setAllCaps(false);
        retry.setBackground(round(PURPLE, 18));
        root.addView(retry, fullWidth());

        retry.setOnClickListener(v -> startGame());
    }
}