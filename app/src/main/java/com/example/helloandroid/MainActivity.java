package com.example.helloandroid;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.SoundEffectConstants;
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

    // プロフィール
    private String playerName = "名もなき冒険者";
    private String playerGoal = "毎日コツコツ単語を覚える！";
    private String playerIcon = "⚔️";

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
        loginBonusReceived = prefs.getBoolean("loginBonusReceived", false);

        // デイリーミッション読み込み
        missionDate = prefs.getString("missionDate", "");
        missionForest = prefs.getBoolean("missionForest", false);
        missionCave = prefs.getBoolean("missionCave", false);
        missionCastle = prefs.getBoolean("missionCastle", false);
        missionBoss = prefs.getBoolean("missionBoss", false);
        missionStreak = prefs.getBoolean("missionStreak", false);
        dailyStreak = prefs.getInt("dailyStreak", 0);

        // プロフィール読み込み
        playerName = prefs.getString("playerName", "名もなき冒険者");
        playerGoal = prefs.getString("playerGoal", "毎日コツコツ単語を覚える！");
        playerIcon = prefs.getString("playerIcon", "⚔️");

        checkLoginBonus();
        checkMissionReset();
        loadWordsFromCSV();

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

    // 効果音を再生する
    private void playSound(int soundId) {
        MediaPlayer mp = MediaPlayer.create(this, soundId);

        if (mp != null) {
            mp.setOnCompletionListener(player -> player.release());
            mp.start();
        }
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
                    System.currentTimeMillis() - 24 * 60 * 60 * 1000
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

        // 左上にプロフィールボタンを配置
        Button profile = makeButton(playerIcon + " " + playerName);
        profile.setTextSize(14);
        profile.setPadding(24, 12, 24, 12);
        profile.setBackground(round(PANEL, 30));

        LinearLayout.LayoutParams profileParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        profileParams.gravity = Gravity.START;
        profileParams.setMargins(0, 0, 0, 10);
        profile.setLayoutParams(profileParams);

        root.addView(profile);
        profile.setOnClickListener(v -> showProfile());

        TextView castle = text("🏰", 65, GOLD);
        castle.setPadding(0, 15, 0, 20);
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

        Button daily = makeButton("🎯 デイリーミッション");
        daily.setBackground(round(PURPLE, 20));
        root.addView(daily);
        daily.setOnClickListener(v -> showDailyMissions());
    }

    // クエスト選択
    private void showQuestSelect() {
        setupScreen("quests");
        addTitle("🗺 QUEST SELECT", "クエストを選ぼう！");

        addQuest("🌲 FOREST", "初級クエスト", 1, 333);
        addQuest("🏔 CAVE", "中級クエスト", 334, 666);
        addQuest("🏰 CASTLE", "上級クエスト", 667, 1000);
        addQuest("👑 BOSS BATTLE", "全範囲から10問", 1, 1000);

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
            startQuest(start, end, title.contains("BOSS") ? 10 : 20);
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

        int safeEnd = Math.min(end, words.length - 1);
        for (int i = start; i <= safeEnd; i++) {
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
            playSound(R.raw.correct); // 正解音を再生

            correct++;
            totalCorrect++;
            streak++;
            dailyStreak++;

            if (dailyStreak >= 7 && !missionStreak) {
                missionStreak = true;
                xp += 30;
                sessionXp += 30;

                Toast.makeText(this,
                        "🎉 デイリーミッション達成！ +30 XP",
                        Toast.LENGTH_SHORT).show();
            }

            int earned = 10 + (streak % 3 == 0 ? 5 : 0);
            xp += earned;
            sessionXp += earned;

            feedbackText.setText(
                    "✨ 正解！ +" + earned + " XP"
                            + (streak % 3 == 0 ? " 🔥 COMBO!" : "")
            );

            answerButtons[selected].setBackground(
                    round(Color.rgb(35, 125, 91), 14)
            );

        } else {
            playSound(R.raw.wrong); // 不正解音を再生
            lives--;
            streak = 0;
            dailyStreak = 0;
            feedbackText.setText(
                    "💥 不正解！ 正解：" + words[wi][1]
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
        int accuracy = total == 0 ? 0 : correct * 100 / total;
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
        addTitle("📖 WORD BOOK", "収録単語：全 " + words.length + " 語");
        addBackButton();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(i + 1).append(". ").append(words[i][0])
                    .append("\n").append(words[i][1]).append("\n\n");
        }

        TextView wt = text(sb.toString(), 16, WHITE);
        wt.setGravity(Gravity.START);
        wt.setPadding(16, 20, 16, 20);
        wt.setBackground(round(PANEL, 14));

        root.addView(wt, fullWidth());
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

    // CSV読み込み
    private void loadWordsFromCSV() {
        List<String[]> tempList = new ArrayList<>();

        try {
            InputStream is = getAssets().open("English Words.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    tempList.add(new String[]{parts[0].trim(), parts[1].trim()});
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            tempList.add(new String[]{"error", "エラー：単語が読み込めませんでした"});
        }

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

    // レベルに応じた称号を取得
    private String getTitleRank() {
        int lv = level();
        if (lv >= 30) return "👑 伝説のワードマスター";
        if (lv >= 20) return "🧙‍♂️ 叡智の大賢者";
        if (lv >= 10) return "⚔️ 言葉の熟練騎士";
        if (lv >= 5)  return "🛡️ 一人前の冒険者";
        return "🌱 見習い冒険者";
    }

    // プロフィール画面
    private void showProfile() {
        setupScreen("profile");
        addTitle("🪪 ADVENTURER CARD", "冒険者プロフィール");

        TextView iconView = text(playerIcon, 70, GOLD);
        iconView.setPadding(0, 10, 0, 15);
        root.addView(iconView, fullWidth());

        TextView card = text(
                "【 名前 】\n" + playerName
                        + "\n\n【 称号 】\n" + getTitleRank()
                        + "\n\n【 レベル / 累計XP 】\nLv." + level() + "  (" + xp + " XP)"
                        + "\n\n【 連続ログイン 】\n🔥 " + loginStreak + " 日目"
                        + "\n\n【 冒険の目標 】\n「" + playerGoal + "」",
                18, WHITE
        );
        card.setPadding(24, 30, 24, 30);
        card.setBackground(round(PANEL, 22));
        root.addView(card, fullWidth());

        Button editIconBtn = makeButton("🎭 アイコンを変更する");
        editIconBtn.setBackground(round(PURPLE, 18));
        root.addView(editIconBtn);
        editIconBtn.setOnClickListener(v -> showIconSelectDialog());

        Button editNameBtn = makeButton("✏️ 名前を変更する");
        root.addView(editNameBtn);
        editNameBtn.setOnClickListener(v -> showEditDialog("名前を入力", playerName, true));

        Button editGoalBtn = makeButton("🎯 目標を変更する");
        root.addView(editGoalBtn);
        editGoalBtn.setOnClickListener(v -> showEditDialog("目標を入力", playerGoal, false));

        addBackButton();
    }

    // アイコン選択用のポップアップ画面
    private void showIconSelectDialog() {
        String[] icons = {
                "⚔️ 剣士",
                "🧙‍♂️ 魔法使い",
                "🛡️ 重装騎士",
                "🏹 弓使い",
                "🥷 忍者",
                "👑 王様",
                "🐉 ドラゴン",
                "🐱 ネコ冒険者"
        };
        String[] iconEmojis = {"⚔️", "🧙‍♂️", "🛡️", "🏹", "🥷", "👑", "🐉", "🐱"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("アイコンを選択")
                .setItems(icons, (dialog, which) -> {
                    playerIcon = iconEmojis[which];
                    prefs.edit().putString("playerIcon", playerIcon).apply();
                    showProfile();
                    Toast.makeText(this, "アイコンを変更しました！", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // テキスト編集用のポップアップ画面
    private void showEditDialog(String title, String currentText, boolean isName) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(currentText);
        input.setSelection(currentText.length());

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setNegativeButton("キャンセル", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        if (isName) {
                            playerName = newText;
                            prefs.edit().putString("playerName", playerName).apply();
                        } else {
                            playerGoal = newText;
                            prefs.edit().putString("playerGoal", playerGoal).apply();
                        }
                        showProfile();
                        Toast.makeText(this, "保存しました！", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}