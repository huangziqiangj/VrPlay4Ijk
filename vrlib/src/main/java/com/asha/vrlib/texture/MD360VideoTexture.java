package com.asha.vrlib.texture;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.google.android.apps.muzei.render.GLTextureView;

import javax.microedition.khronos.opengles.GL10;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360VideoTexture extends MD360Texture {

    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private MDVRLibrary.IOnSurfaceReadyCallback mOnSurfaceReadyListener;
    private float[] mTransformMatrix = new float[16];
    private static final int[] sUseTransform = new int[]{1};

    private int mode=GLTextureView.RENDERMODE_CONTINUOUSLY;

    GLSurfaceView glSurfaceView;
    GLTextureView glTextureView;
    public MD360VideoTexture(MDVRLibrary.IOnSurfaceReadyCallback onSurfaceReadyListener) {
        mOnSurfaceReadyListener = onSurfaceReadyListener;
    }

    public void setGlTextureView(GLTextureView glTextureView) {
        this.glTextureView = glTextureView;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setGlSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }

    public GLSurfaceView getGlSurfaceView() {
        return glSurfaceView;
    }

    public GLTextureView getGlTextureView() {
        return glTextureView;
    }

    @Override
    public void release() {
        mOnSurfaceReadyListener = null;
    }

    @Override
    public void create() {
        super.create();
        int glSurfaceTexture = getCurrentTextureId();
        if (isEmpty(glSurfaceTexture)) return;

        onCreateSurface(glSurfaceTexture);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void destroy() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        mSurfaceTexture = null;

        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = null;
    }

    private void onCreateSurface(int glSurfaceTextureId) {
        if (mSurfaceTexture != null){
            mSurfaceTexture.release();
        }
        //attach the texture to a surface.
        //It's a clue class for rendering an android view to gl level
        mSurfaceTexture = new SurfaceTexture(glSurfaceTextureId);
        // mSurfaceTexture.setDefaultBufferSize(getWidth(), getHeight());

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {

                if (mode!=GLTextureView.RENDERMODE_CONTINUOUSLY){
                    Log.e("=================","=========surfaceTexture==========");
                    if (glTextureView!=null){
                        glTextureView.requestRender();
                    }else if (glSurfaceView!=null){
                        glSurfaceView.requestRender();
                    }
                }

            }
        });
        mSurface = new Surface(mSurfaceTexture);
        if (mOnSurfaceReadyListener != null){
            mOnSurfaceReadyListener.onSurfaceReady(mSurface);
        }
    }

    @Override
    protected int createTextureId() {
        int[] textures = new int[1];

        // Generate the texture to where android view will be rendered
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        glCheck("Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        glCheck("Texture bind");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return textures[0];
    }

    @Override
    public boolean texture(MD360Program program) {
        int glSurfaceTexture = getCurrentTextureId();
        if (isEmpty(glSurfaceTexture)) return false;
        if (mSurfaceTexture == null) return false;

        mSurfaceTexture.updateTexImage();

        // surface transform get
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        GLES20.glUniform1iv(program.getUseTextureTransformHandle(), 1, sUseTransform, 0);
        GLES20.glUniformMatrix4fv(program.getSTMatrixHandle(), 1, false, mTransformMatrix, 0);
        return true;
    }

    @Override
    public void notifyChanged() {
        if (mOnSurfaceReadyListener != null) {
            if (mSurface == null && mSurfaceTexture != null){
                mSurface = new Surface(mSurfaceTexture);
            }
            mOnSurfaceReadyListener.onSurfaceReady(mSurface);
        }
    }

    @Override
    public void releaseSurface() {
        super.releaseSurface();
        if (mSurface != null){
            mSurface.release();
            mSurface = null;
        }
    }
}
