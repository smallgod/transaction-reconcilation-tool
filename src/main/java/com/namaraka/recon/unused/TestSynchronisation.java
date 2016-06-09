/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class TestSynchronisation extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(TestSynchronisation.class);
    private static final long serialVersionUID = 3857973520549244795L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet TestSynchronisation</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet TestSynchronisation at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
        response.flushBuffer();

        logger.info("Done writing response, Going to submit to Future...");

        ExecutorService fileUploaderExecutor = Executors.newSingleThreadExecutor();
        Runnable runnable = new Task();

        fileUploaderExecutor.execute(runnable);
        fileUploaderExecutor.shutdown();

        logger.info("done getting future, exiting processRequest method");

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private final String mutex = "";

    private class Task implements Runnable {

        @Override
        public void run() {

            logger.info("about to sleep for 1 minute");
            
            synchronized (mutex) {
                logger.info("this block is synchronized");

                try {
                    Thread.sleep(60000L);
                } catch (InterruptedException ex) {
                    logger.error("thread interrupted: " + ex.getMessage());
                }
            }

            logger.info("after sleeping!");
        }

    }
}
