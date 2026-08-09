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
    // FULL SCREEN
    // ============================================================

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

        int backgroundColor = Color.rgb(3, 10, 20);

        int cardColor = Color.rgb(8, 27, 48);

        int numberBoxColor = Color.rgb(15, 29, 49);

        int selectedBoxColor = Color.rgb(20, 100, 190);

        int yellow = Color.rgb(255, 215, 0);

        int blue = Color.rgb(20, 140, 255);

        int white = Color.WHITE;

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

            } else {

                return 10 + random.nextInt(90);
            }
        }

        // ========================================================
        // CHECK DUPLICATE
        // ========================================================

        boolean containsNumber(int number) {

            for (int n : numbers) {

                if (n == number) {
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

                int number = smallNumber();

                if (!containsNumber(number)) {

                    numbers.add(number);
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
        // CHECK ANSWER BUTTON
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
        // BUTTON
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
                    20,
                    20,
                    p
            );

            // Border
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);

            p.setColor(
                    Color.argb(
                            130,
                            255,
                            255,
                            255
                    )
            );

            canvas.drawRoundRect(
                    rect,
                    20,
                    20,
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
                RectF rect
        ) {

            // Card
            p.setStyle(Paint.Style.FILL);

            p.setColor(cardColor);

            canvas.drawRoundRect(
                    rect,
                    25,
                    25,
                    p
            );

            // Blue border
            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(
                    Math.max(
                            2,
                            getWidth() * 0.004f
                    )
            );

            p.setColor(blue);

            canvas.drawRoundRect(
                    rect,
                    25,
                    25,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            // Question title
            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    rect.left + 22,
                    rect.top + 52,
                    Math.max(
                            22,
                            getWidth() * 0.050f
                    ),
                    yellow,
                    true
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

            float side =
                    Math.max(
                            20,
                            width * 0.035f
                    );

            float gap =
                    Math.max(
                            8,
                            width * 0.014f
                    );

            // ====================================================
            // HEADER
            // ====================================================

            float headerTop =
                    Math.max(
                            22,
                            height * 0.020f
                    );

            // ----------------------------------------------------
            // JD LOGO CENTER
            // ----------------------------------------------------

            float jdSize =
                    Math.max(
                            58,
                            width * 0.115f
                    );

            p.setTextSize(jdSize);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            String jdText = "JD";

            float jdWidth =
                    p.measureText(jdText);

            float jdX =
                    (width - jdWidth) / 2f;

            drawText(
                    canvas,
                    jdText,
                    jdX,
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
                    jdX - jdSize * 0.55f,
                    headerTop + jdSize * 0.60f,
                    jdSize * 0.45f,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // RIGHT CROWN
            // ----------------------------------------------------

            drawText(
                    canvas,
                    "♛",
                    jdX + jdWidth + jdSize * 0.10f,
                    headerTop + jdSize * 0.60f,
                    jdSize * 0.45f,
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
            p.setTypeface(Typeface.DEFAULT_BOLD);

            float scoreWidth =
                    p.measureText(scoreText);

            RectF scoreBox =
                    new RectF(
                            width - scoreWidth - side - 14,
                            headerTop + 4,
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
            // NUMBER PUZZLE TITLE
            // ====================================================

            float titleY =
                    headerTop
                            + jdSize
                            + 18;

            float titleSize =
                    Math.max(
                            31,
                            width * 0.072f
                    );

            // NUMBER
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
            p.setTypeface(Typeface.DEFAULT_BOLD);

            float numberWidth =
                    p.measureText("NUMBER");

            // PUZZLE
            drawText(
                    canvas,
                    "PUZZLE",
                    side + numberWidth + 8,
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
                                    height * 0.022f
                            );

            float questionHeight =
                    Math.max(
                            210,
                            Math.min(
                                    270,
                                    height * 0.25f
                            )
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            width - side,
                            questionTop + questionHeight
                    );

            drawQuestionCard(
                    canvas,
                    questionCard
            );

            // ====================================================
            // QUESTION TEXT
            // ====================================================

            float questionSize =
                    Math.max(
                            20,
                            Math.min(
                                    31,
                                    width * 0.052f
                            )
                    );

            p.setTextSize(questionSize);
            p.setTypeface(Typeface.DEFAULT);

            float maxQuestionWidth =
                    questionCard.width() - 45;

            ArrayList<String> lines =
                    new ArrayList<>();

            String currentLine = "";

            String[] words =
                    question.split(" ");

            for (String word : words) {

                String testLine =
                        currentLine.isEmpty()
                                ? word
                                : currentLine
                                + " "
                                + word;

                if (p.measureText(testLine)
                        <= maxQuestionWidth) {

                    currentLine = testLine;

                } else {

                    if (!currentLine.isEmpty()) {

                        lines.add(currentLine);
                    }

                    currentLine = word;
                }
            }

            if (!currentLine.isEmpty()) {

                lines.add(currentLine);
            }

            float questionY =
                    questionTop + 105;

            float lineHeight =
                    questionSize + 12;

            for (
                    int i = 0;
                    i < lines.size() && i < 3;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        questionCard.left + 22,
                        questionY
                                + i * lineHeight,
                        questionSize,
                        Color.WHITE,
                        false
                );
            }

            // ====================================================
            // OPERATION
            // ====================================================

            String operationText;

            if (operation.equals("+")) {

                operationText =
                        "क्रिया:  ➕  अधिक";

            } else if (operation.equals("−")) {

                operationText =
                        "क्रिया:  ➖  वजा";

            } else if (operation.equals("×")) {

                operationText =
                        "क्रिया:  ✖  गुणाकार";

            } else {

                operationText =
                        "क्रिया:  ➗  भागाकार";
            }

            drawText(
                    canvas,
                    operationText,
                    questionCard.left + 22,
                    questionCard.bottom - 35,
                    Math.max(
                            18,
                            width * 0.040f
                    ),
                    yellow,
                    true
            );

            // ====================================================
            // NUMBER BOXES
            // ====================================================

            float boxTop =
                    questionCard.bottom
                            + Math.max(
                                    22,
                                    height * 0.018f
                            );

            float availableBoxHeight =
                    Math.min(
                            height * 0.30f,
                            500
                    );

            float boxHeight =
                    (availableBoxHeight - gap)
                            / 2f;

            float boxWidth =
                    (
                            width
                                    - 2 * side
                                    - 4 * gap
                    ) / 5f;

            for (int i = 0; i < 10; i++) {

                int row =
                        i / 5;

                int column =
                        i % 5;

                float left =
                        side
                                + column
                                * (boxWidth + gap);

                float top =
                        boxTop
                                + row
                                * (boxHeight + gap);

                numberBoxes[i] =
                        new RectF(
                                left,
                                top,
                                left + boxWidth,
                                top + boxHeight
                        );

                // Selected
                if (
                        i == selected1 ||
                        i == selected2
                ) {

                    p.setColor(
                            selectedBoxColor
                    );

                } else {

                    p.setColor(
                            numberBoxColor
                    );
                }

                p.setStyle(Paint.Style.FILL);

                canvas.drawRoundRect(
                        numberBoxes[i],
                        15,
                        15,
                        p
                );

                // Blue border
                p.setStyle(Paint.Style.STROKE);

                p.setStrokeWidth(1.5f);

                p.setColor(
                        Color.rgb(
                                65,
                                120,
                                190
                        )
                );

                canvas.drawRoundRect(
                        numberBoxes[i],
                        15,
                        15,
                        p
                );

                p.setStyle(Paint.Style.FILL);

                // Number
                String numberText =
                        String.valueOf(
                                numbers.get(i)
                        );

                float numberSize;

                if (numberText.length() == 1) {

                    numberSize =
                            Math.max(
                                    28,
                                    boxWidth * 0.30f
                            );

                } else {

                    numberSize =
                            Math.max(
                                    23,
                                    boxWidth * 0.24f
                            );
                }

                centerText(
                        canvas,
                        numberText,
                        numberBoxes[i],
                        numberSize,
                        Color.WHITE
                );
            }

            // ====================================================
            // HINT / INFORMATION CARD
            // ====================================================

            float infoTop =
                    boxTop
                            + availableBoxHeight
                            + Math.max(
                                    20,
                                    height * 0.018f
                            );

            float infoHeight =
                    Math.max(
                            70,
                            Math.min(
                                    100,
                                    height * 0.075f
                            )
                    );

            RectF infoCard =
                    new RectF(
                            side,
                            infoTop,
                            width - side,
                            infoTop + infoHeight
                    );

            p.setStyle(Paint.Style.FILL);

            p.setColor(cardColor);

            canvas.drawRoundRect(
                    infoCard,
                    18,
                    18,
                    p
            );

            p.setStyle(Paint.Style.STROKE);

            p.setStrokeWidth(1.5f);

            p.setColor(blue);

            canvas.drawRoundRect(
                    infoCard,
                    18,
                    18,
                    p
            );

            p.setStyle(Paint.Style.FILL);

            drawText(
                    canvas,
                    "💡 सूचना : वरील 10 नंबरमधून योग्य दोन नंबर निवडा",
                    infoCard.left + 18,
                    infoCard.centerY()
                            + 7,
                    Math.max(
                            15,
                            width * 0.032f
                    ),
                    Color.WHITE,
                    true
            );

            // ====================================================
            // BUTTONS
            // ====================================================

            float buttonTop =
                    infoCard.bottom
                            + Math.max(
                                    20,
                                    height * 0.020f
                            );

            float buttonHeight =
                    Math.max(
                            65,
                            Math.min(
                                    92,
                                    height * 0.075f
                            )
                    );

            float buttonWidth =
                    (
                            width
                                    - 2 * side
                                    - 2 * gap
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
                    side + buttonWidth + gap,
                    buttonTop,
                    side
                            + 2 * buttonWidth
                            + gap,
                    buttonTop + buttonHeight
            );

            // RESULT
            resultButton.set(
                    side
                            + 2 * (buttonWidth + gap),
                    buttonTop,
                    width - side,
                    buttonTop + buttonHeight
            );

            drawButton(
                    canvas,
                    resetButton,
                    "↻  RESET",
                    Color.rgb(
                            240,
                            35,
                            45
                    ),
                    Math.max(
                            16,
                            width * 0.040f
                    )
            );

            drawButton(
                    canvas,
                    checkButton,
                    "✓  CHECK",
                    Color.rgb(
                            0,
                            205,
                            90
                    ),
                    Math.max(
                            16,
                            width * 0.040f
                    )
            );

            drawButton(
                    canvas,
                    resultButton,
                    "▮  RESULT",
                    Color.rgb(
                            105,
                            45,
                            225
                    ),
                    Math.max(
                            16,
                            width * 0.040f
                    )
            );

            // ====================================================
            // STATUS
            // ====================================================

            if (!status.isEmpty()) {

                float statusY =
                        buttonTop
                                + buttonHeight
                                + 30;

                drawText(
                        canvas,
                        status,
                        side,
                        statusY,
                        Math.max(
                                16,
                                width * 0.033f
                        ),
                        Color.WHITE,
                        true
                );
            }
        }

        // ========================================================
        // TOUCH
        // ========================================================

        @Override
        public boolean onTouchEvent(MotionEvent event) {

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
                        numberBoxes[i].contains(x, y)
                ) {

                    if (selected1 < 0) {

                        selected1 = i;

                    } else if (
                            selected2 < 0 &&
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

            // ----------------------------------------------------
            // RESET
            // ----------------------------------------------------

            if (
                    resetButton.contains(x, y)
            ) {

                selected1 = -1;

                selected2 = -1;

                status =
                        "RESET केले.";

                invalidate();

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

                showResult(false);

                return true;
            }

            return true;
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
                    40,
                    35,
                    40,
                    35
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setColor(
                    Color.rgb(
                            12,
                            25,
                            42
                    )
            );

            background.setCornerRadius(
                    30
            );

            layout.setBackground(
                    background
            );

            // ----------------------------------------------------
            // TITLE
            // ----------------------------------------------------

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
                    25
            );

            title.setGravity(
                    Gravity.CENTER
            );

            // ----------------------------------------------------
            // INFO
            // ----------------------------------------------------

            TextView info =
                    new TextView(
                            MainActivity.this
                    );

            String levelRange;

            if (finalResult) {

                levelRange =
                        "Level 991 ते 1000";

            } else {

                levelRange =
                        "Level "
                                + (level - 9)
                                + " ते "
                                + level;
            }

            info.setText(
                    levelRange
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
                    18
            );

            info.setGravity(
                    Gravity.CENTER
            );

            // ----------------------------------------------------
            // NEXT BUTTON
            // ----------------------------------------------------

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

            // ----------------------------------------------------
            // NEXT ACTION
            // ----------------------------------------------------

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
    }
}
