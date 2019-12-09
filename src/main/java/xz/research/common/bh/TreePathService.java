package xz.research.common.bh;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TreePathService {

	@Autowired
	public JdbcTemplate jdbcTemplate;

	public void buildPath(String parentId, NamingConfig config) {
		TreePathGenerator.buildPath(jdbcTemplate, StringUtils.isBlank(parentId) ? null : parentId, config);
	}

	public String getPath(String id, NamingConfig config) {
		return TreePathGenerator.getPath(jdbcTemplate, id, config);
	}

	public List<String> getPathValue(String id, NamingConfig config, String field) {
		return TreePathGenerator.getPathValue(jdbcTemplate, id, config, field);
	}

	public List<String> getPathValueByBH(String bh, NamingConfig config, String field) {
		return TreePathGenerator.getPathValueByBH(jdbcTemplate, bh, config, field);
	}

	public List<Map<String, Object>> getPathValue(String id, NamingConfig config, String[] fields) {
		return TreePathGenerator.getPathValue(jdbcTemplate, id, config, fields);
	}

	public List<Map<String, Object>> getPathValueByBH(String bh, NamingConfig config, String[] fields) {
		return TreePathGenerator.getPathValueByBH(jdbcTemplate, bh, config, fields);
	}

}
