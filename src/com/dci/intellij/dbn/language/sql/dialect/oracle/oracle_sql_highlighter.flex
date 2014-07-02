package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
%%

%class OracleSQLHighlighterFlexLexer
%implements FlexLexer
%public
%pack
%final
%unicode
%ignorecase
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private int braceCounter = 0;
    private TokenTypeBundle tt;
    public OracleSQLHighlighterFlexLexer(TokenTypeBundle tt) {
        this.tt = tt;
    }
%}


PLSQL_BLOCK_START = "create"({ws}"or"{ws}"replace")? {ws} ("function"|"procedure"|"type"|"trigger"|"package") | "declare"
PLSQL_BLOCK_END = ";"{wso}"/"[^*]

WHITE_SPACE= {white_space_char}|{line_terminator}
line_terminator = \r|\n|\r\n
input_character = [^\r\n]
white_space = [ \t\f]
white_space_char= [ \n\r\t\f]
ws  = {WHITE_SPACE}+
wso = {WHITE_SPACE}*

BLOCK_COMMENT = "/*"([^*/] | "*"[^/]? | [^*]"/" | {ws})*"*/"
LINE_COMMENT = "--" {input_character}*
REM_LINE_COMMENT = "rem"({white_space}+{input_character}*|{line_terminator})

IDENTIFIER = [:jletter:] ([:jletterdigit:]|"#")*
QUOTED_IDENTIFIER = "\""[^\"]*"\""?

string_simple_quoted      = "'"([^\']|"''"|{WHITE_SPACE})*"'"?
string_alternative_quoted = "q'["[^\[\]]*"]'"?|"q'("[^\(\)]*")'"?|"q'{"[^\{\}]*"}'"?|"q'!"[^\!]*"!'"?|"q'<"[^\<\>]*">'"?
STRING = "n"?({string_simple_quoted}|{string_alternative_quoted})

sign = "+"|"-"
digit = [0-9]
INTEGER = {digit}+("e"{sign}?{digit}+)?
NUMBER = {INTEGER}?"."{digit}+(("e"{sign}?{digit}+)|(("f"|"d"){ws}))?

VARIABLE = ":"{wso}({IDENTIFIER}|{INTEGER})

operator_equals             = "="
operator_not_equals         = (("!"|"^"|"�"){wso}"=")|("<"{wso}">")
operator_greater_than       = ">"
operator_greater_equal_than = ">"{wso}"="
operator_less_than          = "<"
operator_less_equal_than    = ">"{wso}"="
OPERATOR                    = {operator_equals}|{operator_not_equals}|{operator_greater_than}|{operator_greater_equal_than}|{operator_less_than}|{operator_less_equal_than}

SQL_KEYWORD     = "a set"|"abort"|"access"|"activate"|"active"|"add"|"advise"|"alias"|"all"|"allocate"|"allow"|"alter"|"always"|"ancillary"|"and"|"any"|"apply"|"archive"|"archivelog"|"array"|"as"|"asc"|"at"|"attribute"|"attributes"|"authid"|"autoextend"|"automatic"|"availability"|"backup"|"begin"|"between"|"bigfile"|"binding"|"block"|"body"|"both"|"buffer_cache"|"buffer_pool"|"by"|"cache"|"cancel"|"canonical"|"cascade"|"case"|"category"|"change"|"char_cs"|"check"|"checkpoint"|"child"|"chisq_df"|"chisq_obs"|"chisq_sig"|"chunk"|"class"|"clear"|"clone"|"close"|"cluster"|"coalesce"|"coarse"|"coefficient"|"cohens_k"|"column"|"column_value"|"columns"|"comment"|"commit"|"committed"|"compact"|"compatibility"|"compile"|"complete"|"compress"|"compute"|"connect"|"consider"|"consistent"|"constraint"|"constraints"|"cont_coefficient"|"contents"|"context"|"continue"|"controlfile"|"corruption"|"cost"|"cramers_v"|"create"|"cross"|"cube"|"current"|"current_user"|"currval"|"cursor"|"cycle"|"data"|"database"|"datafile"|"datafiles"|"day"|"ddl"|"deallocate"|"debug"|"decrement"|"default"|"deferrable"|"deferred"|"definer"|"delay"|"delete"|"demand"|"dense_rank"|"desc"|"determines"|"df"|"df_between"|"df_den"|"df_num"|"df_within"|"dimension"|"directory"|"disable"|"disconnect"|"disk"|"diskgroup"|"disks"|"dismount"|"distinct"|"distributed"|"dml"|"downgrade"|"drop"|"dump"|"element"|"else"|"empty"|"enable"|"encrypt"|"end"|"enforced"|"entry"|"equals_path"|"errors"|"escape"|"exact_prob"|"exceptions"|"exchange"|"excluding"|"exclusive"|"exists"|"expire"|"extent"|"externally"|"f_ratio"|"failed"|"failgroup"|"fast"|"file"|"fine"|"finish"|"first"|"flashback"|"flush"|"following"|"for"|"force"|"foreign"|"freelist"|"freelists"|"freepools"|"fresh"|"from"|"full"|"function"|"global"|"global_name"|"globally"|"grant"|"group"|"groups"|"guard"|"hash"|"having"|"hierarchy"|"history"|"hour"|"id"|"identified"|"identifier"|"ignore"|"immediate"|"in"|"including"|"increment"|"index"|"indexes"|"indextype"|"infinite"|"initial"|"initially"|"initrans"|"inner"|"insert"|"instance"|"intermediate"|"intersect"|"into"|"invalidate"|"is"|"iterate"|"java"|"join"|"keep"|"key"|"kill"|"last"|"leading"|"left"|"less"|"level"|"levels"|"library"|"like"|"like2"|"like4"|"likec"|"limit"|"link"|"lob"|"local"|"locator"|"lock"|"log"|"logfile"|"logging"|"logical"|"main"|"managed"|"manual"|"mapping"|"master"|"matched"|"materialized"|"maxextents"|"maximize"|"maxsize"|"maxvalue"|"mean_squares_between"|"mean_squares_within"|"measures"|"member"|"memory"|"merge"|"minextents"|"minus"|"minute"|"minutes"|"minvalue"|"mirror"|"mlslabel"|"mode"|"model"|"modify"|"monitoring"|"month"|"mount"|"move"|"multiset"|"name"|"nan"|"natural"|"nav"|"nchar_cs"|"nested"|"new"|"next"|"nextval"|"no"|"noarchivelog"|"noaudit"|"nocache"|"nocompress"|"nocycle"|"nodelay"|"noforce"|"nologging"|"nomapping"|"nomaxvalue"|"nominvalue"|"nomonitoring"|"none"|"noorder"|"noparallel"|"norely"|"norepair"|"noresetlogs"|"noreverse"|"noswitch"|"not"|"nothing"|"notimeout"|"novalidate"|"nowait"|"null"|"nulls"|"object"|"of"|"off"|"offline"|"on"|"one_sided_prob_or_less"|"one_sided_prob_or_more"|"one_sided_sig"|"online"|"only"|"open"|"operator"|"optimal"|"option"|"or"|"order"|"outer"|"outline"|"over"|"overflow"|"package"|"parallel"|"parameters"|"partition"|"partitions"|"pctfree"|"pctincrease"|"pctthreshold"|"pctused"|"pctversion"|"performance"|"phi_coefficient"|"physical"|"post_transaction"|"power"|"preceding"|"prepare"|"present"|"preserve"|"primary"|"prior"|"private"|"privileges"|"procedure"|"profile"|"protection"|"public"|"purge"|"query"|"quiesce"|"range"|"read"|"reads"|"rebalance"|"rebuild"|"recover"|"recovery"|"recycle"|"ref"|"reference"|"references"|"refresh"|"regexp_like"|"register"|"reject"|"rely"|"remainder"|"rename"|"repair"|"replace"|"reset"|"resetlogs"|"resize"|"resolve"|"resolver"|"resource"|"restrict"|"restricted"|"resumable"|"resume"|"retention"|"return"|"returning"|"reuse"|"reverse"|"revoke"|"rewrite"|"right"|"role"|"rollback"|"rollup"|"row"|"rownum"|"rows"|"rules"|"salt"|"sample"|"savepoint"|"scan"|"scn"|"scope"|"second"|"seed"|"segment"|"select"|"sequence"|"sequential"|"serializable"|"session"|"set"|"sets"|"settings"|"share"|"shared_pool"|"shrink"|"shutdown"|"siblings"|"sid"|"sig"|"single"|"size"|"skip"|"smallfile"|"some"|"sort"|"source"|"space"|"specification"|"spfile"|"split"|"standby"|"start"|"statistic"|"statistics"|"stop"|"storage"|"store"|"submultiset"|"subpartition"|"subpartitions"|"substitutable"|"successful"|"sum_squares_between"|"sum_squares_within"|"supplemental"|"suspend"|"switch"|"switchover"|"synonym"|"system"|"table"|"tables"|"tablespace"|"tempfile"|"template"|"temporary"|"test"|"than"|"then"|"thread"|"through"|"time"|"time_zone"|"timeout"|"timezone_abbr"|"timezone_hour"|"timezone_minute"|"timezone_region"|"to"|"trace"|"tracking"|"trailing"|"transaction"|"trigger"|"truncate"|"trusted"|"two_sided_prob"|"two_sided_sig"|"type"|"u_statistic"|"uid"|"unarchived"|"unbounded"|"under"|"under_path"|"undrop"|"union"|"unique"|"unlimited"|"unprotected"|"unquiesce"|"unrecoverable"|"until"|"unusable"|"unused"|"update"|"updated"|"upgrade"|"upsert"|"usage"|"user"|"using"|"validate"|"validation"|"value"|"values"|"varray"|"versions"|"view"|"wait"|"when"|"whenever"|"where"|"with"|"within"|"without"|"work"|"write"|"year"|"zone"|"false"|"true"|"xmlschema"|"xmltype"
SQL_FUNCTION    = "abs"|"acos"|"add_months"|"ascii"|"asciistr"|"asin"|"atan"|"atan2"|"avg"|"bfilename"|"bin_to_num"|"bitand"|"cardinality"|"cast"|"ceil"|"chartorowid"|"chr"|"collect"|"compose"|"concat"|"convert"|"corr"|"corr_k"|"corr_s"|"cos"|"cosh"|"count"|"covar_pop"|"covar_samp"|"cume_dist"|"current_date"|"current_timestamp"|"cv"|"dbtimezone"|"dbtmezone"|"decode"|"decompose"|"depth"|"deref"|"empty_blob"|"empty_clob"|"existsnode"|"exp"|"extract"|"extractvalue"|"first_value"|"floor"|"from_tz"|"greatest"|"group_id"|"grouping"|"grouping_id"|"hextoraw"|"initcap"|"instr"|"instr2"|"instr4"|"instrb"|"instrc"|"iteration_number"|"lag"|"last_day"|"last_value"|"lead"|"least"|"length"|"length2"|"length4"|"lengthb"|"lengthc"|"ln"|"lnnvl"|"localtimestamp"|"lower"|"lpad"|"ltrim"|"make_ref"|"max"|"median"|"min"|"mod"|"months_between"|"nanvl"|"nchr"|"new_time"|"next_day"|"nls_charset_decl_len"|"nls_charset_id"|"nls_charset_name"|"nls_initcap"|"nls_lower"|"nls_upper"|"nlssort"|"ntile"|"nullif"|"numtodsinterval"|"numtoyminterval"|"nvl"|"nvl2"|"ora_hash"|"path"|"percent_rank"|"percentile_cont"|"percentile_disc"|"powermultiset"|"powermultiset_by_cardinality"|"presentnnv"|"presentv"|"previous"|"rank"|"ratio_to_report"|"rawtohex"|"rawtonhex"|"reftohex"|"regexp_instr"|"regexp_replace"|"regexp_substr"|"regr_avgx"|"regr_avgy"|"regr_count"|"regr_intercept"|"regr_r2"|"regr_slope"|"regr_sxx"|"regr_sxy"|"regr_syy"|"round"|"row_number"|"rowidtochar"|"rowidtonchar"|"rpad"|"rtrim"|"scn_to_timestamp"|"sessiontimezone"|"sign"|"sin"|"sinh"|"soundex"|"sqrt"|"stats_binomial_test"|"stats_crosstab"|"stats_f_test"|"stats_ks_test"|"stats_mode"|"stats_mw_test"|"stats_one_way_anova"|"stats_t_test_indep"|"stats_t_test_indepu"|"stats_t_test_one"|"stats_t_test_paired"|"stats_wsr_test"|"stddev"|"stddev_pop"|"stddev_samp"|"substr"|"substr2"|"substr4"|"substrb"|"substrc"|"sum"|"sys_connect_by_path"|"sys_context"|"sys_dburigen"|"sys_extract_utc"|"sys_guid"|"sys_typeid"|"sys_xmlagg"|"sys_xmlgen"|"sysdate"|"systimestamp"|"tan"|"tanh"|"timestamp_to_scn"|"to_binary_double"|"to_binary_float"|"to_char"|"to_clob"|"to_date"|"to_dsinterval"|"to_lob"|"to_multi_byte"|"to_nchar"|"to_nclob"|"to_number"|"to_single_byte"|"to_timestamp"|"to_timestamp_tz"|"to_yminterval"|"translate"|"treat"|"trim"|"trunc"|"tz_offset"|"unistr"|"updatexml"|"upper"|"userenv"|"var_pop"|"var_samp"|"variance"|"vsize"|"width_bucket"|"xmlagg"|"xmlcolattval"|"xmlconcat"|"xmlforest"|"xmlsequence"|"xmltransform"
SQL_PARAMETER   = "aq_tm_processes"|"archive_lag_target"|"audit_file_dest"|"audit_sys_operations"|"audit_trail"|"background_core_dump"|"background_dump_dest"|"backup_tape_io_slaves"|"bitmap_merge_area_size"|"blank_trimming"|"circuits"|"cluster_database"|"cluster_database_instances"|"cluster_interconnects"|"commit_point_strength"|"compatible"|"composite_limit"|"connect_time"|"control_file_record_keep_time"|"control_files"|"core_dump_dest"|"cpu_count"|"cpu_per_call"|"cpu_per_session"|"create_bitmap_area_size"|"create_stored_outlines"|"current_schema"|"cursor_sharing"|"cursor_space_for_time"|"db_block_checking"|"db_block_checksum"|"db_block_size"|"db_cache_advice"|"db_cache_size"|"db_create_file_dest"|"db_create_online_log_dest_n"|"db_domain"|"db_file_multiblock_read_count"|"db_file_name_convert"|"db_files"|"db_flashback_retention_target"|"db_keep_cache_size"|"db_name"|"db_nk_cache_size"|"db_recovery_file_dest"|"db_recovery_file_dest_size"|"db_recycle_cache_size"|"db_unique_name"|"db_writer_processes"|"dbwr_io_slaves"|"ddl_wait_for_locks"|"dg_broker_config_filen"|"dg_broker_start"|"disk_asynch_io"|"dispatchers"|"distributed_lock_timeout"|"dml_locks"|"enqueue_resources"|"error_on_overlap_time"|"event"|"failed_login_attempts"|"fal_client"|"fal_server"|"fast_start_mttr_target"|"fast_start_parallel_rollback"|"file_mapping"|"fileio_network_adapters"|"filesystemio_options"|"fixed_date"|"flagger"|"gc_files_to_locks"|"gcs_server_processes"|"global_names"|"hash_area_size"|"hi_shared_memory_address"|"hs_autoregister"|"idle_time"|"ifile"|"instance"|"instance_groups"|"instance_name"|"instance_number"|"instance_type"|"isolation_level"|"java_max_sessionspace_size"|"java_pool_size"|"java_soft_sessionspace_limit"|"job_queue_processes"|"large_pool_size"|"ldap_directory_access"|"license_max_sessions"|"license_max_users"|"license_sessions_warning"|"local_listener"|"lock_sga"|"log_archive_config"|"log_archive_dest"|"log_archive_dest_n"|"log_archive_dest_state_n"|"log_archive_duplex_dest"|"log_archive_format"|"log_archive_local_first"|"log_archive_max_processes"|"log_archive_min_succeed_dest"|"log_archive_trace"|"log_buffer"|"log_checkpoint_interval"|"log_checkpoint_timeout"|"log_checkpoints_to_alert"|"log_file_name_convert"|"logical_reads_per_call"|"logical_reads_per_session"|"logmnr_max_persistent_sessions"|"max_commit_propagation_delay"|"max_dispatchers"|"max_dump_file_size"|"max_shared_servers"|"nls_calendar"|"nls_comp"|"nls_currency"|"nls_date_format"|"nls_date_language"|"nls_dual_currency"|"nls_iso_currency"|"nls_language"|"nls_length_semantics"|"nls_nchar_conv_excp"|"nls_numeric_characters"|"nls_sort"|"nls_territory"|"nls_timestamp_format"|"nls_timestamp_tz_format"|"o7_dictionary_accessibility"|"object_cache_max_size_percent"|"object_cache_optimal_size"|"olap_page_pool_size"|"open_cursors"|"open_links"|"open_links_per_instance"|"optimizer_dynamic_sampling"|"optimizer_features_enable"|"optimizer_index_caching"|"optimizer_index_cost_adj"|"optimizer_mode"|"os_authent_prefix"|"os_roles"|"osm_diskgroups"|"osm_diskstring"|"osm_power_limit"|"parallel_adaptive_multi_user"|"parallel_execution_message_size"|"parallel_instance_group"|"parallel_max_servers"|"parallel_min_percent"|"parallel_min_servers"|"parallel_threads_per_cpu"|"password_grace_time"|"password_life_time"|"password_lock_time"|"password_reuse_max"|"password_reuse_time"|"password_verify_function"|"pga_aggregate_target"|"plsql_code_type"|"plsql_compiler_flags"|"plsql_debug"|"plsql_native_library_dir"|"plsql_native_library_subdir_count"|"plsql_optimize_level"|"plsql_v2_compatibility"|"plsql_warnings"|"pre_page_sga"|"private_sga"|"processes"|"query_rewrite_enabled"|"query_rewrite_integrity"|"rdbms_server_dn"|"read_only_open_delayed"|"recovery_parallelism"|"remote_archive_enable"|"remote_dependencies_mode"|"remote_listener"|"remote_login_passwordfile"|"remote_os_authent"|"remote_os_roles"|"replication_dependency_tracking"|"resource_limit"|"resource_manager_plan"|"resumable_timeout"|"rollback_segments"|"serial_reuse"|"service_names"|"session_cached_cursors"|"session_max_open_files"|"sessions"|"sessions_per_user"|"sga_max_size"|"sga_target"|"shadow_core_dump"|"shared_memory_address"|"shared_pool_reserved_size"|"shared_pool_size"|"shared_server_sessions"|"shared_servers"|"skip_unusable_indexes"|"smtp_out_server"|"sort_area_retained_size"|"sort_area_size"|"spfile"|"sql_trace"|"sql92_security"|"sqltune_category"|"standby_archive_dest"|"standby_file_management"|"star_transformation_enabled"|"statistics_level"|"streams_pool_size"|"tape_asynch_io"|"thread"|"timed_os_statistics"|"timed_statistics"|"trace_enabled"|"tracefile_identifier"|"transactions"|"transactions_per_rollback_segment"|"undo_management"|"undo_retention"|"undo_tablespace"|"use_indirect_data_buffers"|"use_private_outlines"|"use_stored_outlines"|"user_dump_dest"|"utl_file_dir"|"workarea_size_policy"
SQL_DATA_TYPE   = "varchar2"|"with"{ws}"time"{ws}"zone"|"with"{ws}"local"{ws}"time"{ws}"zone"|"varchar"|"urowid"|"to"{ws}"second"|"to"{ws}"month"|"timestamp"|"smallint"|"rowid"|"real"|"raw"|"nvarchar2"|"numeric"|"number"|"nclob"|"nchar"{ws}"varying"|"nchar"|"national"{ws}"character"{ws}"varying"|"national"{ws}"character"|"national"{ws}"char"{ws}"varying"|"national"{ws}"char"|"long"{ws}"varchar"|"long"{ws}"raw"|"long"|"interval"{ws}"year"|"interval"{ws}"day"|"integer"|"int"|"float"|"double"{ws}"precision"|"decimal"|"date"|"clob"|"character"{ws}"varying"|"character"|"char"|"blob"|"binary_float"|"binary_double"|"bfile"

PLSQL_KEYWORD     = "a"{ws}"set"|"all"|"alter"|"and"|"any"|"array"|"as"|"asc"|"at"|"authid"|"automatic"|"autonomous_transaction"|"begin"|"between"|"block"|"body"|"both"|"bulk"|"bulk_exceptions"|"bulk_rowcount"|"by"|"canonical"|"case"|"char_base"|"char_cs"|"check"|"chisq_df"|"chisq_obs"|"chisq_sig"|"close"|"cluster"|"coalesce"|"coefficient"|"cohens_k"|"collect"|"comment"|"commit"|"committed"|"compatibility"|"compress"|"connect"|"constant"|"constraint"|"cont_coefficient"|"count"|"cramers_v"|"create"|"cross"|"cube"|"current"|"current_user"|"currval"|"cursor"|"day"|"declare"|"decrement"|"default"|"definer"|"delete"|"dense_rank"|"desc"|"deterministic"|"df"|"df_between"|"df_den"|"df_num"|"df_within"|"dimension"|"distinct"|"do"|"drop"|"dump"|"else"|"elsif"|"empty"|"end"|"equals_path"|"error_code"|"error_index"|"errors"|"escape"|"exact_prob"|"exception"|"exception_init"|"exceptions"|"exclusive"|"execute"|"exists"|"exit"|"extend"|"extends"|"f_ratio"|"fetch"|"first"|"following"|"for"|"forall"|"found"|"from"|"full"|"function"|"goto"|"group"|"having"|"heap"|"hour"|"if"|"ignore"|"immediate"|"in"|"in"{ws}"out"|"increment"|"index"|"indicator"|"indices"|"infinite"|"inner"|"insert"|"interface"|"intersect"|"interval"|"into"|"is"|"isolation"|"isopen"|"iterate"|"java"|"join"|"keep"|"last"|"leading"|"left"|"level"|"like"|"like2"|"like4"|"likec"|"limit"|"limited"|"lock"|"log"|"loop"|"main"|"matched"|"maxvalue"|"mean_squares_between"|"mean_squares_within"|"measures"|"member"|"merge"|"minus"|"minute"|"minvalue"|"mlslabel"|"mode"|"model"|"month"|"multiset"|"name"|"nan"|"natural"|"naturaln"|"nav"|"nchar_cs"|"new"|"next"|"nextval"|"nocopy"|"nocycle"|"not"|"notfound"|"nowait"|"null"|"nulls"|"number_base"|"ocirowid"|"of"|"on"|"one_sided_prob_or_less"|"one_sided_prob_or_more"|"one_sided_sig"|"only"|"opaque"|"open"|"operator"|"option"|"or"|"order"|"organization"|"others"|"out"|"outer"|"over"|"package"|"parallel_enable"|"partition"|"pctfree"|"phi_coefficient"|"positive"|"positiven"|"power"|"pragma"|"preceding"|"present"|"prior"|"private"|"procedure"|"public"|"raise"|"range"|"rank"|"read"|"record"|"ref"|"reference"|"regexp_like"|"release"|"remainder"|"replace"|"restrict_references"|"return"|"returning"|"reject"|"reverse"|"right"|"rnds"|"rnps"|"rollback"|"rollup"|"row"|"rowcount"|"rownum"|"rows"|"rowtype"|"rules"|"sample"|"save"|"savepoint"|"scn"|"second"|"seed"|"segment"|"select"|"separate"|"sequential"|"serializable"|"serially_reusable"|"set"|"sets"|"share"|"siblings"|"sig"|"single"|"some"|"space"|"sql"|"sqlcode"|"sqlerrm"|"start"|"statistic"|"submultiset"|"subpartition"|"subtype"|"successful"|"sum_squares_between"|"sum_squares_within"|"synonym"|"table"|"then"|"time"|"timezone_abbr"|"timezone_hour"|"timezone_minute"|"timezone_region"|"to"|"trailing"|"transaction"|"trigger"|"trust"|"two_sided_prob"|"two_sided_sig"|"type"|"u_statistic"|"unbounded"|"under_path"|"union"|"unique"|"unlimited"|"until"|"update"|"updated"|"upsert"|"use"|"user"|"using"|"validate"|"value"|"values"|"varray"|"varying"|"versions"|"view"|"wait"|"when"|"whenever"|"where"|"while"|"with"|"within"|"wnds"|"wnps"|"work"|"write"|"year"|"zone"|"false"|"true"
PLSQL_FUNCTION    = "abs"|"acos"|"add_months"|"ascii"|"asciistr"|"asin"|"atan"|"atan2"|"avg"|"bfilename"|"bin_to_num"|"bitand"|"cardinality"|"cast"|"ceil"|"chartorowid"|"chr"|"compose"|"concat"|"convert"|"corr"|"corr_k"|"corr_s"|"cos"|"cosh"|"covar_pop"|"covar_samp"|"cume_dist"|"current_date"|"current_timestamp"|"cv"|"dbtimezone"|"dbtmezone"|"decode"|"decompose"|"depth"|"deref"|"empty_blob"|"empty_clob"|"existsnode"|"exp"|"extract"|"extractvalue"|"first_value"|"floor"|"from_tz"|"greatest"|"group_id"|"grouping"|"grouping_id"|"hextoraw"|"initcap"|"instr"|"instr2"|"instr4"|"instrb"|"instrc"|"iteration_number"|"lag"|"last_day"|"last_value"|"lead"|"least"|"length"|"length2"|"length4"|"lengthb"|"lengthc"|"ln"|"lnnvl"|"localtimestamp"|"lower"|"lpad"|"ltrim"|"make_ref"|"max"|"median"|"min"|"mod"|"months_between"|"nanvl"|"nchr"|"new_time"|"next_day"|"nls_charset_decl_len"|"nls_charset_id"|"nls_charset_name"|"nls_initcap"|"nls_lower"|"nls_upper"|"nlssort"|"ntile"|"nullif"|"numtodsinterval"|"numtoyminterval"|"nvl"|"nvl2"|"ora_hash"|"path"|"percent_rank"|"percentile_cont"|"percentile_disc"|"powermultiset"|"powermultiset_by_cardinality"|"presentnnv"|"presentv"|"previous"|"ratio_to_report"|"rawtohex"|"rawtonhex"|"reftohex"|"regexp_instr"|"regexp_replace"|"regexp_substr"|"regr_avgx"|"regr_avgy"|"regr_count"|"regr_intercept"|"regr_r2"|"regr_slope"|"regr_sxx"|"regr_sxy"|"regr_syy"|"round"|"row_number"|"rowidtochar"|"rowidtonchar"|"rpad"|"rtrim"|"scn_to_timestamp"|"sessiontimezone"|"sign"|"sin"|"sinh"|"soundex"|"sqrt"|"stats_binomial_test"|"stats_crosstab"|"stats_f_test"|"stats_ks_test"|"stats_mode"|"stats_mw_test"|"stats_one_way_anova"|"stats_t_test_indep"|"stats_t_test_indepu"|"stats_t_test_one"|"stats_t_test_paired"|"stats_wsr_test"|"stddev"|"stddev_pop"|"stddev_samp"|"substr"|"substr2"|"substr4"|"substrb"|"substrc"|"sum"|"sys_connect_by_path"|"sys_context"|"sys_dburigen"|"sys_extract_utc"|"sys_guid"|"sys_typeid"|"sys_xmlagg"|"sys_xmlgen"|"sysdate"|"systimestamp"|"tan"|"tanh"|"timestamp_to_scn"|"to_binary_double"|"to_binary_float"|"to_char"|"to_clob"|"to_date"|"to_dsinterval"|"to_lob"|"to_multi_byte"|"to_nchar"|"to_nclob"|"to_number"|"to_single_byte"|"to_timestamp"|"to_timestamp_tz"|"to_yminterval"|"translate"|"treat"|"trim"|"trunc"|"tz_offset"|"uid"|"unistr"|"updatexml"|"upper"|"userenv"|"var_pop"|"var_samp"|"variance"|"vsize"|"width_bucket"|"xmlagg"|"xmlcolattval"|"xmlconcat"|"xmlforest"|"xmlsequence"|"xmltransform"
PLSQL_PARAMETER   = "composite_limit"|"connect_time"|"cpu_per_call"|"cpu_per_session"|"create_stored_outlines"|"current_schema"|"cursor_sharing"|"db_block_checking"|"db_create_file_dest"|"db_create_online_log_dest_n"|"db_file_multiblock_read_count"|"db_file_name_convert"|"ddl_wait_for_locks"|"error_on_overlap_time"|"failed_login_attempts"|"filesystemio_options"|"flagger"|"global_names"|"hash_area_size"|"idle_time"|"instance"|"isolation_level"|"log_archive_dest_n"|"log_archive_dest_state_n"|"log_archive_min_succeed_dest"|"logical_reads_per_call"|"logical_reads_per_session"|"max_dump_file_size"|"nls_calendar"|"nls_comp"|"nls_currency"|"nls_date_format"|"nls_date_language"|"nls_dual_currency"|"nls_iso_currency"|"nls_language"|"nls_length_semantics"|"nls_nchar_conv_excp"|"nls_numeric_characters"|"nls_sort"|"nls_territory"|"nls_timestamp_format"|"nls_timestamp_tz_format"|"object_cache_max_size_percent"|"object_cache_optimal_size"|"olap_page_pool_size"|"optimizer_dynamic_sampling"|"optimizer_features_enable"|"optimizer_index_caching"|"optimizer_index_cost_adj"|"optimizer_mode"|"osm_power_limit"|"parallel_instance_group"|"parallel_min_percent"|"password_grace_time"|"password_life_time"|"password_lock_time"|"password_reuse_max"|"password_reuse_time"|"password_verify_function"|"plsql_code_type"|"plsql_compiler_flags"|"plsql_debug"|"plsql_optimize_level"|"plsql_v2_compatibility"|"plsql_warnings"|"private_sga"|"query_rewrite_enabled"|"query_rewrite_integrity"|"remote_dependencies_mode"|"resumable_timeout"|"session_cached_cursors"|"sessions_per_user"|"skip_unusable_indexes"|"sort_area_retained_size"|"sort_area_size"|"sql_trace"|"sqltune_category"|"star_transformation_enabled"|"statistics_level"|"timed_os_statistics"|"timed_statistics"|"tracefile_identifier"|"use_private_outlines"|"use_stored_outlines"|"workarea_size_policy"
PLSQL_DATA_TYPE   = "varchar2"|"with"{ws}"time"{ws}"zone"|"with"{ws}"local"{ws}"time"{ws}"zone"|"varchar"|"urowid"|"to"{ws}"second"|"to"{ws}"month"|"timestamp"|"string"|"smallint"|"rowid"|"real"|"raw"|"pls_integer"|"nvarchar2"|"numeric"|"number"|"nclob"|"nchar"{ws}"varying"|"nchar"|"national"{ws}"character"{ws}"varying"|"national"{ws}"character"|"national"{ws}"char"{ws}"varying"|"national"{ws}"char"|"long"{ws}"varchar"|"long"{ws}"raw"|"long"|"interval"{ws}"year"|"interval"{ws}"day"|"integer"|"int"|"float"|"double"{ws}"precision"|"decimal"|"date"|"clob"|"character"{ws}"varying"|"character"|"char"|"byte"|"boolean"|"blob"|"binary_integer"|"binary_float"|"binary_double"|"bfile"
PLSQL_EXCEPTION   = "access_into_null"|"case_not_found"|"collection_is_null"|"cursor_already_open"|"dup_val_on_index"|"invalid_cursor"|"invalid_number"|"login_denied"|"no_data_found"|"not_logged_on"|"program_error"|"rowtype_mismatch"|"self_is_null"|"storage_error"|"subscript_beyond_count"|"subscript_outside_limit"|"sys_invalid_rowid"|"timeout_on_resource"|"too_many_rows"|"value_error"|"zero_divide"


%state SQL, PLSQL
%%


<YYINITIAL> {

    {VARIABLE}           { return tt.getTokenType("VARIABLE"); }
    {WHITE_SPACE}+       { return tt.getSharedTokenTypes().getWhiteSpace(); }

    {BLOCK_COMMENT}      { return tt.getTokenType("BLOCK_COMMENT"); }
    {LINE_COMMENT}       { return tt.getTokenType("LINE_COMMENT"); }
    {REM_LINE_COMMENT}   { return tt.getTokenType("LINE_COMMENT"); }

    {PLSQL_BLOCK_START}  { yybegin(PLSQL); return tt.getTokenType("KEYWORD");}

    {INTEGER}            { return tt.getTokenType("INTEGER"); }
    {NUMBER}             { return tt.getTokenType("NUMBER"); }
    {STRING}             { return tt.getTokenType("STRING"); }

    {SQL_FUNCTION}       { return tt.getTokenType("FUNCTION");}
    {SQL_PARAMETER}      { return tt.getTokenType("PARAMETER");}

    {SQL_DATA_TYPE}      { return tt.getTokenType("DATA_TYPE"); }
    {SQL_KEYWORD}        { return tt.getTokenType("KEYWORD"); }
    {OPERATOR}           { return tt.getTokenType("OPERATOR"); }


    {IDENTIFIER}         { return tt.getSharedTokenTypes().getIdentifier(); }
    {QUOTED_IDENTIFIER}  { return tt.getSharedTokenTypes().getQuotedIdentifier(); }


    "("                  { return tt.getTokenType("CHR_LEFT_PARENTHESIS"); }
    ")"                  { return tt.getTokenType("CHR_RIGHT_PARENTHESIS"); }
    "["                  { return tt.getTokenType("CHR_LEFT_BRACKET"); }
    "]"                  { return tt.getTokenType("CHR_RIGHT_BRACKET"); }

    .                    { return tt.getSharedTokenTypes().getIdentifier(); }
}

<PLSQL> {
    {WHITE_SPACE}+      { return tt.getSharedTokenTypes().getWhiteSpace(); }

    {BLOCK_COMMENT}     { return tt.getTokenType("BLOCK_COMMENT"); }
    {LINE_COMMENT}      { return tt.getTokenType("LINE_COMMENT"); }
    {REM_LINE_COMMENT}  { return tt.getTokenType("LINE_COMMENT"); }

    {PLSQL_BLOCK_END}   { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }

    {INTEGER}           { return tt.getTokenType("INTEGER"); }
    {NUMBER}            { return tt.getTokenType("NUMBER"); }
    {STRING}            { return tt.getTokenType("STRING"); }

    {PLSQL_FUNCTION}    { return tt.getTokenType("FUNCTION");}
    {PLSQL_PARAMETER}   { return tt.getTokenType("PARAMETER");}
    {PLSQL_EXCEPTION}   { return tt.getTokenType("EXCEPTION");}

    {PLSQL_DATA_TYPE}   { return tt.getTokenType("DATA_TYPE"); }
    {PLSQL_KEYWORD}     { return tt.getTokenType("KEYWORD"); }
    {OPERATOR}          { return tt.getTokenType("OPERATOR"); }


    {IDENTIFIER}        { return tt.getSharedTokenTypes().getIdentifier(); }
    {QUOTED_IDENTIFIER} { return tt.getSharedTokenTypes().getQuotedIdentifier(); }


    "("                 { return tt.getTokenType("CHR_LEFT_PARENTHESIS"); }
    ")"                 { return tt.getTokenType("CHR_RIGHT_PARENTHESIS"); }
    "["                 { return tt.getTokenType("CHR_LEFT_BRACKET"); }
    "]"                 { return tt.getTokenType("CHR_RIGHT_BRACKET"); }

    .                   { return tt.getSharedTokenTypes().getIdentifier(); }
}
