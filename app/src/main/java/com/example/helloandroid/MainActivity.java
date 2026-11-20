package com.example.helloandroid;

import android.widget.ImageView;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
    private final int TEAL = Color.rgb(35, 125, 91);

    private MediaPlayer soundPlayer;
    private final Random random = new Random();
    private SharedPreferences prefs;
    private LinearLayout root;

    private TextView statsText, wordText, questionText;
    private TextView feedbackText, progressText;
    private Button[] answerButtons;
    private Button nextButton;

    // 敵の情報
    private TextView enemyText, enemyHpText;
    private ProgressBar enemyHpBar;

    // 敵の画像
    private ImageView enemyImageView;
    private int enemyImageRes;

    private String enemyName = "モンスター";

    private int enemyHp;
    private int enemyMaxHp;
    private int enemiesDefeated;
    // クイズ中のアイテムボタン
    private Button btnUsePotion, btnUseFifty, btnUseSkip;
    private boolean fiftyUsedInThisQuestion = false;

    private List<Integer> questionOrder = new ArrayList<>();
    private String[] options = new String[4];

    private int index, xp, lives, streak, correct, sessionXp;
    private int totalCorrect, totalAnswered, bestStreak;
    private boolean answered;
    private String currentScreen = "home";
    private boolean devXpVisible = false;
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
    private String equippedTitle = "AUTO"; // "AUTO"の場合はレベル称号、それ以外は特別称号

    // ガチャデータ（レアアイコン＆特別称号）
    private final String[] RARE_ICONS = {"🦄", "🦖", "🤖", "👽", "💎"};
    private final String[] RARE_ICON_NAMES = {
            "🦄 ユニコーン",
            "🦖 ティラノサウルス",
            "🤖 ロボット",
            "👽 宇宙人",
            "💎 ダイヤの王冠"
    };
    private final String[] RARE_TITLES = {
            "⚡ 神速の英単語ハンター",
            "🌙 眠らぬ夜の探究者",
            "🔥 不屈のチャレンジャー",
            "🌌 異次元の語彙マスター",
            "🍀 奇跡を呼ぶ大冒険者"
    };
    private Set<String> unlockedIcons = new HashSet<>();
    private Set<String> unlockedTitles = new HashSet<>();

    // お助けアイテム（所持数）
    private int itemPotion = 0; // 回復薬
    private int itemFifty = 0;  // 50/50
    private int itemSkip = 0;   // スキップの巻物

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
        equippedTitle = prefs.getString("equippedTitle", "AUTO");

        // ガチャ解放データ読み込み
        unlockedIcons = new HashSet<>(prefs.getStringSet("unlockedIcons", new HashSet<>()));
        unlockedTitles = new HashSet<>(prefs.getStringSet("unlockedTitles", new HashSet<>()));

        // アイテム所持数読み込み
        itemPotion = prefs.getInt("itemPotion", 0);
        itemFifty = prefs.getInt("itemFifty", 0);
        itemSkip = prefs.getInt("itemSkip", 0);

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

    // 通常のボタン（クリック音あり）
    // 枠線付きの角丸背景
    private GradientDrawable roundWithStroke(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    private Button makeButton(String label) {
        return makeButton(label, true);
    }

    // クリック音の有無を指定できるボタン
    private Button makeButton(String label, boolean clickSound) {
        Button b = new androidx.appcompat.widget.AppCompatButton(this) {
            @Override
            public boolean performClick() {
                if (clickSound) {
                    playSound(R.raw.click);
                }
                return super.performClick();
            }
        };

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
    // 効果音を再生する
    private void playSound(int soundId) {

        // 前の音を停止
        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }

        soundPlayer = MediaPlayer.create(this, soundId);
        android.util.Log.d("WordQuestSound",
                getResources().getResourceEntryName(soundId)
                        + (soundPlayer == null ? " 読み込み失敗" : " 読み込み成功"));
        if (soundPlayer != null) {
            soundPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (soundPlayer == mp) {
                    soundPlayer = null;
                }
            });

            soundPlayer.start();
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

    private void saveItemsAndGacha() {
        prefs.edit()
                .putInt("xp", xp)
                .putInt("itemPotion", itemPotion)
                .putInt("itemFifty", itemFifty)
                .putInt("itemSkip", itemSkip)
                .putStringSet("unlockedIcons", unlockedIcons)
                .putStringSet("unlockedTitles", unlockedTitles)
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

    // ホーム画面（統合冒険者カード ＋ 画面いっぱいの縦3×横2タイルメニュー）
    private void showHome() {
        setupScreen("home");

        // 1. 最上部の統合冒険者カード（プロフィール ＋ ステータス横並び）
        LinearLayout topCard = new LinearLayout(this);
        topCard.setOrientation(LinearLayout.HORIZONTAL);
        topCard.setGravity(Gravity.CENTER_VERTICAL);
        topCard.setPadding(16, 12, 16, 12);
        topCard.setBackground(roundWithStroke(Color.rgb(22, 26, 52), 20, GOLD, 2));

        LinearLayout.LayoutParams topCardParams = fullWidth();
        topCardParams.setMargins(0, 0, 0, 14);
        topCard.setLayoutParams(topCardParams);

        // 左：プロフィールボタン（アイコンを大きく強調）
        LinearLayout profilePill = new LinearLayout(this);
        profilePill.setOrientation(LinearLayout.HORIZONTAL);
        profilePill.setGravity(Gravity.CENTER_VERTICAL);
        profilePill.setPadding(18, 8, 22, 8);
        profilePill.setBackground(round(PANEL, 16));
        profilePill.setOnClickListener(v -> showProfile());

        TextView pIconView = new TextView(this);
        pIconView.setText(playerIcon);
        pIconView.setTextSize(24);
        pIconView.setPadding(0, 0, 10, 0);
        profilePill.addView(pIconView);

        TextView pNameView = new TextView(this);
        pNameView.setText(playerName);
        pNameView.setTextSize(14);
        pNameView.setTextColor(WHITE);
        pNameView.setTypeface(null, Typeface.BOLD);
        profilePill.addView(pNameView);

        topCard.addView(profilePill);

        // 右：コンパクトステータス
        TextView miniStats = new TextView(this);
        miniStats.setText("🛡 Lv." + level() + "  |  ⭐ " + xp + " XP\n🏆 正解 : " + totalCorrect + " 問");
        miniStats.setTextSize(13);
        miniStats.setTextColor(WHITE);
        miniStats.setGravity(Gravity.END);
        LinearLayout.LayoutParams miniStatsParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        miniStats.setLayoutParams(miniStatsParams);
        topCard.addView(miniStats);

        root.addView(topCard);

        // 2. お城シンボル＆タイトル
        TextView castle = text("🏰", 80, GOLD);
        castle.setPadding(0, 0, 0, 4);
        root.addView(castle);

        TextView title = text("⚔ WORD QUEST ⚔", 26, GOLD);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 2, 0, 2);
        root.addView(title, fullWidth());

        TextView subtitle = text("英単語を極め、伝説の冒険者になれ。", 12, WHITE);
        subtitle.setPadding(0, 0, 0, 14);
        root.addView(subtitle, fullWidth());

        // 3. メインアクションボタン（冒険を始める）
        Button start = makeButton("⚔  冒険を始める");
        start.setTextSize(20);
        start.setTypeface(null, Typeface.BOLD);
        start.setPadding(0, 24, 0, 24);
        start.setBackground(round(PURPLE, 22));
        LinearLayout.LayoutParams startParams = fullWidth();
        startParams.setMargins(0, 0, 0, 14);
        start.setLayoutParams(startParams);
        root.addView(start);
        start.setOnClickListener(v -> showQuestSelect());

        // 4. 画面いっぱいに広がる 縦3段 × 横2列のタイルグリッドメニュー
        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        gridContainer.setLayoutParams(gridParams);

        String loginBadge = !loginBonusReceived ? " ✨" : "";

        // 1段目：ログボ / ミッション
        LinearLayout row1 = makeGridRow(true);
        row1.addView(makeTileButton("🎁", "ログボ" + loginBadge, v -> showLoginBonus()));
        row1.addView(makeTileButton("🎯", "ミッション", v -> showDailyMissions()));
        gridContainer.addView(row1);

        // 2段目：ガチャ / ショップ
        LinearLayout row2 = makeGridRow(true);
        row2.addView(makeTileButton("🎰", "ガチャ", v -> showGacha()));
        row2.addView(makeTileButton("🛒", "ショップ", v -> showShop()));
        gridContainer.addView(row2);

        // 3段目：単語図鑑 / 学習記録
        LinearLayout row3 = makeGridRow(false);
        row3.addView(makeTileButton("📖", "単語図鑑", v -> showDictionary()));
        row3.addView(makeTileButton("🏆", "学習記録", v -> showStatus()));
        gridContainer.addView(row3);

        root.addView(gridContainer);
        // 開発者用：デバッグ実行時だけ表示
        if (devXpVisible && (getApplicationInfo().flags
                & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {

            Button devXpButton = makeButton("開発者用：+1000 XP");
            devXpButton.setBackground(round(TEAL, 18));
            root.addView(devXpButton);

            devXpButton.setOnClickListener(v -> {
                xp += 1000;
                saveData();  // XPを保存
                showHome();  // XP・レベル表示を更新

                Toast.makeText(
                        this,
                        "開発者用：1000 XPを追加しました！",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    // 縦3段の均等な行コンテナを作成
    private LinearLayout makeGridRow(boolean hasBottomMargin) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        if (hasBottomMargin) {
            p.setMargins(0, 0, 0, 12);
        }
        row.setLayoutParams(p);
        return row;
    }

    // 横2列グリッド用のタイルボタンを作成（画面いっぱいに広がる高さ＆大きなアイコン）
    private LinearLayout makeTileButton(String emoji, String label, android.view.View.OnClickListener listener) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(8, 12, 8, 12);
        tile.setBackground(roundWithStroke(PANEL, 22, Color.rgb(52, 60, 105), 2));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
        p.setMargins(6, 0, 6, 0);
        tile.setLayoutParams(p);

        TextView iconView = new TextView(this);
        iconView.setText(emoji);
        iconView.setTextSize(54);
        iconView.setGravity(Gravity.CENTER);
        iconView.setPadding(0, 0, 0, 6);
        tile.addView(iconView);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(WHITE);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setGravity(Gravity.CENTER);
        tile.addView(labelView);

        tile.setOnClickListener(listener);
        return tile;
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

        Button b = makeButton("このクエストに挑戦 →", false);
        root.addView(b);

        b.setOnClickListener(v -> {
            questName = title;
            startQuest(start, end, title.contains("BOSS") ? 10 : 20);
        });
    }

    // 敵のデータ
    private static class Enemy {
        String name;
        int imageRes;
        int maxHp;

        Enemy(String name, int imageRes, int maxHp) {
            this.name = name;
            this.imageRes = imageRes;
            this.maxHp = maxHp;
        }
    }

    // クエストに応じてランダムに敵を選択
    private void selectRandomEnemy() {

        Enemy[] enemies;

        if (questName.contains("FOREST")) {

            enemies = new Enemy[]{
                    new Enemy("スライムマスコット", R.drawable.slime, 30),
                    new Enemy("紅蓮のチビドラゴン", R.drawable.chibi_, 30)
            };

        } else if (questName.contains("CAVE")) {

            enemies = new Enemy[]{
                    new Enemy("ネオンゴーストスライム", R.drawable.ghost, 30),
                    new Enemy("守護おばあちゃん", R.drawable.obaa, 30)
            };

        } else if (questName.contains("CASTLE")) {

            enemies = new Enemy[]{
                    new Enemy("ゾンビ侍", R.drawable.samurai, 40),
                    new Enemy("冥王レンデロス", R.drawable.king, 50)
            };

        } else {

            enemies = new Enemy[]{
                    new Enemy("キング", R.drawable.king, 50)
            };
        }

        // 倒した直前の敵を除いて、次の敵の候補を作る
        List<Enemy> candidates = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (enemiesDefeated == 0 || enemy.imageRes != enemyImageRes) {
                candidates.add(enemy);
            }
        }

// 敵が1種類だけの場合は、同じ敵を再登場させる
        if (candidates.isEmpty()) {
            Collections.addAll(candidates, enemies);
        }

// 残った候補からランダムに1体選択
        Enemy selected = candidates.get(random.nextInt(candidates.size()));

        // 敵の情報を設定
        enemyName = selected.name;
        enemyImageRes = selected.imageRes;
        enemyMaxHp = selected.maxHp;
        enemyHp = enemyMaxHp;
    }

    private void startQuest(int start, int end, int count) {
        playSound(R.raw.game_start);
        index = 0;
        lives = 3;
        streak = 0;
        correct = 0;
        sessionXp = 0;
        answered = false;

        // 討伐数を初期化
        enemiesDefeated = 0;

// 最初の敵をランダムに選択
        selectRandomEnemy();

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

        // 敵の名前
        enemyText = text("", 24, WHITE);
        enemyText.setPadding(0, 12, 0, 4);
        root.addView(enemyText, fullWidth());

// 敵の画像
        enemyImageView = new ImageView(this);

        enemyImageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

// 画像の表示サイズ（160dp）
        int imageHeight = (int) (
                160 * getResources().getDisplayMetrics().density
        );

        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        imageHeight
                );

        imageParams.setMargins(0, 8, 0, 8);

        root.addView(enemyImageView, imageParams);

// 敵のHP表示
        enemyHpText = text("", 15, GOLD);
        root.addView(enemyHpText, fullWidth());

// 敵のHPバー
        enemyHpBar = new ProgressBar(
                this, null,
                android.R.attr.progressBarStyleHorizontal
        );

        enemyHpBar.setMax(enemyMaxHp);

        LinearLayout.LayoutParams hpParams = fullWidth();
        hpParams.setMargins(0, 6, 0, 6);

        root.addView(enemyHpBar, hpParams);

// 敵の表示を更新
        updateEnemyDisplay();

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
            answerButtons[i] = makeButton("", false);
            root.addView(answerButtons[i]);
            answerButtons[i].setOnClickListener(
                    v -> checkAnswer(selected)
            );
        }

        // お助けアイテム使用エリア
        TextView itemHeader = text("🎒 お助けアイテム", 14, GOLD);
        itemHeader.setPadding(0, 16, 0, 4);
        root.addView(itemHeader);

        btnUsePotion = makeButton("");
        btnUsePotion.setBackground(round(TEAL, 14));
        root.addView(btnUsePotion);
        btnUsePotion.setOnClickListener(v -> usePotion());

        btnUseFifty = makeButton("");
        btnUseFifty.setBackground(round(TEAL, 14));
        root.addView(btnUseFifty);
        btnUseFifty.setOnClickListener(v -> useFiftyFifty());

        btnUseSkip = makeButton("");
        btnUseSkip.setBackground(round(TEAL, 14));
        root.addView(btnUseSkip);
        btnUseSkip.setOnClickListener(v -> useSkipScroll());

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

        // 前の問題で敵を倒していたら新しい敵を出現させる
        if (enemyHp <= 0) {
            selectRandomEnemy();
        }

        updateEnemyDisplay();

        fiftyUsedInThisQuestion = false;
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
        updateItemButtons();
    }

    // アイテムボタンの表示と有効/無効更新
    private void updateItemButtons() {
        btnUsePotion.setText("❤️ 回復薬を使う (所持: " + itemPotion + ")");
        btnUsePotion.setEnabled(itemPotion > 0 && lives < 3 && lives > 0);
        btnUsePotion.setAlpha(btnUsePotion.isEnabled() ? 1.0f : 0.45f);

        btnUseFifty.setText("🔍 50/50を使う (所持: " + itemFifty + ")");
        btnUseFifty.setEnabled(itemFifty > 0 && !answered && !fiftyUsedInThisQuestion);
        btnUseFifty.setAlpha(btnUseFifty.isEnabled() ? 1.0f : 0.45f);

        btnUseSkip.setText("⏭️ スキップの巻物を使う (所持: " + itemSkip + ")");
        btnUseSkip.setEnabled(itemSkip > 0 && !answered);
        btnUseSkip.setAlpha(btnUseSkip.isEnabled() ? 1.0f : 0.45f);
    }

    // アイテム1: 回復薬
    private void usePotion() {
        if (itemPotion <= 0 || lives >= 3) return;
        itemPotion--;
        lives++;
        saveItemsAndGacha();
        updateStats();
        updateItemButtons();
        Toast.makeText(this, "❤️ ライフが1つ回復した！", Toast.LENGTH_SHORT).show();
    }

    // アイテム2: 50/50
    private void useFiftyFifty() {
        if (itemFifty <= 0 || answered || fiftyUsedInThisQuestion) return;
        itemFifty--;
        fiftyUsedInThisQuestion = true;
        saveItemsAndGacha();

        int wi = questionOrder.get(index);
        String correctAnswer = words[wi][1];

        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (!options[i].equals(correctAnswer)) {
                wrongIndices.add(i);
            }
        }
        Collections.shuffle(wrongIndices);

        // ハズレを2つ無効化
        for (int i = 0; i < 2; i++) {
            int idx = wrongIndices.get(i);
            answerButtons[idx].setEnabled(false);
            answerButtons[idx].setText("✕  ---");
            answerButtons[idx].setBackground(round(Color.rgb(22, 25, 48), 14));
        }

        updateItemButtons();
        Toast.makeText(this, "🔍 ハズレの選択肢を2つ消去した！", Toast.LENGTH_SHORT).show();
    }

    // アイテム3: スキップの巻物
    private void useSkipScroll() {
        if (itemSkip <= 0 || answered) return;
        itemSkip--;
        saveItemsAndGacha();

        answered = true;
        int wi = questionOrder.get(index);
        feedbackText.setText("⏭️ 問題をスキップ！ 正解：" + words[wi][1]);

        for (Button b : answerButtons) {
            b.setEnabled(false);
        }

        updateItemButtons();

        if (index >= questionOrder.size() - 1) {
            nextButton.setText("結果を見る 🏆");
        }
        Toast.makeText(this, "⏭️ スキップの巻物を使った！", Toast.LENGTH_SHORT).show();
    }

    // 正誤判定
    private void checkAnswer(int selected) {
        if (answered) return;

        checkMissionReset();
        answered = true;
        totalAnswered++;

        int wi = questionOrder.get(index);

        if (options[selected].equals(words[wi][1])) {
            playSound(R.raw.correct);

            // 敵に10ダメージ！
            int damage = 10;
            enemyHp -= damage;

            if (enemyHp <= 0) {
                enemyHp = 0;
                enemiesDefeated++;
            }

            // 敵のHP表示を更新
            updateEnemyDisplay();

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
                            + "\n⚔ 敵に10ダメージ！"
                            + (enemyHp <= 0 ? "\n💀 敵を倒した！" : "")
                            + (streak % 3 == 0 ? " 🔥 COMBO!" : "")
            );

            answerButtons[selected].setBackground(
                    round(TEAL, 14)
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
        updateItemButtons();
        saveData();
        saveMissionData();

        if (lives <= 0 ||
                index >= questionOrder.size() - 1) {
            nextButton.setText("結果を見る 🏆");
        }
    }

    // ステータス更新
    // 敵の表示を更新
    private void updateEnemyDisplay() {

        if (enemyText == null ||
                enemyHpText == null ||
                enemyHpBar == null ||
                enemyImageView == null) return;

        // 敵の画像を更新
        enemyImageView.setImageResource(enemyImageRes);

        // 敵の名前を更新
        if (enemyHp <= 0) {
            enemyText.setText("💀 " + enemyName + "を倒した！");
        } else {
            enemyText.setText("⚔ " + enemyName);
        }

        // HPの文字を更新
        enemyHpText.setText(
                "敵のHP：" + enemyHp + " / " + enemyMaxHp
                        + "  討伐数：" + enemiesDefeated
        );

        // HPバーを更新
        enemyHpBar.setMax(enemyMaxHp);
        enemyHpBar.setProgress(enemyHp);
    }
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
                        + "\n\n倒した敵：" + enemiesDefeated + "体"
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

    // アイテムショップ画面
    private void showShop() {
        setupScreen("shop");
        addTitle("🛒 ITEM SHOP", "XPでお助けアイテムを購入しよう！");

        TextView xpView = text("⭐ 現在の所持 XP : " + xp, 20, GOLD);
        xpView.setPadding(16, 18, 16, 18);
        xpView.setBackground(round(PANEL, 18));
        root.addView(xpView, fullWidth());

        addShopItem(
                "❤️ 回復薬（ポーション）",
                "クエスト中に減ったライフを1つ回復する",
                50,
                itemPotion,
                () -> {
                    itemPotion++;
                    saveItemsAndGacha();
                    showShop();
                }
        );

        addShopItem(
                "🔍 50/50（フィフティ・フィフティ）",
                "4択のうちハズレを2つ消して2択にする",
                80,
                itemFifty,
                () -> {
                    itemFifty++;
                    saveItemsAndGacha();
                    showShop();
                }
        );

        addShopItem(
                "⏭️ スキップの巻物",
                "どうしても分からない問題を1問パスする",
                60,
                itemSkip,
                () -> {
                    itemSkip++;
                    saveItemsAndGacha();
                    showShop();
                }
        );

        addBackButton();
    }

    private void addShopItem(String name, String desc, int cost, int owned, Runnable onBuy) {
        TextView card = text(
                name + "\n" + desc + "\n\n所持数: " + owned + " 個",
                15, WHITE
        );
        card.setPadding(16, 18, 16, 18);
        card.setBackground(round(PANEL, 18));
        LinearLayout.LayoutParams p = fullWidth();
        p.setMargins(0, 12, 0, 4);
        root.addView(card, p);

        Button buyBtn = makeButton("💰 " + cost + " XP で購入する");
        buyBtn.setBackground(round(PURPLE, 16));
        root.addView(buyBtn);

        buyBtn.setOnClickListener(v -> {
            if (xp < cost) {
                Toast.makeText(this, "XPが足りません！", Toast.LENGTH_SHORT).show();
                return;
            }
            xp -= cost;
            onBuy.run();
            Toast.makeText(this, name + " を購入しました！", Toast.LENGTH_SHORT).show();
        });
    }

    // レアアイコン＆特別称号ガチャ画面
    private void showGacha() {
        setupScreen("gacha");
        addTitle("🎰 ADVENTURER GACHA", "レアアイコン＆特別称号を手に入れろ！");

        int totalItems = RARE_ICONS.length + RARE_TITLES.length;
        int unlockedCount = unlockedIcons.size() + unlockedTitles.size();

        TextView statusCard = text(
                "⭐ 現在の所持 XP : " + xp
                        + "\n🎁 コンプリート率 : " + unlockedCount + " / " + totalItems,
                18, GOLD
        );
        statusCard.setPadding(16, 20, 16, 20);
        statusCard.setBackground(round(PANEL, 20));
        root.addView(statusCard, fullWidth());

        Button pullBtn = makeButton("🎰 1回ガチャを回す (100 XP)");
        pullBtn.setTextSize(18);
        pullBtn.setBackground(round(Color.rgb(180, 110, 30), 20));
        LinearLayout.LayoutParams btnParams = fullWidth();
        btnParams.setMargins(0, 16, 0, 16);
        pullBtn.setLayoutParams(btnParams);
        root.addView(pullBtn);

        pullBtn.setOnClickListener(v -> spinGacha());

        // 排出ラインナップ一覧
        StringBuilder listSb = new StringBuilder("【 排出ラインナップ 】\n\n");
        listSb.append("▼ レアアイコン\n");
        for (int i = 0; i < RARE_ICONS.length; i++) {
            boolean has = unlockedIcons.contains(RARE_ICONS[i]);
            listSb.append(has ? "✅ " : "🔒 ").append(RARE_ICON_NAMES[i]).append("\n");
        }
        listSb.append("\n▼ 特別称号\n");
        for (String title : RARE_TITLES) {
            boolean has = unlockedTitles.contains(title);
            listSb.append(has ? "✅ " : "🔒 ").append(title).append("\n");
        }

        TextView listView = text(listSb.toString(), 15, WHITE);
        listView.setGravity(Gravity.START);
        listView.setPadding(20, 20, 20, 20);
        listView.setBackground(round(PANEL, 18));
        root.addView(listView, fullWidth());

        addBackButton();
    }

    private void spinGacha() {
        int cost = 100;
        if (xp < cost) {
            Toast.makeText(this, "XPが足りません！（100 XP必要）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 未所持の候補リストを作成
        List<String> lockedPool = new ArrayList<>();
        for (int i = 0; i < RARE_ICONS.length; i++) {
            if (!unlockedIcons.contains(RARE_ICONS[i])) {
                lockedPool.add("ICON:" + i);
            }
        }
        for (int i = 0; i < RARE_TITLES.length; i++) {
            if (!unlockedTitles.contains(RARE_TITLES[i])) {
                lockedPool.add("TITLE:" + i);
            }
        }

        if (lockedPool.isEmpty()) {
            Toast.makeText(this, "🎉 すでに全アイテムをコンプリートしています！", Toast.LENGTH_LONG).show();
            return;
        }

        xp -= cost;
        String picked = lockedPool.get(random.nextInt(lockedPool.size()));
        String resultMessage;

        if (picked.startsWith("ICON:")) {
            int idx = Integer.parseInt(picked.split(":")[1]);
            unlockedIcons.add(RARE_ICONS[idx]);
            resultMessage = "✨ レアアイコン獲得！\n\n" + RARE_ICON_NAMES[idx] + "\n\n（プロフィールから変更できます）";
        } else {
            int idx = Integer.parseInt(picked.split(":")[1]);
            unlockedTitles.add(RARE_TITLES[idx]);
            resultMessage = "👑 特別称号獲得！\n\n「" + RARE_TITLES[idx] + "」\n\n（プロフィールから変更できます）";
        }

        saveItemsAndGacha();
        showGacha();

        new android.app.AlertDialog.Builder(this)
                .setTitle("🎉 ガチャ結果！")
                .setMessage(resultMessage)
                .setPositiveButton("やったー！", null)
                .show();
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

    // 開発者用：音量を下げるキーでXPボタンを切り替える
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean isDebug = (getApplicationInfo().flags
                & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        if (isDebug
                && "home".equals(currentScreen)
                && event.getKeyCode()
                == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {

            // 長押しによる連続切り替えを防ぐ
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                devXpVisible = !devXpVisible;
                showHome();
            }

            return true;
        }

        return super.dispatchKeyEvent(event);
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
    private String getLevelTitleRank() {
        int lv = level();
        if (lv >= 30) return "👑 伝説のワードマスター";
        if (lv >= 20) return "🧙‍♂️ 叡智の大賢者";
        if (lv >= 10) return "⚔️ 言葉の熟練騎士";
        if (lv >= 5)  return "🛡️ 一人前の冒険者";
        return "🌱 見習い冒険者";
    }

    // 現在装備中の称号を取得
    private String getCurrentTitle() {
        if ("AUTO".equals(equippedTitle)) {
            return getLevelTitleRank();
        }
        return equippedTitle;
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
                        + "\n\n【 称号 】\n" + getCurrentTitle()
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

        Button editTitleBtn = makeButton("👑 称号を変更する");
        editTitleBtn.setBackground(round(PURPLE, 18));
        root.addView(editTitleBtn);
        editTitleBtn.setOnClickListener(v -> showTitleSelectDialog());

        Button editNameBtn = makeButton("✏️ 名前を変更する");
        root.addView(editNameBtn);
        editNameBtn.setOnClickListener(v -> showEditDialog("名前を入力", playerName, true));

        Button editGoalBtn = makeButton("🎯 目標を変更する");
        root.addView(editGoalBtn);
        editGoalBtn.setOnClickListener(v -> showEditDialog("目標を入力", playerGoal, false));

        addBackButton();
    }

    // アイコン選択用のポップアップ画面（基本アイコン＋ガチャ解放レアアイコン）
    private void showIconSelectDialog() {
        List<String> displayList = new ArrayList<>();
        List<String> emojiList = new ArrayList<>();

        String[] baseNames = {
                "⚔️ 剣士", "🧙‍♂️ 魔法使い", "🛡️ 重装騎士", "🏹 弓使い",
                "🥷 忍者", "👑 王様", "🐉 ドラゴン", "🐱 ネコ冒険者"
        };
        String[] baseEmojis = {"⚔️", "🧙‍♂️", "🛡️", "🏹", "🥷", "👑", "🐉", "🐱"};

        Collections.addAll(displayList, baseNames);
        Collections.addAll(emojiList, baseEmojis);

        // ガチャで解放済みのレアアイコンを追加
        for (int i = 0; i < RARE_ICONS.length; i++) {
            if (unlockedIcons.contains(RARE_ICONS[i])) {
                displayList.add("【レア】" + RARE_ICON_NAMES[i]);
                emojiList.add(RARE_ICONS[i]);
            }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("アイコンを選択")
                .setItems(displayList.toArray(new String[0]), (dialog, which) -> {
                    playerIcon = emojiList.get(which);
                    prefs.edit().putString("playerIcon", playerIcon).apply();
                    showProfile();
                    Toast.makeText(this, "アイコンを変更しました！", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // 称号選択用のポップアップ画面（レベル称号＋ガチャ解放特別称号）
    private void showTitleSelectDialog() {
        List<String> displayList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();

        displayList.add("自動（レベル称号：" + getLevelTitleRank() + "）");
        valueList.add("AUTO");

        for (String title : unlockedTitles) {
            displayList.add("【特別】" + title);
            valueList.add(title);
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("称号を選択")
                .setItems(displayList.toArray(new String[0]), (dialog, which) -> {
                    equippedTitle = valueList.get(which);
                    prefs.edit().putString("equippedTitle", equippedTitle).apply();
                    showProfile();
                    Toast.makeText(this, "称号を変更しました！", Toast.LENGTH_SHORT).show();
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