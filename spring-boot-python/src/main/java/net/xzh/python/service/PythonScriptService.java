package net.xzh.python.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * 通过 ProcessBuilder 调用本机 Python3 解释器执行脚本
 */
@Service
public class PythonScriptService {

	private static final Logger log = LoggerFactory.getLogger(PythonScriptService.class);

	@Value("${python.command:python}")
	private String pythonCommand;

	@Value("${python.script-location:/py}")
	private String scriptLocation;

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
		return run(command);
	}

	/**
	 * 执行 classpath 下的 Python 脚本，并传入参数
	 *
	 * @param scriptName 脚本文件名，如 add.py
	 * @param args       传递给脚本的命令行参数
	 */
	public String execScript(String scriptName, String... args) throws IOException, InterruptedException {
		Path scriptPath = copyScriptToTemp(scriptName);
		try {
			List<String> command = new ArrayList<String>();
			command.add(pythonCommand);
			command.add(scriptPath.toAbsolutePath().toString());
			for (String arg : args) {
				command.add(arg);
			}
			return run(command);
		} finally {
			scriptPath.toFile().deleteOnExit();
		}
	}

	private String run(List<String> command) throws IOException, InterruptedException {
		log.info("执行 Python 命令: {}", command);
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(false);
		Process process = builder.start();

		String stdout;
		String stderr;
		try (InputStream out = process.getInputStream(); InputStream err = process.getErrorStream()) {
			stdout = StreamUtils.copyToString(out, StandardCharsets.UTF_8);
			stderr = StreamUtils.copyToString(err, StandardCharsets.UTF_8);
			if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				throw new IOException("Python 执行超时（" + timeoutSeconds + " 秒）");
			}
		}
		int exitCode = process.exitValue();
		if (exitCode != 0) {
			throw new IOException("Python 执行失败，exit code=" + exitCode + "，错误信息：" + stderr.trim());
		}
		return stdout.trim();
	}

	/**
	 * 将 classpath 下的脚本复制到临时目录，供 Python 子进程执行。
	 *
	 * <p>原因：脚本打包在 jar 内（classpath），而 Python 子进程只能读取文件系统上的真实文件，
	 * 无法直接执行 jar 内部路径，因此需要先解压复制出来。
	 *
	 * <p>本项目是 demo，重点演示"Java 如何调用 Python"，脚本随 jar 一起打包，
	 * 可保证代码与脚本版本一致，部署时只有一个 jar，不依赖外部目录。
	 *
	 * <p>实际生产中如果需要脚本修改后实时生效（如公式、模板由运营维护），
	 * 可改为配置外部文件路径直接执行，无需复制：
	 * 例如 python.command 直接指向 /opt/scripts/add.py，改完脚本立即生效。
	 */
	private Path copyScriptToTemp(String scriptName) throws IOException {
		ClassPathResource resource = new ClassPathResource(scriptLocation + "/" + scriptName);
		if (!resource.exists()) {
			throw new IOException("Python 脚本不存在: " + scriptLocation + "/" + scriptName);
		}
		Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "spring-boot-python");
		Files.createDirectories(tempDir);
		Path tempFile = tempDir.resolve(scriptName);
		try (InputStream in = resource.getInputStream()) {
			Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}
		return tempFile;
	}
}
