package com.example.versionsstyringmeddownloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import downloadManager.DownloadManager;

import versionsStyring.DataBaseHandler;
import versionsStyring.checksum;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements designPatterns.ObserverPattern_Observer
{
	Button updateButton;
	Button kims_knap;
	TextView statusText;
	private String fileRoot;
	private final int DONE_UPDATING_MD5_FILE = 3, DONE_DOWNLOADING = 2, DONE_CHECKING_VERSIONING = 1, SINGLE_FILE_DOWNLOADED = 4, DOWNLOAD_FAILED = 5,
			MD5_FILE_DOWNLOADED = 6;

	private final int NUMBER_OF_THREADS = 8;

	private HashMap<String, String> filesToDownload;

	private final String BASEURL = "http://10.36.98.82/";

	versionsStyring.checksum versionsStyring;
	downloadManager.DownloadManager downloader;
	versionsStyring.DataBaseHandler dbHandel;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateButton = (Button) findViewById(R.id.update);
		statusText = (TextView) findViewById(R.id.textStatus);

		// sets the path to the local statement file!
		// editMD5.INSTANCE.setParameter(new File(getExternalFilesDir(null) +
		// "/localFileListStatement.txt.txt"));

		downloader = DownloadManager.INSTANCE;
		downloader.setParameters(NUMBER_OF_THREADS, this);

		// skal finde ud af at hente den online Document...
		versionsStyring = checksum.INSTANCE;
		dbHandel = new DataBaseHandler(MainActivity.this);
		fileRoot = getExternalFilesDir(null) + "/uncompressedArea/";
		kims_knap = (Button) findViewById(R.id.button_kim);
		kims_knap.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				versionsStyring.DataBaseHandler kimsHandler = new DataBaseHandler(MainActivity.this);
				kimsHandler.open();
				kimsHandler.clear();
				kimsHandler.close();
			}
		});

		updateButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// meh ska lige finde en løsning på at downloade textfilen...
				Log.i("versions", "clicked da button");

				downloader.downloadSingleFileAndCallBack(BASEURL + "onlineFileListStatement.txt", getExternalFilesDir(null) + "/onlineFileListStatement.txt");

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void checkDone()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Delete the files
				ArrayList<String> filesToDelete = versionsStyring.getFilesToDelete();
				// Download  missing files
				filesToDownload = versionsStyring.getFilesToDownload();
				
				dbHandel.open();
				for (String currentFileToDeleteString : filesToDelete)
				{
					File currentFileToDelete = new File(currentFileToDeleteString);
					currentFileToDelete.delete();
					if(!filesToDownload.containsKey(currentFileToDeleteString)){
						//delete from database...
						dbHandel.deleteEntry(currentFileToDeleteString);
						
					}
				}
				dbHandel.close();
				
				

				Iterator<Entry<String, String>> myIterator = filesToDownload.entrySet().iterator();

				while (myIterator.hasNext())
				{

					Entry<String, String> temp = myIterator.next();

					downloader.addFileToQue(BASEURL + temp.getKey(), fileRoot + temp.getKey());
					//Log.i("versions", "added to download queue" + BASEURL + temp.getKey());
				}
			}
		}).start();

	}

	private void doneDownloading()
	{
		// her er vi færdige med at downloade...
		// MEN ikke nødvendigt vis færdig med at skrive til databasen :(
		// Has to run in a runOnUiThread since it gets called from a other thread
		 runOnUiThread(new Runnable() {

		        @Override
		        public void run() {
		            Toast.makeText(MainActivity.this, "All downloads compleated", Toast.LENGTH_LONG).show();
		        }
		    });
	}

	private void singleDone(String info)
	{
		// Making the static path to the app location, relative.(thus equal the original key in the filesToDownload hashmap)
	//	Log.i("versions", "I FILE HAS DOWNLOADED");
		info = info.substring((fileRoot).length());
		dbHandel.open();
		dbHandel.addEntry(info, filesToDownload.get(info));
		dbHandel.close();
	}

	private void MD5fileDownloaded(String dest)
	{

		File onlineDocument = new File(dest);
		dbHandel.open();
		versionsStyring.compareDate(onlineDocument, dbHandel.getDBentriesAsMap(), this);
		dbHandel.close();

	}
	
	// Receives notifications form observed objects
	@Override
	public void update(int event, String info)
	{

		switch (event)
		{
		case DONE_CHECKING_VERSIONING: checkDone(); break;
		case DONE_DOWNLOADING: doneDownloading(); break;
		case DONE_UPDATING_MD5_FILE: statusText.setEnabled(true); break;
		case SINGLE_FILE_DOWNLOADED: singleDone(info); break;
		case DOWNLOAD_FAILED:
			/*
			 * maybe we should do somthing about it!
			 */
//			Log.i("versions", "failed to download " + info);
			break;
		case MD5_FILE_DOWNLOADED: MD5fileDownloaded(info); break;
		}

	}

}
