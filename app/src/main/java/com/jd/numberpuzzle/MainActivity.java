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
        if (hasFocus) hideBars();
    }

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random = new Random();

        ArrayList<Integer> numbers = new ArrayList<>();
        RectF[] numberBoxes = new RectF[40];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();
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

        GameView(Context context) {
            super(context);
            setFocusable(true);
            newPuzzle();
        }

        int boxCount() {
            if (level <= 50) return 10;
            if (level <= 100) return 20;
            if (level <= 200) return 30;
            return 40;
        }

        int columns() {
            return 5;
        }

        int smallNumber() {
            if (random.nextInt(100) < 25) return 1 + random.nextInt(9);
            return 10 + random.nextInt(90);
        }

        boolean containsNumber(int n) {
            for (int x : numbers) if (x == n) return true;
            return false;
        }

        void fillNumbers() {
            int count = boxCount();
            while (numbers.size() < count) {
                int n = smallNumber();
                if (!containsNumber(n)) numbers.add(n);
            }
            Collections.shuffle(numbers, random);
        }

        void newPuzzle() {
            numbers.clear();
            selected1 = -1;
            selected2 = -1;
            status = "";

            int type = random.nextInt(4);
            int x;
            int y;

            if (type == 0) {
                operation = "+";
                do {
                    x = smallNumber();
                    y = smallNumber();
                    target = x + y;
                } while (target < 10 || target > 150);
                question = "कोणते दोन नंबर अधिक केल्यावर " + target + " मिळेल?";
            } else if (type == 1) {
                operation = "−";
                do {
                    x = smallNumber();
                    y = smallNumber();
                } while (x <= y);
                target = x - y;
                question = "कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा केल्यावर "
                        + target + " मिळेल?";
            } else if (type == 2) {
                operation = "×";
                do {
                    x = 10 + random.nextInt(90);
                    y = 10 + random.nextInt(90);
                    target = x * y;
                } while (target < 1000 || target > 9999);
                question = "कोणते दोन नंबर गुणिले असता " + target + " मिळेल?";
            } else {
                operation = "÷";
                do {
                    y = 2 + random.nextInt(8);
                    target = 2 + random.nextInt(10);
                    x = y * target;
                } while (x > 99);
                question = "कोणता नंबर कोणत्या नंबरने भागल्यावर "
                        + target + " मिळेल?";
            }

            numbers.add(x);
            numbers.add(y);
            fillNumbers();
            invalidate();
        }

        boolean isCorrect(int x, int y) {
            if (operation.equals("+")) return x + y == target;
            if (operation.equals("−")) return x > y && x - y == target;
            if (operation.equals("×")) return x * y == target;
            return y != 0 && x % y == 0 && x / y == target;
        }

        void checkAnswer() {
            if (!gameStarted) return;

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
                        if (level < 1000) level++;
                        newPuzzle();
                    }
                }, 650);
            } else {
                roundWrong++;
                status = "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";
                invalidate();
            }
        }

        void startGame() {
            gameStarted = true;
            level = 1;
            score = 0;
            roundCorrect = 0;
            roundWrong = 0;
            newPuzzle();
            invalidate();
        }

        void continueGame() {
            gameStarted = true;
            invalidate();
        }

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

        void showResult(boolean finalGame) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(finalGame ? "JD NUMBER PUZZLE पूर्ण!" : "RESULT");
            builder.setMessage(
                    "Level : " + level +
                    "\n\n✓ बरोबर : " + roundCorrect +
                    "\n✗ चुकले : " + roundWrong +
                    "\n\n⭐ एकूण गुण : " + score
            );

            builder.setPositiveButton("पुढे खेळा", (dialog, which) -> {
                if (level < 1000) {
                    level++;
                    newPuzzle();
                    invalidate();
                }
            });

            builder.setNegativeButton("ठीक आहे", null);
            builder.show();
        }

        void showScore() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("MY SCORE")
                    .setMessage(
                            "Level : " + level +
                            "\n\nएकूण गुण : " + score +
                            "\n✓ बरोबर : " + roundCorrect +
                            "\n✗ चुकले : " + roundWrong
                    )
                    .setPositiveButton("ठीक आहे", null)
                    .show();
        }

        void showSettings() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("SETTINGS")
                    .setMessage(
                            "JD NUMBER PUZZLE\n\n" +
                            "• 1000 Levels\n" +
                            "• No Timer\n" +
                            "• Mathematical Puzzle\n" +
                            "• Royal Gold Theme\n" +
                            "• 10 / 20 / 30 / 40 Number Boxes\n" +
                            "• Level 401+ Hard Mode"
                    )
                    .setPositiveButton("ठीक आहे", null)
                    .show();
        }

        void showHint() {
            String hint;
            if (operation.equals("+")) {
                hint = "सूचना: " + target + " होण्यासाठी दोन नंबरची बेरीज करा.";
            } else if (operation.equals("−")) {
                hint = "सूचना: मोठ्या नंबरमधून छोटा नंबर वजा करा.";
            } else if (operation.equals("×")) {
                hint = "सूचना: " + target + " चे दोन गुणक शोधा.";
            } else {
                hint = "सूचना: भागाकार केल्यावर उत्तर " + target + " आले पाहिजे.";
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("HINT")
                    .setMessage(hint)
                    .setPositiveButton("ठीक आहे", null)
                    .show();
        }

        void drawText(Canvas c, String text, float x, float y,
                      float size, int color, boolean bold) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            c.drawText(text, x, y, p);
        }

        void centerText(Canvas c, String text, RectF r,
                        float size, int color) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            Paint.FontMetrics fm = p.getFontMetrics();
            float x = r.centerX() - p.measureText(text) / 2f;
            float y = r.centerY() - (fm.ascent + fm.descent) / 2f;
            c.drawText(text, x, y, p);
        }

        void roundedRect(Canvas c, RectF r, int color, float radius) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            c.drawRoundRect(r, radius, radius, p);
        }

        void border(Canvas c, RectF r, int color, float width, float radius) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);
            p.setColor(color);
            c.drawRoundRect(r, radius, radius, p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawStar(Canvas c, float cx, float cy, float radius) {
            p.setColor(yellow);
            p.setStyle(Paint.Style.FILL);
            Path path = new Path();

            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                float rr = (i % 2 == 0) ? radius : radius * .42f;
                float x = cx + (float)Math.cos(a) * rr;
                float y = cy + (float)Math.sin(a) * rr;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            path.close();
            c.drawPath(path, p);
        }

        void drawRoyalBackground(Canvas c, int w, int h) {
            c.drawColor(backgroundColor);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(8, 17, 36));

            c.drawCircle(w * .05f, h * .15f, w * .28f, p);
            c.drawCircle(w * .96f, h * .65f, w * .30f, p);
            c.drawCircle(w * .85f, h * .08f, w * .17f, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f);
            p.setColor(Color.rgb(24, 39, 70));

            for (int i = 0; i < 7; i++) {
                float y = h * .20f + i * h * .11f;
                c.drawLine(w * .04f, y, w * .96f, y, p);
            }
            p.setStyle(Paint.Style.FILL);

            drawStar(c, w * .10f, h * .08f, 10);
            drawStar(c, w * .89f, h * .10f, 9);
            drawStar(c, w * .14f, h * .75f, 7);
            drawStar(c, w * .86f, h * .78f, 8);
        }

        void drawCrownLogo(Canvas c, float cx, float cy, float size) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(255, 193, 20));

            Path crown = new Path();
            crown.moveTo(cx - size * .52f, cy + size * .20f);
            crown.lineTo(cx - size * .70f, cy - size * .42f);
            crown.lineTo(cx - size * .22f, cy - size * .12f);
            crown.lineTo(cx, cy - size * .58f);
            crown.lineTo(cx + size * .22f, cy - size * .12f);
            crown.lineTo(cx + size * .70f, cy - size * .42f);
            crown.lineTo(cx + size * .52f, cy + size * .20f);
            crown.close();
            c.drawPath(crown, p);

            border(c,
                    new RectF(
                            cx - size * .55f,
                            cy + size * .10f,
                            cx + size * .55f,
                            cy + size * .36f
                    ),
                    goldLight, 3, 8);

            drawText(c, "JD",
                    cx - size * .43f,
                    cy + size * .98f,
                    size * .68f,
                    yellow,
                    true);
        }

        void drawMenuButton(Canvas c, RectF r, String text,
                            int color, float size) {
            RectF shadow = new RectF(r.left, r.top + 7, r.right, r.bottom + 7);
            roundedRect(c, shadow, Color.argb(120, 0, 0, 0), 20);
            roundedRect(c, r, color, 20);
            border(c, r, yellow, 2.5f, 20);

            centerText(c, text, r, size, white);
        }

        void drawHome(Canvas c, int w, int h) {
            drawRoyalBackground(c, w, h);

            // Top mini cards
            RectF scoreTop = new RectF(w * .045f, h * .025f,
                    w * .23f, h * .105f);
            RectF settingsTop = new RectF(w * .77f, h * .025f,
                    w * .955f, h * .105f);

            roundedRect(c, scoreTop, Color.rgb(8, 18, 38), 16);
            border(c, scoreTop, yellow, 2, 16);
            centerText(c, "★", new RectF(scoreTop.left, scoreTop.top + 2,
                    scoreTop.right, scoreTop.centerY() + 3),
                    Math.max(20, w * .055f), yellow);
            centerText(c, "SCORE", new RectF(scoreTop.left, scoreTop.centerY(),
                    scoreTop.right, scoreTop.bottom),
                    Math.max(12, w * .027f), white);

            roundedRect(c, settingsTop, Color.rgb(8, 18, 38), 16);
            border(c, settingsTop, yellow, 2, 16);
            centerText(c, "⚙", new RectF(settingsTop.left, settingsTop.top + 1,
                    settingsTop.right, settingsTop.centerY() + 3),
                    Math.max(20, w * .055f), yellow);
            centerText(c, "SETTINGS", new RectF(settingsTop.left, settingsTop.centerY(),
                    settingsTop.right, settingsTop.bottom),
                    Math.max(11, w * .025f), white);

            // Crown
            drawCrownLogo(c, w / 2f, h * .145f,
                    Math.min(w * .24f, h * .12f));

            float titleSize = Math.max(30, Math.min(48, w * .078f));
            String title = "JD NUMBER PUZZLE";
            p.setTextSize(titleSize);
            drawText(c, title,
                    (w - p.measureText(title)) / 2f,
                    h * .315f,
                    titleSize, yellow, true);

            String sub = "ROYAL • MATHEMATICAL • PUZZLE";
            float subSize = Math.max(13, Math.min(22, w * .032f));
            p.setTextSize(subSize);
            drawText(c, sub,
                    (w - p.measureText(sub)) / 2f,
                    h * .355f,
                    subSize, goldLight, true);

            // Decorative separator
            p.setColor(yellow);
            p.setStrokeWidth(2);
            c.drawLine(w * .16f, h * .385f, w * .44f, h * .385f, p);
            c.drawLine(w * .56f, h * .385f, w * .84f, h * .385f, p);
            drawStar(c, w / 2f, h * .385f, 6);

            float left = w * .12f;
            float right = w * .88f;
            float bh = Math.max(58, Math.min(78, h * .070f));
            float gap = Math.max(12, h * .016f);
            float top = h * .425f;

            homeStartButton.set(left, top, right, top + bh);
            homeContinueButton.set(left, top + bh + gap,
                    right, top + bh * 2 + gap);
            homeScoreButton.set(left, top + bh * 2 + gap * 2,
                    right, top + bh * 3 + gap * 2);
            homeSettingsButton.set(left, top + bh * 3 + gap * 3,
                    right, top + bh * 4 + gap * 3);

            drawMenuButton(c, homeStartButton,
                    "▶  START GAME", Color.rgb(170, 10, 20),
                    Math.max(18, w * .040f));

            drawMenuButton(c, homeContinueButton,
                    "▶  CONTINUE   •   Level " + level,
                    orange, Math.max(16, w * .035f));

            drawMenuButton(c, homeScoreButton,
                    "★  MY SCORE   •   " + score + " गुण",
                    Color.rgb(0, 105, 35), Math.max(16, w * .035f));

            drawMenuButton(c, homeSettingsButton,
                    "⚙  SETTINGS", Color.rgb(75, 15, 105),
                    Math.max(18, w * .040f));

            // Stats panel
            float statsTop = homeSettingsButton.bottom + h * .035f;
            float statsBottom = Math.min(h * .86f, statsTop + h * .105f);

            RectF stats = new RectF(w * .055f, statsTop,
                    w * .945f, statsBottom);

            roundedRect(c, stats, Color.rgb(8, 20, 42), 18);
            border(c, stats, yellow, 1.8f, 18);

            float col = stats.width() / 4f;
            for (int i = 1; i < 4; i++) {
                p.setColor(Color.rgb(120, 90, 20));
                p.setStrokeWidth(1);
                c.drawLine(stats.left + col * i,
                        stats.top + 10,
                        stats.left + col * i,
                        stats.bottom - 10, p);
            }

            String[] labels = {"LEVEL", "SCORE", "CORRECT", "WRONG"};
            String[] values = {
                    level + " / 1000",
                    String.valueOf(score),
                    String.valueOf(roundCorrect),
                    String.valueOf(roundWrong)
            };
            int[] colors = {yellow, yellow, green, red};

            for (int i = 0; i < 4; i++) {
                float cx = stats.left + col * i + col / 2f;
                drawText(c, labels[i],
                        cx - measure(labels[i], Math.max(11, w * .025f), true) / 2f,
                        stats.top + stats.height() * .52f,
                        Math.max(11, w * .025f), white, true);

                drawText(c, values[i],
                        cx - measure(values[i], Math.max(12, w * .028f), true) / 2f,
                        stats.top + stats.height() * .82f,
                        Math.max(12, w * .028f), colors[i], true);
            }

            // Bottom status
            float bottomY = h * .925f;
            String footer = "1000 LEVELS   •   NO TIMER";
            p.setTextSize(Math.max(15, w * .032f));
            drawText(c, footer,
                    (w - p.measureText(footer)) / 2f,
                    bottomY,
                    Math.max(15, w * .032f),
                    yellow, true);

            String small = "PLAY • LEARN • IMPROVE";
            p.setTextSize(Math.max(12, w * .026f));
            drawText(c, small,
                    (w - p.measureText(small)) / 2f,
                    h * .965f,
                    Math.max(12, w * .026f),
                    goldLight, true);
        }

        float measure(String s, float size, boolean bold) {
            p.setTextSize(size);
            p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            return p.measureText(s);
        }

        void drawGame(Canvas c, int w, int h) {
            c.drawColor(backgroundColor);

            float side = Math.max(16, w * .035f);

            drawCrownLogo(c, w / 2f, 28, Math.min(w * .11f, 48));

            String scoreText = "★ " + score + " गुण";
            float scoreSize = Math.max(16, w * .035f);
            p.setTextSize(scoreSize);

            RectF scoreBox = new RectF(
                    w - p.measureText(scoreText) - side - 18, 8,
                    w - side, 50
            );
            border(c, scoreBox, yellow, 2, 12);
            centerText(c, scoreText, scoreBox, scoreSize, yellow);

            float titleSize = Math.max(28, Math.min(44, w * .070f));
            String title = "JD NUMBER PUZZLE";
            p.setTextSize(titleSize);
            drawText(c, title,
                    (w - p.measureText(title)) / 2f,
                    100,
                    titleSize, yellow, true);

            String levelText = level >= 401
                    ? "Level " + level + " / 1000 • 🔥 HARD MODE"
                    : "Level " + level + " / 1000 • No Timer";

            p.setTextSize(Math.max(14, w * .030f));
            drawText(c, levelText,
                    (w - p.measureText(levelText)) / 2f,
                    130,
                    Math.max(14, w * .030f),
                    level >= 401 ? red : Color.LTGRAY, true);

            float questionTop = 148;
            float questionHeight = level <= 200 ? 165 : 135;

            RectF card = new RectF(side, questionTop,
                    w - side, questionTop + questionHeight);
            roundedRect(c, card, cardColor, 22);
            border(c, card, blue, 2.2f, 22);

            drawText(c, "🧮  प्रश्न",
                    side + 22, questionTop + 40,
                    Math.max(22, w * .050f), yellow, true);

            float qSize = Math.max(18, Math.min(28, w * .046f));
            p.setTextSize(qSize);

            float maxWidth = card.width() - 44;
            ArrayList<String> lines = new ArrayList<>();
            String current = "";

            for (String word : question.split(" ")) {
                String test = current.isEmpty() ? word : current + " " + word;
                if (p.measureText(test) <= maxWidth) {
                    current = test;
                } else {
                    if (!current.isEmpty()) lines.add(current);
                    current = word;
                }
            }
            if (!current.isEmpty()) lines.add(current);

            float qY = questionTop + 82;
            for (int i = 0; i < lines.size() && i < 2; i++) {
                drawText(c, lines.get(i), side + 22,
                        qY + i * (qSize + 8),
                        qSize, white, false);
            }

            drawText(c, "क्रिया: " + operation + "  " + operationName(),
                    side + 22, card.bottom - 18,
                    Math.max(17, w * .040f), yellow, true);

            int count = boxCount();
            float gridTop = card.bottom + 14;
            float gap = count <= 10 ? 7 : 5;
            float gridWidth = w - side * 2;
            float bw = (gridWidth - gap * 4) / 5f;

            float availableBottom = h * .69f;
            int rows = (int)Math.ceil(count / 5.0);
            float bh = Math.max(38,
                    Math.min(76, (availableBottom - gridTop - (rows - 1) * gap) / rows));

            for (int i = 0; i < count; i++) {
                int row = i / 5;
                int col = i % 5;

                float l = side + col * (bw + gap);
                float t = gridTop + row * (bh + gap);

                numberBoxes[i] = new RectF(l, t, l + bw, t + bh);

                boolean selected = selected1 == i || selected2 == i;

                roundedRect(c, numberBoxes[i],
                        selected ? Color.rgb(25, 90, 160) : boxColor,
                        12);
                border(c, numberBoxes[i],
                        selected ? yellow : Color.rgb(45, 105, 180),
                        selected ? 2.5f : 1.2f, 12);

                centerText(c, String.valueOf(numbers.get(i)),
                        numberBoxes[i],
                        Math.max(15, Math.min(28, bw * .27f)),
                        white);
            }

            float buttonTop = Math.min(h * .80f, gridTop + rows * (bh + gap) + 12);
            float buttonGap = 5;
            float buttonWidth = (w - side * 2 - buttonGap * 3) / 4f;
            float buttonHeight = Math.max(50, Math.min(65, h * .060f));

            hintButton.set(side, buttonTop,
                    side + buttonWidth, buttonTop + buttonHeight);
            resetButton.set(side + buttonWidth + buttonGap, buttonTop,
                    side + buttonWidth * 2 + buttonGap,
                    buttonTop + buttonHeight);
            checkButton.set(side + buttonWidth * 2 + buttonGap * 2,
                    buttonTop,
                    side + buttonWidth * 3 + buttonGap * 2,
                    buttonTop + buttonHeight);
            resultButton.set(side + buttonWidth * 3 + buttonGap * 3,
                    buttonTop,
                    w - side, buttonTop + buttonHeight);

            drawMenuButton(c, hintButton, "HINT", blue,
                    Math.max(13, w * .027f));
            drawMenuButton(c, resetButton, "RESET", red,
                    Math.max(13, w * .027f));
            drawMenuButton(c, checkButton, "CHECK", green,
                    Math.max(13, w * .027f));
            drawMenuButton(c, resultButton, "RESULT", purple,
                    Math.max(13, w * .027f));

            if (!status.isEmpty()) {
                centerText(c, status,
                        new RectF(side, buttonTop + buttonHeight + 8,
                                w - side, buttonTop + buttonHeight + 45),
                        Math.max(14, w * .030f),
                        status.startsWith("✓") ? green :
                                status.startsWith("✗") ? red : yellow);
            }
        }

        String operationName() {
            if (operation.equals("+")) return "अधिक";
            if (operation.equals("−")) return "वजा";
            if (operation.equals("×")) return "गुणाकार";
            return "भागाकार";
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            if (!gameStarted) {
                drawHome(canvas, w, h);
            } else {
                drawGame(canvas, w, h);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;

            float x = event.getX();
            float y = event.getY();

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

            for (int i = 0; i < boxCount(); i++) {
                if (numberBoxes[i] != null &&
                        numberBoxes[i].contains(x, y)) {
                    selectNumber(i);
                    return true;
                }
            }

            if (hintButton.contains(x, y)) {
                showHint();
                return true;
            }
            if (resetButton.contains(x, y)) {
                resetGame();
                return true;
            }
            if (checkButton.contains(x, y)) {
                checkAnswer();
                return true;
            }
            if (resultButton.contains(x, y)) {
                showResult(false);
                return true;
            }

            return true;
        }

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
    }
}
