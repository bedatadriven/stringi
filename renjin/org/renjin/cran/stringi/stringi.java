package org.renjin.cran.stringi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.text.Normalizer;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.Native;
import org.renjin.primitives.Types;
import org.renjin.primitives.Warning;
import org.renjin.primitives.matrix.IntMatrixBuilder;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.primitives.matrix.StringMatrixBuilder;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.sequence.RepIntVector;
import org.renjin.primitives.sequence.RepLogicalVector;
import org.renjin.primitives.sequence.RepStringVector;
import org.renjin.repackaged.guava.base.Utf8;
import org.renjin.repackaged.guava.collect.Range;
import org.renjin.sexp.*;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacter.EastAsianWidth;
import com.ibm.icu.lang.UCharacter.HangulSyllableType;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.UnicodeSetSpanner.TrimOption;
import com.ibm.icu.util.ULocale;

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

  public static SEXP stri_cmp_eq(SEXP s1, SEXP s2) {
    return __cmp_codepoints(s1, s2, false);
  }
  public static SEXP stri_cmp_neq(SEXP s1, SEXP s2) {
    return __cmp_codepoints(s1, s2, true);
  }
  public static SEXP stri_cmp(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_lt(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_le(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_gt(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_ge(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_equiv(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_cmp_nequiv(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }

  public static SEXP stri_count_boundaries(SEXP str, SEXP opts_brkiter) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final BreakIterator brkiter = __open_break_iterator(opts_brkiter, "line_break");
    final int length = str.length();
    final int[] result = new int[length];

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = IntVector.NA;
      } else {
        brkiter.setText(strings.getElementAsString(i));
        brkiter.first();
        int count = 0;
        while (BreakIterator.DONE < brkiter.next()) {
          // FIXME handle skip_* rules
          count++;
        }
        result[i] = count;
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_count_charclass(SEXP str, SEXP pattern) {
    final int length = __recycling_rule(true, str, pattern);
    final int[] result = new int[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = IntVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = 0;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        int found = 0;
        int previousStart = 0;
        int beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
        if (0 < beginIndex) {
          while (previousStart < element.length() && beginIndex < element.length()) {
            found++;
            final int endIndex = matcher.span(element, beginIndex, UnicodeSet.SpanCondition.CONTAINED);
            previousStart = endIndex;
            beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
          }
        }
        result[i] = found;
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_count_coll(SEXP str, SEXP pattern, SEXP opts_collator) {
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final int length = __recycling_rule(true, str, pattern);
    final int[] result = new int[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    StringSearch matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = IntVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = 0;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        int found = 0;
        int beginIndex = matcher.first();
        while (-1 < beginIndex) {
          found++;
          beginIndex = matcher.next();
        }
        result[i] = found;
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_count_fixed(SEXP str, SEXP pattern, SEXP opts_fixed) {
    final int flags = __fixed_flags(opts_fixed, true);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final boolean allows_overlap = (flags & Pattern.COMMENTS) > 0;
    final int length = __recycling_rule(true, str, pattern);
    final int[] result = new int[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = IntVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = 0;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        final int patternLength = separatorPattern.length();
        final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
        final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
        int found = 0;
        int previousStart = 0;
        int beginIndex = elementNormalized.indexOf(patternNormalized);
        while (-1 < beginIndex) {
          found++;
          previousStart = beginIndex + (allows_overlap ? 1 : patternLength);
          beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
        }
        result[i] = found;
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_count_regex(SEXP str, SEXP pattern, SEXP opts_regex) {
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final int[] result = new int[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = IntVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = 0;
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        int found = 0;
        while (matcher.find()) {
          found++;
        }
        result[i] = found;
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_datetime_symbols(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_fields(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_now(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_create(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6, SEXP s7, SEXP s8, SEXP s9) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_format(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_parse(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_datetime_add(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_detect_charclass(SEXP str, SEXP pattern, SEXP negate) {
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final Logical[] result = new Logical[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        result[i] = Logical.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String appliedPattern = patterns.getElementAsString(i);
        if (!appliedPattern.equals(lastPattern)) {
          lastPattern = appliedPattern;
          matcher = new UnicodeSet(appliedPattern);
        }
        final boolean found = -1 < matcher.span(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
        result[i] = Logical.valueOf(is_negating ? !found : found);
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_detect_coll(SEXP str, SEXP pattern, SEXP negate, SEXP opts_collator) {
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final int length = __recycling_rule(true, str, pattern);
    final Logical[] result = new Logical[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    StringSearch matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = Logical.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = Logical.valueOf(is_negating);
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        final boolean found = matcher.first() != StringSearch.DONE;
        result[i] = Logical.valueOf(is_negating ? !found : found);
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_detect_fixed(SEXP str, SEXP pattern, SEXP negate, SEXP opts_fixed) {
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern);
    final Logical[] result = new Logical[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = Logical.NA;
      } else {
        final String element = strings.getElementAsString(i);
        if (element.length() <= 0) {
          result[i] = Logical.valueOf(is_negating);
        } else {
          final String separatorPattern = patterns.getElementAsString(i);
          final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
          final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
          final boolean found = -1 < elementNormalized.indexOf(patternNormalized);
          result[i] = Logical.valueOf(is_negating ? !found : found);
        }
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_detect_regex(SEXP str, SEXP pattern, SEXP negate, SEXP opts_regex) {
    if (str.length() == 0 || pattern.length() == 0) {
      return ListVector.EMPTY;
    }
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final Logical[] result = new Logical[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = Logical.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        final boolean found = matcher.find();
        result[i] = Logical.valueOf(is_negating ? !found : found);
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_dup(SEXP str, SEXP times) {
    final int length = __recycling_rule(true, str, times);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final IntVector repeats = __ensure_length(length, stri_prepare_arg_integer(times, "times"));

    if (length <= 0) {
      return StringVector.EMPTY;
    } else {
      final String[] result = new String[length];
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
        int repeat;
        if (strings.isElementNA(i) || repeats.isElementNA(i) || (repeat = repeats.getElementAsInt(i)) < 0) {
          result[i] = StringVector.NA;
        } else if (repeat <= 0 || strings.getElementAsString(i).length() <= 0) {
          result[i] = "";
        } else {
          final String element = strings.getElementAsString(i);
          sb.setLength(0);
          for (int j = 0; j < repeat; j++) {
            sb.append(element);
          }
          result[i] = sb.toString();
        }
      }
      return new StringArrayVector(result);
    }
  }

  public static SEXP stri_duplicated(SEXP str, SEXP fromLast, SEXP opts_collator) {
    final boolean fromEnd = ((AtomicVector) fromLast).getElementAsLogical(0).toBooleanStrict();
    final int length = str.length();
    final boolean[] result = new boolean[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final LinkedHashSet<CollatedString> unique = new LinkedHashSet<>();

    boolean no_na = true;
    if (fromEnd) {
      for (int i = length - 1; 0 <= i; i--) {
        if (strings.isElementNA(i)) {
          result[i] = true;
          if (no_na) {
            result[i] = false;
            no_na = false;
          }
        } else {
          result[i] = !unique.add(new CollatedString(collator, strings.getElementAsString(i)));
        }
      }
    } else {
      for (int i = 0; i < length; i++) {
        if (strings.isElementNA(i)) {
          result[i] = true;
          if (no_na) {
            result[i] = false;
            no_na = false;
          }
        } else {
          result[i] = !unique.add(new CollatedString(collator, strings.getElementAsString(i)));
        }
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_duplicated_any(SEXP str, SEXP fromLast, SEXP opts_collator) {
    final boolean fromEnd = ((AtomicVector) fromLast).getElementAsLogical(0).toBooleanStrict();
    final int length = str.length();
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final LinkedHashSet<CollatedString> unique = new LinkedHashSet<>();

    int result = 0;
    boolean no_na = true;
    if (fromEnd) {
      for (int i = length - 1; 0 <= i; i--) {
        if (strings.isElementNA(i)) {
          if (no_na) {
            no_na = false;
          } else {
            result = i + 1; // 1-based indices
            break;
          }
        } else {
          if (!unique.add(new CollatedString(collator, strings.getElementAsString(i)))) {
            result = i + 1; // 1-based indices
            break;
          }
        }
      }
    } else {
      for (int i = 0; i < length; i++) {
        if (strings.isElementNA(i)) {
          if (no_na) {
            no_na = false;
          } else {
            result = i + 1; // 1-based indices
            break;
          }
        } else {
          if (!unique.add(new CollatedString(collator, strings.getElementAsString(i)))) {
            result = i + 1; // 1-based indices
            break;
          }
        }
      }
    }

    return IntVector.valueOf(result);
  }

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

  public static SEXP stri_enc_fromutf32(SEXP vec) {
    final int length = vec.length();
    final String[] result = new String[length];
    final ListVector vectors = (ListVector) vec; // FIXME maybe implement stri_prepare_arg_list_integer

    for (int i = 0; i < length; i++) {
      if (vectors.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final DoubleVector element = (DoubleVector) vectors.getElementAsSEXP(i);
        final int size = element.length();
        final StringBuilder sb = new StringBuilder(size);
        boolean foundError = false;
        int codepoint = 0;
        for (int k = 0; !foundError && k < size; k++) {
          codepoint = element.getElementAsInt(k);
          if (codepoint == 0) {
            foundError = true;
          } else {
            try {
              sb.appendCodePoint(codepoint);
            } catch (IllegalArgumentException e) {
              foundError = true;
            }
          }
        }
        if (foundError) {
          Native.currentContext().warn(String.format("invalid Unicode codepoint \\U%08.8x", codepoint));
          result[i] = StringVector.NA;
        } else {
          result[i] = sb.toString();
        }
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_enc_toascii(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_enc_toutf8(SEXP str, SEXP is_unknown_8bit, SEXP validate) {
    // in Java, the invalid code points would result in an exception at the time of reading the string
    // we cannot replace here invalid code points by 0xFFFD or invalid strings by NA

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
  public static SEXP stri_encode(SEXP str, SEXP from, SEXP to, SEXP to_raw) {
    final boolean returns_raw = ((AtomicVector) to_raw).asLogical().toBooleanStrict();
    final org.renjin.sexp.Vector prepared = __prepare_arg_list_raw(str, "str");
    final int length = prepared.length();
    if (length <= 0) {
      return returns_raw ? ListVector.EMPTY : StringVector.EMPTY;
    } else {
      String selected_from = from.asString();
      if (selected_from == StringVector.NA) {
        selected_from = "UTF-16"; // in Java, String is always stored internally as UTF-16
      }
      final String selected_to = to.asString();
      final SEXP[] raws = new SEXP[returns_raw ? length : 0];
      final String[] encoded = new String[returns_raw ? 0 : length];
      // @formatter:off
      final CharsetEncoder encoder = Charset.forName(selected_to).newEncoder()
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset();
      // @formatter:on

      if (prepared instanceof ListVector) {
        final ListVector list = (ListVector) prepared;
        final Charset fromcs = Charset.forName(selected_from);
        // @formatter:off
        final CharsetDecoder decoder = fromcs.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .reset();
        // @formatter:on
        for (int i = 0; i < length; i++) {
          final SEXP entry = list.getElementAsSEXP(i);
          if (Types.isNull(entry)) {
            if (returns_raw) {
              raws[i] = Null.INSTANCE;
            } else {
              encoded[i] = StringVector.NA;
            }
          } else {
            final RawVector raw = (RawVector) entry;
            if (returns_raw) {
              try {
                final CharBuffer chars = decoder.decode(ByteBuffer.wrap(raw.toByteArray()));
                final ByteBuffer converted = encoder.encode(chars);
                raws[i] = new RawVector(converted.array());
              } catch (CharacterCodingException cce) {
                throw new EvalException(cce);
              }
            } else {
              encoded[i] = new String(raw.toByteArray(), fromcs);
            }
          }
        }
      } else if (prepared instanceof StringVector) {
        final StringVector strings = (StringVector) prepared;
        for (int i = 0; i < length; i++) {
          if (strings.isElementNA(i)) {
            if (returns_raw) {
              raws[i] = Null.INSTANCE;
            } else {
              encoded[i] = StringVector.NA;
            }
          } else {
            final String element = strings.getElementAsString(i);
            if (returns_raw) {
              try {
                final CharBuffer chars = CharBuffer.wrap(element);
                final ByteBuffer converted = encoder.encode(chars);
                raws[i] = new RawVector(converted.array());
              } catch (CharacterCodingException cce) {
                throw new EvalException(cce);
              }
            } else {
              encoded[i] = element; // in Java, String is always stored internally as UTF-16
            }
          }
        }
      }

      return returns_raw ? new ListVector(raws) : new StringArrayVector(encoded);
    }
  }

  public static SEXP stri_endswith_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_endswith_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_endswith_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_escape_unicode(SEXP str) {
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        sb.setLength(0);
        final String element = strings.getElementAsString(i);
        for (int j = 0; j < element.length(); j++) {
          final int cp = element.codePointAt(j);
          if (cp <= 126) {
            switch (cp) {
            // @formatter:off
//            case 0x07: sb.append("\\a"); break; // Java doesn't know that
            case 0x08: sb.append("\\b"); break;
            case 0x09: sb.append("\\t"); break;
            case 0x0a: sb.append("\\n"); break;
//            case 0x0b: sb.append("\\v"); break; // Java doesn't know that
            case 0x0c: sb.append("\\f"); break;
            case 0x0d: sb.append("\\r"); break;
//            case 0x1b: sb.append("\\e"); break; // R doesn't know that
            case 0x22: sb.append("\\\""); break;
            case 0x27: sb.append("\\'"); break;
            case 0x5c: sb.append("\\\\"); break;
            // @formatter:on
            default:
              if (cp >= 32 && cp <= 126) { // printable characters
                sb.appendCodePoint(cp);
              } else {
                sb.append(String.format("\\u%04x", cp));
              }
            }
          } else if (cp <= 0xffff) {
            sb.append(String.format("\\u%04x", cp));
          } else {
            sb.append(String.format("\\U%08x", cp));
          }
        }
        result[i] = sb.toString();
      }
    }

    return new StringArrayVector(result);
  }
  public static SEXP stri_extract_first_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_extract_last_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }

  public static SEXP stri_extract_all_boundaries(SEXP str, SEXP simplify, SEXP omit_no_match, SEXP opts_brkiter) {
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final BreakIterator brkiter = __open_break_iterator(opts_brkiter, "line_break");
    final int length = __recycling_rule(true, str);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
        continue;
      }

      final String element = strings.getElementAsString(i);

      if (element.isEmpty()) {
        result[i] = omits_not_found ?
            StringVector.EMPTY : StringVector.valueOf(StringVector.NA);
        continue;
      }

      brkiter.setText(element);
      final LinkedList<Range<Integer>> occurences = new LinkedList<>();
      int previousStart = brkiter.first();

      while (BreakIterator.DONE < brkiter.next()) {
        int lower = previousStart;
        int upper = brkiter.current();
        occurences.add(Range.closedOpen(lower, upper));
        previousStart = upper;
      }

      if (occurences.size() <= 0) {
        result[i] = omits_not_found ?
            StringVector.EMPTY : StringVector.valueOf(StringVector.NA);
        continue;
      }

      final String[] values = new String[occurences.size()];
      int j = 0;
      for (Range<Integer> entry : occurences) {
        values[j++] = element.substring(entry.lowerEndpoint(), entry.upperEndpoint());
      }
      result[i] = new StringArrayVector(values);

    }

    return __simplify_when_required(new ListVector(result), simplify, IntVector.valueOf(0));
  }

  public static SEXP stri_extract_first_charclass(SEXP str, SEXP pattern) {
    return __extract_firstlast_charclass(str, pattern, ReplaceType.FIRST);
  }

  public static SEXP stri_extract_last_charclass(SEXP str, SEXP pattern) {
    return __extract_firstlast_charclass(str, pattern, ReplaceType.LAST);
  }

  public static SEXP stri_extract_all_charclass(SEXP str, SEXP pattern, SEXP merge, SEXP simplify, SEXP omit_no_match) {
    final boolean is_merging = ((AtomicVector) merge).getElementAsLogical(0).toBooleanStrict();
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        int previousStart = 0;
        int beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
        if (-1 < beginIndex) {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (previousStart < element.length() && beginIndex < element.length()) {
            final int endIndex = matcher.span(element, beginIndex, UnicodeSet.SpanCondition.CONTAINED);
            if (is_merging) {
              occurrences.add(Range.closedOpen(beginIndex, endIndex));
            } else {
              for (int k = beginIndex; k < endIndex; k++) {
                occurrences.add(Range.closedOpen(k, k + 1));
              }
            }
            previousStart = endIndex;
            beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
          }
          final String[] values = new String[occurrences.size()];
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            values[j++] = element.substring(entry.lowerEndpoint(), entry.upperEndpoint());
          }
          result[i] = new StringArrayVector(values);
        } else {
          result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
        }
      }
    }

    return __simplify_when_required(new ListVector(result), simplify, IntVector.valueOf(0));
  }

  public static SEXP stri_extract_first_coll(SEXP str, SEXP pattern, SEXP opts_collator) {
    return __extract_firstlast_coll(str, pattern, opts_collator, ReplaceType.FIRST);
  }

  public static SEXP stri_extract_last_coll(SEXP str, SEXP pattern, SEXP opts_collator) {
    return __extract_firstlast_coll(str, pattern, opts_collator, ReplaceType.LAST);
  }

  public static SEXP stri_extract_all_coll(SEXP str, SEXP pattern, SEXP simplify, SEXP omit_no_match, SEXP opts_collator) {
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    
    String lastPattern = null;
    StringSearch matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.valueOf(StringVector.NA);
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        int beginIndex = matcher.first();
        if (beginIndex == StringSearch.DONE) {
          result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
        } else {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (beginIndex != StringSearch.DONE) {
            occurrences.add(Range.closedOpen(beginIndex, beginIndex + matcher.getMatchLength()));
            beginIndex = matcher.next();
          }
          final String[] values = new String[occurrences.size()];
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            values[j++] = element.substring(entry.lowerEndpoint(), entry.upperEndpoint());
          }
          result[i] = new StringArrayVector(values);
        }
      }
    }

    return __simplify_when_required(new ListVector(result), simplify, IntVector.valueOf(0));
  }

  public static SEXP stri_extract_first_fixed(SEXP str, SEXP pattern, SEXP opts_fixed) {
    return __extract_firstlast_fixed(str, pattern, opts_fixed, ReplaceType.FIRST);
  }

  public static SEXP stri_extract_last_fixed(SEXP str, SEXP pattern, SEXP opts_fixed) {
    return __extract_firstlast_fixed(str, pattern, opts_fixed, ReplaceType.LAST);
  }

  public static SEXP stri_extract_all_fixed(SEXP str, SEXP pattern, SEXP simplify, SEXP omit_no_match, SEXP opts_fixed) {
    final int flags = __fixed_flags(opts_fixed, true);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.valueOf(StringVector.NA);
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        final int patternLength = separatorPattern.length();
        final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
        final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
        int previousStart = 0;
        int beginIndex = elementNormalized.indexOf(patternNormalized);
        if (-1 < beginIndex) {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (-1 < beginIndex) {
            occurrences.add(Range.closedOpen(beginIndex, beginIndex + patternLength));
            previousStart = beginIndex + patternLength;
            beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
          }
          final String[] values = new String[occurrences.size()];
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            values[j++] = element.substring(entry.lowerEndpoint(), entry.upperEndpoint());
          }
          result[i] = new StringArrayVector(values);
        } else {
          result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
        }
      }
    }

    return __simplify_when_required(new ListVector(result), simplify, IntVector.valueOf(0));
  }

  public static SEXP stri_extract_first_regex(SEXP str, SEXP pattern, SEXP opts_regex) {
    return __extract_firstlast_regex(str, pattern, opts_regex, ReplaceType.FIRST);
  }

  public static SEXP stri_extract_last_regex(SEXP str, SEXP pattern, SEXP opts_regex) {
    return __extract_firstlast_regex(str, pattern, opts_regex, ReplaceType.LAST);
  }

  public static SEXP stri_extract_all_regex(SEXP str, SEXP pattern, SEXP simplify, SEXP omit_no_match, SEXP opts_regex) {
    final int flags = __regex_flags(opts_regex);
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        final LinkedList<Range<Integer>> occurrences = new LinkedList<>();
        while (matcher.find()) {
          occurrences.add(Range.closedOpen(matcher.start(), matcher.end()));
        }
        if (occurrences.size() <= 0) {
          result[i] = __string_vector_NA(omits_not_found ? 0 : 1);
        } else {
          final String[] values = new String[occurrences.size()];
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            values[j++] = element.substring(entry.lowerEndpoint(), entry.upperEndpoint());
          }
          result[i] = new StringArrayVector(values);
        }
      }
    }

    return __simplify_when_required(new ListVector(result), simplify, IntVector.valueOf(0));
  }

  public static SEXP stri_flatten(SEXP str, SEXP collapse) {
    final StringVector collapsers = stri_prepare_arg_string(collapse, "collapse");
    if (collapsers.isElementNA(0)) {
      return __string_vector_NA(1);
    } else if (collapsers.getElementAsString(0).length() == 0) {
      final StringVector strings = stri_prepare_arg_string(str, "str");
      final int length = strings.length();
      if (length <= 0) {
        return str;
      } else {
        for (int i = 0; i < length; i++) {
          if (strings.isElementNA(i)) {
            return __string_vector_NA(1);
          }
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
          sb.append(strings.getElementAsString(i));
        }
        return StringVector.valueOf(sb.toString());
      }
    } else {
      final StringVector strings = stri_prepare_arg_string(str, "str");
      final int length = strings.length();
      if (length <= 0) {
        return StringVector.EMPTY;
      } else {
        for (int i = 0; i < length; i++) {
          if (strings.isElementNA(i)) {
            return __string_vector_NA(1);
          }
        }
        final String collapser = collapsers.getElementAsString(0);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
          sb.append(strings.getElementAsString(i));
          sb.append(collapser);
        }
        sb.setLength(sb.length() - collapser.length());
        return StringVector.valueOf(sb.toString());
      }
    }
  }

  public static SEXP stri_info(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_isempty(SEXP str) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final int length = strings.length();
    final Logical[] result = new Logical[length];

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = Logical.NA;
      } else {
        result[i] = Logical.valueOf(strings.getElementAsString(i).isEmpty());
      }
    }

    return new LogicalArrayVector(result);
  }

  public static SEXP stri_join(SEXP strlist, SEXP sep, SEXP collapse, SEXP ignore_null) {
    if (Types.isNull(collapse)) {
      return __join_no_collapse(strlist, sep, ignore_null);
    } else {
      final boolean ignore_empty = ((AtomicVector) ignore_null).getElementAsLogical(0).toBooleanStrict();
      final ListVector lists = (ListVector) __prepare_arg_list_ignore_empty(strlist, ignore_empty);
      final int strlist_length = lists.length();

      if (strlist_length <= 0) {
        return StringVector.EMPTY;
      } else if (strlist_length == 1) {
        // one vector + collapse string -- another frequently occurring case
        // sep is ignored here
        return stri_flatten(strlist.getElementAsSEXP(0), collapse);
      } else {
        final StringVector separators = stri_prepare_arg_string(sep, "sep");
        final StringVector collapsers = stri_prepare_arg_string(collapse, "collapse");
        if (separators.isElementNA(0) || collapsers.isElementNA(0)) {
          return __string_vector_NA(1);
        } else if (separators.getElementAsString(0).length() == 0 && strlist_length == 2) {
          // sep==empty string and 2 vectors --
          // an often occurring case - we have some specialized functions for this :-)
          return __join2_with_collapse(lists.getElementAsSEXP(0), lists.getElementAsSEXP(1), collapse);
        } else {
          int vectorize_length = 0;
          for (int i = 0; i < strlist_length; i++) {
            final int current_length = lists.getElementAsSEXP(i).length();
            if (current_length <= 0) {
              return StringVector.EMPTY;
            } else if (current_length > vectorize_length) {
              vectorize_length = current_length;
            }
          }
          final StringVector[] vectorized = new StringVector[strlist_length];
          for (int j = 0; j < strlist_length; j++) {
            vectorized[j] = __ensure_length(vectorize_length, stri_prepare_arg_string(strlist.getElementAsSEXP(j), "strlist" + j));
            for (int i = 0; i < vectorize_length; i++) {
              if (vectorized[j].isElementNA(i)) {
                return __string_vector_NA(1);
              }
            }
          }
          final String separator = separators.getElementAsString(0);
          final String collapser = collapsers.getElementAsString(0);
          final StringBuilder sb = new StringBuilder();
          for (int i = 0; i < vectorize_length; i++) {
            for (int j = 0; j < strlist_length; j++) {
              sb.append(vectorized[j].getElementAsString(i));
              sb.append(separator);
            }
            sb.setLength(sb.length() - separator.length());
            sb.append(collapser);
          }
          sb.setLength(sb.length() - collapser.length());
          return StringVector.valueOf(sb.toString());
        }
      }
    }
  }

  public static SEXP stri_join_list(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_join2(SEXP s1, SEXP s2) {
    if (s1.length() <= 0) {
      return s1;
    }
    if (s2.length() <= 0) {
      return s2;
    }

    final int length = __recycling_rule(true, s1, s2);
    final StringVector e1 = __ensure_length(length, stri_prepare_arg_string(s1, "e1"));
    final StringVector e2 = __ensure_length(length, stri_prepare_arg_string(s2, "e2"));
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

  public static SEXP stri_length(SEXP str) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final int length = strings.length();
    final int[] result = new int[length];

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = IntVector.NA;
      } else {
        result[i] = strings.getElementAsString(i).length();
      }
    }

    return new IntArrayVector(result);
  }

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
      return matrix.setAttribute(Symbols.DIM, new IntArrayVector(maxVectorLength, length));
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
      return matrix.setAttribute(Symbols.DIM, new IntArrayVector(length, maxVectorLength));
    }
  }

  public static SEXP stri_locale_info(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_locale_list(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_locale_set(SEXP s1) { throw new EvalException("TODO"); }

  public static SEXP stri_locate_all_boundaries(SEXP str, SEXP omit_no_match, SEXP opts_brkiter) {
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final BreakIterator brkiter = __open_break_iterator(opts_brkiter, "line_break");
    final int length = __recycling_rule(true, str);
    final IntVector[] result = new IntVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
        continue;
      }

      final String element = strings.getElementAsString(i);

      if (element.isEmpty()) {
        if (omits_not_found) {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_0(0, 2));
        } else {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
        }

        continue;
      }

      brkiter.setText(element);
      final LinkedList<Range<Integer>> occurences = new LinkedList<>();
      int previousStart = brkiter.first();

      while (BreakIterator.DONE < brkiter.next()) {
        int lower = previousStart;
        int upper = brkiter.current();
        occurences.add(Range.closedOpen(lower, upper));
        previousStart = upper;
      }

      if (occurences.size() <= 0) {
        result[i] = __locate_set_dimnames_matrix(__int_matrix_0(omits_not_found ? 0 : 1, 2));
        continue;
      }

      final IntMatrixBuilder builder = new IntMatrixBuilder(occurences.size(), 2);
      int j = 0;
      for (Range<Integer> entry : occurences) {
        builder.set(j, 0, entry.lowerEndpoint() + 1);
        builder.set(j, 1, entry.upperEndpoint());
        j++;
      }

      result[i] = __locate_set_dimnames_matrix(builder);
    }

    return new ListVector(result);
  }

  public static SEXP stri_locate_first_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_locate_last_boundaries(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }

  public static SEXP stri_locate_first_charclass(SEXP str, SEXP pattern) {
    return __locate_firstlast_charclass(str, pattern, ReplaceType.FIRST);
  }

  public static SEXP stri_locate_last_charclass(SEXP str, SEXP pattern) {
    return __locate_firstlast_charclass(str, pattern, ReplaceType.LAST);
  }

  public static SEXP stri_locate_all_charclass(SEXP str, SEXP pattern, SEXP merge, SEXP omit_no_match) {
    final boolean is_merging = ((AtomicVector) merge).getElementAsLogical(0).toBooleanStrict();
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final IntVector[] result = new IntVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        int previousStart = 0;
        int beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
        if (-1 < beginIndex) {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (previousStart < element.length() && beginIndex < element.length()) {
            final int endIndex = matcher.span(element, beginIndex, UnicodeSet.SpanCondition.CONTAINED);
            if (is_merging) {
              occurrences.add(Range.closedOpen(beginIndex, endIndex));
            } else {
              for (int k = beginIndex; k < endIndex; k++) {
                occurrences.add(Range.closedOpen(k, k + 1));
              }
            }
            previousStart = endIndex;
            beginIndex = matcher.span(element, previousStart, UnicodeSet.SpanCondition.NOT_CONTAINED);
          }
          final IntMatrixBuilder builder = new IntMatrixBuilder(occurrences.size(), 2);
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            builder.set(j, 0, entry.lowerEndpoint());
            builder.set(j, 1, entry.upperEndpoint());
            j++;
          }
          result[i] = __locate_set_dimnames_matrix(builder);
        } else {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
        }
      }
    }

    return new ListVector(result);
  }

  public static SEXP stri_locate_last_fixed(SEXP str, SEXP pattern, SEXP opts_fixed) {
    return __locate_firstlast_fixed(str, pattern, opts_fixed, ReplaceType.LAST);
  }

  public static SEXP stri_locate_first_fixed(SEXP str, SEXP pattern, SEXP opts_fixed) {
    return __locate_firstlast_fixed(str, pattern, opts_fixed, ReplaceType.FIRST);
  }

  public static SEXP stri_locate_all_fixed(SEXP str, SEXP pattern, SEXP omit_no_match, SEXP opts_fixed) {
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int flags = __fixed_flags(opts_fixed, true);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern);
    final IntVector[] result = new IntVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        final int patternLength = separatorPattern.length();
        final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
        final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
        int previousStart = 0;
        int beginIndex = elementNormalized.indexOf(patternNormalized);
        if (-1 < beginIndex) {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (-1 < beginIndex) {
            occurrences.add(Range.closedOpen(beginIndex, beginIndex + patternLength));
            previousStart = beginIndex + patternLength;
            beginIndex = elementNormalized.indexOf(patternNormalized, previousStart);
          }
          final IntMatrixBuilder builder = new IntMatrixBuilder(occurrences.size(), 2);
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            builder.set(j, 0, entry.lowerEndpoint());
            builder.set(j, 1, entry.upperEndpoint());
            j++;
          }
          result[i] = __locate_set_dimnames_matrix(builder);
        } else {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
        }
      }
    }

    return new ListVector(result);
  }

  public static SEXP stri_locate_last_coll(SEXP str, SEXP pattern, SEXP opts_collator) {
    return __locate_firstlast_coll(str, pattern, opts_collator, ReplaceType.LAST);
  }

  public static SEXP stri_locate_first_coll(SEXP str, SEXP pattern, SEXP opts_collator) {
    return __locate_firstlast_coll(str, pattern, opts_collator, ReplaceType.FIRST);
  }

  public static SEXP stri_locate_all_coll(SEXP str, SEXP pattern, SEXP omit_no_match, SEXP opts_collator) {
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final int length = __recycling_rule(true, str, pattern);
    final IntVector[] result = new IntVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    StringSearch matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        int beginIndex = matcher.first();
        if (beginIndex == StringSearch.DONE) {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
        } else {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (beginIndex != StringSearch.DONE) {
            occurrences.add(Range.closedOpen(beginIndex, beginIndex + matcher.getMatchLength()));
            beginIndex = matcher.next();
          }
          final IntMatrixBuilder builder = new IntMatrixBuilder(occurrences.size(), 2);
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            builder.set(j, 0, entry.lowerEndpoint());
            builder.set(j, 1, entry.upperEndpoint());
            j++;
          }
          result[i] = __locate_set_dimnames_matrix(builder);
        }
      }
    }

    return new ListVector(result);
  }

  public static SEXP stri_locate_all_regex(SEXP str, SEXP pattern, SEXP omit_no_match, SEXP opts_regex) {
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final IntVector[] result = new IntVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(1, 2));
      } else {
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(strings.getElementAsString(i));
        boolean found = matcher.find();
        if (found) {
          final List<Range<Integer>> occurrences = new LinkedList<>();
          while (found) {
            occurrences.add(Range.closedOpen(matcher.start(), matcher.end()));
            found = matcher.find();
          }
          final IntMatrixBuilder builder = new IntMatrixBuilder(occurrences.size(), 2);
          int j = 0;
          for (Range<Integer> entry : occurrences) {
            builder.set(j, 0, entry.lowerEndpoint());
            builder.set(j, 1, entry.upperEndpoint());
            j++;
          }
          result[i] = __locate_set_dimnames_matrix(builder);
        } else {
          result[i] = __locate_set_dimnames_matrix(__int_matrix_NA(omits_not_found ? 0 : 1, 2));
        }
      }
    }

    return new ListVector(result);
  }

  public static SEXP stri_locate_first_regex(SEXP str, SEXP pattern, SEXP opts_regex) {
    return __locate_firstlast_regex(str, pattern, opts_regex, ReplaceType.FIRST);
  }

  public static SEXP stri_locate_last_regex(SEXP str, SEXP pattern, SEXP opts_regex) {
    return __locate_firstlast_regex(str, pattern, opts_regex, ReplaceType.LAST);
  }

  public static SEXP stri_match_first_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) {
    throw new EvalException("TODO");
  }

  public static SEXP stri_match_last_regex(SEXP s1, SEXP s2, SEXP s3, SEXP s4) {
    throw new EvalException("TODO");
  }

  public static String _deparse(SEXP str) {
    return Deparse.deparseExp(Native.currentContext(), str);
  }
  public static void _print(String str) {
    Native.currentContext().getSession().getStdOut().println(str);
    Native.currentContext().getSession().getStdOut().flush();
  }

  public static SEXP stri_match_all_regex(SEXP str, SEXP pattern, SEXP omit_no_match, SEXP cg_missing, SEXP opts_regex) {
    final int flags = __regex_flags(opts_regex);
    final boolean omits_not_found = ((AtomicVector) omit_no_match).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector cg = (StringVector) stri_prepare_arg_string_1(cg_missing, "cg_missing");
    final String cg_string = cg.getElementAsString(0);

    for (int i = 0; i < length; i++) {
      if (patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i)) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = __stri_matrix_NA(1, 1).build();
        continue;
      }

      final String element = strings.getElementAsString(i);
      final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
      final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
      int pattern_cur_groups = matcher.groupCount();

      if(strings.isElementNA(i)) {
        result[i] = __stri_matrix_NA(1,pattern_cur_groups + 1).build();
        continue;
      }

      matcher.reset();
      int match_count = 0;
      while (matcher.find()) {
        match_count++;
      }

      if (match_count == 0) {
        int nrows = omits_not_found ? 0 : 1;
        result[i] = __stri_matrix_NA(nrows,pattern_cur_groups + 1).build();
        continue;
      }

      final LinkedList<Range<Integer>> occurrences = new LinkedList<>();
      matcher.reset();
      while (matcher.find()) {
        occurrences.add(Range.closedOpen(matcher.start(), matcher.end()));
        for (int j = 0; j < pattern_cur_groups; j++) {
          occurrences.add(Range.closedOpen(matcher.start(j+1), matcher.end(j+1)));
        }
      }

      StringMatrixBuilder cur_res = new StringMatrixBuilder(match_count, pattern_cur_groups + 1);
      int index = 0;
      for (int row = 0; row < match_count; row++) {
        for (int col = 0; col < pattern_cur_groups+1; col++) {
          Range<Integer> entery = occurrences.get(index);
          index++;
          if (entery.lowerEndpoint() < 0 || entery.upperEndpoint() < 0) {
            cur_res.setValue(row, col, cg_string);
          } else {
            String value = element.substring(entery.lowerEndpoint(), entery.upperEndpoint());
            cur_res.setValue(row, col, value);
          }
        }
      }
      result[i] = cur_res.build();
    }

    return new ListVector(result);
  }

  public static SEXP stri_numbytes(SEXP str) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final int length = strings.length();
    final int[] result = new int[length];

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = IntVector.NA;
      } else {
        result[i] = Utf8.encodedLength(strings.getElementAsString(i));
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_order(SEXP str, SEXP decreasing, SEXP na_last, SEXP opts_collator) {
    return __order_or_sort(str, decreasing, na_last, opts_collator, true);
  }

  public static SEXP stri_sort(SEXP str, SEXP decreasing, SEXP na_last, SEXP opts_collator) {
    return __order_or_sort(str, decreasing, na_last, opts_collator, false);
  }

  public static SEXP stri_pad(SEXP str, SEXP width, SEXP side, SEXP pad, SEXP use_length) {
    if (!side.getTypeName().equals(IntVector.TYPE_NAME) || side.length() != 1) {
      throw new EvalException("incorrect argument");
    }
    final int border = side.asInt();
    if (border < 0 || 2 < border) {
      throw new EvalException("incorrect argument");
    }
    final boolean does_use_length = ((AtomicVector) use_length).asLogical().toBooleanStrict();
    final int length = __recycling_rule(true, str, width, pad);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final IntVector widths = __ensure_length(length, stri_prepare_arg_integer(width, "width"));
    final StringVector pads = __ensure_length(length, stri_prepare_arg_string(pad, "pad"));
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || pads.isElementNA(i) | widths.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String padder = pads.getElementAsString(i);
        final int min_width = widths.getElementAsInt(i);
        final int element_width = does_use_length ? element.codePointCount(0, element.length()) : __width_string(element);
        final int padder_width = does_use_length ? padder.codePointCount(0, padder.length()) : __width_string(padder);
        if (padder_width != 1) {
          throw new EvalException("each string in `pad` should consist of exactly 1 code point or of total width 1");
        }
        if (min_width <= element_width) {
          result[i] = element;
        } else {
          sb.setLength(0);
          final int filling = min_width - element_width;
          int k = 0;
          switch (border) {
          case 0: // left
            for (k = 0; k < filling; k++) {
              sb.append(padder);
            }
            sb.append(element);
            break;
          case 1: // right
            sb.append(element);
            for (k = 0; k < filling; k++) {
              sb.append(padder);
            }
            break;
          case 2: // both
            for (k = 0; k < filling / 2; k++) {
              sb.append(padder);
            }
            sb.append(element);
            for (; k < filling; k++) {
              sb.append(padder);
            }
            break;
          }
          result[i] = sb.toString();
        }
      }
    }

    return new StringArrayVector(result);
  }

  public static StringVector stri_prepare_arg_string(SEXP s, String name) {
    if (s instanceof StringVector) {
      return (StringVector) s;
    } else {
      final int length = s.length();
      if (length == 1 && Logical.NA.equals(s.asLogical())) {
        return StringVector.valueOf(StringVector.NA);
      } else if (s instanceof DoubleVector) {
        return new RepStringVector((DoubleVector) s, length, 1, s.getAttributes());
      } else if (s instanceof IntVector) {
        return new RepStringVector((IntVector) s, length, 1, s.getAttributes());
      } else if (s instanceof LogicalVector) {
        return new RepStringVector((LogicalVector) s, length, 1, s.getAttributes());
      }
    }
    throw new EvalException("TODO");
  }

  public static SEXP stri_prepare_arg_POSIXct(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }

  public static DoubleVector stri_prepare_arg_double(SEXP s, String name) {
    if (s instanceof DoubleVector) {
      return (DoubleVector) s;
    } else {
      final int length = s.length();
      if (length == 1 && Logical.NA.equals(s.asLogical())) {
        return DoubleVector.valueOf(DoubleVector.NA);
      } else if (s instanceof IntVector) {
        return new RepDoubleVector((IntVector) s, length, 1, s.getAttributes());
      } else if (s instanceof StringVector) {
        return new RepDoubleVector((StringVector) s, length, 1, s.getAttributes());
      }
    }
    throw new EvalException("TODO");
  }

  public static IntVector stri_prepare_arg_integer(SEXP s, String name) {
    if (s instanceof IntVector) {
      return (IntVector) s;
    } else {
      final int length = s.length();
      if (length == 1 && Logical.NA.equals(s.asLogical())) {
        return IntVector.valueOf(IntVector.NA);
      } else if (s instanceof DoubleVector) {
        return new RepIntVector((DoubleVector) s, length, 1, s.getAttributes());
      } else if (s instanceof StringVector) {
        return new RepIntVector((StringVector) s, length, 1, s.getAttributes());
      }
    }
    throw new EvalException("TODO");
  }

  public static LogicalVector stri_prepare_arg_logical(SEXP s, String name) {
    if (s instanceof LogicalVector) {
      return (LogicalVector) s;
    }
    throw new EvalException("TODO");
  }

  public static SEXP stri_prepare_arg_raw(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }

  public static SEXP stri_prepare_arg_string_1(SEXP x, String argname) {
    if (x == Null.INSTANCE) {
      argname = "noname";
    }

    x = stri_prepare_arg_string(x, argname);
    int nx = x.length();

    if (nx <= 0) {
      Native.currentContext().warn("argument `" + argname + "` should be a non-empty vector");
      // won't come here anyway
      return x; // avoid compiler warning
    } else if (nx > 1) {
      Native.currentContext().warn("argument `" + argname + "` should be one character string; taking the first one");
      return StringVector.valueOf(((StringVector) x).getElementAsString(0));
    } else { // if (nx == 1)
      return x;
    }
  }

  public static SEXP stri_prepare_arg_double_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_integer_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_prepare_arg_logical_1(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_rand_shuffle(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_rand_strings(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }

  public static SEXP stri_replace_na(SEXP str, SEXP replacement) {
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final String na = stri_prepare_arg_string(replacement, "replacement").getElementAsString(0);

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = na;
      } else {
        result[i] = strings.getElementAsString(i);
      }
    }

    return new StringArrayVector(result);
  }

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
        final StringVector strings = stri_prepare_arg_string(str, "str");
        final StringVector patterns = stri_prepare_arg_string(pattern, "pattern");
        final StringVector replacements = __ensure_length(pattern_n, stri_prepare_arg_string(replacement, "replacement"));
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
                final StringBuilder replaced = new StringBuilder();
                int previousStart = 0;
                int beginIndex = elementNormalized.indexOf(patternNormalized);
                while (-1 < beginIndex) {
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
        final StringVector strings = stri_prepare_arg_string(str, "str");
        final StringVector patterns = stri_prepare_arg_string(pattern, "pattern");
        final StringVector replacements = __ensure_length(pattern_n, stri_prepare_arg_string(replacement, "replacement"));
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
              final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
              final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
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
        final StringVector strings = stri_prepare_arg_string(str, "str");
        final StringVector patterns = stri_prepare_arg_string(pattern, "pattern");
        final StringVector replacements = __ensure_length(pattern_n, stri_prepare_arg_string(replacement, "replacement"));
        for (int i = 0; i < pattern_n; i++) {
          if (patterns.isElementNA(i)) {
            return __string_vector_NA(length);
          }
          for (int j = 0; j < length; j++) {
            if (strings.isElementNA(j)) {
              result[j] = StringVector.NA;
            } else {
              final String element = strings.getElementAsString(j);
              final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
              if (replacements.isElementNA(i)) {
                if (Pattern.compile(normalizedPattern).matcher(element).find()) {
                  result[j] = StringVector.NA;
                } else {
                  result[j] = element;
                }
              } else {
                final String appliedPattern = (is_merging) ? "(?:" + normalizedPattern + ")+" : normalizedPattern;
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

  public static SEXP stri_reverse(SEXP str) {
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final StringBuilder sb = new StringBuilder();
    final Deque<Integer> marks = new LinkedList<>();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        sb.setLength(0);
        for (int j = element.length(); 0 < j;) {
          final int codePoint = element.codePointBefore(j);
          if (COMBINING_MARKS.contains(codePoint)) {
            marks.addFirst(codePoint);
          } else if (!marks.isEmpty()) {
            sb.appendCodePoint(codePoint);
            while (!marks.isEmpty()) {
              sb.appendCodePoint(marks.removeFirst());
            }
          } else {
            sb.appendCodePoint(codePoint);
          }
          j -= Character.charCount(codePoint);
        }
        result[i] = sb.toString();
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_split_boundaries(SEXP str, SEXP n, SEXP tokens_only, SEXP simplify, SEXP opts_brkiter) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, n);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final IntVector ns = __ensure_length(length, stri_prepare_arg_integer(n, "n"));

    for (int i = 0; i < length; i++) {
      if (ns.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        if (strings.isElementNA(i)) {
          result[i] = StringVector.valueOf(StringVector.NA);
        } else {
          int depth = ns.getElementAsInt(i);
          if (depth == 0) {
            result[i] = StringVector.EMPTY;
          } else {
            if (depth >= Integer.MAX_VALUE - 1) {
              throw new IllegalArgumentException("argument `" + depth + "`: value too large");
            } else if (depth < 0) {
              depth = Integer.MAX_VALUE;
            }
            final LinkedList<String> fields = new LinkedList<String>();
            final BreakIterator brkiter = __open_break_iterator(opts_brkiter, "line_break");
            final String element = strings.getElementAsString(i);
            brkiter.setText(strings.getElementAsString(i));
            int previousStart = brkiter.first();
            int k = 0;
            while (k < depth && BreakIterator.DONE < brkiter.next()) {
              // FIXME handle skip_* rules
              fields.add(element.substring(previousStart, brkiter.current()));
              previousStart = brkiter.current();
              ++k; // another field
            }
            if (fields.size() <= 0) {
              result[i] = StringVector.EMPTY;
            } else {
              if (k == depth && !only_tokens) {
                fields.removeLast();
                fields.add(element.substring(previousStart, element.length()));
              }
              result[i] = new StringArrayVector(fields);
            }
          }
        }
      }
    }

    return __simplify_when_required(new ListVector(result), simplify, n);
  }

  public static SEXP stri_split_charclass(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final IntVector ns = __ensure_length(length, stri_prepare_arg_integer(n, "n"));
    final LogicalVector omits = __ensure_length(length, stri_prepare_arg_logical(omit_empty, "omit_empty"));

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
          final String separatorPattern = patterns.getElementAsString(i);
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

    return __simplify_when_required(new ListVector(result), simplify, n);
  }

  public static SEXP stri_split_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5, SEXP s6, SEXP s7) { throw new EvalException("TODO"); }
  public static SEXP stri_split_fixed(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_fixed) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final IntVector ns = __ensure_length(length, stri_prepare_arg_integer(n, "n"));
    final LogicalVector omits = __ensure_length(length, stri_prepare_arg_logical(omit_empty, "omit_empty"));

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

    return __simplify_when_required(new ListVector(result), simplify, n);
  }

  public static SEXP stri_split_lines(SEXP str, SEXP omit_empty) {
    final int length = __recycling_rule(true, str, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final LogicalVector omits = __ensure_length(length, stri_prepare_arg_logical(omit_empty, "omit_empty"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.valueOf(StringVector.NA);
      } else {
        final String splitter = "\\R"; // Any Unicode linebreak sequence, is equivalent to \x000D\x000A|[\x000A\x000B\x000C\x000D\x0085\x2028\x2029]
        final String element = strings.getElementAsString(i);
        final boolean omit = omits.getElementAsLogical(i).toBooleanStrict();
        final String[] splitted = element.split(splitter);
        if (omit) {
          final List<String> filtered = Arrays.asList(splitted);
          filtered.removeAll(Collections.singletonList(""));
          result[i] = new StringArrayVector(filtered);
        } else {
          result[i] = new StringArrayVector(splitted);
        }
      }
    }

    return new ListVector(result);
  }

  public static SEXP stri_split_lines1(SEXP str) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    if (strings.isElementNA(0)) {
      return strings;
    } else {
      final String splitter = "\\R"; // Any Unicode linebreak sequence, is equivalent to \x000D\x000A|[\x000A\x000B\x000C\x000D\x0085\x2028\x2029]
      final String[] splitted = strings.getElementAsString(0).split(splitter);
      return new StringArrayVector(splitted);
    }
  }

  public static SEXP stri_split_regex(SEXP str, SEXP pattern, SEXP n, SEXP omit_empty, SEXP tokens_only, SEXP simplify, SEXP opts_regex) {
    final boolean only_tokens = ((AtomicVector) tokens_only).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern, n, omit_empty);
    final StringVector[] result = new StringVector[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final IntVector ns = __ensure_length(length, stri_prepare_arg_integer(n, "n"));
    final LogicalVector omits = __ensure_length(length, stri_prepare_arg_logical(omit_empty, "omit_empty"));

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
              final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
              final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
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

    return __simplify_when_required(new ListVector(result), simplify, n);
  }

  public static SEXP stri_startswith_charclass(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_startswith_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_startswith_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_stats_general(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_stats_latex(SEXP s1) { throw new EvalException("TODO"); }

  public static SEXP stri_sub(SEXP str, SEXP from, SEXP to, SEXP length) {
    final SEXP dims = from.getAttribute(Symbols.DIM);
    if (Types.isMatrix(from) && 2 < ((IntVector) dims).getElementAsInt(1)) {
      throw new EvalException("argument `from` should be a matrix with 2 columns");
    }
    final boolean from_ismatrix = Types.isMatrix(from) && 2 == ((IntVector) dims).getElementAsInt(1);
    IntVector froms = stri_prepare_arg_integer(from, "from");
    IntVector tos = IntVector.EMPTY;
    IntVector lengths = IntVector.EMPTY;
    if (from_ismatrix) {
      tos = new ColumnIntVector(new Matrix(froms, 2), 1);
      froms = new RepIntVector(froms, froms.length() / 2, 1, from.getAttributes());
    } else if (Types.isNull(length)) {
      tos = stri_prepare_arg_integer(to, "to");
    } else {
      lengths = stri_prepare_arg_integer(length, "length");
    }
    final int flen = froms.length();
    final int tlen = tos.length();
    final int llen = lengths.length();
    final boolean uselengths = (tlen == 0) && (llen > 0);
    final int vectorize = __recycling_rule(true, str, froms, (tlen > llen) ? tos : lengths);

    if (vectorize <= 0) {
      return StringVector.EMPTY;
    } else {
      final String[] result = new String[vectorize];
      final StringVector strings = __ensure_length(vectorize, stri_prepare_arg_string(str, "str"));
      for (int i = 0; i < vectorize; i++) {
        int beginIndex = froms.getElementAsInt(i % flen);
        int endIndex = uselengths ? lengths.getElementAsInt(i % llen) : tos.getElementAsInt(i % tlen);
        if (strings.isElementNA(i) || beginIndex == IntVector.NA || endIndex == IntVector.NA) {
          result[i] = StringVector.NA;
        } else {
          if (uselengths && endIndex <= 0) {
            result[i] = "";
          } else {
            final String element = strings.getElementAsString(i);
            final Range<Integer> bounds = __adjust_bounds(uselengths, beginIndex, endIndex, element);
            beginIndex = bounds.lowerEndpoint();
            endIndex = bounds.upperEndpoint();
            if (beginIndex < endIndex) {
              result[i] = element.substring(beginIndex, endIndex);
            } else {
              result[i] = "";
            }
          }
        }
      }

      return new StringArrayVector(result);
    }
  }

  public static SEXP stri_sub_replacement(SEXP str, SEXP from, SEXP to, SEXP length, SEXP omit_na, SEXP value) {
    final SEXP dims = from.getAttribute(Symbols.DIM);
    if (Types.isMatrix(from) && 2 < ((IntVector) dims).getElementAsInt(1)) {
      throw new EvalException("argument `from` should be a matrix with 2 columns");
    }
    final boolean from_ismatrix = Types.isMatrix(from) && 2 == ((IntVector) dims).getElementAsInt(1);
    IntVector froms = stri_prepare_arg_integer(from, "from");
    IntVector tos = IntVector.EMPTY;
    IntVector lengths = IntVector.EMPTY;
    if (from_ismatrix) {
      tos = new ColumnIntVector(new Matrix(froms, 2), 1);
      froms = new RepIntVector(froms, froms.length() / 2, 1, from.getAttributes());
    } else if (Types.isNull(length)) {
      tos = stri_prepare_arg_integer(to, "to");
    } else {
      lengths = stri_prepare_arg_integer(length, "length");
    }
    final int flen = froms.length();
    final int tlen = tos.length();
    final int llen = lengths.length();
    final boolean uselengths = (tlen == 0) && (llen > 0);
    final int vectorize = __recycling_rule(true, str, froms, (tlen > llen) ? tos : lengths);

    if (vectorize <= 0) {
      return StringVector.EMPTY;
    } else {
      final boolean does_omit_na = ((AtomicVector) omit_na).getElementAsLogical(0).toBooleanStrict();
      final String[] result = new String[vectorize];
      final StringVector strings = __ensure_length(vectorize, stri_prepare_arg_string(str, "str"));
      final StringVector values = __ensure_length(vectorize, stri_prepare_arg_string(value, "value"));
      for (int i = 0; i < vectorize; i++) {
        int beginIndex = froms.getElementAsInt(i % flen);
        int endIndex = uselengths ? lengths.getElementAsInt(i % llen) : tos.getElementAsInt(i % tlen);
        if (strings.isElementNA(i) || values.isElementNA(i) || beginIndex == IntVector.NA || endIndex == IntVector.NA) {
          if (does_omit_na) {
            // if strings(i) is NA, this will be NA_STRING as well.
            result[i] = strings.getElementAsString(i);
          } else {
            result[i] = StringVector.NA;
          }
        } else {
          if (uselengths && endIndex <= 0) {
            result[i] = "";
          } else {
            final String element = strings.getElementAsString(i);
            final String replacement = values.getElementAsString(i);
            final Range<Integer> bounds = __adjust_bounds(uselengths, beginIndex, endIndex, element);
            beginIndex = bounds.lowerEndpoint();
            endIndex = bounds.upperEndpoint();
            if (endIndex < beginIndex) {
              endIndex = beginIndex;
            }
            result[i] = element.substring(0, beginIndex) + replacement + element.substring(endIndex, element.length());
          }
        }
      }

      return new StringArrayVector(result);
    }
  }

  public static SEXP stri_subset_charclass(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_coll(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_fixed(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_regex(SEXP str, SEXP pattern, SEXP omit_na, SEXP negate, SEXP opts_regex) {
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final boolean does_omit_na = ((AtomicVector) omit_na).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final Logical[] which = new Logical[length];

    int found = 0;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        if (does_omit_na) {
          which[i] = Logical.FALSE;
        } else {
          which[i] = Logical.NA;
          found++;
        }
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        which[i] = is_negating ? Logical.valueOf(!matcher.find()) : Logical.valueOf(matcher.find());
        if (Logical.TRUE.equals(which[i])) {
          found++;
        }
      }
    }

    return __subset_by_logical(strings, which, found);
  }
  public static SEXP stri_subset_charclass_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_coll_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }
  public static SEXP stri_subset_fixed_replacement(SEXP s1, SEXP s2, SEXP s3, SEXP s4, SEXP s5) { throw new EvalException("TODO"); }

  public static SEXP stri_subset_regex_replacement(SEXP str, SEXP pattern, SEXP negate, SEXP opts_regex, SEXP value) {
    final int value_length = value.length();
    if (value_length <= 0) {
      throw new EvalException("replacement has length zero");
    }
    final boolean is_negating = ((AtomicVector) negate).getElementAsLogical(0).toBooleanStrict();
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector values = stri_prepare_arg_string(value, "value");

    int k = 0;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        final boolean found = is_negating ? !matcher.find() : matcher.find();
        if (found) {
          result[i] = values.getElementAsString((k++)%value_length);
        } else {
          result[i] = strings.getElementAsString(i);
        }
      }
    }

    return new StringArrayVector(result);
  }
  public static SEXP stri_test_Rmark(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_returnasis(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer16(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer16b(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_test_UnicodeContainer8(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_list(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_set(SEXP s1) { throw new EvalException("TODO"); }
  public static SEXP stri_timezone_info(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_char(SEXP s1, SEXP s2, SEXP s3) { throw new EvalException("TODO"); }

  public static SEXP stri_trans_isnfc(SEXP str) {
    return __trans_isnf(str, Normalizer2.getNFCInstance());
  }

  public static SEXP stri_trans_isnfd(SEXP str) {
    return __trans_isnf(str, Normalizer2.getNFDInstance());
  }

  public static SEXP stri_trans_isnfkc(SEXP str) {
    return __trans_isnf(str, Normalizer2.getNFKCInstance());
  }

  public static SEXP stri_trans_isnfkd(SEXP str) {
    return __trans_isnf(str, Normalizer2.getNFKDInstance());
  }

  public static SEXP stri_trans_isnfkc_casefold(SEXP str) {
    return __trans_isnf(str, Normalizer2.getNFKCCasefoldInstance());
  }

  public static SEXP stri_trans_general(SEXP s1, SEXP s2) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_list(SEXP s1, SEXP s0) { throw new EvalException("TODO"); }
  public static SEXP stri_trans_nfc(SEXP str) {
    return __trans_nf(str, Normalizer2.getNFCInstance());
  }

  public static SEXP stri_trans_nfd(SEXP str) {
    return __trans_nf(str, Normalizer2.getNFDInstance());
  }

  public static SEXP stri_trans_nfkc(SEXP str) {
    return __trans_nf(str, Normalizer2.getNFKCInstance());
  }

  public static SEXP stri_trans_nfkd(SEXP str) {
    return __trans_nf(str, Normalizer2.getNFKDInstance());
  }

  public static SEXP stri_trans_nfkc_casefold(SEXP str) {
    return __trans_nf(str, Normalizer2.getNFKCCasefoldInstance());
  }

  public static SEXP stri_trans_totitle(SEXP str, SEXP opts_brkiter) {
    final BreakIterator brkiter = __open_break_iterator(opts_brkiter, "word");
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final StringBuilder sb = new StringBuilder();
    final CaseMap.Title mapper = CaseMap.toTitle();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        sb.setLength(0);
        result[i] = mapper.apply(brkiter.getLocale(ULocale.ACTUAL_LOCALE).toLocale(), brkiter, element, sb, null).toString();
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_trans_tolower(SEXP str, SEXP locale) {
    final Locale language = Types.isNull(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale.asString());
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final StringBuilder sb = new StringBuilder();
    final CaseMap.Lower mapper = CaseMap.toLower();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        sb.setLength(0);
        result[i] = mapper.apply(language, element, sb, null).toString();
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_trans_toupper(SEXP str, SEXP locale) {
    final Locale language = Types.isNull(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale.asString());
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final StringBuilder sb = new StringBuilder();
    final CaseMap.Upper mapper = CaseMap.toUpper();

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        sb.setLength(0);
        result[i] = mapper.apply(language, element, sb, null).toString();
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_trim_both(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.BOTH);
  }

  public static SEXP stri_trim_left(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.LEADING);
  }

  public static SEXP stri_trim_right(SEXP str, SEXP pattern) {
    return __trim_left_right(str, pattern, TrimOption.TRAILING);
  }

  public static SEXP stri_unescape_unicode(SEXP str) {
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        if (element.length() == 0) {
          result[i] = element;
        } else {
          result[i] = __unescape_unicode(element);
        }
      }
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_unique(SEXP str, SEXP opts_collator) {
    final int length = str.length();
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final LinkedHashSet<CollatedString> unique = new LinkedHashSet<>();

    boolean no_na = true;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        if (no_na) {
          unique.add(new CollatedString(collator, StringVector.NA));
          no_na = false;
        }
      } else {
        unique.add(new CollatedString(collator, strings.getElementAsString(i)));
      }
    }
    final String[] result = new String[unique.size()];
    int j = 0;
    for (CollatedString cs : unique) {
      result[j++] = cs.str;
    }

    return new StringArrayVector(result);
  }

  public static SEXP stri_width(SEXP str) {
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final int length = strings.length();
    final int[] result = new int[length];

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = IntVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        result[i] = __width_string(element);
      }
    }

    return new IntArrayVector(result);
  }

  public static SEXP stri_wrap(SEXP str, SEXP width, SEXP cost_exponent, SEXP indent, SEXP exdent, SEXP prefix, SEXP initial, SEXP whitespace_only,
      SEXP use_length, SEXP locale) {
    final int indent_val = indent.asInt();
    if (indent_val < 0) {
      throw new EvalException("argument `indent`: expected a positive numeric value");
    }
    final int exdent_val = exdent.asInt();
    if (exdent_val < 0) {
      throw new EvalException("argument `exdent`: expected a positive numeric value");
    }
    final String prefix_val = prefix.asString();
    final String initial_val = initial.asString();
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final int length = strings.length();
    final StringVector[] result = new StringVector[length];
    if (prefix_val == StringVector.NA || initial_val == StringVector.NA) {
      for (int i = 0; i < length; i++) {
        result[i] = StringVector.valueOf(StringVector.NA);
      }
      return new ListVector(result);
    } else {
      final UnicodeSet whitespaces = new UnicodeSet("\\p{WHITE_SPACE}");
      final int width_val = Math.max(0, width.asInt());
      final double exponent_val = cost_exponent.asReal();
      final boolean does_use_length = use_length.asLogical().toBooleanStrict();
      final boolean breaks_on_whitespace_only = whitespace_only.asLogical().toBooleanStrict();
      final Locale language = Types.isNull(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale.asString());
      final BreakIterator brkiter = BreakIterator.getLineInstance(language);
      // prepare indent/exdent/prefix/initial stuff:
      // 1st line, 1st para (i==0, u==0): initial+indent
      // nth line, 1st para (i==0, u> 0): prefix +exdent
      // 1st line, nth para (i> 0, u==0): prefix +indent
      // nth line, nth para (i> 0, u> 0): prefix +exdent
      final WrapLine ii = new WrapLine(initial_val, indent_val);
      final WrapLine pi = new WrapLine(prefix_val, indent_val);
      final WrapLine pe = new WrapLine(prefix_val, exdent_val);

      for (int i = 0; i < length; i++) {
        if (strings.isElementNA(i)) {
          result[i] = StringVector.valueOf(StringVector.NA);
        } else {
          final LinkedList<Integer> occurrences = new LinkedList<>();
          final String element = strings.getElementAsString(i);
          final int elemlen = element.length();
          brkiter.setText(element);
          int match = brkiter.first();
          while (match != BreakIterator.DONE) {
            if (breaks_on_whitespace_only) {
              if (0 < match && match < elemlen) {
                if (whitespaces.contains(element.codePointAt(match - 1))) {
                  occurrences.add(match);
                }
              } else {
                occurrences.add(match);
              }
            } else {
              occurrences.add(match);
            }
            match = brkiter.next();
          }
          if (occurrences.size() <= 1) {
            result[i] = StringVector.valueOf(element);
          } else {
            occurrences.removeFirst(); // only interested in end positions
            final int nwords = occurrences.size();
            // convert occurrences in order to obtain end positions (in a string) of each "words",
            final int[] end_pos_orig = new int[nwords];
            // we'll get the end positions without trailing whitespaces
            final int[] end_pos_trim = new int[nwords];
            // we'll get the total widths/number of code points in each "word"
            final int[] widths_orig = new int[nwords];
            // we'll get the total widths/number of code points without trailing whitespaces
            final int[] widths_trim = new int[nwords];
            int k = 0;
            for (Integer position : occurrences) {
              end_pos_orig[k++] = position;
            }
            int j = 0;
            int cur_block = 0;
            int cur_width_orig = 0;
            int cur_width_trim = 0;
            int cur_count_orig = 0;
            int cur_count_trim = 0;
            int cur_end_pos_trim = 0;
            while (j < elemlen) {
              int jlast = j;
              final int c = element.codePointAt(j);
              j += Character.charCount(c);
              // if (linebreaks.contains(c))
              // throw StriException(MSG__NEWLINE_FOUND);

              cur_width_orig += __width_char(c);
              ++cur_count_orig;
              if (whitespaces.contains(c)) {
                // NEW: trim just one white space at the end:
                cur_width_trim = __width_char(c);
                cur_count_trim = 1;
                cur_end_pos_trim = jlast;
              } else {
                cur_width_trim = 0;
                cur_count_trim = 0;
                cur_end_pos_trim = j;
              }

              if (j >= elemlen || end_pos_orig[cur_block] <= j) {
                // we'll start a new block in a moment
                if (does_use_length) {
                  widths_orig[cur_block] = cur_count_orig;
                  widths_trim[cur_block] = cur_count_orig - cur_count_trim;
                } else {
                  widths_orig[cur_block] = cur_width_orig;
                  widths_trim[cur_block] = cur_width_orig - cur_width_trim;
                }
                end_pos_trim[cur_block] = cur_end_pos_trim;
                cur_block++;
                cur_width_orig = 0;
                cur_width_trim = 0;
                cur_count_orig = 0;
                cur_count_trim = 0;
                cur_end_pos_trim = j;
              }
            }
            // do wrap
            final int add_para_1 = (does_use_length) ? ((i == 0) ? ii.count : pi.count) : ((i == 0) ? ii.width : pi.width);
            final int add_para_n = (does_use_length) ? pe.count : pe.width;
            // @formatter:off
            Deque<Integer> wrap_after = (exponent_val <= 0.0)
                ? __wrap_greedy(nwords, width_val, widths_orig, widths_trim, add_para_1, add_para_n)
                : __wrap_dynamic(nwords, width_val, exponent_val, widths_orig, widths_trim, add_para_1, add_para_n);
            // @formatter:on
            // wrap_after.size() line breaks => wrap_after.size()+1 lines
            final int nlines = wrap_after.size() + 1;
            final String[] answer = new String[nlines];
            final StringBuilder sb = new StringBuilder();
            int previousStart = 0;
            int u = 0;
            for (Integer after : wrap_after) {
              final int endIndex = end_pos_trim[after];
              sb.setLength(0);
              // @formatter:off
              if (i == 0 && u == 0)     sb.append(ii.text);
              else if (i > 0 && u == 0) sb.append(pi.text);
              else                      sb.append(pe.text);
              // @formatter:on
              sb.append(element.substring(previousStart, endIndex));
              answer[u++] = sb.toString();
              previousStart = end_pos_orig[after];
            }
            // last line goes here:
            sb.setLength(0);
            // @formatter:off
            if (i == 0 && nlines-1 == 0)     sb.append(ii.text);
            else if (i > 0 && nlines-1 == 0) sb.append(pi.text);
            else                             sb.append(pe.text);
            // @formatter:on
            sb.append(element.substring(previousStart, end_pos_trim[nwords - 1]));
            answer[nlines-1] = sb.toString();
            result[i] = new StringArrayVector(answer);
          }
        }
      }

      return new ListVector(result);
    }
  }

  private static class WrapLine {
    protected final String text;
    protected final int count;
    protected final int width;
    protected WrapLine(String text, int added) {
      this.text = text;
      this.count = text.codePointCount(0, text.length()) + added;
      this.width = __width_string(text) + added;
    }
  }

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

  private static final class CollatedString {
    private final RuleBasedCollator collator;
    private final String str;
    private final int hash;

    private CollatedString(RuleBasedCollator collator, String str) {
      super();
      this.collator = collator;
      this.str = str;
      // return the same hash for decomposed strings and let collator decide for equals
      this.hash = Normalizer2.getNFKDInstance().normalize(str).toUpperCase().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof CollatedString) {
        final int cmp = collator.compare(str, ((CollatedString) obj).str);
        return 0 == cmp;
      } else {
        return false;
      }
    }
    @Override
    public int hashCode() {
      return hash;
    }
  }

  // @formatter:off
  private static final UnicodeSet COMBINING_MARKS = new UnicodeSet()
      .add(0x0300, 0x036F).add(0x1AB0, 0x1AFF).add(0x1DC0, 0x1DFF) // combining diacritics on letters
      .add(0x20D0, 0x20FF).add(0xFE20, 0xFE2F); // combining marks on symbols
  // A pattern that matches an escape.
  // What follows the escape indicator is captured by group 1=character 2=octal 3=Unicode.
  private static final Pattern ESCAPES = Pattern
      .compile("\\\\(?:(b|t|n|f|r|\\\"|\\\'|\\\\)|((?:[0-3]?[0-7])?[0-7])|[uU]+(\\p{XDigit}{4,8}))");
  // @formatter:on

  private static String __unescape_unicode(CharSequence encoded) {
    // based on code found in answer to StackOverflow question
    // https://stackoverflow.com/questions/3537706/how-to-unescape-a-java-string-literal-in-java#answer-13278219
    final Matcher matcher = ESCAPES.matcher(encoded);
    final StringBuilder decoded = new StringBuilder();
    // Find each escape of the encoded string in succession.
    int previousStart = 0;
    while (matcher.find()) {
      decoded.append(encoded.subSequence(previousStart, matcher.start()));
      if (matcher.start(1) >= 0) { // Decode a character escape.
        final char ch = matcher.group(1).charAt(0);
        switch (ch) {
          // @formatter:off
          case 'b': decoded.append('\b'); break;
          case 'f': decoded.append('\f'); break;
          case 'n': decoded.append('\n'); break;
          case 'r': decoded.append('\r'); break;
          case 't': decoded.append('\t'); break;
          default: decoded.append(ch);
            // @formatter:on
        }
      } else if (matcher.start(2) >= 0) { // Decode an octal escape.
        decoded.appendCodePoint(Integer.parseInt(matcher.group(2), 8));
      } else /* if (matcher.start(3) >= 0) */ { // Decode a Unicode escape.
        decoded.appendCodePoint(Integer.parseInt(matcher.group(3), 16));
      }
      previousStart = matcher.end();
    }
    decoded.append(encoded.subSequence(previousStart, encoded.length()));

    return decoded.toString();
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
      return new RepStringVector(exp, length, 1, exp.getAttributes());
    }
  }

  private static IntVector __ensure_length(int length, IntVector exp) {
    final int expLength = exp.length();
    if (length == expLength) {
      return exp;
    } else {
      return new RepIntVector(exp, length, 1, exp.getAttributes());
    }
  }

  private static LogicalVector __ensure_length(int length, LogicalVector exp) {
    final int expLength = exp.length();
    if (length == expLength) {
      return exp;
    } else {
      return new RepLogicalVector(exp, length, 1, exp.getAttributes());
    }
  }

  private static SEXP __trim_left_right(SEXP str, SEXP pattern, TrimOption side) {
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

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
        throw new EvalException("fixed search engine config failed");
      }
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          throw new EvalException("fixed search engine config failed");
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

  private static String __binary_properties_to_Java(String pattern) {
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

  private static IntMatrixBuilder __int_matrix_0(int nrows, int ncols) {
    final IntMatrixBuilder builder = new IntMatrixBuilder(nrows, ncols);
    for (int i = 0; i < nrows; i++) {
      for (int j = 0; j < ncols; j++) {
        builder.set(i, j, 0);
      }
    }

    return builder;
  }

  private static IntMatrixBuilder __int_matrix_NA(int nrows, int ncols) {
    final IntMatrixBuilder builder = new IntMatrixBuilder(nrows, ncols);
    for (int i = 0; i < nrows; i++) {
      for (int j = 0; j < ncols; j++) {
        builder.set(i, j, IntVector.NA);
      }
    }

    return builder;
  }

  private static StringMatrixBuilder __stri_matrix_NA(int nrows, int ncols) {
    final StringMatrixBuilder builder = new StringMatrixBuilder(nrows, ncols);
    for (int i = 0; i < nrows; i++) {
      for (int j = 0; j < ncols; j++) {
        builder.setValue(i, j, StringVector.NA);
      }
    }
    return builder;
  }

  private static StringVector __string_vector_NA(final int length) {
    final StringVector.Builder builder = StringVector.newBuilder();
    for (int j = 0; j < length; j++) {
      builder.addNA();
    }

    return builder.build();
  }

  private static SEXP __replace_firstlast_charclass(SEXP str, SEXP pattern, SEXP replacement, ReplaceType replaces) {
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector replacements = __ensure_length(length, stri_prepare_arg_string(replacement, "replacement"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
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
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector replacements = __ensure_length(length, stri_prepare_arg_string(replacement, "replacement"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String patterni = (is_merging) ? "(?:" + patterns.getElementAsString(i) + ")+" : patterns.getElementAsString(i);
        result[i] = strings.getElementAsString(i).replaceAll(__binary_properties_to_Java(patterni), replacements.getElementAsString(i));
      }
    }

    return new StringArrayVector(result);
  }
  private static SEXP __replace_all_fixed_vectorized(SEXP str, SEXP pattern, SEXP replacement, SEXP opts_fixed, ReplaceType replaces) {
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern, replacement);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector replacements = __ensure_length(length, stri_prepare_arg_string(replacement, "replacement"));

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
            final StringBuilder replaced = new StringBuilder();
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
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));
    final StringVector replacements = __ensure_length(length, stri_prepare_arg_string(replacement, "replacement"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || replacements.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final String replacement_i = replacements.getElementAsString(i);
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
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

  private static int __width_string(String element) {
    int width = 0;
    for (int j = 0; j < element.length(); /* j + charCount */) {
      final int cp = element.codePointAt(j);
      width += __width_char(cp);
      j += Character.charCount(cp);
    }

    return width;
  }

  private static int __width_char(int codePoint) {
    if (codePoint == 0x00AD) {
      return 1; /* SOFT HYPHEN */
    }
    if (codePoint == 0x200B) {
      return 0; /* ZERO WIDTH SPACE */
    }

    /* GC: Me, Mn, Cf, Cc -> width = 0 */
    final int category = Character.getType(codePoint);
    if (category == Character.NON_SPACING_MARK | category == Character.ENCLOSING_MARK | category == Character.FORMAT | category == Character.CONTROL) {
      return 0;
    }

    /* Hangul Jamo medial vowels and final consonants have width 0 */
    int hangul = UCharacter.getIntPropertyValue(codePoint, UProperty.HANGUL_SYLLABLE_TYPE);
    if (hangul == HangulSyllableType.VOWEL_JAMO || hangul == HangulSyllableType.TRAILING_JAMO) {
      return 0;
    }

    /*
     * Characters with the \code{UCHAR_EAST_ASIAN_WIDTH} enumerable property equal to \code{U_EA_FULLWIDTH} or \code{U_EA_WIDE} are of width 2.
     */
    int width = UCharacter.getIntPropertyValue(codePoint, UProperty.EAST_ASIAN_WIDTH);
    if (width == EastAsianWidth.FULLWIDTH || width == EastAsianWidth.WIDE) {
      return 2;
    }

    /* any other characters have width 1 */
    return 1;
  }

  private static SEXP __trans_isnf(SEXP str, Normalizer2 normalizer) {
    final int length = str.length();
    final Logical[] result = new Logical[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = Logical.NA;
      } else {
        result[i] = Logical.valueOf(normalizer.isNormalized(strings.getElementAsString(i)));
      }
    }

    return new LogicalArrayVector(result);
  }

  private static SEXP __trans_nf(SEXP str, Normalizer2 normalizer) {
    final int length = str.length();
    final String[] result = new String[length];
    final StringVector strings = stri_prepare_arg_string(str, "str");

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        result[i] = normalizer.normalize(strings.getElementAsString(i));
      }
    }

    return new StringArrayVector(result);
  }

  private static BreakIterator __break_iterator_for_type(String breakType, Locale locale) {
    if ("character".equals(breakType)) {
      return BreakIterator.getCharacterInstance(locale);
    } else if ("line_break".equals(breakType)) {
      return BreakIterator.getLineInstance(locale);
    } else if ("sentence".equals(breakType)) {
      return BreakIterator.getSentenceInstance(locale);
    } else if ("title".equals(breakType)) {
      return BreakIterator.getTitleInstance(locale);
    } else if ("word".equals(breakType)) {
      return BreakIterator.getWordInstance(locale);
    }
    throw new EvalException("incorrect break iterator option specifier. see ?stri_opts_brkiter");
  }

  private static BreakIterator __open_break_iterator(SEXP opts_brkiter, String defaultType) {
    if (Types.isNull(opts_brkiter)) {
      // use default locale
      // use default type
      // use no skip rules
      return __break_iterator_for_type(defaultType, Locale.getDefault());
    } else if (opts_brkiter instanceof ListVector) {
      final ListVector options = (ListVector) opts_brkiter;
      final int indexType = options.getIndexByName("type");
      final String breakType = (-1 < indexType) ? options.getElementAsString(indexType) : defaultType;
      final String knownTypes = "|character|line_break|sentence|title|word|";
      if (-1 < knownTypes.indexOf(breakType)) {
        final int indexLocale = options.getIndexByName("locale");
        final Locale locale = (-1 < indexLocale) ? Locale.forLanguageTag(options.getElementAsString(indexLocale).trim()) : Locale.getDefault();
        return __break_iterator_for_type(breakType, locale);
      } else {
        // FIXME handle skip_* rules
        return new RuleBasedBreakIterator(breakType);
      }
    } else {
      throw new EvalException("incorrect break iterator option specifier. see ?stri_opts_brkiter");
    }
  }

  private static RuleBasedCollator __open_collator(SEXP opts_collator) {
    final int narg = (opts_collator == null) ? 0 : opts_collator.length();
    if (narg <= 0) {
      return (RuleBasedCollator) Collator.getInstance();
    } else {
      final ListVector options = (ListVector) opts_collator;
      final StringVector names = (StringVector) opts_collator.getAttribute(Symbols.NAMES);
      if (names == null || narg != names.length()) {
        throw new EvalException("incorrect collator option specifier. see ?stri_opts_collator");
      }
      Locale locale = Locale.getDefault();
      int strength = Collator.TERTIARY;
      Boolean shifted = null;
      Boolean upperfirst = null;
      Boolean lowerfirst = null;
      Boolean french = null;
      Boolean caselevel = null;
      Integer decomposing = null;
      Boolean numeric = null;
      for (int i = 0; i < narg; i++) {
        if (names.isElementNA(i)) {
          throw new EvalException("incorrect collator option specifier. see ?stri_opts_collator");
        }
        final String name = names.getElementAsString(i);
        if ("locale".equals(name)) {
          locale = Locale.forLanguageTag(options.getElementAsString(i).replace('_', '-'));
        } else if ("strength".equals(name)) {
          strength = options.getElementAsInt(i) - 1;
          if (strength < Collator.PRIMARY) {
            strength = Collator.PRIMARY;
          } else if (Collator.IDENTICAL < strength) {
            strength = Collator.IDENTICAL;
          }
        } else if ("alternate_shifted".equals(name)) {
          shifted = options.getElementAsLogical(i).toBooleanStrict();
        } else if ("uppercase_first".equals(name)) {
          final Logical upfirst = options.getElementAsLogical(i);
          if (Logical.NA.equals(upfirst)) {
            upperfirst = false;
            lowerfirst = false;
          } else if (Logical.TRUE.equals(upfirst)) {
            upperfirst = true;
          } else if (Logical.FALSE.equals(upfirst)) {
            lowerfirst = true;
          }
        } else if ("french".equals(name)) {
          french = options.getElementAsLogical(i).toBooleanStrict();
        } else if ("case_level".equals(name)) {
          caselevel = options.getElementAsLogical(i).toBooleanStrict();
        } else if ("normalization".equals(name)) {
          final boolean normalized = options.getElementAsLogical(i).toBooleanStrict();
          if (normalized) {
            decomposing = Collator.CANONICAL_DECOMPOSITION;
          } else {
            decomposing = Collator.NO_DECOMPOSITION;
          }
        } else if ("numeric".equals(name)) {
          numeric = options.getElementAsLogical(i).toBooleanStrict();
        } else {
          Native.currentContext().warn("incorrect opts_collator setting: `" + name + "`. ignoring");
        }
      }
      final RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(locale);
      collator.setStrength(strength);
      if (shifted != null) {
        collator.setAlternateHandlingShifted(shifted);
      }
      if (upperfirst != null) {
        collator.setUpperCaseFirst(upperfirst);
      }
      if (lowerfirst != null) {
        collator.setLowerCaseFirst(lowerfirst);
      }
      if (french != null) {
        collator.setFrenchCollation(french);
      }
      if (caselevel != null) {
        collator.setCaseLevel(caselevel);
      }
      if (decomposing != null) {
        collator.setDecomposition(decomposing);
      }
      if (numeric != null) {
        collator.setNumericCollation(numeric);
      }

      return collator;
    }
  }

  private static SEXP __join2_with_collapse(SEXP s1, SEXP s2, SEXP collapse) {
    if (Types.isNull(collapse)) {
      return stri_join2(s1, s2);
    }
    final StringVector collapsers = stri_prepare_arg_string(collapse, "collapse");
    if (collapsers.isElementNA(0)) {
      return __string_vector_NA(1);
    }
    if (s1.length() <= 0) {
      return s1;
    }
    if (s2.length() <= 0) {
      return s2;
    }

    final int length = __recycling_rule(true, s1, s2);
    final StringVector e1 = __ensure_length(length, stri_prepare_arg_string(s1, "e1"));
    final StringVector e2 = __ensure_length(length, stri_prepare_arg_string(s2, "e2"));

    for (int i = 0; i < length; i++) {
      if (e1.isElementNA(i) || e2.isElementNA(i)) {
        return __string_vector_NA(1);
      }
    }
    final String collapser = collapsers.getElementAsString(0);
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(e1.getElementAsString(i));
      sb.append(e2.getElementAsString(i));
      sb.append(collapser);
    }
    sb.setLength(sb.length() - collapser.length());

    return StringVector.valueOf(sb.toString());
  }

  private static SEXP __join_no_collapse(SEXP strlist, SEXP sep, SEXP ignore_null) {
    final boolean ignore_empty = ((AtomicVector) ignore_null).getElementAsLogical(0).toBooleanStrict();
    final ListVector lists = (ListVector) __prepare_arg_list_ignore_empty(strlist, ignore_empty);
    final int strlist_length = lists.length();

    if (strlist_length <= 0) {
      return StringVector.EMPTY;
    } else {
      int vectorize_length = 0;
      for (int i = 0; i < strlist_length; i++) {
        final int current_length = lists.getElementAsSEXP(i).length();
        if (current_length <= 0) {
          return StringVector.EMPTY;
        } else if (current_length > vectorize_length) {
          vectorize_length = current_length;
        }
      }
      final StringVector separators = stri_prepare_arg_string(sep, "sep");
      if (separators.isElementNA(0)) {
        return __string_vector_NA(vectorize_length);
      }
      if (separators.getElementAsString(0).length() == 0 && strlist_length == 2) {
        // sep==empty string and 2 vectors --
        // an often occurring case - we have some specialized functions for this :-)
        return stri_join2(lists.getElementAsSEXP(0), lists.getElementAsSEXP(1));
      }

      final boolean[] whichNA = new boolean[vectorize_length];
      Arrays.fill(whichNA, false);
      final StringVector[] vectorized = new StringVector[strlist_length];
      for (int j = 0; j < strlist_length; j++) {
        vectorized[j] = __ensure_length(vectorize_length, stri_prepare_arg_string(strlist.getElementAsSEXP(j), "strlist" + j));
        for (int i = 0; i < vectorize_length; i++) {
          if (vectorized[j].isElementNA(i)) {
            whichNA[i] = true;
          }
        }
      }
      final String separator = separators.getElementAsString(0);
      final String[] result = new String[vectorize_length];

      for (int i = 0; i < vectorize_length; i++) {
        if (whichNA[i]) {
          result[i] = StringVector.NA;
        } else {
          final StringBuilder sb = new StringBuilder(vectorized[0].getElementAsString(i));
          for (int j = 1; j < strlist_length; j++) {
            sb.append(separator);
            sb.append(vectorized[j].getElementAsString(i));
          }
          result[i] = sb.toString();
        }
      }

      return new StringArrayVector(result);
    }
  }

  private static org.renjin.sexp.Vector __prepare_arg_list_raw(SEXP x, String name) {
    if (name == null) {
      name = "<noname>";
    }
    if (Types.isNull(x) || Types.isRaw(x)) {
      return new ListVector(x);
    } else if (x instanceof ListVector) {
      final ListVector list = (ListVector) x;
      for (SEXP entry : list) {
        if (!(Types.isNull(entry) || Types.isRaw(entry))) {
          throw new EvalException("all elements in `" + name + "` should be raw vectors");
        }
      }
      return list;
    } else {

      return stri_prepare_arg_string(x, name);
    }
  }

  private static SEXP __prepare_arg_list_ignore_empty(SEXP x, boolean ignore_empty) {
    if (!ignore_empty) {
      return x;
    }
    final int length = x.length();
    if (length <= 0) {
      return x;
    }
    final ListVector xs = (ListVector) x;
    final List<SEXP> result = new LinkedList<SEXP>();
    for (int i = 0; i < length; i++) {
      final SEXP element = xs.getElementAsSEXP(i);
      if (0 < element.length()) {
        result.add(element);
      }
    }

    return new ListVector(result);
  }

private static SEXP __locate_firstlast_charclass(SEXP str, SEXP pattern, ReplaceType replaces) {
    final int length = __recycling_rule(true, str, pattern);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    final IntMatrixBuilder builder = new IntMatrixBuilder(length, 2);
    for (int i = 0; i < length; i++) {
      builder.set(i, 0, IntVector.NA);
      builder.set(i, 1, IntVector.NA);
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        /* nothing */
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        if (replaces.isFirst()) {
          final int beginIndex = matcher.span(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
          if (-1 < beginIndex) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex);
          }
        } else if (replaces.isLast()) {
          final int beginIndex = matcher.spanBack(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
          if (-1 < beginIndex) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex);
          }
        }
      }
    }

    return __locate_set_dimnames_matrix(builder);
  }

  private static SEXP __locate_firstlast_coll(SEXP str, SEXP pattern, SEXP opts_collator, ReplaceType replaces) {
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final int length = __recycling_rule(true, str, pattern);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    StringSearch matcher = null;
    final IntMatrixBuilder builder = new IntMatrixBuilder(length, 2);
    for (int i = 0; i < length; i++) {
      builder.set(i, 0, IntVector.NA);
      builder.set(i, 1, IntVector.NA);
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        /* nothing */
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        if (replaces.isFirst()) {
          final int beginIndex = matcher.first();
          if (beginIndex != StringSearch.DONE) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex + matcher.getMatchLength());
          }
        } else if (replaces.isLast()) {
          final int beginIndex = matcher.last();
          if (beginIndex != StringSearch.DONE) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex + matcher.getMatchLength());
          }
        }
      }
    }

    return __locate_set_dimnames_matrix(builder);
  }

  private static SEXP __locate_firstlast_fixed(SEXP str, SEXP pattern, SEXP opts_fixed, ReplaceType replaces) {
    final int flags = __fixed_flags(opts_fixed, false);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    final IntMatrixBuilder builder = new IntMatrixBuilder(length, 2);
    for (int i = 0; i < length; i++) {
      builder.set(i, 0, IntVector.NA);
      builder.set(i, 1, IntVector.NA);
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
      } else if (strings.getElementAsString(i).length() <= 0) {
        /* nothing */
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        final int patternLength = separatorPattern.length();
        final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
        final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
        if (replaces.isFirst()) {
          final int beginIndex = elementNormalized.indexOf(patternNormalized);
          if (-1 < beginIndex) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex + patternLength);
          }
        } else if (replaces.isLast()) {
          final int beginIndex = elementNormalized.lastIndexOf(patternNormalized);
          if (-1 < beginIndex) {
            builder.set(i, 0, beginIndex);
            builder.set(i, 1, beginIndex + patternLength);
          }
        }
      }
    }

    return __locate_set_dimnames_matrix(builder);
  }

  private static SEXP __locate_firstlast_regex(SEXP str, SEXP pattern, SEXP opts_regex, ReplaceType replaces) {
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    final IntMatrixBuilder builder = new IntMatrixBuilder(length, 2);
    for (int i = 0; i < length; i++) {
      builder.set(i, 0, IntVector.NA);
      builder.set(i, 1, IntVector.NA);
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
      } else {
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(strings.getElementAsString(i));
        if (replaces.isFirst()) {
          if (matcher.find()) {
            builder.set(i, 0, matcher.start());
            builder.set(i, 1, matcher.end());
          }
        } else if (replaces.isLast()) {
          while (matcher.find()) {
            builder.set(i, 0, matcher.start());
            builder.set(i, 1, matcher.end());
          }
        }
      }
    }

    return __locate_set_dimnames_matrix(builder);
  }

  private static IntVector __locate_set_dimnames_matrix(IntMatrixBuilder builder) {
    final StringVector names = new StringArrayVector(new String[] { "start", "end" });
    builder.setColNames(names);
    return builder.build();
  }

  private static SEXP __order_or_sort(SEXP str, SEXP decreasing, SEXP na_last, SEXP opts_collator, boolean order) {
    final boolean desc = ((AtomicVector) decreasing).getElementAsLogical(0).toBooleanStrict();
    final Logical lastNA = stri_prepare_arg_logical(na_last, "na_last").getElementAsLogical(0);
    final boolean isDefinedLastNA = !Logical.NA.equals(lastNA);
    final int length = str.length();
    final StringVector strings = stri_prepare_arg_string(str, "str");
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final Integer[] ordered = new Integer[length];
    final LinkedList<Integer> nas = new LinkedList<>();

    int notna = 0;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i)) {
        if (isDefinedLastNA) {
          nas.add(i);
        }
      } else {
        ordered[notna++] = i;
      }
    }
    Arrays.sort(ordered, new Comparator<Integer>() {
      @Override
      public int compare(Integer i1, Integer i2) {
        final int cmp = collator.compare(strings.getElementAsString(i1), strings.getElementAsString(i2));
        return desc ? -cmp : cmp;
      }
    });
    if (order) {
      final int[] result = new int[notna + nas.size()];
      int j = 0;
      if (Logical.FALSE.equals(lastNA)) {
        for (Integer index : nas) {
          result[j++] = 1 + index; // 1-based indices
        }
      }
      for (int k = 0; k < notna; k++) {
        result[j++] = 1 + ordered[k]; // 1-based indices
      }
      if (Logical.TRUE.equals(lastNA)) {
        for (Integer index : nas) {
          result[j++] = 1 + index; // 1-based indices
        }
      }
      return new IntArrayVector(result);
    } else {
      final String[] result = new String[notna + nas.size()];
      int j = 0;
      if (Logical.FALSE.equals(lastNA)) {
        for (Integer index : nas) {
          result[j++] = StringVector.NA;
        }
      }
      for (int k = 0; k < notna; k++) {
        result[j++] = strings.getElementAsString(ordered[k]);
      }
      if (Logical.TRUE.equals(lastNA)) {
        for (Integer index : nas) {
          result[j++] = StringVector.NA;
        }
      }

      return new StringArrayVector(result);
    }
  }

  private static SEXP __extract_firstlast_charclass(SEXP str, SEXP pattern, ReplaceType replaces) {
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    UnicodeSet matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i)) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (!separatorPattern.equals(lastPattern)) {
          lastPattern = separatorPattern;
          matcher = new UnicodeSet(separatorPattern);
        }
        if (replaces.isFirst()) {
          final int beginIndex = matcher.span(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
          if (-1 < beginIndex) {
            result[i] = element.substring(beginIndex, beginIndex + 1);
          } else {
            result[i] = StringVector.NA;
          }
        } else if (replaces.isLast()) {
          final int beginIndex = matcher.spanBack(element, UnicodeSet.SpanCondition.NOT_CONTAINED);
          if (-1 < beginIndex) {
            result[i] = element.substring(beginIndex - 1, beginIndex);
          } else {
            result[i] = StringVector.NA;
          }
        } else {
          result[i] = StringVector.NA;
        }
      }
    }

    return new StringArrayVector(result);
  }

  private static SEXP __extract_firstlast_coll(SEXP str, SEXP pattern, SEXP opts_collator, ReplaceType replaces) {
    final RuleBasedCollator collator = __open_collator(opts_collator);
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    String lastPattern = null;
    StringSearch matcher = null;
    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        if (separatorPattern.equals(lastPattern)) {
          matcher.setTarget(new StringCharacterIterator(element));
        } else {
          lastPattern = separatorPattern;
          matcher = new StringSearch(separatorPattern, new StringCharacterIterator(element), collator);
        }
        matcher.reset();
        if (replaces.isFirst()) {
          final int beginIndex = matcher.first();
          if (beginIndex == StringSearch.DONE) {
            result[i] = StringVector.NA;
          } else {
            result[i] = element.substring(beginIndex, beginIndex + matcher.getMatchLength());
          }
        } else if (replaces.isLast()) {
          final int beginIndex = matcher.last();
          if (beginIndex == StringSearch.DONE) {
            result[i] = StringVector.NA;
          } else {
            result[i] = element.substring(beginIndex, beginIndex + matcher.getMatchLength());
          }
        } else {
          result[i] = StringVector.NA;
        }
      }
    }

    return new StringArrayVector(result);
  }

  private static SEXP __extract_firstlast_fixed(SEXP str, SEXP pattern, SEXP opts_fixed, ReplaceType replaces) {
    final int flags = __fixed_flags(opts_fixed, true);
    final boolean is_insensitive = (flags & Pattern.CASE_INSENSITIVE) > 0;
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.NA;
      } else if (strings.getElementAsString(i).length() <= 0) {
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String separatorPattern = patterns.getElementAsString(i);
        final int patternLength = separatorPattern.length();
        final String patternNormalized = is_insensitive ? separatorPattern.toUpperCase() : separatorPattern;
        final String elementNormalized = is_insensitive ? element.toUpperCase() : element;
        if (replaces.isFirst()) {
          final int beginIndex = elementNormalized.indexOf(patternNormalized);
          if (-1 < beginIndex) {
            result[i] = element.substring(beginIndex, beginIndex + patternLength);
          } else {
            result[i] = StringVector.NA;
          }
        } else if (replaces.isLast()) {
          final int beginIndex = elementNormalized.lastIndexOf(patternNormalized);
          if (-1 < beginIndex) {
            result[i] = element.substring(beginIndex, beginIndex + patternLength);
          } else {
            result[i] = StringVector.NA;
          }
        } else {
          result[i] = StringVector.NA;
        }
      }
    }

    return new StringArrayVector(result);
  }

  private static SEXP __extract_firstlast_regex(SEXP str, SEXP pattern, SEXP opts_regex, ReplaceType replaces) {
    final int flags = __regex_flags(opts_regex);
    final int length = __recycling_rule(true, str, pattern);
    final String[] result = new String[length];
    final StringVector strings = __ensure_length(length, stri_prepare_arg_string(str, "str"));
    final StringVector patterns = __ensure_length(length, stri_prepare_arg_string(pattern, "pattern"));

    for (int i = 0; i < length; i++) {
      if (strings.isElementNA(i) || patterns.isElementNA(i) || patterns.getElementAsString(i).length() <= 0) {
        if (!patterns.isElementNA(i) && patterns.getElementAsString(i).length() <= 0) {
          Native.currentContext().warn("empty search patterns are not supported");
        }
        result[i] = StringVector.NA;
      } else {
        final String element = strings.getElementAsString(i);
        final String normalizedPattern = __binary_properties_to_Java(patterns.getElementAsString(i));
        final Matcher matcher = Pattern.compile(normalizedPattern, flags).matcher(element);
        boolean found = matcher.find();
        if (found) {
          if (replaces.isFirst()) {
            result[i] = element.substring(matcher.start(), matcher.end());
          } else if (replaces.isLast()) {
            int start = matcher.start();
            int end = matcher.end();
            while (matcher.find()) {
              start = matcher.start();
              end = matcher.end();
            }
            result[i] = element.substring(start, end);
          } else {
            result[i] = StringVector.NA;
          }
        } else {
          result[i] = StringVector.NA;
        }
      }
    }

    return new StringArrayVector(result);
  }

  private static SEXP __cmp_codepoints(SEXP s1, SEXP s2, boolean negate) {
    final StringVector e1 = stri_prepare_arg_string(s1, "e1");
    final StringVector e2 = stri_prepare_arg_string(s2, "e2");
    final int length = __recycling_rule(true, e1, e2);
    final Logical[] result = new Logical[length];

    for (int i = 0; i < length; i++) {
      if (e1.isElementNA(i) || e2.isElementNA(i)) {
        result[i] = Logical.NA;
      } else {
        final String element1 = e1.getElementAsString(i);
        final String element2 = e2.getElementAsString(i);
        result[i] = Logical.valueOf(negate ? !element1.equals(element2) : element1.equals(element2));
      }
    }

    return new LogicalArrayVector(result);
  }

  private static SEXP __subset_by_logical(StringVector strings, Logical[] which, int found) {
    final String[] result = new String[found];

    for (int i = 0, j = 0; i < found; j++) {
      if (Logical.NA.equals(which[j])) {
        result[i++] = StringVector.NA;
      } else if (Logical.TRUE.equals(which[j])) {
        result[i++] = strings.getElementAsString(j);
      }
    }

    return new StringArrayVector(result);
  }

  private static Range<Integer> __adjust_bounds(boolean uselengths, int beginIndex, int endIndex, String element) {
    if (uselengths) {
      endIndex = beginIndex + endIndex - 1;
      if (beginIndex < 0 && endIndex >= 0) {
        endIndex = -1;
      }
    }
    if (0 <= beginIndex) {
      beginIndex = element.offsetByCodePoints(0, beginIndex - 1);
    } else {
      beginIndex = element.offsetByCodePoints(element.length(), beginIndex);
    }
    if (0 <= endIndex) {
      endIndex = element.offsetByCodePoints(0, endIndex);
    } else {
      endIndex = element.offsetByCodePoints(element.length(), endIndex + 1);
    }

    return Range.closedOpen(beginIndex, endIndex);
  }

  private static Deque<Integer> __wrap_greedy(int nwords, int width_val, int[] widths_orig, int[] widths_trim, int add_1, int add_n) {
    final Deque<Integer> wrap_after = new LinkedList<>();
    int len = add_1 + widths_orig[0];
    for (int j = 1; j < nwords; j++) {
      if (width_val < len + widths_trim[j]) {
        len = add_n + widths_orig[j];
        wrap_after.addLast(j - 1);
      } else {
        len += widths_orig[j];
      }
    }

    return wrap_after;
  }

  private static Deque<Integer> __wrap_dynamic(int nwords, int width_val, double exponent_val, int[] widths_orig, int[] widths_trim, int add_1, int add_n) {
    final Deque<Integer> wrap_after = new LinkedList<>();
    // where cost[i][j] == cost of printing words i..j in a single line, i<=j
    final double[][] cost = new double[nwords][nwords];
    // calculate costs:
    // there is some "punishment" for leaving blanks at the end of each line
    // (number of "blank" codepoints ^ exponent_val)
    for (int i = 0; i < nwords; i++) {
      int sum = 0;
      for (int j = i; j < nwords; j++) {
        if (j > i) {
          if (cost[i][j - 1] < 0.0) { // already Inf
            cost[i][j] = -1.0; // Inf
            continue;
          } else {
            sum -= widths_trim[j - 1];
            sum += widths_orig[j - 1];
          }
        }
        sum += widths_trim[j];
        int ct = width_val - sum;
        if (i == 0)
          ct -= add_1;
        else
          ct -= add_n;

        if (j == nwords - 1) { // last line == cost 0
          if (j == i || ct >= 0)
            cost[i][j] = 0.0;
          else
            cost[i][j] = -1.0/* Inf */;
        } else if (j == i) {
          // some words don't fit in a line at all -> cost 0.0
          cost[i][j] = (ct < 0) ? 0.0 : Math.pow(ct, exponent_val);
        } else {
          cost[i][j] = (ct < 0) ? -1.0/* "Inf" */ : Math.pow(ct, exponent_val);
        }
      }
    }
    // f[j] == total cost of (optimally) printing words 0..j
    final double[] f = new double[nwords];
    // where[i][j] == false iff we don't wrap after i-th word, i<=j when (optimally) printing words 0..j
    final boolean[][] where = new boolean[nwords][nwords];
    for (int j = 0; j < nwords; ++j) {
      if (cost[0][j] >= 0.0) {
        // no breaking needed: words 0..j fit in one line
        f[j] = cost[0][j];
      } else {
        // let i = optimal way of printing of words 0..i + printing i+1..j
        int i = 0;
        while (i <= j) {
          if (cost[i + 1][j] >= 0.0) {
            break;
          }
          ++i;
        }
        double best_i = f[i] + cost[i + 1][j];
        for (int k = i + 1; k < j; ++k) {
          if (cost[k + 1][j] >= 0.0) {
            double best_cur = f[k] + cost[k + 1][j];
            if (best_cur < best_i) {
              best_i = best_cur;
              i = k;
            }
          }
        }
        for (int k = 0; k < i; ++k) {
          where[k][j] = where[k][i];
        }
        where[i][j] = true;
        f[j] = best_i;
      }
    }
    // result is in the last row of where...
    for (int k = 0; k < nwords; ++k) {
      if (where[k][nwords - 1]) {
        wrap_after.addLast(k);
      }
    }

    return wrap_after;
  }
}
