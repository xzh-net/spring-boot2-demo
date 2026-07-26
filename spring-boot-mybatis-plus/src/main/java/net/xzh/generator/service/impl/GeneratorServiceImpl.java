package net.xzh.generator.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.xzh.generator.common.utils.GenUtils;
import net.xzh.generator.framework.service.impl.SuperServiceImpl;
import net.xzh.generator.mapper.GeneratorMapper;
import net.xzh.generator.service.GeneratorService;

/**
 * @Author zlt
 */
@Slf4j
@Service
public class GeneratorServiceImpl extends SuperServiceImpl<GeneratorMapper, Object> implements  GeneratorService {
	@Autowired
	private GeneratorMapper generatorMapper;

	@Override
	public Map<String, String> queryTable(String tableName) {
		return generatorMapper.queryTable(tableName);
	}

	@Override
	public List<Map<String, String>> queryColumns(String tableName) {
		return generatorMapper.queryColumns(tableName);
	}

	@Override
	public byte[] generatorCode(String[] tableNames) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
			for (String tableName : tableNames) {
				// 查询表信息
				Map<String, String> table = queryTable(tableName);
				// 查询列信息
				List<Map<String, String>> columns = queryColumns(tableName);
				// 生成代码
				GenUtils.generatorCode(table, columns, zip);
			}
		} catch (IOException e) {
			log.error("generatorCode-error: ", e);
		}
		return outputStream.toByteArray();
	}
}
