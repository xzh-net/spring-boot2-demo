package net.xzh.python.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.xzh.python.service.PythonScriptService;

/**
 * 演示 Spring Boot 通过 ProcessBuilder 调用本机 Python3
 */
@RestController
@RequestMapping("/api/python")
public class PythonController {

	private final PythonScriptService pythonScriptService;

	public PythonController(PythonScriptService pythonScriptService) {
		this.pythonScriptService = pythonScriptService;
	}

	/**
	 * 执行一段 Python 代码块（python -c），不返回计算结果，仅演示执行过程
	 */
	@PostMapping("/execute")
	public Map<String, Object> executeScript() throws Exception {
		String output = pythonScriptService.execCode("a = 'hello world'\nprint(a)");
		return success(output);
	}

	/**
	 * 统一的 Python 调用入口，支持两种用法：
	 * <p>1. 固定函数调用：不传 code，执行预置脚本 add.py 的 add(a,b)，适合算法固定、参数动态的场景
	 * <p>2. 公式计算/模板渲染：传入 code（函数或模板内容），在内存中直接执行。
	 *    函数无需落盘为 .py 文件，可由 Java 侧从数据库查出、或在内存中动态拼接后传入。
	 *    例如 code = "def add(a,b): return a+b"，接口会拼接参数执行并返回结果。
	 * <p>注意：允许执行任意代码，生产环境需做白名单或沙箱限制。
	 */
	@PostMapping("/compute")
	public Map<String, Object> compute(
			@RequestParam(required = false) String code,
			@RequestParam(defaultValue = "122") int a,
			@RequestParam(defaultValue = "23") int b) throws Exception {
		String output;
		if (code != null && !code.isEmpty()) {
			// 模板/公式场景：code 可来自数据库或内存，无需真实脚本文件
			String script = code + "\nprint(add(" + a + ", " + b + "))";
			output = pythonScriptService.execCode(script);
		} else {
			output = pythonScriptService.execScript("add.py", String.valueOf(a), String.valueOf(b));
		}
		return success(output);
	}

	private Map<String, Object> success(Object result) {
		Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("success", true);
		body.put("result", result);
		return body;
	}
}
