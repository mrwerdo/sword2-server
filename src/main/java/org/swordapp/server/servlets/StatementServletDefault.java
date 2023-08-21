package org.swordapp.server.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.server.StatementAPI;
import org.swordapp.server.StatementManager;

public class StatementServletDefault extends SwordServlet {
    private static final long serialVersionUID = -3559985225243246804L;
    private static Logger log = LoggerFactory.getLogger(StatementServletDefault.class);
    
    private transient StatementAPI statementApi;

    public void init() throws ServletException {
        super.init();

        // load the container manager implementation
        StatementManager sm = (StatementManager) this.loadImplClass("statement-impl", false);

        // initialise the underlying servlet processor
        this.statementApi = new StatementAPI(sm, this.config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.statementApi.get(req, resp);
    }
}
