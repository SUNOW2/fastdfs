package com.software.yanchang.utils;

import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * FastDFS连接池
 */
public class FastdfsPool {
    //    最大可连接数
    private int size = 5;
    //    被使用的连接
    private ConcurrentHashMap<StorageClient1, Object> busyConnectionPool = null;
    //    空闲的连接
    private ArrayBlockingQueue<StorageClient1> idleConnectionPool = null;

    private Object obj = new Object();

    private static FastdfsPool instance;

    static {
        try {
            instance = new FastdfsPool();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    public static FastdfsPool getFastdfs() {
        return instance;
    }

    //    取出连接
    public StorageClient1 checkout(int waitTime) {
        StorageClient1 storageClient1 = null;
        try {
            storageClient1 = idleConnectionPool.poll(waitTime, TimeUnit.SECONDS);
            if (storageClient1 != null) {
                busyConnectionPool.put(storageClient1, obj);
            }
        } catch (InterruptedException e) {
            storageClient1 = null;
            e.printStackTrace();
        }
        return storageClient1;
    }

    //    回收连接
    public void checkin(StorageClient1 storageClient1) {
        if (busyConnectionPool.remove(storageClient1) != null) {
            idleConnectionPool.add(storageClient1);
        }
    }

    //    如果连接无效则抛弃，新建连接补充到池中
    public void drop(StorageClient1 storageClient1) throws IOException {
        if (busyConnectionPool.remove(storageClient1) != null) {
            TrackerServer trackerServer = null;
            TrackerClient trackerClient = new TrackerClient();
            try {
                trackerServer = trackerClient.getConnection();
                StorageClient1 newStorageClient1 = new StorageClient1(trackerServer, null);
                idleConnectionPool.add(newStorageClient1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (trackerServer != null) {
                    trackerServer.close();
                }
            }
        }
    }

    //    单例
    private FastdfsPool() throws IOException, MyException {
        busyConnectionPool = new ConcurrentHashMap<StorageClient1, Object>();
        idleConnectionPool = new ArrayBlockingQueue<StorageClient1>(size);
        init(size);
    }

    //    初始化连接池
    private void init(int size) throws IOException {
        TrackerServer trackerServer = null;
//        加载配置文件信息
        try {
            ClientGlobal.initByProperties("application.properties");
//              获取连接
            TrackerClient trackerClient = new TrackerClient();
//              只需要一个tracker server连接
            trackerServer = trackerClient.getConnection();
            StorageClient1 storageClient1 = null;
            StorageServer storageServer = null;

            for (int i = 0; i < size; i++) {
                storageClient1 = new StorageClient1(trackerServer, storageServer);
                idleConnectionPool.add(storageClient1);
            }
        } catch (MyException e) {
            e.printStackTrace();
        } finally {
            if (trackerServer != null) {
                trackerServer.close();
            }
        }

    }

}
