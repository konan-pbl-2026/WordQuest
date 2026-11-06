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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

//実験
public class MainActivity extends AppCompatActivity {

    private String[][] words;
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
    // ログインボーナス
    private String lastLoginDate = "";
    private int loginStreak = 0;
    private boolean loginBonusReceived = false;
    // デイリーミッション
    private String missionDate = "";

    private boolean missionForest = false;
    private boolean missionCave = false;
    private boolean missionCastle = false;
    private boolean missionBoss = false;
    private boolean missionStreak = false;

    private int dailyStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("WordQuestData", MODE_PRIVATE);

        xp = prefs.getInt("xp", 0);
        totalCorrect = prefs.getInt("totalCorrect", 0);
        totalAnswered = prefs.getInt("totalAnswered", 0);
        bestStreak = prefs.getInt("bestStreak", 0);

        lastLoginDate = prefs.getString("lastLoginDate", "");
        loginStreak = prefs.getInt("loginStreak", 0);
        loginBonusReceived =
                prefs.getBoolean("loginBonusReceived", false);

// デイリーミッション読み込み
        missionDate = prefs.getString("missionDate", "");

        missionForest = prefs.getBoolean("missionForest", false);
        missionCave = prefs.getBoolean("missionCave", false);
        missionCastle = prefs.getBoolean("missionCastle", false);
        missionBoss = prefs.getBoolean("missionBoss", false);
        missionStreak = prefs.getBoolean("missionStreak", false);

        dailyStreak = prefs.getInt("dailyStreak", 0);

        checkLoginBonus();
        checkMissionReset();

        loadWordsFromCSV();

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
    // デイリーミッションの日付確認
    private String getMissionToday() {
        return new java.text.SimpleDateFormat(
                "yyyyMMdd",
                java.util.Locale.JAPAN
        ).format(new java.util.Date());
    }

    // 日付が変わったらミッションをリセット
    private void checkMissionReset() {

        String today = getMissionToday();

        if (!today.equals(missionDate)) {

            missionDate = today;

            missionForest = false;
            missionCave = false;
            missionCastle = false;
            missionBoss = false;
            missionStreak = false;

            dailyStreak = 0;

            saveMissionData();
        }
    }

    // ミッションデータ保存
    private void saveMissionData() {

        prefs.edit()
                .putString("missionDate", missionDate)
                .putBoolean("missionForest", missionForest)
                .putBoolean("missionCave", missionCave)
                .putBoolean("missionCastle", missionCastle)
                .putBoolean("missionBoss", missionBoss)
                .putBoolean("missionStreak", missionStreak)
                .putInt("dailyStreak", dailyStreak)
                .apply();
    }
    // ログインボーナスの日付確認
    private void checkLoginBonus() {

        String today = new java.text.SimpleDateFormat(
                "yyyyMMdd",
                java.util.Locale.JAPAN
        ).format(new java.util.Date());

        if (!today.equals(lastLoginDate)) {

            String yesterday = new java.text.SimpleDateFormat(
                    "yyyyMMdd",
                    java.util.Locale.JAPAN
            ).format(new java.util.Date(
                    System.currentTimeMillis()
                            - 24 * 60 * 60 * 1000
            ));

            if (yesterday.equals(lastLoginDate)) {
                loginStreak++;
            } else {
                loginStreak = 1;
            }

            lastLoginDate = today;
            loginBonusReceived = false;

            prefs.edit()
                    .putString("lastLoginDate", lastLoginDate)
                    .putInt("loginStreak", loginStreak)
                    .putBoolean("loginBonusReceived", false)
                    .apply();
        }
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
        Button login = makeButton("🎁 ログインボーナス");
        login.setBackground(round(PURPLE, 20));
        root.addView(login);

        login.setOnClickListener(v -> showLoginBonus());
        // デイリーミッションボタン
        Button daily = makeButton("🎯 デイリーミッション");
        daily.setBackground(round(PURPLE, 20));
        root.addView(daily);

        daily.setOnClickListener(v -> showDailyMissions());
    }

    // クエスト選択
    private void showQuestSelect() {
        setupScreen("quests");
        addTitle("🗺 QUEST SELECT", "クエストを選ぼう！");

        addQuest("🌲 FOREST", "初級クエスト", 0, 13);
        addQuest("🏔 CAVE", "中級クエスト", 1, 1000);
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

        checkMissionReset();
        answered = true;
        totalAnswered++;

        int wi = questionOrder.get(index);

        if (options[selected].equals(words[wi][1])) {
            correct++;
            totalCorrect++;
            streak++;
            // デイリー7問連続正解
            dailyStreak++;

            if (dailyStreak >= 7 && !missionStreak) {

                missionStreak = true;

                xp += 30;
                sessionXp += 30;

                feedbackText.setText(
                        "🔥 7問連続正解達成！ +30 XP"
                );

                Toast.makeText(this,
                        "🎉 デイリーミッション達成！ +30 XP",
                        Toast.LENGTH_SHORT).show();
            }

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
            dailyStreak = 0;
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
        saveMissionData();

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
        // クエストクリア時のデイリーミッション判定
        boolean questCleared = lives > 0;

        if (questCleared) {

            int reward = 0;
            String message = "";

            if (questName.contains("FOREST") && !missionForest) {

                missionForest = true;
                reward = 20;
                message = "🌲 初級クエスト達成！";

            } else if (questName.contains("CAVE") && !missionCave) {

                missionCave = true;
                reward = 30;
                message = "🏔 中級クエスト達成！";

            } else if (questName.contains("CASTLE") && !missionCastle) {

                missionCastle = true;
                reward = 40;
                message = "🏰 上級クエスト達成！";

            } else if (questName.contains("BOSS") && !missionBoss) {

                missionBoss = true;
                reward = 50;
                message = "👑 ボスバトル達成！";
            }

            if (reward > 0) {

                xp += reward;
                sessionXp += reward;

                Toast.makeText(this,
                        message + " +" + reward + " XP",
                        Toast.LENGTH_LONG).show();
            }
        }

        saveData();
        saveMissionData();

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
    // デイリーミッション画面
    private void showDailyMissions() {

        checkMissionReset();

        setupScreen("daily");

        addTitle("🎯 DAILY MISSIONS",
                "今日の冒険者ミッション！");

        addMission("🌲 初級クエストを1回クリア",
                missionForest, 20);

        addMission("🏔 中級クエストを1回クリア",
                missionCave, 30);

        addMission("🏰 上級クエストを1回クリア",
                missionCastle, 40);

        addMission("👑 ボスバトルを1回クリア",
                missionBoss, 50);

        addMission("🔥 7問連続正解する",
                missionStreak, 30);

        TextView reward = text(
                "🎁 全ミッション達成！\n合計 170 XP",
                21, GOLD
        );

        reward.setPadding(15, 25, 15, 25);
        reward.setBackground(round(PANEL, 20));

        root.addView(reward, fullWidth());

        addBackButton();
    }

    // ミッション表示
    private void addMission(
            String name,
            boolean completed,
            int reward
    ) {

        TextView t = text(
                (completed ? "✅ " : "🎯 ") + name
                        + "\n\n報酬：" + reward + " XP"
                        + "\n"
                        + (completed ? "CLEAR!" : "未達成"),
                16, completed ? GOLD : WHITE
        );

        t.setGravity(Gravity.CENTER);
        t.setPadding(15, 20, 15, 20);
        t.setBackground(round(PANEL, 18));

        LinearLayout.LayoutParams p = fullWidth();
        p.setMargins(0, 7, 0, 7);

        root.addView(t, p);
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
    private void loadWordsFromCSV() {
        List<String[]> tempList = new ArrayList<>();

        try {
            // assetsフォルダ内の words.csv を開く
            InputStream is = getAssets().open("English Words.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            // 1行ずつ読み込む
            while ((line = reader.readLine()) != null) {
                // カンマで英単語と意味に分割 (2つにだけ分ける)
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    // tempListに追加
                    tempList.add(new String[]{parts[0].trim(), parts[1].trim()});
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            // 万が一読み込めなかった時のための予備データ
            tempList.add(new String[]{"error", "エラー：単語が読み込めませんでした"});
        }

        // List を String[][] に変換（既存のコードをそのまま動かすため）
        words = new String[tempList.size()][2];
        for (int i = 0; i < tempList.size(); i++) {
            words[i] = tempList.get(i);
        }
    }
    // ログインボーナス画面
    private void showLoginBonus() {

        setupScreen("loginBonus");

        addTitle("🎁 LOGIN BONUS",
                "今日の冒険者ボーナス！");

        int bonus = loginStreak % 7 == 0 ? 100 : 20;

        TextView reward = text(
                "🔥 連続ログイン " + loginStreak + " 日目！"
                        + "\n\n⭐ 今日の報酬"
                        + "\n\n+" + bonus + " XP",
                24, GOLD
        );

        reward.setPadding(20, 35, 20, 35);
        reward.setBackground(round(PANEL, 22));
        root.addView(reward, fullWidth());

        Button receive = makeButton("🎁 ボーナスを受け取る");
        receive.setBackground(round(PURPLE, 20));
        root.addView(receive);

        if (loginBonusReceived) {
            receive.setText("✅ 受け取り済み");
            receive.setEnabled(false);
        }

        receive.setOnClickListener(v -> {

            if (loginBonusReceived) return;

            xp += bonus;
            loginBonusReceived = true;

            prefs.edit()
                    .putInt("xp", xp)
                    .putBoolean("loginBonusReceived", true)
                    .apply();

            receive.setText("✅ 受け取り完了！");
            receive.setEnabled(false);

            Toast.makeText(this,
                    "+" + bonus + " XP 獲得！",
                    Toast.LENGTH_SHORT).show();
        });

        addBackButton();
    }
}