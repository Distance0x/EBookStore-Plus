package com.llama.ebookfuncservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.function.Function;

@Configuration
public class PriceFunctions {

    @Bean
    public Function<PriceRequest, PriceResponse> calculateTotal() {
        return request -> {
            if (request.getPrice() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "价格不能为空");
            }
            if (request.getQuantity() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量不能为空");
            }
            if (request.getQuantity() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量不能为负数");
            }
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "价格不能为负数");
            }

            BigDecimal total = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
            return new PriceResponse(request.getPrice(), request.getQuantity(), total);
        };
    }
}


