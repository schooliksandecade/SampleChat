package com.example.ihksan.newchat;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ihksan.newchat.animation.ViewProxy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.example.ihksan.newchat.Main2Activity.dp;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton btnPush;
    FloatingActionButton btnRecord;
    EditText etPush;
    FrameLayoutFixed frameLayoutFixed;
    RecyclerView rvList;
    ProgressBar pbar;
    LinearLayout linChat;
    List<Model> modelList = new ArrayList<Model>();
    Adapteres adapteres;
    boolean status = false;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    String x;
    private DatabaseReference mDatabase;
    private TextView recordTimeText;
    private View slideText;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private long startTime = 0L;
    private Timer timer;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPush = (FloatingActionButton) findViewById(R.id.btnPush);
        btnRecord = (FloatingActionButton) findViewById(R.id.btnRecord);
        etPush = (EditText) findViewById(R.id.etPush);
        rvList = (RecyclerView) findViewById(R.id.rvPush);
        frameLayoutFixed = (FrameLayoutFixed) findViewById(R.id.record_panel);
        slideText = findViewById(R.id.slideText);
        recordTimeText = (TextView) findViewById(R.id.recording_time_text);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pbar = (ProgressBar) findViewById(R.id.pBar);
        linChat = (LinearLayout) findViewById(R.id.linChat);


        setAdapteres();
        setLoading(true);
        setTransparenItem();
    }

    void setLoading(Boolean status) {
        if (status) {
            pbar.animate().alpha(1.0f).setDuration(500);
            rvList.animate().alpha(0.0f).setDuration(500);
        } else {
            pbar.animate().alpha(0.0f).setDuration(500);
            rvList.animate().alpha(1.0f).setDuration(500);
        }
    }

    void setAdapteres() {
        adapteres = new Adapteres(modelList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvList.setLayoutManager(layoutManager);
        rvList.setAdapter(adapteres);
    }

    @Override
    protected void onStart() {
        super.onStart();
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status) {
                    String data = etPush.getText().toString();
                    writeNewUser(data, false, false);
                    etPush.setText("");
                } else {
                    etPush.clearFocus();
                }
            }
        });


        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!status) {
                    etPush.clearFocus();
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        linChat.animate().alpha(0.0f).setDuration(500);
                        frameLayoutFixed.animate().alpha(1.0f).setDuration(500);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                                .getLayoutParams();
                        params.leftMargin = dp(50);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                        // startRecording();
                        startrecord();
                        btnPush.getParent()
                                .requestDisallowInterceptTouchEvent(true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP
                            || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        linChat.animate().alpha(1.0f).setDuration(500);
                        frameLayoutFixed.animate().alpha(0.0f).setDuration(500);
                        startedDraggingX = -1;
                        stoprecord();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float x = event.getX();
                        if (x < -distCanMove) {
                            cancelRecord();
                        }
                        x = x + ViewProxy.getX(btnPush);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                                .getLayoutParams();
                        if (startedDraggingX != -1) {
                            float dist = (x - startedDraggingX);
                            params.leftMargin = dp(50) + (int) dist;
                            slideText.setLayoutParams(params);
                            float alpha = 1.0f + dist / distCanMove;
                            if (alpha > 1) {
                                alpha = 1;
                            } else if (alpha < 0) {
                                alpha = 0;
                            }
                            ViewProxy.setAlpha(slideText, alpha);
                        }
                        if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                                + dp(50)) {
                            if (startedDraggingX == -1) {
                                startedDraggingX = x;
                                distCanMove = (frameLayoutFixed.getMeasuredWidth()
                                        - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                                if (distCanMove <= 0) {
                                    distCanMove = dp(80);
                                } else if (distCanMove > dp(80)) {
                                    distCanMove = dp(80);
                                }
                            }
                        }
                        if (params.leftMargin > dp(50)) {
                            params.leftMargin = dp(50);
                            slideText.setLayoutParams(params);
                            ViewProxy.setAlpha(slideText, 1);
                            startedDraggingX = -1;
                        }
                    }
                }
                v.onTouchEvent(event);
                return true;
            }
        });

        etPush.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rvList.smoothScrollToPosition(modelList.size());
                return false;
            }
        });
        etPush.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int data = etPush.getText().toString().length();
                if (data == 0) {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.send);
                    Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.record);
                    btnPush.startAnimation(animation);
                    btnRecord.startAnimation(animation1);
                    btnRecord.bringToFront();
                    status = false;
                } else if (data > 0) {
                    if (!status) {
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.send);
                        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.record);
                        btnPush.startAnimation(animation1);
                        btnRecord.startAnimation(animation);
                        btnPush.bringToFront();
                        status = true;
                    }
                }
            }
        });

        btnPush.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        mDatabase.child("police212").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                modelList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Model post = postSnapshot.getValue(Model.class);
                    modelList.add(post);
                }
                rvList.smoothScrollToPosition(modelList.size());
                adapteres.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                    }
                }, 1000);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void writeNewUser(String message, Boolean image, Boolean audio) {
        Model model = new Model();
        model.setUserId("001");
        model.setName("iksan");
        model.setMessage(message);
        model.setLocation("null");
        model.setTimeStamp(System.currentTimeMillis());
        model.setImage(image);
        model.setAudio(audio);
        String uId = mDatabase.push().getKey();
        mDatabase.child("police212").child(uId).setValue(model);
    }

    void cancelRecord() {
        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
        File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File file = new File(dir, x);
        file.delete();
    }

    private void startrecord() {
        // TODO Auto-generated method stub
        startTime = SystemClock.uptimeMillis();
        timer = new Timer();
        MainActivity.MyTimerTask myTimerTask = new MainActivity.MyTimerTask();
        timer.schedule(myTimerTask, 1000, 1000);
        vibrate();
        x = System.currentTimeMillis() + ".3gp";
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + x;
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stoprecord() {
        // TODO Auto-generated method stub
        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();

        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;

        writeNewUser("//photo", false, true);
    }

    private void vibrate() {
        // TODO Auto-generated method stub
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setTransparenItem() {
        frameLayoutFixed.setAlpha(0.0f);
        pbar.setAlpha(0.0f);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            final String hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastsec + " hms " + hms);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (recordTimeText != null)
                            recordTimeText.setText(hms);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            });
        }
    }
}
