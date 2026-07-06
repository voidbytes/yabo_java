package com.example;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // BCrypt 工作因子，值越大越安全但越慢，10 是推荐值
    private static final int WORK_FACTOR = 10;

    // 对明文密码进行哈希加盐
    // BCrypt.hashpw 内部自动生成随机盐，哈希结果包含盐值
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    // 验证明文密码是否匹配哈希值
    // BCrypt.checkpw 从哈希值中提取盐值，再用相同盐值哈希明文后比较
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
