package com.sosop.zkJedis.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.LockSupport;

import org.apache.log4j.Logger;

/**
 * @author xiaolong.hou
 * @version 0.0.1
 * @date 2014.8.29
 * @describe 文件操作, 主要针对日志和统计信息
 */

public class FileUtil {

	private final static Logger LOG = Logger.getLogger(FileUtil.class);

	private final static String DEFAUL_PATH = "/tmp/redis-hit.log";

	/**
	 * 指定路径和内容写入文件
	 * 
	 * @param path
	 * @param value
	 */
	public static void writeToFile(String path, String value) {
		if (StringUtil.isNull(path)) {
			path = DEFAUL_PATH;
		}
		FileLock lock = null;
		try (RandomAccessFile rf = new RandomAccessFile(path, "rw");
				FileChannel channel = rf.getChannel();) {
			ByteBuffer buf = MappedByteBuffer.wrap(StringUtil.append(value, "\n").getBytes());
			while (true) {
				try {
					lock = channel.tryLock(channel.size() + 1, buf.limit(), false);
					break;
				} catch (OverlappingFileLockException e) {
					LockSupport.parkNanos(100);
				}
			}
			if (null != lock && lock.isValid()) {
				channel.position(channel.size());
				channel.write(buf);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (null != lock && lock.isValid()) {
					lock.release();
					lock = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 向文件中增量写文件
	 * 
	 * @param path
	 * @param value
	 */
	public static void writeToFileByLock(String path, String value) {
		RandomAccessFile rf = null;
		FileChannel fc = null;
		FileLock lock = null;
		ByteBuffer byteBuffer = null;
		try {
			rf = new RandomAccessFile(path, "rw");
			fc = rf.getChannel(); // 获得文件通道
			byteBuffer = ByteBuffer.wrap(StringUtil.append(value, "\n").getBytes());
			while (true) {
				try {
					lock = fc.lock(fc.size() + 1, byteBuffer.limit(), false);// 获取阻塞锁
					break;
				} catch (OverlappingFileLockException e) {
					Thread.sleep(1 * 1000);
					LOG.info("file locked");
				}
			}
			if (null != lock) {
				fc.position(fc.size());
				fc.write(byteBuffer);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (lock != null && lock.isValid()) {
					lock.release();
				}
				if (fc != null && fc.isOpen()) {
					fc.close();
					fc = null;
				}
				if (rf != null) {
					rf.close();
					rf = null;
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	// 读取文件内容
	public static String read(String filename) {
		StringBuffer sb = new StringBuffer();

		try (RandomAccessFile file = new RandomAccessFile(getConfigFile(filename), "r");
				FileChannel channel = file.getChannel();) {
			ByteBuffer buf = MappedByteBuffer.allocateDirect(512);
			while (channel.read(buf) > 0) {
				buf.flip();
				while (buf.hasRemaining()) {
					sb.append((char) buf.get());
				}
				buf.clear();
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		return sb.toString();
	}

	public static String getPath(String filename) {
		String baseDir = FileUtil.class.getResource("/").getPath();
		String fullPath = StringUtil.append(baseDir, filename);
		fullPath = System.getProperty("os.name").contains("indow") ? fullPath.substring(1)
				: fullPath;
		return fullPath;
	}

	public static File getConfigFile(String filename) {

		Path path = Paths.get(getPath(filename));

		if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			path = path.getParent().resolveSibling(StringUtil.append("classes/", filename));
		}
		return path.toFile();
	}
}