
package src;
import java.util.Scanner;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

import src.Tools;

public class PixelCompute{
    public static final int REPEAT = 3;
    public static final int HALF_HDP = Tools.string2int(Tools.readHead1("half-hdp"));
    public static final float TARGET_H = 0.07f;	//目标色相与0.0f的距离
    public static final float TARGET_S = 1.0f;	//目标饱和度
    public static final double TARGET_MUL_B = 0.5;	//亮度增量乘数

    public static void computeHSB(float[] hsb, int k){
    	hsb[0] = divideLine(hsb[0], (hsb[0]<0.5) ? TARGET_H : (1-TARGET_H), k);
    	hsb[1] = divideLine(hsb[1], TARGET_S, k);
	hsb[2] = divideLine(hsb[2], (float)(hsb[2]+(1-hsb[2])*TARGET_MUL_B), k);
    }
    public static float divideLine(float from, float to, int select){
	    return (from + select*(to-from)/HALF_HDP);
    }
    public static BufferedImage[] ms2frame(BufferedImage m, BufferedImage s) {
	int r = s.getWidth();
	int c = s.getHeight();
	if( r > m.getWidth() || c > m.getHeight() )
		throw new AssertionError();
	int rb=-1,re=0,cb=(c-1),ce=0; // 二维for循环计算 选区 窗口 矩阵 坐标
	for(int i=0; i<r; ++i){
	    for(int j=0; j<c; ++j){
		if( Tools.isChooes( s,i,j ) ){
		    if(rb == -1) rb = i; // 首个选区像素可确定rb
		    re = i; // 最后一个选区像素可确定re
		    cb = (j<cb)?j:cb;
		    ce = (j>ce)?j:ce; 
	   	}
	    }
	}
	//以上均为优化运行时间的设计，如不需要可直接调用 screen2frame(0, r, 0, c, m, s)
	return screen2frame(rb, re, cb, ce, m, s);
    }
    public static BufferedImage[] screen2frame(int rb, int re, int cb, int ce, 
    					BufferedImage m, BufferedImage s) {
	BufferedImage[] frames = new BufferedImage[PixelCompute.HALF_HDP*2];
	frames[0] = m;	//首帧使用（可能被darker()处理过的）m 
/* 三重循环算出半程帧并引用于后半程 ( 以 HALF_HDP==6 , step==30时为例 ) 
 * 帧组下标： 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11（0为原图）
 * 分量深度：30,40,50,60,70,80,max,80,70,60,50,40（100为原图深度）
 */
	float[] hsbvals = new float[3];
	for(int k=1; k <= PixelCompute.HALF_HDP ;k++){ 
	    frames[k] = Tools.imClone(m);
	    for(int x = rb; x <= re; ++x){
	    	for(int y = cb; y <= ce; ++y){
			if( ! Tools.isChooes(s,x,y))
				continue;
			Tools.getHSB(m, x, y, hsbvals); 
			PixelCompute.computeHSB(hsbvals, k);
			frames[k].setRGB(x,y, Color.HSBtoRGB(hsbvals[0],
						hsbvals[1],hsbvals[2]));
	    	}
	    }
	    frames[frames.length-k] = frames[k]; //往返闪烁
	}
	return frames; //REPEAT次一重循环写入帧
    }
    public static void writeFrames(BufferedImage[] frames){
	try{
		for(int t=0;t<REPEAT;++t)
			for(int f=0;f<frames.length;++f)
				ImageIO.write(frames[f], Tools.FRAME_FORMAT, Tools.sOut());
	}catch(IOException ex){
    		ex.printStackTrace();
		throw new AssertionError();
	}
    }
}
//
//class PixelCompute{
//    public static final int HALF_HDP = Tools.string2int(Tools.readHead1("half-hdp"));
//    public static final float TARGET_H = 0.05f;	//目标色相与0.0f的距离
//    public static final float TARGET_S = 1.0f;	//目标饱和度
//    public static final double TARGET_MUL_B = 0.5;	//亮度增量乘数
//
//    public static void computeHSB(float[] hsb, int k){
//    	hsb[0] = divideLine(hsb[0], (hsb[0]<0.5) ? TARGET_H : (1-TARGET_H), k);
//    	hsb[1] = divideLine(hsb[1], TARGET_S, k);
//	hsb[2] = divideLine(hsb[2], (float)(hsb[2]+(1-hsb[2])*TARGET_MUL_B), k);
//    }
//    public static float divideLine(float from, float to, int select){
//	    return (from + select*(to-from)/HALF_HDP);
//    }
//}
