package com.dgw.vrplay.play;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.widget.TableLayout;
import android.widget.Toast;
import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.dgw.vrplay.ijk.MediaPlayerWrapper;
import com.google.android.apps.muzei.render.GLTextureView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import static android.animation.PropertyValuesHolder.ofFloat;
import static com.asha.vrlib.MDVRLibrary.DISPLAY_MODE_NORMAL;
import static com.asha.vrlib.MDVRLibrary.INTERACTIVE_MODE_MOTION;

/**
 * Created by huangzhiqiang
 * huangziqiangj@126.com
 */
public class VrPlay4Ijk {

    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private MDVRLibrary mVRLibrary;
    private StartListener startListener;
    private Activity context;
    private Uri uri;
    private IjkMediaPlayer mMediaPlayer;
    private GLTextureView glTextureView;
    private TableLayout tableLayout;

    public static VrPlay4Ijk with(Activity context) {
        return new VrPlay4Ijk(context);
    }

    private VrPlay4Ijk(Activity context) {
        this.context = context;
    }

    //设置自定义的播放器
    public VrPlay4Ijk player(IjkMediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
        return this;
    }

    public VrPlay4Ijk into(GLTextureView glTextureView) {
        this.glTextureView = glTextureView;
        initData();
        return this;
    }

    public VrPlay4Ijk statsView(TableLayout tableLayout) {
        this.tableLayout = tableLayout;
        return this;
    }

    public VrPlay4Ijk onStartListener(StartListener listener) {
        this.startListener = listener;
        return this;
    }

    public VrPlay4Ijk uri(Uri uri) {
        this.uri = uri;
        return this;
    }

    /**
     * 修改播放状态，可选参数见枚举类@Configuration
     *
     * @param playModel 枚举类
     */
    public void setPlayModel(PlayerModel playModel) {
        if (mVRLibrary == null) return;
        switch (playModel) {
            case NORMAL://正常模式
                mVRLibrary.switchDisplayMode(context, MDVRLibrary.DISPLAY_MODE_NORMAL);
                break;
            case GLASS://分屏模式
                mVRLibrary.switchDisplayMode(context, MDVRLibrary.DISPLAY_MODE_GLASS);
                break;
            case MOTION://陀螺仪
                mVRLibrary.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_SPHERE);
                mVRLibrary.switchInteractiveMode(context, MDVRLibrary.INTERACTIVE_MODE_MOTION);
                break;
            case TOUCH://触摸
                mVRLibrary.switchInteractiveMode(context, MDVRLibrary.INTERACTIVE_MODE_TOUCH);
                break;
            case MOTION_TOUCH://触摸和陀螺仪都开启
                mVRLibrary.switchInteractiveMode(context, MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH);
                break;
            case SPHERE://全景模式
                mVRLibrary.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_SPHERE);
                break;
            case PLANE://适配平板
                mVRLibrary.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_PLANE_FIT);
                break;
            case PLANE_CROP://平面裁切
                mVRLibrary.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_PLANE_CROP);
                break;
            case PLAN_FULL://平铺满屏
                mVRLibrary.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_PLANE_FULL);
                break;
            case ANTI_ENABLE://桶形畸变
                mVRLibrary.setAntiDistortionEnabled(true);
                break;
            case ANTI_DISABLE://取消畸变
                mVRLibrary.setAntiDistortionEnabled(false);
                break;
            case CAMERA_LITTLE_PLANET://360度圆形星球模式
                MDDirectorCamUpdate cameraUpdate = mVRLibrary.updateCamera();
                PropertyValuesHolder near = ofFloat("near", cameraUpdate.getNearScale(), -0.5f);
                PropertyValuesHolder eyeZ = ofFloat("eyeZ", cameraUpdate.getEyeZ(), 18f);
                PropertyValuesHolder pitch = ofFloat("pitch", cameraUpdate.getPitch(), 90f);
                PropertyValuesHolder yaw = ofFloat("yaw", cameraUpdate.getYaw(), 90f);
                PropertyValuesHolder roll = ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
                break;
            case CAMERA_LITTLE_PLANET_CANCEL://取消星球模式
                cameraUpdate = mVRLibrary.updateCamera();
                near = ofFloat("near", cameraUpdate.getNearScale(), 0f);
                eyeZ = ofFloat("eyeZ", cameraUpdate.getEyeZ(), 0f);
                pitch = ofFloat("pitch", cameraUpdate.getPitch(), 0f);
                yaw = ofFloat("yaw", cameraUpdate.getYaw(), 0f);
                roll = ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
                break;
            case PLAY://播放
                mMediaPlayerWrapper.start();
                break;
            case PAUSE://暂停
                mMediaPlayerWrapper.pause();
                break;
            case STOP://暂停
                try {
                    mMediaPlayerWrapper.stop();
                }catch (Exception e){
                    Log.e(this.getClass().getName(),"stop error",e);
                }
                break;
        }
    }

    private void initData() {
        // init VR Library
        mVRLibrary = createVRLibrary();
        mMediaPlayerWrapper.init(context, mMediaPlayer);
        if (tableLayout != null) {
            mMediaPlayerWrapper.setHudView(tableLayout, context);
        }
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if(startListener!=null){
                    startListener.start();
                }
                if (mVRLibrary != null) {
                    mVRLibrary.notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.setOnVideoSizeChangedListener(new MediaPlayerWrapper.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                mVRLibrary.onTextureResize(width, height);
            }
        });
        if (uri != null) {
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }

    }


    public void onResume() {
        mVRLibrary.onResume(context);
        mMediaPlayerWrapper.resume();
    }

    public void onPause() {
        mVRLibrary.onPause(context);
        mMediaPlayerWrapper.pause();
    }

    public void onDestroy() {
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.destroy();
    }


    public void onConfigurationChanged(Configuration newConfig) {
        mVRLibrary.onOrientationChanged(context);
    }


    private MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(context)
                .displayMode(DISPLAY_MODE_NORMAL)
                .interactiveMode(INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        Log.e("===========", "==========onSurfaceReady======");
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(glTextureView);

    }


    private ValueAnimator animator;

    private void startCameraAnimation(final MDDirectorCamUpdate cameraUpdate, PropertyValuesHolder... values) {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofPropertyValuesHolder(values).setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float near = (float) animation.getAnimatedValue("near");
                float eyeZ = (float) animation.getAnimatedValue("eyeZ");
                float pitch = (float) animation.getAnimatedValue("pitch");
                float yaw = (float) animation.getAnimatedValue("yaw");
                float roll = (float) animation.getAnimatedValue("roll");
                cameraUpdate.setEyeZ(eyeZ).setNearScale(near).setPitch(pitch).setYaw(yaw).setRoll(roll);
            }
        });
        animator.start();
    }


    public boolean isPlaying() {
        return mMediaPlayerWrapper.getPlayer().isPlaying();
    }

    public IjkMediaPlayer getMediaPlayer() {
        return mMediaPlayerWrapper.getPlayer();
    }

    public void start() {
        setPlayModel(PlayerModel.PLAY);
    }

    public void stop() {
        setPlayModel(PlayerModel.STOP);
    }

    public void pause() {
        setPlayModel(PlayerModel.PAUSE);
    }

}
