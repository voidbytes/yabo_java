import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    AddressBookService service = new AddressBookService();

    public LoginFrame() {
        setTitle("通讯录登录");
        setSize(330, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4,2,10,10));
        setLocationRelativeTo(null);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton loginBtn = new JButton("登录");
        JButton regBtn = new JButton("注册新账号");

        add(new JLabel("账号："));
        add(username);
        add(new JLabel("密码："));
        add(password);
        add(loginBtn);
        add(regBtn);

        // 登录按钮
        loginBtn.addActionListener(e->{
            String un = username.getText().trim();
            String pw = new String(password.getPassword()).trim();
            User user = service.login(un,pw);
            if(user == null){
                JOptionPane.showMessageDialog(this,"账号或密码错误！");
            }else{
                JOptionPane.showMessageDialog(this,"登录成功");
                this.dispose();
                new MainFrame(service).setVisible(true);
            }
        });

        // 注册弹窗
        regBtn.addActionListener(e -> {
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPasswordField confirmPasswordField = new JPasswordField();

            Object[] msg = {
                    "注册账号：", usernameField,
                    "登录密码：", passwordField,
                    "确认密码：", confirmPasswordField
            };

            int result = JOptionPane.showConfirmDialog(
                    this,
                    msg,
                    "注册新账号",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                String un = usernameField.getText().trim();
                String pw = new String(passwordField.getPassword()).trim();
                String confirmPw = new String(confirmPasswordField.getPassword()).trim();

                if (un.isBlank() || pw.isBlank() || confirmPw.isBlank()) {
                    JOptionPane.showMessageDialog(this, "账号和密码不能为空");
                    return;
                }

                if (un.length() < 3) {
                    JOptionPane.showMessageDialog(this, "账号长度不能少于3位");
                    return;
                }

                if (pw.length() < 6) {
                    JOptionPane.showMessageDialog(this, "密码长度不能少于6位");
                    return;
                }

                if (!pw.equals(confirmPw)) {
                    JOptionPane.showMessageDialog(this, "两次输入的密码不一致");
                    return;
                }

                boolean success = service.register(un, pw);

                if (success) {
                    JOptionPane.showMessageDialog(this, "注册成功，请登录");
                } else {
                    JOptionPane.showMessageDialog(this, "注册失败，该账号已存在");
                }
            }
        });
    }
}