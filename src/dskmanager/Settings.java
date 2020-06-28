package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {
  private static File file = new File(System.getProperty("user.home"), "/JavaCPC/fsettings.dat");
  
  private static final Settings instance = new Settings();
  
  private final Properties props = new Properties();
  
  public static final String lastpath = "last_path";
  public static final String lastopened = "last_opened";
  
  
  private Settings() {
    try {
      if (System.getSecurityManager() != null)
        System.getSecurityManager().checkRead(file.getAbsolutePath()); 
      this.props.load(new FileInputStream(file));
      System.out.println("loaded " + this.props.size() + " favourites");
    } catch (Throwable t) {
      System.out.println("can't load user settings (" + t.getMessage() + ")");
    } 
  }
  
  public static boolean getBoolean(String key, boolean defaultValue) {
    String value = instance.props.getProperty(key);
    if (value == null)
      return defaultValue; 
    return value.equals("true");
  }
  
  public static int getInt(String key, int defaultValue) {
    String value = instance.props.getProperty(key);
    if (value == null)
      return defaultValue; 
    return Integer.parseInt(value);
  }
  
  public static void setBoolean(String key, boolean value) {
    instance.props.setProperty(key, value ? "true" : "false");
    save();
  }
  
  public static String get(String key, String defaultValue) {
    String value = instance.props.getProperty(key);
    if (value == null)
      return defaultValue; 
    return value;
  }
  
  public static void set(String key, String value) {
    if (value.equals(get(key, null)))
      return; 
    instance.props.setProperty(key, value);
    save();
  }
  
  public static void delete() {
    try {
      instance.props.clear();
      save();
      System.out.println("Fsettings deleted...");
    } catch (Throwable throwable) {}
  }
  
  private static void save() {
    try {
      File f = new File(System.getProperty("user.home"), "JavaCPC");
      f.mkdir();
      if (System.getSecurityManager() != null)
        System.getSecurityManager().checkWrite(file.getAbsolutePath()); 
      FileOutputStream fos = new FileOutputStream(file);
      instance.props.store(fos, "[Settings]");
      fos.close();
    } catch (Throwable t) {
      System.out.println("can't save user settings (" + t.getMessage() + ")");
    } 
  }
}
