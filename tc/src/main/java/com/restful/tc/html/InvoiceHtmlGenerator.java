package com.restful.tc.html;

import com.restful.tc.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class InvoiceHtmlGenerator {

    @Autowired
    private TemplateEngine templateEngine;

    public String generateHtml(Invoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        return templateEngine.process("invoice", context); // "invoice" is the name of the Thymeleaf template
    }
}