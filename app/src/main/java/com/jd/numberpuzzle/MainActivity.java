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

        int backgroundColor = Color.rgb(4, 10, 20);

        int cardColor = Color.rgb(10, 27, 48);

        int boxColor = Color.rgb(18, 29, 47);

        int yellow = Color.rgb(255, 215, 0);

        int blue = Color.rgb(20, 135, 255);

        int green = Color.rgb(0, 210, 90);

        int red = Color.rgb(245, 35, 45);

        int purple = Color.rgb(100, 45, 230);

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

            // योग्य उत्तर आधी
            numbers.add(x);
            numbers.add(y);

            // बाकी नंबर
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
                            - p.measureText(text) / 2;

            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent) / 2;

            canvas.drawText(
                    text,
                    x,
                    y,
                    p
            );
        }

        // ========================================================
        // BUTTON
        // ========================================================

        void drawButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color,
                float textSize
        ) {

            // मुख्य रंग
            p.setStyle(Paint.Style.FILL);

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    22,
                    22,
                    p
            );

            // Glow / border
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(3);

            p.setColor(
                    Color.argb(
                            110,
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

        // ========================================================
        // OPERATION TEXT
        // ========================================================

        String operationName() {

            if (operation.equals("+"))
                return "➕ अधिक";

            if (operation.equals("−"))
                return "➖ वजा";

            if (operation.equals("×"))
                return "✖ गुणाकार";

            return "➗ भागाकार";
        }

        // ========================================================
        // START SCREEN
        // ========================================================

        void drawStartScreen(Canvas canvas) {

            int width = getWidth();
            int height = getHeight();

            canvas.drawColor(
                    Color.rgb(3, 10, 20)
            );

            // ----------------------------------------------------
            // JD LOGO
            // ----------------------------------------------------

            float jdSize =
                    Math.max(
                            85,
                            width * 0.20f
                    );

            p.setTextSize(jdSize);
            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            String jd = "JD";

            float jdWidth =
                    p.measureText(jd);

            drawText(
                    canvas,
                    jd,
                    (width - jdWidth) / 2,
                    height * 0.22f,
                    jdSize,
                    yellow,
                    true
            );

            // Crown
            drawText(
                    canvas,
                    "♛",
                    width * 0.25f,
                    height * 0.18f,
                    jdSize * 0.55f,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "♛",
                    width * 0.67f,
                    height * 0.18f,
                    jdSize * 0.55f,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // TITLE
            // ----------------------------------------------------

            float titleSize =
                    Math.max(
                            35,
                            width * 0.075f
                    );

            p.setTextSize(titleSize);

            String t1 =
                    "NUMBER";

            String t2 =
                    "PUZZLE";

            float total =
                    p.measureText(t1)
                            + 12
                            + p.measureText(t2);

            float startX =
                    (width - total) / 2;

            drawText(
                    canvas,
                    t1,
                    startX,
                    height * 0.31f,
                    titleSize,
                    Color.WHITE,
                    true
            );

            drawText(
                    canvas,
                    t2,
                    startX
                            + p.measureText(t1)
                            + 12,
                    height * 0.31f,
                    titleSize,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // SUBTITLE
            // ----------------------------------------------------

            float subSize =
                    Math.max(
                            18,
                            width * 0.038f
                    );

            String subtitle =
                    "1000 LEVELS • NO TIMER";

            p.setTextSize(subSize);

            float subWidth =
                    p.measureText(subtitle);

            drawText(
                    canvas,
                    subtitle,
                    (width - subWidth) / 2,
                    height * 0.37f,
                    subSize,
                    Color.LTGRAY,
                    false
            );

            // ----------------------------------------------------
            // START CARD
            // ----------------------------------------------------

            float cardWidth =
                    width * 0.88f;

            float cardLeft =
                    (width - cardWidth) / 2;

            float cardTop =
                    height * 0.43f;

            float cardBottom =
                    height * 0.68f;

            RectF card =
                    new RectF(
                            cardLeft,
                            cardTop,
                            cardLeft + cardWidth,
                            cardBottom
                    );

            p.setColor(cardColor);

            canvas.drawRoundRect(
                    card,
                    30,
                    30,
                    p
            );

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(3);

            p.setColor(blue);

            canvas.drawRoundRect(
                    card,
                    30,
                    30,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            // ----------------------------------------------------
            // READY TEXT
            // ----------------------------------------------------

            float readySize =
                    Math.max(
                            24,
                            width * 0.055f
                    );

            p.setTextSize(readySize);

            String ready =
                    "🎮 GAME सुरू करण्यासाठी तयार?";

            float rw =
                    p.measureText(ready);

            drawText(
                    canvas,
                    ready,
                    (width - rw) / 2,
                    cardTop + 75,
                    readySize,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // INFO
            // ----------------------------------------------------

            float infoSize =
                    Math.max(
                            16,
                            width * 0.035f
                    );

            String info =
                    "10 नंबरमधून योग्य 2 नंबर निवडा";

            p.setTextSize(infoSize);

            float iw =
                    p.measureText(info);

            drawText(
                    canvas,
                    info,
                    (width - iw) / 2,
                    cardTop + 125,
                    infoSize,
                    Color.WHITE,
                    false
            );

            // ----------------------------------------------------
            // START BUTTON
            // ----------------------------------------------------

            float buttonWidth =
                    width * 0.68f;

            float buttonHeight =
                    Math.max(
                            75,
                            height * 0.075f
                    );

            float buttonLeft =
                    (width - buttonWidth) / 2;

            float buttonTop =
                    cardTop + 155;

            startButton.set(
                    buttonLeft,
                    buttonTop,
                    buttonLeft + buttonWidth,
                    buttonTop + buttonHeight
            );

            drawButton(
                    canvas,
                    startButton,
                    "▶  GAME सुरू करा",
                    green,
                    Math.max(
                            20,
                            width * 0.045f
                    )
            );

            // ----------------------------------------------------
            // FOOTER
            // ----------------------------------------------------

            String footer =
                    "JD NUMBER PUZZLE";

            p.setTextSize(infoSize);

            float fw =
                    p.measureText(footer);

            drawText(
                    canvas,
                    footer,
                    (width - fw) / 2,
                    height * 0.88f,
                    infoSize,
                    Color.GRAY,
                    false
            );
        }

        // ========================================================
        // GAME SCREEN
        // ========================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            if (!gameStarted) {

                drawStartScreen(canvas);

                return;
            }

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

            float headerTop =
                    Math.max(
                            22,
                            height * 0.025f
                    );

            float jdSize =
                    Math.max(
                            50,
                            width * 0.115f
                    );

            // JD
            drawText(
                    canvas,
                    "JD",
                    width * 0.40f,
                    headerTop + jdSize,
                    jdSize,
                    yellow,
                    true
            );

            // Crown
            drawText(
                    canvas,
                    "♛",
                    width * 0.29f,
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

            // ====================================================
            // SCORE
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
                            width - scoreWidth - side - 20,
                            headerTop + 8,
                            width - side,
                            headerTop + 58
                    );

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(yellow);

            canvas.drawRoundRect(
                    scoreBox,
                    13,
                    13,
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
            // NUMBER PUZZLE TITLE
            // ====================================================

            float titleY =
                    headerTop
                            + jdSize
                            + 10;

            float titleSize =
                    Math.max(
                            34,
                            width * 0.075f
                    );

            drawText(
                    canvas,
                    "NUMBER",
                    side,
                    titleY,
                    titleSize,
                    Color.WHITE,
                    true
            );

            p.setTextSize(titleSize);

            float numberWidth =
                    p.measureText(
                            "NUMBER"
                    );

            drawText(
                    canvas,
                    "PUZZLE",
                    side
                            + numberWidth
                            + 10,
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
                            width * 0.032f
                    );

            String levelText =
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer";

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
                    titleY
                            + subSize
                            + Math.max(
                                    28,
                                    height * 0.025f
                            );

            float questionHeight =
                    Math.max(
                            230,
                            Math.min(
                                    330,
                                    height * 0.235f
                            )
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            width - side,
                            questionTop + questionHeight
                    );

            // Card
            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(cardColor);

            canvas.drawRoundRect(
                    questionCard,
                    28,
                    28,
                    p
            );

            // Blue border
            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(3);

            p.setColor(blue);

            canvas.drawRoundRect(
                    questionCard,
                    28,
                    28,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );

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
                    side + 28,
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
                            22,
                            Math.min(
                                    38,
                                    width * 0.052f
                            )
                    );

            p.setTextSize(
                    questionSize
            );

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
                    questionTop + 120;

            float lineHeight =
                    questionSize + 14;

            for (
                    int i = 0;
                    i < lines.size() && i < 3;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        side + 28,
                        qY + i * lineHeight,
                        questionSize,
                        Color.WHITE,
                        false
                );
            }

            // ====================================================
            // OPERATION
            // ====================================================

            float operationTop =
                    questionCard.bottom - 65;

            String operationText =
                    "क्रिया: "
                            + operationName();

            float opSize =
                    Math.max(
                            18,
                            width * 0.040f
                    );

            drawText(
                    canvas,
                    operationText,
                    side + 28,
                    operationTop,
                    opSize,
                    yellow,
                    true
            );

            // ====================================================
            // NUMBER BOXES
            // ====================================================

            float boxTop =
                    questionCard.bottom
                            + Math.max(
                                    18,
                                    height * 0.018f
                            );

            float gap =
                    Math.max(
                            7,
                            width * 0.015f
                    );

            float boxAreaHeight =
                    Math.min(
                            height * 0.38f,
                            560
                    );

            float boxHeight =
                    (boxAreaHeight - gap)
                            / 2f;

            float boxWidth =
                    (
                            width
                                    - 2 * side
                                    - 4 * gap
                    ) / 5f;

            for (
                    int i = 0;
                    i < 10;
                    i++
            ) {

                int row =
                        i / 5;

                int column =
                        i % 5;

                float x =
                        side
                                + column
                                * (boxWidth + gap);

                float y =
                        boxTop
                                + row
                                * (boxHeight + gap);

                numberBoxes[i] =
                        new RectF(
                                x,
                                y,
                                x + boxWidth,
                                y + boxHeight
                        );

                // Selected
                if (
                        i == selected1 ||
                        i == selected2
                ) {

                    p.setColor(
                            Color.rgb(
                                    20,
                                    120,
                                    240
                            )
                    );

                } else {

                    p.setColor(
                            boxColor
                    );
                }

                canvas.drawRoundRect(
                        numberBoxes[i],
                        18,
                        18,
                        p
                );

                // Border
                p.setStyle(
                        Paint.Style.STROKE
                );

                p.setStrokeWidth(2);

                p.setColor(
                        Color.rgb(
                                65,
                                105,
                                160
                        )
                );

                canvas.drawRoundRect(
                        numberBoxes[i],
                        18,
                        18,
                        p
                );

                p.setStyle(
                        Paint.Style.FILL
                );

                // Number
                String number =
                        String.valueOf(
                                numbers.get(i)
                        );

                float numberSize;

                if (number.length() == 1) {

                    numberSize =
                            Math.max(
                                    30,
                                    boxWidth * 0.30f
                            );

                } else {

                    numberSize =
                            Math.max(
                                    25,
                                    boxWidth * 0.25f
                            );
                }

                centerText(
                        canvas,
                        number,
                        numberBoxes[i],
                        numberSize,
                        Color.WHITE
                );
            }

            // ====================================================
            // HINT BOX
            // ====================================================

            float hintTop =
                    boxTop
                            + boxAreaHeight
                            + Math.max(
                                    20,
                                    height * 0.018f
                            );

            float hintHeight =
                    Math.max(
                            58,
                            height * 0.065f
                    );

            RectF hintBox =
                    new RectF(
                            side,
                            hintTop,
                            width - side,
                            hintTop + hintHeight
                    );

            p.setColor(
                    Color.rgb(
                            8,
                            25,
                            43
                    )
            );

            canvas.drawRoundRect(
                    hintBox,
                    18,
                    18,
                    p
            );

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(blue);

            canvas.drawRoundRect(
                    hintBox,
                    18,
                    18,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );

            drawText(
                    canvas,
                    "💡",
                    side + 18,
                    hintTop + hintHeight * 0.65f,
                    27,
                    yellow,
                    true
            );

            drawText(
                    canvas,
                    "सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा",
                    side + 58,
                    hintTop + hintHeight * 0.65f,
                    Math.max(
                            14,
                            width * 0.032f
                    ),
                    Color.WHITE,
                    true
            );

            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonTop =
                    hintTop
                            + hintHeight
                            + Math.max(
                                    18,
                                    height * 0.018f
                            );

            float buttonHeight =
                    Math.max(
                            72,
                            Math.min(
                                    100,
                                    height * 0.085f
                            )
                    );

            float buttonGap =
                    Math.max(
                            8,
                            width * 0.015f
                    );

            float buttonWidth =
                    (
                            width
                                    - 2 * side
                                    - 2 * buttonGap
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
                    side
                            + 2 * buttonWidth
                            + buttonGap,
                    buttonTop + buttonHeight
            );

            // RESULT
            resultButton.set(
                    side
                            + 2 * (buttonWidth + buttonGap),
                    buttonTop,
                    width - side,
                    buttonTop + buttonHeight
            );

            drawButton(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    red,
                    Math.max(
                            17,
                            width * 0.038f
                    )
            );

            drawButton(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    green,
                    Math.max(
                            17,
                            width * 0.038f
                    )
            );

            drawButton(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    purple,
                    Math.max(
                            17,
                            width * 0.038f
                    )
            );

            // ====================================================
            // STATUS
            // ====================================================

            if (!status.isEmpty()) {

                drawText(
                        canvas,
                        status,
                        side,
                        buttonTop
                                + buttonHeight
                                + 35,
                        Math.max(
                                15,
                                width * 0.033f
                        ),
                        Color.WHITE,
                        true
                );
            }
        }

        // ========================================================
        // RESULT DIALOG
        // ========================================================

        void showResult(
                final boolean finalResult
        ) {

            final Dialog dialog =
                    new Dialog(
                            MainActivity.this
                    );

            LinearLayout layout =
                    new LinearLayout(
                            MainActivity.this
                    );

            layout.setOrientation(
                    LinearLayout.VERTICAL
            );

            layout.setPadding(
                    45,
                    40,
                    45,
                    40
            );

            GradientDrawable bg =
                    new GradientDrawable();

            bg.setColor(
                    Color.rgb(
                            10,
                            25,
                            45
                    )
            );

            bg.setCornerRadius(
                    35
            );

            layout.setBackground(
                    bg
            );

            // Title
            TextView title =
                    new TextView(
                            MainActivity.this
                    );

            title.setText(
                    finalResult
                            ? "🏆 FINAL RESULT"
                            : "🎯 RESULT"
            );

            title.setTextColor(
                    Color.WHITE
            );

            title.setTextSize(
                    26
            );

            title.setGravity(
                    Gravity.CENTER
            );

            // Info
            TextView info =
                    new TextView(
                            MainActivity.this
                    );

            String range;

            if (finalResult) {

                range =
                        "Level 991 ते 1000";

            } else {

                range =
                        "Level "
                                + (level - 9)
                                + " ते "
                                + level;
            }

            info.setText(
                    range
                            + "\n\nया 10 Levels मधील गुण: "
                            + roundCorrect
                            + "/10"
                            + "\nचुकीचे प्रयत्न: "
                            + roundWrong
                            + "\n\nएकूण गुण: "
                            + score
            );

            info.setTextColor(
                    Color.WHITE
            );

            info.setTextSize(
                    19
            );

            info.setGravity(
                    Gravity.CENTER
            );

            // Next button
            Button next =
                    new Button(
                            MainActivity.this
                    );

            next.setText(
                    finalResult
                            ? "GAME पुन्हा सुरू करा"
                            : "NEXT LEVEL ▶"
            );

            layout.addView(title);

            layout.addView(info);

            layout.addView(next);

            dialog.setContentView(
                    layout
            );

            dialog.show();

            next.setOnClickListener(
                    v -> {

                        dialog.dismiss();

                        if (finalResult) {

                            level = 1;

                            score = 0;

                            roundCorrect = 0;

                            roundWrong = 0;

                        } else {

                            roundCorrect = 0;

                            roundWrong = 0;

                            if (level < 1000) {

                                level++;
                            }
                        }

                        newPuzzle();
                    }
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
            // START GAME
            // ====================================================

            if (!gameStarted) {

                if (
                        startButton.contains(
                                x,
                                y
                        )
                ) {

                    gameStarted = true;

                    level = 1;

                    score = 0;

                    roundCorrect = 0;

                    roundWrong = 0;

                    newPuzzle();

                    invalidate();

                    return true;
                }

                return true;
            }

            // ====================================================
            // NUMBER BOXES
            // ====================================================

            for (
                    int i = 0;
                    i < 10;
                    i++
            ) {

                if (
                        numberBoxes[i] != null
                                &&
                        numberBoxes[i].contains(
                                x,
                                y
                        )
                ) {

                    if (selected1 < 0) {

                        selected1 = i;

                    } else if (
                            selected2 < 0
                                    &&
                            i != selected1
                    ) {

                        selected2 = i;

                    } else {

                        selected1 = i;

                        selected2 = -1;
                    }

                    status = "";

                    invalidate();

                    return true;
                }
            }

            // ====================================================
            // RESET
            // ====================================================

            if (
                    resetButton.contains(
                            x,
                            y
                    )
            ) {

                selected1 = -1;

                selected2 = -1;

                status =
                        "RESET केले.";

                invalidate();

                return true;
            }

            // ====================================================
            // CHECK
            // ====================================================

            if (
                    checkButton.contains(
                            x,
                            y
                    )
            ) {

                checkAnswer();

                return true;
            }

            // ====================================================
            // RESULT
            // ====================================================

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
    }
}
