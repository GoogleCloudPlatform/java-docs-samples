/*
 * @author karansharma6190@gmail.com
 */


package com.smb.googleVision.googleVisionocr;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.smb.googleVision")
public class GoogleVisionOcrApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoogleVisionOcrApplication.class, args);
	}
}
