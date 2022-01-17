package dev.vality.trusted.tokens.servlet;

import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import dev.vality.trusted.tokens.TrustedTokensSrv;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/trusted/tokens")
public class TrustedTokenServlet extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private TrustedTokensSrv.Iface requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder().build(TrustedTokensSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
