package dss.business.Course;

import java.util.*;

import dss.business.User.Student;
import dss.data.CourseDAO;
import dss.data.UCDAO;

public class GesCourseFacade implements IGesCourse {

    private CourseDAO courses;
    private UCDAO ucs;

    /**
     * Constructor for GesCourseFacade.
     * Initializes DAO objects for course and UC management.
     */
    public GesCourseFacade() {
        this.courses = new CourseDAO();
        this.ucs = new UCDAO();
    }

    /**
     * Registers a policy preference for a UC in a course.
     *
     * @param idCourse ID of the course.
     * @param idUC ID of the UC.
     * @param policyPreference The policy preference to be set.
     * @return True if the policy is registered successfully, otherwise false.
     */
    public boolean registerPolicyOption(int idCourse, int idUC, String policyPreference){
        try {
            UC uc = ucs.getUC(idUC);
            if (uc == null) {
                return false;
            }
            uc.setPolicyPreference(policyPreference);
            return ucs.updateUC(uc.getId(), uc.getName(), uc.getYear(), uc.getSemester(), uc.getPolicyPreference(), uc.getIdCourse());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Imports students for a specific course from a file.
     *
     * @param path Path to the file containing student data.
     * @param idCourse ID of the course to which students are being imported.
     * @return True if the students are imported successfully, otherwise false.
     * @throws Exception if an error occurs during the import process.
     */
    public boolean importStudents(String path, int idCourse) throws Exception{
        try{
            List<Student> studentS;
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }

            studentS = course.importStudents(path);

            if (studentS != null) {
                for (Student student : studentS) {
                    boolean flag = courses.addStudent(student);
                    if (!flag) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Imports UCs for a specific course from a file.
     *
     * @param path Path to the file containing UC data.
     * @param idCourse ID of the course to which UCs are being imported.
     * @return True if the UCs are imported successfully, otherwise false.
     */
    public boolean importUCs(String path, int idCourse){
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }

            List<UC> ucS = course.importUCs(path);

            if (ucS != null) {
                for (UC uc : ucS) {
                    ucs.addUC(uc.getId(), uc.getName(), uc.getYear(), uc.getSemester(), uc.getPolicyPreference(), uc.getIdCourse());
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a student to a course.
     *
     * @param idStudent ID of the student.
     * @param idCourse ID of the course.
     * @param ucs List of UC IDs the student is enrolled in.
     * @param type Type of the student (e.g., regular, athlete, employed).
     * @return True if the student is added successfully, otherwise false.
     */
    public boolean addStudent(int idStudent, int idCourse, List<Integer> ucs, int type){
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }
            return courses.addStudent(course.addStudent(idStudent, idCourse, ucs, type));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param idCourse ID of the course to retrieve.
     * @return The Course object if found, otherwise null.
     */
    public Course getCourse(int idCourse){
        try {
            return courses.getCourse(idCourse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
