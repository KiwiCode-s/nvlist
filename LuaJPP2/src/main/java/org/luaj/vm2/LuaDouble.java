/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import nl.weeaboo.lua2.io.LuaSerializable;

import org.luaj.vm2.lib.MathLib;

/**
 * Extension of {@link LuaNumber} which can hold a Java double as its value.
 * <p>
 * These instance are not instantiated directly by clients, but indirectly via
 * the static functions {@link LuaValue#valueOf(int)} or
 * {@link LuaValue#valueOf(double)} functions. This ensures that values which
 * can be represented as int are wrapped in {@link LuaInteger} instead of
 * {@link LuaDouble}.
 * <p>
 * Almost all API's implemented in LuaDouble are defined and documented in
 * {@link LuaValue}.
 * <p>
 * However the constants {@link #NAN}, {@link #POSINF}, {@link #NEGINF},
 * {@link #JSTR_NAN}, {@link #JSTR_POSINF}, and {@link #JSTR_NEGINF} may be
 * useful when dealing with Nan or Infinite values.
 * <p>
 * LuaDouble also defines functions for handling the unique math rules of lua
 * devision and modulo in
 * <ul>
 * <li>{@link #ddiv(double, double)}</li>
 * <li>{@link #ddiv_d(double, double)}</li>
 * <li>{@link #dmod(double, double)}</li>
 * <li>{@link #dmod_d(double, double)}</li>
 * </ul>
 * <p>
 * 
 * @see LuaValue
 * @see LuaNumber
 * @see LuaInteger
 * @see LuaValue#valueOf(int)
 * @see LuaValue#valueOf(double)
 */
@LuaSerializable
public final class LuaDouble extends LuaNumber implements Externalizable {

	private static final long serialVersionUID = 7789354519837795849L;
	
	/** Constant LuaDouble representing NaN (not a number) */
	public static final LuaDouble NAN = new LuaDouble(Double.NaN);

	/** Constant LuaDouble representing positive infinity */
	public static final LuaDouble POSINF = new LuaDouble(Double.POSITIVE_INFINITY);

	/** Constant LuaDouble representing negative infinity */
	public static final LuaDouble NEGINF = new LuaDouble(Double.NEGATIVE_INFINITY);

	/** Constant String representation for NaN (not a number), "nan" */
	public static final String JSTR_NAN = "nan";

	/** Constant String representation for positive infinity, "inf" */
	public static final String JSTR_POSINF = "inf";

	/** Constant String representation for negative infinity, "-inf" */
	public static final String JSTR_NEGINF = "-inf";

	/** The value being held by this instance. */
	private double v;

	public static LuaNumber valueOf(double d) {
		int id = (int) d;
		if (d == id) {
			//Use an integer
			return LuaInteger.valueOf(id);
		}
		
		//We have to allocate a new object
		return new LuaDouble(d);
	}

	/**
	 * Do not use. Required for efficient serialization. 
	 */
	@Deprecated
	public LuaDouble() {		
	}
	
	/** Don't allow ints to be boxed by DoubleValues */
	private LuaDouble(double d) {
		this.v = d;
	}

	protected Object readResolve() {
		// Special serialization returning the cached values
		return valueOf(v);
	}

	@Override
	public int hashCode() {
		long l = Double.doubleToLongBits(v);
		return ((int) (l >> 32)) | (int) l;
	}

	@Override
	public boolean islong() {
		return v == (long) v;
	}

	@Override
	public byte tobyte() {
		return (byte) (long) v;
	}

	@Override
	public char tochar() {
		return (char) (long) v;
	}

	@Override
	public double todouble() {
		return v;
	}

	@Override
	public float tofloat() {
		return (float) v;
	}

	@Override
	public int toint() {
		return (int) (long) v;
	}

	@Override
	public long tolong() {
		return (long) v;
	}

	@Override
	public short toshort() {
		return (short) (long) v;
	}

	@Override
	public double optdouble(double defval) {
		return v;
	}

	@Override
	public int optint(int defval) {
		return (int) (long) v;
	}

	@Override
	public LuaInteger optinteger(LuaInteger defval) {
		return LuaInteger.valueOf((int) (long) v);
	}

	@Override
	public long optlong(long defval) {
		return (long) v;
	}

	@Override
	public LuaInteger checkinteger() {
		return LuaInteger.valueOf((int) (long) v);
	}

	// unary operators
	@Override
	public LuaValue neg() {
		return valueOf(-v);
	}

	// object equality, used for key comparison
	@Override
	public boolean equals(Object o) {
		return o instanceof LuaDouble ? ((LuaDouble) o).v == v : false;
	}

	// equality w/ metatable processing
	@Override
	public LuaValue eq(LuaValue val) {
		return val.raweq(v) ? TRUE : FALSE;
	}

	@Override
	public boolean eq_b(LuaValue val) {
		return val.raweq(v);
	}

	// equality w/o metatable processing
	@Override
	public boolean raweq(LuaValue val) {
		return val.raweq(v);
	}

	@Override
	public boolean raweq(double val) {
		return v == val;
	}

	@Override
	public boolean raweq(int val) {
		return v == val;
	}

	// basic binary arithmetic
	@Override
	public LuaValue add(LuaValue rhs) {
		return rhs.add(v);
	}

	@Override
	public LuaValue add(double lhs) {
		return LuaDouble.valueOf(lhs + v);
	}

	@Override
	public LuaValue sub(LuaValue rhs) {
		return rhs.subFrom(v);
	}

	@Override
	public LuaValue sub(double rhs) {
		return LuaDouble.valueOf(v - rhs);
	}

	@Override
	public LuaValue sub(int rhs) {
		return LuaDouble.valueOf(v - rhs);
	}

	@Override
	public LuaValue subFrom(double lhs) {
		return LuaDouble.valueOf(lhs - v);
	}

	@Override
	public LuaValue mul(LuaValue rhs) {
		return rhs.mul(v);
	}

	@Override
	public LuaValue mul(double lhs) {
		return LuaDouble.valueOf(lhs * v);
	}

	@Override
	public LuaValue mul(int lhs) {
		return LuaDouble.valueOf(lhs * v);
	}

	@Override
	public LuaValue pow(LuaValue rhs) {
		return rhs.powWith(v);
	}

	@Override
	public LuaValue pow(double rhs) {
		return valueOf(MathLib.getInstance().dpow(v, rhs));
	}

	@Override
	public LuaValue pow(int rhs) {
		return valueOf(MathLib.getInstance().dpow(v, rhs));
	}

	@Override
	public LuaValue powWith(double lhs) {
		return valueOf(MathLib.getInstance().dpow(lhs, v));
	}

	@Override
	public LuaValue powWith(int lhs) {
		return valueOf(MathLib.getInstance().dpow(lhs, v));
	}

	@Override
	public LuaValue div(LuaValue rhs) {
		return rhs.divInto(v);
	}

	@Override
	public LuaValue div(double rhs) {
		return LuaDouble.ddiv(v, rhs);
	}

	@Override
	public LuaValue div(int rhs) {
		return LuaDouble.ddiv(v, rhs);
	}

	@Override
	public LuaValue divInto(double lhs) {
		return LuaDouble.ddiv(lhs, v);
	}

	@Override
	public LuaValue mod(LuaValue rhs) {
		return rhs.modFrom(v);
	}

	@Override
	public LuaValue mod(double rhs) {
		return LuaDouble.dmod(v, rhs);
	}

	@Override
	public LuaValue mod(int rhs) {
		return LuaDouble.dmod(v, rhs);
	}

	@Override
	public LuaValue modFrom(double lhs) {
		return LuaDouble.dmod(lhs, v);
	}

	/**
	 * Divide two double numbers according to lua math, and return a
	 * {@link LuaValue} result.
	 * 
	 * @param lhs Left-hand-side of the division.
	 * @param rhs Right-hand-side of the division.
	 * @return {@link LuaValue} for the result of the division, taking into
	 *         account positive and negiative infinity, and Nan
	 * @see #ddiv_d(double, double)
	 */
	public static LuaValue ddiv(double lhs, double rhs) {
		return rhs != 0 ? valueOf(lhs / rhs) : lhs > 0 ? POSINF : lhs == 0 ? NAN : NEGINF;
	}

	/**
	 * Divide two double numbers according to lua math, and return a double
	 * result.
	 * 
	 * @param lhs Left-hand-side of the division.
	 * @param rhs Right-hand-side of the division.
	 * @return Value of the division, taking into account positive and negative
	 *         infinity, and Nan
	 * @see #ddiv(double, double)
	 */
	public static double ddiv_d(double lhs, double rhs) {
		return rhs != 0 ? lhs / rhs : lhs > 0 ? Double.POSITIVE_INFINITY : lhs == 0 ? Double.NaN
				: Double.NEGATIVE_INFINITY;
	}

	/**
	 * Take modulo double numbers according to lua math, and return a
	 * {@link LuaValue} result.
	 * 
	 * @param lhs Left-hand-side of the modulo.
	 * @param rhs Right-hand-side of the modulo.
	 * @return {@link LuaValue} for the result of the modulo, using lua's rules
	 *         for modulo
	 * @see #dmod_d(double, double)
	 */
	public static LuaValue dmod(double lhs, double rhs) {
		return rhs != 0 ? valueOf(lhs - rhs * Math.floor(lhs / rhs)) : NAN;
	}

	/**
	 * Take modulo for double numbers according to lua math, and return a double
	 * result.
	 * 
	 * @param lhs Left-hand-side of the modulo.
	 * @param rhs Right-hand-side of the modulo.
	 * @return double value for the result of the modulo, using lua's rules for
	 *         modulo
	 * @see #dmod(double, double)
	 */
	public static double dmod_d(double lhs, double rhs) {
		return rhs != 0 ? lhs - rhs * Math.floor(lhs / rhs) : Double.NaN;
	}

	// relational operators
	@Override
	public LuaValue lt(LuaValue rhs) {
		return rhs.gt_b(v) ? LuaValue.TRUE : FALSE;
	}

	@Override
	public LuaValue lt(double rhs) {
		return v < rhs ? TRUE : FALSE;
	}

	@Override
	public LuaValue lt(int rhs) {
		return v < rhs ? TRUE : FALSE;
	}

	@Override
	public boolean lt_b(LuaValue rhs) {
		return rhs.gt_b(v);
	}

	@Override
	public boolean lt_b(int rhs) {
		return v < rhs;
	}

	@Override
	public boolean lt_b(double rhs) {
		return v < rhs;
	}

	@Override
	public LuaValue lteq(LuaValue rhs) {
		return rhs.gteq_b(v) ? LuaValue.TRUE : FALSE;
	}

	@Override
	public LuaValue lteq(double rhs) {
		return v <= rhs ? TRUE : FALSE;
	}

	@Override
	public LuaValue lteq(int rhs) {
		return v <= rhs ? TRUE : FALSE;
	}

	@Override
	public boolean lteq_b(LuaValue rhs) {
		return rhs.gteq_b(v);
	}

	@Override
	public boolean lteq_b(int rhs) {
		return v <= rhs;
	}

	@Override
	public boolean lteq_b(double rhs) {
		return v <= rhs;
	}

	@Override
	public LuaValue gt(LuaValue rhs) {
		return rhs.lt_b(v) ? LuaValue.TRUE : FALSE;
	}

	@Override
	public LuaValue gt(double rhs) {
		return v > rhs ? TRUE : FALSE;
	}

	@Override
	public LuaValue gt(int rhs) {
		return v > rhs ? TRUE : FALSE;
	}

	@Override
	public boolean gt_b(LuaValue rhs) {
		return rhs.lt_b(v);
	}

	@Override
	public boolean gt_b(int rhs) {
		return v > rhs;
	}

	@Override
	public boolean gt_b(double rhs) {
		return v > rhs;
	}

	@Override
	public LuaValue gteq(LuaValue rhs) {
		return rhs.lteq_b(v) ? LuaValue.TRUE : FALSE;
	}

	@Override
	public LuaValue gteq(double rhs) {
		return v >= rhs ? TRUE : FALSE;
	}

	@Override
	public LuaValue gteq(int rhs) {
		return v >= rhs ? TRUE : FALSE;
	}

	@Override
	public boolean gteq_b(LuaValue rhs) {
		return rhs.lteq_b(v);
	}

	@Override
	public boolean gteq_b(int rhs) {
		return v >= rhs;
	}

	@Override
	public boolean gteq_b(double rhs) {
		return v >= rhs;
	}

	// string comparison
	@Override
	public int strcmp(LuaString rhs) {
		typerror("attempt to compare number with string");
		return 0;
	}

	@Override
	public String tojstring() {
		/*
		 * if ( v == 0.0 ) { // never occurs in J2me long bits =
		 * Double.doubleToLongBits( v ); return ( bits >> 63 == 0 ) ? "0" :
		 * "-0"; }
		 */
		long l = (long) v;
		if (l == v) return Long.toString(l);
		if (Double.isNaN(v)) return JSTR_NAN;
		if (Double.isInfinite(v)) return (v < 0 ? JSTR_NEGINF : JSTR_POSINF);
		return Float.toString((float) v);
	}

	@Override
	public LuaString strvalue() {
		return LuaString.valueOf(tojstring());
	}

	@Override
	public LuaString optstring(LuaString defval) {
		return LuaString.valueOf(tojstring());
	}

	@Override
	public LuaValue tostring() {
		return LuaString.valueOf(tojstring());
	}

	@Override
	public String optjstring(String defval) {
		return tojstring();
	}

	@Override
	public LuaNumber optnumber(LuaNumber defval) {
		return this;
	}

	@Override
	public boolean isnumber() {
		return true;
	}

	@Override
	public boolean isstring() {
		return true;
	}

	@Override
	public LuaValue tonumber() {
		return this;
	}

	@Override
	public int checkint() {
		return (int) (long) v;
	}

	@Override
	public long checklong() {
		return (long) v;
	}

	@Override
	public LuaNumber checknumber() {
		return this;
	}

	@Override
	public double checkdouble() {
		return v;
	}

	@Override
	public String checkjstring() {
		return tojstring();
	}

	@Override
	public LuaString checkstring() {
		return LuaString.valueOf(tojstring());
	}

	@Override
	public LuaValue checkvalidkey() {
		if (Double.isNaN(v)) throw new LuaError("table index expected, got nan");
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		v = in.readDouble();
	}
}
