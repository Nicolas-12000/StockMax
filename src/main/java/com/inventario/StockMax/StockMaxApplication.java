package com.inventario.StockMax;

import com.inventario.StockMax.config.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockMaxApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(StockMaxApplication.class);
		app.addInitializers(new DotenvInitializer());
		app.run(args);
	}

}
