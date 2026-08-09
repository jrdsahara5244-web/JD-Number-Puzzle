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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
        RectF reset=new RectF(), check=new RectF(), result=new RectF();

        int level=1, score=0, roundCorrect=0, roundWrong=0, a=-1, b=-1, target;
        String operation="", question="", status="";

        GameView(Context c) { super(c); newPuzzle(); }

        int n() {
            return r.nextInt(100)<35 ? 1+r.nextInt(9) : 10+r.nextInt(90);
        }

        void fill() {
            while(nums.size()<10) {
                int x=n(); boolean dup=false;
                for(int v:nums) if(v==x) dup=true;
                if(!dup) nums.add(x);
            }
            Collections.shuffle(nums,r);
        }

        void newPuzzle() {
            nums.clear(); a=b=-1; status="";
            int type=r.nextInt(4), x, y;

            if(type==0) {
                operation="+";
                do { x=n(); y=n(); target=x+y; } while(target<10 || target>198);
                question="कोणते दोन नंबर अधिक केल्यावर "+target+" मिळेल?";
            } else if(type==1) {
                operation="−";
                do { x=n(); y=n(); } while(x<=y);
                target=x-y;
                question="कोणत्या मोठ्या नंबरमधून कोणता नंबर वजा केल्यावर "+target+" मिळेल?";
            } else if(type==2) {
                operation="×";
                do { x=10+r.nextInt(90); y=10+r.nextInt(90); target=x*y; }
                while(target<100 || target>9801);
                question="कोणते दोन नंबर गुणिले असता "+target+" मिळेल?";
            } else {
                operation="÷";
                do { y=2+r.nextInt(8); target=1+r.nextInt(11); x=y*target; }
                while(x>99);
                question="कोणता नंबर कोणत्या नंबरने भागल्यावर "+target+" मिळेल?";
            }

            nums.add(x); nums.add(y); fill(); invalidate();
        }

        boolean correct(int x,int y) {
            if(operation.equals("+")) return x+y==target;
            if(operation.equals("−")) return x>y && x-y==target;
            if(operation.equals("×")) return x*y==target;
            return y!=0 && x%y==0 && x/y==target;
        }

        void checkAnswer() {
            if(a<0 || b<0) {
                status="कृपया दोन नंबर निवडा."; invalidate(); return;
            }
            if(correct(nums.get(a),nums.get(b))) {
                score++; roundCorrect++; status="✓ बरोबर! +1 गुण"; invalidate();
                postDelayed(() -> {
                    if(level%10==0) showResult(level==1000);
                    else { level++; newPuzzle(); }
                },650);
            } else {
                roundWrong++; status="✗ उत्तर चुकले. पुन्हा प्रयत्न करा."; invalidate();
            }
        }

        void showResult(final boolean finalResult) {
            final Dialog d=new Dialog(MainActivity.this);
            LinearLayout box=new LinearLayout(MainActivity.this);
            box.setOrientation(LinearLayout.VERTICAL); box.setPadding(45,35,45,35);
            GradientDrawable bg=new GradientDrawable();
            bg.setColor(Color.rgb(25,31,43)); bg.setCornerRadius(35); box.setBackground(bg);

            TextView title=new TextView(MainActivity.this);
            title.setText(finalResult?"🏆 FINAL RESULT":"🎯 RESULT");
            title.setTextColor(Color.WHITE); title.setTextSize(26); title.setGravity(Gravity.CENTER);

            TextView info=new TextView(MainActivity.this);
            info.setText((finalResult?"Level 991 ते 1000":"Level "+(level-9)+" ते "+level)
                    +"\n\nया 10 Levels मधील गुण: "+roundCorrect+"/10"
                    +"\nचुकीचे प्रयत्न: "+roundWrong+"\n\nएकूण गुण: "+score);
            info.setTextColor(Color.WHITE); info.setTextSize(19); info.setGravity(Gravity.CENTER);

            Button next=new Button(MainActivity.this);
            next.setText(finalResult?"GAME पुन्हा सुरू करा":"NEXT LEVEL ▶");
            box.addView(title); box.addView(info); box.addView(next);
            d.setContentView(box); d.show();

            next.setOnClickListener(v -> {
                d.dismiss();
                if(finalResult) { level=1; score=0; roundCorrect=0; roundWrong=0; }
                else { roundCorrect=0; roundWrong=0; if(level<1000) level++; }
                newPuzzle();
            });
        }

        void btn(Canvas c,RectF q,String s,int color,float size) {
            p.setColor(color); c.drawRoundRect(q,18,18,p);
            p.setColor(Color.WHITE); p.setTextSize(size); p.setTypeface(Typeface.DEFAULT_BOLD);
            Paint.FontMetrics f=p.getFontMetrics();
            c.drawText(s,q.centerX()-p.measureText(s)/2,
                    q.centerY()-(f.ascent+f.descent)/2,p);
        }

        @Override protected void onDraw(Canvas c) {
            c.drawColor(Color.rgb(15,19,28));
            int w=getWidth(), h=getHeight();
            float pad=Math.max(10,w*.025f), gap=Math.max(6,w*.012f);

            p.setTypeface(Typeface.DEFAULT_BOLD); p.setColor(Color.WHITE); p.setTextSize(Math.max(18,w*.045f));
            c.drawText("JD Number Puzzle",pad,35,p);
            p.setTypeface(Typeface.DEFAULT); p.setColor(Color.LTGRAY); p.setTextSize(Math.max(11,w*.026f));
            c.drawText("Level "+level+" / 1000 • छोटे नंबर • No Timer",pad,59,p);

            p.setColor(Color.rgb(255,214,73)); p.setTextSize(Math.max(13,w*.03f));
            String sc="⭐ "+score+" गुण"; c.drawText(sc,w-p.measureText(sc)-pad,35,p);

            float qt=72, qh=Math.max(92,h*.12f);
            p.setColor(Color.rgb(28,34,48)); c.drawRoundRect(pad,qt,w-pad,qt+qh,16,16,p);
            p.setColor(Color.rgb(255,214,73)); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextSize(Math.max(15,w*.032f));
            c.drawText("🧮 प्रश्न",pad+14,qt+27,p);
            p.setColor(Color.WHITE); p.setTypeface(Typeface.DEFAULT); p.setTextSize(Math.max(14,w*.03f));

            String q=question;
            if(p.measureText(q)>w-2*pad-28) {
                int cut=q.length()/2, sp=q.indexOf(" ",cut);
                if(sp>0) {
                    c.drawText(q.substring(0,sp),pad+14,qt+55,p);
                    c.drawText(q.substring(sp+1),pad+14,qt+78,p);
                } else c.drawText(q,pad+14,qt+58,p);
            } else c.drawText(q,pad+14,qt+61,p);

            float top=qt+qh+12, gh=h*.34f, bw=(w-2*pad-4*gap)/5f, bh=(gh-gap)/2f;
            for(int i=0;i<10;i++) {
                int row=i/5,col=i%5; float x=pad+col*(bw+gap), y=top+row*(bh+gap);
                boxes[i]=new RectF(x,y,x+bw,y+bh);
                p.setColor(i==a||i==b?Color.rgb(55,115,200):Color.rgb(43,51,66));
                c.drawRoundRect(boxes[i],16,16,p);
                p.setColor(Color.WHITE); p.setTypeface(Typeface.DEFAULT_BOLD);
                String s=""+nums.get(i); p.setTextSize(s.length()==2?Math.max(20,bw*.22f):Math.max(24,bw*.26f));
                c.drawText(s,boxes[i].centerX()-p.measureText(s)/2,
                        boxes[i].centerY()-(p.ascent()+p.descent())/2,p);
            }

            float label=top+gh+28;
            p.setColor(Color.rgb(255,214,73)); p.setTextSize(Math.max(15,w*.032f)); p.setTypeface(Typeface.DEFAULT_BOLD);
            String op=operation.equals("+")?"➕ अधिक":operation.equals("−")?"➖ वजा":operation.equals("×")?"✖ गुणाकार":"➗ भागाकार";
            c.drawText("क्रिया: "+op,pad,label,p);

            float at=label+18, ah=Math.max(62,h*.075f), aw=(w-2*pad-2*gap)/3f;
            reset.set(pad,at,pad+aw,at+ah);
            check.set(pad+aw+gap,at,pad+2*aw+gap,at+ah);
            result.set(pad+2*(aw+gap),at,w-pad,at+ah);

            btn(c,reset,"RESET",Color.rgb(225,60,65),Math.max(14,w*.035f));
            btn(c,check,"CHECK ✓",Color.rgb(72,205,125),Math.max(14,w*.035f));
            btn(c,result,"RESULT",Color.rgb(105,83,210),Math.max(14,w*.035f));

            if(!status.isEmpty()) {
                p.setColor(Color.WHITE); p.setTextSize(Math.max(13,w*.03f)); c.drawText(status,pad,at+ah+30,p);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY();

            for(int i=0;i<10;i++) if(boxes[i]!=null && boxes[i].contains(x,y)) {
                if(a<0) a=i; else if(b<0 && i!=a) b=i; else { a=i; b=-1; }
                status=""; invalidate(); return true;
            }
            if(reset.contains(x,y)) { a=b=-1; status="RESET केले."; invalidate(); return true; }
            if(check.contains(x,y)) { checkAnswer(); return true; }
            if(result.contains(x,y)) { showResult(false); return true; }
            return true;
        }
    }
}
