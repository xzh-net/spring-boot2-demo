package net.xzh.python.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 通过 ProcessBuilder 调用本机 Python3 解释器执行脚本。
 * 脚本位于外部目录（python.script-dir），修改后实时生效，无需重新打包。
 */
@Service
public class PythonScriptService {

	private static final Logger log = LoggerFactory.getLogger(PythonScriptService.class);

	@Value("${python.command:python}")
	private String pythonCommand;

	/**
	 * 脚本目录。脚本从该目录直接加载执行，修改脚本后实时生效，无需重新打包发布。
	 * 支持绝对路径（如 /opt/scripts、D:/scripts）或相对于进程工作目录的路径。
	 */
	@Value("${python.script-dir:script}")
	private String scriptDir;

	@Value("${python.timeout-seconds:10}")
	private long timeoutSeconds;

	/**
	 * 执行一段 Python 代码（python -c）
	 */
	public String execCode(String code) throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add(pythonCommand);
		command.add("-c");
		command.add(code);
		return run(command, null);
	}

	/**
	 * 执行脚本目录下的 Python 脚本，并传入参数。
	 * 会将子进程工作目录设置为脚本目录，脚本可 import 同目录模块、读取同目录配置文件。
	 *
	 * @param scriptName 脚本文件名，如 add.py
	 * @param args       传递给脚本的命令行参数
	 */
	public String execScript(String scriptName, String... args) throws IOException, InterruptedException {
		File workDir = new File(scriptDir);
		File scriptFile = new File(workDir, scriptName);
		if (!workDir.isDirectory()) {
			throw new IOException("Python 脚本目录不存在: " + workDir.getAbsolutePath());
		}
		if (!scriptFile.isFile()) {
			throw new IOException("Python 脚本不存在: " + scriptFile.getAbsolutePath());
		}

		List<String> command = new ArrayList<String>();
		command.add(pythonCommand);
		// 相对文件名 + 工作目录，便于脚本内相对路径读取同目录资源
		command.add(scriptFile.getName());
		for (String arg : args) {
			command.add(arg);
		}
		return run(command, workDir);
	}

	private String run(List<String> command, File workDir) throws IOException, InterruptedException {
		log.info("执行 Python 命令: {}, 工作目录: {}", command, workDir);
		ProcessBuilder builder = new ProcessBuilder(command);
		// 合并 stderr 到 stdout，由同一个线程消费，避免双流分别读取时管道堵塞死锁
		builder.redirectErrorStream(true);
		if (workDir != null) {
			builder.directory(workDir);
		}
		// 避免子进程中文输出乱码
		builder.environment().put("PYTHONIOENCODING", "utf-8");
		builder.environment().put("PYTHONUTF8", "1");
		builder.environment().put("LANG", "C.UTF-8");

		Process process = builder.start();
		StringBuilder output = new StringBuilder();
		// 守护线程持续消费进程输出，防止输出量超过管道缓冲区导致子进程阻塞、waitFor 超时误判
		Thread readerThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append(System.lineSeparator());
					log.info("[py] {}", line);
				}
			} catch (IOException e) {
				log.warn("读取python输出异常: {}", e.getMessage());
			}
		}, "py-output-reader");
		readerThread.setDaemon(true);
		readerThread.start();

		boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			readerThread.join(2000);
			throw new IOException("Python 执行超时（" + timeoutSeconds + " 秒）");
		}
		readerThread.join(2000);

		int exitCode = process.exitValue();
		String result = output.toString().trim();
		if (exitCode != 0) {
			throw new IOException("Python 执行失败，exit code=" + exitCode + "，输出：" + tail(result));
		}
		return result;
	}

	/**
	 * 截取输出末尾，避免错误日志过长
	 */
	private String tail(String text) {
		int maxLen = 2000;
		if (text == null) {
			return "";
		}
		return text.length() <= maxLen ? text : text.substring(text.length() - maxLen);
	}
}
