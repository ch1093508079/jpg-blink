package src;
import java.util.Scanner;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

import src.Tools;
import src.PixelCompute;

public class MS2Frame{

    public static final boolean DEBUG = false;
    public static final String VIDEO_PATH = "video";
    
    public static String pathS2M(String path) {
//	int index = FILE_SEP.length() + path.lastIndexOf(FILE_SEP+"S"+FILE_SEP);
	StringBuilder sb = new StringBuilder(path);
//	sb.setCharAt(index, 'M');
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
	
	int tHead1 = Tools.string2int(Tools.readHead1("t"));
	int rHead1 = Tools.string2int(Tools.readHead1("r"));
	int framePerPicture = 2*PixelCompute.HALF_HDP*PixelCompute.REPEAT;
	int pictureCount = tHead1 * rHead1 / framePerPicture;

	int successCount = choosen2frame(args);
	Tools.sErr().println("还剩至少"+pictureCount+"张图片，本轮处理数量："+successCount);
	pictureCount -= successCount;
	if(pictureCount<=0)
		return;	//有待商榷
    }
    public static int choosen2frame(String[] sPaths) throws IOException {
	new File(VIDEO_PATH).mkdir();
	Tools.randomSort(sPaths);
	File sf,mf;
	String sp,mp;
	BufferedImage s,m;
	int successCount = 0;
	for(int k=0; k<sPaths.length; ++k){
		sp = sPaths[k];
		sf = new File(sp);
		mp = pathS2M(sp);
		mf = new File(mp);
		if( ! mf.exists() ){
			Tools.sErr().println("掩码图"+sp+"没找到源图："+mp);
			continue;
		}
		if( mf.isDirectory() || ( ! sf.isFile()) ) 
			throw new AssertionError();
		s = ImageIO.read(sf);
		m = ImageIO.read(mf);
		++successCount;
		writeFrames(PixelCompute.ms2frame(m, s));
	}
	return successCount;
    }
    public static void writeFrames(BufferedImage[] frames){
	try{
		for(int t=0;t<PixelCompute.REPEAT;++t)
			for(int f=0;f<frames.length;++f)
				ImageIO.write(frames[f], Tools.FRAME_FORMAT, Tools.sOut());
	}catch(IOException ex){
    		ex.printStackTrace();
		throw new AssertionError();
	}
    }
}

