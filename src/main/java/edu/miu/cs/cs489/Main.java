package edu.miu.cs.cs489;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.miu.cs.cs489.model.Employee;
import edu.miu.cs.cs489.model.PensionPlan;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Employee> employees = buildSampleEmployees();

        List<Employee> allEmployeesSorted = employees.stream()
                .sorted(Comparator.comparingDouble(Employee::getYearlySalary).reversed()
                        .thenComparing(Employee::getLastName))
                .toList();
        LocalDate testDate = LocalDate.now();
        List<Employee> quarterlyUpcomingEnrollees = getQuarterlyUpcomingEnrollees(employees, testDate);
        List<Employee> currentQuarterlyEnrollees = getCurrentQuarterlyEnrollees(employees, testDate);

        printAsJson("All Employees (with pension data if available)", allEmployeesSorted);
        printAsJson("Next Quarterly Upcoming Enrollees", quarterlyUpcomingEnrollees);
        printAsJson("Current Quarterly Enrollees", currentQuarterlyEnrollees);
    }

    private static List<Employee> buildSampleEmployees() {
        return List.of(
                new Employee(
                        1L,
                        "Daniel",
                        "Agar",
                        LocalDate.parse("2025-08-17"),
                        105_945.50,
                        null
                ),
                new Employee(
                        2L,
                        "Benard",
                        "Shaw",
                        LocalDate.parse("2025-02-03"),
                        197_750.00,
                        new PensionPlan("EX0089", LocalDate.parse("2026-02-03"), 450.00)
                ),
                new Employee(
                        3L,
                        "Carly",
                        "Jones",
                        LocalDate.parse("2024-05-16"),
                        842_000.75,
                        new PensionPlan("SM2307", LocalDate.parse("2025-05-17"), 1_555.50)
                ),
                new Employee(
                        4L,
                        "Wesley",
                        "Schneider",
                        LocalDate.parse("2025-04-30"),
                        174_500.00,
                        null
                ),
                new Employee(
                        5L,
                        "Anna",
                        "Wiltord",
                        LocalDate.parse("2025-09-15"),
                        185_750.00,
                        null
                ),
                new Employee(
                        6L,
                        "Yosef",
                        "Tesfalem",
                        LocalDate.parse("2025-07-31"),
                        100_000.00,
                        null
                ),
                new Employee(
                        7L,
                        "Johnny",
                        "Edwards",
                        LocalDate.parse("2025-07-09"),
                        95_500.00,
                        null
                )
        );
    }

    private static List<Employee> getQuarterlyUpcomingEnrollees(List<Employee> employees, LocalDate currentDate) {
        QuarterRange nextQuarter = getNextQuarterDateRange(currentDate);
        return employees.stream()
                .filter(employee -> employee.getPensionPlan() == null)
                .filter(employee -> {
                    LocalDate qualificationDate = employee.getEmploymentDate().plusYears(1);
                    return !qualificationDate.isBefore(nextQuarter.firstDay())
                            && !qualificationDate.isAfter(nextQuarter.lastDay());
                })
                .sorted(Comparator.comparing(Employee::getEmploymentDate).reversed()
                        .thenComparingDouble(Employee::getYearlySalary))
                .toList();
    }

    private static List<Employee> getCurrentQuarterlyEnrollees(List<Employee> employees, LocalDate currentDate) {
        QuarterRange currentQuarter = getCurrentQuarterDateRange(currentDate);
        return employees.stream()
                .filter(employee -> employee.getPensionPlan() == null)
                .filter(employee -> {
                    LocalDate qualificationDate = employee.getEmploymentDate().plusYears(1);
                    return !qualificationDate.isBefore(currentQuarter.firstDay())
                            && !qualificationDate.isAfter(currentQuarter.lastDay());
                })
                .sorted(Comparator.comparing(Employee::getEmploymentDate).reversed())
                .toList();
    }

    private static QuarterRange getCurrentQuarterDateRange(LocalDate date) {
        int currentQuarter = ((date.getMonthValue() - 1) / 3) + 1;
        Month startMonth = switch (currentQuarter) {
            case 1 -> Month.JANUARY;
            case 2 -> Month.APRIL;
            case 3 -> Month.JULY;
            default -> Month.OCTOBER;
        };
        LocalDate firstDay = LocalDate.of(date.getYear(), startMonth, 1);
        LocalDate lastDay = firstDay.plusMonths(3).minusDays(1);
        return new QuarterRange(firstDay, lastDay);
    }

    private static QuarterRange getNextQuarterDateRange(LocalDate date) {
        int currentQuarter = ((date.getMonthValue() - 1) / 3) + 1;
        int nextQuarter = currentQuarter == 4 ? 1 : currentQuarter + 1;
        int year = currentQuarter == 4 ? date.getYear() + 1 : date.getYear();

        Month startMonth = switch (nextQuarter) {
            case 1 -> Month.JANUARY;
            case 2 -> Month.APRIL;
            case 3 -> Month.JULY;
            default -> Month.OCTOBER;
        };

        LocalDate firstDay = LocalDate.of(year, startMonth, 1);
        LocalDate lastDay = firstDay.plusMonths(3).minusDays(1);
        return new QuarterRange(firstDay, lastDay);
    }

    private static void printAsJson(String title, List<Employee> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            System.out.println("===== " + title + " =====");
            System.out.println(mapper.writeValueAsString(data));
            System.out.println();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render JSON output.", e);
        }
    }

    private record QuarterRange(LocalDate firstDay, LocalDate lastDay) {
    }
}