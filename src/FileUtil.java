import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    // 文件路径，自动生成在项目文件夹
    public static final String USER_FILE = "user.txt";
    public static final String GROUP_FILE = "group.txt";
    public static final String CONTACT_FILE = "contact.txt";

    // 初始化文件，不存在自动创建
    static {
        createFile(USER_FILE);
        createFile(GROUP_FILE);
        createFile(CONTACT_FILE);
    }

    private static void createFile(String path) {
        File f = new File(path);
        try {
            if(!f.exists()) f.createNewFile();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 读取全部行
    public static List<String> readAllLines(String path) {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while((line=br.readLine())!=null){
                // 过滤纯空白、空字符串
                String trimLine = line.trim();
                if(!trimLine.isEmpty()){
                    list.add(trimLine);
                }
            }
        }catch (IOException e){e.printStackTrace();}
        return list;
    }

    public static void writeAll(String path, List<String> data) {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))){
            for(String s : data) pw.println(s);
        }catch (IOException e){e.printStackTrace();}
    }

    public static void writeLine(String path, String line) {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path,true))){
            pw.println(line);
        }catch (IOException e){e.printStackTrace();}
    }
}