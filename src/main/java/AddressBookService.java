import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

public class AddressBookService {
    private Integer loginUserId = null;

    public AddressBookService() {
        initDatabase();
    }

    // 初始化数据库表
    private void initDatabase() {
        try (Connection conn = DBUtil.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS address_book DEFAULT CHARSET utf8mb4");
            st.executeUpdate("USE address_book");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(100) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `grp` (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "group_name VARCHAR(50) NOT NULL," +
                    "is_default TINYINT NOT NULL DEFAULT 0," +
                    "user_id INT NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS contact (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "phone VARCHAR(20)," +
                    "province VARCHAR(50)," +
                    "city VARCHAR(50)," +
                    "county VARCHAR(50)," +
                    "company VARCHAR(100)," +
                    "email VARCHAR(100)," +
                    "remark VARCHAR(200)," +
                    "group_id INT," +
                    "user_id INT NOT NULL," +
                    "FOREIGN KEY (group_id) REFERENCES `grp`(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 用户登录
    public User login(String username, String pwd) {
        String sql = "SELECT id, username, password FROM user WHERE username = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, pwd);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                loginUserId = u.getId();
                return u;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "登录失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 注册账号
    public boolean register(String username, String pwd) {
        // 判断账号重复
        String checkSql = "SELECT id FROM user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // 插入新用户
        String insertSql = "INSERT INTO user (username, password) VALUES (?, ?)";
        int newUserId = -1;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, pwd);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) newUserId = keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // 为新用户创建默认分组
        if (newUserId > 0) {
            createDefaultGroup(newUserId);
        }
        return true;
    }

    // 为指定用户创建默认分组
    private void createDefaultGroup(int userId) {
        String sql = "INSERT INTO grp (group_name, is_default, user_id) VALUES ('默认', 1, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询当前用户所有分组
    public List<Group> getAllGroup() {
        List<Group> groupList = new ArrayList<>();
        if (loginUserId == null) return groupList;
        String sql = "SELECT id, group_name, is_default, user_id FROM grp WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loginUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Group g = new Group();
                g.setId(rs.getInt("id"));
                g.setGroupName(rs.getString("group_name"));
                g.setIsDefault(rs.getInt("is_default"));
                g.setUserId(rs.getInt("user_id"));
                groupList.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupList;
    }

    // 新增分组
    public boolean addGroup(String name) {
        if (loginUserId == null) return false;
        // 查重：当前用户下是否有同名分组
        String checkSql = "SELECT id FROM grp WHERE group_name = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, name);
            ps.setInt(2, loginUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "分组名称重复");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        String insertSql = "INSERT INTO grp (group_name, is_default, user_id) VALUES (?, 0, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, name);
            ps.setInt(2, loginUserId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "分组创建成功");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除分组，默认分组不可删，联系人迁移默认分组
    public void delGroup(int gid) {
        if (loginUserId == null) return;
        // 查找默认分组ID
        int defaultId = 0;
        boolean found = false;
        List<Group> groups = getAllGroup();
        for (Group g : groups) {
            if (g.getIsDefault() == 1) defaultId = g.getId();
            if (g.getId() == gid) {
                found = true;
                if (g.getIsDefault() == 1) {
                    JOptionPane.showMessageDialog(null, "默认分组禁止删除");
                    return;
                }
            }
        }
        if (!found) {
            JOptionPane.showMessageDialog(null, "未找到该分组");
            return;
        }
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 将该分组下的联系人迁移到默认分组
                String migrateSql = "UPDATE contact SET group_id = ? WHERE group_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(migrateSql)) {
                    ps.setInt(1, defaultId);
                    ps.setInt(2, gid);
                    ps.setInt(3, loginUserId);
                    ps.executeUpdate();
                }
                // 删除分组
                String delSql = "DELETE FROM grp WHERE id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                    ps.setInt(1, gid);
                    ps.setInt(2, loginUserId);
                    ps.executeUpdate();
                }
                conn.commit();
                JOptionPane.showMessageDialog(null, "分组删除完成，联系人移入默认分组");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }

    // 新增联系人
    public void addContact(Contact c) {
        if (loginUserId == null) {
            JOptionPane.showMessageDialog(null, "登录失效，请重新登录！");
            return;
        }
        c.setUserId(loginUserId);
        String sql = "INSERT INTO contact (name, phone, province, city, county, company, email, remark, group_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getProvince());
            ps.setString(4, c.getCity());
            ps.setString(5, c.getCounty());
            ps.setString(6, c.getCompany());
            ps.setString(7, c.getEmail());
            ps.setString(8, c.getRemark());
            if (c.getGroupId() != null) ps.setInt(9, c.getGroupId());
            else ps.setNull(9, Types.INTEGER);
            ps.setInt(10, c.getUserId());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "新增联系人成功");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "新增失败: " + e.getMessage());
        }
    }

    // 查询当前用户全部联系人
    public List<Contact> queryAllContact() {
        List<Contact> list = new ArrayList<>();
        if (loginUserId == null) return list;
        String sql = "SELECT c.id, c.name, c.phone, c.province, c.city, c.county, " +
                "c.company, c.email, c.remark, c.group_id, c.user_id, g.group_name " +
                "FROM contact c LEFT JOIN grp g ON c.group_id = g.id " +
                "WHERE c.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loginUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Contact c = new Contact();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setProvince(rs.getString("province"));
                c.setCity(rs.getString("city"));
                c.setCounty(rs.getString("county"));
                c.setCompany(rs.getString("company"));
                c.setEmail(rs.getString("email"));
                c.setRemark(rs.getString("remark"));
                c.setGroupId(rs.getObject("group_id") != null ? rs.getInt("group_id") : null);
                c.setUserId(rs.getInt("user_id"));
                c.setGroupName(rs.getString("group_name"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 多条件模糊查询
    public List<Contact> searchContact(String name, String phone, String province, String company) {
        if (loginUserId == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT c.id, c.name, c.phone, c.province, c.city, c.county, " +
                "c.company, c.email, c.remark, c.group_id, c.user_id, g.group_name " +
                "FROM contact c LEFT JOIN grp g ON c.group_id = g.id " +
                "WHERE c.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(loginUserId);
        if (!name.isEmpty()) { sql.append(" AND c.name LIKE ?"); params.add("%" + name + "%"); }
        if (!phone.isEmpty()) { sql.append(" AND c.phone LIKE ?"); params.add("%" + phone + "%"); }
        if (!province.isEmpty()) { sql.append(" AND c.province LIKE ?"); params.add("%" + province + "%"); }
        if (!company.isEmpty()) { sql.append(" AND c.company LIKE ?"); params.add("%" + company + "%"); }

        List<Contact> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Contact c = new Contact();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setProvince(rs.getString("province"));
                c.setCity(rs.getString("city"));
                c.setCounty(rs.getString("county"));
                c.setCompany(rs.getString("company"));
                c.setEmail(rs.getString("email"));
                c.setRemark(rs.getString("remark"));
                c.setGroupId(rs.getObject("group_id") != null ? rs.getInt("group_id") : null);
                c.setUserId(rs.getInt("user_id"));
                c.setGroupName(rs.getString("group_name"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 修改联系人
    public void updateContact(Contact target) {
        if (loginUserId == null) return;
        String sql = "UPDATE contact SET name=?, phone=?, province=?, city=?, county=?, " +
                "company=?, email=?, remark=?, group_id=? WHERE id=? AND user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, target.getName());
            ps.setString(2, target.getPhone());
            ps.setString(3, target.getProvince());
            ps.setString(4, target.getCity());
            ps.setString(5, target.getCounty());
            ps.setString(6, target.getCompany());
            ps.setString(7, target.getEmail());
            ps.setString(8, target.getRemark());
            if (target.getGroupId() != null) ps.setInt(9, target.getGroupId());
            else ps.setNull(9, Types.INTEGER);
            ps.setInt(10, target.getId());
            ps.setInt(11, loginUserId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "修改成功");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "修改失败: " + e.getMessage());
        }
    }

    // 删除联系人
    public void deleteContact(int cid) {
        if (loginUserId == null) return;
        String sql = "DELETE FROM contact WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.setInt(2, loginUserId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "删除完成");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }

    public Integer getLoginUserId() { return loginUserId; }
}
