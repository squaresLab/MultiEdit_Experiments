DOC = icse2021-multi-edit-bugs.tex

OTHERSOURCES := $(filter-out $DOC, $(wildcard *.tex))
PDFLATEX = pdflatex -synctex=1 --shell-escape
BIBTEX = bibtex
RERUNBIB = "No file.*\.bbl|Citation.*undefined"
TARDIR = $(DOC:.tex=-src)

.PHONY: pdf clean

pdf: $(DOC:.tex=.pdf)

all: pdf

%.pdf: %.tex $(DOC) $(OTHERSOURCES)
	${PDFLATEX} $<
	egrep -c $(RERUNBIB) $*.log && ($(BIBTEX) $*;$(PDFLATEX) $<; $(PDFLATEX) $<) ; true

clean:
	@\rm -f \
        $(DOC:.tex=.aux) \
        $(DOC:.tex=.log) \
        $(DOC:.tex=.out) \
        $(DOC:.tex=.dvi) \
        $(DOC:.tex=.pdf) \
        $(DOC:.tex=.ps)  \
        $(DOC:.tex=.bbl) \
        $(DOC:.tex=.synctex.gz) \
        $(DOC:.tex=.blg) \
				$(DOC:.tex=-src.tar.gz)

veryclean: clean
	@\rm -r -f *~ *.log *.pdf *.gnuplot *.fdb_latexmk *.fls *.upa *.upb auto *.gz

tar: pdf
	@test -d $(TARDIR) || mkdir $(TARDIR)
	@cp Makefile *.{tex,bib,bst,cls} $(TARDIR)
	@tar cz $(TARDIR) > $(TARDIR).tar.gz
	@rm -rf $(TARDIR)
