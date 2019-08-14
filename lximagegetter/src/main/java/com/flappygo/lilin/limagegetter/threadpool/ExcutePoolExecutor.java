package com.flappygo.lilin.limagegetter.threadpool;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.flappygo.lilin.limagegetter.tools.LogTool;

/*********************************
 * Created by yang on 2015/12/9. 执行线程的线程池
 */
public class ExcutePoolExecutor extends ScheduledThreadPoolExecutor {

	private ConcurrentHashMap<String, Thread> Lthreads;

	public ExcutePoolExecutor(int corePoolSize) {
		super(corePoolSize);
		init();
	}

	public ExcutePoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
		init();
	}

	public ExcutePoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
		init();
	}

	public ExcutePoolExecutor(int corePoolSize, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
		init();
	}

	/*************
	 * 初始化线程map
	 */
	private void init() {
		Lthreads = new ConcurrentHashMap<String, Thread>();
	}

	/****************
	 * 执行某个线程
	 * 
	 * @param thread
	 *            线程
	 */
	public void execute(Thread thread) {

		if (thread instanceof Thread) {
			// 添加到保存的hashMap中
			addThreadToHashMap(thread);
			// 动态代理，当线程执行完成后进行移除操作
			ThreadInterceptor transactionInterceptor = new ThreadInterceptor();
			transactionInterceptor.setTarget(thread);
			transactionInterceptor
					.setThreadListener(new ThreadInterceptor.ThreadListener() {

						@Override
						public void death(Thread thread) {
							removeThreadFromHashMap(thread);
						}

						@Override
						public void began(Thread thread) {

						}
					});
			Class<?> classType = transactionInterceptor.getClass();
			Object userServiceProxy = Proxy.newProxyInstance(
					classType.getClassLoader(), Thread.class.getInterfaces(),
					transactionInterceptor);
			super.execute((Runnable) userServiceProxy);
		} else {
			super.execute(thread);
		}
	}

	/**************
	 * 添加线程到HashMap中
	 * 
	 * @param thread
	 *            线程
	 */
	private void addThreadToHashMap(Thread thread) {
		// 加入到当前正在进行的线程中
		synchronized (Lthreads) {
			Lthreads.put(Long.toString(thread.getId()), thread);
			LogTool.d("ExcutePoolExecutor", "thread added" + thread.getId());
		}
	}

	/**************
	 * 从hashmap中移除线程
	 * 
	 * @param thread
	 *            线程
	 */
	private void removeThreadFromHashMap(Thread thread) {
		Thread t = (Thread) thread;
		String key = Long.toString(t.getId());
		// 保证原子操作
		synchronized (Lthreads) {
			if (Lthreads.containsKey(key)) {
				this.Lthreads.remove(key);
				LogTool.d("ExcutePoolExecutor", "thread removed" + t.getId());
			}
		}
	}

	@Override
	public boolean remove(Runnable task) {
		if (task instanceof Thread) {
			// 从HashMap中移除某个线程
			removeThreadFromHashMap((Thread) task);
		}
		return super.remove(task);
	}

	/***********
	 * 获取当前所有的线程
	 * @return
	 */
	public List<Thread> getAllThread() {
		List<Thread> ret = new ArrayList<Thread>();
		// 加锁进行原子操作
		synchronized (Lthreads) {
			Iterator<Entry<String, Thread>> iter = Lthreads.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<String, Thread> entry = (Entry<String, Thread>) iter
						.next();
				Thread thread = entry.getValue();
				ret.add(thread);
			}
		}
		return ret;
	}
}
