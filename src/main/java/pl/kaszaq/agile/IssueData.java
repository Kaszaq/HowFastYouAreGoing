package pl.kaszaq.agile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class IssueData {
    private final String key;
    private final String creator;
    private final String type;
    private final String resolution;
    private final String status;
    private final String summary;
    private final String description;
    private final ZonedDateTime created;
    private final ZonedDateTime updated;
    private final boolean subtask;
    private final String parentIssueKey;
    private final List<String> subtaskKeys;
    private final List<String> linkedIssuesKeys;
    private final List<String> labels;
    private final List<String> components;
    private final TreeSet<IssueStatusTransition> issueStatusTransitions;
    private final TreeSet<IssueBlockedTransition> issueBlockedTransitions;
    // TODO: this field should not be a part of issue but rather additional, in some custom fields map.
    private final String timesheetsCode;

    @JsonIgnore
    public String getPrettyName(){
        return "[" + getKey() +"] "+ getSummary();
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IssueData other = (IssueData) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }
    
}
