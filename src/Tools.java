package src;
import java.util.Scanner;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class Tools{
    public static final String FILE_SEP = "/";
    public static final String FRAME_FORMAT = Tools.readHead1("frame-format");
    private static final int F_OUT_DEBUG = Tools.string2int(Tools.readHead1("f-out-debug"));
    private static final String F_OUT_PATH = "F_OUT_DEBUG";
    private static int F_OUT_SUFFIX = 0;
    public static OutputStream sOut() {
	if( F_OUT_DEBUG == 0 ) return System.out;
	++F_OUT_SUFFIX; 
	try{
		new File(F_OUT_PATH).mkdir();
		String name = F_OUT_SUFFIX+"."+FRAME_FORMAT;
		return new FileOutputStream(F_OUT_PATH + Tools.FILE_SEP + name);
	}catch(FileNotFoundException ex){
    		ex.printStackTrace();
		throw new AssertionError();
	}
    }
    public static void randomSort(String[] array) {
	    java.util.Random r = new java.util.Random();
	    for(int i=0; i < array.length ; ++i){
		    int t = r.nextInt(array.length);
		    String temp = array[i];
		    array[i] = array[t];
		    array[t] = temp;
	    }
    }
    public static final int whiteRGB = Color.WHITE.getRGB();
    /** 当前版本只有 s 中的纯白像素才是选区 */
    public static boolean isChooes(BufferedImage s, int x, int y) {
	return s.getRGB(x,y) == whiteRGB;
    }
    /** 获取指定坐标的hsb值并修改实际参数 */
    public static void getHSB(BufferedImage m, int x, int y,float[] hsb){
	int rgb = m.getRGB(x,y);
	Color.RGBtoHSB( (rgb>>>16)&0xff, (rgb>>>8)&0xff, rgb&0xff, hsb);
    }
    public static BufferedImage imClone(BufferedImage from) {
	BufferedImage to = new BufferedImage(from.getWidth(), from.getHeight(), from.getType());
        to.setData(from.getData());
	return to;
    }
    public static String readHead1(String configName){
    	try{
    		String path = "config"+FILE_SEP+configName+".head-1";
    		FileInputStream fOut = new FileInputStream(path);
    		return new Scanner(fOut).nextLine();
    	}catch(FileNotFoundException ex){
    		ex.printStackTrace();
    		throw new AssertionError();
    	}
    }
    public static PrintStream sErr() {
	    return System.err;
    }
    public static int string2int(String str){
    	try {
    		return Integer.parseInt(str);
    	} catch (NumberFormatException ex) {
    		ex.printStackTrace();
    		throw new AssertionError();
    	}
    }
}
