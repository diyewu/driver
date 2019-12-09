package xz.research.common.bh;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class TreePathGenerator {

	public static String getNodePath(JdbcTemplate jdbcTemplate, String bh, String outputField,
                                     String delimiter, NamingConfig namingConfig) {
		if (delimiter == null) {
			delimiter = "/";
		}
		String[] allParentPaths = getAllParentPaths(bh);
		StringBuilder builder = new StringBuilder();
		for (String s : allParentPaths) {
			builder.append("?,");
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}
		List outputFields = jdbcTemplate
				.queryForList(
						getSql("SELECT " + outputField + " FROM $tableName WHERE $bh in ("
								+ builder.toString() + ")", namingConfig), allParentPaths, String.class);
		builder.setLength(0);
		for (Object f : outputFields) {
			builder.append(delimiter + f.toString());
		}
		return builder.toString();
	}

	public static String[] getAllParentPaths(String path) {
		if (path == null) {
			return null;
		}
		String[] strings = path.split("\\.");
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) {
				strings[i] = strings[i - 1] + "." + strings[i];
			}
		}
		return strings;
	}

	public static String getPath(JdbcTemplate jdbcTemplate, String id, NamingConfig config) {
		List<String> l = jdbcTemplate.queryForList(getSql("SELECT $bh FROM $tableName WHERE $id =?", config),
				new Object[] { id }, String.class);
		return l.isEmpty() ? null : l.get(0);
	}

	public static List<String> getPathValue(JdbcTemplate jdbcTemplate, String id, NamingConfig config,
                                            String field) {
		String path = getPath(jdbcTemplate, id, config);
		return getPathValueByBH(jdbcTemplate, path, config, field);
	}

	public static List<String> getPathValueByBH(JdbcTemplate jdbcTemplate, String bh, NamingConfig config,
                                                String field) {
		String path = bh;
		String[] allParentPaths = getAllParentPaths(path);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < allParentPaths.length; i++)
			builder.append("?,");
		builder.setLength(builder.length() - 1);

		List<String> l = jdbcTemplate.queryForList(
				getSql("SELECT " + field + " FROM $tableName WHERE $bh in (" + builder.toString()
						+ ") order by $bh", config), allParentPaths, String.class);
		return l;
	}

	public static List<Map<String, Object>> getPathValue(JdbcTemplate jdbcTemplate, String id,
                                                         NamingConfig config, String[] fields) {
		String path = getPath(jdbcTemplate, id, config);
		return getPathValueByBH(jdbcTemplate, path, config, fields);
	}

	public static List<Map<String, Object>> getPathValueByBH(JdbcTemplate jdbcTemplate, String bh,
                                                             NamingConfig config, String[] fields) {
		String path = bh;
		String[] allParentPaths = getAllParentPaths(path);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < allParentPaths.length; i++)
			builder.append("?,");
		builder.setLength(builder.length() - 1);

		List<Map<String, Object>> l = jdbcTemplate.queryForList(
				getSql("SELECT " + StringUtils.join(fields, ",") + " FROM $tableName WHERE $bh in ("
						+ builder.toString() + ") order by $bh", config), allParentPaths);
		return l;
	}

	/**
	 * 重建某个节点下的孩子们的BH代码
	 */
	@SuppressWarnings("unchecked")
	public static void buildPath(JdbcTemplate jdbcTemplate, String parentId, NamingConfig config) {
		List<Map<String, Object>> list;

		final Map<String, Map<String, Object>> indextemp = new LinkedHashMap<String, Map<String, Object>>();
		final ArrayList<Map<String, Object>> roottemp = new ArrayList<Map<String, Object>>();
		String bh = null;
		if (parentId != null) {
			list = new ArrayList<Map<String, Object>>();
			loadTypes(jdbcTemplate, parentId, list, config);

			bh = (String) jdbcTemplate.queryForMap(
					getSql("select $bh as BH from $tableName where $id =?", config),
					new Object[] { parentId }).get("BH");
			if (bh != null && bh.endsWith("L")) {
				bh = bh.substring(0, bh.length() - 1);
				jdbcTemplate.update(getSql("update $tableName set $bh=? where $id=?", config), new Object[] {
						bh, parentId });
			}
			for (Map<String, Object> map : list) {
				indextemp.put((String) map.get("ID"), map);
				// map.put("child", new HashSet<Map<String, Object>>());
				if (map.get("PARENTID").equals(parentId)) {
					roottemp.add(map);
				}
			}

		} else {
			list = jdbcTemplate.queryForList(getSql(
					"select $id as ID,$parentId as PARENTID,$sortField as N from $tableName order by $id",
					config));

			for (Map<String, Object> map : list) {
				indextemp.put((String) map.get("ID"), map);
				// map.put("child", new HashSet<Map<String, Object>>());
				if (map.get("PARENTID") == null) {
					roottemp.add(map);
				}
			}
		}

		/**
		 * 为数组中的类型建立父子关系
		 */
		Map<String, Object> parent;
		Collection<Map<String, Object>> children;
		for (Map<String, Object> record : list) {
			String _parentId = (String) record.get("PARENTID");
			if (_parentId != null) {
				parent = indextemp.get(_parentId);
				if (parent != null) {
					children = (List<Map<String, Object>>) parent.get("child");
					if (children == null) {
						children = new ArrayList<Map<String, Object>>();
						parent.put("child", children);
					}
					children.add(record);
				}
			}
		}

		list = null;

		final List<IDandBH> sqllist = new ArrayList<IDandBH>();

		/**
		 * 为每一个节点进行编号,并按顺序放入sqllist里面
		 */
		doCoder(roottemp, sqllist, bh);

		/**
		 * 批量执行所有的bh到数据库中去
		 */
		List<IDandBH> batchList = new ArrayList<IDandBH>(50);
		int c = 0;
		for (IDandBH type : sqllist) {
			if (c > 50) {
				executeBatch(jdbcTemplate, batchList, config);
				batchList.clear();
				c = 1;
			}

			batchList.add(type);
			c++;
		}

		if (batchList.size() > 0) {// 处理尾巴
			executeBatch(jdbcTemplate, batchList, config);
		}

	}

	/**
	 * 加载某个指定类型下面的所有类型（迭代所有子节点）
	 * 
	 * @param nodeId
	 * @param tableName
	 * @param list
	 */
	private static void loadTypes(JdbcTemplate jdbcTemplate, String nodeId, List<Map<String, Object>> list,
                                  NamingConfig config) {
		List<Map<String, Object>> temp = jdbcTemplate
				.queryForList(
						getSql("select $id as ID,$parentId as PARENTID from $tableName where $parentId=? order by $id ",
								config), new Object[] { nodeId });

		if (temp.size() > 0) {
			list.addAll(temp);
			for (Map<String, Object> map : temp) {
				loadTypes(jdbcTemplate, (String) map.get("ID"), list, config);
			}
		}
	}

	private static String getSql(String raw, NamingConfig config) {
		return NamingConfig.getNamingString(raw, config);
	}

	private static void executeBatch(JdbcTemplate jdbcTemplate, final List<IDandBH> batchList,
                                     NamingConfig config) {
		jdbcTemplate.batchUpdate(getSql("update $tableName set $bh=? where $id=?", config),
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement statement, int i) throws SQLException {

						IDandBH dandBH = batchList.get(i);
						statement.setString(1, dandBH.bh);
						statement.setString(2, dandBH.id);
					}

					public int getBatchSize() {
						return batchList.size();
					}
				});
	}

	private static class IDandBH {

		public IDandBH(String id, String bh) {
			super();
			this.id = id;
			this.bh = bh;
		}

		String id;

		String bh;

		@Override
		public String toString() {
			return "IDandBH [bh=" + bh + ", id=" + id + "]";
		}
	}

	/**
	 * 为每一个节点进行编号
	 * 
	 * @param nodes
	 * @param sqllist
	 * @param parentcode
	 */
	@SuppressWarnings("unchecked")
	private static void doCoder(List<Map<String, Object>> nodes, List<IDandBH> sqllist, String parentcode) {
		int index = 1000;

		Collections.sort(nodes, xcomparable);

		for (Map<String, Object> node : nodes) {
			List<Map<String, Object>> cld = (List<Map<String, Object>>) node.get("child");

			String bh = (parentcode == null ? "" : parentcode + ".") + ((index++) + (cld == null ? "L" : ""));
			sqllist.add(new IDandBH((String) node.get("ID"), bh));
			if (cld != null)
				doCoder(cld, sqllist, bh);
		}
	}

	private static XComparable xcomparable = new XComparable();

	private static class XComparable implements Comparator<Map<String, Object>> {

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			Comparable n1 = (Comparable) o1.get("N");
			Comparable n2 = (Comparable) o2.get("N");
			if (n1 == null && n2 == null)
				return 0;
			else if (n1 == null && n2 != null)
				return -1;
			else if (n1 != null && n2 == null)
				return 1;
			return n1.compareTo(n2);
		}

	}

}
