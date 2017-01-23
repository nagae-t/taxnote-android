package com.example.taxnoteandroid.Library;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Eiichi on 2017/01/18.
 */

public class FileUtil
{
  public static void saveCSV(String path, String name)
  {
    PrintWriter writer = null;

    try
    {
      File folder = new File(Environment.getExternalStorageDirectory(), path);
      File file = new File(folder, name);
      writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if(writer!=null) writer.close();
    }
  }
}
