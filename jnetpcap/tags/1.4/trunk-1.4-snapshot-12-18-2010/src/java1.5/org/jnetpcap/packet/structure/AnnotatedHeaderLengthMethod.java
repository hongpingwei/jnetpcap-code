/**
 * Copyright (C) 2008 Sly Technologies, Inc. This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jnetpcap.packet.structure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.JHeader;
import org.jnetpcap.packet.annotate.Header;
import org.jnetpcap.packet.annotate.HeaderLength;
import org.jnetpcap.packet.annotate.HeaderLength.Type;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class AnnotatedHeaderLengthMethod
    extends
    AnnotatedMethod {

	private final static Map<Class<?>, AnnotatedHeaderLengthMethod[]> cache =
	    new HashMap<Class<?>, AnnotatedHeaderLengthMethod[]>();

	/**
	 * Inspect annotations within the class for length methods.
	 * 
	 * @param c
	 *          class to inspect
	 * @return array containing length methods for various header "record"
	 *         sub-structures
	 */
	public static AnnotatedHeaderLengthMethod[] inspectClass(
	    Class<? extends JHeader> c) {

		/*
		 * Check if we have this method cached for this class.
		 */
		if (cache.containsKey(c)) {
			return cache.get(c);
		}

		AnnotatedHeaderLengthMethod[] methods =
		    new AnnotatedHeaderLengthMethod[HeaderLength.Type.values().length];

		Header header = c.getAnnotation(Header.class);
		if (header != null && header.length() != -1) {
			methods[HeaderLength.Type.HEADER.ordinal()] =
			    new AnnotatedHeaderLengthMethod(c, header.length(),
			        HeaderLength.Type.HEADER);
		}

		if (header != null && header.prefix() != -1) {
			methods[HeaderLength.Type.PREFIX.ordinal()] =
			    new AnnotatedHeaderLengthMethod(c, header.prefix(),
			        HeaderLength.Type.PREFIX);
		}

		if (header != null && header.gap() != -1) {
			methods[HeaderLength.Type.GAP.ordinal()] =
			    new AnnotatedHeaderLengthMethod(c, header.gap(),
			        HeaderLength.Type.GAP);
		}

		if (header != null && header.payload() != -1) {
			methods[HeaderLength.Type.PAYLOAD.ordinal()] =
			    new AnnotatedHeaderLengthMethod(c, header.payload(),
			        HeaderLength.Type.PAYLOAD);
		}

		if (header != null && header.postfix() != -1) {
			methods[HeaderLength.Type.POSTFIX.ordinal()] =
			    new AnnotatedHeaderLengthMethod(c, header.postfix(),
			        HeaderLength.Type.POSTFIX);
		}

		for (Method method : getMethods(c, HeaderLength.class)) {

			HeaderLength hl = method.getAnnotation(HeaderLength.class);

			if (methods[hl.value().ordinal()] != null) {
				throw new AnnotatedMethodException(c, "duplicate: "
				    + methods[hl.value().ordinal()] + " property and " + method.getName()
				    + "() method");
			}

			checkSignature(method);

			methods[hl.value().ordinal()] =
			    new AnnotatedHeaderLengthMethod(method, hl.value());
		}

		if (methods[HeaderLength.Type.HEADER.ordinal()] == null) {
			throw new AnnotatedMethodException(c,
			    "@HeaderLength annotated method not found");
		}

		cache.put(c, methods);
		return methods;
	}

	private int staticLength;

	private final Type type;

	private AnnotatedHeaderLengthMethod(Method method, HeaderLength.Type type) {
		super(method);
		this.type = type;

		HeaderLength a = method.getAnnotation(HeaderLength.class);
		this.staticLength = -1;
	}

	/**
	 * @param length
	 */
	public AnnotatedHeaderLengthMethod(
	    Class<? extends JHeader> c,
	    int length,
	    HeaderLength.Type type) {
		this.staticLength = length;
		this.type = type;
	}

	public int getHeaderLength(JBuffer buffer, int offset) {

		if (this.staticLength != -1) {
			return this.staticLength;
		}

		/*
		 * Invoke the static method: <code>public static int method(JBuffer, int)</code>
		 */
		try {
			int length = (int) (Integer) this.method.invoke(null, buffer, offset);
			return length;
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new AnnotatedMethodException(declaringClass, e.getCause());
		}
	}

	public final Method getMethod() {
		return this.method;
	}

	public boolean hasStaticLength() {
		return this.staticLength != -1;
	}

	protected void validateSignature(Method method) {
		checkSignature(method);
	}

	/**
	 * @param method
	 */
	private static void checkSignature(Method method) {

		Class<?> declaringClass = method.getDeclaringClass();

		if (method.isAnnotationPresent(HeaderLength.class) == false) {
			throw new AnnotatedMethodException(declaringClass,
			    "@HeaderLength annotation missing for " + method.getName() + "()");
		}

		/*
		 * Now make sure it has the right signature of: <code>static int
		 * name(JBuffer, int)</code.
		 */
		Class<?>[] t = method.getParameterTypes();
		if (t.length != 2 || t[0] != JBuffer.class || t[1] != int.class
		    || method.getReturnType() != int.class) {

			throw new AnnotatedMethodException(declaringClass,
			    "Invalid signature for " + method.getName() + "()");
		}

		if ((method.getModifiers() & Modifier.STATIC) == 0) {
			throw new AnnotatedMethodException(declaringClass, method.getName()
			    + "()" + " must be declared static");

		}
	}

	public static void clearCache() {
		cache.clear();
	}

	public String toString() {
		if (method == null) {
			String property =
			    (type == HeaderLength.Type.HEADER) ? "length" : type.toString()
			        .toLowerCase();
			return "@Header(" + property + "=" + staticLength + ")";
		} else {
			return super.toString();
		}
	}

}
