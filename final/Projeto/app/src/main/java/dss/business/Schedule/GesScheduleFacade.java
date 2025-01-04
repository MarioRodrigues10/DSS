package dss.business.Schedule;

import java.util.*;
import java.util.stream.Collectors;

import dss.business.Course.*;
import dss.business.User.Student;
import dss.data.CourseDAO;
import dss.data.StudentDAO;

public class GesScheduleFacade implements ISchedule {

    private CourseDAO courses;
    private StudentDAO students;

    /**
     * Constructor for GesScheduleFacade.
     * Initializes DAO objects for course and student management.
     */
    public GesScheduleFacade() {
        this.courses = new CourseDAO();
        this.students = new StudentDAO();
    }

    /**
     * Retrieves a list of student IDs with schedule conflicts in a course.
     *
     * @param idCourse ID of the course.
     * @return A list of student IDs with schedule conflicts.
     */
    public List<Integer> getStudentsWithScheduleConflicts(int idCourse) {
        List<Integer> studentsWithScheduleConflicts = new ArrayList<>();
        try {
            List<Student> studentss = this.students.getStudentsByCourse(idCourse);

            Map<Integer,Shift> shifts = this.courses.getShiftsByCourse(idCourse);

            for (Student student : studentss) {
                if (student.hasScheduleConflict(new ArrayList<>(shifts.values()))) {
                    studentsWithScheduleConflicts.add(student.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentsWithScheduleConflicts;
    }

    /**
     * Exports a student's schedule to a file.
     *
     * @param idStudent ID of the student.
     * @param filename Name of the file to export the schedule to.
     * @return True if the export is successful, otherwise false.
     */
    public boolean exportSchedule(int idStudent, String filename){
        try {
            Student student = students.getStudent(idStudent);
            if (student == null) {
                return false;
            }

            Map<UC, Map<Shift, List<TimeSlot>>> schedule = getStudentSchedule(idStudent, student.getCourse());

            return student.exportSchedule(schedule, filename);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a schedule for a course, assigning shifts to students.
     *
     * @param idCourse ID of the course.
     */
    public void generateSchedule (int idCourse) {
        try {

            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return;
            }

            List<Student> studentss = students.getStudentsByCourse(idCourse);
            if (studentss == null) {
                return;
            }

            Map<Integer,Shift> shifts = courses.getShiftsByCourse(idCourse);

            Map<Integer,List<Integer>> ucsByYear = courses.getUcsByYearForCourse(idCourse);

            Map<Integer,PreDefinedSchedule> schedules = courses.getPreDefinedScheduleByCourse(idCourse);

            course.generateSchedule(studentss, shifts, ucsByYear, schedules);

            courses.updateStudentsSchedules(studentss);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of students without an assigned schedule in a course.
     *
     * @param idCourse ID of the course.
     * @return A list of students without a schedule or null if an error occurs.
     */
    public List<Student> getStudentsWithoutSchedule (int idCourse) {
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return null;
            }
            return students.getStudentsByCourse(idCourse).stream().filter(student -> student.getSchedule().isEmpty()).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Imports a timetable for a course from a file.
     *
     * @param idCourse ID of the course.
     * @param year Year of the timetable.
     * @param path Path to the file containing the timetable.
     * @return True if the import is successful, otherwise false.
     */
    public boolean importTimeTable (int idCourse, int year, String path) {
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }

            List<Shift> shifts = course.importTimeTable(year, path);
            if (shifts == null) {
                return false;
            }

            for (Shift shift : shifts) {
                int capacity = -1;
                int type = 0;
                if(shift instanceof TheoreticalPractical) {
                    type = 1;
                    capacity = ((TheoreticalPractical) shift).getCapacity();
                }

                courses.addShiftToCourse(shift.getId(), shift.getCapacityRoom(), shift.getEnrolledCount(), type, capacity, shift.getUcId());

                for (TimeSlot timeSlot : shift.getTimeSlots()) {
                    courses.addTimeSlotToShift(timeSlot.getId(), timeSlot.getTimeStart(), timeSlot.getTimeEnd(), timeSlot.getWeekDay(), shift.getId());
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Posts the schedule for a course and notifies students.
     *
     * @param idCourse ID of the course.
     * @return True if the schedule is posted successfully, otherwise false.
     */
    public boolean postSchedule (int idCourse) {
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }
            course.postSchedule();
            courses.updateCourse(idCourse, course.getName(), course.isVisibilitySchedules());
            sendEmails(idCourse);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sends notification emails to students of a course.
     *
     * @param idCourse ID of the course.
     * @return True if emails are sent successfully, otherwise false.
     */
    public boolean sendEmails (int idCourse) {
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }

            List<Student> studentss = students.getStudentsByCourse(idCourse);
            if (studentss == null) {
                return false;
            }

            for (Student student : studentss) {
                student.sendEmail();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a student's schedule for a specific course.
     *
     * @param idStudent ID of the student.
     * @param idCourse ID of the course.
     * @return A map representing the student's schedule or null if an error occurs.
     */
    public Map<UC, Map<Shift,List<TimeSlot>>> getStudentSchedule (int idStudent, int idCourse) {
        try {
            Student student = students.getStudent(idStudent);
            if (student == null) {
                return null;
            }

            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return null;
            }

            Map<Integer, List<Integer>> schedule = student.getSchedule();

            Map<UC, Map<Shift,List<TimeSlot>>> studentSchedule = new HashMap<>();

            List<UC> allUCs = courses.getUCsByCourse(idCourse);
            List<Shift> allShifts = courses.getShiftsByCourse(idCourse).values().stream().toList();

            for (Map.Entry<Integer, List<Integer>> entry : schedule.entrySet()) {
                    UC uc = allUCs.stream().filter(u -> u.getId() == entry.getKey()).findFirst().orElse(null);
                    if (uc == null) {
                        continue;
                    }

                    Map<Shift, List<TimeSlot>> shifts = new HashMap<>();

                    for (Integer shiftId : entry.getValue()) {
                        Shift shift = allShifts.stream().filter(s -> s.getId() == shiftId).findFirst().orElse(null);
                        if (shift == null) {
                            continue;
                        }

                        List<TimeSlot> timeSlots = shift.getTimeSlots();
                        shifts.put(shift, timeSlots);
                    }

                    studentSchedule.put(uc, shifts);
            }
            return studentSchedule;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a schedule for a student.
     *
     * @param idStudent ID of the student.
     * @param schedule A map of UC IDs to shift IDs representing the student's schedule.
     * @return True if the schedule is registered successfully, otherwise false.
     */
    public boolean registerSchedule (int idStudent, Map<Integer, List<Integer>> schedule) {
        try {

            Student student = students.getStudent(idStudent);
            if (student == null) {
                return false;
            }

            students.removeScheduleFromStudent(idStudent);
            students.addScheduleToStudent(idStudent, schedule);

            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Imports predefined schedules for a course from a file.
     *
     * @param idCourse ID of the course.
     * @param path Path to the file containing the predefined schedules.
     * @return True if the schedules are imported successfully, otherwise false.
     */
    public boolean importSchedulesPreDefined(int idCourse, String path) {
        try {
            Course course = courses.getCourse(idCourse);
            if (course == null) {
                return false;
            }

            List<PreDefinedSchedule> schedules = course.importSchedulesPreDefined(path);
            if (schedules == null) {
                return false;
            }

            for (PreDefinedSchedule schedule : schedules) {
                courses.addPreDefinedScheduleToCourse(idCourse, schedule.getId(), schedule.getYear(), schedule.getNoConflicts(), schedule.getSchedule());
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
