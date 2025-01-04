package dss.business.User;


import dss.data.StudentDAO;
import dss.data.CourseDirectorDAO;

public class GesUserFacade implements IGesUser {
    private final StudentDAO students;
    private final CourseDirectorDAO directors;

    /**
     * Constructor for GesUserFacade.
     * Initializes DAO objects for managing students and course directors.
     */
    public GesUserFacade() {
        this.students = new StudentDAO();
        this.directors = new CourseDirectorDAO();
    }

    /**
     * Verifies the identity of a user by checking if they exist in the system.
     *
     * @param idUser The ID of the user.
     * @return True if the user exists, otherwise false.
     */
    public boolean verifyIdentity(int idUser){
        try {
            return students.getStudent(idUser) != null || directors.getCourseDirector(idUser) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines the type of user based on their ID.
     *
     * @param idUser The ID of the user.
     * @return 1 if the user is a student, 2 if the user is a course director, or -1 if the user type cannot be determined.
     */
    public int getUserType(int idUser){
        try {
            if (students.getStudent(idUser) != null) {
                return 1;
            }

            if (directors.getCourseDirector(idUser) != null) {
                return 2;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    /**
     * Verifies the password of a user.
     *
     * @param idUser The ID of the user.
     * @param password The password to verify.
     * @return True if the password is correct, otherwise false.
     */
    public boolean verifyPassword(int idUser, String password){
        try {
            Student student = students.getStudent(idUser);
            if (student != null) {
                return student.verifyPassword(password);
            }

            CourseDirector director = directors.getCourseDirector(idUser);
            if (director != null) {
                return director.verifyPassword(password);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Retrieves a student object by their ID.
     *
     * @param idStudent The ID of the student.
     * @return The Student object corresponding to the given ID.
     * @throws Exception If the student does not exist or another error occurs.
     */
    public Student getStudent(int idStudent) throws Exception{
        return students.getStudent(idStudent);
    }

    /**
     * Retrieves the course ID associated with a user.
     *
     * @param idUser The ID of the user.
     * @return The course ID associated with the user, or -1 if the course ID cannot be determined.
     */
    public int getCourseId(int idUser){
        try {
            Student student = students.getStudent(idUser);
            if (student != null) {
                return student.getCourse();
            }

            CourseDirector director = directors.getCourseDirector(idUser);
            if (director != null) {
                return director.getCourseId();
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
}