import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import javax.swing.JOptionPane;
import java.awt.Component;

public class MainFrame extends JFrame {
    AddressBookService service = new AddressBookService();
    DefaultTableModel tableModel;
    JTable table;


    public MainFrame(AddressBookService service) {
            this.service = service; // 复用登录时的service，loginUserId不会丢失
            setTitle("通讯录管理系统");
            setSize(920, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

        // 顶部按钮
        JPanel topPanel = new JPanel();
        JButton addBtn = new JButton("新增联系人");
        JButton editBtn = new JButton("编辑联系人");
        JButton delBtn = new JButton("删除联系人");
        JButton groupBtn = new JButton("分组管理");
        JButton searchBtn = new JButton("多条件查询");
        JButton resetBtn = new JButton("重置");
        resetBtn.addActionListener(e -> {
            refreshTable();
        });
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(delBtn);
        topPanel.add(groupBtn);
        topPanel.add(searchBtn);
        topPanel.add(resetBtn);

        // 表格
        String[] col = {"ID","姓名","电话","省","市","区县","单位","邮箱","所属分组"};
        tableModel = new DefaultTableModel(col,0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // 绑定事件
        addBtn.addActionListener(e->addDialog());
        editBtn.addActionListener(e->editDialog());
        delBtn.addActionListener(e->deleteContact());
        groupBtn.addActionListener(e->groupOperate());
        searchBtn.addActionListener(e->searchDialog());

        refreshTable();
    }

    // 刷新表格
    void refreshTable() {
        tableModel.setRowCount(0);
        List<Contact> list = service.queryAllContact();
        // 空数据判断
        if(list.isEmpty()){
            Object[] tip = {"暂无联系人数据","","","","","","","",""};
            tableModel.addRow(tip);
            return;
        }
        for(Contact c : list){
            Object[] row = {c.getId(),c.getName(),c.getPhone(),c.getProvince(),
                    c.getCity(),c.getCounty(),c.getCompany(),c.getEmail(),c.getGroupName()};
            tableModel.addRow(row);
        }
    }

    // 新增弹窗
    void addDialog() {
        List<Group> groups = service.getAllGroup();
        JComboBox<Group> groupBox = new JComboBox<>();
        Group defaultGroup = null;
        for(Group g : groups){
            groupBox.addItem(g);
            if(g.getIsDefault() == 1) defaultGroup = g;
        }
        // 默认选中默认分组
        if(defaultGroup != null) groupBox.setSelectedItem(defaultGroup);
// 下拉框显示名称+ID
        groupBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof Group){
                    Group g = (Group) value;
                    setText(g.getGroupName() + "(" + g.getId() + ")");
                }
                return this;
            }
        });

        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField pro = new JTextField();
        JTextField city = new JTextField();
        JTextField county = new JTextField();
        JTextField company = new JTextField();
        JTextField email = new JTextField();
        JTextField remark = new JTextField();

        Object[] msg = {
                "姓名",name,"手机号",phone,"省份",pro,"城市",city,"区县",county,
                "单位",company,"邮箱",email,"备注",remark,"分组",groupBox
        };
        int res = JOptionPane.showConfirmDialog(this,msg,"新增联系人",JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            Contact c = new Contact();
            c.setName(name.getText());
            c.setPhone(phone.getText());
            c.setProvince(pro.getText());
            c.setCity(city.getText());
            c.setCounty(county.getText());
            c.setCompany(company.getText());
            c.setEmail(email.getText());
            c.setRemark(remark.getText());
            // 取分组ID
            Group selectG = (Group) groupBox.getSelectedItem();
            c.setGroupId(selectG.getId());
            service.addContact(c);
            refreshTable();
        }
    }

    // 编辑弹窗
    void editDialog() {
        int row = table.getSelectedRow();
        if(row == -1){
            JOptionPane.showMessageDialog(this,"请先选中一行联系人");
            return;
        }
        Object idValue = tableModel.getValueAt(row, 0);
        if (!(idValue instanceof Integer)) {
            JOptionPane.showMessageDialog(this, "当前没有可编辑的联系人");
            return;
        }
        int cid = (Integer) idValue;
        String name = tableModel.getValueAt(row,1).toString();
        String phone = tableModel.getValueAt(row,2).toString();
        String pro = tableModel.getValueAt(row,3).toString();
        String city = tableModel.getValueAt(row,4).toString();
        String county = tableModel.getValueAt(row,5).toString();
        String com = tableModel.getValueAt(row,6).toString();
        String mail = tableModel.getValueAt(row,7).toString();
        // 先拿到表格里的分组名称字符串
        String groupName = tableModel.getValueAt(row, 8).toString();
        List<Group> groups = service.getAllGroup();

        JComboBox<Group> groupBox = new JComboBox<>();

        for (Group g : groups) {
            groupBox.addItem(g);

            if (g.getGroupName().equals(groupName)) {
                groupBox.setSelectedItem(g);
            }
        }

// 自定义渲染：显示 名称(Id)
        groupBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Group) {
                    Group g = (Group) value;
                    setText(g.getGroupName() + "(" + g.getId() + ")");
                }

                return this;
            }
        });

        JTextField tfName = new JTextField(name);
        JTextField tfPhone = new JTextField(phone);
        JTextField tfPro = new JTextField(pro);
        JTextField tfCity = new JTextField(city);
        JTextField tfCounty = new JTextField(county);
        JTextField tfCom = new JTextField(com);
        JTextField tfMail = new JTextField(mail);

        Object[] msg = {
                "姓名",tfName,"手机号",tfPhone,"省份",tfPro,"城市",tfCity,"区县",tfCounty,
                "单位",tfCom,"邮箱",tfMail,"分组",groupBox
        };
        int opt = JOptionPane.showConfirmDialog(this,msg,"编辑联系人",JOptionPane.OK_CANCEL_OPTION);
        if(opt == JOptionPane.OK_OPTION){
            Contact c = new Contact();
            c.setId(cid);
            c.setName(tfName.getText());
            c.setPhone(tfPhone.getText());
            c.setProvince(tfPro.getText());
            c.setCity(tfCity.getText());
            c.setCounty(tfCounty.getText());
            c.setCompany(tfCom.getText());
            c.setEmail(tfMail.getText());
            Group selectGroup = (Group) groupBox.getSelectedItem();
            c.setGroupId(selectGroup.getId());
            service.updateContact(c);
            refreshTable();
        }
    }

    // 删除联系人
    void deleteContact() {
        int row = table.getSelectedRow();
        if(row == -1){
            JOptionPane.showMessageDialog(this,"请选中联系人");
            return;
        }
        Object idValue = tableModel.getValueAt(row, 0);
        if (!(idValue instanceof Integer)) {
            JOptionPane.showMessageDialog(this, "当前没有可删除的联系人");
            return;
        }
        int cid = (Integer) idValue;
        int sure = JOptionPane.showConfirmDialog(this,"确定删除该联系人？","确认",JOptionPane.YES_NO_OPTION);
        if(sure == JOptionPane.YES_OPTION){
            service.deleteContact(cid);
            refreshTable();
        }
    }

    // 分组操作弹窗
    void groupOperate() {
        String opt = JOptionPane.showInputDialog("输入1新增分组，输入2删除分组");
        if(opt == null) return;
        if(opt.equals("1")){
            String gname = JOptionPane.showInputDialog("输入分组名称");
            if(gname != null && !gname.isBlank()) service.addGroup(gname);
        }else if(opt.equals("2")){
            String gidStr = JOptionPane.showInputDialog("输入要删除的分组ID");
            try{
                int gid = Integer.parseInt(gidStr);
                service.delGroup(gid);
            }catch (Exception e){
                JOptionPane.showMessageDialog(this,"ID数字格式错误");
            }
        }
    }

    // 多条件查询弹窗
    void searchDialog() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField pro = new JTextField();
        JTextField com = new JTextField();
        Object[] msg = {"姓名",name,"手机号",phone,"省份",pro,"单位",com};
        int res = JOptionPane.showConfirmDialog(this,msg,"多条件查询",JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            List<Contact> list = service.searchContact(name.getText(),phone.getText(),pro.getText(),com.getText());
            if(list.isEmpty()){
                JOptionPane.showMessageDialog(this,"未找到匹配联系人");
            }
            tableModel.setRowCount(0);
            for(Contact c : list){
                Object[] row = {
                        c.getId(),
                        c.getName(),
                        c.getPhone(),
                        c.getProvince(),
                        c.getCity(),
                        c.getCounty(),
                        c.getCompany(),
                        c.getEmail(),
                        c.getGroupName()
                };
                tableModel.addRow(row);
            }
        }
    }
}