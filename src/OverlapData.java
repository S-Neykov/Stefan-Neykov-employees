class OverlapData {
    Employee employee1;
    Employee employee2;
    int projectId;
    int overlapDays;

    public OverlapData(Employee employee1, Employee employee2, int projectId, int overlapDays) {
        this.employee1 = employee1;
        this.employee2 = employee2;
        this.projectId = projectId;
        this.overlapDays = overlapDays;
    }
}