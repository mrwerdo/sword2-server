package org.swordapp.server.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.server.MediaResourceAPI;
import org.swordapp.server.MediaResourceManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MediaResourceServletDefault extends SwordServlet {
    private static final long serialVersionUID = 3073624304950465741L;
    private static Logger log = LoggerFactory.getLogger(MediaResourceServletDefault.class);
    
    protected transient MediaResourceAPI api;

    public void init() throws ServletException {
        super.init();

        // load the Media Resource Manager
        MediaResourceManager mrm = (MediaResourceManager) this.loadImplClass("media-resource-impl", false);

        // load the api
        this.api = new MediaResourceAPI(mrm, this.config);
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
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.post(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.put(req, resp);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.delete(req, resp);
    }
}
