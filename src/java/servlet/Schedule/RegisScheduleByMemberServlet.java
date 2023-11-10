/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.Schedule;

import dao.MemberDAO;
import dao.ScheduleDAO;
import dto.MemberDTO;
import dto.ScheduleDTO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Admin
 */
public class RegisScheduleByMemberServlet extends HttpServlet {

    public static List<Date> convertLocalDateToDate(List<LocalDate> localDateList) {
        List<Date> dateList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            dateList.add(java.sql.Date.valueOf(localDate));
        }
        return dateList;
    }

    public static LocalDate getMondayOfWeek(LocalDate now) {
        // Create a LocalDate object for the specific date
        LocalDate date = now;

        // Get the day of the week for the given date
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Calculate the number of days to subtract to get to Monday (assuming Monday is the first day of the week)
        int daysToSubtract = (dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue() + 7) % 7;

        // Subtract the calculated number of days from the given date to get the Monday of the week
        LocalDate mondayOfWeek = date.minusDays(daysToSubtract);
        return mondayOfWeek;
    }

    public static ArrayList<LocalDate> getWeek(LocalDate day) {
        ArrayList<LocalDate> week = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            week.add(day);
            day = day.plusDays(1);
        }
        return week;
    }

    public ArrayList<LocalDate> getAllMonday() {
        List<LocalDate> firstDaysOfWeek = new ArrayList<>();
        LocalDate currentDate = LocalDate.now(); // Ngày hiện tại
        // Lấy ngày đầu tiên của tuần hiện tại (ngày thứ Hai)
        LocalDate firstDayOfCurrentWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        firstDaysOfWeek.add(firstDayOfCurrentWeek);
        // Lấy ngày đầu tiên của tuần sau (ngày thứ Hai)
        LocalDate firstDayOfNextWeek = firstDayOfCurrentWeek.plusWeeks(1);
        firstDaysOfWeek.add(firstDayOfNextWeek);
        return (ArrayList<LocalDate>) firstDaysOfWeek;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            
            
            LocalDate currentDate = LocalDate.now();
            if (request.getParameter("selectMondayOfWeek") != null) {
                String selectMondayOfWeek = request.getParameter("selectMondayOfWeek");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                currentDate = LocalDate.parse(selectMondayOfWeek, formatter);
            }
            Cookie[] c = request.getCookies();
            String userId = "";
            if (c != null) {
                for (Cookie aCookie : c) {
                    if (aCookie.getName().equals("userId")) {
                        userId = aCookie.getValue();
                    }
                }
            }
            MemberDTO menber = new MemberDTO();
            ArrayList<ScheduleDTO> memberSchedule = new ArrayList<>();
            if (userId != "") {
                menber = MemberDAO.getMemberByUserID(Integer.parseInt(userId));
                memberSchedule = ScheduleDAO.getScheduleByMemberID(menber.getId());
            } else {
                memberSchedule = null;
            }
            if (request.getParameterValues("checkBoxName") != null) {
                String[] checkBoxValues = request.getParameterValues("checkBoxName");
                //add schedule
                for (String object : checkBoxValues) {
                    try {
                        ScheduleDAO.updateMenberIDInSchedule(Integer.parseInt(object), menber.getId());
                    } catch (Exception e) {

                    }
                }
            }
            ArrayList<LocalDate> mondays = getAllMonday();
            LocalDate mondayOfWeek = getMondayOfWeek(currentDate);
            ArrayList<LocalDate> week = getWeek(mondayOfWeek);
            ArrayList<ScheduleDTO> mentorScheduleNotTeache = ScheduleDAO.getScheduleNotTeach();

            Date currentDayCheck = Date.valueOf(LocalDate.now());
            
            //Số buổi còn lại có thể đăng kí
            int remaining = ScheduleDAO.getNumOfRemaining(menber.getId());
            request.setAttribute("remaining", remaining);
            //Dung de check Date cua currentdate
            request.setAttribute("currentDay", currentDayCheck);
            request.setAttribute("mentorScheduleNotTeache", mentorScheduleNotTeache);
            request.setAttribute("scheduleOfMember", memberSchedule);
            request.setAttribute("currentMonday", mondayOfWeek);
            request.setAttribute("week", convertLocalDateToDate(week));
            request.setAttribute("mondays", mondays);
            request.getRequestDispatcher("member/regisScheduleOfMember.jsp").forward(request, response);
        }
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
