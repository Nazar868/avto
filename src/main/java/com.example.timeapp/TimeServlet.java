package com.example.timeapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {

        FileTemplateResolver resolver =
                new FileTemplateResolver();

        String templatePath =
                getServletContext().getRealPath("/WEB-INF/templates/");

        resolver.setPrefix(templatePath + "/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String timezone = req.getParameter("timezone");

        if (timezone == null || timezone.isBlank()) {

            Cookie[] cookies = req.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lastTimezone".equals(cookie.getName())) {
                        timezone = cookie.getValue();
                    }
                }
            }
        }

        if (timezone == null || timezone.isBlank()) {
            timezone = "UTC";
        }

        Cookie cookie =
                new Cookie("lastTimezone", timezone);

        cookie.setMaxAge(60 * 60 * 24 * 30);
        resp.addCookie(cookie);

        ZoneOffset offset;

        if ("UTC".equals(timezone)) {
            offset = ZoneOffset.UTC;
        } else {
            offset = ZoneOffset.of(
                    timezone.replace("UTC", "")
            );
        }

        OffsetDateTime currentTime =
                OffsetDateTime.now(offset);

        String formattedTime =
                currentTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"));

        Context context = new Context();

        context.setVariable("time", formattedTime);
        context.setVariable("timezone", timezone);

        resp.setContentType("text/html;charset=UTF-8");

        templateEngine.process(
                "time",
                context,
                resp.getWriter()
        );
    }
}
