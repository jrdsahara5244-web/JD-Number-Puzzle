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


    // =========================================================
    // GAME VIEW
    // =========================================================

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random = new Random();

        ArrayList<Integer> nums = new ArrayList<>();

        RectF[] boxes = new RectF[10];

        RectF reset = new RectF();
        RectF check = new RectF();
        RectF result = new RectF();

        int level = 1;
        int score = 0;

        int roundCorrect = 0;
        int roundWrong = 0;

        int firstSelected = -1;
        int secondSelected = -1;

        int target;

        String operation = "";
        String question = "";
        String status = "";


        GameView(Context context) {
            super(context);

            setFocusable(true);

            newPuzzle();
        }


        // =====================================================
        // RANDOM SMALL NUMBER
        // =====================================================

        int getSmallNumber() {

            // 35% chance = 1 digit
            // 65% chance = 2 digit

            if (random.nextInt(100) < 35) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }


        // =====================================================
        // FILL NUMBER BOXES
        // =====================================================

        void fillNumbers() {

            while (nums.size() < 10) {

                int value = getSmallNumber();

                boolean duplicate = false;

                for (int n : nums) {

                    if (n == value) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    nums.add(value);
                }
            }

            Collections.shuffle(nums, random);
        }


        // =====================================================
        // NEW PUZZLE
        // =====================================================

        void newPuzzle() {

            nums.clear();

            firstSelected = -1;
            secondSelected = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;


            // =================================================
            // ADDITION
            // =================================================

            if (type == 0) {

                operation = "+";

                do {

                    x = getSmallNumber();
                    y = getSmallNumber();

                    target = x + y;

                } while (target < 10 || target > 150);


                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target
                                + " मिळेल?";


            }


            // =================================================
            // SUBTRACTION
            // =================================================

            else if (type == 1) {

                operation = "−";

                do {

                    x = getSmallNumber();
                    y = getSmallNumber();

                } while (x <= y);


                target = x - y;


                question =
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर "
                                + "वजा केल्यावर "
                                + target
                                + " मिळेल?";


            }


            // =================================================
            // MULTIPLICATION
            // =================================================

            else if (type == 2) {

                operation = "×";

                do {

                    x = getSmallNumber();
                    y = getSmallNumber();

                    target = x * y;

                } while (
                        target < 20 ||
                        target > 9999
                );


                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target
                                + " मिळेल?";


            }


            // =================================================
            // DIVISION
            // =================================================

            else {

                operation = "÷";

                do {

                    y = 2 + random.nextInt(8);

                    target = 1 + random.nextInt(11);

                    x = y * target;

                } while (x > 99);


                question =
                        "कोणता नंबर कोणत्या नंबरने भागल्यावर "
                                + target
                                + " मिळेल?";

            }


            nums.add(x);
            nums.add(y);

            fillNumbers();

            invalidate();
        }


        // =====================================================
        // CHECK ANSWER
        // =====================================================

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


        // =====================================================
        // CHECK BUTTON
        // =====================================================

        void checkAnswer() {

            if (firstSelected < 0 ||
                    secondSelected < 0) {

                status =
                        "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }


            int x = nums.get(firstSelected);
            int y = nums.get(secondSelected);


            if (isCorrect(x, y)) {

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


        // =====================================================
        // RESULT DIALOG
        // =====================================================

        void showResult(final boolean finalResult) {

            final Dialog dialog =
                    new Dialog(MainActivity.this);


            LinearLayout box =
                    new LinearLayout(MainActivity.this);

            box.setOrientation(
                    LinearLayout.VERTICAL
            );

            box.setPadding(
                    45,
                    35,
                    45,
                    35
            );


            GradientDrawable background =
                    new GradientDrawable();

            background.setColor(
                    Color.rgb(25, 31, 43)
            );

            background.setCornerRadius(35);

            box.setBackground(background);


            TextView title =
                    new TextView(MainActivity.this);

            title.setText(
                    finalResult
                            ? "FINAL RESULT"
                            : "RESULT"
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

                            + "\n\n"

                            + "या 10 Levels मधील गुण: "
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
                            : "NEXT LEVEL"
            );


            box.addView(title);

            box.addView(info);

            box.addView(next);


            dialog.setContentView(box);

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


        // =====================================================
        // DRAW BUTTON
        // =====================================================

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
                    18,
                    18,
                    p
            );


            p.setColor(Color.WHITE);

            p.setTextSize(textSize);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );


            Paint.FontMetrics fm =
                    p.getFontMetrics();


            float y =
                    rect.centerY()
                            - (fm.ascent + fm.descent)
                            / 2;


            canvas.drawText(
                    text,
                    rect.centerX()
                            - p.measureText(text) / 2,
                    y,
                    p
            );
        }


        // =====================================================
        // DRAW TEXT
        // =====================================================

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


        // =====================================================
        // DRAW SCREEN
        // =====================================================

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);


            canvas.drawColor(
                    Color.rgb(15, 19, 28)
            );


            int width = getWidth();
            int height = getHeight();


            // =================================================
            // RESPONSIVE SIZE
            // =================================================

            float pad =
                    Math.max(
                            18,
                            width * 0.035f
                    );


            float gap =
                    Math.max(
                            7,
                            width * 0.014f
                    );


            // वरचा स्पेस
            float topSpace =
                    Math.max(
                            45,
                            height * 0.045f
                    );


            // =================================================
            // HEADER
            // =================================================

            float titleSize =
                    Math.max(
                            24,
                            Math.min(
                                    34,
                                    width * 0.060f
                            )
                    );


            drawText(
                    canvas,
                    "JD NUMBER PUZZLE",
                    pad,
                    topSpace + titleSize,
                    titleSize,
                    Color.WHITE,
                    true
            );


            float subSize =
                    Math.max(
                            13,
                            Math.min(
                                    19,
                                    width * 0.030f
                            )
                    );


            drawText(
                    canvas,
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer",
                    pad,
                    topSpace
                            + titleSize
                            + subSize
                            + 4,
                    subSize,
                    Color.LTGRAY,
                    false
            );


            // =================================================
            // SCORE
            // =================================================

            p.setColor(
                    Color.rgb(255, 214, 73)
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextSize(
                    Math.max(
                            16,
                            width * 0.035f
                    )
            );


            String scoreText =
                    "★ " + score + " गुण";


            canvas.drawText(
                    scoreText,
                    width
                            - p.measureText(scoreText)
                            - pad,
                    topSpace + titleSize,
                    p
            );


            // =================================================
            // QUESTION BOX
            // =================================================

            float headerBottom =
                    topSpace
                            + titleSize
                            + subSize
                            + 18;


            float questionTop =
                    headerBottom
                            + Math.max(
                                    18,
                                    height * 0.018f
                            );


            float questionHeight =
                    Math.max(
                            135,
                            Math.min(
                                    190,
                                    height * 0.16f
                            )
                    );


            p.setColor(
                    Color.rgb(28, 34, 48)
            );


            canvas.drawRoundRect(
                    pad,
                    questionTop,
                    width - pad,
                    questionTop + questionHeight,
                    20,
                    20,
                    p
            );


            // =================================================
            // QUESTION TITLE
            // =================================================

            drawText(
                    canvas,
                    "प्रश्न",
                    pad + 30,
                    questionTop + 38,
                    Math.max(
                            19,
                            width * 0.042f
                    ),
                    Color.rgb(255, 214, 73),
                    true
            );


            // =================================================
            // QUESTION TEXT
            // =================================================

            float questionTextSize =
                    Math.max(
                            18,
                            Math.min(
                                    26,
                                    width * 0.043f
                            )
                    );


            p.setTextSize(
                    questionTextSize
            );

            p.setTypeface(
                    Typeface.DEFAULT
            );


            float maxWidth =
                    width
                            - 2 * pad
                            - 40;


            ArrayList<String> lines =
                    new ArrayList<>();


            String[] words =
                    question.split(" ");


            String currentLine = "";


            for (String word : words) {

                String testLine;

                if (currentLine.isEmpty()) {

                    testLine = word;

                } else {

                    testLine =
                            currentLine
                                    + " "
                                    + word;
                }


                if (p.measureText(testLine)
                        <= maxWidth) {

                    currentLine =
                            testLine;

                } else {

                    if (!currentLine.isEmpty()) {

                        lines.add(
                                currentLine
                        );
                    }

                    currentLine = word;
                }
            }


            if (!currentLine.isEmpty()) {

                lines.add(
                        currentLine
                );
            }


            float questionY =
                    questionTop + 83;


            float lineHeight =
                    questionTextSize + 10;


            for (
                    int i = 0;
                    i < lines.size() && i < 2;
                    i++
            ) {

                drawText(
                        canvas,
                        lines.get(i),
                        pad + 30,
                        questionY
                                + i * lineHeight,
                        questionTextSize,
                        Color.WHITE,
                        false
                );
            }


            // =================================================
            // NUMBER BOX AREA
            // =================================================

            float boxTop =
                    questionTop
                            + questionHeight
                            + Math.max(
                                    20,
                                    height * 0.020f
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
                                    - 2 * pad
                                    - 4 * gap
                    ) / 5f;


            // =================================================
            // 10 NUMBER BOXES
            // =================================================

            for (int i = 0; i < 10; i++) {

                int row = i / 5;

                int column = i % 5;


                float x =
                        pad
                                + column
                                * (boxWidth + gap);


                float y =
                        boxTop
                                + row
                                * (boxHeight + gap);


                boxes[i] =
                        new RectF(
                                x,
                                y,
                                x + boxWidth,
                                y + boxHeight
                        );


                // Selected
                if (
                        i == firstSelected ||
                        i == secondSelected
                ) {

                    p.setColor(
                            Color.rgb(
                                    55,
                                    115,
                                    200
                            )
                    );

                } else {

                    p.setColor(
                            Color.rgb(
                                    43,
                                    51,
                                    66
                            )
                    );
                }


                canvas.drawRoundRect(
                        boxes[i],
                        16,
                        16,
                        p
                );


                String number =
                        String.valueOf(
                                nums.get(i)
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


                p.setColor(Color.WHITE);

                p.setTypeface(
                        Typeface.DEFAULT_BOLD
                );

                p.setTextSize(
                        numberSize
                );


                Paint.FontMetrics fm =
                        p.getFontMetrics();


                float numberY =
                        boxes[i].centerY()
                                - (
                                fm.ascent
                                        + fm.descent
                        ) / 2;


                canvas.drawText(
                        number,
                        boxes[i].centerX()
                                - p.measureText(
                                number
                        ) / 2,
                        numberY,
                        p
                );
            }


            // =================================================
            // OPERATION
            // =================================================

            float operationY =
                    boxTop
                            + boxAreaHeight
                            + Math.max(
                                    22,
                                    height * 0.020f
                            );


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


            drawText(
                    canvas,
                    operationText,
                    pad,
                    operationY,
                    Math.max(
                            18,
                            width * 0.040f
                    ),
                    Color.rgb(
                            255,
                            214,
                            73
                    ),
                    true
            );


            // =================================================
            // BUTTONS
            // =================================================

            float buttonTop =
                    operationY
                            + Math.max(
                                    20,
                                    height * 0.022f
                            );


            float buttonHeight =
                    Math.max(
                            72,
                            Math.min(
                                    94,
                                    height * 0.082f
                            )
                    );


            float buttonWidth =
                    (
                            width
                                    - 2 * pad
                                    - 2 * gap
                    ) / 3f;


            reset.set(
                    pad,
                    buttonTop,
                    pad + buttonWidth,
                    buttonTop + buttonHeight
            );


            check.set(
                    pad + buttonWidth + gap,
                    buttonTop,
                    pad
                            + 2 * buttonWidth
                            + gap,
                    buttonTop + buttonHeight
            );


            result.set(
                    pad
                            + 2 * (
                            buttonWidth + gap
                    ),
                    buttonTop,
                    width - pad,
                    buttonTop + buttonHeight
            );


            drawButton(
                    canvas,
                    reset,
                    "RESET",
                    Color.rgb(
                            225,
                            60,
                            65
                    ),
                    Math.max(
                            17,
                            width * 0.040f
                    )
            );


            drawButton(
                    canvas,
                    check,
                    "CHECK ✓",
                    Color.rgb(
                            72,
                            205,
                            125
                    ),
                    Math.max(
                            17,
                            width * 0.040f
                    )
            );


            drawButton(
                    canvas,
                    result,
                    "RESULT",
                    Color.rgb(
                            105,
                            83,
                            210
                    ),
                    Math.max(
                            17,
                            width * 0.040f
                    )
            );


            // =================================================
            // STATUS
            // =================================================

            if (!status.isEmpty()) {

                drawText(
                        canvas,
                        status,
                        pad,
                        buttonTop
                                + buttonHeight
                                + 35,
                        Math.max(
                                15,
                                width * 0.033f
                        ),
                        Color.WHITE,
                        false
                );
            }
        }


        // =====================================================
        // TOUCH
        // =====================================================

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


            // Number boxes
            for (int i = 0; i < 10; i++) {

                if (
                        boxes[i] != null &&
                        boxes[i].contains(x, y)
                ) {

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
            if (reset.contains(x, y)) {

                firstSelected = -1;

                secondSelected = -1;

                status = "RESET केले.";

                invalidate();

                return true;
            }


            // CHECK
            if (check.contains(x, y)) {

                checkAnswer();

                return true;
            }


            // RESULT
            if (result.contains(x, y)) {

                showResult(false);

                return true;
            }


            return true;
        }
    }
}
