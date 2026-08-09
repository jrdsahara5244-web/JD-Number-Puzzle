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
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity {

    GameView game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestFullscreen();

        game = new GameView(this);
        setContentView(game);
    }

    private void requestFullscreen() {

        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideSystemBars();
    }

    private void hideSystemBars() {

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
            hideSystemBars();
        }
    }


    // ============================================================
    // GAME VIEW
    // ============================================================

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Random random = new Random();

        ArrayList<Integer> numbers =
                new ArrayList<>();

        RectF[] numberBoxes =
                new RectF[10];

        RectF resetButton =
                new RectF();

        RectF checkButton =
                new RectF();

        RectF resultButton =
                new RectF();

        int level = 1;

        int score = 0;

        int correct = 0;

        int wrong = 0;

        int selected1 = -1;

        int selected2 = -1;

        int target = 0;

        String operation = "";

        String question = "";

        String status = "";


        // ========================================================
        // COLORS
        // ========================================================

        int background =
                Color.rgb(3, 10, 20);

        int questionCard =
                Color.rgb(7, 27, 47);

        int numberCard =
                Color.rgb(15, 29, 48);

        int yellow =
                Color.rgb(255, 215, 0);

        int blue =
                Color.rgb(25, 135, 255);

        int green =
                Color.rgb(0, 210, 95);

        int red =
                Color.rgb(245, 35, 50);

        int purple =
                Color.rgb(105, 45, 235);

        int white =
                Color.WHITE;


        // ========================================================
        // CONSTRUCTOR
        // ========================================================

        GameView(Context context) {

            super(context);

            setFocusable(true);

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

            Collections.shuffle(
                    numbers,
                    random
            );
        }


        // ========================================================
        // NEW PUZZLE
        // ========================================================

        void newPuzzle() {

            numbers.clear();

            selected1 = -1;
            selected2 = -1;

            status = "";

            int type =
                    random.nextInt(4);

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

                }
                while (
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

                }
                while (x <= y);

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

                    x =
                            10 + random.nextInt(90);

                    y =
                            10 + random.nextInt(90);

                    target = x * y;

                }
                while (
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

                    y =
                            2 + random.nextInt(8);

                    target =
                            2 + random.nextInt(10);

                    x = y * target;

                }
                while (x > 99);

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

        boolean isCorrect(
                int x,
                int y
        ) {

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


            if (
                    isCorrect(
                            first,
                            second
                    )
            ) {

                score++;

                correct++;

                status =
                        "✓ बरोबर! +1 गुण";

                invalidate();

                postDelayed(
                        new Runnable() {

                            @Override
                            public void run() {

                                if (
                                        level >= 1000
                                ) {

                                    showResult(true);

                                    return;
                                }

                                if (
                                        level % 10 == 0
                                ) {

                                    showResult(false);

                                } else {

                                    level++;

                                    newPuzzle();
                                }
                            }

                        },
                        650
                );

            } else {

                wrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                invalidate();
            }
        }


        // ========================================================
        // RESET
        // ========================================================

        void resetPuzzle() {

            selected1 = -1;

            selected2 = -1;

            status = "";

            newPuzzle();
        }


        // ========================================================
        // RESULT
        // ========================================================

        void showResult(
                boolean finalGame
        ) {

            String title;

            if (finalGame) {

                title =
                        "🎉 JD NUMBER PUZZLE पूर्ण!";
            } else {

                title =
                        "📊 निकाल";
            }


            String message =
                    "Level : "
                            + level
                            + " / 1000\n\n"
                            + "✓ बरोबर : "
                            + correct
                            + "\n"
                            + "✗ चुकले : "
                            + wrong
                            + "\n\n"
                            + "⭐ एकूण गुण : "
                            + score;


            new AlertDialog.Builder(
                    MainActivity.this
            )
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                            "पुढे चला",
                            (dialog, which) -> {

                                if (!finalGame) {

                                    level++;

                                    newPuzzle();
                                }
                            }
                    )
                    .setNegativeButton(
                            "बंद",
                            null
                    )
                    .show();
        }


        // ========================================================
        // TEXT
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

            p.setStyle(
                    Paint.Style.FILL
            );

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

            p.setTextSize(size);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setColor(color);

            p.setStyle(
                    Paint.Style.FILL
            );

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
        // ROUNDED CARD
        // ========================================================

        void roundedCard(
                Canvas canvas,
                RectF rect,
                int fill,
                int border
        ) {

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(fill);

            canvas.drawRoundRect(
                    rect,
                    20,
                    20,
                    p
            );

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(border);

            canvas.drawRoundRect(
                    rect,
                    20,
                    20,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );
        }


        // ========================================================
        // BUTTON
        // ========================================================

        void drawButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color
        ) {

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    15,
                    15,
                    p
            );

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(
                    Color.argb(
                            180,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRoundRect(
                    rect,
                    15,
                    15,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );

            centerText(
                    canvas,
                    text,
                    rect,
                    rect.height() * 0.32f,
                    Color.WHITE
            );
        }


        // ========================================================
        // MAIN DRAW
        // ========================================================

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            canvas.drawColor(
                    background
            );


            int width =
                    getWidth();

            int height =
                    getHeight();


            // ====================================================
            // RESPONSIVE SCALE
            // ====================================================

            float scale =
                    width / 690f;

            scale =
                    Math.max(
                            0.72f,
                            Math.min(
                                    scale,
                                    1.12f
                            )
                    );


            /*
             * मुख्य बदल:
             *
             * डॅशबोर्ड आता स्क्रीनच्या वरच्या भागात
             * अडकून राहणार नाही.
             *
             * उपलब्ध स्क्रीननुसार content height
             * ठरवली जाते.
             */

            float contentWidth =
                    width * 0.94f;

            float left =
                    (width - contentWidth) / 2f;

            float right =
                    width - left;


            // ====================================================
            // CONTENT HEIGHT
            // ====================================================

            float contentHeight;

            if (height < 1100) {

                contentHeight =
                        height * 0.92f;

            } else {

                contentHeight =
                        Math.min(
                                height * 0.82f,
                                1180
                        );
            }


            // ====================================================
            // TOP POSITION
            // ====================================================

            float top;

            if (height > 1250) {

                top =
                        Math.max(
                                25,
                                (height - contentHeight) * 0.20f
                        );

            } else {

                top =
                        22;
            }


            // ====================================================
            // HEADER
            // ====================================================

            float jdSize =
                    Math.max(
                            45,
                            Math.min(
                                    68,
                                    width * 0.105f
                            )
                    );


            float jdY =
                    top + jdSize;


            p.setTextSize(
                    jdSize
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );


            float jdWidth =
                    p.measureText("JD");


            drawText(
                    canvas,
                    "JD",
                    (width - jdWidth) / 2f,
                    jdY,
                    jdSize,
                    yellow,
                    true
            );


            // ====================================================
            // CROWN LEFT
            // ====================================================

            drawText(
                    canvas,
                    "♛",
                    width / 2f
                            - jdWidth / 2f
                            - jdSize * 0.50f,
                    jdY - jdSize * 0.25f,
                    jdSize * 0.40f,
                    yellow,
                    true
            );


            // ====================================================
            // CROWN RIGHT
            // ====================================================

            drawText(
                    canvas,
                    "♛",
                    width / 2f
                            + jdWidth / 2f
                            + jdSize * 0.08f,
                    jdY - jdSize * 0.25f,
                    jdSize * 0.40f,
                    yellow,
                    true
            );


            // ====================================================
            // SCORE
            // ====================================================

            float scoreSize =
                    Math.max(
                            15,
                            width * 0.030f
                    );


            String scoreText =
                    "★ "
                            + score
                            + " गुण";


            p.setTextSize(
                    scoreSize
            );

            float scoreWidth =
                    p.measureText(
                            scoreText
                    );


            RectF scoreBox =
                    new RectF(
                            right - scoreWidth - 22,
                            top + 5,
                            right,
                            top + 45
                    );


            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(yellow);

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
                    yellow
            );


            // ====================================================
            // NUMBER PUZZLE
            // ====================================================

            float titleSize =
                    Math.max(
                            30,
                            Math.min(
                                    46,
                                    width * 0.067f
                            )
                    );


            float titleY =
                    jdY + titleSize * 0.95f;


            p.setTextSize(
                    titleSize
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );


            String title1 =
                    "NUMBER";

            String title2 =
                    " PUZZLE";


            float totalTitleWidth =
                    p.measureText(title1)
                            + p.measureText(title2);


            float titleX =
                    (width - totalTitleWidth)
                            / 2f;


            drawText(
                    canvas,
                    title1,
                    titleX,
                    titleY,
                    titleSize,
                    white,
                    true
            );


            float numberWidth =
                    p.measureText(
                            title1
                    );


            drawText(
                    canvas,
                    title2,
                    titleX + numberWidth,
                    titleY,
                    titleSize,
                    yellow,
                    true
            );


            // ====================================================
            // LEVEL
            // ====================================================

            float subSize =
                    Math.max(
                            14,
                            Math.min(
                                    22,
                                    width * 0.030f
                            )
                    );


            String levelText =
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer";


            p.setTextSize(
                    subSize
            );


            float levelWidth =
                    p.measureText(
                            levelText
                    );


            float levelY =
                    titleY
                            + subSize
                            + 8;


            drawText(
                    canvas,
                    levelText,
                    (width - levelWidth) / 2f,
                    levelY,
                    subSize,
                    Color.LTGRAY,
                    false
            );


            // ====================================================
            // QUESTION CARD
            // ====================================================

            float questionTop =
                    levelY + 22;


            float questionHeight =
                    Math.max(
                            170,
                            Math.min(
                                    225,
                                    height * 0.16f
                            )
                    );


            RectF qCard =
                    new RectF(
                            left,
                            questionTop,
                            right,
                            questionTop
                                    + questionHeight
                    );


            roundedCard(
                    canvas,
                    qCard,
                    questionCard,
                    blue
            );


            // ====================================================
            // QUESTION TITLE
            // ====================================================

            float qTitleSize =
                    Math.max(
                            22,
                            Math.min(
                                    30,
                                    width * 0.045f
                            )
                    );


            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    left + 18,
                    questionTop + 42,
                    qTitleSize,
                    yellow,
                    true
            );


            // ====================================================
            // QUESTION
            // ====================================================

            float qSize =
                    Math.max(
                            20,
                            Math.min(
                                    27,
                                    width * 0.041f
                            )
                    );


            p.setTextSize(qSize);

            p.setTypeface(
                    Typeface.DEFAULT
            );


            float maxWidth =
                    qCard.width() - 36;


            ArrayList<String> lines =
                    makeLines(
                            question,
                            maxWidth
                    );


            float qY =
                    questionTop + 82;


            for (
                    int i = 0;
                    i < lines.size() && i < 2;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        left + 18,
                        qY,
                        qSize,
                        white,
                        false
                );

                qY +=
                        qSize + 8;
            }


            // ====================================================
            // OPERATION
            // ====================================================

            String operationText;

            if (operation.equals("+")) {

                operationText =
                        "क्रिया:  +  अधिक";

            } else if (
                    operation.equals("−")
            ) {

                operationText =
                        "क्रिया:  −  वजा";

            } else if (
                    operation.equals("×")
            ) {

                operationText =
                        "क्रिया:  ×  गुणाकार";

            } else {

                operationText =
                        "क्रिया:  ÷  भागाकार";
            }


            drawText(
                    canvas,
                    operationText,
                    left + 18,
                    questionTop
                            + questionHeight
                            - 25,
                    qTitleSize * 0.88f,
                    yellow,
                    true
            );


            // ====================================================
            // NUMBER BOXES
            // ====================================================

            float gridTop =
                    qCard.bottom + 18;


            float gap =
                    Math.max(
                            8,
                            width * 0.012f
                    );


            float boxWidth =
                    (contentWidth
                            - gap * 4)
                            / 5f;


            float boxHeight =
                    Math.max(
                            92,
                            Math.min(
                                    125,
                                    height * 0.095f
                            )
                    );


            for (int i = 0; i < 10; i++) {

                int row =
                        i / 5;

                int col =
                        i % 5;


                float x =
                        left
                                + col
                                * (boxWidth + gap);


                float y =
                        gridTop
                                + row
                                * (boxHeight + gap);


                numberBoxes[i] =
                        new RectF(
                                x,
                                y,
                                x + boxWidth,
                                y + boxHeight
                        );


                boolean selected =
                        i == selected1 ||
                                i == selected2;


                int fill =
                        selected
                                ? Color.rgb(
                                35,
                                75,
                                125
                        )
                                : numberCard;


                int border =
                        selected
                                ? yellow
                                : Color.rgb(
                                45,
                                110,
                                190
                        );


                roundedCard(
                        canvas,
                        numberBoxes[i],
                        fill,
                        border
                );


                centerText(
                        canvas,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        numberBoxes[i],
                        Math.max(
                                23,
                                Math.min(
                                        31,
                                        width * 0.045f
                                )
                        ),
                        white
                );
            }


            // ====================================================
            // HINT
            // ====================================================

            float hintTop =
                    gridTop
                            + boxHeight * 2
                            + gap
                            + 18;


            float hintHeight =
                    Math.max(
                            62,
                            Math.min(
                                    82,
                                    height * 0.065f
                            )
                    );


            RectF hint =
                    new RectF(
                            left,
                            hintTop,
                            right,
                            hintTop
                                    + hintHeight
                    );


            roundedCard(
                    canvas,
                    hint,
                    questionCard,
                    blue
            );


            drawText(
                    canvas,
                    "💡 सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा",
                    left + 18,
                    hint.centerY()
                            + 8,
                    Math.max(
                            15,
                            Math.min(
                                    20,
                                    width * 0.029f
                            )
                    ),
                    white,
                    true
            );


            // ====================================================
            // STATUS
            // ====================================================

            if (!status.isEmpty()) {

                drawText(
                        canvas,
                        status,
                        left + 18,
                        hint.bottom + 28,
                        17,
                        status.startsWith("✓")
                                ? green
                                : red,
                        true
                );
            }


            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonsTop =
                    hint.bottom
                            + (
                            status.isEmpty()
                                    ? 18
                                    : 42
                    );


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
                                    78,
                                    height * 0.065f
                            )
                    );


            resetButton =
                    new RectF(
                            left,
                            buttonsTop,
                            left + buttonWidth,
                            buttonsTop
                                    + buttonHeight
                    );


            checkButton =
                    new RectF(
                            left
                                    + buttonWidth
                                    + buttonGap,
                            buttonsTop,
                            left
                                    + buttonWidth * 2
                                    + buttonGap,
                            buttonsTop
                                    + buttonHeight
                    );


            resultButton =
                    new RectF(
                            left
                                    + (buttonWidth + buttonGap) * 2,
                            buttonsTop,
                            right,
                            buttonsTop
                                    + buttonHeight
                    );


            drawButton(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    red
            );


            drawButton(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    green
            );


            drawButton(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    purple
            );
        }


        // ========================================================
        // MAKE TEXT LINES
        // ========================================================

        ArrayList<String> makeLines(
                String text,
                float maxWidth
        ) {

            ArrayList<String> result =
                    new ArrayList<>();

            String current = "";

            String[] words =
                    text.split(" ");


            for (String word : words) {

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

                        result.add(current);
                    }

                    current = word;
                }
            }


            if (!current.isEmpty()) {

                result.add(current);
            }


            return result;
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


            // ----------------------------------------------------
            // NUMBER BOX
            // ----------------------------------------------------

            for (int i = 0; i < 10; i++) {

                if (
                        numberBoxes[i] != null &&
                                numberBoxes[i].contains(
                                        x,
                                        y
                                )
                ) {

                    selectNumber(i);

                    return true;
                }
            }


            // ----------------------------------------------------
            // RESET
            // ----------------------------------------------------

            if (
                    resetButton.contains(
                            x,
                            y
                    )
            ) {

                resetPuzzle();

                return true;
            }


            // ----------------------------------------------------
            // CHECK
            // ----------------------------------------------------

            if (
                    checkButton.contains(
                            x,
                            y
                    )
            ) {

                checkAnswer();

                return true;
            }


            // ----------------------------------------------------
            // RESULT
            // ----------------------------------------------------

            if (
                    resultButton.contains(
                            x,
                            y
                    )
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
    }
}
