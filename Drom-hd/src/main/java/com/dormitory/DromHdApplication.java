package com.dormitory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // 启用定时任务，每天自动扫描数据库，用于处理那些“休学超期”和“毕业超期”的学生。
public class DromHdApplication {

	public static void main(String[] args) {
		SpringApplication.run(DromHdApplication.class, args);
        System.out.println("################################################################################");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("项目启动成功，请访问：http://localhost:8080/api/swagger-ui.html 查看接口文档");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("#################################################################################");
        
        /**
         * 运行此方法来生成你的 BCrypt 加密后的密码
         */
//        // 1. 创建 BCryptPasswordEncoder 实例
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//        // 2. 定义明文密码
//        String adminPassword = "Admin@123";
//        String userPassword = "123456";
//
//        // 3. 生成 'Admin@123' 的哈希值
//        String hashedAdminPassword = passwordEncoder.encode(adminPassword);
//
//        // 4. 生成 '123456' 的哈希值
//        String hashedUserPassword = passwordEncoder.encode(userPassword);
//
//        // 5. 打印到控制台
//        System.out.println("====================================================================");
//        System.out.println("请将此哈希值用于 admin (user_id = 10001) 的密码:");
//        System.out.println(hashedAdminPassword);
//        System.out.println("====================================================================");
//        System.out.println("请将此哈希值用于所有其他用户的密码:");
//        System.out.println(hashedUserPassword);
//        System.out.println("====================================================================");
    
	}

}
