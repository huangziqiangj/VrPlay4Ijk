package com.perfectcorp.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import com.dgw.vrplay.play.VrPlay4Ijk;
import com.dgw.vrplay.play.PlayerModel;
import com.dgw.vrplay.play.StartListener;
import com.google.android.apps.muzei.render.GLTextureView;
import java.io.File;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity implements StartListener {
    private VrPlay4Ijk vrPlay4Ijk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GLTextureView glTextureView = findViewById(R.id.gl_view);
        File file = new File("/storage/self/primary/Download/testvr.mp4");
        Uri uri = Uri.fromFile(file);
        TableLayout tableLayout= findViewById(R.id.hud_view);

        vrPlay4Ijk = VrPlay4Ijk
                .with(this)//Activity对象（必选）
                .uri(uri)//播放源，支持本地视频，和网络流（必选）

                .statsView(tableLayout)//用来查看播放时的参数，可删除
                .player(new IjkMediaPlayer())//设置自定的播放器，可删除
                .onStartListener(this)//设置播放开始时的监听，可删除

                .into(glTextureView);//用来播放视频的控件（必选）

    }
    //这三个事件一定要加上
    @Override
    protected void onResume() {
        super.onResume();
        vrPlay4Ijk.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vrPlay4Ijk.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vrPlay4Ijk.onDestroy();
    }

    /**
     * 开始播放时的回调
     */
    @Override
    public void start() {
        View viewById = findViewById(R.id.progress);
        viewById.setVisibility(View.GONE);

        //设置播放的各种模式，详细见选项见枚举对象PlayerModel
        vrPlay4Ijk.setPlayModel(PlayerModel.MOTION_TOUCH);
        IjkMediaPlayer mediaPlayer = vrPlay4Ijk.getMediaPlayer();
    }
}