package com.jd.numberpuzzle;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    GameView game;
    @Override public void onCreate(Bundle b){super.onCreate(b); game=new GameView(this); setContentView(game);}
}

class GameView extends View {
    Paint p=new Paint(1); Random rng=new Random();
    int level=1, score=0, target, hints=3; ArrayList<Integer> nums=new ArrayList<>(), chosen=new ArrayList<>();
    char op='+'; long flashUntil=0; String status="";

    GameView(Context c){super(c);setFocusable(true);load();newPuzzle();}
    int boxes(){return level<=50?10:level<=100?20:level<=200?30:40;}
    String difficulty(){return level<=50?"Easy":level<=100?"Medium":level<=200?"Hard":level<=400?"Hard+":"Very Hard";}
    void save(){getContext().getSharedPreferences("jd",0).edit().putInt("level",level).putInt("score",score).putInt("hints",hints).apply();}
    void load(){android.content.SharedPreferences s=getContext().getSharedPreferences("jd",0);level=s.getInt("level",1);score=s.getInt("score",0);hints=s.getInt("hints",3);}

    void newPuzzle(){
        chosen.clear();op='+';
        int n=boxes();
        target=level<=400?20+rng.nextInt(181):100+rng.nextInt(901);
        nums.clear();
        // Guaranteed verified base solution.
        if(level<=400){
            int a=1+rng.nextInt(Math.max(1,target-1)); int b=target-a;
            if(b<1){a=target-1;b=1;}
            nums.add(a);nums.add(b);
        }else{
            int a=1+target/3; int b=1+target/3; int c=target-a-b;
            if(c<1){a=target/4;b=target/4;c=target-a-b;if(c<1)c=1;}
            nums.add(a);nums.add(b);nums.add(c);
        }
        while(nums.size()<n) nums.add(level<=400?1+rng.nextInt(40):1+rng.nextInt(99));
        status="";invalidate();
    }

    String expression(){StringBuilder s=new StringBuilder();for(int i=0;i<chosen.size();i++){if(i>0)s.append(" ").append(op).append(" ");s.append(nums.get(chosen.get(i)));}return s.length()==0?"नंबर निवडा":s.toString();}
    double value(){if(chosen.size()==0)return Double.NaN;double r=nums.get(chosen.get(0));for(int k=1;k<chosen.size();k++){double v=nums.get(chosen.get(k));if(op=='+')r+=v;else if(op=='-')r-=v;else if(op=='*')r*=v;else{if(v==0)return Double.NaN;r/=v;}}return r;}

    void tone(){try{ToneGenerator t=new ToneGenerator(AudioManager.STREAM_MUSIC,80);t.startTone(ToneGenerator.TONE_PROP_BEEP,100);}catch(Exception ignored){}}
    void check(){
        if(Math.abs(value()-target)<1e-9){
            score+=level>=401?20:10;status="🎉 बरोबर!";flashUntil=System.currentTimeMillis()+600;tone();save();invalidate();
            new Handler().postDelayed(()->{if(level<1000)level++;save();newPuzzle();},650);
        }else{status="❌ अजून प्रयत्न करा!";tone();invalidate();}
    }
    void hint(){
        if(hints<=0){status="💡 Hints संपले";invalidate();return;}
        hints--; chosen.clear(); op='+';
        chosen.add(0);chosen.add(1);
        status="💡 Hint: पहिल्या दोन नंबरपासून सुरुवात करा";save();invalidate();
    }
    void reset(){chosen.clear();status="";invalidate();}

    protected void onDraw(Canvas c){
        c.drawColor(Color.rgb(16,19,26));
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setColor(Color.rgb(255,213,74));p.setTextSize(56);c.drawText("JD",24,60,p);
        p.setColor(Color.WHITE);p.setTextSize(25);c.drawText("Number Puzzle",96,56,p);
        p.setColor(Color.LTGRAY);p.setTextSize(15);c.drawText("Level "+level+" / 1000   ⭐ "+score+"   💡 "+hints,24,91,p);
        p.setColor(Color.LTGRAY);p.setTextSize(15);c.drawText(boxes()+" boxes • "+difficulty()+" • No Timer",24,118,p);
        p.setColor(Color.rgb(255,213,74));p.setTextSize(48);String ts=""+target;c.drawText(ts,getWidth()/2-p.measureText(ts)/2,172,p);

        int cols=getWidth()<600?4:5,gap=10,top=195,cell=(getWidth()-gap*(cols+1))/cols;
        for(int i=0;i<nums.size();i++){int r=i/cols,col=i%cols,x=gap+col*(cell+gap),y=top+r*(cell+gap);
            p.setColor(chosen.contains(i)?Color.rgb(255,213,74):Color.rgb(42,50,64));c.drawRoundRect(x,y,x+cell,y+cell,15,15,p);
            p.setColor(chosen.contains(i)?Color.rgb(16,19,26):Color.WHITE);p.setTextSize(20);String q=""+nums.get(i);c.drawText(q,x+cell/2-p.measureText(q)/2,y+cell/2+7,p);}
        int bottom=Math.min(getHeight()-210,top+((nums.size()+cols-1)/cols)*(cell+gap)+6);
        p.setColor(Color.rgb(25,30,40));c.drawRoundRect(14,bottom,getWidth()-14,bottom+55,14,14,p);
        p.setColor(Color.WHITE);p.setTextSize(15);c.drawText(expression(),25,bottom+34,p);

        String[] ops={"+","−","×","÷"};int ow=(getWidth()-44)/4;
        for(int i=0;i<4;i++){p.setColor(Color.rgb(54,64,82));c.drawRoundRect(14+i*ow,bottom+66,14+(i+1)*ow-7,bottom+112,11,11,p);p.setColor(Color.WHITE);p.setTextSize(23);c.drawText(ops[i],32+i*ow,bottom+97,p);}
        // Buttons
        p.setColor(Color.rgb(229,233,239));c.drawRoundRect(14,bottom+123,getWidth()/3-7,bottom+173,11,11,p);
        p.setColor(Color.rgb(91,213,138));c.drawRoundRect(getWidth()/3+3,bottom+123,2*getWidth()/3-7,bottom+173,11,11,p);
        p.setColor(Color.rgb(110,92,210));c.drawRoundRect(2*getWidth()/3+3,bottom+123,getWidth()-14,bottom+173,11,11,p);
        p.setColor(Color.rgb(20,24,30));p.setTextSize(15);c.drawText("RESET",30,bottom+155,p);
        p.setColor(Color.rgb(15,25,20));c.drawText("CHECK ✓",getWidth()/3+22,bottom+155,p);
        p.setColor(Color.WHITE);c.drawText("HINT 💡",2*getWidth()/3+20,bottom+155,p);
        p.setColor(Color.WHITE);p.setTextSize(16);if(!status.isEmpty())c.drawText(status,24,bottom+200,p);
    }

    public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();
        int cols=getWidth()<600?4:5,gap=10,top=195,cell=(getWidth()-gap*(cols+1))/cols;
        for(int i=0;i<nums.size();i++){int r=i/cols,col=i%cols,xx=gap+col*(cell+gap),yy=top+r*(cell+gap);if(x>=xx&&x<=xx+cell&&y>=yy&&y<=yy+cell){if(chosen.contains(i))chosen.remove((Integer)i);else chosen.add(i);invalidate();return true;}}
        int bottom=Math.min(getHeight()-210,top+((nums.size()+cols-1)/cols)*(cell+gap)+6);
        if(y>=bottom+66&&y<=bottom+112){int idx=(int)((x-14)/((getWidth()-44)/4));if(idx>=0&&idx<4){op="+-*/".charAt(idx);invalidate();return true;}}
        if(y>=bottom+123&&y<=bottom+180){
            if(x<getWidth()/3){reset();return true;}
            if(x<2*getWidth()/3){check();return true;}
            hint();return true;
        }
        return true;
    }
}