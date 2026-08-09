package com.jd.numberpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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

    // ============================================================
    // HIDE SYSTEM BARS
    // ============================================================

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

        // ========================================================
        // COLORS
        // ========================================================

        int backgroundColor =
                Color.rgb(3, 10, 20);

        int questionColor =
                Color.rgb(8, 28, 48);

        int boxColor =
                Color.rgb(15, 29, 49);

        int selectedColor =
                Color.rgb(25, 95, 170);

        int blue =
                Color.rgb(20, 130, 255);

        int yellow =
                Color.rgb(255, 215, 0);

        int white =
                Color.WHITE;

        int red =
                Color.rgb(245, 35, 50);

        int green =
                Color.rgb(0, 210, 95);

        int purple =
                Color.rgb(105, 45, 230);

        // ========================================================
        // CONSTRUCTOR
        // ========================================================

        GameView(Context context) {

            super(context);

            setFocusable(true);

            for (int i = 0; i < 10; i++) {
                numberBoxes[i] = new RectF();
            }

            newPuzzle();
        }

        // ========================================================
        // SMALL NUMBER
        // ========================================================

        int smallNumber() {

            if (random.nextInt(100) < 30) {

                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        // ========================================================
        // CHECK DUPLICATE
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

            int x = 0;
            int y = 0;

            // ====================================================
            // ADDITION
            // ====================================================

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
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा "
                                + "केल्यावर "
                                + target
                                + " मिळेल?";
            }

            // ====================================================
            // MULTIPLICATION
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
                                + target
                                + " मिळेल?";
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
                                + target
                                + " मिळेल?";
            }

            // योग्य दोन नंबर
            numbers.add(x);
            numbers.add(y);

            // उरलेले नंबर
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

                return x > y &&
                        x - y == target;
            }

            if (operation.equals("×")) {

                return x * y == target;
            }

            if (operation.equals("÷")) {

                return y != 0 &&
                        x % y == 0 &&
                        x / y == target;
            }

            return false;
        }

        // ========================================================
        // CHECK BUTTON
        // ========================================================

        void checkAnswer() {

            if (
                    selected1 < 0 ||
                    selected2 < 0
            ) {

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

                postDelayed(
                        new Runnable() {

                            @Override
                            public void run() {

                                if (level >= 1000) {

                                    showResult(true);

                                } else if (level % 10 == 0) {

                                    showResult(false);

                                } else {

                                    level++;

                                    newPuzzle();
                                }
                            }
                        },
                        700
                );

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                invalidate();
            }
        }

        // ========================================================
        // DRAW TEXT
        // ========================================================

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

            p.setStyle(Paint.Style.FILL);

            canvas.drawText(
                    text,
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
                String text,
                RectF rect,
                float size,
                int color
        ) {

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(Typeface.DEFAULT_BOLD);

            p.setStyle(Paint.Style.FILL);

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float x =
                    rect.centerX()
                            - p.measureText(text) / 2f;

            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent) / 2f;

            canvas.drawText(
                    text,
                    x,
                    y,
                    p
            );
        }

        // ========================================================
        // ROUNDED BUTTON
        // ========================================================

        void drawButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color,
                float textSize
        ) {

            // Main button
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    18,
                    18,
                    p
            );

            // Border
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(2);

            p.setColor(
                    Color.argb(
                            150,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRoundRect(
                    rect,
                    18,
                    18,
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

        // ========================================================
        // QUESTION CARD
        // ========================================================

        void drawQuestionCard(
                Canvas canvas,
                float left,
                float top,
                float right,
                float bottom,
                float width
        ) {

            RectF card =
                    new RectF(
                            left,
                            top,
                            right,
                            bottom
                    );

            // Card
            p.setStyle(Paint.Style.FILL);
            p.setColor(questionColor);

            canvas.drawRoundRect(
                    card,
                    24,
                    24,
                    p
            );

            // Blue border
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(2.5f);

            p.setColor(blue);

            canvas.drawRoundRect(
                    card,
                    24,
                    24,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            // Question title
            float qTitleSize =
                    Math.max(
                            22,
                            width * 0.052f
                    );

            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    left + 18,
                    top + 48,
                    qTitleSize,
                    yellow,
                    true
            );

            // Question text
            float qSize =
                    Math.max(
                            20,
                            Math.min(
                                    31,
                                    width * 0.050f
                            )
                    );

            p.setTextSize(qSize);
            p.setTypeface(Typeface.DEFAULT);

            float maxWidth =
                    card.width() - 36;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            String[] words =
                    question.split(" ");

            for (String word : words) {

                String test =
                        current.length() == 0
                                ? word
                                : current + " " + word;

                if (p.measureText(test)
                        <= maxWidth) {

                    current = test;

                } else {

                    if (current.length() > 0) {
                        lines.add(current);
                    }

                    current = word;
                }
            }

            if (current.length() > 0) {
                lines.add(current);
            }

            float startY =
                    top + 92;

            float lineHeight =
                    qSize + 13;

            for (
                    int i = 0;
                    i < lines.size() && i < 3;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        left + 18,
                        startY + i * lineHeight,
                        qSize,
                        white,
                        false
                );
            }

            // Operation
            float opY =
                    bottom - 30;

            drawText(
                    canvas,
                    "क्रिया: " + operation,
                    left + 18,
                    opY,
                    Math.max(
                            19,
                            width * 0.040f
                    ),
                    yellow,
                    true
            );

            String operationName = "";

            if (operation.equals("+")) {
                operationName = " अधिक";
            } else if (operation.equals("−")) {
                operationName = " वजा";
            } else if (operation.equals("×")) {
                operationName = " गुणाकार";
            } else if (operation.equals("÷")) {
                operationName = " भागाकार";
            }

            p.setTextSize(
                    Math.max(
                            19,
                            width * 0.040f
                    )
            );

            float opWidth =
                    p.measureText(
                            "क्रिया: " + operation
                    );

            drawText(
                    canvas,
                    operationName,
                    left + 18 + opWidth + 5,
                    opY,
                    Math.max(
                            19,
                            width * 0.040f
                    ),
                    yellow,
                    true
            );
        }

        // ========================================================
        // HEADER
        // ========================================================

        void drawHeader(
                Canvas canvas,
                float width,
                float height,
                float side
        ) {

            // ====================================================
            // FIXED HEADER
            // JD आणि NUMBER PUZZLE वेगळे
            // ====================================================

            float headerTop =
                    Math.max(
                            20,
                            height * 0.025f
                    );

            // ----------------------------------------------------
            // JD
            // ----------------------------------------------------

            float jdSize =
                    Math.max(
                            52,
                            Math.min(
                                    76,
                                    width * 0.105f
                            )
                    );

            p.setTextSize(jdSize);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            float jdWidth =
                    p.measureText("JD");

            // JD center
            drawText(
                    canvas,
                    "JD",
                    (width - jdWidth) / 2f,
                    headerTop + jdSize,
                    jdSize,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // LEFT CROWN
            // ----------------------------------------------------

            drawText(
                    canvas,
                    "♛",
                    width / 2f
                            - jdWidth / 2f
                            - jdSize * 0.55f,
                    headerTop + jdSize * 0.70f,
                    jdSize * 0.42f,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // RIGHT CROWN
            // ----------------------------------------------------

            drawText(
                    canvas,
                    "♛",
                    width / 2f
                            + jdWidth / 2f
                            + jdSize * 0.10f,
                    headerTop + jdSize * 0.70f,
                    jdSize * 0.42f,
                    yellow,
                    true
            );

            // ====================================================
            // SCORE BOX
            // ====================================================

            float scoreSize =
                    Math.max(
                            16,
                            width * 0.032f
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
                                    - 18,
                            headerTop + 5,
                            width - side,
                            headerTop + 50
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
            // NUMBER PUZZLE
            // ====================================================

            float titleY =
                    headerTop
                            + jdSize
                            + 48;

            float numberPuzzleSize =
                    Math.max(
                            30,
                            Math.min(
                                    48,
                                    width * 0.070f
                            )
                    );

            p.setTextSize(
                    numberPuzzleSize
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            String title1 =
                    "NUMBER";

            String title2 =
                    " PUZZLE";

            float titleWidth =
                    p.measureText(title1)
                            + p.measureText(title2);

            float titleX =
                    (width - titleWidth) / 2f;

            // NUMBER
            drawText(
                    canvas,
                    title1,
                    titleX,
                    titleY,
                    numberPuzzleSize,
                    Color.WHITE,
                    true
            );

            float numberWidth =
                    p.measureText(title1);

            // PUZZLE
            drawText(
                    canvas,
                    title2,
                    titleX + numberWidth,
                    titleY,
                    numberPuzzleSize,
                    yellow,
                    true
            );

            // ====================================================
            // LEVEL
            // ====================================================

            float subSize =
                    Math.max(
                            14,
                            width * 0.032f
                    );

            String levelText =
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer";

            p.setTextSize(subSize);

            float levelWidth =
                    p.measureText(levelText);

            drawText(
                    canvas,
                    levelText,
                    (width - levelWidth) / 2f,
                    titleY + subSize + 8,
                    subSize,
                    Color.LTGRAY,
                    false
            );
        }

        // ========================================================
        // ON DRAW
        // ========================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            canvas.drawColor(
                    backgroundColor
            );

            int width =
                    getWidth();

            int height =
                    getHeight();

            float side =
                    Math.max(
                            20,
                            width * 0.035f
                    );

            // ====================================================
            // HEADER
            // ====================================================

            drawHeader(
                    canvas,
                    width,
                    height,
                    side
            );

            // ====================================================
            // CALCULATE HEADER HEIGHT
            // ====================================================

            float headerTop =
                    Math.max(
                            20,
                            height * 0.025f
                    );

            float jdSize =
                    Math.max(
                            52,
                            Math.min(
                                    76,
                                    width * 0.105f
                            )
                    );

            float titleY =
                    headerTop
                            + jdSize
                            + 48;

            float subSize =
                    Math.max(
                            14,
                            width * 0.032f
                    );

            // ====================================================
            // QUESTION CARD POSITION
            // ====================================================

            float questionTop =
                    titleY
                            + subSize
                            + 28;

            // Responsive card height
            float questionHeight =
                    Math.max(
                            190,
                            Math.min(
                                    230,
                                    height * 0.19f
                            )
                    );

            float questionBottom =
                    questionTop
                            + questionHeight;

            // ====================================================
            // QUESTION CARD
            // ====================================================

            drawQuestionCard(
                    canvas,
                    side,
                    questionTop,
                    width - side,
                    questionBottom,
                    width
            );

            // ====================================================
            // NUMBER BOX AREA
            // ====================================================

            float numbersTop =
                    questionBottom + 25;

            float gap =
                    Math.max(
                            8,
                            width * 0.012f
                    );

            float boxWidth =
                    (
                            width
                                    - side * 2
                                    - gap * 4
                    ) / 5f;

            float boxHeight =
                    Math.max(
                            100,
                            Math.min(
                                    155,
                                    height * 0.115f
                            )
                    );

            for (int i = 0; i < 10; i++) {

                int row = i / 5;

                int col = i % 5;

                float left =
                        side
                                + col
                                * (boxWidth + gap);

                float top =
                        numbersTop
                                + row
                                * (boxHeight + gap);

                RectF rect =
                        numberBoxes[i];

                rect.set(
                        left,
                        top,
                        left + boxWidth,
                        top + boxHeight
                );

                // Selected / normal color
                if (
                        i == selected1 ||
                        i == selected2
                ) {

                    p.setColor(
                            selectedColor
                    );

                } else {

                    p.setColor(
                            boxColor
                    );
                }

                p.setStyle(Paint.Style.FILL);

                canvas.drawRoundRect(
                        rect,
                        15,
                        15,
                        p
                );

                // Blue border
                p.setStyle(Paint.Style.STROKE);

                p.setStrokeWidth(
                        i == selected1 ||
                                i == selected2
                                ? 3
                                : 1.5f
                );

                p.setColor(
                        blue
                );

                canvas.drawRoundRect(
                        rect,
                        15,
                        15,
                        p
                );

                p.setStyle(Paint.Style.FILL);

                // Number
                centerText(
                        canvas,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        rect,
                        Math.max(
                                23,
                                Math.min(
                                        34,
                                        width * 0.050f
                                )
                        ),
                        Color.WHITE
                );
            }

            // ====================================================
            // HINT BOX
            // ====================================================

            float hintTop =
                    numbersTop
                            + 2 * (boxHeight + gap)
                            + 22;

            float hintHeight =
                    Math.max(
                            62,
                            Math.min(
                                    82,
                                    height * 0.065f
                            )
                    );

            RectF hintBox =
                    new RectF(
                            side,
                            hintTop,
                            width - side,
                            hintTop + hintHeight
                    );

            p.setStyle(Paint.Style.FILL);

            p.setColor(
                    questionColor
            );

            canvas.drawRoundRect(
                    hintBox,
                    15,
                    15,
                    p
            );

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(1.5f);

            p.setColor(
                    blue
            );

            canvas.drawRoundRect(
                    hintBox,
                    15,
                    15,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            String hint =
                    "💡 सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा";

            float hintSize =
                    Math.max(
                            15,
                            Math.min(
                                    22,
                                    width * 0.036f
                            )
                    );

            p.setTextSize(hintSize);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            float hintWidth =
                    p.measureText(hint);

            if (hintWidth <= hintBox.width() - 20) {

                centerText(
                        canvas,
                        hint,
                        hintBox,
                        hintSize,
                        Color.WHITE
                );

            } else {

                drawText(
                        canvas,
                        "💡 सूचना : वरील 10 नंबरमधून",
                        hintBox.left + 12,
                        hintBox.centerY() - 3,
                        hintSize,
                        Color.WHITE,
                        true
                );

                drawText(
                        canvas,
                        "योग्य दोन नंबर निवडा",
                        hintBox.left + 12,
                        hintBox.centerY() + hintSize + 2,
                        hintSize,
                        Color.WHITE,
                        true
                );
            }

            // ====================================================
            // STATUS
            // ====================================================

            if (
                    status != null &&
                    status.length() > 0
            ) {

                float statusY =
                        hintBox.bottom + 28;

                int statusColor;

                if (status.startsWith("✓")) {

                    statusColor = green;

                } else {

                    statusColor = Color.rgb(
                            255,
                            90,
                            90
                    );
                }

                float statusSize =
                        Math.max(
                                16,
                                width * 0.034f
                        );

                p.setTextSize(
                        statusSize
                );

                float statusWidth =
                        p.measureText(status);

                drawText(
                        canvas,
                        status,
                        (width - statusWidth) / 2f,
                        statusY,
                        statusSize,
                        statusColor,
                        true
                );
            }

            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonTop =
                    hintBox.bottom
                            + (
                            status.length() > 0
                                    ? 48
                                    : 25
                    );

            float buttonGap =
                    Math.max(
                            8,
                            width * 0.012f
                    );

            float buttonWidth =
                    (
                            width
                                    - side * 2
                                    - buttonGap * 2
                    ) / 3f;

            float buttonHeight =
                    Math.max(
                            58,
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
                    side
                            + buttonWidth
                            + buttonGap,
                    buttonTop,
                    side
                            + buttonWidth * 2
                            + buttonGap,
                    buttonTop + buttonHeight
            );

            resultButton.set(
                    side
                            + buttonWidth * 2
                            + buttonGap * 2,
                    buttonTop,
                    width - side,
                    buttonTop + buttonHeight
            );

            float buttonTextSize =
                    Math.max(
                            16,
                            Math.min(
                                    22,
                                    width * 0.040f
                            )
                    );

            drawButton(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    red,
                    buttonTextSize
            );

            drawButton(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    green,
                    buttonTextSize
            );

            drawButton(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    purple,
                    buttonTextSize
            );
        }

        // ========================================================
        // TOUCH
        // ========================================================

        @Override
        public boolean onTouchEvent(
                MotionEvent event
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

            // ====================================================
            // NUMBER BOX
            // ====================================================

            for (int i = 0; i < 10; i++) {

                if (
                        numberBoxes[i]
                                .contains(x, y)
                ) {

                    selectNumber(i);

                    return true;
                }
            }

            // ====================================================
            // RESET
            // ====================================================

            if (
                    resetButton.contains(x, y)
            ) {

                newPuzzle();

                invalidate();

                return true;
            }

            // ====================================================
            // CHECK
            // ====================================================

            if (
                    checkButton.contains(x, y)
            ) {

                checkAnswer();

                return true;
            }

            // ====================================================
            // RESULT
            // ====================================================

            if (
                    resultButton.contains(x, y)
            ) {

                showResult(false);

                return true;
            }

            return true;
        }

        // ========================================================
        // SELECT NUMBER
        // ========================================================

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

        // ========================================================
        // RESULT DIALOG
        // ========================================================

        void showResult(
                boolean completed
        ) {

            String title;

            if (completed) {

                title =
                        "🎉 अभिनंदन!";

            } else {

                title =
                        "📊 तुमचा Result";
            }

            String message =
                    "Level : "
                            + level
                            + " / 1000\n\n"
                            + "⭐ गुण : "
                            + score
                            + "\n\n"
                            + "✓ बरोबर : "
                            + roundCorrect
                            + "\n"
                            + "✗ चुकले : "
                            + roundWrong;

            if (completed) {

                message +=
                        "\n\n🏆 तुम्ही सर्व 1000 Levels पूर्ण केले!";
            }

            AlertDialog dialog =
                    new AlertDialog.Builder(
                            MainActivity.this
                    )
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(
                                    "OK",
                                    null
                            )
                            .create();

            dialog.show();
        }
    }
}
