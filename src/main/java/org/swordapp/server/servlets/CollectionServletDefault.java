package org.swordapp.server.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.server.CollectionAPI;
import org.swordapp.server.CollectionDepositManager;
import org.swordapp.server.CollectionListManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CollectionServletDefault extends SwordServlet {
    private static Logger log = LoggerFactory.getLogger(CollectionServletDefault.class);

    protected CollectionListManager clm = null;
    protected CollectionDepositManager cdm;
    protected CollectionAPI api;

    public void init() throws ServletException {
        super.init();

        // load the collection list manager implementation
        Object possibleClm = this.loadImplClass("collection-list-impl", true); // allow null
        this.clm = possibleClm == null ? null : (CollectionListManager) possibleClm;

        // load the deposit manager implementation
        this.cdm = (CollectionDepositManager) this.loadImplClass("collection-deposit-impl", false);

        // load the API
        this.api = new CollectionAPI(this.clm, this.cdm, this.config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.get(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.post(req, resp);
    }
}
