package com.software.yanchang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@MapperScan("com.software.yanchang.dao")
@PropertySource(value = {"classpath:/application.properties"}, encoding = "utf-8")
public class FastdfsApplication {

	@Value("${breakpoint.upload.dir}")
	private static String path;
	public static void main(String[] args) {
		SpringApplication.run(FastdfsApplication.class, args);
	}
}
