package com.example.filter;

import com.alibaba.fastjson2.JSON;
import com.example.config.BaseContext;
import com.example.config.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //过滤器，进行相关内容的过滤
    public static final AntPathMatcher antPathMatcher=new AntPathMatcher();//这是进行匹配用
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        log.info("开启拦截");
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();

        log.info(requestURI);
        String []urls=new String[]{

        };
//        这里还不能拦截backend下面的静态资源

        boolean check = check(requestURI, urls);

        if(check){
            //如果是urls中一个，就放行
            log.info("本次请求不需要处理");
            filterChain.doFilter(request,response);
            return;//就不用在往下执行
        }
        //System.out.println(check);;
        //判断是否是登录状态
        if(request.getSession().getAttribute("employee")!=null){
            //是登录状态就放行
            //log.info("用户已登录");
            //同一个线程内获取此线程id进行存储值操作,在过滤器中进行获取
            BaseContext.setId((Long) request.getSession().getAttribute("employee"));//同一线程内保存id

            filterChain.doFilter(request,response);
            return;
        }

        //判断移动端是否登录
        if(request.getSession().getAttribute("user")!=null){
            //是登录状态就放行
            //log.info("用户已登录");
            //同一个线程内获取此线程id进行存储值操作,在过滤器中进行获取
            BaseContext.setId((Long) request.getSession().getAttribute("user"));//同一线程内保存id

            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGI")));
        //System.out.println(JSON.parse(JSON.toJSONString(R.error("NOTLOGI"))));
        return;

    }
    //方法进行循环遍历
    public boolean check(String requestURI,String []urls){
        for(String url:urls){
            boolean match = antPathMatcher.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
