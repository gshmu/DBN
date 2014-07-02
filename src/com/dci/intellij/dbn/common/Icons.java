package com.dci.intellij.dbn.common;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.RowIcon;
import gnu.trove.THashMap;

import javax.swing.Icon;
import java.util.Map;

public class Icons {
    private static final Map<String, Icon> REGISTERED_ICONS = new THashMap<String, Icon>();

    public static final Icon DBN_SPLASH = load("/img/DBN.png");
    public static final Icon DONATE = load("/img/Donate.png");
    public static final Icon DONATE_DISABLED = load("/img/DonateDisabled.png");

    public static final Icon COMMON_INFO = load("/img/v1/common/Info.png");
    public static final Icon COMMON_INFO_DISABLED = load("/img/v1/common/InfoDisabled.png");
    public static final Icon COMMON_WARNING = load("/img/v1/common/WarningTriangle.png");
    public static final Icon COMMON_RIGHT = load("/img/v1/common/SplitRight.png");
    public static final Icon COMMON_UP = load("/img/v1/common/SplitUp.png");
    public static final Icon COMMON_ARROW_DOWN = load("/img/v1/ComboBoxArrow.png");


    public static final Icon ACTION_COPY = load("/img/v1/action/Copy.png");
    public static final Icon ACTION_SORT_ALPHA = load("/img/v1/action/SortAlphabetically.png");
    public static final Icon ACTION_SORT_NUMERIC = load("/img/v1/action/SortNumerically.png");
    public static final Icon ACTION_SORT_ASC = load("/img/v1/action/SortAscending.png");
    public static final Icon ACTION_SORT_DESC = load("/img/v1/action/SortDescending.png");
    public static final Icon ACTION_ADD = load("/img/v1/action/Add.png");
    public static final Icon ACTION_ADD_SPECIAL = load("/img/v1/action/AddSpecial.png");
    public static final Icon ACTION_REMOVE = load("/img/v1/action/Remove.png");
    public static final Icon ACTION_MOVE_UP = load("/img/v1/action/MoveUp.png");
    public static final Icon ACTION_MOVE_DOWN = load("/img/v1/action/MoveDown.png");
    public static final Icon ACTION_EDIT = load("/img/v1/action/EditSource.png");
    public static final Icon ACTION_SETTINGS = load("/img/v1/action/Properties.png");
    public static final Icon ACTION_COLLAPSE_ALL = load("/img/v1/action/CollapseAll.png");
    public static final Icon ACTION_EXPAND_ALL = load("/img/v1/action/ExpandAll.png");
    public static final Icon ACTION_GROUP = load("/img/v1/action/Group.png");
    public static final Icon ACTION_DELETE = load("/img/v1/action/Delete.png");
    public static final Icon ACTION_CLOSE = ACTION_DELETE;
    public static final Icon ACTION_UP_DOWN = load("/img/v1/action/UpDown.png");
    public static final Icon ACTION_REFRESH = load("/img/v1/action/Synchronize.png");
    public static final Icon ACTION_FIND = load("/img/v1/action/Find.png");
    public static final Icon ACTION_WRAP_TEXT = load("/img/v1/action/WrapText.png");
    public static final Icon ACTION_RERUN = load("/img/v1/action/Rerun.png");
    public static final Icon ACTION_PIN = load("/img/v1/action/Pin.png");
    public static final Icon ACTION_REVERT_CHANGES = load("/img/v1/action/RevertChanges.png");
    public static final Icon ACTION_SELECT_ALL = load("/img/v1/action/SelectAll.png");


    public static final Icon DATABASE_NAVIGATOR = load("/img/v1/project/DatabaseNavigator.png");
    public static final Icon DATABASE_MODULE = load("/img/v1/project/DatabaseModule.png");
    public static final Icon DATABASE_MODULE_SMALL_OPEN = load("/img/v1/project/DatabaseModuleOpen.png");
    public static final Icon DATABASE_MODULE_SMALL_CLOSED = load("/img/v1/project/DatabaseModuleClosed.png");

    public static final Icon WINDOW_DATABASE_BROWSER = load("/img/v1/window/DatabaseBrowser.png");
    public static final Icon WINDOW_EXECUTION_CONSOLE = load("/img/v1/window/ExecutionConsole.png");

    public static final Icon FILE_SQL_CONSOLE = load("/img/v1/file/SQLConsole.png");
    public static final Icon FILE_SQL = load("/img/v1/file/SQLFile.png");
    public static final Icon FILE_PLSQL = load("/img/v1/file/PLSQLFile.png");
    public static final Icon FILE_BLOCK_PLSQL = load("FILE_BLOCK_PLSQL", "/img/v1/PLSQLBlock.png");
    public static final Icon FILE_BLOCK_PSQL = load("FILE_BLOCK_PSQL", "/img/v1/file/PSQLBlock.png");
    public static final Icon FILE_BLOCK_SQL = load("FILE_BLOCK_SQL", "/img/v1/file/SQLBlock.png");


    public static final Icon DIALOG_INFO     = load("/img/v1/dialog/Information.png");
    public static final Icon DIALOG_WARNING  = load("/img/v1/dialog/Warning.png");
    public static final Icon DIALOG_ERROR    = load("/img/v1/dialog/Error.png");
    public static final Icon DIALOG_QUESTION = load("/img/v1/dialog/Dialog.png");


    public static final Icon METHOD_EXECUTION_RUN     = load("/img/v1/action/ExecuteMethod.png");
    public static final Icon METHOD_EXECUTION_DEBUG   = load("/img/v1/action/DebugMethod.png");
    public static final Icon METHOD_EXECUTION_RERUN   = load("/img/v1/RerunMethodExecution.png");
    public static final Icon METHOD_EXECUTION_DIALOG  = load("/img/v1/ExecuteMethodDialog.png");
    public static final Icon METHOD_EXECUTION_HISTORY = load("/img/v1/MethodExecutionHistory.png");
    public static final Icon METHOD_LOOKUP            = load("/img/v1/MethodLookup.png");


    public static final Icon STMT_EXECUTION_RUN           = load("/img/v1/action/ExecuteStatement.png");
    public static final Icon STMT_EXECUTION_RERUN         = load("/img/v1/action/Rerun.png");
    public static final Icon STMT_EXECUTION_RESUME        = load("/img/v1/action/ResumeExecution.png");
    public static final Icon STMT_EXECUTION_REFRESH       = load("/img/v1/action/Refresh.png");
    public static final Icon STMT_EXECUTION_ERROR         = load("/img/v1/common/Error.png");
    public static final Icon STMT_EXECUTION_ERROR_RERUN   = load("/img/v1/action/ExecuteStatementError.png");

    public static final Icon STMT_EXEC_RESULTSET        = load("/img/v1/ExecutionResultSet.png");
    public static final Icon STMT_EXEC_RESULTSET_RERUN  = load("/img/v1/ExecutionResultSetRerun.png");
    public static final Icon STMT_EXEC_RESULTSET_ORPHAN = load("/img/v1/ExecutionResultSetOrphan.png");

    public static final Icon EXEC_RESULT_RERUN            = load("/img/v1/action/Refresh.png");
    public static final Icon EXEC_RESULT_OPEN_EXEC_DIALOG = load("/img/v1/ExecuteMethodDialog.png");
    public static final Icon EXEC_RESULT_RESUME           = load("/img/v1/action/ResumeExecution.png");
    public static final Icon EXEC_RESULT_STOP             = load("/img/v1/action/StopExecution.png");
    public static final Icon EXEC_RESULT_CLOSE            = load("/img/v1/action/Close.png");
    public static final Icon EXEC_RESULT_VIEW_STATEMENT   = load("/img/v1/action/Preview.png");
    public static final Icon EXEC_RESULT_VIEW_RECORD      = load("/img/v1/RecordViewer.png");
    public static final Icon EXEC_RESULT_OPTIONS          = load("/img/v1/action/Properties.png");
    public static final Icon EXEC_RESULT_MESSAGES         = load("/img/v1/common/Messages.png");
    public static final Icon EXEC_CONFIG                  = load("/img/v1/DBProgram.png");

    public static final Icon NAVIGATION_GO_TO_SPEC       = load("/img/v1/GoToSpec.png");
    public static final Icon NAVIGATION_GO_TO_BODY       = load("/img/v1/GoToBody.png");

    public static final Icon BROWSER_BACK = load("/img/v1/action/BrowserBack.png");
    public static final Icon BROWSER_NEXT = load("/img/v1/action/BrowserForward.png");
    public static final Icon BROWSER_AUTOSCROLL_TO_EDITOR = load("/img/v1/action/AutoscrollToSource.png");
    public static final Icon BROWSER_AUTOSCROLL_FROM_EDITOR = load("/img/v1/action/AutoscrollFromSource.png");
    public static final Icon BROWSER_OBJECT_PROPERTIES = load("/img/v1/ObjectProperties.png");


    public static final Icon DATA_EDITOR_ROW_DEFAULT = load("/img/v1/DefaultRow.png");
    public static final Icon DATA_EDITOR_ROW_INSERT = load("/img/v1/InsertRow.png");
    public static final Icon DATA_EDITOR_ROW_NEW = load("/img/v1/NewRow.png");
    public static final Icon DATA_EDITOR_ROW_MODIFIED = load("/img/v1/ModifiedRow.png");
    public static final Icon DATA_EDITOR_ROW_DELETED = load("/img/v1/DeletedRow.png");

    public static final Icon DATA_EDITOR_DUPLICATE_RECORD = load("/img/v1/action/DuplicateRecord.png");
    public static final Icon DATA_EDITOR_INSERT_RECORD = load("/img/v1/action/InsertRecord.png");
    public static final Icon DATA_EDITOR_DELETE_RECORD = load("/img/v1/action/DeleteRecord.png");
    public static final Icon DATA_EDITOR_SWITCH_EDITABLE_STATUS = load("/img/v1/DatasetEditorSwitchEditableStatus.png");
    public static final Icon DATA_EDITOR_FETCH_NEXT_RECORDS = load("/img/v1/action/ResumeExecution.png");
    public static final Icon DATA_EDITOR_EDIT_RECORD = load("/img/v1/EditDatasetRecord.png");
    public static final Icon DATA_EDITOR_NEXT_RECORD = load("/img/v1/NextRecord.png");
    public static final Icon DATA_EDITOR_PREVIOUS_RECORD = load("/img/v1/PreviousRecord.png");
    public static final Icon DATA_EDITOR_FIRST_RECORD = load("/img/v1/FirstRecord.png");
    public static final Icon DATA_EDITOR_LAST_RECORD = load("/img/v1/LastRecord.png");
    public static final Icon DATA_EDITOR_LOCK_EDITING = load("/img/v1/LockEditing.png");
    public static final Icon DATA_EDITOR_SORT_ASC = load("/img/v1/action/DataEditorSortAscending.png");
    public static final Icon DATA_EDITOR_SORT_DESC = load("/img/v1/action/DataEditorSortDescending.png");


    public static final Icon DATA_EDITOR_RELOAD_DATA = load("/img/v1/action/Refresh.png");
    public static final Icon DATA_EDITOR_BROWSE =    load("/img/v1/Browse.png");
    public static final Icon DATA_EDITOR_CALENDAR =    load("/img/v1/Calendar.png");

    public static final Icon DATA_EXPORT =    load("/img/v1/action/DataExport.png");
    public static final Icon DATA_IMPORT =    load("/img/v1/action/DataImport.png");
    public static final Icon DATA_SORTING =    load("/img/v1/action/DataSorting.png");
    public static final Icon DATA_SORTING_ASC =    load("/img/v1/action/DataSortingAsc.png");
    public static final Icon DATA_SORTING_DESC =    load("/img/v1/action/DataSortingDesc.png");
    public static final Icon DATA_COLUMNS =    load("/img/v1/action/ColumnSetup.png");

    public static final Icon DATASET_FILTER =    load("/img/v1/filter/DatasetFilter.png");
    public static final Icon DATASET_FILTER_NEW =    load("/img/v1/filter/DatasetFilterNew.png");
    public static final Icon DATASET_FILTER_EDIT =    load("/img/v1/filter/DatasetFilterEdit.png");
    public static final Icon DATASET_FILTER_BASIC =    load("/img/v1/filter/DatasetFilterBasic.png");
    public static final Icon DATASET_FILTER_BASIC_ERR =    load("/img/v1/filter/DatasetFilterBasicErr.png");
    public static final Icon DATASET_FILTER_BASIC_TEMP =    load("/img/v1/filter/DatasetFilterBasicTemp.png");
    public static final Icon DATASET_FILTER_BASIC_TEMP_ERR =    load("/img/v1/filter/DatasetFilterBasicTempErr.png");
    public static final Icon DATASET_FILTER_CUSTOM =    load("/img/v1/filter/DatasetFilterCustom.png");
    public static final Icon DATASET_FILTER_CUSTOM_ERR =    load("/img/v1/filter/DatasetFilterCustomErr.png");
    public static final Icon DATASET_FILTER_GLOBAL =    load("/img/v1/filter/DatasetFilterGlobal.png");
    public static final Icon DATASET_FILTER_GLOBAL_ERR =    load("/img/v1/filter/DatasetFilterGlobalErr.png");
    public static final Icon DATASET_FILTER_EMPTY =    load("/img/v1/filter/DatasetFilterEmpty.png");

    public static final Icon DATASET_FILTER_CONDITION_ACTIVE =    load("/img/v1/ActiveFilterCondition.png");
    public static final Icon DATASET_FILTER_CONDITION_INACTIVE =    load("/img/v1/InactiveFilterCondition.png");
    public static final Icon DATASET_FILTER_CONDITION_REMOVE =    load("/img/v1/RemoveFilterCondition.png");
    public static final Icon DATASET_FILTER_CONDITION_NEW =    load("/img/v1/NewFilterCondition.png");


    public static final Icon CONDITION_JOIN_TYPE =    load("/img/v1/JoinTypeSwitch.png");

    public static final Icon TEXT_CELL_EDIT_ACCEPT = load("/img/v1/CellEditAccept.png");
    public static final Icon TEXT_CELL_EDIT_REVERT = load("/img/v1/CellEditRevert.png");
    public static final Icon TEXT_CELL_EDIT_DELETE = load("/img/v1/CellEditDelete.png");

    public static final Icon CALENDAR_CELL_EDIT_NEXT_MONTH = load("/img/v1/CalendarNextMonth.png");
    public static final Icon CALENDAR_CELL_EDIT_NEXT_YEAR = load("/img/v1/CalendarNextYear.png");
    public static final Icon CALENDAR_CELL_EDIT_PREVIOUS_MONTH = load("/img/v1/CalendarPreviousMonth.png");
    public static final Icon CALENDAR_CELL_EDIT_PREVIOUS_YEAR = load("/img/v1/CalendarPreviousYear.png");
    public static final Icon CALENDAR_CELL_EDIT_CLEAR_TIME = load("/img/v1/CalendarResetTime.png");

    public static final Icon EXEC_MESSAGES_INFO    = load("/img/v1/common/Info.png");
    public static final Icon EXEC_MESSAGES_WARNING = load("/img/v1/common/Warning.png");
    public static final Icon EXEC_MESSAGES_ERROR   = load("/img/v1/common/Error.png");

    public static final Icon CHECK   = load("/img/v1/common/Checked.png");
    public static final Icon PROJECT = load("/img/v1/project/Project.png");
    public static final Icon FILE_CONNECTION_MAPPING = load("/img/v1/FileConnection.png");
    public static final Icon FILE_SCHEMA_MAPPING = load("/img/v1/FileSchema.png");

    public static final Icon CODE_EDITOR_SAVE = load("/img/v1/action/SaveToDatabase.png");
    public static final Icon CODE_EDITOR_RESET = load("/img/v1/action/Reset.png");
    public static final Icon CODE_EDITOR_RELOAD = load("/img/v1/action/Refresh.png");
    public static final Icon CODE_EDITOR_DIFF = load("/img/v1/action/ShowDiff.png");
    public static final Icon CODE_EDITOR_DIFF_DB = load("/img/v1/action/ShowDbDiff.png");
    public static final Icon CODE_EDITOR_DDL_FILE = load("/img/v1/DDLFile.png");
    public static final Icon CODE_EDITOR_DDL_FILE_NEW = load("/img/v1/DDLFileNew.png");
    public static final Icon CODE_EDITOR_DDL_FILE_DETACH = load("/img/v1/DDLFileUnbind.png");
    public static final Icon CODE_EDITOR_DDL_FILE_ATTACH = load("/img/v1/DDLFileBind.png");
    public static final Icon CODE_EDITOR_SPEC = load("/img/v1/CodeSpec.png");
    public static final Icon CODE_EDITOR_BODY = load("/img/v1/CodeBody.png");

    public static final Icon OBEJCT_COMPILE     = load("/img/v1/action/Compile.png");
    public static final Icon OBEJCT_COMPILE_DEBUG = load("/img/v1/action/CompileDebug.png");
    //public static final Icon OBEJCT_COMPILE_KEEP = load("/img/CompileKeep.png");
    public static final Icon OBEJCT_COMPILE_ASK = load("/img/v1/action/CompileAsk.png");
    public static final Icon OBEJCT_EDIT_SOURCE = load("/img/v1/EditSource.png");
    public static final Icon OBEJCT_EDIT_DATA = load("/img/v1/EditData.png");
    public static final Icon OBEJCT_VIEW_DATA = load("/img/v1/ViewData.png");

    public static final Icon CONNECTION_COMMIT   = load("CONNECTION_COMMIT", "/img/v1/action/ConnectionCommit.png");
    public static final Icon CONNECTION_ROLLBACK = load("CONNECTION_ROLLBACK", "/img/v1/action/ConnectionRollback.png");
    public static final Icon CONNECTION_DUPLICATE = load("/img/v1/action/DuplicateConnection.png");
    public static final Icon CONNECTION_COPY = load("/img/v1/action/CopyConnection.png");
    public static final Icon CONNECTION_PASTE = load("/img/v1/action/PasteConnection.png");

    public static final Icon COMMON_DIRECTION_IN = load("/img/v1/common/DirectionIn.png");
    public static final Icon COMMON_DIRECTION_OUT = load("/img/v1/common/DirectionOut.png");
    public static final Icon COMMON_DIRECTION_IN_OUT = load("/img/v1/common/DirectionInOut.png");




    public static final Icon CONN_STATUS_INVALID      = load("/img/v1/common/ErrorBig.png");
    public static final Icon CONN_STATUS_CONNECTED    = load("/img/v1/common/BulbOn.png");
    public static final Icon CONN_STATUS_DISCONNECTED = load("/img/v1/common/BulbOff.png");

    public static final Icon CONNECTION_VIRTUAL       = load("/img/v1/connection/ConnectionVirtual.png");
    public static final Icon CONNECTION_ACTIVE        = load("/img/v1/connection/ConnectionActive.png");
    public static final Icon CONNECTION_ACTIVE_NEW    = load("/img/v1/connection/ConnectionActiveNew.png");
    public static final Icon CONNECTION_INACTIVE      = load("/img/v1/connection/ConnectionInactive.png");
    public static final Icon CONNECTION_DISABLED      = load("/img/v1/connection/ConnectionDisabled.png");
    public static final Icon CONNECTION_NEW           = load("/img/v1/connection/ConnectionNew.png");
    public static final Icon CONNECTION_INVALID       = load("/img/v1/connection/ConnectionInvalid.png");
    public static final Icon CONNECTIONS              = load("/img/v1/connection/Connections.png");


//    public static final Icon DBO_ARGUMENT_IN         = createRowIcon(DBO_ARGUMENT, COMMON_DIRECTION_IN);
//    public static final Icon DBO_ARGUMENT_OUT        = createRowIcon(DBO_ARGUMENT, COMMON_DIRECTION_OUT);
//    public static final Icon DBO_ARGUMENT_IN_OUT     = createRowIcon(DBO_ARGUMENT, COMMON_DIRECTION_IN_OUT);

    public static final Icon DBO_ATTRIBUTE           = load("/img/v1/object/Attribute.png");
    public static final Icon DBO_ATTRIBUTES          = load("/img/v1/object/Attributes.png");
    public static final Icon DBO_ARGUMENT            = load("/img/v1/object/Argument.png");
    public static final Icon DBO_ARGUMENTS           = load("/img/v1/object/Arguments.png");
    public static final Icon DBO_ARGUMENT_IN         = load("/img/v1/object/ArgumentIn.png");
    public static final Icon DBO_ARGUMENT_OUT        = load("/img/v1/object/ArgumentOut.png");
    public static final Icon DBO_ARGUMENT_IN_OUT     = load("/img/v1/object/ArgumentInOut.png");
    public static final Icon DBO_CLUSTER                = load("/img/v1/object/Cluster.png");
    public static final Icon DBO_CLUSTERS               = load("/img/v1/object/Clusters.png");
    public static final Icon DBO_COLUMN                 = load("/img/v1/object/Column.png");
    public static final Icon DBO_COLUMN_PK              = load("/img/v1/object/ColumnPk.png");
    public static final Icon DBO_COLUMN_FK              = load("/img/v1/object/ColumnFk.png");
    public static final Icon DBO_COLUMN_PFK             = load("/img/v1/object/ColumnPkFk.png");
    public static final Icon DBO_COLUMN_HIDDEN          = load("/img/v1/object/ColumnHidden.png");
    public static final Icon DBO_COLUMNS                = load("/img/v1/object/Columns.png");
    public static final Icon DBO_CONSTRAINT             = load("/img/v1/object/Constraint.png");
    public static final Icon DBO_CONSTRAINT_DISABLED    = load("/img/v1/object/ConstraintDisabled.png");
    public static final Icon DBO_CONSTRAINTS            = load("/img/v1/object/Constraints.png");
    public static final Icon DBO_DATABASE_LINK          = load("/img/v1/object/DatabaseLink.png");
    public static final Icon DBO_DATABASE_LINKS         = load("/img/v1/object/DatabaseLinks.png");
    public static final Icon DBO_DIMENSION              = load("/img/v1/object/Dimension.png");
    public static final Icon DBO_DIMENSIONS             = load("/img/v1/object/Dimensions.png");
    public static final Icon DBO_FUNCTION               = load("/img/v1/object/Function.png");
    public static final Icon DBO_FUNCTION_DEBUG         = load("/img/v1/object/FunctionDebug.png");
    public static final Icon DBO_FUNCTION_ERR           = load("/img/v1/object/FunctionErr.png");
    public static final Icon DBO_FUNCTIONS              = load("/img/v1/object/Functions.png");
    public static final Icon DBO_INDEX                  = load("/img/v1/object/Index.png");
    public static final Icon DBO_INDEX_DISABLED         = load("/img/v1/object/IndexDisabled.png");
    public static final Icon DBO_INDEXES                = load("/img/v1/object/Indexes.png");
    public static final Icon DBO_MATERIALIZED_VIEW      = load("/img/v1/object/MaterializedView.png");
    public static final Icon DBO_MATERIALIZED_VIEWS     = load("/img/v1/object/MaterializedViews.png");
    public static final Icon DBO_METHOD                 = load("/img/v1/object/Method.png");
    public static final Icon DBO_METHODS                = load("/img/v1/object/Methods.png");
    public static final Icon DBO_NESTED_TABLE           = load("/img/v1/object/NestedTable.png");
    public static final Icon DBO_NESTED_TABLES          = load("/img/v1/object/NestedTables.png");
    public static final Icon DBO_PACKAGE                = load("/img/v1/object/Package.png");
    public static final Icon DBO_PACKAGE_ERR            = load("/img/v1/object/PackageErr.png");
    public static final Icon DBO_PACKAGE_DEBUG          = load("/img/v1/object/PackageDebug.png");
    public static final Icon DBO_PACKAGES               = load("/img/v1/object/Packages.png");
    public static final Icon DBO_PACKAGE_SPEC           = load("DBO_PACKAGE_SPEC", "/img/v1/object/PackageSpec.png");
    public static final Icon DBO_PACKAGE_BODY           = load("DBO_PACKAGE_BODY", "/img/v1/object/PackageBody.png");
    public static final Icon DBO_PROCEDURE              = load("/img/v1/object/Procedure.png");
    public static final Icon DBO_PROCEDURE_ERR          = load("/img/v1/object/ProcedureErr.png");
    public static final Icon DBO_PROCEDURE_DEBUG        = load("/img/v1/object/ProcedureDebug.png");
    public static final Icon DBO_PROCEDURES             = load("/img/v1/object/Procedures.png");
    public static final Icon DBO_PRIVILEGE              = load("/img/v1/object/Privilege.png");
    public static final Icon DBO_PRIVILEGES             = load("/img/v1/object/Privileges.png");
    public static final Icon DBO_ROLE                   = load("/img/v1/object/Role.png");
    public static final Icon DBO_ROLES                  = load("/img/v1/object/Roles.png");
    public static final Icon DBO_SCHEMA                 = load("/img/v1/object/Schema.png");
    public static final Icon DBO_SCHEMAS                = load("/img/v1/object/Schemas.png");
    public static final Icon DBO_SYNONYM                = load("/img/v1/object/Synonym.png");
    public static final Icon DBO_SYNONYM_ERR            = load("/img/v1/object/SynonymErr.png");
    public static final Icon DBO_SYNONYMS               = load("/img/v1/object/Synonyms.png");
    public static final Icon DBO_SEQUENCE               = load("/img/v1/object/Sequence.png");
    public static final Icon DBO_SEQUENCES              = load("/img/v1/object/Sequences.png");
    public static final Icon DBO_TMP_TABLE              = load("/img/v1/object/TableTmp.png");
    public static final Icon DBO_TABLE                  = load("/img/v1/object/Table.png");
    public static final Icon DBO_TABLES                 = load("/img/v1/object/Tables.png");
    public static final Icon DBO_TRIGGER                = load("/img/v1/object/Trigger.png");
    public static final Icon DBO_TRIGGER_ERR            = load("/img/v1/object/TriggerErr.png");
    public static final Icon DBO_TRIGGER_DEBUG          = load("/img/v1/object/TriggerDebug.png");
    public static final Icon DBO_TRIGGER_ERR_DISABLED   = load("/img/v1/object/TriggerErrDisabled.png");
    public static final Icon DBO_TRIGGER_DISABLED       = load("/img/v1/object/TriggerDisabled.png");
    public static final Icon DBO_TRIGGER_DISABLED_DEBUG = load("/img/v1/object/TriggerDisabledDebug.png");
    public static final Icon DBO_TRIGGERS               = load("/img/v1/object/Triggers.png");
    public static final Icon DBO_TYPE                   = load("/img/v1/object/Type.png");
    public static final Icon DBO_TYPE_COLLECTION        = load("/img/v1/object/TypeCollection.png");
    public static final Icon DBO_TYPE_COLLECTION_ERR    = load("/img/v1/object/TypeCollectionErr.png");
    public static final Icon DBO_TYPE_ERR               = load("/img/v1/object/TypeErr.png");
    public static final Icon DBO_TYPE_DEBUG             = load("/img/v1/object/TypeDebug.png");
    public static final Icon DBO_TYPES                  = load("/img/v1/object/Types.png");
    public static final Icon DBO_USER                   = load("/img/v1/object/User.png");
    public static final Icon DBO_USER_EXPIRED           = load("/img/v1/object/UserExpired.png");
    public static final Icon DBO_USER_LOCKED            = load("/img/v1/object/UserLocked.png");
    public static final Icon DBO_USER_EXPIRED_LOCKED    = load("/img/v1/object/UserExpiredLocked.png");
    public static final Icon DBO_USERS                  = load("/img/v1/object/Users.png");
    public static final Icon DBO_VIEW                   = load("/img/v1/object/View.png");
    public static final Icon DBO_VIEW_SYNONYM           = load("/img/v1/object/ViewSynonym.png");
    public static final Icon DBO_VIEWS                  = load("/img/v1/object/Views.png");
    public static final Icon DBO_VARIABLE               = load("/img/v1/object/Variable.png");

    public static final Icon DBO_LABEL_PK_FK            = load("/img/v1/object/PrimaryKeyForeignKey.png");
    public static final Icon DBO_LABEL_PK               = load("/img/v1/object/PrimaryKey.png");
    public static final Icon DBO_LABEL_FK               = load("/img/v1/object/ForeignKey.png");


    public static final Icon DEBUG_INVALID_BREAKPOINT  = load("/img/v1/InvalidBreakpoint.png");



    public static final Icon SPACE                        = load("/img/v1/Space.png");
    public static final Icon TREE_BRANCH                  = load("/img/v1/TreeBranch.png");
    public static final Icon SMALL_TREE_BRANCH            = load("/img/v1/SmallTreeBranch.png");






    private static Icon load(String path) {
        if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() < 122) {
        //if (GUIUtil.supportsDarkLookAndFeel()) {
            path = path.replace("/img/v1/", "/img/v0/");
        }
        return IconLoader.getIcon(path);
    }

    private static Icon load(String key, String path) {
        Icon icon = load(path);
        REGISTERED_ICONS.put(key, icon);
        return icon;
    }

    public static Icon getIcon(String key) {
        return REGISTERED_ICONS.get(key);
    }

    private static Icon createRowIcon(Icon left, Icon right) {
        RowIcon rowIcon = new RowIcon(2);
        rowIcon.setIcon(left, 0);
        rowIcon.setIcon(right, 1);
        return rowIcon;
    }

}
