package com.cloudminds.meta.util;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消息处理缓存型线程工厂 
 */
public class MessageHandleThreadFactory {

	private static MessageHandleThreadFactory cachedThreadFactory;
	private ExecutorService executorService;
	private static final Object o = new Object();

	private ArrayList<Runnable> runnableList = new ArrayList<Runnable>();

	private MessageHandleThreadFactory() {
		executorService = Executors.newCachedThreadPool();
	}

	public static MessageHandleThreadFactory getInstance() {
		if (cachedThreadFactory == null) {
			synchronized (o) {
				if (cachedThreadFactory == null) {
					cachedThreadFactory = new MessageHandleThreadFactory();
				}
			}
		}
		return cachedThreadFactory;
	}

	/**
	 * 添加任务
	 */
	public void addTask(Runnable thread) {
		runnableList.add(thread);
		executorService.execute(thread);
	}

	/**
	 * 清除所有任务
	 */
	public void clearAllTask() {
		runnableList.clear();
	}
}
