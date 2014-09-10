package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;

%%

%class OraclePLSQLParserFlexLexer
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
    public OraclePLSQLParserFlexLexer(TokenTypeBundle tt) {
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

comment_tail =([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
BLOCK_COMMENT=("/*"[^]{comment_tail})|"/*"
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

%state DIV
%%

{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
{LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
{REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

{INTEGER}     { return tt.getSharedTokenTypes().getInteger(); }
{NUMBER}      { return tt.getSharedTokenTypes().getNumber(); }
{STRING}      { return tt.getSharedTokenTypes().getString(); }

"("{wso}"+"{wso}")"  {return tt.getTokenType("CT_OUTER_JOIN");}

"||" {return tt.getOperatorTokenType(0);}
":=" {return tt.getOperatorTokenType(1);}
".." {return tt.getOperatorTokenType(2);}

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
"with"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(1);}
"with"{ws}"local"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(2);}
"varchar" {return tt.getDataTypeTokenType(3);}
"urowid" {return tt.getDataTypeTokenType(4);}
"to"{ws}"second" {return tt.getDataTypeTokenType(5);}
"to"{ws}"month" {return tt.getDataTypeTokenType(6);}
"timestamp" {return tt.getDataTypeTokenType(7);}
"string" {return tt.getDataTypeTokenType(8);}
"smallint" {return tt.getDataTypeTokenType(9);}
"rowid" {return tt.getDataTypeTokenType(10);}
"real" {return tt.getDataTypeTokenType(11);}
"raw" {return tt.getDataTypeTokenType(12);}
"pls_integer" {return tt.getDataTypeTokenType(13);}
"nvarchar2" {return tt.getDataTypeTokenType(14);}
"numeric" {return tt.getDataTypeTokenType(15);}
"number" {return tt.getDataTypeTokenType(16);}
"nclob" {return tt.getDataTypeTokenType(17);}
"nchar"{ws}"varying" {return tt.getDataTypeTokenType(18);}
"nchar" {return tt.getDataTypeTokenType(19);}
"national"{ws}"character"{ws}"varying" {return tt.getDataTypeTokenType(20);}
"national"{ws}"character" {return tt.getDataTypeTokenType(21);}
"national"{ws}"char"{ws}"varying" {return tt.getDataTypeTokenType(22);}
"national"{ws}"char" {return tt.getDataTypeTokenType(23);}
"long"{ws}"varchar" {return tt.getDataTypeTokenType(24);}
"long"{ws}"raw" {return tt.getDataTypeTokenType(25);}
"long" {return tt.getDataTypeTokenType(26);}
"interval"{ws}"year" {return tt.getDataTypeTokenType(27);}
"interval"{ws}"day" {return tt.getDataTypeTokenType(28);}
"integer" {return tt.getDataTypeTokenType(29);}
"int" {return tt.getDataTypeTokenType(30);}
"float" {return tt.getDataTypeTokenType(31);}
"double"{ws}"precision" {return tt.getDataTypeTokenType(32);}
"decimal" {return tt.getDataTypeTokenType(33);}
"date" {return tt.getDataTypeTokenType(34);}
"clob" {return tt.getDataTypeTokenType(35);}
"character"{ws}"varying" {return tt.getDataTypeTokenType(36);}
"character" {return tt.getDataTypeTokenType(37);}
"char" {return tt.getDataTypeTokenType(38);}
"byte" {return tt.getDataTypeTokenType(39);}
"boolean" {return tt.getDataTypeTokenType(40);}
"blob" {return tt.getDataTypeTokenType(41);}
"binary_integer" {return tt.getDataTypeTokenType(42);}
"binary_float" {return tt.getDataTypeTokenType(43);}
"binary_double" {return tt.getDataTypeTokenType(44);}
"bfile" {return tt.getDataTypeTokenType(45);}




"a"{ws}"set" {return tt.getKeywordTokenType(0);}
"after" {return tt.getKeywordTokenType(1);}
"all" {return tt.getKeywordTokenType(2);}
"alter" {return tt.getKeywordTokenType(3);}
"analyze" {return tt.getKeywordTokenType(4);}
"and" {return tt.getKeywordTokenType(5);}
"any" {return tt.getKeywordTokenType(6);}
"array" {return tt.getKeywordTokenType(7);}
"as" {return tt.getKeywordTokenType(8);}
"asc" {return tt.getKeywordTokenType(9);}
"associate" {return tt.getKeywordTokenType(10);}
"at" {return tt.getKeywordTokenType(11);}
"audit" {return tt.getKeywordTokenType(12);}
"authid" {return tt.getKeywordTokenType(13);}
"automatic" {return tt.getKeywordTokenType(14);}
"autonomous_transaction" {return tt.getKeywordTokenType(15);}
"before" {return tt.getKeywordTokenType(16);}
"begin" {return tt.getKeywordTokenType(17);}
"between" {return tt.getKeywordTokenType(18);}
"block" {return tt.getKeywordTokenType(19);}
"body" {return tt.getKeywordTokenType(20);}
"both" {return tt.getKeywordTokenType(21);}
"bulk" {return tt.getKeywordTokenType(22);}
"bulk_exceptions" {return tt.getKeywordTokenType(23);}
"bulk_rowcount" {return tt.getKeywordTokenType(24);}
"by" {return tt.getKeywordTokenType(25);}
"call" {return tt.getKeywordTokenType(26);}
"canonical" {return tt.getKeywordTokenType(27);}
"case" {return tt.getKeywordTokenType(28);}
"char_base" {return tt.getKeywordTokenType(29);}
"char_cs" {return tt.getKeywordTokenType(30);}
"check" {return tt.getKeywordTokenType(31);}
"chisq_df" {return tt.getKeywordTokenType(32);}
"chisq_obs" {return tt.getKeywordTokenType(33);}
"chisq_sig" {return tt.getKeywordTokenType(34);}
"close" {return tt.getKeywordTokenType(35);}
"cluster" {return tt.getKeywordTokenType(36);}
"coalesce" {return tt.getKeywordTokenType(37);}
"coefficient" {return tt.getKeywordTokenType(38);}
"cohens_k" {return tt.getKeywordTokenType(39);}
"collect" {return tt.getKeywordTokenType(40);}
"comment" {return tt.getKeywordTokenType(41);}
"commit" {return tt.getKeywordTokenType(42);}
"committed" {return tt.getKeywordTokenType(43);}
"compatibility" {return tt.getKeywordTokenType(44);}
"compound" {return tt.getKeywordTokenType(45);}
"compress" {return tt.getKeywordTokenType(46);}
"connect" {return tt.getKeywordTokenType(47);}
"constant" {return tt.getKeywordTokenType(48);}
"constraint" {return tt.getKeywordTokenType(49);}
"cont_coefficient" {return tt.getKeywordTokenType(50);}
"count" {return tt.getKeywordTokenType(51);}
"cramers_v" {return tt.getKeywordTokenType(52);}
"create" {return tt.getKeywordTokenType(53);}
"cross" {return tt.getKeywordTokenType(54);}
"cube" {return tt.getKeywordTokenType(55);}
"current" {return tt.getKeywordTokenType(56);}
"current_user" {return tt.getKeywordTokenType(57);}
"currval" {return tt.getKeywordTokenType(58);}
"cursor" {return tt.getKeywordTokenType(59);}
"database" {return tt.getKeywordTokenType(60);}
"day" {return tt.getKeywordTokenType(61);}
"db_role_change" {return tt.getKeywordTokenType(62);}
"ddl" {return tt.getKeywordTokenType(63);}
"declare" {return tt.getKeywordTokenType(64);}
"decrement" {return tt.getKeywordTokenType(65);}
"default" {return tt.getKeywordTokenType(66);}
"definer" {return tt.getKeywordTokenType(67);}
"delete" {return tt.getKeywordTokenType(68);}
"dense_rank" {return tt.getKeywordTokenType(69);}
"desc" {return tt.getKeywordTokenType(70);}
"deterministic" {return tt.getKeywordTokenType(71);}
"df" {return tt.getKeywordTokenType(72);}
"df_between" {return tt.getKeywordTokenType(73);}
"df_den" {return tt.getKeywordTokenType(74);}
"df_num" {return tt.getKeywordTokenType(75);}
"df_within" {return tt.getKeywordTokenType(76);}
"dimension" {return tt.getKeywordTokenType(77);}
"disable" {return tt.getKeywordTokenType(78);}
"disassociate" {return tt.getKeywordTokenType(79);}
"distinct" {return tt.getKeywordTokenType(80);}
"do" {return tt.getKeywordTokenType(81);}
"drop" {return tt.getKeywordTokenType(82);}
"dump" {return tt.getKeywordTokenType(83);}
"each" {return tt.getKeywordTokenType(84);}
"else" {return tt.getKeywordTokenType(85);}
"elsif" {return tt.getKeywordTokenType(86);}
"empty" {return tt.getKeywordTokenType(87);}
"enable" {return tt.getKeywordTokenType(88);}
"end" {return tt.getKeywordTokenType(89);}
"equals_path" {return tt.getKeywordTokenType(90);}
"error_code" {return tt.getKeywordTokenType(91);}
"error_index" {return tt.getKeywordTokenType(92);}
"errors" {return tt.getKeywordTokenType(93);}
"escape" {return tt.getKeywordTokenType(94);}
"exact_prob" {return tt.getKeywordTokenType(95);}
"exception" {return tt.getKeywordTokenType(96);}
"exception_init" {return tt.getKeywordTokenType(97);}
"exceptions" {return tt.getKeywordTokenType(98);}
"exclusive" {return tt.getKeywordTokenType(99);}
"execute" {return tt.getKeywordTokenType(100);}
"exists" {return tt.getKeywordTokenType(101);}
"exit" {return tt.getKeywordTokenType(102);}
"extend" {return tt.getKeywordTokenType(103);}
"extends" {return tt.getKeywordTokenType(104);}
"f_ratio" {return tt.getKeywordTokenType(105);}
"fetch" {return tt.getKeywordTokenType(106);}
"first" {return tt.getKeywordTokenType(107);}
"following" {return tt.getKeywordTokenType(108);}
"follows" {return tt.getKeywordTokenType(109);}
"for" {return tt.getKeywordTokenType(110);}
"forall" {return tt.getKeywordTokenType(111);}
"found" {return tt.getKeywordTokenType(112);}
"from" {return tt.getKeywordTokenType(113);}
"full" {return tt.getKeywordTokenType(114);}
"function" {return tt.getKeywordTokenType(115);}
"goto" {return tt.getKeywordTokenType(116);}
"grant" {return tt.getKeywordTokenType(117);}
"group" {return tt.getKeywordTokenType(118);}
"having" {return tt.getKeywordTokenType(119);}
"heap" {return tt.getKeywordTokenType(120);}
"hour" {return tt.getKeywordTokenType(121);}
"if" {return tt.getKeywordTokenType(122);}
"ignore" {return tt.getKeywordTokenType(123);}
"immediate" {return tt.getKeywordTokenType(124);}
"in" {return tt.getKeywordTokenType(125);}
"in"{ws}"out" {return tt.getKeywordTokenType(126);}
"increment" {return tt.getKeywordTokenType(127);}
"index" {return tt.getKeywordTokenType(128);}
"indicator" {return tt.getKeywordTokenType(129);}
"indices" {return tt.getKeywordTokenType(130);}
"infinite" {return tt.getKeywordTokenType(131);}
"inner" {return tt.getKeywordTokenType(132);}
"insert" {return tt.getKeywordTokenType(133);}
"instead" {return tt.getKeywordTokenType(134);}
"interface" {return tt.getKeywordTokenType(135);}
"intersect" {return tt.getKeywordTokenType(136);}
"interval" {return tt.getKeywordTokenType(137);}
"into" {return tt.getKeywordTokenType(138);}
"is" {return tt.getKeywordTokenType(139);}
"isolation" {return tt.getKeywordTokenType(140);}
"isopen" {return tt.getKeywordTokenType(141);}
"iterate" {return tt.getKeywordTokenType(142);}
"java" {return tt.getKeywordTokenType(143);}
"join" {return tt.getKeywordTokenType(144);}
"keep" {return tt.getKeywordTokenType(145);}
"last" {return tt.getKeywordTokenType(146);}
"leading" {return tt.getKeywordTokenType(147);}
"left" {return tt.getKeywordTokenType(148);}
"level" {return tt.getKeywordTokenType(149);}
"like" {return tt.getKeywordTokenType(150);}
"like2" {return tt.getKeywordTokenType(151);}
"like4" {return tt.getKeywordTokenType(152);}
"likec" {return tt.getKeywordTokenType(153);}
"limit" {return tt.getKeywordTokenType(154);}
"limited" {return tt.getKeywordTokenType(155);}
"local" {return tt.getKeywordTokenType(156);}
"lock" {return tt.getKeywordTokenType(157);}
"log" {return tt.getKeywordTokenType(158);}
"logoff" {return tt.getKeywordTokenType(159);}
"logon" {return tt.getKeywordTokenType(160);}
"loop" {return tt.getKeywordTokenType(161);}
"main" {return tt.getKeywordTokenType(162);}
"matched" {return tt.getKeywordTokenType(163);}
"maxvalue" {return tt.getKeywordTokenType(164);}
"mean_squares_between" {return tt.getKeywordTokenType(165);}
"mean_squares_within" {return tt.getKeywordTokenType(166);}
"measures" {return tt.getKeywordTokenType(167);}
"member" {return tt.getKeywordTokenType(168);}
"merge" {return tt.getKeywordTokenType(169);}
"minus" {return tt.getKeywordTokenType(170);}
"minute" {return tt.getKeywordTokenType(171);}
"minvalue" {return tt.getKeywordTokenType(172);}
"mlslabel" {return tt.getKeywordTokenType(173);}
"mode" {return tt.getKeywordTokenType(174);}
"model" {return tt.getKeywordTokenType(175);}
"month" {return tt.getKeywordTokenType(176);}
"multiset" {return tt.getKeywordTokenType(177);}
"name" {return tt.getKeywordTokenType(178);}
"nan" {return tt.getKeywordTokenType(179);}
"natural" {return tt.getKeywordTokenType(180);}
"naturaln" {return tt.getKeywordTokenType(181);}
"nav" {return tt.getKeywordTokenType(182);}
"nchar_cs" {return tt.getKeywordTokenType(183);}
"nested" {return tt.getKeywordTokenType(184);}
"new" {return tt.getKeywordTokenType(185);}
"next" {return tt.getKeywordTokenType(186);}
"nextval" {return tt.getKeywordTokenType(187);}
"noaudit" {return tt.getKeywordTokenType(188);}
"nocopy" {return tt.getKeywordTokenType(189);}
"nocycle" {return tt.getKeywordTokenType(190);}
"not" {return tt.getKeywordTokenType(191);}
"notfound" {return tt.getKeywordTokenType(192);}
"nowait" {return tt.getKeywordTokenType(193);}
"null" {return tt.getKeywordTokenType(194);}
"nulls" {return tt.getKeywordTokenType(195);}
"number_base" {return tt.getKeywordTokenType(196);}
"ocirowid" {return tt.getKeywordTokenType(197);}
"of" {return tt.getKeywordTokenType(198);}
"old" {return tt.getKeywordTokenType(199);}
"on" {return tt.getKeywordTokenType(200);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(201);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(202);}
"one_sided_sig" {return tt.getKeywordTokenType(203);}
"only" {return tt.getKeywordTokenType(204);}
"opaque" {return tt.getKeywordTokenType(205);}
"open" {return tt.getKeywordTokenType(206);}
"operator" {return tt.getKeywordTokenType(207);}
"option" {return tt.getKeywordTokenType(208);}
"or" {return tt.getKeywordTokenType(209);}
"order" {return tt.getKeywordTokenType(210);}
"organization" {return tt.getKeywordTokenType(211);}
"others" {return tt.getKeywordTokenType(212);}
"out" {return tt.getKeywordTokenType(213);}
"outer" {return tt.getKeywordTokenType(214);}
"over" {return tt.getKeywordTokenType(215);}
"package" {return tt.getKeywordTokenType(216);}
"parallel_enable" {return tt.getKeywordTokenType(217);}
"parent" {return tt.getKeywordTokenType(218);}
"partition" {return tt.getKeywordTokenType(219);}
"pctfree" {return tt.getKeywordTokenType(220);}
"phi_coefficient" {return tt.getKeywordTokenType(221);}
"positive" {return tt.getKeywordTokenType(222);}
"positiven" {return tt.getKeywordTokenType(223);}
"power" {return tt.getKeywordTokenType(224);}
"pragma" {return tt.getKeywordTokenType(225);}
"preceding" {return tt.getKeywordTokenType(226);}
"present" {return tt.getKeywordTokenType(227);}
"prior" {return tt.getKeywordTokenType(228);}
"private" {return tt.getKeywordTokenType(229);}
"procedure" {return tt.getKeywordTokenType(230);}
"public" {return tt.getKeywordTokenType(231);}
"raise" {return tt.getKeywordTokenType(232);}
"range" {return tt.getKeywordTokenType(233);}
"rank" {return tt.getKeywordTokenType(234);}
"read" {return tt.getKeywordTokenType(235);}
"record" {return tt.getKeywordTokenType(236);}
"ref" {return tt.getKeywordTokenType(237);}
"reference" {return tt.getKeywordTokenType(238);}
"referencing" {return tt.getKeywordTokenType(239);}
"regexp_like" {return tt.getKeywordTokenType(240);}
"reject" {return tt.getKeywordTokenType(241);}
"release" {return tt.getKeywordTokenType(242);}
"remainder" {return tt.getKeywordTokenType(243);}
"rename" {return tt.getKeywordTokenType(244);}
"replace" {return tt.getKeywordTokenType(245);}
"restrict_references" {return tt.getKeywordTokenType(246);}
"return" {return tt.getKeywordTokenType(247);}
"returning" {return tt.getKeywordTokenType(248);}
"reverse" {return tt.getKeywordTokenType(249);}
"revoke" {return tt.getKeywordTokenType(250);}
"right" {return tt.getKeywordTokenType(251);}
"rnds" {return tt.getKeywordTokenType(252);}
"rnps" {return tt.getKeywordTokenType(253);}
"rollback" {return tt.getKeywordTokenType(254);}
"rollup" {return tt.getKeywordTokenType(255);}
"row" {return tt.getKeywordTokenType(256);}
"rowcount" {return tt.getKeywordTokenType(257);}
"rownum" {return tt.getKeywordTokenType(258);}
"rows" {return tt.getKeywordTokenType(259);}
"rowtype" {return tt.getKeywordTokenType(260);}
"rules" {return tt.getKeywordTokenType(261);}
"sample" {return tt.getKeywordTokenType(262);}
"save" {return tt.getKeywordTokenType(263);}
"savepoint" {return tt.getKeywordTokenType(264);}
"schema" {return tt.getKeywordTokenType(265);}
"scn" {return tt.getKeywordTokenType(266);}
"second" {return tt.getKeywordTokenType(267);}
"seed" {return tt.getKeywordTokenType(268);}
"segment" {return tt.getKeywordTokenType(269);}
"select" {return tt.getKeywordTokenType(270);}
"separate" {return tt.getKeywordTokenType(271);}
"sequential" {return tt.getKeywordTokenType(272);}
"serializable" {return tt.getKeywordTokenType(273);}
"serially_reusable" {return tt.getKeywordTokenType(274);}
"servererror" {return tt.getKeywordTokenType(275);}
"set" {return tt.getKeywordTokenType(276);}
"sets" {return tt.getKeywordTokenType(277);}
"share" {return tt.getKeywordTokenType(278);}
"shutdown" {return tt.getKeywordTokenType(279);}
"siblings" {return tt.getKeywordTokenType(280);}
"sig" {return tt.getKeywordTokenType(281);}
"single" {return tt.getKeywordTokenType(282);}
"some" {return tt.getKeywordTokenType(283);}
"space" {return tt.getKeywordTokenType(284);}
"sql" {return tt.getKeywordTokenType(285);}
"sqlcode" {return tt.getKeywordTokenType(286);}
"sqlerrm" {return tt.getKeywordTokenType(287);}
"start" {return tt.getKeywordTokenType(288);}
"startup" {return tt.getKeywordTokenType(289);}
"statement" {return tt.getKeywordTokenType(290);}
"statistic" {return tt.getKeywordTokenType(291);}
"statistics" {return tt.getKeywordTokenType(292);}
"submultiset" {return tt.getKeywordTokenType(293);}
"subpartition" {return tt.getKeywordTokenType(294);}
"subtype" {return tt.getKeywordTokenType(295);}
"successful" {return tt.getKeywordTokenType(296);}
"sum_squares_between" {return tt.getKeywordTokenType(297);}
"sum_squares_within" {return tt.getKeywordTokenType(298);}
"suspend" {return tt.getKeywordTokenType(299);}
"synonym" {return tt.getKeywordTokenType(300);}
"table" {return tt.getKeywordTokenType(301);}
"then" {return tt.getKeywordTokenType(302);}
"time" {return tt.getKeywordTokenType(303);}
"timezone_abbr" {return tt.getKeywordTokenType(304);}
"timezone_hour" {return tt.getKeywordTokenType(305);}
"timezone_minute" {return tt.getKeywordTokenType(306);}
"timezone_region" {return tt.getKeywordTokenType(307);}
"to" {return tt.getKeywordTokenType(308);}
"trailing" {return tt.getKeywordTokenType(309);}
"transaction" {return tt.getKeywordTokenType(310);}
"trigger" {return tt.getKeywordTokenType(311);}
"truncate" {return tt.getKeywordTokenType(312);}
"trust" {return tt.getKeywordTokenType(313);}
"two_sided_prob" {return tt.getKeywordTokenType(314);}
"two_sided_sig" {return tt.getKeywordTokenType(315);}
"type" {return tt.getKeywordTokenType(316);}
"u_statistic" {return tt.getKeywordTokenType(317);}
"unbounded" {return tt.getKeywordTokenType(318);}
"under_path" {return tt.getKeywordTokenType(319);}
"union" {return tt.getKeywordTokenType(320);}
"unique" {return tt.getKeywordTokenType(321);}
"unlimited" {return tt.getKeywordTokenType(322);}
"until" {return tt.getKeywordTokenType(323);}
"update" {return tt.getKeywordTokenType(324);}
"updated" {return tt.getKeywordTokenType(325);}
"upsert" {return tt.getKeywordTokenType(326);}
"use" {return tt.getKeywordTokenType(327);}
"user" {return tt.getKeywordTokenType(328);}
"using" {return tt.getKeywordTokenType(329);}
"validate" {return tt.getKeywordTokenType(330);}
"value" {return tt.getKeywordTokenType(331);}
"values" {return tt.getKeywordTokenType(332);}
"varray" {return tt.getKeywordTokenType(333);}
"varying" {return tt.getKeywordTokenType(334);}
"versions" {return tt.getKeywordTokenType(335);}
"view" {return tt.getKeywordTokenType(336);}
"wait" {return tt.getKeywordTokenType(337);}
"when" {return tt.getKeywordTokenType(338);}
"whenever" {return tt.getKeywordTokenType(339);}
"where" {return tt.getKeywordTokenType(340);}
"while" {return tt.getKeywordTokenType(341);}
"with" {return tt.getKeywordTokenType(342);}
"within" {return tt.getKeywordTokenType(343);}
"wnds" {return tt.getKeywordTokenType(344);}
"wnps" {return tt.getKeywordTokenType(345);}
"work" {return tt.getKeywordTokenType(346);}
"write" {return tt.getKeywordTokenType(347);}
"year" {return tt.getKeywordTokenType(348);}
"zone" {return tt.getKeywordTokenType(349);}
"false" {return tt.getKeywordTokenType(350);}
"true" {return tt.getKeywordTokenType(351);}





"abs" {return tt.getFunctionTokenType(0);}
"acos" {return tt.getFunctionTokenType(1);}
"add_months" {return tt.getFunctionTokenType(2);}
"ascii" {return tt.getFunctionTokenType(3);}
"asciistr" {return tt.getFunctionTokenType(4);}
"asin" {return tt.getFunctionTokenType(5);}
"atan" {return tt.getFunctionTokenType(6);}
"atan2" {return tt.getFunctionTokenType(7);}
"avg" {return tt.getFunctionTokenType(8);}
"bfilename" {return tt.getFunctionTokenType(9);}
"bin_to_num" {return tt.getFunctionTokenType(10);}
"bitand" {return tt.getFunctionTokenType(11);}
"cardinality" {return tt.getFunctionTokenType(12);}
"cast" {return tt.getFunctionTokenType(13);}
"ceil" {return tt.getFunctionTokenType(14);}
"chartorowid" {return tt.getFunctionTokenType(15);}
"chr" {return tt.getFunctionTokenType(16);}
"compose" {return tt.getFunctionTokenType(17);}
"concat" {return tt.getFunctionTokenType(18);}
"convert" {return tt.getFunctionTokenType(19);}
"corr" {return tt.getFunctionTokenType(20);}
"corr_k" {return tt.getFunctionTokenType(21);}
"corr_s" {return tt.getFunctionTokenType(22);}
"cos" {return tt.getFunctionTokenType(23);}
"cosh" {return tt.getFunctionTokenType(24);}
"covar_pop" {return tt.getFunctionTokenType(25);}
"covar_samp" {return tt.getFunctionTokenType(26);}
"cume_dist" {return tt.getFunctionTokenType(27);}
"current_date" {return tt.getFunctionTokenType(28);}
"current_timestamp" {return tt.getFunctionTokenType(29);}
"cv" {return tt.getFunctionTokenType(30);}
"dbtimezone" {return tt.getFunctionTokenType(31);}
"dbtmezone" {return tt.getFunctionTokenType(32);}
"decode" {return tt.getFunctionTokenType(33);}
"decompose" {return tt.getFunctionTokenType(34);}
"depth" {return tt.getFunctionTokenType(35);}
"deref" {return tt.getFunctionTokenType(36);}
"empty_blob" {return tt.getFunctionTokenType(37);}
"empty_clob" {return tt.getFunctionTokenType(38);}
"existsnode" {return tt.getFunctionTokenType(39);}
"exp" {return tt.getFunctionTokenType(40);}
"extract" {return tt.getFunctionTokenType(41);}
"extractvalue" {return tt.getFunctionTokenType(42);}
"first_value" {return tt.getFunctionTokenType(43);}
"floor" {return tt.getFunctionTokenType(44);}
"from_tz" {return tt.getFunctionTokenType(45);}
"greatest" {return tt.getFunctionTokenType(46);}
"group_id" {return tt.getFunctionTokenType(47);}
"grouping" {return tt.getFunctionTokenType(48);}
"grouping_id" {return tt.getFunctionTokenType(49);}
"hextoraw" {return tt.getFunctionTokenType(50);}
"initcap" {return tt.getFunctionTokenType(51);}
"instr" {return tt.getFunctionTokenType(52);}
"instr2" {return tt.getFunctionTokenType(53);}
"instr4" {return tt.getFunctionTokenType(54);}
"instrb" {return tt.getFunctionTokenType(55);}
"instrc" {return tt.getFunctionTokenType(56);}
"iteration_number" {return tt.getFunctionTokenType(57);}
"lag" {return tt.getFunctionTokenType(58);}
"last_day" {return tt.getFunctionTokenType(59);}
"last_value" {return tt.getFunctionTokenType(60);}
"lead" {return tt.getFunctionTokenType(61);}
"least" {return tt.getFunctionTokenType(62);}
"length" {return tt.getFunctionTokenType(63);}
"length2" {return tt.getFunctionTokenType(64);}
"length4" {return tt.getFunctionTokenType(65);}
"lengthb" {return tt.getFunctionTokenType(66);}
"lengthc" {return tt.getFunctionTokenType(67);}
"ln" {return tt.getFunctionTokenType(68);}
"lnnvl" {return tt.getFunctionTokenType(69);}
"localtimestamp" {return tt.getFunctionTokenType(70);}
"lower" {return tt.getFunctionTokenType(71);}
"lpad" {return tt.getFunctionTokenType(72);}
"ltrim" {return tt.getFunctionTokenType(73);}
"make_ref" {return tt.getFunctionTokenType(74);}
"max" {return tt.getFunctionTokenType(75);}
"median" {return tt.getFunctionTokenType(76);}
"min" {return tt.getFunctionTokenType(77);}
"mod" {return tt.getFunctionTokenType(78);}
"months_between" {return tt.getFunctionTokenType(79);}
"nanvl" {return tt.getFunctionTokenType(80);}
"nchr" {return tt.getFunctionTokenType(81);}
"new_time" {return tt.getFunctionTokenType(82);}
"next_day" {return tt.getFunctionTokenType(83);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(84);}
"nls_charset_id" {return tt.getFunctionTokenType(85);}
"nls_charset_name" {return tt.getFunctionTokenType(86);}
"nls_initcap" {return tt.getFunctionTokenType(87);}
"nls_lower" {return tt.getFunctionTokenType(88);}
"nls_upper" {return tt.getFunctionTokenType(89);}
"nlssort" {return tt.getFunctionTokenType(90);}
"ntile" {return tt.getFunctionTokenType(91);}
"nullif" {return tt.getFunctionTokenType(92);}
"numtodsinterval" {return tt.getFunctionTokenType(93);}
"numtoyminterval" {return tt.getFunctionTokenType(94);}
"nvl" {return tt.getFunctionTokenType(95);}
"nvl2" {return tt.getFunctionTokenType(96);}
"ora_hash" {return tt.getFunctionTokenType(97);}
"path" {return tt.getFunctionTokenType(98);}
"percent_rank" {return tt.getFunctionTokenType(99);}
"percentile_cont" {return tt.getFunctionTokenType(100);}
"percentile_disc" {return tt.getFunctionTokenType(101);}
"powermultiset" {return tt.getFunctionTokenType(102);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(103);}
"presentnnv" {return tt.getFunctionTokenType(104);}
"presentv" {return tt.getFunctionTokenType(105);}
"previous" {return tt.getFunctionTokenType(106);}
"ratio_to_report" {return tt.getFunctionTokenType(107);}
"rawtohex" {return tt.getFunctionTokenType(108);}
"rawtonhex" {return tt.getFunctionTokenType(109);}
"reftohex" {return tt.getFunctionTokenType(110);}
"regexp_instr" {return tt.getFunctionTokenType(111);}
"regexp_replace" {return tt.getFunctionTokenType(112);}
"regexp_substr" {return tt.getFunctionTokenType(113);}
"regr_avgx" {return tt.getFunctionTokenType(114);}
"regr_avgy" {return tt.getFunctionTokenType(115);}
"regr_count" {return tt.getFunctionTokenType(116);}
"regr_intercept" {return tt.getFunctionTokenType(117);}
"regr_r2" {return tt.getFunctionTokenType(118);}
"regr_slope" {return tt.getFunctionTokenType(119);}
"regr_sxx" {return tt.getFunctionTokenType(120);}
"regr_sxy" {return tt.getFunctionTokenType(121);}
"regr_syy" {return tt.getFunctionTokenType(122);}
"round" {return tt.getFunctionTokenType(123);}
"row_number" {return tt.getFunctionTokenType(124);}
"rowidtochar" {return tt.getFunctionTokenType(125);}
"rowidtonchar" {return tt.getFunctionTokenType(126);}
"rpad" {return tt.getFunctionTokenType(127);}
"rtrim" {return tt.getFunctionTokenType(128);}
"scn_to_timestamp" {return tt.getFunctionTokenType(129);}
"sessiontimezone" {return tt.getFunctionTokenType(130);}
"sign" {return tt.getFunctionTokenType(131);}
"sin" {return tt.getFunctionTokenType(132);}
"sinh" {return tt.getFunctionTokenType(133);}
"soundex" {return tt.getFunctionTokenType(134);}
"sqrt" {return tt.getFunctionTokenType(135);}
"stats_binomial_test" {return tt.getFunctionTokenType(136);}
"stats_crosstab" {return tt.getFunctionTokenType(137);}
"stats_f_test" {return tt.getFunctionTokenType(138);}
"stats_ks_test" {return tt.getFunctionTokenType(139);}
"stats_mode" {return tt.getFunctionTokenType(140);}
"stats_mw_test" {return tt.getFunctionTokenType(141);}
"stats_one_way_anova" {return tt.getFunctionTokenType(142);}
"stats_t_test_indep" {return tt.getFunctionTokenType(143);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(144);}
"stats_t_test_one" {return tt.getFunctionTokenType(145);}
"stats_t_test_paired" {return tt.getFunctionTokenType(146);}
"stats_wsr_test" {return tt.getFunctionTokenType(147);}
"stddev" {return tt.getFunctionTokenType(148);}
"stddev_pop" {return tt.getFunctionTokenType(149);}
"stddev_samp" {return tt.getFunctionTokenType(150);}
"substr" {return tt.getFunctionTokenType(151);}
"substr2" {return tt.getFunctionTokenType(152);}
"substr4" {return tt.getFunctionTokenType(153);}
"substrb" {return tt.getFunctionTokenType(154);}
"substrc" {return tt.getFunctionTokenType(155);}
"sum" {return tt.getFunctionTokenType(156);}
"sys_connect_by_path" {return tt.getFunctionTokenType(157);}
"sys_context" {return tt.getFunctionTokenType(158);}
"sys_dburigen" {return tt.getFunctionTokenType(159);}
"sys_extract_utc" {return tt.getFunctionTokenType(160);}
"sys_guid" {return tt.getFunctionTokenType(161);}
"sys_typeid" {return tt.getFunctionTokenType(162);}
"sys_xmlagg" {return tt.getFunctionTokenType(163);}
"sys_xmlgen" {return tt.getFunctionTokenType(164);}
"sysdate" {return tt.getFunctionTokenType(165);}
"systimestamp" {return tt.getFunctionTokenType(166);}
"tan" {return tt.getFunctionTokenType(167);}
"tanh" {return tt.getFunctionTokenType(168);}
"timestamp_to_scn" {return tt.getFunctionTokenType(169);}
"to_binary_double" {return tt.getFunctionTokenType(170);}
"to_binary_float" {return tt.getFunctionTokenType(171);}
"to_char" {return tt.getFunctionTokenType(172);}
"to_clob" {return tt.getFunctionTokenType(173);}
"to_date" {return tt.getFunctionTokenType(174);}
"to_dsinterval" {return tt.getFunctionTokenType(175);}
"to_lob" {return tt.getFunctionTokenType(176);}
"to_multi_byte" {return tt.getFunctionTokenType(177);}
"to_nchar" {return tt.getFunctionTokenType(178);}
"to_nclob" {return tt.getFunctionTokenType(179);}
"to_number" {return tt.getFunctionTokenType(180);}
"to_single_byte" {return tt.getFunctionTokenType(181);}
"to_timestamp" {return tt.getFunctionTokenType(182);}
"to_timestamp_tz" {return tt.getFunctionTokenType(183);}
"to_yminterval" {return tt.getFunctionTokenType(184);}
"translate" {return tt.getFunctionTokenType(185);}
"treat" {return tt.getFunctionTokenType(186);}
"trim" {return tt.getFunctionTokenType(187);}
"trunc" {return tt.getFunctionTokenType(188);}
"tz_offset" {return tt.getFunctionTokenType(189);}
"uid" {return tt.getFunctionTokenType(190);}
"unistr" {return tt.getFunctionTokenType(191);}
"updatexml" {return tt.getFunctionTokenType(192);}
"upper" {return tt.getFunctionTokenType(193);}
"userenv" {return tt.getFunctionTokenType(194);}
"var_pop" {return tt.getFunctionTokenType(195);}
"var_samp" {return tt.getFunctionTokenType(196);}
"variance" {return tt.getFunctionTokenType(197);}
"vsize" {return tt.getFunctionTokenType(198);}
"width_bucket" {return tt.getFunctionTokenType(199);}
"xmlagg" {return tt.getFunctionTokenType(200);}
"xmlcolattval" {return tt.getFunctionTokenType(201);}
"xmlconcat" {return tt.getFunctionTokenType(202);}
"xmlforest" {return tt.getFunctionTokenType(203);}
"xmlsequence" {return tt.getFunctionTokenType(204);}
"xmltransform" {return tt.getFunctionTokenType(205);}





"access_into_null" {return tt.getExceptionTokenType(0);}
"case_not_found" {return tt.getExceptionTokenType(1);}
"collection_is_null" {return tt.getExceptionTokenType(2);}
"cursor_already_open" {return tt.getExceptionTokenType(3);}
"dup_val_on_index" {return tt.getExceptionTokenType(4);}
"invalid_cursor" {return tt.getExceptionTokenType(5);}
"invalid_number" {return tt.getExceptionTokenType(6);}
"login_denied" {return tt.getExceptionTokenType(7);}
"no_data_found" {return tt.getExceptionTokenType(8);}
"not_logged_on" {return tt.getExceptionTokenType(9);}
"program_error" {return tt.getExceptionTokenType(10);}
"rowtype_mismatch" {return tt.getExceptionTokenType(11);}
"self_is_null" {return tt.getExceptionTokenType(12);}
"storage_error" {return tt.getExceptionTokenType(13);}
"subscript_beyond_count" {return tt.getExceptionTokenType(14);}
"subscript_outside_limit" {return tt.getExceptionTokenType(15);}
"sys_invalid_rowid" {return tt.getExceptionTokenType(16);}
"timeout_on_resource" {return tt.getExceptionTokenType(17);}
"too_many_rows" {return tt.getExceptionTokenType(18);}
"value_error" {return tt.getExceptionTokenType(19);}
"zero_divide" {return tt.getExceptionTokenType(20);}



{IDENTIFIER}           { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}    { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getQuotedIdentifier(); }

<YYINITIAL> {
    .                  { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
}
