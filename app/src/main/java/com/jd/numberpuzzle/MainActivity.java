package com.jd.numberpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.*;
import java.util.*;

/**
 * JD NUMBER PUZZLE
 *
 * Updated features:
 * - Start permission
 * - Exit permission
 * - Background music
 * - Save / Continue
 * - Large centered question
 * - 3D number boxes
 * - Large number font
 * - Automatic result after every level
 * - Correct / Wrong / Total score below Hint
 * - Hint shows actual answer
 * - 1000 levels
 */
public class MainActivity extends Activity {

    GameView game;

    MediaPlayer musicPlayer;
    SharedPreferences prefs;

    boolean exitDialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "JD_NUMBER_PUZZLE_SAVE",
                MODE_PRIVATE
        );

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideBars();

        prepareMusic();

        game = new GameView(this);
        setContentView(game);

        // गेम सुरू करण्यापूर्वी परवानगी
        postStartPermission();
    }

    private void postStartPermission() {

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("JD NUMBER PUZZLE")
                .setMessage(
                        game.hasSavedProgress()
                                ? "तुमची गेम प्रगती सेव्ह आहे.\n\n" +
                                  "Level " + game.level +
                                  " पासून गेम सुरू करायचा आहे का?"
                                : "गेम सुरू करायचा आहे का?\n\n" +
                                  "गेम सुरू झाल्यावर संगीत वाजेल."
                )
                .setPositiveButton(
                        game.hasSavedProgress()
                                ? "CONTINUE"
                                : "होय, सुरू करा",
                        (dialog, which) -> {

                            if (game.hasSavedProgress()) {
                                game.continueGame();
                            } else {
                                game.startGame();
                            }

                            startMusic();
                        }
                )
                .setNegativeButton(
                        "नंतर",
                        (dialog, which) -> {
                            game.gameStarted = false;
                            game.invalidate();
                        }
                )
                .setOnCancelListener(
                        dialog -> {
                            game.gameStarted = false;
                            game.invalidate();
                        }
                )
                .show();
    }

    // ------------------------------------------------------------
    // MUSIC
    // ------------------------------------------------------------

    private void prepareMusic() {

        try {

            int musicId = getResources().getIdentifier(
                    "background_music",
                    "raw",
                    getPackageName()
            );

            if (musicId == 0) {
                musicPlayer = null;
                return;
            }

            musicPlayer = MediaPlayer.create(
                    this,
                    musicId
            );

            if (musicPlayer != null) {

                musicPlayer.setLooping(true);

                musicPlayer.setVolume(
                        0.55f,
                        0.55f
                );
            }

        } catch (Exception ignored) {

            musicPlayer = null;
        }
    }

    private void startMusic() {

        try {

            if (musicPlayer == null) {
                prepareMusic();
            }

            if (musicPlayer != null &&
                    !musicPlayer.isPlaying()) {

                musicPlayer.start();
            }

        } catch (Exception ignored) {
        }
    }

    private void pauseMusic() {

        try {

            if (musicPlayer != null &&
                    musicPlayer.isPlaying()) {

                musicPlayer.pause();
            }

        } catch (Exception ignored) {
        }
    }

    private void stopMusic() {

        try {

            if (musicPlayer != null) {

                if (musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                }

                musicPlayer.release();
                musicPlayer = null;
            }

        } catch (Exception ignored) {
        }
    }

    // ------------------------------------------------------------
    // SYSTEM UI
    // ------------------------------------------------------------

    private void hideBars() {

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(

                        View.SYSTEM_UI_FLAG_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |

                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    @Override
    public void onWindowFocusChanged(
            boolean hasFocus
    ) {

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideBars();
        }
    }

    // ------------------------------------------------------------
    // PAUSE / RESUME
    // ------------------------------------------------------------

    @Override
    protected void onPause() {

        super.onPause();

        if (game != null) {
            game.saveGame();
        }

        pauseMusic();
    }

    @Override
    protected void onResume() {

        super.onResume();

        hideBars();

        if (game != null &&
                game.gameStarted) {

            startMusic();
        }
    }

    // ------------------------------------------------------------
    // EXIT PERMISSION
    // ------------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (exitDialogShowing) {
            return;
        }

        exitDialogShowing = true;

        new AlertDialog.Builder(
                MainActivity.this
        )
                .setTitle("गेम बंद करायचा?")

                .setMessage(
                        "तुमची गेम प्रगती सेव्ह केली जाईल.\n\n" +
                        "गेम बंद करण्यासाठी परवानगी द्या."
                )

                .setPositiveButton(
                        "YES",
                        (dialog, which) -> {

                            if (game != null) {
                                game.saveGame();
                            }

                            stopMusic();

                            finish();
                        }
                )

                .setNegativeButton(
                        "NO",
                        (dialog, which) -> {

                            exitDialogShowing = false;
                        }
                )

                .setOnCancelListener(
                        dialog -> {
                            exitDialogShowing = false;
                        }
                )

                .show();
    }

    @Override
    protected void onDestroy() {

        stopMusic();

        if (game != null) {
            game.releaseSound();
        }

        super.onDestroy();
    }

    // ============================================================
    // GAME VIEW
    // ============================================================

    class GameView extends View {

        Paint p = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );

        Random random = new Random();

        ArrayList<Integer> numbers =
                new ArrayList<>();

        RectF[] numberBoxes =
                new RectF[4];

        RectF resetButton =
                new RectF();

        RectF checkButton =
                new RectF();

        RectF resultButton =
                new RectF();

        RectF hintButton =
                new RectF();

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

        boolean gameStarted = false;

        ArrayList<Particle> particles =
                new ArrayList<>();

        ToneGenerator tone;

        final int backgroundColor =
                Color.rgb(3, 7, 18);

        final int cardColor =
                Color.rgb(17, 23, 40);

        final int boxColor =
                Color.rgb(10, 25, 48);

        final int yellow =
                Color.rgb(255, 211, 35);

        final int goldLight =
                Color.rgb(255, 239, 155);

        final int blue =
                Color.rgb(35, 145, 255);

        final int white =
                Color.WHITE;

        final int green =
                Color.rgb(0, 220, 95);

        final int red =
                Color.rgb(235, 25, 45);

        final int purple =
                Color.rgb(115, 45, 230);

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

            loadSavedGame();

            if (numbers.size() != 4 ||
                    operation.isEmpty()) {

                newPuzzle();
            }
        }

        // ========================================================
        // SAVE / LOAD
        // ========================================================

        boolean hasSavedProgress() {

            if (prefs == null) {
                return false;
            }

            return prefs.getBoolean(
                    "gameStarted",
                    false
            );
        }

        void saveGame() {

            if (prefs == null) {
                return;
            }

            StringBuilder savedNumbers =
                    new StringBuilder();

            for (int i = 0;
                 i < numbers.size();
                 i++) {

                if (i > 0) {
                    savedNumbers.append(",");
                }

                savedNumbers.append(
                        numbers.get(i)
                );
            }

            prefs.edit()

                    .putBoolean(
                            "gameStarted",
                            gameStarted
                    )

                    .putInt(
                            "level",
                            level
                    )

                    .putInt(
                            "score",
                            score
                    )

                    .putInt(
                            "correct",
                            roundCorrect
                    )

                    .putInt(
                            "wrong",
                            roundWrong
                    )

                    .putInt(
                            "selected1",
                            selected1
                    )

                    .putInt(
                            "selected2",
                            selected2
                    )

                    .putInt(
                            "target",
                            target
                    )

                    .putString(
                            "operation",
                            operation
                    )

                    .putString(
                            "question",
                            question
                    )

                    .putString(
                            "status",
                            status
                    )

                    .putString(
                            "numbers",
                            savedNumbers.toString()
                    )

                    .apply();
        }

        boolean loadSavedGame() {

            if (prefs == null) {
                return false;
            }

            if (!prefs.getBoolean(
                    "gameStarted",
                    false
            )) {

                return false;
            }

            level = Math.max(
                    1,
                    Math.min(
                            1000,
                            prefs.getInt(
                                    "level",
                                    1
                            )
                    )
            );

            score = Math.max(
                    0,
                    prefs.getInt(
                            "score",
                            0
                    )
            );

            roundCorrect = Math.max(
                    0,
                    prefs.getInt(
                            "correct",
                            0
                    )
            );

            roundWrong = Math.max(
                    0,
                    prefs.getInt(
                            "wrong",
                            0
                    )
            );

            selected1 = prefs.getInt(
                    "selected1",
                    -1
            );

            selected2 = prefs.getInt(
                    "selected2",
                    -1
            );

            target = prefs.getInt(
                    "target",
                    0
            );

            operation = prefs.getString(
                    "operation",
                    ""
            );

            question = prefs.getString(
                    "question",
                    ""
            );

            status = prefs.getString(
                    "status",
                    ""
            );

            numbers.clear();

            String saved =
                    prefs.getString(
                            "numbers",
                            ""
                    );

            if (!saved.isEmpty()) {

                try {

                    String[] values =
                            saved.split(",");

                    for (String value :
                            values) {

                        numbers.add(
                                Integer.parseInt(
                                        value
                                )
                        );
                    }

                } catch (Exception ignored) {

                    numbers.clear();
                }
            }

            gameStarted = true;

            return true;
        }

        // ========================================================
        // START / CONTINUE
        // ========================================================

        void startGame() {

            gameStarted = true;

            level = 1;

            score = 0;

            roundCorrect = 0;

            roundWrong = 0;

            selected1 = -1;

            selected2 = -1;

            newPuzzle();

            saveGame();

            startMusic();

            invalidate();
        }

        void continueGame() {

            gameStarted = true;

            if (!loadSavedGame()) {

                level = 1;

                score = 0;

                roundCorrect = 0;

                roundWrong = 0;

                newPuzzle();
            }

            startMusic();

            invalidate();
        }

        // ========================================================
        // SOUND
        // ========================================================

        void releaseSound() {

            if (tone != null) {

                tone.release();

                tone = null;
            }
        }

        void beep() {

            if (tone == null) {
                return;
            }

            try {

                tone.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        120
                );

            } catch (Exception ignored) {
            }
        }

        void wrongSound() {

            if (tone == null) {
                return;
            }

            try {

                tone.startTone(
                        ToneGenerator.TONE_PROP_NACK,
                        180
                );

            } catch (Exception ignored) {
            }
        }

        void celebrationSound() {

            if (tone == null) {
                return;
            }

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

        // ========================================================
        // PUZZLE
        // ========================================================

        int smallNumber() {

            if (random.nextInt(100) < 25) {

                return 1 +
                        random.nextInt(9);
            }

            return 10 +
                    random.nextInt(90);
        }

        boolean containsNumber(int n) {

            for (int x : numbers) {

                if (x == n) {
                    return true;
                }
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

            Collections.shuffle(
                    numbers,
                    random
            );
        }

        void newPuzzle() {

            celebration = false;

            particles.clear();

            numbers.clear();

            selected1 = -1;

            selected2 = -1;

            status = "";

            int type =
                    random.nextInt(4);

            int x;

            int y;

            // ----------------------------------------------------
            // ADDITION
            // ----------------------------------------------------

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

            // ----------------------------------------------------
            // SUBTRACTION
            // ----------------------------------------------------

            else if (type == 1) {

                operation = "−";

                do {

                    x = smallNumber();

                    y = smallNumber();

                } while (x <= y);

                target = x - y;

                question =
                        "कोणत्या मोठ्या नंबरमधून "
                                + "कोणता नंबर वजा केल्यावर "
                                + target
                                + " मिळेल?";

            }

            // ----------------------------------------------------
            // MULTIPLICATION
            // ----------------------------------------------------

            else if (type == 2) {

                operation = "×";

                do {

                    x =
                            2 +
                            random.nextInt(18);

                    y =
                            2 +
                            random.nextInt(18);

                    target = x * y;

                } while (
                        target < 20 ||
                        target > 300
                );

                question =
                        "कोणते दोन नंबर गुणिले असता "
                                + target
                                + " मिळेल?";

            }

            // ----------------------------------------------------
            // DIVISION
            // ----------------------------------------------------

            else {

                operation = "÷";

                do {

                    y =
                            2 +
                            random.nextInt(8);

                    target =
                            2 +
                            random.nextInt(10);

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

            fillDecoys();

            saveGame();

            invalidate();
        }

        boolean isCorrect(
                int x,
                int y
        ) {

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
        // CHECK ANSWER
        // ========================================================

        void checkAnswer() {

            if (celebration) {
                return;
            }

            if (!gameStarted) {
                return;
            }

            beep();

            if (selected1 < 0 ||
                    selected2 < 0) {

                status =
                        "कृपया दोन नंबर निवडा.";

                invalidate();

                return;
            }

            int first =
                    numbers.get(selected1);

            int second =
                    numbers.get(selected2);

            if (isCorrect(
                    first,
                    second
            )) {

                score++;

                roundCorrect++;

                status =
                        "✓ बरोबर! +1 गुण";

                saveGame();

                startCelebration();

            } else {

                roundWrong++;

                status =
                        "✗ उत्तर चुकले. पुन्हा प्रयत्न करा.";

                wrongSound();

                saveGame();

                invalidate();
            }
        }

        // ========================================================
        // CELEBRATION
        // ========================================================

        void startCelebration() {

            celebration = true;

            status = "";

            createFireShower();

            celebrationSound();

            saveGame();

            invalidate();

            // Celebration नंतर RESULT दाखवणे
            postDelayed(() -> {

                celebration = false;

                showResult(
                        level >= 1000
                );

            }, 2200);
        }

        void createFireShower() {

            particles.clear();

            int w = getWidth();

            for (int i = 0;
                 i < 120;
                 i++) {

                Particle particle =
                        new Particle();

                particle.x =
                        w * .10f +
                        random.nextFloat()
                                * w * .80f;

                particle.y =
                        -random.nextFloat()
                                * getHeight()
                                * .20f;

                particle.vx =
                        (random.nextFloat()
                                - .5f) * 5f;

                particle.vy =
                        5f +
                        random.nextFloat()
                                * 9f;

                particle.size =
                        3f +
                        random.nextFloat()
                                * 7f;

                particle.life =
                        80 +
                        random.nextInt(80);

                int[] colors = {

                        Color.YELLOW,

                        Color.rgb(
                                255,
                                150,
                                0
                        ),

                        Color.RED,

                        Color.WHITE,

                        Color.rgb(
                                255,
                                220,
                                40
                        )
                };

                particle.color =
                        colors[
                                random.nextInt(
                                        colors.length
                                )
                        ];

                particles.add(
                        particle
                );
            }
        }

        void updateParticles() {

            for (
                    int i =
                            particles.size() - 1;
                    i >= 0;
                    i--
            ) {

                Particle particle =
                        particles.get(i);

                particle.x +=
                        particle.vx;

                particle.y +=
                        particle.vy;

                particle.vy +=
                        .12f;

                particle.life--;

                if (
                        particle.life <= 0 ||
                        particle.y > getHeight()
                ) {

                    particles.remove(i);
                }
            }
        }

        void drawFireShower(
                Canvas c
        ) {

            if (!celebration) {
                return;
            }

            updateParticles();

            p.setStyle(
                    Paint.Style.FILL
            );

            for (
                    Particle particle :
                    particles
            ) {

                p.setColor(
                        particle.color
                );

                c.drawCircle(
                        particle.x,
                        particle.y,
                        particle.size,
                        p
                );
            }

            if (celebration) {

                postInvalidateDelayed(
                        30
                );
            }
        }

        // ========================================================
        // RESULT
        // ========================================================

        void showResult(
                boolean finalGame
        ) {

            if (finalGame) {

                new AlertDialog.Builder(
                        MainActivity.this
                )

                        .setTitle(
                                "🏆 JD NUMBER PUZZLE पूर्ण!"
                        )

                        .setMessage(
                                "Level : " +
                                        level +

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

                                    saveGame();

                                }
                        )

                        .setNegativeButton(
                                "ठीक आहे",
                                null
                        )

                        .show();

                return;
            }

            new AlertDialog.Builder(
                    MainActivity.this
            )

                    .setTitle(
                            "RESULT • Level " +
                                    level
                    )

                    .setMessage(
                            "✓ बरोबर : " +
                                    roundCorrect +

                                    "\n✗ चुकीचे : " +
                                    roundWrong +

                                    "\n⭐ एकूण गुण : " +
                                    score +

                                    "\n\nLevel " +
                                    level +
                                    " पूर्ण झाले!"
                    )

                    .setPositiveButton(
                            "पुढील LEVEL",
                            (dialog, which) -> {

                                level++;

                                newPuzzle();

                                saveGame();

                                invalidate();
                            }
                    )

                    .setNegativeButton(
                            "ठीक आहे",
                            null
                    )

                    .show();
        }

        // ========================================================
        // HINT
        // ========================================================

        void showHint() {

            int answerA = -1;

            int answerB = -1;

            for (
                    int i = 0;
                    i < numbers.size();
                    i++
            ) {

                for (
                        int j = i + 1;
                        j < numbers.size();
                        j++
                ) {

                    if (
                            isCorrect(
                                    numbers.get(i),
                                    numbers.get(j)
                            )
                    ) {

                        answerA =
                                numbers.get(i);

                        answerB =
                                numbers.get(j);

                        break;
                    }
                }

                if (answerA != -1) {
                    break;
                }
            }

            String answer;

            if (answerA != -1) {

                answer =
                        "उत्तर : " +
                                answerA +
                                " " +
                                operation +
                                " " +
                                answerB +
                                " = " +
                                target;

                status =
                        "💡 " + answer;

            } else {

                answer =
                        "योग्य उत्तर सापडले नाही.";

                status =
                        "💡 " + answer;
            }

            saveGame();

            invalidate();

            new AlertDialog.Builder(
                    MainActivity.this
            )

                    .setTitle(
                            "💡 HINT • उत्तर"
                    )

                    .setMessage(
                            answer +
                                    "\n\n" +
                                    "हे उत्तर पाहिल्यानंतर " +
                                    "तुम्ही CHECK करू शकता."
                    )

                    .setPositiveButton(
                            "ठीक आहे",
                            null
                    )

                    .show();
        }

        // ========================================================
        // RESET
        // ========================================================

        void resetGame() {

            beep();

            selected1 = -1;

            selected2 = -1;

            status = "";

            newPuzzle();

            saveGame();
        }

        // ========================================================
        // DRAW HELPERS
        // ========================================================

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

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(
                    bold
                            ? Typeface.DEFAULT_BOLD
                            : Typeface.DEFAULT
            );

            c.drawText(
                    text,
                    x,
                    y,
                    p
            );
        }

        void centerText(
                Canvas c,
                String text,
                RectF r,
                float size,
                int color
        ) {

            p.setShader(null);

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(color);

            p.setTextSize(size);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            Paint.FontMetrics fm =
                    p.getFontMetrics();

            float x =
                    r.centerX()
                            - p.measureText(text)
                            / 2f;

            float y =
                    r.centerY()
                            - (
                            fm.ascent
                                    + fm.descent
                    ) / 2f;

            c.drawText(
                    text,
                    x,
                    y,
                    p
            );
        }

        void roundedRect(
                Canvas c,
                RectF r,
                int color,
                float radius
        ) {

            p.setShader(null);

            p.setStyle(
                    Paint.Style.FILL
            );

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

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(width);

            p.setColor(color);

            c.drawRoundRect(
                    r,
                    radius,
                    radius,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );
        }

        // ========================================================
        // 3D BUTTON
        // ========================================================

        void draw3DButton(
                Canvas c,
                RectF r,
                String text,
                int color,
                float textSize
        ) {

            float shadow =
                    Math.max(
                            6,
                            r.height() * .12f
                    );

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
                    Color.rgb(
                            0,
                            0,
                            0
                    ),
                    14
            );

            roundedRect(
                    c,
                    r,
                    color,
                    14
            );

            // 3D top highlight
            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(2);

            p.setColor(
                    Color.argb(
                            150,
                            255,
                            255,
                            255
                    )
            );

            c.drawLine(
                    r.left + 10,
                    r.top + 4,
                    r.right - 10,
                    r.top + 4,
                    p
            );

            border(
                    c,
                    r,
                    yellow,
                    2,
                    14
            );

            centerText(
                    c,
                    text,
                    r,
                    textSize,
                    white
            );
        }

        // ========================================================
        // ROYAL BACKGROUND
        // ========================================================

        void drawBackground(
                Canvas c,
                int w,
                int h
        ) {

            c.drawColor(
                    backgroundColor
            );

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(
                    Color.rgb(
                            8,
                            17,
                            36
                    )
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

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setStrokeWidth(1.2f);

            p.setColor(
                    Color.rgb(
                            24,
                            39,
                            70
                    )
            );

            for (
                    int i = 0;
                    i < 7;
                    i++
            ) {

                float y =
                        h * .20f +
                                i * h * .11f;

                c.drawLine(
                        w * .04f,
                        y,
                        w * .96f,
                        y,
                        p
                );
            }

            p.setStyle(
                    Paint.Style.FILL
            );
        }

        // ========================================================
        // CROWN / JD
        // ========================================================

        void drawCrownLogo(
                Canvas c,
                float cx,
                float cy,
                float size
        ) {

            p.setStyle(
                    Paint.Style.FILL
            );

            p.setColor(
                    Color.rgb(
                            255,
                            193,
                            20
                    )
            );

            Path crown =
                    new Path();

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

            c.drawPath(
                    crown,
                    p
            );

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

            drawText(
                    c,
                    "JD",
                    cx - size * .43f,
                    cy + size * .98f,
                    size * .68f,
                    yellow,
                    true
            );
        }

        // ========================================================
        // MAIN GAME DRAW
        // ========================================================

        void drawGame(
                Canvas c,
                int w,
                int h
        ) {

            drawBackground(
                    c,
                    w,
                    h
            );

            float side =
                    Math.max(
                            16,
                            w * .035f
                    );

            // ----------------------------------------------------
            // JD LOGO
            // ----------------------------------------------------

            drawCrownLogo(
                    c,
                    w / 2f,
                    35,
                    Math.min(
                            w * .12f,
                            48
                    )
            );

            // ----------------------------------------------------
            // TITLE - मोठा
            // ----------------------------------------------------

            float titleSize =
                    Math.max(
                            36,
                            Math.min(
                                    56,
                                    w * .088f
                            )
                    );

            String title =
                    "JD NUMBER PUZZLE";

            p.setTextSize(
                    titleSize
            );

            drawText(
                    c,
                    title,
                    (w -
                            p.measureText(title))
                            / 2f,
                    105,
                    titleSize,
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // LEVEL
            // ----------------------------------------------------

            String levelText =
                    "Level " +
                            level +
                            " / 1000";

            if (level >= 401) {

                levelText +=
                        "  •  🔥 HARD MODE";

            } else {

                levelText +=
                        "  •  No Timer";
            }

            float levelSize =
                    Math.max(
                            15,
                            w * .032f
                    );

            p.setTextSize(
                    levelSize
            );

            drawText(
                    c,
                    levelText,
                    (w -
                            p.measureText(
                                    levelText
                            )) / 2f,
                    135,
                    levelSize,
                    level >= 401
                            ? red
                            : Color.LTGRAY,
                    true
            );

            // ----------------------------------------------------
            // QUESTION CARD
            // ----------------------------------------------------

            float questionTop =
                    150;

            float questionHeight =
                    155;

            RectF card =
                    new RectF(
                            side,
                            questionTop,
                            w - side,
                            questionTop +
                                    questionHeight
                    );

            roundedRect(
                    c,
                    card,
                    cardColor,
                    22
            );

            border(
                    c,
                    card,
                    blue,
                    2.2f,
                    22
            );

            drawText(
                    c,
                    "🧮  प्रश्न",
                    side + 22,
                    questionTop + 38,
                    Math.max(
                            23,
                            w * .052f
                    ),
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // QUESTION FONT मोठा + CENTER
            // ----------------------------------------------------

            float qSize =
                    Math.max(
                            25,
                            Math.min(
                                    38,
                                    w * .065f
                            )
                    );

            p.setTextSize(
                    qSize
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            float maxWidth =
                    card.width() - 40;

            ArrayList<String> lines =
                    new ArrayList<>();

            String current = "";

            for (
                    String word :
                    question.split(" ")
            ) {

                String test =
                        current.isEmpty()
                                ? word
                                : current +
                                  " " +
                                  word;

                if (
                        p.measureText(
                                test
                        ) <= maxWidth
                ) {

                    current = test;

                } else {

                    if (!current.isEmpty()) {
                        lines.add(
                                current
                        );
                    }

                    current = word;
                }
            }

            if (!current.isEmpty()) {
                lines.add(current);
            }

            float lineHeight =
                    qSize + 8;

            float totalHeight =
                    Math.min(
                            lines.size(),
                            2
                    ) * lineHeight;

            float firstY =
                    questionTop +
                    82 -
                    totalHeight / 2f +
                    qSize;

            for (
                    int i = 0;
                    i < lines.size() &&
                    i < 2;
                    i++
            ) {

                String line =
                        lines.get(i);

                p.setTextSize(
                        qSize
                );

                drawText(
                        c,
                        line,
                        (w -
                                p.measureText(
                                        line
                                )) / 2f,
                        firstY +
                                i *
                                lineHeight,
                        qSize,
                        white,
                        true
                );
            }

            drawText(
                    c,
                    "क्रिया: " +
                            operation +
                            "  " +
                            operationName(),
                    side + 22,
                    card.bottom - 17,
                    Math.max(
                            17,
                            w * .040f
                    ),
                    yellow,
                    true
            );

            // ----------------------------------------------------
            // NUMBER BOXES
            // ----------------------------------------------------

            int count = 4;

            float gridTop =
                    card.bottom + 18;

            float gap =
                    10;

            float gridWidth =
                    w -
                    side * 2;

            float bw =
                    (gridWidth -
                            gap * 3) /
                            4f;

            float bh =
                    Math.max(
                            95,
                            Math.min(
                                    130,
                                    h * .115f
                            )
                    );

            for (
                    int i = 0;
                    i < count;
                    i++
            ) {

                int col =
                        i % 4;

                float l =
                        side +
                        col *
                        (bw + gap);

                float t =
                        gridTop;

                numberBoxes[i] =
                        new RectF(
                                l,
                                t,
                                l + bw,
                                t + bh
                        );

                boolean selected =
                        selected1 == i ||
                        selected2 == i;

                // ------------------------------------------------
                // 3D SHADOW
                // ------------------------------------------------

                RectF shadow =
                        new RectF(
                                numberBoxes[i].left + 5,
                                numberBoxes[i].top + 9,
                                numberBoxes[i].right + 5,
                                numberBoxes[i].bottom + 9
                        );

                roundedRect(
                        c,
                        shadow,
                        Color.argb(
                                190,
                                0,
                                0,
                                0
                        ),
                        16
                );

                // ------------------------------------------------
                // MAIN BOX
                // ------------------------------------------------

                roundedRect(
                        c,
                        numberBoxes[i],
                        selected
                                ? Color.rgb(
                                        28,
                                        105,
                                        180
                                )
                                : boxColor,
                        16
                );

                // ------------------------------------------------
                // TOP 3D HIGHLIGHT
                // ------------------------------------------------

                p.setStyle(
                        Paint.Style.STROKE
                );

                p.setStrokeWidth(3);

                p.setColor(
                        Color.argb(
                                150,
                                255,
                                255,
                                255
                        )
                );

                c.drawLine(
                        numberBoxes[i].left + 12,
                        numberBoxes[i].top + 5,
                        numberBoxes[i].right - 12,
                        numberBoxes[i].top + 5,
                        p
                );

                // ------------------------------------------------
                // BORDER
                // ------------------------------------------------

                border(
                        c,
                        numberBoxes[i],
                        selected
                                ? yellow
                                : Color.rgb(
                                        45,
                                        120,
                                        205
                                ),
                        selected
                                ? 3.5f
                                : 2f,
                        16
                );

                // ------------------------------------------------
                // LARGE NUMBER
                // ------------------------------------------------

                float numberSize =
                        Math.max(
                                30,
                                Math.min(
                                        48,
                                        bw * .42f
                                )
                        );

                centerText(
                        c,
                        String.valueOf(
                                numbers.get(i)
                        ),
                        numberBoxes[i],
                        numberSize,
                        white
                );
            }

            // ----------------------------------------------------
            // HINT
            // ----------------------------------------------------

            float hintTop =
                    gridTop +
                    bh +
                    18;

            float hintHeight =
                    Math.max(
                            60,
                            Math.min(
                                    74,
                                    h * .068f
                            )
                    );

            hintButton.set(
                    side,
                    hintTop,
                    w - side,
                    hintTop +
                            hintHeight
            );

            draw3DButton(
                    c,
                    hintButton,
                    "💡 HINT • उत्तर पाहण्यासाठी",
                    blue,
                    Math.max(
                            17,
                            w * .034f
                    )
            );

            // ----------------------------------------------------
            // STATS - HINT च्या खाली
            // ----------------------------------------------------

            float statTop =
                    hintButton.bottom +
                    10;

            float statHeight =
                    70;

            RectF statCard =
                    new RectF(
                            side,
                            statTop,
                            w - side,
                            statTop +
                                    statHeight
                    );

            roundedRect(
                    c,
                    statCard,
                    Color.rgb(
                            10,
                            22,
                            43
                    ),
                    15
            );

            border(
                    c,
                    statCard,
                    yellow,
                    1.8f,
                    15
            );

            float sw =
                    statCard.width() /
                            3f;

            String[] labels = {

                    "✓ बरोबर",

                    "✗ चुकीचे",

                    "⭐ एकूण गुण"
            };

            String[] values = {

                    String.valueOf(
                            roundCorrect
                    ),

                    String.valueOf(
                            roundWrong
                    ),

                    String.valueOf(
                            score
                    )
            };

            int[] statColors = {

                    green,

                    red,

                    yellow
            };

            for (
                    int i = 0;
                    i < 3;
                    i++
            ) {

                if (i > 0) {

                    p.setColor(
                            Color.rgb(
                                    75,
                                    70,
                                    35
                            )
                    );

                    p.setStrokeWidth(
                            1
                    );

                    c.drawLine(
                            statCard.left +
                                    sw * i,
                            statCard.top + 8,
                            statCard.left +
                                    sw * i,
                            statCard.bottom - 8,
                            p
                    );
                }

                RectF cell =
                        new RectF(
                                statCard.left +
                                        sw * i,
                                statCard.top,
                                statCard.left +
                                        sw * (i + 1),
                                statCard.bottom
                        );

                centerText(
                        c,
                        labels[i],
                        new RectF(
                                cell.left,
                                cell.top + 3,
                                cell.right,
                                cell.centerY()
                        ),
                        Math.max(
                                13,
                                w * .027f
                        ),
                        white
                );

                centerText(
                        c,
                        values[i],
                        new RectF(
                                cell.left,
                                cell.centerY() - 2,
                                cell.right,
                                cell.bottom
                        ),
                        Math.max(
                                21,
                                w * .040f
                        ),
                        statColors[i]
                );
            }

            // ----------------------------------------------------
            // BUTTONS
            // ----------------------------------------------------

            float buttonTop =
                    statCard.bottom +
                    10;

            float buttonGap =
                    7;

            float buttonWidth =
                    (
                            w -
                            side * 2 -
                            buttonGap * 2
                    ) / 3f;

            float buttonHeight =
                    Math.max(
                            55,
                            Math.min(
                                    70,
                                    h * .065f
                            )
                    );

            resetButton.set(
                    side,
                    buttonTop,
                    side +
                            buttonWidth,
                    buttonTop +
                            buttonHeight
            );

            checkButton.set(
                    side +
                            buttonWidth +
                            buttonGap,
                    buttonTop,
                    side +
                            buttonWidth * 2 +
                            buttonGap,
                    buttonTop +
                            buttonHeight
            );

            resultButton.set(
                    side +
                            buttonWidth * 2 +
                            buttonGap * 2,
                    buttonTop,
                    w - side,
                    buttonTop +
                            buttonHeight
            );

            draw3DButton(
                    c,
                    resetButton,
                    "RESET",
                    red,
                    Math.max(
                            15,
                            w * .030f
                    )
            );

            draw3DButton(
                    c,
                    checkButton,
                    "✓ CHECK",
                    green,
                    Math.max(
                            15,
                            w * .030f
                    )
            );

            draw3DButton(
                    c,
                    resultButton,
                    "RESULT",
                    purple,
                    Math.max(
                            15,
                            w * .030f
                    )
            );

            // ----------------------------------------------------
            // STATUS
            // ----------------------------------------------------

            if (!status.isEmpty()) {

                float statusTop =
                        buttonTop +
                        buttonHeight +
                        10;

                RectF statusRect =
                        new RectF(
                                side,
                                statusTop,
                                w - side,
                                Math.min(
                                        h - 15,
                                        statusTop + 55
                                )
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
                        statusRect,
                        Math.max(
                                15,
                                w * .032f
                        ),
                        statusColor
                );
            }

            // ----------------------------------------------------
            // FIREWORK
            // ----------------------------------------------------

            drawFireShower(c);
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
        // ON DRAW
        // ========================================================

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            int w =
                    getWidth();

            int h =
                    getHeight();

            if (!gameStarted) {

                drawBackground(
                        canvas,
                        w,
                        h
                );

                drawCrownLogo(
                        canvas,
                        w / 2f,
                        h * .25f,
                        Math.min(
                                w * .25f,
                                h * .14f
                        )
                );

                String title =
                        "JD NUMBER PUZZLE";

                float titleSize =
                        Math.max(
                                38,
                                Math.min(
                                        58,
                                        w * .09f
                                )
                        );

                p.setTextSize(
                        titleSize
                );

                drawText(
                        canvas,
                        title,
                        (w -
                                p.measureText(
                                        title
                                )) / 2f,
                        h * .43f,
                        titleSize,
                        yellow,
                        true
                );

                String msg =
                        "गेम सुरू करण्यासाठी परवानगी द्या";

                float msgSize =
                        Math.max(
                                17,
                                w * .035f
                        );

                p.setTextSize(
                        msgSize
                );

                drawText(
                        canvas,
                        msg,
                        (w -
                                p.measureText(
                                        msg
                                )) / 2f,
                        h * .50f,
                        msgSize,
                        goldLight,
                        true
                );

                return;
            }

            drawGame(
                    canvas,
                    w,
                    h
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

            if (!gameStarted) {

                // Permission पुन्हा दाखवणे
                postStartPermission();

                return true;
            }

            if (celebration) {
                return true;
            }

            float x =
                    event.getX();

            float y =
                    event.getY();

            // ----------------------------------------------------
            // NUMBER BOX
            // ----------------------------------------------------

            for (
                    int i = 0;
                    i < 4;
                    i++
            ) {

                if (
                        numberBoxes[i] != null &&
                        numberBoxes[i].contains(
                                x,
                                y
                        )
                ) {

                    selectNumber(i);

                    return true;
                }
            }

            // ----------------------------------------------------
            // HINT
            // ----------------------------------------------------

            if (
                    hintButton.contains(
                            x,
                            y
                    )
            ) {

                showHint();

                return true;
            }

            // ----------------------------------------------------
            // RESET
            // ----------------------------------------------------

            if (
                    resetButton.contains(
                            x,
                            y
                    )
            ) {

                resetGame();

                return true;
            }

            // ----------------------------------------------------
            // CHECK
            // ----------------------------------------------------

            if (
                    checkButton.contains(
                            x,
                            y
                    )
            ) {

                checkAnswer();

                return true;
            }

            // ----------------------------------------------------
            // RESULT
            // ----------------------------------------------------

            if (
                    resultButton.contains(
                            x,
                            y
                    )
            ) {

                showResult(
                        level >= 1000
                );

                return true;
            }

            return true;
        }

        // ========================================================
        // SELECT NUMBER
        // ========================================================

        void selectNumber(
                int index
        ) {

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

            saveGame();

            invalidate();
        }
    }

    // ============================================================
    // PARTICLE
    // ============================================================

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
