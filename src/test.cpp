/* This file is part of the 'stringi' library.
 *
 * Copyright 2013 Marek Gagolewski, Bartek Tartanus
 *
 * 'stringi' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 'stringi' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with 'stringi'. If not, see <http://www.gnu.org/licenses/>.
 */


#include "stringi.h"


/** dummy fun to measure the performance of .Call
 * 
 */
SEXP stri_test_returnasis(SEXP x)
{
#ifndef NDEBUG
   return x;  
#else
   Rf_error("This function is enabled only if NDEBUG is undef.");
   return R_NilValue;
#endif
}


/** Check R encoding marking *for testing only*
 *  This function should not be exported
 * 
 *  @param s character vector
 * 
 *  Results are printed on STDERR
 * 
 * @version 0.1 (Marek Gagolewski)
 */
SEXP stri_test_Rmark(SEXP s)
{
#ifndef NDEBUG
   s = stri_prepare_arg_string(s, "str");
   int ns = LENGTH(s);
   for (int i=0; i < ns; ++i) {
      cerr << "Element #" <<  i << ":";
      SEXP curs = STRING_ELT(s, i);
      if (curs == NA_STRING){
         cerr << "\tNA" << endl;
         continue;
      }
      //const char* string = CHAR(curs);
      cerr << "\tMARK_ASCII =" << (IS_ASCII(curs) > 0);
      cerr << "\tMARK_UTF8  =" << (IS_UTF8(curs) > 0);
      cerr << "\tMARK_LATIN1=" << (IS_LATIN1(curs) > 0);
      cerr << "\tMARK_BYTES =" << (IS_BYTES(curs) > 0);
      cerr << endl;
   }
   return R_NilValue;
#else
   Rf_error("This function is enabled only if NDEBUG is undef.");
   return R_NilValue;
#endif
}


/** for testing efficiency of StriContainerUTF16 [internal]
 * @param str character vector
 * @return R_NilValue
 */
SEXP stri_test_UnicodeContainer16(SEXP str)
{
#ifndef NDEBUG
   str = stri_prepare_arg_string(str, "str");
   STRI__ERROR_HANDLER_BEGIN
   StriContainerUTF16 ss(str, LENGTH(str));
   return R_NilValue;
   STRI__ERROR_HANDLER_END(;/* nothing special to be done on error */)
#else
   Rf_error("This function is enabled only if NDEBUG is undef.");
   return R_NilValue;
#endif
}


/** for testing efficiency of StriContainerUTF8  [internal]
 * @param str character vector
 * @return R_NilValue
 */
SEXP stri_test_UnicodeContainer8(SEXP str)
{
#ifndef NDEBUG
   str = stri_prepare_arg_string(str, "str");
   STRI__ERROR_HANDLER_BEGIN
   StriContainerUTF8 ss(str, LENGTH(str));
   return R_NilValue;
   STRI__ERROR_HANDLER_END(;/* nothing special to be done on error */)
#else
   Rf_error("This function is enabled only if NDEBUG is undef.");
   return R_NilValue;
#endif
}


