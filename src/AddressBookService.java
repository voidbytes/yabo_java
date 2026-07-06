import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

public class AddressBookService {
    private Integer loginUserId = null;

    // 用户登录
    public User login(String username, String pwd) {
        List<String> userData = FileUtil.readAllLines(FileUtil.USER_FILE);
        for(String line : userData){
            String[] arr = line.split(",");
            int id = Integer.parseInt(arr[0]);
            String un = arr[1];
            String pw = arr[2];
            if(un.equals(username) && pw.equals(pwd)){
                User u = new User();
                u.setId(id);
                u.setUsername(un);
                u.setPassword(pw);
                loginUserId = id;
                return u;
            }
        }
        return null;
    }

    // 注册账号
    public boolean register(String username, String pwd) {
        List<String> userData = FileUtil.readAllLines(FileUtil.USER_FILE);

        // 判断账号重复
        for (String line : userData) {
            String un = line.split(",")[1];

            if (un.equals(username)) {
                return false;
            }
        }

        // 生成id
        int newId = userData.size() + 1;
        FileUtil.writeLine(FileUtil.USER_FILE, newId + "," + username + "," + pwd);

        // 为新用户创建默认分组
        createDefaultGroup(newId);

        return true;
    }

    // 为指定用户创建默认分组
    private void createDefaultGroup(int userId) {
        List<String> groups = FileUtil.readAllLines(FileUtil.GROUP_FILE);
        int maxId = 0;
        for (String line : groups) {
            int id = Integer.parseInt(line.split(",")[0]);
            if (id > maxId) maxId = id;
        }
        FileUtil.writeLine(FileUtil.GROUP_FILE, (maxId + 1) + ",默认,1," + userId);
    }

    // 查询当前用户所有分组
    public List<Group> getAllGroup() {
        List<Group> groupList = new ArrayList<>();
        List<String> data = FileUtil.readAllLines(FileUtil.GROUP_FILE);
        for(String line : data){
            String[] arr = line.split(",");
            // 旧格式兼容：少于4个字段的跳过（属于其他用户或脏数据）
            if (arr.length < 4) continue;
            int uid = Integer.parseInt(arr[3]);
            if (uid != loginUserId) continue;
            Group g = new Group();
            g.setId(Integer.parseInt(arr[0]));
            g.setGroupName(arr[1]);
            g.setIsDefault(Integer.parseInt(arr[2]));
            g.setUserId(uid);
            groupList.add(g);
        }
        return groupList;
    }

    // 新增分组
    public boolean addGroup(String name) {
        List<String> groups = FileUtil.readAllLines(FileUtil.GROUP_FILE);
        for(String line : groups){
            String[] arr = line.split(",");
            if (arr.length < 4) continue;
            int uid = Integer.parseInt(arr[3]);
            if (uid != loginUserId) continue;
            String gName = arr[1];
            if(gName.equals(name)){
                JOptionPane.showMessageDialog(null,"分组名称重复");
                return false;
            }
        }
        int maxId = 0;
        for(String line : groups){
            int id = Integer.parseInt(line.split(",")[0]);
            if(id > maxId) maxId = id;
        }
        FileUtil.writeLine(FileUtil.GROUP_FILE, (maxId+1)+","+name+",0,"+loginUserId);
        JOptionPane.showMessageDialog(null,"分组创建成功");
        return true;
    }

    // 删除分组，默认分组不可删，联系人迁移默认分组
    public void delGroup(int gid) {
        List<Group> allGroup = getAllGroup();
        int defaultId = 0;
        boolean found = false;
        for(Group g : allGroup){
            if(g.getIsDefault() == 1) defaultId = g.getId();
            if(g.getId() == gid){
                found = true;
                if(g.getIsDefault() == 1){
                    JOptionPane.showMessageDialog(null,"默认分组禁止删除");
                    return;
                }
            }
        }
        if(!found){
            JOptionPane.showMessageDialog(null,"未找到该分组");
            return;
        }
        // 联系人更换分组id
        List<String> contactData = FileUtil.readAllLines(FileUtil.CONTACT_FILE);
        List<String> newContact = new ArrayList<>();
        for(String line : contactData){
            String[] arr = line.split(",");
            if (arr.length < 11) { newContact.add(line); continue; }
            int gId = Integer.parseInt(arr[9]);
            int uid = Integer.parseInt(arr[10]);
            if(gId == gid && uid == loginUserId) arr[9] = defaultId+"";
            newContact.add(String.join(",",arr));
        }
        FileUtil.writeAll(FileUtil.CONTACT_FILE, newContact);
        // 删除分组
        List<String> groupData = FileUtil.readAllLines(FileUtil.GROUP_FILE);
        List<String> newGroup = groupData.stream()
                .filter(s->{
                    String[] a = s.split(",");
                    if (a.length < 4) return true;
                    int id = Integer.parseInt(a[0]);
                    int uid = Integer.parseInt(a[3]);
                    return !(id == gid && uid == loginUserId);
                })
                .collect(Collectors.toList());
        FileUtil.writeAll(FileUtil.GROUP_FILE, newGroup);
        JOptionPane.showMessageDialog(null,"分组删除完成，联系人移入默认分组");
    }

    // 新增联系人
    public void addContact(Contact c) {
        // 拦截未登录状态，避免userId为null写入文件
        if (loginUserId == null) {
            JOptionPane.showMessageDialog(null, "登录失效，请重新登录！");
            return;
        }
        c.setUserId(loginUserId);
        List<String> data = FileUtil.readAllLines(FileUtil.CONTACT_FILE);
        int newId = data.size() + 1;
        String line = newId+","+c.getName()+","+c.getPhone()+","+c.getProvince()+","
                +c.getCity()+","+c.getCounty()+","+c.getCompany()+","+c.getEmail()+","
                +c.getRemark()+","+c.getGroupId()+","+c.getUserId();
        FileUtil.writeLine(FileUtil.CONTACT_FILE, line);
        JOptionPane.showMessageDialog(null,"新增联系人成功");
    }

    // 查询当前用户全部联系人
    public List<Contact> queryAllContact() {
        List<Contact> list = new ArrayList<>();
        List<String> data = FileUtil.readAllLines(FileUtil.CONTACT_FILE);
        List<Group> allGroups = getAllGroup();
        Map<Integer,String> groupNameMap = new HashMap<>();
        for(Group g : allGroups){
            groupNameMap.put(g.getId(), g.getGroupName());
        }
        for(String line : data){
            String[] arr = line.split(",");
            // 字段不足11个直接跳过
            if(arr.length < 11){
                continue;
            }
            int uid;
            try {
                uid = Integer.parseInt(arr[10]);
            } catch (NumberFormatException e) {
                // 用户ID不是数字，脏数据直接跳过
                continue;
            }
            if(uid != loginUserId) continue;
            Contact c = new Contact();
            c.setId(Integer.parseInt(arr[0]));
            c.setName(arr[1]);
            c.setPhone(arr[2]);
            c.setProvince(arr[3]);
            c.setCity(arr[4]);
            c.setCounty(arr[5]);
            c.setCompany(arr[6]);
            c.setEmail(arr[7]);
            c.setRemark(arr[8]);
            c.setGroupId(Integer.parseInt(arr[9]));
            c.setUserId(uid);
            c.setGroupName(groupNameMap.get(c.getGroupId()));
            list.add(c);
        }
        return list;
    }

    // 多条件模糊查询
    public List<Contact> searchContact(String name, String phone, String province, String company) {
        List<Contact> all = queryAllContact();
        return all.stream().filter(c->{
            boolean match = true;
            if(!name.isEmpty()) match &= c.getName().contains(name);
            if(!phone.isEmpty()) match &= c.getPhone().contains(phone);
            if(!province.isEmpty()) match &= c.getProvince().contains(province);
            if(!company.isEmpty()) match &= c.getCompany().contains(company);
            return match;
        }).collect(Collectors.toList());
    }

    // 修改联系人
    public void updateContact(Contact target) {
        List<String> data = FileUtil.readAllLines(FileUtil.CONTACT_FILE);
        List<String> newData = new ArrayList<>();
        for(String line : data){
            String[] arr = line.split(",");
            int id = Integer.parseInt(arr[0]);
            if(id == target.getId()){
                String newLine = id+","+target.getName()+","+target.getPhone()+","
                        +target.getProvince()+","+target.getCity()+","+target.getCounty()+","
                        +target.getCompany()+","+target.getEmail()+","+target.getRemark()+","
                        +target.getGroupId()+","+loginUserId;
                newData.add(newLine);
            }else{
                newData.add(line);
            }
        }
        FileUtil.writeAll(FileUtil.CONTACT_FILE, newData);
        JOptionPane.showMessageDialog(null,"修改成功");
    }

    // 删除联系人
    public void deleteContact(int cid) {
        List<String> data = FileUtil.readAllLines(FileUtil.CONTACT_FILE);
        List<String> newData = data.stream()
                .filter(s->Integer.parseInt(s.split(",")[0]) != cid)
                .collect(Collectors.toList());
        FileUtil.writeAll(FileUtil.CONTACT_FILE, newData);
        JOptionPane.showMessageDialog(null,"删除完成");
    }

    public Integer getLoginUserId() { return loginUserId; }
}