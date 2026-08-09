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

        int firstSelected = -1;
        int secondSelected = -1;

        int target = 0;

        String operation = "";
        String question = "";
        String status = "";

        float density;

        GameView(Context context) {
            super(context);

            density = getResources().getDisplayMetrics().density;

            setFocusable(true);

            newPuzzle();
        }

        float dp(float value) {
            return value * density;
        }

        // ---------------------------------------
        // नंबर तयार करणे
        // ---------------------------------------

        int smallNumber() {

            if (random.nextInt(100) < 40) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        // ---------------------------------------
        // 10 वेगवेगळे नंबर
        // ---------------------------------------

        void fillNumbers() {

            while (numbers.size() < 10) {

                int n = smallNumber();

                boolean duplicate = false;

                for (int x : numbers) {

                    if (x == n) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    numbers.add(n);
                }
            }

            Collections.shuffle(numbers, random);
        }

        // ---------------------------------------
        // नवीन प्रश्न
        // ---------------------------------------

        void newPuzzle() {

            numbers.clear();

            firstSelected = -1;
            secondSelected = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;

            // -------------------------------
            // ADDITION
            // -------------------------------

            if (type == 0) {

                operation = "+";

                do {

                    x = smallNumber();
                    y = smallNumber();

                    target = x + y;

                } while (target < 10 || target > 180);

                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target
                                + " मिळेल?";

            }

            // -------------------------------
            // SUBTRACTION
            // -------------------------------

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

            // -------------------------------
            // MULTIPLICATION
            // -------------------------------

            else if (type == 2) {

                operation = "×";

                /*
                 * दोन अंकी नंबर × दोन अंकी नंबर
                 * त्यामुळे उत्तर 4 अंकी होऊ शकते.
                 */

                do {

                    x = 10 + random.nextInt(90);
                    y = 10 + random.nextInt(90);

                    target = x * y;

                } while (target < 1000 || target > 9801);

                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target
                                + " मिळेल?";

            }

            // -------------------------------
            // DIVISION
            // -------------------------------

            else {

                operation = "÷";

                do {

                    y = 2 + random.nextInt(8);

                    target = 1 + random.nextInt(12);

                    x = y * target;

                } while (x > 99);

                question =
                        "कोणता नंबर कोणत्या नंबरने "
                                + "भागल्यावर "
                                + target
                                + " मिळेल?";
            }

            numbers.add(x);
            numbers.add(y);

            fillNumbers();

            invalidate();
        }

        // ---------------------------------------
        // उत्तर तपासणे
        // ---------------------------------------

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

            if (operation.equals("÷")) {

                return y != 0 &&
                        x % y == 0 &&
                        x / y == target;
            }

            return false;
        }

        // ---------------------------------------
        // CHECK
        // ---------------------------------------

        void checkAnswer() {

            if (firstSelected < 0 ||
                    secondSelected < 0) {

                status =
                        "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first =
                    numbers.get(firstSelected);

            int second =
                    numbers.get(secondSelected);

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

                }, 650);

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                invalidate();
            }
        }

        // ---------------------------------------
        // RESULT
        // ---------------------------------------

        void showResult(final boolean finalResult) {

            final Dialog dialog =
                    new Dialog(MainActivity.this);

            LinearLayout layout =
                    new LinearLayout(MainActivity.this);

            layout.setOrientation(
                    LinearLayout.VERTICAL
            );

            layout.setPadding(
                    (int) dp(28),
                    (int) dp(28),
                    (int) dp(28),
                    (int) dp(28)
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setColor(
                    Color.rgb(25, 31, 43)
            );

            background.setCornerRadius(
                    dp(22)
            );

            layout.setBackground(background);

            TextView title =
                    new TextView(MainActivity.this);

            title.setText(
                    finalResult
                            ? "🏆 FINAL RESULT"
                            : "🎯 RESULT"
            );

            title.setTextColor(Color.WHITE);

            title.setTextSize(26);

            title.setGravity(
                    Gravity.CENTER
            );

            TextView info =
                    new TextView(MainActivity.this);

            info.setText(
                    (finalResult
                            ? "Level 991 ते 1000"
                            : "Level "
                            + (level - 9)
                            + " ते "
                            + level)

                            + "\n\nया 10 Levels मधील गुण: "
                            + roundCorrect
                            + "/10"

                            + "\nचुकीचे प्रयत्न: "
                            + roundWrong

                            + "\n\nएकूण गुण: "
                            + score
            );

            info.setTextColor(Color.WHITE);

            info.setTextSize(19);

            info.setGravity(
                    Gravity.CENTER
            );

            Button next =
                    new Button(MainActivity.this);

            next.setText(
                    finalResult
                            ? "GAME पुन्हा सुरू करा"
                            : "NEXT LEVEL ▶"
            );

            layout.addView(
                    title,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    )
            );

            layout.addView(
                    info,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    )
            );

            layout.addView(
                    next,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    )
            );

            dialog.setContentView(layout);

            dialog.show();

            next.setOnClickListener(v -> {

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
            });
        }

        // ---------------------------------------
        // TEXT
        // ---------------------------------------

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

        // ---------------------------------------
        // BUTTON
        // ---------------------------------------

        void drawButton(
                Canvas canvas,
                RectF rect,
                String text,
                int color,
                float size
        ) {

            p.setColor(color);

            canvas.drawRoundRect(
                    rect,
                    dp(12),
                    dp(12),
                    p
            );

            p.setColor(Color.WHITE);

            p.setTextSize(size);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent) / 2;

            canvas.drawText(
                    text,
                    rect.centerX()
                            - p.measureText(text) / 2,
                    y,
                    p
            );
        }

        // ---------------------------------------
        // ON DRAW
        // ---------------------------------------

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            canvas.drawColor(
                    Color.rgb(15, 19, 28)
            );

            float w = getWidth();
            float h = getHeight();

            /*
             * RESPONSIVE DESIGN
             *
             * मोबाइलच्या रुंदीप्रमाणे
             * सगळे आकार आपोआप बदलतील.
             */

            float side =
                    Math.max(
                            dp(18),
                            w * 0.045f
                    );

            float gap =
                    Math.max(
                            dp(7),
                            w * 0.015f
                    );

            // -----------------------------------
            // TOP SPACE
            // -----------------------------------

            float top =
                    Math.max(
                            dp(30),
                            h * 0.025f
                    );

            // -----------------------------------
            // HEADER
            // -----------------------------------

            float titleSize =
                    Math.max(
                            dp(25),
                            Math.min(
                                    dp(36),
                                    w * 0.062f
                            )
                    );

            drawText(
                    canvas,
                    "JD NUMBER PUZZLE",
                    side,
                    top + titleSize,
                    titleSize,
                    Color.WHITE,
                    true
            );

            float scoreSize =
                    Math.max(
                            dp(16),
                            w * 0.035f
                    );

            p.setTextSize(scoreSize);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            String scoreText =
                    "★ " + score + " गुण";

            p.setColor(
                    Color.rgb(255, 214, 73)
            );

            canvas.drawText(
                    scoreText,
                    w
                            - side
                            - p.measureText(scoreText),
                    top + titleSize,
                    p
            );

            // -----------------------------------
            // LEVEL
            // -----------------------------------

            float subSize =
                    Math.max(
                            dp(14),
                            w * 0.030f
                    );

            drawText(
                    canvas,
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer",
                    side,
                    top
                            + titleSize
                            + subSize
                            + dp(2),
                    subSize,
                    Color.LTGRAY,
                    false
            );

            // -----------------------------------
            // QUESTION BOX
            // -----------------------------------

            float questionTop =
                    top
                            + titleSize
                            + subSize
                            + dp(25);

            float questionHeight =
                    Math.max(
                            dp(125),
                            Math.min(
                                    dp(170),
                                    h * 0.145f
                            )
                    );

            p.setColor(
                    Color.rgb(28, 34, 48)
            );

            canvas.drawRoundRect(
                    side,
                    questionTop,
                    w - side,
                    questionTop
                            + questionHeight,
                    dp(17),
                    dp(17),
                    p
            );

            // प्रश्न शीर्षक

            float questionTitleSize =
                    Math.max(
                            dp(19),
                            w * 0.040f
                    );

            drawText(
                    canvas,
                    "🧮  प्रश्न",
                    side + dp(16),
                    questionTop + dp(32),
                    questionTitleSize,
                    Color.rgb(255, 214, 73),
                    true
            );

            // -----------------------------------
            // मोठा प्रश्न
            // -----------------------------------

            float questionSize =
                    Math.max(
                            dp(18),
                            Math.min(
                                    dp(24),
                                    w * 0.040f
                            )
                    );

            p.setTextSize(questionSize);

            p.setTypeface(
                    Typeface.DEFAULT
            );

            float availableWidth =
                    w
                            - side * 2
                            - dp(32);

            ArrayList<String> lines =
                    new ArrayList<>();

            String currentLine = "";

            String[] words =
                    question.split(" ");

            for (String word : words) {

                String test =
                        currentLine.isEmpty()
                                ? word
                                : currentLine
                                + " "
                                + word;

                if (p.measureText(test)
                        <= availableWidth) {

                    currentLine = test;

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
                    questionTop + dp(70);

            float lineHeight =
                    questionSize + dp(8);

            for (int i = 0;
                 i < lines.size() && i < 2;
                 i++) {

                drawText(
                        canvas,
                        lines.get(i),
                        side + dp(16),
                        questionY
                                + i * lineHeight,
                        questionSize,
                        Color.WHITE,
                        false
                );
            }

            // -----------------------------------
            // NUMBER BOX AREA
            // -----------------------------------

            float boxesTop =
                    questionTop
                            + questionHeight
                            + dp(18);

            float boxAreaHeight =
                    Math.min(
                            h * 0.34f,
                            dp(500)
                    );

            float boxHeight =
                    (boxAreaHeight - gap)
                            / 2f;

            float boxWidth =
                    (
                            w
                                    - side * 2
                                    - gap * 4
                    ) / 5f;

            // -----------------------------------
            // 10 BOXES
            // -----------------------------------

            for (int i = 0; i < 10; i++) {

                int row = i / 5;
                int column = i % 5;

                float left =
                        side
                                + column
                                * (boxWidth + gap);

                float topBox =
                        boxesTop
                                + row
                                * (boxHeight + gap);

                numberBoxes[i] =
                        new RectF(
                                left,
                                topBox,
                                left + boxWidth,
                                topBox + boxHeight
                        );

                // Selected box

                if (i == firstSelected ||
                        i == secondSelected) {

                    p.setColor(
                            Color.rgb(55, 115, 200)
                    );

                } else {

                    p.setColor(
                            Color.rgb(43, 51, 66)
                    );
                }

                canvas.drawRoundRect(
                        numberBoxes[i],
                        dp(14),
                        dp(14),
                        p
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
                                    dp(30),
                                    boxWidth * 0.30f
                            );

                } else {

                    numberSize =
                            Math.max(
                                    dp(25),
                                    boxWidth * 0.25f
                            );
                }

                p.setColor(Color.WHITE);

                p.setTextSize(
                        numberSize
                );

                p.setTypeface(
                        Typeface.DEFAULT_BOLD
                );

                Paint.FontMetrics fm =
                        p.getFontMetrics();

                float numberY =
                        numberBoxes[i].centerY()
                                - (fm.ascent
                                + fm.descent) / 2;

                canvas.drawText(
                        number,
                        numberBoxes[i].centerX()
                                - p.measureText(number) / 2,
                        numberY,
                        p
                );
            }

            // -----------------------------------
            // OPERATION
            // -----------------------------------

            float operationY =
                    boxesTop
                            + boxAreaHeight
                            + dp(25);

            String operationText;

            if (operation.equals("+")) {

                operationText =
                        "➕  अधिक";

            } else if (operation.equals("−")) {

                operationText =
                        "➖  वजा";

            } else if (operation.equals("×")) {

                operationText =
                        "✖  गुणाकार";

            } else {

                operationText =
                        "➗  भागाकार";
            }

            float operationSize =
                    Math.max(
                            dp(18),
                            w * 0.040f
                    );

            drawText(
                    canvas,
                    "क्रिया: " + operationText,
                    side,
                    operationY,
                    operationSize,
                    Color.rgb(255, 214, 73),
                    true
            );

            // -----------------------------------
            // BUTTONS
            // -----------------------------------

            float buttonTop =
                    operationY
                            + dp(20);

            float buttonHeight =
                    Math.max(
                            dp(70),
                            Math.min(
                                    dp(90),
                                    h * 0.085f
                            )
                    );

            float buttonWidth =
                    (
                            w
                                    - side * 2
                                    - gap * 2
                    ) / 3f;

            resetButton.set(
                    side,
                    buttonTop,
                    side + buttonWidth,
                    buttonTop + buttonHeight
            );

            checkButton.set(
                    side
                            + buttonWidth
                            + gap,
                    buttonTop,
                    side
                            + buttonWidth * 2
                            + gap,
                    buttonTop + buttonHeight
            );

            resultButton.set(
                    side
                            + buttonWidth * 2
                            + gap * 2,
                    buttonTop,
                    w - side,
                    buttonTop + buttonHeight
            );

            float buttonTextSize =
                    Math.max(
                            dp(16),
                            Math.min(
                                    dp(21),
                                    w * 0.040f
                            )
                    );

            drawButton(
                    canvas,
                    resetButton,
                    "RESET",
                    Color.rgb(225, 60, 65),
                    buttonTextSize
            );

            drawButton(
                    canvas,
                    checkButton,
                    "CHECK ✓",
                    Color.rgb(72, 205, 125),
                    buttonTextSize
            );

            drawButton(
                    canvas,
                    resultButton,
                    "RESULT",
                    Color.rgb(105, 83, 210),
                    buttonTextSize
            );

            // -----------------------------------
            // STATUS
            // -----------------------------------

            if (!status.isEmpty()) {

                drawText(
                        canvas,
                        status,
                        side,
                        buttonTop
                                + buttonHeight
                                + dp(30),
                        Math.max(
                                dp(15),
                                w * 0.032f
                        ),
                        Color.WHITE,
                        false
                );
            }
        }

        // ---------------------------------------
        // TOUCH
        // ---------------------------------------

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getAction()
                    != MotionEvent.ACTION_UP) {

                return true;
            }

            float x = event.getX();
            float y = event.getY();

            // Number boxes

            for (int i = 0; i < 10; i++) {

                if (numberBoxes[i] != null &&
                        numberBoxes[i].contains(x, y)) {

                    if (firstSelected < 0) {

                        firstSelected = i;

                    } else if (
                            secondSelected < 0 &&
                            i != firstSelected
                    ) {

                        secondSelected = i;

                    } else {

                        firstSelected = i;
                        secondSelected = -1;
                    }

                    status = "";

                    invalidate();

                    return true;
                }
            }

            // RESET

            if (resetButton.contains(x, y)) {

                firstSelected = -1;
                secondSelected = -1;

                status =
                        "RESET केले.";

                invalidate();

                return true;
            }

            // CHECK

            if (checkButton.contains(x, y)) {

                checkAnswer();

                return true;
            }

            // RESULT

            if (resultButton.contains(x, y)) {

                showResult(false);

                return true;
            }

            return true;
        }
    }
}
