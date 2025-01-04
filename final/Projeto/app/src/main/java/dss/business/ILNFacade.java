package dss.business;

import java.util.List;
import java.util.Map;

import dss.business.Course.Course;
import dss.business.Course.Shift;
import dss.business.Course.TimeSlot;
import dss.business.Course.UC;
import dss.business.User.Student;

public interface ILNFacade {

    public boolean registerPolicyOption(int idCourse, int idUC, String policyPreference);

    public boolean importStudents(String path, int idCourse) throws Exception;

    public boolean importUCs(String path, int idCourse);

    public boolean addStudent(int idStudent, int idCourse, List<Integer> ucs, int type);

    public boolean verifyIdentity(int idUser);

    public boolean verifyPassword(int idUser, String password);

    public Student getStudent(int idStudent) throws Exception;

    public int getCourseId(int idUser);

    public int getUserType(int idUser);

    public List<Integer> getStudentsWithScheduleConflicts(int idCourse);

    public boolean exportSchedule (int idStudent, String filename);

    public void generateSchedule (int idCourse);

    public List<Student> getStudentsWithoutSchedule (int idCourse);

    public boolean importTimeTable (int idCourse, int year, String path);

    public boolean postSchedule (int idCourse);

    public boolean sendEmails (int idCourse);

    public Map<UC, Map<Shift,List<TimeSlot>>> getStudentSchedule (int idStudent, int idCourse);

    public boolean registerSchedule (int idStudent, Map<Integer, List<Integer>> schedule);

    public boolean importSchedulesPreDefined (int idCourse, String path);

    public Course getCourse(int idCourse);
    
}
