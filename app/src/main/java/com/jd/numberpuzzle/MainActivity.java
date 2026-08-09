package com.jd.numberpuzzle;

import android.app.Activity;
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
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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

        RectF startButton = new RectF();
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

        boolean gameStarted = false;

        // ========================================================
        // COLORS
        // ========================================================

        final int BACKGROUND =
                Color.rgb(3, 10, 20);

        final int CARD =
                Color.rgb(7, 28, 48);

        final int BOX =
                Color.rgb(14, 29, 48);

        final int YELLOW =
                Color.rgb(255, 215, 0);

        final int BLUE =
                Color.rgb(20, 140, 255);

        final int WHITE =
                Color.WHITE;

        final int RED =
                Color.rgb(245, 30, 45);

        final int GREEN =
                Color.rgb(0, 205, 90);

        final int PURPLE =
                Color.rgb(105, 45, 235);

        // ========================================================
        // CONSTRUCTOR
        // ========================================================

        GameView(Context context) {
            super(context);

            setFocusable(true);

            newPuzzle();
        }

        // ========================================================
        // NUMBER GENERATOR
        // ========================================================

        int smallNumber() {

            if (random.nextInt(100) < 30) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        // ========================================================
        // UNIQUE NUMBER
        // ========================================================

        boolean containsNumber(int n) {

            for (int x : numbers) {

                if (x == n) {
                    return true;
                }
            }

            return false;
        }

        // ========================================================
        // FILL NUMBERS
        // ========================================================

        void fillNumbers() {

            while (numbers.size() < 10) {

                int n = smallNumber();

                if (!containsNumber(n)) {
                    numbers.add(n);
                }
            }

            Collections.shuffle(numbers, random);
        }

        // ========================================================
        // NEW PUZZLE
        // ========================================================

        void newPuzzle() {

            numbers.clear();

            selected1 = -1;
            selected2 = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;

            // ----------------------------------------------------
            // ADDITION
            // ----------------------------------------------------

            if (type == 0) {

                operation = "+";

                do {

                    x = smallNumber();
                    y = smallNumber();

                    target = x + y;

                } while (
                        target < 10 ||
                        target > 150
                );

                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target
                                + " मिळेल?";
            }

            // ----------------------------------------------------
            // SUBTRACTION
            // ----------------------------------------------------

            else if (type == 1) {

                operation = "−";

                do {

                    x = smallNumber();
                    y = smallNumber();

                } while (x <= y);

                target = x - y;

                question =
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा केल्यावर "
                                + target
                                + " मिळेल?";
            }

            // ----------------------------------------------------
            // MULTIPLICATION
            // ----------------------------------------------------

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
                                + target
                                + " मिळेल?";
            }

            // ----------------------------------------------------
            // DIVISION
            // ----------------------------------------------------

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

            numbers.add(x);
            numbers.add(y);

            fillNumbers();

            invalidate();
        }

        // ========================================================
        // CHECK ANSWER
        // ========================================================

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

            return y != 0
                    && x % y == 0
                    && x / y == target;
        }

        // ========================================================
        // CHECK
        // ========================================================

        void checkAnswer() {

            if (selected1 < 0 || selected2 < 0) {

                status =
                        "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first =
                    numbers.get(selected1);

            int second =
                    numbers.get(selected2);

            if (isCorrect(first, second)) {

                score++;

                roundCorrect++;

                status =
                        "✓ बरोबर! +1 गुण";

                invalidate();

                postDelayed(() -> {

                    if (level % 10 == 0) {

                        showResult(
                                level == 1000
                        );

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

        // ========================================================
        // RESULT
        // ========================================================

        void showResult(boolean finalLevel) {

            String title =
                    finalLevel
                            ? "🎉 अभिनंदन!"
                            : "📊 RESULT";

            String msg =
                    finalLevel
                            ? "तुम्ही 1000 Levels पूर्ण केले!"
                            : "Level " + level
                                    + " पूर्ण झाली.";

            new android.app.AlertDialog.Builder(
                    MainActivity.this
            )
                    .setTitle(title)
                    .setMessage(
                            msg
                                    + "\n\nगुण : "
                                    + score
                                    + "\nबरोबर : "
                                    + roundCorrect
                                    + "\nचुकीचे : "
                                    + roundWrong
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();
        }

        // ========================================================
        // TEXT
        // ========================================================

        void text(
                Canvas canvas,
                String value,
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

            p.setStyle(Paint.Style.FILL);

            canvas.drawText(
                    value,
                    x,
                    y,
                    p
            );
        }

        // ========================================================
        // CENTER TEXT
        // ========================================================

        void centerText(
                Canvas canvas,
                String value,
                RectF rect,
                float size,
                int color,
                boolean bold
        ) {

            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            p.setColor(color);

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float x =
                    rect.centerX()
                            - p.measureText(value) / 2f;

            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent) / 2f;

            canvas.drawText(
                    value,
                    x,
                    y,
                    p
            );
        }

        // ========================================================
        // ROUNDED RECT
        // ========================================================

        void rounded(
                Canvas canvas,
                RectF rect,
                int fill,
                int stroke,
                float radius
        ) {

            p.setStyle(Paint.Style.FILL);
            p.setColor(fill);

            canvas.drawRoundRect(
                    rect,
                    radius,
                    radius,
                    p
            );

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.5f);
            p.setColor(stroke);

            canvas.drawRoundRect(
                    rect,
                    radius,
                    radius,
                    p
            );

            p.setStyle(Paint.Style.FILL);
        }

        // ========================================================
        // BUTTON
        // ========================================================

        void button(
                Canvas canvas,
                RectF rect,
                String value,
                int color,
                float size
        ) {

            rounded(
                    canvas,
                    rect,
                    color,
                    Color.argb(
                            180,
                            255,
                            255,
                            255
                    ),
                    18
            );

            centerText(
                    canvas,
                    value,
                    rect,
                    size,
                    WHITE,
                    true
            );
        }

        // ========================================================
        // START SCREEN
        // ========================================================

        void drawStartScreen(
                Canvas canvas,
                int width,
                int height
        ) {

            canvas.drawColor(BACKGROUND);

            float center =
                    width / 2f;

            // ----------------------------------------------------
            // JD
            // ----------------------------------------------------

            float jdSize =
                    Math.min(
                            85,
                            Math.max(
                                    60,
                                    width * 0.13f
                            )
                    );

            centerText(
                    canvas,
                    "JD",
                    new RectF(
                            0,
                            height * 0.15f,
                            width,
                            height * 0.15f + jdSize + 20
                    ),
                    jdSize,
                    YELLOW,
                    true
            );

            text(
                    canvas,
                    "♛",
                    center - jdSize * 0.85f,
                    height * 0.19f,
                    jdSize * 0.38f,
                    YELLOW,
                    true
            );

            text(
                    canvas,
                    "♛",
                    center + jdSize * 0.60f,
                    height * 0.19f,
                    jdSize * 0.38f,
                    YELLOW,
                    true
            );

            // ----------------------------------------------------
            // TITLE
            // ----------------------------------------------------

            float titleSize =
                    Math.min(
                            50,
                            Math.max(
                                    32,
                                    width * 0.075f
                            )
                    );

            String a = "NUMBER ";
            String b = "PUZZLE";

            p.setTextSize(titleSize);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            float total =
                    p.measureText(a)
                            + p.measureText(b);

            float x =
                    (width - total) / 2f;

            text(
                    canvas,
                    a,
                    x,
                    height * 0.30f,
                    titleSize,
                    WHITE,
                    true
            );

            text(
                    canvas,
                    b,
                    x + p.measureText(a),
                    height * 0.30f,
                    titleSize,
                    YELLOW,
                    true
            );

            // ----------------------------------------------------
            // DESCRIPTION
            // ----------------------------------------------------

            centerText(
                    canvas,
                    "1000 Levels • छोटे नंबर • No Timer",
                    new RectF(
                            0,
                            height * 0.32f,
                            width,
                            height * 0.37f
                    ),
                    Math.max(
                            18,
                            width * 0.035f
                    ),
                    Color.LTGRAY,
                    false
            );

            // ----------------------------------------------------
            // START CARD
            // ----------------------------------------------------

            float cardLeft =
                    width * 0.08f;

            float cardRight =
                    width * 0.92f;

            RectF card =
                    new RectF(
                            cardLeft,
                            height * 0.40f,
                            cardRight,
                            height * 0.60f
                    );

            rounded(
                    canvas,
                    card,
                    CARD,
                    BLUE,
                    25
            );

            centerText(
                    canvas,
                    "🎮",
                    new RectF(
                            cardLeft,
                            card.top + 25,
                            cardRight,
                            card.top + 90
                    ),
                    45,
                    YELLOW,
                    true
            );

            centerText(
                    canvas,
                    "गेम सुरू करण्यासाठी तयार?",
                    new RectF(
                            cardLeft,
                            card.top + 90,
                            cardRight,
                            card.top + 140
                    ),
                    Math.max(
                            22,
                            width * 0.045f
                    ),
                    WHITE,
                    true
            );

            centerText(
                    canvas,
                    "योग्य दोन नंबर निवडा आणि गुण मिळवा!",
                    new RectF(
                            cardLeft,
                            card.top + 135,
                            cardRight,
                            card.top + 185
                    ),
                    Math.max(
                            15,
                            width * 0.030f
                    ),
                    Color.LTGRAY,
                    false
            );

            // ----------------------------------------------------
            // START BUTTON
            // ----------------------------------------------------

            startButton.set(
                    width * 0.16f,
                    height * 0.68f,
                    width * 0.84f,
                    height * 0.76f
            );

            button(
                    canvas,
                    startButton,
                    "▶  START GAME",
                    GREEN,
                    Math.max(
                            20,
                            width * 0.045f
                    )
            );
        }

        // ========================================================
        // GAME SCREEN
        // ========================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            int width =
                    getWidth();

            int height =
                    getHeight();

            if (!gameStarted) {

                drawStartScreen(
                        canvas,
                        width,
                        height
                );

                return;
            }

            canvas.drawColor(BACKGROUND);

            // ====================================================
            // RESPONSIVE DIMENSIONS
            // ====================================================

            float side =
                    Math.max(
                            18,
                            width * 0.035f
                    );

            float contentWidth =
                    width - side * 2;

            float scale =
                    Math.min(
                            width / 720f,
                            height / 1480f
                    );

            scale =
                    Math.max(
                            0.72f,
                            Math.min(
                                    scale,
                                    1.25f
                            )
                    );

            // ====================================================
            // HEADER
            // ====================================================

            float headerTop =
                    Math.max(
                            18,
                            height * 0.018f
                    );

            float jdSize =
                    Math.max(
                            48,
                            Math.min(
                                    78,
                                    width * 0.105f
                            )
                    );

            p.setTextSize(jdSize);
            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            float jdWidth =
                    p.measureText("JD");

            float jdX =
                    (width - jdWidth) / 2f;

            text(
                    canvas,
                    "JD",
                    jdX,
                    headerTop + jdSize,
                    jdSize,
                    YELLOW,
                    true
            );

            // crowns

            text(
                    canvas,
                    "♛",
                    jdX - jdSize * 0.48f,
                    headerTop + jdSize * 0.72f,
                    jdSize * 0.40f,
                    YELLOW,
                    true
            );

            text(
                    canvas,
                    "♛",
                    jdX + jdWidth + jdSize * 0.08f,
                    headerTop + jdSize * 0.72f,
                    jdSize * 0.40f,
                    YELLOW,
                    true
            );

            // ====================================================
            // SCORE
            // ====================================================

            float scoreSize =
                    Math.max(
                            15,
                            width * 0.031f
                    );

            String scoreText =
                    "★ " + score + " गुण";

            p.setTextSize(scoreSize);
            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            float scoreWidth =
                    p.measureText(scoreText);

            RectF scoreBox =
                    new RectF(
                            width
                                    - scoreWidth
                                    - side
                                    - 16,
                            headerTop + 5,
                            width - side,
                            headerTop + 49
                    );

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(YELLOW);

            canvas.drawRoundRect(
                    scoreBox,
                    12,
                    12,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );

            centerText(
                    canvas,
                    scoreText,
                    scoreBox,
                    scoreSize,
                    YELLOW,
                    true
            );

            // ====================================================
            // NUMBER PUZZLE TITLE
            // ====================================================

            float titleY =
                    headerTop
                            + jdSize
                            + 42;

            float titleSize =
                    Math.max(
                            30,
                            Math.min(
                                    48,
                                    width * 0.070f
                            )
                    );

            p.setTextSize(titleSize);
            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            String title1 =
                    "NUMBER ";

            String title2 =
                    "PUZZLE";

            float titleWidth =
                    p.measureText(title1)
                            + p.measureText(title2);

            float titleX =
                    (width - titleWidth) / 2f;

            text(
                    canvas,
                    title1,
                    titleX,
                    titleY,
                    titleSize,
                    WHITE,
                    true
            );

            float numberWidth =
                    p.measureText(title1);

            text(
                    canvas,
                    title2,
                    titleX + numberWidth,
                    titleY,
                    titleSize,
                    YELLOW,
                    true
            );

            // ====================================================
            // LEVEL
            // ====================================================

            float subSize =
                    Math.max(
                            14,
                            width * 0.031f
                    );

            String levelText =
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer";

            p.setTextSize(subSize);

            float levelWidth =
                    p.measureText(levelText);

            text(
                    canvas,
                    levelText,
                    (width - levelWidth) / 2f,
                    titleY + subSize + 8,
                    subSize,
                    Color.LTGRAY,
                    false
            );

            // ====================================================
            // QUESTION CARD
            // ====================================================

            float questionTop =
                    titleY
                            + subSize
                            + 22;

            float questionHeight =
                    Math.max(
                            185,
                            Math.min(
                                    245,
                                    height * 0.19f
                            )
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            width - side,
                            questionTop
                                    + questionHeight
                    );

            rounded(
                    canvas,
                    questionCard,
                    CARD,
                    BLUE,
                    24
            );

            // ----------------------------------------------------
            // QUESTION TITLE
            // ----------------------------------------------------

            float qTitle =
                    Math.max(
                            22,
                            width * 0.048f
                    );

            text(
                    canvas,
                    "🧮  प्रश्न",
                    side + 18,
                    questionTop + 40,
                    qTitle,
                    YELLOW,
                    true
            );

            // ----------------------------------------------------
            // QUESTION
            // ----------------------------------------------------

            float qSize =
                    Math.max(
                            19,
                            Math.min(
                                    30,
                                    width * 0.050f
                            )
                    );

            p.setTextSize(qSize);
            p.setTypeface(
                    Typeface.DEFAULT
            );

            float maxWidth =
                    questionCard.width()
                            - 36;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            for (
                    String word :
                    question.split(" ")
            ) {

                String test =
                        current.isEmpty()
                                ? word
                                : current + " " + word;

                if (
                        p.measureText(test)
                                <= maxWidth
                ) {

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
                    questionTop + 82;

            for (
                    int i = 0;
                    i < lines.size() && i < 2;
                    i++
            ) {

                text(
                        canvas,
                        lines.get(i),
                        side + 18,
                        qY + i * (qSize + 8),
                        qSize,
                        WHITE,
                        false
                );
            }

            // ====================================================
            // OPERATION
            // ====================================================

            String operationText;

            if (operation.equals("+")) {
                operationText =
                        "क्रिया:  +  अधिक";
            } else if (operation.equals("−")) {
                operationText =
                        "क्रिया:  −  वजा";
            } else if (operation.equals("×")) {
                operationText =
                        "क्रिया:  ×  गुणाकार";
            } else {
                operationText =
                        "क्रिया:  ÷  भागाकार";
            }

            text(
                    canvas,
                    operationText,
                    side + 18,
                    questionCard.bottom - 22,
                    Math.max(
                            19,
                            width * 0.043f
                    ),
                    YELLOW,
                    true
            );

            // ====================================================
            // NUMBER GRID
            // ====================================================

            float gridTop =
                    questionCard.bottom
                            + 16;

            float gap =
                    Math.max(
                            7,
                            width * 0.013f
                    );

            float boxWidth =
                    (
                            contentWidth
                                    - gap * 4
                    ) / 5f;

            float boxHeight =
                    Math.max(
                            92,
                            Math.min(
                                    145,
                                    height * 0.105f
                            )
                    );

            for (
                    int i = 0;
                    i < 10;
                    i++
            ) {

                int row =
                        i / 5;

                int col =
                        i % 5;

                float left =
                        side
                                + col
                                * (boxWidth + gap);

                float top =
                        gridTop
                                + row
                                * (boxHeight + gap);

                RectF r =
                        new RectF(
                                left,
                                top,
                                left + boxWidth,
                                top + boxHeight
                        );

                numberBoxes[i] = r;

                boolean selected =
                        i == selected1
                                || i == selected2;

                int fill =
                        selected
                                ? Color.rgb(
                                35,
                                100,
                                180
                        )
                                : BOX;

                int border =
                        selected
                                ? YELLOW
                                : Color.rgb(
                                30,
                                120,
                                230
                        );

                rounded(
                        canvas,
                        r,
                        fill,
                        border,
                        15
                );

                centerText(
                        canvas,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        r,
                        Math.max(
                                20,
                                Math.min(
                                        32,
                                        width * 0.048f
                                )
                        ),
                        WHITE,
                        true
                );
            }

            float gridBottom =
                    gridTop
                            + boxHeight * 2
                            + gap;

            // ====================================================
            // HINT
            // ====================================================

            float hintTop =
                    gridBottom + 16;

            float hintHeight =
                    Math.max(
                            60,
                            Math.min(
                                    82,
                                    height * 0.065f
                            )
                    );

            RectF hint =
                    new RectF(
                            side,
                            hintTop,
                            width - side,
                            hintTop + hintHeight
                    );

            rounded(
                    canvas,
                    hint,
                    CARD,
                    BLUE,
                    18
            );

            centerText(
                    canvas,
                    "💡 सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा",
                    hint,
                    Math.max(
                            14,
                            Math.min(
                                    21,
                                    width * 0.034f
                            )
                    ),
                    WHITE,
                    true
            );

            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonTop =
                    hint.bottom + 14;

            float buttonGap =
                    8;

            float buttonWidth =
                    (
                            contentWidth
                                    - buttonGap * 2
                    ) / 3f;

            float buttonHeight =
                    Math.max(
                            62,
                            Math.min(
                                    82,
                                    height * 0.065f
                            )
                    );

            resetButton.set(
                    side,
                    buttonTop,
                    side + buttonWidth,
                    buttonTop + buttonHeight
            );

            checkButton.set(
                    side + buttonWidth + buttonGap,
                    buttonTop,
                    side
                            + buttonWidth * 2
                            + buttonGap,
                    buttonTop + buttonHeight
            );

            resultButton.set(
                    side
                            + (buttonWidth + buttonGap) * 2,
                    buttonTop,
                    width - side,
                    buttonTop + buttonHeight
            );

            button(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    RED,
                    Math.max(
                            16,
                            Math.min(
                                    23,
                                    width * 0.038f
                            )
                    )
            );

            button(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    GREEN,
                    Math.max(
                            16,
                            Math.min(
                                    23,
                                    width * 0.038f
                            )
                    )
            );

            button(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    PURPLE,
                    Math.max(
                            16,
                            Math.min(
                                    23,
                                    width * 0.038f
                            )
                    )
            );

            // ====================================================
            // STATUS
            // ====================================================

            if (!status.isEmpty()) {

                int statusColor =
                        status.startsWith("✓")
                                ? GREEN
                                : status.startsWith("✗")
                                ? RED
                                : YELLOW;

                float statusY =
                        buttonTop
                                + buttonHeight
                                + 32;

                p.setTextSize(
                        Math.max(
                                18,
                                width * 0.040f
                        )
                );

                p.setTypeface(
                        Typeface.DEFAULT_BOLD
                );

                float sw =
                        p.measureText(status);

                text(
                        canvas,
                        status,
                        (width - sw) / 2f,
                        statusY,
                        Math.max(
                                18,
                                width * 0.040f
                        ),
                        statusColor,
                        true
                );
            }
        }

        // ========================================================
        // TOUCH
        // ========================================================

        @Override
        public boolean onTouchEvent(
                android.view.MotionEvent event
        ) {

            if (
                    event.getAction()
                            != MotionEvent.ACTION_UP
            ) {
                return true;
            }

            float x =
                    event.getX();

            float y =
                    event.getY();

            // ----------------------------------------------------
            // START GAME
            // ----------------------------------------------------

            if (!gameStarted) {

                if (
                        startButton.contains(x, y)
                ) {

                    gameStarted = true;

                    level = 1;

                    score = 0;

                    roundCorrect = 0;

                    roundWrong = 0;

                    newPuzzle();

                    invalidate();
                }

                return true;
            }

            // ----------------------------------------------------
            // NUMBER BOX
            // ----------------------------------------------------

            for (
                    int i = 0;
                    i < 10;
                    i++
            ) {

                if (
                        numberBoxes[i] != null
                                && numberBoxes[i]
                                .contains(x, y)
                ) {

                    if (selected1 == i) {

                        selected1 = -1;

                    } else if (selected2 == i) {

                        selected2 = -1;

                    } else if (selected1 < 0) {

                        selected1 = i;

                    } else if (selected2 < 0) {

                        selected2 = i;

                    } else {

                        selected1 = i;

                        selected2 = -1;
                    }

                    invalidate();

                    return true;
                }
            }

            // ----------------------------------------------------
            // RESET
            // ----------------------------------------------------

            if (
                    resetButton.contains(x, y)
            ) {

                selected1 = -1;

                selected2 = -1;

                status = "";

                newPuzzle();

                return true;
            }

            // ----------------------------------------------------
            // CHECK
            // ----------------------------------------------------

            if (
                    checkButton.contains(x, y)
            ) {

                checkAnswer();

                return true;
            }

            // ----------------------------------------------------
            // RESULT
            // ----------------------------------------------------

            if (
                    resultButton.contains(x, y)
            ) {

                showResult(
                        level == 1000
                );

                return true;
            }

            return true;
        }
    }
}
