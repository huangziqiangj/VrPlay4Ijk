# VrPlay4Iik
基于ijk与MD360Play封装的VR播放器，调用简单、逻辑清晰、兼容性好

基本使用两种使用方案
第一种方案，下载完整aar包，该aar包里面包含了对ijk的封装和依赖（推荐）
https://github.com/huangziqiangj/VrPlay4Iik/blob/master/demo/libs/vrplay4ijk1.0-all.aar

1、将arr包拷贝到项目中libs文件夹下

2、在moudel的build.gradle的android节点中加入


 repositories {
        flatDir {
            dirs 'libs'   
        }
    }
在dependencies节点加入
implementation(name: 'vrplay4ijk1.0-all', ext: 'aar')

第二种方案，下载只包含vr渲染的部分

https://github.com/huangziqiangj/VrPlay4Iik/blob/master/demo/libs/vrplay4ijk-release.aar

和第一种方案一样引入aar包后，加入ijk的依赖即可
	
dependencies {

    implementation 'tv.danmaku.ijk.media:ijkplayer-exo:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-java:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-armv5:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-arm64:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-x86:0.8.8'
    implementation 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.8'
}

	
完成包引入后即可使用
布局

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <FrameLayout
        android:orientation="horizontal"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.google.android.apps.muzei.render.GLTextureView
            android:id="@+id/gl_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
        <TableLayout
            android:id="@+id/hud_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="right|center_vertical"
            android:padding="8dp" />

    </FrameLayout>

    <ProgressBar
        android:layout_centerInParent="true"
        android:id="@+id/progress"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</RelativeLayout>


代码


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

                .statsView(tableLayout)//用来查看播放时的参数，可不设置
                .player(new IjkMediaPlayer())//设置自定的播放器，可不设置
                .onStartListener(this)//设置播放开始时的监听，可不设置

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

                
改变播放的状态

        //设置播放的各种模式，详细见选项见枚举对象PlayerModel
        vrPlay4Ijk.setPlayModel(PlayerModel.MOTION_TOUCH);
        
        
有任何疑问可以看源码，有demo有注释
