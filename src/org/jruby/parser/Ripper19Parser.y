%{
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
%}

// We need to make sure we have same tokens in the same order and up
// front so 1.8 and 1.9 parser can use the same Tokens.java file.
%token <IRubyObject> kCLASS kMODULE kDEF kUNDEF kBEGIN kRESCUE kENSURE kEND kIF
  kUNLESS kTHEN kELSIF kELSE kCASE kWHEN kWHILE kUNTIL kFOR kBREAK kNEXT
  kREDO kRETRY kIN kDO kDO_COND kDO_BLOCK kRETURN kYIELD kSUPER kSELF kNIL
  kTRUE kFALSE kAND kOR kNOT kIF_MOD kUNLESS_MOD kWHILE_MOD kUNTIL_MOD
  kRESCUE_MOD kALIAS kDEFINED klBEGIN klEND k__LINE__ k__FILE__
  k__ENCODING__ kDO_LAMBDA 

%token <IRubyObject> tIDENTIFIER tFID tGVAR tIVAR tCONSTANT tCVAR tLABEL tCHAR
%type <IRubyObject> variable
%type <IRubyObject> sym symbol operation operation2 operation3 cname fname 
%type <Token> op
%type <IRubyObject> f_norm_arg dot_or_colon restarg_mark blkarg_mark
%token <Token> tUPLUS         /* unary+ */
%token <Token> tUMINUS        /* unary- */
%token <Token> tUMINUS_NUM    /* unary- */
%token <Token> tPOW           /* ** */
%token <Token> tCMP           /* <=> */
%token <Token> tEQ            /* == */
%token <Token> tEQQ           /* === */
%token <Token> tNEQ           /* != */
%token <Token> tGEQ           /* >= */
%token <Token> tLEQ           /* <= */
%token <Token> tANDOP tOROP   /* && and || */
%token <Token> tMATCH tNMATCH /* =~ and !~ */
%token <Token>  tDOT           /* Is just '.' in ruby and not a token */
%token <Token> tDOT2 tDOT3    /* .. and ... */
%token <Token> tAREF tASET    /* [] and []= */
%token <Token> tLSHFT tRSHFT  /* << and >> */
%token <Token> tCOLON2        /* :: */
%token <Token> tCOLON3        /* :: at EXPR_BEG */
%token <Token> tOP_ASGN       /* +=, -=  etc. */
%token <Token> tASSOC         /* => */
%token <Token> tLPAREN        /* ( */
%token <Token> tLPAREN2        /* ( Is just '(' in ruby and not a token */
%token <Token> tRPAREN        /* ) */
%token <Token> tLPAREN_ARG    /* ( */
%token <Token> tLBRACK        /* [ */
%token <Token> tRBRACK        /* ] */
%token <Token> tLBRACE        /* { */
%token <Token> tLBRACE_ARG    /* { */
%token <Token> tSTAR          /* * */
%token <Token> tSTAR2         /* *  Is just '*' in ruby and not a token */
%token <Token> tAMPER         /* & */
%token <Token> tAMPER2        /* &  Is just '&' in ruby and not a token */
%token <Token> tTILDE         /* ` is just '`' in ruby and not a token */
%token <Token> tPERCENT       /* % is just '%' in ruby and not a token */
%token <Token> tDIVIDE        /* / is just '/' in ruby and not a token */
%token <Token> tPLUS          /* + is just '+' in ruby and not a token */
%token <Token> tMINUS         /* - is just '-' in ruby and not a token */
%token <Token> tLT            /* < is just '<' in ruby and not a token */
%token <Token> tGT            /* > is just '>' in ruby and not a token */
%token <Token> tPIPE          /* | is just '|' in ruby and not a token */
%token <Token> tBANG          /* ! is just '!' in ruby and not a token */
%token <Token> tCARET         /* ^ is just '^' in ruby and not a token */
%token <Token> tLCURLY        /* { is just '{' in ruby and not a token */
%token <Token> tRCURLY        /* } is just '}' in ruby and not a token */
%token <Token> tBACK_REF2     /* { is just '`' in ruby and not a token */
%token <Token> tSYMBEG tSTRING_BEG tXSTRING_BEG tREGEXP_BEG tWORDS_BEG tQWORDS_BEG
%token <Token> tSTRING_DBEG tSTRING_DVAR tSTRING_END
%token <Token> tLAMBDA tLAMBEG
%token <IRubyObject> tNTH_REF tBACK_REF tSTRING_CONTENT tINTEGER
%token <IRubyObject> tFLOAT  
%token <IRubyObject>  tREGEXP_END
%type <IRubyObject> f_rest_arg 
%type <IRubyObject> singleton strings string string1 xstring regexp
%type <IRubyObject> string_contents xstring_contents string_content method_call
%type <IRubyObject> words qwords word literal numeric dsym cpath command_asgn command_call
%type <IRubyObject> compstmt bodystmt stmts stmt expr arg primary command 
%type <IRubyObject> expr_value primary_value opt_else cases if_tail exc_var
   // ENEBO: missing call_args2, open_args
%type <IRubyObject> call_args opt_ensure paren_args superclass
%type <IRubyObject> command_args var_ref opt_paren_args block_call block_command
%type <IRubyObject> f_opt
%type <RubyArray> undef_list 
%type <IRubyObject> string_dvar backref
%type <IRubyObject> f_args f_arglist f_larglist block_param block_param_def opt_block_param 
%type <IRubyObject> mrhs mlhs_item mlhs_node arg_value case_body exc_list aref_args
   // ENEBO: missing block_var == for_var, opt_block_var
%type <IRubyObject> lhs none args
%type <IRubyObject> qword_list word_list f_arg f_optarg f_marg_list
   // ENEBO: missing when_args
%type <IRubyObject> mlhs_head assocs assoc assoc_list mlhs_post f_block_optarg
%type <IRubyObject> opt_block_arg block_arg none_block_pass
%type <IRubyObject> opt_f_block_arg f_block_arg
%type <IRubyObject> brace_block do_block cmd_brace_block
   // ENEBO: missing mhls_entry
%type <IRubyObject> mlhs mlhs_basic 
%type <IRubyObject> opt_rescue
%type <IRubyObject> var_lhs
%type <IRubyObject> fsym
%type <IRubyObject> fitem
   // ENEBO: begin all new types
%type <IRubyObject> f_arg_item
%type <IRubyObject> bv_decls opt_bv_decl lambda_body 
%type <IRubyObject> lambda
%type <IRubyObject> mlhs_inner f_block_opt for_var
%type <IRubyObject> opt_call_args f_marg f_margs
%type <IRubyObject> bvar
   // ENEBO: end all new types

%type <IRubyObject> rparen rbracket reswords f_bad_arg
%type <IRubyObject> top_compstmt top_stmts top_stmt

/*
 *    precedence table
 */

%nonassoc tLOWEST
%nonassoc tLBRACE_ARG

%nonassoc  kIF_MOD kUNLESS_MOD kWHILE_MOD kUNTIL_MOD
%left  kOR kAND
%right kNOT
%nonassoc kDEFINED
%right '=' tOP_ASGN
%left kRESCUE_MOD
%right '?' ':'
%nonassoc tDOT2 tDOT3
%left  tOROP
%left  tANDOP
%nonassoc  tCMP tEQ tEQQ tNEQ tMATCH tNMATCH
%left  tGT tGEQ tLT tLEQ
%left  tPIPE tCARET
%left  tAMPER2
%left  tLSHFT tRSHFT
%left  tPLUS tMINUS
%left  tSTAR2 tDIVIDE tPERCENT
%right tUMINUS_NUM tUMINUS
%right tPOW
%right tBANG tTILDE tUPLUS

   //%token <Integer> tLAST_TOKEN

%%
program       : {
                  lexer.setState(LexState.EXPR_BEG);
              } top_compstmt {
                  support.setResult(support.dispatch("on_program", $2));
              }

top_compstmt  : top_stmts opt_terms {
                  $$ = $1;
              }

top_stmts     : none {
                  $$ = support.dispatch("on_stmts_add", 
                                           support.dispatch("on_stmts_new"), 
                                           support.dispatch("on_void_stmt"));
              }
              | top_stmt {
                  $$ = support.dispatch("on_stmts_add", 
                                           support.dispatch("on_stmts_new"), $1);
              }
              | top_stmts terms top_stmt {
                  $$ = support.dispatch("on_stmts_add", $1, $3);
              }
              | error top_stmt {
                  $$ = support.remove_begin($2);
              }

top_stmt      : stmt
              | klBEGIN {
                  if (support.isInDef() || support.isInSingle()) {
                      support.yyerror("BEGIN in method");
                  }
              } tLCURLY top_compstmt tRCURLY {
                  $$ = support.dispatch("on_BEGIN", $4);
              }

bodystmt      : compstmt opt_rescue opt_else opt_ensure {
                  $$ = support.dispatch("on_bodystmt", support.escape($1), support.escape($2),
                                support.escape($3), support.escape($4));
                }

compstmt        : stmts opt_terms {
                    $$ = $1;
                }

 stmts          : none {
                    $$ = support.dispatch("on_stmts_add", 
                                          support.dispatch("on_stmts_new"),
                                          support.dispatch("on_void_stmt"));
                }
                | stmt {
                    $$ = support.dispatch("on_stmts_add",
                                          ripper.dispatch("on_stmts_new"), $1);
                }
                | stmts terms stmt {
                    $$ = support.dispatch("on_stmts_add", $1, $3);
                }
                | error stmt {
                    $$ = remove_begin($2);
                }

stmt            : kALIAS fitem {
                    lexer.setState(LexState.EXPR_FNAME);
                } fitem {
                    $$ = support.dispatch("on_alias", $2, $4);
                }
                | kALIAS tGVAR tGVAR {
                    $$ = support.dispatch("on_var_alias", $2, $3);
                }
                | kALIAS tGVAR tBACK_REF {
                    $$ = support.dispatch("on_var_alias", $2, $3);
                }
                | kALIAS tGVAR tNTH_REF {
                    $$ = support.dispatch("on_alias_error", 
                                          support.dispatch("on_var_alias", $2, $3));
                }
                | kUNDEF undef_list {
                    $$ = support.dispatch("on_undef", $2);
                }
                | stmt kIF_MOD expr_value {
                    $$ = support.dispatch("on_if_mod", $3, $1);
                }
                | stmt kUNLESS_MOD expr_value {
                    $$ = support.dispatch("on_unless_mod", $3, $1);
                }
                | stmt kWHILE_MOD expr_value {
                    $$ = support.dispatch("on_while_mod", $3, $1);
                }
                | stmt kUNTIL_MOD expr_value {
                    $$ = support.dispatch("on_until_mod", $3, $1);
                }
                | stmt kRESCUE_MOD stmt {
                    $$ = support.dispatch("on_rescue_mod", $1, $3);
                }
                | klEND tLCURLY compstmt tRCURLY {
                    if (support.isInDef() || support.isInSingle()) {
                        support.warn(ID.END_IN_METHOD, $1.getPosition(), "END in method; use at_exit");
                    }
                    $$ = support.dispatch("on_END", $3);
                }
                | command_asgn
                | mlhs '=' command_call {
                    $$ = support.dispatch("on_massign", $1, $3);
                }
                | var_lhs tOP_ASGN command_call {
                    $$ = support.dispatch("on_opassign", $1, $2, $3);
                }
                | primary_value '[' opt_call_args rbracket tOP_ASGN command_call {
                    $$ = support.dispatch("on_aref_field", $1, support.escape($3));
                    $$ = support.dispatch("on_opassign", $$, $5, $6);
                }
                | primary_value tDOT tIDENTIFIER tOP_ASGN command_call {
                    $$ = support.dispatch("on_field", $1, ".", $3);
                    $$ = support.dispatch("on_opassign", $$, $4, $5);
                }
                | primary_value tDOT tCONSTANT tOP_ASGN command_call {
                    $$ = support.dispatch("on_field", $1, ".", $3);
                    $$ = support.dispatch("on_opassign", $$, $4, $5);
                }
                | primary_value tCOLON2 tCONSTANT tOP_ASGN command_call {
                    $$ = support.dispatch("on_const_path_field", $1, $3);
                    $$ = support.dispatch("on_opassign", $$, $4, $5);
                    $$ = support.dispatch("on_assign_error", $$);
                }
                | primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_call {
                    $$ = support.dispatch("on_field", $1, "::", $3);
                    $$ = support.dispatch("on_opassign", $$, $4, $5);
                }
                | backref tOP_ASGN command_call {
                    $$ = support.dispatch("on_assign", 
                                             support.dispatch("on_var_field", $1), $3);
                    $$ = support.dispatch("on_assign_error", $$);
               }
                | lhs '=' mrhs {
                    $$ = support.dispatch("on_assign", $1, $3);
                }
                | mlhs '=' arg_value {
                    $$ = support.dispatch("on_assign", $1, $3);
                }
                | mlhs '=' mrhs {
                    $$ = support.dispatch("on_massign", $1, $3);
                }
                | expr

command_asgn    : lhs '=' command_call {
                    $$ = support.dispatch("on_assign", $1, $3);
                }
                | lhs '=' command_asgn {
                    $$ = support.dispatch("on_assign", $1, $3);
                }

// Node:expr *CURRENT* all but arg so far
expr            : command_call
                | expr kAND expr {
                    $$ = support.dispatch("on_binary", $1, "and", $3);
                }
                | expr kOR expr {
                    $$ = support.dispatch("on_binary", $1, "or", $3);
                }
                | kNOT opt_nl expr {
                    $$ = support.dispatch("on_unary", ripper_intern("not"), $3);
                }
                | tBANG command_call {
                    $$ = support.dispatch("on_unary", ripper_id2sym("!"), $2);
                }
                | arg

expr_value      : expr {
                    $$ = $1;
                }

// Node:command - call with or with block on end [!null]
command_call    : command
                | block_command
                ;

// Node:block_command - A call with a block (foo.bar {...}, foo::bar {...}, bar {...}) [!null]
block_command   : block_call
                | block_call tDOT operation2 command_args {
                    $$ = method_arg(support.dispatch("on_call", $1, ".", $3), $4);
                }
                | block_call tCOLON2 operation2 command_args {
                    $$ = method_arg(support.dispatch("on_call", $1, "::", $3), $4);
                }

// :brace_block - [!null]
cmd_brace_block : tLBRACE_ARG {
                    support.pushBlockScope();
                } opt_block_param compstmt tRCURLY {
                    $$ = support.dispatch("on_brace_block", support.escape($3), $4);
                    support.popCurrentScope();
                }

// Node:command - fcall/call/yield/super [!null]
command        : operation command_args %prec tLOWEST {
                    $$ = support.dispatch("on_command", $1, $2);
                }
                | operation command_args cmd_brace_block {
                    $$ = method_add_block(support.dispatch("on_command", $1, $2), $3);
                }
                | primary_value tDOT operation2 command_args %prec tLOWEST {
                    $$ = support.dispatch("on_command_call", $1, ".", $3, $4);
                }
                | primary_value tDOT operation2 command_args cmd_brace_block {
                    $$ = support.dispatch("on_command_call", $1, ".", $3, $4);
                    $$ = method_add_block($$, $5); 
                }
                | primary_value tCOLON2 operation2 command_args %prec tLOWEST {
                    $$ = support.dispatch("on_command_call", $1, "::", $3, $4);
                }
                | primary_value tCOLON2 operation2 command_args cmd_brace_block {
                    $$ = support.dispatch("on_command_call", $1, "::", $3, $4);
                    $$ = method_add_block($$, $5);
                }
                | kSUPER command_args {
                    $$ = support.dispatch("on_super", $2);
                }
                | kYIELD command_args {
                    $$ = support.dispatch("on_yield", $2);
                }
                | kRETURN call_args {
                    $$ = support.dispatch("on_areturn", $2);
                }
		| kBREAK call_args {
                    $$ = support.dispatch("on_abreak", $2);
                }
		| kNEXT call_args {
                    $$ = support.dispatch("on_next", $2);
                }


// MultipleAssig19Node:mlhs - [!null]
mlhs            : mlhs_basic
                | tLPAREN mlhs_inner rparen {
                    $$ = support.dispatch("on_mlhs_paren", $2);
                }

// MultipleAssign19Node:mlhs_entry - mlhs w or w/o parens [!null]
mlhs_inner      : mlhs_basic
                | tLPAREN mlhs_inner rparen {
                    $$ = support.dispatch("on_mlhs_paren", $2);
                }

// MultipleAssign19Node:mlhs_basic - multiple left hand side (basic because used in multiple context) [!null]
mlhs_basic      : mlhs_head {
                    $$ = $1;
                }
                | mlhs_head mlhs_item {
                    $$ = support.mlhs_add($1, $2);
                }
                | mlhs_head tSTAR mlhs_node {
                    $$ = support.mlhs_add_star($1, $3);
                }
                | mlhs_head tSTAR mlhs_node ',' mlhs_post {
                    $$ = support.mlhs_add(support.mlhs_add_star($1, $3), $5);
                }
                | mlhs_head tSTAR {
                    $$ = support.mlhs_add_star($1, Qnil);
                }
                | mlhs_head tSTAR ',' mlhs_post {
                    $1 = support.mlhs_add_star($1, Qnil);
                    $$ = support.mlhs_add($1, $4);
                }
                | tSTAR mlhs_node {
                    $$ = support.mlhs_add_star(support.mlhs_new(), $2);
                }
                | tSTAR mlhs_node ',' mlhs_post {
                    $$ = support.mlhs_add(support.mlhs_add_star(support.mlhs_new(), $2), $4);
                }
                | tSTAR {
                    $$ = support.mlhs_add_star(support.mlhs_new(), Qnil);
                }
                | tSTAR ',' mlhs_post {
                    $$ = support.mlhs_add(support.mlhs_add_star(support.mlhs_new(), Qnil), $3);
                }

mlhs_item       : mlhs_node
                | tLPAREN mlhs_inner rparen {
                    $$ = support.dispatch("on_mlhs_paren", $2);
                }

// Set of mlhs terms at front of mlhs (a, *b, d, e = arr  # a is head)
mlhs_head       : mlhs_item ',' {
                    $$ = support.mlhs_add(support.mlhs_new(), $1);
                }
                | mlhs_head mlhs_item ',' {
                    $$ = support.mlhs_add($1, $2);
                }

// Set of mlhs terms at end of mlhs (a, *b, d, e = arr  # d,e is post)
mlhs_post       : mlhs_item {
                    $$ = support.mlhs_add(support.mlhs_new(), $1);
                }
                | mlhs_post ',' mlhs_item {
                    $$ = support.mlhs_add($1, $3);
                }

mlhs_node       : variable {
                    $$ = support.assignable($1, null);
                }
		| keyword_variable {
                    $$ = support.assignable($1, null);
                }
                | primary_value '[' opt_call_args rbracket {
                    $$ = support.dispatch("on_aref_field", $1, support.escape($3));
                }
                | primary_value tDOT tIDENTIFIER {
                    $$ = support.dispatch("on_field", $1, support.symbol('.'), $3);
                    
                }
                | primary_value tCOLON2 tIDENTIFIER {
                    $$ = support.dispatch("on_const_path_field", $1, $3);
                }
                | primary_value tDOT tCONSTANT {
		    $$ = support.dispatch('on_field', $1, support.symbol('.'), $3);
                }
                | primary_value tCOLON2 tCONSTANT {
                    if (support.isInDef() || support.isInSingle()) {
                        support.yyerror("dynamic constant assignment");
                    }

                    $$ = support.dispatch('on_const_path_field', $1, $3);
                }
                | tCOLON3 tCONSTANT {
                    $$ = support.dispatch("on_top_const_field", $2);
                }
                | backref {
                    $$ = support.dispatch("on_var_field", $1);
                    $$ = support.dispatch("on_assign_error", $$);
                }

lhs             : user_variable {
                    $$ = support.assignable($1, null);
                    $$ = support.dispatch("on_var_field", $$);
                }
		| keyword_variable {
                    $$ = support.assignable($1, null);
                    $$ = support.dispatch("on_var_field", $$);
                }
                | primary_value '[' opt_call_args rbracket {
                    $$ = support.dispatch("on_aref_field", $1, support.escape($3));
                }
                | primary_value tDOT tIDENTIFIER {
                    $$ = support.dispatch("on_field", $1, support.symbol('.'), $3);
                }
                | primary_value tCOLON2 tIDENTIFIER {
                    $$ = support.dispatch("on_field", $1, support.string("::"), $3);
                }
                | primary_value tDOT tCONSTANT {
                    $$ = support.dispatch("on_field", $1, support.symbol('.'), $3);
                }
                | primary_value tCOLON2 tCONSTANT {
                    $$ = support.dispatch("on_const_path_field", $1, $3);

                    if (support.isInDef() || support.isInSingle()) {
                        $$ = support.dispatch("on_assign_error", $$);
                    }
                }
                | tCOLON3 tCONSTANT {
                    $$ = support.dispatch("on_top_const_field", $2);

                    if (support.isInDef() || support.isInSingle()) {
                        $$ = support.dispatch("on_assign_error", $$);
                    }
                }
                | backref {
                    $$ = support.dispatch("on_assign_error", $1);
                }

cname           : tIDENTIFIER {
                    $$ = support.dispatch("on_class_name_error", $1);
                }
                | tCONSTANT

cpath           : tCOLON3 cname {
                    $$ = support.dispatch("on_top_const_ref", $2);
                }
                | cname {
                    $$ = support.dispatch("on_const_ref", $1);
                }
                | primary_value tCOLON2 cname {
                    $$ = support.dispatch("on_const_path_ref", $1, $3);
                }

// Token:fname - A function name [!null]
fname          : tIDENTIFIER | tCONSTANT | tFID 
               | op {
                   lexer.setState(LexState.EXPR_ENDFN);
                   $$ = $1;
               }
               | reswords {
                   lexer.setState(LexState.EXPR_ENDFN);
                   $$ = $1;
               }

// LiteralNode:fsym
 fsym          : fname {
                   $$ = $1;
               }
               | symbol {
                   $$ = $1;
               }

// Node:fitem
fitem           : fsym {
                   $$ = support.dispatch("on_symbol_literal", $1);
                }
                | dsym {
                   $$ = $1;
                }

undef_list      : fitem {
                    $$ = support.new_array($1);
                }
                | undef_list ',' {
                    lexer.setState(LexState.EXPR_FNAME);
                } fitem {
                    $$ = $1.append($4);
                }

// Token:op
op              : tPIPE | tCARET | tAMPER2 | tCMP | tEQ | tEQQ | tMATCH
                | tNMATCH | tGT | tGEQ | tLT | tLEQ | tNEQ | tLSHFT | tRSHFT
                | tPLUS | tMINUS | tSTAR2 | tSTAR | tDIVIDE | tPERCENT | tPOW
                | tBANG | tTILDE | tUPLUS | tUMINUS | tAREF | tASET | tBACK_REF2

// Token:op
reswords        : k__LINE__ | k__FILE__ | k__ENCODING__ | klBEGIN | klEND
                | kALIAS | kAND | kBEGIN | kBREAK | kCASE | kCLASS | kDEF
                | kDEFINED | kDO | kELSE | kELSIF | kEND | kENSURE | kFALSE
                | kFOR | kIN | kMODULE | kNEXT | kNIL | kNOT
                | kOR | kREDO | kRESCUE | kRETRY | kRETURN | kSELF | kSUPER
                | kTHEN | kTRUE | kUNDEF | kWHEN | kYIELD
                | kIF_MOD | kUNLESS_MOD | kWHILE_MOD | kUNTIL_MOD | kRESCUE_MOD

arg             : lhs '=' arg {
                    $$ = support.dispatch("on_assign", $1, $3);
                }
                | lhs '=' arg kRESCUE_MOD arg {
                    $$ = support.dispatch("on_assign", $1, support.dispatch("on_rescue_mod", $3, $5));
                }
                | var_lhs tOP_ASGN arg {
                    $$ = support.dispatch("on_opassign", $1, $2, $3);
                }
                | var_lhs tOP_ASGN arg kRESCUE_MOD arg {
                    $3 = support.dispatch("on_rescue_mod", $3, $5);
                    $$ = support.dispatch("on_opassign", $1, $2, $3);
                }
                | primary_value '[' opt_call_args rbracket tOP_ASGN arg {
                    $1 = support.dispatch("on_aref_field", $1, support.escape($3));
                    $$ = support.dispatch("on_opassign", $1, $5, $6);
                }
                | primary_value tDOT tIDENTIFIER tOP_ASGN arg {
                    $1 = support.dispatch("on_field", $1, support.symbol('.'), $3);
                    $$ = support.dispatch("on_opassign", $1, $4, $5);
                }
                | primary_value tDOT tCONSTANT tOP_ASGN arg {
                    $1 = support.dispatch("on_field", $1, support.symbol('.'), $3);
                    $$ = support.dispatch("on_opassign", $1, $4, $5);
                }
                | primary_value tCOLON2 tIDENTIFIER tOP_ASGN arg {
                    $1 = support.dispatch("on_field", $1, support.string("::"), $3);
                    $$ = support.dispatch("on_opassign", $1, $4, $5);
                }
                | primary_value tCOLON2 tCONSTANT tOP_ASGN arg {
                    $$ = support.dispatch("on_const_path_field", $1, $3);
                    $$ = support.dispatch("on_opassign", $$, $4, $5);
                    $$ = support.dispatch("on_assign_error", $$);
                }
                | tCOLON3 tCONSTANT tOP_ASGN arg {
                    $$ = support.dispatch("on_top_const_field", $2);
                    $$ = support.dispatch("on_opassign", $$, $3, $4);
                    $$ = support.dispatch("on_assign_error", $$);
                }
                | backref tOP_ASGN arg {
                    $$ = support.dispatch("on_var_field", $1);
                    $$ = support.dispatch("on_opassign", $$, $2, $3);
                    $$ = support.dispatch("on_assign_error", $$);
                }
                | arg tDOT2 arg {
                    $$ = support.dispatch("on_dot2", $1, $3);
                }
                | arg tDOT3 arg {
                    $$ = support.dispatch("on_dot3", $1, $3);
                }
                | arg tPLUS arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('+'), $3);
                }
                | arg tMINUS arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('-'), $3);
                }
                | arg tSTAR2 arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('*'), $3);
                }
                | arg tDIVIDE arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('/'), $3);
                }
                | arg tPERCENT arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('%'), $3);
                }
                | arg tPOW arg {
                    $$ = support.dispatch("on_binary", $1, support.string("**"), $3);
                }
                | tUMINUS_NUM tINTEGER tPOW arg {
                    $$ = support.dispatch("on_binary", $2, support.string("**"), $4);
                    $$ = support.dispatch("on_unary", support.string("-@"), $$);
                }
                | tUMINUS_NUM tFLOAT tPOW arg {
                    $$ = support.dispatch("on_binary", $2, support.string("**"), $4);
                    $$ = support.dispatch("on_unary", support.string("-@"), $$);
                }
                | tUPLUS arg {
                    $$ = support.dispatch("on_unary", support.string("+@"), $2);
                }
                | tUMINUS arg {
                    $$ = support.dispatch("on_unary", support.string("-@"), $2);
                }
                | arg tPIPE arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('|'), $3);
                }
                | arg tCARET arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('^'), $3);
                }
                | arg tAMPER2 arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('&'), $3);
                }
                | arg tCMP arg {
                    $$ = support.dispatch("on_binary", $1, support.string("<=>"), $3);
                }
                | arg tGT arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('>'), $3);
                }
                | arg tGEQ arg {
                    $$ = support.dispatch("on_binary", $1, support.stringl("<="), $3);
                }
                | arg tLT arg {
                    $$ = support.dispatch("on_binary", $1, support.symbol('<'), $3);
                }
                | arg tLEQ arg {
                    $$ = support.dispatch("on_binary", $1, support.string("<="), $3);
                }
                | arg tEQ arg {
                    $$ = support.dispatch("on_binary", $1, support.string("=="), $3);
                }
                | arg tEQQ arg {
                    $$ = support.dispatch("on_binary", $1, support.string("==="), $3);
                }
                | arg tNEQ arg {
                    $$ = support.dispatch("on_binary", $1, support.string("!="), $3);
                }
                | arg tMATCH arg {
                    $$ = support.dispatch("on_binary", $1, support.string("=~"), $3);
                }
                | arg tNMATCH arg {
                    $$ = support.dispatch("on_binary", $1, support.string("!~"), $3);
                }
                | tBANG arg {
                    $$ = support.dispatch("on_unary", support.symbol('!'), $2);
                }
                | tTILDE arg {
                    $$ = support.dispatch("on_unary", support.symbol('~'), $2);
                }
                | arg tLSHFT arg {
                    $$ = support.dispatch("on_binary", $1, support.string("<<"), $3);
                }
                | arg tRSHFT arg {
                    $$ = support.dispatch("on_binary", $1, support.string(">>"), $3);
                }
                | arg tANDOP arg {
                    $$ = support.dispatch("on_binary", $1, support.string("&&"), $3);
                }
                | arg tOROP arg {
                    $$ = support.dispatch("on_binary", $1, support.string("||"), $3);
                }
                | kDEFINED opt_nl arg {
                    $$ = support.dispatch("on_defined", $3);
                }
                | arg '?' arg opt_nl ':' arg {
                    $$ = support.dispatch("on_ifop", $1, $3, $6);
                }
                | primary {
                    $$ = $1;
                }

arg_value       : arg {
                    $$ = $1;
                }

aref_args       : none
                | args trailer {
                    $$ = $1;
                }
                | args ',' assocs trailer {
                    $$ = support.arg_append($1, new Hash19Node(lexer.getPosition(), $3));
                }
                | assocs trailer {
                    $$ = support.newArrayNode($1.getPosition(), new Hash19Node(lexer.getPosition(), $1));
                }

paren_args      : tLPAREN2 opt_call_args rparen {
                    $$ = $2;
                    if ($$ != null) $<Node>$.setPosition($1.getPosition());
                }

opt_paren_args  : none | paren_args

opt_call_args   : none | call_args

// [!null]
call_args       : command {
                    $$ = support.newArrayNode(support.getPosition($1), $1);
                }
                | args opt_block_arg {
                    $$ = support.arg_blk_pass($1, $2);
                }
                | assocs opt_block_arg {
                    $$ = support.newArrayNode($1.getPosition(), new Hash19Node(lexer.getPosition(), $1));
                    $$ = support.arg_blk_pass((Node)$$, $2);
                }
                | args ',' assocs opt_block_arg {
                    $$ = support.arg_append($1, new Hash19Node(lexer.getPosition(), $3));
                    $$ = support.arg_blk_pass((Node)$$, $4);
                }
                | block_arg {
                }

command_args    : /* none */ {
                    $$ = Long.valueOf(lexer.getCmdArgumentState().begin());
                } call_args {
                    lexer.getCmdArgumentState().reset($<Long>1.longValue());
                    $$ = $2;
                }

block_arg       : tAMPER arg_value {
                    $$ = new BlockPassNode($1.getPosition(), $2);
                }

opt_block_arg   : ',' block_arg {
                    $$ = $2;
                }
                | ',' {
                    $$ = null;
                }
                | none_block_pass

// [!null]
args            : arg_value {
                    ISourcePosition pos = $1 == null ? lexer.getPosition() : $1.getPosition();
                    $$ = support.newArrayNode(pos, $1);
                }
                | tSTAR arg_value {
                    $$ = support.newSplatNode($1.getPosition(), $2);
                }
                | args ',' arg_value {
                    Node node = support.splat_array($1);

                    if (node != null) {
                        $$ = support.list_append(node, $3);
                    } else {
                        $$ = support.arg_append($1, $3);
                    }
                }
                | args ',' tSTAR arg_value {
                    Node node = null;

                    // FIXME: lose syntactical elements here (and others like this)
                    if ($4 instanceof ArrayNode &&
                        (node = support.splat_array($1)) != null) {
                        $$ = support.list_concat(node, $4);
                    } else {
                        $$ = support.arg_concat(support.getPosition($1), $1, $4);
                    }
                }

mrhs            : args ',' arg_value {
                    Node node = support.splat_array($1);

                    if (node != null) {
                        $$ = support.list_append(node, $3);
                    } else {
                        $$ = support.arg_append($1, $3);
                    }
                }
                | args ',' tSTAR arg_value {
                    Node node = null;

                    if ($4 instanceof ArrayNode &&
                        (node = support.splat_array($1)) != null) {
                        $$ = support.list_concat(node, $4);
                    } else {
                        $$ = support.arg_concat($1.getPosition(), $1, $4);
                    }
                }
                | tSTAR arg_value {
                     $$ = support.newSplatNode(support.getPosition($1), $2);  
                }

primary         : literal
                | strings
                | xstring
                | regexp
                | words
                | qwords
                | var_ref
                | backref
                | tFID {
                    $$ = new FCallNoArgNode($1.getPosition(), (String) $1.getValue());
                }
                | kBEGIN bodystmt kEND {
                    $$ = new BeginNode(support.getPosition($1), $2 == null ? NilImplicitNode.NIL : $2);
                }
                | tLPAREN_ARG expr {
                    lexer.setState(LexState.EXPR_ENDARG); 
                } rparen {
                    support.warning(ID.GROUPED_EXPRESSION, $1.getPosition(), "(...) interpreted as grouped expression");
                    $$ = $2;
                }
                | tLPAREN compstmt tRPAREN {
                    if ($2 != null) {
                        // compstmt position includes both parens around it
                        ((ISourcePositionHolder) $2).setPosition($1.getPosition());
                        $$ = $2;
                    } else {
                        $$ = new NilNode($1.getPosition());
                    }
                }
                | primary_value tCOLON2 tCONSTANT {
                    $$ = support.new_colon2(support.getPosition($1), $1, (String) $3.getValue());
                }
                | tCOLON3 tCONSTANT {
                    $$ = support.new_colon3($1.getPosition(), (String) $2.getValue());
                }
                | tLBRACK aref_args tRBRACK {
                    ISourcePosition position = $1.getPosition();
                    if ($2 == null) {
                        $$ = new ZArrayNode(position); /* zero length array */
                    } else {
                        $$ = $2;
                        $<ISourcePositionHolder>$.setPosition(position);
                    }
                }
                | tLBRACE assoc_list tRCURLY {
                    $$ = new Hash19Node($1.getPosition(), $2);
                }
                | kRETURN {
                    $$ = new ReturnNode($1.getPosition(), NilImplicitNode.NIL);
                }
                | kYIELD tLPAREN2 call_args rparen {
                    $$ = support.new_yield($1.getPosition(), $3);
                }
                | kYIELD tLPAREN2 rparen {
                    $$ = new ZYieldNode($1.getPosition());
                }
                | kYIELD {
                    $$ = new ZYieldNode($1.getPosition());
                }
                | kDEFINED opt_nl tLPAREN2 expr rparen {
                    $$ = new DefinedNode($1.getPosition(), $4);
                }
                | kNOT tLPAREN2 expr rparen {
                    $$ = support.getOperatorCallNode(support.getConditionNode($3), "!");
                }
                | kNOT tLPAREN2 rparen {
                    $$ = support.getOperatorCallNode(NilImplicitNode.NIL, "!");
                }
                | operation brace_block {
                    $$ = new FCallNoArgBlockNode($1.getPosition(), (String) $1.getValue(), $2);
                }
                | method_call
                | method_call brace_block {
                    if ($1 != null && 
                          $<BlockAcceptingNode>1.getIterNode() instanceof BlockPassNode) {
                        throw new SyntaxException(PID.BLOCK_ARG_AND_BLOCK_GIVEN, $1.getPosition(), lexer.getCurrentLine(), "Both block arg and actual block given.");
                    }
                    $$ = $<BlockAcceptingNode>1.setIterNode($2);
                    $<Node>$.setPosition($1.getPosition());
                }
                | tLAMBDA lambda {
                    $$ = $2;
                }
                | kIF expr_value then compstmt if_tail kEND {
                    $$ = new IfNode($1.getPosition(), support.getConditionNode($2), $4, $5);
                }
                | kUNLESS expr_value then compstmt opt_else kEND {
                    $$ = new IfNode($1.getPosition(), support.getConditionNode($2), $5, $4);
                }
                | kWHILE {
                    lexer.getConditionState().begin();
                } expr_value do {
                    lexer.getConditionState().end();
                } compstmt kEND {
                    Node body = $6 == null ? NilImplicitNode.NIL : $6;
                    $$ = new WhileNode($1.getPosition(), support.getConditionNode($3), body);
                }
                | kUNTIL {
                  lexer.getConditionState().begin();
                } expr_value do {
                  lexer.getConditionState().end();
                } compstmt kEND {
                    Node body = $6 == null ? NilImplicitNode.NIL : $6;
                    $$ = new UntilNode($1.getPosition(), support.getConditionNode($3), body);
                }
                | kCASE expr_value opt_terms case_body kEND {
                    $$ = support.newCaseNode($1.getPosition(), $2, $4);
                }
                | kCASE opt_terms case_body kEND {
                    $$ = support.newCaseNode($1.getPosition(), null, $3);
                }
                | kFOR for_var kIN {
                    lexer.getConditionState().begin();
                } expr_value do {
                    lexer.getConditionState().end();
                } compstmt kEND {
                      // ENEBO: Lots of optz in 1.9 parser here
                    $$ = new ForNode($1.getPosition(), $2, $8, $5, support.getCurrentScope());
                }
                | kCLASS cpath superclass {
                    if (support.isInDef() || support.isInSingle()) {
                        support.yyerror("class definition in method body");
                    }
                    support.pushLocalScope();
                } bodystmt kEND {
                    Node body = $5 == null ? NilImplicitNode.NIL : $5;

                    $$ = new ClassNode($1.getPosition(), $<Colon3Node>2, support.getCurrentScope(), body, $3);
                    support.popCurrentScope();
                }
                | kCLASS tLSHFT expr {
                    $$ = Boolean.valueOf(support.isInDef());
                    support.setInDef(false);
                } term {
                    $$ = Integer.valueOf(support.getInSingle());
                    support.setInSingle(0);
                    support.pushLocalScope();
                } bodystmt kEND {
                    $$ = new SClassNode($1.getPosition(), $3, support.getCurrentScope(), $7);
                    support.popCurrentScope();
                    support.setInDef($<Boolean>4.booleanValue());
                    support.setInSingle($<Integer>6.intValue());
                }
                | kMODULE cpath {
                    if (support.isInDef() || support.isInSingle()) { 
                        support.yyerror("module definition in method body");
                    }
                    support.pushLocalScope();
                } bodystmt kEND {
                    Node body = $4 == null ? NilImplicitNode.NIL : $4;

                    $$ = new ModuleNode($1.getPosition(), $<Colon3Node>2, support.getCurrentScope(), body);
                    support.popCurrentScope();
                }
                | kDEF fname {
                    support.setInDef(true);
                    support.pushLocalScope();
                } f_arglist bodystmt kEND {
                    // TODO: We should use implicit nil for body, but problem (punt til later)
                    Node body = $5; //$5 == null ? NilImplicitNode.NIL : $5;

                    $$ = new DefnNode($1.getPosition(), new ArgumentNode($2.getPosition(), (String) $2.getValue()), $4, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setInDef(false);
                }
                | kDEF singleton dot_or_colon {
                    lexer.setState(LexState.EXPR_FNAME);
                } fname {
                    support.setInSingle(support.getInSingle() + 1);
                    support.pushLocalScope();
                    lexer.setState(LexState.EXPR_ENDFN); /* force for args */
                } f_arglist bodystmt kEND {
                    // TODO: We should use implicit nil for body, but problem (punt til later)
                    Node body = $8; //$8 == null ? NilImplicitNode.NIL : $8;

                    $$ = new DefsNode($1.getPosition(), $2, new ArgumentNode($5.getPosition(), (String) $5.getValue()), $7, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setInSingle(support.getInSingle() - 1);
                }
                | kBREAK {
                    $$ = new BreakNode($1.getPosition(), NilImplicitNode.NIL);
                }
                | kNEXT {
                    $$ = new NextNode($1.getPosition(), NilImplicitNode.NIL);
                }
                | kREDO {
                    $$ = new RedoNode($1.getPosition());
                }
                | kRETRY {
                    $$ = new RetryNode($1.getPosition());
                }

primary_value   : primary {
                    support.checkExpression($1);
                    $$ = $1;
                    if ($$ == null) $$ = NilImplicitNode.NIL;
                }

then            : term
                | kTHEN
                | term kTHEN

do              : term
                | kDO_COND

if_tail         : opt_else
                | kELSIF expr_value then compstmt if_tail {
                    $$ = new IfNode($1.getPosition(), support.getConditionNode($2), $4, $5);
                }

opt_else        : none
                | kELSE compstmt {
                    $$ = $2;
                }

for_var         : lhs
                | mlhs {
                }

f_marg          : f_norm_arg {
                     $$ = support.assignable($1, NilImplicitNode.NIL);
                }
                | tLPAREN f_margs rparen {
                    $$ = $2;
                }

// [!null]
f_marg_list     : f_marg {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | f_marg_list ',' f_marg {
                    $$ = $1.add($3);
                }

f_margs         : f_marg_list {
                    $$ = new MultipleAsgn19Node($1.getPosition(), $1, null, null);
                }
                | f_marg_list ',' tSTAR f_norm_arg {
                    $$ = new MultipleAsgn19Node($1.getPosition(), $1, support.assignable($4, null), null);
                }
                | f_marg_list ',' tSTAR f_norm_arg ',' f_marg_list {
                    $$ = new MultipleAsgn19Node($1.getPosition(), $1, support.assignable($4, null), $6);
                }
                | f_marg_list ',' tSTAR {
                    $$ = new MultipleAsgn19Node($1.getPosition(), $1, new StarNode(lexer.getPosition()), null);
                }
                | f_marg_list ',' tSTAR ',' f_marg_list {
                    $$ = new MultipleAsgn19Node($1.getPosition(), $1, new StarNode(lexer.getPosition()), $5);
                }
                | tSTAR f_norm_arg {
                    $$ = new MultipleAsgn19Node($1.getPosition(), null, support.assignable($2, null), null);
                }
                | tSTAR f_norm_arg ',' f_marg_list {
                    $$ = new MultipleAsgn19Node($1.getPosition(), null, support.assignable($2, null), $4);
                }
                | tSTAR {
                    $$ = new MultipleAsgn19Node($1.getPosition(), null, new StarNode(lexer.getPosition()), null);
                }
                | tSTAR ',' f_marg_list {
                    $$ = new MultipleAsgn19Node($1.getPosition(), null, null, $3);
                }

// [!null]
block_param     : f_arg ',' f_block_optarg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, null, $6);
                }
                | f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, $7, $8);
                }
                | f_arg ',' f_block_optarg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, null, $4);
                }
                | f_arg ',' f_block_optarg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, $5, $6);
                }
                | f_arg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, null, $4);
                }
                | f_arg ',' {
                    RestArgNode rest = new UnnamedRestArgNode($1.getPosition(), null, support.getCurrentScope().addVariable("*"));
                    $$ = support.new_args($1.getPosition(), $1, null, rest, null, null);
                }
                | f_arg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, $5, $6);
                }
                | f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, null, null, $2);
                }
                | f_block_optarg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args(support.getPosition($1), null, $1, $3, null, $4);
                }
                | f_block_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args(support.getPosition($1), null, $1, $3, $5, $6);
                }
                | f_block_optarg opt_f_block_arg {
                    $$ = support.new_args(support.getPosition($1), null, $1, null, null, $2);
                }
                | f_block_optarg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, $1, null, $3, $4);
                }
                | f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, $1, null, $2);
                }
                | f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, $1, $3, $4);
                }
                | f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, $1);
                }

opt_block_param : none {
    // was $$ = null;
                   $$ = support.new_args(lexer.getPosition(), null, null, null, null, null);
                }
                | block_param_def {
                    lexer.commandStart = true;
                    $$ = $1;
                }

block_param_def : tPIPE opt_bv_decl tPIPE {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, null);
                }
                | tOROP {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, null);
                }
                | tPIPE block_param opt_bv_decl tPIPE {
                    $$ = $2;
                }

// shadowed block variables....
opt_bv_decl     : opt_nl {
                    $$ = null;
                }
                | opt_nl ';' bv_decls opt_nl {
                    $$ = null;
                }

// ENEBO: This is confusing...
bv_decls        : bvar {
                    $$ = null;
                }
                | bv_decls ',' bvar {
                    $$ = null;
                }

bvar            : tIDENTIFIER {
                    support.new_bv($1);
                }
                | f_bad_arg {
                    $$ = null;
                }

lambda          : /* none */  {
                    support.pushBlockScope();
                    $$ = lexer.getLeftParenBegin();
                    lexer.setLeftParenBegin(lexer.incrementParenNest());
                } f_larglist lambda_body {
                    $$ = new LambdaNode($2.getPosition(), $2, $3, support.getCurrentScope());
                    support.popCurrentScope();
                    lexer.setLeftParenBegin($<Integer>1);
                }

f_larglist      : tLPAREN2 f_args opt_bv_decl tRPAREN {
                    $$ = $2;
                    $<ISourcePositionHolder>$.setPosition($1.getPosition());
                }
                | f_args opt_bv_decl {
                    $$ = $1;
                }

lambda_body     : tLAMBEG compstmt tRCURLY {
                    $$ = $2;
                }
                | kDO_LAMBDA compstmt kEND {
                    $$ = $2;
                }

do_block        : kDO_BLOCK {
                    support.pushBlockScope();
                } opt_block_param compstmt kEND {
                    $$ = new IterNode(support.getPosition($1), $3, $4, support.getCurrentScope());
                    support.popCurrentScope();
                }

block_call      : command do_block {
                    // Workaround for JRUBY-2326 (MRI does not enter this production for some reason)
                    if ($1 instanceof YieldNode) {
                        throw new SyntaxException(PID.BLOCK_GIVEN_TO_YIELD, $1.getPosition(), lexer.getCurrentLine(), "block given to yield");
                    }
                    if ($<BlockAcceptingNode>1.getIterNode() instanceof BlockPassNode) {
                        throw new SyntaxException(PID.BLOCK_ARG_AND_BLOCK_GIVEN, $1.getPosition(), lexer.getCurrentLine(), "Both block arg and actual block given.");
                    }
                    $$ = $<BlockAcceptingNode>1.setIterNode($2);
                    $<Node>$.setPosition($1.getPosition());
                }
                | block_call tDOT operation2 opt_paren_args {
                    $$ = support.new_call($1, $3, $4, null);
                }
                | block_call tCOLON2 operation2 opt_paren_args {
                    $$ = support.new_call($1, $3, $4, null);
                }

// [!null]
method_call     : operation paren_args {
                    $$ = support.new_fcall($1, $2, null);
                }
                | primary_value tDOT operation2 opt_paren_args {
                    $$ = support.new_call($1, $3, $4, null);
                }
                | primary_value tCOLON2 operation2 paren_args {
                    $$ = support.new_call($1, $3, $4, null);
                }
                | primary_value tCOLON2 operation3 {
                    $$ = support.new_call($1, $3, null, null);
                }
                | primary_value tDOT paren_args {
                    $$ = support.new_call($1, new Token("call", $1.getPosition()), $3, null);
                }
                | primary_value tCOLON2 paren_args {
                    $$ = support.new_call($1, new Token("call", $1.getPosition()), $3, null);
                }
                | kSUPER paren_args {
                    $$ = support.new_super($2, $1);
                }
                | kSUPER {
                    $$ = new ZSuperNode($1.getPosition());
                }
                | primary_value '[' opt_call_args rbracket {
                    if ($1 instanceof SelfNode) {
                        $$ = support.new_fcall(new Token("[]", support.getPosition($1)), $3, null);
                    } else {
                        $$ = support.new_call($1, new Token("[]", support.getPosition($1)), $3, null);
                    }
                }

brace_block     : tLCURLY {
                    support.pushBlockScope();
                } opt_block_param compstmt tRCURLY {
                    $$ = new IterNode($1.getPosition(), $3, $4, support.getCurrentScope());
                    support.popCurrentScope();
                }
                | kDO {
                    support.pushBlockScope();
                } opt_block_param compstmt kEND {
                    $$ = new IterNode($1.getPosition(), $3, $4, support.getCurrentScope());
                    // FIXME: What the hell is this?
                    $<ISourcePositionHolder>0.setPosition(support.getPosition($<ISourcePositionHolder>0));
                    support.popCurrentScope();
                }

case_body       : kWHEN args then compstmt cases {
                    $$ = support.newWhenNode($1.getPosition(), $2, $4, $5);
                }

cases           : opt_else | case_body

opt_rescue      : kRESCUE exc_list exc_var then compstmt opt_rescue {
                    Node node;
                    if ($3 != null) {
                        node = support.appendToBlock(support.node_assign($3, new GlobalVarNode($1.getPosition(), "$!")), $5);
                        if ($5 != null) {
                            node.setPosition(support.unwrapNewlineNode($5).getPosition());
                        }
                    } else {
                        node = $5;
                    }
                    Node body = node == null ? NilImplicitNode.NIL : node;
                    $$ = new RescueBodyNode($1.getPosition(), $2, body, $6);
                }
                | { 
                    $$ = null; 
                }

exc_list        : arg_value {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | mrhs {
                    $$ = support.splat_array($1);
                    if ($$ == null) $$ = $1;
                }
                | none

exc_var         : tASSOC lhs {
                    $$ = $2;
                }
                | none

opt_ensure      : kENSURE compstmt {
                    $$ = $2;
                }
                | none

literal         : numeric
                | symbol {
                    // FIXME: We may be intern'ing more than once.
                    $$ = new SymbolNode($1.getPosition(), ((String) $1.getValue()).intern());
                }
                | dsym

strings         : string {
                    $$ = $1 instanceof EvStrNode ? new DStrNode($1.getPosition(), lexer.getEncoding()).add($1) : $1;
                    /*
                    NODE *node = $1;
                    if (!node) {
                        node = NEW_STR(STR_NEW0());
                    } else {
                        node = evstr2dstr(node);
                    }
                    $$ = node;
                    */
                }

// [!null]
string          : tCHAR {
                    ByteList aChar = ByteList.create((String) $1.getValue());
                    aChar.setEncoding(lexer.getEncoding());
                    $$ = lexer.createStrNode($<Token>0.getPosition(), aChar, 0);
                }
                | string1 {
                    $$ = $1;
                }
                | string string1 {
                    $$ = support.literal_concat($1.getPosition(), $1, $2);
                }

string1         : tSTRING_BEG string_contents tSTRING_END {
                    $$ = $2;

                    $<ISourcePositionHolder>$.setPosition($1.getPosition());
                    int extraLength = ((String) $1.getValue()).length() - 1;

                    // We may need to subtract addition offset off of first 
                    // string fragment (we optimistically take one off in
                    // ParserSupport.literal_concat).  Check token length
                    // and subtract as neeeded.
                    if (($2 instanceof DStrNode) && extraLength > 0) {
                      Node strNode = ((DStrNode)$2).get(0);
                    }
                }

xstring         : tXSTRING_BEG xstring_contents tSTRING_END {
                    ISourcePosition position = $1.getPosition();

                    if ($2 == null) {
                        $$ = new XStrNode(position, null);
                    } else if ($2 instanceof StrNode) {
                        $$ = new XStrNode(position, (ByteList) $<StrNode>2.getValue().clone());
                    } else if ($2 instanceof DStrNode) {
                        $$ = new DXStrNode(position, $<DStrNode>2);

                        $<Node>$.setPosition(position);
                    } else {
                        $$ = new DXStrNode(position).add($2);
                    }
                }

regexp          : tREGEXP_BEG xstring_contents tREGEXP_END {
                    $$ = support.newRegexpNode($1.getPosition(), $2, (RegexpNode) $3);
                }

words           : tWORDS_BEG ' ' tSTRING_END {
                    $$ = new ZArrayNode($1.getPosition());
                }
                | tWORDS_BEG word_list tSTRING_END {
                    $$ = $2;
                }

word_list       : /* none */ {
                    $$ = new ArrayNode(lexer.getPosition());
                }
                | word_list word ' ' {
                     $$ = $1.add($2 instanceof EvStrNode ? new DStrNode($1.getPosition(), lexer.getEncoding()).add($2) : $2);
                }

word            : string_content
                | word string_content {
                     $$ = support.literal_concat(support.getPosition($1), $1, $2);
                }

qwords          : tQWORDS_BEG ' ' tSTRING_END {
                     $$ = new ZArrayNode($1.getPosition());
                }
                | tQWORDS_BEG qword_list tSTRING_END {
                    $$ = $2;
                    $<ISourcePositionHolder>$.setPosition($1.getPosition());
                }

qword_list      : /* none */ {
                    $$ = new ArrayNode(lexer.getPosition());
                }
                | qword_list tSTRING_CONTENT ' ' {
                    $$ = $1.add($2);
                }

string_contents : /* none */ {
                    ByteList aChar = ByteList.create("");
                    aChar.setEncoding(lexer.getEncoding());
                    $$ = lexer.createStrNode($<Token>0.getPosition(), aChar, 0);
                }
                | string_contents string_content {
                    $$ = support.literal_concat($1.getPosition(), $1, $2);
                }

xstring_contents: /* none */ {
                    $$ = null;
                }
                | xstring_contents string_content {
                    $$ = support.literal_concat(support.getPosition($1), $1, $2);
                }

string_content  : tSTRING_CONTENT {
                    $$ = $1;
                }
                | tSTRING_DVAR {
                    $$ = lexer.getStrTerm();
                    lexer.setStrTerm(null);
                    lexer.setState(LexState.EXPR_BEG);
                } string_dvar {
                    lexer.setStrTerm($<StrTerm>2);
                    $$ = new EvStrNode($1.getPosition(), $3);
                }
                | tSTRING_DBEG {
                   $$ = lexer.getStrTerm();
                   lexer.getConditionState().stop();
                   lexer.getCmdArgumentState().stop();
                   lexer.setStrTerm(null);
                   lexer.setState(LexState.EXPR_BEG);
                } compstmt tRCURLY {
                   lexer.getConditionState().restart();
                   lexer.getCmdArgumentState().restart();
                   lexer.setStrTerm($<StrTerm>2);

                   $$ = support.newEvStrNode($1.getPosition(), $3);
                }

string_dvar     : tGVAR {
                     $$ = new GlobalVarNode($1.getPosition(), (String) $1.getValue());
                }
                | tIVAR {
                     $$ = new InstVarNode($1.getPosition(), (String) $1.getValue());
                }
                | tCVAR {
                     $$ = new ClassVarNode($1.getPosition(), (String) $1.getValue());
                }
                | backref

// Token:symbol
symbol          : tSYMBEG sym {
                     lexer.setState(LexState.EXPR_END);
                     $$ = $2;
                     $<ISourcePositionHolder>$.setPosition($1.getPosition());
                }

// Token:symbol
sym             : fname | tIVAR | tGVAR | tCVAR

dsym            : tSYMBEG xstring_contents tSTRING_END {
                     lexer.setState(LexState.EXPR_END);

                     // DStrNode: :"some text #{some expression}"
                     // StrNode: :"some text"
                     // EvStrNode :"#{some expression}"
                     // Ruby 1.9 allows empty strings as symbols
                     if ($2 == null) {
                         $$ = new SymbolNode($1.getPosition(), "");
                     } else if ($2 instanceof DStrNode) {
                         $$ = new DSymbolNode($1.getPosition(), $<DStrNode>2);
                     } else if ($2 instanceof StrNode) {
                         $$ = new SymbolNode($1.getPosition(), $<StrNode>2.getValue().toString().intern());
                     } else {
                         $$ = new DSymbolNode($1.getPosition());
                         $<DSymbolNode>$.add($2);
                     }
                }

numeric         : tINTEGER {
                    $$ = $1;
                }
                | tFLOAT {
                     $$ = $1;
                }
                | tUMINUS_NUM tINTEGER %prec tLOWEST {
                     $$ = support.negateInteger($2);
                }
                | tUMINUS_NUM tFLOAT %prec tLOWEST {
                     $$ = support.negateFloat($2);
                }

// [!null]
variable        : tIDENTIFIER | tIVAR | tGVAR | tCONSTANT | tCVAR
                | kNIL { 
                    $$ = new Token("nil", Tokens.kNIL, $1.getPosition());
                }
                | kSELF {
                    $$ = new Token("self", Tokens.kSELF, $1.getPosition());
                }
                | kTRUE { 
                    $$ = new Token("true", Tokens.kTRUE, $1.getPosition());
                }
                | kFALSE {
                    $$ = new Token("false", Tokens.kFALSE, $1.getPosition());
                }
                | k__FILE__ {
                    $$ = new Token("__FILE__", Tokens.k__FILE__, $1.getPosition());
                }
                | k__LINE__ {
                    $$ = new Token("__LINE__", Tokens.k__LINE__, $1.getPosition());
                }
                | k__ENCODING__ {
                    $$ = new Token("__ENCODING__", Tokens.k__ENCODING__, $1.getPosition());
                }

// [!null]
var_ref         : variable {
                    $$ = support.gettable($1);
                }

// [!null]
var_lhs         : variable {
                    $$ = support.assignable($1, NilImplicitNode.NIL);
                }

// [!null]
backref         : tNTH_REF {
                    $$ = $1;
                }
                | tBACK_REF {
                    $$ = $1;
                }

superclass      : term {
                    $$ = null;
                }
                | tLT {
                   lexer.setState(LexState.EXPR_BEG);
                } expr_value term {
                    $$ = $3;
                }
                | error term {
                   $$ = null;
                }

// [!null]
// ENEBO: Look at command_start stuff I am ripping out
f_arglist       : tLPAREN2 f_args rparen {
                    $$ = $2;
                    $<ISourcePositionHolder>$.setPosition($1.getPosition());
                    lexer.setState(LexState.EXPR_BEG);
                }
                | f_args term {
                    $$ = $1;
                }

// [!null]
f_args          : f_arg ',' f_optarg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, null, $6);
                }
                | f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, $7, $8);
                }
                | f_arg ',' f_optarg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, null, $4);
                }
                | f_arg ',' f_optarg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, $5, $6);
                }
                | f_arg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, null, $4);
                }
                | f_arg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, $5, $6);
                }
                | f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), $1, null, null, null, $2);
                }
                | f_optarg ',' f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, $1, $3, null, $4);
                }
                | f_optarg ',' f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, $1, $3, $5, $6);
                }
                | f_optarg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, $1, null, null, $2);
                }
                | f_optarg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, $1, null, $3, $4);
                }
                | f_rest_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, $1, null, $2);
                }
                | f_rest_arg ',' f_arg opt_f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, $1, $3, $4);
                }
                | f_block_arg {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, $1);
                }
                | /* none */ {
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, null);
                }

f_bad_arg       : tCONSTANT {
                    support.yyerror("formal argument cannot be a constant");
                }
                | tIVAR {
                    support.yyerror("formal argument cannot be an instance variable");
                }
                | tGVAR {
                    support.yyerror("formal argument cannot be a global variable");
                }
                | tCVAR {
                    support.yyerror("formal argument cannot be a class variable");
                }

// Token:f_norm_arg [!null]
f_norm_arg      : f_bad_arg
                | tIDENTIFIER {
                    $$ = support.formal_argument($1);
                }

f_arg_item      : f_norm_arg {
                    $$ = support.arg_var($1);
  /*
                    $$ = new ArgAuxiliaryNode($1.getPosition(), (String) $1.getValue(), 1);
  */
                }
                | tLPAREN f_margs rparen {
                    $$ = $2;
                    /*		    {
			ID tid = internal_id();
			arg_var(tid);
			if (dyna_in_block()) {
			    $2->nd_value = NEW_DVAR(tid);
			}
			else {
			    $2->nd_value = NEW_LVAR(tid);
			}
			$$ = NEW_ARGS_AUX(tid, 1);
			$$->nd_next = $2;*/
                }

// [!null]
f_arg           : f_arg_item {
                    $$ = new ArrayNode(lexer.getPosition(), $1);
                }
                | f_arg ',' f_arg_item {
                    $1.add($3);
                    $$ = $1;
                }

f_opt           : tIDENTIFIER '=' arg_value {
                    support.arg_var(support.formal_argument($1));
                    $$ = new OptArgNode($1.getPosition(), support.assignable($1, $3));
                }

f_block_opt     : tIDENTIFIER '=' primary_value {
                    support.arg_var(support.formal_argument($1));
                    $$ = new OptArgNode($1.getPosition(), support.assignable($1, $3));
                }

f_block_optarg  : f_block_opt {
                    $$ = new BlockNode($1.getPosition()).add($1);
                }
                | f_block_optarg ',' f_block_opt {
                    $$ = support.appendToBlock($1, $3);
                }

f_optarg        : f_opt {
                    $$ = new BlockNode($1.getPosition()).add($1);
                }
                | f_optarg ',' f_opt {
                    $$ = support.appendToBlock($1, $3);
                }

restarg_mark    : tSTAR2 | tSTAR

// [!null]
f_rest_arg      : restarg_mark tIDENTIFIER {
                    support.arg_var(support.shadowing_lvar($2));
                    $$ = support.dispatch("on_rest_param", $2);
                }
                | restarg_mark {
                    $$ = support.dispatch("on_rest_param", null);
                }

// [!null]
blkarg_mark     : tAMPER2 | tAMPER

// f_block_arg - Block argument def for function (foo(&block)) [!null]
f_block_arg     : blkarg_mark tIDENTIFIER {
                    support.arg_var(support.shadowing_lvar($2));
                    $$ = support.dispatch("on_blockarg", $2);
                }

opt_f_block_arg : ',' f_block_arg {
                    $$ = $2;
                }
                | /* none */ {
                    $$ = null;
                }

singleton       : var_ref {
                    $$ = $1;
                }
                | tLPAREN2 {
                    lexer.setState(LexState.EXPR_BEG);
                } expr rparen {
                    $$ = support.dispatch("on_paren", $3);
                }

// [!null]
assoc_list      : none
                | assocs trailer {
                    $$ = support.dispatch("on_assoclist_from_args", $1);
                }

// [!null]
assocs          : assoc
                | assocs ',' assoc {
                    $$ = $1.addAll($3);
                }

// [!null]
assoc           : arg_value tASSOC arg_value {
                    $$ = support.dispatch("on_assoc_new", $1, $3);
                }
                | tLABEL arg_value {
                    $$ = support.dispatch("on_assoc_new", $1, $2);
                }

operation       : tIDENTIFIER | tCONSTANT | tFID
operation2      : tIDENTIFIER | tCONSTANT | tFID | op
operation3      : tIDENTIFIER | tFID | op
dot_or_colon    : tDOT {
                    $$ = $1;
                }
                | tCOLON2 {
                    $$ = $1;
                }
opt_terms       : /* none */ | terms
opt_nl          : /* none */ | '\n'
rparen          : opt_nl tRPAREN {
                    $$ = $2;
                }
rbracket        : opt_nl tRBRACK {
                    $$ = $2;
                }
trailer         : /* none */ | '\n' | ','

term            : ';'
                | '\n'

terms           : term
                | terms ';'

none            : /* none */ {
                      $$ = null;
                }

none_block_pass : /* none */ {  
                  $$ = null;
                }

%%

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
