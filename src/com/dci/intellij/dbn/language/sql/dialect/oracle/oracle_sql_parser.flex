package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.sql.dialect.oracle.OraclePLSQLBlockMonitor.Marker;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%class OracleSQLParserFlexLexer
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
            public OracleSQLParserFlexLexer(TokenTypeBundle tt) {
                this.tt = tt;
            }

            private int blockNesting = 0;
            private int blockStartPos = 0;

            public IElementType getChameleon() {
                return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);
            }

            OraclePLSQLBlockMonitor plsqlBlockMonitor = new OraclePLSQLBlockMonitor() {
                @Override protected void lexerStart() {
                    //yypushback(yylength());
                    yybegin(PSQL_BLOCK);
                    blockStartPos = zzStartRead;
                }
                @Override protected void lexerEnd() {
                    yybegin(YYINITIAL);
                    zzStartRead = blockStartPos;
                }
            };

%}

NON_PSQL_BLOCK_ENTER = ("grant"|"revoke"){ws}"create"
NON_PSQL_BLOCK_EXIT = "to"|"from"|";"

PSQL_BLOCK_START_CREATE = "create"({ws}"or"{ws}"replace")?{ws}("package"|"trigger"|"function"|"procedure"|"type")
PSQL_BLOCK_START_DECLARE = "declare"
PSQL_BLOCK_START_BEGIN = "begin"
PSQL_BLOCK_END_IGNORE = "end"{ws}("if"|"loop"|"case")({ws}({IDENTIFIER}|{QUOTED_IDENTIFIER}))*{wso}";"
PSQL_BLOCK_END = "end"({ws}({IDENTIFIER}|{QUOTED_IDENTIFIER}))*{wso}(";"({wso}"/")?)?

WHITE_SPACE= {white_space_char}|{line_terminator}
line_terminator = \r|\n|\r\n
input_character = [^\r\n]
white_space = [ \t\f]
white_space_char = [ \n\r\t\f]
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
SQLP_VARIABLE = "&""&"?({IDENTIFIER}|{INTEGER})
VARIABLE_IDENTIFIER={IDENTIFIER}"&""&"?({IDENTIFIER}|{INTEGER})

CT_SIZE_CLAUSE = {INTEGER}{wso}("k"|"m"|"g"|"t"|"p"|"e"){ws}

%state PSQL_BLOCK
%state NON_PSQL_BLOCK
%%

<PSQL_BLOCK> {
    {BLOCK_COMMENT}                 {}
    {LINE_COMMENT}                  {}
    {STRING}                        {}

    {PSQL_BLOCK_START_CREATE}       { if (blockStartPos < zzCurrentPos) {yypushback(yylength()); plsqlBlockMonitor.end(true); return getChameleon();}}
    {PSQL_BLOCK_END_IGNORE}         {}
    {PSQL_BLOCK_END}                { if (plsqlBlockMonitor.end(false)) return getChameleon();}

    "begin"                         { plsqlBlockMonitor.mark(Marker.BEGIN); }
    "function"{ws}{IDENTIFIER}      { plsqlBlockMonitor.mark(Marker.METHOD); }
    "procedure"{ws}{IDENTIFIER}     { plsqlBlockMonitor.mark(Marker.METHOD); }
    "case"                          { plsqlBlockMonitor.mark(Marker.CASE); }
    "end"                           { plsqlBlockMonitor.end(false);}
    {IDENTIFIER}                    {}
    {INTEGER}                       {}
    {NUMBER}                        {}
    {WHITE_SPACE}+                  {}
    \n|\r|.                         {}
    <<EOF>>                         { plsqlBlockMonitor.end(true); return getChameleon(); }
}

<NON_PSQL_BLOCK> {
    {NON_PSQL_BLOCK_EXIT}          { yybegin(YYINITIAL); yypushback(yylength()); }
}


<YYINITIAL> {
    {NON_PSQL_BLOCK_ENTER}         { yybegin(NON_PSQL_BLOCK); yypushback(yylength()); }

    {PSQL_BLOCK_START_CREATE}      { plsqlBlockMonitor.start(Marker.CREATE); }
    {PSQL_BLOCK_START_DECLARE}     { plsqlBlockMonitor.start(Marker.DECLARE); }
    {PSQL_BLOCK_START_BEGIN}       { plsqlBlockMonitor.start(Marker.BEGIN); }
}

<YYINITIAL, NON_PSQL_BLOCK> {

{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
{LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
{REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

//{PSQL_BLOCK_START_CREATE}          { plsqlBlockMonitor.start(Marker.CREATE); }
//{PSQL_BLOCK_START_DECLARE}         { plsqlBlockMonitor.start(Marker.DECLARE); }
//{PSQL_BLOCK_START_BEGIN}           { plsqlBlockMonitor.start(Marker.BEGIN); }

{VARIABLE}             { return tt.getSharedTokenTypes().getVariable(); }
{VARIABLE_IDENTIFIER}  { return tt.getSharedTokenTypes().getIdentifier(); }
{SQLP_VARIABLE}        { return tt.getSharedTokenTypes().getVariable(); }

"("{wso}"+"{wso}")"  {return tt.getTokenType("CT_OUTER_JOIN");}


"="{wso}"=" {return tt.getOperatorTokenType(0);}
"|"{wso}"|" {return tt.getOperatorTokenType(1);}
"<"{wso}"=" {return tt.getOperatorTokenType(2);}
">"{wso}"=" {return tt.getOperatorTokenType(3);}
"<"{wso}">" {return tt.getOperatorTokenType(4);}
"!"{wso}"=" {return tt.getOperatorTokenType(5);}
":"{wso}"=" {return tt.getOperatorTokenType(6);}
"="{wso}">" {return tt.getOperatorTokenType(7);}
".."        {return tt.getOperatorTokenType(8);}
"::"        {return tt.getOperatorTokenType(9);}


"@" {return tt.getCharacterTokenType(0);}
":" {return tt.getCharacterTokenType(1);}
"," {return tt.getCharacterTokenType(2);}
"." {return tt.getCharacterTokenType(3);}
"=" {return tt.getCharacterTokenType(4);}
"!" {return tt.getCharacterTokenType(5);}
">" {return tt.getCharacterTokenType(6);}
"#" {return tt.getCharacterTokenType(7);}
"[" {return tt.getCharacterTokenType(8);}
"{" {return tt.getCharacterTokenType(9);}
"(" {return tt.getCharacterTokenType(10);}
"<" {return tt.getCharacterTokenType(11);}
"-" {return tt.getCharacterTokenType(12);}
"%" {return tt.getCharacterTokenType(13);}
"+" {return tt.getCharacterTokenType(14);}
"]" {return tt.getCharacterTokenType(15);}
"}" {return tt.getCharacterTokenType(16);}
")" {return tt.getCharacterTokenType(17);}
";" {return tt.getCharacterTokenType(18);}
"/" {return tt.getCharacterTokenType(19);}
"*" {return tt.getCharacterTokenType(20);}
"|" {return tt.getCharacterTokenType(21);}



"varchar2" {return tt.getDataTypeTokenType(0);}
"bfile" {return tt.getDataTypeTokenType(1);}
"binary_double" {return tt.getDataTypeTokenType(2);}
"binary_float" {return tt.getDataTypeTokenType(3);}
"blob" {return tt.getDataTypeTokenType(4);}
"boolean" {return tt.getDataTypeTokenType(5);}
"byte" {return tt.getDataTypeTokenType(6);}
"char" {return tt.getDataTypeTokenType(7);}
"character" {return tt.getDataTypeTokenType(8);}
"character"{ws}"varying" {return tt.getDataTypeTokenType(9);}
"clob" {return tt.getDataTypeTokenType(10);}
"date" {return tt.getDataTypeTokenType(11);}
"decimal" {return tt.getDataTypeTokenType(12);}
"double"{ws}"precision" {return tt.getDataTypeTokenType(13);}
"float" {return tt.getDataTypeTokenType(14);}
"int" {return tt.getDataTypeTokenType(15);}
"integer" {return tt.getDataTypeTokenType(16);}
"interval" {return tt.getDataTypeTokenType(17);}
"long" {return tt.getDataTypeTokenType(18);}
"long"{ws}"raw" {return tt.getDataTypeTokenType(19);}
"long"{ws}"varchar" {return tt.getDataTypeTokenType(20);}
"national"{ws}"char" {return tt.getDataTypeTokenType(21);}
"national"{ws}"char"{ws}"varying" {return tt.getDataTypeTokenType(22);}
"national"{ws}"character" {return tt.getDataTypeTokenType(23);}
"national"{ws}"character"{ws}"varying" {return tt.getDataTypeTokenType(24);}
"nchar" {return tt.getDataTypeTokenType(25);}
"nchar"{ws}"varying" {return tt.getDataTypeTokenType(26);}
"nclob" {return tt.getDataTypeTokenType(27);}
"number" {return tt.getDataTypeTokenType(28);}
"numeric" {return tt.getDataTypeTokenType(29);}
"nvarchar2" {return tt.getDataTypeTokenType(30);}
"raw" {return tt.getDataTypeTokenType(31);}
"real" {return tt.getDataTypeTokenType(32);}
"rowid" {return tt.getDataTypeTokenType(33);}
"smallint" {return tt.getDataTypeTokenType(34);}
"timestamp" {return tt.getDataTypeTokenType(35);}
"urowid" {return tt.getDataTypeTokenType(36);}
"varchar" {return tt.getDataTypeTokenType(37);}
"with"{ws}"local"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(38);}
"with"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(39);}



"a set" {return tt.getKeywordTokenType(0);}
"abort" {return tt.getKeywordTokenType(1);}
"absent" {return tt.getKeywordTokenType(2);}
"access" {return tt.getKeywordTokenType(3);}
"account" {return tt.getKeywordTokenType(4);}
"activate" {return tt.getKeywordTokenType(5);}
"active" {return tt.getKeywordTokenType(6);}
"add" {return tt.getKeywordTokenType(7);}
"admin" {return tt.getKeywordTokenType(8);}
"administer" {return tt.getKeywordTokenType(9);}
"advise" {return tt.getKeywordTokenType(10);}
"advisor" {return tt.getKeywordTokenType(11);}
"after" {return tt.getKeywordTokenType(12);}
"alias" {return tt.getKeywordTokenType(13);}
"all" {return tt.getKeywordTokenType(14);}
"allocate" {return tt.getKeywordTokenType(15);}
"allow" {return tt.getKeywordTokenType(16);}
"alter" {return tt.getKeywordTokenType(17);}
"always" {return tt.getKeywordTokenType(18);}
"analyze" {return tt.getKeywordTokenType(19);}
"ancillary" {return tt.getKeywordTokenType(20);}
"and" {return tt.getKeywordTokenType(21);}
"any" {return tt.getKeywordTokenType(22);}
"apply" {return tt.getKeywordTokenType(23);}
"archive" {return tt.getKeywordTokenType(24);}
"archivelog" {return tt.getKeywordTokenType(25);}
"array" {return tt.getKeywordTokenType(26);}
"as" {return tt.getKeywordTokenType(27);}
"asc" {return tt.getKeywordTokenType(28);}
"asynchronous" {return tt.getKeywordTokenType(29);}
"assembly" {return tt.getKeywordTokenType(30);}
"at" {return tt.getKeywordTokenType(31);}
"attribute" {return tt.getKeywordTokenType(32);}
"attributes" {return tt.getKeywordTokenType(33);}
"audit" {return tt.getKeywordTokenType(34);}
"authid" {return tt.getKeywordTokenType(35);}
"authentication" {return tt.getKeywordTokenType(36);}
"auto" {return tt.getKeywordTokenType(37);}
"autoextend" {return tt.getKeywordTokenType(38);}
"automatic" {return tt.getKeywordTokenType(39);}
"availability" {return tt.getKeywordTokenType(40);}
"backup" {return tt.getKeywordTokenType(41);}
"become" {return tt.getKeywordTokenType(42);}
"before" {return tt.getKeywordTokenType(43);}
"begin" {return tt.getKeywordTokenType(44);}
"beginning" {return tt.getKeywordTokenType(45);}
"bequeath" {return tt.getKeywordTokenType(46);}
"between" {return tt.getKeywordTokenType(47);}
"bigfile" {return tt.getKeywordTokenType(48);}
"binding" {return tt.getKeywordTokenType(49);}
"bitmap" {return tt.getKeywordTokenType(50);}
"block" {return tt.getKeywordTokenType(51);}
"body" {return tt.getKeywordTokenType(52);}
"both" {return tt.getKeywordTokenType(53);}
"buffer_cache" {return tt.getKeywordTokenType(54);}
"buffer_pool" {return tt.getKeywordTokenType(55);}
"build" {return tt.getKeywordTokenType(56);}
"by" {return tt.getKeywordTokenType(57);}
"cache" {return tt.getKeywordTokenType(58);}
"cancel" {return tt.getKeywordTokenType(59);}
"canonical" {return tt.getKeywordTokenType(60);}
"capacity" {return tt.getKeywordTokenType(61);}
"cascade" {return tt.getKeywordTokenType(62);}
"case" {return tt.getKeywordTokenType(63);}
"category" {return tt.getKeywordTokenType(64);}
"change" {return tt.getKeywordTokenType(65);}
"char_cs" {return tt.getKeywordTokenType(66);}
"check" {return tt.getKeywordTokenType(67);}
"checkpoint" {return tt.getKeywordTokenType(68);}
"child" {return tt.getKeywordTokenType(69);}
"chisq_df" {return tt.getKeywordTokenType(70);}
"chisq_obs" {return tt.getKeywordTokenType(71);}
"chisq_sig" {return tt.getKeywordTokenType(72);}
"chunk" {return tt.getKeywordTokenType(73);}
"class" {return tt.getKeywordTokenType(74);}
"clear" {return tt.getKeywordTokenType(75);}
"clone" {return tt.getKeywordTokenType(76);}
"close" {return tt.getKeywordTokenType(77);}
"cluster" {return tt.getKeywordTokenType(78);}
"coalesce" {return tt.getKeywordTokenType(79);}
"coarse" {return tt.getKeywordTokenType(80);}
"coefficient" {return tt.getKeywordTokenType(81);}
"cohens_k" {return tt.getKeywordTokenType(82);}
"collation" {return tt.getKeywordTokenType(83);}
"column" {return tt.getKeywordTokenType(84);}
"column_value" {return tt.getKeywordTokenType(85);}
"columns" {return tt.getKeywordTokenType(86);}
"comment" {return tt.getKeywordTokenType(87);}
"commit" {return tt.getKeywordTokenType(88);}
"committed" {return tt.getKeywordTokenType(89);}
"compact" {return tt.getKeywordTokenType(90);}
"compatibility" {return tt.getKeywordTokenType(91);}
"compile" {return tt.getKeywordTokenType(92);}
"complete" {return tt.getKeywordTokenType(93);}
"compress" {return tt.getKeywordTokenType(94);}
"computation" {return tt.getKeywordTokenType(95);}
"compute" {return tt.getKeywordTokenType(96);}
"conditional" {return tt.getKeywordTokenType(97);}
"connect" {return tt.getKeywordTokenType(98);}
"consider" {return tt.getKeywordTokenType(99);}
"consistent" {return tt.getKeywordTokenType(100);}
"constraint" {return tt.getKeywordTokenType(101);}
"constraints" {return tt.getKeywordTokenType(102);}
"cont_coefficient" {return tt.getKeywordTokenType(103);}
"container" {return tt.getKeywordTokenType(104);}
"container_map" {return tt.getKeywordTokenType(105);}
"containers_default" {return tt.getKeywordTokenType(106);}
"content" {return tt.getKeywordTokenType(107);}
"contents" {return tt.getKeywordTokenType(108);}
"context" {return tt.getKeywordTokenType(109);}
"continue" {return tt.getKeywordTokenType(110);}
"controlfile" {return tt.getKeywordTokenType(111);}
"conversion" {return tt.getKeywordTokenType(112);}
"corruption" {return tt.getKeywordTokenType(113);}
"cost" {return tt.getKeywordTokenType(114);}
"cramers_v" {return tt.getKeywordTokenType(115);}
"create" {return tt.getKeywordTokenType(116);}
"creation" {return tt.getKeywordTokenType(117);}
"critical" {return tt.getKeywordTokenType(118);}
"cross" {return tt.getKeywordTokenType(119);}
"cube" {return tt.getKeywordTokenType(120);}
"current" {return tt.getKeywordTokenType(121);}
"current_user" {return tt.getKeywordTokenType(122);}
"currval" {return tt.getKeywordTokenType(123);}
"cursor" {return tt.getKeywordTokenType(124);}
"cycle" {return tt.getKeywordTokenType(125);}
"data" {return tt.getKeywordTokenType(126);}
"database" {return tt.getKeywordTokenType(127);}
"datafile" {return tt.getKeywordTokenType(128);}
"datafiles" {return tt.getKeywordTokenType(129);}
"day" {return tt.getKeywordTokenType(130);}
"days" {return tt.getKeywordTokenType(131);}
"ddl" {return tt.getKeywordTokenType(132);}
"deallocate" {return tt.getKeywordTokenType(133);}
"debug" {return tt.getKeywordTokenType(134);}
"decrement" {return tt.getKeywordTokenType(135);}
"default" {return tt.getKeywordTokenType(136);}
"defaults" {return tt.getKeywordTokenType(137);}
"deferrable" {return tt.getKeywordTokenType(138);}
"deferred" {return tt.getKeywordTokenType(139);}
"definer" {return tt.getKeywordTokenType(140);}
"delay" {return tt.getKeywordTokenType(141);}
"delegate" {return tt.getKeywordTokenType(142);}
"delete" {return tt.getKeywordTokenType(143);}
"demand" {return tt.getKeywordTokenType(144);}
"dense_rank" {return tt.getKeywordTokenType(145);}
"dequeue" {return tt.getKeywordTokenType(146);}
"desc" {return tt.getKeywordTokenType(147);}
"determines" {return tt.getKeywordTokenType(148);}
"df" {return tt.getKeywordTokenType(149);}
"df_between" {return tt.getKeywordTokenType(150);}
"df_den" {return tt.getKeywordTokenType(151);}
"df_num" {return tt.getKeywordTokenType(152);}
"df_within" {return tt.getKeywordTokenType(153);}
"dictionary" {return tt.getKeywordTokenType(154);}
"digest" {return tt.getKeywordTokenType(155);}
"dimension" {return tt.getKeywordTokenType(156);}
"directory" {return tt.getKeywordTokenType(157);}
"disable" {return tt.getKeywordTokenType(158);}
"disconnect" {return tt.getKeywordTokenType(159);}
"disk" {return tt.getKeywordTokenType(160);}
"diskgroup" {return tt.getKeywordTokenType(161);}
"disks" {return tt.getKeywordTokenType(162);}
"dismount" {return tt.getKeywordTokenType(163);}
"distinct" {return tt.getKeywordTokenType(164);}
"distribute" {return tt.getKeywordTokenType(165);}
"distributed" {return tt.getKeywordTokenType(166);}
"dml" {return tt.getKeywordTokenType(167);}
"document" {return tt.getKeywordTokenType(168);}
"downgrade" {return tt.getKeywordTokenType(169);}
"drop" {return tt.getKeywordTokenType(170);}
"dump" {return tt.getKeywordTokenType(171);}
"duplicate" {return tt.getKeywordTokenType(172);}
"edition" {return tt.getKeywordTokenType(173);}
"editionable" {return tt.getKeywordTokenType(174);}
"editioning" {return tt.getKeywordTokenType(175);}
"element" {return tt.getKeywordTokenType(176);}
"else" {return tt.getKeywordTokenType(177);}
"empty" {return tt.getKeywordTokenType(178);}
"enable" {return tt.getKeywordTokenType(179);}
"encoding" {return tt.getKeywordTokenType(180);}
"encrypt" {return tt.getKeywordTokenType(181);}
"end" {return tt.getKeywordTokenType(182);}
"enforced" {return tt.getKeywordTokenType(183);}
"entityescaping" {return tt.getKeywordTokenType(184);}
"entry" {return tt.getKeywordTokenType(185);}
"equals_path" {return tt.getKeywordTokenType(186);}
"error" {return tt.getKeywordTokenType(187);}
"errors" {return tt.getKeywordTokenType(188);}
"escape" {return tt.getKeywordTokenType(189);}
"evalname" {return tt.getKeywordTokenType(190);}
"evaluate" {return tt.getKeywordTokenType(191);}
"evaluation" {return tt.getKeywordTokenType(192);}
"exact_prob" {return tt.getKeywordTokenType(193);}
"except" {return tt.getKeywordTokenType(194);}
"exceptions" {return tt.getKeywordTokenType(195);}
"exchange" {return tt.getKeywordTokenType(196);}
"exclude" {return tt.getKeywordTokenType(197);}
"excluding" {return tt.getKeywordTokenType(198);}
"exclusive" {return tt.getKeywordTokenType(199);}
"execute" {return tt.getKeywordTokenType(200);}
"exempt" {return tt.getKeywordTokenType(201);}
"exists" {return tt.getKeywordTokenType(202);}
"expire" {return tt.getKeywordTokenType(203);}
"explain" {return tt.getKeywordTokenType(204);}
"export" {return tt.getKeywordTokenType(205);}
"extended" {return tt.getKeywordTokenType(206);}
"extensions" {return tt.getKeywordTokenType(207);}
"extent" {return tt.getKeywordTokenType(208);}
"external" {return tt.getKeywordTokenType(209);}
"externally" {return tt.getKeywordTokenType(210);}
"f_ratio" {return tt.getKeywordTokenType(211);}
"failed" {return tt.getKeywordTokenType(212);}
"failgroup" {return tt.getKeywordTokenType(213);}
"fast" {return tt.getKeywordTokenType(214);}
"fetch" {return tt.getKeywordTokenType(215);}
"file" {return tt.getKeywordTokenType(216);}
"filesystem_like_logging" {return tt.getKeywordTokenType(217);}
"fine" {return tt.getKeywordTokenType(218);}
"finish" {return tt.getKeywordTokenType(219);}
"first" {return tt.getKeywordTokenType(220);}
"flashback" {return tt.getKeywordTokenType(221);}
"flush" {return tt.getKeywordTokenType(222);}
"folder" {return tt.getKeywordTokenType(223);}
"following" {return tt.getKeywordTokenType(224);}
"for" {return tt.getKeywordTokenType(225);}
"force" {return tt.getKeywordTokenType(226);}
"foreign" {return tt.getKeywordTokenType(227);}
"format" {return tt.getKeywordTokenType(228);}
"freelist" {return tt.getKeywordTokenType(229);}
"freelists" {return tt.getKeywordTokenType(230);}
"freepools" {return tt.getKeywordTokenType(231);}
"fresh" {return tt.getKeywordTokenType(232);}
"from" {return tt.getKeywordTokenType(233);}
"full" {return tt.getKeywordTokenType(234);}
"function" {return tt.getKeywordTokenType(235);}
"global" {return tt.getKeywordTokenType(236);}
"global_name" {return tt.getKeywordTokenType(237);}
"globally" {return tt.getKeywordTokenType(238);}
"grant" {return tt.getKeywordTokenType(239);}
"group" {return tt.getKeywordTokenType(240);}
"groups" {return tt.getKeywordTokenType(241);}
"guard" {return tt.getKeywordTokenType(242);}
"hash" {return tt.getKeywordTokenType(243);}
"having" {return tt.getKeywordTokenType(244);}
"heap" {return tt.getKeywordTokenType(245);}
"hide" {return tt.getKeywordTokenType(246);}
"hierarchy" {return tt.getKeywordTokenType(247);}
"high" {return tt.getKeywordTokenType(248);}
"history" {return tt.getKeywordTokenType(249);}
"hour" {return tt.getKeywordTokenType(250);}
"http" {return tt.getKeywordTokenType(251);}
"id" {return tt.getKeywordTokenType(252);}
"identified" {return tt.getKeywordTokenType(253);}
"identifier" {return tt.getKeywordTokenType(254);}
"ignore" {return tt.getKeywordTokenType(255);}
"ilm" {return tt.getKeywordTokenType(256);}
"immediate" {return tt.getKeywordTokenType(257);}
"import" {return tt.getKeywordTokenType(258);}
"in" {return tt.getKeywordTokenType(259);}
"include" {return tt.getKeywordTokenType(260);}
"including" {return tt.getKeywordTokenType(261);}
"increment" {return tt.getKeywordTokenType(262);}
"indent" {return tt.getKeywordTokenType(263);}
"index" {return tt.getKeywordTokenType(264);}
"indexes" {return tt.getKeywordTokenType(265);}
"indexing" {return tt.getKeywordTokenType(266);}
"indextype" {return tt.getKeywordTokenType(267);}
"infinite" {return tt.getKeywordTokenType(268);}
"initial" {return tt.getKeywordTokenType(269);}
"initially" {return tt.getKeywordTokenType(270);}
"initrans" {return tt.getKeywordTokenType(271);}
"inmemory" {return tt.getKeywordTokenType(272);}
"inner" {return tt.getKeywordTokenType(273);}
"insert" {return tt.getKeywordTokenType(274);}
"instance" {return tt.getKeywordTokenType(275);}
"intermediate" {return tt.getKeywordTokenType(276);}
"intersect" {return tt.getKeywordTokenType(277);}
"into" {return tt.getKeywordTokenType(278);}
"invalidate" {return tt.getKeywordTokenType(279);}
"invisible" {return tt.getKeywordTokenType(280);}
"is" {return tt.getKeywordTokenType(281);}
"iterate" {return tt.getKeywordTokenType(282);}
"java" {return tt.getKeywordTokenType(283);}
"job" {return tt.getKeywordTokenType(284);}
"join" {return tt.getKeywordTokenType(285);}
"json" {return tt.getKeywordTokenType(286);}
"keep" {return tt.getKeywordTokenType(287);}
"key" {return tt.getKeywordTokenType(288);}
"keys" {return tt.getKeywordTokenType(289);}
"kill" {return tt.getKeywordTokenType(290);}
"last" {return tt.getKeywordTokenType(291);}
"leading" {return tt.getKeywordTokenType(292);}
"left" {return tt.getKeywordTokenType(293);}
"less" {return tt.getKeywordTokenType(294);}
"level" {return tt.getKeywordTokenType(295);}
"levels" {return tt.getKeywordTokenType(296);}
"library" {return tt.getKeywordTokenType(297);}
"like" {return tt.getKeywordTokenType(298);}
"like2" {return tt.getKeywordTokenType(299);}
"like4" {return tt.getKeywordTokenType(300);}
"likec" {return tt.getKeywordTokenType(301);}
"limit" {return tt.getKeywordTokenType(302);}
"link" {return tt.getKeywordTokenType(303);}
"lob" {return tt.getKeywordTokenType(304);}
"local" {return tt.getKeywordTokenType(305);}
"location" {return tt.getKeywordTokenType(306);}
"locator" {return tt.getKeywordTokenType(307);}
"lock" {return tt.getKeywordTokenType(308);}
"lockdown" {return tt.getKeywordTokenType(309);}
"locked" {return tt.getKeywordTokenType(310);}
"log" {return tt.getKeywordTokenType(311);}
"logfile" {return tt.getKeywordTokenType(312);}
"logging" {return tt.getKeywordTokenType(313);}
"logical" {return tt.getKeywordTokenType(314);}
"low" {return tt.getKeywordTokenType(315);}
"low_cost_tbs" {return tt.getKeywordTokenType(316);}
"main" {return tt.getKeywordTokenType(317);}
"manage" {return tt.getKeywordTokenType(318);}
"managed" {return tt.getKeywordTokenType(319);}
"manager" {return tt.getKeywordTokenType(320);}
"management" {return tt.getKeywordTokenType(321);}
"manual" {return tt.getKeywordTokenType(322);}
"mapping" {return tt.getKeywordTokenType(323);}
"master" {return tt.getKeywordTokenType(324);}
"matched" {return tt.getKeywordTokenType(325);}
"materialized" {return tt.getKeywordTokenType(326);}
"maxextents" {return tt.getKeywordTokenType(327);}
"maximize" {return tt.getKeywordTokenType(328);}
"maxsize" {return tt.getKeywordTokenType(329);}
"maxvalue" {return tt.getKeywordTokenType(330);}
"mean_squares_between" {return tt.getKeywordTokenType(331);}
"mean_squares_within" {return tt.getKeywordTokenType(332);}
"measure" {return tt.getKeywordTokenType(333);}
"measures" {return tt.getKeywordTokenType(334);}
"medium" {return tt.getKeywordTokenType(335);}
"member" {return tt.getKeywordTokenType(336);}
"memcompress" {return tt.getKeywordTokenType(337);}
"memory" {return tt.getKeywordTokenType(338);}
"merge" {return tt.getKeywordTokenType(339);}
"metadata" {return tt.getKeywordTokenType(340);}
"minextents" {return tt.getKeywordTokenType(341);}
"mining" {return tt.getKeywordTokenType(342);}
"minus" {return tt.getKeywordTokenType(343);}
"minute" {return tt.getKeywordTokenType(344);}
"minutes" {return tt.getKeywordTokenType(345);}
"minvalue" {return tt.getKeywordTokenType(346);}
"mirror" {return tt.getKeywordTokenType(347);}
"mismatch" {return tt.getKeywordTokenType(348);}
"mlslabel" {return tt.getKeywordTokenType(349);}
"mode" {return tt.getKeywordTokenType(350);}
"model" {return tt.getKeywordTokenType(351);}
"modification" {return tt.getKeywordTokenType(352);}
"modify" {return tt.getKeywordTokenType(353);}
"monitoring" {return tt.getKeywordTokenType(354);}
"month" {return tt.getKeywordTokenType(355);}
"months" {return tt.getKeywordTokenType(356);}
"mount" {return tt.getKeywordTokenType(357);}
"move" {return tt.getKeywordTokenType(358);}
"multiset" {return tt.getKeywordTokenType(359);}
"multivalue" {return tt.getKeywordTokenType(360);}
"name" {return tt.getKeywordTokenType(361);}
"nan" {return tt.getKeywordTokenType(362);}
"natural" {return tt.getKeywordTokenType(363);}
"nav" {return tt.getKeywordTokenType(364);}
"nchar_cs" {return tt.getKeywordTokenType(365);}
"nested" {return tt.getKeywordTokenType(366);}
"never" {return tt.getKeywordTokenType(367);}
"new" {return tt.getKeywordTokenType(368);}
"next" {return tt.getKeywordTokenType(369);}
"nextval" {return tt.getKeywordTokenType(370);}
"no" {return tt.getKeywordTokenType(371);}
"noarchivelog" {return tt.getKeywordTokenType(372);}
"noaudit" {return tt.getKeywordTokenType(373);}
"nocache" {return tt.getKeywordTokenType(374);}
"nocompress" {return tt.getKeywordTokenType(375);}
"nocycle" {return tt.getKeywordTokenType(376);}
"nodelay" {return tt.getKeywordTokenType(377);}
"noentityescaping" {return tt.getKeywordTokenType(378);}
"noforce" {return tt.getKeywordTokenType(379);}
"nologging" {return tt.getKeywordTokenType(380);}
"nomapping" {return tt.getKeywordTokenType(381);}
"nomaxvalue" {return tt.getKeywordTokenType(382);}
"nominvalue" {return tt.getKeywordTokenType(383);}
"nomonitoring" {return tt.getKeywordTokenType(384);}
"none" {return tt.getKeywordTokenType(385);}
"noneditionable" {return tt.getKeywordTokenType(386);}
"noorder" {return tt.getKeywordTokenType(387);}
"noparallel" {return tt.getKeywordTokenType(388);}
"norely" {return tt.getKeywordTokenType(389);}
"norepair" {return tt.getKeywordTokenType(390);}
"noresetlogs" {return tt.getKeywordTokenType(391);}
"noreverse" {return tt.getKeywordTokenType(392);}
"noschemacheck" {return tt.getKeywordTokenType(393);}
"nosort" {return tt.getKeywordTokenType(394);}
"noswitch" {return tt.getKeywordTokenType(395);}
"not" {return tt.getKeywordTokenType(396);}
"nothing" {return tt.getKeywordTokenType(397);}
"notification" {return tt.getKeywordTokenType(398);}
"notimeout" {return tt.getKeywordTokenType(399);}
"novalidate" {return tt.getKeywordTokenType(400);}
"nowait" {return tt.getKeywordTokenType(401);}
"null" {return tt.getKeywordTokenType(402);}
"nulls" {return tt.getKeywordTokenType(403);}
"object" {return tt.getKeywordTokenType(404);}
"of" {return tt.getKeywordTokenType(405);}
"off" {return tt.getKeywordTokenType(406);}
"offline" {return tt.getKeywordTokenType(407);}
"offset" {return tt.getKeywordTokenType(408);}
"on" {return tt.getKeywordTokenType(409);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(410);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(411);}
"one_sided_sig" {return tt.getKeywordTokenType(412);}
"online" {return tt.getKeywordTokenType(413);}
"only" {return tt.getKeywordTokenType(414);}
"open" {return tt.getKeywordTokenType(415);}
"operator" {return tt.getKeywordTokenType(416);}
"optimal" {return tt.getKeywordTokenType(417);}
"optimize" {return tt.getKeywordTokenType(418);}
"option" {return tt.getKeywordTokenType(419);}
"or" {return tt.getKeywordTokenType(420);}
"order" {return tt.getKeywordTokenType(421);}
"ordinality" {return tt.getKeywordTokenType(422);}
"organization" {return tt.getKeywordTokenType(423);}
"outer" {return tt.getKeywordTokenType(424);}
"outline" {return tt.getKeywordTokenType(425);}
"over" {return tt.getKeywordTokenType(426);}
"overflow" {return tt.getKeywordTokenType(427);}
"package" {return tt.getKeywordTokenType(428);}
"parallel" {return tt.getKeywordTokenType(429);}
"parameters" {return tt.getKeywordTokenType(430);}
"partial" {return tt.getKeywordTokenType(431);}
"partition" {return tt.getKeywordTokenType(432);}
"partitions" {return tt.getKeywordTokenType(433);}
"passing" {return tt.getKeywordTokenType(434);}
"password" {return tt.getKeywordTokenType(435);}
"path" {return tt.getKeywordTokenType(436);}
"pctfree" {return tt.getKeywordTokenType(437);}
"pctincrease" {return tt.getKeywordTokenType(438);}
"pctthreshold" {return tt.getKeywordTokenType(439);}
"pctused" {return tt.getKeywordTokenType(440);}
"pctversion" {return tt.getKeywordTokenType(441);}
"percent" {return tt.getKeywordTokenType(442);}
"performance" {return tt.getKeywordTokenType(443);}
"phi_coefficient" {return tt.getKeywordTokenType(444);}
"physical" {return tt.getKeywordTokenType(445);}
"pivot" {return tt.getKeywordTokenType(446);}
"plan" {return tt.getKeywordTokenType(447);}
"pluggable" {return tt.getKeywordTokenType(448);}
"policy" {return tt.getKeywordTokenType(449);}
"post_transaction" {return tt.getKeywordTokenType(450);}
"power" {return tt.getKeywordTokenType(451);}
"prebuilt" {return tt.getKeywordTokenType(452);}
"preceding" {return tt.getKeywordTokenType(453);}
"precision" {return tt.getKeywordTokenType(454);}
"prepare" {return tt.getKeywordTokenType(455);}
"present" {return tt.getKeywordTokenType(456);}
"preserve" {return tt.getKeywordTokenType(457);}
"pretty" {return tt.getKeywordTokenType(458);}
"primary" {return tt.getKeywordTokenType(459);}
"prior" {return tt.getKeywordTokenType(460);}
"priority" {return tt.getKeywordTokenType(461);}
"private" {return tt.getKeywordTokenType(462);}
"privilege" {return tt.getKeywordTokenType(463);}
"privileges" {return tt.getKeywordTokenType(464);}
"procedure" {return tt.getKeywordTokenType(465);}
"process" {return tt.getKeywordTokenType(466);}
"profile" {return tt.getKeywordTokenType(467);}
"program" {return tt.getKeywordTokenType(468);}
"protection" {return tt.getKeywordTokenType(469);}
"public" {return tt.getKeywordTokenType(470);}
"purge" {return tt.getKeywordTokenType(471);}
"query" {return tt.getKeywordTokenType(472);}
"queue" {return tt.getKeywordTokenType(473);}
"quiesce" {return tt.getKeywordTokenType(474);}
"quota" {return tt.getKeywordTokenType(475);}
"range" {return tt.getKeywordTokenType(476);}
"read" {return tt.getKeywordTokenType(477);}
"reads" {return tt.getKeywordTokenType(478);}
"rebalance" {return tt.getKeywordTokenType(479);}
"rebuild" {return tt.getKeywordTokenType(480);}
"recover" {return tt.getKeywordTokenType(481);}
"recovery" {return tt.getKeywordTokenType(482);}
"recycle" {return tt.getKeywordTokenType(483);}
"redefine" {return tt.getKeywordTokenType(484);}
"reduced" {return tt.getKeywordTokenType(485);}
"ref" {return tt.getKeywordTokenType(486);}
"reference" {return tt.getKeywordTokenType(487);}
"references" {return tt.getKeywordTokenType(488);}
"refresh" {return tt.getKeywordTokenType(489);}
"regexp_like" {return tt.getKeywordTokenType(490);}
"register" {return tt.getKeywordTokenType(491);}
"reject" {return tt.getKeywordTokenType(492);}
"rely" {return tt.getKeywordTokenType(493);}
"remainder" {return tt.getKeywordTokenType(494);}
"rename" {return tt.getKeywordTokenType(495);}
"repair" {return tt.getKeywordTokenType(496);}
"repeat" {return tt.getKeywordTokenType(497);}
"replace" {return tt.getKeywordTokenType(498);}
"reset" {return tt.getKeywordTokenType(499);}
"resetlogs" {return tt.getKeywordTokenType(500);}
"resize" {return tt.getKeywordTokenType(501);}
"resolve" {return tt.getKeywordTokenType(502);}
"resolver" {return tt.getKeywordTokenType(503);}
"resource" {return tt.getKeywordTokenType(504);}
"restrict" {return tt.getKeywordTokenType(505);}
"restricted" {return tt.getKeywordTokenType(506);}
"resumable" {return tt.getKeywordTokenType(507);}
"resume" {return tt.getKeywordTokenType(508);}
"retention" {return tt.getKeywordTokenType(509);}
"return" {return tt.getKeywordTokenType(510);}
"returning" {return tt.getKeywordTokenType(511);}
"reuse" {return tt.getKeywordTokenType(512);}
"reverse" {return tt.getKeywordTokenType(513);}
"revoke" {return tt.getKeywordTokenType(514);}
"rewrite" {return tt.getKeywordTokenType(515);}
"right" {return tt.getKeywordTokenType(516);}
"role" {return tt.getKeywordTokenType(517);}
"rollback" {return tt.getKeywordTokenType(518);}
"rollup" {return tt.getKeywordTokenType(519);}
"row" {return tt.getKeywordTokenType(520);}
"rownum" {return tt.getKeywordTokenType(521);}
"rows" {return tt.getKeywordTokenType(522);}
"rule" {return tt.getKeywordTokenType(523);}
"rules" {return tt.getKeywordTokenType(524);}
"salt" {return tt.getKeywordTokenType(525);}
"sample" {return tt.getKeywordTokenType(526);}
"savepoint" {return tt.getKeywordTokenType(527);}
"scan" {return tt.getKeywordTokenType(528);}
"scheduler" {return tt.getKeywordTokenType(529);}
"schemacheck" {return tt.getKeywordTokenType(530);}
"scn" {return tt.getKeywordTokenType(531);}
"scope" {return tt.getKeywordTokenType(532);}
"second" {return tt.getKeywordTokenType(533);}
"seed" {return tt.getKeywordTokenType(534);}
"segment" {return tt.getKeywordTokenType(535);}
"select" {return tt.getKeywordTokenType(536);}
"sequence" {return tt.getKeywordTokenType(537);}
"sequential" {return tt.getKeywordTokenType(538);}
"serializable" {return tt.getKeywordTokenType(539);}
"service" {return tt.getKeywordTokenType(540);}
"session" {return tt.getKeywordTokenType(541);}
"set" {return tt.getKeywordTokenType(542);}
"sets" {return tt.getKeywordTokenType(543);}
"settings" {return tt.getKeywordTokenType(544);}
"share" {return tt.getKeywordTokenType(545);}
"shared_pool" {return tt.getKeywordTokenType(546);}
"sharing" {return tt.getKeywordTokenType(547);}
"show" {return tt.getKeywordTokenType(548);}
"shrink" {return tt.getKeywordTokenType(549);}
"shutdown" {return tt.getKeywordTokenType(550);}
"siblings" {return tt.getKeywordTokenType(551);}
"sid" {return tt.getKeywordTokenType(552);}
"sig" {return tt.getKeywordTokenType(553);}
"single" {return tt.getKeywordTokenType(554);}
"size" {return tt.getKeywordTokenType(555);}
"skip" {return tt.getKeywordTokenType(556);}
"smallfile" {return tt.getKeywordTokenType(557);}
"snapshot" {return tt.getKeywordTokenType(558);}
"some" {return tt.getKeywordTokenType(559);}
"sort" {return tt.getKeywordTokenType(560);}
"source" {return tt.getKeywordTokenType(561);}
"space" {return tt.getKeywordTokenType(562);}
"specification" {return tt.getKeywordTokenType(563);}
"spfile" {return tt.getKeywordTokenType(564);}
"split" {return tt.getKeywordTokenType(565);}
"sql" {return tt.getKeywordTokenType(566);}
"standalone" {return tt.getKeywordTokenType(567);}
"standby" {return tt.getKeywordTokenType(568);}
"start" {return tt.getKeywordTokenType(569);}
"statement" {return tt.getKeywordTokenType(570);}
"statistic" {return tt.getKeywordTokenType(571);}
"statistics" {return tt.getKeywordTokenType(572);}
"stop" {return tt.getKeywordTokenType(573);}
"storage" {return tt.getKeywordTokenType(574);}
"store" {return tt.getKeywordTokenType(575);}
"strict" {return tt.getKeywordTokenType(576);}
"submultiset" {return tt.getKeywordTokenType(577);}
"subpartition" {return tt.getKeywordTokenType(578);}
"subpartitions" {return tt.getKeywordTokenType(579);}
"substitutable" {return tt.getKeywordTokenType(580);}
"successful" {return tt.getKeywordTokenType(581);}
"sum_squares_between" {return tt.getKeywordTokenType(582);}
"sum_squares_within" {return tt.getKeywordTokenType(583);}
"supplemental" {return tt.getKeywordTokenType(584);}
"suspend" {return tt.getKeywordTokenType(585);}
"switch" {return tt.getKeywordTokenType(586);}
"switchover" {return tt.getKeywordTokenType(587);}
"synchronous" {return tt.getKeywordTokenType(588);}
"synonym" {return tt.getKeywordTokenType(589);}
"sysbackup" {return tt.getKeywordTokenType(590);}
"sysdba" {return tt.getKeywordTokenType(591);}
"sysdg" {return tt.getKeywordTokenType(592);}
"syskm" {return tt.getKeywordTokenType(593);}
"sysoper" {return tt.getKeywordTokenType(594);}
"system" {return tt.getKeywordTokenType(595);}
"table" {return tt.getKeywordTokenType(596);}
"tables" {return tt.getKeywordTokenType(597);}
"tablespace" {return tt.getKeywordTokenType(598);}
"tempfile" {return tt.getKeywordTokenType(599);}
"template" {return tt.getKeywordTokenType(600);}
"temporary" {return tt.getKeywordTokenType(601);}
"test" {return tt.getKeywordTokenType(602);}
"than" {return tt.getKeywordTokenType(603);}
"then" {return tt.getKeywordTokenType(604);}
"thread" {return tt.getKeywordTokenType(605);}
"through" {return tt.getKeywordTokenType(606);}
"tier" {return tt.getKeywordTokenType(607);}
"ties" {return tt.getKeywordTokenType(608);}
"time" {return tt.getKeywordTokenType(609);}
"time_zone" {return tt.getKeywordTokenType(610);}
"timeout" {return tt.getKeywordTokenType(611);}
"timezone_abbr" {return tt.getKeywordTokenType(612);}
"timezone_hour" {return tt.getKeywordTokenType(613);}
"timezone_minute" {return tt.getKeywordTokenType(614);}
"timezone_region" {return tt.getKeywordTokenType(615);}
"to" {return tt.getKeywordTokenType(616);}
"trace" {return tt.getKeywordTokenType(617);}
"tracking" {return tt.getKeywordTokenType(618);}
"trailing" {return tt.getKeywordTokenType(619);}
"transaction" {return tt.getKeywordTokenType(620);}
"translation" {return tt.getKeywordTokenType(621);}
"trigger" {return tt.getKeywordTokenType(622);}
"truncate" {return tt.getKeywordTokenType(623);}
"trusted" {return tt.getKeywordTokenType(624);}
"tuning" {return tt.getKeywordTokenType(625);}
"two_sided_prob" {return tt.getKeywordTokenType(626);}
"two_sided_sig" {return tt.getKeywordTokenType(627);}
"type" {return tt.getKeywordTokenType(628);}
"u_statistic" {return tt.getKeywordTokenType(629);}
"uid" {return tt.getKeywordTokenType(630);}
"unarchived" {return tt.getKeywordTokenType(631);}
"unbounded" {return tt.getKeywordTokenType(632);}
"unconditional" {return tt.getKeywordTokenType(633);}
"under" {return tt.getKeywordTokenType(634);}
"under_path" {return tt.getKeywordTokenType(635);}
"undrop" {return tt.getKeywordTokenType(636);}
"union" {return tt.getKeywordTokenType(637);}
"unique" {return tt.getKeywordTokenType(638);}
"unlimited" {return tt.getKeywordTokenType(639);}
"unlock" {return tt.getKeywordTokenType(640);}
"unpivot" {return tt.getKeywordTokenType(641);}
"unprotected" {return tt.getKeywordTokenType(642);}
"unquiesce" {return tt.getKeywordTokenType(643);}
"unrecoverable" {return tt.getKeywordTokenType(644);}
"until" {return tt.getKeywordTokenType(645);}
"unusable" {return tt.getKeywordTokenType(646);}
"unused" {return tt.getKeywordTokenType(647);}
"update" {return tt.getKeywordTokenType(648);}
"updated" {return tt.getKeywordTokenType(649);}
"upgrade" {return tt.getKeywordTokenType(650);}
"upsert" {return tt.getKeywordTokenType(651);}
"usage" {return tt.getKeywordTokenType(652);}
"use" {return tt.getKeywordTokenType(653);}
"user" {return tt.getKeywordTokenType(654);}
"using" {return tt.getKeywordTokenType(655);}
"validate" {return tt.getKeywordTokenType(656);}
"validation" {return tt.getKeywordTokenType(657);}
"value" {return tt.getKeywordTokenType(658);}
"values" {return tt.getKeywordTokenType(659);}
"varray" {return tt.getKeywordTokenType(660);}
"version" {return tt.getKeywordTokenType(661);}
"versions" {return tt.getKeywordTokenType(662);}
"view" {return tt.getKeywordTokenType(663);}
"visible" {return tt.getKeywordTokenType(664);}
"wait" {return tt.getKeywordTokenType(665);}
"wellformed" {return tt.getKeywordTokenType(666);}
"when" {return tt.getKeywordTokenType(667);}
"whenever" {return tt.getKeywordTokenType(668);}
"where" {return tt.getKeywordTokenType(669);}
"with" {return tt.getKeywordTokenType(670);}
"within" {return tt.getKeywordTokenType(671);}
"without" {return tt.getKeywordTokenType(672);}
"work" {return tt.getKeywordTokenType(673);}
"wrapper" {return tt.getKeywordTokenType(674);}
"write" {return tt.getKeywordTokenType(675);}
"xml" {return tt.getKeywordTokenType(676);}
"xmlnamespaces" {return tt.getKeywordTokenType(677);}
"xmlschema" {return tt.getKeywordTokenType(678);}
"xmltype" {return tt.getKeywordTokenType(679);}
"year" {return tt.getKeywordTokenType(680);}
"years" {return tt.getKeywordTokenType(681);}
"yes" {return tt.getKeywordTokenType(682);}
"zone" {return tt.getKeywordTokenType(683);}
"false" {return tt.getKeywordTokenType(684);}
"true" {return tt.getKeywordTokenType(685);}






"abs" {return tt.getFunctionTokenType(0);}
"acos" {return tt.getFunctionTokenType(1);}
"add_months" {return tt.getFunctionTokenType(2);}
"appendchildxml" {return tt.getFunctionTokenType(3);}
"ascii" {return tt.getFunctionTokenType(4);}
"asciistr" {return tt.getFunctionTokenType(5);}
"asin" {return tt.getFunctionTokenType(6);}
"atan" {return tt.getFunctionTokenType(7);}
"atan2" {return tt.getFunctionTokenType(8);}
"avg" {return tt.getFunctionTokenType(9);}
"bfilename" {return tt.getFunctionTokenType(10);}
"bin_to_num" {return tt.getFunctionTokenType(11);}
"bitand" {return tt.getFunctionTokenType(12);}
"cardinality" {return tt.getFunctionTokenType(13);}
"cast" {return tt.getFunctionTokenType(14);}
"ceil" {return tt.getFunctionTokenType(15);}
"chartorowid" {return tt.getFunctionTokenType(16);}
"chr" {return tt.getFunctionTokenType(17);}
"collect" {return tt.getFunctionTokenType(18);}
"compose" {return tt.getFunctionTokenType(19);}
"concat" {return tt.getFunctionTokenType(20);}
"convert" {return tt.getFunctionTokenType(21);}
"corr" {return tt.getFunctionTokenType(22);}
"corr_k" {return tt.getFunctionTokenType(23);}
"corr_s" {return tt.getFunctionTokenType(24);}
"cos" {return tt.getFunctionTokenType(25);}
"cosh" {return tt.getFunctionTokenType(26);}
"count" {return tt.getFunctionTokenType(27);}
"covar_pop" {return tt.getFunctionTokenType(28);}
"covar_samp" {return tt.getFunctionTokenType(29);}
"cume_dist" {return tt.getFunctionTokenType(30);}
"current_date" {return tt.getFunctionTokenType(31);}
"current_timestamp" {return tt.getFunctionTokenType(32);}
"cv" {return tt.getFunctionTokenType(33);}
"dbtimezone" {return tt.getFunctionTokenType(34);}
"dbtmezone" {return tt.getFunctionTokenType(35);}
"decode" {return tt.getFunctionTokenType(36);}
"decompose" {return tt.getFunctionTokenType(37);}
"deletexml" {return tt.getFunctionTokenType(38);}
"depth" {return tt.getFunctionTokenType(39);}
"deref" {return tt.getFunctionTokenType(40);}
"empty_blob" {return tt.getFunctionTokenType(41);}
"empty_clob" {return tt.getFunctionTokenType(42);}
"existsnode" {return tt.getFunctionTokenType(43);}
"exp" {return tt.getFunctionTokenType(44);}
"extract" {return tt.getFunctionTokenType(45);}
"extractvalue" {return tt.getFunctionTokenType(46);}
"first_value" {return tt.getFunctionTokenType(47);}
"floor" {return tt.getFunctionTokenType(48);}
"from_tz" {return tt.getFunctionTokenType(49);}
"greatest" {return tt.getFunctionTokenType(50);}
"group_id" {return tt.getFunctionTokenType(51);}
"grouping" {return tt.getFunctionTokenType(52);}
"grouping_id" {return tt.getFunctionTokenType(53);}
"hextoraw" {return tt.getFunctionTokenType(54);}
"initcap" {return tt.getFunctionTokenType(55);}
"insertchildxml" {return tt.getFunctionTokenType(56);}
"insertchildxmlafter" {return tt.getFunctionTokenType(57);}
"insertchildxmlbefore" {return tt.getFunctionTokenType(58);}
"insertxmlafter" {return tt.getFunctionTokenType(59);}
"insertxmlbefore" {return tt.getFunctionTokenType(60);}
"instr" {return tt.getFunctionTokenType(61);}
"instr2" {return tt.getFunctionTokenType(62);}
"instr4" {return tt.getFunctionTokenType(63);}
"instrb" {return tt.getFunctionTokenType(64);}
"instrc" {return tt.getFunctionTokenType(65);}
"iteration_number" {return tt.getFunctionTokenType(66);}
"json_array" {return tt.getFunctionTokenType(67);}
"json_arrayagg" {return tt.getFunctionTokenType(68);}
"json_dataguide" {return tt.getFunctionTokenType(69);}
"json_object" {return tt.getFunctionTokenType(70);}
"json_objectagg" {return tt.getFunctionTokenType(71);}
"json_query" {return tt.getFunctionTokenType(72);}
"json_table" {return tt.getFunctionTokenType(73);}
"json_value" {return tt.getFunctionTokenType(74);}
"lag" {return tt.getFunctionTokenType(75);}
"last_day" {return tt.getFunctionTokenType(76);}
"last_value" {return tt.getFunctionTokenType(77);}
"lateral" {return tt.getFunctionTokenType(78);}
"lead" {return tt.getFunctionTokenType(79);}
"least" {return tt.getFunctionTokenType(80);}
"length" {return tt.getFunctionTokenType(81);}
"length2" {return tt.getFunctionTokenType(82);}
"length4" {return tt.getFunctionTokenType(83);}
"lengthb" {return tt.getFunctionTokenType(84);}
"lengthc" {return tt.getFunctionTokenType(85);}
"listagg" {return tt.getFunctionTokenType(86);}
"ln" {return tt.getFunctionTokenType(87);}
"lnnvl" {return tt.getFunctionTokenType(88);}
"localtimestamp" {return tt.getFunctionTokenType(89);}
"lower" {return tt.getFunctionTokenType(90);}
"lpad" {return tt.getFunctionTokenType(91);}
"ltrim" {return tt.getFunctionTokenType(92);}
"make_ref" {return tt.getFunctionTokenType(93);}
"max" {return tt.getFunctionTokenType(94);}
"median" {return tt.getFunctionTokenType(95);}
"min" {return tt.getFunctionTokenType(96);}
"mod" {return tt.getFunctionTokenType(97);}
"months_between" {return tt.getFunctionTokenType(98);}
"nanvl" {return tt.getFunctionTokenType(99);}
"nchr" {return tt.getFunctionTokenType(100);}
"new_time" {return tt.getFunctionTokenType(101);}
"next_day" {return tt.getFunctionTokenType(102);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(103);}
"nls_charset_id" {return tt.getFunctionTokenType(104);}
"nls_charset_name" {return tt.getFunctionTokenType(105);}
"nls_initcap" {return tt.getFunctionTokenType(106);}
"nls_lower" {return tt.getFunctionTokenType(107);}
"nls_upper" {return tt.getFunctionTokenType(108);}
"nlssort" {return tt.getFunctionTokenType(109);}
"ntile" {return tt.getFunctionTokenType(110);}
"nullif" {return tt.getFunctionTokenType(111);}
"numtodsinterval" {return tt.getFunctionTokenType(112);}
"numtoyminterval" {return tt.getFunctionTokenType(113);}
"nvl" {return tt.getFunctionTokenType(114);}
"nvl2" {return tt.getFunctionTokenType(115);}
"ora_hash" {return tt.getFunctionTokenType(116);}
"percent_rank" {return tt.getFunctionTokenType(117);}
"percentile_cont" {return tt.getFunctionTokenType(118);}
"percentile_disc" {return tt.getFunctionTokenType(119);}
"powermultiset" {return tt.getFunctionTokenType(120);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(121);}
"presentnnv" {return tt.getFunctionTokenType(122);}
"presentv" {return tt.getFunctionTokenType(123);}
"previous" {return tt.getFunctionTokenType(124);}
"rank" {return tt.getFunctionTokenType(125);}
"ratio_to_report" {return tt.getFunctionTokenType(126);}
"rawtohex" {return tt.getFunctionTokenType(127);}
"rawtonhex" {return tt.getFunctionTokenType(128);}
"reftohex" {return tt.getFunctionTokenType(129);}
"regexp_instr" {return tt.getFunctionTokenType(130);}
"regexp_replace" {return tt.getFunctionTokenType(131);}
"regexp_substr" {return tt.getFunctionTokenType(132);}
"regr_avgx" {return tt.getFunctionTokenType(133);}
"regr_avgy" {return tt.getFunctionTokenType(134);}
"regr_count" {return tt.getFunctionTokenType(135);}
"regr_intercept" {return tt.getFunctionTokenType(136);}
"regr_r2" {return tt.getFunctionTokenType(137);}
"regr_slope" {return tt.getFunctionTokenType(138);}
"regr_sxx" {return tt.getFunctionTokenType(139);}
"regr_sxy" {return tt.getFunctionTokenType(140);}
"regr_syy" {return tt.getFunctionTokenType(141);}
"round" {return tt.getFunctionTokenType(142);}
"row_number" {return tt.getFunctionTokenType(143);}
"rowidtochar" {return tt.getFunctionTokenType(144);}
"rowidtonchar" {return tt.getFunctionTokenType(145);}
"rpad" {return tt.getFunctionTokenType(146);}
"rtrim" {return tt.getFunctionTokenType(147);}
"scn_to_timestamp" {return tt.getFunctionTokenType(148);}
"sessiontimezone" {return tt.getFunctionTokenType(149);}
"sign" {return tt.getFunctionTokenType(150);}
"sin" {return tt.getFunctionTokenType(151);}
"sinh" {return tt.getFunctionTokenType(152);}
"soundex" {return tt.getFunctionTokenType(153);}
"sqrt" {return tt.getFunctionTokenType(154);}
"stats_binomial_test" {return tt.getFunctionTokenType(155);}
"stats_crosstab" {return tt.getFunctionTokenType(156);}
"stats_f_test" {return tt.getFunctionTokenType(157);}
"stats_ks_test" {return tt.getFunctionTokenType(158);}
"stats_mode" {return tt.getFunctionTokenType(159);}
"stats_mw_test" {return tt.getFunctionTokenType(160);}
"stats_one_way_anova" {return tt.getFunctionTokenType(161);}
"stats_t_test_indep" {return tt.getFunctionTokenType(162);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(163);}
"stats_t_test_one" {return tt.getFunctionTokenType(164);}
"stats_t_test_paired" {return tt.getFunctionTokenType(165);}
"stats_wsr_test" {return tt.getFunctionTokenType(166);}
"stddev" {return tt.getFunctionTokenType(167);}
"stddev_pop" {return tt.getFunctionTokenType(168);}
"stddev_samp" {return tt.getFunctionTokenType(169);}
"substr" {return tt.getFunctionTokenType(170);}
"substr2" {return tt.getFunctionTokenType(171);}
"substr4" {return tt.getFunctionTokenType(172);}
"substrb" {return tt.getFunctionTokenType(173);}
"substrc" {return tt.getFunctionTokenType(174);}
"sum" {return tt.getFunctionTokenType(175);}
"sys_connect_by_path" {return tt.getFunctionTokenType(176);}
"sys_context" {return tt.getFunctionTokenType(177);}
"sys_dburigen" {return tt.getFunctionTokenType(178);}
"sys_extract_utc" {return tt.getFunctionTokenType(179);}
"sys_guid" {return tt.getFunctionTokenType(180);}
"sys_typeid" {return tt.getFunctionTokenType(181);}
"sys_xmlagg" {return tt.getFunctionTokenType(182);}
"sys_xmlgen" {return tt.getFunctionTokenType(183);}
"sysdate" {return tt.getFunctionTokenType(184);}
"systimestamp" {return tt.getFunctionTokenType(185);}
"tan" {return tt.getFunctionTokenType(186);}
"tanh" {return tt.getFunctionTokenType(187);}
"timestamp_to_scn" {return tt.getFunctionTokenType(188);}
"to_binary_double" {return tt.getFunctionTokenType(189);}
"to_binary_float" {return tt.getFunctionTokenType(190);}
"to_char" {return tt.getFunctionTokenType(191);}
"to_clob" {return tt.getFunctionTokenType(192);}
"to_date" {return tt.getFunctionTokenType(193);}
"to_dsinterval" {return tt.getFunctionTokenType(194);}
"to_lob" {return tt.getFunctionTokenType(195);}
"to_multi_byte" {return tt.getFunctionTokenType(196);}
"to_nchar" {return tt.getFunctionTokenType(197);}
"to_nclob" {return tt.getFunctionTokenType(198);}
"to_number" {return tt.getFunctionTokenType(199);}
"to_single_byte" {return tt.getFunctionTokenType(200);}
"to_timestamp" {return tt.getFunctionTokenType(201);}
"to_timestamp_tz" {return tt.getFunctionTokenType(202);}
"to_yminterval" {return tt.getFunctionTokenType(203);}
"translate" {return tt.getFunctionTokenType(204);}
"treat" {return tt.getFunctionTokenType(205);}
"trim" {return tt.getFunctionTokenType(206);}
"trunc" {return tt.getFunctionTokenType(207);}
"tz_offset" {return tt.getFunctionTokenType(208);}
"unistr" {return tt.getFunctionTokenType(209);}
"updatexml" {return tt.getFunctionTokenType(210);}
"upper" {return tt.getFunctionTokenType(211);}
"userenv" {return tt.getFunctionTokenType(212);}
"validate_conversion" {return tt.getFunctionTokenType(213);}
"var_pop" {return tt.getFunctionTokenType(214);}
"var_samp" {return tt.getFunctionTokenType(215);}
"variance" {return tt.getFunctionTokenType(216);}
"vsize" {return tt.getFunctionTokenType(217);}
"width_bucket" {return tt.getFunctionTokenType(218);}
"xmlagg" {return tt.getFunctionTokenType(219);}
"xmlattributes" {return tt.getFunctionTokenType(220);}
"xmlcast" {return tt.getFunctionTokenType(221);}
"xmlcdata" {return tt.getFunctionTokenType(222);}
"xmlcolattval" {return tt.getFunctionTokenType(223);}
"xmlcomment" {return tt.getFunctionTokenType(224);}
"xmlconcat" {return tt.getFunctionTokenType(225);}
"xmldiff" {return tt.getFunctionTokenType(226);}
"xmlelement" {return tt.getFunctionTokenType(227);}
"xmlforest" {return tt.getFunctionTokenType(228);}
"xmlisvalid" {return tt.getFunctionTokenType(229);}
"xmlparse" {return tt.getFunctionTokenType(230);}
"xmlpatch" {return tt.getFunctionTokenType(231);}
"xmlpi" {return tt.getFunctionTokenType(232);}
"xmlquery" {return tt.getFunctionTokenType(233);}
"xmlroot" {return tt.getFunctionTokenType(234);}
"xmlsequence" {return tt.getFunctionTokenType(235);}
"xmlserialize" {return tt.getFunctionTokenType(236);}
"xmltable" {return tt.getFunctionTokenType(237);}
"xmltransform" {return tt.getFunctionTokenType(238);}







"aq_tm_processes" {return tt.getParameterTokenType(0);}
"archive_lag_target" {return tt.getParameterTokenType(1);}
"audit_file_dest" {return tt.getParameterTokenType(2);}
"audit_sys_operations" {return tt.getParameterTokenType(3);}
"audit_trail" {return tt.getParameterTokenType(4);}
"background_core_dump" {return tt.getParameterTokenType(5);}
"background_dump_dest" {return tt.getParameterTokenType(6);}
"backup_tape_io_slaves" {return tt.getParameterTokenType(7);}
"bitmap_merge_area_size" {return tt.getParameterTokenType(8);}
"blank_trimming" {return tt.getParameterTokenType(9);}
"circuits" {return tt.getParameterTokenType(10);}
"cluster_database" {return tt.getParameterTokenType(11);}
"cluster_database_instances" {return tt.getParameterTokenType(12);}
"cluster_interconnects" {return tt.getParameterTokenType(13);}
"commit_point_strength" {return tt.getParameterTokenType(14);}
"compatible" {return tt.getParameterTokenType(15);}
"composite_limit" {return tt.getParameterTokenType(16);}
"connect_time" {return tt.getParameterTokenType(17);}
"control_file_record_keep_time" {return tt.getParameterTokenType(18);}
"control_files" {return tt.getParameterTokenType(19);}
"core_dump_dest"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(20);}
"cpu_count" {return tt.getParameterTokenType(21);}
"cpu_per_call" {return tt.getParameterTokenType(22);}
"cpu_per_session" {return tt.getParameterTokenType(23);}
"create_bitmap_area_size" {return tt.getParameterTokenType(24);}
"create_stored_outlines" {return tt.getParameterTokenType(25);}
"current_schema" {return tt.getParameterTokenType(26);}
"cursor_sharing" {return tt.getParameterTokenType(27);}
"cursor_space_for_time" {return tt.getParameterTokenType(28);}
"db_block_checking" {return tt.getParameterTokenType(29);}
"db_block_checksum" {return tt.getParameterTokenType(30);}
"db_block_size" {return tt.getParameterTokenType(31);}
"db_cache_advice" {return tt.getParameterTokenType(32);}
"db_cache_size" {return tt.getParameterTokenType(33);}
"db_create_file_dest" {return tt.getParameterTokenType(34);}
"db_create_online_log_dest_n" {return tt.getParameterTokenType(35);}
"db_domain" {return tt.getParameterTokenType(36);}
"db_file_multiblock_read_count" {return tt.getParameterTokenType(37);}
"db_file_name_convert" {return tt.getParameterTokenType(38);}
"db_files" {return tt.getParameterTokenType(39);}
"db_flashback_retention_target" {return tt.getParameterTokenType(40);}
"db_keep_cache_size" {return tt.getParameterTokenType(41);}
"db_name" {return tt.getParameterTokenType(42);}
"db_nk_cache_size" {return tt.getParameterTokenType(43);}
"db_recovery_file_dest" {return tt.getParameterTokenType(44);}
"db_recovery_file_dest_size" {return tt.getParameterTokenType(45);}
"db_recycle_cache_size" {return tt.getParameterTokenType(46);}
"db_unique_name" {return tt.getParameterTokenType(47);}
"db_writer_processes" {return tt.getParameterTokenType(48);}
"dbwr_io_slaves" {return tt.getParameterTokenType(49);}
"ddl_wait_for_locks" {return tt.getParameterTokenType(50);}
"dg_broker_config_filen" {return tt.getParameterTokenType(51);}
"dg_broker_start" {return tt.getParameterTokenType(52);}
"disk_asynch_io" {return tt.getParameterTokenType(53);}
"dispatchers" {return tt.getParameterTokenType(54);}
"distributed_lock_timeout" {return tt.getParameterTokenType(55);}
"dml_locks" {return tt.getParameterTokenType(56);}
"enqueue_resources" {return tt.getParameterTokenType(57);}
"error_on_overlap_time" {return tt.getParameterTokenType(58);}
"event" {return tt.getParameterTokenType(59);}
"failed_login_attempts" {return tt.getParameterTokenType(60);}
"fal_client" {return tt.getParameterTokenType(61);}
"fal_server" {return tt.getParameterTokenType(62);}
"fast_start_mttr_target" {return tt.getParameterTokenType(63);}
"fast_start_parallel_rollback" {return tt.getParameterTokenType(64);}
"file_mapping" {return tt.getParameterTokenType(65);}
"fileio_network_adapters" {return tt.getParameterTokenType(66);}
"filesystemio_options" {return tt.getParameterTokenType(67);}
"fixed_date" {return tt.getParameterTokenType(68);}
"flagger" {return tt.getParameterTokenType(69);}
"gc_files_to_locks" {return tt.getParameterTokenType(70);}
"gcs_server_processes" {return tt.getParameterTokenType(71);}
"global_names" {return tt.getParameterTokenType(72);}
"hash_area_size" {return tt.getParameterTokenType(73);}
"hi_shared_memory_address" {return tt.getParameterTokenType(74);}
"hs_autoregister" {return tt.getParameterTokenType(75);}
"idle_time" {return tt.getParameterTokenType(76);}
"ifile" {return tt.getParameterTokenType(77);}
"instance"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(78);}
"instance_groups" {return tt.getParameterTokenType(79);}
"instance_name" {return tt.getParameterTokenType(80);}
"instance_number" {return tt.getParameterTokenType(81);}
"instance_type" {return tt.getParameterTokenType(82);}
"isolation_level" {return tt.getParameterTokenType(83);}
"java_max_sessionspace_size" {return tt.getParameterTokenType(84);}
"java_pool_size" {return tt.getParameterTokenType(85);}
"java_soft_sessionspace_limit" {return tt.getParameterTokenType(86);}
"job_queue_processes" {return tt.getParameterTokenType(87);}
"large_pool_size" {return tt.getParameterTokenType(88);}
"ldap_directory_access" {return tt.getParameterTokenType(89);}
"license_max_sessions" {return tt.getParameterTokenType(90);}
"license_max_users" {return tt.getParameterTokenType(91);}
"license_sessions_warning" {return tt.getParameterTokenType(92);}
"local_listener" {return tt.getParameterTokenType(93);}
"lock_sga" {return tt.getParameterTokenType(94);}
"log_archive_config" {return tt.getParameterTokenType(95);}
"log_archive_dest" {return tt.getParameterTokenType(96);}
"log_archive_dest_n" {return tt.getParameterTokenType(97);}
"log_archive_dest_state_n" {return tt.getParameterTokenType(98);}
"log_archive_duplex_dest" {return tt.getParameterTokenType(99);}
"log_archive_format" {return tt.getParameterTokenType(100);}
"log_archive_local_first" {return tt.getParameterTokenType(101);}
"log_archive_max_processes" {return tt.getParameterTokenType(102);}
"log_archive_min_succeed_dest" {return tt.getParameterTokenType(103);}
"log_archive_trace" {return tt.getParameterTokenType(104);}
"log_buffer" {return tt.getParameterTokenType(105);}
"log_checkpoint_interval" {return tt.getParameterTokenType(106);}
"log_checkpoint_timeout" {return tt.getParameterTokenType(107);}
"log_checkpoints_to_alert" {return tt.getParameterTokenType(108);}
"log_file_name_convert" {return tt.getParameterTokenType(109);}
"logical_reads_per_call" {return tt.getParameterTokenType(110);}
"logical_reads_per_session" {return tt.getParameterTokenType(111);}
"logmnr_max_persistent_sessions" {return tt.getParameterTokenType(112);}
"max_commit_propagation_delay" {return tt.getParameterTokenType(113);}
"max_dispatchers" {return tt.getParameterTokenType(114);}
"max_dump_file_size" {return tt.getParameterTokenType(115);}
"max_shared_servers" {return tt.getParameterTokenType(116);}
"nls_calendar" {return tt.getParameterTokenType(117);}
"nls_comp" {return tt.getParameterTokenType(118);}
"nls_currency" {return tt.getParameterTokenType(119);}
"nls_date_format" {return tt.getParameterTokenType(120);}
"nls_date_language" {return tt.getParameterTokenType(121);}
"nls_dual_currency" {return tt.getParameterTokenType(122);}
"nls_iso_currency" {return tt.getParameterTokenType(123);}
"nls_language" {return tt.getParameterTokenType(124);}
"nls_length_semantics" {return tt.getParameterTokenType(125);}
"nls_nchar_conv_excp" {return tt.getParameterTokenType(126);}
"nls_numeric_characters" {return tt.getParameterTokenType(127);}
"nls_sort" {return tt.getParameterTokenType(128);}
"nls_territory" {return tt.getParameterTokenType(129);}
"nls_timestamp_format" {return tt.getParameterTokenType(130);}
"nls_timestamp_tz_format" {return tt.getParameterTokenType(131);}
"o7_dictionary_accessibility" {return tt.getParameterTokenType(132);}
"object_cache_max_size_percent" {return tt.getParameterTokenType(133);}
"object_cache_optimal_size" {return tt.getParameterTokenType(134);}
"olap_page_pool_size" {return tt.getParameterTokenType(135);}
"open_cursors" {return tt.getParameterTokenType(136);}
"open_links" {return tt.getParameterTokenType(137);}
"open_links_per_instance" {return tt.getParameterTokenType(138);}
"optimizer_dynamic_sampling" {return tt.getParameterTokenType(139);}
"optimizer_features_enable" {return tt.getParameterTokenType(140);}
"optimizer_index_caching" {return tt.getParameterTokenType(141);}
"optimizer_index_cost_adj" {return tt.getParameterTokenType(142);}
"optimizer_mode" {return tt.getParameterTokenType(143);}
"os_authent_prefix" {return tt.getParameterTokenType(144);}
"os_roles" {return tt.getParameterTokenType(145);}
"osm_diskgroups" {return tt.getParameterTokenType(146);}
"osm_diskstring" {return tt.getParameterTokenType(147);}
"osm_power_limit" {return tt.getParameterTokenType(148);}
"parallel_adaptive_multi_user" {return tt.getParameterTokenType(149);}
"parallel_execution_message_size" {return tt.getParameterTokenType(150);}
"parallel_instance_group" {return tt.getParameterTokenType(151);}
"parallel_max_servers" {return tt.getParameterTokenType(152);}
"parallel_min_percent" {return tt.getParameterTokenType(153);}
"parallel_min_servers" {return tt.getParameterTokenType(154);}
"parallel_threads_per_cpu" {return tt.getParameterTokenType(155);}
"password_grace_time" {return tt.getParameterTokenType(156);}
"password_life_time" {return tt.getParameterTokenType(157);}
"password_lock_time" {return tt.getParameterTokenType(158);}
"password_reuse_max" {return tt.getParameterTokenType(159);}
"password_reuse_time" {return tt.getParameterTokenType(160);}
"password_verify_function" {return tt.getParameterTokenType(161);}
"pga_aggregate_target" {return tt.getParameterTokenType(162);}
"plsql_code_type" {return tt.getParameterTokenType(163);}
"plsql_compiler_flags" {return tt.getParameterTokenType(164);}
"plsql_debug" {return tt.getParameterTokenType(165);}
"plsql_native_library_dir" {return tt.getParameterTokenType(166);}
"plsql_native_library_subdir_count" {return tt.getParameterTokenType(167);}
"plsql_optimize_level" {return tt.getParameterTokenType(168);}
"plsql_v2_compatibility" {return tt.getParameterTokenType(169);}
"plsql_warnings" {return tt.getParameterTokenType(170);}
"pre_page_sga" {return tt.getParameterTokenType(171);}
"private_sga" {return tt.getParameterTokenType(172);}
"processes" {return tt.getParameterTokenType(173);}
"query_rewrite_enabled" {return tt.getParameterTokenType(174);}
"query_rewrite_integrity" {return tt.getParameterTokenType(175);}
"rdbms_server_dn" {return tt.getParameterTokenType(176);}
"read_only_open_delayed" {return tt.getParameterTokenType(177);}
"recovery_parallelism" {return tt.getParameterTokenType(178);}
"remote_archive_enable" {return tt.getParameterTokenType(179);}
"remote_dependencies_mode" {return tt.getParameterTokenType(180);}
"remote_listener" {return tt.getParameterTokenType(181);}
"remote_login_passwordfile" {return tt.getParameterTokenType(182);}
"remote_os_authent" {return tt.getParameterTokenType(183);}
"remote_os_roles" {return tt.getParameterTokenType(184);}
"replication_dependency_tracking" {return tt.getParameterTokenType(185);}
"resource_limit" {return tt.getParameterTokenType(186);}
"resource_manager_plan" {return tt.getParameterTokenType(187);}
"resumable_timeout" {return tt.getParameterTokenType(188);}
"rollback_segments" {return tt.getParameterTokenType(189);}
"serial_reuse" {return tt.getParameterTokenType(190);}
"service_names" {return tt.getParameterTokenType(191);}
"session_cached_cursors" {return tt.getParameterTokenType(192);}
"session_max_open_files" {return tt.getParameterTokenType(193);}
"sessions" {return tt.getParameterTokenType(194);}
"sessions_per_user" {return tt.getParameterTokenType(195);}
"sga_max_size" {return tt.getParameterTokenType(196);}
"sga_target" {return tt.getParameterTokenType(197);}
"shadow_core_dump" {return tt.getParameterTokenType(198);}
"shared_memory_address" {return tt.getParameterTokenType(199);}
"shared_pool_reserved_size" {return tt.getParameterTokenType(200);}
"shared_pool_size" {return tt.getParameterTokenType(201);}
"shared_server_sessions" {return tt.getParameterTokenType(202);}
"shared_servers" {return tt.getParameterTokenType(203);}
"skip_unusable_indexes" {return tt.getParameterTokenType(204);}
"smtp_out_server" {return tt.getParameterTokenType(205);}
"sort_area_retained_size" {return tt.getParameterTokenType(206);}
"sort_area_size" {return tt.getParameterTokenType(207);}
"spfile"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(208);}
"sql_trace" {return tt.getParameterTokenType(209);}
"sql92_security" {return tt.getParameterTokenType(210);}
"sqltune_category" {return tt.getParameterTokenType(211);}
"standby_archive_dest" {return tt.getParameterTokenType(212);}
"standby_file_management" {return tt.getParameterTokenType(213);}
"star_transformation_enabled" {return tt.getParameterTokenType(214);}
"statement_id" {return tt.getParameterTokenType(215);}
"statistics_level" {return tt.getParameterTokenType(216);}
"streams_pool_size" {return tt.getParameterTokenType(217);}
"tape_asynch_io" {return tt.getParameterTokenType(218);}
"thread"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(219);}
"timed_os_statistics" {return tt.getParameterTokenType(220);}
"timed_statistics" {return tt.getParameterTokenType(221);}
"trace_enabled" {return tt.getParameterTokenType(222);}
"tracefile_identifier" {return tt.getParameterTokenType(223);}
"transactions" {return tt.getParameterTokenType(224);}
"transactions_per_rollback_segment" {return tt.getParameterTokenType(225);}
"undo_management" {return tt.getParameterTokenType(226);}
"undo_retention" {return tt.getParameterTokenType(227);}
"undo_tablespace" {return tt.getParameterTokenType(228);}
"use_indirect_data_buffers" {return tt.getParameterTokenType(229);}
"use_private_outlines" {return tt.getParameterTokenType(230);}
"use_stored_outlines" {return tt.getParameterTokenType(231);}
"user_dump_dest" {return tt.getParameterTokenType(232);}
"utl_file_dir" {return tt.getParameterTokenType(233);}
"workarea_size_policy" {return tt.getParameterTokenType(234);}


{CT_SIZE_CLAUSE} {return tt.getTokenType("CT_SIZE_CLAUSE");}

{INTEGER}     { return tt.getSharedTokenTypes().getInteger(); }
{NUMBER}      { return tt.getSharedTokenTypes().getNumber(); }
{STRING}      { return tt.getSharedTokenTypes().getString(); }

{IDENTIFIER}           { return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}    { return tt.getSharedTokenTypes().getQuotedIdentifier(); }

.                    { return tt.getSharedTokenTypes().getIdentifier(); }
}
