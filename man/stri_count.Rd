% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/search_count_4.R
\name{stri_count}
\alias{stri_count}
\alias{stri_count_charclass}
\alias{stri_count_coll}
\alias{stri_count_fixed}
\alias{stri_count_regex}
\title{Count the Number of Pattern Matches}
\usage{
stri_count(str, ..., regex, fixed, coll, charclass)

stri_count_charclass(str, pattern)

stri_count_coll(str, pattern, ..., opts_collator = NULL)

stri_count_fixed(str, pattern, ..., opts_fixed = NULL)

stri_count_regex(str, pattern, ..., opts_regex = NULL)
}
\arguments{
\item{str}{character vector with strings to search in}

\item{...}{supplementary arguments passed to the underlying functions,
including additional settings for \code{opts_collator}, \code{opts_regex},
\code{opts_fixed}, and so on}

\item{pattern, regex, fixed, coll, charclass}{character vector defining search patterns;
for more details refer to \link{stringi-search}}

\item{opts_collator, opts_fixed, opts_regex}{a named list used to tune up
a search engine's settings; see
\code{\link{stri_opts_collator}}, \code{\link{stri_opts_fixed}},
and \code{\link{stri_opts_regex}}, respectively; \code{NULL}
for default settings;}
}
\value{
All the functions return an integer vector.
}
\description{
These functions count the number of occurrences
of a pattern in a string.
}
\details{
Vectorized over \code{str} and \code{pattern}.

If \code{pattern} is empty, then the result is \code{NA}
and a warning is generated.

\code{stri_count} is a convenience function.
It calls either \code{stri_count_regex},
\code{stri_count_fixed}, \code{stri_count_coll},
or \code{stri_count_charclass}, depending on the argument used; relying
on one of those underlying functions will be faster.
}
\examples{


s1 <- "Lorem ipsum dolor sit amet, cOnsectetur adipisicing elit."
s2 <- c("Lorem ipsum d00olor sit amet, cOnsectetur adipisicing elit.",
        "Lorem ipsum dolor sit amet, cOnsectetur adipisicing elit.",
        "Lorem ipsum d1234ol'oöo¨r sit amet, cOnsectetur adipisicing elit.")
opts1 <- stri_opts_collator(strength = 1L, case_level = FALSE, normalization = FALSE, numeric = FALSE)
opts2 <- stri_opts_collator(strength = 2L, case_level = FALSE, normalization = FALSE, numeric = FALSE)
opts3 <- stri_opts_collator(strength = 3L, case_level = FALSE, normalization = FALSE, numeric = FALSE)
opts4 <- stri_opts_collator(strength = 4L, case_level = FALSE, normalization = FALSE, numeric = FALSE)

x1 = stri_count_coll(s1, pattern = "o", opts_collator = opts1)
x2 = stri_count_coll(s1, pattern = "o", opts_collator = opts2)
x3 = stri_count_coll(s1, pattern = "o", opts_collator = opts3)
x4 = stri_count_coll(s1, pattern = "o", opts_collator = opts4)

x1b = stri_count_coll(s2, pattern = "o", opts_collator = opts1)
x2b = stri_count_coll(s2, pattern = "o", opts_collator = opts2)
x3b = stri_count_coll(s2, pattern = "o", opts_collator = opts3)
x4b = stri_count_coll(s2, pattern = "o", opts_collator = opts4)


stopifnot(x1 == 4L)
stopifnot(x2 == 4L)
stopifnot(x3 == 3L)
stopifnot(x4 == 3L)

stopifnot(x1b == c(4L, 4L, 6L))
stopifnot(x2b == c(4L, 4L, 5L))
stopifnot(x3b == c(3L, 3L, 4L))
stopifnot(x4b == c(3L, 3L, 4L))




s <- "Lorem ipsum dolor sit amet, consectetur adipisicing elit."
stri_count(s, fixed="dolor")
stri_count(s, regex="\\\\p{L}+")

stri_count_fixed(s, " ")
stri_count_fixed(s, "o")
stri_count_fixed(s, "it")
stri_count_fixed(s, letters)
stri_count_fixed("babab", "b")
stri_count_fixed(c("stringi", "123"), "string")

stri_count_charclass(c("stRRRingi", "STrrrINGI", "123"),
   c("\\\\p{Ll}", "\\\\p{Lu}", "\\\\p{Zs}"))
stri_count_charclass(" \\t\\n", "\\\\p{WHITE_SPACE}") # white space - binary property
stri_count_charclass(" \\t\\n", "\\\\p{Z}") # whitespace - general category (note the difference)

stri_count_regex(s, "(s|el)it")
stri_count_regex(s, "i.i")
stri_count_regex(s, ".it")
stri_count_regex("bab baab baaab", c("b.*?b", "b.b"))
stri_count_regex(c("stringi", "123"), "^(s|1)")

}
\seealso{
Other search_count: \code{\link{stri_count_boundaries}},
  \code{\link{stringi-search}}
}
