package com.jd.numberpuzzle;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {

    GameView game;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideBars();

        game = new GameView(this);
        setContentView(game);
    }

    void hideBars() {
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
        RectF[] numberBoxes = new RectF[10];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();

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

        // --------------------------------------------------------
        // COLORS
        // --------------------------------------------------------

        int backgroundColor = Color.rgb(5, 12, 22);

        int cardColor = Color.rgb(12, 28, 48);

        int boxColor = Color.rgb(18, 30, 48);

        int yellow = Color.rgb(255, 215, 0);

        int blue = Color.rgb(20, 130, 255);

        int white = Color.WHITE;

        // --------------------------------------------------------
        // CONSTRUCTOR
        // --------------------------------------------------------

        GameView(Context context) {
            super(context);

            setFocusable(true);

            newPuzzle();
        }

        // --------------------------------------------------------
        // NUMBER GENERATOR
        // 1 किंवा 2 अंकी नंबर
        // --------------------------------------------------------

        int smallNumber() {

            if (random.nextInt(100) < 30) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        // --------------------------------------------------------
        // UNIQUE NUMBERS
        // --------------------------------------------------------

        boolean containsNumber(int n) {

            for (int x : numbers) {

                if (x == n) {
                    return true;
                }
            }

            return false;
        }

        // --------------------------------------------------------
        // FILL 10 BOXES
        // --------------------------------------------------------

        void fillNumbers() {

            while (numbers.size() < 10) {

                int n = smallNumber();

                if (!containsNumber(n)) {

                    numbers.add(n);
                }
            }

            Collections.shuffle(numbers, random);
        }

        // --------------------------------------------------------
        // NEW PUZZLE
        // --------------------------------------------------------

        void newPuzzle() {

            numbers.clear();

            selected1 = -1;
            selected2 = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;

            // ====================================================
            // ADDITION
            // ====================================================

            if (type == 0) {

                operation = "+";

                do {

                    x = smallNumber();
                    y = smallNumber();

                    target = x + y;

                } while (target < 10 || target > 150);

                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target +
                                " मिळेल?";

            }

            // ====================================================
            // SUBTRACTION
            // ====================================================

            else if (type == 1) {

                operation = "−";

                do {

                    x = smallNumber();
                    y = smallNumber();

                } while (x <= y);

                target = x - y;

                question =
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा केल्यावर "
                                + target +
                                " मिळेल?";

            }

            // ====================================================
            // MULTIPLICATION
            // 2 DIGIT × 2 DIGIT = 4 DIGIT
            // ====================================================

            else if (type == 2) {

                operation = "×";

                do {

                    x = 10 + random.nextInt(90);
                    y = 10 + random.nextInt(90);

                    target = x * y;

                } while (
                        target < 1000 ||
                        target > 9999
                );

                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target +
                                " मिळेल?";

            }

            // ====================================================
            // DIVISION
            // ====================================================

            else {

                operation = "÷";

                do {

                    y = 2 + random.nextInt(8);

                    target = 2 + random.nextInt(10);

                    x = y * target;

                } while (x > 99);

                question =
                        "कोणता नंबर कोणत्या नंबरने भागल्यावर "
                                + target +
                                " मिळेल?";
            }

            // योग्य उत्तराचे नंबर आधी टाका
            numbers.add(x);
            numbers.add(y);

            // उरलेले नंबर भरा
            fillNumbers();

            invalidate();
        }

        // --------------------------------------------------------
        // CHECK ANSWER
        // --------------------------------------------------------

        boolean isCorrect(int x, int y) {

            if (operation.equals("+")) {

                return x + y == target;
            }

            if (operation.equals("−")) {

                return x > y &&
                        x - y == target;
            }

            if (operation.equals("×")) {

                return x * y == target;
            }

            return y != 0 &&
                    x % y == 0 &&
                    x / y == target;
        }

        // --------------------------------------------------------
        // CHECK BUTTON
        // --------------------------------------------------------

        void checkAnswer() {

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

                        level++;

                        newPuzzle();
                    }

                }, 700);

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                invalidate();
            }
        }

        // --------------------------------------------------------
        // TEXT DRAW
        // --------------------------------------------------------

        void drawText(
                Canvas canvas,
                String text,
                float x,
                float y,
                float size,
                int color,
                boolean bold
        ) {

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            canvas.drawText(text, x, y, p);
        }

        // --------------------------------------------------------
        // CENTER TEXT
        // --------------------------------------------------------

        void centerText(
                Canvas canvas,
                String text,
                RectF rect,
                float size,
                int color
        ) {

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(Typeface.DEFAULT_BOLD);

            Paint.FontMetrics fm = p.getFontMetrics();

            float x =
                    rect.centerX()
                            - p.measureText(text) / 2;

            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent) / 2;

            canvas.drawText(text, x, y, p);
        }

        // --------------------------------------------------------
        // ROUNDED BUTTON
        // --------------------------------------------------------

        void drawButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color,
                float textSize
        ) {

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    22,
                    22,
                    p
            );

            // हलका shine
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(2);

            p.setColor(
                    Color.argb(
                            80,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRoundRect(
                    rect,
                    22,
                    22,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            centerText(
                    canvas,
                    text,
                    rect,
                    textSize,
                    Color.WHITE
            );
        }

        // --------------------------------------------------------
        // QUESTION NUMBER HIGHLIGHT
        // --------------------------------------------------------

        void drawQuestion(Canvas canvas, float x, float y, float size) {

            String[] parts =
                    question.split(" ");

            StringBuilder normal =
                    new StringBuilder();

            for (String word : parts) {

                if (word.matches(".*\\d+.*")) {

                    String clean =
                            word.replaceAll("[^0-9]", "");

                    if (!clean.isEmpty()) {

                        String before =
                                word.substring(
                                        0,
                                        word.indexOf(clean)
                                );

                        String after =
                                word.substring(
                                        word.indexOf(clean)
                                                + clean.length()
                                );

                        if (before.length() > 0) {

                            drawText(
                                    canvas,
                                    normal.toString()
                                            + before,
                                    x,
                                    y,
                                    size,
                                    Color.WHITE,
                                    false
                            );

                            normal.setLength(0);
                        }

                        drawText(
                                canvas,
                                clean,
                                x + p.measureText(
                                        normal.toString()
                                ),
                                y,
                                size,
                                yellow,
                                true
                        );

                        if (after.length() > 0) {

                            drawText(
                                    canvas,
                                    after,
                                    x + p.measureText(clean),
                                    y,
                                    size,
                                    Color.WHITE,
                                    false
                            );
                        }

                        return;
                    }
                }

                if (normal.length() > 0) {

                    normal.append(" ");
                }

                normal.append(word);
            }

            drawText(
                    canvas,
                    normal.toString(),
                    x,
                    y,
                    size,
                    Color.WHITE,
                    false
            );
        }

        // ========================================================
        // ON DRAW
        // ========================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            canvas.drawColor(backgroundColor);

            int width = getWidth();

            int height = getHeight();

            // ----------------------------------------------------
            // RESPONSIVE SCALE
            // ----------------------------------------------------

            float base =
                    Math.min(width / 720f, height / 1480f);

            if (base < 0.65f)
                base = 0.65f;

            if (base > 1.20f)
                base = 1.20f;

            float side =
                    Math.max(20, width * 0.035f);

            // ====================================================
            // HEADER
            // ====================================================

            float headerTop =
                    Math.max(28, height * 0.025f);

            // JD
            float jdSize =
                    Math.max(
                            50,
                            width * 0.115f
                    );

            drawText(
                    canvas,
                    "JD",
                    width * 0.40f,
                    headerTop + jdSize,
                    jdSize,
                    yellow,
                    true
            );

            // crowns
            drawText(
                    canvas,
                    "♛",
                    width * 0.30f,
                    headerTop + jdSize * 0.55f,
                    jdSize * 0.45f,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "♛",
                    width * 0.62f,
                    headerTop + jdSize * 0.55f,
                    jdSize * 0.45f,
                    yellow,
                    true
            );

            // score box
            float scoreSize =
                    Math.max(
                            16,
                            width * 0.032f
                    );

            String scoreText =
                    "★ " + score + " गुण";

            p.setTextSize(scoreSize);

            p.setTypeface(Typeface.DEFAULT_BOLD);

            float scoreWidth =
                    p.measureText(scoreText);

            RectF scoreBox =
                    new RectF(
                            width - scoreWidth - side - 12,
                            headerTop + 8,
                            width - side,
                            headerTop + 52
                    );

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(2);

            p.setColor(yellow);

            canvas.drawRoundRect(
                    scoreBox,
                    12,
                    12,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            centerText(
                    canvas,
                    scoreText,
                    scoreBox,
                    scoreSize,
                    yellow
            );

            // ====================================================
            // NUMBER PUZZLE TITLE
            // ====================================================

            float titleY =
                    headerTop + jdSize + 8;

            float numberPuzzleSize =
                    Math.max(
                            34,
                            width * 0.075f
                    );

            drawText(
                    canvas,
                    "NUMBER",
                    side,
                    titleY,
                    numberPuzzleSize,
                    Color.WHITE,
                    true
            );

            p.setTextSize(numberPuzzleSize);

            float numberWidth =
                    p.measureText("NUMBER");

            drawText(
                    canvas,
                    "PUZZLE",
                    side + numberWidth + 10,
                    titleY,
                    numberPuzzleSize,
                    yellow,
                    true
            );

            // ====================================================
            // LEVEL LINE
            // ====================================================

            float subSize =
                    Math.max(
                            14,
                            width * 0.032f
                    );

            String levelText =
                    "Level " +
                            level +
                            " / 1000 • छोटे नंबर • No Timer";

            drawText(
                    canvas,
                    levelText,
                    side,
                    titleY + subSize + 8,
                    subSize,
                    Color.LTGRAY,
                    false
            );

            // ====================================================
            // QUESTION CARD
            // ====================================================

            float questionTop =
                    titleY +
                            subSize +
                            Math.max(
                                    30,
                                    height * 0.025f
                            );

            float questionHeight =
                    Math.max(
                            250,
                            height * 0.25f
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            width - side,
                            questionTop + questionHeight
                    );

            // card
            p.setColor(cardColor);

            canvas.drawRoundRect(
                    questionCard,
                    28,
                    28,
                    p
            );

            // blue border
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(
                    Math.max(2, width * 0.004f)
            );

            p.setColor(
                    Color.rgb(
                            20,
                            140,
                            255
                    )
            );

            canvas.drawRoundRect(
                    questionCard,
                    28,
                    28,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            // ====================================================
            // QUESTION TITLE
            // ====================================================

            float qTitleSize =
                    Math.max(
                            22,
                            width * 0.050f
                    );

            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    side + 30,
                    questionTop + 58,
                    qTitleSize,
                    yellow,
                    true
            );

            // ====================================================
            // QUESTION TEXT
            // ====================================================

            float questionSize =
                    Math.max(
                            24,
                            width * 0.055f
                    );

            p.setTextSize(questionSize);

            p.setTypeface(Typeface.DEFAULT);

            float maxQuestionWidth =
                    questionCard.width() - 60;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            String[] words =
                    question.split(" ");

            for (String word : words) {

                String test =
                        current.isEmpty()
                                ? word
                                : current + " " + word;

                if (p.measureText(test)
                        <= maxQuestionWidth) {

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
                    questionTop + 125;

            float lineHeight =
                    questionSize + 14;

            for (
                    int i = 0;
                    i < lines.size() && i < 2;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        side
