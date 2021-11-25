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

            /*
            public void startPsqlBlock(boolean isDeclareBloc) {
                blockNesting = 0;
                yybegin(PSQL_BLOCK);
                blockStartPos = zzStartRead;
                if (incNesting) blockNesting++;
            }

            public IElementType endPsqlBlock() {
                blockNesting = 0;
                yybegin(YYINITIAL);
                zzStartRead = blockStartPos;
                return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);
            }
            */

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


//PLSQL_BLOCK_START = "create"({ws}"or"{ws}"replace")? {ws} ("function"|"procedure"|"type"|"trigger"|"package") | "declare" | "begin"
//PLSQL_BLOCK_END = ";"{wso}"/"
//PLSQL_BLOCK = {PLSQL_BLOCK_START}{ws}([^/;] | ";"{wso}([^/] | "/*") | [^;]{wso}"/")*{PLSQL_BLOCK_END}?

PSQL_BLOCK_START_CREATE_OR_REPLACE = "create"({ws}"or"{ws}"replace")? {ws}
PSQL_BLOCK_START_CREATE_PACKAGE = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"package"
PSQL_BLOCK_START_CREATE_TRIGGER = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"trigger"
PSQL_BLOCK_START_CREATE_METHOD  = {PSQL_BLOCK_START_CREATE_OR_REPLACE}("function"|"procedure")
PSQL_BLOCK_START_CREATE_TYPE    = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"type"
PSQL_BLOCK_START_CREATE = {PSQL_BLOCK_START_CREATE_PACKAGE}|{PSQL_BLOCK_START_CREATE_TRIGGER}|{PSQL_BLOCK_START_CREATE_METHOD}|{PSQL_BLOCK_START_CREATE_TYPE}
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
SQLP_VARIABLE = "&""&"?{IDENTIFIER}

CT_SIZE_CLAUSE = {INTEGER}{wso}("k"|"m"|"g"|"t"|"p"|"e"){ws}

%state PSQL_BLOCK
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


<YYINITIAL> {

{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
{LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
{REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

//{PSQL_BLOCK_START}  {  }
//{PSQL_BLOCK_START_CREATE_PACKAGE}  { startPsqlBlock(true); }
//{PSQL_BLOCK_START_CREATE_TRIGGER}  { startPsqlBlock(false); }
//{PSQL_BLOCK_START_CREATE_METHOD}   { startPsqlBlock(false); }
//{PSQL_BLOCK_START_CREATE_TYPE}     { startPsqlBlock(false); }
//{PSQL_BLOCK_START_CREATE_TYPE}     { startPsqlBlock(false); }
{PSQL_BLOCK_START_CREATE}          { plsqlBlockMonitor.start(Marker.CREATE); }
{PSQL_BLOCK_START_DECLARE}         { plsqlBlockMonitor.start(Marker.DECLARE); }
{PSQL_BLOCK_START_BEGIN}           { plsqlBlockMonitor.start(Marker.BEGIN); }

{VARIABLE}          {return tt.getSharedTokenTypes().getVariable(); }
{SQLP_VARIABLE}     {return tt.getSharedTokenTypes().getVariable(); }

//{PLSQL_BLOCK}    {return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);}

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
"activate" {return tt.getKeywordTokenType(4);}
"active" {return tt.getKeywordTokenType(5);}
"add" {return tt.getKeywordTokenType(6);}
"admin" {return tt.getKeywordTokenType(7);}
"administer" {return tt.getKeywordTokenType(8);}
"advise" {return tt.getKeywordTokenType(9);}
"advisor" {return tt.getKeywordTokenType(10);}
"alias" {return tt.getKeywordTokenType(11);}
"all" {return tt.getKeywordTokenType(12);}
"allocate" {return tt.getKeywordTokenType(13);}
"allow" {return tt.getKeywordTokenType(14);}
"alter" {return tt.getKeywordTokenType(15);}
"always" {return tt.getKeywordTokenType(16);}
"analyze" {return tt.getKeywordTokenType(17);}
"ancillary" {return tt.getKeywordTokenType(18);}
"and" {return tt.getKeywordTokenType(19);}
"any" {return tt.getKeywordTokenType(20);}
"apply" {return tt.getKeywordTokenType(21);}
"archive" {return tt.getKeywordTokenType(22);}
"archivelog" {return tt.getKeywordTokenType(23);}
"array" {return tt.getKeywordTokenType(24);}
"as" {return tt.getKeywordTokenType(25);}
"asc" {return tt.getKeywordTokenType(26);}
"assembly" {return tt.getKeywordTokenType(27);}
"at" {return tt.getKeywordTokenType(28);}
"attribute" {return tt.getKeywordTokenType(29);}
"attributes" {return tt.getKeywordTokenType(30);}
"audit" {return tt.getKeywordTokenType(31);}
"authid" {return tt.getKeywordTokenType(32);}
"auto" {return tt.getKeywordTokenType(33);}
"autoextend" {return tt.getKeywordTokenType(34);}
"automatic" {return tt.getKeywordTokenType(35);}
"availability" {return tt.getKeywordTokenType(36);}
"backup" {return tt.getKeywordTokenType(37);}
"become" {return tt.getKeywordTokenType(38);}
"before" {return tt.getKeywordTokenType(39);}
"begin" {return tt.getKeywordTokenType(40);}
"beginning" {return tt.getKeywordTokenType(41);}
"bequeath" {return tt.getKeywordTokenType(42);}
"between" {return tt.getKeywordTokenType(43);}
"bigfile" {return tt.getKeywordTokenType(44);}
"binding" {return tt.getKeywordTokenType(45);}
"block" {return tt.getKeywordTokenType(46);}
"body" {return tt.getKeywordTokenType(47);}
"both" {return tt.getKeywordTokenType(48);}
"buffer_cache" {return tt.getKeywordTokenType(49);}
"buffer_pool" {return tt.getKeywordTokenType(50);}
"build" {return tt.getKeywordTokenType(51);}
"by" {return tt.getKeywordTokenType(52);}
"cache" {return tt.getKeywordTokenType(53);}
"cancel" {return tt.getKeywordTokenType(54);}
"canonical" {return tt.getKeywordTokenType(55);}
"capacity" {return tt.getKeywordTokenType(56);}
"cascade" {return tt.getKeywordTokenType(57);}
"case" {return tt.getKeywordTokenType(58);}
"category" {return tt.getKeywordTokenType(59);}
"change" {return tt.getKeywordTokenType(60);}
"char_cs" {return tt.getKeywordTokenType(61);}
"check" {return tt.getKeywordTokenType(62);}
"checkpoint" {return tt.getKeywordTokenType(63);}
"child" {return tt.getKeywordTokenType(64);}
"chisq_df" {return tt.getKeywordTokenType(65);}
"chisq_obs" {return tt.getKeywordTokenType(66);}
"chisq_sig" {return tt.getKeywordTokenType(67);}
"chunk" {return tt.getKeywordTokenType(68);}
"class" {return tt.getKeywordTokenType(69);}
"clear" {return tt.getKeywordTokenType(70);}
"clone" {return tt.getKeywordTokenType(71);}
"close" {return tt.getKeywordTokenType(72);}
"cluster" {return tt.getKeywordTokenType(73);}
"coalesce" {return tt.getKeywordTokenType(74);}
"coarse" {return tt.getKeywordTokenType(75);}
"coefficient" {return tt.getKeywordTokenType(76);}
"cohens_k" {return tt.getKeywordTokenType(77);}
"collation" {return tt.getKeywordTokenType(78);}
"column" {return tt.getKeywordTokenType(79);}
"column_value" {return tt.getKeywordTokenType(80);}
"columns" {return tt.getKeywordTokenType(81);}
"comment" {return tt.getKeywordTokenType(82);}
"commit" {return tt.getKeywordTokenType(83);}
"committed" {return tt.getKeywordTokenType(84);}
"compact" {return tt.getKeywordTokenType(85);}
"compatibility" {return tt.getKeywordTokenType(86);}
"compile" {return tt.getKeywordTokenType(87);}
"complete" {return tt.getKeywordTokenType(88);}
"compress" {return tt.getKeywordTokenType(89);}
"computation" {return tt.getKeywordTokenType(90);}
"compute" {return tt.getKeywordTokenType(91);}
"conditional" {return tt.getKeywordTokenType(92);}
"connect" {return tt.getKeywordTokenType(93);}
"consider" {return tt.getKeywordTokenType(94);}
"consistent" {return tt.getKeywordTokenType(95);}
"constraint" {return tt.getKeywordTokenType(96);}
"constraints" {return tt.getKeywordTokenType(97);}
"cont_coefficient" {return tt.getKeywordTokenType(98);}
"container_map" {return tt.getKeywordTokenType(99);}
"containers_default" {return tt.getKeywordTokenType(100);}
"content" {return tt.getKeywordTokenType(101);}
"contents" {return tt.getKeywordTokenType(102);}
"context" {return tt.getKeywordTokenType(103);}
"continue" {return tt.getKeywordTokenType(104);}
"controlfile" {return tt.getKeywordTokenType(105);}
"conversion" {return tt.getKeywordTokenType(106);}
"corruption" {return tt.getKeywordTokenType(107);}
"cost" {return tt.getKeywordTokenType(108);}
"cramers_v" {return tt.getKeywordTokenType(109);}
"create" {return tt.getKeywordTokenType(110);}
"creation" {return tt.getKeywordTokenType(111);}
"critical" {return tt.getKeywordTokenType(112);}
"cross" {return tt.getKeywordTokenType(113);}
"cube" {return tt.getKeywordTokenType(114);}
"current" {return tt.getKeywordTokenType(115);}
"current_user" {return tt.getKeywordTokenType(116);}
"currval" {return tt.getKeywordTokenType(117);}
"cursor" {return tt.getKeywordTokenType(118);}
"cycle" {return tt.getKeywordTokenType(119);}
"data" {return tt.getKeywordTokenType(120);}
"database" {return tt.getKeywordTokenType(121);}
"datafile" {return tt.getKeywordTokenType(122);}
"datafiles" {return tt.getKeywordTokenType(123);}
"day" {return tt.getKeywordTokenType(124);}
"ddl" {return tt.getKeywordTokenType(125);}
"deallocate" {return tt.getKeywordTokenType(126);}
"debug" {return tt.getKeywordTokenType(127);}
"decrement" {return tt.getKeywordTokenType(128);}
"default" {return tt.getKeywordTokenType(129);}
"defaults" {return tt.getKeywordTokenType(130);}
"deferrable" {return tt.getKeywordTokenType(131);}
"deferred" {return tt.getKeywordTokenType(132);}
"definer" {return tt.getKeywordTokenType(133);}
"delay" {return tt.getKeywordTokenType(134);}
"delete" {return tt.getKeywordTokenType(135);}
"demand" {return tt.getKeywordTokenType(136);}
"dense_rank" {return tt.getKeywordTokenType(137);}
"dequeue" {return tt.getKeywordTokenType(138);}
"desc" {return tt.getKeywordTokenType(139);}
"determines" {return tt.getKeywordTokenType(140);}
"df" {return tt.getKeywordTokenType(141);}
"df_between" {return tt.getKeywordTokenType(142);}
"df_den" {return tt.getKeywordTokenType(143);}
"df_num" {return tt.getKeywordTokenType(144);}
"df_within" {return tt.getKeywordTokenType(145);}
"dictionary" {return tt.getKeywordTokenType(146);}
"dimension" {return tt.getKeywordTokenType(147);}
"directory" {return tt.getKeywordTokenType(148);}
"disable" {return tt.getKeywordTokenType(149);}
"disconnect" {return tt.getKeywordTokenType(150);}
"disk" {return tt.getKeywordTokenType(151);}
"diskgroup" {return tt.getKeywordTokenType(152);}
"disks" {return tt.getKeywordTokenType(153);}
"dismount" {return tt.getKeywordTokenType(154);}
"distinct" {return tt.getKeywordTokenType(155);}
"distribute" {return tt.getKeywordTokenType(156);}
"distributed" {return tt.getKeywordTokenType(157);}
"dml" {return tt.getKeywordTokenType(158);}
"document" {return tt.getKeywordTokenType(159);}
"downgrade" {return tt.getKeywordTokenType(160);}
"drop" {return tt.getKeywordTokenType(161);}
"dump" {return tt.getKeywordTokenType(162);}
"duplicate" {return tt.getKeywordTokenType(163);}
"edition" {return tt.getKeywordTokenType(164);}
"editionable" {return tt.getKeywordTokenType(165);}
"editioning" {return tt.getKeywordTokenType(166);}
"element" {return tt.getKeywordTokenType(167);}
"else" {return tt.getKeywordTokenType(168);}
"empty" {return tt.getKeywordTokenType(169);}
"enable" {return tt.getKeywordTokenType(170);}
"encoding" {return tt.getKeywordTokenType(171);}
"encrypt" {return tt.getKeywordTokenType(172);}
"end" {return tt.getKeywordTokenType(173);}
"enforced" {return tt.getKeywordTokenType(174);}
"entityescaping" {return tt.getKeywordTokenType(175);}
"entry" {return tt.getKeywordTokenType(176);}
"equals_path" {return tt.getKeywordTokenType(177);}
"error" {return tt.getKeywordTokenType(178);}
"errors" {return tt.getKeywordTokenType(179);}
"escape" {return tt.getKeywordTokenType(180);}
"evalname" {return tt.getKeywordTokenType(181);}
"evaluate" {return tt.getKeywordTokenType(182);}
"evaluation" {return tt.getKeywordTokenType(183);}
"exact_prob" {return tt.getKeywordTokenType(184);}
"except" {return tt.getKeywordTokenType(185);}
"exceptions" {return tt.getKeywordTokenType(186);}
"exchange" {return tt.getKeywordTokenType(187);}
"exclude" {return tt.getKeywordTokenType(188);}
"excluding" {return tt.getKeywordTokenType(189);}
"exclusive" {return tt.getKeywordTokenType(190);}
"execute" {return tt.getKeywordTokenType(191);}
"exempt" {return tt.getKeywordTokenType(192);}
"exists" {return tt.getKeywordTokenType(193);}
"expire" {return tt.getKeywordTokenType(194);}
"explain" {return tt.getKeywordTokenType(195);}
"export" {return tt.getKeywordTokenType(196);}
"extended" {return tt.getKeywordTokenType(197);}
"extent" {return tt.getKeywordTokenType(198);}
"external" {return tt.getKeywordTokenType(199);}
"externally" {return tt.getKeywordTokenType(200);}
"f_ratio" {return tt.getKeywordTokenType(201);}
"failed" {return tt.getKeywordTokenType(202);}
"failgroup" {return tt.getKeywordTokenType(203);}
"fast" {return tt.getKeywordTokenType(204);}
"fetch" {return tt.getKeywordTokenType(205);}
"file" {return tt.getKeywordTokenType(206);}
"fine" {return tt.getKeywordTokenType(207);}
"finish" {return tt.getKeywordTokenType(208);}
"first" {return tt.getKeywordTokenType(209);}
"flashback" {return tt.getKeywordTokenType(210);}
"flush" {return tt.getKeywordTokenType(211);}
"folder" {return tt.getKeywordTokenType(212);}
"following" {return tt.getKeywordTokenType(213);}
"for" {return tt.getKeywordTokenType(214);}
"force" {return tt.getKeywordTokenType(215);}
"foreign" {return tt.getKeywordTokenType(216);}
"format" {return tt.getKeywordTokenType(217);}
"freelist" {return tt.getKeywordTokenType(218);}
"freelists" {return tt.getKeywordTokenType(219);}
"freepools" {return tt.getKeywordTokenType(220);}
"fresh" {return tt.getKeywordTokenType(221);}
"from" {return tt.getKeywordTokenType(222);}
"full" {return tt.getKeywordTokenType(223);}
"function" {return tt.getKeywordTokenType(224);}
"global" {return tt.getKeywordTokenType(225);}
"global_name" {return tt.getKeywordTokenType(226);}
"globally" {return tt.getKeywordTokenType(227);}
"grant" {return tt.getKeywordTokenType(228);}
"group" {return tt.getKeywordTokenType(229);}
"groups" {return tt.getKeywordTokenType(230);}
"guard" {return tt.getKeywordTokenType(231);}
"hash" {return tt.getKeywordTokenType(232);}
"having" {return tt.getKeywordTokenType(233);}
"heap" {return tt.getKeywordTokenType(234);}
"hide" {return tt.getKeywordTokenType(235);}
"hierarchy" {return tt.getKeywordTokenType(236);}
"high" {return tt.getKeywordTokenType(237);}
"history" {return tt.getKeywordTokenType(238);}
"hour" {return tt.getKeywordTokenType(239);}
"id" {return tt.getKeywordTokenType(240);}
"identified" {return tt.getKeywordTokenType(241);}
"identifier" {return tt.getKeywordTokenType(242);}
"ignore" {return tt.getKeywordTokenType(243);}
"immediate" {return tt.getKeywordTokenType(244);}
"import" {return tt.getKeywordTokenType(245);}
"in" {return tt.getKeywordTokenType(246);}
"include" {return tt.getKeywordTokenType(247);}
"including" {return tt.getKeywordTokenType(248);}
"increment" {return tt.getKeywordTokenType(249);}
"indent" {return tt.getKeywordTokenType(250);}
"index" {return tt.getKeywordTokenType(251);}
"indexes" {return tt.getKeywordTokenType(252);}
"indextype" {return tt.getKeywordTokenType(253);}
"infinite" {return tt.getKeywordTokenType(254);}
"initial" {return tt.getKeywordTokenType(255);}
"initially" {return tt.getKeywordTokenType(256);}
"initrans" {return tt.getKeywordTokenType(257);}
"inmemory" {return tt.getKeywordTokenType(258);}
"inner" {return tt.getKeywordTokenType(259);}
"insert" {return tt.getKeywordTokenType(260);}
"instance" {return tt.getKeywordTokenType(261);}
"intermediate" {return tt.getKeywordTokenType(262);}
"intersect" {return tt.getKeywordTokenType(263);}
"into" {return tt.getKeywordTokenType(264);}
"invalidate" {return tt.getKeywordTokenType(265);}
"invisible" {return tt.getKeywordTokenType(266);}
"is" {return tt.getKeywordTokenType(267);}
"iterate" {return tt.getKeywordTokenType(268);}
"java" {return tt.getKeywordTokenType(269);}
"job" {return tt.getKeywordTokenType(270);}
"join" {return tt.getKeywordTokenType(271);}
"json" {return tt.getKeywordTokenType(272);}
"keep" {return tt.getKeywordTokenType(273);}
"key" {return tt.getKeywordTokenType(274);}
"keys" {return tt.getKeywordTokenType(275);}
"kill" {return tt.getKeywordTokenType(276);}
"last" {return tt.getKeywordTokenType(277);}
"leading" {return tt.getKeywordTokenType(278);}
"left" {return tt.getKeywordTokenType(279);}
"less" {return tt.getKeywordTokenType(280);}
"level" {return tt.getKeywordTokenType(281);}
"levels" {return tt.getKeywordTokenType(282);}
"library" {return tt.getKeywordTokenType(283);}
"like" {return tt.getKeywordTokenType(284);}
"like2" {return tt.getKeywordTokenType(285);}
"like4" {return tt.getKeywordTokenType(286);}
"likec" {return tt.getKeywordTokenType(287);}
"limit" {return tt.getKeywordTokenType(288);}
"link" {return tt.getKeywordTokenType(289);}
"lob" {return tt.getKeywordTokenType(290);}
"local" {return tt.getKeywordTokenType(291);}
"locator" {return tt.getKeywordTokenType(292);}
"lock" {return tt.getKeywordTokenType(293);}
"locked" {return tt.getKeywordTokenType(294);}
"log" {return tt.getKeywordTokenType(295);}
"logfile" {return tt.getKeywordTokenType(296);}
"logging" {return tt.getKeywordTokenType(297);}
"logical" {return tt.getKeywordTokenType(298);}
"low" {return tt.getKeywordTokenType(299);}
"main" {return tt.getKeywordTokenType(300);}
"manage" {return tt.getKeywordTokenType(301);}
"managed" {return tt.getKeywordTokenType(302);}
"manager" {return tt.getKeywordTokenType(303);}
"management" {return tt.getKeywordTokenType(304);}
"manual" {return tt.getKeywordTokenType(305);}
"mapping" {return tt.getKeywordTokenType(306);}
"master" {return tt.getKeywordTokenType(307);}
"matched" {return tt.getKeywordTokenType(308);}
"materialized" {return tt.getKeywordTokenType(309);}
"maxextents" {return tt.getKeywordTokenType(310);}
"maximize" {return tt.getKeywordTokenType(311);}
"maxsize" {return tt.getKeywordTokenType(312);}
"maxvalue" {return tt.getKeywordTokenType(313);}
"mean_squares_between" {return tt.getKeywordTokenType(314);}
"mean_squares_within" {return tt.getKeywordTokenType(315);}
"measure" {return tt.getKeywordTokenType(316);}
"measures" {return tt.getKeywordTokenType(317);}
"medium" {return tt.getKeywordTokenType(318);}
"member" {return tt.getKeywordTokenType(319);}
"memcompress" {return tt.getKeywordTokenType(320);}
"memory" {return tt.getKeywordTokenType(321);}
"merge" {return tt.getKeywordTokenType(322);}
"metadata" {return tt.getKeywordTokenType(323);}
"minextents" {return tt.getKeywordTokenType(324);}
"mining" {return tt.getKeywordTokenType(325);}
"minus" {return tt.getKeywordTokenType(326);}
"minute" {return tt.getKeywordTokenType(327);}
"minutes" {return tt.getKeywordTokenType(328);}
"minvalue" {return tt.getKeywordTokenType(329);}
"mirror" {return tt.getKeywordTokenType(330);}
"mismatch" {return tt.getKeywordTokenType(331);}
"mlslabel" {return tt.getKeywordTokenType(332);}
"mode" {return tt.getKeywordTokenType(333);}
"model" {return tt.getKeywordTokenType(334);}
"modify" {return tt.getKeywordTokenType(335);}
"monitoring" {return tt.getKeywordTokenType(336);}
"month" {return tt.getKeywordTokenType(337);}
"mount" {return tt.getKeywordTokenType(338);}
"move" {return tt.getKeywordTokenType(339);}
"multiset" {return tt.getKeywordTokenType(340);}
"name" {return tt.getKeywordTokenType(341);}
"nan" {return tt.getKeywordTokenType(342);}
"natural" {return tt.getKeywordTokenType(343);}
"nav" {return tt.getKeywordTokenType(344);}
"nchar_cs" {return tt.getKeywordTokenType(345);}
"nested" {return tt.getKeywordTokenType(346);}
"never" {return tt.getKeywordTokenType(347);}
"new" {return tt.getKeywordTokenType(348);}
"next" {return tt.getKeywordTokenType(349);}
"nextval" {return tt.getKeywordTokenType(350);}
"no" {return tt.getKeywordTokenType(351);}
"noarchivelog" {return tt.getKeywordTokenType(352);}
"noaudit" {return tt.getKeywordTokenType(353);}
"nocache" {return tt.getKeywordTokenType(354);}
"nocompress" {return tt.getKeywordTokenType(355);}
"nocycle" {return tt.getKeywordTokenType(356);}
"nodelay" {return tt.getKeywordTokenType(357);}
"noentityescaping" {return tt.getKeywordTokenType(358);}
"noforce" {return tt.getKeywordTokenType(359);}
"nologging" {return tt.getKeywordTokenType(360);}
"nomapping" {return tt.getKeywordTokenType(361);}
"nomaxvalue" {return tt.getKeywordTokenType(362);}
"nominvalue" {return tt.getKeywordTokenType(363);}
"nomonitoring" {return tt.getKeywordTokenType(364);}
"none" {return tt.getKeywordTokenType(365);}
"noneditionable" {return tt.getKeywordTokenType(366);}
"noorder" {return tt.getKeywordTokenType(367);}
"noparallel" {return tt.getKeywordTokenType(368);}
"norely" {return tt.getKeywordTokenType(369);}
"norepair" {return tt.getKeywordTokenType(370);}
"noresetlogs" {return tt.getKeywordTokenType(371);}
"noreverse" {return tt.getKeywordTokenType(372);}
"noschemacheck" {return tt.getKeywordTokenType(373);}
"noswitch" {return tt.getKeywordTokenType(374);}
"not" {return tt.getKeywordTokenType(375);}
"nothing" {return tt.getKeywordTokenType(376);}
"notification" {return tt.getKeywordTokenType(377);}
"notimeout" {return tt.getKeywordTokenType(378);}
"novalidate" {return tt.getKeywordTokenType(379);}
"nowait" {return tt.getKeywordTokenType(380);}
"null" {return tt.getKeywordTokenType(381);}
"nulls" {return tt.getKeywordTokenType(382);}
"object" {return tt.getKeywordTokenType(383);}
"of" {return tt.getKeywordTokenType(384);}
"off" {return tt.getKeywordTokenType(385);}
"offline" {return tt.getKeywordTokenType(386);}
"offset" {return tt.getKeywordTokenType(387);}
"on" {return tt.getKeywordTokenType(388);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(389);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(390);}
"one_sided_sig" {return tt.getKeywordTokenType(391);}
"online" {return tt.getKeywordTokenType(392);}
"only" {return tt.getKeywordTokenType(393);}
"open" {return tt.getKeywordTokenType(394);}
"operator" {return tt.getKeywordTokenType(395);}
"optimal" {return tt.getKeywordTokenType(396);}
"option" {return tt.getKeywordTokenType(397);}
"or" {return tt.getKeywordTokenType(398);}
"order" {return tt.getKeywordTokenType(399);}
"ordinality" {return tt.getKeywordTokenType(400);}
"organization" {return tt.getKeywordTokenType(401);}
"outer" {return tt.getKeywordTokenType(402);}
"outline" {return tt.getKeywordTokenType(403);}
"over" {return tt.getKeywordTokenType(404);}
"overflow" {return tt.getKeywordTokenType(405);}
"package" {return tt.getKeywordTokenType(406);}
"parallel" {return tt.getKeywordTokenType(407);}
"parameters" {return tt.getKeywordTokenType(408);}
"partition" {return tt.getKeywordTokenType(409);}
"partitions" {return tt.getKeywordTokenType(410);}
"passing" {return tt.getKeywordTokenType(411);}
"path" {return tt.getKeywordTokenType(412);}
"pctfree" {return tt.getKeywordTokenType(413);}
"pctincrease" {return tt.getKeywordTokenType(414);}
"pctthreshold" {return tt.getKeywordTokenType(415);}
"pctused" {return tt.getKeywordTokenType(416);}
"pctversion" {return tt.getKeywordTokenType(417);}
"percent" {return tt.getKeywordTokenType(418);}
"performance" {return tt.getKeywordTokenType(419);}
"phi_coefficient" {return tt.getKeywordTokenType(420);}
"physical" {return tt.getKeywordTokenType(421);}
"pivot" {return tt.getKeywordTokenType(422);}
"plan" {return tt.getKeywordTokenType(423);}
"policy" {return tt.getKeywordTokenType(424);}
"post_transaction" {return tt.getKeywordTokenType(425);}
"power" {return tt.getKeywordTokenType(426);}
"prebuilt" {return tt.getKeywordTokenType(427);}
"preceding" {return tt.getKeywordTokenType(428);}
"precision" {return tt.getKeywordTokenType(429);}
"prepare" {return tt.getKeywordTokenType(430);}
"present" {return tt.getKeywordTokenType(431);}
"preserve" {return tt.getKeywordTokenType(432);}
"pretty" {return tt.getKeywordTokenType(433);}
"primary" {return tt.getKeywordTokenType(434);}
"prior" {return tt.getKeywordTokenType(435);}
"priority" {return tt.getKeywordTokenType(436);}
"private" {return tt.getKeywordTokenType(437);}
"privilege" {return tt.getKeywordTokenType(438);}
"privileges" {return tt.getKeywordTokenType(439);}
"procedure" {return tt.getKeywordTokenType(440);}
"process" {return tt.getKeywordTokenType(441);}
"profile" {return tt.getKeywordTokenType(442);}
"program" {return tt.getKeywordTokenType(443);}
"protection" {return tt.getKeywordTokenType(444);}
"public" {return tt.getKeywordTokenType(445);}
"purge" {return tt.getKeywordTokenType(446);}
"query" {return tt.getKeywordTokenType(447);}
"queue" {return tt.getKeywordTokenType(448);}
"quiesce" {return tt.getKeywordTokenType(449);}
"range" {return tt.getKeywordTokenType(450);}
"read" {return tt.getKeywordTokenType(451);}
"reads" {return tt.getKeywordTokenType(452);}
"rebalance" {return tt.getKeywordTokenType(453);}
"rebuild" {return tt.getKeywordTokenType(454);}
"recover" {return tt.getKeywordTokenType(455);}
"recovery" {return tt.getKeywordTokenType(456);}
"recycle" {return tt.getKeywordTokenType(457);}
"reduced" {return tt.getKeywordTokenType(458);}
"ref" {return tt.getKeywordTokenType(459);}
"reference" {return tt.getKeywordTokenType(460);}
"references" {return tt.getKeywordTokenType(461);}
"refresh" {return tt.getKeywordTokenType(462);}
"regexp_like" {return tt.getKeywordTokenType(463);}
"register" {return tt.getKeywordTokenType(464);}
"reject" {return tt.getKeywordTokenType(465);}
"rely" {return tt.getKeywordTokenType(466);}
"remainder" {return tt.getKeywordTokenType(467);}
"rename" {return tt.getKeywordTokenType(468);}
"repair" {return tt.getKeywordTokenType(469);}
"replace" {return tt.getKeywordTokenType(470);}
"reset" {return tt.getKeywordTokenType(471);}
"resetlogs" {return tt.getKeywordTokenType(472);}
"resize" {return tt.getKeywordTokenType(473);}
"resolve" {return tt.getKeywordTokenType(474);}
"resolver" {return tt.getKeywordTokenType(475);}
"resource" {return tt.getKeywordTokenType(476);}
"restrict" {return tt.getKeywordTokenType(477);}
"restricted" {return tt.getKeywordTokenType(478);}
"resumable" {return tt.getKeywordTokenType(479);}
"resume" {return tt.getKeywordTokenType(480);}
"retention" {return tt.getKeywordTokenType(481);}
"return" {return tt.getKeywordTokenType(482);}
"returning" {return tt.getKeywordTokenType(483);}
"reuse" {return tt.getKeywordTokenType(484);}
"reverse" {return tt.getKeywordTokenType(485);}
"revoke" {return tt.getKeywordTokenType(486);}
"rewrite" {return tt.getKeywordTokenType(487);}
"right" {return tt.getKeywordTokenType(488);}
"role" {return tt.getKeywordTokenType(489);}
"rollback" {return tt.getKeywordTokenType(490);}
"rollup" {return tt.getKeywordTokenType(491);}
"row" {return tt.getKeywordTokenType(492);}
"rownum" {return tt.getKeywordTokenType(493);}
"rows" {return tt.getKeywordTokenType(494);}
"rule" {return tt.getKeywordTokenType(495);}
"rules" {return tt.getKeywordTokenType(496);}
"salt" {return tt.getKeywordTokenType(497);}
"sample" {return tt.getKeywordTokenType(498);}
"savepoint" {return tt.getKeywordTokenType(499);}
"scan" {return tt.getKeywordTokenType(500);}
"scheduler" {return tt.getKeywordTokenType(501);}
"schemacheck" {return tt.getKeywordTokenType(502);}
"scn" {return tt.getKeywordTokenType(503);}
"scope" {return tt.getKeywordTokenType(504);}
"second" {return tt.getKeywordTokenType(505);}
"seed" {return tt.getKeywordTokenType(506);}
"segment" {return tt.getKeywordTokenType(507);}
"select" {return tt.getKeywordTokenType(508);}
"sequence" {return tt.getKeywordTokenType(509);}
"sequential" {return tt.getKeywordTokenType(510);}
"serializable" {return tt.getKeywordTokenType(511);}
"service" {return tt.getKeywordTokenType(512);}
"session" {return tt.getKeywordTokenType(513);}
"set" {return tt.getKeywordTokenType(514);}
"sets" {return tt.getKeywordTokenType(515);}
"settings" {return tt.getKeywordTokenType(516);}
"share" {return tt.getKeywordTokenType(517);}
"shared_pool" {return tt.getKeywordTokenType(518);}
"sharing" {return tt.getKeywordTokenType(519);}
"show" {return tt.getKeywordTokenType(520);}
"shrink" {return tt.getKeywordTokenType(521);}
"shutdown" {return tt.getKeywordTokenType(522);}
"siblings" {return tt.getKeywordTokenType(523);}
"sid" {return tt.getKeywordTokenType(524);}
"sig" {return tt.getKeywordTokenType(525);}
"single" {return tt.getKeywordTokenType(526);}
"size" {return tt.getKeywordTokenType(527);}
"skip" {return tt.getKeywordTokenType(528);}
"smallfile" {return tt.getKeywordTokenType(529);}
"some" {return tt.getKeywordTokenType(530);}
"sort" {return tt.getKeywordTokenType(531);}
"source" {return tt.getKeywordTokenType(532);}
"space" {return tt.getKeywordTokenType(533);}
"specification" {return tt.getKeywordTokenType(534);}
"spfile" {return tt.getKeywordTokenType(535);}
"split" {return tt.getKeywordTokenType(536);}
"sql" {return tt.getKeywordTokenType(537);}
"standalone" {return tt.getKeywordTokenType(538);}
"standby" {return tt.getKeywordTokenType(539);}
"start" {return tt.getKeywordTokenType(540);}
"statement" {return tt.getKeywordTokenType(541);}
"statistic" {return tt.getKeywordTokenType(542);}
"statistics" {return tt.getKeywordTokenType(543);}
"stop" {return tt.getKeywordTokenType(544);}
"storage" {return tt.getKeywordTokenType(545);}
"store" {return tt.getKeywordTokenType(546);}
"strict" {return tt.getKeywordTokenType(547);}
"submultiset" {return tt.getKeywordTokenType(548);}
"subpartition" {return tt.getKeywordTokenType(549);}
"subpartitions" {return tt.getKeywordTokenType(550);}
"substitutable" {return tt.getKeywordTokenType(551);}
"successful" {return tt.getKeywordTokenType(552);}
"sum_squares_between" {return tt.getKeywordTokenType(553);}
"sum_squares_within" {return tt.getKeywordTokenType(554);}
"supplemental" {return tt.getKeywordTokenType(555);}
"suspend" {return tt.getKeywordTokenType(556);}
"switch" {return tt.getKeywordTokenType(557);}
"switchover" {return tt.getKeywordTokenType(558);}
"synonym" {return tt.getKeywordTokenType(559);}
"system" {return tt.getKeywordTokenType(560);}
"table" {return tt.getKeywordTokenType(561);}
"tables" {return tt.getKeywordTokenType(562);}
"tablespace" {return tt.getKeywordTokenType(563);}
"tempfile" {return tt.getKeywordTokenType(564);}
"template" {return tt.getKeywordTokenType(565);}
"temporary" {return tt.getKeywordTokenType(566);}
"test" {return tt.getKeywordTokenType(567);}
"than" {return tt.getKeywordTokenType(568);}
"then" {return tt.getKeywordTokenType(569);}
"thread" {return tt.getKeywordTokenType(570);}
"through" {return tt.getKeywordTokenType(571);}
"ties" {return tt.getKeywordTokenType(572);}
"time" {return tt.getKeywordTokenType(573);}
"time_zone" {return tt.getKeywordTokenType(574);}
"timeout" {return tt.getKeywordTokenType(575);}
"timezone_abbr" {return tt.getKeywordTokenType(576);}
"timezone_hour" {return tt.getKeywordTokenType(577);}
"timezone_minute" {return tt.getKeywordTokenType(578);}
"timezone_region" {return tt.getKeywordTokenType(579);}
"to" {return tt.getKeywordTokenType(580);}
"trace" {return tt.getKeywordTokenType(581);}
"tracking" {return tt.getKeywordTokenType(582);}
"trailing" {return tt.getKeywordTokenType(583);}
"transaction" {return tt.getKeywordTokenType(584);}
"trigger" {return tt.getKeywordTokenType(585);}
"truncate" {return tt.getKeywordTokenType(586);}
"trusted" {return tt.getKeywordTokenType(587);}
"tuning" {return tt.getKeywordTokenType(588);}
"two_sided_prob" {return tt.getKeywordTokenType(589);}
"two_sided_sig" {return tt.getKeywordTokenType(590);}
"type" {return tt.getKeywordTokenType(591);}
"u_statistic" {return tt.getKeywordTokenType(592);}
"uid" {return tt.getKeywordTokenType(593);}
"unarchived" {return tt.getKeywordTokenType(594);}
"unbounded" {return tt.getKeywordTokenType(595);}
"unconditional" {return tt.getKeywordTokenType(596);}
"under" {return tt.getKeywordTokenType(597);}
"under_path" {return tt.getKeywordTokenType(598);}
"undrop" {return tt.getKeywordTokenType(599);}
"union" {return tt.getKeywordTokenType(600);}
"unique" {return tt.getKeywordTokenType(601);}
"unlimited" {return tt.getKeywordTokenType(602);}
"unpivot" {return tt.getKeywordTokenType(603);}
"unprotected" {return tt.getKeywordTokenType(604);}
"unquiesce" {return tt.getKeywordTokenType(605);}
"unrecoverable" {return tt.getKeywordTokenType(606);}
"until" {return tt.getKeywordTokenType(607);}
"unusable" {return tt.getKeywordTokenType(608);}
"unused" {return tt.getKeywordTokenType(609);}
"update" {return tt.getKeywordTokenType(610);}
"updated" {return tt.getKeywordTokenType(611);}
"upgrade" {return tt.getKeywordTokenType(612);}
"upsert" {return tt.getKeywordTokenType(613);}
"usage" {return tt.getKeywordTokenType(614);}
"use" {return tt.getKeywordTokenType(615);}
"user" {return tt.getKeywordTokenType(616);}
"using" {return tt.getKeywordTokenType(617);}
"validate" {return tt.getKeywordTokenType(618);}
"validation" {return tt.getKeywordTokenType(619);}
"value" {return tt.getKeywordTokenType(620);}
"values" {return tt.getKeywordTokenType(621);}
"varray" {return tt.getKeywordTokenType(622);}
"version" {return tt.getKeywordTokenType(623);}
"versions" {return tt.getKeywordTokenType(624);}
"view" {return tt.getKeywordTokenType(625);}
"visible" {return tt.getKeywordTokenType(626);}
"wait" {return tt.getKeywordTokenType(627);}
"wellformed" {return tt.getKeywordTokenType(628);}
"when" {return tt.getKeywordTokenType(629);}
"whenever" {return tt.getKeywordTokenType(630);}
"where" {return tt.getKeywordTokenType(631);}
"with" {return tt.getKeywordTokenType(632);}
"within" {return tt.getKeywordTokenType(633);}
"without" {return tt.getKeywordTokenType(634);}
"work" {return tt.getKeywordTokenType(635);}
"wrapper" {return tt.getKeywordTokenType(636);}
"write" {return tt.getKeywordTokenType(637);}
"xml" {return tt.getKeywordTokenType(638);}
"xmlnamespaces" {return tt.getKeywordTokenType(639);}
"xmlschema" {return tt.getKeywordTokenType(640);}
"xmltype" {return tt.getKeywordTokenType(641);}
"year" {return tt.getKeywordTokenType(642);}
"yes" {return tt.getKeywordTokenType(643);}
"zone" {return tt.getKeywordTokenType(644);}
"false" {return tt.getKeywordTokenType(645);}
"true" {return tt.getKeywordTokenType(646);}










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

{IDENTIFIER}         { return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}  { return tt.getSharedTokenTypes().getQuotedIdentifier(); }

.                    { return tt.getSharedTokenTypes().getIdentifier(); }
}
