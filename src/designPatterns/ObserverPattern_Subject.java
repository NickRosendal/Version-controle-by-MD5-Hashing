package designPatterns;

public interface ObserverPattern_Subject 
	{
		public void registerObserver(ObserverPattern_Observer o);
		public void removeObserver(ObserverPattern_Observer o);
		public void notifyObservers(int event, String info);
}
