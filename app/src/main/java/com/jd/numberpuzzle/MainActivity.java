package com.jd.numberpuzzle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity {

    private GameView game;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        // FULL SCREEN
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideSystemBars();

        game = new GameView(this);
        setContentView(game);
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

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random r = new Random();

        ArrayList<BigInteger> nums = new ArrayList<>();

        RectF[] boxes = new RectF[10];
        RectF[] ops = new RectF[4];

        RectF reset = new RectF();
        RectF check = new RectF();
        RectF result = new RectF();

        int level = 1;
        int score = 0;
        int roundCorrect = 0;
        int roundWrong = 0;

        int a = -1;
        int b = -1;

        String selectedOp = "";
        String status = "";

        BigInteger target;
        BigInteger solA;
        BigInteger solB;
        String solOp;

        String[] opList = {"+", "−", "×", "÷"};

        GameView(Context c) {
            super(c);
            setFocusable(true);
            newPuzzle();
        }

        int digits(int lv) {
            if (lv <= 100) return 4;
            if (lv <= 250) return 5;
            if (lv <= 400) return 6;
            if (lv <= 550) return 7;
            if (lv <= 700) return 8;
            if (lv <= 850) return 9;
            return 10;
        }

        BigInteger odd(int d) {

            StringBuilder s = new StringBuilder();

            s.append((char) ('1' + r.nextInt(9)));

            for (int i = 1; i < d; i++) {
                s.append((char) ('0' + r.nextInt(10)));
            }

            s.setCharAt(
                    d - 1,
                    (char) ('0' + (2 * r.nextInt(5) + 1))
            );

            return new BigInteger(s.toString());
        }

        void newPuzzle() {

            nums.clear();

            a = -1;
            b = -1;
            selectedOp = "";
            status = "";

            int d = digits(level);

            int oi = r.nextInt(4);

            solOp = opList[oi];

            solA = odd(d);
            solB = odd(d);

            if (solOp.equals("+")) {

                target = solA.add(solB);

            } else if (solOp.equals("−")) {

                if (solA.compareTo(solB) < 0) {

                    BigInteger t = solA;
                    solA = solB;
                    solB = t;
                }

                target = solA.subtract(solB);

            } else if (solOp.equals("×")) {

                target = solA.multiply(solB);

            } else {

                BigInteger divisor =
                        odd(Math.max(2, d - 1));

                BigInteger quotient =
                        odd(Math.max(2, d - 1));

                solB = divisor;
                solA = divisor.multiply(quotient);
                target = quotient;
            }

            nums.add(solA);
            nums.add(solB);

            while (nums.size() < 10) {

                BigInteger n = odd(d);

                boolean duplicate = false;

                for (BigInteger x : nums) {

                    if (x.equals(n)) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    nums.add(n);
                }
            }

            Collections.shuffle(nums, r);

            invalidate();
        }

        boolean correct(
                BigInteger x,
                BigInteger y,
                String o
        ) {

            if (!o.equals(solOp))
                return false;

            try {

                BigInteger v;

                if (o.equals("+")) {

                    v = x.add(y);

                } else if (o.equals("−")) {

                    v = x.subtract(y);

                } else if (o.equals("×")) {

                    v = x.multiply(y);

                } else {

                    if (y.equals(BigInteger.ZERO))
                        return false;

                    if (!x.mod(y)
                            .equals(BigInteger.ZERO))
                        return false;

                    v = x.divide(y);
                }

                return v.equals(target);

            } catch (Exception e) {

                return false;
            }
        }

        void checkAnswer() {

            if (a < 0 ||
                    b < 0 ||
                    selectedOp.isEmpty()) {

                status =
                        "दोन बॉक्स आणि ऑपरेटर निवडा.";

                invalidate();
                return;
            }

            if (correct(
                    nums.get(a),
                    nums.get(b),
                    selectedOp
            )) {

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

            TextView title =
                    new TextView(
                            MainActivity.this
                    );

            title.setText(
                    finalResult
                            ? "🏆 FINAL RESULT"
                            : "🎯 RESULT"
            );

            title.setTextColor(Color.WHITE);
            title.setTextSize(26);
            title.setGravity(Gravity.CENTER);

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
                        "Level " +
                                (level - 9) +
                                " ते " +
                                level;
            }

            info.setText(
                    levelText +
                            "\n\n" +
                            "या 10 Levels मधील गुण: " +
                            roundCorrect +
                            "/10\n" +
                            "चुकीचे प्रयत्न: " +
                            roundWrong +
                            "\n\n" +
                            "एकूण गुण: " +
                            score
            );

            info.setTextColor(Color.WHITE);
            info.setTextSize(19);
            info.setGravity(Gravity.CENTER);

            Button next =
                    new Button(
                            MainActivity.this
                    );

            next.setText(
                    finalResult
                            ? "GAME पुन्हा सुरू करा"
                            : "NEXT LEVEL ▶"
            );

            box.addView(title);
            box.addView(info);
            box.addView(next);

            d.setContentView(box);
            d.show();

            next.setOnClickListener(
                    v -> {

                        d.dismiss();

                        if (finalResult) {

                            level = 1;
                            score = 0;
                            roundCorrect = 0;
                            roundWrong = 0;

                            newPuzzle();
                            return;
                        }

                        roundCorrect = 0;
                        roundWrong = 0;

                        if (level < 1000) {

                            level++;
                            newPuzzle();
                        }
                    }
            );
        }

        void button(
                Canvas c,
                RectF q,
                String text,
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

            p.setColor(Color.WHITE);
            p.setTextSize(size);
            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            Paint.FontMetrics f =
                    p.getFontMetrics();

            c.drawText(
                    text,
                    q.centerX()
                            - p.measureText(text) / 2,
                    q.centerY()
                            - (f.ascent + f.descent) / 2,
                    p
            );
        }

        @Override
        protected void onDraw(Canvas c) {

            super.onDraw(c);

            c.drawColor(
                    Color.rgb(15, 19, 28)
            );

            int w = getWidth();
            int h = getHeight();

            float pad = Math.max(10, w * 0.025f);

            // =========================
            // RESPONSIVE SCREEN
            // =========================

            float headerH =
                    Math.max(65, h * 0.055f);

            float targetH =
                    Math.max(75, h * 0.075f);

            float gridTop =
                    headerH + targetH;

            // Grid gets more screen space
            float gridH =
                    Math.max(
                            230,
                            h * 0.30f
                    );

            float gap =
                    Math.max(6, w * 0.012f);

            float bw =
                    (w - 2 * pad - 4 * gap)
                            / 5f;

            float bh =
                    (gridH - gap) / 2f;

            // =========================
            // HEADER
            // =========================

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setColor(Color.WHITE);
            p.setTextSize(
                    Math.max(18, w * 0.045f)
            );

            c.drawText(
                    "JD Number Puzzle",
                    pad,
                    headerH * 0.38f,
                    p
            );

            p.setTypeface(
                    Typeface.DEFAULT
            );

            p.setColor(Color.LTGRAY);
            p.setTextSize(
                    Math.max(11, w * 0.026f)
            );

            c.drawText(
                    "Level " +
                            level +
                            " / 1000 • " +
                            digits(level) +
                            " digit • No Timer",
                    pad,
                    headerH * 0.72f,
                    p
            );

            // SCORE

            p.setColor(
                    Color.rgb(255, 214, 73)
            );

            p.setTextSize(
                    Math.max(13, w * 0.03f)
            );

            String ss =
                    "⭐ " +
                            score +
                            " गुण";

            c.drawText(
                    ss,
                    w -
                            p.measureText(ss) -
                            pad,
                    headerH * 0.38f,
                    p
            );

            // =========================
            // TARGET
            // =========================

            p.setColor(
                    Color.rgb(255, 214, 73)
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextSize(
                    Math.min(
                            34,
                            w * 0.075f
                    )
            );

            String targetText =
                    target == null
                            ? "0"
                            : target.toString();

            c.drawText(
                    targetText,
                    w / 2f
                            - p.measureText(targetText) / 2,
                    gridTop - 15,
                    p
            );

            // =========================
            // NUMBER BOXES
            // =========================

            float top = gridTop;

            for (int i = 0; i < 10; i++) {

                int row = i / 5;
                int col = i % 5;

                float x =
                        pad +
                                col * (bw + gap);

                float y =
                        top +
                                row * (bh + gap);

                boxes[i] =
                        new RectF(
                                x,
                                y,
                                x + bw,
                                y + bh
                        );

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
                        16,
                        16,
                        p
                );

                p.setColor(Color.WHITE);

                String s =
                        nums.get(i).toString();

                float textSize;

                if (s.length() > 9) {

                    textSize =
                            Math.max(
                                    11,
                                    bw * 0.14f
                            );

                } else if (s.length() > 7) {

                    textSize =
                            Math.max(
                                    13,
                                    bw * 0.17f
                            );

                } else {

                    textSize =
                            Math.max(
                                    16,
                                    bw * 0.20f
                            );
                }

                p.setTextSize(textSize);

                c.drawText(
                        s,
                        boxes[i].centerX()
                                - p.measureText(s) / 2,
                        boxes[i].centerY()
                                - (
                                p.ascent()
                                        + p.descent()
                        ) / 2,
                        p
                );
            }

            // =========================
            // INSTRUCTION
            // =========================

            float infoY =
                    top +
                            gridH +
                            h * 0.018f;

            float infoH =
                    Math.max(
                            42,
                            h * 0.045f
                    );

            p.setColor(
                    Color.rgb(28, 34, 48)
            );

            c.drawRoundRect(
                    0,
                    infoY,
                    w,
                    infoY + infoH,
                    8,
                    8,
                    p
            );

            p.setColor(Color.LTGRAY);

            p.setTextSize(
                    Math.max(
                            12,
                            w * 0.027f
                    )
            );

            c.drawText(
                    "दोन नंबर निवडा → ऑपरेटर निवडा → CHECK",
                    pad,
                    infoY + infoH * 0.68f,
                    p
            );

            // =========================
            // OPERATORS
            // =========================

            float ot =
                    infoY +
                            infoH +
                            h * 0.018f;

            float oh =
                    Math.max(
                            55,
                            h * 0.065f
                    );

            float ow =
                    (
                            w -
                                    2 * pad -
                                    3 * gap
                    ) / 4f;

            for (int i = 0; i < 4; i++) {

                float x =
                        pad +
                                i * (ow + gap);

                ops[i] =
                        new RectF(
                                x,
                                ot,
                                x + ow,
                                ot + oh
                        );

                int color =
                        opList[i]
                                .equals(selectedOp)
                                ? Color.rgb(
                                72,
                                120,
                                210
                        )
                                : Color.rgb(
                                60,
                                70,
                                92
                        );

                button(
                        c,
                        ops[i],
                        opList[i],
                        color,
                        Math.max(
                                22,
                                w * 0.055f
                        )
                );
            }

            // =========================
            // RESET / CHECK / RESULT
            // =========================

            float at =
                    ot +
                            oh +
                            h * 0.022f;

            float ah =
                    Math.max(
                            62,
                            h * 0.075f
                    );

            float aw =
                    (
                            w -
                                    2 * pad -
                                    2 * gap
                    ) / 3f;

            reset.set(
                    pad,
                    at,
                    pad + aw,
                    at + ah
            );

            check.set(
                    pad + aw + gap,
                    at,
                    pad + 2 * aw + gap,
                    at + ah
            );

            result.set(
                    pad + 2 * (aw + gap),
                    at,
                    w - pad,
                    at + ah
            );

            button(
                    c,
                    reset,
                    "RESET",
                    Color.rgb(
                            225,
                            60,
                            65
                    ),
                    Math.max(
                            14,
                            w * 0.035f
                    )
            );

            button(
                    c,
                    check,
                    "CHECK ✓",
                    Color.rgb(
                            72,
                            205,
                            125
                    ),
                    Math.max(
                            14,
                            w * 0.035f
                    )
            );

            button(
                    c,
                    result,
                    "RESULT",
                    Color.rgb(
                            105,
                            83,
                            210
                    ),
                    Math.max(
                            14,
                            w * 0.035f
                    )
            );

            // =========================
            // STATUS
            // =========================

            if (!status.isEmpty()) {

                p.setColor(Color.WHITE);

                p.setTextSize(
                        Math.max(
                                13,
                                w * 0.030f
                        )
                );

                c.drawText(
                        status,
                        pad,
                        at +
                                ah +
                                h * 0.035f,
                        p
                );
            }
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent e
        ) {

            if (e.getAction()
                    != MotionEvent.ACTION_UP) {

                return true;
            }

            float x = e.getX();
            float y = e.getY();

            // NUMBER BOXES

            for (int i = 0; i < 10; i++) {

                if (boxes[i] != null &&
                        boxes[i].contains(x, y)) {

                    if (a < 0) {

                        a = i;

                    } else if (
                            b < 0 &&
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

            // OPERATORS

            for (int i = 0; i < 4; i++) {

                if (ops[i] != null &&
                        ops[i].contains(x, y)) {

                    selectedOp =
                            opList[i];

                    status = "";

                    invalidate();

                    return true;
                }
            }

            // RESET

            if (reset.contains(x, y)) {

                a = -1;
                b = -1;
                selectedOp = "";

                status =
                        "RESET केले.";

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
