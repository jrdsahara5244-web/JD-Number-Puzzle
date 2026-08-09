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
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);

        if (f) {
            hideBars();
        }
    }

    // =========================================================
    // GAME VIEW
    // =========================================================

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Random r = new Random();

        ArrayList<Integer> nums = new ArrayList<>();

        RectF[] boxes = new RectF[10];

        RectF reset = new RectF();
        RectF check = new RectF();
        RectF result = new RectF();

        int level = 1;
        int score = 0;

        int roundCorrect = 0;
        int roundWrong = 0;

        int a = -1;
        int b = -1;

        int target;

        String operation = "";
        String question = "";
        String status = "";

        // =====================================================
        // CONSTRUCTOR
        // =====================================================

        GameView(Context c) {
            super(c);

            setFocusable(true);

            newPuzzle();
        }

        // =====================================================
        // RANDOM SMALL NUMBER
        // =====================================================

        int n() {

            // 1 ते 9 = 35%
            // 10 ते 99 = 65%

            if (r.nextInt(100) < 35) {

                return 1 + r.nextInt(9);

            } else {

                return 10 + r.nextInt(90);
            }
        }

        // =====================================================
        // FILL NUMBER BOXES
        // =====================================================

        void fill() {

            while (nums.size() < 10) {

                int x = n();

                boolean duplicate = false;

                for (int v : nums) {

                    if (v == x) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    nums.add(x);
                }
            }

            Collections.shuffle(nums, r);
        }

        // =====================================================
        // NEW PUZZLE
        // =====================================================

        void newPuzzle() {

            nums.clear();

            a = -1;
            b = -1;

            status = "";

            int type = r.nextInt(4);

            int x;
            int y;

            // =================================================
            // ADDITION
            // =================================================

            if (type == 0) {

                operation = "+";

                do {

                    x = n();
                    y = n();

                    target = x + y;

                } while (target < 10 || target > 198);

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

                    x = n();
                    y = n();

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

                    x = 1 + r.nextInt(99);
                    y = 1 + r.nextInt(99);

                    target = x * y;

                } while (
                        target < 10 ||
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

                    y = 2 + r.nextInt(8);

                    target = 1 + r.nextInt(11);

                    x = y * target;

                } while (x > 99);

                question =
                        "कोणता नंबर कोणत्या नंबरने "
                                + "भागल्यावर "
                                + target
                                + " मिळेल?";
            }

            // =================================================
            // ADD CORRECT NUMBERS
            // =================================================

            nums.add(x);

            if (!nums.contains(y)) {
                nums.add(y);
            } else {

                // y आधीच असेल तर नवीन नंबर
                int newY;

                do {
                    newY = n();
                } while (nums.contains(newY));

                nums.add(newY);
            }

            // =================================================
            // FILL REMAINING BOXES
            // =================================================

            fill();

            invalidate();
        }

        // =====================================================
        // CHECK CORRECT ANSWER
        // =====================================================

        boolean correct(int x, int y) {

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

        // =====================================================
        // CHECK BUTTON
        // =====================================================

        void checkAnswer() {

            if (a < 0 || b < 0) {

                status =
                        "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first = nums.get(a);
            int second = nums.get(b);

            if (correct(first, second)) {

                score++;

                roundCorrect++;

                status =
                        "✓ बरोबर! +1 गुण";

                invalidate();

                postDelayed(
                        new Runnable() {

                            @Override
                            public void run() {

                                if (level % 10 == 0) {

                                    showResult(
                                            level == 1000
                                    );

                                } else {

                                    level++;

                                    newPuzzle();
                                }
                            }

                        },
                        650
                );

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

            final Dialog d =
                    new Dialog(MainActivity.this);

            LinearLayout box =
                    new LinearLayout(
                            MainActivity.this
                    );

            box.setOrientation(
                    LinearLayout.VERTICAL
            );

            box.setPadding(
                    45,
                    35,
                    45,
                    35
            );

            GradientDrawable bg =
                    new GradientDrawable();

            bg.setColor(
                    Color.rgb(25, 31, 43)
            );

            bg.setCornerRadius(35);

            box.setBackground(bg);

            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

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

            title.setTextSize(26);

            title.setGravity(
                    Gravity.CENTER
            );

            // -------------------------------------------------
            // INFORMATION
            // -------------------------------------------------

            TextView info =
                    new TextView(
                            MainActivity.this
                    );

            String levelText;

            if (finalResult) {

                levelText =
                        "Level 991 ते 1000";

            } else {

                levelText =
                        "Level "
                                + (level - 9)
                                + " ते "
                                + level;
            }

            info.setText(
                    levelText
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

            info.setTextSize(19);

            info.setGravity(
                    Gravity.CENTER
            );

            // -------------------------------------------------
            // NEXT BUTTON
            // -------------------------------------------------

            Button next =
                    new Button(
                            MainActivity.this
                    );

            next.setText(
                    finalResult
                            ? "GAME पुन्हा सुरू करा"
                            : "NEXT LEVEL ▶"
            );

            // -------------------------------------------------
            // ADD VIEWS
            // -------------------------------------------------

            box.addView(title);

            box.addView(info);

            box.addView(next);

            d.setContentView(box);

            d.show();

            // -------------------------------------------------
            // NEXT CLICK
            // -------------------------------------------------

            next.setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            d.dismiss();

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
                    }
            );
        }

        // =====================================================
        // DRAW BUTTON
        // =====================================================

        void btn(
                Canvas c,
                RectF q,
                String s,
                int color,
                float size
        ) {

            p.setColor(color);

            c.drawRoundRect(
                    q,
                    18,
                    18,
                    p
            );

            p.setColor(
                    Color.WHITE
            );

            p.setTextSize(size);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            Paint.FontMetrics f =
                    p.getFontMetrics();

            c.drawText(
                    s,
                    q.centerX()
                            - p.measureText(s) / 2,
                    q.centerY()
                            - (f.ascent + f.descent) / 2,
                    p
            );
        }

        // =====================================================
        // DRAW TEXT
        // =====================================================

        void text(
                Canvas c,
                String s,
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

            c.drawText(
                    s,
                    x,
                    y,
                    p
            );
        }

        // =====================================================
        // MAIN DRAW
        // =====================================================

        @Override
        protected void onDraw(Canvas c) {

            super.onDraw(c);

            // -------------------------------------------------
            // BACKGROUND
            // -------------------------------------------------

            c.drawColor(
                    Color.rgb(15, 19, 28)
            );

            int w = getWidth();

            int h = getHeight();

            // -------------------------------------------------
            // RESPONSIVE SCALE
            // -------------------------------------------------

            float baseW = 720f;

            float scale = w / baseW;

            if (scale < 0.80f) {
                scale = 0.80f;
            }

            if (scale > 1.35f) {
                scale = 1.35f;
            }

            float pad =
                    24f * scale;

            float gap =
                    10f * scale;

            // -------------------------------------------------
            // TOP SPACE
            // -------------------------------------------------

            float top =
                    38f * scale;

            // =================================================
            // HEADER
            // =================================================

            float titleSize =
                    34f * scale;

            text(
                    c,
                    "JD NUMBER PUZZLE",
                    pad,
                    top + titleSize,
                    titleSize,
                    Color.WHITE,
                    true
            );

            float subSize =
                    19f * scale;

            text(
                    c,
                    "Level "
                            + level
                            + " / 1000 • छोटे नंबर • No Timer",
                    pad,
                    top
                            + titleSize
                            + subSize
                            + 5f,
                    subSize,
                    Color.LTGRAY,
                    false
            );

            // =================================================
            // SCORE
            // =================================================

            p.setColor(
                    Color.rgb(
                            255,
                            214,
                            73
                    )
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextSize(
                    23f * scale
            );

            String scoreText =
                    "★ " + score + " गुण";

            c.drawText(
                    scoreText,
                    w
                            - pad
                            - p.measureText(scoreText),
                    top + titleSize,
                    p
            );

            // =================================================
            // QUESTION BOX
            // =================================================

            float headerBottom =
                    top
                            + titleSize
                            + subSize
                            + 12f * scale;

            float qTop =
                    headerBottom
                            + 22f * scale;

            float qHeight =
                    Math.max(
                            145f * scale,
                            Math.min(
                                    190f * scale,
                                    h * 0.17f
                            )
                    );

            RectF questionBox =
                    new RectF(
                            pad,
                            qTop,
                            w - pad,
                            qTop + qHeight
                    );

            p.setColor(
                    Color.rgb(
                            28,
                            34,
                            48
                    )
            );

            c.drawRoundRect(
                    questionBox,
                    18f * scale,
                    18f * scale,
                    p
            );

            // =================================================
            // QUESTION TITLE
            // =================================================

            text(
                    c,
                    "प्रश्न",
                    pad + 18f * scale,
                    qTop + 34f * scale,
                    24f * scale,
                    Color.rgb(
                            255,
                            214,
                            73
                    ),
                    true
            );

            // =================================================
            // QUESTION TEXT
            // =================================================

            float questionSize =
                    24f * scale;

            p.setTextSize(
                    questionSize
            );

            p.setTypeface(
                    Typeface.DEFAULT
            );

            float maxWidth =
                    w
                            - (pad * 2)
                            - 36f * scale;

            ArrayList<String> lines =
                    new ArrayList<>();

            String[] words =
                    question.split(" ");

            String currentLine = "";

            for (String word : words) {

                String test;

                if (currentLine.length() == 0) {

                    test = word;

                } else {

                    test =
                            currentLine
                                    + " "
                                    + word;
                }

                if (p.measureText(test)
                        <= maxWidth) {

                    currentLine = test;

                } else {

                    if (currentLine.length()
                            > 0) {

                        lines.add(
                                currentLine
                        );
                    }

                    currentLine = word;
                }
            }

            if (currentLine.length()
                    > 0) {

                lines.add(
                        currentLine
                );
            }

            float questionY =
                    qTop
                            + 76f * scale;

            float lineHeight =
                    32f * scale;

            for (
                    int i = 0;
                    i < lines.size() && i < 3;
                    i++
            ) {

                text(
                        c,
                        lines.get(i),
                        pad + 18f * scale,
                        questionY
                                + i * lineHeight,
                        questionSize,
                        Color.WHITE,
                        false
                );
            }

            // =================================================
            // NUMBER BOX AREA
            // =================================================

            float boxTop =
                    qTop
                            + qHeight
                            + 22f * scale;

            float availableForGrid =
                    h
                            - boxTop
                            - 250f * scale;

            float gridHeight =
                    Math.max(
                            330f * scale,
                            Math.min(
                                    500f * scale,
                                    availableForGrid
                            )
                    );

            float boxHeight =
                    (gridHeight - gap) / 2f;

            float boxWidth =
                    (
                            w
                                    - 2f * pad
                                    - 4f * gap
                    ) / 5f;

            // =================================================
            // 10 NUMBER BOXES
            // =================================================

            for (int i = 0; i < 10; i++) {

                int row = i / 5;

                int col = i % 5;

                float x =
                        pad
                                + col
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

                // ------------------------------------------------
                // SELECTED
                // ------------------------------------------------

                if (i == a || i == b) {

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

                c.drawRoundRect(
                        boxes[i],
                        16f * scale,
                        16f * scale,
                        p
                );

                // ------------------------------------------------
                // NUMBER
                // ------------------------------------------------

                String number =
                        String.valueOf(
                                nums.get(i)
                        );

                float numberSize;

                if (number.length() == 1) {

                    numberSize =
                            42f * scale;

                } else {

                    numberSize =
                            34f * scale;
                }

                p.setColor(
                        Color.WHITE
                );

                p.setTypeface(
                        Typeface.DEFAULT_BOLD
                );

                p.setTextSize(
                        numberSize
                );

                Paint.FontMetrics fm =
                        p.getFontMetrics();

                float textY =
                        boxes[i].centerY()
                                - (
                                fm.ascent
                                        + fm.descent
                        ) / 2f;

                c.drawText(
                        number,
                        boxes[i].centerX()
                                - p.measureText(
                                number
                        ) / 2f,
                        textY,
                        p
                );
            }

            // =================================================
            // OPERATION
            // =================================================

            float operationY =
                    boxTop
                            + gridHeight
                            + 30f * scale;

            String op;

            if (operation.equals("+")) {

                op =
                        "+  अधिक";

            } else if (
                    operation.equals("−")
            ) {

                op =
                        "−  वजा";

            } else if (
                    operation.equals("×")
            ) {

                op =
                        "×  गुणाकार";

            } else {

                op =
                        "÷  भागाकार";
            }

            text(
                    c,
                    "क्रिया: " + op,
                    pad,
                    operationY,
                    24f * scale,
                    Color.rgb(
                            255,
                            214,
                            73
                    ),
                    true
            );

            // =================================================
            // BUTTON AREA
            // =================================================

            float buttonTop =
                    operationY
                            + 22f * scale;

            float buttonHeight =
                    Math.max(
                            72f * scale,
                            Math.min(
                                    100f * scale,
                                    h * 0.09f
                            )
                    );

            float buttonWidth =
                    (
                            w
                                    - 2f * pad
                                    - 2f * gap
                    ) / 3f;

            // =================================================
            // RESET
            // =================================================

            reset.set(
                    pad,
                    buttonTop,
                    pad + buttonWidth,
                    buttonTop + buttonHeight
            );

            // =================================================
            // CHECK
            // =================================================

            check.set(
                    pad + buttonWidth + gap,
                    buttonTop,
                    pad
                            + 2f * buttonWidth
                            + gap,
                    buttonTop + buttonHeight
            );

            // =================================================
            // RESULT
            // =================================================

            result.set(
                    pad
                            + 2f
                            * (
                            buttonWidth
                                    + gap
                    ),
                    buttonTop,
                    w - pad,
                    buttonTop + buttonHeight
            );

            // =================================================
            // DRAW BUTTONS
            // =================================================

            btn(
                    c,
                    reset,
                    "RESET",
                    Color.rgb(
                            225,
                            60,
                            65
                    ),
                    22f * scale
            );

            btn(
                    c,
                    check,
                    "CHECK ✓",
                    Color.rgb(
                            72,
                            205,
                            125
                    ),
                    22f * scale
            );

            btn(
                    c,
                    result,
                    "RESULT",
                    Color.rgb(
                            105,
                            83,
                            210
                    ),
                    22f * scale
            );

            // =================================================
            // STATUS
            // =================================================

            if (!status.isEmpty()) {

                text(
                        c,
                        status,
                        pad,
                        buttonTop
                                + buttonHeight
                                + 32f * scale,
                        20f * scale,
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
                MotionEvent e
        ) {

            if (
                    e.getAction()
                            != MotionEvent.ACTION_UP
            ) {

                return true;
            }

            float x =
                    e.getX();

            float y =
                    e.getY();

            // =================================================
            // NUMBER BOX TOUCH
            // =================================================

            for (int i = 0; i < 10; i++) {

                if (
                        boxes[i] != null
                                &&
                                boxes[i].contains(
                                        x,
                                        y
                                )
                ) {

                    if (a < 0) {

                        a = i;

                    } else if (
                            b < 0
                                    &&
                                    i != a
                    ) {

                        b = i;

                    } else {

                        a = i;

                        b = -1;
                    }

                    status = "";

                    invalidate();

                    return true;
                }
            }

            // =================================================
            // RESET
            // =================================================

            if (
                    reset.contains(
                            x,
                            y
                    )
            ) {

                a = -1;

                b = -1;

                status =
                        "RESET केले.";

                invalidate();

                return true;
            }

            // =================================================
            // CHECK
            // =================================================

            if (
                    check.contains(
                            x,
                            y
                    )
            ) {

                checkAnswer();

                return true;
            }

            // =================================================
            // RESULT
            // =================================================

            if (
                    result.contains(
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
