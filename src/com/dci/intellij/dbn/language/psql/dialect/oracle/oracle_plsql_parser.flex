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

string_simple_quoted      = "'"([^\']|"''")*"'"?
string_alternative_quoted = "q'["[^\[\]]*"]'"? | "q'("[^\(\)]*")'"? | "q'{"[^\{\}]*"}'"? | "q'!"[^\!]*"!'"? | "q'<"[^\<\>]*">'"? | "q'|"[^|]*"|'"?
STRING = "n"?({string_alternative_quoted}|{string_simple_quoted})

sign = "+"|"-"
digit = [0-9]
INTEGER = {digit}+("e"{sign}?{digit}+)?
NUMBER = {INTEGER}?"."{digit}+(("e"{sign}?{digit}+)|(("f"|"d"){ws}))?

VARIABLE = ":"{INTEGER}
SQLP_VARIABLE = "&""&"?{IDENTIFIER}

%state PLSQL, WRAPPED
%%

<WRAPPED> {
    .*               { return tt.getSharedTokenTypes().getLineComment(); }
    .               { return tt.getSharedTokenTypes().getLineComment(); }
}


{WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

{BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
{LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
{REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

"wrapped"            { yybegin(WRAPPED); return tt.getTokenType("KW_WRAPPED");}

{VARIABLE}          {return tt.getSharedTokenTypes().getVariable(); }
{SQLP_VARIABLE}     {return tt.getSharedTokenTypes().getVariable(); }


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
"bfile" {return tt.getDataTypeTokenType(1);}
"binary_double" {return tt.getDataTypeTokenType(2);}
"binary_float" {return tt.getDataTypeTokenType(3);}
"binary_integer" {return tt.getDataTypeTokenType(4);}
"blob" {return tt.getDataTypeTokenType(5);}
"boolean" {return tt.getDataTypeTokenType(6);}
"byte" {return tt.getDataTypeTokenType(7);}
"char" {return tt.getDataTypeTokenType(8);}
"character" {return tt.getDataTypeTokenType(9);}
"character"{ws}"varying" {return tt.getDataTypeTokenType(10);}
"clob" {return tt.getDataTypeTokenType(11);}
"date" {return tt.getDataTypeTokenType(12);}
"decimal" {return tt.getDataTypeTokenType(13);}
"double"{ws}"precision" {return tt.getDataTypeTokenType(14);}
"float" {return tt.getDataTypeTokenType(15);}
"int" {return tt.getDataTypeTokenType(16);}
"integer" {return tt.getDataTypeTokenType(17);}
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
"pls_integer" {return tt.getDataTypeTokenType(31);}
"raw" {return tt.getDataTypeTokenType(32);}
"real" {return tt.getDataTypeTokenType(33);}
"rowid" {return tt.getDataTypeTokenType(34);}
"smallint" {return tt.getDataTypeTokenType(35);}
"string" {return tt.getDataTypeTokenType(36);}
"timestamp" {return tt.getDataTypeTokenType(37);}
"urowid" {return tt.getDataTypeTokenType(38);}
"varchar" {return tt.getDataTypeTokenType(39);}
"with"{ws}"local"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(40);}
"with"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(41);}





"a set" {return tt.getKeywordTokenType(0);}
"absent" {return tt.getKeywordTokenType(1);}
"after" {return tt.getKeywordTokenType(2);}
"agent" {return tt.getKeywordTokenType(3);}
"all" {return tt.getKeywordTokenType(4);}
"alter" {return tt.getKeywordTokenType(5);}
"analyze" {return tt.getKeywordTokenType(6);}
"and" {return tt.getKeywordTokenType(7);}
"any" {return tt.getKeywordTokenType(8);}
"array" {return tt.getKeywordTokenType(9);}
"as" {return tt.getKeywordTokenType(10);}
"asc" {return tt.getKeywordTokenType(11);}
"associate" {return tt.getKeywordTokenType(12);}
"at" {return tt.getKeywordTokenType(13);}
"audit" {return tt.getKeywordTokenType(14);}
"authid" {return tt.getKeywordTokenType(15);}
"automatic" {return tt.getKeywordTokenType(16);}
"autonomous_transaction" {return tt.getKeywordTokenType(17);}
"before" {return tt.getKeywordTokenType(18);}
"begin" {return tt.getKeywordTokenType(19);}
"between" {return tt.getKeywordTokenType(20);}
"block" {return tt.getKeywordTokenType(21);}
"body" {return tt.getKeywordTokenType(22);}
"both" {return tt.getKeywordTokenType(23);}
"bulk" {return tt.getKeywordTokenType(24);}
"bulk_exceptions" {return tt.getKeywordTokenType(25);}
"bulk_rowcount" {return tt.getKeywordTokenType(26);}
"by" {return tt.getKeywordTokenType(27);}
"c" {return tt.getKeywordTokenType(28);}
"call" {return tt.getKeywordTokenType(29);}
"canonical" {return tt.getKeywordTokenType(30);}
"case" {return tt.getKeywordTokenType(31);}
"char_base" {return tt.getKeywordTokenType(32);}
"char_cs" {return tt.getKeywordTokenType(33);}
"charsetform" {return tt.getKeywordTokenType(34);}
"charsetid" {return tt.getKeywordTokenType(35);}
"check" {return tt.getKeywordTokenType(36);}
"chisq_df" {return tt.getKeywordTokenType(37);}
"chisq_obs" {return tt.getKeywordTokenType(38);}
"chisq_sig" {return tt.getKeywordTokenType(39);}
"close" {return tt.getKeywordTokenType(40);}
"cluster" {return tt.getKeywordTokenType(41);}
"coalesce" {return tt.getKeywordTokenType(42);}
"coefficient" {return tt.getKeywordTokenType(43);}
"cohens_k" {return tt.getKeywordTokenType(44);}
"collect" {return tt.getKeywordTokenType(45);}
"columns" {return tt.getKeywordTokenType(46);}
"comment" {return tt.getKeywordTokenType(47);}
"commit" {return tt.getKeywordTokenType(48);}
"committed" {return tt.getKeywordTokenType(49);}
"compatibility" {return tt.getKeywordTokenType(50);}
"compound" {return tt.getKeywordTokenType(51);}
"compress" {return tt.getKeywordTokenType(52);}
"connect" {return tt.getKeywordTokenType(53);}
"constant" {return tt.getKeywordTokenType(54);}
"constraint" {return tt.getKeywordTokenType(55);}
"constructor" {return tt.getKeywordTokenType(56);}
"cont_coefficient" {return tt.getKeywordTokenType(57);}
"content" {return tt.getKeywordTokenType(58);}
"context" {return tt.getKeywordTokenType(59);}
"count" {return tt.getKeywordTokenType(60);}
"cramers_v" {return tt.getKeywordTokenType(61);}
"create" {return tt.getKeywordTokenType(62);}
"cross" {return tt.getKeywordTokenType(63);}
"cube" {return tt.getKeywordTokenType(64);}
"current" {return tt.getKeywordTokenType(65);}
"current_user" {return tt.getKeywordTokenType(66);}
"currval" {return tt.getKeywordTokenType(67);}
"cursor" {return tt.getKeywordTokenType(68);}
"database" {return tt.getKeywordTokenType(69);}
"day" {return tt.getKeywordTokenType(70);}
"db_role_change" {return tt.getKeywordTokenType(71);}
"ddl" {return tt.getKeywordTokenType(72);}
"declare" {return tt.getKeywordTokenType(73);}
"decrement" {return tt.getKeywordTokenType(74);}
"default" {return tt.getKeywordTokenType(75);}
"defaults" {return tt.getKeywordTokenType(76);}
"definer" {return tt.getKeywordTokenType(77);}
"delete" {return tt.getKeywordTokenType(78);}
"deleting" {return tt.getKeywordTokenType(79);}
"dense_rank" {return tt.getKeywordTokenType(80);}
"desc" {return tt.getKeywordTokenType(81);}
"deterministic" {return tt.getKeywordTokenType(82);}
"df" {return tt.getKeywordTokenType(83);}
"df_between" {return tt.getKeywordTokenType(84);}
"df_den" {return tt.getKeywordTokenType(85);}
"df_num" {return tt.getKeywordTokenType(86);}
"df_within" {return tt.getKeywordTokenType(87);}
"dimension" {return tt.getKeywordTokenType(88);}
"disable" {return tt.getKeywordTokenType(89);}
"disassociate" {return tt.getKeywordTokenType(90);}
"distinct" {return tt.getKeywordTokenType(91);}
"do" {return tt.getKeywordTokenType(92);}
"document" {return tt.getKeywordTokenType(93);}
"drop" {return tt.getKeywordTokenType(94);}
"dump" {return tt.getKeywordTokenType(95);}
"duration" {return tt.getKeywordTokenType(96);}
"each" {return tt.getKeywordTokenType(97);}
"else" {return tt.getKeywordTokenType(98);}
"elsif" {return tt.getKeywordTokenType(99);}
"empty" {return tt.getKeywordTokenType(100);}
"enable" {return tt.getKeywordTokenType(101);}
"encoding" {return tt.getKeywordTokenType(102);}
"end" {return tt.getKeywordTokenType(103);}
"entityescaping" {return tt.getKeywordTokenType(104);}
"equals_path" {return tt.getKeywordTokenType(105);}
"error_code" {return tt.getKeywordTokenType(106);}
"error_index" {return tt.getKeywordTokenType(107);}
"errors" {return tt.getKeywordTokenType(108);}
"escape" {return tt.getKeywordTokenType(109);}
"evalname" {return tt.getKeywordTokenType(110);}
"exact_prob" {return tt.getKeywordTokenType(111);}
"except" {return tt.getKeywordTokenType(112);}
"exception" {return tt.getKeywordTokenType(113);}
"exception_init" {return tt.getKeywordTokenType(114);}
"exceptions" {return tt.getKeywordTokenType(115);}
"exclude" {return tt.getKeywordTokenType(116);}
"exclusive" {return tt.getKeywordTokenType(117);}
"execute" {return tt.getKeywordTokenType(118);}
"exists" {return tt.getKeywordTokenType(119);}
"exit" {return tt.getKeywordTokenType(120);}
"extend" {return tt.getKeywordTokenType(121);}
"extends" {return tt.getKeywordTokenType(122);}
"external" {return tt.getKeywordTokenType(123);}
"f_ratio" {return tt.getKeywordTokenType(124);}
"fetch" {return tt.getKeywordTokenType(125);}
"final" {return tt.getKeywordTokenType(126);}
"first" {return tt.getKeywordTokenType(127);}
"following" {return tt.getKeywordTokenType(128);}
"follows" {return tt.getKeywordTokenType(129);}
"for" {return tt.getKeywordTokenType(130);}
"forall" {return tt.getKeywordTokenType(131);}
"force" {return tt.getKeywordTokenType(132);}
"found" {return tt.getKeywordTokenType(133);}
"from" {return tt.getKeywordTokenType(134);}
"format" {return tt.getKeywordTokenType(135);}
"full" {return tt.getKeywordTokenType(136);}
"function" {return tt.getKeywordTokenType(137);}
"goto" {return tt.getKeywordTokenType(138);}
"grant" {return tt.getKeywordTokenType(139);}
"group" {return tt.getKeywordTokenType(140);}
"having" {return tt.getKeywordTokenType(141);}
"heap" {return tt.getKeywordTokenType(142);}
"hide" {return tt.getKeywordTokenType(143);}
"hour" {return tt.getKeywordTokenType(144);}
"if" {return tt.getKeywordTokenType(145);}
"ignore" {return tt.getKeywordTokenType(146);}
"immediate" {return tt.getKeywordTokenType(147);}
"in" {return tt.getKeywordTokenType(148);}
"in"{ws}"out" {return tt.getKeywordTokenType(149);}
"include" {return tt.getKeywordTokenType(150);}
"increment" {return tt.getKeywordTokenType(151);}
"indent" {return tt.getKeywordTokenType(152);}
"index" {return tt.getKeywordTokenType(153);}
"indicator" {return tt.getKeywordTokenType(154);}
"indices" {return tt.getKeywordTokenType(155);}
"infinite" {return tt.getKeywordTokenType(156);}
"inline" {return tt.getKeywordTokenType(157);}
"inner" {return tt.getKeywordTokenType(158);}
"insert" {return tt.getKeywordTokenType(159);}
"inserting" {return tt.getKeywordTokenType(160);}
"instantiable" {return tt.getKeywordTokenType(161);}
"instead" {return tt.getKeywordTokenType(162);}
"interface" {return tt.getKeywordTokenType(163);}
"intersect" {return tt.getKeywordTokenType(164);}
"interval" {return tt.getKeywordTokenType(165);}
"into" {return tt.getKeywordTokenType(166);}
"is" {return tt.getKeywordTokenType(167);}
"isolation" {return tt.getKeywordTokenType(168);}
"isopen" {return tt.getKeywordTokenType(169);}
"iterate" {return tt.getKeywordTokenType(170);}
"java" {return tt.getKeywordTokenType(171);}
"join" {return tt.getKeywordTokenType(172);}
"json" {return tt.getKeywordTokenType(173);}
"keep" {return tt.getKeywordTokenType(174);}
"key" {return tt.getKeywordTokenType(175);}
"language" {return tt.getKeywordTokenType(176);}
"last" {return tt.getKeywordTokenType(177);}
"leading" {return tt.getKeywordTokenType(178);}
"left" {return tt.getKeywordTokenType(179);}
"level" {return tt.getKeywordTokenType(180);}
"library" {return tt.getKeywordTokenType(181);}
"like" {return tt.getKeywordTokenType(182);}
"like2" {return tt.getKeywordTokenType(183);}
"like4" {return tt.getKeywordTokenType(184);}
"likec" {return tt.getKeywordTokenType(185);}
"limit" {return tt.getKeywordTokenType(186);}
"limited" {return tt.getKeywordTokenType(187);}
"local" {return tt.getKeywordTokenType(188);}
"lock" {return tt.getKeywordTokenType(189);}
"log" {return tt.getKeywordTokenType(190);}
"logoff" {return tt.getKeywordTokenType(191);}
"logon" {return tt.getKeywordTokenType(192);}
"loop" {return tt.getKeywordTokenType(193);}
"main" {return tt.getKeywordTokenType(194);}
"map" {return tt.getKeywordTokenType(195);}
"matched" {return tt.getKeywordTokenType(196);}
"maxlen" {return tt.getKeywordTokenType(197);}
"maxvalue" {return tt.getKeywordTokenType(198);}
"mean_squares_between" {return tt.getKeywordTokenType(199);}
"mean_squares_within" {return tt.getKeywordTokenType(200);}
"measures" {return tt.getKeywordTokenType(201);}
"member" {return tt.getKeywordTokenType(202);}
"merge" {return tt.getKeywordTokenType(203);}
"minus" {return tt.getKeywordTokenType(204);}
"minute" {return tt.getKeywordTokenType(205);}
"minvalue" {return tt.getKeywordTokenType(206);}
"mlslabel" {return tt.getKeywordTokenType(207);}
"mode" {return tt.getKeywordTokenType(208);}
"model" {return tt.getKeywordTokenType(209);}
"month" {return tt.getKeywordTokenType(210);}
"multiset" {return tt.getKeywordTokenType(211);}
"name" {return tt.getKeywordTokenType(212);}
"nan" {return tt.getKeywordTokenType(213);}
"natural" {return tt.getKeywordTokenType(214);}
"naturaln" {return tt.getKeywordTokenType(215);}
"nav" {return tt.getKeywordTokenType(216);}
"nchar_cs" {return tt.getKeywordTokenType(217);}
"nested" {return tt.getKeywordTokenType(218);}
"new" {return tt.getKeywordTokenType(219);}
"next" {return tt.getKeywordTokenType(220);}
"nextval" {return tt.getKeywordTokenType(221);}
"no" {return tt.getKeywordTokenType(222);}
"noaudit" {return tt.getKeywordTokenType(223);}
"nocopy" {return tt.getKeywordTokenType(224);}
"nocycle" {return tt.getKeywordTokenType(225);}
"noentityescaping" {return tt.getKeywordTokenType(226);}
"noschemacheck" {return tt.getKeywordTokenType(227);}
"not" {return tt.getKeywordTokenType(228);}
"notfound" {return tt.getKeywordTokenType(229);}
"nowait" {return tt.getKeywordTokenType(230);}
"null" {return tt.getKeywordTokenType(231);}
"nulls" {return tt.getKeywordTokenType(232);}
"number_base" {return tt.getKeywordTokenType(233);}
"object" {return tt.getKeywordTokenType(234);}
"ocirowid" {return tt.getKeywordTokenType(235);}
"of" {return tt.getKeywordTokenType(236);}
"offset" {return tt.getKeywordTokenType(237);}
"oid" {return tt.getKeywordTokenType(238);}
"old" {return tt.getKeywordTokenType(239);}
"on" {return tt.getKeywordTokenType(240);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(241);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(242);}
"one_sided_sig" {return tt.getKeywordTokenType(243);}
"only" {return tt.getKeywordTokenType(244);}
"opaque" {return tt.getKeywordTokenType(245);}
"open" {return tt.getKeywordTokenType(246);}
"operator" {return tt.getKeywordTokenType(247);}
"option" {return tt.getKeywordTokenType(248);}
"or" {return tt.getKeywordTokenType(249);}
"order" {return tt.getKeywordTokenType(250);}
"ordinality" {return tt.getKeywordTokenType(251);}
"organization" {return tt.getKeywordTokenType(252);}
"others" {return tt.getKeywordTokenType(253);}
"out" {return tt.getKeywordTokenType(254);}
"outer" {return tt.getKeywordTokenType(255);}
"over" {return tt.getKeywordTokenType(256);}
"overriding" {return tt.getKeywordTokenType(257);}
"package" {return tt.getKeywordTokenType(258);}
"parallel_enable" {return tt.getKeywordTokenType(259);}
"parameters" {return tt.getKeywordTokenType(260);}
"parent" {return tt.getKeywordTokenType(261);}
"partition" {return tt.getKeywordTokenType(262);}
"passing" {return tt.getKeywordTokenType(263);}
"path" {return tt.getKeywordTokenType(264);}
"pctfree" {return tt.getKeywordTokenType(265);}
"percent" {return tt.getKeywordTokenType(266);}
"phi_coefficient" {return tt.getKeywordTokenType(267);}
"pipe" {return tt.getKeywordTokenType(268);}
"pipelined" {return tt.getKeywordTokenType(269);}
"pivot" {return tt.getKeywordTokenType(270);}
"positive" {return tt.getKeywordTokenType(271);}
"positiven" {return tt.getKeywordTokenType(272);}
"power" {return tt.getKeywordTokenType(273);}
"pragma" {return tt.getKeywordTokenType(274);}
"preceding" {return tt.getKeywordTokenType(275);}
"present" {return tt.getKeywordTokenType(276);}
"prior" {return tt.getKeywordTokenType(277);}
"private" {return tt.getKeywordTokenType(278);}
"procedure" {return tt.getKeywordTokenType(279);}
"public" {return tt.getKeywordTokenType(280);}
"raise" {return tt.getKeywordTokenType(281);}
"range" {return tt.getKeywordTokenType(282);}
"read" {return tt.getKeywordTokenType(283);}
"record" {return tt.getKeywordTokenType(284);}
"ref" {return tt.getKeywordTokenType(285);}
"reference" {return tt.getKeywordTokenType(286);}
"referencing" {return tt.getKeywordTokenType(287);}
"regexp_like" {return tt.getKeywordTokenType(288);}
"reject" {return tt.getKeywordTokenType(289);}
"release" {return tt.getKeywordTokenType(290);}
"relies_on" {return tt.getKeywordTokenType(291);}
"remainder" {return tt.getKeywordTokenType(292);}
"rename" {return tt.getKeywordTokenType(293);}
"replace" {return tt.getKeywordTokenType(294);}
"restrict_references" {return tt.getKeywordTokenType(295);}
"result" {return tt.getKeywordTokenType(296);}
"result_cache" {return tt.getKeywordTokenType(297);}
"return" {return tt.getKeywordTokenType(298);}
"returning" {return tt.getKeywordTokenType(299);}
"reverse" {return tt.getKeywordTokenType(300);}
"revoke" {return tt.getKeywordTokenType(301);}
"right" {return tt.getKeywordTokenType(302);}
"rnds" {return tt.getKeywordTokenType(303);}
"rnps" {return tt.getKeywordTokenType(304);}
"rollback" {return tt.getKeywordTokenType(305);}
"rollup" {return tt.getKeywordTokenType(306);}
"row" {return tt.getKeywordTokenType(307);}
"rowcount" {return tt.getKeywordTokenType(308);}
"rownum" {return tt.getKeywordTokenType(309);}
"rows" {return tt.getKeywordTokenType(310);}
"rowtype" {return tt.getKeywordTokenType(311);}
"rules" {return tt.getKeywordTokenType(312);}
"sample" {return tt.getKeywordTokenType(313);}
"save" {return tt.getKeywordTokenType(314);}
"savepoint" {return tt.getKeywordTokenType(315);}
"schema" {return tt.getKeywordTokenType(316);}
"schemacheck" {return tt.getKeywordTokenType(317);}
"scn" {return tt.getKeywordTokenType(318);}
"second" {return tt.getKeywordTokenType(319);}
"seed" {return tt.getKeywordTokenType(320);}
"segment" {return tt.getKeywordTokenType(321);}
"select" {return tt.getKeywordTokenType(322);}
"self" {return tt.getKeywordTokenType(323);}
"separate" {return tt.getKeywordTokenType(324);}
"sequential" {return tt.getKeywordTokenType(325);}
"serializable" {return tt.getKeywordTokenType(326);}
"serially_reusable" {return tt.getKeywordTokenType(327);}
"servererror" {return tt.getKeywordTokenType(328);}
"set" {return tt.getKeywordTokenType(329);}
"sets" {return tt.getKeywordTokenType(330);}
"share" {return tt.getKeywordTokenType(331);}
"show" {return tt.getKeywordTokenType(332);}
"shutdown" {return tt.getKeywordTokenType(333);}
"siblings" {return tt.getKeywordTokenType(334);}
"sig" {return tt.getKeywordTokenType(335);}
"single" {return tt.getKeywordTokenType(336);}
"size" {return tt.getKeywordTokenType(337);}
"some" {return tt.getKeywordTokenType(338);}
"space" {return tt.getKeywordTokenType(339);}
"sql" {return tt.getKeywordTokenType(340);}
"sqlcode" {return tt.getKeywordTokenType(341);}
"sqlerrm" {return tt.getKeywordTokenType(342);}
"standalone" {return tt.getKeywordTokenType(343);}
"start" {return tt.getKeywordTokenType(344);}
"startup" {return tt.getKeywordTokenType(345);}
"statement" {return tt.getKeywordTokenType(346);}
"static" {return tt.getKeywordTokenType(347);}
"statistic" {return tt.getKeywordTokenType(348);}
"statistics" {return tt.getKeywordTokenType(349);}
"struct" {return tt.getKeywordTokenType(350);}
"submultiset" {return tt.getKeywordTokenType(351);}
"subpartition" {return tt.getKeywordTokenType(352);}
"subtype" {return tt.getKeywordTokenType(353);}
"successful" {return tt.getKeywordTokenType(354);}
"sum_squares_between" {return tt.getKeywordTokenType(355);}
"sum_squares_within" {return tt.getKeywordTokenType(356);}
"suspend" {return tt.getKeywordTokenType(357);}
"synonym" {return tt.getKeywordTokenType(358);}
"table" {return tt.getKeywordTokenType(359);}
"tdo" {return tt.getKeywordTokenType(360);}
"then" {return tt.getKeywordTokenType(361);}
"ties" {return tt.getKeywordTokenType(362);}
"time" {return tt.getKeywordTokenType(363);}
"timezone_abbr" {return tt.getKeywordTokenType(364);}
"timezone_hour" {return tt.getKeywordTokenType(365);}
"timezone_minute" {return tt.getKeywordTokenType(366);}
"timezone_region" {return tt.getKeywordTokenType(367);}
"to" {return tt.getKeywordTokenType(368);}
"trailing" {return tt.getKeywordTokenType(369);}
"transaction" {return tt.getKeywordTokenType(370);}
"trigger" {return tt.getKeywordTokenType(371);}
"truncate" {return tt.getKeywordTokenType(372);}
"trust" {return tt.getKeywordTokenType(373);}
"two_sided_prob" {return tt.getKeywordTokenType(374);}
"two_sided_sig" {return tt.getKeywordTokenType(375);}
"type" {return tt.getKeywordTokenType(376);}
"u_statistic" {return tt.getKeywordTokenType(377);}
"unbounded" {return tt.getKeywordTokenType(378);}
"under" {return tt.getKeywordTokenType(379);}
"under_path" {return tt.getKeywordTokenType(380);}
"union" {return tt.getKeywordTokenType(381);}
"unique" {return tt.getKeywordTokenType(382);}
"unlimited" {return tt.getKeywordTokenType(383);}
"unpivot" {return tt.getKeywordTokenType(384);}
"until" {return tt.getKeywordTokenType(385);}
"update" {return tt.getKeywordTokenType(386);}
"updated" {return tt.getKeywordTokenType(387);}
"updating" {return tt.getKeywordTokenType(388);}
"upsert" {return tt.getKeywordTokenType(389);}
"use" {return tt.getKeywordTokenType(390);}
"user" {return tt.getKeywordTokenType(391);}
"using" {return tt.getKeywordTokenType(392);}
"validate" {return tt.getKeywordTokenType(393);}
"value" {return tt.getKeywordTokenType(394);}
"values" {return tt.getKeywordTokenType(395);}
"variable" {return tt.getKeywordTokenType(396);}
"varray" {return tt.getKeywordTokenType(397);}
"varying" {return tt.getKeywordTokenType(398);}
"version" {return tt.getKeywordTokenType(399);}
"versions" {return tt.getKeywordTokenType(400);}
"view" {return tt.getKeywordTokenType(401);}
"wait" {return tt.getKeywordTokenType(402);}
"wellformed" {return tt.getKeywordTokenType(403);}
"when" {return tt.getKeywordTokenType(404);}
"whenever" {return tt.getKeywordTokenType(405);}
"where" {return tt.getKeywordTokenType(406);}
"while" {return tt.getKeywordTokenType(407);}
"with" {return tt.getKeywordTokenType(408);}
"within" {return tt.getKeywordTokenType(409);}
"wnds" {return tt.getKeywordTokenType(410);}
"wnps" {return tt.getKeywordTokenType(411);}
"work" {return tt.getKeywordTokenType(412);}
"write" {return tt.getKeywordTokenType(413);}
"wrapped" {return tt.getKeywordTokenType(414);}
"xml" {return tt.getKeywordTokenType(415);}
"xmlnamespaces" {return tt.getKeywordTokenType(416);}
"year" {return tt.getKeywordTokenType(417);}
"yes" {return tt.getKeywordTokenType(418);}
"zone" {return tt.getKeywordTokenType(419);}
"false" {return tt.getKeywordTokenType(420);}
"true" {return tt.getKeywordTokenType(421);}







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
"compose" {return tt.getFunctionTokenType(18);}
"concat" {return tt.getFunctionTokenType(19);}
"convert" {return tt.getFunctionTokenType(20);}
"corr" {return tt.getFunctionTokenType(21);}
"corr_k" {return tt.getFunctionTokenType(22);}
"corr_s" {return tt.getFunctionTokenType(23);}
"cos" {return tt.getFunctionTokenType(24);}
"cosh" {return tt.getFunctionTokenType(25);}
"covar_pop" {return tt.getFunctionTokenType(26);}
"covar_samp" {return tt.getFunctionTokenType(27);}
"cume_dist" {return tt.getFunctionTokenType(28);}
"current_date" {return tt.getFunctionTokenType(29);}
"current_timestamp" {return tt.getFunctionTokenType(30);}
"cv" {return tt.getFunctionTokenType(31);}
"dbtimezone" {return tt.getFunctionTokenType(32);}
"dbtmezone" {return tt.getFunctionTokenType(33);}
"decode" {return tt.getFunctionTokenType(34);}
"decompose" {return tt.getFunctionTokenType(35);}
"deletexml" {return tt.getFunctionTokenType(36);}
"depth" {return tt.getFunctionTokenType(37);}
"deref" {return tt.getFunctionTokenType(38);}
"empty_blob" {return tt.getFunctionTokenType(39);}
"empty_clob" {return tt.getFunctionTokenType(40);}
"existsnode" {return tt.getFunctionTokenType(41);}
"exp" {return tt.getFunctionTokenType(42);}
"extract" {return tt.getFunctionTokenType(43);}
"extractvalue" {return tt.getFunctionTokenType(44);}
"first_value" {return tt.getFunctionTokenType(45);}
"floor" {return tt.getFunctionTokenType(46);}
"from_tz" {return tt.getFunctionTokenType(47);}
"greatest" {return tt.getFunctionTokenType(48);}
"group_id" {return tt.getFunctionTokenType(49);}
"grouping" {return tt.getFunctionTokenType(50);}
"grouping_id" {return tt.getFunctionTokenType(51);}
"hextoraw" {return tt.getFunctionTokenType(52);}
"initcap" {return tt.getFunctionTokenType(53);}
"insertchildxml" {return tt.getFunctionTokenType(54);}
"insertchildxmlafter" {return tt.getFunctionTokenType(55);}
"insertchildxmlbefore" {return tt.getFunctionTokenType(56);}
"insertxmlafter" {return tt.getFunctionTokenType(57);}
"insertxmlbefore" {return tt.getFunctionTokenType(58);}
"instr" {return tt.getFunctionTokenType(59);}
"instr2" {return tt.getFunctionTokenType(60);}
"instr4" {return tt.getFunctionTokenType(61);}
"instrb" {return tt.getFunctionTokenType(62);}
"instrc" {return tt.getFunctionTokenType(63);}
"iteration_number" {return tt.getFunctionTokenType(64);}
"json_object" {return tt.getFunctionTokenType(65);}
"lag" {return tt.getFunctionTokenType(66);}
"last_day" {return tt.getFunctionTokenType(67);}
"last_value" {return tt.getFunctionTokenType(68);}
"lead" {return tt.getFunctionTokenType(69);}
"least" {return tt.getFunctionTokenType(70);}
"length" {return tt.getFunctionTokenType(71);}
"length2" {return tt.getFunctionTokenType(72);}
"length4" {return tt.getFunctionTokenType(73);}
"lengthb" {return tt.getFunctionTokenType(74);}
"lengthc" {return tt.getFunctionTokenType(75);}
"listagg" {return tt.getFunctionTokenType(76);}
"ln" {return tt.getFunctionTokenType(77);}
"lnnvl" {return tt.getFunctionTokenType(78);}
"localtimestamp" {return tt.getFunctionTokenType(79);}
"lower" {return tt.getFunctionTokenType(80);}
"lpad" {return tt.getFunctionTokenType(81);}
"ltrim" {return tt.getFunctionTokenType(82);}
"make_ref" {return tt.getFunctionTokenType(83);}
"max" {return tt.getFunctionTokenType(84);}
"median" {return tt.getFunctionTokenType(85);}
"min" {return tt.getFunctionTokenType(86);}
"mod" {return tt.getFunctionTokenType(87);}
"months_between" {return tt.getFunctionTokenType(88);}
"nanvl" {return tt.getFunctionTokenType(89);}
"nchr" {return tt.getFunctionTokenType(90);}
"new_time" {return tt.getFunctionTokenType(91);}
"next_day" {return tt.getFunctionTokenType(92);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(93);}
"nls_charset_id" {return tt.getFunctionTokenType(94);}
"nls_charset_name" {return tt.getFunctionTokenType(95);}
"nls_initcap" {return tt.getFunctionTokenType(96);}
"nls_lower" {return tt.getFunctionTokenType(97);}
"nls_upper" {return tt.getFunctionTokenType(98);}
"nlssort" {return tt.getFunctionTokenType(99);}
"ntile" {return tt.getFunctionTokenType(100);}
"nullif" {return tt.getFunctionTokenType(101);}
"numtodsinterval" {return tt.getFunctionTokenType(102);}
"numtoyminterval" {return tt.getFunctionTokenType(103);}
"nvl" {return tt.getFunctionTokenType(104);}
"nvl2" {return tt.getFunctionTokenType(105);}
"ora_hash" {return tt.getFunctionTokenType(106);}
"percent_rank" {return tt.getFunctionTokenType(107);}
"percentile_cont" {return tt.getFunctionTokenType(108);}
"percentile_disc" {return tt.getFunctionTokenType(109);}
"powermultiset" {return tt.getFunctionTokenType(110);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(111);}
"presentnnv" {return tt.getFunctionTokenType(112);}
"presentv" {return tt.getFunctionTokenType(113);}
"previous" {return tt.getFunctionTokenType(114);}
"rank" {return tt.getFunctionTokenType(115);}
"ratio_to_report" {return tt.getFunctionTokenType(116);}
"rawtohex" {return tt.getFunctionTokenType(117);}
"rawtonhex" {return tt.getFunctionTokenType(118);}
"reftohex" {return tt.getFunctionTokenType(119);}
"regexp_instr" {return tt.getFunctionTokenType(120);}
"regexp_replace" {return tt.getFunctionTokenType(121);}
"regexp_substr" {return tt.getFunctionTokenType(122);}
"regr_avgx" {return tt.getFunctionTokenType(123);}
"regr_avgy" {return tt.getFunctionTokenType(124);}
"regr_count" {return tt.getFunctionTokenType(125);}
"regr_intercept" {return tt.getFunctionTokenType(126);}
"regr_r2" {return tt.getFunctionTokenType(127);}
"regr_slope" {return tt.getFunctionTokenType(128);}
"regr_sxx" {return tt.getFunctionTokenType(129);}
"regr_sxy" {return tt.getFunctionTokenType(130);}
"regr_syy" {return tt.getFunctionTokenType(131);}
"round" {return tt.getFunctionTokenType(132);}
"row_number" {return tt.getFunctionTokenType(133);}
"rowidtochar" {return tt.getFunctionTokenType(134);}
"rowidtonchar" {return tt.getFunctionTokenType(135);}
"rpad" {return tt.getFunctionTokenType(136);}
"rtrim" {return tt.getFunctionTokenType(137);}
"scn_to_timestamp" {return tt.getFunctionTokenType(138);}
"sessiontimezone" {return tt.getFunctionTokenType(139);}
"sign" {return tt.getFunctionTokenType(140);}
"sin" {return tt.getFunctionTokenType(141);}
"sinh" {return tt.getFunctionTokenType(142);}
"soundex" {return tt.getFunctionTokenType(143);}
"sqrt" {return tt.getFunctionTokenType(144);}
"stats_binomial_test" {return tt.getFunctionTokenType(145);}
"stats_crosstab" {return tt.getFunctionTokenType(146);}
"stats_f_test" {return tt.getFunctionTokenType(147);}
"stats_ks_test" {return tt.getFunctionTokenType(148);}
"stats_mode" {return tt.getFunctionTokenType(149);}
"stats_mw_test" {return tt.getFunctionTokenType(150);}
"stats_one_way_anova" {return tt.getFunctionTokenType(151);}
"stats_t_test_indep" {return tt.getFunctionTokenType(152);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(153);}
"stats_t_test_one" {return tt.getFunctionTokenType(154);}
"stats_t_test_paired" {return tt.getFunctionTokenType(155);}
"stats_wsr_test" {return tt.getFunctionTokenType(156);}
"stddev" {return tt.getFunctionTokenType(157);}
"stddev_pop" {return tt.getFunctionTokenType(158);}
"stddev_samp" {return tt.getFunctionTokenType(159);}
"substr" {return tt.getFunctionTokenType(160);}
"substr2" {return tt.getFunctionTokenType(161);}
"substr4" {return tt.getFunctionTokenType(162);}
"substrb" {return tt.getFunctionTokenType(163);}
"substrc" {return tt.getFunctionTokenType(164);}
"sum" {return tt.getFunctionTokenType(165);}
"sys_connect_by_path" {return tt.getFunctionTokenType(166);}
"sys_context" {return tt.getFunctionTokenType(167);}
"sys_dburigen" {return tt.getFunctionTokenType(168);}
"sys_extract_utc" {return tt.getFunctionTokenType(169);}
"sys_guid" {return tt.getFunctionTokenType(170);}
"sys_typeid" {return tt.getFunctionTokenType(171);}
"sys_xmlagg" {return tt.getFunctionTokenType(172);}
"sys_xmlgen" {return tt.getFunctionTokenType(173);}
"sysdate" {return tt.getFunctionTokenType(174);}
"systimestamp" {return tt.getFunctionTokenType(175);}
"tan" {return tt.getFunctionTokenType(176);}
"tanh" {return tt.getFunctionTokenType(177);}
"timestamp_to_scn" {return tt.getFunctionTokenType(178);}
"to_binary_double" {return tt.getFunctionTokenType(179);}
"to_binary_float" {return tt.getFunctionTokenType(180);}
"to_char" {return tt.getFunctionTokenType(181);}
"to_clob" {return tt.getFunctionTokenType(182);}
"to_date" {return tt.getFunctionTokenType(183);}
"to_dsinterval" {return tt.getFunctionTokenType(184);}
"to_lob" {return tt.getFunctionTokenType(185);}
"to_multi_byte" {return tt.getFunctionTokenType(186);}
"to_nchar" {return tt.getFunctionTokenType(187);}
"to_nclob" {return tt.getFunctionTokenType(188);}
"to_number" {return tt.getFunctionTokenType(189);}
"to_single_byte" {return tt.getFunctionTokenType(190);}
"to_timestamp" {return tt.getFunctionTokenType(191);}
"to_timestamp_tz" {return tt.getFunctionTokenType(192);}
"to_yminterval" {return tt.getFunctionTokenType(193);}
"translate" {return tt.getFunctionTokenType(194);}
"treat" {return tt.getFunctionTokenType(195);}
"trim" {return tt.getFunctionTokenType(196);}
"trunc" {return tt.getFunctionTokenType(197);}
"tz_offset" {return tt.getFunctionTokenType(198);}
"uid" {return tt.getFunctionTokenType(199);}
"unistr" {return tt.getFunctionTokenType(200);}
"updatexml" {return tt.getFunctionTokenType(201);}
"upper" {return tt.getFunctionTokenType(202);}
"userenv" {return tt.getFunctionTokenType(203);}
"var_pop" {return tt.getFunctionTokenType(204);}
"var_samp" {return tt.getFunctionTokenType(205);}
"variance" {return tt.getFunctionTokenType(206);}
"vsize" {return tt.getFunctionTokenType(207);}
"width_bucket" {return tt.getFunctionTokenType(208);}
"xmlagg" {return tt.getFunctionTokenType(209);}
"xmlattributes" {return tt.getFunctionTokenType(210);}
"xmlcast" {return tt.getFunctionTokenType(211);}
"xmlcdata" {return tt.getFunctionTokenType(212);}
"xmlcolattval" {return tt.getFunctionTokenType(213);}
"xmlcomment" {return tt.getFunctionTokenType(214);}
"xmlconcat" {return tt.getFunctionTokenType(215);}
"xmldiff" {return tt.getFunctionTokenType(216);}
"xmlelement" {return tt.getFunctionTokenType(217);}
"xmlforest" {return tt.getFunctionTokenType(218);}
"xmlisvalid" {return tt.getFunctionTokenType(219);}
"xmlparse" {return tt.getFunctionTokenType(220);}
"xmlpatch" {return tt.getFunctionTokenType(221);}
"xmlpi" {return tt.getFunctionTokenType(222);}
"xmlquery" {return tt.getFunctionTokenType(223);}
"xmlroot" {return tt.getFunctionTokenType(224);}
"xmlsequence" {return tt.getFunctionTokenType(225);}
"xmlserialize" {return tt.getFunctionTokenType(226);}
"xmltable" {return tt.getFunctionTokenType(227);}
"xmltransform" {return tt.getFunctionTokenType(228);}






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



{IDENTIFIER}           { return tt.getSharedTokenTypes().getIdentifier(); }
{QUOTED_IDENTIFIER}    { return tt.getSharedTokenTypes().getQuotedIdentifier(); }
.                      { return tt.getSharedTokenTypes().getIdentifier(); }


