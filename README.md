# VrPlay4Iik
基于ijk与MD360Play封装的VR播放器，调用简单、逻辑清晰、兼容性好

基本使用
两种使用方案，第一种
1、将arr包拷贝到项目中libs文件夹下
2、在build.gradle中加入

allprojects {
    repositories {
        //加入这个
        flatDir {
            dirs 'libs'
        }
        google()
        jcenter()
    }
}
3、在module的build.gradle中加入
implementation(name: 'vrPlay4Ijk', ext: 'aar')


完成包引入后即可使用
GLTextureView glTextureView = findViewById(R.id.gl_view);
File file = new File("/storage/self/primary/Download/testvr.mp4");
Uri uri = Uri.fromFile(file);
TableLayout tableLayout= findViewById(R.id.hud_view);

VrPlay4Ijk vrPlay4Ijk = VrPlay4Ijk
                .with(this)//Activity对象（必选）
                .uri(uri)//播放源，支持本地视频，和网络流（必选）

                .statsView(tableLayout)//用来查看播放时的参数，可删除
                .player(new IjkMediaPlayer())//设置自定的播放器，可删除
                .onStartListener(this)//设置播放开始时的监听，可删除

                .into(glTextureView);//用来播放视频的控件（必选）
完成事件的传递

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

                
改变播放的状态
        //设置播放的各种模式，详细见选项见枚举对象PlayerModel
        vrPlay4Ijk.setPlayModel(PlayerModel.MOTION_TOUCH);
        
        
有任何疑问可以看源码，有demo有注释
