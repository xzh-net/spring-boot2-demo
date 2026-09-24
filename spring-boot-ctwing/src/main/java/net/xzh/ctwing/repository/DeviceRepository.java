package net.xzh.ctwing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.model.entity.Device;

/**
 * device 本地库访问
 */
@Repository
@RequiredArgsConstructor
public class DeviceRepository {

	private final JdbcTemplate jdbcTemplate;

	private static final RowMapper<Device> MAPPER = (rs, i) -> {
		Device d = new Device();
		d.setId(rs.getLong("id"));
		d.setSn(rs.getString("sn"));
		d.setName(rs.getString("name"));
		d.setType(rs.getString("type"));
		d.setModel(rs.getString("model"));
		d.setProto(rs.getString("proto"));
		d.setDeviceId(rs.getString("device_id"));
		d.setRegStatus(rs.getInt("reg_status"));
		return d;
	};

	/** 关键字模糊查询：sn / name / type / model */
	public List<Device> findAll(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return jdbcTemplate.query("SELECT * FROM device ORDER BY id", MAPPER);
		}
		String like = "%" + keyword.trim() + "%";
		return jdbcTemplate.query(
				"SELECT * FROM device WHERE sn LIKE ? OR name LIKE ? OR type LIKE ? OR model LIKE ? ORDER BY id",
				MAPPER, like, like, like, like);
	}

	public Optional<Device> findById(Long id) {
		List<Device> list = jdbcTemplate.query(
				"SELECT * FROM device WHERE id = ?", MAPPER, id);
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	public Optional<Device> findBySn(String sn) {
		List<Device> list = jdbcTemplate.query(
				"SELECT * FROM device WHERE sn = ?", MAPPER, sn);
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	public int insert(Device d) {
		return jdbcTemplate.update(
				"INSERT INTO device(sn,name,type,model,proto,device_id,reg_status) VALUES(?,?,?,?,?,?,?)",
				d.getSn(), d.getName(), d.getType(), d.getModel(), d.getProto(), d.getDeviceId(),
				d.getRegStatus() == null ? 0 : d.getRegStatus());
	}

	public int update(Device d) {
		return jdbcTemplate.update(
				"UPDATE device SET name=?, type=?, model=?, proto=?, device_id=?, "
						+ "update_time=CURRENT_TIMESTAMP WHERE id=?",
				d.getName(), d.getType(), d.getModel(), d.getProto(), d.getDeviceId(), d.getId());
	}

	/** 更新注册状态；deviceId 传 null 表示清空设备ID */
	public int updateRegStatus(Long id, int regStatus, String deviceId) {
		return jdbcTemplate.update(
				"UPDATE device SET reg_status=?, device_id=?, update_time=CURRENT_TIMESTAMP WHERE id=?",
				regStatus, deviceId, id);
	}

	public int deleteById(Long id) {
		return jdbcTemplate.update("DELETE FROM device WHERE id = ?", id);
	}
}
