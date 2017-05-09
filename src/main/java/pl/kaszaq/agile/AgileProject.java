package pl.kaszaq.agile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AgileProject {

    private final Map<String, IssueData> data;
    private final String id;

    AgileProject(String id, Map<String, IssueData> data) {
        this.id=id;
        this.data = new HashMap<>(data);
    }

    public Collection<IssueData> getAllIssues() {
        return new HashMap<>(data).values();
    }

    public IssueData getIssue(String issueId) {
        return data.get(issueId);
    }

    public boolean contains(IssueData issue) {
        return data.containsKey(issue.getKey());
    }

    public boolean contains(String issueKey) {
        return data.containsKey(issueKey);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final AgileProject other = (AgileProject) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    

}
