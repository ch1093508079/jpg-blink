package src;
import java.util.Scanner;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class Tools{
    public static final String FILE_SEP = "/";
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
