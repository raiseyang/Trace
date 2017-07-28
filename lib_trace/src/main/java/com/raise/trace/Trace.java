package com.raise.trace;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * How to use.
 * Trace.d(TAG, " d");
 * Trace.i(TAG, " i");
 * Trace.w(TAG, " w");
 * Trace.e(TAG, " e = " + null);
 * Trace.d(TAG, " %s,%d", "raise", 1);
 * Trace.i(TAG, " %s,%d", "raise", 1);
 * Trace.w(TAG, " %s,%d", "raise", 1);
 * Trace.e(TAG, new NullPointerException("fu*k null pointer exception."));
 * Trace.json(TAG, "{\"name\":\"BeJson\",\"url\":\"http://www.bejson.com\",\"page\":88,\"isNonProfit\":true}");
 * Trace.array(TAG, new String[]{"value1", "value2"});
 * Trace.list(TAG, Arrays.asList("list1", "list2", "list3"));
 * Trace.xml(TAG,"<student><age>12</age><name>jack</name><skill><language>chinese</language><run>22</run></skill></student>");
 * Trace.file(TAG, new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.log"));
 */
public class Trace {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int NONE = 7; // 关闭日志

    private static int s_level;//日志级别
    private static boolean s_show_code_position; //是否显示 调用位置
    private static boolean s_write_file; //是否写文件
    private static int offset;

    private static String m_log_path;
    private static int s_log_size = 500;

    private static String global_tag;

    static {
        s_level = NONE;
        s_show_code_position = false;
        s_write_file = true;
        global_tag = "iport/";
        m_log_path = Environment.getExternalStorageDirectory() + "/iport_log.txt";
    }

    public static void setShowPosition(boolean showPosition) {
        s_show_code_position = showPosition;
    }

    private static void println(int level, String tag, String message) {
        if (level >= s_level) {//过滤级别
            if (s_write_file) {
                write_log(tag, message);
            }
            if (s_show_code_position) {
                message += getCodePosition();
            }
            printAndroidLog(level, global_tag + tag, message);
        }
    }

    private static void printAndroidLog(int level, String tag, String message) {
        switch (level) {
            case VERBOSE:
                Log.v(tag, message);
                break;
            case DEBUG:
                Log.d(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case WARN:
                Log.w(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
        }
    }

    public static void setLevel(int level) {
        s_level = level;
    }

    /**
     * 是否写日志文件
     *
     * @param write_file
     */
    public static void write_file(boolean write_file) {
        s_write_file = write_file;
    }

    /**
     * 日志文件大小，单位：KB,超过此大小会重新读写文件,默认值 500
     */
    public static void setLog_size(int log_size) {
        s_log_size = log_size;
    }

    /**
     * 设置日志保存路径
     *
     * @param m_log_path
     */
    public static void setLog_path(String m_log_path) {
        Trace.m_log_path = m_log_path;
    }

    public static void v(String tag, String msg) {
        println(2, tag, msg);
    }

    public static void d(String tag, String msg) {
        println(3, tag, msg);
    }

    public static void d(String tag, String msg, Object... args) {
        println(3, tag, String.format(msg, args));
    }

    public static void i(String tag, String msg) {
        println(4, tag, msg);
    }

    public static void i(String tag, String msg, Object... args) {
        println(4, tag, String.format(msg, args));
    }

    public static void w(String tag, String msg, Object... args) {
        println(5, tag, String.format(msg, args));
    }

    public static void w(String tag, String msg) {
        println(5, tag, msg);
    }

    public static void e(String tag, String msg) {
        println(6, tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        println(6, tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, Throwable tr) {
        println(6, tag, getStackTraceString(tr));
    }

    public static <T> void array(String tag, T[] array) {
        offset = 1;
        if (array == null) {
            e(tag, "array is null");
        } else {
            d(tag, Arrays.toString(array));
        }
    }

    public static void list(String tag, List<?> lists) {
        offset = 1;
        if (lists == null) {
            e(tag, "lists is null");
        } else {

            int iMax = lists.size() - 1;
            if (iMax == -1)
                d(tag, "{}");
            else {
                StringBuilder b = new StringBuilder();
                b.append('{');
                for (int i = 0; i < lists.size(); i++) {
                    b.append(String.valueOf(lists.get(i)));
                    if (i == iMax)
                        b.append('}');
                    b.append(", ");
                }
                d(tag, b.toString());
            }
        }
    }

    public static void json(String tag, String json) {
        offset = 1;
        if (TextUtils.isEmpty(json)) {
            e(tag, "Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(2);
                d(tag, message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(2);
                d(tag, message);
                return;
            }
            e(tag, "Invalid Json");
        } catch (JSONException e) {
            e(tag, "Invalid Json");
        }
    }

    public static void xml(String tag, String xml) {
        offset = 1;
        if (TextUtils.isEmpty(xml)) {
            e(tag, "Empty/Null xml content");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            xml = xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
            d(tag, xml);
        } catch (Exception e) {
            e(tag, "Invalid Xml");
        }
    }

    public static void file(String tag, File file) {
        offset = 1;
        if (file == null || !file.exists()) {
            e(tag, "Empty/Null file");
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024 * 4];
            fis.read(bytes);
            String content = new String(bytes, Charset.defaultCharset());
            String result = String.format("file name:%s,file size:%s\n%s", file.getName(), file.length(), content);
            d(tag, result);
        } catch (Exception e) {
            e(tag, "Invalid Xml");
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 显示当前打印日志代码位置
     *
     * @return
     */
    private static String getCodePosition() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        int index = 5 + offset;
        offset = 0;
        String className = stackTrace[index].getFileName();
        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();
//       return " at "+stackTrace[index].toString();
        return String.format(".(%s:%s) %s()", className, lineNumber, methodName);
    }

    private static boolean check_log_file() {
        File log_file = new File(m_log_path);
        if (!log_file.exists()) {
            if (!log_file.getParentFile().exists()) {
                boolean mkdirs = log_file.getParentFile().mkdirs();//父目录不存在创建
                if (!mkdirs) {
                    Log.e(global_tag, getStackTraceString(new IOException("Can't create the directory of trace. Please check the trace path.")));
                    return false;
                }
            }
            try {
                log_file.createNewFile();
            } catch (IOException e) {
                Log.e("Trace", getStackTraceString(e));
                return false;
            }
        } else {
            // 日志文件备份
            if (log_file.length() > 1024 * s_log_size)
                log_file.renameTo(new File(m_log_path + "(1)"));
        }
        return true;
    }

    private static void write_log(String tag, String msg) {
        File log_file = new File(m_log_path);
        if (!check_log_file()) return;

        String text = getFormatLog(tag, msg);
        FileOutputStream fos = null;
        try {
            boolean append = log_file.length() <= 1024 * s_log_size;
            fos = new FileOutputStream(log_file, append);
            fos.write(text.getBytes());
            fos.write("\n".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFormatLog(String tag, String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateStr = sdf.format(new Date());
        return dateStr +
                " " +
                String.format("%s ", convertThreadId((int) Thread.currentThread().getId())) +
                String.format("%s: ", tag) +
                msg;
    }

    private static String convertThreadId(int value) {
        int limit = 5;
        String src = String.valueOf(value);
        int i = limit - src.length();
        if (i < 0) {
            src = src.substring(-i, src.length());
        }
        for (; i > 0; i--) {
            src = "0" + src;
        }
        return src;
    }
}
