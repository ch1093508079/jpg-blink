package src;

import javax.crypto.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.*;

/**此类配置全局参数以及为UI层提供工具函数*/
class Tool{
    public static final boolean DEBUG = false;
    public static final int CLOCK_HDP = DEBUG ? 500 : 4000;
    public static final int QUEUE_SIZE = 3;
    public static final int SCALE_HINTS = DEBUG ? Image.SCALE_FAST : Image.SCALE_SMOOTH;
    public static final int MY_PANEL_HEIGHT = FileOperate.IS_WIN ? 705 : 700;
    public static final int MIN_PASSWORD_LENGTH = DEBUG ? 0 : 12;
    public static final String DEFAULT_PASSWORD = DEBUG ? "p" : "";
    public static final String PATH_PROPERTIES = DEBUG ? "TEST.XML" : "HasRead.xml";
    public static final String DEFAULT_REG = DEBUG ? "." : "【M】【|【M】[^【]";
    public static final String DEFAULT_PROP_VALUE = "0";
    public static final String TEXT_NEXT = "→";
    public static final String CURRENT_DIRECTORY = FileOperate.WAREHOUSE_DIR+FileOperate.SEP+FileOperate.cWAREHOUSE;
    
    
    public static CipherInputStream doGetInputStream(final SecretKey sks, File file){
        return FileOperate.getInputStream(sks, file);
    }
    public static void doExport(final SecretKey sks, File file){
        FileOperate.exportFa(sks, file);
    }
    public static void doImport(final SecretKey sks){
        var message = FileOperate.importAll(sks);
        if(message.equals(FileOperate.M_IMPORT_SUCCESS))
            JOptionPane.showMessageDialog(null,"入库完成，请及时销毁明文");
        else
            JOptionPane.showMessageDialog(null,"importAll STATU : "+message);
    }
    public static Image scalePH(BufferedImage source) {
        int autoWidth = MY_PANEL_HEIGHT * source.getWidth() / source.getHeight();
        return source.getScaledInstance(autoWidth, MY_PANEL_HEIGHT, SCALE_HINTS);
    }
    public static SecretKey passwordDialog() {
        String pw = JOptionPane.showInputDialog(null,"输入密码", DEFAULT_PASSWORD);
        if( pw == null || pw.length() <= MIN_PASSWORD_LENGTH ){
            JOptionPane.showMessageDialog(null, "密码过短，程序退出");
            System.exit(0);
        }
        return FileOperate.password2Key(pw);
    }
    public static int chooseMinutes() {
        int m = JOptionPane.showOptionDialog(null,"请选择阅读时间（分钟）",
                "看几分钟？", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"1","6","11","16","21","26"}, "16");
        return 5*m + 1;
    }
    public static int minutesToCount(int m) {
        return m * 60000 / CLOCK_HDP;
    }
    public static Properties readProp() {
        Properties prop = new Properties();
        try {
            prop.loadFromXML(new FileInputStream(PATH_PROPERTIES));
        } catch (FileNotFoundException e) {//FileNotFound则新建
            prop = new Properties();
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
            System.out.println("程序退出：配置文件的语法格式被破坏");
            System.exit(0xf0);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
        return prop;
    }
    public static void saveProp(Properties prop) {
        try {
            var out = new FileOutputStream(PATH_PROPERTIES);
            prop.storeToXML(out, new Date().toString());
            out.close();
        } catch (IOException e) {
            int option = JOptionPane.showConfirmDialog(null, e.getLocalizedMessage(),
                    "写配置文件异常，是否忽视异常继续运行？", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.NO_OPTION){
                e.printStackTrace();
                System.exit(0xa0);
            }
            if(DEBUG) System.out.println("忽视配置文件异常，请谨慎处理仓库数据");
        }
    }
    public static void savePropAsk(Properties xml) {
        int option = JOptionPane.showConfirmDialog(null, "更新配置文件吗？",
                "选“是”写入配置文件；选“否”重新读入配置文件", JOptionPane.YES_NO_CANCEL_OPTION);
        if(option == JOptionPane.YES_OPTION) Tool.saveProp(xml);
        if(option == JOptionPane.NO_OPTION) Tool.readProp();
    }
    private static String fileToEntry(File f){
        String path = f.getPath();
        var i = path.lastIndexOf(CURRENT_DIRECTORY);
        if(i==-1) return path; //找不到路径分割符则返回原串
        return path.substring(i);
    }
    public static void increaseProp(File f, Properties prop) {
        var entry = Tool.fileToEntry(f);
        var value = prop.getProperty(entry, DEFAULT_PROP_VALUE);
        var trans = Integer.toString(1+Integer.decode(value));
        prop.setProperty(entry, trans);
    }
    public static boolean isNewPicture(File f, Properties prop) {
        var value = prop.getProperty(fileToEntry(f), DEFAULT_PROP_VALUE);
        return value.equals(DEFAULT_PROP_VALUE);
    }
    public static boolean matchPrefix(String s, String reg) {
        return s.matches("(" + reg + ").*" );
    }
    public static File[] openFileChooser(int mode, File[] cancel) {
        final JFileChooser fc = new JFileChooser();
        File current = new File(CURRENT_DIRECTORY);
        if(current.exists()) fc.setCurrentDirectory(current);
        fc.setFileSelectionMode(mode);
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("按住Ctrl可选中多个目录");
        int returnVal = fc.showOpenDialog(null);
        if(returnVal==JFileChooser.APPROVE_OPTION)
            return fc.getSelectedFiles();
        else
            return cancel;
    }
    public static File[] defaultIfCancel() {
        String cw = CURRENT_DIRECTORY;
        var allCipherText = new File(cw).listFiles();
        if(allCipherText==null) {
            JOptionPane.showMessageDialog(null, "Returns null if this abstract pathname does not denote a directory, or if an I/O error occurs.");
            System.exit(0xce);
        }
        if(allCipherText.length==0){
            JOptionPane.showMessageDialog(null,"空目录："+cw);
            System.exit(0xb1a);
        }
        var dirs = allCipherText[0].listFiles();
        if(dirs==null) throw new AssertionError();
        return dirs;
    }
    public static void randomSortPrefix(Random r, ArrayList<File> array, int count) {
        if(array.size() <= 1) return;
        if(DEBUG) System.out.println("即将重排"+count+"个File");
        File swap;
        int target;
        for(int i=0;i<count;++i){
            target = r.nextInt(array.size());
            swap = array.get(i);
            array.set(i, array.get(target));
            array.set(target, swap);
        }
    }
}
