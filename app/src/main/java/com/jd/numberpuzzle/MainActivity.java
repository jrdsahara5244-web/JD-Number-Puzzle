package com.jd.numberpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {

    GameView game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideBars();

        game = new GameView(this);
        setContentView(game);
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideBars();
        }
    }

    // ============================================================
    // GAME VIEW
    // ============================================================

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random = new Random();

        ArrayList<Integer> numbers = new ArrayList<>();

        // फक्त 4 नंबर
        RectF[] numberBoxes = new RectF[4];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();

        // मोठा Hint Box
        RectF hintButton = new RectF();

        RectF homeStartButton = new RectF();
        RectF homeContinueButton = new RectF();
        RectF homeScoreButton = new RectF();
        RectF homeSettingsButton = new RectF();

        int level = 1;
        int score = 0;
        int roundCorrect = 0;
        int roundWrong = 0;

        int selected1 = -1;
        int selected2 = -1;

        int target = 0;

        String operation = "";
        String question = "";
        String status = "";

        boolean gameStarted = false;

        // ============================================================
        // COLORS
        // ============================================================

        final int backgroundColor = Color.rgb(4, 8, 20);
        final int cardColor = Color.rgb(17, 23, 40);
        final int boxColor = Color.rgb(13, 24, 45);

        final int yellow = Color.rgb(255, 211, 35);
        final int goldLight = Color.rgb(255, 239, 155);

        final int blue = Color.rgb(30, 145, 255);

        final int white = Color.WHITE;

        final int green = Color.rgb(0, 215, 90);
        final int red = Color.rgb(225, 25, 40);
        final int purple = Color.rgb(115, 45, 225);
        final int orange = Color.rgb(225, 145, 5);

        // ============================================================
        // CONSTRUCTOR
        // ============================================================

        GameView(Context context) {
            super(context);

            setFocusable(true);

            newPuzzle();
        }

        // ============================================================
        // IMPORTANT
        // आता प्रत्येक level मध्ये फक्त 4 नंबर
        // ============================================================

        int boxCount() {
            return 4;
        }

        // ============================================================
        // 4 नंबर एका रांगेत
        // ============================================================

        int columns() {
            return 4;
        }

        // ============================================================
        // RANDOM NUMBER
        // ============================================================

        int smallNumber() {

            if (random.nextInt(100) < 25) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        boolean containsNumber(int n) {

            for (int x : numbers) {

                if (x == n) {
                    return true;
                }
            }

            return false;
        }

        // ============================================================
        // FILL ONLY 4 NUMBERS
        // ============================================================

        void fillNumbers() {

            int count = boxCount();

            while (numbers.size() < count) {

                int n = smallNumber();

                if (!containsNumber(n)) {
                    numbers.add(n);
                }
            }

            Collections.shuffle(numbers, random);
        }

        // ============================================================
        // NEW PUZZLE
        // ============================================================

        void newPuzzle() {

            numbers.clear();

            selected1 = -1;
            selected2 = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;

            // ========================================================
            // ADDITION
            // ========================================================

            if (type == 0) {

                operation = "+";

                do {

                    x = smallNumber();
                    y = smallNumber();

                    target = x + y;

                } while (target < 10 || target > 150);

                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target
                                + " मिळेल?";

            }

            // ========================================================
            // SUBTRACTION
            // ========================================================

            else if (type == 1) {

                operation = "−";

                do {

                    x = smallNumber();
                    y = smallNumber();

                } while (x <= y);

                target = x - y;

                question =
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर "
                                + "वजा केल्यावर "
                                + target
                                + " मिळेल?";

            }

            // ========================================================
            // MULTIPLICATION
            // ========================================================

            else if (type == 2) {

                operation = "×";

                do {

                    x = 10 + random.nextInt(90);
                    y = 10 + random.nextInt(90);

                    target = x * y;

                } while (target < 1000 || target > 9999);

                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target
                                + " मिळेल?";

            }

            // ========================================================
            // DIVISION
            // ========================================================

            else {

                operation = "÷";

                do {

                    y = 2 + random.nextInt(8);

                    target = 2 + random.nextInt(10);

                    x = y * target;

                } while (x > 99);

                question =
                        "कोणता नंबर कोणत्या नंबरने भागल्यावर "
                                + target
                                + " मिळेल?";
            }

            // योग्य दोन नंबर
            numbers.add(x);
            numbers.add(y);

            // उरलेले 2 नंबर
            fillNumbers();

            invalidate();
        }

        // ============================================================
        // CHECK MATHEMATICAL ANSWER
        // ============================================================

        boolean isCorrect(int x, int y) {

            if (operation.equals("+")) {

                return x + y == target;
            }

            if (operation.equals("−")) {

                return x > y && x - y == target;
            }

            if (operation.equals("×")) {

                return x * y == target;
            }

            return y != 0 &&
                    x % y == 0 &&
                    x / y == target;
        }

        // ============================================================
        // CHECK ANSWER
        // ============================================================

        void checkAnswer() {

            if (!gameStarted) {
                return;
            }

            if (selected1 < 0 || selected2 < 0) {

                status = "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first = numbers.get(selected1);
            int second = numbers.get(selected2);

            if (isCorrect(first, second)) {

                score++;
                roundCorrect++;

                status = "✓ बरोबर! +1 गुण";

                invalidate();

                postDelayed(() -> {

                    if (level % 10 == 0) {

                        showResult(level == 1000);

                    } else {

                        if (level < 1000) {
                            level++;
                        }

                        newPuzzle();
                    }

                }, 650);

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                invalidate();
            }
        }

        // ============================================================
        // START GAME
        // ============================================================

        void startGame() {

            gameStarted = true;

            level = 1;
            score = 0;

            roundCorrect = 0;
            roundWrong = 0;

            newPuzzle();

            invalidate();
        }

        // ============================================================
        // CONTINUE GAME
        // ============================================================

        void continueGame() {

            gameStarted = true;

            invalidate();
        }

        // ============================================================
        // RESET
        // ============================================================

        void resetGame() {

            level = 1;

            score = 0;

            roundCorrect = 0;
            roundWrong = 0;

            selected1 = -1;
            selected2 = -1;

            status = "";

            newPuzzle();

            invalidate();
        }

        // ============================================================
        // RESULT
        // ============================================================

        void showResult(boolean finalGame) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(
                    finalGame
                            ? "JD NUMBER PUZZLE पूर्ण!"
                            : "RESULT"
            );

            builder.setMessage(
                    "Level : " + level +

                            "\n\n✓ बरोबर : "
                            + roundCorrect +

                            "\n✗ चुकले : "
                            + roundWrong +

                            "\n\n⭐ एकूण गुण : "
                            + score
            );

            builder.setPositiveButton(
                    "पुढे खेळा",
                    (dialog, which) -> {

                        if (level < 1000) {

                            level++;

                            newPuzzle();

                            invalidate();
                        }
                    }
            );

            builder.setNegativeButton(
                    "ठीक आहे",
                    null
            );

            builder.show();
        }

        // ============================================================
        // SCORE
        // ============================================================

        void showScore() {

            new AlertDialog.Builder(MainActivity.this)

                    .setTitle("MY SCORE")

                    .setMessage(
                            "Level : " + level +

                                    "\n\nएकूण गुण : "
                                    + score +

                                    "\n✓ बरोबर : "
                                    + roundCorrect +

                                    "\n✗ चुकले : "
                                    + roundWrong
                    )

                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )

                    .show();
        }

        // ============================================================
        // SETTINGS
        // ============================================================

        void showSettings() {

            new AlertDialog.Builder(MainActivity.this)

                    .setTitle("SETTINGS")

                    .setMessage(
                            "JD NUMBER PUZZLE\n\n" +

                                    "• 1000 Levels\n" +
                                    "• No Timer\n" +
                                    "• Mathematical Puzzle\n" +
                                    "• Royal Gold Theme\n" +
                                    "• 4 Number Boxes\n" +
                                    "• Level 401+ Hard Mode"
                    )

                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )

                    .show();
        }

        // ============================================================
        // HINT
        // ============================================================

        void showHint() {

            String hint;

            if (operation.equals("+")) {

                hint =
                        "सूचना: "
                                + target
                                + " होण्यासाठी दोन नंबरची बेरीज करा.";

            }

            else if (operation.equals("−")) {

                hint =
                        "सूचना: मोठ्या नंबरमधून छोटा नंबर वजा करा.";

            }

            else if (operation.equals("×")) {

                hint =
                        "सूचना: "
                                + target
                                + " चे दोन गुणक शोधा.";

            }

            else {

                hint =
                        "सूचना: भागाकार केल्यावर उत्तर "
                                + target
                                + " आले पाहिजे.";
            }

            new AlertDialog.Builder(MainActivity.this)

                    .setTitle("HINT")

                    .setMessage(hint)

                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )

                    .show();
        }

        // ============================================================
        // DRAW TEXT
        // ============================================================

        void drawText(
                Canvas c,
                String text,
                float x,
                float y,
                float size,
                int color,
                boolean bold
        ) {

            p.setStyle(Paint.Style.FILL);

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            c.drawText(text, x, y, p);
        }

        // ============================================================
        // CENTER TEXT
        // ============================================================

        void centerText(
                Canvas c,
                String text,
                RectF r,
                float size,
                int color
        ) {

            p.setStyle(Paint.Style.FILL);

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(Typeface.DEFAULT_BOLD);

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float x =
                    r.centerX()
                            - p.measureText(text) / 2f;

            float y =
                    r.centerY()
                            - (fm.ascent + fm.descent) / 2f;

            c.drawText(text, x, y, p);
        }

        // ============================================================
        // ROUNDED RECT
        // ============================================================

        void roundedRect(
                Canvas c,
                RectF r,
                int color,
                float radius
        ) {

            p.setStyle(Paint.Style.FILL);

            p.setColor(color);

            c.drawRoundRect(
                    r,
                    radius,
                    radius,
                    p
            );
        }

        // ============================================================
        // BORDER
        // ============================================================

        void border(
                Canvas c,
                RectF r,
                int color,
                float width,
                float radius
        ) {

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(width);

            p.setColor(color);

            c.drawRoundRect(
                    r,
                    radius,
                    radius,
                    p
            );

            p.setStyle(Paint.Style.FILL);
        }

        // ============================================================
        // STAR
        // ============================================================

        void drawStar(
                Canvas c,
                float cx,
                float cy,
                float radius
        ) {

            p.setColor(yellow);

            p.setStyle(Paint.Style.FILL);

            Path path = new Path();

            for (int i = 0; i < 10; i++) {

                double a =
                        -Math.PI / 2
                                + i * Math.PI / 5;

                float rr =
                        (i % 2 == 0)
                                ? radius
                                : radius * .42f;

                float x =
                        cx
                                + (float) Math.cos(a)
                                * rr;

                float y =
                        cy
                                + (float) Math.sin(a)
                                * rr;

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            path.close();

            c.drawPath(path, p);
        }

        // ============================================================
        // ROYAL BACKGROUND
        // ============================================================

        void drawRoyalBackground(
                Canvas c,
                int w,
                int h
        ) {

            c.drawColor(backgroundColor);

            p.setStyle(Paint.Style.FILL);

            p.setColor(
                    Color.rgb(8, 17, 36)
            );

            c.drawCircle(
                    w * .05f,
                    h * .15f,
                    w * .28f,
                    p
            );

            c.drawCircle(
                    w * .96f,
                    h * .65f,
                    w * .30f,
                    p
            );

            c.drawCircle(
                    w * .85f,
                    h * .08f,
                    w * .17f,
                    p
            );

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(1.2f);

            p.setColor(
                    Color.rgb(24, 39, 70)
            );

            for (int i = 0; i < 7; i++) {

                float y =
                        h * .20f
                                + i * h * .11f;

                c.drawLine(
                        w * .04f,
                        y,
                        w * .96f,
                        y,
                        p
                );
            }

            p.setStyle(Paint.Style.FILL);

            drawStar(
                    c,
                    w * .10f,
                    h * .08f,
                    10
            );

            drawStar(
                    c,
                    w * .89f,
                    h * .10f,
                    9
            );

            drawStar(
                    c,
                    w * .14f,
                    h * .75f,
                    7
            );

            drawStar(
                    c,
                    w * .86f,
                    h * .78f,
                    8
            );
        }

        // ============================================================
        // CROWN LOGO
        // Crown नंतर title साठी स्वतंत्र जागा
        // ============================================================

        void drawCrownLogo(
                Canvas c,
                float cx,
                float cy,
                float size
        ) {

            p.setStyle(Paint.Style.FILL);

            p.setColor(
                    Color.rgb(255, 193, 20)
            );

            Path crown = new Path();

            crown.moveTo(
                    cx - size * .52f,
                    cy + size * .20f
            );

            crown.lineTo(
                    cx - size * .70f,
                    cy - size * .42f
            );

            crown.lineTo(
                    cx - size * .22f,
                    cy - size * .12f
            );

            crown.lineTo(
                    cx,
                    cy - size * .58f
            );

            crown.lineTo(
                    cx + size * .22f,
                    cy - size * .12f
            );

            crown.lineTo(
                    cx + size * .70f,
                    cy - size * .42f
            );

            crown.lineTo(
                    cx + size * .52f,
                    cy + size * .20f
            );

            crown.close();

            c.drawPath(crown, p);

            RectF base =
                    new RectF(
                            cx - size * .55f,
                            cy + size * .10f,
                            cx + size * .55f,
                            cy + size * .36f
                    );

            border(
                    c,
                    base,
                    goldLight,
                    3,
                    8
            );
        }

        // ============================================================
        // MENU BUTTON
        // ============================================================

        void drawMenuButton(
                Canvas c,
                RectF r,
                String text,
                int color,
                float size
        ) {

            RectF shadow =
                    new RectF(
                            r.left,
                            r.top + 7,
                            r.right,
                            r.bottom + 7
                    );

            roundedRect(
                    c,
                    shadow,
                    Color.argb(
                            120,
                            0,
                            0,
                            0
                    ),
                    20
            );

            roundedRect(
                    c,
                    r,
                    color,
                    20
            );

            border(
                    c,
                    r,
                    yellow,
                    2.5f,
                    20
            );

            centerText(
                    c,
                    text,
                    r,
                    size,
                    white
            );
        }

        // ============================================================
        // HOME SCREEN
        // ============================================================

        void drawHome(
                Canvas c,
                int w,
                int h
        ) {

            drawRoyalBackground(
                    c,
                    w,
                    h
            );

            RectF scoreTop =
                    new RectF(
                            w * .045f,
                            h * .025f,
                            w * .23f,
                            h * .105f
                    );

            RectF settingsTop =
                    new RectF(
                            w * .77f,
                            h * .025f,
                            w * .955f,
                            h * .105f
                    );

            roundedRect(
                    c,
                    scoreTop,
                    Color.rgb(8, 18, 38),
                    16
            );

            border(
                    c,
                    scoreTop,
                    yellow,
                    2,
                    16
            );

            centerText(
                    c,
                    "★",
                    new RectF(
                            scoreTop.left,
                            scoreTop.top + 2,
                            scoreTop.right,
                            scoreTop.centerY() + 3
                    ),
                    Math.max(20, w * .055f),
                    yellow
            );

            centerText(
                    c,
                    "SCORE",
                    new RectF(
                            scoreTop.left,
                            scoreTop.centerY(),
                            scoreTop.right,
                            scoreTop.bottom
                    ),
                    Math.max(12, w * .027f),
                    white
            );

            roundedRect(
                    c,
                    settingsTop,
                    Color.rgb(8, 18, 38),
                    16
            );

            border(
                    c,
                    settingsTop,
                    yellow,
                    2,
                    16
            );

            centerText(
                    c,
                    "⚙",
                    new RectF(
                            settingsTop.left,
                            settingsTop.top + 1,
                            settingsTop.right,
                            settingsTop.centerY() + 3
                    ),
                    Math.max(20, w * .055f),
                    yellow
            );

            centerText(
                    c,
                    "SETTINGS",
                    new RectF(
                            settingsTop.left,
                            settingsTop.centerY(),
                            settingsTop.right,
                            settingsTop.bottom
                    ),
                    Math.max(11, w * .025f),
                    white
            );

            // Crown
            drawCrownLogo(
                    c,
                    w / 2f,
                    h * .145f,
                    Math.min(
                            w * .24f,
                            h * .12f
                    )
            );

            float titleSize =
                    Math.max(
                            30,
                            Math.min(
                                    48,
                                    w * .078f
                            )
                    );

            String title =
                    "JD NUMBER PUZZLE";

            p.setTextSize(titleSize);

            drawText(
                    c,
                    title,
                    (w - p.measureText(title)) / 2f,
                    h * .315f,
                    titleSize,
                    yellow,
                    true
            );

            String sub =
                    "ROYAL • MATHEMATICAL • PUZZLE";

            float subSize =
                    Math.max(
                            13,
                            Math.min(
                                    22,
                                    w * .032f
                            )
                    );

            p.setTextSize(subSize);

            drawText(
                    c,
                    sub,
                    (w - p.measureText(sub)) / 2f,
                    h * .355f,
                    subSize,
                    goldLight,
                    true
            );

            p.setColor(yellow);
            p.setStrokeWidth(2);

            c.drawLine(
                    w * .16f,
                    h * .385f,
                    w * .44f,
                    h * .385f,
                    p
            );

            c.drawLine(
                    w * .56f,
                    h * .385f,
                    w * .84f,
                    h * .385f,
                    p
            );

            drawStar(
                    c,
                    w / 2f,
                    h * .385f,
                    6
            );

            float left = w * .12f;
            float right = w * .88f;

            float bh =
                    Math.max(
                            58,
                            Math.min(
                                    78,
                                    h * .070f
                            )
                    );

            float gap =
                    Math.max(
                            12,
                            h * .016f
                    );

            float top =
                    h * .425f;

            homeStartButton.set(
                    left,
                    top,
                    right,
                    top + bh
            );

            homeContinueButton.set(
                    left,
                    top + bh + gap,
                    right,
                    top + bh * 2 + gap
            );

            homeScoreButton.set(
                    left,
                    top + bh * 2 + gap * 2,
                    right,
                    top + bh * 3 + gap * 2
            );

            homeSettingsButton.set(
                    left,
                    top + bh * 3 + gap * 3,
                    right,
                    top + bh * 4 + gap * 3
            );

            drawMenuButton(
                    c,
                    homeStartButton,
                    "▶  START GAME",
                    Color.rgb(170, 10, 20),
                    Math.max(18, w * .040f)
            );

            drawMenuButton(
                    c,
                    homeContinueButton,
                    "▶  CONTINUE   •   Level " + level,
                    orange,
                    Math.max(16, w * .035f)
            );

            drawMenuButton(
                    c,
                    homeScoreButton,
                    "★  MY SCORE   •   " + score + " गुण",
                    Color.rgb(0, 105, 35),
                    Math.max(16, w * .035f)
            );

            drawMenuButton(
                    c,
                    homeSettingsButton,
                    "⚙  SETTINGS",
                    Color.rgb(75, 15, 105),
                    Math.max(18, w * .040f)
            );
        }

        // ============================================================
        // TEXT WIDTH
        // ============================================================

        float measure(
                String s,
                float size,
                boolean bold
        ) {

            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            return p.measureText(s);
        }

        // ============================================================
        // OPERATION NAME
        // ============================================================

        String operationName() {

            if (operation.equals("+")) {
                return "बेरीज";
            }

            if (operation.equals("−")) {
                return "वजाबाकी";
            }

            if (operation.equals("×")) {
                return "गुणाकार";
            }

            return "भागाकार";
        }

        // ============================================================
        // DRAW GAME DASHBOARD
        // ============================================================

        void drawGame(
                Canvas c,
                int w,
                int h
        ) {

            c.drawColor(backgroundColor);

            // ========================================================
            // RESPONSIVE SIZES
            // ========================================================

            float side =
                    Math.max(
                            16,
                            w * .035f
                    );

            // ========================================================
            // 1. CROWN
            // ========================================================

            float crownSize =
                    Math.min(
                            w * .18f,
                            h * .065f
                    );

            drawCrownLogo(
                    c,
                    w / 2f,
                    h * .055f,
                    crownSize
            );

            // ========================================================
            // 2. TITLE
            // Crown नंतर स्पष्ट gap
            // ========================================================

            float titleSize =
                    Math.max(
                            28,
                            Math.min(
                                    48,
                                    w * .068f
                            )
                    );

            String title =
                    "JD NUMBER PUZZLE";

            p.setTextSize(titleSize);

            drawText(
                    c,
                    title,
                    (w - p.measureText(title)) / 2f,
                    h * .155f,
                    titleSize,
                    yellow,
                    true
            );

            // ========================================================
            // 3. LEVEL
            // ========================================================

            String levelText;

            if (level >= 401) {

                levelText =
                        "Level "
                                + level
                                + " / 1000 • HARD MODE";

            } else {

                levelText =
                        "Level "
                                + level
                                + " / 1000 • No Timer";
            }

            float levelSize =
                    Math.max(
                            15,
                            Math.min(
                                    22,
                                    w * .030f
                            )
                    );

            p.setTextSize(levelSize);

            drawText(
                    c,
                    levelText,
                    (w - p.measureText(levelText)) / 2f,
                    h * .190f,
                    levelSize,
                    level >= 401
                            ? red
                            : Color.LTGRAY,
                    true
            );

            // ========================================================
            // 4. QUESTION BOX
            // मोठा
            // ========================================================

            float questionTop =
                    h * .215f;

            float questionHeight =
                    h * .215f;

            RectF card =
                    new RectF(
                            side,
                            questionTop,
                            w - side,
                            questionTop + questionHeight
                    );

            roundedRect(
                    c,
                    card,
                    cardColor,
                    22
            );

            border(
                    c,
                    card,
                    blue,
                    3f,
                    22
            );

            // Question heading

            drawText(
                    c,
                    "🧮  प्रश्न",
                    side + 22,
                    questionTop + 48,
                    Math.max(
                            24,
                            w * .050f
                    ),
                    yellow,
                    true
            );

            // ========================================================
            // QUESTION TEXT WRAPPING
            // ========================================================

            float qSize =
                    Math.max(
                            18,
                            Math.min(
                                    27,
                                    w * .044f
                            )
                    );

            p.setTextSize(qSize);

            float maxWidth =
                    card.width() - 44;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            for (String word : question.split(" ")) {

                String test =
                        current.isEmpty()
                                ? word
                                : current + " " + word;

                if (p.measureText(test) <= maxWidth) {

                    current = test;

                } else {

                    if (!current.isEmpty()) {
                        lines.add(current);
                    }

                    current = word;
                }
            }

            if (!current.isEmpty()) {
                lines.add(current);
            }

            float qY =
                    questionTop + 94;

            for (int i = 0;
                 i < lines.size() && i < 2;
                 i++) {

                drawText(
                        c,
                        lines.get(i),
                        side + 22,
                        qY + i * (qSize + 10),
                        qSize,
                        white,
                        true
                );
            }

            // ========================================================
            // OPERATION
            // ========================================================

            drawText(
                    c,
                    "क्रिया: "
                            + operation
                            + "  "
                            + operationName(),
                    side + 22,
                    card.bottom - 22,
                    Math.max(
                            18,
                            w * .040f
                    ),
                    yellow,
                    true
            );

            // ========================================================
            // 5. FOUR BIG NUMBER BOXES
            // एका रांगेत
            // ========================================================

            float numberTop =
                    h * .455f;

            float numberHeight =
                    h * .135f;

            float gap =
                    Math.max(
                            7,
                            w * .012f
                    );

            float gridWidth =
                    w - side * 2;

            float bw =
                    (gridWidth - gap * 3)
                            / 4f;

            for (int i = 0; i < 4; i++) {

                float left =
                        side
                                + i * (bw + gap);

                numberBoxes[i] =
                        new RectF(
                                left,
                                numberTop,
                                left + bw,
                                numberTop + numberHeight
                        );

                boolean selected =
                        selected1 == i
                                || selected2 == i;

                // Shadow

                RectF shadow =
                        new RectF(
                                numberBoxes[i].left,
                                numberBoxes[i].top + 7,
                                numberBoxes[i].right,
                                numberBoxes[i].bottom + 7
                        );

                roundedRect(
                        c,
                        shadow,
                        Color.argb(
                                120,
                                0,
                                0,
                                0
                        ),
                        16
                );

                // Box

                roundedRect(
                        c,
                        numberBoxes[i],
                        selected
                                ? Color.rgb(25, 90, 160)
                                : boxColor,
                        16
                );

                border(
                        c,
                        numberBoxes[i],
                        selected
                                ? yellow
                                : Color.rgb(
                                45,
                                125,
                                215
                        ),
                        selected ? 3f : 2f,
                        16
                );

                // Number

                centerText(
                        c,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        numberBoxes[i],
                        Math.max(
                                24,
                                Math.min(
                                        38,
                                        bw * .27f
                                )
                        ),
                        white
                );
            }

            // ========================================================
            // 6. LARGE HINT BOX
            // ========================================================

            float hintTop =
                    h * .615f;

            float hintHeight =
                    h * .105f;

            hintButton.set(
                    side,
                    hintTop,
                    w - side,
                    hintTop + hintHeight
            );

            roundedRect(
                    c,
                    hintButton,
                    Color.rgb(
                            12,
                            28,
                            52
                    ),
                    18
            );

            border(
                    c,
                    hintButton,
                    blue,
                    2.5f,
                    18
            );

            // Light bulb

            float bulbSize =
                    Math.max(
                            28,
                            w * .055f
                    );

            centerText(
                    c,
                    "💡",
                    new RectF(
                            side + 8,
                            hintTop,
                            side + 75,
                            hintButton.bottom
                    ),
                    bulbSize,
                    yellow
            );

            // Hint text

            String hintText =
                    "सूचना : योग्य दोन नंबर निवडा";

            float hintSize =
                    Math.max(
                            17,
                            Math.min(
                                    23,
                                    w * .037f
                            )
                    );

            centerText(
                    c,
                    hintText,
                    new RectF(
                            side + 70,
                            hintTop,
                            w - side - 10,
                            hintButton.bottom
                    ),
                    hintSize,
                    white
            );

            // ========================================================
            // 7. THREE LARGE ACTION BUTTONS
            // ========================================================

            float buttonTop =
                    h * .755f;

            float buttonHeight =
                    h * .095f;

            float buttonGap =
                    Math.max(
                            6,
                            w * .012f
                    );

            float buttonWidth =
                    (
                            w
                                    - side * 2
                                    - buttonGap * 2
                    ) / 3f;

            // RESET

            resetButton.set(
                    side,
                    buttonTop,
                    side + buttonWidth,
                    buttonTop + buttonHeight
            );

            // CHECK

            checkButton.set(
                    side + buttonWidth + buttonGap,
                    buttonTop,
                    side + buttonWidth * 2
                            + buttonGap,
                    buttonTop + buttonHeight
            );

            // RESULT

            resultButton.set(
                    side + buttonWidth * 2
                            + buttonGap * 2,
                    buttonTop,
                    w - side,
                    buttonTop + buttonHeight
            );

            float buttonTextSize =
                    Math.max(
                            15,
                            Math.min(
                                    22,
                                    w * .034f
                            )
                    );

            drawMenuButton(
                    c,
                    resetButton,
                    "↻  RESET",
                    red,
                    buttonTextSize
            );

            drawMenuButton(
                    c,
                    checkButton,
                    "✓  CHECK",
                    green,
                    buttonTextSize
            );

            drawMenuButton(
                    c,
                    resultButton,
                    "▮  RESULT",
                    purple,
                    buttonTextSize
            );

            // ========================================================
            // 8. STATUS
            // ========================================================

            if (!status.isEmpty()) {

                RectF statusBox =
                        new RectF(
                                side,
                                h * .865f,
                                w - side,
                                h * .925f
                        );

                int statusColor;

                if (status.startsWith("✓")) {

                    statusColor = green;

                } else if (status.startsWith("✗")) {

                    statusColor = red;

                } else {

                    statusColor = yellow;
                }

                centerText(
                        c,
                        status,
                        statusBox,
                        Math.max(
                                15,
                                Math.min(
                                        21,
                                        w * .032f
                                )
                        ),
                        statusColor
                );
            }

            // ========================================================
            // 9. BOTTOM SMALL INFO
            // ========================================================

            String bottomText =
                    "Level "
                            + level
                            + " / 1000   •   ⭐ "
                            + score
                            + " गुण";

            float bottomSize =
                    Math.max(
                            12,
                            Math.min(
                                    17,
                                    w * .026f
                            )
                    );

            p.setTextSize(bottomSize);

            drawText(
                    c,
                    bottomText,
                    (w - p.measureText(bottomText)) / 2f,
                    h * .970f,
                    bottomSize,
                    goldLight,
                    true
            );
        }

        // ============================================================
        // ON DRAW
        // ============================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            if (!gameStarted) {

                drawHome(
                        canvas,
                        w,
                        h
                );

            } else {

                drawGame(
                        canvas,
                        w,
                        h
                );
            }
        }

        // ============================================================
        // TOUCH
        // ============================================================

        @Override
        public boolean onTouchEvent(
                MotionEvent event
        ) {

            if (event.getAction()
                    != MotionEvent.ACTION_UP) {

                return true;
            }

            float x = event.getX();
            float y = event.getY();

            // ========================================================
            // HOME TOUCH
            // ========================================================

            if (!gameStarted) {

                if (homeStartButton.contains(x, y)) {

                    startGame();

                    return true;
                }

                if (homeContinueButton.contains(x, y)) {

                    continueGame();

                    return true;
                }

                if (homeScoreButton.contains(x, y)) {

                    showScore();

                    return true;
                }

                if (homeSettingsButton.contains(x, y)) {

                    showSettings();

                    return true;
                }

                return true;
            }

            // ========================================================
            // NUMBER TOUCH
            // ========================================================

            for (int i = 0; i < 4; i++) {

                if (numberBoxes[i] != null
                        && numberBoxes[i].contains(x, y)) {

                    selectNumber(i);

                    return true;
                }
            }

            // ========================================================
            // HINT TOUCH
            // ========================================================

            if (hintButton.contains(x, y)) {

                showHint();

                return true;
            }

            // ========================================================
            // RESET
            // ========================================================

            if (resetButton.contains(x, y)) {

                resetGame();

                return true;
            }

            // ========================================================
            // CHECK
            // ========================================================

            if (checkButton.contains(x, y)) {

                checkAnswer();

                return true;
            }

            // ========================================================
            // RESULT
            // ========================================================

            if (resultButton.contains(x, y)) {

                showResult(false);

                return true;
            }

            return true;
        }

        // ============================================================
        // SELECT NUMBER
        // ============================================================

        void selectNumber(int index) {

    if (selected1 == index) {
        selected1 = -1;
    } else if (selected2 == index) {
        selected2 = -1;
    } else if (selected1 == -1) {
        selected1 = index;
    } else if (selected2 == -1) {
        selected2 = index;
    } else {
        selected1 = selected2;
        selected2 = index;
    }

    status = "";
    invalidate();
}

}   // GameView बंद

}   // MainActivity बंद
