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
        game = new GameView(this);
        setContentView(game);
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

            s.setCharAt(d - 1, (char) ('0' + (2 * r.nextInt(5) + 1)));
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
                // Division is always exact.
                BigInteger divisor = odd(Math.max(2, d - 1));
                BigInteger quotient = odd(Math.max(2, d - 1));

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

        boolean correct(BigInteger x, BigInteger y, String o) {
            if (!o.equals(solOp)) return false;

            try {
                BigInteger v;

                if (o.equals("+")) {
                    v = x.add(y);

                } else if (o.equals("−")) {
                    v = x.subtract(y);

                } else if (o.equals("×")) {
                    v = x.multiply(y);

                } else {
                    if (y.equals(BigInteger.ZERO)) return false;
                    if (!x.mod(y).equals(BigInteger.ZERO)) return false;
                    v = x.divide(y);
                }

                return v.equals(target);

            } catch (Exception e) {
                return false;
            }
        }

        void checkAnswer() {
            if (a < 0 || b < 0 || selectedOp.isEmpty()) {
                status = "दोन बॉक्स आणि ऑपरेटर निवडा.";
                invalidate();
                return;
            }

            if (correct(nums.get(a), nums.get(b), selectedOp)) {
                score++;
                roundCorrect++;
                status = "✓ बरोबर! +1 गुण";
                invalidate();

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (level % 10 == 0) {
                            showResult(level == 1000);
                        } else {
                            level++;
                            newPuzzle();
                        }
                    }
                }, 650);

            } else {
                roundWrong++;
                status = "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";
                invalidate();
            }
        }

        void showResult(final boolean finalResult) {
            final Dialog d = new Dialog(MainActivity.this);

            LinearLayout box = new LinearLayout(MainActivity.this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(45, 35, 45, 35);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.rgb(25, 31, 43));
            bg.setCornerRadius(35);
            box.setBackground(bg);

            TextView title = new TextView(MainActivity.this);
            title.setText(finalResult ? "🏆 FINAL RESULT" : "🎯 RESULT");
            title.setTextColor(Color.WHITE);
            title.setTextSize(26);
            title.setGravity(Gravity.CENTER);

            TextView info = new TextView(MainActivity.this);

            String levelText;
            if (finalResult) {
                levelText = "Level 991 ते 1000";
            } else {
                levelText = "Level " + (level - 9) + " ते " + level;
            }

            info.setText(
                    levelText + "\n\n" +
                    "या 10 Levels मधील गुण: " + roundCorrect + "/10\n" +
                    "चुकीचे प्रयत्न: " + roundWrong + "\n\n" +
                    "एकूण गुण: " + score
            );

            info.setTextColor(Color.WHITE);
            info.setTextSize(19);
            info.setGravity(Gravity.CENTER);

            Button next = new Button(MainActivity.this);
            next.setText(finalResult ? "GAME पुन्हा सुरू करा" : "NEXT LEVEL ▶");

            box.addView(title);
            box.addView(info);
            box.addView(next);

            d.setContentView(box);
            d.show();

            next.setOnClickListener(v -> {
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
            });
        }

        void button(Canvas c, RectF q, String text, int color, float size) {
            p.setColor(color);
            c.drawRoundRect(q, 18, 18, p);

            p.setColor(Color.WHITE);
            p.setTextSize(size);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            Paint.FontMetrics f = p.getFontMetrics();

            c.drawText(
                    text,
                    q.centerX() - p.measureText(text) / 2,
                    q.centerY() - (f.ascent + f.descent) / 2,
                    p
            );
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            c.drawColor(Color.rgb(15, 19, 28));

            int w = getWidth();
            float pad = 10;

            // Header
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setColor(Color.WHITE);
            p.setTextSize(20);
            c.drawText("JD Number Puzzle", pad, 28, p);

            p.setTypeface(Typeface.DEFAULT);
            p.setColor(Color.LTGRAY);
            p.setTextSize(12);
            c.drawText(
                    "Level " + level + " / 1000 • " +
                    digits(level) + " digit • No Timer",
                    pad,
                    48,
                    p
            );

            // Score
            p.setColor(Color.rgb(255, 214, 73));
            p.setTextSize(14);
            String ss = "⭐ " + score + " गुण";
            c.drawText(ss, w - p.measureText(ss) - 12, 28, p);

            // Target
            p.setColor(Color.rgb(255, 214, 73));
            p.setTextSize(30);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            String targetText = target == null ? "0" : target.toString();

            c.drawText(
                    targetText,
                    w / 2f - p.measureText(targetText) / 2,
                    92,
                    p
            );

            // Number boxes
            float top = 112;
            float gap = 6;
            float bw = (w - 2 * pad - 4 * gap) / 5f;
            float bh = 105;

            for (int i = 0; i < 10; i++) {
                int row = i / 5;
                int col = i % 5;

                float x = pad + col * (bw + gap);
                float y = top + row * (bh + gap);

                boxes[i] = new RectF(x, y, x + bw, y + bh);

                if (i == a || i == b) {
                    p.setColor(Color.rgb(55, 115, 200));
                } else {
                    p.setColor(Color.rgb(43, 51, 66));
                }

                c.drawRoundRect(boxes[i], 12, 12, p);

                p.setColor(Color.WHITE);

                String s = nums.get(i).toString();

                if (s.length() > 9) {
                    p.setTextSize(11);
                } else if (s.length() > 7) {
                    p.setTextSize(13);
                } else {
                    p.setTextSize(16);
                }

                c.drawText(
                        s,
                        boxes[i].centerX() - p.measureText(s) / 2,
                        boxes[i].centerY() - (p.ascent() + p.descent()) / 2,
                        p
                );
            }

            // Instruction bar
            float infoY = top + 2 * (bh + gap) + 8;

            p.setColor(Color.rgb(28, 34, 48));
            c.drawRect(0, infoY, w, infoY + 46, p);

            p.setColor(Color.LTGRAY);
            p.setTextSize(13);
            c.drawText(
                    "दोन नंबर निवडा → ऑपरेटर निवडा → CHECK",
                    12,
                    infoY + 30,
                    p
            );

            // Operator buttons
            float ot = infoY + 58;
            float ow = (w - 2 * pad - 3 * gap) / 4f;

            for (int i = 0; i < 4; i++) {
                float x = pad + i * (ow + gap);

                ops[i] = new RectF(
                        x,
                        ot,
                        x + ow,
                        ot + 62
                );

                int color = opList[i].equals(selectedOp)
                        ? Color.rgb(72, 120, 210)
                        : Color.rgb(60, 70, 92);

                button(c, ops[i], opList[i], color, 25);
            }

            // Bottom buttons: larger RESET / CHECK / RESULT
            float at = ot + 75;
            float aw = (w - 2 * pad - 14) / 3f;

            reset.set(
                    pad,
                    at,
                    pad + aw,
                    at + 70
            );

            check.set(
                    pad + aw + 7,
                    at,
                    pad + 2 * aw + 7,
                    at + 70
            );

            result.set(
                    pad + 2 * (aw + 7),
                    at,
                    w - pad,
                    at + 70
            );

            button(
                    c,
                    reset,
                    "RESET",
                    Color.rgb(225, 60, 65),
                    16
            );

            button(
                    c,
                    check,
                    "CHECK ✓",
                    Color.rgb(72, 205, 125),
                    16
            );

            button(
                    c,
                    result,
                    "RESULT",
                    Color.rgb(105, 83, 210),
                    16
            );

            // Status
            if (!status.isEmpty()) {
                p.setColor(Color.WHITE);
                p.setTextSize(14);
                p.setTypeface(Typeface.DEFAULT);

                c.drawText(
                        status,
                        pad,
                        at + 95,
                        p
                );
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }

            float x = e.getX();
            float y = e.getY();

            // Number boxes
            for (int i = 0; i < 10; i++) {
                if (boxes[i] != null && boxes[i].contains(x, y)) {

                    if (a < 0) {
                        a = i;

                    } else if (b < 0 && i != a) {
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

            // Operators
            for (int i = 0; i < 4; i++) {
                if (ops[i] != null && ops[i].contains(x, y)) {
                    selectedOp = opList[i];
                    status = "";
                    invalidate();
                    return true;
                }
            }

            // Reset
            if (reset.contains(x, y)) {
                a = -1;
                b = -1;
                selectedOp = "";
                status = "RESET केले.";
                invalidate();
                return true;
            }

            // Check
            if (check.contains(x, y)) {
                checkAnswer();
                return true;
            }

            // Result
            if (result.contains(x, y)) {
                showResult(false);
                return true;
            }

            return true;
        }
    }
}
