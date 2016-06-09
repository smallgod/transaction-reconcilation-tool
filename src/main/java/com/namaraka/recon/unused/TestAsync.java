/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
@WebServlet(name = "TestAsync", urlPatterns = {"/testasync"}, asyncSupported = true)
public class TestAsync extends HttpServlet {

    private static final long serialVersionUID = -4534362350296103522L;

    private static final Logger logger = LoggerFactory.getLogger(TestAsync.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
         logger.info("client requesting....");
        
        final AsyncContext ac = request.startAsync();
        ac.setTimeout(0);

        ac.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                System.out.println("On complete");

                //ac.complete();
                logger.info("now completed async process");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                System.out.println("On timeout");
                ac.complete();
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                System.out.println("On error");
                ac.complete();
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                System.out.println("On start async");
            }
        });

        ac.start(new Runnable() {

            @Override
            public void run() {
                System.out.println("Async task: " + Thread.currentThread().getName());
                try {
                    //for (Part part : ((HttpServletRequest) ac.getRequest()).getParts()) {
                    System.out.println("File received"); // You Should write
                    // file here
                    // like
                    // part.write("fileName");
                    //}
                    Thread.sleep(6L);
                    logger.info("after sleeping!!");
                } catch (InterruptedException e1) {
                    e1.getMessage();
                }

                ac.complete();
                /*
                PrintWriter pw = null;
                try {
                    pw = ac.getResponse().getWriter();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                pw.write("end");
                pw.close();*/
            }

        });
       

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

}
