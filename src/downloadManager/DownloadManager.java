package downloadManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import designPatterns.ObserverPattern_Observer;

public enum DownloadManager implements designPatterns.ObserverPattern_Subject
{
	INSTANCE;
	private LinkedList<DownloadItem> completedDownloads = new LinkedList<DownloadItem>();
	private int threadCount;
	private ExecutorService threadPool = null;
	private int allowedAtteptsToDownload = 3;
	private ObserverPattern_Observer observer;
//	private static DownloadManager instance = null;; // donno what it is 
	int jobsInCue = 0;

	public void setParameters(int threadCount, ObserverPattern_Observer o)
	{
		this.threadCount = threadCount;
		registerObserver(o);
	}

	public void addDownloadItemToQue(DownloadItem downloadItem)
	{

		if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated())
		{
			threadPool = Executors.newFixedThreadPool(threadCount);
		}

		try
		{
			threadPool.execute(new DownloadTask(downloadItem));
			jobsInCue++;

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void addFileToQue(String url, String destination)
	{
		addDownloadItemToQue(new DownloadItem(url, destination));

	}

	public void notifiedThatDownloadHasFinished(DownloadItem downloadItem)
	{
		jobsInCue--;
		if (!downloadItem.getDownloadStatus().equals("completed") && downloadItem.getAttemptsToDownload() < allowedAtteptsToDownload)
		{
			downloadItem.addAttemptToDownLoad();
			addDownloadItemToQue(downloadItem);

		} else
		{
			completedDownloads.add(downloadItem);

			if (downloadItem.getDownloadStatus().equals("completed"))
			{

				notifyObservers(4, downloadItem.getDestiantion());
			} else
			{
				notifyObservers(5, downloadItem.getUrl());
			}
		}

	}

	private void downloadAFile(String urlString, String destination) throws IOException
	{
		File directory = new File(destination.substring(0, destination.lastIndexOf("/")));
		directory.mkdirs();
		
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		connection.connect();
		InputStream input = new BufferedInputStream(url.openStream());
		OutputStream output = new FileOutputStream(destination);

		byte data[] = new byte[1024];
		int count;

		while ((count = input.read(data)) != -1)
		{
			output.write(data, 0, count);
			// Log.i("versions", "downloading data");
		}

		output.flush();
		output.close();
		input.close();
	}

	public void downloadSingleFileAndCallBack(final String urlString, final String destination)
	{

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				int returnCode = 6;
				String returnInfo = destination;
				try
				{
					downloadAFile(urlString, destination);
				} catch (Exception E)
				{
					// ooh i �hh'
					Log.i("versions", "single file erronr " + E);
					returnCode = 5;
					returnInfo = "" + E;

				}
				// Log.i("versions", "done notifying Observers");
				notifyObservers(returnCode, returnInfo);

			}
		}).start();

	}

	class DownloadTask implements Runnable
	{
		DownloadItem itemToDownload;

		public DownloadTask(DownloadItem itemToDownload)
		{
			this.itemToDownload = itemToDownload;
		}

		@Override
		public void run()
		{
			try
			{
				downloadAFile(itemToDownload.getUrl(), itemToDownload.getDestiantion());
				itemToDownload.setDownloadStatus("completed");
			} catch (Exception E)
			{
			//	E.printStackTrace();
				itemToDownload.setDownloadStatus(E + "");

			}
			notifiedThatDownloadHasFinished(itemToDownload);
			// Log.i("versions", "A file have been downloaded " + jobsInCue +
			// " jobsInCue to go");
			if (jobsInCue == 0)
			{
				notifyObservers(2, null);
				threadPool.shutdown();
			}
		}
	}

	public LinkedList<DownloadItem> getCompletedDownloads()
	{
		return completedDownloads;
	}

	public LinkedList getIncompleteDownloads()
	{
		LinkedList<DownloadItem> incompletede = new LinkedList<DownloadItem>();
		for (DownloadItem curr : completedDownloads)
		{
			if (!curr.getDownloadStatus().equals("completed"))
			{
				incompletede.add(curr);
			}
		}

		return incompletede;

	}

	@Override
	public void registerObserver(ObserverPattern_Observer o)
	{
		observer = o;
	}

	@Override
	public void removeObserver(ObserverPattern_Observer o)
	{
		observer = null;
	}

	@Override
	public void notifyObservers(int event, String info)
	{
		observer.update(event, info);
	}

}
