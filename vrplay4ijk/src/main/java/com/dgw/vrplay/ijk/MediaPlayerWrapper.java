package com.dgw.vrplay.ijk;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.widget.TableLayout;

import java.io.IOException;
import java.io.InputStream;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 * <p>
 * http://developer.android.com/intl/zh-cn/reference/android/media/MediaPlayer.html
 * status
 */
public class MediaPlayerWrapper implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener {
    protected IjkMediaPlayer mMediaPlayer;
    private IjkMediaPlayer.OnPreparedListener mPreparedListener;
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PREPARING = 1;
    private static final int STATUS_PREPARED = 2;
    private static final int STATUS_STARTED = 3;
    private static final int STATUS_PAUSED = 4;
    private static final int STATUS_STOPPED = 5;
    private int mStatus = STATUS_IDLE;

    private long mPrepareStartTime = 0;
    private long mOpenInputEndTime = 0;
    private long mFindStreamEndTime = 0;
    private long mOpenComponentTime = 0;
    private long mPrepareEndTime = 0;
    private long mFirstVideoPktTime = 0;
    private long mFirstVideoDecodeEndTime = 0;
    private long mFirstVideoDisplayEndTime = 0;
    private long mSeekStartTime = 0;
    private long mSeekEndTime = 0;

    public void init(Context context,IjkMediaPlayer ijkMediaPlayer) {
        mStatus = STATUS_IDLE;
        if (mMediaPlayer == null) {
            if(ijkMediaPlayer!=null){
                mMediaPlayer=ijkMediaPlayer;
            }else{
                mMediaPlayer = new IjkMediaPlayer();
            }
            //如果用户未传入自己的播放器，那么就设置默认参数
            if(ijkMediaPlayer==null){
                Settings mSettings = new Settings(context);
                //是否开启硬件编码
                if (mSettings.getUsingMediaCodec()) {
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L);
                    if (mSettings.getUsingMediaCodecAutoRotate()) {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L);
                    } else {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0L);
                    }

                    if (mSettings.getMediaCodecHandleResolutionChange()) {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L);
                    } else {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0L);
                    }
                } else {
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0L);
                }
                //是否使用opensl es音频解析
                if (mSettings.getUsingOpenSLES()) {
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1L);
                } else {
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0L);
                }

                String pixelFormat = mSettings.getPixelFormat();
                if (TextUtils.isEmpty(pixelFormat)) {
                    //覆盖格式
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", 909203026L);
                } else {
                    mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
                }

                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 0L);
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0L);
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0L);
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0L);
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L);
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 30000000L);


                // whether start play automatically after prepared, default value is 1
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                mMediaPlayer.setLooping(true);//是否开启循环播放
                mMediaPlayer.setScreenOnWhilePlaying(true);//播放时屏幕打开

                mMediaPlayer.setOnInfoListener(this);//播放的监听
                mMediaPlayer.setOnBufferingUpdateListener(this);//缓冲监听
                mMediaPlayer.setOnErrorListener(this);//错误监听
                mMediaPlayer.setOnCompletionListener(this);//完成监听
                mMediaPlayer.setOnVideoSizeChangedListener(this);//视频大小改变监听
            }
            mMediaPlayer.setOnPreparedListener(this);//资源准备完成监听

        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_OPEN_INPUT:
                mOpenInputEndTime = System.currentTimeMillis();
                if (mHudViewHolder != null)
                    mHudViewHolder.updateOpenInputCost(mOpenInputEndTime - mPrepareStartTime);

                break;
            case IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO:
                mFindStreamEndTime = System.currentTimeMillis();
                IjkMediaPlayer ijk = (IjkMediaPlayer) mMediaPlayer;
                if (mHudViewHolder != null)
                    mHudViewHolder.updateFindStreamCost(mFindStreamEndTime - mPrepareStartTime, ijk.getTrafficStatisticByteCount());
                break;
            case IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN:
                mOpenComponentTime = System.currentTimeMillis();
                if (mHudViewHolder != null)
                    mHudViewHolder.updateOpenComponentCost(mOpenComponentTime - mPrepareStartTime);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_FIRSTPKT_GOT:
                mFirstVideoPktTime = System.currentTimeMillis();
                if (mHudViewHolder != null)
                    mHudViewHolder.updateFirstVideoPktCost(mFirstVideoPktTime - mPrepareStartTime);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_DECODED_START:
                mFirstVideoDecodeEndTime = System.currentTimeMillis();
                if (mHudViewHolder != null)
                    mHudViewHolder.updateFirstVideoDecodeCost(mFirstVideoDecodeEndTime - mPrepareStartTime);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mFirstVideoDisplayEndTime = System.currentTimeMillis();
                if (mHudViewHolder != null)
                    mHudViewHolder.updateFirstVideoDisplayCost(mFirstVideoDisplayEndTime - mPrepareStartTime);
                break;
//                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
//                    //当前播放地址等于回调的播放地址，视频真正开始播放才显示,防止透明跟闪屏
//                    if (iMediaPlayer.getDataSource().equals(mCurrentPlayUrl)) {
//                        mTextureView.setVisibility(View.VISIBLE);
//                        beginSeekbarUpdate();
//                        PrintLog.e(TAG, "test MEDIA_INFO_VIDEO_RENDERING_START");
//                    }
//                    break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
//                    mTextureView.setDisplayOrientation(360 - extra);
                break;
//                case IMediaPlayer.MEDIA_INFO_AUDIO_SEEK_RENDERING_START:
//                    if (mVideoPlayListener != null) {
//                        mVideoPlayListener.onComplete(iMediaPlayer);
//                    }
//                    break;
            default:
                break;
        }
//            if (mVideoPlayListener != null) {
//                mVideoPlayListener.onInfo(iMediaPlayer, what, extra);
//            }
        return false;
    }

    public void setSurface(Surface surface) {
        if (getPlayer() != null) {
            getPlayer().setSurface(surface);
        }
    }

    public void openRemoteFile(String url) {
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAssetFile(Context context, String assetPath) {
        try {
            AssetManager am = context.getResources().getAssets();
            final InputStream is = am.open(assetPath);
            mMediaPlayer.setDataSource(new IMediaDataSource() {
                @Override
                public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                    return is.read(buffer, offset, size);
                }

                @Override
                public long getSize() throws IOException {
                    return is.available();
                }

                @Override
                public void close() throws IOException {
                    is.close();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IjkMediaPlayer getPlayer() {
        return mMediaPlayer;
    }

    public void prepare() {
        if (mMediaPlayer == null) return;
        if (mStatus == STATUS_IDLE || mStatus == STATUS_STOPPED) {
            mMediaPlayer.prepareAsync();
            mPrepareStartTime = System.currentTimeMillis();
            mStatus = STATUS_PREPARING;
        }
        if (mHudViewHolder != null)
            mHudViewHolder.setMediaPlayer(mMediaPlayer);
    }

    public void stop() {
        if (mMediaPlayer == null) return;
        if (mStatus == STATUS_STARTED || mStatus == STATUS_PAUSED) {
            mMediaPlayer.stop();
            mStatus = STATUS_STOPPED;
        }
        if (mHudViewHolder != null)
            mHudViewHolder.setMediaPlayer(null);
    }

    public void pause() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying() && mStatus == STATUS_STARTED) {
            mMediaPlayer.pause();
            mStatus = STATUS_PAUSED;
        }
    }

    public void start() {
        if (mMediaPlayer == null) return;
        if (mStatus == STATUS_PREPARED || mStatus == STATUS_PAUSED) {
            mMediaPlayer.start();
            mStatus = STATUS_STARTED;
        }

    }

    public void setPreparedListener(IMediaPlayer.OnPreparedListener mPreparedListener) {
        this.mPreparedListener = mPreparedListener;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        if (mHudViewHolder != null) {
            mPrepareEndTime = System.currentTimeMillis();
            mHudViewHolder.updateLoadCost(mPrepareEndTime - mPrepareStartTime);
        }
        mStatus = STATUS_PREPARED;
        Log.e("===========","==========onPrepared======");
        start();
        if (mPreparedListener != null) mPreparedListener.onPrepared(mp);
    }

    public void resume() {
        start();
    }

    public void destroy() {
        stop();
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(null);
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sar_num, sar_den);
        }
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den);
    }

    public OnVideoSizeChangedListener mOnVideoSizeChangedListener = null;

    public final void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }


    private InfoHudViewHolder mHudViewHolder;

    public void setHudView(TableLayout tableLayout, Context context) {
        mHudViewHolder = new InfoHudViewHolder(context, tableLayout);
    }


}
