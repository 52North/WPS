package org.n52.wps.server.oberserpattern;
public interface ISubject {
	 public void addObserver(IObserver o);
	 public void removeObserver(IObserver o);
	 public Object getState();
	 public void update(Object state);
}