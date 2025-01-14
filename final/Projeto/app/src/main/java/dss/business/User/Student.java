package dss.business.User;

import java.util.*;

import dss.business.Course.Shift;
import dss.business.Course.TimeSlot;
import dss.business.Course.UC;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class Student {

    private int id;
    private String password;
    private int course;
    private Map<Integer, List<Integer>> schedule; // Map<UC, List<Shift>>
    private List<Integer> ucs;

    public Student(int id, String password, int idCourse){
        this.id = id;
        this.password = password;
        this.course = idCourse;
    }

    public Student(int id, String password, int idCourse, List<Integer> ucs){
        this.id = id;
        this.password = password;
        this.course = idCourse;
        this.ucs = ucs;
    }

    public Student(int id, String password, int idCourse, List<Integer> ucs, Map<Integer, List<Integer>> schedule){
        this.id = id;
        this.password = password;
        this.course = idCourse;
        this.ucs = ucs;
        this.schedule = schedule;
    }

    public int getId() {
        return id;
    }

    public int getCourse() {
        return course;
    }

    public void addUC(UC uc) {
        ucs.add(uc.getId());
    }

    public void removeUC(UC uc) {
        ucs.remove(uc.getId());
    }

    public List<Integer> getUCs() {
        return ucs;
    }

    public void setUCs(List<Integer> ucs) {
        this.ucs = ucs;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    public int getType() {
        return 0;
    }

    public Map<Integer, List<Integer>> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<Integer, List<Integer>> schedule) {
        this.schedule = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : schedule.entrySet()) {
            this.schedule.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    /**
     * Generates a random 8-character password consisting of uppercase letters, lowercase letters, and digits.
     *
     * @return The generated random password.
     */
    public static String generateRandomPassword() {
        String password = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password += characters.charAt(random.nextInt(characters.length()));
        }

        return password;
    }

    /**
     * Removes the student's schedule, clearing all assigned shifts.
     */
    public void removeSchedule() {
        this.schedule = new HashMap<>();
   }

    /**
     * Retrieves the email address associated with the student.
     *
     * @return The student's email address.
     */
    public String getEmail() {
        return "a" + id + "@alunos.uminho.pt";
    }

    /**
     * Checks if the student has any schedule assigned.
     *
     * @return True if the student has a schedule, otherwise false.
     */
    public boolean hasSchedule() {
        return !schedule.isEmpty();
    }

    /**
     * Checks if the student's schedule has any conflicts with the given list of shifts.
     *
     * @param shifts A list of shifts to check for conflicts.
     * @return True if there is a schedule conflict, otherwise false.
     */
    public boolean hasScheduleConflict(List<Shift> shifts) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : this.schedule.entrySet()) {
            for (int shiftId : entry.getValue()) {
                Shift shift = this.findShiftById(shifts, shiftId);
                if (shift != null) {
                    for (TimeSlot timeSlot : shift.getTimeSlots()) {
                        if (timeSlots.contains(timeSlot)) {
                            return true;
                        }
                        timeSlots.add(timeSlot);
                    }
                }
            }
        }

        return timeSlots.stream().anyMatch(timeSlot -> 
                    timeSlots.stream().filter(otherTimeSlot -> !timeSlot.equals(otherTimeSlot))
                    .anyMatch(timeSlot::hasConflict));
    }

    /**
     * Finds a shift by its ID from a list of shifts.
     *
     * @param shifts The list of shifts to search through.
     * @param shiftId The ID of the shift to find.
     * @return The matching Shift object, or null if not found.
     */
    private Shift findShiftById(List<Shift> shifts, int shiftId) {
        for (Shift shift : shifts) {
            if (shift.getId() == shiftId) {
                return shift;
            }
        }
        return null;
    }

    /**
     * Sends an email notification to the student about their schedule.
     */
    public void sendEmail() {
        String title = "Horário do Curso publicado";
        String body = this.buildEmailContent();

        String to = this.getEmail();

        this.sendEmailAux(title, body, to);
    }

    /**
     * Helper method for sending an email using the specified parameters.
     *
     * @param title The email subject.
     * @param body The email body.
     * @param to The recipient's email address.
     */
    public void sendEmailAux(String title, String body, String to) {
        // Email de onde quer enviar
        String from = "xxxx@gmail.com";
        String password = "xxxx";
        String host = "smtp.gmail.com";
        int port = 587;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);  
        props.setProperty("mail.smtp.host", host);  

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress("destinatario@dominio.com"));
            message.setSubject(title);
            message.setText(body);
        
            //Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the email content for schedule notifications.
     *
     * @return A formatted string containing the email content.
     */
    private String buildEmailContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bom dia, ").append(this.getEmail()).append("!\n\n");
        sb.append("O horário do seu curso foi publicado.\n");
        sb.append("Encontram-se, de seguida, os seus dados de log-in:\n");
        sb.append("ID: ").append(this.getId()).append("\n");
        sb.append("Palavra-passe: ").append(this.getPassword()).append("\n\n");
        sb.append("Na plataforma encontra-se o seu horário.\n");
        sb.append("Atentamente,\n");
        sb.append("Diretor de Curso");
    
        return sb.toString();
    }

    @Override
    public Student clone() {
        try {
            Student cloned = (Student) super.clone();
            cloned.schedule = new HashMap<>(this.schedule);
            for (Map.Entry<Integer, List<Integer>> entry : this.schedule.entrySet()) {
                cloned.schedule.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            cloned.ucs = new ArrayList<>(this.ucs);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Determines the years associated with the student's UCs.
     *
     * @param ucsByYear A map of year IDs to UC lists.
     * @return A list of years containing the student's UCs.
     */
    public List<Integer> getYearsWithUCs(Map<Integer, List<Integer>> ucsByYear) {
        List<Integer> years = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : ucsByYear.entrySet()) {
            int year = entry.getKey();
            List<Integer> ucsInYear = entry.getValue();
    
            for (int ucId : this.getUCs()) {
                if (ucsInYear.contains(ucId)) {
                    years.add(year);
                    break;
                }
            }
        }
        return years;
    }

    /**
     * Exports the student's schedule to a file.
     *
     * @param schedule The schedule data to export.
     * @param filename The name of the file to save the schedule.
     * @return True if the schedule is successfully exported, otherwise false.
     */
    public boolean exportSchedule(Map<UC, Map<Shift, List<TimeSlot>>> schedule, String filename){
        try {
            FileWriter fileWriter = new FileWriter(filename);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (Map.Entry<UC, Map<Shift, List<TimeSlot>>> ucEntry : schedule.entrySet()) {
                UC uc = ucEntry.getKey();
                Map<Shift, List<TimeSlot>> shiftMap = ucEntry.getValue();

                bufferedWriter.write("UC: " + uc.getName());
                bufferedWriter.newLine();

                for (Map.Entry<Shift, List<TimeSlot>> shiftEntry : shiftMap.entrySet()) {
                    Shift shift = shiftEntry.getKey();
                    List<TimeSlot> timeSlots = shiftEntry.getValue();

                    bufferedWriter.write("Shift: " + shift.getId());
                    bufferedWriter.newLine();

                    for (TimeSlot timeSlot : timeSlots) {
                        bufferedWriter.write("Time Slot: " + timeSlot.getTimeStart() + " - " + timeSlot.getTimeEnd());
                        bufferedWriter.newLine();
                    }
                }

                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            fileWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}