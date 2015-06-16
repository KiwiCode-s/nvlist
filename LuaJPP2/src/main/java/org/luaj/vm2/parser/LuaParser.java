/* Generated By:JavaCC: Do not edit this line. LuaParser.java */
package org.luaj.vm2.parser;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Block;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.Exp;
import org.luaj.vm2.ast.FuncArgs;
import org.luaj.vm2.ast.FuncBody;
import org.luaj.vm2.ast.FuncName;
import org.luaj.vm2.ast.Name;
import org.luaj.vm2.ast.ParList;
import org.luaj.vm2.ast.Stat;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.ast.TableConstructor;
import org.luaj.vm2.ast.TableField;

public class LuaParser implements LuaParserConstants {

	public static void main(String args[]) throws ParseException {
		LuaParser parser = new LuaParser(System.in);
		parser.Chunk();
	}

	private static Exp.VarExp assertvarexp(Exp.PrimaryExp pe) throws ParseException {
		if (!pe.isvarexp()) throw new ParseException("expected variable");
		return (Exp.VarExp) pe;
	}

	private static Exp.FuncCall assertfunccall(Exp.PrimaryExp pe) throws ParseException {
		if (!pe.isfunccall()) throw new ParseException("expected function call");
		return (Exp.FuncCall) pe;
	}

	/** Root production. */
	final public Chunk Chunk() throws ParseException {
		Block b;
		b = Block();
		jj_consume_token(0);
		return new Chunk(b);
	}

	final public Block Block() throws ParseException {
		Block b = new Block();
		Stat s;
		label_1: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case DO:
			case FOR:
			case FUNCTION:
			case IF:
			case LOCAL:
			case REPEAT:
			case WHILE:
			case NAME:
			case 69:
				
				break;
			default:
				break label_1;
			}
			s = Stat();
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 64:
				jj_consume_token(64);
				break;
			default:
				
			}
			b.add(s);
		}
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case BREAK:
		case RETURN:
			s = LastStat();
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 64:
				jj_consume_token(64);
				break;
			default:
				
			}
			b.add(s);
			break;
		default:
			
		}
		return b;
	}

	final public Stat Stat() throws ParseException {
		Block b;
		Exp e, e2, e3 = null;
		Stat s;
		FuncName fn;
		FuncBody fb;
		Token n;
		List<Name> nl;
		List<Exp> el = null;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case DO:
			jj_consume_token(DO);
			b = Block();
			jj_consume_token(END);
			return Stat.block(b);
		case WHILE:
			jj_consume_token(WHILE);
			e = Exp();
			jj_consume_token(DO);
			b = Block();
			jj_consume_token(END);
			return Stat.whiledo(e, b);
		case REPEAT:
			jj_consume_token(REPEAT);
			b = Block();
			jj_consume_token(UNTIL);
			e = Exp();
			return Stat.repeatuntil(b, e);
		case IF:
			s = IfThenElse();
			return s;
		default:
			if (jj_2_1(3)) {
				jj_consume_token(FOR);
				n = jj_consume_token(NAME);
				jj_consume_token(65);
				e = Exp();
				jj_consume_token(66);
				e2 = Exp();
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case 66:
					jj_consume_token(66);
					e3 = Exp();
					break;
				default:
					
				}
				jj_consume_token(DO);
				b = Block();
				jj_consume_token(END);
				return Stat.fornumeric(n.image, e, e2, e3, b);
			} else {
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case FOR:
					jj_consume_token(FOR);
					nl = NameList();
					jj_consume_token(IN);
					el = ExpList();
					jj_consume_token(DO);
					b = Block();
					jj_consume_token(END);
					return Stat.forgeneric(nl, el, b);
				case FUNCTION:
					jj_consume_token(FUNCTION);
					fn = FuncName();
					fb = FuncBody();
					return Stat.functiondef(fn, fb);
				default:
					if (jj_2_2(2)) {
						jj_consume_token(LOCAL);
						jj_consume_token(FUNCTION);
						n = jj_consume_token(NAME);
						fb = FuncBody();
						return Stat.localfunctiondef(n.image, fb);
					} else {
						switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
						case LOCAL:
							jj_consume_token(LOCAL);
							nl = NameList();
							switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
							case 65:
								jj_consume_token(65);
								el = ExpList();
								break;
							default:
								
							}
							return Stat.localassignment(nl, el);
						case NAME:
						case 69:
							s = ExprStat();
							return s;
						default:
							jj_consume_token(-1);
							throw new ParseException();
						}
					}
				}
			}
		}
	}

	final public Stat IfThenElse() throws ParseException {
		Block b, b2, b3 = null;
		Exp e, e2;
		List<Exp> el = null;
		List<Block> bl = null;
		jj_consume_token(IF);
		e = Exp();
		jj_consume_token(THEN);
		b = Block();
		label_2: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case ELSEIF:
				
				break;
			default:
				break label_2;
			}
			jj_consume_token(ELSEIF);
			e2 = Exp();
			jj_consume_token(THEN);
			b2 = Block();
			if (el == null) el = new ArrayList<Exp>();
			if (bl == null) bl = new ArrayList<Block>();
			el.add(e2);
			bl.add(b2);
		}
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case ELSE:
			jj_consume_token(ELSE);
			b3 = Block();
			break;
		default:
			
		}
		jj_consume_token(END);
		return Stat.ifthenelse(e, b, el, bl, b3);
	}

	final public Stat LastStat() throws ParseException {
		List<Exp> el = null;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case BREAK:
			jj_consume_token(BREAK);
			return Stat.breakstat();
		case RETURN:
			jj_consume_token(RETURN);
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case LONGSTRING0:
			case LONGSTRING1:
			case LONGSTRING2:
			case LONGSTRING3:
			case LONGSTRINGN:
			case FALSE:
			case FUNCTION:
			case NIL:
			case NOT:
			case TRUE:
			case NAME:
			case NUMBER:
			case STRING:
			case CHARSTRING:
			case 69:
			case 73:
			case 74:
			case 77:
			case 89:
				el = ExpList();
				break;
			default:
				
			}
			return Stat.returnstat(el);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public Stat ExprStat() throws ParseException {
		Exp.PrimaryExp pe;
		Stat as = null;
		pe = PrimaryExp();
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 65:
		case 66:
			as = Assign(assertvarexp(pe));
			break;
		default:
			
		}
		return as == null ? Stat.functioncall(assertfunccall(pe)) : as;
	}

	final public Stat Assign(Exp.VarExp v0) throws ParseException {
		List<Exp.VarExp> vl = new ArrayList<Exp.VarExp>();
		vl.add(v0);
		Exp.VarExp ve;
		List<Exp> el;
		label_3: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 66:
				
				break;
			default:
				break label_3;
			}
			jj_consume_token(66);
			ve = VarExp();
			vl.add(ve);
		}
		jj_consume_token(65);
		el = ExpList();
		return Stat.assignment(vl, el);
	}

	final public Exp.VarExp VarExp() throws ParseException {
		Exp.PrimaryExp pe;
		pe = PrimaryExp();
		return assertvarexp(pe);
	}

	final public FuncName FuncName() throws ParseException {
		FuncName fn;
		Token n;
		n = jj_consume_token(NAME);
		fn = new FuncName(n.image);
		label_4: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 67:
				
				break;
			default:
				break label_4;
			}
			jj_consume_token(67);
			n = jj_consume_token(NAME);
			fn.adddot(n.image);
		}
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 68:
			jj_consume_token(68);
			n = jj_consume_token(NAME);
			fn.method = n.image;
			break;
		default:
			
		}
		return fn;
	}

	final public Exp.PrimaryExp PrefixExp() throws ParseException {
		Token n;
		Exp e;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case NAME:
			n = jj_consume_token(NAME);
			return Exp.nameprefix(n.image);
		case 69:
			jj_consume_token(69);
			e = Exp();
			jj_consume_token(70);
			return Exp.parensprefix(e);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public Exp.PrimaryExp PrimaryExp() throws ParseException {
		Exp.PrimaryExp pe;
		pe = PrefixExp();
		label_5: while (true) {
			if (jj_2_3(2)) {
				
			} else {
				break label_5;
			}
			pe = PostfixOp(pe);
		}
		return pe;
	}

	final public Exp.PrimaryExp PostfixOp(Exp.PrimaryExp lhs) throws ParseException {
		Token n;
		Exp e;
		FuncArgs a;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 67:
			jj_consume_token(67);
			n = jj_consume_token(NAME);
			return Exp.fieldop(lhs, n.image);
		case 71:
			jj_consume_token(71);
			e = Exp();
			jj_consume_token(72);
			return Exp.indexop(lhs, e);
		case 68:
			jj_consume_token(68);
			n = jj_consume_token(NAME);
			a = FuncArgs();
			return Exp.methodop(lhs, n.image, a);
		case LONGSTRING0:
		case LONGSTRING1:
		case LONGSTRING2:
		case LONGSTRING3:
		case LONGSTRINGN:
		case STRING:
		case CHARSTRING:
		case 69:
		case 74:
			a = FuncArgs();
			return Exp.functionop(lhs, a);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public FuncArgs FuncArgs() throws ParseException {
		List<Exp> el = null;
		TableConstructor tc;
		LuaString s;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 69:
			jj_consume_token(69);
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case LONGSTRING0:
			case LONGSTRING1:
			case LONGSTRING2:
			case LONGSTRING3:
			case LONGSTRINGN:
			case FALSE:
			case FUNCTION:
			case NIL:
			case NOT:
			case TRUE:
			case NAME:
			case NUMBER:
			case STRING:
			case CHARSTRING:
			case 69:
			case 73:
			case 74:
			case 77:
			case 89:
				el = ExpList();
				break;
			default:
				
			}
			jj_consume_token(70);
			return FuncArgs.explist(el);
		case 74:
			tc = TableConstructor();
			return FuncArgs.tableconstructor(tc);
		case LONGSTRING0:
		case LONGSTRING1:
		case LONGSTRING2:
		case LONGSTRING3:
		case LONGSTRINGN:
		case STRING:
		case CHARSTRING:
			s = Str();
			return FuncArgs.string(s);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public List<Name> NameList() throws ParseException {
		List<Name> nl = new ArrayList<Name>();
		Token name;
		name = jj_consume_token(NAME);
		nl.add(new Name(name.image));
		label_6: while (true) {
			if (jj_2_4(2)) {
				
			} else {
				break label_6;
			}
			jj_consume_token(66);
			name = jj_consume_token(NAME);
			nl.add(new Name(name.image));
		}
		return nl;
	}

	final public List<Exp> ExpList() throws ParseException {
		List<Exp> el = new ArrayList<Exp>();
		Exp e;
		e = Exp();
		el.add(e);
		label_7: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 66:
				
				break;
			default:
				break label_7;
			}
			jj_consume_token(66);
			e = Exp();
			el.add(e);
		}
		return el;
	}

	final public Exp SimpleExp() throws ParseException {
		Token n;
		LuaString s;
		Exp e;
		TableConstructor tc;
		FuncBody fb;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case NIL:
			jj_consume_token(NIL);
			return Exp.constant(LuaValue.NIL);
		case TRUE:
			jj_consume_token(TRUE);
			return Exp.constant(LuaValue.TRUE);
		case FALSE:
			jj_consume_token(FALSE);
			return Exp.constant(LuaValue.FALSE);
		case NUMBER:
			n = jj_consume_token(NUMBER);
			return Exp.numberconstant(n.image);
		case LONGSTRING0:
		case LONGSTRING1:
		case LONGSTRING2:
		case LONGSTRING3:
		case LONGSTRINGN:
		case STRING:
		case CHARSTRING:
			s = Str();
			return Exp.constant(s);
		case 73:
			jj_consume_token(73);
			return Exp.varargs();
		case 74:
			tc = TableConstructor();
			return Exp.tableconstructor(tc);
		case FUNCTION:
			fb = Function();
			return Exp.anonymousfunction(fb);
		case NAME:
		case 69:
			e = PrimaryExp();
			return e;
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public LuaString Str() throws ParseException {
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case STRING:
			jj_consume_token(STRING);
			return Str.quoteString(token.image);
		case CHARSTRING:
			jj_consume_token(CHARSTRING);
			return Str.charString(token.image);
		case LONGSTRING0:
			jj_consume_token(LONGSTRING0);
			return Str.longString(token.image);
		case LONGSTRING1:
			jj_consume_token(LONGSTRING1);
			return Str.longString(token.image);
		case LONGSTRING2:
			jj_consume_token(LONGSTRING2);
			return Str.longString(token.image);
		case LONGSTRING3:
			jj_consume_token(LONGSTRING3);
			return Str.longString(token.image);
		case LONGSTRINGN:
			jj_consume_token(LONGSTRINGN);
			return Str.longString(token.image);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public Exp Exp() throws ParseException {
		Exp e, s;
		int op;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case LONGSTRING0:
		case LONGSTRING1:
		case LONGSTRING2:
		case LONGSTRING3:
		case LONGSTRINGN:
		case FALSE:
		case FUNCTION:
		case NIL:
		case TRUE:
		case NAME:
		case NUMBER:
		case STRING:
		case CHARSTRING:
		case 69:
		case 73:
		case 74:
			e = SimpleExp();
			break;
		case NOT:
		case 77:
		case 89:
			op = Unop();
			s = Exp();
			e = Exp.unaryexp(op, s);
			break;
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
		label_8: while (true) {
			if (jj_2_5(2)) {
				
			} else {
				break label_8;
			}
			op = Binop();
			s = Exp();
			e = Exp.binaryexp(e, op, s);
		}
		return e;
	}

	final public FuncBody Function() throws ParseException {
		FuncBody fb;
		jj_consume_token(FUNCTION);
		fb = FuncBody();
		return fb;
	}

	final public FuncBody FuncBody() throws ParseException {
		ParList pl = null;
		Block b;
		jj_consume_token(69);
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case NAME:
		case 73:
			pl = ParList();
			break;
		default:
			
		}
		jj_consume_token(70);
		b = Block();
		jj_consume_token(END);
		return new FuncBody(pl, b);
	}

	final public ParList ParList() throws ParseException {
		List<Name> nl = null;
		boolean v = false;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case NAME:
			nl = NameList();
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case 66:
				jj_consume_token(66);
				jj_consume_token(73);
				v = true;
				break;
			default:
				
			}
			return new ParList(nl, v);
		case 73:
			jj_consume_token(73);
			return new ParList(null, true);
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public TableConstructor TableConstructor() throws ParseException {
		TableConstructor tc = new TableConstructor();
		List<TableField> fl = null;
		jj_consume_token(74);
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case LONGSTRING0:
		case LONGSTRING1:
		case LONGSTRING2:
		case LONGSTRING3:
		case LONGSTRINGN:
		case FALSE:
		case FUNCTION:
		case NIL:
		case NOT:
		case TRUE:
		case NAME:
		case NUMBER:
		case STRING:
		case CHARSTRING:
		case 69:
		case 71:
		case 73:
		case 74:
		case 77:
		case 89:
			fl = FieldList();
			tc.fields = fl;
			break;
		default:
			
		}
		jj_consume_token(75);
		return tc;
	}

	final public List<TableField> FieldList() throws ParseException {
		List<TableField> fl = new ArrayList<TableField>();
		TableField f;
		f = Field();
		fl.add(f);
		label_9: while (true) {
			if (jj_2_6(2)) {
				
			} else {
				break label_9;
			}
			FieldSep();
			f = Field();
			fl.add(f);
		}
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 64:
		case 66:
			FieldSep();
			break;
		default:
			
		}
		return fl;
	}

	final public TableField Field() throws ParseException {
		Token name;
		Exp exp, rhs;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 71:
			jj_consume_token(71);
			exp = Exp();
			jj_consume_token(72);
			jj_consume_token(65);
			rhs = Exp();
			return TableField.keyedField(exp, rhs);
		default:
			if (jj_2_7(2)) {
				name = jj_consume_token(NAME);
				jj_consume_token(65);
				rhs = Exp();
				return TableField.namedField(name.image, rhs);
			} else {
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case LONGSTRING0:
				case LONGSTRING1:
				case LONGSTRING2:
				case LONGSTRING3:
				case LONGSTRINGN:
				case FALSE:
				case FUNCTION:
				case NIL:
				case NOT:
				case TRUE:
				case NAME:
				case NUMBER:
				case STRING:
				case CHARSTRING:
				case 69:
				case 73:
				case 74:
				case 77:
				case 89:
					rhs = Exp();
					return TableField.listField(rhs);
				default:
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
		}
	}

	final public void FieldSep() throws ParseException {
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 66:
			jj_consume_token(66);
			break;
		case 64:
			jj_consume_token(64);
			break;
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public int Binop() throws ParseException {
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 76:
			jj_consume_token(76);
			return Lua.OP_ADD;
		case 77:
			jj_consume_token(77);
			return Lua.OP_SUB;
		case 78:
			jj_consume_token(78);
			return Lua.OP_MUL;
		case 79:
			jj_consume_token(79);
			return Lua.OP_DIV;
		case 80:
			jj_consume_token(80);
			return Lua.OP_POW;
		case 81:
			jj_consume_token(81);
			return Lua.OP_MOD;
		case 82:
			jj_consume_token(82);
			return Lua.OP_CONCAT;
		case 83:
			jj_consume_token(83);
			return Lua.OP_LT;
		case 84:
			jj_consume_token(84);
			return Lua.OP_LE;
		case 85:
			jj_consume_token(85);
			return Lua.OP_GT;
		case 86:
			jj_consume_token(86);
			return Lua.OP_GE;
		case 87:
			jj_consume_token(87);
			return Lua.OP_EQ;
		case 88:
			jj_consume_token(88);
			return Lua.OP_NEQ;
		case AND:
			jj_consume_token(AND);
			return Lua.OP_AND;
		case OR:
			jj_consume_token(OR);
			return Lua.OP_OR;
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	final public int Unop() throws ParseException {
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case 77:
			jj_consume_token(77);
			return Lua.OP_UNM;
		case NOT:
			jj_consume_token(NOT);
			return Lua.OP_NOT;
		case 89:
			jj_consume_token(89);
			return Lua.OP_LEN;
		default:
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	private boolean jj_2_1(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_1();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_2(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_2();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_3(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_3();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_4(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_4();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_5(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_5();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_6(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_6();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_2_7(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		try {
			return !jj_3_7();
		} catch (LookaheadSuccess ls) {
			return true;
		}
	}

	private boolean jj_3R_55() {
		if (jj_scan_token(89)) return true;
		return false;
	}

	private boolean jj_3_4() {
		if (jj_scan_token(66)) return true;
		if (jj_scan_token(NAME)) return true;
		return false;
	}

	private boolean jj_3R_54() {
		if (jj_scan_token(NOT)) return true;
		return false;
	}

	private boolean jj_3R_53() {
		if (jj_scan_token(77)) return true;
		return false;
	}

	private boolean jj_3R_40() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_53()) {
			jj_scanpos = xsp;
			if (jj_3R_54()) {
				jj_scanpos = xsp;
				if (jj_3R_55()) return true;
			}
		}
		return false;
	}

	private boolean jj_3R_69() {
		if (jj_scan_token(LONGSTRINGN)) return true;
		return false;
	}

	private boolean jj_3R_68() {
		if (jj_scan_token(LONGSTRING3)) return true;
		return false;
	}

	private boolean jj_3R_67() {
		if (jj_scan_token(LONGSTRING2)) return true;
		return false;
	}

	private boolean jj_3R_66() {
		if (jj_scan_token(LONGSTRING1)) return true;
		return false;
	}

	private boolean jj_3R_33() {
		if (jj_scan_token(OR)) return true;
		return false;
	}

	private boolean jj_3R_65() {
		if (jj_scan_token(LONGSTRING0)) return true;
		return false;
	}

	private boolean jj_3R_32() {
		if (jj_scan_token(AND)) return true;
		return false;
	}

	private boolean jj_3R_64() {
		if (jj_scan_token(CHARSTRING)) return true;
		return false;
	}

	private boolean jj_3R_31() {
		if (jj_scan_token(88)) return true;
		return false;
	}

	private boolean jj_3R_63() {
		if (jj_scan_token(STRING)) return true;
		return false;
	}

	private boolean jj_3R_58() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_63()) {
			jj_scanpos = xsp;
			if (jj_3R_64()) {
				jj_scanpos = xsp;
				if (jj_3R_65()) {
					jj_scanpos = xsp;
					if (jj_3R_66()) {
						jj_scanpos = xsp;
						if (jj_3R_67()) {
							jj_scanpos = xsp;
							if (jj_3R_68()) {
								jj_scanpos = xsp;
								if (jj_3R_69()) return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_30() {
		if (jj_scan_token(87)) return true;
		return false;
	}

	private boolean jj_3R_29() {
		if (jj_scan_token(86)) return true;
		return false;
	}

	private boolean jj_3R_28() {
		if (jj_scan_token(85)) return true;
		return false;
	}

	private boolean jj_3R_27() {
		if (jj_scan_token(84)) return true;
		return false;
	}

	private boolean jj_3R_26() {
		if (jj_scan_token(83)) return true;
		return false;
	}

	private boolean jj_3R_25() {
		if (jj_scan_token(82)) return true;
		return false;
	}

	private boolean jj_3R_52() {
		if (jj_3R_60()) return true;
		return false;
	}

	private boolean jj_3R_24() {
		if (jj_scan_token(81)) return true;
		return false;
	}

	private boolean jj_3R_51() {
		if (jj_3R_59()) return true;
		return false;
	}

	private boolean jj_3R_23() {
		if (jj_scan_token(80)) return true;
		return false;
	}

	private boolean jj_3R_50() {
		if (jj_3R_57()) return true;
		return false;
	}

	private boolean jj_3R_22() {
		if (jj_scan_token(79)) return true;
		return false;
	}

	private boolean jj_3R_49() {
		if (jj_scan_token(73)) return true;
		return false;
	}

	private boolean jj_3R_21() {
		if (jj_scan_token(78)) return true;
		return false;
	}

	private boolean jj_3R_48() {
		if (jj_3R_58()) return true;
		return false;
	}

	private boolean jj_3_6() {
		if (jj_3R_13()) return true;
		if (jj_3R_14()) return true;
		return false;
	}

	private boolean jj_3R_20() {
		if (jj_scan_token(77)) return true;
		return false;
	}

	private boolean jj_3R_47() {
		if (jj_scan_token(NUMBER)) return true;
		return false;
	}

	private boolean jj_3R_19() {
		if (jj_scan_token(76)) return true;
		return false;
	}

	private boolean jj_3R_11() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_19()) {
			jj_scanpos = xsp;
			if (jj_3R_20()) {
				jj_scanpos = xsp;
				if (jj_3R_21()) {
					jj_scanpos = xsp;
					if (jj_3R_22()) {
						jj_scanpos = xsp;
						if (jj_3R_23()) {
							jj_scanpos = xsp;
							if (jj_3R_24()) {
								jj_scanpos = xsp;
								if (jj_3R_25()) {
									jj_scanpos = xsp;
									if (jj_3R_26()) {
										jj_scanpos = xsp;
										if (jj_3R_27()) {
											jj_scanpos = xsp;
											if (jj_3R_28()) {
												jj_scanpos = xsp;
												if (jj_3R_29()) {
													jj_scanpos = xsp;
													if (jj_3R_30()) {
														jj_scanpos = xsp;
														if (jj_3R_31()) {
															jj_scanpos = xsp;
															if (jj_3R_32()) {
																jj_scanpos = xsp;
																if (jj_3R_33()) return true;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_46() {
		if (jj_scan_token(FALSE)) return true;
		return false;
	}

	private boolean jj_3R_45() {
		if (jj_scan_token(TRUE)) return true;
		return false;
	}

	private boolean jj_3R_44() {
		if (jj_scan_token(NIL)) return true;
		return false;
	}

	private boolean jj_3R_39() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_44()) {
			jj_scanpos = xsp;
			if (jj_3R_45()) {
				jj_scanpos = xsp;
				if (jj_3R_46()) {
					jj_scanpos = xsp;
					if (jj_3R_47()) {
						jj_scanpos = xsp;
						if (jj_3R_48()) {
							jj_scanpos = xsp;
							if (jj_3R_49()) {
								jj_scanpos = xsp;
								if (jj_3R_50()) {
									jj_scanpos = xsp;
									if (jj_3R_51()) {
										jj_scanpos = xsp;
										if (jj_3R_52()) return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_13() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(66)) {
			jj_scanpos = xsp;
			if (jj_scan_token(64)) return true;
		}
		return false;
	}

	private boolean jj_3R_37() {
		if (jj_3R_12()) return true;
		return false;
	}

	private boolean jj_3_7() {
		if (jj_scan_token(NAME)) return true;
		if (jj_scan_token(65)) return true;
		return false;
	}

	private boolean jj_3R_14() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_36()) {
			jj_scanpos = xsp;
			if (jj_3_7()) {
				jj_scanpos = xsp;
				if (jj_3R_37()) return true;
			}
		}
		return false;
	}

	private boolean jj_3R_36() {
		if (jj_scan_token(71)) return true;
		return false;
	}

	private boolean jj_3R_61() {
		if (jj_3R_12()) return true;
		return false;
	}

	private boolean jj_3R_71() {
		if (jj_3R_14()) return true;
		return false;
	}

	private boolean jj_3R_62() {
		if (jj_3R_71()) return true;
		return false;
	}

	private boolean jj_3R_56() {
		if (jj_3R_61()) return true;
		return false;
	}

	private boolean jj_3R_57() {
		if (jj_scan_token(74)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_62()) jj_scanpos = xsp;
		if (jj_scan_token(75)) return true;
		return false;
	}

	private boolean jj_3R_43() {
		if (jj_3R_58()) return true;
		return false;
	}

	private boolean jj_3R_42() {
		if (jj_3R_57()) return true;
		return false;
	}

	private boolean jj_3R_41() {
		if (jj_scan_token(69)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_56()) jj_scanpos = xsp;
		if (jj_scan_token(70)) return true;
		return false;
	}

	private boolean jj_3R_38() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_41()) {
			jj_scanpos = xsp;
			if (jj_3R_42()) {
				jj_scanpos = xsp;
				if (jj_3R_43()) return true;
			}
		}
		return false;
	}

	private boolean jj_3_3() {
		if (jj_3R_10()) return true;
		return false;
	}

	private boolean jj_3R_18() {
		if (jj_3R_38()) return true;
		return false;
	}

	private boolean jj_3R_17() {
		if (jj_scan_token(68)) return true;
		if (jj_scan_token(NAME)) return true;
		return false;
	}

	private boolean jj_3R_16() {
		if (jj_scan_token(71)) return true;
		if (jj_3R_12()) return true;
		return false;
	}

	private boolean jj_3R_15() {
		if (jj_scan_token(67)) return true;
		if (jj_scan_token(NAME)) return true;
		return false;
	}

	private boolean jj_3R_10() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_15()) {
			jj_scanpos = xsp;
			if (jj_3R_16()) {
				jj_scanpos = xsp;
				if (jj_3R_17()) {
					jj_scanpos = xsp;
					if (jj_3R_18()) return true;
				}
			}
		}
		return false;
	}

	private boolean jj_3R_35() {
		if (jj_3R_40()) return true;
		return false;
	}

	private boolean jj_3_2() {
		if (jj_scan_token(LOCAL)) return true;
		if (jj_scan_token(FUNCTION)) return true;
		return false;
	}

	private boolean jj_3_1() {
		if (jj_scan_token(FOR)) return true;
		if (jj_scan_token(NAME)) return true;
		if (jj_scan_token(65)) return true;
		return false;
	}

	private boolean jj_3R_60() {
		if (jj_3R_70()) return true;
		return false;
	}

	private boolean jj_3_5() {
		if (jj_3R_11()) return true;
		if (jj_3R_12()) return true;
		return false;
	}

	private boolean jj_3R_59() {
		if (jj_scan_token(FUNCTION)) return true;
		return false;
	}

	private boolean jj_3R_73() {
		if (jj_scan_token(69)) return true;
		return false;
	}

	private boolean jj_3R_72() {
		if (jj_scan_token(NAME)) return true;
		return false;
	}

	private boolean jj_3R_70() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_72()) {
			jj_scanpos = xsp;
			if (jj_3R_73()) return true;
		}
		return false;
	}

	private boolean jj_3R_34() {
		if (jj_3R_39()) return true;
		return false;
	}

	private boolean jj_3R_12() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_34()) {
			jj_scanpos = xsp;
			if (jj_3R_35()) return true;
		}
		return false;
	}

	/** Generated Token Manager. */
	public LuaParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	/** Current token. */
	public Token token;
	/** Next token. */
	public Token jj_nt;
	private int jj_ntk;
	private Token jj_scanpos, jj_lastpos;
	private int jj_la;

	/** Constructor with InputStream. */
	public LuaParser(java.io.InputStream stream) {
		this(stream, null);
	}

	/** Constructor with InputStream and supplied encoding */
	public LuaParser(java.io.InputStream stream, String encoding) {
		try {
			jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
		token_source = new LuaParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream) {
		ReInit(stream, null);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream, String encoding) {
		try {
			jj_input_stream.ReInit(stream, encoding, 1, 1);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
	}

	/** Constructor. */
	public LuaParser(java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new LuaParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
	}

	/** Constructor with generated Token Manager. */
	public LuaParser(LuaParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
	}

	/** Reinitialise. */
	public void ReInit(LuaParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
	}

	private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			return token;
		}
		token = oldToken;
		throw generateParseException();
	}

	static private final class LookaheadSuccess extends RuntimeException {

		private static final long serialVersionUID = -7367405402612591505L;
		
	}

	final private LookaheadSuccess jj_ls = new LookaheadSuccess();

	private boolean jj_scan_token(int kind) {
		if (jj_scanpos == jj_lastpos) {
			jj_la--;
			if (jj_scanpos.next == null) {
				jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
			} else {
				jj_lastpos = jj_scanpos = jj_scanpos.next;
			}
		} else {
			jj_scanpos = jj_scanpos.next;
		}
		if (jj_scanpos.kind != kind) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
		return false;
	}

	/** Get the next Token. */
	final public Token getNextToken() {
		if (token.next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		return token;
	}

	/** Get the specific Token. */
	final public Token getToken(int index) {
		Token t = token;
		for (int i = 0; i < index; i++) {
			if (t.next != null) t = t.next;
			else t = t.next = token_source.getNextToken();
		}
		return t;
	}

	private int jj_ntk() {
		if ((jj_nt = token.next) == null) return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else return (jj_ntk = jj_nt.kind);
	}

	/** Generate ParseException. */
	public ParseException generateParseException() {
		Token errortok = token.next;
		int line = errortok.beginLine, column = errortok.beginColumn;
		String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;
		return new ParseException("Parse error at line " + line + ", column " + column + ".  Encountered: "
				+ mess);
	}

	/** Enable tracing. */
	final public void enable_tracing() {
	}

	/** Disable tracing. */
	final public void disable_tracing() {
	}

}
