// created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
// skeleton Java 1.0 (c) 2002 ats@cs.rit.edu

					// line 2 "Ripper19Parser.y"
/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2008-2009 Thomas E Enebo <enebo@acm.org>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.parser;

import java.io.IOException;

import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.common.IRubyWarnings;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.lexer.yacc.ISourcePositionHolder;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.RubyYaccLexer;
import org.jruby.lexer.yacc.RubyYaccLexer.LexState;
import org.jruby.lexer.yacc.StrTerm;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.lexer.yacc.SyntaxException.PID;
import org.jruby.lexer.yacc.Token;
import org.jruby.util.ByteList;

public class Ripper19Parser implements RubyParser {
    protected RipperSupport support;
    protected RubyYaccLexer lexer;

    public Ripper19Parser() {
        this(new RipperSupport());
    }

    public Ripper19Parser(RipperSupport support) {
        this.support = support;
        lexer = new RubyYaccLexer(false);
        lexer.setParserSupport(support);
        support.setLexer(lexer);
    }

    public void setWarnings(IRubyWarnings warnings) {
        support.setWarnings(warnings);
        lexer.setWarnings(warnings);
    }
					// line 71 "-"
  // %token constants
  public static final int kCLASS = 257;
  public static final int kMODULE = 258;
  public static final int kDEF = 259;
  public static final int kUNDEF = 260;
  public static final int kBEGIN = 261;
  public static final int kRESCUE = 262;
  public static final int kENSURE = 263;
  public static final int kEND = 264;
  public static final int kIF = 265;
  public static final int kUNLESS = 266;
  public static final int kTHEN = 267;
  public static final int kELSIF = 268;
  public static final int kELSE = 269;
  public static final int kCASE = 270;
  public static final int kWHEN = 271;
  public static final int kWHILE = 272;
  public static final int kUNTIL = 273;
  public static final int kFOR = 274;
  public static final int kBREAK = 275;
  public static final int kNEXT = 276;
  public static final int kREDO = 277;
  public static final int kRETRY = 278;
  public static final int kIN = 279;
  public static final int kDO = 280;
  public static final int kDO_COND = 281;
  public static final int kDO_BLOCK = 282;
  public static final int kRETURN = 283;
  public static final int kYIELD = 284;
  public static final int kSUPER = 285;
  public static final int kSELF = 286;
  public static final int kNIL = 287;
  public static final int kTRUE = 288;
  public static final int kFALSE = 289;
  public static final int kAND = 290;
  public static final int kOR = 291;
  public static final int kNOT = 292;
  public static final int kIF_MOD = 293;
  public static final int kUNLESS_MOD = 294;
  public static final int kWHILE_MOD = 295;
  public static final int kUNTIL_MOD = 296;
  public static final int kRESCUE_MOD = 297;
  public static final int kALIAS = 298;
  public static final int kDEFINED = 299;
  public static final int klBEGIN = 300;
  public static final int klEND = 301;
  public static final int k__LINE__ = 302;
  public static final int k__FILE__ = 303;
  public static final int k__ENCODING__ = 304;
  public static final int kDO_LAMBDA = 305;
  public static final int tIDENTIFIER = 306;
  public static final int tFID = 307;
  public static final int tGVAR = 308;
  public static final int tIVAR = 309;
  public static final int tCONSTANT = 310;
  public static final int tCVAR = 311;
  public static final int tLABEL = 312;
  public static final int tCHAR = 313;
  public static final int tUPLUS = 314;
  public static final int tUMINUS = 315;
  public static final int tUMINUS_NUM = 316;
  public static final int tPOW = 317;
  public static final int tCMP = 318;
  public static final int tEQ = 319;
  public static final int tEQQ = 320;
  public static final int tNEQ = 321;
  public static final int tGEQ = 322;
  public static final int tLEQ = 323;
  public static final int tANDOP = 324;
  public static final int tOROP = 325;
  public static final int tMATCH = 326;
  public static final int tNMATCH = 327;
  public static final int tDOT = 328;
  public static final int tDOT2 = 329;
  public static final int tDOT3 = 330;
  public static final int tAREF = 331;
  public static final int tASET = 332;
  public static final int tLSHFT = 333;
  public static final int tRSHFT = 334;
  public static final int tCOLON2 = 335;
  public static final int tCOLON3 = 336;
  public static final int tOP_ASGN = 337;
  public static final int tASSOC = 338;
  public static final int tLPAREN = 339;
  public static final int tLPAREN2 = 340;
  public static final int tRPAREN = 341;
  public static final int tLPAREN_ARG = 342;
  public static final int tLBRACK = 343;
  public static final int tRBRACK = 344;
  public static final int tLBRACE = 345;
  public static final int tLBRACE_ARG = 346;
  public static final int tSTAR = 347;
  public static final int tSTAR2 = 348;
  public static final int tAMPER = 349;
  public static final int tAMPER2 = 350;
  public static final int tTILDE = 351;
  public static final int tPERCENT = 352;
  public static final int tDIVIDE = 353;
  public static final int tPLUS = 354;
  public static final int tMINUS = 355;
  public static final int tLT = 356;
  public static final int tGT = 357;
  public static final int tPIPE = 358;
  public static final int tBANG = 359;
  public static final int tCARET = 360;
  public static final int tLCURLY = 361;
  public static final int tRCURLY = 362;
  public static final int tBACK_REF2 = 363;
  public static final int tSYMBEG = 364;
  public static final int tSTRING_BEG = 365;
  public static final int tXSTRING_BEG = 366;
  public static final int tREGEXP_BEG = 367;
  public static final int tWORDS_BEG = 368;
  public static final int tQWORDS_BEG = 369;
  public static final int tSTRING_DBEG = 370;
  public static final int tSTRING_DVAR = 371;
  public static final int tSTRING_END = 372;
  public static final int tLAMBDA = 373;
  public static final int tLAMBEG = 374;
  public static final int tNTH_REF = 375;
  public static final int tBACK_REF = 376;
  public static final int tSTRING_CONTENT = 377;
  public static final int tINTEGER = 378;
  public static final int tFLOAT = 379;
  public static final int tREGEXP_END = 380;
  public static final int tLOWEST = 381;
  public static final int variable = 382;
  public static final int yyErrorCode = 256;

  /** number of final state.
    */
  protected static final int yyFinal = 1;

  /** parser tables.
      Order is mandated by <i>jay</i>.
    */
  protected static final short[] yyLhs = {
//yyLhs 559
    -1,   123,     0,   119,   120,   120,   120,   120,   121,   126,
   121,    34,    33,    35,    35,    35,    35,   127,    36,    36,
    36,    36,    36,    36,    36,    36,    36,    36,    36,    36,
    36,    36,    36,    36,    36,    36,    36,    36,    36,    36,
    36,    36,    31,    31,    37,    37,    37,    37,    37,    37,
    41,    32,    32,    55,    55,    55,   129,    94,    40,    40,
    40,    40,    40,    40,    40,    40,    40,    40,    40,    95,
    95,   106,   106,    96,    96,    96,    96,    96,    96,    96,
    96,    96,    96,    67,    67,    81,    81,    85,    85,    68,
    68,    68,    68,    68,    68,    68,    68,    68,    73,    73,
    73,    73,    73,    73,    73,    73,    73,     6,     6,    30,
    30,    30,     7,     7,     7,     7,     7,    99,    99,   100,
   100,    57,   130,    57,     8,     8,     8,     8,     8,     8,
     8,     8,     8,     8,     8,     8,     8,     8,     8,     8,
     8,     8,     8,     8,     8,     8,     8,     8,     8,     8,
     8,     8,     8,   117,   117,   117,   117,   117,   117,   117,
   117,   117,   117,   117,   117,   117,   117,   117,   117,   117,
   117,   117,   117,   117,   117,   117,   117,   117,   117,   117,
   117,   117,   117,   117,   117,   117,   117,   117,   117,   117,
   117,   117,   117,   117,   117,    38,    38,    38,    38,    38,
    38,    38,    38,    38,    38,    38,    38,    38,    38,    38,
    38,    38,    38,    38,    38,    38,    38,    38,    38,    38,
    38,    38,    38,    38,    38,    38,    38,    38,    38,    38,
    38,    38,    38,    38,    38,    38,    38,    38,    38,    38,
    69,    72,    72,    72,    72,    49,    53,    53,   109,   109,
   109,   109,   109,    47,    47,    47,    47,    47,   132,    51,
    88,    87,    87,    75,    75,    75,    75,    66,    66,    66,
    39,    39,    39,    39,    39,    39,    39,    39,    39,    39,
   133,    39,    39,    39,    39,    39,    39,    39,    39,    39,
    39,    39,    39,    39,    39,    39,    39,    39,    39,    39,
   135,   136,    39,   137,   138,    39,    39,    39,   139,   140,
    39,   141,    39,   143,   144,    39,   145,    39,   146,    39,
   147,   148,    39,    39,    39,    39,    39,    42,   134,   134,
   134,   122,   122,    45,    45,    43,    43,   108,   108,   110,
   110,    80,    80,   111,   111,   111,   111,   111,   111,   111,
   111,   111,    63,    63,    63,    63,    63,    63,    63,    63,
    63,    63,    63,    63,    63,    63,    63,    65,    65,    64,
    64,    64,   103,   103,   102,   102,   112,   112,   149,   105,
    62,    62,   104,   104,   150,    93,    54,    54,    54,    23,
    23,    23,    23,    23,    23,    23,    23,    23,   151,    92,
   152,    92,    70,    44,    44,    97,    97,    71,    71,    71,
    46,    46,    48,    48,    27,    27,    27,    15,    16,    16,
    16,    17,    18,    19,    24,    24,    77,    77,    26,    26,
    25,    25,    76,    76,    20,    20,    21,    21,   153,   153,
    22,   154,    22,   155,    22,    58,    58,    58,    58,     2,
     1,     1,     1,     1,    29,    28,    28,    28,    28,   113,
   113,   113,   113,   113,   114,   114,   114,   114,   114,   114,
   114,    52,    52,    98,    98,    59,    59,    50,   156,    50,
    50,    61,    61,    60,    60,    60,    60,    60,    60,    60,
    60,    60,    60,    60,    60,    60,    60,    60,   118,   118,
   118,   118,     9,     9,   101,   101,    78,    78,    56,   107,
    86,    86,    79,    79,    11,    11,    13,    13,    12,    12,
    91,    90,    90,    14,   157,    14,    84,    84,    82,    82,
    83,    83,     3,     3,     3,     4,     4,     4,     4,     5,
     5,     5,    10,    10,   124,   124,   128,   128,   115,   116,
   131,   131,   131,   142,   142,   125,   125,    74,    89,
    }, yyLen = {
//yyLen 559
     2,     0,     2,     2,     1,     1,     3,     2,     1,     0,
     5,     4,     2,     1,     1,     3,     2,     0,     4,     3,
     3,     3,     2,     3,     3,     3,     3,     3,     4,     1,
     3,     3,     6,     5,     5,     5,     5,     3,     3,     3,
     3,     1,     3,     3,     1,     3,     3,     3,     2,     1,
     1,     1,     1,     1,     4,     4,     0,     5,     2,     3,
     4,     5,     4,     5,     2,     2,     2,     2,     2,     1,
     3,     1,     3,     1,     2,     3,     5,     2,     4,     2,
     4,     1,     3,     1,     3,     2,     3,     1,     3,     1,
     1,     4,     3,     3,     3,     3,     2,     1,     1,     1,
     4,     3,     3,     3,     3,     2,     1,     1,     1,     2,
     1,     3,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     0,     4,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     3,     5,     3,     5,     6,
     5,     5,     5,     5,     4,     3,     3,     3,     3,     3,
     3,     3,     3,     3,     4,     4,     2,     2,     3,     3,
     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,
     3,     2,     2,     3,     3,     3,     3,     3,     6,     1,
     1,     1,     2,     4,     2,     3,     1,     1,     1,     1,
     2,     4,     2,     1,     2,     2,     4,     1,     0,     2,
     2,     2,     1,     1,     2,     3,     4,     3,     4,     2,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     3,
     0,     4,     3,     3,     2,     3,     3,     1,     4,     3,
     1,     5,     4,     3,     2,     1,     2,     2,     6,     6,
     0,     0,     7,     0,     0,     7,     5,     4,     0,     0,
     9,     0,     6,     0,     0,     8,     0,     5,     0,     6,
     0,     0,     9,     1,     1,     1,     1,     1,     1,     1,
     2,     1,     1,     1,     5,     1,     2,     1,     1,     1,
     3,     1,     3,     1,     4,     6,     3,     5,     2,     4,
     1,     3,     6,     8,     4,     6,     4,     2,     6,     2,
     4,     6,     2,     4,     2,     4,     1,     1,     1,     3,
     1,     4,     1,     2,     1,     3,     1,     1,     0,     3,
     4,     2,     3,     3,     0,     5,     2,     4,     4,     2,
     4,     4,     3,     3,     3,     2,     1,     4,     0,     5,
     0,     5,     5,     1,     1,     6,     1,     1,     1,     1,
     2,     1,     2,     1,     1,     1,     1,     1,     1,     1,
     2,     3,     3,     3,     3,     3,     0,     3,     1,     2,
     3,     3,     0,     3,     0,     2,     0,     2,     0,     2,
     1,     0,     3,     0,     4,     1,     1,     1,     1,     2,
     1,     1,     1,     1,     3,     1,     1,     2,     2,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     0,     4,
     2,     3,     2,     6,     8,     4,     6,     4,     6,     2,
     4,     6,     2,     4,     2,     4,     1,     0,     1,     1,
     1,     1,     1,     1,     1,     3,     1,     3,     3,     3,
     1,     3,     1,     3,     1,     1,     2,     1,     1,     1,
     2,     2,     0,     1,     0,     4,     1,     2,     1,     3,
     3,     2,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     0,     1,     0,     1,     2,     2,
     0,     1,     1,     1,     1,     1,     2,     0,     0,
    }, yyDefRed = {
//yyDefRed 972
     1,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   300,   303,     0,     0,     0,   325,   326,     0,
     0,     0,   465,   464,   466,   467,     0,     0,     0,     9,
     0,   469,   468,   470,     0,     0,   461,   460,     0,   463,
   418,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   434,   436,   436,     0,     0,   378,   475,
   476,   455,   456,    89,   415,     0,   271,     0,   419,   272,
   273,     0,   274,   275,   270,   414,   416,    29,    44,     0,
     0,     0,     0,     0,     0,   276,     0,    52,     0,     0,
    83,     0,     4,     0,     0,    69,     0,     0,     0,     2,
     0,     5,     7,   323,   324,   287,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   110,     0,   327,
     0,   277,   471,   472,   316,   163,   174,   164,   187,   160,
   180,   170,   169,   185,   168,   167,   162,   188,   172,   161,
   175,   179,   181,   173,   166,   182,   189,   184,     0,     0,
     0,     0,   159,   178,   177,   190,   191,   192,   193,   194,
   158,   165,   156,   157,     0,     0,     0,     0,   114,     0,
   148,   149,   145,   127,   128,   129,   136,   133,   135,   130,
   131,   150,   151,   137,   138,   524,   142,   141,   126,   147,
   144,   143,   139,   140,   134,   132,   124,   146,   125,   152,
   318,   115,     0,   523,   116,   183,   176,   186,   171,   153,
   154,   155,   112,   113,   118,   117,   120,     0,   119,   121,
     0,     0,     0,     0,     0,    13,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   553,   554,     0,     0,     0,
   555,     0,     0,     0,     0,     0,   337,   338,     0,     0,
     0,     0,     0,     0,     0,     0,   253,    67,     0,     0,
     0,   528,   257,    68,    66,     0,    65,     0,     0,   395,
    64,     0,   547,     0,     0,    17,     0,     0,     0,   216,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   241,     0,     0,     0,   526,     0,     0,     0,     0,
     0,     0,     0,     0,   232,    48,   231,   452,   451,   453,
   449,   450,     0,     0,     0,     0,     0,     0,     0,     0,
   297,     0,   400,   398,   389,     0,   294,   420,   296,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   384,   386,     0,     0,     0,     0,     0,     0,
    85,     0,     0,     0,     0,     0,     0,     3,     0,     0,
   457,   458,     0,   107,     0,   109,     0,   478,   311,   477,
     0,     0,     0,     0,     0,     0,   542,   543,   320,   122,
     0,     0,   406,     0,   279,    12,     0,     0,   329,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   556,     0,     0,     0,     0,     0,     0,   308,   531,
   264,   260,     0,     0,   254,   262,     0,   255,     0,   289,
     0,   259,   249,   248,     0,     0,     0,     0,   293,    47,
    19,    21,    20,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   282,     0,     0,   285,     0,   551,
   242,     0,   244,   527,   286,     0,    87,     0,     0,     0,
     0,     0,   443,   441,   454,   440,   437,   421,   435,   422,
   423,   424,   425,   428,     0,   430,   431,     0,     0,   500,
   499,   498,   501,     0,     0,   515,   514,   519,   518,   504,
     0,     0,     0,   512,     0,     0,     0,     0,   496,   506,
   502,     0,     0,    56,    59,    23,    24,    25,    26,    27,
    45,    46,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   537,     0,     0,   538,   393,     0,     0,     0,     0,   392,
     0,   394,     0,   535,   536,     0,     0,    37,     0,     0,
    43,    42,     0,    38,   263,     0,     0,     0,     0,     0,
    86,    30,    40,     0,    31,     0,     6,     0,   480,     0,
     0,     0,     0,     0,     0,   111,     0,     0,     0,     0,
     0,     0,     0,     0,   408,     0,     0,   409,     0,     0,
   335,     0,     0,   330,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   307,   332,   301,   331,   304,     0,     0,
     0,     0,     0,     0,   530,     0,     0,     0,   261,   529,
   288,   548,     0,     0,   245,   292,    18,     0,     0,    28,
     0,     0,     0,     0,   281,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   427,   429,   433,     0,   503,
     0,     0,   339,     0,   341,     0,     0,   516,   520,     0,
   494,     0,   372,   381,     0,     0,   379,     0,   489,     0,
   492,   370,     0,   368,     0,   367,     0,     0,     0,     0,
     0,     0,   247,     0,   390,   246,     0,     0,   391,     0,
     0,     0,    54,   387,    55,   388,     0,     0,     0,     0,
    84,     0,     0,     0,   314,     0,     0,   397,   317,   525,
     0,   482,     0,   321,   123,     0,     0,   411,   336,     0,
    11,   413,     0,   333,     0,     0,     0,     0,     0,     0,
     0,   306,     0,     0,     0,     0,     0,     0,   266,   256,
     0,   291,    10,   243,    88,     0,     0,   445,   446,   447,
   442,   448,   508,     0,     0,     0,     0,   505,     0,     0,
   521,   376,     0,   374,   377,     0,     0,     0,     0,   507,
     0,   513,     0,     0,     0,     0,     0,     0,   366,     0,
   510,     0,     0,     0,     0,     0,    33,     0,    34,     0,
    61,    36,     0,    35,     0,    63,     0,   549,     0,     0,
     0,     0,     0,     0,   479,   312,   481,   319,     0,     0,
     0,     0,   410,     0,     0,   412,     0,   298,     0,   299,
   265,     0,     0,     0,   309,     0,   444,   340,     0,     0,
     0,   342,   380,     0,   495,     0,   383,   382,     0,   487,
     0,   485,     0,   490,   493,     0,     0,   364,     0,     0,
   359,     0,   362,   369,   401,   399,     0,     0,   385,    32,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   403,   402,   404,   302,   305,     0,     0,     0,     0,     0,
   375,     0,     0,     0,     0,     0,     0,     0,   371,     0,
     0,     0,     0,   511,    57,   315,     0,     0,     0,     0,
     0,     0,   405,     0,     0,     0,     0,   488,     0,   483,
   486,   491,   284,     0,   365,     0,   356,     0,   354,     0,
   360,   363,   322,     0,   334,   310,     0,     0,     0,     0,
     0,     0,     0,     0,   484,   358,     0,   352,   355,   361,
     0,   353,
    }, yyDgoto = {
//yyDgoto 158
     1,   310,    64,   116,   604,   569,   117,   215,   563,   509,
   398,   510,   511,   512,   202,    66,    67,    68,    69,    70,
   313,   312,   486,    71,    72,    73,   494,    74,    75,    76,
   118,    77,    78,   221,   222,   223,   224,    80,    81,    82,
    83,   228,   280,   753,   901,   754,   746,   442,   750,   571,
   388,   266,    85,   714,    86,    87,   513,   217,   780,   230,
   610,   611,   515,   805,   703,   704,   583,    89,    90,   258,
   420,   616,   290,   231,   225,   444,   319,   317,   516,   517,
   683,    93,   445,   261,   297,   477,   807,   434,   262,   435,
   690,   790,   326,   363,   524,    94,    95,   403,   232,   218,
   219,   519,   792,   693,   696,   320,   288,   810,   248,   446,
   684,   685,   793,    97,   233,   439,   720,   204,   520,    99,
   100,   101,   635,     2,   238,   239,   277,   453,   440,   707,
   613,   470,   267,   466,   409,   241,   764,   242,   765,   643,
   905,   600,   410,   597,   833,   393,   395,   612,   838,   321,
   558,   522,   521,     0,   674,   673,   599,   394,
    }, yySindex = {
//yySindex 972
     0,     0, 14914, 15294, 17886, 18009, 18594, 18486, 15041, 17394,
 17394, 13273,     0,     0,  5773, 15672, 15672,     0,     0, 15672,
  -182,  -154,     0,     0,     0,     0,    70, 18378,   185,     0,
  -149,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0, 17517, 17517,   -72,   -95, 15168, 17394, 16041, 16410, 14407,
 17517, 17640, 18701,     0,     0,     0,   198,   262,     0,     0,
     0,     0,     0,     0,     0,  -161,     0,   -68,     0,     0,
     0,  -160,     0,     0,     0,     0,     0,     0,     0,  1212,
    20,  5182,     0,    55,    -8,     0,  -126,     0,     7,   296,
     0,   287,     0,  6269,   297,     0,   172,     0,     0,     0,
   135,     0,     0,     0,     0,     0,  -182,  -154,   181,   185,
     0,     0,   286, 17394,  -123, 15041,  -161,     0,    94,     0,
   211,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   -53,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   326,     0,     0,
 15420,   285,   293,   135,  1212,     0,   242,    20,   250,   369,
   223,   512,   247,     0,   250,     0,     0,   135,   325,   540,
     0, 17394, 17394,   301,   438,     0,     0,     0,   348,     0,
     0, 17517, 17517, 17517, 17517,  5182,     0,     0,   292,   589,
   594,     0,     0,     0,     0,  3753,     0, 15672, 15672,     0,
     0,  4739,     0, 17394,  -171,     0, 16533,   279, 15041,     0,
   481,   345,   352,   360,   343, 15168,   344,     0,   185,    20,
   346,     0,   147,   257,   292,     0,   257,   339,   392, 15546,
   484,     0,   667,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   613,   921,   970,   375,   347,  1018,   351,   -92,
     0,  1696,     0,     0,     0,   385,     0,     0,     0, 17394,
 17394, 17394, 17394, 15420, 17394, 17394, 17517, 17517, 17517, 17517,
 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517,
 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517, 17517,
 17517, 17517,     0,     0,  2722,  3252, 15672, 19358, 19358, 17640,
     0, 16656, 15168, 14534,   690, 16656, 17640,     0, 14660,   399,
     0,     0,    20,     0,     0,     0,   135,     0,     0,     0,
  4238,  5312, 15672, 15041, 17394,  1713,     0,     0,     0,     0,
  1212, 16779,     0,   446,     0,     0, 14787,   343,     0, 15041,
   494, 18863, 18918, 15672, 17517, 17517, 17517, 15041,   325, 16902,
   486,     0,   106,   106,     0, 18973, 19028, 15672,     0,     0,
     0,     0, 17517, 15795,     0,     0, 16164,     0,   185,     0,
   407,     0,     0,     0,   721,   724,   185,   240,     0,     0,
     0,     0,     0, 18486, 17394,  5182, 14914,   408, 18863, 18918,
 17517, 17517, 17517,   185,     0,     0,   185,     0, 16287,     0,
     0, 16410,     0,     0,     0,     0,     0,   725, 19083, 19138,
 15672, 15546,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,    68,     0,     0,   745,   710,     0,
     0,     0,     0,  1007,  1976,     0,     0,     0,     0,     0,
   474,   476,   744,     0,   730,  -181,   753,   755,     0,     0,
     0,  -184,  -184,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   345,  2270,  2270,  2270,  2270,  1732,  1732,  4286,
  3300,  2270,  2270,  2807,  2807,  1293,  1293,   345,  1854,   345,
   345,   -49,   -49,  1732,  1732,  1532,  1532,  2667,  -184,   473,
     0,   477,  -154,     0,     0,   478,     0,   488,  -154,     0,
     0,     0,   185,     0,     0,  -154,  -154,     0,  5182, 17517,
     0,     0,  4816,     0,     0,   759,   782,   185, 15546,   784,
     0,     0,     0,     0,     0,  5251,     0,   135,     0, 17394,
 15041,  -154,     0,     0,  -154,     0,   185,   519,   240,  1976,
   135, 15041, 18808, 18486,     0,     0,   491,     0, 15041,   576,
     0,  1212,   377,     0,   503,   508,   509,   513,   185,  4816,
   446,   567,    74,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   185, 17394,     0, 17517,   292,   594,     0,     0,
     0,     0, 15795, 16164,     0,     0,     0,   240,   492,     0,
   345,   345,  5182,     0,     0,   257, 15546,     0,     0,     0,
     0,   185,   725, 15041,  -133,     0,     0,     0, 17517,     0,
  1007,   524,     0,   812,     0,   185,   730,     0,     0,  1512,
     0,   928,     0,     0, 15041, 15041,     0,  1976,     0,  1976,
     0,     0,  1318,     0, 15041,     0, 15041,  -184,   799, 15041,
 17640, 17640,     0,   385,     0,     0, 17640, 17640,     0,   385,
   522,   517,     0,     0,     0,     0,     0, 17517, 17640, 17025,
     0,   725, 15546, 17517,     0,   135,   601,     0,     0,     0,
   185,     0,   605,     0,     0, 18132,   250,     0,     0, 15041,
     0,     0, 17394,     0,   606, 17517, 17517, 17517, 17517,   534,
   608,     0, 17148, 15041, 15041, 15041,     0,   106,     0,     0,
   834,     0,     0,     0,     0,     0,   521,     0,     0,     0,
     0,     0,     0,   185,  1305,   840,  1190,     0,   551,   850,
     0,     0,   851,     0,     0,   636,   539,   858,   860,     0,
   861,     0,   850,   845,   878,   730,   882,   889,     0,   578,
     0,   677,   583, 15041, 17517,   686,     0,  5182,     0,  5182,
     0,     0,  5182,     0,  5182,     0, 17640,     0,  5182, 17517,
     0,   725,  5182, 15041,     0,     0,     0,     0,  1713,   646,
   525,     0,     0,     0, 15041,     0,   250,     0, 17517,     0,
     0,    21,   694,   696,     0, 16164,     0,     0,   917,  1305,
   976,     0,     0,  1512,     0,   928,     0,     0,  1512,     0,
  1976,     0,  1512,     0,     0, 18255,  1512,     0,   610,  2154,
     0,  2154,     0,     0,     0,     0,   609,  5182,     0,     0,
  5182,     0,   712, 15041,     0, 19193, 19248, 15672,   285, 15041,
     0,     0,     0,     0,     0, 15041,  1305,   917,  1305,   938,
     0,   850,   950,   850,   850,   687,   629,   850,     0,   955,
   956,   967,   850,     0,     0,     0,   748,     0,     0,     0,
     0,   185,     0,   377,   749,   917,  1305,     0,  1512,     0,
     0,     0,     0, 19303,     0,  1512,     0,  2154,     0,  1512,
     0,     0,     0,     0,     0,     0,   917,   850,     0,     0,
   850,   972,   850,   850,     0,     0,  1512,     0,     0,     0,
   850,     0,
    }, yyRindex = {
//yyRindex 972
     0,     0,   188,     0,     0,     0,     0,     0,   604,     0,
     0,   750,     0,     0,     0, 13424, 13554,     0,     0, 13664,
  5067,  4574,     0,     0,     0,     0, 17763,     0, 17271,     0,
     0,     0,     0,     0,  2479,  3588,     0,     0,  2602,     0,
     0,     0,     0,     0,     0,   122,     0,   688,   666,    81,
     0,     0,  1045,     0,     0,     0,  1066,   -84,     0,     0,
     0,     0,     0,     0,     0, 15918,     0,  7301,     0,     0,
     0,  7445,     0,     0,     0,     0,     0,     0,     0,    37,
  5674,  2288,  7549,  6898,     0,     0, 14313,     0, 14034,     0,
     0,     0,     0,    95,     0,     0,     0,  6506, 13905,     0,
    30,     0,     0,     0,     0,     0,  7662,  6620,     0,   693,
 12230, 12358,     0,     0,     0,   122,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,  1520,  1788,
  2348,  2841,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  3192,  3334,  3827,  4178,     0,  4320,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0, 12054,     0,     0,
     0,   458,     0,   826,   274,     0,     0,  1511,     0,     0,
  7086,     0,     0,  6754,     0,     0,     0,   750,     0,   769,
     0,     0,     0,     0,     0,   568,     0,     0,     0,  1009,
   879,     0,     0,     0,     0, 11926,     0,     0,  1901,  1067,
  1067,     0,     0,     0,     0,   701,     0,     0,    35,     0,
     0,   701,     0,     0,     0,     0,     0,     0,    64,     0,
     0,  8011,  7794,  7910, 14153,   122,     0,   121,   701,   133,
     0,     0,   707,   707,     0,     0,   683,     0,     0,     0,
     0,   200,   163,   307,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   175,     0,     0,     0,  1672,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,    72,     0,     0,     0,
     0,     0,   122,   209,   210,     0,     0,     0,    50,     0,
     0,     0,   154,     0, 12746,     0,     0,     0,     0,     0,
     0,     0,    72,   604,     0,   189,     0,     0,     0,     0,
   647,   322,     0,   419,     0,     0,  1387,  7200,     0,   671,
 12874,     0,     0,    72,     0,     0,     0,   573,     0,     0,
     0,     0,     0,     0,   991,     0,     0,    72,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   701,     0,
     0,     0,     0,     0,    42,    42,   701,   701,     0,     0,
     0,     0,     0,     0,     0, 11034,    64,     0,     0,     0,
     0,     0,     0,   701,     0,   600,   701,     0,   711,     0,
     0,  -214,     0,     0,     0,   962,     0,   217,     0,     0,
    72,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   102,     0,
     0,     0,     0,     0,    34,     0,     0,     0,     0,     0,
    67,     0,   125,     0,  -177,     0,   125,   125,     0,     0,
     0, 13004, 13143,     0,     0,     0,     0,     0,     0,     0,
     0,     0,  8126, 10097, 10215, 10312, 10398,  9640,  9760, 10488,
 10761, 10585, 10671, 10858, 10944,  9062,  9185,  8259,  9303,  8374,
  8475,  8836,  8940,  9857,  9975,  9411,  9520,  1000, 13004,  5428,
     0,  5551,  4944,     0,     0,  5921,  3958,  6044, 15918,     0,
  4081,     0,   715,     0,     0,  2107,  2107,     0, 11074,     0,
     0,     0,  1548,     0,     0,     0,     0,   701,     0,   228,
     0,     0,     0, 10982,     0, 12014,     0,     0,     0,     0,
   604,  6952, 12488, 12616,     0,     0,   715,     0,   701,   142,
     0,   604,     0,     0,     0,   303,   267,     0,   126,   796,
     0,   739,   796,     0,  2972,  3095,  3465,  4451,   715, 12110,
   796,     0,     0,     0,     0,     0,     0,     0,   634,  1363,
  1453,   614,   715,     0,     0,     0, 14256,  1067,     0,     0,
     0,     0,    80,    93,     0,     0,     0,   701,     0,     0,
  8591,  8723, 11159,   107,     0,   707,     0,   483,   794,  1233,
   797,   715,   239,    64,     0,     0,     0,     0,     0,     0,
     0,   144,     0,   145,     0,   701,   723,     0,     0,     0,
     0,     0,     0,     0,   249,    64,     0,     0,     0,     0,
     0,     0,   704,     0,   249,     0,    64, 13143,     0,   249,
     0,     0,     0,  5185,     0,     0,     0,     0,     0,  6163,
 13794,     0,     0,     0,     0,     0,   684,     0,     0,     0,
     0,   288,     0,     0,     0,     0,     0,     0,     0,     0,
   701,     0,     0,     0,     0,     0,     0,     0,     0,   249,
     0,     0,     0,     0,     0,     0,     0,     0,     0,  6405,
     0,     0,     0,  1103,   249,   249,   838,     0,     0,     0,
    42,     0,     0,     0,     0,  1117,     0,     0,     0,     0,
     0,     0,     0,   701,     0,   149,     0,     0,     0,   125,
     0,     0,   366,     0,     0,     0,     0,   125,   125,     0,
   125,     0,   125,   148,   -20,   704,   -20,   -20,     0,     0,
     0,     0,     0,    64,     0,     0,     0, 11255,     0, 11340,
     0,     0, 11401,     0, 11498,     0,     0,     0, 11584,     0,
 12070,   302, 11693,   604,     0,     0,     0,     0,   189,     0,
     0,   722,     0,  1118,   604,     0,     0,     0,     0,     0,
     0,   796,     0,     0,     0,   128,     0,     0,   153,     0,
   156,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   -16,
     0,     0,     0,     0,     0,     0,     0, 11754,     0,     0,
 11840, 14366,     0,   604,  1131,     0,     0,    72,   458,   671,
     0,     0,     0,     0,     0,   249,     0,   157,     0,   159,
     0,   125,   125,   125,   125,     0,   162,   -20,     0,   -20,
   -20,   -20,   -20,     0,     0,     0,     0,   914,  1065,  1214,
   699,   715,     0,   796,     0,   160,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,  1236,     0,     0,   161,   125,   758,   900,
   -20,   -20,   -20,   -20,     0,     0,     0,     0,     0,     0,
   -20,     0,
    }, yyGindex = {
//yyGindex 158
     0,     0,    11,   776,  -298,     0,   -27,     9,    -6,  -441,
     0,     0,     0,   825,     0,     0,     0,   996,     0,     0,
     0,   702,  -207,     0,     0,     0,     0,     0,     0,    15,
  1061,  -348,    25,   558,  -374,     0,    76,  1421,  1880,    22,
   -10,    23,   320,  -395,     0,   137,     0,   660,     0,    33,
     0,     3,  1072,   184,     0,     0,  -624,     0,     0,   872,
  -248,   233,     0,     0,     0,  -433,  -198,   -76,    73,  1008,
  -390,     0,     0,  1184,     1,   299,     0,     0,  1037,   382,
  -192,     0,    16,  -377,     0,  -416,   202,  -216,  -380,     0,
  1192,  -314,  1016,     0,  -468,  1077,   241,   201,    99,     0,
   -13,  -600,     0,  -552,     0,     0,  -176,  -719,     0,  -341,
  -665,   416,   237,     6,  1306,  -230,  -417,     0,  -603,   648,
     0,    13,  -394,     0,    -5,     8,     0,     0,   -24,     0,
     0,  -236,     0,     0,  -221,     0,     0,     0,     0,     0,
     0,     0,    29,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,
    };
    protected static final short[] yyTable = Ripper19YyTables.yyTable();
    protected static final short[] yyCheck = Ripper19YyTables.yyCheck();

  /** maps symbol value to printable name.
      @see #yyExpecting
    */
  protected static final String[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,"' '",null,null,null,null,null,
    null,null,null,null,null,null,"','",null,null,null,null,null,null,
    null,null,null,null,null,null,null,"':'","';'",null,"'='",null,"'?'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "'['",null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "kCLASS","kMODULE","kDEF","kUNDEF","kBEGIN","kRESCUE","kENSURE",
    "kEND","kIF","kUNLESS","kTHEN","kELSIF","kELSE","kCASE","kWHEN",
    "kWHILE","kUNTIL","kFOR","kBREAK","kNEXT","kREDO","kRETRY","kIN",
    "kDO","kDO_COND","kDO_BLOCK","kRETURN","kYIELD","kSUPER","kSELF",
    "kNIL","kTRUE","kFALSE","kAND","kOR","kNOT","kIF_MOD","kUNLESS_MOD",
    "kWHILE_MOD","kUNTIL_MOD","kRESCUE_MOD","kALIAS","kDEFINED","klBEGIN",
    "klEND","k__LINE__","k__FILE__","k__ENCODING__","kDO_LAMBDA",
    "tIDENTIFIER","tFID","tGVAR","tIVAR","tCONSTANT","tCVAR","tLABEL",
    "tCHAR","tUPLUS","tUMINUS","tUMINUS_NUM","tPOW","tCMP","tEQ","tEQQ",
    "tNEQ","tGEQ","tLEQ","tANDOP","tOROP","tMATCH","tNMATCH","tDOT",
    "tDOT2","tDOT3","tAREF","tASET","tLSHFT","tRSHFT","tCOLON2","tCOLON3",
    "tOP_ASGN","tASSOC","tLPAREN","tLPAREN2","tRPAREN","tLPAREN_ARG",
    "tLBRACK","tRBRACK","tLBRACE","tLBRACE_ARG","tSTAR","tSTAR2","tAMPER",
    "tAMPER2","tTILDE","tPERCENT","tDIVIDE","tPLUS","tMINUS","tLT","tGT",
    "tPIPE","tBANG","tCARET","tLCURLY","tRCURLY","tBACK_REF2","tSYMBEG",
    "tSTRING_BEG","tXSTRING_BEG","tREGEXP_BEG","tWORDS_BEG","tQWORDS_BEG",
    "tSTRING_DBEG","tSTRING_DVAR","tSTRING_END","tLAMBDA","tLAMBEG",
    "tNTH_REF","tBACK_REF","tSTRING_CONTENT","tINTEGER","tFLOAT",
    "tREGEXP_END","tLOWEST","variable",
    };

  /** printable rules for debugging.
    */
  protected static final String [] yyRule = {
    "$accept : program",
    "$$1 :",
    "program : $$1 top_compstmt",
    "top_compstmt : top_stmts opt_terms",
    "top_stmts : none",
    "top_stmts : top_stmt",
    "top_stmts : top_stmts terms top_stmt",
    "top_stmts : error top_stmt",
    "top_stmt : stmt",
    "$$2 :",
    "top_stmt : klBEGIN $$2 tLCURLY top_compstmt tRCURLY",
    "bodystmt : compstmt opt_rescue opt_else opt_ensure",
    "compstmt : stmts opt_terms",
    "stmts : none",
    "stmts : stmt",
    "stmts : stmts terms stmt",
    "stmts : error stmt",
    "$$3 :",
    "stmt : kALIAS fitem $$3 fitem",
    "stmt : kALIAS tGVAR tGVAR",
    "stmt : kALIAS tGVAR tBACK_REF",
    "stmt : kALIAS tGVAR tNTH_REF",
    "stmt : kUNDEF undef_list",
    "stmt : stmt kIF_MOD expr_value",
    "stmt : stmt kUNLESS_MOD expr_value",
    "stmt : stmt kWHILE_MOD expr_value",
    "stmt : stmt kUNTIL_MOD expr_value",
    "stmt : stmt kRESCUE_MOD stmt",
    "stmt : klEND tLCURLY compstmt tRCURLY",
    "stmt : command_asgn",
    "stmt : mlhs '=' command_call",
    "stmt : var_lhs tOP_ASGN command_call",
    "stmt : primary_value '[' opt_call_args rbracket tOP_ASGN command_call",
    "stmt : primary_value tDOT tIDENTIFIER tOP_ASGN command_call",
    "stmt : primary_value tDOT tCONSTANT tOP_ASGN command_call",
    "stmt : primary_value tCOLON2 tCONSTANT tOP_ASGN command_call",
    "stmt : primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_call",
    "stmt : backref tOP_ASGN command_call",
    "stmt : lhs '=' mrhs",
    "stmt : mlhs '=' arg_value",
    "stmt : mlhs '=' mrhs",
    "stmt : expr",
    "command_asgn : lhs '=' command_call",
    "command_asgn : lhs '=' command_asgn",
    "expr : command_call",
    "expr : expr kAND expr",
    "expr : expr kOR expr",
    "expr : kNOT opt_nl expr",
    "expr : tBANG command_call",
    "expr : arg",
    "expr_value : expr",
    "command_call : command",
    "command_call : block_command",
    "block_command : block_call",
    "block_command : block_call tDOT operation2 command_args",
    "block_command : block_call tCOLON2 operation2 command_args",
    "$$4 :",
    "cmd_brace_block : tLBRACE_ARG $$4 opt_block_param compstmt tRCURLY",
    "command : operation command_args",
    "command : operation command_args cmd_brace_block",
    "command : primary_value tDOT operation2 command_args",
    "command : primary_value tDOT operation2 command_args cmd_brace_block",
    "command : primary_value tCOLON2 operation2 command_args",
    "command : primary_value tCOLON2 operation2 command_args cmd_brace_block",
    "command : kSUPER command_args",
    "command : kYIELD command_args",
    "command : kRETURN call_args",
    "command : kBREAK call_args",
    "command : kNEXT call_args",
    "mlhs : mlhs_basic",
    "mlhs : tLPAREN mlhs_inner rparen",
    "mlhs_inner : mlhs_basic",
    "mlhs_inner : tLPAREN mlhs_inner rparen",
    "mlhs_basic : mlhs_head",
    "mlhs_basic : mlhs_head mlhs_item",
    "mlhs_basic : mlhs_head tSTAR mlhs_node",
    "mlhs_basic : mlhs_head tSTAR mlhs_node ',' mlhs_post",
    "mlhs_basic : mlhs_head tSTAR",
    "mlhs_basic : mlhs_head tSTAR ',' mlhs_post",
    "mlhs_basic : tSTAR mlhs_node",
    "mlhs_basic : tSTAR mlhs_node ',' mlhs_post",
    "mlhs_basic : tSTAR",
    "mlhs_basic : tSTAR ',' mlhs_post",
    "mlhs_item : mlhs_node",
    "mlhs_item : tLPAREN mlhs_inner rparen",
    "mlhs_head : mlhs_item ','",
    "mlhs_head : mlhs_head mlhs_item ','",
    "mlhs_post : mlhs_item",
    "mlhs_post : mlhs_post ',' mlhs_item",
    "mlhs_node : variable",
    "mlhs_node : keyword_variable",
    "mlhs_node : primary_value '[' opt_call_args rbracket",
    "mlhs_node : primary_value tDOT tIDENTIFIER",
    "mlhs_node : primary_value tCOLON2 tIDENTIFIER",
    "mlhs_node : primary_value tDOT tCONSTANT",
    "mlhs_node : primary_value tCOLON2 tCONSTANT",
    "mlhs_node : tCOLON3 tCONSTANT",
    "mlhs_node : backref",
    "lhs : user_variable",
    "lhs : keyword_variable",
    "lhs : primary_value '[' opt_call_args rbracket",
    "lhs : primary_value tDOT tIDENTIFIER",
    "lhs : primary_value tCOLON2 tIDENTIFIER",
    "lhs : primary_value tDOT tCONSTANT",
    "lhs : primary_value tCOLON2 tCONSTANT",
    "lhs : tCOLON3 tCONSTANT",
    "lhs : backref",
    "cname : tIDENTIFIER",
    "cname : tCONSTANT",
    "cpath : tCOLON3 cname",
    "cpath : cname",
    "cpath : primary_value tCOLON2 cname",
    "fname : tIDENTIFIER",
    "fname : tCONSTANT",
    "fname : tFID",
    "fname : op",
    "fname : reswords",
    "fsym : fname",
    "fsym : symbol",
    "fitem : fsym",
    "fitem : dsym",
    "undef_list : fitem",
    "$$5 :",
    "undef_list : undef_list ',' $$5 fitem",
    "op : tPIPE",
    "op : tCARET",
    "op : tAMPER2",
    "op : tCMP",
    "op : tEQ",
    "op : tEQQ",
    "op : tMATCH",
    "op : tNMATCH",
    "op : tGT",
    "op : tGEQ",
    "op : tLT",
    "op : tLEQ",
    "op : tNEQ",
    "op : tLSHFT",
    "op : tRSHFT",
    "op : tPLUS",
    "op : tMINUS",
    "op : tSTAR2",
    "op : tSTAR",
    "op : tDIVIDE",
    "op : tPERCENT",
    "op : tPOW",
    "op : tBANG",
    "op : tTILDE",
    "op : tUPLUS",
    "op : tUMINUS",
    "op : tAREF",
    "op : tASET",
    "op : tBACK_REF2",
    "reswords : k__LINE__",
    "reswords : k__FILE__",
    "reswords : k__ENCODING__",
    "reswords : klBEGIN",
    "reswords : klEND",
    "reswords : kALIAS",
    "reswords : kAND",
    "reswords : kBEGIN",
    "reswords : kBREAK",
    "reswords : kCASE",
    "reswords : kCLASS",
    "reswords : kDEF",
    "reswords : kDEFINED",
    "reswords : kDO",
    "reswords : kELSE",
    "reswords : kELSIF",
    "reswords : kEND",
    "reswords : kENSURE",
    "reswords : kFALSE",
    "reswords : kFOR",
    "reswords : kIN",
    "reswords : kMODULE",
    "reswords : kNEXT",
    "reswords : kNIL",
    "reswords : kNOT",
    "reswords : kOR",
    "reswords : kREDO",
    "reswords : kRESCUE",
    "reswords : kRETRY",
    "reswords : kRETURN",
    "reswords : kSELF",
    "reswords : kSUPER",
    "reswords : kTHEN",
    "reswords : kTRUE",
    "reswords : kUNDEF",
    "reswords : kWHEN",
    "reswords : kYIELD",
    "reswords : kIF_MOD",
    "reswords : kUNLESS_MOD",
    "reswords : kWHILE_MOD",
    "reswords : kUNTIL_MOD",
    "reswords : kRESCUE_MOD",
    "arg : lhs '=' arg",
    "arg : lhs '=' arg kRESCUE_MOD arg",
    "arg : var_lhs tOP_ASGN arg",
    "arg : var_lhs tOP_ASGN arg kRESCUE_MOD arg",
    "arg : primary_value '[' opt_call_args rbracket tOP_ASGN arg",
    "arg : primary_value tDOT tIDENTIFIER tOP_ASGN arg",
    "arg : primary_value tDOT tCONSTANT tOP_ASGN arg",
    "arg : primary_value tCOLON2 tIDENTIFIER tOP_ASGN arg",
    "arg : primary_value tCOLON2 tCONSTANT tOP_ASGN arg",
    "arg : tCOLON3 tCONSTANT tOP_ASGN arg",
    "arg : backref tOP_ASGN arg",
    "arg : arg tDOT2 arg",
    "arg : arg tDOT3 arg",
    "arg : arg tPLUS arg",
    "arg : arg tMINUS arg",
    "arg : arg tSTAR2 arg",
    "arg : arg tDIVIDE arg",
    "arg : arg tPERCENT arg",
    "arg : arg tPOW arg",
    "arg : tUMINUS_NUM tINTEGER tPOW arg",
    "arg : tUMINUS_NUM tFLOAT tPOW arg",
    "arg : tUPLUS arg",
    "arg : tUMINUS arg",
    "arg : arg tPIPE arg",
    "arg : arg tCARET arg",
    "arg : arg tAMPER2 arg",
    "arg : arg tCMP arg",
    "arg : arg tGT arg",
    "arg : arg tGEQ arg",
    "arg : arg tLT arg",
    "arg : arg tLEQ arg",
    "arg : arg tEQ arg",
    "arg : arg tEQQ arg",
    "arg : arg tNEQ arg",
    "arg : arg tMATCH arg",
    "arg : arg tNMATCH arg",
    "arg : tBANG arg",
    "arg : tTILDE arg",
    "arg : arg tLSHFT arg",
    "arg : arg tRSHFT arg",
    "arg : arg tANDOP arg",
    "arg : arg tOROP arg",
    "arg : kDEFINED opt_nl arg",
    "arg : arg '?' arg opt_nl ':' arg",
    "arg : primary",
    "arg_value : arg",
    "aref_args : none",
    "aref_args : args trailer",
    "aref_args : args ',' assocs trailer",
    "aref_args : assocs trailer",
    "paren_args : tLPAREN2 opt_call_args rparen",
    "opt_paren_args : none",
    "opt_paren_args : paren_args",
    "opt_call_args : none",
    "opt_call_args : call_args",
    "opt_call_args : args ','",
    "opt_call_args : args ',' assocs ','",
    "opt_call_args : assocs ','",
    "call_args : command",
    "call_args : args opt_block_arg",
    "call_args : assocs opt_block_arg",
    "call_args : args ',' assocs opt_block_arg",
    "call_args : block_arg",
    "$$6 :",
    "command_args : $$6 call_args",
    "block_arg : tAMPER arg_value",
    "opt_block_arg : ',' block_arg",
    "opt_block_arg : none_block_pass",
    "args : arg_value",
    "args : tSTAR arg_value",
    "args : args ',' arg_value",
    "args : args ',' tSTAR arg_value",
    "mrhs : args ',' arg_value",
    "mrhs : args ',' tSTAR arg_value",
    "mrhs : tSTAR arg_value",
    "primary : literal",
    "primary : strings",
    "primary : xstring",
    "primary : regexp",
    "primary : words",
    "primary : qwords",
    "primary : var_ref",
    "primary : backref",
    "primary : tFID",
    "primary : kBEGIN bodystmt kEND",
    "$$7 :",
    "primary : tLPAREN_ARG expr $$7 rparen",
    "primary : tLPAREN compstmt tRPAREN",
    "primary : primary_value tCOLON2 tCONSTANT",
    "primary : tCOLON3 tCONSTANT",
    "primary : tLBRACK aref_args tRBRACK",
    "primary : tLBRACE assoc_list tRCURLY",
    "primary : kRETURN",
    "primary : kYIELD tLPAREN2 call_args rparen",
    "primary : kYIELD tLPAREN2 rparen",
    "primary : kYIELD",
    "primary : kDEFINED opt_nl tLPAREN2 expr rparen",
    "primary : kNOT tLPAREN2 expr rparen",
    "primary : kNOT tLPAREN2 rparen",
    "primary : operation brace_block",
    "primary : method_call",
    "primary : method_call brace_block",
    "primary : tLAMBDA lambda",
    "primary : kIF expr_value then compstmt if_tail kEND",
    "primary : kUNLESS expr_value then compstmt opt_else kEND",
    "$$8 :",
    "$$9 :",
    "primary : kWHILE $$8 expr_value do $$9 compstmt kEND",
    "$$10 :",
    "$$11 :",
    "primary : kUNTIL $$10 expr_value do $$11 compstmt kEND",
    "primary : kCASE expr_value opt_terms case_body kEND",
    "primary : kCASE opt_terms case_body kEND",
    "$$12 :",
    "$$13 :",
    "primary : kFOR for_var kIN $$12 expr_value do $$13 compstmt kEND",
    "$$14 :",
    "primary : kCLASS cpath superclass $$14 bodystmt kEND",
    "$$15 :",
    "$$16 :",
    "primary : kCLASS tLSHFT expr $$15 term $$16 bodystmt kEND",
    "$$17 :",
    "primary : kMODULE cpath $$17 bodystmt kEND",
    "$$18 :",
    "primary : kDEF fname $$18 f_arglist bodystmt kEND",
    "$$19 :",
    "$$20 :",
    "primary : kDEF singleton dot_or_colon $$19 fname $$20 f_arglist bodystmt kEND",
    "primary : kBREAK",
    "primary : kNEXT",
    "primary : kREDO",
    "primary : kRETRY",
    "primary_value : primary",
    "then : term",
    "then : kTHEN",
    "then : term kTHEN",
    "do : term",
    "do : kDO_COND",
    "if_tail : opt_else",
    "if_tail : kELSIF expr_value then compstmt if_tail",
    "opt_else : none",
    "opt_else : kELSE compstmt",
    "for_var : lhs",
    "for_var : mlhs",
    "f_marg : f_norm_arg",
    "f_marg : tLPAREN f_margs rparen",
    "f_marg_list : f_marg",
    "f_marg_list : f_marg_list ',' f_marg",
    "f_margs : f_marg_list",
    "f_margs : f_marg_list ',' tSTAR f_norm_arg",
    "f_margs : f_marg_list ',' tSTAR f_norm_arg ',' f_marg_list",
    "f_margs : f_marg_list ',' tSTAR",
    "f_margs : f_marg_list ',' tSTAR ',' f_marg_list",
    "f_margs : tSTAR f_norm_arg",
    "f_margs : tSTAR f_norm_arg ',' f_marg_list",
    "f_margs : tSTAR",
    "f_margs : tSTAR ',' f_marg_list",
    "block_param : f_arg ',' f_block_optarg ',' f_rest_arg opt_f_block_arg",
    "block_param : f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "block_param : f_arg ',' f_block_optarg opt_f_block_arg",
    "block_param : f_arg ',' f_block_optarg ',' f_arg opt_f_block_arg",
    "block_param : f_arg ',' f_rest_arg opt_f_block_arg",
    "block_param : f_arg ','",
    "block_param : f_arg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "block_param : f_arg opt_f_block_arg",
    "block_param : f_block_optarg ',' f_rest_arg opt_f_block_arg",
    "block_param : f_block_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "block_param : f_block_optarg opt_f_block_arg",
    "block_param : f_block_optarg ',' f_arg opt_f_block_arg",
    "block_param : f_rest_arg opt_f_block_arg",
    "block_param : f_rest_arg ',' f_arg opt_f_block_arg",
    "block_param : f_block_arg",
    "opt_block_param : none",
    "opt_block_param : block_param_def",
    "block_param_def : tPIPE opt_bv_decl tPIPE",
    "block_param_def : tOROP",
    "block_param_def : tPIPE block_param opt_bv_decl tPIPE",
    "opt_bv_decl : none",
    "opt_bv_decl : ';' bv_decls",
    "bv_decls : bvar",
    "bv_decls : bv_decls ',' bvar",
    "bvar : tIDENTIFIER",
    "bvar : f_bad_arg",
    "$$21 :",
    "lambda : $$21 f_larglist lambda_body",
    "f_larglist : tLPAREN2 f_args opt_bv_decl tRPAREN",
    "f_larglist : f_args opt_bv_decl",
    "lambda_body : tLAMBEG compstmt tRCURLY",
    "lambda_body : kDO_LAMBDA compstmt kEND",
    "$$22 :",
    "do_block : kDO_BLOCK $$22 opt_block_param compstmt kEND",
    "block_call : command do_block",
    "block_call : block_call tDOT operation2 opt_paren_args",
    "block_call : block_call tCOLON2 operation2 opt_paren_args",
    "method_call : operation paren_args",
    "method_call : primary_value tDOT operation2 opt_paren_args",
    "method_call : primary_value tCOLON2 operation2 paren_args",
    "method_call : primary_value tCOLON2 operation3",
    "method_call : primary_value tDOT paren_args",
    "method_call : primary_value tCOLON2 paren_args",
    "method_call : kSUPER paren_args",
    "method_call : kSUPER",
    "method_call : primary_value '[' opt_call_args rbracket",
    "$$23 :",
    "brace_block : tLCURLY $$23 opt_block_param compstmt tRCURLY",
    "$$24 :",
    "brace_block : kDO $$24 opt_block_param compstmt kEND",
    "case_body : kWHEN args then compstmt cases",
    "cases : opt_else",
    "cases : case_body",
    "opt_rescue : kRESCUE exc_list exc_var then compstmt opt_rescue",
    "opt_rescue : none",
    "exc_list : arg_value",
    "exc_list : mrhs",
    "exc_list : none",
    "exc_var : tASSOC lhs",
    "exc_var : none",
    "opt_ensure : kENSURE compstmt",
    "opt_ensure : none",
    "literal : numeric",
    "literal : symbol",
    "literal : dsym",
    "strings : string",
    "string : tCHAR",
    "string : string1",
    "string : string string1",
    "string1 : tSTRING_BEG string_contents tSTRING_END",
    "xstring : tXSTRING_BEG xstring_contents tSTRING_END",
    "regexp : tREGEXP_BEG xstring_contents tREGEXP_END",
    "words : tWORDS_BEG ' ' tSTRING_END",
    "words : tWORDS_BEG word_list tSTRING_END",
    "word_list :",
    "word_list : word_list word ' '",
    "word : string_content",
    "word : word string_content",
    "qwords : tQWORDS_BEG ' ' tSTRING_END",
    "qwords : tQWORDS_BEG qword_list tSTRING_END",
    "qword_list :",
    "qword_list : qword_list tSTRING_CONTENT ' '",
    "string_contents :",
    "string_contents : string_contents string_content",
    "xstring_contents :",
    "xstring_contents : xstring_contents string_content",
    "regexp_contents :",
    "regexp_contents : regexp_contents string_content",
    "string_content : tSTRING_CONTENT",
    "$$25 :",
    "string_content : tSTRING_DVAR $$25 string_dvar",
    "$$26 :",
    "string_content : tSTRING_DBEG $$26 compstmt tRCURLY",
    "string_dvar : tGVAR",
    "string_dvar : tIVAR",
    "string_dvar : tCVAR",
    "string_dvar : backref",
    "symbol : tSYMBEG sym",
    "sym : fname",
    "sym : tIVAR",
    "sym : tGVAR",
    "sym : tCVAR",
    "dsym : tSYMBEG xstring_contents tSTRING_END",
    "numeric : tINTEGER",
    "numeric : tFLOAT",
    "numeric : tUMINUS_NUM tINTEGER",
    "numeric : tUMINUS_NUM tFLOAT",
    "user_variable : tIDENTIFIER",
    "user_variable : tIVAR",
    "user_variable : tGVAR",
    "user_variable : tCONSTANT",
    "user_variable : tCVAR",
    "keyword_variable : kNIL",
    "keyword_variable : kSELF",
    "keyword_variable : kTRUE",
    "keyword_variable : kFALSE",
    "keyword_variable : k__FILE__",
    "keyword_variable : k__LINE__",
    "keyword_variable : k__ENCODING__",
    "var_ref : user_variable",
    "var_ref : keyword_variable",
    "var_lhs : user_variable",
    "var_lhs : keyword_variable",
    "backref : tNTH_REF",
    "backref : tBACK_REF",
    "superclass : term",
    "$$27 :",
    "superclass : tLT $$27 expr_value term",
    "superclass : error term",
    "f_arglist : tLPAREN2 f_args rparen",
    "f_arglist : f_args term",
    "f_args : f_arg ',' f_optarg ',' f_rest_arg opt_f_block_arg",
    "f_args : f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "f_args : f_arg ',' f_optarg opt_f_block_arg",
    "f_args : f_arg ',' f_optarg ',' f_arg opt_f_block_arg",
    "f_args : f_arg ',' f_rest_arg opt_f_block_arg",
    "f_args : f_arg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "f_args : f_arg opt_f_block_arg",
    "f_args : f_optarg ',' f_rest_arg opt_f_block_arg",
    "f_args : f_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg",
    "f_args : f_optarg opt_f_block_arg",
    "f_args : f_optarg ',' f_arg opt_f_block_arg",
    "f_args : f_rest_arg opt_f_block_arg",
    "f_args : f_rest_arg ',' f_arg opt_f_block_arg",
    "f_args : f_block_arg",
    "f_args :",
    "f_bad_arg : tCONSTANT",
    "f_bad_arg : tIVAR",
    "f_bad_arg : tGVAR",
    "f_bad_arg : tCVAR",
    "f_norm_arg : f_bad_arg",
    "f_norm_arg : tIDENTIFIER",
    "f_arg_item : f_norm_arg",
    "f_arg_item : tLPAREN f_margs rparen",
    "f_arg : f_arg_item",
    "f_arg : f_arg ',' f_arg_item",
    "f_opt : tIDENTIFIER '=' arg_value",
    "f_block_opt : tIDENTIFIER '=' primary_value",
    "f_block_optarg : f_block_opt",
    "f_block_optarg : f_block_optarg ',' f_block_opt",
    "f_optarg : f_opt",
    "f_optarg : f_optarg ',' f_opt",
    "restarg_mark : tSTAR2",
    "restarg_mark : tSTAR",
    "f_rest_arg : restarg_mark tIDENTIFIER",
    "f_rest_arg : restarg_mark",
    "blkarg_mark : tAMPER2",
    "blkarg_mark : tAMPER",
    "f_block_arg : blkarg_mark tIDENTIFIER",
    "opt_f_block_arg : ',' f_block_arg",
    "opt_f_block_arg :",
    "singleton : var_ref",
    "$$28 :",
    "singleton : tLPAREN2 $$28 expr rparen",
    "assoc_list : none",
    "assoc_list : assocs trailer",
    "assocs : assoc",
    "assocs : assocs ',' assoc",
    "assoc : arg_value tASSOC arg_value",
    "assoc : tLABEL arg_value",
    "operation : tIDENTIFIER",
    "operation : tCONSTANT",
    "operation : tFID",
    "operation2 : tIDENTIFIER",
    "operation2 : tCONSTANT",
    "operation2 : tFID",
    "operation2 : op",
    "operation3 : tIDENTIFIER",
    "operation3 : tFID",
    "operation3 : op",
    "dot_or_colon : tDOT",
    "dot_or_colon : tCOLON2",
    "opt_terms :",
    "opt_terms : terms",
    "opt_nl :",
    "opt_nl : '\\n'",
    "rparen : opt_nl tRPAREN",
    "rbracket : opt_nl tRBRACK",
    "trailer :",
    "trailer : '\\n'",
    "trailer : ','",
    "term : ';'",
    "term : '\\n'",
    "terms : term",
    "terms : terms ';'",
    "none :",
    "none_block_pass :",
    };

  /** debugging support, requires the package <tt>jay.yydebug</tt>.
      Set to <tt>null</tt> to suppress debugging messages.
    */
  protected jay.yydebug.yyDebug yydebug;

  /** index-checked interface to {@link #yyNames}.
      @param token single character or <tt>%token</tt> value.
      @return token name or <tt>[illegal]</tt> or <tt>[unknown]</tt>.
    */
  public static final String yyName (int token) {
    if (token < 0 || token > yyNames.length) return "[illegal]";
    String name;
    if ((name = yyNames[token]) != null) return name;
    return "[unknown]";
  }


  /** computes list of expected tokens on error by tracing the tables.
      @param state for which to compute the list.
      @return list of token names.
    */
  protected String[] yyExpecting (int state) {
    int token, n, len = 0;
    boolean[] ok = new boolean[yyNames.length];

    if ((n = yySindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }
    if ((n = yyRindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }

    String result[] = new String[len];
    for (n = token = 0; n < len;  ++ token)
      if (ok[token]) result[n++] = yyNames[token];
    return result;
  }

  /** the generated parser, with debugging messages.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @param yydebug debug message writer implementing <tt>yyDebug</tt>, or <tt>null</tt>.
      @return result of the last reduction, if any.
    */
  public Object yyparse (RubyYaccLexer yyLex, Object ayydebug)
				throws java.io.IOException {
    this.yydebug = (jay.yydebug.yyDebug)ayydebug;
    return yyparse(yyLex);
  }

  /** initial size and increment of the state/value stack [default 256].
      This is not final so that it can be overwritten outside of invocations
      of {@link #yyparse}.
    */
  protected int yyMax;

  /** executed at the beginning of a reduce action.
      Used as <tt>$$ = yyDefault($1)</tt>, prior to the user-specified action, if any.
      Can be overwritten to provide deep copy, etc.
      @param first value for <tt>$1</tt>, or <tt>null</tt>.
      @return first.
    */
  protected Object yyDefault (Object first) {
    return first;
  }

  /** the generated parser.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
    */
  public Object yyparse (RubyYaccLexer yyLex) throws java.io.IOException {
    if (yyMax <= 0) yyMax = 256;			// initial size
    int yyState = 0, yyStates[] = new int[yyMax];	// state stack
    Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
    int yyToken = -1;					// current input
    int yyErrorFlag = 0;				// #tokens to shift

    yyLoop: for (int yyTop = 0;; ++ yyTop) {
      if (yyTop >= yyStates.length) {			// dynamically increase
        int[] i = new int[yyStates.length+yyMax];
        System.arraycopy(yyStates, 0, i, 0, yyStates.length);
        yyStates = i;
        Object[] o = new Object[yyVals.length+yyMax];
        System.arraycopy(yyVals, 0, o, 0, yyVals.length);
        yyVals = o;
      }
      yyStates[yyTop] = yyState;
      yyVals[yyTop] = yyVal;
      if (yydebug != null) yydebug.push(yyState, yyVal);

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
            yyToken = yyLex.nextToken();
            if (yydebug != null)
              yydebug.lex(yyState, yyToken, yyName(yyToken), yyLex.value());
          }
          if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
            if (yydebug != null)
              yydebug.shift(yyState, yyTable[yyN], yyErrorFlag-1);
            yyState = yyTable[yyN];		// shift to yyN
            yyVal = yyLex.value();
            yyToken = -1;
            if (yyErrorFlag > 0) -- yyErrorFlag;
            continue yyLoop;
          }
          if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken)
            yyN = yyTable[yyN];			// reduce (yyN)
          else
            switch (yyErrorFlag) {
  
            case 0:
              support.yyerror("syntax error", yyExpecting(yyState), yyNames[yyToken]);
              if (yydebug != null) yydebug.error("syntax error");
  
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length
                    && yyCheck[yyN] == yyErrorCode) {
                  if (yydebug != null)
                    yydebug.shift(yyStates[yyTop], yyTable[yyN], 3);
                  yyState = yyTable[yyN];
                  yyVal = yyLex.value();
                  continue yyLoop;
                }
                if (yydebug != null) yydebug.pop(yyStates[yyTop]);
              } while (-- yyTop >= 0);
              if (yydebug != null) yydebug.reject();
              support.yyerror("irrecoverable syntax error");
  
            case 3:
              if (yyToken == 0) {
                if (yydebug != null) yydebug.reject();
                support.yyerror("irrecoverable syntax error at end-of-file");
              }
              if (yydebug != null)
                yydebug.discard(yyState, yyToken, yyName(yyToken),
  							yyLex.value());
              yyToken = -1;
              continue yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
        if (yydebug != null)
          yydebug.reduce(yyState, yyStates[yyV-1], yyN, yyRule[yyN], yyLen[yyN]);
        ParserState state = states[yyN];
        if (state == null) {
            yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        } else {
            yyVal = state.execute(support, lexer, yyVal, yyVals, yyTop);
        }
//        switch (yyN) {
// ACTIONS_END
//        }
        yyTop -= yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
          if (yydebug != null) yydebug.shift(0, yyFinal);
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.nextToken();
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
            if (yydebug != null)
               yydebug.lex(yyState, yyToken,yyName(yyToken), yyLex.value());
          }
          if (yyToken == 0) {
            if (yydebug != null) yydebug.accept(yyVal);
            return yyVal;
          }
          continue yyLoop;
        }
        if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
            && yyN < yyTable.length && yyCheck[yyN] == yyState)
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
        if (yydebug != null) yydebug.shift(yyStates[yyTop], yyState);
        continue yyLoop;
      }
    }
  }

static ParserState[] states = new ParserState[559];
static {
states[435] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_string_add", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[368] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.commandStart = true;
    return yyVal;
  }
};
states[33] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), ".", ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[234] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string(">>"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[100] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_aref_field", ((IRubyObject)yyVals[-3+yyTop]), support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[301] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.getConditionState().end();
    return yyVal;
  }
};
states[402] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_when", ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));

    return yyVal;
  }
};
states[201] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-4+yyTop]) = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-4+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[67] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_abreak", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[268] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mrhs_add_star(args2mrhs(((IRubyObject)yyVals[-3+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[503] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.formal_argument(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[436] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_xstring_new");
    return yyVal;
  }
};
states[369] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.blockvar_new(support.params_new(null, null, null, null, null), support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[34] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), ".", ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[235] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("&&"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[101] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[302] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_while", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[336] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_else", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[202] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-4+yyTop]) = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), support.string("::"), ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-4+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[68] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_next", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[269] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mrhs_add_star(mrhs_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[1] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[504] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_var(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[437] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_xstring_add", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[370] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.blockvar_new(support.params_new(null, null, null, null, null), null);
    return yyVal;
  }
};
states[35] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_field", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[236] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("||"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[102] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-2+yyTop]), support.string("::"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[303] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  lexer.getConditionState().begin();
    return yyVal;
  }
};
states[471] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    if (support.id_is_var(support.get_id(((Token)yyVals[0+yyTop])))) {
                        yyVal = support.dispatch1("on_var_ref", ((Token)yyVals[0+yyTop]));
                    } else {
                        yyVal = support.dispatch1("on_vcall", ((Token)yyVals[0+yyTop]));
                    }
    return yyVal;
  }
};
states[2] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  support.setResult(support.dispatch("on_program", ((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[203] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_field", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[505] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_mlhs_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[438] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_regexp_new");
    return yyVal;
  }
};
states[371] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.blockvar_new(support.escape(((IRubyObject)yyVals[-2+yyTop])), support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[36] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), "::", ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[237] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_defined", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[103] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[304] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  lexer.getConditionState().end();
    return yyVal;
  }
};
states[472] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_var_ref", ((Token)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[405] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch4("on_rescue",
				       support.escape(((IRubyObject)yyVals[-4+yyTop])),
				       support.escape(((IRubyObject)yyVals[-3+yyTop])),
				       support.escape(((IRubyObject)yyVals[-1+yyTop])),
				       support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[338] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
    return yyVal;
  }
};
states[70] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_mlhs_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[3] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[204] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_top_const_field", ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[506] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[439] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_regexp_add", yyVals[-1+yyTop], ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[238] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_ifop", ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[104] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_field", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));

                    if (support.isInDef() || support.isInSingle()) {
                        yyVal = support.dispatch("on_assign_error", yyVal);
                    }
    return yyVal;
  }
};
states[305] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_until", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[37] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", 
                                             support.dispatch("on_var_field", ((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[473] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
                    yyVal = support.dispatch("on_var_field", yyVal);
    return yyVal;
  }
};
states[339] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((IRubyObject)yyVals[0+yyTop]), null);
                    yyVal = support.dispatch("on_mlhs_paren", yyVal);
    return yyVal;
  }
};
states[4] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.dispatch("on_stmts_add", 
                                           support.dispatch("on_stmts_new"), 
                                           support.dispatch("on_void_stmt"));
    return yyVal;
  }
};
states[205] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_var_field", ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[507] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]).append(((IRubyObject)yyVals[0+yyTop]))
    return yyVal;
  }
};
states[373] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[239] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[105] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_top_const_field", ((IRubyObject)yyVals[0+yyTop]));

                    if (support.isInDef() || support.isInSingle()) {
                        yyVal = support.dispatch("on_assign_error", yyVal);
                    }
    return yyVal;
  }
};
states[306] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_case", ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[38] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[474] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
                    yyVal = support.dispatch("on_var_field", yyVal);
    return yyVal;
  }
};
states[407] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[340] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch1("on_mlhs_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[72] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_mlhs_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[5] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.dispatch("on_stmts_add", 
                                           support.dispatch("on_stmts_new"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[206] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_dot2", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[508] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.arg_var(support.formal_argument(((IRubyObject)yyVals[-2+yyTop])));
                    yyVal = support.assignable(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.new_assoc(yyVal, ((IRubyObject)yyVals[0+yyTop]));

    return yyVal;
  }
};
states[441] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = lexer.getStrTerm();
                    lexer.setStrTerm(null);
                    lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[374] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[106] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[307] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_case", null, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[39] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[240] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[542] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((Token)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[408] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[341] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[73] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[6] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.dispatch("on_stmts_add", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[207] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_dot3", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[509] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.arg_var(support.formal_argument(((IRubyObject)yyVals[-2+yyTop])));
                    yyVal = support.assignable(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.new_assoc(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[442] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setStrTerm(((StrTerm)yyVals[-1+yyTop]));
                    yyVal = support.dispatch("on_string_dvar", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[375] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]).append(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[107] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_class_name_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[308] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.getConditionState().begin();
    return yyVal;
  }
};
states[40] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_massign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[543] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((Token)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[342] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[7] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.remove_begin(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[208] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('+'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[74] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[510] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[443] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = lexer.getStrTerm();
                   lexer.getConditionState().stop();
                   lexer.getCmdArgumentState().stop();
                   lexer.setStrTerm(null);
                   lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[376] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.new_bv(support.get_id(((IRubyObject)yyVals[0+yyTop])));
                    yyVal = support.get_value(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[309] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.getConditionState().end();
    return yyVal;
  }
};
states[242] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[477] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = null;
    return yyVal;
  }
};
states[410] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[343] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[209] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('-'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[75] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[511] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]).append(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[444] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   lexer.getConditionState().restart();
                   lexer.getCmdArgumentState().restart();
                   lexer.setStrTerm(((StrTerm)yyVals[-2+yyTop]));
                   yyVal = support.dispatch("on_string_embexpr", ((Token)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[377] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = null;
    return yyVal;
  }
};
states[109] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_top_const_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[310] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_for", ((IRubyObject)yyVals[-7+yyTop]), ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[42] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[243] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_assocs(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[478] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[344] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((IRubyObject)yyVals[0+yyTop]), null);
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-3+yyTop]), yyVal);
    return yyVal;
  }
};
states[9] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  if (support.isInDef() || support.isInSingle()) {
                      support.yyerror("BEGIN in method");
                  }
    return yyVal;
  }
};
states[210] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('*'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[76] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_add_star(((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[512] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[445] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = support.dispatch("on_var_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[378] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.pushBlockScope();
                    yyVal = lexer.getLeftParenBegin();
                    lexer.setLeftParenBegin(lexer.incrementParenNest());
    return yyVal;
  }
};
states[110] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[311] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    if (support.isInDef() || support.isInSingle()) {
                        support.yyerror("class definition in method body");
                    }
                    support.pushLocalScope();
    return yyVal;
  }
};
states[43] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[244] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_assocs(support.arg_new(), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[479] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[412] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_ensure", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[345] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((IRubyObject)yyVals[-2+yyTop]), 0);
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-5+yyTop]), yyVal);
    return yyVal;
  }
};
states[10] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.dispatch("on_BEGIN", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[211] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('/'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[77] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-1+yyTop]), Qnil);
    return yyVal;
  }
};
states[278] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.method_arg(support.dispatch1("on_fcall", ((IRubyObject)yyVals[0+yyTop])), 
                                            support.arg_new());
    return yyVal;
  }
};
states[513] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]).append(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[446] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = support.dispatch("on_var_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[379] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_lambda", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    support.popCurrentScope();
                    lexer.setLeftParenBegin(((Integer)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[312] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_class", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[245] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_arg_paren", support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[111] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_ref", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[480] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = null;
    return yyVal;
  }
};
states[346] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-2+yyTop]), null);
    return yyVal;
  }
};
states[11] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = support.dispatch("on_bodystmt", support.escape(((IRubyObject)yyVals[-3+yyTop])), support.escape(((IRubyObject)yyVals[-2+yyTop])),
                                support.escape(((IRubyObject)yyVals[-1+yyTop])), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[212] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('%'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[78] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-3+yyTop]) = support.mlhs_add_star(((IRubyObject)yyVals[-3+yyTop]), Qnil);
                    yyVal = support.mlhs_add(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[279] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_begin", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[447] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = support.dispatch("on_var_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[380] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]);
                    ((ISourcePositionHolder)yyVal).setPosition(((Token)yyVals[-3+yyTop]).getPosition());
    return yyVal;
  }
};
states[313] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = Boolean.valueOf(support.isInDef());
                    support.setInDef(false);
    return yyVal;
  }
};
states[45] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), "and", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[548] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((Token)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[481] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
                    ((ISourcePositionHolder)yyVal).setPosition(((Token)yyVals[-2+yyTop]).getPosition());
                    lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[347] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[12] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[213] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("**"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[79] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(support.mlhs_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[280] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setState(LexState.EXPR_ENDARG); 
    return yyVal;
  }
};
states[381] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[46] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), "or", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[314] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = Integer.valueOf(support.getInSingle());
                    support.setInSingle(0);
                    support.pushLocalScope();
    return yyVal;
  }
};
states[549] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((Token)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[482] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[415] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_symbol_literal", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[348] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((IRubyObject)yyVals[0+yyTop]), null);
                    yyVal = supportmlhs_add_star(support.mlhs_new(), yyVal);
    return yyVal;
  }
};
states[13] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_stmts_add", 
                                          support.dispatch("on_stmts_new"),
                                          support.dispatch("on_void_stmt"));
    return yyVal;
  }
};
states[214] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("**"), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_unary", support.string("-@"), yyVal);
    return yyVal;
  }
};
states[80] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_add_star(support.mlhs_new(), ((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[281] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.warning(ID.GROUPED_EXPRESSION, ((Token)yyVals[-3+yyTop]).getPosition(), "(...) interpreted as grouped expression");
                    yyVal = support.dispatch("on_paren", ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[516] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.arg_var(support.shadowing_lvar(((IRubyObject)yyVals[0+yyTop])));
                    yyVal = support.dispatch("on_rest_param", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[449] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                     lexer.setState(LexState.EXPR_END);
                     yyVal = support.dispatch("on_symbol", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[382] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[47] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", ripper_intern("not"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[315] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_sclass", ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));

                    support.popCurrentScope();
                    support.setInDef(((Boolean)yyVals[-4+yyTop]).booleanValue());
                    support.setInSingle(((Integer)yyVals[-2+yyTop]).intValue());
    return yyVal;
  }
};
states[483] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[349] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((IRubyObject)yyVals[-2+yyTop]), null);
                    yyVal = support.mlhs_add_star(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[14] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_stmts_add",
                                          ripper.dispatch("on_stmts_new"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[215] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("**"), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_unary", support.string("-@"), yyVal);
    return yyVal;
  }
};
states[81] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(support.mlhs_new(), Qnil);
    return yyVal;
  }
};
states[282] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[517] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_rest_param", null);
    return yyVal;
  }
};
states[383] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[48] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", ripper_id2sym("!"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[115] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   lexer.setState(LexState.EXPR_ENDFN);
                   yyVal = ((Token)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[316] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    if (support.isInDef() || support.isInSingle()) { 
                        support.yyerror("module definition in method body");
                    }
                    support.pushLocalScope();
    return yyVal;
  }
};
states[484] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-7+yyTop]), ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[417] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[350] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(support.mlhs_new(), null);
    return yyVal;
  }
};
states[15] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_stmts_add", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[216] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("+@"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[82] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_add_star(support.mlhs_new(), Qnil), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[283] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_ref", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[384] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.pushBlockScope();
    return yyVal;
  }
};
states[250] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-1+yyTop]);
    return yyVal;
  }
};
states[116] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   lexer.setState(LexState.EXPR_ENDFN);
                   yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[317] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_module", ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[485] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[351] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add_star(suuport.mlhs_new(), Qnil);
    return yyVal;
  }
};
states[16] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = remove_begin(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[217] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("-@"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[284] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_top_const_ref", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[385] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch2("on_do_block", support.escape(((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[50] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[251] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_assocs(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[117] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[318] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.setInDef(true);
                    support.pushLocalScope();
    return yyVal;
  }
};
states[486] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[352] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[17] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setState(LexState.EXPR_FNAME);
    return yyVal;
  }
};
states[218] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('|'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[84] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_mlhs_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[285] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_array", support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[520] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.arg_var(support.shadowing_lvar(((IRubyObject)yyVals[0+yyTop])));
                    yyVal = support.dispatch("on_blockarg", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[386] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.method_add_block(((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[252] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_assocs(support.arg_new(), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[118] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[319] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_def", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));

                    support.popCurrentScope();
                    support.setInDef(false);
    return yyVal;
  }
};
states[487] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[420] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_string_concat", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[353] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-7+yyTop]), ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[219] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('^'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[85] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_new(), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[286] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_hash", escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[18] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_alias", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[521] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[454] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                     lexer.setState(LexState.EXPR_END);
                     yyVal = support.dispatch("on_dyna_symbol", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[387] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-3+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[253] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add(support.arg_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[119] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = support.dispatch("on_symbol_literal", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[320] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setState(LexState.EXPR_FNAME);
    return yyVal;
  }
};
states[488] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[421] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_string_literal", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[354] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[220] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('&'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[86] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[287] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_return0");
    return yyVal;
  }
};
states[19] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_var_alias", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[522] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = null;
    return yyVal;
  }
};
states[455] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[388] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch3(call, ((IRubyObject)yyVals[-3+yyTop]), support.string("::"), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[254] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_optblock(((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[120] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                   yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[321] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.setInSingle(support.getInSingle() + 1);
                    support.pushLocalScope();
                    lexer.setState(LexState.EXPR_ENDFN); /* force for args */
    return yyVal;
  }
};
states[489] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-1+yyTop]), null, null, null,support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[422] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_xstring_literal", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[355] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[221] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("<=>"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[87] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(support.mlhs_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[288] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_yield", 
                                          support.dispatch("on_paren", ((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[20] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_var_alias", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[523] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[456] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[389] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.method_arg(support.dispatch("on_fcall", ((IRubyObject)yyVals[-1+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[54] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = method_arg(support.dispatch("on_call", ((IRubyObject)yyVals[-3+yyTop]), ".", ((IRubyObject)yyVals[-1+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[255] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_assocs(support.arg_new(), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = support.arg_add_optblock(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[121] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[322] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_defs", ((IRubyObject)yyVals[-7+yyTop]), ((IRubyObject)yyVals[-6+yyTop]), ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));

                    support.popCurrentScope();
                    support.setInSingle(support.getInSingle() - 1);
    return yyVal;
  }
};
states[557] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                      yyVal = null;
    return yyVal;
  }
};
states[490] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[423] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_regexp_literal", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[356] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[88] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mlhs_add(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[289] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_yield", 
                                          support.dispatch("on_paren", 
                                                           support.arg_new()));
    return yyVal;
  }
};
states[21] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_alias_error", 
                                          support.dispatch("on_var_alias", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[222] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('>'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[524] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setState(LexState.EXPR_BEG);
    return yyVal;
  }
};
states[457] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("-@"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[390] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-3+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[256] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_optblock(support.arg_add_assocs(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[122] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.setState(LexState.EXPR_FNAME);
    return yyVal;
  }
};
states[323] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_break", support.arg_new());
    return yyVal;
  }
};
states[55] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = method_arg(support.dispatch("on_call", ((IRubyObject)yyVals[-3+yyTop]), "::", ((IRubyObject)yyVals[-1+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[558] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                  yyVal = null;
    return yyVal;
  }
};
states[491] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[424] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_words_new");
                    yyVal = support.dispatch("on_array", yyVal);
    return yyVal;
  }
};
states[357] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-1+yyTop]), null, null, null, null);
                    support.dispatch("on_excessed_comma", yyVal);
    return yyVal;
  }
};
states[89] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(yyVals[0+yyTop], null);
    return yyVal;
  }
};
states[290] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_yield0");
    return yyVal;
  }
};
states[22] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_undef", ((RubyArray)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[223] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.stringl("<="), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[525] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_paren", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[458] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("-@"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[391] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-3+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[324] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_next", support.arg_new());
    return yyVal;
  }
};
states[257] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_block(support.arg_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[123] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((RubyArray)yyVals[-3+yyTop]).append(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[56] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.pushBlockScope();
    return yyVal;
  }
};
states[492] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-1+yyTop]), null, null,support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[425] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_array", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[358] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-5+yyTop]), null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[90] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
    return yyVal;
  }
};
states[291] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_defined", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[23] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_if_mod", ((IRubyObject)yyVals[0+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[224] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('<'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[392] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-2+yyTop]), support.string("::"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[325] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_redo");
    return yyVal;
  }
};
states[258] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = Long.valueOf(lexer.getCmdArgumentState().begin());
    return yyVal;
  }
};
states[57] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_brace_block", support.escape(((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[493] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[426] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_words_new");
    return yyVal;
  }
};
states[359] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(((IRubyObject)yyVals[-1+yyTop]), null, null, null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[91] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_aref_field", ((IRubyObject)yyVals[-3+yyTop]), support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[292] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("not"), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[24] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unless_mod", ((IRubyObject)yyVals[0+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[225] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("<="), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[527] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assoclist_from_args", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[393] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('.'),
                                          support.string("call"));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[326] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_retry");
    return yyVal;
  }
};
states[58] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_command", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[259] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[494] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, ((IRubyObject)yyVals[-1+yyTop]), null,support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[427] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_words_add", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[360] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[293] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.string("not"), null); /*FIXME: null should be nil*/
    return yyVal;
  }
};
states[25] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_while_mod", ((IRubyObject)yyVals[0+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[226] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("=="), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[92] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_field", ((IRubyObject)yyVals[-2+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[0+yyTop]));
                    
    return yyVal;
  }
};
states[528] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.new_array(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[394] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_call", ((IRubyObject)yyVals[-2+yyTop]), support.string("::"),
                                          support.string("call"));
                    yyVal = support.method_optarg(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[327] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[59] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = method_add_block(support.dispatch("on_command", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[260] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[495] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[428] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_word_new");
                    yyVal = support.dispatch("on_word_add", yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[361] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-5+yyTop]), ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[294] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.method_arg(support.dispatch1("on_fcall", ((IRubyObject)yyVals[-1+yyTop])), 
                                            support.arg_new());
                    yyVal = support.method_add_block(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[26] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_until_mod", ((IRubyObject)yyVals[0+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[227] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("==="), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[93] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_const_path_field", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[529] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[-2+yyTop]).append(((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[395] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_super", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[328] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = null;
    return yyVal;
  }
};
states[60] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_command_call", ((IRubyObject)yyVals[-3+yyTop]), ".", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[261] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[496] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, null, null, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[429] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_word_add", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[362] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-1+yyTop]), null, null, support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[27] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_rescue_mod", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[228] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("!="), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[94] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
		    yyVal = support.dispatch('on_field', ((IRubyObject)yyVals[-2+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[530] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assoc_new", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[396] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_zsuper");
    return yyVal;
  }
};
states[195] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[61] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_command_call", ((IRubyObject)yyVals[-4+yyTop]), ".", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = method_add_block(yyVal, ((IRubyObject)yyVals[0+yyTop])); 
    return yyVal;
  }
};
states[497] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, null, null, null);
    return yyVal;
  }
};
states[430] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_qwords_new");
                    yyVal = support.dispatch("on_array", yyVal);
    return yyVal;
  }
};
states[363] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, ((IRubyObject)yyVals[-3+yyTop]), null, ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[28] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    if (support.isInDef() || support.isInSingle()) {
                        support.warn(ID.END_IN_METHOD, ((IRubyObject)yyVals[-3+yyTop]).getPosition(), "END in method; use at_exit");
                    }
                    yyVal = support.dispatch("on_END", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[229] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("=~"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[95] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    if (support.isInDef() || support.isInSingle()) {
                        support.yyerror("dynamic constant assignment");
                    }

                    yyVal = support.dispatch('on_const_path_field', ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[296] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.method_add_block(((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[531] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assoc_new", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[397] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_aref", ((IRubyObject)yyVals[-3+yyTop]), support.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[330] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[196] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_assign", ((IRubyObject)yyVals[-4+yyTop]), support.dispatch("on_rescue_mod", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[62] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_command_call", ((IRubyObject)yyVals[-3+yyTop]), "::", ((IRubyObject)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[263] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add(support.arg_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[498] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_param_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[431] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_array", ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[364] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, ((IRubyObject)yyVals[-1+yyTop]), null, support.escape(((IRubyObject)yyVals[0+yyTop])));     
    return yyVal;
  }
};
states[230] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("!~"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[96] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_top_const_field", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[297] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = ((IRubyObject)yyVals[0+yyTop]);
    return yyVal;
  }
};
states[398] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.pushBlockScope();
    return yyVal;
  }
};
states[331] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = null;
    return yyVal;
  }
};
states[197] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-2+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[63] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_command_call", ((IRubyObject)yyVals[-4+yyTop]), "::", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
                    yyVal = method_add_block(yyVal, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[264] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_star(support.arg_new(), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[499] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_param_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[432] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_qwords_new");
    return yyVal;
  }
};
states[365] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[30] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_massign", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[231] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.symbol('!'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[97] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_var_field", ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_assign_error", yyVal);
    return yyVal;
  }
};
states[298] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_if", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]), supoprt.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[399] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_brace_block", support.escape(((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[198] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-2+yyTop]) = support.dispatch("on_rescue_mod", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-4+yyTop]), ((Token)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-2+yyTop]));
    return yyVal;
  }
};
states[64] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_super", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[265] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add(((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[500] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_param_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[433] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_qwords_add", ((IRubyObject)yyVals[-2+yyTop]), ((IRubyObject)yyVals[-1+yyTop]));
    return yyVal;
  }
};
states[366] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.params_new(null, null, null, null, ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[31] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-2+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[232] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unary", support.symbol('~'), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[98] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
                    yyVal = support.dispatch("on_var_field", yyVal);
    return yyVal;
  }
};
states[299] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_unless", ((IRubyObject)yyVals[-4+yyTop]), ((IRubyObject)yyVals[-2+yyTop]), supoprt.escape(((IRubyObject)yyVals[-1+yyTop])));
    return yyVal;
  }
};
states[400] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    support.pushBlockScope();
    return yyVal;
  }
};
states[199] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-5+yyTop]) = support.dispatch("on_aref_field", ((IRubyObject)yyVals[-5+yyTop]), support.escape(((IRubyObject)yyVals[-3+yyTop])));
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-5+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[65] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_yield", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[266] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.arg_add_star(((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[501] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_param_error", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[434] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_string_content");
    return yyVal;
  }
};
states[32] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_aref_field", ((IRubyObject)yyVals[-5+yyTop]), support.escape(((IRubyObject)yyVals[-3+yyTop])));
                    yyVal = support.dispatch("on_opassign", yyVal, ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[233] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_binary", ((IRubyObject)yyVals[-2+yyTop]), support.string("<<"), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[99] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
                    yyVal = support.dispatch("on_var_field", yyVal);
    return yyVal;
  }
};
states[300] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    lexer.getConditionState().begin();
    return yyVal;
  }
};
states[401] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_do_block", support.escape(((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[-1+yyTop]));
                    support.popCurrentScope();
    return yyVal;
  }
};
states[334] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_elsif", ((IRubyObject)yyVals[-3+yyTop]), ((IRubyObject)yyVals[-1+yyTop]), support.escape(((IRubyObject)yyVals[0+yyTop])));
    return yyVal;
  }
};
states[200] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    ((IRubyObject)yyVals[-4+yyTop]) = support.dispatch("on_field", ((IRubyObject)yyVals[-4+yyTop]), support.symbol('.'), ((IRubyObject)yyVals[-2+yyTop]));
                    yyVal = support.dispatch("on_opassign", ((IRubyObject)yyVals[-4+yyTop]), ((Token)yyVals[-1+yyTop]), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[66] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.dispatch("on_areturn", ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
states[267] = new ParserState() {
  @Override public Object execute(ParserSupport support, RubyYaccLexer lexer, Object yyVal, Object[] yyVals, int yyTop) {
                    yyVal = support.mrhs_add(args2mrhs(((IRubyObject)yyVals[-2+yyTop])), ((IRubyObject)yyVals[0+yyTop]));
    return yyVal;
  }
};
}
					// line 1754 "Ripper19Parser.y"

    /** The parse method use an lexer stream and parse it to an AST node 
     * structure
     */
    public RubyParserResult parse(ParserConfiguration configuration, LexerSource source) throws IOException {
        support.reset();
        support.setConfiguration(configuration);
        support.setResult(new RubyParserResult());
        
        lexer.reset();
        lexer.setSource(source);
        lexer.setEncoding(configuration.getDefaultEncoding());

        Object debugger = null;
        if (configuration.isDebug()) {
            try {
                Class yyDebugAdapterClass = Class.forName("jay.yydebug.yyDebugAdapter");
                debugger = yyDebugAdapterClass.newInstance();
            } catch (IllegalAccessException iae) {
                // ignore, no debugger present
            } catch (InstantiationException ie) {
                // ignore, no debugger present
            } catch (ClassNotFoundException cnfe) {
                // ignore, no debugger present
            }
        }
        //yyparse(lexer, new jay.yydebug.yyAnim("JRuby", 9));
        yyparse(lexer, debugger);
        
        return support.getResult();
    }
}
					// line 7961 "-"
