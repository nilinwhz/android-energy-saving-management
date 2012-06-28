/**
 * 用于执行shell命令的类
 */
package com.cmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CMDExecute {

	public synchronized String run(String[] cmd, String workdirectory)
			throws IOException {
		String result = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			InputStream in = null;
			if (workdirectory != null) {
				builder.directory(new File(workdirectory));
				builder.redirectErrorStream(true);
				Process process = builder.start();
				in = process.getInputStream();
				byte[] buffer = new byte[1024];
				while (in.read(buffer) != -1)
					result += new String(buffer);
			}
			if (in != null) {
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
