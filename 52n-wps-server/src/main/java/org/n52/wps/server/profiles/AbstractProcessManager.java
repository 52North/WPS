package org.n52.wps.server.profiles;

import net.opengis.wps.x100.ExecuteResponseDocument;

import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;

public abstract class AbstractProcessManager implements IProcessManager {
	protected ITransactionalAlgorithmRepository parentRepository;
	protected ExecuteResponseDocument executeResponse;
	public AbstractProcessManager(
			ITransactionalAlgorithmRepository parentRepository) {
		this.parentRepository = parentRepository;
	}

	/**
	 * Wait the asynchronousCallback
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitCallback() throws InterruptedException {
		try {
			wait();
		} catch (InterruptedException e) {
			throw e;
		}

		catch (Exception e) {
			System.out.println(e);
		}
		return;
	}

	public synchronized void notifyRequestManager() {
		notify();
	}
	@Override
	public void callback(ExecuteResponseDocument execRespDom) {
		this.executeResponse = execRespDom;
		this.notifyRequestManager();
		return;
	}

}
