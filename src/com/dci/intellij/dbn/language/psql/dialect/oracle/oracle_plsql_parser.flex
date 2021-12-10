package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%class OraclePLSQLParserFlexLexer
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
"accessible" {return tt.getKeywordTokenType(2);}
"after" {return tt.getKeywordTokenType(3);}
"agent" {return tt.getKeywordTokenType(4);}
"aggregate" {return tt.getKeywordTokenType(5);}
"all" {return tt.getKeywordTokenType(6);}
"alter" {return tt.getKeywordTokenType(7);}
"analyze" {return tt.getKeywordTokenType(8);}
"and" {return tt.getKeywordTokenType(9);}
"any" {return tt.getKeywordTokenType(10);}
"apply" {return tt.getKeywordTokenType(11);}
"array" {return tt.getKeywordTokenType(12);}
"as" {return tt.getKeywordTokenType(13);}
"asc" {return tt.getKeywordTokenType(14);}
"associate" {return tt.getKeywordTokenType(15);}
"at" {return tt.getKeywordTokenType(16);}
"audit" {return tt.getKeywordTokenType(17);}
"authid" {return tt.getKeywordTokenType(18);}
"automatic" {return tt.getKeywordTokenType(19);}
"autonomous_transaction" {return tt.getKeywordTokenType(20);}
"before" {return tt.getKeywordTokenType(21);}
"begin" {return tt.getKeywordTokenType(22);}
"between" {return tt.getKeywordTokenType(23);}
"block" {return tt.getKeywordTokenType(24);}
"body" {return tt.getKeywordTokenType(25);}
"both" {return tt.getKeywordTokenType(26);}
"bulk" {return tt.getKeywordTokenType(27);}
"bulk_exceptions" {return tt.getKeywordTokenType(28);}
"bulk_rowcount" {return tt.getKeywordTokenType(29);}
"by" {return tt.getKeywordTokenType(30);}
"c" {return tt.getKeywordTokenType(31);}
"call" {return tt.getKeywordTokenType(32);}
"canonical" {return tt.getKeywordTokenType(33);}
"case" {return tt.getKeywordTokenType(34);}
"char_base" {return tt.getKeywordTokenType(35);}
"char_cs" {return tt.getKeywordTokenType(36);}
"charsetform" {return tt.getKeywordTokenType(37);}
"charsetid" {return tt.getKeywordTokenType(38);}
"check" {return tt.getKeywordTokenType(39);}
"chisq_df" {return tt.getKeywordTokenType(40);}
"chisq_obs" {return tt.getKeywordTokenType(41);}
"chisq_sig" {return tt.getKeywordTokenType(42);}
"close" {return tt.getKeywordTokenType(43);}
"cluster" {return tt.getKeywordTokenType(44);}
"coalesce" {return tt.getKeywordTokenType(45);}
"coefficient" {return tt.getKeywordTokenType(46);}
"cohens_k" {return tt.getKeywordTokenType(47);}
"collation" {return tt.getKeywordTokenType(48);}
"collect" {return tt.getKeywordTokenType(49);}
"columns" {return tt.getKeywordTokenType(50);}
"comment" {return tt.getKeywordTokenType(51);}
"commit" {return tt.getKeywordTokenType(52);}
"committed" {return tt.getKeywordTokenType(53);}
"compatibility" {return tt.getKeywordTokenType(54);}
"compound" {return tt.getKeywordTokenType(55);}
"compress" {return tt.getKeywordTokenType(56);}
"conditional" {return tt.getKeywordTokenType(57);}
"connect" {return tt.getKeywordTokenType(58);}
"constant" {return tt.getKeywordTokenType(59);}
"constraint" {return tt.getKeywordTokenType(60);}
"constructor" {return tt.getKeywordTokenType(61);}
"cont_coefficient" {return tt.getKeywordTokenType(62);}
"content" {return tt.getKeywordTokenType(63);}
"context" {return tt.getKeywordTokenType(64);}
"conversion" {return tt.getKeywordTokenType(65);}
"count" {return tt.getKeywordTokenType(66);}
"cramers_v" {return tt.getKeywordTokenType(67);}
"create" {return tt.getKeywordTokenType(68);}
"cross" {return tt.getKeywordTokenType(69);}
"crossedition" {return tt.getKeywordTokenType(70);}
"cube" {return tt.getKeywordTokenType(71);}
"current" {return tt.getKeywordTokenType(72);}
"current_user" {return tt.getKeywordTokenType(73);}
"currval" {return tt.getKeywordTokenType(74);}
"cursor" {return tt.getKeywordTokenType(75);}
"database" {return tt.getKeywordTokenType(76);}
"day" {return tt.getKeywordTokenType(77);}
"db_role_change" {return tt.getKeywordTokenType(78);}
"ddl" {return tt.getKeywordTokenType(79);}
"declare" {return tt.getKeywordTokenType(80);}
"decrement" {return tt.getKeywordTokenType(81);}
"default" {return tt.getKeywordTokenType(82);}
"defaults" {return tt.getKeywordTokenType(83);}
"definer" {return tt.getKeywordTokenType(84);}
"delete" {return tt.getKeywordTokenType(85);}
"deleting" {return tt.getKeywordTokenType(86);}
"dense_rank" {return tt.getKeywordTokenType(87);}
"deprecate" {return tt.getKeywordTokenType(88);}
"desc" {return tt.getKeywordTokenType(89);}
"deterministic" {return tt.getKeywordTokenType(90);}
"df" {return tt.getKeywordTokenType(91);}
"df_between" {return tt.getKeywordTokenType(92);}
"df_den" {return tt.getKeywordTokenType(93);}
"df_num" {return tt.getKeywordTokenType(94);}
"df_within" {return tt.getKeywordTokenType(95);}
"dimension" {return tt.getKeywordTokenType(96);}
"disable" {return tt.getKeywordTokenType(97);}
"disassociate" {return tt.getKeywordTokenType(98);}
"distinct" {return tt.getKeywordTokenType(99);}
"do" {return tt.getKeywordTokenType(100);}
"document" {return tt.getKeywordTokenType(101);}
"drop" {return tt.getKeywordTokenType(102);}
"dump" {return tt.getKeywordTokenType(103);}
"duration" {return tt.getKeywordTokenType(104);}
"each" {return tt.getKeywordTokenType(105);}
"editionable" {return tt.getKeywordTokenType(106);}
"else" {return tt.getKeywordTokenType(107);}
"elsif" {return tt.getKeywordTokenType(108);}
"empty" {return tt.getKeywordTokenType(109);}
"enable" {return tt.getKeywordTokenType(110);}
"encoding" {return tt.getKeywordTokenType(111);}
"end" {return tt.getKeywordTokenType(112);}
"entityescaping" {return tt.getKeywordTokenType(113);}
"equals_path" {return tt.getKeywordTokenType(114);}
"error" {return tt.getKeywordTokenType(115);}
"error_code" {return tt.getKeywordTokenType(116);}
"error_index" {return tt.getKeywordTokenType(117);}
"errors" {return tt.getKeywordTokenType(118);}
"escape" {return tt.getKeywordTokenType(119);}
"evalname" {return tt.getKeywordTokenType(120);}
"exact_prob" {return tt.getKeywordTokenType(121);}
"except" {return tt.getKeywordTokenType(122);}
"exception" {return tt.getKeywordTokenType(123);}
"exception_init" {return tt.getKeywordTokenType(124);}
"exceptions" {return tt.getKeywordTokenType(125);}
"exclude" {return tt.getKeywordTokenType(126);}
"exclusive" {return tt.getKeywordTokenType(127);}
"execute" {return tt.getKeywordTokenType(128);}
"exists" {return tt.getKeywordTokenType(129);}
"exit" {return tt.getKeywordTokenType(130);}
"extend" {return tt.getKeywordTokenType(131);}
"extends" {return tt.getKeywordTokenType(132);}
"external" {return tt.getKeywordTokenType(133);}
"f_ratio" {return tt.getKeywordTokenType(134);}
"fetch" {return tt.getKeywordTokenType(135);}
"final" {return tt.getKeywordTokenType(136);}
"first" {return tt.getKeywordTokenType(137);}
"following" {return tt.getKeywordTokenType(138);}
"follows" {return tt.getKeywordTokenType(139);}
"for" {return tt.getKeywordTokenType(140);}
"forall" {return tt.getKeywordTokenType(141);}
"force" {return tt.getKeywordTokenType(142);}
"forward" {return tt.getKeywordTokenType(143);}
"found" {return tt.getKeywordTokenType(144);}
"from" {return tt.getKeywordTokenType(145);}
"format" {return tt.getKeywordTokenType(146);}
"full" {return tt.getKeywordTokenType(147);}
"function" {return tt.getKeywordTokenType(148);}
"goto" {return tt.getKeywordTokenType(149);}
"grant" {return tt.getKeywordTokenType(150);}
"group" {return tt.getKeywordTokenType(151);}
"hash" {return tt.getKeywordTokenType(152);}
"having" {return tt.getKeywordTokenType(153);}
"heap" {return tt.getKeywordTokenType(154);}
"hide" {return tt.getKeywordTokenType(155);}
"hour" {return tt.getKeywordTokenType(156);}
"if" {return tt.getKeywordTokenType(157);}
"ignore" {return tt.getKeywordTokenType(158);}
"immediate" {return tt.getKeywordTokenType(159);}
"in" {return tt.getKeywordTokenType(160);}
"in"{ws}"out" {return tt.getKeywordTokenType(161);}
"include" {return tt.getKeywordTokenType(162);}
"increment" {return tt.getKeywordTokenType(163);}
"indent" {return tt.getKeywordTokenType(164);}
"index" {return tt.getKeywordTokenType(165);}
"indicator" {return tt.getKeywordTokenType(166);}
"indices" {return tt.getKeywordTokenType(167);}
"infinite" {return tt.getKeywordTokenType(168);}
"inline" {return tt.getKeywordTokenType(169);}
"inner" {return tt.getKeywordTokenType(170);}
"insert" {return tt.getKeywordTokenType(171);}
"inserting" {return tt.getKeywordTokenType(172);}
"instantiable" {return tt.getKeywordTokenType(173);}
"instead" {return tt.getKeywordTokenType(174);}
"interface" {return tt.getKeywordTokenType(175);}
"intersect" {return tt.getKeywordTokenType(176);}
"interval" {return tt.getKeywordTokenType(177);}
"into" {return tt.getKeywordTokenType(178);}
"is" {return tt.getKeywordTokenType(179);}
"isolation" {return tt.getKeywordTokenType(180);}
"isopen" {return tt.getKeywordTokenType(181);}
"iterate" {return tt.getKeywordTokenType(182);}
"java" {return tt.getKeywordTokenType(183);}
"join" {return tt.getKeywordTokenType(184);}
"json" {return tt.getKeywordTokenType(185);}
"keep" {return tt.getKeywordTokenType(186);}
"key" {return tt.getKeywordTokenType(187);}
"keys" {return tt.getKeywordTokenType(188);}
"language" {return tt.getKeywordTokenType(189);}
"last" {return tt.getKeywordTokenType(190);}
"leading" {return tt.getKeywordTokenType(191);}
"left" {return tt.getKeywordTokenType(192);}
"level" {return tt.getKeywordTokenType(193);}
"library" {return tt.getKeywordTokenType(194);}
"like" {return tt.getKeywordTokenType(195);}
"like2" {return tt.getKeywordTokenType(196);}
"like4" {return tt.getKeywordTokenType(197);}
"likec" {return tt.getKeywordTokenType(198);}
"limit" {return tt.getKeywordTokenType(199);}
"limited" {return tt.getKeywordTokenType(200);}
"local" {return tt.getKeywordTokenType(201);}
"lock" {return tt.getKeywordTokenType(202);}
"locked" {return tt.getKeywordTokenType(203);}
"log" {return tt.getKeywordTokenType(204);}
"logoff" {return tt.getKeywordTokenType(205);}
"logon" {return tt.getKeywordTokenType(206);}
"loop" {return tt.getKeywordTokenType(207);}
"main" {return tt.getKeywordTokenType(208);}
"map" {return tt.getKeywordTokenType(209);}
"matched" {return tt.getKeywordTokenType(210);}
"maxlen" {return tt.getKeywordTokenType(211);}
"maxvalue" {return tt.getKeywordTokenType(212);}
"mean_squares_between" {return tt.getKeywordTokenType(213);}
"mean_squares_within" {return tt.getKeywordTokenType(214);}
"measures" {return tt.getKeywordTokenType(215);}
"member" {return tt.getKeywordTokenType(216);}
"merge" {return tt.getKeywordTokenType(217);}
"metadata" {return tt.getKeywordTokenType(218);}
"minus" {return tt.getKeywordTokenType(219);}
"minute" {return tt.getKeywordTokenType(220);}
"minvalue" {return tt.getKeywordTokenType(221);}
"mismatch" {return tt.getKeywordTokenType(222);}
"mlslabel" {return tt.getKeywordTokenType(223);}
"mode" {return tt.getKeywordTokenType(224);}
"model" {return tt.getKeywordTokenType(225);}
"month" {return tt.getKeywordTokenType(226);}
"multiset" {return tt.getKeywordTokenType(227);}
"name" {return tt.getKeywordTokenType(228);}
"nan" {return tt.getKeywordTokenType(229);}
"natural" {return tt.getKeywordTokenType(230);}
"naturaln" {return tt.getKeywordTokenType(231);}
"nav" {return tt.getKeywordTokenType(232);}
"nchar_cs" {return tt.getKeywordTokenType(233);}
"nested" {return tt.getKeywordTokenType(234);}
"new" {return tt.getKeywordTokenType(235);}
"next" {return tt.getKeywordTokenType(236);}
"nextval" {return tt.getKeywordTokenType(237);}
"no" {return tt.getKeywordTokenType(238);}
"noaudit" {return tt.getKeywordTokenType(239);}
"nocopy" {return tt.getKeywordTokenType(240);}
"nocycle" {return tt.getKeywordTokenType(241);}
"none" {return tt.getKeywordTokenType(242);}
"noentityescaping" {return tt.getKeywordTokenType(243);}
"noneditionable" {return tt.getKeywordTokenType(244);}
"noschemacheck" {return tt.getKeywordTokenType(245);}
"not" {return tt.getKeywordTokenType(246);}
"notfound" {return tt.getKeywordTokenType(247);}
"nowait" {return tt.getKeywordTokenType(248);}
"null" {return tt.getKeywordTokenType(249);}
"nulls" {return tt.getKeywordTokenType(250);}
"number_base" {return tt.getKeywordTokenType(251);}
"object" {return tt.getKeywordTokenType(252);}
"ocirowid" {return tt.getKeywordTokenType(253);}
"of" {return tt.getKeywordTokenType(254);}
"offset" {return tt.getKeywordTokenType(255);}
"oid" {return tt.getKeywordTokenType(256);}
"old" {return tt.getKeywordTokenType(257);}
"on" {return tt.getKeywordTokenType(258);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(259);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(260);}
"one_sided_sig" {return tt.getKeywordTokenType(261);}
"only" {return tt.getKeywordTokenType(262);}
"opaque" {return tt.getKeywordTokenType(263);}
"open" {return tt.getKeywordTokenType(264);}
"operator" {return tt.getKeywordTokenType(265);}
"option" {return tt.getKeywordTokenType(266);}
"or" {return tt.getKeywordTokenType(267);}
"order" {return tt.getKeywordTokenType(268);}
"ordinality" {return tt.getKeywordTokenType(269);}
"organization" {return tt.getKeywordTokenType(270);}
"others" {return tt.getKeywordTokenType(271);}
"out" {return tt.getKeywordTokenType(272);}
"outer" {return tt.getKeywordTokenType(273);}
"over" {return tt.getKeywordTokenType(274);}
"overflow" {return tt.getKeywordTokenType(275);}
"overriding" {return tt.getKeywordTokenType(276);}
"package" {return tt.getKeywordTokenType(277);}
"parallel_enable" {return tt.getKeywordTokenType(278);}
"parameters" {return tt.getKeywordTokenType(279);}
"parent" {return tt.getKeywordTokenType(280);}
"partition" {return tt.getKeywordTokenType(281);}
"passing" {return tt.getKeywordTokenType(282);}
"path" {return tt.getKeywordTokenType(283);}
"pctfree" {return tt.getKeywordTokenType(284);}
"percent" {return tt.getKeywordTokenType(285);}
"phi_coefficient" {return tt.getKeywordTokenType(286);}
"pipe" {return tt.getKeywordTokenType(287);}
"pipelined" {return tt.getKeywordTokenType(288);}
"pivot" {return tt.getKeywordTokenType(289);}
"pluggable" {return tt.getKeywordTokenType(290);}
"positive" {return tt.getKeywordTokenType(291);}
"positiven" {return tt.getKeywordTokenType(292);}
"power" {return tt.getKeywordTokenType(293);}
"pragma" {return tt.getKeywordTokenType(294);}
"preceding" {return tt.getKeywordTokenType(295);}
"precedes" {return tt.getKeywordTokenType(296);}
"present" {return tt.getKeywordTokenType(297);}
"pretty" {return tt.getKeywordTokenType(298);}
"prior" {return tt.getKeywordTokenType(299);}
"private" {return tt.getKeywordTokenType(300);}
"procedure" {return tt.getKeywordTokenType(301);}
"public" {return tt.getKeywordTokenType(302);}
"raise" {return tt.getKeywordTokenType(303);}
"range" {return tt.getKeywordTokenType(304);}
"read" {return tt.getKeywordTokenType(305);}
"record" {return tt.getKeywordTokenType(306);}
"ref" {return tt.getKeywordTokenType(307);}
"reference" {return tt.getKeywordTokenType(308);}
"referencing" {return tt.getKeywordTokenType(309);}
"regexp_like" {return tt.getKeywordTokenType(310);}
"reject" {return tt.getKeywordTokenType(311);}
"release" {return tt.getKeywordTokenType(312);}
"relies_on" {return tt.getKeywordTokenType(313);}
"remainder" {return tt.getKeywordTokenType(314);}
"rename" {return tt.getKeywordTokenType(315);}
"replace" {return tt.getKeywordTokenType(316);}
"restrict_references" {return tt.getKeywordTokenType(317);}
"result" {return tt.getKeywordTokenType(318);}
"result_cache" {return tt.getKeywordTokenType(319);}
"return" {return tt.getKeywordTokenType(320);}
"returning" {return tt.getKeywordTokenType(321);}
"reverse" {return tt.getKeywordTokenType(322);}
"revoke" {return tt.getKeywordTokenType(323);}
"right" {return tt.getKeywordTokenType(324);}
"rnds" {return tt.getKeywordTokenType(325);}
"rnps" {return tt.getKeywordTokenType(326);}
"rollback" {return tt.getKeywordTokenType(327);}
"rollup" {return tt.getKeywordTokenType(328);}
"row" {return tt.getKeywordTokenType(329);}
"rowcount" {return tt.getKeywordTokenType(330);}
"rownum" {return tt.getKeywordTokenType(331);}
"rows" {return tt.getKeywordTokenType(332);}
"rowtype" {return tt.getKeywordTokenType(333);}
"rules" {return tt.getKeywordTokenType(334);}
"sample" {return tt.getKeywordTokenType(335);}
"save" {return tt.getKeywordTokenType(336);}
"savepoint" {return tt.getKeywordTokenType(337);}
"schema" {return tt.getKeywordTokenType(338);}
"schemacheck" {return tt.getKeywordTokenType(339);}
"scn" {return tt.getKeywordTokenType(340);}
"second" {return tt.getKeywordTokenType(341);}
"seed" {return tt.getKeywordTokenType(342);}
"segment" {return tt.getKeywordTokenType(343);}
"select" {return tt.getKeywordTokenType(344);}
"self" {return tt.getKeywordTokenType(345);}
"separate" {return tt.getKeywordTokenType(346);}
"sequential" {return tt.getKeywordTokenType(347);}
"serializable" {return tt.getKeywordTokenType(348);}
"serially_reusable" {return tt.getKeywordTokenType(349);}
"servererror" {return tt.getKeywordTokenType(350);}
"set" {return tt.getKeywordTokenType(351);}
"sets" {return tt.getKeywordTokenType(352);}
"share" {return tt.getKeywordTokenType(353);}
"sharing" {return tt.getKeywordTokenType(354);}
"show" {return tt.getKeywordTokenType(355);}
"shutdown" {return tt.getKeywordTokenType(356);}
"siblings" {return tt.getKeywordTokenType(357);}
"sig" {return tt.getKeywordTokenType(358);}
"single" {return tt.getKeywordTokenType(359);}
"size" {return tt.getKeywordTokenType(360);}
"skip" {return tt.getKeywordTokenType(361);}
"some" {return tt.getKeywordTokenType(362);}
"space" {return tt.getKeywordTokenType(363);}
"sql" {return tt.getKeywordTokenType(364);}
"sqlcode" {return tt.getKeywordTokenType(365);}
"sqlerrm" {return tt.getKeywordTokenType(366);}
"standalone" {return tt.getKeywordTokenType(367);}
"start" {return tt.getKeywordTokenType(368);}
"startup" {return tt.getKeywordTokenType(369);}
"statement" {return tt.getKeywordTokenType(370);}
"static" {return tt.getKeywordTokenType(371);}
"statistic" {return tt.getKeywordTokenType(372);}
"statistics" {return tt.getKeywordTokenType(373);}
"strict" {return tt.getKeywordTokenType(374);}
"struct" {return tt.getKeywordTokenType(375);}
"submultiset" {return tt.getKeywordTokenType(376);}
"subpartition" {return tt.getKeywordTokenType(377);}
"subtype" {return tt.getKeywordTokenType(378);}
"successful" {return tt.getKeywordTokenType(379);}
"sum_squares_between" {return tt.getKeywordTokenType(380);}
"sum_squares_within" {return tt.getKeywordTokenType(381);}
"suspend" {return tt.getKeywordTokenType(382);}
"synonym" {return tt.getKeywordTokenType(383);}
"table" {return tt.getKeywordTokenType(384);}
"tdo" {return tt.getKeywordTokenType(385);}
"then" {return tt.getKeywordTokenType(386);}
"ties" {return tt.getKeywordTokenType(387);}
"time" {return tt.getKeywordTokenType(388);}
"timezone_abbr" {return tt.getKeywordTokenType(389);}
"timezone_hour" {return tt.getKeywordTokenType(390);}
"timezone_minute" {return tt.getKeywordTokenType(391);}
"timezone_region" {return tt.getKeywordTokenType(392);}
"to" {return tt.getKeywordTokenType(393);}
"trailing" {return tt.getKeywordTokenType(394);}
"transaction" {return tt.getKeywordTokenType(395);}
"trigger" {return tt.getKeywordTokenType(396);}
"truncate" {return tt.getKeywordTokenType(397);}
"trust" {return tt.getKeywordTokenType(398);}
"two_sided_prob" {return tt.getKeywordTokenType(399);}
"two_sided_sig" {return tt.getKeywordTokenType(400);}
"type" {return tt.getKeywordTokenType(401);}
"u_statistic" {return tt.getKeywordTokenType(402);}
"unbounded" {return tt.getKeywordTokenType(403);}
"unconditional" {return tt.getKeywordTokenType(404);}
"under" {return tt.getKeywordTokenType(405);}
"under_path" {return tt.getKeywordTokenType(406);}
"union" {return tt.getKeywordTokenType(407);}
"unique" {return tt.getKeywordTokenType(408);}
"unlimited" {return tt.getKeywordTokenType(409);}
"unpivot" {return tt.getKeywordTokenType(410);}
"until" {return tt.getKeywordTokenType(411);}
"update" {return tt.getKeywordTokenType(412);}
"updated" {return tt.getKeywordTokenType(413);}
"updating" {return tt.getKeywordTokenType(414);}
"upsert" {return tt.getKeywordTokenType(415);}
"use" {return tt.getKeywordTokenType(416);}
"user" {return tt.getKeywordTokenType(417);}
"using" {return tt.getKeywordTokenType(418);}
"validate" {return tt.getKeywordTokenType(419);}
"value" {return tt.getKeywordTokenType(420);}
"values" {return tt.getKeywordTokenType(421);}
"variable" {return tt.getKeywordTokenType(422);}
"varray" {return tt.getKeywordTokenType(423);}
"varying" {return tt.getKeywordTokenType(424);}
"version" {return tt.getKeywordTokenType(425);}
"versions" {return tt.getKeywordTokenType(426);}
"view" {return tt.getKeywordTokenType(427);}
"wait" {return tt.getKeywordTokenType(428);}
"wellformed" {return tt.getKeywordTokenType(429);}
"when" {return tt.getKeywordTokenType(430);}
"whenever" {return tt.getKeywordTokenType(431);}
"where" {return tt.getKeywordTokenType(432);}
"while" {return tt.getKeywordTokenType(433);}
"with" {return tt.getKeywordTokenType(434);}
"within" {return tt.getKeywordTokenType(435);}
"without" {return tt.getKeywordTokenType(436);}
"wnds" {return tt.getKeywordTokenType(437);}
"wnps" {return tt.getKeywordTokenType(438);}
"work" {return tt.getKeywordTokenType(439);}
"write" {return tt.getKeywordTokenType(440);}
"wrapped" {return tt.getKeywordTokenType(441);}
"wrapper" {return tt.getKeywordTokenType(442);}
"xml" {return tt.getKeywordTokenType(443);}
"xmlnamespaces" {return tt.getKeywordTokenType(444);}
"year" {return tt.getKeywordTokenType(445);}
"yes" {return tt.getKeywordTokenType(446);}
"zone" {return tt.getKeywordTokenType(447);}
"false" {return tt.getKeywordTokenType(448);}
"true" {return tt.getKeywordTokenType(449);}








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
"json_array" {return tt.getFunctionTokenType(65);}
"json_arrayagg" {return tt.getFunctionTokenType(66);}
"json_dataguide" {return tt.getFunctionTokenType(67);}
"json_object" {return tt.getFunctionTokenType(68);}
"json_objectagg" {return tt.getFunctionTokenType(69);}
"json_query" {return tt.getFunctionTokenType(70);}
"json_table" {return tt.getFunctionTokenType(71);}
"json_value" {return tt.getFunctionTokenType(72);}
"lag" {return tt.getFunctionTokenType(73);}
"last_day" {return tt.getFunctionTokenType(74);}
"last_value" {return tt.getFunctionTokenType(75);}
"lateral" {return tt.getFunctionTokenType(76);}
"lead" {return tt.getFunctionTokenType(77);}
"least" {return tt.getFunctionTokenType(78);}
"length" {return tt.getFunctionTokenType(79);}
"length2" {return tt.getFunctionTokenType(80);}
"length4" {return tt.getFunctionTokenType(81);}
"lengthb" {return tt.getFunctionTokenType(82);}
"lengthc" {return tt.getFunctionTokenType(83);}
"listagg" {return tt.getFunctionTokenType(84);}
"ln" {return tt.getFunctionTokenType(85);}
"lnnvl" {return tt.getFunctionTokenType(86);}
"localtimestamp" {return tt.getFunctionTokenType(87);}
"lower" {return tt.getFunctionTokenType(88);}
"lpad" {return tt.getFunctionTokenType(89);}
"ltrim" {return tt.getFunctionTokenType(90);}
"make_ref" {return tt.getFunctionTokenType(91);}
"max" {return tt.getFunctionTokenType(92);}
"median" {return tt.getFunctionTokenType(93);}
"min" {return tt.getFunctionTokenType(94);}
"mod" {return tt.getFunctionTokenType(95);}
"months_between" {return tt.getFunctionTokenType(96);}
"nanvl" {return tt.getFunctionTokenType(97);}
"nchr" {return tt.getFunctionTokenType(98);}
"new_time" {return tt.getFunctionTokenType(99);}
"next_day" {return tt.getFunctionTokenType(100);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(101);}
"nls_charset_id" {return tt.getFunctionTokenType(102);}
"nls_charset_name" {return tt.getFunctionTokenType(103);}
"nls_initcap" {return tt.getFunctionTokenType(104);}
"nls_lower" {return tt.getFunctionTokenType(105);}
"nls_upper" {return tt.getFunctionTokenType(106);}
"nlssort" {return tt.getFunctionTokenType(107);}
"ntile" {return tt.getFunctionTokenType(108);}
"nullif" {return tt.getFunctionTokenType(109);}
"numtodsinterval" {return tt.getFunctionTokenType(110);}
"numtoyminterval" {return tt.getFunctionTokenType(111);}
"nvl" {return tt.getFunctionTokenType(112);}
"nvl2" {return tt.getFunctionTokenType(113);}
"ora_hash" {return tt.getFunctionTokenType(114);}
"percent_rank" {return tt.getFunctionTokenType(115);}
"percentile_cont" {return tt.getFunctionTokenType(116);}
"percentile_disc" {return tt.getFunctionTokenType(117);}
"powermultiset" {return tt.getFunctionTokenType(118);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(119);}
"presentnnv" {return tt.getFunctionTokenType(120);}
"presentv" {return tt.getFunctionTokenType(121);}
"previous" {return tt.getFunctionTokenType(122);}
"rank" {return tt.getFunctionTokenType(123);}
"ratio_to_report" {return tt.getFunctionTokenType(124);}
"rawtohex" {return tt.getFunctionTokenType(125);}
"rawtonhex" {return tt.getFunctionTokenType(126);}
"reftohex" {return tt.getFunctionTokenType(127);}
"regexp_instr" {return tt.getFunctionTokenType(128);}
"regexp_replace" {return tt.getFunctionTokenType(129);}
"regexp_substr" {return tt.getFunctionTokenType(130);}
"regr_avgx" {return tt.getFunctionTokenType(131);}
"regr_avgy" {return tt.getFunctionTokenType(132);}
"regr_count" {return tt.getFunctionTokenType(133);}
"regr_intercept" {return tt.getFunctionTokenType(134);}
"regr_r2" {return tt.getFunctionTokenType(135);}
"regr_slope" {return tt.getFunctionTokenType(136);}
"regr_sxx" {return tt.getFunctionTokenType(137);}
"regr_sxy" {return tt.getFunctionTokenType(138);}
"regr_syy" {return tt.getFunctionTokenType(139);}
"round" {return tt.getFunctionTokenType(140);}
"row_number" {return tt.getFunctionTokenType(141);}
"rowidtochar" {return tt.getFunctionTokenType(142);}
"rowidtonchar" {return tt.getFunctionTokenType(143);}
"rpad" {return tt.getFunctionTokenType(144);}
"rtrim" {return tt.getFunctionTokenType(145);}
"scn_to_timestamp" {return tt.getFunctionTokenType(146);}
"sessiontimezone" {return tt.getFunctionTokenType(147);}
"sign" {return tt.getFunctionTokenType(148);}
"sin" {return tt.getFunctionTokenType(149);}
"sinh" {return tt.getFunctionTokenType(150);}
"soundex" {return tt.getFunctionTokenType(151);}
"sqrt" {return tt.getFunctionTokenType(152);}
"stats_binomial_test" {return tt.getFunctionTokenType(153);}
"stats_crosstab" {return tt.getFunctionTokenType(154);}
"stats_f_test" {return tt.getFunctionTokenType(155);}
"stats_ks_test" {return tt.getFunctionTokenType(156);}
"stats_mode" {return tt.getFunctionTokenType(157);}
"stats_mw_test" {return tt.getFunctionTokenType(158);}
"stats_one_way_anova" {return tt.getFunctionTokenType(159);}
"stats_t_test_indep" {return tt.getFunctionTokenType(160);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(161);}
"stats_t_test_one" {return tt.getFunctionTokenType(162);}
"stats_t_test_paired" {return tt.getFunctionTokenType(163);}
"stats_wsr_test" {return tt.getFunctionTokenType(164);}
"stddev" {return tt.getFunctionTokenType(165);}
"stddev_pop" {return tt.getFunctionTokenType(166);}
"stddev_samp" {return tt.getFunctionTokenType(167);}
"substr" {return tt.getFunctionTokenType(168);}
"substr2" {return tt.getFunctionTokenType(169);}
"substr4" {return tt.getFunctionTokenType(170);}
"substrb" {return tt.getFunctionTokenType(171);}
"substrc" {return tt.getFunctionTokenType(172);}
"sum" {return tt.getFunctionTokenType(173);}
"sys_connect_by_path" {return tt.getFunctionTokenType(174);}
"sys_context" {return tt.getFunctionTokenType(175);}
"sys_dburigen" {return tt.getFunctionTokenType(176);}
"sys_extract_utc" {return tt.getFunctionTokenType(177);}
"sys_guid" {return tt.getFunctionTokenType(178);}
"sys_typeid" {return tt.getFunctionTokenType(179);}
"sys_xmlagg" {return tt.getFunctionTokenType(180);}
"sys_xmlgen" {return tt.getFunctionTokenType(181);}
"sysdate" {return tt.getFunctionTokenType(182);}
"systimestamp" {return tt.getFunctionTokenType(183);}
"tan" {return tt.getFunctionTokenType(184);}
"tanh" {return tt.getFunctionTokenType(185);}
"timestamp_to_scn" {return tt.getFunctionTokenType(186);}
"to_binary_double" {return tt.getFunctionTokenType(187);}
"to_binary_float" {return tt.getFunctionTokenType(188);}
"to_char" {return tt.getFunctionTokenType(189);}
"to_clob" {return tt.getFunctionTokenType(190);}
"to_date" {return tt.getFunctionTokenType(191);}
"to_dsinterval" {return tt.getFunctionTokenType(192);}
"to_lob" {return tt.getFunctionTokenType(193);}
"to_multi_byte" {return tt.getFunctionTokenType(194);}
"to_nchar" {return tt.getFunctionTokenType(195);}
"to_nclob" {return tt.getFunctionTokenType(196);}
"to_number" {return tt.getFunctionTokenType(197);}
"to_single_byte" {return tt.getFunctionTokenType(198);}
"to_timestamp" {return tt.getFunctionTokenType(199);}
"to_timestamp_tz" {return tt.getFunctionTokenType(200);}
"to_yminterval" {return tt.getFunctionTokenType(201);}
"translate" {return tt.getFunctionTokenType(202);}
"treat" {return tt.getFunctionTokenType(203);}
"trim" {return tt.getFunctionTokenType(204);}
"trunc" {return tt.getFunctionTokenType(205);}
"tz_offset" {return tt.getFunctionTokenType(206);}
"uid" {return tt.getFunctionTokenType(207);}
"unistr" {return tt.getFunctionTokenType(208);}
"updatexml" {return tt.getFunctionTokenType(209);}
"upper" {return tt.getFunctionTokenType(210);}
"userenv" {return tt.getFunctionTokenType(211);}
"validate_conversion" {return tt.getFunctionTokenType(212);}
"var_pop" {return tt.getFunctionTokenType(213);}
"var_samp" {return tt.getFunctionTokenType(214);}
"variance" {return tt.getFunctionTokenType(215);}
"vsize" {return tt.getFunctionTokenType(216);}
"width_bucket" {return tt.getFunctionTokenType(217);}
"xmlagg" {return tt.getFunctionTokenType(218);}
"xmlattributes" {return tt.getFunctionTokenType(219);}
"xmlcast" {return tt.getFunctionTokenType(220);}
"xmlcdata" {return tt.getFunctionTokenType(221);}
"xmlcolattval" {return tt.getFunctionTokenType(222);}
"xmlcomment" {return tt.getFunctionTokenType(223);}
"xmlconcat" {return tt.getFunctionTokenType(224);}
"xmldiff" {return tt.getFunctionTokenType(225);}
"xmlelement" {return tt.getFunctionTokenType(226);}
"xmlforest" {return tt.getFunctionTokenType(227);}
"xmlisvalid" {return tt.getFunctionTokenType(228);}
"xmlparse" {return tt.getFunctionTokenType(229);}
"xmlpatch" {return tt.getFunctionTokenType(230);}
"xmlpi" {return tt.getFunctionTokenType(231);}
"xmlquery" {return tt.getFunctionTokenType(232);}
"xmlroot" {return tt.getFunctionTokenType(233);}
"xmlsequence" {return tt.getFunctionTokenType(234);}
"xmlserialize" {return tt.getFunctionTokenType(235);}
"xmltable" {return tt.getFunctionTokenType(236);}
"xmltransform" {return tt.getFunctionTokenType(237);}










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


