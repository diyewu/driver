package xz.research.common.bh;

import java.io.Serializable;

public class NamingConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	String table;
	String id = "id";
	String name = "name";
	String path = "bh";
	String sortField = "name";
	String parentId = "parentId";

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public static String getNamingString(String raw, NamingConfig config) {
		return raw.replace("$bh", config.getPath()).replace("$tableName", config.getTable())
				.replace("$id", config.getId()).replace("$sortField", config.getSortField())
				.replace("$parentId", config.getParentId()).replace("$name", config.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
