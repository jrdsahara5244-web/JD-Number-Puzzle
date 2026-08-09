package com.jd.numberpuzzle;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
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

        RectF[] numberBoxes = new RectF[10];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();
        RectF startButton = new RectF();

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

        int backgroundColor =
                Color.rgb(2, 10, 20);

        int cardColor =
                Color.rgb(7, 27, 48);

        int boxColor =
                Color.rgb(14, 29, 50);

        int yellow =
                Color.rgb(255, 215, 0);

        int blue =
                Color.rgb(20, 145, 255);

        int white =
                Color.WHITE;

        int green =
                Color.rgb(0, 210, 90);

        int red =
                Color.rgb(245, 30, 50);

        int purple =
                Color.rgb(105, 45, 230);

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

            if (random.nextInt(100) < 25) {

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
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा केल्यावर "
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

            // Correct numbers
            numbers.add(x);
            numbers.add(y);

            // Wrong numbers
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

            return y != 0 &&
                    x % y == 0 &&
                    x / y == target;
        }

        // ========================================================
        // CHECK BUTTON
        // ========================================================

        void checkAnswer() {

            if (!gameStarted) {
                return;
            }

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
        // START GAME
        // ========================================================

        void startGame() {

            gameStarted = true;

            level = 1;

            score = 0;

            roundCorrect = 0;

            roundWrong = 0;

            newPuzzle();

            invalidate();
        }

        // ========================================================
        // RESET GAME
        // ========================================================

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

        // ========================================================
        // RESULT DIALOG
        // ========================================================

        void showResult(boolean finalGame) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(MainActivity.this);

            String title;

            if (finalGame) {

                title =
                        "🎉 JD Number Puzzle पूर्ण!";
            } else {

                title =
                        "🏆 Result";
            }

            String message =
                    "Level : " + level +
                    "\n\n" +
                    "✓ बरोबर : " + roundCorrect +
                    "\n" +
                    "✗ चुकले : " + roundWrong +
                    "\n\n" +
                    "⭐ एकूण गुण : " + score;

            builder.setTitle(title);

            builder.setMessage(message);

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

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
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
        // ROUNDED RECT
        // ========================================================

        void roundedRect(
                Canvas canvas,
                RectF rect,
                int color,
                float radius
        ) {

            p.setStyle(Paint.Style.FILL);

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    radius,
                    radius,
                    p
            );
        }

        // ========================================================
        // BORDER
        // ========================================================

        void border(
                Canvas canvas,
                RectF rect,
                int color,
                float width,
                float radius
        ) {

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(width);

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    radius,
                    radius,
                    p
            );

            p.setStyle(Paint.Style.FILL);
        }

        // ========================================================
        // GAME BUTTON
        // ========================================================

        void drawGameButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color,
                float size
        ) {

            roundedRect(
                    canvas,
                    rect,
                    color,
                    18
            );

            border(
                    canvas,
                    rect,
                    Color.argb(
                            180,
                            255,
                            255,
                            255
                    ),
                    2,
                    18
            );

            centerText(
                    canvas,
                    text,
                    rect,
                    size,
                    Color.WHITE
            );
        }

        // ========================================================
        // ON DRAW
        // ========================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            int width = getWidth();

            int height = getHeight();

            canvas.drawColor(
                    backgroundColor
            );

            // ====================================================
            // RESPONSIVE SCALE
            // ====================================================

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
                                    1.12f
                            )
                    );

            float side =
                    Math.max(
                            20,
                            width * 0.035f
                    );

            // ====================================================
            // START SCREEN
            // ====================================================

            if (!gameStarted) {

                drawStartScreen(
                        canvas,
                        width,
                        height,
                        side
                );

                return;
            }

            // ====================================================
            // HEADER
            // ====================================================

            float headerTop =
                    Math.max(
                            18,
                            height * 0.015f
                    );

            float jdSize =
                    Math.max(
                            48,
                            Math.min(
                                    82,
                                    width * 0.115f
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

            // JD
            drawText(
                    canvas,
                    "JD",
                    jdX,
                    headerTop + jdSize,
                    jdSize,
                    yellow,
                    true
            );

            // Crown left
            drawText(
                    canvas,
                    "♛",
                    jdX - jdSize * 0.52f,
                    headerTop + jdSize * 0.72f,
                    jdSize * 0.40f,
                    yellow,
                    true
            );

            // Crown right
            drawText(
                    canvas,
                    "♛",
                    jdX + jdWidth + jdSize * 0.08f,
                    headerTop + jdSize * 0.72f,
                    jdSize * 0.40f,
                    yellow,
                    true
            );

            // ====================================================
            // SCORE
            // ====================================================

            float scoreSize =
                    Math.max(
                            17,
                            width * 0.035f
                    );

            String scoreText =
                    "★ " + score + " गुण";

            p.setTextSize(scoreSize);

            float scoreWidth =
                    p.measureText(scoreText);

            RectF scoreBox =
                    new RectF(
                            width - scoreWidth - side - 18,
                            headerTop + 5,
                            width - side,
                            headerTop + 52
                    );

            border(
                    canvas,
                    scoreBox,
                    yellow,
                    2,
                    12
            );

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
                    headerTop +
                            jdSize +
                            42;

            float titleSize =
                    Math.max(
                            31,
                            Math.min(
                                    50,
                                    width * 0.072f
                            )
                    );

            p.setTextSize(titleSize);

            String title1 =
                    "NUMBER";

            String title2 =
                    " PUZZLE";

            float titleWidth =
                    p.measureText(title1)
                            + p.measureText(title2);

            float titleX =
                    (width - titleWidth) / 2f;

            drawText(
                    canvas,
                    title1,
                    titleX,
                    titleY,
                    titleSize,
                    white,
                    true
            );

            float nWidth =
                    p.measureText(title1);

            drawText(
                    canvas,
                    title2,
                    titleX + nWidth,
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
                            15,
                            width * 0.032f
                    );

            String levelText =
                    "Level " +
                            level +
                            " / 1000 • छोटे नंबर • No Timer";

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

            // ====================================================
            // QUESTION CARD
            // ====================================================

            float questionTop =
                    titleY +
                            subSize +
                            28;

            float questionHeight =
                    Math.min(
                            height * 0.205f,
                            255
                    );

            questionHeight =
                    Math.max(
                            questionHeight,
                            180
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            width - side,
                            questionTop +
                                    questionHeight
                    );

            roundedRect(
                    canvas,
                    questionCard,
                    cardColor,
                    25
            );

            border(
                    canvas,
                    questionCard,
                    blue,
                    2.5f,
                    25
            );

            // ====================================================
            // QUESTION HEADER
            // ====================================================

            float qTitleSize =
                    Math.max(
                            24,
                            width * 0.052f
                    );

            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    side + 28,
                    questionTop + 55,
                    qTitleSize,
                    yellow,
                    true
            );

            // ====================================================
            // QUESTION
            // ====================================================

            float qSize =
                    Math.max(
                            21,
                            Math.min(
                                    32,
                                    width * 0.050f
                            )
                    );

            p.setTextSize(qSize);

            p.setTypeface(
                    Typeface.DEFAULT
            );

            float maxWidth =
                    questionCard.width()
                            - 55;

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
                        <= maxWidth) {

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
                    questionTop + 100;

            float lineHeight =
                    qSize + 12;

            for (
                    int i = 0;
                    i < lines.size() && i < 2;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        side + 28,
                        qY + i * lineHeight,
                        qSize,
                        white,
                        false
                );
            }

            // ====================================================
            // OPERATION
            // ====================================================

            drawText(
                    canvas,
                    "क्रिया: " +
                            operation +
                            "  " +
                            operationName(),
                    side + 28,
                    questionCard.bottom - 24,
                    Math.max(
                            21,
                            width * 0.047f
                    ),
                    yellow,
                    true
            );

            // ====================================================
            // NUMBER GRID
            // ====================================================

            float gridTop =
                    questionCard.bottom + 20;

            float gap =
                    Math.max(
                            8,
                            width * 0.012f
                    );

            float gridWidth =
                    width -
                            side * 2;

            float boxWidth =
                    (gridWidth -
                            gap * 4) / 5f;

            float boxHeight =
                    Math.max(
                            78,
                            Math.min(
                                    118,
                                    height * 0.095f
                            )
                    );

            for (int i = 0; i < 10; i++) {

                int row =
                        i / 5;

                int col =
                        i % 5;

                float left =
                        side +
                                col *
                                        (boxWidth + gap);

                float top =
                        gridTop +
                                row *
                                        (boxHeight + gap);

                numberBoxes[i] =
                        new RectF(
                                left,
                                top,
                                left + boxWidth,
                                top + boxHeight
                        );

                int color =
                        boxColor;

                if (selected1 == i ||
                        selected2 == i) {

                    color =
                            Color.rgb(
                                    30,
                                    90,
                                    160
                            );
                }

                roundedRect(
                        canvas,
                        numberBoxes[i],
                        color,
                        16
                );

                border(
                        canvas,
                        numberBoxes[i],
                        selected1 == i ||
                                selected2 == i
                                ? yellow
                                : Color.rgb(
                                        45,
                                        105,
                                        180
                                ),
                        selected1 == i ||
                                selected2 == i
                                ? 3
                                : 1.5f,
                        16
                );

                centerText(
                        canvas,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        numberBoxes[i],
                        Math.max(
                                24,
                                Math.min(
                                        34,
                                        boxWidth * 0.25f
                                )
                        ),
                        white
                );
            }

            // ====================================================
            // HINT
            // ====================================================

            float hintTop =
                    gridTop +
                            boxHeight * 2 +
                            gap +
                            18;

            float hintHeight =
                    Math.max(
                            62,
                            Math.min(
                                    86,
                                    height * 0.075f
                            )
                    );

            RectF hint =
                    new RectF(
                            side,
                            hintTop,
                            width - side,
                            hintTop + hintHeight
                    );

            roundedRect(
                    canvas,
                    hint,
                    cardColor,
                    16
            );

            border(
                    canvas,
                    hint,
                    blue,
                    1.8f,
                    16
            );

            String hintText =
                    "💡 सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा";

            float hintSize =
                    Math.max(
                            14,
                            Math.min(
                                    20,
                                    width * 0.033f
                            )
                    );

            centerText(
                    canvas,
                    hintText,
                    hint,
                    hintSize,
                    white
            );

            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonTop =
                    hint.bottom + 16;

            float buttonGap =
                    8;

            float buttonWidth =
                    (
                            width -
                                    side * 2 -
                                    buttonGap * 2
                    ) / 3f;

            float buttonHeight =
                    Math.max(
                            58,
                            Math.min(
                                    82,
                                    height * 0.075f
                            )
                    );

            resetButton =
                    new RectF(
                            side,
                            buttonTop,
                            side + buttonWidth,
                            buttonTop + buttonHeight
                    );

            checkButton =
                    new RectF(
                            side + buttonWidth + buttonGap,
                            buttonTop,
                            side +
                                    buttonWidth * 2 +
                                    buttonGap,
                            buttonTop + buttonHeight
                    );

            resultButton =
                    new RectF(
                            side +
                                    buttonWidth * 2 +
                                    buttonGap * 2,
                            buttonTop,
                            width - side,
                            buttonTop + buttonHeight
                    );

            drawGameButton(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    red,
                    Math.max(
                            16,
                            width * 0.030f
                    )
            );

            drawGameButton(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    green,
                    Math.max(
                            16,
                            width * 0.030f
                    )
            );

            drawGameButton(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    purple,
                    Math.max(
                            16,
                            width * 0.030f
                    )
            );

            // ====================================================
            // STATUS
            // ====================================================

            if (!status.isEmpty()) {

                float statusY =
                        buttonTop +
                                buttonHeight +
                                30;

                centerText(
                        canvas,
                        status,
                        new RectF(
                                side,
                                statusY - 25,
                                width - side,
                                statusY + 25
                        ),
                        Math.max(
                                16,
                                width * 0.035f
                        ),
                        status.startsWith("✓")
                                ? green
                                : status.startsWith("✗")
                                ? red
                                : yellow
                );
            }
        }

        // ========================================================
        // START SCREEN
        // ========================================================

        void drawStartScreen(
                Canvas canvas,
                int width,
                int height,
                float side
        ) {

            float center =
                    width / 2f;

            float jdSize =
                    Math.max(
                            65,
                            Math.min(
                                    100,
                                    width * 0.15f
                            )
                    );

            // Crown
            drawText(
                    canvas,
                    "♛",
                    center - jdSize * 0.70f,
                    height * 0.24f,
                    jdSize * 0.55f,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "JD",
                    center - jdSize * 0.55f,
                    height * 0.28f,
                    jdSize,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "♛",
                    center + jdSize * 0.30f,
                    height * 0.24f,
                    jdSize * 0.55f,
                    yellow,
                    true
            );

            float titleSize =
                    Math.max(
                            32,
                            Math.min(
                                    48,
                                    width * 0.075f
                            )
                    );

            p.setTextSize(titleSize);

            String a =
                    "NUMBER";

            String b =
                    " PUZZLE";

            float total =
                    p.measureText(a)
                            + p.measureText(b);

            drawText(
                    canvas,
                    a,
                    center - total / 2f,
                    height * 0.34f,
                    titleSize,
                    white,
                    true
            );

            drawText(
                    canvas,
                    b,
                    center - total / 2f
                            + p.measureText(a),
                    height * 0.34f,
                    titleSize,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "1000 Levels • No Timer",
                    center -
                            p.measureText(
                                    "1000 Levels • No Timer"
                            ) / 2f,
                    height * 0.38f,
                    18,
                    Color.LTGRAY,
                    false
            );

            // Start card
            RectF card =
                    new RectF(
                            side,
                            height * 0.45f,
                            width - side,
                            height * 0.70f
                    );

            roundedRect(
                    canvas,
                    card,
                    cardColor,
                    28
            );

            border(
                    canvas,
                    card,
                    blue,
                    2.5f,
                    28
            );

            centerText(
                    canvas,
                    "🎮  JD NUMBER PUZZLE",
                    new RectF(
                            card.left,
                            card.top + 25,
                            card.right,
                            card.top + 85
                    ),
                    25,
                    yellow
            );

            centerText(
                    canvas,
                    "1000 वेगवेगळ्या Number Puzzles",
                    new RectF(
                            card.left,
                            card.top + 85,
                            card.right,
                            card.top + 135
                    ),
                    18,
                    white
            );

            centerText(
                    canvas,
                    "वेळेची कोणतीही मर्यादा नाही",
                    new RectF(
                            card.left,
                            card.top + 125,
                            card.right,
                            card.top + 175
                    ),
                    17,
                    Color.LTGRAY
            );

            // Start button
            startButton =
                    new RectF(
                            width * 0.12f,
                            height * 0.76f,
                            width * 0.88f,
                            height * 0.86f
                    );

            drawGameButton(
                    canvas,
                    startButton,
                    "▶  गेम सुरू करा",
                    green,
                    Math.max(
                            21,
                            width * 0.045f
                    )
            );
        }

        // ========================================================
        // OPERATION NAME
        // ========================================================

        String operationName() {

            if (operation.equals("+")) {
                return "अधिक";
            }

            if (operation.equals("−")) {
                return "वजा";
            }

            if (operation.equals("×")) {
                return "गुणाकार";
            }

            return "भागाकार";
        }

        // ========================================================
        // TOUCH
        // ========================================================

        @Override
        public boolean onTouchEvent(
                android.view.MotionEvent event
        ) {

            if (event.getAction()
                    != MotionEvent.ACTION_UP) {

                return true;
            }

            float x =
                    event.getX();

            float y =
                    event.getY();

            // ====================================================
            // START
            // ====================================================

            if (!gameStarted) {

                if (startButton.contains(x, y)) {

                    startGame();
                }

                return true;
            }

            // ====================================================
            // NUMBER BOX
            // ====================================================

            for (int i = 0; i < 10; i++) {

                if (numberBoxes[i] != null &&
                        numberBoxes[i].contains(x, y)) {

                    selectNumber(i);

                    return true;
                }
            }

            // ====================================================
            // RESET
            // ====================================================

            if (resetButton.contains(x, y)) {

                resetGame();

                return true;
            }

            // ====================================================
            // CHECK
            // ====================================================

            if (checkButton.contains(x, y)) {

                checkAnswer();

                return true;
            }

            // ====================================================
            // RESULT
            // ====================================================

            if (resultButton.contains(x, y)) {

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
