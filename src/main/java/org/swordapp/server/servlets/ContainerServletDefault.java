package org.swordapp.server.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.server.ContainerAPI;
import org.swordapp.server.ContainerManager;
import org.swordapp.server.StatementManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContainerServletDefault extends SwordServlet {
    private static final long serialVersionUID = 5943438798706617147L;
    private static Logger log = LoggerFactory.getLogger(ContainerServletDefault.class);
    
    private transient ContainerAPI api;
    
    public void init() throws ServletException {
        super.init();

        // load the container manager implementation
        ContainerManager cm = (ContainerManager) this.loadImplClass("container-impl", false);

        // load the container manager implementation
        StatementManager sm = (StatementManager) this.loadImplClass("statement-impl", false);

        // initialise the underlying servlet processor
        this.api = new ContainerAPI(cm, sm, this.config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.get(req, resp);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.head(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.put(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.post(req, resp);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.delete(req, resp);
    }
}
