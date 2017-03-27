package pl.kaszaq.cycletime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import pl.kaszaq.agile.AgileClient;
import pl.kaszaq.agile.AgileProject;
import pl.kaszaq.agile.Issue;
import pl.kaszaq.agile.IssuePredicates;
import pl.kaszaq.agile.IssueStatusTransition;
import pl.kaszaq.agile.IssueStatusTransitionPredicates;
import pl.kaszaq.agile.IssueWrapper;

/**
 *
 * @author michal.kasza
 */
@AllArgsConstructor
public class CycleTimeComputer {

    private final AgileProject agileProject;
    private final String finalStatus;// = "Closed";

    public double calulcateCycleTimeOfClosedIssues(Predicate<Issue> filters, LocalDate toDate, LocalDate fromDate,
            String... statuses) {
        LongSummaryStatistics cycleTimeStatistics = agileProject.getAllIssues()
                .stream()
                .filter(filters.and(IssuePredicates.hasStatusTransitionsThat(IssueStatusTransitionPredicates.createdAfter(fromDate),
                        IssueStatusTransitionPredicates.createdAfter(toDate),
                        IssueStatusTransitionPredicates.to(finalStatus)
                )))
                //.peek(i -> System.out.println(i.getPrettyName()))
                .mapToLong(i -> new IssueWrapper(i).getDurationInStatuses(statuses).getSeconds())
                //                .peek(n -> System.out.println(NumberUtils.prettyPrint((double) n / 3600.0)))
                .summaryStatistics();
        return cycleTimeStatistics.getAverage() / 3600;
    }

    public double calulcateCycleTimeOfStories(Predicate<Issue> filters, LocalDate toDate, LocalDate fromDate,
            String... statuses) {
        List<Duration> durations = new ArrayList<>();
        for (Issue issue : agileProject.getAllIssues()) {
            if (IssuePredicates.hasSubtasks().test(issue)) {
                Set<Issue> subtasks = getSubtasks(issue);
                if (allSubtasksClosed(subtasks)) {
                    List<IssueStatusTransition> transitions = subtasks.stream()
                            .flatMap(i -> i.getIssueStatusTransitions().stream())
                            .filter(IssueStatusTransitionPredicates.to(statuses))
                            .sorted()
                            .collect(Collectors.toList());
                    Duration duration;
                    if (!transitions.isEmpty()) {
                        duration = Duration.between(transitions.get(0).getDate(), transitions.get(transitions.size() - 1).getDate());
                    } else {
                        duration = Duration.ZERO;
                    }
                    durations.add(duration);
                }
            }
        }
        LongSummaryStatistics cycleTimeStatistics = durations.stream().mapToLong(d -> d.getSeconds())
                //                .peek(n -> System.out.println(NumberUtils.prettyPrint((double) n / 3600.0)))
                .summaryStatistics();
        return cycleTimeStatistics.getAverage() / 3600;
    }

    private boolean allSubtasksClosed(Set<Issue> subtasks) {
        return !subtasks.stream().anyMatch(s -> !s.getStatus().equals(finalStatus));
    }

    private Set<Issue> getSubtasks(Issue i) {
        //TODO this could be moved to some helper method...
        return i.getSubtaskKeys().stream().map(id -> agileProject.getIssue(id)).collect(Collectors.toSet());
    }

}
