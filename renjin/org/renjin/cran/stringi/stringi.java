package org.renjin.cran.stringi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.renjin.eval.EvalException;
import org.renjin.primitives.Native;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.RecyclingIntVector;
import org.renjin.sexp.RecyclingLogicalVector;
import org.renjin.sexp.RecyclingStringVector;
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
    // in Java, the invalid code points would result in an exception at the time of reading the string
    // we cannot replace here invalid code points by \uFFFD or invalid strings by NA

    final boolean strict_ascii = ((AtomicVector) is_unknown_8bit).getElementAsLogical(0).toBooleanStrict();
    if (strict_ascii) {
      final int length = str.length();
      final String[] result = new String[length];

      for (int i = 0; i < length; i++) {
        final String element = ((AtomicVector) str).getElementAsString(i);
        result[i] = element.replaceAll("[^\u0000-\u007F]", "\uFFFD");
      }

      return new StringArrayVector(result);
    } else {
      return str;
    }
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
  public static SEXP stri_join2(SEXP s1, SEXP s2) {
    if (s1.length() <= 0) {
      return s1;
    }
    if (s2.length() <= 0) {
      return s2;
    }

    final int length = __recycling_rule(true, s1, s2);
    final StringVector e1 = __ensure_length(length, __prepare_string(s1));
    final StringVector e2 = __ensure_length(length, __prepare_string(s2));
    final String[] result = new String[length];

    for (int i = 0; i < length; i++) {
      if (e1.isElementNA(i) || e2.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        result[i] = e1.getElementAsString(i) + e2.getElementAsString(i);
      }
    }

    return new StringArrayVector(result);
  }
  public static SEXP stri_length(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_list2matrix(SEXP x, SEXP byrow, SEXP fill, SEXP n_min) {
    final boolean bycolumn = !((AtomicVector) byrow).getElementAsLogical(0).toBooleanStrict();
    final String filler = ((AtomicVector) fill).getElementAsString(0);
    final int depth = ((AtomicVector) n_min).getElementAsInt(0);
    final int length = x.length();

    if (depth < 0) {
      throw new EvalException("argument `n_min`: expected a nonnegative numeric value");
    }

    int maxVectorLength = depth;
    for (int i = 0; i < length; i++) {
      maxVectorLength = Math.max(maxVectorLength, x.getElementAsSEXP(i).length());
    }
    final String[] result = new String[length * maxVectorLength];

    if (bycolumn) {
      int index = 0;
      for (int i = 0; i < length; i++) {
        final AtomicVector column = x.getElementAsSEXP(i);
        final int colLength = column.length();
        int j = 0;
        for (;j < colLength; ++j) {
          result[index++] = column.getElementAsString(j);
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
        final AtomicVector column = x.getElementAsSEXP(i);
        final int colLength = column.length();
        int j = 0;
        for (;j < colLength; ++j) {
          result[i + j * length] = column.getElementAsString(j);
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
  public static SEXP stri_replace_all_fixed(SEXP str, SEXP pattern, SEXP replacement, SEXP vectorize_all, SEXP opts_fixed) {
    final boolean is_vectorized = ((AtomicVector) vectorize_all).getElementAsLogical(0).toBooleanStrict();
    if (is_vectorized) {
      return __replace_all_fixed_vectorized(str, pattern, replacement, opts_fixed, ReplaceType.ALL);
    } else {
      final int length = str.length();
      if (length <= 0) {
        return StringVector.EMPTY;
      } else {
        final int pattern_n = pattern.length();
        final int replacement_n = replacement.length();
        if (pattern_n < replacement_n || pattern_n <= 0 || replacement_n <= 0) {
          throw new EvalException("vector length not consistent with other arguments");
        }
        if (pattern_n % replacement_n != 0) {
          Native.currentContext().warn("longer object length is not a multiple of shorter object length");
        }
        if (pattern_n == 1) {// this will be much faster:
          return __replace_all_fixed_vectorized(str, pattern, replacement, opts_fixed, ReplaceType.ALL);
        }
        final int flags = __fixed_flags(opts_fixed, false);
        final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
        final String[] result = new String[length];
        final StringVector strings = __prepare_string(str);
        final StringVector patterns = __prepare_string(pattern);
        final StringVector replacements = __ensure_length(pattern_n, __prepare_string(replacement));
        for (int i = 0; i < pattern_n; i++) {
          if (patterns.isElementNA(i)) {
            return __string_vector_NA(length);
          } else if (patterns.getElementAsString(i).length() <= 0) {
            Native.currentContext().warn("empty search patterns are not supported");
            return __string_vector_NA(length);
          }
          for (int j = 0; j < length; j++) {
            if (strings.isElementNA(j)) {
              result[j] = StringVector.NA;
            } else {
              final String element = strings.getElementAsString(j);
              final String separatorPattern = patterns.getElementAsString(i);
              final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
              final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
              if (replacements.isElementNA(i)) {
                if (-1 < elementNormalized.indexOf(patternNormalized)) {
                  result[j] = StringVector.NA;
                } else {
                  result[j] = element;
                }
              } else {
                final String replacement_i = replacements.getElementAsString(i);
                final int patternLength = separatorPattern.length();
                final StringBuffer replaced = new StringBuffer();
                int previousStart = 0;
                int beginIndex = elementNormalized.indexOf(patternNormalized);
                while (beginIndex != -1) {
                  replaced.append(element.substring(previousStart, beginIndex));
                  replaced.append(replacement_i);
                  previousStart = beginIndex + patternLength;
                  beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
                }
                replaced.append(element.substring(previousStart));
                result[j] = replaced.toString();
              }
            }
          }
        }
        return new StringArrayVector(result);
      }
    }
  }
  public static SEXP stri_replace_first_fixed(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_fixed) {
    return __replace_all_fixed_vectorized(str, pattern, replacement, opts_fixed, ReplaceType.FIRST);
  }
  public static SEXP stri_replace_last_fixed(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_fixed) {
    return __replace_all_fixed_vectorized(str, pattern, replacement, opts_fixed, ReplaceType.LAST);
  }
  public static SEXP stri_replace_all_coll(SEXP str, SEXP pattern, SEXP replacement, SEXP vectorize_all, SEXP opts_collator) {
    throw new EvalException("TODO");
  }
  public static SEXP stri_replace_first_coll(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_collator) {
    throw new EvalException("TODO");
  }
  public static SEXP stri_replace_last_coll(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_collator) {
    throw new EvalException("TODO");
  }
  public static SEXP stri_replace_all_regex(SEXP str, SEXP pattern, SEXP replacement, SEXP vectorize_all, SEXP opts_regex) {
    final boolean is_vectorized = ((AtomicVector) vectorize_all).getElementAsLogical(0).toBooleanStrict();
    if (is_vectorized) {
      return __replace_all_regex_vectorized(str, pattern, replacement, opts_regex, ReplaceType.ALL);
    } else {
      final int length = str.length();
      if (length <= 0) {
        return StringVector.EMPTY;
      } else {
        final int pattern_n = pattern.length();
        final int replacement_n = replacement.length();
        if (pattern_n < replacement_n || pattern_n <= 0 || replacement_n <= 0) {
          throw new EvalException("vector length not consistent with other arguments");
        }
        if (pattern_n % replacement_n != 0) {
          Native.currentContext().warn("longer object length is not a multiple of shorter object length");
        }
        if (pattern_n == 1) {// this will be much faster:
          return __replace_all_regex_vectorized(str, pattern, replacement, opts_regex, ReplaceType.ALL);
        }
        final int flags = __regex_flags(opts_regex);
        final String[] result = new String[length];
        final StringVector strings = __prepare_string(str);
        final StringVector patterns = __prepare_string(pattern);
        final StringVector replacements = __ensure_length(pattern_n, __prepare_string(replacement));
        for (int i = 0; i < pattern_n; i++) {
          if (patterns.isElementNA(i)) {
            return __string_vector_NA(length);
          } else if (patterns.getElementAsString(i).length() <= 0) {
            Native.currentContext().warn("empty search patterns are not supported");
            return __string_vector_NA(length);
          }
          for (int j = 0; j < length; j++) {
            if (strings.isElementNA(j)) {
              result[j] = StringVector.NA;
            } else {
              final String element = strings.getElementAsString(j);
              final String appliedPattern = __normalize_binary_properties(patterns.getElementAsString(i));
              final Matcher matcher = Pattern.compile(appliedPattern, flags).matcher(element);
              if (replacements.isElementNA(i)) {
                if (matcher.find()) {
                  result[j] = StringVector.NA;
                } else {
                  result[j] = element;
                }
              } else {
                matcher.replaceAll(replacements.getElementAsString(i));
              }
            }
          }
        }
        return new StringArrayVector(result);
      }
    }
  }
  public static SEXP stri_replace_first_regex(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_regex) {
    return __replace_all_regex_vectorized(str, pattern, replacement, opts_regex, ReplaceType.FIRST);
  }
  public static SEXP stri_replace_last_regex(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_regex) {
    return __replace_all_regex_vectorized(str, pattern, replacement, opts_regex, ReplaceType.LAST);
  }
  public static SEXP stri_replace_all_charclass(SEXP str, SEXP pattern, SEXP replacement, SEXP merge, SEXP vectorize_all) {
    final boolean is_merging = ((AtomicVector) merge).getElementAsLogical(0).toBooleanStrict();
    final boolean is_vectorized = ((AtomicVector) vectorize_all).getElementAsLogical(0).toBooleanStrict();
    if (is_vectorized) {
      return __replace_all_charclass_vectorized(str, pattern, replacement, is_merging);
    } else {
      final int length = str.length();
      if (length <= 0) {
        return StringVector.EMPTY;
      } else {
        final int pattern_n = pattern.length();
        final int replacement_n = replacement.length();
        if (pattern_n < replacement_n || pattern_n <= 0 || replacement_n <= 0) {
          throw new EvalException("vector length not consistent with other arguments");
        }
        if (pattern_n % replacement_n != 0) {
          Native.currentContext().warn("longer object length is not a multiple of shorter object length");
        }
        if (pattern_n == 1) {// this will be much faster:
          return __replace_all_charclass_vectorized(str, pattern, replacement, is_merging);
        }
        final String[] result = new String[length];
        final StringVector strings = __prepare_string(str);
        final StringVector patterns = __prepare_string(pattern);
        final StringVector replacements = __ensure_length(pattern_n, __prepare_string(replacement));
        for (int i = 0; i < pattern_n; i++) {
          if (patterns.isElementNA(i)) {
            return __string_vector_NA(length);
          }
          for (int j = 0; j < length; j++) {
            if (strings.isElementNA(j)) {
              result[j] = StringVector.NA;
            } else {
              final String element = strings.getElementAsString(j);
              final String pattern_i = __normalize_binary_properties(patterns.getElementAsString(i));
              if (replacements.isElementNA(i)) {
                if (Pattern.compile(pattern_i).matcher(element).find()) {
                  result[j] = StringVector.NA;
                } else {
                  result[j] = element;
                }
              } else {
                final String appliedPattern = (is_merging) ? "(?:" + pattern_i + ")+" : pattern_i;
                result[j] = element.replaceAll(appliedPattern, replacements.getElementAsString(i));
              }
            }
          }
        }
        return new StringArrayVector(result);
      }
    }
  }
  public static SEXP stri_replace_first_charclass(SEXP str, SEXP pattern, SEXP replacement) {
    return __replace_firstlast_charclass(str, pattern, replacement, ReplaceType.FIRST);
  }
  public static SEXP stri_replace_last_charclass(SEXP str, SEXP pattern, SEXP replacement) {
    return __replace_firstlast_charclass(str, pattern, replacement, ReplaceType.LAST);
  }
  public static SEXP stri_reverse(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_split_boundaries(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_split_charclass(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final IntVector ns = __ensure_length(length, __prepare_int(n));
    final LogicalVector omits = __ensure_length(length, __prepare_logical(omit_empty));

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
          final String separatorPattern = __normalize_binary_properties(patterns.getElementAsString(i));
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

    final ListVector resultSexp = new ListVector(result);
    return __simplify_when_required(resultSexp, simplify, n);
  }
  public static SEXP stri_split_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6, SEXP s7) { throw new EvalException("TODO"); }
  public static SEXP stri_split_fixed(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_fixed) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final IntVector ns = __ensure_length(length, __prepare_int(n));
    final LogicalVector omits = __ensure_length(length, __prepare_logical(omit_empty));

    for (int i = 0; i < length; i++) {
      if (ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
          if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
            Native.currentContext().warn("empty search patterns are not supported");
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
              final int patternLength = separatorPattern.length();
              int previousStart = 0;
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

    final ListVector resultSexp = new ListVector(result);
    return __simplify_when_required(resultSexp, simplify, n);
  }
  public static SEXP stri_split_lines(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_split_lines1(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_split_regex(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_regex) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final IntVector ns = __ensure_length(length, __prepare_int(n));
    final LogicalVector omits = __ensure_length(length, __prepare_logical(omit_empty));

    for (int i = 0; i < length; i++) {
      if (ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
          if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
            Native.currentContext().warn("empty search patterns are not supported");
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
              final Matcher matcher = Pattern.compile(__normalize_binary_properties(patterns.getElementAsString(i)), flags).matcher(element);
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

    final ListVector resultSexp = new ListVector(result);
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

  private enum ReplaceType {
    ALL, FIRST, LAST;
    boolean isAll() {
      return ALL.equals(this);
    }
    boolean isFirst() {
      return FIRST.equals(this);
    }
    boolean isLast() {
      return LAST.equals(this);
    }
  }

  /**
   * Calculate the length of the output vector when applying a vectorized operation on >= 2 vectors
   *
   * For nonconforming lengths, a warning is given
   *
   * @param enableWarning
   *          enable warning in case any argument length is not a divisor of max length
   * @param arguments
   *          vectors to recycle
   * @return max of the given arguments' lengths or 0 iff any length is <= 0
   */
  private static int __recycling_rule(boolean enableWarning, SEXP... arguments) {
    int length = -1;
    for (SEXP arg: arguments) {
    	  final int argLength = arg.length();
      if (argLength <= 0) {
        return 0;
      }
      length = Math.max(length, argLength);
    }
    if (enableWarning) {
      for (SEXP arg : arguments) {
        final int argLength = arg.length();
        if (length % argLength != 0) {
          Native.currentContext().warn("longer object length is not a multiple of shorter object length");
        }
      }
    }
    return length < 0 ? 0 : length;
  }
  private static StringVector __ensure_length(int length, StringVector exp) {
    final int expLength = exp.length();
    if (length == expLength) {
      return exp;
    } else {
      return new RecyclingStringVector(length, (StringVector) exp);
    }
  }
  private static IntVector __ensure_length(int length, IntVector exp) {
    final int expLength = exp.length();
    if (length == expLength) {
      return exp;
    } else {
      return new RecyclingIntVector(length, (IntVector) exp);
    }
  }
  private static LogicalVector __ensure_length(int length, LogicalVector exp) {
    final int expLength = exp.length();
    if (length == expLength) {
      return exp;
    } else {
      return new RecyclingLogicalVector(length, (LogicalVector) exp);
    }
  }
  private static StringVector __prepare_string(SEXP s) {
    if (s instanceof StringVector) {
      return (StringVector) s;
    }
    throw new EvalException("TODO");
  }
  private static IntVector __prepare_int(SEXP s) {
    if (s instanceof IntVector) {
      return (IntVector) s;
    }
    throw new EvalException("TODO");
  }
  private static LogicalVector __prepare_logical(SEXP s) {
    if (s instanceof LogicalVector) {
      return (LogicalVector) s;
    }
    throw new EvalException("TODO");
  }
  private static SEXP __trim_left_right(SEXP str, SEXP pattern, TrimOption side) {
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));

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
        throw new EvalException("regexp engine config failed");
      }
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          throw new EvalException("regexp engine config failed");
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
          Native.currentContext().warn("incorrect opts_fixed setting: `" + name + "`. ignoring");
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
        throw new EvalException("regexp engine config failed");
      }
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          throw new EvalException("regexp engine config failed");
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
          Native.currentContext().warn("incorrect opts_regex setting: `" + name + "`. ignoring");
        }
      }
    }

    return flags;
  }
  private static String __normalize_binary_properties(String pattern) {
    if (null == pattern) {
      return null;
    } else {
      // Java gives a different name to the supported Unicode binary properties
      // see https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#ubpc
      // @formatter:off
      return pattern
          .replace("{ALPHABETIC}", "{IsAlphabetic}")
          .replace("{IDEOGRAPHIC}", "{IsIdeographic}")
          .replace("{LETTER}", "{IsLetter}")
          .replace("{LOWERCASE}", "{IsLowercase}")
          .replace("{UPPERCASE}", "{IsUppercase}")
          .replace("{TITLECASE}", "{IsTitlecase}")
          .replace("{PUNCTUATION}", "{IsPunctuation}")
          .replace("{CONTROL}", "{IsControl}")
          .replace("{WHITE_SPACE}", "{IsWhite_Space}")
          .replace("{DIGIT}", "{IsDigit}")
          .replace("{HEX_DIGIT}", "{IsHex_Digit}")
          .replace("{JOIN_CONTROL}", "{IsJoin_Control}")
          .replace("{NONCHARACTER_CODE_POINT}", "{IsNoncharacter_Code_Point}")
          .replace("{ASSIGNED}", "{IsAssigned}");
      // @formatter:on
    }
  }
  private static SEXP __simplify_when_required(SEXP resultSexp, SEXP simplify, SEXP n) {
    final Logical first_simplify = ((AtomicVector) simplify).getElementAsLogical(0);
    if (first_simplify.equals(Logical.FALSE)) {
      return resultSexp;
    } else { // NA or TRUE
      int required_depth = 0;
      for (int i = 0; i < n.length(); i++) {
        required_depth = Math.max(required_depth, ((AtomicVector) n).getElementAsInt(i));
      }
      final String filler = first_simplify.equals(Logical.NA) ? StringVector.NA : "";
      return stri_list2matrix(resultSexp, LogicalVector.valueOf(true), StringVector.valueOf(filler), IntVector.valueOf(required_depth));
    }
  }
  private static SEXP __string_vector_NA(final int length) {
    final StringVector.Builder builder = StringVector.newBuilder();
    for (int j = 0; j < length; j++) {
      builder.addNA();
    }
    return builder.build();
  }
  private static SEXP __replace_firstlast_charclass(SEXP str, SEXP pattern, SEXP replacement, ReplaceType replaces) {
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final StringVector replacements = __ensure_length(length, __prepare_string(replacement));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = __normalize_binary_properties(patterns.getElementAsString(i));
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        int nomatch = -1;
        int cut = -1;
        if (replaces.isFirst()) {
          nomatch = element.length();
          cut = matcher.span(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
        } else if (replaces.isLast()) {
          nomatch = 0;
          cut = matcher.spanBack(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
        }
        if (cut == nomatch) {
          result[i] = element;
        } else {
          result[i] = element.substring(0, cut) + replacements.getElementAsString(i) + element.substring(cut + 1);
        }
      }
    }

    return new StringArrayVector(result);
  }
  private static SEXP __replace_all_charclass_vectorized(SEXP str, SEXP pattern, SEXP replacement, boolean is_merging) {
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final StringVector replacements = __ensure_length(length, __prepare_string(replacement));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String patterni = (is_merging) ? "(?:" + patterns.getElementAsString(i) + ")+" : patterns.getElementAsString(i);
        result[i] = strings.getElementAsString(i).replaceAll(__normalize_binary_properties(patterni), replacements.getElementAsString(i));
      }
    }

    return new StringArrayVector(result);
  }
  private static SEXP __replace_all_fixed_vectorized(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_fixed, ReplaceType replaces) {
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final StringVector replacements = __ensure_length(length, __prepare_string(replacement));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        if (element.length() <= 0) {
          result[i] = "";
        } else {
          final String separatorPattern = patterns.getElementAsString(i);
          final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
          final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
          if (replacements.isElementNA(i)) {
            if (-1 < elementNormalized.indexOf(patternNormalized)) {
              result[i] = StringVector.NA;
            } else {
              result[i] = element;
            }
          } else {
            final String replacement_i = replacements.getElementAsString(i);
            final int patternLength = separatorPattern.length();
            final StringBuffer replaced = new StringBuffer();
            int previousStart = 0;
            int beginIndex = (replaces.isLast()) ? elementNormalized.lastIndexOf(patternNormalized) : elementNormalized.indexOf(patternNormalized);
            while (replaces.isAll() && beginIndex != -1) {
              replaced.append(element.substring(previousStart, beginIndex));
              replaced.append(replacement_i);
              previousStart = beginIndex + patternLength;
              beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
            }
            replaced.append(element.substring(previousStart));
            result[i] = replaced.toString();
          }
        }
      }
    }

    return new StringArrayVector(result);
  }
  private static SEXP __replace_all_regex_vectorized(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_regex, ReplaceType replaces) {
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, __prepare_string(str));
    final StringVector patterns = __ensure_length(length, __prepare_string(pattern));
    final StringVector replacements = __ensure_length(length, __prepare_string(replacement));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String appliedPattern = __normalize_binary_properties(patterns.getElementAsString(i));
        final String replacement_i = replacements.getElementAsString(i);
        final Matcher matcher = Pattern.compile(appliedPattern, flags).matcher(element);
        if (replaces.isAll()) {
          result[i] = matcher.replaceAll(replacement_i);
        } else if (replaces.isFirst()) {
          result[i] = matcher.replaceFirst(replacement_i);
        } else if (replaces.isLast()) {
          int start = -1;
          int end = -1;
          while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
          }
          if (start < 0 || end < 0) {
            result[i] = element;
          } else {
            result[i] = element.substring(0, start) + replacement_i + element.substring(end);
          }
        }
      }
    }

    return new StringArrayVector(result);
  }
}
