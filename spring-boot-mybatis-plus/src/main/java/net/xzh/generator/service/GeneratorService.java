package net.xzh.generator.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import net.xzh.generator.framework.service.SuperService;

/**
 * @Author zlt
 */
@Service
public interface GeneratorService extends SuperService<Object> {

     Map<String, String> queryTable(String tableName);

     List<Map<String, String>> queryColumns(String tableName);

     byte[] generatorCode(String[] tableNames);
}
