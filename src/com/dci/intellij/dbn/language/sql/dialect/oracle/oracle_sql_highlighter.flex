package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%class OracleSQLHighlighterFlexLexer
%implements FlexLexer
%public
%final
%unicode
%ignorecase
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private TokenTypeBundle tt;
    public OracleSQLHighlighterFlexLexer(TokenTypeBundle tt) {
        this.tt = tt;
    }
%}


PLSQL_BLOCK_START = "create"({ws}"or"{ws}"replace")? {ws} ("function"|"procedure"|"type"|"trigger"|"package") | "declare" | "begin"
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

string_simple_quoted      = "'"([^\']|"''")*"'"?
string_alternative_quoted = "q'["[^\[\]]*"]'"? | "q'("[^\(\)]*")'"? | "q'{"[^\{\}]*"}'"? | "q'!"[^\!]*"!'"? | "q'<"[^\<\>]*">'"? | "q'|"[^|]*"|'"?
STRING = "n"?({string_alternative_quoted}|{string_simple_quoted})

sign = "+"|"-"
digit = [0-9]
INTEGER = {digit}+("e"{sign}?{digit}+)?
NUMBER = {INTEGER}?"."{digit}+(("e"{sign}?{digit}+)|(("f"|"d"){ws}))?

VARIABLE = ":"({IDENTIFIER}|{INTEGER})
SQLP_VARIABLE = "&""&"?{IDENTIFIER}

operator_equals             = "="
operator_not_equals         = (("!"|"^"){wso}"=")|("<"{wso}">")
operator_greater_than       = ">"
operator_greater_equal_than = ">"{wso}"="
operator_less_than          = "<"
operator_less_equal_than    = ">"{wso}"="
OPERATOR                    = {operator_equals}|{operator_not_equals}|{operator_greater_than}|{operator_greater_equal_than}|{operator_less_than}|{operator_less_equal_than}

SQL_KEYWORD     = "a set"|"abort"|"absent"|"access"|"activate"|"active"|"add"|"admin"|"administer"|"advise"|"advisor"|"alias"|"all"|"allocate"|"allow"|"alter"|"always"|"analyze"|"ancillary"|"and"|"any"|"apply"|"archive"|"archivelog"|"array"|"as"|"asc"|"ascii"|"assembly"|"at"|"attribute"|"attributes"|"audit"|"authid"|"autoextend"|"automatic"|"availability"|"backup"|"become"|"begin"|"between"|"bigfile"|"binding"|"block"|"body"|"both"|"buffer_cache"|"buffer_pool"|"build"|"by"|"cache"|"cancel"|"canonical"|"cascade"|"case"|"category"|"change"|"char_cs"|"check"|"checkpoint"|"child"|"chisq_df"|"chisq_obs"|"chisq_sig"|"chunk"|"class"|"clear"|"clone"|"close"|"cluster"|"coalesce"|"coarse"|"coefficient"|"cohens_k"|"column"|"column_value"|"columns"|"comment"|"commit"|"committed"|"compact"|"compatibility"|"compile"|"complete"|"compress"|"compute"|"conditional"|"connect"|"consider"|"consistent"|"constraint"|"constraints"|"cont_coefficient"|"content"|"contents"|"context"|"continue"|"controlfile"|"corruption"|"cost"|"cramers_v"|"create"|"cross"|"cube"|"current"|"current_user"|"currval"|"cursor"|"cycle"|"data"|"database"|"datafile"|"datafiles"|"day"|"ddl"|"deallocate"|"debug"|"decrement"|"default"|"defaults"|"deferrable"|"deferred"|"definer"|"delay"|"delete"|"demand"|"dense_rank"|"dequeue"|"desc"|"determines"|"df"|"df_between"|"df_den"|"df_num"|"df_within"|"dictionary"|"dimension"|"directory"|"disable"|"disconnect"|"disk"|"diskgroup"|"disks"|"dismount"|"distinct"|"distributed"|"dml"|"document"|"downgrade"|"drop"|"dump"|"edition"|"editioning"|"element"|"else"|"empty"|"enable"|"encoding"|"encrypt"|"end"|"enforced"|"entityescaping"|"entry"|"equals_path"|"error"|"errors"|"escape"|"evalname"|"evaluation"|"exact_prob"|"except"|"exceptions"|"exchange"|"exclude"|"excluding"|"exclusive"|"execute"|"exempt"|"exists"|"expire"|"explain"|"export"|"extent"|"external"|"externally"|"f_ratio"|"failed"|"failgroup"|"fast"|"fetch"|"file"|"fine"|"finish"|"first"|"flashback"|"flush"|"folder"|"following"|"for"|"force"|"foreign"|"format"|"freelist"|"freelists"|"freepools"|"fresh"|"from"|"full"|"function"|"global"|"global_name"|"globally"|"grant"|"group"|"groups"|"guard"|"hash"|"having"|"hide"|"hierarchy"|"history"|"hour"|"id"|"identified"|"identifier"|"ignore"|"immediate"|"import"|"in"|"include"|"including"|"increment"|"indent"|"index"|"indexes"|"indextype"|"infinite"|"initial"|"initially"|"initrans"|"inner"|"insert"|"instance"|"intermediate"|"intersect"|"into"|"invalidate"|"is"|"iterate"|"java"|"job"|"join"|"json"|"keep"|"key"|"keys"|"kill"|"last"|"leading"|"left"|"less"|"level"|"levels"|"library"|"like"|"like2"|"like4"|"likec"|"limit"|"link"|"lob"|"local"|"locator"|"lock"|"locked"|"log"|"logfile"|"logging"|"logical"|"main"|"manage"|"managed"|"manager"|"management"|"manual"|"mapping"|"master"|"matched"|"materialized"|"maxextents"|"maximize"|"maxsize"|"maxvalue"|"mean_squares_between"|"mean_squares_within"|"measure"|"measures"|"member"|"memory"|"merge"|"minextents"|"mining"|"minus"|"minute"|"minutes"|"minvalue"|"mirror"|"mlslabel"|"mode"|"model"|"modify"|"monitoring"|"month"|"mount"|"move"|"multiset"|"name"|"nan"|"natural"|"nav"|"nchar_cs"|"nested"|"new"|"next"|"nextval"|"no"|"noarchivelog"|"noaudit"|"nocache"|"nocompress"|"nocycle"|"nodelay"|"noentityescaping"|"noforce"|"nologging"|"nomapping"|"nomaxvalue"|"nominvalue"|"nomonitoring"|"none"|"noorder"|"noparallel"|"norely"|"norepair"|"noresetlogs"|"noreverse"|"noschemacheck"|"noswitch"|"not"|"nothing"|"notification"|"notimeout"|"novalidate"|"nowait"|"null"|"nulls"|"object"|"of"|"off"|"offline"|"offset"|"on"|"one_sided_prob_or_less"|"one_sided_prob_or_more"|"one_sided_sig"|"online"|"only"|"open"|"operator"|"optimal"|"option"|"or"|"order"|"ordinality"|"outer"|"outline"|"over"|"overflow"|"package"|"parallel"|"parameters"|"partition"|"partitions"|"passing"|"path"|"pctfree"|"pctincrease"|"pctthreshold"|"pctused"|"pctversion"|"percent"|"performance"|"phi_coefficient"|"physical"|"pivot"|"plan"|"policy"|"post_transaction"|"power"|"preceding"|"prepare"|"present"|"preserve"|"pretty"|"primary"|"prior"|"private"|"privilege"|"privileges"|"procedure"|"process"|"profile"|"program"|"protection"|"public"|"purge"|"query"|"queue"|"quiesce"|"range"|"read"|"reads"|"rebalance"|"rebuild"|"recover"|"recovery"|"recycle"|"ref"|"reference"|"references"|"refresh"|"regexp_like"|"register"|"reject"|"rely"|"remainder"|"rename"|"repair"|"replace"|"reset"|"resetlogs"|"resize"|"resolve"|"resolver"|"resource"|"restrict"|"restricted"|"resumable"|"resume"|"retention"|"return"|"returning"|"reuse"|"reverse"|"revoke"|"rewrite"|"right"|"role"|"rollback"|"rollup"|"row"|"rownum"|"rows"|"rule"|"rules"|"salt"|"sample"|"savepoint"|"scan"|"scheduler"|"schemacheck"|"scn"|"scope"|"second"|"seed"|"segment"|"select"|"sequence"|"sequential"|"serializable"|"session"|"set"|"sets"|"settings"|"share"|"shared_pool"|"show"|"shrink"|"shutdown"|"siblings"|"sid"|"sig"|"single"|"size"|"skip"|"smallfile"|"some"|"sort"|"source"|"space"|"specification"|"spfile"|"split"|"sql"|"standalone"|"standby"|"start"|"statistic"|"statistics"|"stop"|"storage"|"store"|"strict"|"submultiset"|"subpartition"|"subpartitions"|"substitutable"|"successful"|"sum_squares_between"|"sum_squares_within"|"supplemental"|"suspend"|"switch"|"switchover"|"synonym"|"system"|"table"|"tables"|"tablespace"|"tempfile"|"template"|"temporary"|"test"|"than"|"then"|"thread"|"through"|"ties"|"time"|"time_zone"|"timeout"|"timezone_abbr"|"timezone_hour"|"timezone_minute"|"timezone_region"|"to"|"trace"|"tracking"|"trailing"|"transaction"|"trigger"|"truncate"|"trusted"|"tuning"|"two_sided_prob"|"two_sided_sig"|"type"|"u_statistic"|"uid"|"unarchived"|"unbounded"|"unconditional"|"under"|"under_path"|"undrop"|"union"|"unique"|"unlimited"|"unpivot"|"unprotected"|"unquiesce"|"unrecoverable"|"until"|"unusable"|"unused"|"update"|"updated"|"upgrade"|"upsert"|"usage"|"use"|"user"|"using"|"validate"|"validation"|"value"|"values"|"varray"|"version"|"versions"|"view"|"wait"|"wellformed"|"when"|"whenever"|"where"|"with"|"within"|"without"|"work"|"wrapper"|"write"|"xml"|"xmlnamespaces"|"xmlschema"|"xmltype"|"year"|"yes"|"zone"|"false"|"true"
SQL_FUNCTION    = "abs"|"acos"|"add_months"|"appendchildxml"|"ascii"|"asciistr"|"asin"|"atan"|"atan2"|"avg"|"bfilename"|"bin_to_num"|"bitand"|"cardinality"|"cast"|"ceil"|"chartorowid"|"chr"|"collect"|"compose"|"concat"|"convert"|"corr"|"corr_k"|"corr_s"|"cos"|"cosh"|"count"|"covar_pop"|"covar_samp"|"cume_dist"|"current_date"|"current_timestamp"|"cv"|"dbtimezone"|"dbtmezone"|"decode"|"decompose"|"deletexml"|"depth"|"deref"|"empty_blob"|"empty_clob"|"existsnode"|"exp"|"extract"|"extractvalue"|"first_value"|"floor"|"from_tz"|"greatest"|"group_id"|"grouping"|"grouping_id"|"hextoraw"|"initcap"|"insertchildxml"|"insertchildxmlafter"|"insertchildxmlbefore"|"insertxmlafter"|"insertxmlbefore"|"instr"|"instr2"|"instr4"|"instrb"|"instrc"|"iteration_number"|"json_array"|"json_arrayagg"|"json_dataguide"|"json_object"|"json_objectagg"|"json_query"|"json_table"|"json_value"|"lag"|"last_day"|"last_value"|"lateral"|"lead"|"least"|"length"|"length2"|"length4"|"lengthb"|"lengthc"|"listagg"|"ln"|"lnnvl"|"localtimestamp"|"lower"|"lpad"|"ltrim"|"make_ref"|"max"|"median"|"min"|"mod"|"months_between"|"nanvl"|"nchr"|"new_time"|"next_day"|"nls_charset_decl_len"|"nls_charset_id"|"nls_charset_name"|"nls_initcap"|"nls_lower"|"nls_upper"|"nlssort"|"ntile"|"nullif"|"numtodsinterval"|"numtoyminterval"|"nvl"|"nvl2"|"ora_hash"|"percent_rank"|"percentile_cont"|"percentile_disc"|"powermultiset"|"powermultiset_by_cardinality"|"presentnnv"|"presentv"|"previous"|"rank"|"ratio_to_report"|"rawtohex"|"rawtonhex"|"reftohex"|"regexp_instr"|"regexp_replace"|"regexp_substr"|"regr_avgx"|"regr_avgy"|"regr_count"|"regr_intercept"|"regr_r2"|"regr_slope"|"regr_sxx"|"regr_sxy"|"regr_syy"|"round"|"row_number"|"rowidtochar"|"rowidtonchar"|"rpad"|"rtrim"|"scn_to_timestamp"|"sessiontimezone"|"sign"|"sin"|"sinh"|"soundex"|"sqrt"|"stats_binomial_test"|"stats_crosstab"|"stats_f_test"|"stats_ks_test"|"stats_mode"|"stats_mw_test"|"stats_one_way_anova"|"stats_t_test_indep"|"stats_t_test_indepu"|"stats_t_test_one"|"stats_t_test_paired"|"stats_wsr_test"|"stddev"|"stddev_pop"|"stddev_samp"|"substr"|"substr2"|"substr4"|"substrb"|"substrc"|"sum"|"sys_connect_by_path"|"sys_context"|"sys_dburigen"|"sys_extract_utc"|"sys_guid"|"sys_typeid"|"sys_xmlagg"|"sys_xmlgen"|"sysdate"|"systimestamp"|"tan"|"tanh"|"timestamp_to_scn"|"to_binary_double"|"to_binary_float"|"to_char"|"to_clob"|"to_date"|"to_dsinterval"|"to_lob"|"to_multi_byte"|"to_nchar"|"to_nclob"|"to_number"|"to_single_byte"|"to_timestamp"|"to_timestamp_tz"|"to_yminterval"|"translate"|"treat"|"trim"|"trunc"|"tz_offset"|"unistr"|"updatexml"|"upper"|"userenv"|"var_pop"|"var_samp"|"variance"|"vsize"|"width_bucket"|"xmlagg"|"xmlattributes"|"xmlcast"|"xmlcdata"|"xmlcolattval"|"xmlcomment"|"xmlconcat"|"xmldiff"|"xmlelement"|"xmlforest"|"xmlisvalid"|"xmlparse"|"xmlpatch"|"xmlpi"|"xmlquery"|"xmlroot"|"xmlsequence"|"xmlserialize"|"xmltable"|"xmltransform"
SQL_PARAMETER   = "aq_tm_processes"|"archive_lag_target"|"audit_file_dest"|"audit_sys_operations"|"audit_trail"|"background_core_dump"|"background_dump_dest"|"backup_tape_io_slaves"|"bitmap_merge_area_size"|"blank_trimming"|"circuits"|"cluster_database"|"cluster_database_instances"|"cluster_interconnects"|"commit_point_strength"|"compatible"|"composite_limit"|"connect_time"|"control_file_record_keep_time"|"control_files"|"core_dump_dest"|"cpu_count"|"cpu_per_call"|"cpu_per_session"|"create_bitmap_area_size"|"create_stored_outlines"|"current_schema"|"cursor_sharing"|"cursor_space_for_time"|"db_block_checking"|"db_block_checksum"|"db_block_size"|"db_cache_advice"|"db_cache_size"|"db_create_file_dest"|"db_create_online_log_dest_n"|"db_domain"|"db_file_multiblock_read_count"|"db_file_name_convert"|"db_files"|"db_flashback_retention_target"|"db_keep_cache_size"|"db_name"|"db_nk_cache_size"|"db_recovery_file_dest"|"db_recovery_file_dest_size"|"db_recycle_cache_size"|"db_unique_name"|"db_writer_processes"|"dbwr_io_slaves"|"ddl_wait_for_locks"|"dg_broker_config_filen"|"dg_broker_start"|"disk_asynch_io"|"dispatchers"|"distributed_lock_timeout"|"dml_locks"|"enqueue_resources"|"error_on_overlap_time"|"event"|"failed_login_attempts"|"fal_client"|"fal_server"|"fast_start_mttr_target"|"fast_start_parallel_rollback"|"file_mapping"|"fileio_network_adapters"|"filesystemio_options"|"fixed_date"|"flagger"|"gc_files_to_locks"|"gcs_server_processes"|"global_names"|"hash_area_size"|"hi_shared_memory_address"|"hs_autoregister"|"idle_time"|"ifile"|"instance"|"instance_groups"|"instance_name"|"instance_number"|"instance_type"|"isolation_level"|"java_max_sessionspace_size"|"java_pool_size"|"java_soft_sessionspace_limit"|"job_queue_processes"|"large_pool_size"|"ldap_directory_access"|"license_max_sessions"|"license_max_users"|"license_sessions_warning"|"local_listener"|"lock_sga"|"log_archive_config"|"log_archive_dest"|"log_archive_dest_n"|"log_archive_dest_state_n"|"log_archive_duplex_dest"|"log_archive_format"|"log_archive_local_first"|"log_archive_max_processes"|"log_archive_min_succeed_dest"|"log_archive_trace"|"log_buffer"|"log_checkpoint_interval"|"log_checkpoint_timeout"|"log_checkpoints_to_alert"|"log_file_name_convert"|"logical_reads_per_call"|"logical_reads_per_session"|"logmnr_max_persistent_sessions"|"max_commit_propagation_delay"|"max_dispatchers"|"max_dump_file_size"|"max_shared_servers"|"nls_calendar"|"nls_comp"|"nls_currency"|"nls_date_format"|"nls_date_language"|"nls_dual_currency"|"nls_iso_currency"|"nls_language"|"nls_length_semantics"|"nls_nchar_conv_excp"|"nls_numeric_characters"|"nls_sort"|"nls_territory"|"nls_timestamp_format"|"nls_timestamp_tz_format"|"o7_dictionary_accessibility"|"object_cache_max_size_percent"|"object_cache_optimal_size"|"olap_page_pool_size"|"open_cursors"|"open_links"|"open_links_per_instance"|"optimizer_dynamic_sampling"|"optimizer_features_enable"|"optimizer_index_caching"|"optimizer_index_cost_adj"|"optimizer_mode"|"os_authent_prefix"|"os_roles"|"osm_diskgroups"|"osm_diskstring"|"osm_power_limit"|"parallel_adaptive_multi_user"|"parallel_execution_message_size"|"parallel_instance_group"|"parallel_max_servers"|"parallel_min_percent"|"parallel_min_servers"|"parallel_threads_per_cpu"|"password_grace_time"|"password_life_time"|"password_lock_time"|"password_reuse_max"|"password_reuse_time"|"password_verify_function"|"pga_aggregate_target"|"plsql_code_type"|"plsql_compiler_flags"|"plsql_debug"|"plsql_native_library_dir"|"plsql_native_library_subdir_count"|"plsql_optimize_level"|"plsql_v2_compatibility"|"plsql_warnings"|"pre_page_sga"|"private_sga"|"processes"|"query_rewrite_enabled"|"query_rewrite_integrity"|"rdbms_server_dn"|"read_only_open_delayed"|"recovery_parallelism"|"remote_archive_enable"|"remote_dependencies_mode"|"remote_listener"|"remote_login_passwordfile"|"remote_os_authent"|"remote_os_roles"|"replication_dependency_tracking"|"resource_limit"|"resource_manager_plan"|"resumable_timeout"|"rollback_segments"|"serial_reuse"|"service_names"|"session_cached_cursors"|"session_max_open_files"|"sessions"|"sessions_per_user"|"sga_max_size"|"sga_target"|"shadow_core_dump"|"shared_memory_address"|"shared_pool_reserved_size"|"shared_pool_size"|"shared_server_sessions"|"shared_servers"|"skip_unusable_indexes"|"smtp_out_server"|"sort_area_retained_size"|"sort_area_size"|"spfile"|"sql_trace"|"sql92_security"|"sqltune_category"|"standby_archive_dest"|"standby_file_management"|"star_transformation_enabled"|"statement_id"|"statistics_level"|"streams_pool_size"|"tape_asynch_io"|"thread"|"timed_os_statistics"|"timed_statistics"|"trace_enabled"|"tracefile_identifier"|"transactions"|"transactions_per_rollback_segment"|"undo_management"|"undo_retention"|"undo_tablespace"|"use_indirect_data_buffers"|"use_private_outlines"|"use_stored_outlines"|"user_dump_dest"|"utl_file_dir"|"workarea_size_policy"
SQL_DATA_TYPE   = "varchar2"|"with"{ws}"time"{ws}"zone"|"with"{ws}"local"{ws}"time"{ws}"zone"|"varchar"|"urowid"|"timestamp"|"smallint"|"rowid"|"real"|"raw"|"nvarchar2"|"numeric"|"number"|"nclob"|"nchar"{ws}"varying"|"nchar"|"national"{ws}"character"{ws}"varying"|"national"{ws}"character"|"national"{ws}"char"{ws}"varying"|"national"{ws}"char"|"long"{ws}"varchar"|"long"{ws}"raw"|"long"|"interval"|"integer"|"int"|"float"|"double"{ws}"precision"|"decimal"|"date"|"clob"|"character"{ws}"varying"|"character"|"char"|"blob"|"binary_float"|"binary_double"|"bfile"

PLSQL_KEYWORD     = "a set"|"absent"|"accessible"|"after"|"agent"|"aggregate"|"all"|"alter"|"analyze"|"and"|"any"|"apply"|"array"|"as"|"asc"|"associate"|"at"|"audit"|"authid"|"automatic"|"autonomous_transaction"|"before"|"begin"|"between"|"block"|"body"|"both"|"bulk"|"bulk_exceptions"|"bulk_rowcount"|"by"|"c"|"call"|"canonical"|"case"|"char_base"|"char_cs"|"charsetform"|"charsetid"|"check"|"chisq_df"|"chisq_obs"|"chisq_sig"|"close"|"cluster"|"coalesce"|"coefficient"|"cohens_k"|"collation"|"collect"|"columns"|"comment"|"commit"|"committed"|"compatibility"|"compound"|"compress"|"conditional"|"connect"|"constant"|"constraint"|"constructor"|"cont_coefficient"|"content"|"context"|"count"|"cramers_v"|"create"|"cross"|"cube"|"current"|"current_user"|"currval"|"cursor"|"database"|"day"|"db_role_change"|"ddl"|"declare"|"decrement"|"default"|"defaults"|"definer"|"delete"|"deleting"|"dense_rank"|"desc"|"deterministic"|"df"|"df_between"|"df_den"|"df_num"|"df_within"|"dimension"|"disable"|"disassociate"|"distinct"|"do"|"document"|"drop"|"dump"|"duration"|"each"|"editionable"|"else"|"elsif"|"empty"|"enable"|"encoding"|"end"|"entityescaping"|"equals_path"|"error"|"error_code"|"error_index"|"errors"|"escape"|"evalname"|"exact_prob"|"except"|"exception"|"exception_init"|"exceptions"|"exclude"|"exclusive"|"execute"|"exists"|"exit"|"extend"|"extends"|"external"|"f_ratio"|"fetch"|"final"|"first"|"following"|"follows"|"for"|"forall"|"force"|"found"|"from"|"format"|"full"|"function"|"goto"|"grant"|"group"|"hash"|"having"|"heap"|"hide"|"hour"|"if"|"ignore"|"immediate"|"in"|"in"{ws}"out"|"include"|"increment"|"indent"|"index"|"indicator"|"indices"|"infinite"|"inline"|"inner"|"insert"|"inserting"|"instantiable"|"instead"|"interface"|"intersect"|"interval"|"into"|"is"|"isolation"|"isopen"|"iterate"|"java"|"join"|"json"|"keep"|"key"|"keys"|"language"|"last"|"leading"|"left"|"level"|"library"|"like"|"like2"|"like4"|"likec"|"limit"|"limited"|"local"|"lock"|"locked"|"log"|"logoff"|"logon"|"loop"|"main"|"map"|"matched"|"maxlen"|"maxvalue"|"mean_squares_between"|"mean_squares_within"|"measures"|"member"|"merge"|"minus"|"minute"|"minvalue"|"mlslabel"|"mode"|"model"|"month"|"multiset"|"name"|"nan"|"natural"|"naturaln"|"nav"|"nchar_cs"|"nested"|"new"|"next"|"nextval"|"no"|"noaudit"|"nocopy"|"nocycle"|"noentityescaping"|"noneditionable"|"noschemacheck"|"not"|"notfound"|"nowait"|"null"|"nulls"|"number_base"|"object"|"ocirowid"|"of"|"offset"|"oid"|"old"|"on"|"one_sided_prob_or_less"|"one_sided_prob_or_more"|"one_sided_sig"|"only"|"opaque"|"open"|"operator"|"option"|"or"|"order"|"ordinality"|"organization"|"others"|"out"|"outer"|"over"|"overflow"|"overriding"|"package"|"parallel_enable"|"parameters"|"parent"|"partition"|"passing"|"path"|"pctfree"|"percent"|"phi_coefficient"|"pipe"|"pipelined"|"pivot"|"positive"|"positiven"|"power"|"pragma"|"preceding"|"present"|"pretty"|"prior"|"private"|"procedure"|"public"|"raise"|"range"|"read"|"record"|"ref"|"reference"|"referencing"|"regexp_like"|"reject"|"release"|"relies_on"|"remainder"|"rename"|"replace"|"restrict_references"|"result"|"result_cache"|"return"|"returning"|"reverse"|"revoke"|"right"|"rnds"|"rnps"|"rollback"|"rollup"|"row"|"rowcount"|"rownum"|"rows"|"rowtype"|"rules"|"sample"|"save"|"savepoint"|"schema"|"schemacheck"|"scn"|"second"|"seed"|"segment"|"select"|"self"|"separate"|"sequential"|"serializable"|"serially_reusable"|"servererror"|"set"|"sets"|"share"|"show"|"shutdown"|"siblings"|"sig"|"single"|"size"|"skip"|"some"|"space"|"sql"|"sqlcode"|"sqlerrm"|"standalone"|"start"|"startup"|"statement"|"static"|"statistic"|"statistics"|"strict"|"struct"|"submultiset"|"subpartition"|"subtype"|"successful"|"sum_squares_between"|"sum_squares_within"|"suspend"|"synonym"|"table"|"tdo"|"then"|"ties"|"time"|"timezone_abbr"|"timezone_hour"|"timezone_minute"|"timezone_region"|"to"|"trailing"|"transaction"|"trigger"|"truncate"|"trust"|"two_sided_prob"|"two_sided_sig"|"type"|"u_statistic"|"unbounded"|"unconditional"|"under"|"under_path"|"union"|"unique"|"unlimited"|"unpivot"|"until"|"update"|"updated"|"updating"|"upsert"|"use"|"user"|"using"|"validate"|"value"|"values"|"variable"|"varray"|"varying"|"version"|"versions"|"view"|"wait"|"wellformed"|"when"|"whenever"|"where"|"while"|"with"|"within"|"without"|"wnds"|"wnps"|"work"|"write"|"wrapped"|"wrapper"|"xml"|"xmlnamespaces"|"year"|"yes"|"zone"|"false"|"true"
PLSQL_FUNCTION    = "abs"|"acos"|"add_months"|"appendchildxml"|"ascii"|"asciistr"|"asin"|"atan"|"atan2"|"avg"|"bfilename"|"bin_to_num"|"bitand"|"cardinality"|"cast"|"ceil"|"chartorowid"|"chr"|"compose"|"concat"|"convert"|"corr"|"corr_k"|"corr_s"|"cos"|"cosh"|"covar_pop"|"covar_samp"|"cume_dist"|"current_date"|"current_timestamp"|"cv"|"dbtimezone"|"dbtmezone"|"decode"|"decompose"|"deletexml"|"depth"|"deref"|"empty_blob"|"empty_clob"|"existsnode"|"exp"|"extract"|"extractvalue"|"first_value"|"floor"|"from_tz"|"greatest"|"group_id"|"grouping"|"grouping_id"|"hextoraw"|"initcap"|"insertchildxml"|"insertchildxmlafter"|"insertchildxmlbefore"|"insertxmlafter"|"insertxmlbefore"|"instr"|"instr2"|"instr4"|"instrb"|"instrc"|"iteration_number"|"json_array"|"json_arrayagg"|"json_dataguide"|"json_object"|"json_objectagg"|"json_query"|"json_table"|"json_value"|"lag"|"last_day"|"last_value"|"lateral"|"lead"|"least"|"length"|"length2"|"length4"|"lengthb"|"lengthc"|"listagg"|"ln"|"lnnvl"|"localtimestamp"|"lower"|"lpad"|"ltrim"|"make_ref"|"max"|"median"|"min"|"mod"|"months_between"|"nanvl"|"nchr"|"new_time"|"next_day"|"nls_charset_decl_len"|"nls_charset_id"|"nls_charset_name"|"nls_initcap"|"nls_lower"|"nls_upper"|"nlssort"|"ntile"|"nullif"|"numtodsinterval"|"numtoyminterval"|"nvl"|"nvl2"|"ora_hash"|"percent_rank"|"percentile_cont"|"percentile_disc"|"powermultiset"|"powermultiset_by_cardinality"|"presentnnv"|"presentv"|"previous"|"rank"|"ratio_to_report"|"rawtohex"|"rawtonhex"|"reftohex"|"regexp_instr"|"regexp_replace"|"regexp_substr"|"regr_avgx"|"regr_avgy"|"regr_count"|"regr_intercept"|"regr_r2"|"regr_slope"|"regr_sxx"|"regr_sxy"|"regr_syy"|"round"|"row_number"|"rowidtochar"|"rowidtonchar"|"rpad"|"rtrim"|"scn_to_timestamp"|"sessiontimezone"|"sign"|"sin"|"sinh"|"soundex"|"sqrt"|"stats_binomial_test"|"stats_crosstab"|"stats_f_test"|"stats_ks_test"|"stats_mode"|"stats_mw_test"|"stats_one_way_anova"|"stats_t_test_indep"|"stats_t_test_indepu"|"stats_t_test_one"|"stats_t_test_paired"|"stats_wsr_test"|"stddev"|"stddev_pop"|"stddev_samp"|"substr"|"substr2"|"substr4"|"substrb"|"substrc"|"sum"|"sys_connect_by_path"|"sys_context"|"sys_dburigen"|"sys_extract_utc"|"sys_guid"|"sys_typeid"|"sys_xmlagg"|"sys_xmlgen"|"sysdate"|"systimestamp"|"tan"|"tanh"|"timestamp_to_scn"|"to_binary_double"|"to_binary_float"|"to_char"|"to_clob"|"to_date"|"to_dsinterval"|"to_lob"|"to_multi_byte"|"to_nchar"|"to_nclob"|"to_number"|"to_single_byte"|"to_timestamp"|"to_timestamp_tz"|"to_yminterval"|"translate"|"treat"|"trim"|"trunc"|"tz_offset"|"uid"|"unistr"|"updatexml"|"upper"|"userenv"|"var_pop"|"var_samp"|"variance"|"vsize"|"width_bucket"|"xmlagg"|"xmlattributes"|"xmlcast"|"xmlcdata"|"xmlcolattval"|"xmlcomment"|"xmlconcat"|"xmldiff"|"xmlelement"|"xmlforest"|"xmlisvalid"|"xmlparse"|"xmlpatch"|"xmlpi"|"xmlquery"|"xmlroot"|"xmlsequence"|"xmlserialize"|"xmltable"|"xmltransform"
PLSQL_PARAMETER   = "composite_limit"|"connect_time"|"cpu_per_call"|"cpu_per_session"|"create_stored_outlines"|"current_schema"|"cursor_sharing"|"db_block_checking"|"db_create_file_dest"|"db_create_online_log_dest_n"|"db_file_multiblock_read_count"|"db_file_name_convert"|"ddl_wait_for_locks"|"error_on_overlap_time"|"failed_login_attempts"|"filesystemio_options"|"flagger"|"global_names"|"hash_area_size"|"idle_time"|"instance"|"isolation_level"|"log_archive_dest_n"|"log_archive_dest_state_n"|"log_archive_min_succeed_dest"|"logical_reads_per_call"|"logical_reads_per_session"|"max_dump_file_size"|"nls_calendar"|"nls_comp"|"nls_currency"|"nls_date_format"|"nls_date_language"|"nls_dual_currency"|"nls_iso_currency"|"nls_language"|"nls_length_semantics"|"nls_nchar_conv_excp"|"nls_numeric_characters"|"nls_sort"|"nls_territory"|"nls_timestamp_format"|"nls_timestamp_tz_format"|"object_cache_max_size_percent"|"object_cache_optimal_size"|"olap_page_pool_size"|"optimizer_dynamic_sampling"|"optimizer_features_enable"|"optimizer_index_caching"|"optimizer_index_cost_adj"|"optimizer_mode"|"osm_power_limit"|"parallel_instance_group"|"parallel_min_percent"|"password_grace_time"|"password_life_time"|"password_lock_time"|"password_reuse_max"|"password_reuse_time"|"password_verify_function"|"plsql_code_type"|"plsql_compiler_flags"|"plsql_debug"|"plsql_optimize_level"|"plsql_v2_compatibility"|"plsql_warnings"|"private_sga"|"query_rewrite_enabled"|"query_rewrite_integrity"|"remote_dependencies_mode"|"resumable_timeout"|"session_cached_cursors"|"sessions_per_user"|"skip_unusable_indexes"|"sort_area_retained_size"|"sort_area_size"|"sql_trace"|"sqltune_category"|"star_transformation_enabled"|"statistics_level"|"timed_os_statistics"|"timed_statistics"|"tracefile_identifier"|"use_private_outlines"|"using_nls_comp"|"use_stored_outlines"|"workarea_size_policy"
PLSQL_DATA_TYPE   = "varchar2"|"with"{ws}"time"{ws}"zone"|"with"{ws}"local"{ws}"time"{ws}"zone"|"varchar"|"urowid"|"to"{ws}"second"|"to"{ws}"month"|"timestamp"|"string"|"smallint"|"rowid"|"real"|"raw"|"pls_integer"|"nvarchar2"|"numeric"|"number"|"nclob"|"nchar"{ws}"varying"|"nchar"|"national"{ws}"character"{ws}"varying"|"national"{ws}"character"|"national"{ws}"char"{ws}"varying"|"national"{ws}"char"|"long"{ws}"varchar"|"long"{ws}"raw"|"long"|"integer"|"int"|"float"|"double"{ws}"precision"|"decimal"|"date"|"clob"|"character"{ws}"varying"|"character"|"char"|"byte"|"boolean"|"blob"|"binary_integer"|"binary_float"|"binary_double"|"bfile"
PLSQL_EXCEPTION   = "access_into_null"|"case_not_found"|"collection_is_null"|"cursor_already_open"|"dup_val_on_index"|"invalid_cursor"|"invalid_number"|"login_denied"|"no_data_found"|"not_logged_on"|"program_error"|"rowtype_mismatch"|"self_is_null"|"storage_error"|"subscript_beyond_count"|"subscript_outside_limit"|"sys_invalid_rowid"|"timeout_on_resource"|"too_many_rows"|"value_error"|"zero_divide"


%state SQL, PLSQL
%%


<YYINITIAL> {
    {WHITE_SPACE}+       { return tt.getSharedTokenTypes().getWhiteSpace(); }
    {VARIABLE}           {return tt.getSharedTokenTypes().getVariable(); }
    {SQLP_VARIABLE}      {return tt.getSharedTokenTypes().getVariable(); }

    {BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
    {LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
    {REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

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
    //{VARIABLE}           {return tt.getSharedTokenTypes().getVariable(); }
    {SQLP_VARIABLE}      {return tt.getSharedTokenTypes().getVariable(); }


    {BLOCK_COMMENT}     { return tt.getSharedTokenTypes().getBlockComment(); }
    {LINE_COMMENT}      { return tt.getSharedTokenTypes().getLineComment(); }
    {REM_LINE_COMMENT}  { return tt.getSharedTokenTypes().getLineComment(); }

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
