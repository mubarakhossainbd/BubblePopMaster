package com.example.bubblepopmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class BubbleBackgroundView extends View {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ArrayList<Ball> balls = new ArrayList<>();

    Random random = new Random();

    int[] colors = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
    };

    public BubbleBackgroundView(Context c, AttributeSet a) {
        super(c, a);

        for (int i = 0; i < 100; i++) {

            Ball b = new Ball();

            b.x = random.nextInt(1080);
            b.y = random.nextInt(2200);

            b.r = random.nextInt(25) + 12;

            b.dy = random.nextFloat() * 2 + 0.5f;

            b.color = colors[random.nextInt(colors.length)];

            balls.add(b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.rgb(18,20,33));

        for (Ball b : balls) {

            paint.setColor(b.color);
            paint.setAlpha(120);

            canvas.drawCircle(b.x,b.y,b.r,paint);



        }

    }

    class Ball{

        float x;
        float y;

        float r;

        float dy;

        int color;

    }

}