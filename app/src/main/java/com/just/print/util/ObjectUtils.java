package com.just.print.util;

/**
 * Object Utils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-10-24
 */
public class ObjectUtils {

	private ObjectUtils() {
		throw new AssertionError();
	}

	/**
	 * Assert a boolean expression, throwing
	 * <code>IllegalArgumentException</code> if the test result is
	 * <code>false</code>.
	 * 
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param message
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert a boolean expression, throwing
	 * <code>IllegalArgumentException</code> if the test result is
	 * <code>false</code>.
	 * 
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0);
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @throws IllegalArgumentException
	 *             if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression) {
		isTrue(expression, "[Assertion failed] - this expression must be true");
	}

	/**
	 * Checks that the specified object reference is not {@code null}. This
	 * method is designed primarily for doing parameter validation in methods
	 * and constructors, as demonstrated below: <blockquote>
	 * 
	 * <pre>
	 * public Foo(Bar bar) {
	 * 	this.bar = Objects.requireNonNull(bar);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param obj
	 *            the object reference to check for nullity
	 * @param <T>
	 *            the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException
	 *             if {@code obj} is {@code null}
	 */
	public static <T> T requireNonNull(T obj) {
		if (obj == null)
			throw new NullPointerException();
		return obj;
	}

	/**
	 * Checks that the specified object reference is not {@code null} and throws
	 * a customized {@link NullPointerException} if it is. This method is
	 * designed primarily for doing parameter validation in methods and
	 * constructors with multiple parameters, as demonstrated below:
	 * <blockquote>
	 * 
	 * <pre>
	 * public Foo(Bar bar, Baz baz) {
	 * 	this.bar = Objects.requireNonNull(bar, &quot;bar must not be null&quot;);
	 * 	this.baz = Objects.requireNonNull(baz, &quot;baz must not be null&quot;);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param obj
	 *            the object reference to check for nullity
	 * @param message
	 *            detail message to be used in the event that a
	 *            {@code NullPointerException} is thrown
	 * @param <T>
	 *            the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException
	 *             if {@code obj} is {@code null}
	 */
	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null)
			throw new NullPointerException(message);
		return obj;
	}

	/**
	 * compare two object
	 * 
	 * @param actual
	 * @param expected
	 * @return <ul>
	 *         <li>if both are null, return true</li>
	 *         <li>return actual.{@link Object#equals(Object)}</li>
	 *         </ul>
	 */
	public static boolean isEquals(Object actual, Object expected) {
		return actual == expected
				|| (actual == null ? expected == null : actual.equals(expected));
	}

	/**
	 * null Object to empty string
	 * 
	 * <pre>
	 * nullStrToEmpty(null) = &quot;&quot;;
	 * nullStrToEmpty(&quot;&quot;) = &quot;&quot;;
	 * nullStrToEmpty(&quot;aa&quot;) = &quot;aa&quot;;
	 * </pre>
	 * 
	 * @param str
	 * @return
	 */
	public static String nullStrToEmpty(Object str) {
		return (str == null ? "" : (str instanceof String ? (String) str : str
				.toString()));
	}

	/**
	 * convert long array to Long array
	 * 
	 * @param source
	 * @return
	 */
	public static Long[] transformLongArray(long[] source) {
		Long[] destin = new Long[source.length];
		for (int i = 0; i < source.length; i++) {
			destin[i] = source[i];
		}
		return destin;
	}

	/**
	 * convert Long array to long array
	 * 
	 * @param source
	 * @return
	 */
	public static long[] transformLongArray(Long[] source) {
		long[] destin = new long[source.length];
		for (int i = 0; i < source.length; i++) {
			destin[i] = source[i];
		}
		return destin;
	}

	/**
	 * convert int array to Integer array
	 * 
	 * @param source
	 * @return
	 */
	public static Integer[] transformIntArray(int[] source) {
		Integer[] destin = new Integer[source.length];
		for (int i = 0; i < source.length; i++) {
			destin[i] = source[i];
		}
		return destin;
	}

	/**
	 * convert Integer array to int array
	 * 
	 * @param source
	 * @return
	 */
	public static int[] transformIntArray(Integer[] source) {
		int[] destin = new int[source.length];
		for (int i = 0; i < source.length; i++) {
			destin[i] = source[i];
		}
		return destin;
	}

	/**
	 * compare two object
	 * <ul>
	 * <strong>About result</strong>
	 * <li>if v1 > v2, return 1</li>
	 * <li>if v1 = v2, return 0</li>
	 * <li>if v1 < v2, return -1</li>
	 * </ul>
	 * <ul>
	 * <strong>About rule</strong>
	 * <li>if v1 is null, v2 is null, then return 0</li>
	 * <li>if v1 is null, v2 is not null, then return -1</li>
	 * <li>if v1 is not null, v2 is null, then return 1</li>
	 * <li>return v1.{@link Comparable#compareTo(Object)}</li>
	 * </ul>
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> int compare(V v1, V v2) {
		return v1 == null ? (v2 == null ? 0 : -1) : (v2 == null ? 1
				: ((Comparable) v1).compareTo(v2));
	}
}
