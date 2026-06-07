package com.example.bubblepopmaster;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.*;

public class BubbleGameView extends View {

    private Paint paint;
    private SoundPool soundPool;
    private int shootSound, popSound, winSound, clickSound;
    private Bubble[][] grid;

    private int rows = 25, cols = 11;
    private int startRows = 8;
    private float radius = 28;
    private float gap = 3;

    private float shooterX, shooterY;
    private float ballX, ballY;
    private float dx, dy;
    private boolean shooting = false;

    private float aimX, aimY;
    private boolean aiming = false;
    private int hitRow = -1;
    private int hitCol = -1;

    private int currentColor;
    private int nextColor;
    private boolean canSwap = true;
    private Random random = new Random();

    private int score = 0;
    private int level = 1;
    private boolean gameOver = false;
    private int shotsCount = 0;
    private int shotsBeforeDown = 25;
    private boolean levelComplete = false;
    private long levelCompleteTime = 0;
    private int levelDelay = 3000;



    private int[] colors = {
            Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA
    };

    public BubbleGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grid = new Bubble[rows][cols];
        currentColor = randomColor();
        nextColor = randomColor();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        shootSound = soundPool.load(getContext(), R.raw.shoot, 1);
        popSound = soundPool.load(getContext(), R.raw.pop, 1);
        winSound = soundPool.load(getContext(), R.raw.win, 1);
        clickSound = soundPool.load(getContext(), R.raw.click, 1);

        postDelayed(gameLoop, 16);
    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            updateGame();
            invalidate();
            postDelayed(this, 16);
        }
    };

    private int randomColor() {
        return colors[random.nextInt(colors.length)];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        radius = w / 26f;
        gap = radius * 0.10f;

        shooterX = w / 2f;
        shooterY = h * 0.86f;

        ballX = shooterX;
        ballY = shooterY;

        createBubbles();
    }

    private void createBubbles() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (r < startRows) {
                    float[] pos = getCellPosition(r, c);
                    grid[r][c] = new Bubble(pos[0], pos[1], randomColor());
                } else {
                    grid[r][c] = null;
                }
            }
        }
    }

    private float[] getCellPosition(int r, int c) {

        float scoreTop = radius * 3.2f;
        float scoreBoxHeight = radius * 2.2f;

        float startY = scoreTop + scoreBoxHeight + radius * 1.4f;

        float cellW = radius * 2 + gap;
        float totalWidth = cols * cellW - gap + radius;

        float startX = (getWidth() - totalWidth) / 2f + radius;

        float x = startX + c * cellW;

        if (r % 2 == 1) {
            x += radius;
        }

        float y = startY + r * (radius * 1.75f);

        return new float[]{x, y};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.rgb(18, 20, 33));

        drawScore(canvas);
        drawAimLine(canvas);
        drawGrid(canvas);
        drawShooter(canvas);
        drawBall(canvas);
        drawNextBubble(canvas);

        if (gameOver) {
            drawGameOver(canvas);
        }
        if (levelComplete) {
            drawLevelComplete(canvas);
        }
    }
    private void drawLevelComplete(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        paint.setColor(Color.YELLOW);
        paint.setTextSize(70);
        canvas.drawText("LEVEL COMPLETE!", getWidth() / 2f, getHeight() / 2f - 40, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(38);
        canvas.drawText("Next Level Loading...", getWidth() / 2f, getHeight() / 2f + 35, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setFakeBoldText(false);
    }

    private void drawScore(Canvas canvas) {

        float top = radius * 3.2f;
        float boxHeight = radius * 2.2f;

        float margin = radius * 0.45f;

        float scoreLeft = margin;
        float scoreRight = getWidth() * 0.48f;

        float levelLeft = getWidth() * 0.52f;
        float levelRight = getWidth() - margin;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(30, 28, 55));

        RectF scoreBox = new RectF(scoreLeft, top, scoreRight, top + boxHeight);
        canvas.drawRoundRect(scoreBox, 18, 18, paint);

        RectF levelBox = new RectF(levelLeft, top, levelRight, top + boxHeight);
        canvas.drawRoundRect(levelBox, 18, 18, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.14f);
        paint.setColor(Color.rgb(160, 90, 255));

        canvas.drawRoundRect(scoreBox, 18, 18, paint);
        canvas.drawRoundRect(levelBox, 18, 18, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(true);
        paint.setTextSize(radius * 0.78f);

        float textY = top + boxHeight * 0.66f;

        paint.setColor(Color.WHITE);
        canvas.drawText("SCORE: ", scoreLeft + radius * 0.45f, textY, paint);

        float scoreLabelWidth = paint.measureText("SCORE: ");

        paint.setColor(Color.YELLOW);
        canvas.drawText(
                String.valueOf(score),
                scoreLeft + radius * 0.45f + scoreLabelWidth,
                textY,
                paint
        );

        paint.setColor(Color.WHITE);
        canvas.drawText("LEVEL: ", levelLeft + radius * 0.45f, textY, paint);

        float levelLabelWidth = paint.measureText("LEVEL: ");

        paint.setColor(Color.YELLOW);
        canvas.drawText(
                String.valueOf(level),
                levelLeft + radius * 0.45f + levelLabelWidth,
                textY,
                paint
        );

        paint.setFakeBoldText(false);
    }

    private void drawGrid(Canvas canvas) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Bubble b = grid[r][c];
                if (b != null) {
                    drawBubble(canvas, b.x, b.y, b.color);
                }
            }
        }
    }

    private void drawBubble(Canvas canvas, float x, float y, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y, radius, paint);

        paint.setColor(Color.argb(90, 170, 255, 255));
        canvas.drawCircle(x - radius / 3, y - radius / 3, radius / 3, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);

        paint.setStyle(Paint.Style.FILL);
    }

    private void drawAimLine(Canvas canvas) {
        if (!shooting && aiming) {
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(4);
            paint.setAlpha(160);
            float dx = aimX - shooterX;
            float dy = aimY - shooterY;

            float len = (float)Math.sqrt(dx * dx + dy * dy);

            float endX = shooterX + dx / len * 180;
            float endY = shooterY + dy / len * 180;

            canvas.drawLine(shooterX, shooterY, endX, endY, paint);
            paint.setAlpha(255);
        }
    }

    private void drawShooter(Canvas canvas) {
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(shooterX, shooterY + 55, 48, paint);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        canvas.drawLine(shooterX, shooterY + 40, shooterX, shooterY - 45, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(8);
        canvas.drawLine(shooterX, shooterY - 10, shooterX, shooterY - 100, paint);

        Path arrow = new Path();
        arrow.moveTo(shooterX, shooterY - 120);
        arrow.lineTo(shooterX - 18, shooterY - 90);
        arrow.lineTo(shooterX + 18, shooterY - 90);
        arrow.close();
        canvas.drawPath(arrow, paint);
    }

    private void drawBall(Canvas canvas) {
        drawBubble(canvas, ballX, ballY, currentColor);
    }
    private void drawNextBubble(Canvas canvas) {

        float boxW = radius * 2.8f;
        float boxH = radius * 3.2f;

        float boxX = shooterX + radius * 2.2f;
        float boxY = shooterY - radius * 2.2f;

        // ডান পাশে চলে গেলে ভিতরে রাখবে
        if (boxX + boxW > getWidth() - 10) {
            boxX = getWidth() - boxW - 10;
        }

        RectF nextBox = new RectF(boxX, boxY, boxX + boxW, boxY + boxH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(30, 28, 55));
        canvas.drawRoundRect(nextBox, 18, 18, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.GRAY);
        canvas.drawRoundRect(nextBox, 18, 18, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(true);
        paint.setTextSize(getWidth() / 34f);
        paint.setColor(Color.WHITE);
        canvas.drawText("NEXT", boxX + boxW * 0.18f, boxY + boxH * 0.32f, paint);

        drawBubble(canvas, boxX + boxW / 2f, boxY + boxH * 0.72f, nextColor);

        paint.setFakeBoldText(false);
    }
    private void drawGameOver(Canvas canvas) {

        paint.setColor(Color.RED);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                "GAME OVER",
                getWidth() / 2f,
                getHeight() / 2f,
                paint
        );

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);

        canvas.drawText(
                "Tap To Restart",
                getWidth() / 2f,
                getHeight() / 2f + 80,
                paint
        );

        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (shooting) return true;

        float touchX = event.getX();
        float touchY = event.getY();

        float nextBoxW = radius * 2.8f;
        float nextBoxH = radius * 3.2f;

        float nextBoxX = shooterX + radius * 2.2f;
        float nextBoxY = shooterY - radius * 2.2f;

        if (event.getAction() == MotionEvent.ACTION_UP
                && canSwap
                && touchX >= nextBoxX
                && touchX <= nextBoxX + nextBoxW
                && touchY >= nextBoxY
                && touchY <= nextBoxY + nextBoxH) {

            int temp = currentColor;
            currentColor = nextColor;
            nextColor = temp;

            canSwap = false;
            aiming = false;

            invalidate();
            return true;
        }

        if (gameOver && event.getAction() == MotionEvent.ACTION_UP) {
            level = 1;
            score = 0;
            startRows = 8;
            shotsCount = 0;
            gameOver = false;

            createBubbles();
            resetBall();

            invalidate();
            return true;
        }

        aimX = touchX;
        aimY = touchY;
        aiming = true;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            float angleX = aimX - shooterX;
            float angleY = aimY - shooterY;

            float length = (float) Math.sqrt(angleX * angleX + angleY * angleY);
            if (length == 0) return true;

            dx = angleX / length * 22;
            dy = angleY / length * 22;

            if (dy > 0) dy = -Math.abs(dy);
            if (dy > -10) dy = -10;

            if (dx > 16) dx = 16;
            if (dx < -16) dx = -16;

            soundPool.play(shootSound, 1, 1, 0, 0, 1);

            canSwap = false;
            shooting = true;
            aiming = false;
        }

        invalidate();
        return true;
    }

    private void updateGame() {
        if (levelComplete) {
            if (System.currentTimeMillis() - levelCompleteTime >= levelDelay) {
                levelComplete = false;
                nextLevel();
            }
            return;
        }
        if (!shooting) return;

        ballX += dx;
        ballY += dy;

        if (ballX <= radius || ballX >= getWidth() - radius) {
            dx = -dx;
        }

        if (ballY <= radius + 40) {
            attachBubble();
            return;
        }

        checkCollision();
    }

    private void checkCollision() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Bubble b = grid[r][c];

                if (b != null) {
                    float d = distance(ballX, ballY, b.x, b.y);

                    if (d <= radius * 2) {
                        hitRow = r;
                        hitCol = c;
                        attachBubble();
                        return;
                    }
                }
            }
        }
    }

    private void attachBubble() {
        hitRow = -1;
        hitCol = -1;

        boolean foundHit = false;

        for (int r = 0; r < rows && !foundHit; r++) {
            for (int c = 0; c < cols; c++) {

                Bubble b = grid[r][c];

                if (b != null) {

                    float d = distance(ballX, ballY, b.x, b.y);

                    if (d <= radius * 2.2f) {

                        hitRow = r;
                        hitCol = c;

                        foundHit = true;
                        break;
                    }
                }
            }
        }

        // যদি কোনো bubble-এ hit না করে, তাহলে বসবে না, গায়েব হবে
        if (hitRow == -1 || hitCol == -1) {
            countShotAndReset();
            return;
        }

        int bestR = -1;
        int bestC = -1;
        float minD = Float.MAX_VALUE;

        int[][] dirs;

        if (hitRow % 2 == 0) {
            dirs = new int[][]{
                    {-1, -1}, {-1, 0},
                    {0, -1}, {0, 1},
                    {1, -1}, {1, 0}
            };
        } else {
            dirs = new int[][]{
                    {-1, 0}, {-1, 1},
                    {0, -1}, {0, 1},
                    {1, 0}, {1, 1}
            };
        }

        for (int[] d : dirs) {
            int nr = hitRow + d[0];
            int nc = hitCol + d[1];

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == null) {
                float[] pos = getCellPosition(nr, nc);
                float dist = distance(ballX, ballY, pos[0], pos[1]);

                if (dist < minD) {
                    minD = dist;
                    bestR = nr;
                    bestC = nc;
                }
            }
        }

        if (bestR != -1 && bestC != -1) {
            float[] pos = getCellPosition(bestR, bestC);
            grid[bestR][bestC] = new Bubble(pos[0], pos[1], currentColor);
            checkMatch(bestR, bestC);
        } else {
            // জায়গা না থাকলে bubble বসবে না, গায়েব হবে
        }

        hitRow = -1;
        hitCol = -1;

        countShotAndReset();
    }
    private void countShotAndReset() {
        shotsCount++;

        if (shotsCount >= shotsBeforeDown) {
            moveBubblesDown();
            shotsCount = 0;
        }

        resetBall();
    }

    private void checkMatch(int row, int col) {
        if (grid[row][col] == null) return;

        boolean[][] visited = new boolean[rows][cols];
        ArrayList<int[]> matched = new ArrayList<>();

        findSameColor(row, col, grid[row][col].color, visited, matched);

        if (matched.size() >= 3) {

            for (int[] p : matched) {
                grid[p[0]][p[1]] = null;
            }

            score += matched.size() * 10;
            soundPool.play(popSound, 1, 1, 0, 0, 1);
        }

        // match হোক বা না হোক floating bubble remove করবে
        removeFloatingBubbles();

        checkWin();
    }
    private void checkWin() {
        boolean hasBubble = false;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) {
                    hasBubble = true;
                    break;
                }
            }
        }

        if (!hasBubble && !levelComplete) {
            levelComplete = true;
            levelCompleteTime = System.currentTimeMillis();

            score += level * 100;

            soundPool.play(winSound, 1, 1, 0, 0, 1);
        }
    }
    private void checkGameOver() {

        float dangerLine = shooterY - radius * 2.5f;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                Bubble b = grid[r][c];

                if (b != null && b.y + radius >= dangerLine) {
                    gameOver = true;
                    return;
                }
            }
        }
    }
    private void nextLevel() {
        level++;

        if (startRows < 10) {
            startRows++;
        }

        createBubbles();
        resetBall();
    }
    private void moveBubblesDown() {

        for (int r = rows - 1; r > 0; r--) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = grid[r - 1][c];

                if (grid[r][c] != null) {
                    float[] pos = getCellPosition(r, c);
                    grid[r][c].x = pos[0];
                    grid[r][c].y = pos[1];
                }
            }
        }

        for (int c = 0; c < cols; c++) {
            float[] pos = getCellPosition(0, c);
            grid[0][c] = new Bubble(pos[0], pos[1], randomColor());
        }


        checkGameOver();
    }
    private void removeFloatingBubbles() {
        boolean[][] connected = new boolean[rows][cols];

        for (int c = 0; c < cols; c++) {
            if (grid[0][c] != null) {
                markConnectedToTop(0, c, connected);
            }
        }

        int removed = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null && !connected[r][c]) {
                    grid[r][c] = null;
                    removed++;
                }
            }
        }

        if (removed > 0) {
            score += removed * 20;
        }
    }

    private void markConnectedToTop(int r, int c, boolean[][] connected) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return;
        if (connected[r][c]) return;
        if (grid[r][c] == null) return;

        connected[r][c] = true;

        int[][] dirs;

        if (r % 2 == 0) {
            dirs = new int[][]{
                    {-1, -1}, {-1, 0},
                    {0, -1}, {0, 1},
                    {1, -1}, {1, 0}
            };
        } else {
            dirs = new int[][]{
                    {-1, 0}, {-1, 1},
                    {0, -1}, {0, 1},
                    {1, 0}, {1, 1}
            };
        }

        for (int[] d : dirs) {
            markConnectedToTop(r + d[0], c + d[1], connected);
        }
    }

    private void findSameColor(int r, int c, int color,
                               boolean[][] visited,
                               ArrayList<int[]> matched) {

        if (r < 0 || r >= rows || c < 0 || c >= cols) return;
        if (visited[r][c]) return;
        if (grid[r][c] == null) return;
        if (grid[r][c].color != color) return;

        visited[r][c] = true;
        matched.add(new int[]{r, c});

        int[][] dirs;

        if (r % 2 == 0) {
            dirs = new int[][]{
                    {-1, -1}, {-1, 0},
                    {0, -1}, {0, 1},
                    {1, -1}, {1, 0}
            };
        } else {
            dirs = new int[][]{
                    {-1, 0}, {-1, 1},
                    {0, -1}, {0, 1},
                    {1, 0}, {1, 1}
            };
        }

        for (int[] d : dirs) {
            findSameColor(r + d[0], c + d[1], color, visited, matched);
        }
    }

    private void resetBall() {
        shooting = false;

        currentColor = nextColor;
        nextColor = randomColor();

        ballX = shooterX;
        ballY = shooterY;
        canSwap = true;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    static class Bubble {
        float x, y;
        int color;

        Bubble(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}