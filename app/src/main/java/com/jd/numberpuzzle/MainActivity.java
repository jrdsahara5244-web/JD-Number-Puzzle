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

    @Override public void onCreate(Bundle b) {
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

    @Override public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) hideBars();
    }

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

        GameView(Context c) {
            super(c);
            setFocusable(true);
            newPuzzle();
        }

        int n() {
            return r.nextInt(100) < 35
                    ? 1 + r.nextInt(9)
                    : 10 + r.nextInt(90);
        }

        void fill() {
            while (nums.size() < 10) {
                int x = n();
                boolean dup = false;

                for (int v : nums) {
                    if (v == x) {
                        dup = true;
                        break;
                    }
                }

                if (!dup) nums.add(x);
            }

            Collections.shuffle(nums, r);
        }

        void newPuzzle() {
            nums.clear();
            a = -1;
            b = -1;
            status = "";

            int type = r.nextInt(4);
            int x, y;

            if (type == 0) {
                operation = "+";
                do {
                    x = n();
                    y = n();
                    target = x + y;
                } while (target < 10 || target > 198);

                question = "कोणते दोन नंबर अधिक केल्यावर " +
                        target + " मिळेल?";

            } else if (type == 1) {
                operation = "−";

                do {
                    x = n();
                    y = n();
                } while (x <= y);

                target = x - y;

                question = "कोणत्या मोठ्या नंबरमधून कोणता नंबर " +
                        "वजा केल्यावर " + target + " मिळेल?";

            } else if (type == 2) {
                operation = "×";

                do {
                    x = 10 + r.nextInt(90);
                    y = 10 + r.nextInt(90);
                    target = x * y;
                } while (target < 100 || target > 9801);

                question = "कोणते दोन नंबर गुणिले असता " +
                        target + " मिळेल?";

            } else {
                operation = "÷";

                do {
                    y = 2 + r.nextInt(8);
                    target = 1 + r.nextInt(11);
                    x = y * target;
                } while (x > 99);

                question = "कोणता नंबर कोणत्या नंबरने भागल्यावर " +
                        target + " मिळेल?";
            }

            nums.add(x);
            nums.add(y);
            fill();
            invalidate();
        }

        boolean correct(int x, int y) {
            if (operation.equals("+"))
                return x + y == target;

            if (operation.equals("−"))
                return x > y && x - y == target;

            if (operation.equals("×"))
                return x * y == target;

            return y != 0 && x % y == 0 && x / y == target;
        }

        void checkAnswer() {
            if (a < 0 || b < 0) {
                status = "कृपया दोन नंबर निवडा.";
                invalidate();
                return;
            }

            if (correct(nums.get(a), nums.get(b))) {
                score++;
                roundCorrect++;
                status = "✓ बरोबर! +1 गुण";
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
            info.setText(
                    (finalResult
                            ? "Level 991 ते 1000"
                            : "Level " + (level - 9) + " ते " + level)
                            + "\n\nया 10 Levels मधील गुण: "
                            + roundCorrect + "/10"
                            + "\nचुकीचे प्रयत्न: " + roundWrong
                            + "\n\nएकूण गुण: " + score
            );

            info.setTextColor(Color.WHITE);
            info.setTextSize(19);
            info.setGravity(Gravity.CENTER);

            Button next = new Button(MainActivity.this);
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

            next.setOnClickListener(v -> {
                d.dismiss();

                if (finalResult) {
                    level = 1;
                    score = 0;
                    roundCorrect = 0;
                    roundWrong = 0;
                } else {
                    roundCorrect = 0;
                    roundWrong = 0;

                    if (level < 1000)
                        level++;
                }

                newPuzzle();
            });
        }

        void btn(Canvas c, RectF q, String s, int color, float size) {
            p.setColor(color);
            c.drawRoundRect(q, 18, 18, p);

            p.setColor(Color.WHITE);
            p.setTextSize(size);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            Paint.FontMetrics f = p.getFontMetrics();

            c.drawText(
                    s,
                    q.centerX() - p.measureText(s) / 2,
                    q.centerY() - (f.ascent + f.descent) / 2,
                    p
            );
        }

        void text(Canvas c, String s, float x, float y,
                  float size, int color, boolean bold) {
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(
                    bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT
            );
            c.drawText(s, x, y, p);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            c.drawColor(Color.rgb(15, 19, 28));

            int w = getWidth();
            int h = getHeight();

            float pad = Math.max(14, w * 0.035f);
            float gap = Math.max(7, w * 0.015f);

            /*
             * FINAL RESPONSIVE LAYOUT
             * वर पुरेसा स्पेस + मोठे शीर्षक + मोठा प्रश्न.
             */

            float topSpace = Math.max(38, h * 0.035f);

            // ---------------- HEADER ----------------
            float titleSize = Math.max(24, w * 0.060f);

            text(
                    c,
                    "JD NUMBER PUZZLE",
                    pad,
                    topSpace + titleSize,
                    titleSize,
                    Color.WHITE,
                    true
            );

            float subSize = Math.max(13, w * 0.030f);

            text(
                    c,
                    "Level " + level + " / 1000 • छोटे नंबर • No Timer",
                    pad,
                    topSpace + titleSize + subSize + 2,
                    subSize,
                    Color.LTGRAY,
                    false
            );

            p.setColor(Color.rgb(255, 214, 73));
            p.setTextSize(Math.max(16, w * 0.035f));
            p.setTypeface(Typeface.DEFAULT_BOLD);

            String sc = "★ " + score + " गुण";

            c.drawText(
                    sc,
                    w - p.measureText(sc) - pad,
                    topSpace + titleSize,
                    p
            );

            // ---------------- QUESTION ----------------
            float headerBottom =
                    topSpace + titleSize + subSize + 12;

            float qTop = headerBottom + Math.max(18, h * 0.018f);

            float qHeight = Math.max(125, h * 0.135f);

            p.setColor(Color.rgb(28, 34, 48));
            c.drawRoundRect(
                    pad,
                    qTop,
                    w - pad,
                    qTop + qHeight,
                    18,
                    18,
                    p
            );

            text(
                    c,
                    "🧮  प्रश्न",
                    pad + 16,
                    qTop + 30,
                    Math.max(18, w * 0.040f),
                    Color.rgb(255, 214, 73),
                    true
            );

            // मोठा प्रश्न
            float qTextSize = Math.max(17, w * 0.039f);

            p.setTextSize(qTextSize);
            p.setTypeface(Typeface.DEFAULT);

            float maxWidth = w - 2 * pad - 32;

            // प्रश्न 2 ओळींत व्यवस्थित बसवणे
            ArrayList<String> lines = new ArrayList<>();
            String[] words = question.split(" ");
            String line = "";

            for (String word : words) {
                String test = line.isEmpty()
                        ? word
                        : line + " " + word;

                if (p.measureText(test) <= maxWidth) {
                    line = test;
                } else {
                    if (!line.isEmpty())
                        lines.add(line);
                    line = word;
                }
            }

            if (!line.isEmpty())
                lines.add(line);

            float qY = qTop + 68;
            float lineHeight = qTextSize + 8;

            for (int i = 0; i < lines.size() && i < 2; i++) {
                text(
                        c,
                        lines.get(i),
                        pad + 16,
                        qY + i * lineHeight,
                        qTextSize,
                        Color.WHITE,
                        false
                );
            }

            // ---------------- NUMBER BOXES ----------------
            float boxTop = qTop + qHeight + Math.max(18, h * 0.018f);

            // स्क्रीनवर थोडे मोठे 5 x 2 बॉक्स
            float boxAreaHeight = Math.min(
                    h * 0.38f,
                    560
            );

            float bh = (boxAreaHeight - gap) / 2f;
            float bw = (w - 2 * pad - 4 * gap) / 5f;

            for (int i = 0; i < 10; i++) {

                int row = i / 5;
                int col = i % 5;

                float x = pad + col * (bw + gap);
                float y = boxTop + row * (bh + gap);

                boxes[i] = new RectF(
                        x,
                        y,
                        x + bw,
                        y + bh
                );

                if (i == a || i == b) {
                    p.setColor(Color.rgb(55, 115, 200));
                } else {
                    p.setColor(Color.rgb(43, 51, 66));
                }

                c.drawRoundRect(
                        boxes[i],
                        16,
                        16,
                        p
                );

                String s = String.valueOf(nums.get(i));

                float numSize;

                if (s.length() == 1)
                    numSize = Math.max(29, bw * 0.30f);
                else
                    numSize = Math.max(25, bw * 0.25f);

                p.setColor(Color.WHITE);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(numSize);

                c.drawText(
                        s,
                        boxes[i].centerX()
                                - p.measureText(s) / 2,
                        boxes[i].centerY()
                                - (p.ascent() + p.descent()) / 2,
                        p
                );
            }

            // ---------------- OPERATION ----------------
            float operationY =
                    boxTop + boxAreaHeight + Math.max(22, h * 0.020f);

            String op;

            if (operation.equals("+"))
                op = "➕  अधिक";
            else if (operation.equals("−"))
                op = "➖  वजा";
            else if (operation.equals("×"))
                op = "✖  गुणाकार";
            else
                op = "➗  भागाकार";

            text(
                    c,
                    "क्रिया: " + op,
                    pad,
                    operationY,
                    Math.max(18, w * 0.040f),
                    Color.rgb(255, 214, 73),
                    true
            );

            // ---------------- BUTTONS ----------------
            float buttonTop =
                    operationY + Math.max(20, h * 0.022f);

            float buttonHeight =
                    Math.max(72, Math.min(94, h * 0.082f));

            float aw =
                    (w - 2 * pad - 2 * gap) / 3f;

            reset.set(
                    pad,
                    buttonTop,
                    pad + aw,
                    buttonTop + buttonHeight
            );

            check.set(
                    pad + aw + gap,
                    buttonTop,
                    pad + 2 * aw + gap,
                    buttonTop + buttonHeight
            );

            result.set(
                    pad + 2 * (aw + gap),
                    buttonTop,
                    w - pad,
                    buttonTop + buttonHeight
            );

            btn(
                    c,
                    reset,
                    "RESET",
                    Color.rgb(225, 60, 65),
                    Math.max(17, w * 0.040f)
            );

            btn(
                    c,
                    check,
                    "CHECK ✓",
                    Color.rgb(72, 205, 125),
                    Math.max(17, w * 0.040f)
            );

            btn(
                    c,
                    result,
                    "RESULT",
                    Color.rgb(105, 83, 210),
                    Math.max(17, w * 0.040f)
            );

            // ---------------- STATUS ----------------
            if (!status.isEmpty()) {

                text(
                        c,
                        status,
                        pad,
                        buttonTop + buttonHeight + 34,
                        Math.max(15, w * 0.033f),
                        Color.WHITE,
                        false
                );
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {

            if (e.getAction() != MotionEvent.ACTION_UP)
                return true;

            float x = e.getX();
            float y = e.getY();

            for (int i = 0; i < 10; i++) {

                if (boxes[i] != null &&
                        boxes[i].contains(x, y)) {

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

            if (reset.contains(x, y)) {
                a = -1;
                b = -1;
                status = "RESET केले.";
                invalidate();
                return true;
            }

            if (check.contains(x, y)) {
                checkAnswer();
                return true;
            }

            if (result.contains(x, y)) {
                showResult(false);
                return true;
            }

            return true;
        }
    }
}
