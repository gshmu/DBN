package com.dci.intellij.dbn.common.file;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.thread.Write;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.file.FileTypeService.COMPONENT_NAME;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@State(
    name = COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class FileTypeService extends ApplicationComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Application.FileTypeService";

    private final Map<String, String> originalFileAssociations = new HashMap<>();
    private boolean fileTypesClaimed;

    public FileTypeService() {
        super(COMPONENT_NAME);
    }

    public static FileTypeService getInstance() {
        return applicationService(FileTypeService.class);
    }

    public final void associateExtension(@NotNull FileType fileType, @NotNull String extension) {
        Write.run(() -> {
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            FileType currentFileType = fileTypeManager.getFileTypeByExtension(extension);
            if (currentFileType == fileType) return;

            if (!DBLanguageFileType.matches(currentFileType)) {
                originalFileAssociations.put(extension, currentFileType.getName());
            }

            fileTypeManager.removeAssociatedExtension(currentFileType, extension);
            fileTypeManager.associateExtension(fileType, extension);
        });
    }

    public void restoreFileTypeAssociations() {
        Write.run(() -> {
            fileTypesClaimed = false;
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            for (String fileExtension : originalFileAssociations.keySet()) {
                String fileTypeName = originalFileAssociations.get(fileExtension);
                FileType fileType = getFileType(fileTypeName);
                if (fileType == null) continue;

                fileTypeManager.associateExtension(fileType, fileExtension);
            }


            restoreFileTypes();
        });
    }

    private void claimFileTypes() {
        if (fileTypesClaimed) return;

        // do not overwrite file associations once claimed and reverted by user
        associateExtension(SQLFileType.INSTANCE, "sql");
        associateExtension(SQLFileType.INSTANCE, "ddl");
        fileTypesClaimed = true;
    }

    private void restoreFileTypes() {
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType originalSqlFileType = getFileType("SQL");
        if (originalSqlFileType != null) {
            fileTypeManager.associateExtension(originalSqlFileType, "sql");
            fileTypeManager.associateExtension(originalSqlFileType, "ddl");
        }
    }


    @Override
    public Element getComponentState() {
        Element stateElement = new Element("state");
        Element mappingsElement = new Element("original-file-types");
        stateElement.addContent(mappingsElement);

        for (String fileExtension : originalFileAssociations.keySet()) {
            String fileType = originalFileAssociations.get(fileExtension);
            Element mappingElement = new Element("mapping");
            mappingElement.setAttribute("file-extension", fileExtension);
            mappingElement.setAttribute("file-type", fileType);
            mappingsElement.addContent(mappingElement);
        }
        setBoolean(stateElement, "file-types-claimed", fileTypesClaimed);
        return stateElement;
    }

    @Override
    public void loadComponentState(@NotNull Element stateElement) {
        Element mappingsElement = stateElement.getChild("original-file-types");
        if (mappingsElement == null) return;

        for (Element mappingElement : mappingsElement.getChildren()) {
            String fileExtension = stringAttribute(mappingElement, "file-extension");
            String fileType = stringAttribute(mappingElement, "file-type");
            originalFileAssociations.put(fileExtension, fileType);
        }
        fileTypesClaimed = getBoolean(stateElement, "file-types-claimed", false);

        claimFileTypes();
    }

    @Nullable
    private static FileType getFileType(String fileTypeName) {
        FileType[] registeredFileTypes = FileTypeManager.getInstance().getRegisteredFileTypes();
        return Arrays
                .stream(registeredFileTypes)
                .filter(ft -> Objects.equals(ft.getName(), fileTypeName))
                .findFirst()
                .orElse(null);
    }

    public List<FileNameMatcher> getAssociations(DBLanguageFileType fileType) {
        return FileTypeManager.getInstance().getAssociations(fileType);
    }
}
