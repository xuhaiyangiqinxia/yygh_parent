package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R err(Exception e){
        return R.error();
    }

    @ExceptionHandler(ArithmeticException.class)
    public R error(ArithmeticException e){
        return R.error().message("数学异常");
    }

    @ExceptionHandler(YyghException.class)
    public R yyghError(YyghException e){
        return R.error().code(e.getCode()).message(e.getMsg());
    }


}
