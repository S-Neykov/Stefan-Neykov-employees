import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        String filename = chooseFile();

        Map<Integer, List<Employee>> teamsWithWorkers = getMapOfTeams(filename);
        List<OverlapData> overlapData = getOverlapData(teamsWithWorkers);
        // Get the employees which have worked the longest together
        OverlapData longestOverlapData = overlapData.stream().max(Comparator.comparingInt(e -> e.overlapDays)).orElse(null);
        if (longestOverlapData == null) {
            System.out.println("No 2 employees have worked together.");
            return;
        }

        System.out.println("\nEmployees which have worked the longest together are with IDs " + longestOverlapData.employee1.employeeId + " and " + longestOverlapData.employee2.employeeId + " with " + longestOverlapData.overlapDays + " days.");
        System.out.println("Common projects for the longest working pair:");
        System.out.println("Employee ID #1, Employee ID #2, Project ID, Days Worked");

        // find and print all common projects (and the overlap data) for the pair
        List<OverlapData> commonProjectsOverlapData = overlapData.stream()
                .filter(e -> (e.employee1.equalsById(longestOverlapData.employee1) && e.employee2.equalsById(longestOverlapData.employee2))
                        || (e.employee1.equalsById(longestOverlapData.employee2) && e.employee2.equalsById(longestOverlapData.employee1)))
                .toList();
        commonProjectsOverlapData.forEach(e -> System.out.println(e.employee1.employeeId + ", " + e.employee2.employeeId + ", " + e.projectId + ", " + e.overlapDays));
    }

    private static List<String> getCsvFilesInCurrentDirectory() throws IOException {
        List<String> csvFiles = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(new File("").getAbsolutePath() + "/src/"), "*.csv")) {
            for (Path path : directoryStream) {
                csvFiles.add(path.getFileName().toString());
            }
        }
        return csvFiles;
    }

    /**
     * Choose a CSV file from the directory of the current Main class. Only CSV files will be listed.
     * This is a simple UI for the user to choose a file. For a more complex application, a GUI would be more appropriate.
     * @return String Name of the chosen file
     */
    private static String chooseFile() throws IOException {
        List<String> csvFiles = getCsvFilesInCurrentDirectory();
        System.out.println("Choose a file to load (enter the file number):");
        for (int i = 0; i < csvFiles.size(); i++) {
            System.out.println((i + 1) + ". " + csvFiles.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        String chosenFile = csvFiles.get(choice - 1);

        System.out.println("You chose: " + chosenFile);
        return chosenFile;
    }


    /**
     * Get a map of teams with their workers from a CSV file. The key is the project ID and the value is a list of
     * employees which have worked there at any point.
     * @param filename Name of the source file
     * @return Map<Integer, List<Employee>>
     */
    private static Map<Integer, List<Employee>> getMapOfTeams(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get("", "src", filename))) {
            return lines
                    .map(line -> getWorkerFromCsvLine(line))
                    .collect(Collectors.groupingBy(employee -> employee.projectId));
        }
    }

    /**
     * Map each read line from the data file to an Employee object. It handles all common date formats.
     * @param line Line read from the CSV file
     * @return Employee object
     */
    private static Employee getWorkerFromCsvLine(String line) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("[yyyy.MM.dd][yyyyy.MMMMM.dd][EEE, d MMM yyyy][yyyy-MM-dd][yyyy/MM/dd][yyyyMMdd]")
                .toFormatter();
        String[] values = line.split(",");
        int employeeId = Integer.parseInt(values[0]);
        int projectId = Integer.parseInt(values[1]);
        LocalDate dateFrom = formatter.parse(values[2], LocalDate::from);
        LocalDate dateTo = values[3].equals("null") ? LocalDate.now() : formatter.parse(values[3], LocalDate::from);
        return new Employee(employeeId, projectId, dateFrom, dateTo);
    }

    /**
     * Get a list of data how many days and for what project each pair of employees have worked together. All cases of
     * employees working together are returned.
     * @param teamsWithEmployees
     * @return List<OverlapData> All pairs of employees which have worked together
     */
    private static List<OverlapData> getOverlapData(Map<Integer, List<Employee>> teamsWithEmployees) {
        return teamsWithEmployees.values().parallelStream().filter(workers -> workers.size() > 1)
                .map(employeeList -> {
                    List<OverlapData> overlapDataList = new ArrayList<>();

                    //Queue is used to iterate over the list of workers in a more efficient way
                    Queue<Employee> queue = new LinkedList<>(employeeList);
                    while (queue.size() > 1) {
                        Employee employee1 = queue.poll();
                        for (Employee employee2 : queue) {
                            if (employee1.dateFrom.isBefore(employee2.dateTo) && employee2.dateFrom.isBefore(employee1.dateTo)) {
                                int overlapDays = getOverlapDays(employee1, employee2);
                                OverlapData overlapData = new OverlapData(employee1, employee2, employee1.projectId, overlapDays);
                                overlapDataList.add(overlapData);
                            }
                        }
                    }
                    return overlapDataList;
                }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static int getOverlapDays(Employee employee1, Employee employee2) {
        LocalDate overlapStart = employee1.dateFrom.isAfter(employee2.dateFrom) ? employee1.dateFrom : employee2.dateFrom;
        LocalDate overlapEnd = employee1.dateTo.isBefore(employee2.dateTo) ? employee1.dateTo : employee2.dateTo;
        return (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd);
    }
}
