package org.renjin.cran.stringi;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.renjin.eval.EvalException;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;

import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.text.UnicodeSetSpanner;
import com.ibm.icu.text.UnicodeSetSpanner.TrimOption;

/**
 * Substitute implementations for C functions
 */
public class stringi {

  public static void R_init_stringi(DllInfo dll) {

    // Register all methods in this class
    for (Method method : stringi.class.getMethods()) {
      if(method.getName().startsWith("stri_")) {
        try {
          final String methodName = "C_" + method.getName();
          final MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
          final DllSymbol symbol = new DllSymbol(methodName, methodHandle, DllSymbol.Convention.CALL);
          dll.register(symbol);
        } catch (IllegalAccessException e) {
          throw new EvalException("Cannot access method '%s': %s", method.getName(), e.getMessage(), e);
        }
      }
    }

  }

  public static SEXP stri_cmp_eq(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_neq(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_lt(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_le(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_gt(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_ge(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_equiv(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_nequiv(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_count_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_count_charclass(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_count_fixed(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_count_coll(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_count_regex(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_symbols(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_fields(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_now(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_create(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6, SEXP s7, SEXP s8, SEXP s9) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_format(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_parse(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_add(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_detect_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_detect_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_detect_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_detect_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_dup(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_duplicated(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_duplicated_any(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_detect(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_detect2(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isutf8(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isutf16le(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isutf16be(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isutf32le(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isutf32be(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_isascii(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_info(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_list(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_mark(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_set(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_fromutf32(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_toascii(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_toutf8(SEXP str, SEXP is_unknown_8bit, SEXP validate) {
    final boolean strict_ascii = is_unknown_8bit.getElementAsSEXP(0).asLogical().toBooleanStrict(); 
    final int length = str.length();
    final String[] result = new String[length];
    
    for (int i = 0; i < length; i++) {
      final String element = str.getElementAsSEXP(i).asString();
      if (strict_ascii) {
        result[i] = element.replaceAll("[^\u0000-\u008F]", "\uFFFD");
      } else {
        result[i] = element;
      }
    }

    // in Java, the invalid code points would result in an exception at the time of reading the string
    // we cannot replace here invalid code points by \uFFFD or invalid strings by NA

    return new StringArrayVector(result);
  }
  public static SEXP stri_enc_toutf32(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_encode(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_endswith_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_endswith_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_endswith_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_escape_unicode(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_first_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_all_boundaries(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_first_charclass(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_charclass(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_all_charclass(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_first_coll(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_coll(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_all_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_first_fixed(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_fixed(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_all_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_first_regex(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_regex(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_all_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_flatten(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_info(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_isempty(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_join(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_join_list(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_join2(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_length(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_list2matrix(SEXP x, SEXP byrow, SEXP fill, SEXP n_min) {
    final boolean bycolumn = !byrow.getElementAsSEXP(0).asLogical().toBooleanStrict();
    final String filler = fill.getElementAsSEXP(0).asString();
    final int depth = n_min.getElementAsSEXP(0).asInt();
    final int length = x.length();
    
    if (depth < 0) {
      // TODO log error message: "argument `n_min`: expected a nonnegative numeric value"
    }
    
    int maxVectorLength = depth;
    for (int i = 0; i < length; i++) {
      maxVectorLength = Math.max(maxVectorLength, x.getElementAsSEXP(i).length());
    }
    final String[] result = new String[length * maxVectorLength];
    
    if (bycolumn) {
      int index = 0;
      for (int i = 0; i < length; i++) {
        final SEXP column = x.getElementAsSEXP(i);
        final int colLength = column.length();
        int j = 0;
        for (;j < colLength; ++j) {
          result[index++] = column.getElementAsSEXP(j).asString();
        }
        for (;j < maxVectorLength; ++j) {
          result[index++] = filler;
        }
      }
      StringVector matrix = new StringArrayVector(result);
      matrix.setAttribute(Symbols.DIM, new IntArrayVector(maxVectorLength, length));
      return matrix;
    } else {
      for (int i = 0; i < length; i++) {
        final SEXP column = x.getElementAsSEXP(i);
        final int colLength = column.length();
        int j = 0;
        for (;j < colLength; ++j) {
          result[i + j * length] = column.getElementAsSEXP(j).asString();
        }
        for (;j < maxVectorLength; ++j) {
          result[i + j * length] = filler;
        }
      }
      StringVector matrix = new StringArrayVector(result);
      matrix.setAttribute(Symbols.DIM, new IntArrayVector(length, maxVectorLength));
      return matrix;
    }
  }
  public static SEXP stri_locale_info(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_locale_list(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_locale_set(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_all_boundaries(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_first_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_first_charclass(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_charclass(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_all_charclass(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_fixed(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_first_fixed(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_all_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_coll(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_first_coll(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_all_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_all_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_first_regex(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_regex(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_match_first_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_match_last_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_match_all_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_numbytes(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_order(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_sort(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_pad(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_string(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_POSIXct(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_double(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_integer(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_logical(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_raw(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_string_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_double_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_integer_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_logical_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_rand_shuffle(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_rand_strings(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_na(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_all_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_first_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_last_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_all_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_first_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_last_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_all_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_first_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_last_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_all_charclass(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_first_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_replace_last_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_reverse(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_split_boundaries(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_split_charclass(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify) {
    final boolean only_tokens = tokens_only.getElementAsSEXP(0).asLogical().toBooleanStrict();
    final int length = __max_length(str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final ExpressionVector strings = __ensure_length(length, str);
    final ExpressionVector patterns = __ensure_length(length, pattern);
    final ExpressionVector ns = __ensure_length(length, n);
    final ExpressionVector omits = __ensure_length(length, omit_empty);

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        final boolean omit = omits.getElementAsLogical(i).toBooleanStrict();
        int depth = ns.getElementAsInt(i);
        if (depth == 0) {
          result[i] = StringVector.EMPTY;
        } else {
          if (depth >= Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("argument `" + depth + "`: value too large");
          } else if (depth < 0) {
            depth = Integer.MAX_VALUE;
          } else if (only_tokens) {
            depth++; // we need to do one split ahead here
          }
          final LinkedList<String> fields = new LinkedList<String>();
          final String element = strings.getElementAsString(i);
          final String separatorPattern = __normalize_pattern(patterns.getElementAsString(i));
          if (!separatorPattern.equals(lastPattern)) {
            lastPattern = separatorPattern;
            matcher = new UnicodeSet(separatorPattern);
          }
          int previousStart = 0;
          for (int k = 0; previousStart < element.length() && k < depth;) {
            final int beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
            final int endIndex = matcher.span(element, beginIndex, UnicodeSet.SpanCondition.CONTAINED);
            if (omit && previousStart == beginIndex) {
              // don't start any new field
            } else { // start a new field here
              final String field = element.substring(previousStart, beginIndex);
              fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
              ++k; // another field
            }
            previousStart = endIndex;
          }
          if (!(omit && previousStart == element.length())) {
            final String field = element.substring(previousStart, element.length());
            fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
          }
          if (only_tokens && depth < Integer.MAX_VALUE) {
            depth--; // one split ahead could have been made, see above
            while (fields.size() > depth) {
              fields.removeLast(); // get rid of the remainder
            }
          }
          result[i] = new StringArrayVector(fields);
        }
      }
    }

    final ExpressionVector resultSexp = new ExpressionVector(result);
    return __simplify_when_required(resultSexp, simplify, n);
  }
  public static SEXP stri_split_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6, SEXP s7) { throw new EvalException("TODO"); }
  public static SEXP stri_split_fixed(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_fixed) {
    final boolean only_tokens = tokens_only.getElementAsSEXP(0).asLogical().toBooleanStrict();
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = flags == Pattern.CASE_INSENSITIVE;
    final int length = __max_length(str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final ExpressionVector strings = __ensure_length(length, str);
    final ExpressionVector patterns = __ensure_length(length, pattern);
    final ExpressionVector ns = __ensure_length(length, n);
    final ExpressionVector omits = __ensure_length(length, omit_empty);

    for (int i = 0; i < length; i++) {
      if (ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
          if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
            // TODO log warning message: "empty search patterns are not supported"
          }
          result[i] = StringVector.valueOf(StringVector.NA);
        } else {
          int depth = ns.getElementAsInt(i);
          final boolean omit = omits.getElementAsLogical(i).toBooleanStrict();
          final String element = strings.getElementAsString(i);
          if (element.length() <= 0) {
            if (omits.isElementNA(i)) {
              result[i] = StringVector.valueOf(StringVector.NA);
            } else if (omit || depth == 0) {
              result[i] = StringVector.EMPTY;
            } else {
              result[i] = StringVector.valueOf("");
            }
          } else {
            if (depth == 0) {
              result[i] = StringVector.EMPTY;
            } else {
              if (depth >= Integer.MAX_VALUE - 1) {
                throw new IllegalArgumentException("argument `" + depth + "`: value too large");
              } else if (depth < 0) {
                depth = Integer.MAX_VALUE;
              } else if (only_tokens) {
                depth++; // we need to do one split ahead here
              }
              final LinkedList<String> fields = new LinkedList<String>();
              final String separatorPattern = patterns.getElementAsString(i);
              final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
              final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
              int previousStart = 0;
              final int patternLength = separatorPattern.length();
              for (int k = 0; previousStart < element.length() && k < depth;) {
                final int beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
                if (-1 < beginIndex) {
                  if (omit && previousStart == beginIndex) {
                    // don't start any new field
                  } else { // start a new field here
                    final String field = element.substring(previousStart, beginIndex);
                    fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
                    ++k; // another field
                  }
                  previousStart = beginIndex + patternLength;
                } else { // start a new field here
                  final String field = element.substring(previousStart);
                  fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
                  ++k; // another field
                  previousStart = element.length();
                }
              }
              if (!(omit && previousStart == element.length())) {
                final String field = element.substring(previousStart, element.length());
                fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
              }
              if (only_tokens && depth < Integer.MAX_VALUE) {
                depth--; // one split ahead could have been made, see above
                while (fields.size() > depth) {
                  fields.removeLast(); // get rid of the remainder
                }
              }
              result[i] = new StringArrayVector(fields);
            }
          }
        }
      }
    }

    final ExpressionVector resultSexp = new ExpressionVector(result);
    return __simplify_when_required(resultSexp, simplify, n);
  }
  public static SEXP stri_split_lines(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_split_lines1(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_split_regex(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_regex) {
    final boolean only_tokens = tokens_only.getElementAsSEXP(0).asLogical().toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __max_length(str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final ExpressionVector strings = __ensure_length(length, str);
    final ExpressionVector patterns = __ensure_length(length, pattern);
    final ExpressionVector ns = __ensure_length(length, n);
    final ExpressionVector omits = __ensure_length(length, omit_empty);

    for (int i = 0; i < length; i++) {
      if (ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
          if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
            // TODO log warning message: "empty search patterns are not supported"
          }
          result[i] = StringVector.valueOf(StringVector.NA);
        } else {
          int depth = ns.getElementAsInt(i);
          final boolean omit = omits.getElementAsLogical(i).toBooleanStrict();
          final String element = strings.getElementAsString(i);
          if (element.length() <= 0) {
            if (omits.isElementNA(i)) {
              result[i] = StringVector.valueOf(StringVector.NA);
            } else if (omit || depth == 0) {
              result[i] = StringVector.EMPTY;
            } else {
              result[i] = StringVector.valueOf("");
            }
          } else {
            if (depth == 0) {
              result[i] = StringVector.EMPTY;
            } else {
              if (depth >= Integer.MAX_VALUE - 1) {
                throw new IllegalArgumentException("argument `" + depth + "`: value too large");
              } else if (depth < 0) {
                depth = Integer.MAX_VALUE;
              } else if (only_tokens) {
                depth++; // we need to do one split ahead here
              }
              final LinkedList<String> fields = new LinkedList<String>();
              final Matcher matcher = Pattern.compile(__normalize_pattern(patterns.getElementAsString(i)), flags).matcher(element);
              int previousStart = 0;
              for (int k = 0; k < depth && matcher.find();) {
                final int beginIndex = matcher.start();
                final int endIndex = matcher.end();
                if (omit && previousStart == beginIndex) {
                  // don't start any new field
                } else { // start a new field here
                  final String field = element.substring(previousStart, beginIndex);
                  fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
                  ++k; // another field
                }
                previousStart = endIndex;
              }
              if (!(omit && previousStart == element.length())) {
                final String field = element.substring(previousStart, element.length());
                fields.add(("".equals(field) && omits.isElementNA(i)) ? StringVector.NA : field);
              }
              if (only_tokens && depth < Integer.MAX_VALUE) {
                depth--; // one split ahead could have been made, see above
                while (fields.size() > depth) {
                  fields.removeLast(); // get rid of the remainder
                }
              }
              result[i] = new StringArrayVector(fields);
            }
          }
        }
      }
    }

    final ExpressionVector resultSexp = new ExpressionVector(result);
    return __simplify_when_required(resultSexp, simplify, n);
  }
  public static SEXP stri_startswith_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_startswith_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_startswith_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_stats_general(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_stats_latex(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_sub(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_sub_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_charclass(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_charclass_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_coll_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_fixed_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_regex_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_test_Rmark(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_returnasis(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer16(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer16b(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer8(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_list(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_set(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_info(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_char(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_isnfc(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_isnfd(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_isnfkc(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_isnfkd(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_isnfkc_casefold(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_general(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_list(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfc(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfd(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfkc(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfkd(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfkc_casefold(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_totitle(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_tolower(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_toupper(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_trim_both(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.BOTH);
  }
  public static SEXP stri_trim_left(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.LEADING);
  }
  public static SEXP stri_trim_right(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.TRAILING);
  }
  public static SEXP stri_unescape_unicode(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_unique(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_width(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_wrap(SEXP s1) { throw new EvalException("TODO"); }

  private static int __max_length(SEXP argument, SEXP... arguments) {
    int length = argument.length();
    for (SEXP arg: arguments) {
    	  length = Math.max(length, arg.length());
    }
    return length;
  }
  private static ExpressionVector __ensure_length(int length, SEXP exp) {
    final int expLength = exp.length();
    if (length == expLength && exp instanceof ExpressionVector) {
      return (ExpressionVector) exp;
    }
    final SEXP[] result = new SEXP[length];
    for (int i = 0; i < length; i++) {
      result[i] = exp.getElementAsSEXP(i % expLength);
    }
    return new ExpressionVector(result);
  }
  private static SEXP __trim_left_right(SEXP str, SEXP pattern, TrimOption side) {
    final int length = __max_length(str, pattern);
    final String[] result = new String[length];
    final ExpressionVector strings = __ensure_length(length, str);
    final ExpressionVector patterns = __ensure_length(length, pattern);

    String lastPattern = null;
    UnicodeSetSpanner matcher = null;
    for (int i = 0; i < length; i++) {
    	  if (strings.isElementNA(i) || patterns.isElementNA(i)) {
    	    result[i] = StringVector.NA;
    	  } else {
        final String element = strings.getElementAsString(i);
        final String preservedPattern = patterns.getElementAsString(i);
        if (!preservedPattern.equals(lastPattern)) {
          lastPattern = preservedPattern;
          matcher = new UnicodeSetSpanner(new UnicodeSet(preservedPattern));
        }
        result[i] = matcher.trim(element, side, UnicodeSet.SpanCondition.NOT_CONTAINED).toString();
    	  }
    }

    return new StringArrayVector(result);
  }
  private static int __fixed_flags(SEXP opts_fixed, boolean allow_overlap) {
    int flags = 0;

    final int narg = (opts_fixed == null) ? 0 : opts_fixed.length();
    if (0 < narg) {
      final ListVector options = (ListVector) opts_fixed;
      final StringVector names = (StringVector) opts_fixed.getAttribute(Symbols.NAMES);
      if (names == null || narg != names.length()) {
        // TODO log error message: "regexp engine config failed"
      }
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          // TODO log error message: "regexp engine config failed"
        }
        final String name = names.getElementAsString(i);
        if ("case_insensitive".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.CASE_INSENSITIVE;
          }
        } else if (allow_overlap && "overlap".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.COMMENTS;
          }
        } else {
          // TODO log warning message: "incorrect opts_fixed setting: `" + name + "`. ignoring"
        }
      }
    }

    return flags;
  }
  private static int __regex_flags(SEXP opts_regex) {
    int flags = 0;

    final int narg = (opts_regex == null) ? 0 : opts_regex.length();
    if (0 < narg) {
      final ListVector options = (ListVector) opts_regex;
      final StringVector names = (StringVector) opts_regex.getAttribute(Symbols.NAMES);
      if (names == null || narg != names.length()) {
        // TODO log error message: "regexp engine config failed"
      }
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          // TODO log error message: "regexp engine config failed"
        }
        final String name = names.getElementAsString(i);
        if ("case_insensitive".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.CASE_INSENSITIVE;
          }
        } else if ("comments".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.COMMENTS;
          }
        } else if ("dotall".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.DOTALL;
          }
        } else if ("literal".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.LITERAL;
          }
        } else if ("multiline".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.MULTILINE;
          }
        } else if ("unix_lines".equals(name)) {
          if (options.getElementAsLogical(i).toBooleanStrict()) {
            flags |= Pattern.UNIX_LINES;
          }
        } else if ("uword".equals(name)) {
          // in Java regex word boundaries always conform to the Unicode definition
        } else if ("error_on_unknown_escapes".equals(name)) {
          // in Java it is always an error to use a backslash prior to any alphabetic
          // character that does not denote an escaped construct
        } else {
          // TODO log warning message: "incorrect opts_regex setting: `" + name + "`. ignoring"
        }
      }
    }

    return flags;
  }
  private static String __normalize_pattern(String pattern) {
    return (null == pattern) ? null : pattern.replace("{WHITE_SPACE}", "{Space}");
  }
  private static SEXP __simplify_when_required(SEXP resultSexp, SEXP simplify, SEXP n) {
    final Logical first_simplify = simplify.getElementAsSEXP(0).asLogical();
    if (first_simplify.equals(Logical.FALSE)) {
      return resultSexp;
    } else { // NA or TRUE
      int required_depth = 0;
      for (int i = 0; i < n.length(); i++) {
        required_depth = Math.max(required_depth, n.getElementAsSEXP(i).asInt());
      }
      final String filler = first_simplify.equals(Logical.NA) ? StringVector.NA : "";
      return stri_list2matrix(resultSexp, LogicalVector.valueOf(true), StringVector.valueOf(filler), IntVector.valueOf(required_depth));
    }
  }
}
