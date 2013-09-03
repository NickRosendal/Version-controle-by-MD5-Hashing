package versionsStyring;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import android.util.Log;

import designPatterns.ObserverPattern_Observer;

public enum checksum implements designPatterns.ObserverPattern_Subject
{
	INSTANCE;
	public static final String LOCAL_FILE_PATHS_TXT = "TextFiles\\Local_File_Paths.txt";

	//private File localFile = new File(LOCAL_FILE_PATHS_TXT);
	private File onlineFile;

	private static checksum instance = null;
	private ObserverPattern_Observer observer;
	private ArrayList<String> delete = new ArrayList<String>();
	private HashMap<String, String> download = new HashMap<String, String>();

	

	public void compareDate(File onlineDocument, Map<String, String> localMap,designPatterns.ObserverPattern_Observer o){
		registerObserver(o);
		Map<String, String> onlineMap = traverseTextFile(onlineDocument);
		compareMD5(onlineMap, localMap);
	}



	private Map<String, String> traverseTextFile(File subject)
	{
		
		Map<String, String> traversed = new HashMap<String, String>();

		try
		{
			Scanner scanner = new Scanner(subject);
			while (scanner.hasNextLine())
			{
				String temp = scanner.nextLine();
				String md5 = temp.split("@")[1];
				String path = temp.split("@")[0];
				traversed.put(path, md5);
			}
			scanner.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.out.println(e);
		}

		return traversed;
	}



	private void compareMD5(Map<String, String> online, Map<String, String> local)
	{
		int localSize = local.size();
		int onlineSize = online.size();
		delete.clear();
		download.clear();

		for (Map.Entry<String, String> onlineEntry : online.entrySet())
		{
			if (!local.containsKey(onlineEntry.getKey()))
			{
				// adding the file to be downloaded to the list
				
//				String[] temp={onlineEntry.getKey(), onlineEntry.getValue()};
				
				
				download.put(onlineEntry.getKey(), onlineEntry.getValue());
				

			} else if (!onlineEntry.getValue().equals(local.get(onlineEntry.getKey())))
				{
					// as we uses insert ignore we dont need to remove it.
					delete.add(onlineEntry.getValue());
//					Log.i("versions", "file added to remove list");

					
					download.put(onlineEntry.getKey(), onlineEntry.getValue());
//					Log.i("versions", "file added to download");

				}

			
		}
//		Log.i("versions", "All files added to download 	que");
		for (Map.Entry<String, String> localEntry : local.entrySet())
		{
			if (!online.containsKey(localEntry.getKey()))
			{
				delete.add(localEntry.getKey());
			}
		}
//		Log.i("versions", "notifying!");

		notifyObservers(1, null);

	}

	@Override
	public void registerObserver(ObserverPattern_Observer o)
	{
		observer = o;

	}

	@Override
	public void removeObserver(ObserverPattern_Observer o)
	{
		// TODO Auto-generated method stub
		observer = null;

	}

	@Override
	public void notifyObservers(int event, String info)
	{
		// TODO Auto-generated method stub
		observer.update(event, info);

	}

	public ArrayList<String> getFilesToDelete()
	{
		return delete;

	}

	public HashMap<String, String> getFilesToDownload()
	{
		return download;

	}

}