package com.jd.numberpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
        if (hasFocus) hideBars();
    }

    @Override
    protected void onDestroy() {
        if (game != null) {
            game.releaseSound();
        }
        super.onDestroy();
    }

    class GameView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random = new Random();

        ArrayList<Integer> numbers = new ArrayList<>();
        RectF[] numberBoxes = new RectF[4];

        RectF resetButton = new RectF();
        RectF checkButton = new RectF();
        RectF resultButton = new RectF();
        RectF hintButton = new RectF();

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

        boolean celebration = false;

        ArrayList<Particle> particles = new ArrayList<>();

        ToneGenerator tone;

        final int backgroundColor = Color.rgb(3, 7, 18);
        final int cardColor = Color.rgb(17, 23, 40);
        final int boxColor = Color.rgb(10, 25, 48);

        final int yellow = Color.rgb(255, 211, 35);
        final int goldLight = Color.rgb(255, 239, 155);
        final int blue = Color.rgb(35, 145, 255);
        final int white = Color.WHITE;
        final int green = Color.rgb(0, 220, 95);
        final int red = Color.rgb(235, 25, 45);
        final int purple = Color.rgb(115, 45, 230);

        GameView(Context context) {
            super(context);

            setFocusable(true);

            try {
                tone = new ToneGenerator(
                        AudioManager.STREAM_MUSIC,
                        85
                );
            } catch (Exception e) {
                tone = null;
            }

            newPuzzle();
        }

        void releaseSound() {
            if (tone != null) {
                tone.release();
                tone = null;
            }
        }

        // ------------------------------------------------------------
        // SOUND
        // ------------------------------------------------------------

        void beep() {
            if (tone == null) return;

            try {
                tone.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        120
                );
            } catch (Exception ignored) {
            }
        }

        void wrongSound() {
            if (tone == null) return;

            try {
                tone.startTone(
                        ToneGenerator.TONE_PROP_NACK,
                        180
                );
            } catch (Exception ignored) {
            }
        }

        void celebrationSound() {
            if (tone == null) return;

            try {
                tone.startTone(
                        ToneGenerator.TONE_PROP_ACK,
                        160
                );

                postDelayed(() -> {
                    if (tone != null) {
                        tone.startTone(
                                ToneGenerator.TONE_PROP_BEEP2,
                                150
                        );
                    }
                }, 180);

                postDelayed(() -> {
                    if (tone != null) {
                        tone.startTone(
                                ToneGenerator.TONE_PROP_ACK,
                                180
                        );
                    }
                }, 360);

                postDelayed(() -> {
                    if (tone != null) {
                        tone.startTone(
                                ToneGenerator.TONE_PROP_BEEP2,
                                220
                        );
                    }
                }, 560);

            } catch (Exception ignored) {
            }
        }

        // ------------------------------------------------------------
        // PUZZLE
        // ------------------------------------------------------------

        int smallNumber() {
            if (random.nextInt(100) < 25) {
                return 1 + random.nextInt(9);
            }

            return 10 + random.nextInt(90);
        }

        boolean containsNumber(int n) {
            for (int x : numbers) {
                if (x == n) return true;
            }

            return false;
        }

        void fillDecoys() {

            while (numbers.size() < 4) {

                int n = smallNumber();

                if (!containsNumber(n)) {
                    numbers.add(n);
                }
            }

            Collections.shuffle(numbers, random);
        }

        void newPuzzle() {

            celebration = false;
            particles.clear();

            numbers.clear();

            selected1 = -1;
            selected2 = -1;

            status = "";

            int type = random.nextInt(4);

            int x;
            int y;

            if (type == 0) {

                operation = "+";

                do {
                    x = smallNumber();
                    y = smallNumber();

                    target = x + y;

                } while (target < 10 || target > 150);

                question =
                        "कोणते दोन नंबर अधिक केल्यावर "
                                + target +
                                " मिळेल?";

            } else if (type == 1) {

                operation = "−";

                do {
                    x = smallNumber();
                    y = smallNumber();

                } while (x <= y);

                target = x - y;

                question =
                        "कोणत्या मोठ्या नंबरमधून कोणता नंबर "
                                + "वजा केल्यावर "
                                + target +
                                " मिळेल?";

            } else if (type == 2) {

                operation = "×";

                do {
                    x = 2 + random.nextInt(18);
                    y = 2 + random.nextInt(18);

                    target = x * y;

                } while (target < 20 || target > 300);

                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target +
                                " मिळेल?";

            } else {

                operation = "÷";

                do {

                    y = 2 + random.nextInt(8);
                    target = 2 + random.nextInt(10);

                    x = y * target;

                } while (x > 99);

                question =
                        "कोणता नंबर कोणत्या नंबरने "
                                + "भागल्यावर "
                                + target +
                                " मिळेल?";
            }

            numbers.add(x);
            numbers.add(y);

            fillDecoys();

            invalidate();
        }

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

            return y != 0 &&
                    x % y == 0 &&
                    x / y == target;
        }

        // ------------------------------------------------------------
        // CHECK
        // ------------------------------------------------------------

        void checkAnswer() {

            if (celebration) return;

            beep();

            if (selected1 < 0 || selected2 < 0) {

                status = "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first = numbers.get(selected1);
            int second = numbers.get(selected2);

            if (isCorrect(first, second)) {

                score++;
                roundCorrect++;

                status = "✓ बरोबर! +1 गुण";

                startCelebration();

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                wrongSound();

                invalidate();
            }
        }

        // ------------------------------------------------------------
        // CELEBRATION
        // ------------------------------------------------------------

        void startCelebration() {

            celebration = true;

            status = "";

            createFireShower();

            celebrationSound();

            invalidate();

            postDelayed(() -> {

                if (level < 1000) {
                    level++;
                    newPuzzle();
                } else {
                    celebration = false;
                    showFinalResult();
                }

            }, 2200);
        }

        void createFireShower() {

            particles.clear();

            int w = getWidth();

            for (int i = 0; i < 120; i++) {

                Particle particle = new Particle();

                particle.x =
                        w * .10f +
                                random.nextFloat() *
                                        w * .80f;

                particle.y =
                        -random.nextFloat() *
                                getHeight() * .20f;

                particle.vx =
                        (random.nextFloat() - .5f) * 5f;

                particle.vy =
                        5f +
                                random.nextFloat() * 9f;

                particle.size =
                        3f +
                                random.nextFloat() * 7f;

                particle.life =
                        80 +
                                random.nextInt(80);

                int[] colors = {
                        Color.YELLOW,
                        Color.rgb(255, 150, 0),
                        Color.RED,
                        Color.WHITE,
                        Color.rgb(255, 220, 40)
                };

                particle.color =
                        colors[random.nextInt(colors.length)];

                particles.add(particle);
            }
        }

        void updateParticles() {

            for (int i = particles.size() - 1; i >= 0; i--) {

                Particle particle = particles.get(i);

                particle.x += particle.vx;
                particle.y += particle.vy;

                particle.vy += .12f;

                particle.life--;

                if (particle.life <= 0 ||
                        particle.y > getHeight()) {

                    particles.remove(i);
                }
            }
        }

        void drawFireShower(Canvas c) {

            if (!celebration) return;

            updateParticles();

            p.setStyle(Paint.Style.FILL);

            for (Particle particle : particles) {

                p.setColor(particle.color);

                c.drawCircle(
                        particle.x,
                        particle.y,
                        particle.size,
                        p
                );
            }

            if (celebration) {
                postInvalidateDelayed(30);
            }
        }

        // ------------------------------------------------------------
        // RESULT
        // ------------------------------------------------------------

        void showFinalResult() {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("🏆 JD NUMBER PUZZLE पूर्ण!")
                    .setMessage(
                            "Level : " + level +
                                    "\n\n✓ बरोबर : " +
                                    roundCorrect +
                                    "\n✗ चुकले : " +
                                    roundWrong +
                                    "\n\n⭐ एकूण गुण : " +
                                    score
                    )
                    .setPositiveButton(
                            "पुन्हा खेळा",
                            (dialog, which) -> {

                                level = 1;
                                score = 0;
                                roundCorrect = 0;
                                roundWrong = 0;

                                newPuzzle();
                            }
                    )
                    .setNegativeButton(
                            "ठीक आहे",
                            null
                    )
                    .show();
        }

        void showResult() {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("RESULT")
                    .setMessage(
                            "Level : " + level +
                                    "\n\n⭐ गुण : " + score +
                                    "\n✓ बरोबर : " +
                                    roundCorrect +
                                    "\n✗ चुकले : " +
                                    roundWrong
                    )
                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )
                    .show();
        }

        void showHint() {

            String hint;

            if (operation.equals("+")) {

                hint =
                        "सूचना: " +
                                target +
                                " होण्यासाठी दोन नंबरची बेरीज करा.";

            } else if (operation.equals("−")) {

                hint =
                        "सूचना: मोठ्या नंबरमधून छोटा नंबर वजा करा.";

            } else if (operation.equals("×")) {

                hint =
                        "सूचना: " +
                                target +
                                " चे दोन गुणक शोधा.";

            } else {

                hint =
                        "सूचना: भागाकार केल्यावर उत्तर " +
                                target +
                                " आले पाहिजे.";
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("💡 HINT")
                    .setMessage(hint)
                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )
                    .show();
        }

        // ------------------------------------------------------------
        // RESET
        // ------------------------------------------------------------

        void resetGame() {

            beep();

            selected1 = -1;
            selected2 = -1;

            status = "";

            newPuzzle();
        }

        // ------------------------------------------------------------
        // DRAW HELPERS
        // ------------------------------------------------------------

        void drawText(
                Canvas c,
                String text,
                float x,
                float y,
                float size,
                int color,
                boolean bold
        ) {

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            c.drawText(text, x, y, p);
        }

        void centerText(
                Canvas c,
                String text,
                RectF r,
                float size,
                int color
        ) {

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float x =
                    r.centerX() -
                            p.measureText(text) / 2f;

            float y =
                    r.centerY() -
                            (fm.ascent + fm.descent) / 2f;

            c.drawText(text, x, y, p);
        }

        void roundedRect(
                Canvas c,
                RectF r,
                int color,
                float radius
        ) {

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            c.drawRoundRect(
                    r,
                    radius,
                    radius,
                    p
            );
        }

        void border(
                Canvas c,
                RectF r,
                int color,
                float width,
                float radius
        ) {

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);
            p.setColor(color);

            c.drawRoundRect(
                    r,
                    radius,
                    radius,
                    p
            );

            p.setStyle(Paint.Style.FILL);
        }

        // ------------------------------------------------------------
        // 3D BUTTON
        // ------------------------------------------------------------

        void draw3DButton(
                Canvas c,
                RectF r,
                String text,
                int color,
                float textSize
        ) {

            float shadow = Math.max(6, r.height() * .12f);

            RectF shadowRect =
                    new RectF(
                            r.left,
                            r.top + shadow,
                            r.right,
                            r.bottom + shadow
                    );

            roundedRect(
                    c,
                    shadowRect,
                    Color.rgb(0, 0, 0),
                    18
            );

            LinearGradient gradient =
                    new LinearGradient(
                            r.left,
                            r.top,
                            r.left,
                            r.bottom,
                            lighten(color),
                            color,
                            Shader.TileMode.CLAMP
                    );

            p.setShader(gradient);
            p.setStyle(Paint.Style.FILL);

            c.drawRoundRect(
                    r,
                    18,
                    18,
                    p
            );

            p.setShader(null);

            border(
                    c,
                    r,
                    yellow,
                    2.2f,
                    18
            );

            // top highlight
            p.setColor(
                    Color.argb(
                            100,
                            255,
                            255,
                            255
                    )
            );

            p.setStrokeWidth(2);

            c.drawLine(
                    r.left + 14,
                    r.top + 7,
                    r.right - 14,
                    r.top + 7,
                    p
            );

            centerText(
                    c,
                    text,
                    r,
                    textSize,
                    white
            );
        }

        int lighten(int color) {

            int redC =
                    Math.min(
                            255,
                            Color.red(color) + 55
                    );

            int greenC =
                    Math.min(
                            255,
                            Color.green(color) + 55
                    );

            int blueC =
                    Math.min(
                            255,
                            Color.blue(color) + 55
                    );

            return Color.rgb(
                    redC,
                    greenC,
                    blueC
            );
        }

        // ------------------------------------------------------------
        // CROWN
        // ------------------------------------------------------------

        void drawCrownLogo(
                Canvas c,
                float cx,
                float cy,
                float size
        ) {

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(255, 193, 20));

            Path crown = new Path();

            crown.moveTo(
                    cx - size * .52f,
                    cy + size * .20f
            );

            crown.lineTo(
                    cx - size * .70f,
                    cy - size * .42f
            );

            crown.lineTo(
                    cx - size * .22f,
                    cy - size * .12f
            );

            crown.lineTo(
                    cx,
                    cy - size * .58f
            );

            crown.lineTo(
                    cx + size * .22f,
                    cy - size * .12f
            );

            crown.lineTo(
                    cx + size * .70f,
                    cy - size * .42f
            );

            crown.lineTo(
                    cx + size * .52f,
                    cy + size * .20f
            );

            crown.close();

            c.drawPath(crown, p);

            border(
                    c,
                    new RectF(
                            cx - size * .55f,
                            cy + size * .10f,
                            cx + size * .55f,
                            cy + size * .36f
                    ),
                    goldLight,
                    3,
                    8
            );
        }

        // ------------------------------------------------------------
        // BACKGROUND
        // ------------------------------------------------------------

        void drawBackground(
                Canvas c,
                int w,
                int h
        ) {

            c.drawColor(backgroundColor);

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);

            p.setColor(
                    Color.rgb(8, 17, 36)
            );

            c.drawCircle(
                    w * .05f,
                    h * .15f,
                    w * .28f,
                    p
            );

            c.drawCircle(
                    w * .96f,
                    h * .65f,
                    w * .30f,
                    p
            );

            c.drawCircle(
                    w * .85f,
                    h * .08f,
                    w * .17f,
                    p
            );
        }

        // ------------------------------------------------------------
        // GAME SCREEN
        // ------------------------------------------------------------

        @Override
        protected void onDraw(Canvas c) {

            super.onDraw(c);

            int w = getWidth();
            int h = getHeight();

            drawBackground(c, w, h);

            float side =
                    Math.max(
                            18,
                            w * .035f
                    );

            // Crown
            float crownSize =
                    Math.min(
                            w * .16f,
                            82
                    );

            drawCrownLogo(
                    c,
                    w / 2f,
                    Math.max(45, h * .055f),
                    crownSize
            );

            // --------------------------------------------------------
            // TITLE
            // --------------------------------------------------------

            float titleSize =
                    Math.max(
                            29,
                            Math.min(
                                    43,
                                    w * .070f
                            )
                    );

            String title =
                    "JD NUMBER PUZZLE";

            p.setTextSize(titleSize);

            drawText(
                    c,
                    title,
                    (w - p.measureText(title)) / 2f,
                    h * .165f,
                    titleSize,
                    yellow,
                    true
            );

            // Level
            String levelText =
                    "Level " +
                            level +
                            " / 1000 • No Timer";

            float levelSize =
                    Math.max(
                            15,
                            w * .030f
                    );

            p.setTextSize(levelSize);

            drawText(
                    c,
                    levelText,
                    (w - p.measureText(levelText)) / 2f,
                    h * .205f,
                    levelSize,
                    Color.LTGRAY,
                    true
            );

            // --------------------------------------------------------
            // QUESTION CARD
            // --------------------------------------------------------

            float questionTop =
                    h * .235f;

            float questionHeight =
                    Math.max(
                            245,
                            Math.min(
                                    330,
                                    h * .285f
                            )
                    );

            RectF questionCard =
                    new RectF(
                            side,
                            questionTop,
                            w - side,
                            questionTop + questionHeight
                    );

            roundedRect(
                    c,
                    questionCard,
                    cardColor,
                    22
            );

            border(
                    c,
                    questionCard,
                    blue,
                    2.5f,
                    22
            );

            // प्रश्न title
            drawText(
                    c,
                    "🧮  प्रश्न",
                    side + 18,
                    questionTop + 43,
                    Math.max(
                            25,
                            w * .052f
                    ),
                    yellow,
                    true
            );

            // --------------------------------------------------------
            // BIG QUESTION FONT
            // --------------------------------------------------------

            float qSize =
                    Math.max(
                            23,
                            Math.min(
                                    32,
                                    w * .050f
                            )
                    );

            p.setTextSize(qSize);

            float maxWidth =
                    questionCard.width() - 36;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            for (String word :
                    question.split(" ")) {

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
                    questionTop + 92;

            for (int i = 0;
                 i < lines.size() && i < 3;
                 i++) {

                drawText(
                        c,
                        lines.get(i),
                        side + 18,
                        qY + i * (qSize + 10),
                        qSize,
                        white,
                        true
                );
            }

            // Operation
            drawText(
                    c,
                    "क्रिया: " +
                            operation +
                            "  " +
                            operationName(),
                    side + 18,
                    questionCard.bottom - 22,
                    Math.max(
                            20,
                            w * .042f
                    ),
                    yellow,
                    true
            );

            // --------------------------------------------------------
            // FOUR BIG NUMBER BOXES
            // --------------------------------------------------------

            float gridTop =
                    questionCard.bottom + h * .025f;

            float gap =
                    Math.max(
                            7,
                            w * .015f
                    );

            float gridWidth =
                    w - side * 2;

            float bw =
                    (gridWidth - gap * 3) / 4f;

            float bh =
                    Math.max(
                            120,
                            Math.min(
                                    190,
                                    h * .145f
                            )
                    );

            for (int i = 0; i < 4; i++) {

                float left =
                        side +
                                i * (bw + gap);

                float top =
                        gridTop;

                numberBoxes[i] =
                        new RectF(
                                left,
                                top,
                                left + bw,
                                top + bh
                        );

                boolean selected =
                        selected1 == i ||
                                selected2 == i;

                roundedRect(
                        c,
                        numberBoxes[i],
                        selected
                                ? Color.rgb(
                                25,
                                90,
                                165
                        )
                                : boxColor,
                        16
                );

                border(
                        c,
                        numberBoxes[i],
                        selected
                                ? yellow
                                : Color.rgb(
                                45,
                                110,
                                190
                        ),
                        selected ? 3 : 1.8f,
                        16
                );

                centerText(
                        c,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        numberBoxes[i],
                        Math.max(
                                25,
                                Math.min(
                                        38,
                                        bw * .34f
                                )
                        ),
                        white
                );
            }

            // --------------------------------------------------------
            // HINT BOX
            // --------------------------------------------------------

            float hintTop =
                    gridTop + bh + h * .025f;

            float hintHeight =
                    Math.max(
                            105,
                            Math.min(
                                    145,
                                    h * .105f
                            )
                    );

            hintButton.set(
                    side,
                    hintTop,
                    w - side,
                    hintTop + hintHeight
            );

            roundedRect(
                    c,
                    hintButton,
                    Color.rgb(11, 29, 55),
                    18
            );

            border(
                    c,
                    hintButton,
                    blue,
                    2.2f,
                    18
            );

            drawText(
                    c,
                    "💡",
                    side + 16,
                    hintTop + hintHeight * .58f,
                    Math.max(
                            28,
                            w * .055f
                    ),
                    yellow,
                    true
            );

            centerText(
                    c,
                    "सूचना : योग्य दोन नंबर निवडा",
                    new RectF(
                            side + 55,
                            hintTop,
                            w - side - 10,
                            hintTop + hintHeight
                    ),
                    Math.max(
                            16,
                            w * .032f
                    ),
                    white
            );

            // --------------------------------------------------------
            // THREE 3D BUTTONS
            // --------------------------------------------------------

            float buttonTop =
                    hintButton.bottom +
                            h * .035f;

            float buttonGap =
                    Math.max(
                            7,
                            w * .012f
                    );

            float buttonWidth =
                    (
                            w -
                                    side * 2 -
                                    buttonGap * 2
                    ) / 3f;

            float buttonHeight =
                    Math.max(
                            68,
                            Math.min(
                                    92,
                                    h * .070f
                            )
                    );

            resetButton.set(
                    side,
                    buttonTop,
                    side + buttonWidth,
                    buttonTop + buttonHeight
            );

            checkButton.set(
                    side + buttonWidth + buttonGap,
                    buttonTop,
                    side +
                            buttonWidth * 2 +
                            buttonGap,
                    buttonTop + buttonHeight
            );

            resultButton.set(
                    side +
                            buttonWidth * 2 +
                            buttonGap * 2,
                    buttonTop,
                    w - side,
                    buttonTop + buttonHeight
            );

            float buttonText =
                    Math.max(
                            16,
                            Math.min(
                                    23,
                                    w * .036f
                            )
                    );

            draw3DButton(
                    c,
                    resetButton,
                    "↻  RESET",
                    red,
                    buttonText
            );

            draw3DButton(
                    c,
                    checkButton,
                    "✓  CHECK",
                    green,
                    buttonText
            );

            draw3DButton(
                    c,
                    resultButton,
                    "▮  RESULT",
                    purple,
                    buttonText
            );

            // --------------------------------------------------------
            // STATUS
            // --------------------------------------------------------

            if (!status.isEmpty()) {

                RectF statusBox =
                        new RectF(
                                side,
                                buttonTop +
                                        buttonHeight +
                                        12,
                                w - side,
                                buttonTop +
                                        buttonHeight +
                                        52
                        );

                int statusColor =
                        status.startsWith("✓")
                                ? green
                                : status.startsWith("✗")
                                ? red
                                : yellow;

                centerText(
                        c,
                        status,
                        statusBox,
                        Math.max(
                                15,
                                w * .030f
                        ),
                        statusColor
                );
            }

            // --------------------------------------------------------
            // STYLISH FOOTER
            // --------------------------------------------------------

            String footer =
                    "✦  JD NUMBER PUZZLE  •  ROYAL EDITION  ✦";

            float footerSize =
                    Math.max(
                            12,
                            Math.min(
                                    17,
                                    w * .026f
                            )
                    );

            p.setTextSize(footerSize);

            drawText(
                    c,
                    footer,
                    (w - p.measureText(footer)) / 2f,
                    h * .965f,
                    footerSize,
                    goldLight,
                    true
            );

            // --------------------------------------------------------
            // CELEBRATION
            // --------------------------------------------------------

            drawFireShower(c);

            if (celebration) {
                drawCongratulations(c, w, h);
            }
        }

        // ------------------------------------------------------------
        // CONGRATULATIONS SCREEN
        // ------------------------------------------------------------

        void drawCongratulations(
                Canvas c,
                int w,
                int h
        ) {

            // Dark transparent overlay
            p.setStyle(Paint.Style.FILL);
            p.setColor(
                    Color.argb(
                            150,
                            0,
                            0,
                            0
                    )
            );

            c.drawRect(
                    0,
                    0,
                    w,
                    h,
                    p
            );

            float cardW =
                    w * .86f;

            float cardH =
                    Math.min(
                            300,
                            h * .30f
                    );

            RectF card =
                    new RectF(
                            (w - cardW) / 2f,
                            h * .32f,
                            (w + cardW) / 2f,
                            h * .32f + cardH
                    );

            // Shadow
            RectF shadow =
                    new RectF(
                            card.left,
                            card.top + 10,
                            card.right,
                            card.bottom + 10
                    );

            roundedRect(
                    c,
                    shadow,
                    Color.rgb(
                            0,
                            0,
                            0
                    ),
                    25
            );

            roundedRect(
                    c,
                    card,
                    Color.rgb(
                            15,
                            27,
                            52
                    ),
                    25
            );

            border(
                    c,
                    card,
                    yellow,
                    3.5f,
                    25
            );

            centerText(
                    c,
                    "🎉",
                    new RectF(
                            card.left,
                            card.top + 15,
                            card.right,
                            card.top + 85
                    ),
                    48,
                    yellow
            );

            centerText(
                    c,
                    "CONGRATULATIONS!",
                    new RectF(
                            card.left,
                            card.top + 75,
                            card.right,
                            card.top + 140
                    ),
                    Math.max(
                            24,
                            w * .055f
                    ),
                    yellow
            );

            centerText(
                    c,
                    "✓ योग्य उत्तर!",
                    new RectF(
                            card.left,
                            card.top + 135,
                            card.right,
                            card.top + 185
                    ),
                    Math.max(
                            19,
                            w * .040f
                    ),
                    green
            );

            centerText(
                    c,
                    "+1 गुण   •   Level " + level,
                    new RectF(
                            card.left,
                            card.top + 185,
                            card.right,
                            card.bottom - 15
                    ),
                    Math.max(
                            16,
                            w * .032f
                    ),
                    white
            );
        }

        String operationName() {

            if (operation.equals("+")) {
                return "बेरीज";
            }

            if (operation.equals("−")) {
                return "वजाबाकी";
            }

            if (operation.equals("×")) {
                return "गुणाकार";
            }

            return "भागाकार";
        }

        // ------------------------------------------------------------
        // TOUCH
        // ------------------------------------------------------------

        @Override
        public boolean onTouchEvent(
                MotionEvent event
        ) {

            if (event.getAction()
                    != MotionEvent.ACTION_UP) {

                return true;
            }

            if (celebration) {
                return true;
            }

            float x = event.getX();
            float y = event.getY();

            // Number boxes
            for (int i = 0; i < 4; i++) {

                if (numberBoxes[i] != null &&
                        numberBoxes[i].contains(x, y)) {

                    selectNumber(i);

                    return true;
                }
            }

            // Hint
            if (hintButton.contains(x, y)) {

                beep();

                showHint();

                return true;
            }

            // Reset
            if (resetButton.contains(x, y)) {

                resetGame();

                return true;
            }

            // Check
            if (checkButton.contains(x, y)) {

                checkAnswer();

                return true;
            }

            // Result
            if (resultButton.contains(x, y)) {

                beep();

                showResult();

                return true;
            }

            return true;
        }

        void selectNumber(int index) {

            beep();

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

    // ------------------------------------------------------------
    // FIRE PARTICLE CLASS
    // ------------------------------------------------------------

    class Particle {

        float x;
        float y;

        float vx;
        float vy;

        float size;

        int life;

        int color;
    }
}
