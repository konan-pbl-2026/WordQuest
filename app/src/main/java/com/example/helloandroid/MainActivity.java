package com.example.helloandroid;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
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

//実験
public class MainActivity extends AppCompatActivity {

    private final String[][] words = {
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
            {"coherent", "一貫した"},
            {"compensate", "補償する"},
            {"complex", "複雑な"},
            {"comprehensive", "包括的な"},
            {"consequence", "結果、影響"},
            {"considerable", "かなりの"},
            {"constitute", "構成する"},
            {"controversial", "議論を呼ぶ"},
            {"conventional", "従来の"},
            {"crucial", "極めて重要な"},
            {"decline", "減少する、断る"},
            {"demonstrate", "実証する"},
            {"derive", "由来する、引き出す"},
            {"diverse", "多様な"},
            {"eliminate", "取り除く"},
            {"emerge", "現れる"},
            {"emphasize", "強調する"},
            {"enable", "可能にする"},
            {"encounter", "遭遇する"},
            {"equivalent", "同等の"},
            {"establish", "確立する"},
            {"evaluate", "評価する"},
            {"evident", "明らかな"}
    };

    private final int BG = Color.rgb(17, 20, 42);
    private final int PANEL = Color.rgb(31, 36, 68);
    private final int PURPLE = Color.rgb(124, 92, 230);
    private final int GOLD = Color.rgb(255, 202, 87);
    private final int WHITE = Color.rgb(245, 245, 255);

    private final Random random = new Random();
    private SharedPreferences prefs;
    private LinearLayout root;

    private TextView statsText, wordText, questionText;
    private TextView feedbackText, progressText;
    private Button[] answerButtons;
    private Button nextButton;

    private List<Integer> questionOrder = new ArrayList<>();
    private String[] options = new String[4];

    private int index, xp, lives, streak, correct, sessionXp;
    private int totalCorrect, totalAnswered, bestStreak;
    private boolean answered;
    private String currentScreen = "home";
    private String questName = "冒険";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("WordQuestData", MODE_PRIVATE);
        xp = prefs.getInt("xp", 0);
        totalCorrect = prefs.getInt("totalCorrect", 0);
        totalAnswered = prefs.getInt("totalAnswered", 0);
        bestStreak = prefs.getInt("bestStreak", 0);

        showHome();
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private Button makeButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackground(round(PANEL, 18));

        LinearLayout.LayoutParams p = fullWidth();
        p.setMargins(0, 6, 0, 6);
        b.setLayoutParams(p);
        return b;
    }

    private void setupScreen(String name) {
        currentScreen = name;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(22, 24, 22, 24);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void addTitle(String title, String subtitle) {
        TextView t = text(title, 28, GOLD);
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, 10, 0, 6);
        root.addView(t, fullWidth());

        TextView s = text(subtitle, 13, WHITE);
        s.setPadding(0, 0, 0, 22);
        root.addView(s, fullWidth());
    }

    private void addBackButton() {
        Button b = makeButton("← ホームに戻る");
        root.addView(b);
        b.setOnClickListener(v -> showHome());
    }

    private void saveData() {
        prefs.edit()
                .putInt("xp", xp)
                .putInt("totalCorrect", totalCorrect)
                .putInt("totalAnswered", totalAnswered)
                .putInt("bestStreak", bestStreak)
                .apply();
    }

    private int level() {
        return xp / 50 + 1;
    }

    // ホーム画面
    private void showHome() {
        setupScreen("home");

        TextView castle = text("🏰", 65, GOLD);
        castle.setPadding(0, 35, 0, 20);
        root.addView(castle);

        addTitle("⚔ WORD QUEST ⚔",
                "英単語を極め、伝説の冒険者になれ。");

        TextView stats = text(
                "🛡 冒険者 Lv." + level()
                        + "\n⭐ TOTAL XP : " + xp
                        + "\n🏆 正解した問題 : " + totalCorrect,
                18, WHITE
        );

        stats.setPadding(16, 24, 16, 24);
        stats.setBackground(round(PANEL, 22));
        root.addView(stats, fullWidth());

        Button start = makeButton("⚔ 冒険を始める");
        start.setTextSize(19);
        start.setBackground(round(PURPLE, 20));
        root.addView(start);
        start.setOnClickListener(v -> showQuestSelect());

        Button book = makeButton("📖 単語図鑑");
        root.addView(book);
        book.setOnClickListener(v -> showDictionary());

        Button record = makeButton("🏆 学習記録・実績");
        root.addView(record);
        record.setOnClickListener(v -> showStatus());
    }

    // クエスト選択
    private void showQuestSelect() {
        setupScreen("quests");
        addTitle("🗺 QUEST SELECT", "クエストを選ぼう！");

        addQuest("🌲 FOREST", "初級クエスト", 0, 13);
        addQuest("🏔 CAVE", "中級クエスト", 14, 26);
        addQuest("🏰 CASTLE", "上級クエスト", 27, 39);
        addQuest("👑 BOSS BATTLE", "全範囲から10問", 0, 39);

        addBackButton();
    }

    private void addQuest(String title, String subtitle,
                          int start, int end) {
        TextView label = text(
                title + "\n" + subtitle, 18, GOLD
        );
        label.setPadding(12, 15, 12, 15);
        label.setBackground(round(PANEL, 18));
        root.addView(label, fullWidth());

        Button b = makeButton("このクエストに挑戦 →");
        root.addView(b);

        b.setOnClickListener(v -> {
            questName = title;
            startQuest(start, end,
                    title.contains("BOSS") ? 10 : end - start + 1);
        });
    }

    private void startQuest(int start, int end, int count) {
        index = 0;
        lives = 3;
        streak = 0;
        correct = 0;
        sessionXp = 0;
        answered = false;

        questionOrder = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            questionOrder.add(i);
        }

        Collections.shuffle(questionOrder);

        if (count < questionOrder.size()) {
            questionOrder = new ArrayList<>(
                    questionOrder.subList(0, count)
            );
        }

        buildQuiz();
        showQuestion();
    }

    // クイズ画面
    private void buildQuiz() {
        setupScreen("quiz");
        addTitle("⚔ WORD QUEST", questName);

        statsText = text("", 15, WHITE);
        statsText.setPadding(12, 14, 12, 14);
        statsText.setBackground(round(PANEL, 18));
        root.addView(statsText, fullWidth());

        progressText = text("", 15, GOLD);
        progressText.setPadding(0, 18, 0, 10);
        root.addView(progressText);

        wordText = text("", 34, WHITE);
        wordText.setTypeface(null, Typeface.BOLD);
        wordText.setPadding(0, 28, 0, 28);
        wordText.setBackground(round(PANEL, 22));
        root.addView(wordText, fullWidth());

        questionText = text("意味を選べ！", 16, WHITE);
        root.addView(questionText);

        answerButtons = new Button[4];

        for (int i = 0; i < 4; i++) {
            final int selected = i;
            answerButtons[i] = makeButton("");
            root.addView(answerButtons[i]);
            answerButtons[i].setOnClickListener(
                    v -> checkAnswer(selected)
            );
        }

        feedbackText = text("", 15, GOLD);
        feedbackText.setPadding(0, 15, 0, 15);
        root.addView(feedbackText);

        nextButton = makeButton("次の問題 →");
        nextButton.setBackground(round(PURPLE, 18));
        root.addView(nextButton);

        nextButton.setOnClickListener(v -> {
            if (!answered) {
                Toast.makeText(this, "先に答えを選んでね！",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (lives <= 0 ||
                    index >= questionOrder.size() - 1) {
                showResult();
            } else {
                index++;
                showQuestion();
            }
        });

        Button home = makeButton("ホームに戻る");
        root.addView(home);
        home.setOnClickListener(v -> showHome());
    }
    // 問題を表示
    private void showQuestion() {
        answered = false;
        int wi = questionOrder.get(index);

        wordText.setText(words[wi][0]);
        progressText.setText(
                "QUEST " + (index + 1) + " / "
                        + questionOrder.size()
        );

        questionText.setText("正しい意味はどれだ？");
        feedbackText.setText("");

        List<String> list = new ArrayList<>();
        list.add(words[wi][1]);

        while (list.size() < 4) {
            String meaning =
                    words[random.nextInt(words.length)][1];

            if (!list.contains(meaning)) {
                list.add(meaning);
            }
        }

        Collections.shuffle(list);

        for (int i = 0; i < 4; i++) {
            options[i] = list.get(i);

            answerButtons[i].setText(
                    (char) ('A' + i) + "  " + options[i]
            );

            answerButtons[i].setEnabled(true);
            answerButtons[i].setBackground(
                    round(PANEL, 14)
            );
        }

        nextButton.setText("次の問題 →");
        updateStats();
    }

    // 正誤判定
    private void checkAnswer(int selected) {
        if (answered) return;
        answered = true;
        totalAnswered++;

        int wi = questionOrder.get(index);

        if (options[selected].equals(words[wi][1])) {
            correct++;
            totalCorrect++;
            streak++;

            int earned = 10 + (streak % 3 == 0 ? 5 : 0);

            xp += earned;
            sessionXp += earned;

            feedbackText.setText(
                    "✨ 正解！ +" + earned + " XP"
                            + (streak % 3 == 0
                            ? " 🔥 COMBO!" : "")
            );

            answerButtons[selected].setBackground(
                    round(Color.rgb(35, 125, 91), 14)
            );

        } else {
            lives--;
            streak = 0;

            feedbackText.setText(
                    "💥 不正解！ 正解："
                            + words[wi][1]
            );

            answerButtons[selected].setBackground(
                    round(Color.rgb(145, 47, 68), 14)
            );
        }

        bestStreak = Math.max(bestStreak, streak);

        for (Button b : answerButtons) {
            b.setEnabled(false);
        }

        updateStats();
        saveData();

        if (lives <= 0 ||
                index >= questionOrder.size() - 1) {
            nextButton.setText("結果を見る 🏆");
        }
    }

    // ステータス更新
    private void updateStats() {
        String hearts = "";

        for (int i = 0; i < lives; i++) {
            hearts += "♥ ";
        }

        for (int i = lives; i < 3; i++) {
            hearts += "♡ ";
        }

        statsText.setText(
                "❤️ LIFE: " + hearts
                        + "\n⭐ LEVEL " + level()
                        + "     ✨ XP " + xp
                        + "\n🔥 COMBO " + streak
                        + "     🏆 " + correct + "問正解"
        );
    }

    // 結果画面
    private void showResult() {
        setupScreen("result");

        addTitle("🏆 QUEST COMPLETE!",
                "冒険お疲れさまでした！");

        int total = questionOrder.size();
        int accuracy = correct * 100 / total;

        TextView result = text(
                "正解数：" + correct + " / " + total
                        + "\n\n獲得 XP：" + sessionXp
                        + "\n\n総 XP：" + xp
                        + "\n\nレベル：" + level()
                        + "\n\n正答率：" + accuracy + "%",
                20, WHITE
        );

        result.setPadding(20, 25, 20, 25);
        result.setBackground(round(PANEL, 22));
        root.addView(result, fullWidth());

        Button retry = makeButton("↻ もう一度挑戦");
        root.addView(retry);
        retry.setOnClickListener(v -> showQuestSelect());

        Button book = makeButton("📖 単語図鑑");
        root.addView(book);
        book.setOnClickListener(v -> showDictionary());

        addBackButton();
    }

    // 単語図鑑
    private void showDictionary() {
        setupScreen("dictionary");
        addTitle("📖 WORD BOOK", "英単語40語");

        for (int i = 0; i < words.length; i++) {
            TextView t = text(
                    (i + 1) + ". " + words[i][0]
                            + "\n" + words[i][1],
                    16, WHITE
            );

            t.setGravity(Gravity.START);
            t.setPadding(14, 12, 14, 12);
            t.setBackground(round(PANEL, 14));

            LinearLayout.LayoutParams p = fullWidth();
            p.setMargins(0, 4, 0, 4);
            root.addView(t, p);
        }

        addBackButton();
    }

    // 学習記録
    private void showStatus() {
        setupScreen("status");
        addTitle("🏆 ADVENTURER STATUS",
                "冒険の記録");

        int accuracy = totalAnswered == 0 ? 0 :
                totalCorrect * 100 / totalAnswered;

        TextView t = text(
                "レベル：" + level()
                        + "\n\n累計 XP：" + xp
                        + "\n\n解答数：" + totalAnswered
                        + "\n\n正解数：" + totalCorrect
                        + "\n\n正答率：" + accuracy + "%"
                        + "\n\n最高コンボ：" + bestStreak,
                20, WHITE
        );

        t.setPadding(20, 25, 20, 25);
        t.setBackground(round(PANEL, 22));
        root.addView(t, fullWidth());

        Button reset = makeButton("記録をリセット");
        root.addView(reset);

        reset.setOnClickListener(v ->
                new android.app.AlertDialog.Builder(this)
                        .setTitle("記録をリセット")
                        .setMessage("すべての記録を消しますか？")
                        .setNegativeButton("キャンセル", null)
                        .setPositiveButton("リセット", (d, w) -> {
                            xp = 0;
                            totalCorrect = 0;
                            totalAnswered = 0;
                            bestStreak = 0;
                            saveData();
                            showStatus();
                        })
                        .show()
        );

        addBackButton();
    }

    // 戻るボタン
    @Override
    public void onBackPressed() {
        if ("home".equals(currentScreen)) {
            super.onBackPressed();
        } else {
            showHome();
        }
    }
}