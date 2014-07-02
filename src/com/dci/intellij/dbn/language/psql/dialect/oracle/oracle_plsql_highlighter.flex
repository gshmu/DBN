package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;

%%

%class OraclePLSQLHighlighterFlexLexer
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
    public OraclePLSQLHighlighterFlexLexer(TokenTypeBundle tt) {
        this.tt = tt;
    }
%}

WHITE_SPACE= {white_space_char}|{line_terminator}
line_terminator = \r|\n|\r\n
input_character = [^\r\n]
white_space = [ \t\f]
white_space_char= [ \n\r\t\f]
ws  = {WHITE_SPACE}+
wso = {WHITE_SPACE}*

block_comment_end =([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
BLOCK_COMMENT=("/*"[^]{block_comment_end})|"/*"
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

operator_equals             = "="
operator_not_equals         = (("!"|"^"|"�"){wso}"=")|("<"{wso}">")
operator_greater_than       = ">"
operator_greater_equal_than = ">"{wso}"="
operator_less_than          = "<"
operator_less_equal_than    = ">"{wso}"="
OPERATOR                    = {operator_equals}|{operator_not_equals}|{operator_greater_than}|{operator_greater_equal_than}|{operator_less_than}|{operator_less_equal_than}


KEYWORD     = "a set"|"all"|"alter"|"and"|"any"|"array"|"as"|"asc"|"at"|"authid"|"automatic"|"autonomous_transaction"|"begin"|"between"|"block"|"body"|"both"|"bulk"|"bulk_exceptions"|"bulk_rowcount"|"by"|"canonical"|"case"|"char_base"|"char_cs"|"check"|"chisq_df"|"chisq_obs"|"chisq_sig"|"close"|"cluster"|"coalesce"|"coefficient"|"cohens_k"|"collect"|"comment"|"commit"|"committed"|"compatibility"|"compress"|"connect"|"constant"|"constraint"|"cont_coefficient"|"count"|"cramers_v"|"create"|"cross"|"cube"|"current"|"current_user"|"currval"|"cursor"|"day"|"declare"|"decrement"|"default"|"definer"|"delete"|"dense_rank"|"desc"|"deterministic"|"df"|"df_between"|"df_den"|"df_num"|"df_within"|"dimension"|"distinct"|"do"|"drop"|"dump"|"else"|"elsif"|"empty"|"end"|"equals_path"|"error_code"|"error_index"|"errors"|"escape"|"exact_prob"|"exception"|"exception_init"|"exceptions"|"exclusive"|"execute"|"exists"|"exit"|"extend"|"extends"|"f_ratio"|"fetch"|"first"|"following"|"for"|"forall"|"found"|"from"|"full"|"function"|"goto"|"group"|"having"|"heap"|"hour"|"if"|"ignore"|"immediate"|"in"|"in"{ws}"out"|"increment"|"index"|"indicator"|"indices"|"infinite"|"inner"|"insert"|"interface"|"intersect"|"interval"|"into"|"is"|"isolation"|"isopen"|"iterate"|"java"|"join"|"keep"|"last"|"leading"|"left"|"level"|"like"|"like2"|"like4"|"likec"|"limit"|"limited"|"local"|"lock"|"log"|"loop"|"main"|"matched"|"maxvalue"|"mean_squares_between"|"mean_squares_within"|"measures"|"member"|"merge"|"minus"|"minute"|"minvalue"|"mlslabel"|"mode"|"model"|"month"|"multiset"|"name"|"nan"|"natural"|"naturaln"|"nav"|"nchar_cs"|"new"|"next"|"nextval"|"nocopy"|"nocycle"|"not"|"notfound"|"nowait"|"null"|"nulls"|"number_base"|"ocirowid"|"of"|"on"|"one_sided_prob_or_less"|"one_sided_prob_or_more"|"one_sided_sig"|"only"|"opaque"|"open"|"operator"|"option"|"or"|"order"|"organization"|"others"|"out"|"outer"|"over"|"package"|"parallel_enable"|"partition"|"pctfree"|"phi_coefficient"|"positive"|"positiven"|"power"|"pragma"|"preceding"|"present"|"prior"|"private"|"procedure"|"public"|"raise"|"range"|"rank"|"read"|"record"|"ref"|"reference"|"regexp_like"|"release"|"remainder"|"replace"|"restrict_references"|"return"|"returning"|"reject"|"reverse"|"right"|"rnds"|"rnps"|"rollback"|"rollup"|"row"|"rowcount"|"rownum"|"rows"|"rowtype"|"rules"|"sample"|"save"|"savepoint"|"scn"|"second"|"seed"|"segment"|"select"|"separate"|"sequential"|"serializable"|"serially_reusable"|"set"|"sets"|"share"|"siblings"|"sig"|"single"|"some"|"space"|"sql"|"sqlcode"|"sqlerrm"|"start"|"statistic"|"submultiset"|"subpartition"|"subtype"|"successful"|"sum_squares_between"|"sum_squares_within"|"synonym"|"table"|"then"|"time"|"timezone_abbr"|"timezone_hour"|"timezone_minute"|"timezone_region"|"to"|"trailing"|"transaction"|"trigger"|"trust"|"two_sided_prob"|"two_sided_sig"|"type"|"u_statistic"|"unbounded"|"under_path"|"union"|"unique"|"unlimited"|"until"|"update"|"updated"|"upsert"|"use"|"user"|"using"|"validate"|"value"|"values"|"varray"|"varying"|"versions"|"view"|"wait"|"when"|"whenever"|"where"|"while"|"with"|"within"|"wnds"|"wnps"|"work"|"write"|"year"|"zone"|"false"|"true"
FUNCTION    = "abs"|"acos"|"add_months"|"ascii"|"asciistr"|"asin"|"atan"|"atan2"|"avg"|"bfilename"|"bin_to_num"|"bitand"|"cardinality"|"cast"|"ceil"|"chartorowid"|"chr"|"compose"|"concat"|"convert"|"corr"|"corr_k"|"corr_s"|"cos"|"cosh"|"covar_pop"|"covar_samp"|"cume_dist"|"current_date"|"current_timestamp"|"cv"|"dbtimezone"|"dbtmezone"|"decode"|"decompose"|"depth"|"deref"|"empty_blob"|"empty_clob"|"existsnode"|"exp"|"extract"|"extractvalue"|"first_value"|"floor"|"from_tz"|"greatest"|"group_id"|"grouping"|"grouping_id"|"hextoraw"|"initcap"|"instr"|"instr2"|"instr4"|"instrb"|"instrc"|"iteration_number"|"lag"|"last_day"|"last_value"|"lead"|"least"|"length"|"length2"|"length4"|"lengthb"|"lengthc"|"ln"|"lnnvl"|"localtimestamp"|"lower"|"lpad"|"ltrim"|"make_ref"|"max"|"median"|"min"|"mod"|"months_between"|"nanvl"|"nchr"|"new_time"|"next_day"|"nls_charset_decl_len"|"nls_charset_id"|"nls_charset_name"|"nls_initcap"|"nls_lower"|"nls_upper"|"nlssort"|"ntile"|"nullif"|"numtodsinterval"|"numtoyminterval"|"nvl"|"nvl2"|"ora_hash"|"path"|"percent_rank"|"percentile_cont"|"percentile_disc"|"powermultiset"|"powermultiset_by_cardinality"|"presentnnv"|"presentv"|"previous"|"ratio_to_report"|"rawtohex"|"rawtonhex"|"reftohex"|"regexp_instr"|"regexp_replace"|"regexp_substr"|"regr_avgx"|"regr_avgy"|"regr_count"|"regr_intercept"|"regr_r2"|"regr_slope"|"regr_sxx"|"regr_sxy"|"regr_syy"|"round"|"row_number"|"rowidtochar"|"rowidtonchar"|"rpad"|"rtrim"|"scn_to_timestamp"|"sessiontimezone"|"sign"|"sin"|"sinh"|"soundex"|"sqrt"|"stats_binomial_test"|"stats_crosstab"|"stats_f_test"|"stats_ks_test"|"stats_mode"|"stats_mw_test"|"stats_one_way_anova"|"stats_t_test_indep"|"stats_t_test_indepu"|"stats_t_test_one"|"stats_t_test_paired"|"stats_wsr_test"|"stddev"|"stddev_pop"|"stddev_samp"|"substr"|"substr2"|"substr4"|"substrb"|"substrc"|"sum"|"sys_connect_by_path"|"sys_context"|"sys_dburigen"|"sys_extract_utc"|"sys_guid"|"sys_typeid"|"sys_xmlagg"|"sys_xmlgen"|"sysdate"|"systimestamp"|"tan"|"tanh"|"timestamp_to_scn"|"to_binary_double"|"to_binary_float"|"to_char"|"to_clob"|"to_date"|"to_dsinterval"|"to_lob"|"to_multi_byte"|"to_nchar"|"to_nclob"|"to_number"|"to_single_byte"|"to_timestamp"|"to_timestamp_tz"|"to_yminterval"|"translate"|"treat"|"trim"|"trunc"|"tz_offset"|"uid"|"unistr"|"updatexml"|"upper"|"userenv"|"var_pop"|"var_samp"|"variance"|"vsize"|"width_bucket"|"xmlagg"|"xmlcolattval"|"xmlconcat"|"xmlforest"|"xmlsequence"|"xmltransform"
PARAMETER   = "composite_limit"|"connect_time"|"cpu_per_call"|"cpu_per_session"|"create_stored_outlines"|"current_schema"|"cursor_sharing"|"db_block_checking"|"db_create_file_dest"|"db_create_online_log_dest_n"|"db_file_multiblock_read_count"|"db_file_name_convert"|"ddl_wait_for_locks"|"error_on_overlap_time"|"failed_login_attempts"|"filesystemio_options"|"flagger"|"global_names"|"hash_area_size"|"idle_time"|"instance"|"isolation_level"|"log_archive_dest_n"|"log_archive_dest_state_n"|"log_archive_min_succeed_dest"|"logical_reads_per_call"|"logical_reads_per_session"|"max_dump_file_size"|"nls_calendar"|"nls_comp"|"nls_currency"|"nls_date_format"|"nls_date_language"|"nls_dual_currency"|"nls_iso_currency"|"nls_language"|"nls_length_semantics"|"nls_nchar_conv_excp"|"nls_numeric_characters"|"nls_sort"|"nls_territory"|"nls_timestamp_format"|"nls_timestamp_tz_format"|"object_cache_max_size_percent"|"object_cache_optimal_size"|"olap_page_pool_size"|"optimizer_dynamic_sampling"|"optimizer_features_enable"|"optimizer_index_caching"|"optimizer_index_cost_adj"|"optimizer_mode"|"osm_power_limit"|"parallel_instance_group"|"parallel_min_percent"|"password_grace_time"|"password_life_time"|"password_lock_time"|"password_reuse_max"|"password_reuse_time"|"password_verify_function"|"plsql_code_type"|"plsql_compiler_flags"|"plsql_debug"|"plsql_optimize_level"|"plsql_v2_compatibility"|"plsql_warnings"|"private_sga"|"query_rewrite_enabled"|"query_rewrite_integrity"|"remote_dependencies_mode"|"resumable_timeout"|"session_cached_cursors"|"sessions_per_user"|"skip_unusable_indexes"|"sort_area_retained_size"|"sort_area_size"|"sql_trace"|"sqltune_category"|"star_transformation_enabled"|"statistics_level"|"timed_os_statistics"|"timed_statistics"|"tracefile_identifier"|"use_private_outlines"|"use_stored_outlines"|"workarea_size_policy"
DATA_TYPE   = "varchar2"|"with"{ws}"time"{ws}"zone"|"with"{ws}"local"{ws}"time"{ws}"zone"|"varchar"|"urowid"|"to"{ws}"second"|"to"{ws}"month"|"timestamp"|"string"|"smallint"|"rowid"|"real"|"raw"|"pls_integer"|"nvarchar2"|"numeric"|"number"|"nclob"|"nchar"{ws}"varying"|"nchar"|"national"{ws}"character"{ws}"varying"|"national"{ws}"character"|"national"{ws}"char"{ws}"varying"|"national"{ws}"char"|"long"{ws}"varchar"|"long"{ws}"raw"|"long"|"interval"{ws}"year"|"interval"{ws}"day"|"integer"|"int"|"float"|"double"{ws}"precision"|"decimal"|"date"|"clob"|"character"{ws}"varying"|"character"|"char"|"byte"|"boolean"|"blob"|"binary_integer"|"binary_float"|"binary_double"|"bfile"
EXCEPTION   = "access_into_null"|"case_not_found"|"collection_is_null"|"cursor_already_open"|"dup_val_on_index"|"invalid_cursor"|"invalid_number"|"login_denied"|"no_data_found"|"not_logged_on"|"program_error"|"rowtype_mismatch"|"self_is_null"|"storage_error"|"subscript_beyond_count"|"subscript_outside_limit"|"sys_invalid_rowid"|"timeout_on_resource"|"too_many_rows"|"value_error"|"zero_divide"


%state DIV
%%

{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getTokenType("BLOCK_COMMENT"); }
{LINE_COMMENT}       { return tt.getTokenType("LINE_COMMENT"); }
{REM_LINE_COMMENT}       { return tt.getTokenType("LINE_COMMENT"); }

{INTEGER}     { return tt.getTokenType("INTEGER"); }
{NUMBER}      { return tt.getTokenType("NUMBER"); }
{STRING}      { return tt.getTokenType("STRING"); }

{FUNCTION}             { yybegin(YYINITIAL); return tt.getTokenType("FUNCTION");}
{PARAMETER}            { yybegin(YYINITIAL); return tt.getTokenType("PARAMETER");}
{EXCEPTION}            { yybegin(YYINITIAL); return tt.getTokenType("EXCEPTION");}

{DATA_TYPE}            { yybegin(YYINITIAL); return tt.getTokenType("DATA_TYPE"); }
{KEYWORD}              { yybegin(YYINITIAL); return tt.getTokenType("KEYWORD"); }
{OPERATOR}             { yybegin(YYINITIAL); return tt.getTokenType("OPERATOR"); }


{IDENTIFIER}           { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}    { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getQuotedIdentifier(); }


"("                    { return tt.getTokenType("CHR_LEFT_PARENTHESIS"); }
")"                    { return tt.getTokenType("CHR_RIGHT_PARENTHESIS"); }
"["                    { return tt.getTokenType("CHR_LEFT_BRACKET"); }
"]"                    { return tt.getTokenType("CHR_RIGHT_BRACKET"); }

<YYINITIAL> {
    .                  { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
}
