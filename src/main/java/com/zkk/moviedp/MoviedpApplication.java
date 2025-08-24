package com.zkk.moviedp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.zkk.moviedp.mapper")
@SpringBootApplication
public class MoviedpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviedpApplication.class, args);
	}

}
