import java.time.LocalDate;

class Employee {
    int employeeId;
    int projectId;
    LocalDate dateFrom;
    LocalDate dateTo;

    public Employee(int employeeId, int projectId, LocalDate dateFrom, LocalDate dateTo) {
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public boolean equalsById(Employee employee) {
        return this.employeeId == employee.employeeId;
    }
}