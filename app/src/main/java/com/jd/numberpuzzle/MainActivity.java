package com.jd.numberpuzzle;

import android.app.*;
import android.content.*;
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
        RectF[] numberBoxes = new RectF[10];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();

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
        boolean intro = true;
        long introStartTime;

        int backgroundColor = Color.rgb(8, 12, 22);
        int cardColor = Color.rgb(20, 24, 38);
        int boxColor = Color.rgb(18, 25, 42);
        int yellow = Color.rgb(255, 215, 0);
        int goldLight = Color.rgb(255, 236, 150);
        int blue = Color.rgb(25, 145, 255);
        int white = Color.WHITE;
        int green = Color.rgb(0, 210, 90);
        int red = Color.rgb(245, 55, 65);
        int purple = Color.rgb(105, 55, 230);

        GameView(Context context) {
            super(context);
            setFocusable(true);
            introStartTime = System.currentTimeMillis();
            newPuzzle();
        }

        int smallNumber() {
            if (random.nextInt(100) < 25) return 1 + random.nextInt(9);
            return 10 + random.nextInt(90);
        }

        boolean containsNumber(int n) {
            for (int x : numbers) {
                if (x == n) return true;
            }
            return false;
        }

        void fillNumbers() {
            while (numbers.size() < 10) {
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
                question = "कोणत्या दोन नंबरचा फरक " + target + " मिळेल?";

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

                question = "कोणता नंबर कोणत्या नंबरने भागल्यावर " +
                        target + " मिळेल?";
            }

            numbers.add(x);
            numbers.add(y);
            fillNumbers();
            invalidate();
        }

        boolean isCorrect(int x, int y) {
            if (operation.equals("+")) {
                return x + y == target;
            }

            if (operation.equals("−")) {
                return Math.abs(x - y) == target;
            }

            if (operation.equals("×")) {
                return x * y == target;
            }

            return (y != 0 && x % y == 0 && x / y == target) ||
                    (x != 0 && y % x == 0 && y / x == target);
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

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (level % 10 == 0) {
                            showResult(level == 1000);
                        } else {
                            level++;
                            newPuzzle();
                        }
                    }
                }, 700);

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
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(MainActivity.this);

            String title = finalGame
                    ? "🎉 JD Number Puzzle पूर्ण!"
                    : "🏆 Result";

            String message =
                    "Level : " + level +
                    "\n\n✓ बरोबर : " + roundCorrect +
                    "\n✗ चुकले : " + roundWrong +
                    "\n\n⭐ एकूण गुण : " + score;

            builder.setTitle(title);
            builder.setMessage(message);

            if (level < 1000) {
                builder.setPositiveButton("पुढे खेळा",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                level++;
                                newPuzzle();
                                invalidate();
                            }
                        });
            } else {
                builder.setPositiveButton("ठीक आहे", null);
            }

            builder.setNegativeButton("ठीक आहे", null);
            builder.show();
        }

        void showScore() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("⭐ MY SCORE")
                    .setMessage(
                            "सध्याचा Level : " + level +
                            "\n\nबरोबर : " + roundCorrect +
                            "\nचुकले : " + roundWrong +
                            "\n\nएकूण गुण : " + score
                    )
                    .setPositiveButton("ठीक आहे", null)
                    .show();
        }

        void showSettings() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("⚙ Settings")
                    .setMessage(
                            "JD NUMBER PUZZLE\n\n" +
                            "• 1000 Levels\n" +
                            "• No Timer\n" +
                            "• Mathematical Puzzle\n" +
                            "• Royal Gold Theme"
                    )
                    .setPositiveButton("ठीक आहे", null)
                    .show();
        }

        void drawText(Canvas canvas, String text, float x, float y,
                      float size, int color, boolean bold) {
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            p.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x, y, p);
        }

        void centerText(Canvas canvas, String text, RectF rect,
                        float size, int color) {
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setStyle(Paint.Style.FILL);

            Paint.FontMetrics fm = p.getFontMetrics();

            float x = rect.centerX() - p.measureText(text) / 2f;
            float y = rect.centerY() - (fm.ascent + fm.descent) / 2f;

            canvas.drawText(text, x, y, p);
        }

        void roundedRect(Canvas canvas, RectF rect, int color, float radius) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            canvas.drawRoundRect(rect, radius, radius, p);
        }

        void border(Canvas canvas, RectF rect, int color,
                    float width, float radius) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);
            p.setColor(color);
            canvas.drawRoundRect(rect, radius, radius, p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawGameButton(Canvas canvas, RectF rect, String text,
                            int color, float size) {
            RectF shadow = new RectF(
                    rect.left, rect.top + 5,
                    rect.right, rect.bottom + 5
            );

            roundedRect(canvas, shadow,
                    Color.argb(90, 0, 0, 0), 18);

            roundedRect(canvas, rect, color, 18);

            border(canvas, rect,
                    Color.argb(210, 255, 255, 255),
                    2, 18);

            centerText(canvas, text, rect, size, Color.WHITE);
        }

        void drawRoyalBackground(Canvas canvas, int width, int height) {
            canvas.drawColor(backgroundColor);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(16, 29, 50));

            canvas.drawCircle(width * .08f, height * .18f,
                    width * .18f, p);

            canvas.drawCircle(width * .94f, height * .72f,
                    width * .22f, p);

            p.setColor(Color.rgb(30, 25, 48));

            canvas.drawCircle(width * .90f, height * .20f,
                    width * .13f, p);

            drawStar(canvas, width * .10f, height * .08f, 12);
            drawStar(canvas, width * .88f, height * .10f, 10);
            drawStar(canvas, width * .16f, height * .76f, 9);
            drawStar(canvas, width * .82f, height * .80f, 12);
        }

        void drawStar(Canvas canvas, float cx, float cy, float r) {
            p.setColor(yellow);
            p.setStyle(Paint.Style.FILL);

            Path path = new Path();

            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                float rr = (i % 2 == 0) ? r : r * .38f;

                float x = cx + (float) Math.cos(a) * rr;
                float y = cy + (float) Math.sin(a) * rr;

                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            path.close();
            canvas.drawPath(path, p);
        }

        void drawKingLogo(Canvas canvas, float cx, float cy, float size) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(yellow);

            Path crown = new Path();

            crown.moveTo(cx - size * .55f, cy + size * .18f);
            crown.lineTo(cx - size * .70f, cy - size * .42f);
            crown.lineTo(cx - size * .22f, cy - size * .12f);
            crown.lineTo(cx, cy - size * .55f);
            crown.lineTo(cx + size * .22f, cy - size * .12f);
            crown.lineTo(cx + size * .70f, cy - size * .42f);
            crown.lineTo(cx + size * .55f, cy + size * .18f);
            crown.close();

            canvas.drawPath(crown, p);

            border(canvas,
                    new RectF(
                            cx - size * .55f,
                            cy + size * .10f,
                            cx + size * .55f,
                            cy + size * .38f
                    ),
                    goldLight, 3, 8);

            drawText(canvas, "JD",
                    cx - size * .44f,
                    cy + size * 1.05f,
                    size * .70f,
                    yellow,
                    true);
        }

        void drawMathCartoon(Canvas canvas, float cx, float cy, float size) {
            p.setColor(Color.rgb(255, 218, 175));
            p.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, size * .32f, p);

            p.setColor(Color.rgb(80, 55, 35));

            Path hair = new Path();
            hair.moveTo(cx - size * .32f, cy - size * .05f);
            hair.quadTo(cx - size * .20f, cy - size * .48f,
                    cx + size * .12f, cy - size * .40f);
            hair.quadTo(cx + size * .38f, cy - size * .22f,
                    cx + size * .30f, cy + size * .05f);
            hair.lineTo(cx + size * .16f, cy - size * .15f);
            hair.lineTo(cx - size * .32f, cy - size * .05f);
            hair.close();

            canvas.drawPath(hair, p);

            p.setColor(Color.BLACK);

            canvas.drawCircle(cx - size * .12f, cy - size * .02f,
                    size * .035f, p);
            canvas.drawCircle(cx + size * .12f, cy - size * .02f,
                    size * .035f, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(size * .025f);

            Path smile = new Path();
            smile.moveTo(cx - size * .09f, cy + size * .10f);
            smile.quadTo(cx, cy + size * .18f,
                    cx + size * .09f, cy + size * .10f);

            canvas.drawPath(smile, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(35, 75, 150));

            RectF body = new RectF(
                    cx - size * .35f,
                    cy + size * .28f,
                    cx + size * .35f,
                    cy + size * .95f
            );

            canvas.drawRoundRect(body, size * .12f,
                    size * .12f, p);

            RectF board = new RectF(
                    cx - size * .70f,
                    cy + size * .40f,
                    cx - size * .05f,
                    cy + size * .82f
            );

            roundedRect(canvas, board, Color.WHITE, size * .06f);
            border(canvas, board, yellow, 3, size * .06f);

            drawText(canvas, "7 + 5",
                    board.left + size * .08f,
                    board.top + size * .18f,
                    size * .16f,
                    Color.rgb(30, 45, 80), true);

            drawText(canvas, "× 2",
                    board.left + size * .12f,
                    board.top + size * .37f,
                    size * .16f,
                    Color.rgb(30, 45, 80), true);

            drawText(canvas, "= 24",
                    board.left + size * .08f,
                    board.top + size * .56f,
                    size * .16f,
                    Color.rgb(30, 45, 80), true);

            p.setColor(yellow);
            p.setStrokeWidth(size * .08f);

            canvas.drawLine(
                    cx + size * .20f, cy + size * .62f,
                    cx + size * .58f, cy + size * .30f, p
            );

            p.setStyle(Paint.Style.FILL);
        }

        void drawHomeButton(Canvas canvas, RectF rect,
                            String text, int color, float size) {
            drawGameButton(canvas, rect, text, color, size);
        }

        void drawStartScreen(Canvas canvas, int width, int height,
                             float side) {

            drawRoyalBackground(canvas, width, height);

            long elapsed =
                    System.currentTimeMillis() - introStartTime;

            float progress = Math.min(1f, elapsed / 1800f);
            float eased = 1f - (1f - progress) * (1f - progress);
            float alpha = Math.min(1f, progress * 1.35f);
            float scale = 0.65f + 0.35f * eased;

            p.setAlpha((int) (255 * alpha));

            canvas.save();

            canvas.scale(scale, scale,
                    width / 2f, height * .20f);

            drawKingLogo(canvas,
                    width / 2f, height * .17f,
                    Math.min(width * .20f, 105));

            canvas.restore();

            p.setAlpha(255);

            float titleSize =
                    Math.max(32, Math.min(48, width * .075f));

            String title = "JD NUMBER PUZZLE";

            p.setTextSize(titleSize);

            float titleWidth = p.measureText(title);

            drawText(canvas, title,
                    (width - titleWidth) / 2f,
                    height * .34f,
                    titleSize,
                    yellow,
                    true);

            String sub = "ROYAL • MATHEMATICAL • PUZZLE";

            p.setTextSize(Math.max(13, width * .028f));

            drawText(canvas, sub,
                    (width - p.measureText(sub)) / 2f,
                    height * .385f,
                    Math.max(13, width * .028f),
                    goldLight,
                    true);

            drawMathCartoon(canvas,
                    width * .79f,
                    height * .64f,
                    Math.min(width * .34f, height * .22f));

            float buttonLeft = width * .10f;
            float buttonRight = width * .72f;

            float buttonHeight =
                    Math.max(58, Math.min(76, height * .065f));

            float gap =
                    Math.max(12, height * .018f);

            float top = height * .47f;

            homeStartButton.set(
                    buttonLeft, top,
                    buttonRight, top + buttonHeight
            );

            homeContinueButton.set(
                    buttonLeft,
                    top + buttonHeight + gap,
                    buttonRight,
                    top + buttonHeight * 2 + gap
            );

            homeScoreButton.set(
                    buttonLeft,
                    top + buttonHeight * 2 + gap * 2,
                    buttonRight,
                    top + buttonHeight * 3 + gap * 2
            );

            homeSettingsButton.set(
                    buttonLeft,
                    top + buttonHeight * 3 + gap * 3,
                    buttonRight,
                    top + buttonHeight * 4 + gap * 3
            );

            drawHomeButton(canvas, homeStartButton,
                    "▶  START GAME", red,
                    Math.max(18, width * .038f));

            drawHomeButton(canvas, homeContinueButton,
                    "▶  CONTINUE",
                    Color.rgb(235, 175, 20),
                    Math.max(18, width * .038f));

            drawHomeButton(canvas, homeScoreButton,
                    "★  MY SCORE", green,
                    Math.max(18, width * .038f));

            drawHomeButton(canvas, homeSettingsButton,
                    "⚙  SETTINGS", purple,
                    Math.max(18, width * .038f));

            String footer = "1000 LEVELS  •  NO TIMER";

            p.setTextSize(Math.max(13, width * .026f));

            drawText(canvas, footer,
                    (width - p.measureText(footer)) / 2f,
                    height * .94f,
                    Math.max(13, width * .026f),
                    Color.LTGRAY,
                    false);

            if (intro) {
                if (elapsed >= 2200) {
                    intro = false;
                }

                postInvalidateOnAnimation();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            if (!gameStarted) {
                drawStartScreen(canvas, width, height,
                        Math.max(20, width * .035f));
                return;
            }

            canvas.drawColor(backgroundColor);

            float side = Math.max(20, width * .035f);
            float headerTop = Math.max(18, height * .015f);

            drawKingLogo(canvas,
                    width / 2f,
                    headerTop + 20,
                    Math.min(width * .12f, 58));

            float scoreSize = Math.max(17, width * .035f);
            String scoreText = "★ " + score + " गुण";

            p.setTextSize(scoreSize);

            float scoreWidth = p.measureText(scoreText);

            RectF scoreBox = new RectF(
                    width - scoreWidth - side - 18,
                    headerTop + 5,
                    width - side,
                    headerTop + 52
            );

            border(canvas, scoreBox, yellow, 2, 12);
            centerText(canvas, scoreText,
                    scoreBox, scoreSize, yellow);

            float titleY = headerTop + 92;
            float titleSize =
                    Math.max(31, Math.min(50, width * .072f));

            String title = "JD NUMBER PUZZLE";

            p.setTextSize(titleSize);

            drawText(canvas, title,
                    (width - p.measureText(title)) / 2f,
                    titleY,
                    titleSize,
                    yellow,
                    true);

            float subSize = Math.max(15, width * .032f);

            String levelText =
                    "Level " + level + " / 1000 • No Timer";

            p.setTextSize(subSize);

            drawText(canvas, levelText,
                    (width - p.measureText(levelText)) / 2f,
                    titleY + subSize + 8,
                    subSize,
                    Color.LTGRAY,
                    false);

            float questionTop =
                    titleY + subSize + 28;

            float questionHeight =
                    Math.max(160,
                            Math.min(height * .18f, 220));

            RectF questionCard = new RectF(
                    side, questionTop,
                    width - side,
                    questionTop + questionHeight
            );

            roundedRect(canvas, questionCard,
                    cardColor, 25);

            border(canvas, questionCard,
                    blue, 2.5f, 25);

            float qTitleSize =
                    Math.max(24, width * .052f);

            drawText(canvas, "🧮  प्रश्न",
                    side + 28,
                    questionTop + 55,
                    qTitleSize,
                    yellow,
                    true);

            float qSize =
                    Math.max(20, Math.min(30, width * .047f));

            p.setTextSize(qSize);
            p.setTypeface(Typeface.DEFAULT);

            float maxWidth =
                    questionCard.width() - 55;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            for (String word : question.split(" ")) {
                String test = current.isEmpty()
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

            float qY = questionTop + 100;
            float lineHeight = qSize + 10;

            for (int i = 0;
                 i < lines.size() && i < 2;
                 i++) {

                drawText(canvas, lines.get(i),
                        side + 28,
                        qY + i * lineHeight,
                        qSize,
                        white,
                        false);
            }

            drawText(canvas,
                    "क्रिया: " + operation +
                            "  " + operationName(),
                    side + 28,
                    questionCard.bottom - 24,
                    Math.max(20, width * .044f),
                    yellow,
                    true);

            float gridTop =
                    questionCard.bottom + 16;

            float gap =
                    Math.max(8, width * .012f);

            float gridWidth =
                    width - side * 2;

            float boxWidth =
                    (gridWidth - gap * 4) / 5f;

            float boxHeight =
                    Math.max(68,
                            Math.min(100, height * .082f));

            for (int i = 0; i < 10; i++) {

                int row = i / 5;
                int col = i % 5;

                float left =
                        side + col * (boxWidth + gap);

                float top =
                        gridTop + row * (boxHeight + gap);

                numberBoxes[i] = new RectF(
                        left, top,
                        left + boxWidth,
                        top + boxHeight
                );

                boolean selected =
                        selected1 == i ||
                                selected2 == i;

                roundedRect(canvas,
                        numberBoxes[i],
                        selected
                                ? Color.rgb(30, 90, 160)
                                : boxColor,
                        16);

                border(canvas,
                        numberBoxes[i],
                        selected
                                ? yellow
                                : Color.rgb(45, 105, 180),
                        selected ? 3 : 1.5f,
                        16);

                centerText(canvas,
                        String.valueOf(numbers.get(i)),
                        numberBoxes[i],
                        Math.max(22,
                                Math.min(32,
                                        boxWidth * .25f)),
                        white);
            }

            float hintTop =
                    gridTop + boxHeight * 2 +
                            gap + 14;

            float hintHeight =
                    Math.max(54,
                            Math.min(72, height * .065f));

            RectF hint = new RectF(
                    side, hintTop,
                    width - side,
                    hintTop + hintHeight
            );

            roundedRect(canvas, hint,
                    cardColor, 16);

            border(canvas, hint,
                    blue, 1.8f, 16);

            centerText(canvas,
                    "💡 सूचना : योग्य दोन नंबर निवडा",
                    hint,
                    Math.max(14,
                            Math.min(19,
                                    width * .031f)),
                    white);

            float buttonTop = hint.bottom + 12;
            float buttonGap = 8;

            float buttonWidth =
                    (width - side * 2 -
                            buttonGap * 2) / 3f;

            float buttonHeight =
                    Math.max(52,
                            Math.min(70,
                                    height * .062f));

            resetButton.set(
                    side,
                    buttonTop,
                    side + buttonWidth,
                    buttonTop + buttonHeight
            );

            checkButton.set(
                    side + buttonWidth + buttonGap,
                    buttonTop,
                    side + buttonWidth * 2 +
                            buttonGap,
                    buttonTop + buttonHeight
            );

            resultButton.set(
                    side + buttonWidth * 2 +
                            buttonGap * 2,
                    buttonTop,
                    width - side,
                    buttonTop + buttonHeight
            );

            drawGameButton(canvas,
                    resetButton,
                    "↻ RESET",
                    red,
                    Math.max(15, width * .028f));

            drawGameButton(canvas,
                    checkButton,
                    "✓ CHECK",
                    green,
                    Math.max(15, width * .028f));

            drawGameButton(canvas,
                    resultButton,
                    "▮ RESULT",
                    purple,
                    Math.max(15, width * .028f));

            if (!status.isEmpty()) {
                float statusY =
                        buttonTop + buttonHeight + 25;

                centerText(canvas, status,
                        new RectF(
                                side,
                                statusY - 22,
                                width - side,
                                statusY + 22
                        ),
                        Math.max(15,
                                width * .033f),
                        status.startsWith("✓")
                                ? green
                                : status.startsWith("✗")
                                ? red
                                : yellow);
            }
        }

        String operationName() {
            if (operation.equals("+")) return "अधिक";
            if (operation.equals("−")) return "वजा";
            if (operation.equals("×")) return "गुणाकार";
            return "भागाकार";
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }

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

            for (int i = 0; i < 10; i++) {
                if (numberBoxes[i] != null &&
                        numberBoxes[i].contains(x, y)) {
                    selectNumber(i);
                    return true;
                }
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
