package src;
import java.util.Scanner;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final String FRAME_FORMAT = "bmp";	//需考虑ffmpeg能否解码
    public static final String FILE_SEP = "/";
    
    private static final boolean F_OUT_DEBUG = false;
    private static final String F_OUT_PATH = "F_OUT_DEBUG";
    private static int F_OUT_SUFFIX = 0;
    private static OutputStream sOut() { //TODO:外置类，cmake
	if( ! F_OUT_DEBUG) return System.out;
	++F_OUT_SUFFIX; 
	try{
		new File(F_OUT_PATH).mkdir();
		String name = F_OUT_SUFFIX+"."+FRAME_FORMAT;
		return new FileOutputStream(F_OUT_PATH + FILE_SEP + name);
	}catch(FileNotFoundException ex){
		throw new AssertionError();
	}
    }
    private static PrintStream sErr() {
	    return System.err;
    }
    private static int readNoLine(String configName){
    	try{
    		String path = "config"+FILE_SEP+configName+".NoLine";
    		FileInputStream fOut = new FileInputStream(path);
    		return new Scanner(fOut).nextInt();
    	}catch(FileNotFoundException ex){
    		throw new AssertionError();
    	}
    }

    public static final boolean DEBUG = false;
    public static final int REPEAT = ( F_OUT_DEBUG ? 1 : ( DEBUG ? 2 : 3 ) );
    public static final int HALF_HDP = 8;
    public static final float TARGET_H = 0.05f;	//目标色相与0.0f的距离
    public static final float TARGET_S = 1.0f;	//目标饱和度
    public static final double TARGET_MUL_B = 0.5;	//亮度增量乘数
    
    public static void randomSort(File[] array) {
	    java.util.Random r = new java.util.Random();
	    for(int i=0; i < array.length ; ++i){
		    int t = r.nextInt(array.length);
		    File temp = array[i];
		    array[i] = array[t];
		    array[t] = temp;
	    }
    }
    public static String pathS2M(String path) {
	int index = FILE_SEP.length() + path.lastIndexOf(FILE_SEP+"S"+FILE_SEP);
	StringBuilder sb = new StringBuilder(path);
	sb.setCharAt(index, 'M');
	return sb.substring( 0 , path.lastIndexOf('.') );//去掉后缀
    }
    public static void main(String[] args) throws IOException {
	if(args == null || args.length == 0){
		System.err.println("需要参数：指示选区的掩码图片的路径集");
		return;
	}
	if(args.length == 1 && args[0].indexOf('*') != -1){
		System.err.println(args[0] + "为空，程序退出");
		return;
	}
	/*
	java.util.Scanner scan = new java.util.Scanner(System.in);
	String password = scan.nextline();
	SecretKey sks = new SecretKey(password);
	*/
	
	int pictureCount = readNoLine("t")*readNoLine("r") / (2*HALF_HDP*REPEAT);
	
	File dir;
	for(int k=0;k<args.length;++k){
		dir = new File(args[k]);
		int successCount = choosen2frame(dir.listFiles());
		sErr().println("还剩至少"+pictureCount+"张图片，本轮处理数量："+successCount);
		sErr().println("当前文件夹："+dir.getPath());
		pictureCount -= successCount;
		if(pictureCount<=0)
			break;
	}
    }
    public static int choosen2frame(File[] sPaths) throws IOException {
	randomSort(sPaths);
	File sf,mf;
	BufferedImage s,m;
	int successCount = 0;
	for(int k=0; k<sPaths.length; ++k){
		sf = sPaths[k];
		mf = new File(pathS2M(sf.getPath()));
		if( ! mf.exists() ){
			sErr().println("没找到："+mf.getPath());
			continue;
		}
		if( mf.isDirectory() || ( ! sf.isFile()) ) 
			throw new AssertionError();
		//TODO: Fileperate.getInputStream(f, sks);
		s = ImageIO.read(sf);
		m = ImageIO.read(mf);
		++successCount;
		ms2frame(m, s);
	}
	return successCount;
    }
    public static void ms2frame(BufferedImage m, BufferedImage s) {
	int r = s.getWidth();
	int c = s.getHeight();
	if( r > m.getWidth() || c > m.getHeight() ) throw new AssertionError();
	int rb=-1,re=0,cb=(c-1),ce=0; // 二维for循环计算 选区 窗口 矩阵 坐标
	for(int i=0; i<r; ++i){
	    for(int j=0; j<c; ++j){
		if( isChooes( s,i,j ) ){
		    if(rb == -1) rb = i; // 首个选区像素可确定rb
		    re = i; // 最后一个选区像素可确定re
		    cb = (j<cb)?j:cb;
		    ce = (j>ce)?j:ce; 
	   	}
	    }
	}
	BufferedImage[] frames = new BufferedImage[HALF_HDP*2];
	frames[0] = m;	//首帧使用（可能被darker()处理过的）m 
/* 三重循环算出半程帧并引用于后半程 ( 以 HALF_HDP==6 , step==30时为例 ) 
 * 帧组下标： 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11（0为原图）
 * 分量深度：30,40,50,60,70,80,max,80,70,60,50,40（100为原图深度）
 */
	float[] hsbvals = new float[3];
	for(int k=1; k <= HALF_HDP ;k++){ 
	    frames[k] = imClone(m);
	    for(int x = rb; x <= re; ++x){
	    	for(int y = cb; y <= ce; ++y){
			if( ! isChooes(s,x,y))
				continue;
			getHSB(m, x, y, hsbvals); 
			computeHSB(hsbvals, k);
			frames[k].setRGB(x,y, Color.HSBtoRGB(hsbvals[0],
						hsbvals[1],hsbvals[2]));
	    	}
	    }
	    frames[frames.length-k] = frames[k]; //往返闪烁
	}
	writeFrames(frames); //REPEAT次一重循环写入帧
    }
    public static void computeHSB(float[] hsb, int k){
    	hsb[0] = divideLine(hsb[0], (hsb[0]<0.5) ? TARGET_H : (1-TARGET_H), k);
    	hsb[1] = divideLine(hsb[1], TARGET_S, k);
	hsb[2] = divideLine(hsb[2], (float)(hsb[2]+(1-hsb[2])*TARGET_MUL_B), k);
    }
    public static float divideLine(float from, float to, int select){
	    return (from + select*(to-from)/HALF_HDP);
    }
    public static void writeFrames(BufferedImage[] frames){
	try{
		for(int t=0;t<REPEAT;++t)
			for(int f=0;f<frames.length;++f)
				ImageIO.write(frames[f], FRAME_FORMAT, sOut());
	}catch(IOException ex){
		throw new AssertionError();
	}
    }
    public static BufferedImage imClone(BufferedImage from) {
	BufferedImage to = new BufferedImage(from.getWidth(), from.getHeight(), from.getType());
        to.setData(from.getData());
	return to;
    }
    public static void getHSB(BufferedImage m, int x,int y,float[] hsb){
	int rgb = m.getRGB(x,y);
	Color.RGBtoHSB( (rgb>>>16)&0xff, (rgb>>>8)&0xff, rgb&0xff, hsb);
    }
    /** 当前版本只有 s 中的纯白像素才是选区 */
    public static boolean isChooes(BufferedImage s, int x, int y) {
	return s.getRGB(x,y) == Color.WHITE.getRGB();
    }
}
