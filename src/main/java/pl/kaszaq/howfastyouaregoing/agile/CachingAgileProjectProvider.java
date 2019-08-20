/*
 * Copyright 2017 Michał Kasza <kaszaq@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.kaszaq.howfastyouaregoing.agile;

import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import pl.kaszaq.howfastyouaregoing.Config;
import static pl.kaszaq.howfastyouaregoing.Config.OBJECT_MAPPER;
import pl.kaszaq.howfastyouaregoing.agile.jira.AgileProjectDataReader;
import pl.kaszaq.howfastyouaregoing.agile.pojo.AgileProjectData;
import pl.kaszaq.howfastyouaregoing.storage.FileStorage;

@Slf4j
public class CachingAgileProjectProvider implements AgileProjectProvider {

    private static final ZonedDateTime INITIAL_DATE = ZonedDateTime.of(1970, Month.JANUARY.getValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault());

    private final File cacheDirectory;
    private final Set<String> customFieldsNames;
    private final AgileProjectDataReader agileProjectDataReader;
    private final boolean cacheOnly;
    private final FileStorage fileStorage;

    public CachingAgileProjectProvider(
            File cacheDirectory,
            Set<String> customFieldsNames,
            AgileProjectDataReader agileProjectDataReader,
            Boolean cacheOnly,
            FileStorage fileStorage) {
        this.cacheDirectory = cacheDirectory;
        this.customFieldsNames = new HashSet<>(customFieldsNames);
        this.agileProjectDataReader = agileProjectDataReader;
        this.cacheOnly = cacheOnly;
        this.fileStorage = fileStorage;
    }

    @Override
    public Optional<AgileProject> loadProject(String projectId, AgileProjectConfiguration configuration) {
        return loadProject(projectId, configuration, null);
    }

    @Override
    public Optional<AgileProject> loadProject(String projectId, AgileProjectConfiguration configuration, AgileProjectDataObserver observer) {
        try {
            Optional<AgileProjectData> projectDataOptional = loadProjectFromFile(projectId);
            AgileProjectData projectData = projectDataOptional.orElse(createNewEmptyProject(projectId));
            if (!projectData.getCustomFieldsNames().containsAll(customFieldsNames)) {
                LOG.info("Noticied different setup of custom fields. Forcing to recreate project.");
                projectData = createNewEmptyProject(projectId);
            }

            projectData = agileProjectDataReader.updateProject(projectData, (project, progress) -> {
                if (observer != null) {
                    observer.updated(project, progress);
                }
                saveProjectToFile(project);
            }, cacheOnly);

            return Optional.of(new AgileProjectFactory().createAgileProject(projectData, configuration.getIssueStatusMapping()));
        } catch (Throwable ex) {
            LOG.warn("Problem while reading project data of project {}" , projectId, ex);
            return Optional.empty();
        }
    }

    private AgileProjectData createNewEmptyProject(String projectId) {
        return new AgileProjectData(projectId, INITIAL_DATE, INITIAL_DATE, new HashMap<>(), customFieldsNames, null);
    }

    private Optional<AgileProjectData> loadProjectFromFile(String projectId) throws IOException {
        File projectFile = getProjectFile(projectId);
        if (projectFile.exists()) {
            try {
                return Optional.of(OBJECT_MAPPER.readValue(fileStorage.loadFile(projectFile), AgileProjectData.class));
            } catch (Throwable ex) {
                LOG.warn("Problem while reading project {} from file" + projectId, ex);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private File getProjectFile(String projectId) {
        return new File(cacheDirectory, projectId + ".json");
    }

    private void saveProjectToFile(AgileProjectData project) {
        try {
            File projectFile = getProjectFile(project.getProjectId());
            fileStorage.storeFile(projectFile, OBJECT_MAPPER.writeValueAsString(project));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
