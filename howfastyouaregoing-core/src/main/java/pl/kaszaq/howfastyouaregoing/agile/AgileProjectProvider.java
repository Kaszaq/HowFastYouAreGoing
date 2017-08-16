package pl.kaszaq.howfastyouaregoing.agile;

import java.util.Optional;

/**
 *
 * @author michal.kasza
 */
public interface AgileProjectProvider {

    Optional<AgileProject> loadProject(String projectId, AgileProjectConfiguration configuration);
    
}
