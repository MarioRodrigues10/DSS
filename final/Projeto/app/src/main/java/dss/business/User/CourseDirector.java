package dss.business.User;

public class CourseDirector {

    private int id;
    private String password;
    private int courseId;

    public CourseDirector(int id, String password, int courseId) {
        this.id = id;
        this.password = password;
        this.courseId = courseId;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    /**
     * Verifies if the password is correct.
     * @param password Password to be verified.
     * @return True if the password is correct, otherwise false.
     */
    public boolean verifyPassword(String password) {
        return String.valueOf(this.password).equals(password);
    }
}