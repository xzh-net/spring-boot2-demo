package net.xzh.generator.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import net.xzh.generator.framework.repository.SuperMapper;

/**
 * @Author zlt
 */
@Component
@Mapper
public interface GeneratorMapper extends SuperMapper<Object> {

	Map<String, String> queryTable(String tableName);

	List<Map<String, String>> queryColumns(String tableName);
}
